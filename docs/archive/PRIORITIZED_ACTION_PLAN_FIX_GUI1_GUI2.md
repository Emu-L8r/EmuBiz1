# 🚀 PRIORITIZED ACTION PLAN: Fix GUI1/GUI2 Issues

**Date**: March 8, 2026  
**Objective**: Make both GUIs operational and ensure data consistency  
**Total Effort**: 8-12 hours

---

## PHASE 1: CRITICAL FIXES (3-4 hours) 🔴

### Action 1.1: Fix GUI2 StatusUpdateMenuV2 (30 minutes)

**File**: `app/src/main/java/com/emul8r/bizap/ui/gui2/invoices/InvoiceDetailScreenV2.kt`  
**Problem**: Lambda callback type inference failing  
**Location**: Line 96-107

**Current Code (BROKEN)**:
```kotlin
// Currently commented out or doesn't compile
// onStatusChange = { newStatus -> ... }  // Type error
```

**Fix Options**:

**Option A: Explicit Type Declaration** (Recommended - 15 min)
```kotlin
val onStatusChange: (InvoiceStatus) -> Unit = { newStatus ->
    viewModel.updateStatus(newStatus)
}

// Then use the callback
StatusUpdateMenuV2(
    currentStatus = invoice.status,
    onStatusChange = onStatusChange
)
```

**Option B: Inline with Type** (10 min)
```kotlin
StatusUpdateMenuV2(
    currentStatus = invoice.status,
    onStatusChange = { newStatus: InvoiceStatus ->
        viewModel.updateStatus(newStatus)
    }
)
```

**Option C: Use ViewModel State** (20 min)
```kotlin
val selectedStatus by viewModel.selectedStatus.collectAsStateWithLifecycle()
StatusUpdateMenuV2(
    currentStatus = selectedStatus,
    onStatusChange = viewModel::updateStatus
)
```

**Recommendation**: Use Option A (safest, most maintainable)

**Test After Fix**:
```
1. Compile: ./gradlew compileDebugKotlin
2. Run: Open GUI2 → Invoice Detail
3. Test: Tap status button → Should open menu
4. Change status → Should update invoice
```

---

### Action 1.2: Fix GUI2 Customer Screen Type Errors (1 hour)

**Files**:
- `CreateCustomerViewModelV2.kt`: Line 28 - unresolved `saveCustomer`
- `CustomerDetailScreenV2.kt`: Lines 130, 132, 140, 142, 149, 151, 159, 161 - null safety errors
- `EditCustomerScreenV2.kt`: Lines 110-137 - TextField type mismatches

**Problem**: Multiple type inference issues with nullable String parameters

**Fix Pattern** (same for all):

```kotlin
// Before (BROKEN)
val address: String? = viewModel.customerAddress
Text(address)  // ❌ Error: need safe call

// After (FIXED)
val address: String = viewModel.customerAddress ?: ""
Text(address)  // ✅ OK
```

**Specific Fixes**:

1. **CustomerDetailScreenV2.kt** (line 130-162):
```kotlin
// Replace nullable String with safe default
val email: String = customer.email ?: ""
val phone: String = customer.phone ?: ""
val address: String = customer.address ?: ""

Text(email)    // Now safe
Text(phone)    // Now safe
Text(address)  // Now safe
```

2. **CreateCustomerViewModelV2.kt** (line 28):
```kotlin
// Resolve 'saveCustomer' reference
// Should be in CreateCustomerUseCase or CustomerRepository
private val createCustomerUseCase: CreateCustomerUseCase = /* injected */

fun saveCustomer(name: String, email: String, phone: String) {
    viewModelScope.launch {
        createCustomerUseCase(name, email, phone)
    }
}
```

3. **EditCustomerScreenV2.kt** (line 110-137):
```kotlin
// Fix OutlinedTextField parameter types
OutlinedTextField(
    value = customerName,  // Should be String, not TextFieldValue
    onValueChange = { newName -> 
        customerName = newName
    },
    // ... other parameters
)
```

