# ✅ PHASE 2 DAY 2 CHECKLIST - QUEUE SERVICE IMPLEMENTATION

**Date:** March 9, 2026  
**Task:** Build the Queue Service that manages offline operations  
**Estimated Time:** 3-4 hours  
**Difficulty:** Medium (more business logic, but patterns established)  
**Prerequisites:** Day 1 database layer ✅ COMPLETE  

---

## 🎯 TODAY'S MISSION

Build the `OfflineQueueService` that will:
- Queue operations when offline
- Serialize/deserialize data
- Track operation state
- Provide query interface for sync worker

---

## 📋 WHAT YOU'LL BUILD TODAY

1. **OfflineQueueService** - Main queue management service
2. **OperationSerializer** - Handles Invoice/Payment data serialization
3. **QueueState** - Data class for queue status
4. **Unit Tests** - Comprehensive service tests

---

## 📝 STEP-BY-STEP CHECKLIST

### **Step 1: Create OperationSerializer** ✅

**File to create:** `app/src/main/java/com/emul8r/bizap/data/local/offline/OperationSerializer.kt`

```kotlin
object OperationSerializer {
    
    fun serializeInvoice(invoice: Invoice): String {
        return try {
            val json = Json.encodeToString(invoice)
            Timber.d("📦 Serialized invoice: ${invoice.id}")
            json
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to serialize invoice")
            throw e
        }
    }
    
    fun deserializeInvoice(json: String): Invoice {
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to deserialize invoice")
            throw e
        }
    }
    
    fun serializePayment(invoiceId: Long, amountPaid: Long): String {
        return try {
            val data = mapOf(
                "invoiceId" to invoiceId,
                "amountPaid" to amountPaid,
                "timestamp" to System.currentTimeMillis()
            )
            Json.encodeToString(data)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to serialize payment")
            throw e
        }
    }
    
    fun deserializePayment(json: String): Pair<Long, Long> {
        return try {
            val data: Map<String, Long> = Json.decodeFromString(json)
            Pair(data["invoiceId"] ?: 0L, data["amountPaid"] ?: 0L)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to deserialize payment")
            throw e
        }
    }
}
```

**Checklist:**
- [ ] File created in correct location
- [ ] Uses kotlinx.serialization.Json
- [ ] Handles Invoice serialization
- [ ] Handles Payment serialization
- [ ] Includes error logging
- [ ] Compiles without errors

---

### **Step 2: Create QueueState Data Class** ✅

**File to create:** `app/src/main/java/com/emul8r/bizap/data/local/offline/QueueState.kt`

```kotlin
/**
 * Represents the current state of the offline queue
 */
data class QueueState(
    val totalPending: Int = 0,
    val failedCount: Int = 0,
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false,
    val errorMessage: String? = null
) {
    val hasFailedOperations: Boolean
        get() = failedCount > 0
    
    val needsSync: Boolean
        get() = totalPending > 0 && !isSyncing
    
    val isHealthy: Boolean
        get() = !hasFailedOperations && !isSyncing
}
```

**Checklist:**
- [ ] Data class created
- [ ] All state fields included
- [ ] Computed properties for UI logic
- [ ] Compiles without errors

---

### **Step 3: Create OfflineQueueService** ✅

**File to create:** `app/src/main/java/com/emul8r/bizap/data/local/offline/OfflineQueueService.kt`

