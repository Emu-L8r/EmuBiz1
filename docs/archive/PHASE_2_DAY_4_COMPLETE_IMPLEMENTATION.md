# ✅ PHASE 2 DAY 4 EXPANSION - COMPLETE IMPLEMENTATION GUIDE

**Date:** March 11, 2026  
**Phase 2 Status:** 40% Complete (Day 3 Finished) ✅  
**Today's Mission:** Complete offline integration for ALL data-modifying operations  
**Estimated Time:** 3-4 hours  
**Difficulty:** Low (proven pattern, copy-paste implementation)  

---

## 🎯 DAY 4 COMPLETE MISSION

Extend the offline-first pattern to all remaining UseCases that modify invoice/customer data:

**What You'll Complete:**
- ✅ UpdateInvoiceUseCase (edit invoice details)
- ✅ UpdateStatusUseCase (change invoice status)
- ✅ UpdatePaymentUseCase (if separate from RecordPayment)
- ✅ CreateCustomerUseCase (add customers offline)
- ✅ UpdateCustomerUseCase (edit customers offline)
- ✅ DeleteCustomerUseCase (delete customers offline)
- ✅ Any other data-modifying UseCases

**Result:** Every user action that modifies data is now fully offline-ready ✅

---

## 🏗️ THE PROVEN PATTERN (From Day 3)

You've already verified this works. Day 4 is repetition:

```kotlin
// The offline-first pattern:
suspend operator fun invoke(...): Result<*> {
    return try {
        // 1. Check connectivity
        val isOnline = ConnectivityHelper.isNetworkAvailable(context)
        
        if (!isOnline) {
            // 2. OFFLINE: Queue operation
            val queueId = offlineQueueService.queue*(operation)
            return Result.success(queueId)
        }
        
        // 3. ONLINE: Process directly
        val result = repository.operation*(data)
        return result
    } catch (e: Exception) {
        return Result.failure(e)
    }
}
```

**That's it. Apply this to each UseCase.**

---

## 📋 STEP-BY-STEP IMPLEMENTATION

### **Step 1: UpdateInvoiceUseCase** ✅

**File:** `app/src/main/java/com/emul8r/bizap/domain/usecase/UpdateInvoiceUseCase.kt`

```kotlin
suspend operator fun invoke(invoice: Invoice): Result<Long> {
    return try {
        // Validation
        if (invoice.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invoice must have at least one line item"))
        }

        // 🔌 Check connectivity
        val isOnline = ConnectivityHelper.isNetworkAvailable(context)
        
        if (!isOnline) {
            // 📝 OFFLINE: Queue the update
            Timber.i("📶 Offline detected. Queueing invoice update for sync.")
            val queuedId = offlineQueueService.queueUpdateInvoice(invoice)
            return Result.success(queuedId)
        }
        
        // 🌐 ONLINE: Update directly
        val result = repository.updateInvoice(invoice)
        
        if (result.isSuccess) {
            val invoiceId = result.getOrNull() ?: return result
            try {
                // Sync snapshots
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
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to sync snapshots")
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
)
```

**Checklist:**
- [ ] Import ConnectivityHelper
- [ ] Inject dependencies
- [ ] Add connectivity check
- [ ] Queue if offline
- [ ] Update if online
- [ ] Compiles without errors

---

### **Step 2: UpdateStatusUseCase** ✅

**File:** `app/src/main/java/com/emul8r/bizap/domain/usecase/UpdateStatusUseCase.kt`

```kotlin
suspend operator fun invoke(
    invoiceId: Long,
    newStatus: InvoiceStatus,
    businessId: Long
): Result<Unit> {
    return try {
        // 🔌 Check connectivity
        val isOnline = ConnectivityHelper.isNetworkAvailable(context)
        
        if (!isOnline) {
            // 📝 OFFLINE: Queue the status update
            Timber.i("📶 Offline detected. Queueing status update for sync.")
            offlineQueueService.queueStatusUpdate(invoiceId, newStatus.toString(), businessId)
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
)
```

**Checklist:**
- [ ] Import ConnectivityHelper
- [ ] Inject dependencies
- [ ] Add connectivity check
- [ ] Queue if offline
- [ ] Update if online
- [ ] Compiles without errors

---

### **Step 3: CreateCustomerUseCase** ✅

**File:** `app/src/main/java/com/emul8r/bizap/domain/usecase/CreateCustomerUseCase.kt`

```kotlin
suspend operator fun invoke(customer: Customer): Result<Long> {
    return try {
        // Validation
        if (customer.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name cannot be empty"))
        }

        // 🔌 Check connectivity
        val isOnline = ConnectivityHelper.isNetworkAvailable(context)
        
        if (!isOnline) {
            // 📝 OFFLINE: Queue the customer creation
            Timber.i("📶 Offline detected. Queueing customer creation for sync.")
            val queuedId = offlineQueueService.queueCreateCustomer(customer)
            return Result.success(queuedId)
        }
        
        // 🌐 ONLINE: Create customer directly
        repository.createCustomer(customer)
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to create customer")
        Result.failure(e)
    }
}
```

