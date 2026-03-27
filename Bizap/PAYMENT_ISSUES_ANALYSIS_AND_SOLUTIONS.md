# 🔍 PAYMENT SUMMARY COMPONENT - ISSUES ANALYSIS & 7 SOLUTION APPROACHES

**Date:** March 27, 2026  
**Status:** Issue Analysis Complete  
**Priority:** HIGH (Crashes & Data Consistency)

---

## 📋 ISSUES SUMMARY

### **Issue #1: GUI2 Payment Tab Crash**
**Severity:** CRITICAL  
**Location:** `PaymentHistoryScreen.kt` (GUI2) → When clicking invoice and switching to Payment tab

**Observed Behavior:**
- User clicks on an invoice detail
- Switches to "Payment History" tab
- App crashes

**Root Cause Analysis:**
The issue stems from **ViewModel initialization without explicit invoiceId parameter**:

1. **Primary Issue:** `PaymentHistoryScreen` doesn't pass `invoiceId` to `PaymentHistoryViewModel`
   - Screen calls: `viewModel: PaymentHistoryViewModel = hiltViewModel()`
   - ViewModel expects `invoiceId` from `SavedStateHandle["invoiceId"]`
   - But `SavedStateHandle` may not contain invoiceId (null)
   - ViewModel's `paymentHistory` Flow emits: `Error("No invoice ID provided", -1L)`

2. **Secondary Issue:** Even if `SavedStateHandle` has invoiceId, there's a **Nested Flow Collection Problem**
   - `createPaymentHistoryFlow()` uses two nested `.collect()` blocks
   - When inner flow errors or completes unexpectedly, the outer flow doesn't handle it gracefully
   - No cancellation management between nested flows

3. **Tertiary Issue:** **Missing BusinessId Parameter**
   - `observePaymentHistory()` in repository only accepts `invoiceId`
   - Doesn't validate the invoice belongs to the active business (multi-tenant safety)
   - Could potentially leak cross-tenant data

**Crash Stack Trace Likely Shows:**
```
NullPointerException: Cannot invoke method on null SavedStateHandle
  at PaymentHistoryViewModel.paymentHistory.init()
  
OR

IllegalStateException: No invoice ID provided
  at PaymentHistoryScreen.collectAsStateWithLifecycle()
```

---

### **Issue #2: GUI1 Payment History Scope Problem**
**Severity:** HIGH  
**Location:** `InvoicePaymentDao.kt` → `observeAllSnapshots()` used by GUI1

**Observed Behavior:**
- User opens Payment History view in GUI1
- Instead of seeing payments for just the selected customer/invoice
- Sees ALL payments from all customers/invoices in the business
- Data scoping is broken

**Root Cause Analysis:**
The DAO query for payment history is **missing invoice-level filtering**:

```kotlin
// CURRENT BROKEN CODE (InvoicePaymentDao.kt, line 31-41):
@Query("""
    SELECT * FROM invoice_payment_snapshots
    WHERE businessProfileId = :businessId
      AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
    ORDER BY dueDate ASC
""")
fun observeAllSnapshots(businessId: Long): Flow<List<InvoicePaymentSnapshot>>
```

**Issues:**
1. **No invoiceId filter** - Returns ALL snapshots for entire business
2. **No customerId filter** - Doesn't even filter by customer
3. **Scope is too broad** - If used in GUI1 invoice detail, shows all business payments
4. **Data consistency** - Misleading UI showing irrelevant payment records

**The Fix That's Missing:**
```kotlin
// SHOULD BE:
@Query("""
    SELECT * FROM invoice_payment_snapshots
    WHERE businessProfileId = :businessId
      AND invoiceId = :invoiceId
      AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
    ORDER BY lastUpdatedMs DESC
""")
fun observeAllSnapshots(businessId: Long, invoiceId: Long): Flow<List<InvoicePaymentSnapshot>>
```

---

## ✅ 7 DIFFERENT SOLUTION APPROACHES

### **APPROACH 1: The "Parameter Injection Fix" (Recommended for GUI2 Crash)**

**Concept:** Pass invoiceId explicitly from screen to ViewModel instead of relying on SavedStateHandle

**What to Do:**
1. Modify `PaymentHistoryScreen` to create ViewModelFactory
2. Pass `invoiceId` parameter explicitly to ViewModel constructor
3. Remove reliance on SavedStateHandle for invoiceId
4. Add businessId parameter for multi-tenant safety

