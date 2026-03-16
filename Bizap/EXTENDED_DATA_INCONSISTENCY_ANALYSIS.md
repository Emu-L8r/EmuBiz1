# 🔍 COMPREHENSIVE DATA INCONSISTENCY ANALYSIS - EXTENDED
**Date:** March 16, 2026  
**Status:** Pre-Testing Analysis  
**Scope:** GUI1 & GUI2 Dashboard Data Integrity

---

## ✅ ORIGINAL 4 ISSUES (VERIFIED)

### **Issue #1: Month-to-Date (MTD) Logic Gap** 🔴
**File:** InvoiceDao.kt  
**Problem:** `Calendar.getInstance()` is called once when Flow is created, not reactively  
**Impact:** If user leaves app open past midnight/month boundary, data becomes stale  
**Severity:** HIGH - Silent failure, user unaware data is wrong

### **Issue #2: Status Inconsistency (PARTIAL Problem)** 🔴
**File:** InvoiceDao.kt & AnalyticsDao.kt  
**Problem:** 
- Revenue queries: Include `'PARTIALLY_PAID'` in status filter
- Count queries: Only look for `'PAID'` status
- Result: Dollar amounts include partials, counts don't = inflation in metrics

**Severity:** HIGH - Makes financial metrics unreliable

### **Issue #3: Date Parsing Fragility** 🔴
**File:** RevenueRepositoryImpl.kt, line 96-102  
**Problem:** 
```kotlin
try {
    DailyRevenuePoint(date = LocalDate.parse(dateString), ...)
} catch (e: Exception) {
    Timber.w(e, "Failed to parse date: $dateString")
    null  // Silently drops data
}
```
**Impact:** Data points silently disappear from charts with only a warning log

**Severity:** MEDIUM - Data loss without user notification

### **Issue #4: Direct Query vs. Snapshot Conflict** 🔴
**Files:** InvoiceDao.kt (direct) vs. AnalyticsDao.kt (snapshots)  
**Problem:** Two data sources with different update mechanisms  
**Current State:** 
- RevenueRepositoryImpl uses InvoiceDao (direct queries)
- Some systems use snapshot tables (unclear which GUI uses which)
- Snapshot tables only update when triggers fire
**Severity:** HIGH - Inconsistent data between GUIs

---

## 🆕 ADDITIONAL ISSUES DISCOVERED

### **Issue #5: Timezone Handling Inconsistency Across Queries** 🔴
**Severity:** HIGH  
**Files Affected:**
- `InvoiceDao.kt` - Uses `Calendar.getInstance()` (respects device timezone)
- `AnalyticsDao.kt` - Uses SQLite `DATE()` function (UTC-based, sometimes)
- `DateUtils.kt` - Uses `Locale.getDefault()` for formatting
- `LocalDateTimeTypeConverter.kt` - Uses ISO format (no timezone)

**Root Cause:** Multiple timezone strategies in different parts of codebase:

```kotlin
// InvoiceDao.kt - Device timezone
val calendar = Calendar.getInstance().apply { timeInMillis = today }
calendar.set(Calendar.DAY_OF_MONTH, 1)
val monthStartMillis = calendar.timeInMillis

// AnalyticsDao.kt - SQLite UTC (potential issue)
WHERE date >= CAST(strftime('%s', 'now', '-30 days') AS INTEGER) * 1000

// DateUtils.kt - Locale-based formatting
SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

// LocalDateTimeTypeConverter - No timezone info
DateTimeFormatter.ISO_LOCAL_DATE_TIME
```

**Problem:** If user switches devices or timezones:
- Device A (EST): Month starts at 2026-03-01 00:00:00 EST
- Device B (PST): Same moment = 2026-02-28 21:00:00 PST
- SQLite queries might see different "month start" depending on function used

**Impact:**
- MTD/YTD boundaries misaligned
- Date range queries return different results
- User sees different numbers on iPad vs. iPhone

**Recommendation:** Standardize to UTC for all database operations, convert to local time only in UI

---

### **Issue #6: PAID Status Query Ambiguity** 🔴
**Severity:** MEDIUM-HIGH  
**Files:** AnalyticsDao.kt, multiple queries

**Problem:** Inconsistent status filtering across analytics queries:

```kotlin
// observeAverageDaysToPayment - Only PAID
WHERE status = 'PAID'

// observeLastPaymentDate in CustomerRevenue - Only PAID  
WHERE status = 'PAID'

// observeTotalCollected - Only PAID
WHERE status = 'PAID'

// But InvoiceDao.observeLast30DaysRevenueTrend - Includes PARTIALLY_PAID
CASE WHEN status IN ('PAID', 'PARTIALLY_PAID') THEN amountPaid

// And InvoiceDao.observeMTDRevenue - Includes PARTIALLY_PAID
WHERE status IN ('PAID', 'PARTIALLY_PAID')
```

**Impact:** 
- AnalyticsDao shows one number for total collected (PAID only)
- InvoiceDao shows higher number (PAID + PARTIALLY_PAID)
- Dashboard displays inconsistent totals

**Current Behavior:** 
- GUI1 (RevenueRepositoryImpl using InvoiceDao): Includes partials
- GUI2 (unclear which source): May exclude or include partials differently

---

### **Issue #7: Outstanding Amount Status Filter Mismatch** 🔴
**Severity:** MEDIUM  
**Files:** InvoiceDao.kt, AnalyticsDao.kt

**Problem:** Different definitions of "outstanding":

```kotlin
// InvoiceDao.kt - observeOutstandingAmount
WHERE status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')
// Includes PARTIALLY_PAID as "outstanding"

// AnalyticsDao.kt - observeTotalOutstanding
WHERE status IN ('SENT', 'DRAFT', 'OVERDUE')
// Includes DRAFT but NOT PARTIALLY_PAID

// RevenueRepositoryImpl combines both...
```

**Impact:**
- Two different calculations for same metric
- If invoice is PARTIALLY_PAID, one says it's outstanding, other says it's not
- Creates accounting mismatch (total collected + total outstanding ≠ total invoiced)

**Example:**
```
Invoice: $1000
Paid: $300 (status = PARTIALLY_PAID)

InvoiceDao: 
  - Collected: $300 (from PAID status only)
  - Outstanding: $700 (from PARTIALLY_PAID + SENT/OVERDUE)
  - ✅ Matches: $300 + $700 = $1000

AnalyticsDao:
  - Collected: $300
  - Outstanding: $700 (SENT/DRAFT/OVERDUE, NOT PARTIALLY_PAID)
  - ❌ Doesn't match: $300 + $700 = $1000 (PARTIALLY_PAID is missing)
```

---

### **Issue #8: Null/Zero Handling Inconsistencies** 🟡
**Severity:** MEDIUM  
**Files:** Multiple DAO queries

**Problem:** COALESCE usage varies:

```kotlin
// InvoiceDao - Using COALESCE(SUM(...), 0)
COALESCE(SUM(amountPaid), 0) as mtdRevenue

// AnalyticsDao - Sometimes missing COALESCE
SUM(CASE WHEN status IN ('PAID', 'PARTIALLY_PAID') THEN amountPaid ELSE 0 END)
// If no matching rows: NULL (not 0)

// Some queries use CAST(SUM(...) AS REAL)
CAST(AVG(CAST((...) AS REAL)) AS DOUBLE)
// May return NULL instead of 0.0
```

**Impact:**
- Some endpoints return NULL for empty result sets
- Others return 0
- UI code might crash on NULL or display inconsistently
- Comparisons fail: `null != 0` in Kotlin, but `null == 0` in SQL

---

### **Issue #9: isActive Flag Not Consistently Applied** 🟡
**Severity:** MEDIUM  
**Files:** InvoiceDao.kt, AnalyticsDao.kt

**Problem:** Some queries filter by `isActive = 1`, others don't:

```kotlin
// InvoiceDao.observeLast30DaysRevenueTrend - HAS isActive filter
WHERE businessProfileId = :businessId
AND isActive = 1
GROUP BY DATE(...)

// InvoiceDao.observeAverageDaysToPayment - NO isActive filter
fun observeAverageDaysToPayment(businessId: Long)
// Missing: AND isActive = 1

// RevenueRepositoryImpl.calculatePaymentMetrics - HAS isActive filter
WHERE isActive = 1

// But some queries don't check isActive at all
```