**Test After Fix**:
```
1. Compile: ./gradlew compileDebugKotlin
2. Run: GUI2 → Customers → Create/Edit
3. Test: Create customer → Should save
4. Edit customer → Should update
```

---

### Action 1.3: Fix GUI2 Business Profile Screen (30-45 minutes)

**File**: `BusinessProfileScreenV2.kt`  
**Problem**: Parameter mismatches with OutlinedTextField

**Issues**:
- Line 74-77: Multiple `TextField` calls with wrong parameter types
- Line 104-130: Callback `it` parameter unresolved
- Line 148-151: Parameter name mismatches

**Fix**:

```kotlin
// Before (BROKEN)
val businessAbn: Bindable<String> = /* ... */
OutlinedTextField(
    value = businessAbn,  // ❌ Wrong type
    onValueChange = { it ->  // ❌ it unresolved
        businessAbn = it  // ❌ Type error
    }
)

// After (FIXED)
val businessAbn by viewModel.businessAbn.collectAsStateWithLifecycle()
var abnValue by remember { mutableStateOf(businessAbn) }

OutlinedTextField(
    value = abnValue,
    onValueChange = { newValue ->
        abnValue = newValue
        viewModel.updateAbn(newValue)
    }
)
```

**Systematic Fix**:
1. Identify all `Bindable<String>` types
2. Convert to `val x by viewModel.x.collectAsStateWithLifecycle()`
3. Use mutable state for editing
4. Update ViewModel method calls

**Test After Fix**:
```
1. Compile: ./gradlew compileDebugKotlin
2. Run: GUI2 → Settings → Business Profile
3. Test: Edit ABN, address, email → Should save
```

---

### Action 1.4: Promote Snapshot Sync Errors (10 minutes) 🎯

**File**: `InvoiceRepositoryImpl.kt`  
**Location**: Line 141-143

**Current Code (BROKEN)**:
```kotlin
} catch (e: Exception) {
    Timber.w(e, "⚠️ Failed to sync payment snapshots")  // WARNING
}
```

**Fixed Code**:
```kotlin
} catch (e: Exception) {
    Timber.e(e, "❌ CRITICAL: Failed to sync payment snapshots for invoice $invoiceId")  // ERROR
    // Optional: Re-throw to force caller to handle
    throw e
}
```

**Why This Matters**:
- ERROR level gets developer attention
- WARNING gets lost in logs
- Silent failures cause data inconsistency
- Re-throwing ensures callers handle failures

**Test After Fix**:
```
1. Compile: ./gradlew compileDebugKotlin
2. Logcat: Create invoice with bad data → Should see CRITICAL error
3. Verify: Error is visible in Logcat, not silent
```

---

## PHASE 2: DATA CONSISTENCY FIXES (1-2 hours) 🟠

### Action 2.1: Add Payment Validation (20 minutes)

**File**: `InvoiceDetailViewModel.kt`  
**Method**: `recordPayment(amount: Long)`

**Current Code (BROKEN)**:
```kotlin
fun recordPayment(amount: Long) {
    val currentState = uiState.value as? InvoiceDetailUiState.Success ?: return
    val invoice = currentState.data
    
    viewModelScope.launch {
        try {
            val newAmountPaid = invoice.amountPaid + amount  // NO VALIDATION!
            // ... continues
        }
    }
}
```

**Fixed Code**:
```kotlin
fun recordPayment(amount: Long) {
    val currentState = uiState.value as? InvoiceDetailUiState.Success ?: return
    val invoice = currentState.data
    
    viewModelScope.launch {
        try {
            // ✅ ADD VALIDATION
            if (amount <= 0) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Amount must be greater than 0"))
                return@launch
            }
            
            val remaining = invoice.totalAmount - invoice.amountPaid
            if (amount > remaining) {
                _uiEvent.emit(UiEvent.ShowSnackbar(
                    "Payment cannot exceed remaining balance: ${CentsFormatter.formatCents(remaining)}"
                ))
                return@launch
            }
            
            val newAmountPaid = invoice.amountPaid + amount
            invoiceRepo.updateAmountPaid(invoice.id, newAmountPaid).getOrThrow()
            
            _uiEvent.emit(UiEvent.ShowSnackbar(
                "Payment of ${CentsFormatter.formatCents(amount)} recorded"
            ))
        } catch (e: Exception) {
            _uiEvent.emit(UiEvent.ShowSnackbar("Failed to record payment: ${e.message}"))
        }
    }
}
```

