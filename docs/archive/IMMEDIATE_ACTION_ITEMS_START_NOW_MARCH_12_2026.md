# ✅ IMMEDIATE ACTION ITEMS - START NOW (March 12, 2026)

**Status:** Ready to execute Phase 0  
**Timeline:** This week  
**First Task:** Fix Dashboard $0.00 bug  

---

## 🎯 YOUR FIRST TASK (Next 2-3 Hours)

### **Investigate Dashboard Revenue Query**

1. **Open these files:**
   - `app/src/main/java/com/emul8r/bizap/data/local/InvoiceDao.kt`
   - `app/src/main/java/com/emul8r/bizap/domain/service/AccountingService.kt`
   - `app/src/main/java/com/emul8r/bizap/data/repository/RevenueRepositoryImpl.kt`

2. **Answer these questions:**
   - Which method is actually being called for dashboard MTD revenue?
     - `InvoiceDao.observeMTDRevenue()` ?
     - `AccountingService.observeMTDRevenue()` ?
   - What date range is being used?
   - Are timezone-aware SQL functions causing issues?

3. **Run this test on emulator:**
   ```
   - Create invoice: $100 (status: DRAFT)
   - Record payment: $100
   - Check GUI2 dashboard
   - Does it show $100 or $0.00?
   ```

4. **If it shows $0.00:**
   - Means the query is broken or data isn't persisting
   - Check logcat for Timber.d() logs from RevenueRepository
   - Share the log output

5. **Create a PR with diagnostics:**
   - Add logging to trace the issue
   - Don't fix yet, just log the values
   - Push to `investigation/dashboard-zero-revenue`

---

## 📝 WHAT TO LOOK FOR

### In InvoiceDao.observeMTDRevenue():
```kotlin
@Query("""
    SELECT 
        COALESCE(SUM(amountPaid), 0) as mtdRevenue
    FROM invoices
    WHERE businessProfileId = :businessId
    AND status IN ('PAID', 'PARTIALLY_PAID')
    AND DATE(date/1000, 'unixepoch') >= date('now', 'start of month')
    """)
fun observeMTDRevenue(businessId: Long): Flow<Long>
```

**Potential issues:**
- ❌ Timezone from 'now' might be UTC, not local
- ❌ 'PAID' status might not be set correctly
- ❌ Date filter might exclude month's invoices

### In AccountingService.observeMTDRevenue():
```kotlin
fun observeMTDRevenue(businessId: Long): Flow<Long> {
    val today = System.currentTimeMillis()
    val calendar = Calendar.getInstance().apply { timeInMillis = today }
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    // ... creates month start in device timezone
    
    return invoiceDaoV2.observeRevenueInDateRange(
        businessId = businessId,
        startDateMillis = monthStartMillis,
        endDateMillis = today,
        status = InvoiceStatus.PAID.name
    )
}
```

**This approach is safer** because it uses device timezone.

---

## 🔧 INVESTIGATION STEPS

### Step 1: Add Logging
```kotlin
// In RevenueRepositoryImpl.kt, add to observeRevenueMetrics():
Timber.d("RevenueRepository: Querying MTD revenue for business $businessProfileId")
Timber.d("RevenueRepository: MTD result = $mtd (cents), expected > 0 if PAID invoices exist")

if (mtd == 0L) {
    Timber.w("⚠️ RevenueRepository: MTD is zero, check if invoices have PAID status")
}
```

### Step 2: Verify Data in Database
```sql
-- Check invoices exist and have correct status
SELECT COUNT(*), status FROM invoices GROUP BY status;

-- Check specific invoice for debugging
SELECT id, totalAmount, amountPaid, status, date FROM invoices WHERE id = 1;

-- Check if snapshot data exists
SELECT COUNT(*) FROM daily_revenue_snapshots;
```

### Step 3: Test the Flow
```kotlin
// In a test or debug mode:
val mtdRevenue = revenueRepository.observeRevenueMetrics(businessId)
    .first()  // Get first emission
    
println("MTD Revenue = ${mtdRevenue.mtdRevenue}")

if (mtdRevenue.mtdRevenue == 0L) {
    // Problem: either no PAID invoices, or query is broken
}
```

---

## 📊 SUCCESS INDICATORS

✅ Dashboard shows correct amount (not $0.00)  
✅ Log shows MTD revenue > 0  
✅ Verified PAID invoices exist in database  

---

## 🚀 NEXT STEPS AFTER INVESTIGATION

**If dashboard is working:**
- Mark Bug #1 as ✅ FIXED
- Move to Bug #2 (Snapshot Sync)

**If dashboard is still broken:**
- Apply either:
  - **Fix A:** Use AccountingService approach instead of SQL
  - **Fix B:** Fix timezone in SQL query
- Test again

**Then move to Bug #2 and #3**

---

## 📋 RESOURCES

**See these documents:**
- `PHASE_0_IMPLEMENTATION_GUIDE_MARCH_12_2026.md` - Full implementation guide
- `APP_BUILD_AND_TEST_VERIFICATION_MARCH_12_2026.md` - Current test status
- `SUPERIOR_APPROACH_VALIDATE_THEN_SECURE_THEN_POLISH_MARCH_12_2026.md` - Strategy

---

**Ready?**

Open `app/src/main/java/com/emul8r/bizap/data/local/InvoiceDao.kt` now and start investigating. Let me know what you find!


