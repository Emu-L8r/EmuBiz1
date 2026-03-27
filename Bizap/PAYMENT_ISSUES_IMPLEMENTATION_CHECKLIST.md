# ✅ PAYMENT ISSUES FIX - IMPLEMENTATION CHECKLIST

**Quick Reference for Implementing Fixes**

---

## 🎯 RECOMMENDED APPROACH: #1 + #3 (Parameter Injection + Repository Filtering)

Choose your implementation track:

---

## 📋 IMPLEMENTATION TRACK A: APPROACH 1 (GUI2 Crash Fix)

### Step 1: Update PaymentHistoryViewModel Signature

- [ ] Open `PaymentHistoryViewModel.kt`
- [ ] Add `invoiceId: Long` parameter to constructor
- [ ] Add `businessId: Long` parameter to constructor  
- [ ] Change `paymentHistory` property initialization logic
- [ ] Update `createPaymentHistoryFlow()` signature to accept both parameters
- [ ] Add parameter validation: `require(invoiceId > 0 && businessId > 0)`

**Code Changes:**
```kotlin
// BEFORE:
@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel()

// AFTER:
@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val invoiceId: Long,
    private val businessId: Long,
    private val invoiceRepository: InvoiceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    init {
        require(invoiceId > 0) { "invoiceId must be > 0" }
        require(businessId > 0) { "businessId must be > 0" }
    }
}
```

### Step 2: Update createPaymentHistoryFlow Method

- [ ] Change method signature to accept `invoiceId` and `businessId`
- [ ] Pass `businessId` to `observePaymentHistory()` call
- [ ] Update all error messages to include both parameters

**Code Changes:**
```kotlin
// BEFORE:
private fun createPaymentHistoryFlow(invoiceId: Long): Flow<PaymentHistoryUiState> {
    invoiceRepository.observePaymentHistory(invoiceId)
}

// AFTER:
private fun createPaymentHistoryFlow(
    invoiceId: Long,
    businessId: Long
): Flow<PaymentHistoryUiState> {
    invoiceRepository.observePaymentHistory(invoiceId, businessId)
}
```

### Step 3: Update paymentHistory Property

- [ ] Initialize with invoiceId from constructor
- [ ] Remove reliance on SavedStateHandle["invoiceId"]
- [ ] Call createPaymentHistoryFlow with both parameters

**Code Changes:**
```kotlin
// BEFORE:
val paymentHistory: Flow<PaymentHistoryUiState> = 
    if (invoiceIdFromHandle != null && invoiceIdFromHandle > 0) {
        createPaymentHistoryFlow(invoiceIdFromHandle)
    } else {
        flowOf(PaymentHistoryUiState.Error("No invoice ID provided", -1L))
    }

// AFTER:
val paymentHistory: Flow<PaymentHistoryUiState> = 
    createPaymentHistoryFlow(invoiceId, businessId)
```

### Step 4: Update PaymentHistoryScreen

- [ ] Add `invoiceId` and `businessId` parameters to function signature
- [ ] Add validation for both parameters at function entry
- [ ] Create ViewModelFactory with explicit parameters
- [ ] Pass factory to `viewModel()` call
- [ ] Update error message in validation block

**Code Changes:**
```kotlin
// BEFORE:
@Composable
fun PaymentHistoryScreen(
    invoiceId: Long,
    viewModel: PaymentHistoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
)

// AFTER:
@Composable
fun PaymentHistoryScreen(
    invoiceId: Long,
    businessId: Long,
    viewModel: PaymentHistoryViewModel? = null,
    modifier: Modifier = Modifier
) {
    if (invoiceId <= 0 || businessId <= 0) {
        Box(...) { Text("Invalid invoice or business") }
        return
    }
    
    val factory = viewModelFactory {
        PaymentHistoryViewModel(
            invoiceId = invoiceId,
            businessId = businessId,
            invoiceRepository = hilt.InvoiceRepository(),
            savedStateHandle = SavedStateHandle()
        )
    }
    val vm = viewModel(factory = factory)
    val uiState by vm.paymentHistory.collectAsStateWithLifecycle(
        initialValue = PaymentHistoryUiState.Loading
    )
    // Rest of code...
}
```

### Step 5: Update InvoiceDetailScreenV2

- [ ] Find `PaymentHistoryTab` composable
- [ ] Add `businessId` parameter to `PaymentHistoryScreen` call
- [ ] Update call signature with both parameters

