# ✅ PHASE 2 DAY 4 CHECKLIST - INTEGRATION EXPANSION

**Date:** March 11, 2026  
**Task:** Apply offline pattern to remaining data-modifying UseCases  
**Estimated Time:** 3-4 hours  
**Difficulty:** Low (pattern already proven)  
**Prerequisites:** Day 1-3 ✅ COMPLETE  

---

## 🎯 TODAY'S MISSION

Apply the **proven offline-first pattern** from Day 3 to all remaining UseCases that modify data.

**Pattern (from Day 3):**
```kotlin
val isOnline = ConnectivityHelper.isNetworkAvailable(context)
if (!isOnline) {
    // Queue operation
    offlineQueueService.queue*(operation)
    return Result.success(operationId)
} else {
    // Process directly
    repository.save*(data)
    return Result.success(actualId)
}
```

**Today:** Repeat this pattern for 5+ more UseCases.

---

## 📋 USECASES TO UPDATE (COPY-PASTE PATTERN)

### **UseCase 1: UpdateInvoiceUseCase** ✅

**File to modify:** `app/src/main/java/com/emul8r/bizap/domain/usecase/UpdateInvoiceUseCase.kt`

```kotlin
suspend operator fun invoke(invoice: Invoice): Result<Long> {
    return try {
        // Validation (existing)
        if (invoice.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invoice must have at least one line item"))
        }

        // 🔌 Check network connectivity
        val isOnline = ConnectivityHelper.isNetworkAvailable(context)
        
        if (!isOnline) {
            // 📝 OFFLINE: Queue the update
            Timber.i("📶 Offline detected. Queueing invoice update for sync.")
            val queuedId = offlineQueueService.queueUpdateInvoice(invoice)
            Timber.d("✅ Invoice update queued with operation ID: $queuedId")
            return Result.success(queuedId)
        }
        
        // 🌐 ONLINE: Update directly
        val result = repository.updateInvoice(invoice)
        
        if (result.isSuccess) {
            val invoiceId = result.getOrNull() ?: return result
            try {
                val invoiceEntity = InvoiceEntity(
                    id = invoiceId,
                    businessProfileId = invoice.businessProfileId,
                    customerId = invoice.customerId,
                    customerName = invoice.customerName,
                    totalAmount = invoice.totalAmount,
                    amountPaid = invoice.amountPaid,
                    status = invoice.status.toString(),
                    dueDate = invoice.dueDate?.toEpochMilli() ?: System.currentTimeMillis(),
                    createdAt = invoice.createdAt?.toEpochMilli() ?: System.currentTimeMillis(),
                    invoiceYear = invoice.createdAt?.year ?: 2026,
                    invoiceSequence = invoice.invoiceNumber?.substringAfterLast("-")?.toIntOrNull() ?: 0,
                    description = invoice.description
                )
                snapshotSyncHelper.syncAllSnapshots(invoiceEntity, invoice.businessProfileId)
                Timber.d("✅ Snapshots synced for updated invoice $invoiceId")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to sync snapshots, but invoice updated successfully")
            }
        }
        result
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to update invoice")
        Result.failure(e)
    }
}
```

**Constructor:**
```kotlin
class UpdateInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val snapshotSyncHelper: SnapshotSyncHelper,
    private val offlineQueueService: OfflineQueueService,
    private val context: Context
) {
    // ...
}
```

**Checklist:**
- [ ] Import ConnectivityHelper
- [ ] Inject OfflineQueueService and Context
- [ ] Add connectivity check
- [ ] Queue if offline
- [ ] Update directly if online
- [ ] Compiles without errors

---

### **UseCase 2: UpdateStatusUseCase** ✅

**File to modify:** `app/src/main/java/com/emul8r/bizap/domain/usecase/UpdateStatusUseCase.kt`