**Impact:**
- "Deleted" (soft-deleted) invoices may be included in some metrics
- Different queries return different totals for same business
- User deletes invoice, it still appears in some dashboards

---

### **Issue #10: Date Range Boundary Conditions** 🟡
**Severity:** MEDIUM  
**Files:** InvoiceDao.kt convenience overloads

**Problem:** Inconsistent boundary handling:

```kotlin
// observeMTDRevenue
calendar.set(Calendar.DAY_OF_MONTH, 1)
calendar.set(Calendar.HOUR_OF_DAY, 0)
calendar.set(Calendar.MINUTE, 0)
calendar.set(Calendar.SECOND, 0)
calendar.set(Calendar.MILLISECOND, 0)
val monthStartMillis = calendar.timeInMillis
// Start: 2026-03-01 00:00:00.000

val today = System.currentTimeMillis()
// End: 2026-03-16 15:47:32.123 (current time)

// Query: date >= monthStartMillis AND date <= endDateMillis
// Issue: Does invoice at 23:59:59.999 on March 16 get included?
// Or only up to 15:47:32.123?
```

**Impact:**
- Invoices created in the last hour of the day might be excluded/included unpredictably
- User creates invoice at 11:58 PM, doesn't appear in today's MTD
- Creates "off-by-one" type errors

---

### **Issue #11: Multi-Currency Aggregation Without Conversion** 🟡
**Severity:** MEDIUM  
**Files:** RevenueRepositoryImpl.kt, InvoiceDao.kt

**Problem:** Queries sum amounts across different currencies:

```kotlin
// In observeLast30DaysRevenueTrend
SELECT 
    SUM(CASE WHEN status IN ('PAID', 'PARTIALLY_PAID') THEN amountPaid ELSE 0 END) as revenue,
    currencyCode
FROM invoices
// Sums amountPaid from INR, USD, EUR all together!

// Then calculateByCurrency groups by currencyCode
// But the SUM already includes all currencies mixed together
```

**Example:**
```
Invoice 1: $100 USD, PAID
Invoice 2: ₹100 INR, PAID

Query result: revenue = 200 (cents), currencyCode = ??? 
Which currency is the 200 for?
```

**Impact:**
- Currency breakdown is meaningless
- $100 USD + ₹100 INR = ₹200? Or $200? Or $1.36 USD?
- Dashboard shows incorrect totals for multi-currency businesses

---

### **Issue #12: DailyRevenueTrend Query Logic Error** 🔴
**Severity:** HIGH  
**File:** InvoiceDao.kt, lines 205-220

**Critical Bug in observeLast30DaysRevenueTrend:**

```kotlin
@Query("""
    SELECT 
        DATE(date/1000, 'unixepoch') as dateString,
        COALESCE(SUM(CASE WHEN status IN ('PAID', 'PARTIALLY_PAID') 
                          THEN amountPaid ELSE 0 END), 0) as revenue,
        COUNT(*) as invoiceCount,
        COUNT(CASE WHEN status = 'PAID' THEN 1 END) as paidCount,
        currencyCode
    FROM invoices
    WHERE businessProfileId = :businessId
    AND DATE(date/1000, 'unixepoch') >= date('now', '-30 days')
    GROUP BY dateString, currencyCode
    ORDER BY dateString DESC
""")
```

**The Problem:**
1. Groups by `dateString` AND `currencyCode` (2 dimensions)
2. But `invoiceCount` sums ALL invoices per day-currency combo
3. And `revenue` is PAID + PARTIALLY_PAID amounts
4. But `paidCount` is only PAID status
5. Result: `invoiceCount` may be more than `paidCount` (expected) but the mix is unclear

**Example:**
```
Date: 2026-03-16
Currency: USD
Invoices: 5 total (3 PAID, 1 PARTIALLY_PAID, 1 DRAFT)

Query returns:
- invoiceCount = 5 ✅
- paidCount = 3 (only PAID) ✅
- revenue = sum of PAID + PARTIALLY_PAID amounts ✅

But wait... which invoices are in "revenue"?
The 3 PAID ones + the 1 PARTIALLY_PAID one (4 total)
But invoiceCount = 5 includes the DRAFT too

So: invoiceCount (5) != paidCount (3) but also != revenue count (4)
Dashboard might show: "5 invoices, 3 paid" which is correct
But "revenue = sum of 4 invoices" - confusing!
```

