# 🔍 DEEP DIVE ANALYSIS: Dashboard Update Issue

**Date:** March 6, 2026  
**Problem:** Dashboards not updating when invoice status changes (SENT → PAID/PARTIALLY_PAID)  
**Scope:** 7-layer analysis of potential causes

---

## 📊 CURRENT ARCHITECTURE OVERVIEW

```
Invoice Status Change
    ↓
InvoiceDetailViewModel.updateStatus()
    ↓
InvoiceRepository.updateInvoiceStatus()
    ↓
InvoiceDao.updateInvoiceStatus() [Updates invoices table]
    ↓
??? MISSING CONNECTION ???
    ↓
DailyRevenueSnapshot table [Not being updated]
    ↓
RevenueRepository.observeRevenueMetrics() [Queries snapshots, not invoices]
    ↓
RevenueDashboardViewModel [Reactive StateFlow]
    ↓
UI [Shows old data because snapshots weren't updated]
```

---

# 🎯 7 SEPARATE POTENTIAL CAUSES (Layer-by-Layer)

## **CAUSE #1: Invoice Status Updates Don't Trigger Snapshot Updates** ❌
**Layer:** Data Persistence (Database)  
**Severity:** 🔴 CRITICAL - Root Cause Likely

### Current Implementation:
```kotlin
// InvoiceRepositoryImpl.kt
override suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit> = runCatching {
    invoiceDao.updateInvoiceStatus(invoiceId, status.name)
    // ✅ Updates invoices table
    // ❌ MISSING: Does NOT update daily_revenue_snapshots table
}.also { result ->
    result.onFailure { e -> Timber.e(e, "Database operation failed") }
}
```

### The Problem:
- When you update an invoice from SENT → PAID:
  - ✅ `invoices` table is updated
  - ❌ `daily_revenue_snapshots` table is **NOT updated**
  - ❌ `invoice_analytics_snapshots` table is **NOT updated**
  - ❌ `invoice_payment_snapshots` table is **NOT updated**

### Why This Breaks Dashboards:
- RevenueDashboard queries `daily_revenue_snapshots` (NOT invoices table)
- The snapshots were created during **invoice creation** (via migration 24→25)
- When status changes, snapshots are never updated
- Dashboard shows stale snapshot data

### Evidence:
- Migration 24→25 backfills snapshots with initial data
- No code exists to update snapshots when status changes
- RevenueRepository calls `analyticsDao.observeLast30DaysRevenue()` which reads snapshots
- AnalyticsDao methods exist to **get** and **update** snapshots, but they're never called

### The Fix Required:
```kotlin
// MISSING CODE - Need to add to updateInvoiceStatus()
override suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit> = runCatching {
    invoiceDao.updateInvoiceStatus(invoiceId, status.name)
    
    // ✅ NEW: Update snapshots to reflect status change
    val invoiceEntity = invoiceDao.getInvoiceById(invoiceId)
    if (invoiceEntity != null) {
        updateAnalyticsSnapshots(invoiceEntity)  // ← MISSING METHOD CALL
    }
}
```

---

## **CAUSE #2: No Snapshot Update Logic After Status Changes** ❌
**Layer:** Business Logic (Repository)  
**Severity:** 🔴 CRITICAL

### Current State:
The InvoiceRepositoryImpl only has **5 DAO helper methods** added:
- `getInvoiceSnapshot()` ✅
- `updateInvoiceSnapshot()` ✅
- `getDailySnapshotByDate()` ✅
- `updateDailySnapshot()` ✅
- `insertDailySnapshot()` ✅

### Missing Logic:
There is **NO method** that:
1. Gets the updated invoice
2. Recalculates metrics
3. Updates the snapshots

### Example of What's Missing:
```kotlin
// THIS METHOD DOESN'T EXIST
private suspend fun updateAnalyticsSnapshots(invoice: InvoiceEntity) {
    // Recalculate which snapshot to update based on invoice status
    // Update the revenue amount if status changed to PAID/PARTIALLY_PAID
    // Update the payment status snapshot
    // This would trigger the reactive chain
}
```

### Why Dashboards Don't Update:
1. Status changes: SENT → PAID
2. Snapshots are never recalculated
3. Revenue snapshot still shows 0 revenue for that date
4. Dashboard queries snapshots → gets old data → shows no change

---

## **CAUSE #3: UpdateInvoiceStatus Missing Database Transaction** ❌
**Layer:** Data Access (DAO)  
**Severity:** 🟠 HIGH