**Implementation Steps:**
```kotlin
// In PaymentHistoryScreen.kt:
@Composable
fun PaymentHistoryScreen(
    invoiceId: Long,
    businessId: Long,  // ADD THIS
    modifier: Modifier = Modifier
) {
    // Validate both parameters
    if (invoiceId <= 0 || businessId <= 0) {
        // Show error
        return
    }
    
    // Create factory with explicit parameters
    val viewModelFactory = viewModelFactory { 
        PaymentHistoryViewModel(
            invoiceId = invoiceId,
            businessId = businessId,
            invoiceRepository = hilt.InvoiceRepository()
        ) 
    }
    
    val viewModel: PaymentHistoryViewModel = viewModel(factory = viewModelFactory)
    // Rest of code...
}

// In PaymentHistoryViewModel.kt:
@HiltViewModel
class PaymentHistoryViewModel(
    private val invoiceId: Long,
    private val businessId: Long,
    private val invoiceRepository: InvoiceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    // Initialize with known invoiceId, not SavedStateHandle
    val paymentHistory: Flow<PaymentHistoryUiState> = 
        if (invoiceId > 0 && businessId > 0) {
            createPaymentHistoryFlow(invoiceId, businessId)
        } else {
            flowOf(PaymentHistoryUiState.Error("Invalid parameters", -1L))
        }
}
```

**Pros:**
- ✅ Solves crash immediately
- ✅ Adds multi-tenant safety with businessId
- ✅ No SavedStateHandle dependency
- ✅ Explicit parameter passing is testable

**Cons:**
- ⚠️ Requires ViewModelFactory setup
- ⚠️ Breaks backward compatibility with SavedStateHandle
- ⚠️ Need to update InvoiceDetailScreenV2 to pass businessId

**Effort:** 2-3 hours | **Risk:** LOW

---

### **APPROACH 2: The "Flatten Nested Flows Fix" (Better Architecture)**

**Concept:** Eliminate nested `.collect()` blocks that cause flow completion issues

**What to Do:**
1. Use `flatMapLatest` instead of nested `.collect()` in ViewModel
2. Properly handle flow cancellation
3. Add operator chaining for cleaner code

**Implementation:**
```kotlin
// In PaymentHistoryViewModel.kt:
private fun createPaymentHistoryFlow(invoiceId: Long): Flow<PaymentHistoryUiState> {
    return invoiceRepository.getInvoiceWithItemsById(invoiceId)
        .flatMapLatest { invoice ->
            if (invoice == null) {
                flowOf(PaymentHistoryUiState.NotFound(invoiceId))
            } else {
                invoiceRepository.observePaymentHistory(invoiceId)
                    .map { snapshots ->
                        if (snapshots.isEmpty()) {
                            PaymentHistoryUiState.Success(
                                invoiceId = invoiceId,
                                invoiceName = invoice.invoiceNumber,
                                totalAmount = invoice.totalAmount,
                                paidAmount = invoice.amountPaid,
                                outstandingAmount = invoice.totalAmount - invoice.amountPaid,
                                paymentHistory = emptyList()
                            )
                        } else {
                            val latest = snapshots.first()
                            PaymentHistoryUiState.Success(
                                invoiceId = invoiceId,
                                invoiceName = latest.invoiceNumber,
                                totalAmount = latest.totalAmount,
                                paidAmount = latest.paidAmount,
                                outstandingAmount = latest.outstandingAmount,
                                paymentHistory = snapshots.map { snapshot ->
                                    PaymentHistoryItem(
                                        date = snapshot.lastUpdatedMs,
                                        amount = snapshot.paidAmount,
                                        status = snapshot.paymentStatus,
                                        daysSinceDue = snapshot.daysSinceDue,
                                        notes = null
                                    )
                                }
                            )
                        }
                    }
            }
        }
        .catch { e ->
            Timber.e(e, "❌ Error loading payment history")
            emit(PaymentHistoryUiState.Error("Failed to load payment history", invoiceId))
        }
        .onStart { 
            emit(PaymentHistoryUiState.Loading) 
        }
}

val paymentHistory: Flow<PaymentHistoryUiState> = 
    if (invoiceIdFromHandle != null && invoiceIdFromHandle > 0) {
        createPaymentHistoryFlow(invoiceIdFromHandle)
    } else {
        flowOf(PaymentHistoryUiState.Error("No invoice ID provided", -1L))
    }
```