**Test After Fix**:
```
1. Compile: ./gradlew compileDebugKotlin
2. Create invoice: A$100
3. Try payment of A$150 → Should show error
4. Try payment of A$50 → Should succeed
5. Try payment of A$60 → Should show error (exceeds remaining A$50)
```

---

### Action 2.2: Add Snapshot Creation Verification (30 minutes)

**File**: `InvoiceRepositoryImpl.kt`  
**Method**: `saveInvoice()`

**Current Code (RISKY)**:
```kotlin
invoiceDao.insertInvoice(invoiceEntity)
snapshotSyncHelper.syncAllSnapshots(invoice, activeBusinessId)  // No check if succeeded
return@runCatching invoiceId
```

**Fixed Code**:
```kotlin
invoiceDao.insertInvoice(invoiceEntity)

// ✅ TRY to sync snapshots with verification
try {
    snapshotSyncHelper.syncAllSnapshots(invoice, activeBusinessId)
    
    // ✅ VERIFY snapshots were created
    val snapshot = paymentDao.getSnapshotByInvoiceId(invoiceId)
    if (snapshot == null) {
        Timber.e("❌ CRITICAL: Snapshot not created for invoice $invoiceId after sync")
        throw Exception("Snapshot creation failed for invoice $invoiceId")
    }
    Timber.d("✅ Verified: Snapshots created for invoice $invoiceId")
} catch (e: Exception) {
    Timber.e(e, "❌ CRITICAL: Failed to sync/verify snapshots for invoice $invoiceId")
    throw e  // Ensure invoice creation fails if snapshots can't be created
}

return@runCatching invoiceId
```

**Test After Fix**:
```
1. Compile: ./gradlew compileDebugKotlin
2. Create invoice → Check Logcat for "Verified: Snapshots created"
3. Check database: Should have entries in snapshot tables
4. Simulate failure: Mock snapshot creation failure → Should see CRITICAL error
```

---

### Action 2.3: Add Aging Bucket Validation (15 minutes)

**File**: `PaymentAnalyticsRepositoryImpl.kt`  
**Method**: `getPaymentAnalytics()`  
**Location**: Line 165-170

**Current Code (RISKY)**:
```kotlin
outstandingByAging = OutstandingByAging(
    current = agingRow.current,
    past30 = agingRow.past30,
    past60 = agingRow.past60,
    past90 = agingRow.past90,
    totalOutstanding = metricsRow.outstanding  // Never verified!
)
```

**Fixed Code**:
```kotlin
val bucketSum = agingRow.current + agingRow.past30 + agingRow.past60 + agingRow.past90

// ✅ VALIDATE bucket sum matches total
if (Math.abs(bucketSum - metricsRow.outstanding) > 0.01) {  // Allow 1 cent difference
    Timber.w("""
        ⚠️ AGING BUCKET MISMATCH DETECTED
        Bucket Sum: ${CentsFormatter.formatCents(bucketSum.toLong())}
        Total Outstanding: ${CentsFormatter.formatCents(metricsRow.outstanding.toLong())}
        Difference: ${CentsFormatter.formatCents(Math.abs(bucketSum - metricsRow.outstanding).toLong())}
        
        This indicates snapshots may be stale or inconsistent.
        Recommend running snapshot rebuild.
    """.trimIndent())
}

outstandingByAging = OutstandingByAging(
    current = agingRow.current,
    past30 = agingRow.past30,
    past60 = agingRow.past60,
    past90 = agingRow.past90,
    totalOutstanding = metricsRow.outstanding
)
```

