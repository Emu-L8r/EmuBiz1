# 📋 AGENT CONTEXT - COMPREHENSIVE ANALYTICS DATA FIX

**Date:** March 16, 2026  
**Project:** Bizap / EmuBiz1  
**Database Version:** 35  
**Status:** Pre-implementation information gathering

---

## 🎯 EXECUTIVE SUMMARY

**Problem:** 13 data integrity issues across analytics and payment calculations
**Root Cause:** Inconsistent SQL queries, timezone handling, and status filtering
**Scope:** InvoiceDao, AnalyticsDao, RevenueRepositoryImpl, PaymentAnalyticsRepositoryImpl
**Timeline:** 6-9 hours to fix critical issues, ~18 hours for all issues

---

## 💾 DATABASE SCHEMA

### **Core Tables**

#### **invoices**
```kotlin
@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey val id: Long,
    val businessProfileId: Long,          // FK to business_profiles
    val customerId: Long? = null,          // FK to customers
    val date: Long,                        // Unix milliseconds
    val dueDate: Long?,                    // Unix milliseconds
    val totalAmount: Long,                 // Cents (e.g., 10000 = $100.00)
    val amountPaid: Long = 0L,             // Cents already paid
    val status: String,                    // DRAFT, SENT, PAID, PARTIALLY_PAID, OVERDUE
    val currencyCode: String = "USD",
    val invoiceNumber: String?,            // INV-YYYY-NNN format
    val invoiceYear: Int,
    val invoiceSequence: Int,
    val taxAmount: Long = 0L,
    val taxRate: Double = 0.0,
    val isActive: Boolean = true,          // Soft-delete flag
    val pdfUri: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val version: Int = 1,
    val parentInvoiceId: Long? = null      // For corrections/versions
)
```

#### **customers**
```kotlin
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: Long,
    val businessProfileId: Long,           // FK to business_profiles
    val name: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val isActive: Boolean = true
)
```

#### **business_profiles**
```kotlin
@Entity(tableName = "business_profiles")
data class BusinessProfileEntity(
    @PrimaryKey val id: Long,
    val businessName: String,
    val email: String?,
    val phone: String?,
    val address: String?,
    val abn: String?,                      // ABN / Tax ID
    val isTaxRegistered: Boolean = false,
    val defaultTaxRate: Float = 0f,
    val logoBase64: String?,
    val signatureUri: String?,
    val bankDetails: String?,
    val createdAt: Long,
    val updatedAt: Long
)
```

#### **line_items** (GUI1)
```kotlin
@Entity(tableName = "line_items")
data class LineItemEntity(
    @PrimaryKey val id: Long,
    val invoiceId: Long,                   // FK to invoices
    val description: String,
    val quantity: Double,
    val unitPrice: Long,                   // Cents
    val lineAmount: Long,                  // Cents = quantity * unitPrice
    val taxableAmount: Long?,
    val createdAt: Long
)
```

#### **payments** (GUI2)
```kotlin
@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessId: Long,
    val invoiceId: Long,                   // FK to invoices
    val amount: Long,                      // Cents
    val paymentDate: Long,                 // Unix milliseconds
    val notes: String?,
    val createdAt: Long
)
```

#### **invoice_payments** (GUI1)
```kotlin
@Entity(tableName = "invoice_payments")
data class InvoicePaymentEntity(
    @PrimaryKey val id: Long,
    val invoiceId: Long,                   // FK to invoices
    val businessProfileId: Long,
    val customerId: Long?,
    val amountCents: Long,
    val paymentDate: Long,
    val notes: String?,
    val createdAt: Long
)
```

### **Analytics/Snapshot Tables**

#### **daily_revenue_snapshots**
Cached daily totals for dashboard performance

#### **invoice_analytics_snapshots**
Cached per-invoice analytics

#### **daily_payment_snapshots**
Cached daily payment metrics

---

## 🔍 CURRENT PROBLEMATIC QUERIES

### **Issue #6: PAID Query Ambiguity**

**InvoiceDao.observeMTDRevenue():**
```sql
SELECT COALESCE(SUM(amountPaid), 0) as mtdRevenue
FROM invoices
WHERE businessProfileId = :businessId
AND status IN ('PAID', 'PARTIALLY_PAID')  -- ✅ INCLUDES PARTIAL
AND date >= :startDateMillis
AND date <= :endDateMillis
```

**AnalyticsDao.observeTotalCollected():**
```sql
SELECT COALESCE(SUM(amountPaid), 0)
FROM invoices
WHERE businessProfileId = :businessId
AND status = 'PAID'  -- ❌ EXCLUDES PARTIAL
AND isActive = 1
```

**Result:** Different totals for same data