---

### **Issue #13: Time Window Calculation Not Reactive** 🔴
**Severity:** MEDIUM  
**Files:** InvoiceDao.kt, all convenience overloads

**Problem:** Time windows calculated at Flow creation time, never re-evaluated:

```kotlin
fun observeMTDRevenue(businessId: Long): Flow<Long> {
    val today = System.currentTimeMillis()  // Captured NOW
    val calendar = Calendar.getInstance().apply { timeInMillis = today }
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val monthStartMillis = calendar.timeInMillis  // Fixed at this moment
    
    // If this Flow is observed for 2 hours, monthStartMillis never changes
    // Even if a new month starts, it still uses the old month!
    return observeMTDRevenue(businessId, monthStartMillis, today)
}
```

**Impact:**
- If app stays open across month boundary, MTD becomes stale
- User doesn't know data is old
- Only way to fix: Force app restart or manually refresh

**Real Scenario:**
```
11:59 PM March 31: User opens app
- MTD calculated from 2026-03-01 to 2026-03-31 23:59:00

12:01 AM April 1: Month changes!
- But Flow still using March month start
- Dashboard shows March MTD but it's now April!
- User confused
```

---

## 📊 ISSUE SEVERITY MATRIX

| Issue # | Title | Severity | Data Loss? | Silent? | GUI1 Impact | GUI2 Impact |
|---------|-------|----------|-----------|---------|------------|------------|
| 1 | MTD Stale Window | 🔴 HIGH | No | Yes | ✅ YES | ✅ YES |
| 2 | PARTIAL Status Mismatch | 🔴 HIGH | No | No | ✅ YES | ✅ YES |
| 3 | Date Parsing Failure | 🔴 HIGH | Yes | Yes | ✅ YES | ✅ YES |
| 4 | Two Data Sources | 🔴 HIGH | No | No | ✅ YES | ✅ YES |
| 5 | Timezone Inconsistency | 🔴 HIGH | No | No | ✅ YES | ✅ YES |
| 6 | PAID Ambiguity | 🔴 HIGH | No | No | ✅ YES | ✅ YES |
| 7 | Outstanding Mismatch | 🟡 MED-HIGH | No | No | ✅ YES | ✅ YES |
| 8 | Null/Zero Handling | 🟡 MED | Maybe | No | ✅ YES | ✅ YES |
| 9 | isActive Missing | 🟡 MED | No | No | ✅ YES | ✅ YES |
| 10 | Boundary Conditions | 🟡 MED | No | Yes | ✅ YES | ⚠️ MAYBE |
| 11 | Multi-Currency Sum | 🟡 MED | No | No | ✅ YES | ✅ YES |
| 12 | DailyTrend Logic Error | 🔴 HIGH | No | No | ✅ YES | ⚠️ UNCLEAR |
| 13 | Window Not Reactive | 🟡 MED | No | Yes | ✅ YES | ⚠️ UNCLEAR |

---

## 🔧 RECOMMENDED FIX PRIORITY

### **Phase 1 (Critical - This Week)**
1. **Issue #5:** Standardize timezone handling
2. **Issue #6:** Fix PAID/PARTIALLY_PAID inconsistency
3. **Issue #2:** Align status filters across all queries
4. **Issue #13:** Make time windows reactive with `debounce()`

### **Phase 2 (High - Next Week)**
5. **Issue #1:** Implement reactive month boundary detection
6. **Issue #4:** Consolidate data sources (choose one)
7. **Issue #7:** Fix outstanding amount calculation
8. **Issue #12:** Fix DailyRevenueTrend grouping logic

### **Phase 3 (Medium)**
9. **Issue #3:** Add better error handling + user notification
10. **Issue #8:** Standardize null/zero handling
11. **Issue #9:** Apply isActive consistently everywhere
12. **Issue #10:** Use `.dayStart` and `.dayEnd` helpers
13. **Issue #11:** Add currency conversion or separate by currency

---

## ✅ SUMMARY

**Original Issues:** 4  
**Additional Issues Found:** 9  
**Total Critical Issues:** 13  
**Data Integrity Risk:** VERY HIGH  
**Recommended Action:** Hold final testing until Issues #1-7 fixed

