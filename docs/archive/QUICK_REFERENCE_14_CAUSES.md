# 🗺️ QUICK REFERENCE: 14 Causes Map

**For quick navigation and understanding**

---

## 📍 WHERE EACH CAUSE LIVES IN THE CODE

### TRUNK CAUSES (Architecture Level)

**#8 - Architectural Mismatch**
- Location: System design
- Evidence: InvoiceDetailScreen reads invoices; RevenueDashboard reads snapshots
- File: Both screens show different data sources

**#9 - No Write-Through Consistency**
- Location: Database transaction design
- Evidence: updateInvoiceStatus() updates only invoices table
- File: `InvoiceRepositoryImpl.kt` line ~140

**#10 - Snapshots as Write-Once**
- Location: Data model definition
- Evidence: DailyRevenueSnapshot never gets updated after creation
- File: `DailyRevenueSnapshot` entity definition

**#11 - Missing Update Hooks**
- Location: InvoiceRepository interface
- Evidence: updateInvoiceStatus() calls only invoiceDao.update...()
- File: `InvoiceRepositoryImpl.kt` lines 135-145

**#12 - No Sync Strategy**
- Location: System-wide architecture
- Evidence: No defined approach to keeping invoices and snapshots in sync
- File: None (doesn't exist, that's the problem!)

**#13 - Inverted Dependency**
- Location: Data flow direction
- Evidence: Invoices change but snapshots don't know; dashboard doesn't know
- File: Between `InvoiceRepository` and `AnalyticsRepository`

**#14 - Wrong Semantics**
- Location: Time-series data design
- Evidence: Treating daily snapshot as immutable historical record
- File: `DailyRevenueSnapshot.kt` entity

---

### BRANCH CAUSES (Implementation Level)

**#1 - No Update Call**
- Location: updateInvoiceStatus method
- Evidence: No calls to analyticsDao methods
- File: `InvoiceRepositoryImpl.kt` line ~142