**Pros:**
- ✅ Cleaner, more reactive code
- ✅ Proper flow cancellation/completion handling
- ✅ Follows Kotlin coroutines best practices
- ✅ Better state management
- ✅ No nested `.collect()` blocks

**Cons:**
- ⚠️ Requires understanding of `flatMapLatest`
- ⚠️ Behavior change from nested flows
- ⚠️ May need adjustment if SavedStateHandle is critical

**Effort:** 1-2 hours | **Risk:** LOW-MEDIUM

---

### **APPROACH 3: The "Repository Filtering Fix" (For GUI1 Scope Issue)**

**Concept:** Fix the DAO query to properly filter by invoiceId at the database level

**What to Do:**
1. Update `InvoicePaymentDao.observePaymentHistory()` signature to require invoiceId and businessId
2. Add WHERE clause filtering in the SQL query
3. Update repository implementation to pass both parameters
4. Add parameter validation

**Implementation:**
```kotlin
// In InvoicePaymentDao.kt:
/**
 * Observe payment history for a specific invoice.
 * 
 * CRITICAL: This query filters by BOTH invoiceId AND businessId to ensure:
 * 1. Only payments for the specific invoice are returned (not all business payments)
 * 2. Multi-tenant data isolation (can't query other business's invoices)
 * 3. Data consistency between invoice and payment records
 */
@Query("""
    SELECT * FROM invoice_payment_snapshots
    WHERE invoiceId = :invoiceId
      AND businessProfileId = :businessId
    ORDER BY lastUpdatedMs DESC
""")
fun observePaymentHistory(invoiceId: Long, businessId: Long): Flow<List<InvoicePaymentSnapshot>>

// In InvoiceRepositoryImpl.kt:
override fun observePaymentHistory(invoiceId: Long, businessId: Long): Flow<List<InvoicePaymentSnapshot>> {
    require(invoiceId > 0) { "invoiceId must be > 0" }
    require(businessId > 0) { "businessId must be > 0" }
    
    return paymentDao.observePaymentHistory(invoiceId, businessId)
        .catch { e ->
            Timber.e(e, "Error observing payment history for invoice $invoiceId, business $businessId")
            emit(emptyList())
        }
}
```

**Pros:**
- ✅ Fixes GUI1 scope issue at source (database level)
- ✅ Adds multi-tenant safety
- ✅ Clear parameter validation
- ✅ Better performance (filtered at DB, not in app)
- ✅ Follows the interface specification in InvoiceRepository.kt comment

**Cons:**
- ⚠️ Requires updating InvoiceRepository interface signature
- ⚠️ ALL callers must pass businessId
- ⚠️ Backward incompatible change

**Effort:** 1-2 hours | **Risk:** MEDIUM (Breaking change)

---

### **APPROACH 4: The "Comprehensive Refactor" (All-In-One Fix)**

**Concept:** Combine Approaches 1, 2, and 3 for complete solution

**What to Do:**
1. Fix ViewModel initialization with explicit parameters (Approach 1)
2. Flatten nested flows (Approach 2)
3. Fix repository filtering (Approach 3)
4. Add comprehensive validation at all layers
5. Add detailed logging for debugging

**Implementation Structure:**
```
PaymentHistoryScreen (GUI2)
  ├─ Validate invoiceId and businessId
  ├─ Create ViewModelFactory with explicit parameters
  ├─ Pass to PaymentHistoryViewModel
  └─ Handle all UI states (Loading, Success, NotFound, Error)

PaymentHistoryViewModel
  ├─ Accept invoiceId and businessId in constructor
  ├─ Use flatMapLatest (not nested .collect)
  ├─ Call observePaymentHistory(invoiceId, businessId)
  └─ Transform to UI state with proper error handling

InvoiceRepository & Impl
  ├─ Update observePaymentHistory signature
  ├─ Add businessId parameter requirement
  ├─ Pass both parameters to DAO
  └─ Add parameter validation

InvoicePaymentDao
  ├─ Filter by invoiceId AND businessId
  ├─ Order by lastUpdatedMs DESC
  └─ Return Flow<List<InvoicePaymentSnapshot>>
```

**Pros:**
- ✅ Fixes ALL known issues (crash, scope, architecture)
- ✅ Adds multi-tenant safety everywhere
- ✅ Follows best practices throughout
- ✅ More robust and maintainable
- ✅ Better error handling

**Cons:**
- ⚠️ Largest scope of changes
- ⚠️ Highest breaking change risk
- ⚠️ Requires testing at multiple layers
- ⚠️ Most time-intensive