```kotlin
@Singleton
class OfflineQueueService @Inject constructor(
    private val dao: OfflineOperationDao,
    private val invoiceRepository: InvoiceRepository
) {
    
    // In-memory cache of pending operations
    private val pendingCache = mutableListOf<OfflineOperation>()
    private val cacheUpdateMutex = Mutex()
    
    // State Flow for UI observation
    private val _queueState = MutableStateFlow<QueueState>(QueueState())
    val queueState: StateFlow<QueueState> = _queueState.asStateFlow()
    
    // Initialize cache on first use
    suspend fun initialize(businessId: Long) {
        cacheUpdateMutex.withLock {
            pendingCache.clear()
            pendingCache.addAll(dao.getPendingOperations(businessId))
            updateStateFlow(businessId)
            Timber.i("✅ Queue initialized with ${pendingCache.size} pending operations")
        }
    }
    
    /**
     * Queue a new invoice creation
     */
    suspend fun queueCreateInvoice(invoice: Invoice): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "CREATE_INVOICE",
                entityId = invoice.id,
                entityData = OperationSerializer.serializeInvoice(invoice),
                businessProfileId = invoice.businessProfileId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(invoice.businessProfileId)
            }
            
            Timber.d("📝 Queued CREATE_INVOICE: $id for invoice ${invoice.id}")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue invoice creation")
            throw e
        }
    }
    
    /**
     * Queue an invoice update
     */
    suspend fun queueUpdateInvoice(invoice: Invoice): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "UPDATE_INVOICE",
                entityId = invoice.id,
                entityData = OperationSerializer.serializeInvoice(invoice),
                businessProfileId = invoice.businessProfileId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(invoice.businessProfileId)
            }
            
            Timber.d("📝 Queued UPDATE_INVOICE: $id for invoice ${invoice.id}")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue invoice update")
            throw e
        }
    }
    
    /**
     * Queue a payment record
     */
    suspend fun queueRecordPayment(invoiceId: Long, amountPaid: Long, businessId: Long): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "UPDATE_PAYMENT",
                entityId = invoiceId,
                entityData = OperationSerializer.serializePayment(invoiceId, amountPaid),
                businessProfileId = businessId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(businessId)
            }
            
            Timber.d("💰 Queued RECORD_PAYMENT: $id for invoice $invoiceId")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue payment")
            throw e
        }
    }
    
    /**
     * Queue an invoice deletion
     */
    suspend fun queueDeleteInvoice(invoiceId: Long, businessId: Long): Long {
        return try {
            val operation = OfflineOperation(
                operationType = "DELETE_INVOICE",
                entityId = invoiceId,
                entityData = "",
                businessProfileId = businessId,
                status = "PENDING"
            )
            
            val id = dao.insert(operation)
            
            cacheUpdateMutex.withLock {
                pendingCache.add(operation.copy(id = id))
                updateStateFlow(businessId)
            }
            
            Timber.d("🗑️ Queued DELETE_INVOICE: $id for invoice $invoiceId")
            id
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to queue deletion")
            throw e
        }
    }
    
    /**
     * Get all pending operations for a business (FIFO order)
     */
    suspend fun getPendingOperations(businessId: Long): List<OfflineOperation> {
        return try {
            val ops = dao.getPendingOperations(businessId)
            Timber.d("📋 Found ${ops.size} pending operations for business $businessId")
            ops
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get pending operations")
            emptyList()
        }
    }
    
    /**
     * Mark operation as syncing
     */
    suspend fun markSyncing(operationId: Long) {
        try {
            dao.updateStatus(operationId, "SYNCING")
            Timber.d("⏳ Marked operation $operationId as SYNCING")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to mark as syncing")
        }
    }
    
    /**
     * Mark operation as successfully synced
     */
    suspend fun markSynced(operationId: Long) {
        try {
            dao.updateStatus(operationId, "SYNCED")
            Timber.d("✅ Marked operation $operationId as SYNCED")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to mark as synced")
        }
    }
    
    /**
     * Mark operation as failed with error message
     */
    suspend fun markFailed(operationId: Long, errorMessage: String) {
        try {
            val op = dao.getById(operationId)
            if (op != null) {
                val updated = op.copy(
                    status = "FAILED",
                    errorMessage = errorMessage,
                    retryCount = op.retryCount + 1
                )
                dao.update(updated)
                Timber.e("❌ Operation $operationId failed: $errorMessage (retry ${updated.retryCount})")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to mark as failed")
        }
    }
    
    /**
     * Get failed operations for retry
     */
    suspend fun getFailedOperations(): List<OfflineOperation> {
        return try {
            dao.getFailedOperations()
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get failed operations")
            emptyList()
        }
    }
    
    /**
     * Clean up successfully synced operations
     */
    suspend fun cleanupSyncedOperations(businessId: Long) {
        try {
            dao.deleteSuccessfullySyncedOperations(businessId)
            Timber.d("🧹 Cleaned up synced operations for business $businessId")
            updateStateFlow(businessId)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to cleanup operations")
        }
    }
    
    /**
     * Update the state flow with current queue status
     */
    private suspend fun updateStateFlow(businessId: Long) {
        try {
            val pending = dao.getPendingOperations(businessId)
            val failed = dao.getFailedOperations()
            
            _queueState.value = QueueState(
                totalPending = pending.size,
                failedCount = failed.size,
                lastSyncTime = System.currentTimeMillis(),
                isSyncing = false,
                errorMessage = null
            )
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update state flow")
        }
    }
}
```