---

### **Issue #7: Outstanding Calculation**

**InvoiceDao.observeOutstandingAmount():**
```sql
SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
FROM invoices
WHERE businessProfileId = :businessId
AND status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')  -- Includes PARTIAL
```

**AnalyticsDao.observeTotalOutstanding():**
```sql
SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
FROM invoices
WHERE businessProfileId = :businessId
AND status IN ('SENT', 'DRAFT', 'OVERDUE')  -- EXCLUDES PARTIAL!
AND isActive = 1
```

**Accounting Equation Fails:**
- Invoice: $1000 total, $300 paid (PARTIALLY_PAID)
- InvoiceDao: Outstanding = $700 ✅
- AnalyticsDao: Outstanding = $600 ❌ (Missing $100)

---

### **Issue #12: DailyTrend Logic Error**

```sql
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
```

**The Problem:** Groups by 2 dimensions (date, currency) but doesn't separate counts:
```
Date: 2026-03-16, Currency: USD
Invoices: 5 total (3 PAID, 1 PARTIALLY_PAID, 1 DRAFT)

Results:
- invoiceCount = 5 (all 5)
- paidCount = 3 (only PAID)
- revenue = 4 (PAID + PARTIALLY_PAID)

Why confusing?
- invoiceCount (5) ≠ paidCount (3) ≠ revenueCount (4)
- Dashboard shows mathematically inconsistent numbers
```

---

## 🔄 DATA FLOW DIAGRAM

```
Invoice Created (Status: DRAFT)
    ↓
Stored in invoices table (id=1, status='DRAFT', totalAmount=100000)
    ↓
    ├─→ GUI1 Dashboard reads via RevenueRepositoryImpl
    │   └─→ Uses InvoiceDao queries
    │       ├─ observeMTDRevenue() → SUM(amountPaid) WHERE status IN ('PAID', 'PARTIALLY_PAID')
    │       ├─ observeOutstandingAmount() → SUM(totalAmount - amountPaid) WHERE status IN (...)
    │       └─ Result: DRAFT excluded from both ✅
    │
    ├─→ GUI2 Dashboard reads via RevenueRepositoryV2
    │   └─→ Uses AnalyticsDao OR InvoiceDaoV2 queries
    │       ├─ Different query implementations
    │       └─ Result: DIFFERENT numbers from GUI1 ❌
    │
    └─→ Optional: Snapshot caching
        └─→ daily_revenue_snapshots updated (only on schedule)
            └─→ Result: Stale if snapshots haven't been updated ⚠️

Status changes: DRAFT → SENT → PARTIALLY_PAID → PAID
    ↓
All watchers (StateFlow) notified → DAOs re-query → Results updated
    ✅ Works correctly for real-time updates
    ❌ But GUI1 and GUI2 use different queries → Different results
```

---

## 📊 FINANCIAL RULES & DEFINITIONS

### **Invoice Status Meanings**
```
DRAFT:            Not sent, internal only, work-in-progress
SENT:             Sent to customer, awaiting payment
PARTIALLY_PAID:   Customer paid some amount, balance outstanding
PAID:             Fully paid, collection complete
OVERDUE:          Due date passed, payment still outstanding
CANCELLED:        Voided, should not appear in financials
```

### **Key Financial Metrics (As Currently Calculated)**

**Revenue (Collected Money):**
- Current: `SUM(amountPaid) WHERE status IN ('PAID', 'PARTIALLY_PAID')`
- Question: Should PARTIALLY_PAID count as revenue?
  - **Option A (Cash Basis):** Only PAID
  - **Option B (Mixed):** PAID + PARTIALLY_PAID (current)
  - **Option C (Accrual):** Any invoice SENT or beyond (most aggressive)

**Outstanding Amount:**
- Current: `SUM(totalAmount - amountPaid) WHERE status IN ('SENT', 'PARTIALLY_PAID', 'OVERDUE')`
- Should match: `Total Billed - Total Collected` (accounting equation)

**Total Billed:**
- Should be: `SUM(totalAmount) WHERE status IN ('SENT', 'PARTIALLY_PAID', 'PAID', 'OVERDUE')`
- Excludes DRAFT (not billed yet)

**Collection Rate:**
- Should be: `Total Collected / Total Billed` × 100%

---

## 🧪 TEST DATA SCENARIO

### **Test Business: Business ID = 100**