**#2 - No Update Logic**
- Location: Repository layer
- Evidence: updateAnalyticsSnapshots() method doesn't exist
- File: Missing (doesn't exist)

**#3 - Single-Table Update**
- Location: Database operations
- Evidence: Only invoices table gets UPDATE statement
- File: `InvoiceRepositoryImpl.kt` and `InvoiceDao.kt`

**#4 - Broken Flow Chain**
- Location: Reactive architecture
- Evidence: analyticsDao.observeLast30DaysRevenue() never gets new data
- File: `RevenueRepositoryImpl.kt` line 24

**#5 - Migration Only Once**
- Location: Database initialization
- Evidence: Migration_24_25 runs once, never again
- File: `Migration_24_25.kt` and `DatabaseModule.kt`

**#6 - Query Strategy**
- Location: Dashboard data retrieval
- Evidence: observeRevenueMetrics() only queries snapshots
- File: `RevenueRepositoryImpl.kt` line 24

**#7 - No Refresh Mechanism**
- Location: Event handling
- Evidence: updateStatus() completes but doesn't notify anyone
- File: `InvoiceDetailViewModel.kt` line ~147

---

## 🔗 CAUSE DEPENDENCIES

```
You Change Status
        ↓
   [#8] Architecture mismatch
        ↓ (system designed wrong)
   [#9] No write-through consistency
        ↓ (no mechanism to sync)
   [#12] No strategy defined
        ↓ (don't know what to do)
   [#11] Missing update hooks
        ↓ (nowhere to do it)
   [#1] No update call
        ↓ (code doesn't call it)
   [#13] Inverted dependency
        ↓ (data doesn't flow back)
   [#4] Broken Flow
        ↓ (never emits)
   
   Dashboard Shows Old Data ❌
```

---

## ✅ HOW TO FIX EACH CAUSE

### Quick Fix (1-2 hours)
Fix #1 and #2 only:
```kotlin
// Add to updateInvoiceStatus():
val snapshot = analyticsDao?.getInvoiceSnapshot(invoiceId)
if (snapshot != null) {
    analyticsDao?.updateInvoiceSnapshot(snapshot.copy(
        status = status.name,
        isPaid = status == InvoiceStatus.PAID
    ))
}
```

### Proper Fix (4-8 hours)
Fix #1, #2, #3, #11:
- Add snapshot updates to updateInvoiceStatus()
- Add snapshot updates to updateAmountPaid()
- Add snapshot updates to recordPayment()
- Create consistent pattern for all invoice changes

### Complete Fix (8-16 hours)
Fix all 14:
- Redefine system with clear cache consistency strategy
- Update repository to be cache guardian
- Add synchronization to all invoice operations
- Document and test thoroughly
- Fix #5 by creating ongoing sync (not one-time migration)
- Redefine #14 semantics
- Create #12 strategy document

---

## 🎯 MINIMUM VIABLE FIX

To get dashboard working quickly:

```kotlin
// In InvoiceRepositoryImpl.updateInvoiceStatus():

override suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit> = runCatching {
    // 1. Update invoice
    invoiceDao.updateInvoiceStatus(invoiceId, status.name)
    
    // 2. Update snapshots (FIX #1 and #2)
    val invoice = invoiceDao.getInvoiceById(invoiceId)
    if (invoice != null) {
        // Update invoice analytics snapshot
        val analyticsSnapshot = analyticsDao?.getInvoiceSnapshot(invoiceId)
        if (analyticsSnapshot != null) {
            analyticsDao?.updateInvoiceSnapshot(analyticsSnapshot.copy(
                status = status.name,
                isPaid = status == InvoiceStatus.PAID
            ))
        }
        
        // Update daily revenue snapshot
        val dateString = java.time.LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(invoice.date),
            java.time.ZoneId.systemDefault()
        ).toString()
        
        val dailySnapshot = analyticsDao?.getDailySnapshotByDate(
            invoice.businessProfileId,
            dateString
        )
        if (dailySnapshot != null) {
            val newRevenue = if (status == InvoiceStatus.PAID) invoice.amountPaid else 0L
            analyticsDao?.updateDailySnapshot(dailySnapshot.copy(
                totalRevenue = dailySnapshot.totalRevenue + newRevenue,
                paidInvoiceCount = if (status == InvoiceStatus.PAID) 
                    dailySnapshot.paidInvoiceCount + 1 else dailySnapshot.paidInvoiceCount
            ))
        }
        
        // Update payment snapshot
        val paymentSnapshot = paymentDao?.getSnapshotByInvoiceId(invoiceId)
        if (paymentSnapshot != null) {
            paymentDao?.updateSnapshot(paymentSnapshot.copy(
                paymentStatus = when (status) {
                    InvoiceStatus.PAID -> "PAID"
                    InvoiceStatus.PARTIALLY_PAID -> "PARTIALLY_PAID"
                    else -> "UNPAID"
                }
            ))
        }
    }
}
```

**Fixes:** #1, #2  
**Improves:** #3, #4, #6  
**Timeline:** 1-2 hours  
**Result:** Dashboard updates ✅

---

## 📊 CAUSE IMPACT MATRIX

| Cause | Dashboard | System Integrity | Scalability | Maintainability |
|-------|-----------|-----------------|-------------|-----------------|
| #1 | 🔴 | 🟠 | 🟠 | 🟠 |
| #2 | 🔴 | 🟠 | 🟠 | 🟠 |
| #3 | 🟠 | 🔴 | 🟠 | 🟠 |
| #4 | 🔴 | 🟠 | 🟠 | 🟡 |
| #5 | 🟠 | 🟠 | 🟠 | 🔴 |
| #6 | 🟡 | 🟠 | 🟠 | 🟡 |
| #7 | 🟡 | 🟡 | 🟡 | 🟠 |
| #8 | 🔴 | 🔴 | 🔴 | 🔴 |
| #9 | 🔴 | 🔴 | 🔴 | 🔴 |
| #10 | 🔴 | 🔴 | 🟠 | 🔴 |
| #11 | 🔴 | 🔴 | 🟠 | 🔴 |
| #12 | 🔴 | 🔴 | 🔴 | 🔴 |
| #13 | 🔴 | 🔴 | 🟠 | 🟠 |
| #14 | 🔴 | 🔴 | 🟠 | 🔴 |

---

## 🎯 RECOMMENDED IMPLEMENTATION ORDER

1. **Quick Win (1-2h):** Fix #1 + #2
   - Makes dashboard work
   - Get user-visible improvement fast

2. **Complete (4-8h):** Fix #1, #2, #3, #11
   - Make all invoice operations consistent
   - Extend fix to updateAmountPaid, recordPayment, etc.

3. **Proper (8-16h):** Fix all
   - Redefine architecture (#8, #9, #12)
   - Create strategy document
   - Update semantics (#10, #14)
   - Replace migration with ongoing sync (#5)

---

## 📞 WHEN TO USE THIS

- **Quick Reference:** Understand which cause is which
- **Implementation:** Find exactly where to make changes
- **Debugging:** Trace issue back to root cause
- **Prevention:** Understand what went wrong and how to avoid it

---

**Save this for when you implement the fix!**