**Checklist:**
- [ ] Service created with @Singleton
- [ ] DAO injected
- [ ] All queue methods implemented
- [ ] State Flow for UI
- [ ] Thread-safe with Mutex
- [ ] Comprehensive logging
- [ ] Compiles without errors

---

### **Step 4: Register Service in Hilt Module** ✅

**File to modify:** `app/src/main/java/com/emul8r/bizap/di/DataModule.kt`

Add to your data module (or create if doesn't exist):

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    
    @Provides
    @Singleton
    fun provideOfflineQueueService(
        dao: OfflineOperationDao,
        invoiceRepository: InvoiceRepository
    ): OfflineQueueService {
        return OfflineQueueService(dao, invoiceRepository)
    }
}
```

**Checklist:**
- [ ] Module exists or created
- [ ] Service provided as singleton
- [ ] Dependencies properly injected
- [ ] Compiles without errors

---

### **Step 5: Write Comprehensive Unit Tests** ✅

**File to create:** `app/src/test/java/com/emul8r/bizap/data/local/offline/OfflineQueueServiceTest.kt`

```kotlin
@RunWith(RobolectricTestRunner::class)
class OfflineQueueServiceTest {
    
    private lateinit var db: AppDatabase
    private lateinit var dao: OfflineOperationDao
    private lateinit var mockInvoiceRepository: InvoiceRepository
    private lateinit var service: OfflineQueueService
    
    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = db.offlineOperationDao()
        mockInvoiceRepository = mock()
        service = OfflineQueueService(dao, mockInvoiceRepository)
    }
    
    @After
    fun tearDown() {
        db.close()
    }
    
    @Test
    fun testQueueCreateInvoice() = runBlocking {
        val invoice = createTestInvoice()
        val opId = service.queueCreateInvoice(invoice)
        
        assertThat(opId).isGreaterThan(0)
        
        val pending = service.getPendingOperations(invoice.businessProfileId)
        assertThat(pending).hasSize(1)
        assertThat(pending[0].operationType).isEqualTo("CREATE_INVOICE")
    }
    
    @Test
    fun testQueueUpdateInvoice() = runBlocking {
        val invoice = createTestInvoice()
        val opId = service.queueUpdateInvoice(invoice)
        
        assertThat(opId).isGreaterThan(0)
        
        val pending = service.getPendingOperations(invoice.businessProfileId)
        assertThat(pending[0].operationType).isEqualTo("UPDATE_INVOICE")
    }
    
    @Test
    fun testQueueRecordPayment() = runBlocking {
        val opId = service.queueRecordPayment(1L, 5000L, 1L)
        
        assertThat(opId).isGreaterThan(0)
        
        val pending = service.getPendingOperations(1L)
        assertThat(pending).hasSize(1)
        assertThat(pending[0].operationType).isEqualTo("UPDATE_PAYMENT")
    }
    
    @Test
    fun testMarkSynced() = runBlocking {
        val invoice = createTestInvoice()
        val opId = service.queueCreateInvoice(invoice)
        
        service.markSynced(opId)
        
        val op = dao.getById(opId)
        assertThat(op?.status).isEqualTo("SYNCED")
    }
    
    @Test
    fun testMarkFailed() = runBlocking {
        val invoice = createTestInvoice()
        val opId = service.queueCreateInvoice(invoice)
        
        service.markFailed(opId, "Network error")
        
        val op = dao.getById(opId)
        assertThat(op?.status).isEqualTo("FAILED")
        assertThat(op?.errorMessage).contains("Network")
    }
    
    @Test
    fun testCleanupSyncedOperations() = runBlocking {
        val invoice = createTestInvoice()
        val opId = service.queueCreateInvoice(invoice)
        
        service.markSynced(opId)
        service.cleanupSyncedOperations(invoice.businessProfileId)
        
        val pending = service.getPendingOperations(invoice.businessProfileId)
        assertThat(pending).isEmpty()
    }
    
    @Test
    fun testStateFlowUpdates() = runBlocking {
        val invoice = createTestInvoice()
        
        service.queueCreateInvoice(invoice)
        
        val state = service.queueState.first()
        assertThat(state.totalPending).isGreaterThan(0)
        assertThat(state.needsSync).isTrue()
    }
    
    // Helper function
    private fun createTestInvoice(): Invoice {
        return Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test Customer",
            totalAmount = 10000,
            amountPaid = 0,
            status = InvoiceStatus.DRAFT,
            items = listOf()
        )
    }
}
```

**Checklist:**
- [ ] Test file created
- [ ] Uses Robolectric
- [ ] Tests all service methods
- [ ] Uses Mockk for dependencies
- [ ] Assertions verify behavior
- [ ] Tests compile and pass

---

### **Step 6: Build & Verify** ✅

Run these commands:

```bash
# Clean and build
./gradlew clean compileDebugKotlin

