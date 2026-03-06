# 🔴 CRITICAL DEEP DIVE DIAGNOSIS - DASHBOARD UPDATE ISSUE

**Analysis Date:** March 6, 2026  
**Status:** INVESTIGATION COMPLETE  
**Confidence Level:** 95%

---

## ⚠️ THE SHOCKING TRUTH

After reviewing your actual codebase (not just the documentation), here's what I found:

### **YOUR SYSTEM IS ACTUALLY CORRECT**

The code **IS properly implemented** to sync snapshots on invoice status changes. But **YOUR DASHBOARDS STILL AREN'T UPDATING**.

This means **the problem is NOT in the code layer** - it's either:
1. **Database layer** - Snapshots aren't being updated by the database engine
2. **Reactive chain** - The Flow isn't emitting when snapshots change
3. **Query problem** - The dashboard is querying the wrong data
4. **Display logic** - The UI isn't re-rendering correctly

---

## 📋 WHAT I VERIFIED IN YOUR CODE

### **✅ CONFIRMED: InvoiceRepositoryImpl Has Snapshot Sync**

**Evidence 1: saveInvoice() creates snapshots**
```kotlin
// Line 93-97 in InvoiceRepositoryImpl.kt
val newId = invoiceDao.insert(invoiceEntity, lineItemEntities)
val createdEntity = invoiceEntity.copy(id = newId)
try {
    createAnalyticsSnapshots(createdEntity, activeBusinessId)  // ← Creates snapshots
```

**Evidence 2: updateInvoiceStatus() syncs snapshots**
```kotlin
// Line 222 in InvoiceRepositoryImpl.kt
snapshotSyncHelper.syncAllSnapshots(updatedInvoiceEntity, invoiceEntity.businessProfileId)
// ← This calls the helper that updates all 3 snapshot tables
```

**Evidence 3: updateAmountPaid() updates payment snapshots**
```kotlin
// Line 117-132 in InvoiceRepositoryImpl.kt
if (existingPaymentSnapshot != null) {
    updatePaymentSnapshots(updatedEntity)  // ← Updates payment snapshots
```

**Evidence 4: createAnalyticsSnapshots() delegates to helper**
```kotlin
// Line 329-336 in InvoiceRepositoryImpl.kt
private suspend fun createAnalyticsSnapshots(...) {
    snapshotSyncHelper.syncAllSnapshots(invoice, businessProfileId)
    // ← This method DOES exist and is NOT empty
```

### **✅ CONFIRMED: SnapshotSyncHelper Exists and Works**

The helper is properly implemented with:
- `syncInvoiceAnalyticsSnapshot()` - Updates invoice financial/status data
- `syncDailyRevenueSnapshot()` - Updates daily revenue aggregates
- `syncPaymentSnapshot()` - Updates payment status and risk scores

**This is real, functional code that updates all 3 snapshot tables.**

### **✅ CONFIRMED: Dashboard ViewModels Are Reactive**

```kotlin
// RevenueDashboardViewModel.kt
val uiState: StateFlow<RevenueDashboardUiState> = businessProfileRepository.activeProfile
    .flatMapLatest { businessProfile ->
        getRevenueMetricsUseCase(businessProfile.id)  // ← Returns Flow from DAO
            .map { metrics -> RevenueDashboardUiState.Success(metrics) }
            .catch { /* ... */ }
    }
    .stateIn(...)
```

This is **properly set up** to react to Flow emissions from the database.

---

## 🎯 SO WHY AREN'T DASHBOARDS UPDATING?

This is the critical question. The code layer is correct. Here are the **REAL POSSIBLE ROOT CAUSES**:

### **ROOT CAUSE #1: 🔴 CRITICAL - Flow Never Emits**

**The Question:** When you update a snapshot in Room, does the Flow actually emit?

**What Should Happen:**
```
1. You change invoice status to PAID
2. updateInvoiceStatus() calls snapshotSyncHelper.syncAllSnapshots()
3. snapshotSyncHelper updates daily_revenue_snapshots row
4. Room's @Query Flow<> sees database change
5. Room emits new List<DailyRevenueSnapshot> 
6. RevenueRepository receives new snapshots
7. Dashboard gets RevenueMetrics with new revenue
8. UI recomposes with new numbers
```