**Code Changes:**
```kotlin
// BEFORE:
@Composable
private fun PaymentHistoryTab(
    invoice: InvoiceWithItems,
    modifier: Modifier = Modifier
) {
    PaymentHistoryScreen(
        invoiceId = invoice.invoice.id,
        modifier = modifier.fillMaxWidth()
    )
}

// AFTER:
@Composable
private fun PaymentHistoryTab(
    invoice: InvoiceWithItems,
    businessId: Long,
    modifier: Modifier = Modifier
) {
    PaymentHistoryScreen(
        invoiceId = invoice.invoice.id,
        businessId = businessId,
        modifier = modifier.fillMaxWidth()
    )
}
```

### Step 6: Test Approach 1

- [ ] Compile project: `./gradlew build`
- [ ] Run on emulator: `./gradlew installDebug`
- [ ] Navigate to invoice detail in GUI2
- [ ] Click on Payment History tab
- [ ] Verify: Payment history loads without crash
- [ ] Verify: Correct invoice ID shown
- [ ] Test with multiple invoices
- [ ] Check logcat for error messages

**Test Commands:**
```bash
# Compile
./gradlew build

# Run tests
./gradlew testDebugUnitTest PaymentHistoryViewModelTest

# Install and run
./gradlew installDebug
adb shell am start -n com.emul8r.bizap/.ui.gui2.invoices.InvoiceListActivity
```

---

## 📋 IMPLEMENTATION TRACK B: APPROACH 3 (GUI1 Scope Fix)

### Step 1: Update InvoicePaymentDao Query

- [ ] Open `InvoicePaymentDao.kt`
- [ ] Find `observePaymentHistory()` method
- [ ] Update method signature to include `businessId` parameter
- [ ] Update SQL query to filter by BOTH `invoiceId` AND `businessProfileId`
- [ ] Add KDoc comment explaining multi-tenant filtering

**Code Changes:**
```kotlin
// BEFORE:
@Query("""
    SELECT * FROM invoice_payment_snapshots
    WHERE invoiceId = :invoiceId
    ORDER BY lastUpdatedMs DESC
""")
fun observePaymentHistory(invoiceId: Long): Flow<List<InvoicePaymentSnapshot>>

// AFTER:
@Query("""
    SELECT * FROM invoice_payment_snapshots
    WHERE invoiceId = :invoiceId
      AND businessProfileId = :businessId
    ORDER BY lastUpdatedMs DESC
""")
fun observePaymentHistory(
    invoiceId: Long, 
    businessId: Long
): Flow<List<InvoicePaymentSnapshot>>
```

### Step 2: Update InvoiceRepository Interface

- [ ] Open `InvoiceRepository.kt` (domain/repository)
- [ ] Find `observePaymentHistory` method signature in interface
- [ ] Update signature to include `businessId` parameter
- [ ] Update KDoc comment with new parameter description
- [ ] Update documentation about multi-tenant filtering

**Code Changes:**
```kotlin
// BEFORE:
fun observePaymentHistory(invoiceId: Long, businessId: Long): Flow<List<com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot>>

// AFTER - Already has businessId! Check if implementation matches.
```

### Step 3: Update InvoiceRepositoryImpl

- [ ] Open `InvoiceRepositoryImpl.kt`
- [ ] Find `observePaymentHistory()` implementation
- [ ] Update to pass `businessId` to DAO call
- [ ] Add parameter validation with `require()`
- [ ] Add error handling with `.catch { ... emit(emptyList()) }`

**Code Changes:**
```kotlin
// BEFORE:
override fun observePaymentHistory(invoiceId: Long, businessId: Long): Flow<List<InvoicePaymentSnapshot>> {
    return paymentDao.observePaymentHistory(invoiceId)
}

// AFTER:
override fun observePaymentHistory(invoiceId: Long, businessId: Long): Flow<List<InvoicePaymentSnapshot>> {
    require(invoiceId > 0) { "invoiceId must be > 0" }
    require(businessId > 0) { "businessId must be > 0" }
    
    return paymentDao.observePaymentHistory(invoiceId, businessId)
        .catch { e ->
            Timber.e(e, "Error observing payment history for invoice=$invoiceId, business=$businessId")
            emit(emptyList())
        }
}
```

### Step 4: Find All Call Sites

- [ ] Search for `observePaymentHistory(` in entire project
- [ ] Identify all locations that call this method
- [ ] Note: Some may be in PaymentHistoryViewModel (handled in Track A)

**Search Command:**
```bash
grep -r "observePaymentHistory(" --include="*.kt" app/src
```

### Step 5: Update Call Sites