### Current Issue:
```kotlin
// InvoiceRepositoryImpl.updateInvoiceStatus()
invoiceDao.updateInvoiceStatus(invoiceId, status.name)  // Only 1 DB operation
```

### The Problem:
- Updates `invoices` table (1 operation)
- Doesn't update `invoice_analytics_snapshots` table (missing operation)
- Doesn't update `invoice_payment_snapshots` table (missing operation)
- Doesn't update `daily_revenue_snapshots` table (missing operation)

### Data Inconsistency:
- `invoices.status` = "PAID"
- `daily_revenue_snapshots.totalRevenue` = 0 (never updated)
- Dashboard reads snapshots, not invoices
- Result: Dashboard shows old data even though database was updated

### Required Fix:
Need a transaction that:
1. Updates `invoices.status`
2. Updates `invoice_analytics_snapshots.status` and `isPaid`
3. Updates `invoice_payment_snapshots.paymentStatus`
4. Updates `daily_revenue_snapshots.totalRevenue`

---

## **CAUSE #4: No Reactive Chain from Invoice Updates to Snapshots** ❌
**Layer:** Reactive Architecture (Flow)  
**Severity:** 🔴 CRITICAL

### How It Should Work:
```
Invoice Status Update
    ↓ (database change)
InvoiceDao [Flow<Invoice>]
    ↓
InvoiceRepository.updateInvoiceStatus()
    ↓ (triggers recalculation)
AnalyticsRepository.updateSnapshots()
    ↓
AnalyticsDao [Flow<DailyRevenueSnapshot>]
    ↓ (emits new snapshots)
RevenueDashboardViewModel [StateFlow collects]
    ↓
UI recomposes with new data ✅
```

### Current Reality:
```
Invoice Status Update
    ↓ (database change)
InvoiceDao [Flow<Invoice>] ← Updated
    ↓
InvoiceRepository.updateInvoiceStatus()
    ↓ (NO RECALCULATION)
AnalyticsRepository ← Never called
    ↓
AnalyticsDao [Flow<DailyRevenueSnapshot>] ← Not emitting
    ↓ (stale data)
RevenueDashboardViewModel [StateFlow receives old data]
    ↓
UI shows old data ❌
```

### Why This Matters:
- RevenueDashboardViewModel is **reactive** and **correct**
- It's collecting from `analyticsDao.observeLast30DaysRevenue()`
- But that Flow **never emits new data** because snapshots are never updated
- The chain is broken at the repository level

---

## **CAUSE #5: Migration 24→25 Only Backfills Initial Data** ❌
**Layer:** Data Synchronization  
**Severity:** 🟠 HIGH

### What Migration Does:
```kotlin
// Migration 24→25 runs ONCE on app startup
database.execSQL("""
    INSERT INTO daily_revenue_snapshots (
        businessProfileId, dateString, dateMs, totalRevenue, ...
    )
    SELECT ... FROM invoices i
    WHERE NOT EXISTS (snapshot already exists)
""")
```

### The Limitation:
- ✅ Creates snapshots for existing invoices (one-time backfill)
- ❌ Only runs once on app startup
- ❌ Doesn't handle SUBSEQUENT status changes
- ❌ After migration, snapshots become stale immediately

### Timeline:
1. Migration 24→25 runs: Creates snapshots with status=SENT, revenue=0
2. App starts, dashboard shows A$0.00 ✅
3. User changes invoice status to PAID
4. Snapshots are never updated ❌
5. Dashboard still shows A$0.00 ❌

---

## **CAUSE #6: RevenueRepository Queries Snapshots Instead of Recalculating** ❌
**Layer:** Query Strategy  
**Severity:** 🟠 MEDIUM

### Current Approach:
```kotlin
// RevenueRepositoryImpl.kt
override fun observeRevenueMetrics(businessProfileId: Long): Flow<RevenueMetrics> {
    return analyticsDao.observeLast30DaysRevenue(businessProfileId)  // Reads snapshots
        .map { snapshots ->
            // Just calculates from snapshots, never touches invoices table
            RevenueMetrics(
                mtdRevenue = calculateMTD(snapshots),
                ytdRevenue = calculateYTD(snapshots),
                ...
            )
        }
}
```

### The Problem:
- Snapshots are **denormalized cache** of invoice data
- Designed for **performance** (not real-time accuracy)
- When snapshots go stale, dashboard is stale
- No fallback to recalculate from invoices table