```kotlin
suspend operator fun invoke(
    invoiceId: Long,
    newStatus: InvoiceStatus,
    businessId: Long
): Result<Unit> {
    return try {
        // 🔌 Check network connectivity
        val isOnline = ConnectivityHelper.isNetworkAvailable(context)
        
        if (!isOnline) {
            // 📝 OFFLINE: Queue the status update
            Timber.i("📶 Offline detected. Queueing status update for sync.")
            offlineQueueService.queueStatusUpdate(invoiceId, newStatus, businessId)
            Timber.d("✅ Status update queued for invoice $invoiceId")
            return Result.success(Unit)
        }
        
        // 🌐 ONLINE: Update status directly
        repository.updateInvoiceStatus(invoiceId, newStatus)
        Timber.d("✅ Invoice status updated to: $newStatus")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to update status")
        Result.failure(e)
    }
}
```

**Constructor:**
```kotlin
class UpdateStatusUseCase @Inject constructor(
    private val repository: InvoiceRepository,
    private val offlineQueueService: OfflineQueueService,
    private val context: Context
) {
    // ...
}
```

**Note:** You may need to add `queueStatusUpdate()` method to OfflineQueueService if not already present.

**Checklist:**
- [ ] Import ConnectivityHelper
- [ ] Inject OfflineQueueService and Context
- [ ] Add connectivity check
- [ ] Queue if offline
- [ ] Update if online
- [ ] Compiles without errors

---

### **UseCase 3: UpdatePaymentUseCase (if exists)** ✅

**File to modify:** `app/src/main/java/com/emul8r/bizap/domain/usecase/UpdatePaymentUseCase.kt` (if it exists separately from RecordPaymentUseCase)

Same pattern as UpdateStatusUseCase:
```kotlin
val isOnline = ConnectivityHelper.isNetworkAvailable(context)
if (!isOnline) {
    offlineQueueService.queuePaymentUpdate(...)
    return Result.success(Unit)
} else {
    repository.updatePayment(...)
    return Result.success(Unit)
}
```

**Checklist:**
- [ ] Check if this UseCase exists
- [ ] If yes, apply same pattern
- [ ] If no, skip this step

---

### **UseCase 4-5+: Other Data-Modifying UseCases** ✅

Look for any other UseCases that modify data:
- CreateCustomerUseCase
- UpdateCustomerUseCase
- DeleteCustomerUseCase
- CreateLineItemUseCase
- Any others that write/update/delete data

**For each one:**
```kotlin
val isOnline = ConnectivityHelper.isNetworkAvailable(context)
if (!isOnline) {
    offlineQueueService.queue*(operation)
    return Result.success(operationId)
} else {
    repository.*(data)
    return Result.success(actualId)
}
```

**Checklist (for each):**
- [ ] Found UseCase
- [ ] Added connectivity check
- [ ] Queuing logic added
- [ ] Direct processing logic maintained
- [ ] Compiles without errors

---

## 🛠️ OPTIONAL: ADD MISSING QUEUE METHODS

If OfflineQueueService doesn't have these methods, add them:

**File:** `app/src/main/java/com/emul8r/bizap/data/local/offline/OfflineQueueService.kt`

```kotlin
/**
 * Queue a status update
 */
suspend fun queueStatusUpdate(invoiceId: Long, status: InvoiceStatus, businessId: Long): Long {
    return try {
        val operation = OfflineOperation(
            operationType = "UPDATE_STATUS",
            entityId = invoiceId,
            entityData = Json.encodeToString(mapOf("status" to status.toString())),
            businessProfileId = businessId,
            status = "PENDING"
        )
        
        val id = dao.insert(operation)
        cacheUpdateMutex.withLock {
            pendingCache.add(operation.copy(id = id))
            updateStateFlow(businessId)
        }
        
        Timber.d("📋 Queued UPDATE_STATUS: $id for invoice $invoiceId")
        id
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to queue status update")
        throw e
    }
}
```

**Checklist (if needed):**
- [ ] Check if queueStatusUpdate exists
- [ ] If not, add the method above
- [ ] Add any other missing queue methods
- [ ] Compiles without errors

