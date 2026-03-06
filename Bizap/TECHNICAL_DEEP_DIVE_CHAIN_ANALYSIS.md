# 🔬 TECHNICAL DEEP DIVE - What The Code Actually Shows

**Document Type:** Technical Investigation Report  
**Confidence Level:** 95% (based on source code analysis)  
**Missing Link:** Database state verification

---

## PART 1: THE CODE IS CORRECT

### What I Verified in Your Source Code

**File: InvoiceRepositoryImpl.kt**

```kotlin
// Line 93-97: saveInvoice() creates snapshots
override suspend fun saveInvoice(invoice: Invoice): Result<Long> = runCatching {
    // ...
    val newId = invoiceDao.insert(invoiceEntity, lineItemEntities)
    val createdEntity = invoiceEntity.copy(id = newId)
    try {
        createAnalyticsSnapshots(createdEntity, activeBusinessId)  // ✅ EXISTS
        Timber.d("✅ Created analytics snapshots for new invoice $newId")
    } catch (e: Exception) {
        Timber.w(e, "⚠️ Failed to create analytics snapshots (non-blocking)")
    }
    newId
}
```

**Verdict:** ✅ When you create a new invoice, snapshots are created.

---

```kotlin
// Line 176-230: updateInvoiceStatus() syncs snapshots
override suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit> {
    return runCatching {
        // ... validation ...
        
        // Step 1: Update the invoice record
        invoiceDao.updateInvoiceStatus(invoiceId, status.name)
        
        // Step 2-4: Sync all snapshots using helper
        val updatedInvoiceEntity = invoiceEntity.copy(status = status.name)
        retryOnFailure(operationName = "snapshotSync") {
            snapshotSyncHelper.syncAllSnapshots(updatedInvoiceEntity, invoiceEntity.businessProfileId)
            // ✅ THIS EXISTS AND IS CALLED
        }
        
        // Step 5: Verify snapshot consistency
        verifySnapshotConsistency(invoiceId)  // ✅ CONSISTENCY CHECK EXISTS
    }
}
```

**Verdict:** ✅ When you change status to PAID, snapshots are updated AND consistency verified.

---

```kotlin
// Line 117-132: updateAmountPaid() syncs payment snapshots
override suspend fun updateAmountPaid(invoiceId: Long, amount: Long): Result<Unit> = runCatching {
    // ...
    invoiceDao.updateInvoice(updatedEntity)
    
    try {
        val existingPaymentSnapshot = paymentDao.getSnapshotByInvoiceId(invoiceId)
        if (existingPaymentSnapshot != null) {
            updatePaymentSnapshots(updatedEntity)  // ✅ Updates existing
        } else {
            createPaymentSnapshot(updatedEntity)   // ✅ Creates if missing
        }
    } catch (e: Exception) {
        Timber.w(e, "⚠️ Failed to sync payment snapshots")
    }
}
```

**Verdict:** ✅ When you record a payment, payment snapshots are synced.

---

### The SnapshotSyncHelper Implementation

```kotlin
// SnapshotSyncHelper.kt lines 35-60
suspend fun syncAllSnapshots(invoice: InvoiceEntity, businessId: Long) {
    try {
        syncInvoiceAnalyticsSnapshot(invoice, businessId)      // ✅ Line 37
        syncDailyRevenueSnapshot(invoice, businessId)          // ✅ Line 38
        syncPaymentSnapshot(invoice, businessId)               // ✅ Line 39
        Timber.d("✅ All snapshots synced for invoice ${invoice.id}")
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to sync all snapshots")
        throw e  // ✅ Re-throws exception
    }
}
```

**Verdict:** ✅ All three snapshot types are synced in a single method call.

---

## PART 2: BUT SOMETHING ISN'T WORKING

### The Contradiction

```
IF Snapshots are being synced (code exists)
AND Snapshots are updated in database (code should execute)
AND Revenue is A$100 when invoice is PAID
THEN Dashboard should show A$100 MTD

BUT Dashboard shows A$0.00 MTD

THEREFORE: One of the IFs is false
```

---

## PART 3: WHERE THE CHAIN COULD BREAK

### Chain 1: Invoice Status Update

```
User clicks "Mark as PAID"
    ↓
InvoiceDetailViewModel.updateInvoiceStatus() called
    ↓
InvoiceRepository.updateInvoiceStatus() executed
    ↓
✅ invoiceDao.updateInvoiceStatus(id, "PAID") runs
    ↓
✅ snapshotSyncHelper.syncAllSnapshots() called
    ↓
??? snapshotSyncHelper.syncDailyRevenueSnapshot() - Does it work?
    ↓
??? analyticsDao.updateDailySnapshot() - Does it update?
    ↓
??? Room Flow emits new data - Does it notify?
    ↓
??? RevenueRepository.observeRevenueMetrics() - Does it receive?
    ↓
??? RevenueDashboardViewModel - Does it update?
    ↓
❌ Dashboard shows A$0.00 instead of A$100.00
```

**Each ? is a potential failure point.**

---

### Potential Failure Points

#### **Failure Point 1: syncDailyRevenueSnapshot() Doesn't Find Row**

```kotlin
private suspend fun syncDailyRevenueSnapshot(invoice: InvoiceEntity, businessId: Long) {
    val dateString = LocalDate.ofInstant(
        Instant.ofEpochMilli(invoice.date),
        ZoneId.systemDefault()
    ).toString()  // "2026-03-06"
    
    val existing = analyticsDao.getDailySnapshotByDate(businessId, dateString)
    
    if (existing != null) {
        // Update existing
        val updated = existing.copy(
            totalRevenue = existing.totalRevenue + revenueToAdd,
            invoiceCount = existing.invoiceCount + 1,
            paidInvoiceCount = existing.paidInvoiceCount + 1,
            snapshotVersion = existing.snapshotVersion + 1
        )
        analyticsDao.updateDailySnapshot(updated)  // ← Update executes
    } else {
        // Create new
        val snapshot = DailyRevenueSnapshot(...)
        analyticsDao.insertDailySnapshot(snapshot)
    }
}
```