### Why Snapshots Exist:
- ✅ Queries are fast (no aggregation needed)
- ✅ Dashboards are responsive
- ❌ Requires snapshots to be kept in sync with invoices

### The Trade-off:
- **Performance:** Query snapshots (fast)
- **Accuracy:** Snapshots must stay in sync (broken)

---

## **CAUSE #7: No Mechanism to Refresh Snapshots After Invoice Changes** ❌
**Layer:** Event Handling (UI Layer)  
**Severity:** 🟠 MEDIUM

### Current UI Flow:
```kotlin
// InvoiceDetailViewModel.updateStatus()
invoiceRepo.updateInvoiceStatus(invoiceId, status)
    .onSuccess {
        Timber.d("✅ Invoice status updated")
        _uiEvent.emit(UiEvent.ShowSnackbar("Status updated"))
        // ❌ MISSING: Notification to refresh dashboards/snapshots
    }
```

### What's Missing:
When a status changes:
1. ✅ Event is emitted to show snackbar
2. ❌ No event to notify dashboards
3. ❌ No event to trigger snapshot refresh
4. ❌ No mechanism to invalidate cache

### Expected Flow:
```kotlin
invoiceRepo.updateInvoiceStatus(invoiceId, status)
    .onSuccess {
        _uiEvent.emit(UiEvent.ShowSnackbar("Status updated"))
        // ✅ MISSING: Should emit this too
        _uiEvent.emit(UiEvent.InvalidateAnalyticsCache())  // Or similar
    }
```

### Dashboard Refresh Mechanism:
According to historical documents, dashboard screens have `refreshMetrics()` methods:
```kotlin
// RevenueDashboardScreen.kt (should have this)
LaunchedEffect(Unit) {
    viewModel.refreshMetrics()  // Reload from repository
}
```

But there's **no trigger** from invoice updates to call this.

---

## 📋 SUMMARY TABLE

| Cause # | Layer | Severity | Root Issue | Impact |
|---------|-------|----------|-----------|--------|
| 1 | Database | 🔴 CRITICAL | No snapshot update on status change | Snapshots stay stale |
| 2 | Business Logic | 🔴 CRITICAL | Missing snapshot update method | Metrics not recalculated |
| 3 | Data Access | 🟠 HIGH | Single table update only | Inconsistent data |
| 4 | Reactive | 🔴 CRITICAL | Broken Flow chain | No data emission |
| 5 | Sync | 🟠 HIGH | One-time backfill only | Snapshots become stale |
| 6 | Query | 🟠 MEDIUM | Cache-dependent queries | No fallback |
| 7 | Events | 🟠 MEDIUM | No refresh trigger | Dashboards never update |

---

## 🔗 THE CONNECTION CHAIN

These causes are **interconnected**:

```
Cause #1 (No update) 
    ↓
Causes #2 & #3 (No update logic)
    ↓
Cause #4 (Flow broken)
    ↓
Cause #5 (Stale snapshots)
    ↓
Cause #6 (Query from stale cache)
    ↓
Cause #7 (No refresh mechanism)
    ↓
DASHBOARD SHOWS OLD DATA ❌
```

---

## ✅ HOW TO FIX (Overview)

### Quick Fix (Temporary):
Add snapshot update call to `updateInvoiceStatus()`:
```kotlin
invoiceRepo.updateInvoiceStatus(invoiceId, status)
    .onSuccess {
        updateAnalyticsSnapshots(invoiceId)  // ← Add this
    }
```

### Proper Fix (Recommended):
1. Create `updateAnalyticsSnapshots()` method in InvoiceRepositoryImpl
2. Call it after every invoice change (status, amount paid, etc.)
3. Ensure it updates all 3 snapshot tables
4. Add fallback query to invoices table if needed
5. Add event to notify dashboard of cache invalidation

### Long-term Fix:
- Consider real-time aggregation instead of snapshots
- Or use database triggers to keep snapshots in sync
- Or implement automatic snapshot refresh on query

---

## 📌 KEY INSIGHT

**The reactive architecture is correct:**
- ✅ RevenueDashboardViewModel properly collects from Flow
- ✅ AnalyticsDao methods properly exist
- ✅ RevenueRepository properly queries snapshots

**The missing link is between invoice updates and snapshot updates.**

When you change an invoice status, the snapshots are **never updated**, so the dashboard's reactive chain has no new data to emit.

---

**Next Step:** Implement the missing snapshot update logic in InvoiceRepositoryImpl