For each call site found:

- [ ] Check if `businessId` is available in context
- [ ] Update call to pass `businessId` parameter
- [ ] Add null check if getting businessId from optional source
- [ ] Add error handling

**Example:**
```kotlin
// BEFORE:
invoiceRepository.observePaymentHistory(invoiceId)

// AFTER:
val businessId = /* get from context */
invoiceRepository.observePaymentHistory(invoiceId, businessId)
```

### Step 6: Test Approach 3

- [ ] Compile project: `./gradlew build`
- [ ] Run on emulator
- [ ] Open GUI1 payment history
- [ ] Select specific invoice
- [ ] Verify: Only that invoice's payments shown
- [ ] Verify: No payments from other invoices visible
- [ ] Test with multiple invoices
- [ ] Check database queries in logcat

**Test Commands:**
```bash
# Compile
./gradlew build

# Run database tests
./gradlew testDebugUnitTest InvoicePaymentDaoTest

# Install and test
./gradlew installDebug
adb shell am start -n com.emul8r.bizap/.MainActivity  # GUI1
```

---

## 🧪 TESTING CHECKLIST (Both Approaches)

### Unit Tests

- [ ] **PaymentHistoryViewModelTest**
  - [ ] Test initialization with valid parameters
  - [ ] Test initialization with invalid invoiceId
  - [ ] Test initialization with invalid businessId
  - [ ] Test flow emission sequence (Loading → Success)
  - [ ] Test NotFound state when invoice doesn't exist
  - [ ] Test Error state on exception

- [ ] **InvoicePaymentDaoTest**
  - [ ] Test query returns only specified invoiceId payments
  - [ ] Test query filters by businessId
  - [ ] Test query returns empty list when no payments
  - [ ] Test query ordering (newest first)
  - [ ] Test multi-tenant isolation

### Integration Tests

- [ ] **PaymentHistoryScreenTest**
  - [ ] Test screen displays without crash
  - [ ] Test screen shows loading state initially
  - [ ] Test screen shows success state with data
  - [ ] Test screen shows error state on failure
  - [ ] Test screen shows not found state

### Manual Testing

- [ ] Create test invoices with payments
- [ ] Open each invoice in GUI2
- [ ] Click Payment History tab
- [ ] Verify payment list is correct
- [ ] Verify no other invoices' payments shown
- [ ] Try with multiple invoices simultaneously
- [ ] Check for memory leaks using Android Studio Profiler
- [ ] Check logcat for error messages
- [ ] Test on different API levels (if applicable)

**Manual Test Data Setup:**
```kotlin
// Create test invoice with payments
val invoice = Invoice(
    id = 0,
    invoiceNumber = "TEST-001",
    totalAmount = 100_00, // $100.00
    amountPaid = 50_00,   // $50.00
    // ... other fields
)
val paymentSnapshot = InvoicePaymentSnapshot(
    invoiceId = invoice.id,
    businessProfileId = BUSINESS_ID,
    paidAmount = 50_00,
    // ... other fields
)
```

---

## 🐛 DEBUGGING CHECKLIST

If tests fail or crash occurs:

### Crash Analysis

- [ ] Check logcat for full stack trace
- [ ] Look for NullPointerException
- [ ] Look for IllegalStateException
- [ ] Look for IllegalArgumentException
- [ ] Search for "observePaymentHistory" in logs
- [ ] Check parameter values in Timber logs

**Common Crash Patterns:**
```
NullPointerException: Cannot invoke
  → Check if invoiceId/businessId is null

IllegalStateException: No invoice ID provided
  → Check if SavedStateHandle is empty

IllegalArgumentException: invoiceId must be > 0
  → Check if validation is too strict
```

### Data Verification

- [ ] Query database directly to verify data
- [ ] Check if payments are associated with correct invoice
- [ ] Check if businessId is set correctly
- [ ] Verify no NULL values in critical fields

**Database Queries:**
```sql
-- Check payments for specific invoice
SELECT * FROM invoice_payment_snapshots 
WHERE invoiceId = 123;

-- Check all payments for business
SELECT * FROM invoice_payment_snapshots 
WHERE businessProfileId = 456;

-- Check for orphaned payments
SELECT * FROM invoice_payment_snapshots 
WHERE invoiceId NOT IN (SELECT id FROM invoices);
```

### Performance Checks

- [ ] Monitor database query execution time
- [ ] Check if flow is being re-collected unnecessarily
- [ ] Verify no infinite loops in flow chain
- [ ] Check memory usage in Android Studio Profiler