**Effort:** 4-5 hours | **Risk:** MEDIUM

---

### **APPROACH 5: The "Safe Migration Strategy" (Gradual Rollout)**

**Concept:** Create new methods alongside old ones, then gradually migrate

**What to Do:**
1. Create new `observePaymentHistoryV2()` with invoiceId + businessId parameters
2. Create new `PaymentHistoryViewModelV2` with explicit parameter injection
3. Update `PaymentHistoryScreenV2` to use new components
4. Keep old components working (backward compatibility)
5. Gradually migrate old screens to use V2
6. Remove old components after full migration

**Implementation:**
```kotlin
// In InvoicePaymentDao.kt - ADD NEW METHOD (don't remove old):
@Query("""
    SELECT * FROM invoice_payment_snapshots
    WHERE invoiceId = :invoiceId
      AND businessProfileId = :businessId
    ORDER BY lastUpdatedMs DESC
""")
fun observePaymentHistoryV2(invoiceId: Long, businessId: Long): Flow<List<InvoicePaymentSnapshot>>

// In InvoiceRepository.kt - ADD NEW METHOD:
fun observePaymentHistoryV2(invoiceId: Long, businessId: Long): Flow<List<InvoicePaymentSnapshot>>

// In PaymentHistoryScreenV2.kt - CREATE NEW SCREEN:
@Composable
fun PaymentHistoryScreenV2(
    invoiceId: Long,
    businessId: Long,
    viewModel: PaymentHistoryViewModelV2 = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // Improved implementation
}

// Gradually migrate usages:
// OLD: PaymentHistoryScreen(invoiceId, modifier)
// NEW: PaymentHistoryScreenV2(invoiceId, businessId, modifier)
```

**Pros:**
- ✅ Zero breaking changes during transition
- ✅ Can test V2 in parallel with V1
- ✅ Rollback capability if issues found
- ✅ Reduced risk
- ✅ Allows phased testing

**Cons:**
- ⚠️ Code duplication during migration period
- ⚠️ Longer implementation timeline
- ⚠️ More complex maintenance
- ⚠️ Eventually need cleanup phase

**Effort:** 5-6 hours (includes migration period) | **Risk:** LOW

---

### **APPROACH 6: The "Defensive Programming Fix" (Quick Hotfix)**

**Concept:** Add defensive checks and fallback logic without major refactoring

**What to Do:**
1. Add null safety checks in PaymentHistoryScreen
2. Add fallback initialization in PaymentHistoryViewModel
3. Add defensive filtering in presentation layer
4. Add detailed error messages for debugging
5. Implement graceful degradation

**Implementation:**
```kotlin
// In PaymentHistoryScreen.kt - ADD DEFENSIVE LOGIC:
@Composable
fun PaymentHistoryScreen(
    invoiceId: Long,
    viewModel: PaymentHistoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    // DEFENSIVE: Validate invoiceId
    if (invoiceId <= 0) {
        Timber.e("❌ CRITICAL: Invalid invoiceId=$invoiceId passed to PaymentHistoryScreen")
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Error, "Error", tint = Color.Red)
                Spacer(Modifier.height(8.dp))
                Text("Invalid invoice", color = Color.Red)
                Spacer(Modifier.height(4.dp))
                Text("ID: $invoiceId", style = MaterialTheme.typography.labelSmall)
            }
        }
        return
    }
    
    // DEFENSIVE: Initialize ViewModel with explicit parameter
    val uiState by viewModel.initialize(invoiceId)
        .collectAsStateWithLifecycle(initialValue = PaymentHistoryUiState.Loading)
    
    // Rest of code...
}

// In PaymentHistoryViewModel.kt - ADD FALLBACK:
val paymentHistory: Flow<PaymentHistoryUiState> = 
    if (invoiceIdFromHandle != null && invoiceIdFromHandle > 0) {
        Timber.d("Using invoiceId from SavedStateHandle: $invoiceIdFromHandle")
        createPaymentHistoryFlow(invoiceIdFromHandle)
    } else {
        Timber.w("⚠️ No invoiceId in SavedStateHandle, will wait for initialize() call")
        flowOf(PaymentHistoryUiState.Loading)  // CHANGED: Load instead of error
    }

// In InvoicePaymentDao.kt - ADD DEFENSIVE FILTERING:
@Query("""
    SELECT * FROM invoice_payment_snapshots
    WHERE invoiceId = :invoiceId
      AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
    ORDER BY lastUpdatedMs DESC
    LIMIT :limit
""")
fun observePaymentHistory(
    invoiceId: Long, 
    limit: Int = 1000
): Flow<List<InvoicePaymentSnapshot>>
```