**Constructor:**
```kotlin
class CreateCustomerUseCase @Inject constructor(
    private val repository: CustomerRepository,
    private val offlineQueueService: OfflineQueueService,
    private val context: Context
)
```

**Note:** May need to add `queueCreateCustomer()` to OfflineQueueService if not present.

---

### **Step 4: UpdateCustomerUseCase** ✅

**File:** `app/src/main/java/com/emul8r/bizap/domain/usecase/UpdateCustomerUseCase.kt`

```kotlin
suspend operator fun invoke(customer: Customer): Result<Long> {
    return try {
        // Validation
        if (customer.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Customer name cannot be empty"))
        }

        // 🔌 Check connectivity
        val isOnline = ConnectivityHelper.isNetworkAvailable(context)
        
        if (!isOnline) {
            // 📝 OFFLINE: Queue the update
            Timber.i("📶 Offline detected. Queueing customer update for sync.")
            val queuedId = offlineQueueService.queueUpdateCustomer(customer)
            return Result.success(queuedId)
        }
        
        // 🌐 ONLINE: Update directly
        repository.updateCustomer(customer)
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to update customer")
        Result.failure(e)
    }
}
```

**Note:** May need to add `queueUpdateCustomer()` to OfflineQueueService if not present.

---

### **Step 5: DeleteCustomerUseCase** ✅

**File:** `app/src/main/java/com/emul8r/bizap/domain/usecase/DeleteCustomerUseCase.kt`

```kotlin
suspend operator fun invoke(customerId: Long, businessId: Long): Result<Unit> {
    return try {
        // 🔌 Check connectivity
        val isOnline = ConnectivityHelper.isNetworkAvailable(context)
        
        if (!isOnline) {
            // 📝 OFFLINE: Queue the deletion
            Timber.i("📶 Offline detected. Queueing customer deletion for sync.")
            offlineQueueService.queueDeleteCustomer(customerId, businessId)
            return Result.success(Unit)
        }
        
        // 🌐 ONLINE: Delete directly
        repository.deleteCustomer(customerId)
        Timber.d("✅ Customer deleted: $customerId")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to delete customer")
        Result.failure(e)
    }
}
```

**Note:** May need to add `queueDeleteCustomer()` to OfflineQueueService if not present.

---

### **Step 6: Add Missing Queue Methods (Optional)**

**If OfflineQueueService doesn't have these methods, add them:**

**File:** `app/src/main/java/com/emul8r/bizap/data/local/offline/OfflineQueueService.kt`

```kotlin
// ... existing code ...

/**
 * Queue a customer creation
 */
suspend fun queueCreateCustomer(customer: Customer): Long {
    return try {
        val operation = OfflineOperation(
            operationType = "CREATE_CUSTOMER",
            entityId = customer.id,
            entityData = OperationSerializer.serializeCustomer(customer),
            businessProfileId = customer.businessId,
            status = "PENDING"
        )
        
        val id = dao.insert(operation)
        cacheUpdateMutex.withLock {
            pendingCache.add(operation.copy(id = id))
            updateStateFlow(customer.businessId)
        }
        
        Timber.d("👤 Queued CREATE_CUSTOMER: $id")
        id
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to queue customer creation")
        throw e
    }
}

/**
 * Queue a customer update
 */
suspend fun queueUpdateCustomer(customer: Customer): Long {
    // Similar to queueCreateCustomer but with UPDATE_CUSTOMER type
    return try {
        val operation = OfflineOperation(
            operationType = "UPDATE_CUSTOMER",
            entityId = customer.id,
            entityData = OperationSerializer.serializeCustomer(customer),
            businessProfileId = customer.businessId,
            status = "PENDING"
        )
        
        val id = dao.insert(operation)
        cacheUpdateMutex.withLock {
            pendingCache.add(operation.copy(id = id))
            updateStateFlow(customer.businessId)
        }
        
        Timber.d("👤 Queued UPDATE_CUSTOMER: $id")
        id
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to queue customer update")
        throw e
    }
}

/**
 * Queue a customer deletion
 */
suspend fun queueDeleteCustomer(customerId: Long, businessId: Long): Long {
    return try {
        val operation = OfflineOperation(
            operationType = "DELETE_CUSTOMER",
            entityId = customerId,
            entityData = "",
            businessProfileId = businessId,
            status = "PENDING"
        )
        
        val id = dao.insert(operation)
        cacheUpdateMutex.withLock {
            pendingCache.add(operation.copy(id = id))
            updateStateFlow(businessId)
        }
        
        Timber.d("🗑️ Queued DELETE_CUSTOMER: $id")
        id
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to queue customer deletion")
        throw e
    }
}

// ... existing code ...
```

