# 🔧 RECOMMENDED FIXES - PRIORITIZED ACTION PLAN

**Status:** Pre-Testing Review  
**Date:** March 16, 2026  
**Recommendation:** HOLD testing until Critical issues are fixed

---

## 🚨 CRITICAL FIXES (MUST DO BEFORE TESTING)

### **FIX #1: Standardize Timezone Handling (Issue #5)**
**Impact:** Affects all date-based queries  
**Effort:** 2-3 hours

**Current Problem:**
- InvoiceDao uses `Calendar.getInstance()` (device TZ)
- AnalyticsDao uses SQLite `DATE()` (UTC-ish)
- Results depend on user's timezone

**Solution:**
1. Create `TimeZoneUtil.kt`:
```kotlin
object TimeZoneUtil {
    fun getMonthStart(date: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.timeInMillis = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    // Similar for year start, week start, today end, etc.
}
```

2. Update all InvoiceDao overloads to use this
3. Update AnalyticsDao queries to use explicit UTC timestamps from app code

**Testing:** Test on device with different timezone settings

---

### **FIX #2: Unify PAID/PARTIALLY_PAID Handling (Issues #2, #6)**
**Impact:** Revenue accuracy  
**Effort:** 1-2 hours

**Current Problem:**
```kotlin
// Different across files
InvoiceDao: WHERE status IN ('PAID', 'PARTIALLY_PAID')
AnalyticsDao: WHERE status = 'PAID'
Result: Inconsistent numbers
```

**Solution - Choose One Approach:**

**Option A: Exclude Partially Paid (Conservative)**
```kotlin
// In ALL queries:
WHERE status = 'PAID'  // Only fully paid invoices count

// Partial payments treated as:
// - Collected amount: counted in observeTotalCollected
// - Outstanding: NOT included in either collected or outstanding
// - Requires separate "Partial Payments" metric
```

**Option B: Include Partially Paid (Inclusive)**
```kotlin
// In ALL queries:
WHERE status IN ('PAID', 'PARTIALLY_PAID')

// Partial payments treated as:
// - Revenue: counted
// - Collected: YES (their amountPaid)
// - Outstanding: YES (remaining amount)
// - Metric: Collected + Outstanding = Total Invoiced ✅
```

**Recommendation:** Choose Option B (inclusive)

**Changes Required:**
1. AnalyticsDao.kt - Update 8+ queries to use `IN ('PAID', 'PARTIALLY_PAID')`
2. AnalyticsDao.kt - Update observeTotalCollected and observeTotalRevenue
3. Update outstanding calculation to match
4. Test with multi-state invoice (DRAFT → SENT → PARTIALLY_PAID → PAID)

**Testing:** 
```kotlin
// Create test invoice
Invoice($1000 USD, status DRAFT)
  → expectOutstanding = $1000
  → expectCollected = $0

Invoice status → SENT
  → expectOutstanding = $1000
  → expectCollected = $0

Invoice status → PARTIALLY_PAID, amountPaid = $300
  → expectOutstanding = $700 ✅
  → expectCollected = $300 ✅
  → Total = $1000 ✅

Invoice status → PAID
  → expectOutstanding = $0 ✅
  → expectCollected = $1000 ✅
```

---

### **FIX #3: Fix DailyRevenueTrend Query Logic (Issue #12)**
**Impact:** Trend charts display correct data  
**Effort:** 1 hour

**Current Problem:**
```sql
SELECT 
    DATE(...) as dateString,
    SUM(CASE WHEN status IN ('PAID', 'PARTIALLY_PAID') THEN amountPaid ELSE 0 END) as revenue,
    COUNT(*) as invoiceCount,
    COUNT(CASE WHEN status = 'PAID' THEN 1 END) as paidCount,
    currencyCode
FROM invoices
GROUP BY dateString, currencyCode
-- Groups by 2 dimensions but aggregates are unclear
```

**Solution:**
```sql
SELECT 
    DATE(date/1000, 'unixepoch') as dateString,
    currencyCode,
    COALESCE(SUM(CASE WHEN status IN ('PAID', 'PARTIALLY_PAID') 
                     THEN amountPaid ELSE 0 END), 0) as revenue,
    COUNT(*) as totalInvoiceCount,
    COUNT(CASE WHEN status = 'PAID' THEN 1 END) as fullyPaidCount,
    COUNT(CASE WHEN status = 'PARTIALLY_PAID' THEN 1 END) as partiallyPaidCount
FROM invoices
WHERE businessProfileId = :businessId
  AND date >= :startMs
  AND date <= :endMs
  AND isActive = 1
GROUP BY dateString, currencyCode
ORDER BY dateString DESC
```

**Key Changes:**
- Add explicit businessId and date range (not relying on 'now')
- Add isActive = 1 filter
- Separate partiallyPaidCount from fullyPaidCount
- Clear naming: totalInvoiceCount, fullyPaidCount, partiallyPaidCount