**Test After Fix**:
```
1. Compile: ./gradlew compileDebugKotlin
2. Check payment analytics
3. Logcat: Should show bucket validation results
4. Simulate stale data: Manually update snapshot to wrong value
5. Check analytics: Should warn about mismatch
```

---

## PHASE 3: LONG-TERM FIX (4-6 hours) 🟡

### Action 3.1: Migrate GUI1 to Use InvoiceDaoV2 (4-6 hours)

**Objective**: Remove snapshot dependency from GUI1, ensure consistency

**Current Architecture**:
```
GUI1 → Snapshot tables → Stale data risk
GUI2 → InvoiceDaoV2 → Always fresh
```

**New Architecture**:
```
GUI1 → InvoiceDaoV2 → Always fresh ✅
GUI2 → InvoiceDaoV2 → Always fresh ✅
```

**Changes Required**:

1. **PaymentAnalyticsRepositoryImpl**: Use InvoiceDaoV2 instead of paymentDao
2. **RevenueDashboardViewModel**: Use RevenueRepositoryV2 instead of old impl
3. **RiskDashboardViewModel**: Use RiskAnalyticsRepositoryV2
4. **CustomerSegmentationScreen**: Use customer queries directly

**Timeline**:
- Update 3-4 repository methods: 2 hours
- Update 2-3 ViewModels: 1.5 hours
- Test and verify: 1 hour
- Fix any edge cases: 1 hour

---

## TESTING CHECKLIST

After completing each phase, run these tests:

```
☐ Build succeeds: ./gradlew clean compileDebugKotlin
☐ Tests pass: ./gradlew testDebugUnitTest
☐ APK builds: ./gradlew assembleDebug
☐ GUI1 launches without errors
☐ GUI2 launches without errors
☐ Can switch between GUI1 and GUI2
☐ Invoice operations work in both GUIs
☐ Payment analytics consistent in both GUIs
☐ Create invoice A$100 in GUI1, view in GUI2 → same amount
☐ Record payment A$50 in GUI1, check outstanding in GUI2 → A$50
☐ Logcat: No ERROR messages about sync failures
☐ Database: Snapshots created for all invoices
```

---

## QUICK TIME SUMMARY

| Phase | Task | Time | Impact |
|-------|------|------|--------|
| 1.1 | Fix StatusUpdateMenuV2 | 30 min | ✅ Can update status in GUI2 |
| 1.2 | Fix customer screens | 1 hour | ✅ Can edit customers in GUI2 |
| 1.3 | Fix business profile | 30 min | ✅ Can edit profile in GUI2 |
| 1.4 | Promote sync errors | 10 min | ✅ See failures in logs |
| **Phase 1 Total** | **Critical Fixes** | **~3 hours** | **GUI2 runnable** |
| 2.1 | Payment validation | 20 min | ✅ Prevent bad data |
| 2.2 | Snapshot verification | 30 min | ✅ Catch silent failures |
| 2.3 | Aging bucket validation | 15 min | ✅ Detect inconsistency |
| **Phase 2 Total** | **Data Consistency** | **~1.5 hours** | **Data safety** |
| 3.1 | Remove snapshots | 4-6 hours | ✅ Perfect consistency |
| **Phase 3 Total** | **Long-term** | **4-6 hours** | **Production-ready** |
| **TOTAL** | **All Phases** | **8-12 hours** | **Both GUIs safe** |

---

## EXECUTION ORDER

**Recommended Execution Sequence**:

1. **Day 1 Morning (3 hours)**: Complete Phase 1
   - Actions 1.1-1.4 in order
   - Verify GUI2 compiles after each fix
   - Test in emulator

2. **Day 1 Afternoon (2 hours)**: Complete Phase 2
   - Actions 2.1-2.3 in order
   - Run tests after each action
   - Verify data consistency

3. **Day 2 (4-6 hours)**: Complete Phase 3
   - Action 3.1 - migrate to InvoiceDaoV2
   - Comprehensive testing
   - Verify all features work in both GUIs

---

**Status**: ACTION PLAN COMPLETE ✅  
**Ready to Execute**: YES ✅