**Performance Tools:**
```bash
# Enable database query logging
adb shell setprop log.tag.Room VERBOSE

# Monitor network/database
adb shell am start -n com.android.systemui/.tuner.TunerActivity
```

---

## 📝 COMMIT CHECKLIST

Before committing changes:

### Code Review

- [ ] All code follows project style guide
- [ ] No unused imports or variables
- [ ] Proper error handling in all paths
- [ ] Logging at appropriate levels (debug, info, warn, error)
- [ ] Comments explain "why" not "what"
- [ ] No hardcoded values (use constants)
- [ ] No TODO comments left in code

### Documentation

- [ ] Updated KDoc comments
- [ ] Updated method signatures in interface
- [ ] Added breaking change notes in commit message
- [ ] Updated README if public API changed
- [ ] Created migration guide if needed

### Testing

- [ ] All unit tests passing
- [ ] All integration tests passing
- [ ] Manual testing completed
- [ ] No regression in other features
- [ ] Tested edge cases

### Git Hygiene

- [ ] Branch name follows convention: `fix/payment-issues-approach-X`
- [ ] Commit message is clear and descriptive
- [ ] Changes are logically organized in commits
- [ ] No merge conflicts
- [ ] No sensitive data in commits

**Example Commit Message:**
```
fix: Payment history - Fix GUI2 crash with explicit parameter injection

- Pass invoiceId and businessId explicitly from screen to ViewModel
- Remove SavedStateHandle dependency for invoiceId
- Add parameter validation with require() statements
- Update InvoiceDetailScreenV2 to pass businessId

Fixes crash when switching to Payment History tab in invoice detail screen.

BREAKING CHANGE: PaymentHistoryScreen now requires businessId parameter
Migration: Update all calls to PaymentHistoryScreen(invoiceId, businessId, ...)
```

---

## 🚀 DEPLOYMENT CHECKLIST

After merge:

- [ ] Deployed to staging environment
- [ ] QA testing completed
- [ ] Crash reporting shows no new crashes
- [ ] Performance metrics stable or improved
- [ ] User-facing documentation updated if needed
- [ ] Deployed to production
- [ ] Monitor for 24-48 hours for issues
- [ ] Create post-mortem if any issues found

---

## 📊 METRICS TO TRACK

Monitor these metrics after deployment:

| Metric | Target | How to Check |
|--------|--------|--------------|
| Crash Rate | < 0.1% | Firebase Crashlytics |
| Load Time | < 500ms | Android Profiler |
| Memory Leak | None | Leak Canary |
| Query Time | < 50ms | Database logs |
| User Feedback | Positive | App Store reviews |

---

## 📞 TROUBLESHOOTING GUIDE

**Problem: Compilation errors after changes**

Solution:
1. Run `./gradlew clean`
2. Run `./gradlew build`
3. Check error messages carefully
4. Look for missing imports or wrong types

**Problem: Tests failing**

Solution:
1. Run single test: `./gradlew testDebugUnitTest PaymentHistoryViewModelTest`
2. Check test assertion messages
3. Add `.also { println(it) }` for debugging
4. Verify test data is set up correctly

**Problem: Still crashing on Payment tab**

Solution:
1. Check if `businessId` parameter is being passed
2. Verify `viewModelFactory` is created correctly
3. Check `SavedStateHandle` isn't being used anymore
4. Look for null pointer in `createPaymentHistoryFlow`

**Problem: Showing all payments instead of filtered**

Solution:
1. Verify DAO query has BOTH `invoiceId` AND `businessId` filters
2. Check if `businessId` is being passed to DAO call
3. Verify database has correct data in payment snapshots
4. Run direct SQL query to verify filtering works

---

## ✨ COMPLETION CRITERIA

Fix is complete when:

- [ ] ✅ GUI2 payment tab no longer crashes
- [ ] ✅ GUI2 shows correct invoice's payments only
- [ ] ✅ GUI1 shows correct invoice's payments only
- [ ] ✅ No data leakage between invoices
- [ ] ✅ Multi-tenant safety verified
- [ ] ✅ All unit tests passing
- [ ] ✅ All integration tests passing
- [ ] ✅ Manual testing successful
- [ ] ✅ Code reviewed and approved
- [ ] ✅ Deployed to production
- [ ] ✅ No crashes reported post-deployment
- [ ] ✅ Performance metrics stable
- [ ] ✅ Documentation updated

---

**Checklist Version:** 1.0  
**Last Updated:** March 27, 2026  
**Status:** Ready for Implementation  