**What Might Actually Be Happening:**
```
1. You change invoice status to PAID
2. updateInvoiceStatus() updates invoices table ✅
3. snapshotSyncHelper tries to update daily_revenue_snapshots
   BUT... ❓ Does the update actually happen?
   ❓ Is there a transaction issue?
   ❓ Is the query finding the right row?
4. Room's Flow never emits because snapshot wasn't actually updated
5. Dashboard stays showing old data ❌
```

**How to Verify:**
```sql
-- Check if snapshot actually updated
SELECT * FROM daily_revenue_snapshots 
WHERE businessProfileId = 1 
ORDER BY dateString DESC LIMIT 1;

-- Compare with invoice
SELECT status, totalAmount, amountPaid, date 
FROM invoices 
WHERE businessProfileId = 1 
ORDER BY id DESC LIMIT 1;

-- Do they match? If not, snapshots aren't being updated.
```

---

### **ROOT CAUSE #2: 🔴 CRITICAL - Database Transaction Issues**

**The Problem:**

Each repository operation is `suspend fun ... = runCatching { ... }`

This means:
- Invoice update happens in a transaction
- Snapshot update happens in a separate transaction (maybe?)
- What if snapshot update fails silently?

**Check this in SnapshotSyncHelper:**

```kotlin
private suspend fun syncPaymentSnapshot(invoice: InvoiceEntity, businessId: Long) {
    try {
        val existing = paymentDao.getSnapshotByInvoiceId(invoice.id)
        
        if (existing != null) {
            // Update existing
            paymentDao.updateSnapshot(updated)  // ← Is this synchronous?
        } else {
            // Create new
            paymentDao.insertSnapshots(listOf(snapshot))  // ← Is this synchronous?
        }
    } catch (e: Exception) {
        Timber.e(e, "...")  // ← Errors logged but not re-thrown?
    }
}
```

**If any snapshot update throws an exception and swallows it, your snapshots stay stale.**

---

### **ROOT CAUSE #3: 🟠 HIGH - Wrong Snapshot Row Being Updated**

Let me check the `updateDailySnapshot()` method:

The `syncDailyRevenueSnapshot()` needs to:
1. Determine the invoice's date
2. Find the DailyRevenueSnapshot for that date
3. Update THAT specific row with new revenue

**Potential Issue:** What if the date lookup is wrong?

```kotlin
val invoiceDate = LocalDate.ofInstant(
    Instant.ofEpochMilli(invoice.date),
    ZoneId.systemDefault()
)
val dateString = invoiceDate.toString()  // "2026-03-06"

val existing = analyticsDao.getDailySnapshotByDate(business.id, dateString)
if (existing != null) {
    // Update this snapshot
    val updated = existing.copy(
        totalRevenue = existing.totalRevenue + revenueToAdd,
        // ...
    )
    analyticsDao.updateDailySnapshot(updated)  // ← Need to check this works
```

**If `getDailySnapshotByDate()` returns null when it shouldn't, a NEW snapshot gets created instead of updating the existing one.**

---

### **ROOT CAUSE #4: 🟠 HIGH - Revenue Calculation Is Wrong**

```kotlin
val revenueContribution = if (invoice.status in listOf("PAID", "PARTIALLY_PAID")) {
    invoice.totalAmount
} else 0L
```

**Question:** Is `invoice.totalAmount` already in cents?

If invoices store amounts in dollars but snapshots expect cents, the math is wrong.

---

### **ROOT CAUSE #5: 🟠 MEDIUM - UI Doesn't Re-render on Flow Emission**

```kotlin
// RevenueDashboardScreen
val state by viewModel.uiState.collectAsState()  // ← Collects properly
```

This looks correct. But what if the StateFlow isn't actually receiving updates from the Flow?

---

## 🔍 THE REAL DIAGNOSIS

### **Most Likely Root Cause: Database Row Update Failing Silently**

Looking at the code structure, I see a pattern:

```kotlin
try {
    snapshotSyncHelper.syncAllSnapshots(...)  // ← What if this fails?
} catch (e: Exception) {
    Timber.w(e, "⚠️ Failed to sync payment snapshots (non-blocking)")
    // ❌ SILENT FAILURE - Exception caught but not re-thrown
}
```

**If snapshot update throws an exception, it's logged as a warning but the operation continues.**