---

## 📊 VERIFICATION CHECKLIST

After updating each UseCase:

```
For Each UseCase:
[✅] Imports added (ConnectivityHelper)
[✅] Dependencies injected (OfflineQueueService, Context)
[✅] Connectivity check added
[✅] Offline branch queues operation
[✅] Online branch processes directly
[✅] Both branches return Result<*> successfully
[✅] Error handling present
[✅] Logging added
[✅] No compiler errors
```

---

## 🔨 BUILD & TEST

After updating all UseCases:

```bash
# Clean and build
./gradlew clean compileDebugKotlin

# Run all tests
./gradlew testDebugUnitTest

# Expected: BUILD SUCCESSFUL, 295+ tests passing (same as Day 3)
```

**Checklist:**
- [ ] `./gradlew compileDebugKotlin` → SUCCESS
- [ ] `./gradlew testDebugUnitTest` → All tests pass
- [ ] No new compilation errors
- [ ] No regressions in existing tests

---

## 📝 COMMIT & PUSH

```bash
git add -A
git commit -m "Phase 2 Day 4: Integration Expansion - Offline Pattern Applied

Updated UseCases with offline-first pattern:
- UpdateInvoiceUseCase: Queues updates when offline
- UpdateStatusUseCase: Queues status changes when offline
- [Any other UseCases]: Applied same pattern

All UseCases now:
1. Check network connectivity
2. Queue operation if offline
3. Process directly if online
4. Return success either way

Tests: All passing (295+ total, no regressions)
Build: Clean compilation
Status: Ready for Day 5 (comprehensive testing)"

git push origin main
```

**Checklist:**
- [ ] All files staged
- [ ] Commit message clear
- [ ] Pushed to GitHub

---

## ⏱️ TIME BREAKDOWN

Assuming 5 UseCases to update:

- UpdateInvoiceUseCase: 15-20 min
- UpdateStatusUseCase: 10-15 min
- UpdatePaymentUseCase (if exists): 10-15 min
- Other UseCases (2-3 more): 30-40 min
- Build, test, commit: 20-30 min

**Total: 3-4 hours**

---

## 🎯 SUCCESS CRITERIA FOR DAY 4

```
Code:
[✅] 5+ UseCases updated with offline pattern
[✅] All connectivity checks in place
[✅] All queuing logic working
[✅] All direct processing working
[✅] All imports correct

Tests:
[✅] All 295+ tests passing
[✅] No regressions
[✅] Build clean

Coverage:
[✅] Every data-modifying operation covered
[✅] Every offline scenario handled
[✅] Every online scenario handled
[✅] Ready for comprehensive testing (Day 5)
```

---

## 🚀 WEEK 1 PROGRESS

```
Day 1: ✅ Database Layer
Day 2: ✅ Queue Service
Day 3: ✅ UseCase Integration (First Wave)
Day 4: ⏳ Integration Expansion (Today - Second Wave)
Day 5: ⏳ E2E Testing & Verification

By Friday: Week 1 Complete ✅
- All data operations are offline-ready ✅
- All operations queue properly ✅
- All operations tested ✅
- Ready for sync worker ✅
```

---

## 💡 KEY INSIGHT

**Days 1-3 were foundational.**
**Days 4-5 are expansion and verification.**

The hard architecture work is done.

Today is about **repetition** - applying the proven pattern to more UseCases.

Tomorrow is about **verification** - comprehensive testing to ensure everything works together.

---

## 📚 REFERENCE DOCUMENTS

- PHASE_2_DAY_3_COMPLETION_REPORT.md (what you just did)
- PHASE_2_IMPLEMENTATION_GUIDE.md (architecture overview)
- Day 3 code (pattern examples)

---

**Day 4 Status:** Ready to begin  
**Difficulty:** Low (copy-paste pattern)  
**Confidence:** 98% (pattern proven)  
**Next Milestone:** Day 5 comprehensive testing  