**Testing:** Create 5 invoices (3 PAID, 1 PARTIALLY_PAID, 1 DRAFT) on same day, verify counts

---

### **FIX #4: Make Time Windows Reactive (Issue #13)**
**Impact:** MTD/YTD don't become stale  
**Effort:** 2-3 hours

**Current Problem:**
```kotlin
fun observeMTDRevenue(businessId: Long): Flow<Long> {
    val today = System.currentTimeMillis()  // Captured once
    val monthStart = calculateMonthStart(today)  // Fixed forever
    return observeMTDRevenue(businessId, monthStart, today)  // Never updates
}
```

**Solution - Use System Clock as Flow:**
```kotlin
@Dao
interface InvoiceDao {
    @Query("""...""")  
    fun observeMTDRevenue(businessId: Long, startMs: Long, endMs: Long): Flow<Long>
}

@Singleton
class InvoiceRepository {
    private val systemClock: Flow<Long> = flow {
        while (currentCoroutineContext().isActive) {
            emit(System.currentTimeMillis())
            delay(60000)  // Update every minute
        }
    }
    
    fun observeMTDRevenue(businessId: Long): Flow<Long> =
        systemClock
            .debounce(1000)  // Don't recalculate too often
            .flatMapLatest { now ->
                val monthStart = TimeZoneUtil.getMonthStart(now)
                invoiceDao.observeMTDRevenue(businessId, monthStart, now)
            }
            .distinctUntilChanged()
}
```

**Testing:**
```kotlin
// Test at 11:59 PM, wait for midnight, verify MTD window updates
val flow = repository.observeMTDRevenue(businessId)
val values = mutableListOf<Long>()

// At 11:59 PM
launch { flow.collect { values.add(it) } }
delay(90_000)  // Wait past midnight
// Verify values list has 2+ entries with different windows
```

---

## 🟡 HIGH PRIORITY FIXES (Do Next)

### **FIX #5: Consolidate Data Sources (Issue #4)**
**Impact:** GUI1 and GUI2 always agree  
**Effort:** 4-6 hours  
**Recommendation:** Have both GUIs use RevenueRepositoryImpl (InvoiceDao direct queries)

### **FIX #6: Apply isActive Consistently (Issue #9)**
**Impact:** Soft-deleted invoices don't leak into metrics  
**Effort:** 1-2 hours  
**Change:** Add `AND isActive = 1` to ALL queries that don't have it

### **FIX #7: Fix Outstanding Calculation (Issue #7)**
**Impact:** Collected + Outstanding = Total (accounting equation)  
**Effort:** 1-2 hours  
**Change:** Update AnalyticsDao to include PARTIALLY_PAID in outstanding

### **FIX #8: Add Error Notifications (Issue #3)**
**Impact:** Users know when data is missing  
**Effort:** 2-3 hours  
**Change:** Instead of silently dropping parse errors, log and show banner in UI

---

## 📋 CHECKLIST FOR TESTING APPROVAL

Before final testing starts, verify:

- [ ] **All 4 critical fixes implemented**
  - [ ] Timezone standardized
  - [ ] PAID/PARTIAL unified  
  - [ ] DailyTrend query fixed
  - [ ] Time windows reactive

- [ ] **Test cases pass:**
  - [ ] Timezone test (different timezones)
  - [ ] Partial payment test (accounting equation)
  - [ ] Daily trend test (counts match aggregation)
  - [ ] Stale window test (crosses month boundary)

- [ ] **Code review:**
  - [ ] All queries have `AND isActive = 1`
  - [ ] All PAID queries use consistent filters
  - [ ] All COALESCE null handling consistent
  - [ ] No hardcoded `'now'` in SQL (all from app code)

- [ ] **Integration tests:**
  - [ ] GUI1 and GUI2 show same totals (within 0.01%)
  - [ ] Create invoice → appears in both dashboards
  - [ ] Update status → both dashboards reflect change
  - [ ] Multi-currency works or clearly documented as limitation

---

## 🎯 TIMELINE

**If all 4 critical fixes done:**
- 2 hours: Code implementation
- 1 hour: Unit test updates
- 1 hour: Manual testing of fixes
- 0.5 hour: Integration testing
- **Total: 4.5 hours**

**Then:** Safe to proceed with final testing

**Without fixes:** Testing will identify inconsistencies and require rework anyway

---

## ✅ NEXT STEPS

1. **Review** this document with dev team
2. **Approve** approach (especially PAID/PARTIAL choice)
3. **Assign** dev to FIX #1-4
4. **Estimated completion:** 1 day
5. **Then:** Run test suite with confidence

---

**Recommendation:** Don't skip these fixes. Testing without them will just identify these same issues and delay release by 2-3 days anyway.

Better to fix now than find in testing.