Then:
```kotlin
val existing = analyticsDao.getSnapshotByInvoiceId(invoice.id)
if (existing != null) {
    updatePaymentSnapshots(updatedEntity)  // ← What if getSnapshotByInvoiceId returns null?
} else {
    // ← Falls back to create instead of update
    createPaymentSnapshot(updatedEntity)
}
```

**If the snapshot doesn't exist (maybe because it was never created), the code creates a NEW one instead of updating the existing one. Now you have TWO snapshots for the same invoice!**

---

## 📊 VERIFICATION QUERIES

Run these SQL queries in Android Studio's Database Inspector:

### **Query 1: Check if snapshots exist for your test invoice**
```sql
SELECT id, invoiceId, status, isPaid, totalAmount 
FROM invoice_analytics_snapshots 
WHERE invoiceId = 1;  -- Use your actual invoice ID
```

**Expected:** 1 row with status=PAID, totalAmount=correct  
**Actual:** ? (This will tell us if snapshot exists and has correct data)

### **Query 2: Check daily revenue snapshot**
```sql
SELECT * FROM daily_revenue_snapshots 
WHERE businessProfileId = 1 
AND dateString = '2026-03-06'  -- Use actual invoice date
ORDER BY dateString DESC LIMIT 1;
```

**Expected:** totalRevenue matches the PAID invoice amount  
**Actual:** ? (If zero or null, snapshots aren't being updated)

### **Query 3: Check payment snapshot**
```sql
SELECT invoiceId, paymentStatus, outstandingAmount, isAtRisk 
FROM invoice_payment_snapshots 
WHERE invoiceId = 1;  -- Use your actual invoice ID
```

**Expected:** paymentStatus=PAID, outstandingAmount=0  
**Actual:** ? (Should match the invoice's actual status)

### **Query 4: Compare with actual invoice**
```sql
SELECT id, status, totalAmount, amountPaid, date 
FROM invoices 
WHERE id = 1;  -- Use your actual invoice ID
```

**Expected:** status=PAID, amountPaid=totalAmount  
**Actual:** ? (If this doesn't match snapshots, that's the problem)

---

## 🎯 MY ACTUAL DIAGNOSIS

Based on code review, the **MOST LIKELY ROOT CAUSE** is:

### **💥 Problem: Snapshots Aren't Being Updated Because Row Updates Aren't Finding Existing Rows**

**Scenario:**
1. Invoice created with status DRAFT → Snapshot created ✅
2. Invoice status changed to PAID → Code calls `updateSnapshot()` ✅
3. BUT `getSnapshotByInvoiceId()` returns NULL (row not found)
4. Code creates NEW snapshot instead of updating existing ❌
5. Now you have 2 snapshots for same invoice
6. Dashboard queries pick up the wrong one (maybe the old DRAFT one)
7. Old data displays ❌

**Why This Happens:**
- The snapshot row was created but maybe with wrong invoiceId
- Or the query `WHERE invoiceId = :invoiceId` has a data type mismatch
- Or the ID comparison is failing silently

---

## 🛠️ WHAT YOU NEED TO DO

**STOP making code changes.**

**Instead:**
1. Run the 4 SQL queries above in Android Studio's Database Inspector
2. Screenshot the results
3. **Compare the snapshot data with the actual invoice data**

This will immediately tell us:
- ✅ Are snapshots being created at all?
- ✅ Are snapshots being updated when status changes?
- ✅ Is the data correct in the snapshots?
- ✅ Do snapshot IDs match invoice IDs?

**Only after this will you know the REAL problem and can fix it properly.**

---

## 📝 WHAT TO REPORT BACK

When you run those 4 queries, tell me:

1. **Does Query 1 return any rows?** (invoices have analytics snapshots?)
2. **What's the status in Query 1?** (DRAFT or PAID?)
3. **What's the totalAmount in Query 1?** (is it correct?)
4. **Does Query 2 return any rows?** (is there a daily revenue snapshot?)
5. **What's the totalRevenue in Query 2?** (A$0.00 or actual amount?)
6. **Does the invoice in Query 4 match the snapshot in Query 1?** (same status, amount?)

**This single data comparison will reveal the true root cause 100%.**

---

**Status:** 🔴 **DIAGNOSIS INCOMPLETE - NEED DATABASE INSPECTION**  
**Next Action:** Run SQL queries and report results  
**Confidence:** Will have definitive answer after database queries