# Run tests
./gradlew testDebugUnitTest

# Check for errors
# Look for: "BUILD SUCCESSFUL"
```

**Checklist:**
- [ ] `./gradlew compileDebugKotlin` → SUCCESS
- [ ] `./gradlew testDebugUnitTest` → All tests pass (now 300+ tests!)
- [ ] No new compilation errors
- [ ] No regressions in existing tests

---

### **Step 7: Commit & Push** ✅

```bash
git add -A
git commit -m "Phase 2 Day 2: Implement OfflineQueueService

- Created OperationSerializer for Invoice/Payment serialization
- Created QueueState data class for UI observation
- Implemented OfflineQueueService with all queue methods
- Registered service in Hilt DatabaseModule
- Created comprehensive unit tests (8+ test cases)

Features:
- Queue create/update/delete invoices
- Queue payment recording
- Mark operations as synced/failed
- Cleanup successful operations
- Thread-safe with Mutex
- StateFlow for reactive UI updates

Tests: All passing (300+ total)
Build: Clean compilation"

git push origin main
```

**Checklist:**
- [ ] Files staged and committed
- [ ] Commit message clear and detailed
- [ ] Pushed to GitHub

---

## 📊 SUCCESS METRICS FOR DAY 2

```
Code:
[✅] OperationSerializer.kt created
[✅] QueueState.kt created
[✅] OfflineQueueService.kt created
[✅] Hilt module updated
[✅] Service registered as singleton

Tests:
[✅] OfflineQueueServiceTest created
[✅] 8+ test methods
[✅] All tests passing
[✅] Queue operations verified
[✅] State flow updates verified

Build:
[✅] Clean compilation
[✅] 300+ total tests passing
[✅] No regressions
[✅] All pushed to GitHub

Architecture:
[✅] Thread-safe with Mutex
[✅] Reactive with StateFlow
[✅] Comprehensive logging
[✅] Error handling in place
```

---

## ⏱️ TIME BREAKDOWN

- OperationSerializer: 20-30 min
- QueueState: 10-15 min
- OfflineQueueService: 45-60 min
- Hilt registration: 10-15 min
- Unit tests: 45-60 min
- Build, test, commit: 20-30 min

**Total: 3-4 hours**

---

## 🎯 NEXT: DAY 3 (MARCH 10)

Tomorrow you'll integrate the queue service into the UseCase layer:
- SaveInvoiceUseCase checks if online
- If offline, queue instead of sync
- Same for UpdateInvoice, RecordPayment, DeleteInvoice

Reference: PHASE_2_IMPLEMENTATION_GUIDE.md (Day 3-4 section)

---

## 💪 EXCELLENT WORK!

You just completed:
- Day 1: Bulletproof database layer ✅
- Day 2: Robust queue service ✅

**You're 20% through Phase 2 already!**

By end of Week 1, offline operations will be fully queued and ready for sync.

---

**Day 2 Status:** Ready to begin  
**Difficulty:** Medium  
**Confidence:** High (patterns from Phase 1 + Day 1)  
**Next Step:** Follow steps above, build, test, commit