---

### **Step 7: Update OperationSerializer (if needed)**

If you're adding customer operations, add serialization:

**File:** `app/src/main/java/com/emul8r/bizap/data/local/offline/OperationSerializer.kt`

```kotlin
// ... existing code ...

fun serializeCustomer(customer: Customer): String {
    return try {
        Json.encodeToString(customer)
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to serialize customer")
        throw e
    }
}

fun deserializeCustomer(json: String): Customer {
    return try {
        Json.decodeFromString(json)
    } catch (e: Exception) {
        Timber.e(e, "❌ Failed to deserialize customer")
        throw e
    }
}

// ... existing code ...
```

---

## 🔨 BUILD & VERIFY

After updating/creating all UseCases:

```bash
# Clean and build
./gradlew clean compileDebugKotlin

# Run all tests
./gradlew testDebugUnitTest

# Expected: BUILD SUCCESSFUL, 295+ tests passing
```

**Checklist:**
- [ ] All files compile without errors
- [ ] All tests passing (295+)
- [ ] No regressions
- [ ] Build clean (0 errors, 0 warnings)

---

## 📝 INTEGRATION TESTS (Optional but Recommended)

Create integration tests for customer operations:

**File:** `app/src/test/java/com/emul8r/bizap/domain/usecase/CreateCustomerUseCaseOfflineTest.kt`

```kotlin
@RunWith(RobolectricTestRunner::class)
class CreateCustomerUseCaseOfflineTest {
    
    private lateinit var context: Context
    private lateinit var mockRepository: CustomerRepository
    private lateinit var mockQueueService: OfflineQueueService
    private lateinit var useCase: CreateCustomerUseCase
    
    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockRepository = mock()
        mockQueueService = mock()
        useCase = CreateCustomerUseCase(mockRepository, mockQueueService, context)
    }
    
    @Test
    fun testCreateCustomerOffline() = runBlocking {
        val customer = Customer(
            id = 1L,
            businessId = 1L,
            name = "Test Customer",
            email = "test@example.com",
            phone = "555-1234"
        )
        
        coEvery { mockQueueService.queueCreateCustomer(customer) } returns 1L
        
        val result = useCase(customer)
        
        assertThat(result.isSuccess).isTrue()
    }
}
```

---

## ✅ SUCCESS CHECKLIST FOR DAY 4

```
Code:
[✅] UpdateInvoiceUseCase updated
[✅] UpdateStatusUseCase updated
[✅] CreateCustomerUseCase created/updated
[✅] UpdateCustomerUseCase created/updated
[✅] DeleteCustomerUseCase created/updated
[✅] OfflineQueueService enhanced (if needed)
[✅] OperationSerializer updated (if needed)
[✅] All imports correct
[✅] All dependencies injected

Tests:
[✅] All 295+ tests passing
[✅] Integration tests passing (if added)
[✅] No regressions
[✅] Build clean (0 errors)

Functionality:
[✅] All data operations are offline-ready
[✅] Queuing works for all operations
[✅] Direct save works for all operations
[✅] Ready for Day 5 testing
```

---

## ⏱️ TIME BREAKDOWN

- UpdateInvoiceUseCase: 15-20 min
- UpdateStatusUseCase: 10-15 min
- CreateCustomerUseCase: 15-20 min
- UpdateCustomerUseCase: 15-20 min
- DeleteCustomerUseCase: 10-15 min
- OfflineQueueService enhancements: 15-20 min (if needed)
- Integration tests: 20-30 min (optional)
- Build, test, commit: 20-30 min

**Total: 3-4 hours**

---

## 🎯 PHASE 2 PROGRESS

```
Day 1: ✅ Database Layer (100%)
Day 2: ✅ Queue Service (100%)
Day 3: ✅ UseCase Integration - Wave 1 (100%)
Day 4: ⏳ UseCase Integration - Wave 2 (TODAY)
Day 5: ⏳ Comprehensive E2E Testing

After Day 4:
- Phase 2 Progress: 50% Complete ✅
- Every user action: Offline-ready ✅
- Ready for: SyncWorker implementation ✅
```

---

## 🚀 FINAL NOTES

**This is the last day of expansion.** After today:
- Every user action is offline-aware ✅
- Everything queues properly ✅
- Pattern is proven and tested ✅

**Day 5 is about comprehensive testing** - making sure everything works together end-to-end.

**Week 2 (Days 6-10) is about the SyncWorker** - actually processing the queue.

---

**Day 4 Status:** Ready to begin  
**Difficulty:** Low (copy-paste pattern)  
**Confidence:** 98% (proven approach)  
**Next Milestone:** 50% Phase 2 Complete by end of day  