**Pros:**
- ✅ Quick to implement (1-2 hours)
- ✅ Minimal code changes
- ✅ Backward compatible
- ✅ Improves error messages
- ✅ Better debugging information

**Cons:**
- ⚠️ Doesn't fully fix root cause (GUI1 scope issue remains)
- ⚠️ Relies on initialize() being called correctly
- ⚠️ Still has multi-tenant gap
- ⚠️ More of a patch than solution

**Effort:** 1-2 hours | **Risk:** LOW

---

### **APPROACH 7: The "Test-Driven Development Fix" (TDD Approach)**

**Concept:** Write tests first to define expected behavior, then fix implementation to pass tests

**What to Do:**
1. Write comprehensive unit tests defining correct behavior
2. Write integration tests for crash scenario
3. Write data validation tests for GUI1 scope
4. Run tests to identify exact failure points
5. Fix code to pass all tests
6. Ensure 100% test coverage

**Test Structure:**
```kotlin
// In PaymentHistoryViewModelTest.kt:
class PaymentHistoryViewModelTest {
    
    @Test
    fun testPaymentHistoryInitializationWithValidInvoiceId() {
        // Given valid invoiceId
        val invoiceId = 123L
        val viewModel = PaymentHistoryViewModel(invoiceId, 456L, mockRepository, mockHandle)
        
        // When ViewModel is created
        // Then paymentHistory should emit Loading immediately
        val emissions = viewModel.paymentHistory.test()
        emissions.awaitItem() shouldBe PaymentHistoryUiState.Loading
    }
    
    @Test
    fun testPaymentHistoryWithInvalidInvoiceId() {
        // Given invalid invoiceId
        val invoiceId = -1L
        val viewModel = PaymentHistoryViewModel(invoiceId, 456L, mockRepository, mockHandle)
        
        // When ViewModel is created
        // Then paymentHistory should emit Error
        val emissions = viewModel.paymentHistory.test()
        emissions.awaitItem() shouldBe PaymentHistoryUiState.Loading
        val error = emissions.awaitItem()
        error shouldBeInstanceOf PaymentHistoryUiState.Error::class
        (error as PaymentHistoryUiState.Error).invoiceId shouldBe invoiceId
    }
    
    @Test
    fun testPaymentHistoryFiltersCorrectly() {
        // Given multiple invoices with payments
        val invoice1Id = 100L
        val invoice2Id = 200L
        val businessId = 999L
        
        // When querying payment history for invoice1
        val dao = InvoicePaymentDaoTest()
        val result = dao.observePaymentHistory(invoice1Id, businessId)
        
        // Then should only return payments for invoice1
        val emissions = result.test()
        val snapshots = emissions.awaitItem()
        snapshots.all { it.invoiceId == invoice1Id } shouldBe true
    }
    
    @Test
    fun testPaymentHistoryMultiTenantIsolation() {
        // Given invoices from different businesses
        val invoice1 = createInvoice(id = 100L, businessId = 111L)
        val invoice2 = createInvoice(id = 200L, businessId = 222L)
        
        // When querying payments for invoice1 in business111
        val result = dao.observePaymentHistory(100L, 111L)
        
        // Then should NOT return payments from invoice2 (different business)
        val emissions = result.test()
        val snapshots = emissions.awaitItem()
        snapshots.none { it.invoiceId == 200L } shouldBe true
    }
}
```

**Pros:**
- ✅ Comprehensive testing ensures quality
- ✅ Tests document expected behavior
- ✅ Catches regressions automatically
- ✅ Increases confidence in fixes
- ✅ Provides regression protection

**Cons:**
- ⚠️ Most time-intensive (3-4 hours just for tests)
- ⚠️ Requires good testing infrastructure
- ⚠️ Learning curve for TDD approach
- ⚠️ Difficult to retrofit existing code

**Effort:** 5-7 hours (includes test implementation) | **Risk:** LOW

---

## 🎯 RECOMMENDED APPROACH

**Best Option: APPROACH 3 (Repository Filtering Fix) + APPROACH 1 (Parameter Injection)**