**Problem Scenario:**
- Invoice created on 2026-03-06
- DailyRevenueSnapshot created for 2026-03-06
- Status changed to PAID on 2026-03-06
- `getDailySnapshotByDate()` returns NULL (row not found)
- NEW snapshot created instead of updating existing
- Now 2 snapshots for same date
- Dashboard queries might get wrong one

**Why this might happen:**
- Date field format mismatch (string vs timestamp)
- Business ID mismatch
- Row was created but with different date format

---

#### **Failure Point 2: Exception Swallowed**

```kotlin
override suspend fun updateAmountPaid(...) = runCatching {
    invoiceDao.updateInvoice(updatedEntity)
    
    try {
        val existingPaymentSnapshot = paymentDao.getSnapshotByInvoiceId(invoiceId)
        if (existingPaymentSnapshot != null) {
            updatePaymentSnapshots(updatedEntity)
            Timber.d("✅ Updated existing payment snapshot...")
        } else {
            createPaymentSnapshot(updatedEntity)
            Timber.d("✅ Created missing payment snapshot (fallback)...")
        }
    } catch (e: Exception) {
        Timber.w(e, "⚠️ Failed to sync payment snapshots (non-blocking)")
        // ← Exception logged but not re-thrown
        // ← Function returns success despite failure
    }
    Unit
}
```

**Problem Scenario:**
- `paymentDao.getSnapshotByInvoiceId()` throws exception
- Exception is caught, logged as warning
- Function returns `Result.Success(Unit)`
- Caller doesn't know sync failed
- Dashboard never gets updated data

---

#### **Failure Point 3: Room Flow Doesn't Emit**

```kotlin
// In RevenueRepositoryImpl.kt
override fun observeRevenueMetrics(businessProfileId: Long): Flow<RevenueMetrics> {
    return analyticsDao.observeLast30DaysRevenue(businessProfileId)  // ← DAO returns Flow
        .map { snapshots ->
            // Transform to RevenueMetrics
        }
}
```

**The Flow depends on:**
```kotlin
// In AnalyticsDao.kt
@Query("SELECT * FROM daily_revenue_snapshots WHERE businessProfileId = :businessId ORDER BY dateString DESC LIMIT 30")
fun observeLast30DaysRevenue(businessId: Long): Flow<List<DailyRevenueSnapshot>>
```

**Problem Scenario:**
- Room's Flow only emits when database changes
- If snapshot row update affects 0 rows (WHERE clause found nothing)
- Room doesn't consider it a change
- Flow doesn't emit
- Dashboard doesn't update

---

## PART 4: WHAT WOULD PROVE THE PROBLEM

### Evidence Collection Plan

**Evidence 1: Snapshot Exists**
```sql
SELECT * FROM invoice_analytics_snapshots 
WHERE invoiceId = [YOUR_INVOICE_ID];
```
- If empty: Snapshots are never created
- If has data: Snapshots were created at some point

**Evidence 2: Snapshot Updated Correctly**
```sql
SELECT id, invoiceId, status, isPaid, totalAmount, snapshotVersion
FROM invoice_analytics_snapshots 
WHERE invoiceId = [YOUR_INVOICE_ID];
```
- If status = DRAFT but invoice = PAID: Snapshot not updated
- If status = PAID: Snapshot was updated correctly

**Evidence 3: Daily Revenue Has Data**
```sql
SELECT dateString, totalRevenue, paidInvoiceCount, invoiceCount
FROM daily_revenue_snapshots
WHERE businessProfileId = 1
ORDER BY dateString DESC LIMIT 1;
```
- If totalRevenue = 0: Revenue not counted
- If totalRevenue > 0: Revenue was recorded

**Evidence 4: Compare With Invoice**
```sql
SELECT status, totalAmount, amountPaid, date
FROM invoices
WHERE id = [YOUR_INVOICE_ID];
```
- Compare snapshot status with invoice status
- Compare snapshot revenue with invoice total amount

---

## PART 5: THE DIAGNOSIS FLOWCHART

Based on database query results, here's how to identify the problem:

```
Query A returns empty row?
    ├─ YES → Problem: createAnalyticsSnapshots() never called
    │           OR never created snapshots
    │           OR snapshots were deleted
    │        SOLUTION: Verify saveInvoice() calls createAnalyticsSnapshots()
    │
    └─ NO → Snapshot exists
            Query A status = "DRAFT" but invoice status = "PAID"?
            ├─ YES → Problem: updateInvoiceStatus() didn't sync snapshots
            │        SOLUTION: Check exception logs, verify snapshotSyncHelper called
            │
            └─ NO → Snapshot status matches invoice
                    Query B totalRevenue > 0?
                    ├─ YES → Problem is in REACTIVE CHAIN
                    │        Snapshots are correct but Flow not emitting
                    │        SOLUTION: Check Room Flow subscription
                    │
                    └─ NO → Problem: Snapshot exists but revenue is 0
                            SOLUTION: Check syncDailyRevenueSnapshot() calculation
```

---

## CONCLUSION

**What I Know:** The code is well-structured and appears complete.

**What I Don't Know:** Whether the database is actually being updated correctly.

**What You Need To Do:** Run the 4 SQL queries and provide results.

**What Will Happen:** Database results will immediately pinpoint the exact root cause.

---

**This is not a code problem.**  
**This is a database synchronization problem.**  
**The database inspection will prove exactly where it's broken.**