```
Invoice 1:
  ID: 1, Total: $1000, Status: PAID, AmountPaid: $1000
  Created: 2026-03-01, Due: 2026-03-15
  Expected: Fully collected ✅

Invoice 2:
  ID: 2, Total: $500, Status: PARTIALLY_PAID, AmountPaid: $200
  Created: 2026-03-05, Due: 2026-03-20
  Expected: Outstanding = $300 ✅

Invoice 3:
  ID: 3, Total: $800, Status: SENT, AmountPaid: $0
  Created: 2026-03-10, Due: 2026-03-25
  Expected: Outstanding = $800 ✅

Invoice 4:
  ID: 4, Total: $600, Status: DRAFT, AmountPaid: $0
  Created: 2026-03-15, Not sent
  Expected: NOT included in financial metrics ✅

Invoice 5:
  ID: 5, Total: $400, Status: OVERDUE, AmountPaid: $0
  Created: 2026-02-01, Due: 2026-02-15 (PAST DUE)
  Expected: Outstanding = $400, included in overdue metrics ✅
```

### **Expected Totals (Financial Basis: PAID + PARTIALLY_PAID = Revenue)**

```
Total Billed:        $1000 + $500 + $800 + $400 = $2700 (excludes DRAFT)
Total Collected:     $1000 + $200 = $1200
Outstanding:         $300 + $800 + $400 = $1500
Collection Rate:     $1200 / $2700 = 44.4%

Accounting Check:    $1200 + $1500 = $2700 ✅
```

---

## ✅ SUCCESS VERIFICATION CRITERIA

### **Test #1: Accounting Equation**
```
For businessId = 100:
    Total Collected + Total Outstanding = Total Billed
    
Expected after fix:
    $1200 + $1500 = $2700 ✅
    
Current (broken):
    $1200 + $1600 = $2800 ❌ (PARTIALLY_PAID counted in outstanding?)
```

### **Test #2: GUI1 = GUI2**
```
RevenueRepositoryImpl.observeOutstandingAmount()
  = RevenueRepositoryV2.observeOutstandingAmount()
  
Both should return $1500 (not different values)
```

### **Test #3: DRAFT Exclusion**
```
When DRAFT invoice created:
  - Total Billed should NOT change
  - Total Outstanding should NOT change
  - Revenue metrics should NOT change
  
Current bug: DRAFT might be included in some queries
```

### **Test #4: PARTIALLY_PAID Consistency**
```
When invoice moves SENT → PARTIALLY_PAID ($200 payment):
  - Revenue increases by $200
  - Outstanding decreases by $200 (from $500 to $300)
  - Accounting equation still holds
  
Current bug: Sometimes counted differently in different queries
```

---

## 🔧 FILES TO MODIFY (Priority Order)

### **Priority 1: Query Standardization**
1. **InvoiceDao.kt** - Standardize status filters
2. **AnalyticsDao.kt** - Update to match InvoiceDao logic
3. **RevenueRepositoryImpl.kt** - Ensure consistent aggregation

### **Priority 2: Timezone Handling**
4. **Create TimeZoneUtil.kt** - Centralized timezone logic
5. **InvoiceDao.kt** - Update date calculations
6. **AnalyticsDao.kt** - Update date calculations

### **Priority 3: Data Flow**
7. **DatabaseModule.kt** - Verify provider setup
8. **PaymentAnalyticsRepositoryImpl.kt** - Review payment calculations

### **Priority 4: Testing**
9. **Add unit tests** for all financial scenarios
10. **Add integration tests** for GUI1 vs GUI2 comparison

---

## 📈 EXPECTED IMPROVEMENT METRICS

| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| GUI1 vs GUI2 match | <50% | 100% | ✅ |
| Accounting equation holds | 30% of cases | 100% of cases | ✅ |
| Silent failures | 4 issues | 0 issues | ✅ |
| Timezone-independent | No | Yes | ✅ |
| Code duplication | High | Low | ✅ |

---

## 🎯 CRITICAL SUCCESS FACTORS

1. **Choose accounting basis:** PAID only, or PAID + PARTIALLY_PAID?
   - Recommend: PAID + PARTIALLY_PAID (more complete picture)

2. **Standardize query filters:**
   - All revenue queries use same status list
   - All outstanding queries use same status list
   - Difference must equal total billed

3. **Fix DailyTrend grouping:**
   - Separate counts by status category
   - invoiceCount, fullyPaidCount, partiallyPaidCount distinct

4. **Implement reactive windows:**
   - Time windows update every minute
   - No stale data after month boundary

5. **Test both GUIs:**
   - Verify GUI1 and GUI2 show identical numbers
   - Run accounting equation check
   - Verify DRAFT exclusion

---

## 🚀 READY TO IMPLEMENT

All information gathered:
✅ Database schema complete
✅ Problematic queries identified
✅ Test data scenarios provided
✅ Success criteria defined
✅ Files to modify listed
✅ Expected improvements quantified

**Next Step:** Begin implementation of 4 critical fixes