**Why This Combination:**
1. **Approach 3** fixes GUI1 scope issue at the correct layer (database query)
2. **Approach 1** fixes GUI2 crash with explicit parameter passing
3. Together they address root causes, not symptoms
4. Both are relatively straightforward (3-4 hours total)
5. Both add multi-tenant safety
6. Minimal breaking changes with clear migration path

**Implementation Roadmap:**
```
Phase 1: Foundation (Approach 3)
  Hour 1-2: Update DAO, Repository interface, Implementation
  
Phase 2: GUI2 Fix (Approach 1)
  Hour 3-4: Update ViewModel, Screen, ViewModelFactory
  
Phase 3: Testing
  Hour 5-6: Write unit & integration tests
  
Phase 4: Deployment
  Hour 7: Code review, merge, deploy
```

---

## 📊 COMPARISON MATRIX

| Approach | Fixes GUI2 Crash | Fixes GUI1 Scope | Time | Risk | Backward Compat |
|----------|-----------------|-----------------|------|------|-----------------|
| 1. Parameter Injection | ✅ YES | ❌ NO | 2-3h | LOW | ❌ NO |
| 2. Flatten Flows | ✅ YES | ❌ NO | 1-2h | LOW | ✅ YES |
| 3. Repository Filtering | ❌ NO | ✅ YES | 1-2h | MED | ❌ NO |
| 4. Comprehensive | ✅ YES | ✅ YES | 4-5h | MED | ❌ NO |
| 5. Safe Migration | ✅ YES | ✅ YES | 5-6h | LOW | ✅ YES |
| 6. Defensive Prog | ✅ PARTIAL | ⚠️ PARTIAL | 1-2h | LOW | ✅ YES |
| 7. TDD Approach | ✅ YES | ✅ YES | 5-7h | LOW | ✅ YES |

---

## ✨ NEXT STEPS

1. **Choose Approach** based on your constraints:
   - If speed is critical → Approach 6 (Quick fix)
   - If quality is critical → Approach 7 (TDD)
   - If balance needed → Approach 1+3 (Recommended)
   - If future-proof needed → Approach 5 (Safe Migration)

2. **Create Branch** for implementation
   ```bash
   git checkout -b fix/payment-issues-approach-X
   ```

3. **Start Implementation** based on chosen approach
   - Review detailed code snippets above
   - Test each change thoroughly
   - Add logging for debugging

4. **Test Thoroughly**
   - Unit tests for each component
   - Integration tests for crash scenario
   - Manual testing on emulator
   - Code review before merge

5. **Document Changes**
   - Update README with new parameters
   - Add inline comments explaining fixes
   - Create migration guide if breaking changes

---

## 🔗 RELATED FILES TO MODIFY

### For Approach 1 (Parameter Injection):
- `app/src/main/java/com/emul8r/bizap/ui/gui2/invoices/PaymentHistoryViewModel.kt`
- `app/src/main/java/com/emul8r/bizap/ui/gui2/invoices/PaymentHistoryScreen.kt`
- `app/src/main/java/com/emul8r/bizap/ui/gui2/invoice/InvoiceDetailScreenV2.kt` (pass businessId)

### For Approach 3 (Repository Filtering):
- `app/src/main/java/com/emul8r/bizap/domain/repository/InvoiceRepository.kt` (update signature)
- `app/src/main/java/com/emul8r/bizap/data/repository/InvoiceRepositoryImpl.kt`
- `app/src/main/java/com/emul8r/bizap/data/local/dao/InvoicePaymentDao.kt`
- All call sites of `observePaymentHistory()`

### For Approach 7 (TDD):
- All files above, plus:
- `app/src/test/java/com/emul8r/bizap/ui/gui2/invoices/PaymentHistoryViewModelTest.kt`
- `app/src/test/java/com/emul8r/bizap/data/local/dao/InvoicePaymentDaoTest.kt`

---

## 📞 SUPPORT QUESTIONS

**Q: Will this fix work without updating the UI?**  
A: Approach 3 works at the database level, but Approach 1 requires UI changes. Use both for complete fix.

**Q: Can I deploy partial fixes?**  
A: Not recommended. Fix both issues together for data consistency.

**Q: What about performance impact?**  
A: Filtering at database level (Approach 3) actually improves performance by reducing data transfer.

**Q: Do I need to migrate old data?**  
A: No, changes are forward-compatible. Historical data remains accessible.

---

**Document Version:** 1.0  
**Last Updated:** March 27, 2026  
**Status:** Ready for Implementation  

