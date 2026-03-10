# 🛠️ TEST SUITE FIX CHECKLIST
**Date:** March 10, 2026  
**Scope:** Fix 40+ test compilation errors  
**Estimated Time:** 4-6 hours  
**Priority:** CRITICAL (blocks production deployment)

---

## 📋 TEST FILES WITH ERRORS

### ✅ ALREADY FIXED
- [x] `NavigationTest.kt` - Fixed `dataStore.edit()` syntax (line 185, 192)
- [x] `InvoiceRepositoryTest.kt` - Added missing `InvoiceApi` import

---

### ⏳ NEEDS FIXING (8 files, 40+ errors)

#### 1. **PaymentRepositoryTest.kt** (5 errors)
**File:** `app/src/test/java/com/emul8r/bizap/data/repository/PaymentRepositoryTest.kt`

**Errors:**
- Line 5: `Unresolved reference 'any'`
- Line 8: `Unresolved reference 'eq'`
- Lines 65-155: MockK matcher invocations failing

**Status:** PARTIALLY FIXED (imports added, but test methods need rewriting)

**What's Wrong:**
- Currently mocking the repository itself
- Should be mocking the DAO and testing real repository behavior
- Using `io.mockk.any()` fully qualified instead of imported `any()`

**Fix Needed:**
```kotlin
// ❌ WRONG: Mocking the repository
private lateinit var paymentRepository: PaymentRepositoryV2

@Before
fun setUp() {
    paymentRepository = mockk(relaxed = true)  // Wrong!
}

// ✅ RIGHT: Mock the DAO, create real repository
private val paymentDao: InvoicePaymentDao = mockk(relaxed = true)
private lateinit var paymentRepository: PaymentRepositoryV2

@Before
fun setUp() {
    paymentRepository = PaymentRepositoryV2(paymentDao)  // Real instance!
}
```

**Action Items:**
- [ ] Review PaymentRepositoryV2 constructor signature
- [ ] Replace `mockk(relaxed = true)` with real instantiation
- [ ] Update all `coEvery` statements to mock DAO methods instead of repository
- [ ] Update all `coVerify` statements to verify DAO calls
- [ ] Run test to verify compilation

**Time Estimate:** 30 minutes

---

#### 2. **RecordPaymentUseCaseTest.kt** (20+ errors)
**File:** `app/src/test/java/com/emul8r/bizap/domain/usecase/RecordPaymentUseCaseTest.kt`

**Errors:**
- Lines 88-92: `Unresolved reference 'any'` (5 errors)
- Lines 140-144: `Unresolved reference 'any'` (5 errors)
- Lines 228-232: `Unresolved reference 'any'` (5 errors)
- Lines 247-251: `Unresolved reference 'any'` (5 errors)
- Lines 261, 265: `Unresolved reference 'any'`, `eq`

**Status:** NOT FIXED

**What's Wrong:**
- Same issue as PaymentRepositoryTest - using fully qualified MockK calls
- Missing proper imports (or imports not resolving)

**Fix Pattern:**
```kotlin
// ❌ WRONG
paymentRepository.recordPayment(
    invoiceId = io.mockk.any(),
    businessId = io.mockk.any(),
    ...
)

// ✅ RIGHT
paymentRepository.recordPayment(
    invoiceId = any(),
    businessId = any(),
    ...
)
```

**Action Items:**
- [ ] Check that `io.mockk.any` and `io.mockk.eq` are imported
- [ ] Replace all `io.mockk.any()` with `any()`
- [ ] Replace all `io.mockk.eq()` with `eq()`
- [ ] Verify test base class (`BaseUnitTest`) is properly extended
- [ ] Run test to verify compilation

**Time Estimate:** 20 minutes

---

#### 3. **DualGUINavigationTest.kt** (4 errors)
**File:** `app/src/test/java/com/emul8r/bizap/navigation/DualGUINavigationTest.kt`

**Errors:**
- Line 15: `Unresolved reference 'any'`
- Line 152: `Unresolved reference 'edit'` + type inference issues
- Line 160: `Unresolved reference 'edit'` + type inference issues

**Status:** NOT FIXED

**What's Wrong:**
- Line 152-160: Using `dataStore.edit()` incorrectly (same as NavigationTest)
- Already fixed in NavigationTest, need to apply same fix here

**Fix Pattern:**
```kotlin
// ❌ WRONG
coEvery { dataStore.edit<Preferences>(any()) } returns emptyPreferences()

// ✅ RIGHT
coEvery { dataStore.edit(any()) } returns emptyPreferences()
```

**Action Items:**
- [ ] Add `import io.mockk.any` if missing
- [ ] Replace `dataStore.edit<Preferences>(any())` with `dataStore.edit(any())`
- [ ] Run test to verify compilation

**Time Estimate:** 10 minutes

---

#### 4. **CreateCustomerViewModelTest.kt** (4 errors)
**File:** `app/src/test/java/com/emul8r/bizap/ui/gui2/customers/CreateCustomerViewModelTest.kt`

**Errors:**
- Lines 44, 57, 124, 154: `Unresolved reference 'advanceUntilIdle'`

**Status:** NOT FIXED

**What's Wrong:**
- `advanceUntilIdle()` is only available in test scope from `runTest`
- These calls are outside the `runTest` block or test class doesn't extend `BaseUnitTest`

**Fix Pattern:**
```kotlin
// ❌ WRONG
viewModel.someMethod()
testDispatcher.scheduler.advanceUntilIdle()  // Not in runTest block!

// ✅ RIGHT
runTest {
    viewModel.someMethod()
    advanceUntilIdle()  // Now it works!
}
```

**Action Items:**
- [ ] Verify class extends `BaseUnitTest`
- [ ] Ensure `advanceUntilIdle()` calls are inside `runTest` blocks
- [ ] Check that test dispatcher is properly inherited
- [ ] Run test to verify compilation

**Time Estimate:** 15 minutes

---

#### 5. **DashboardViewModelTest.kt** (2 errors)
**File:** `app/src/test/java/com/emul8r/bizap/ui/gui2/dashboard/DashboardViewModelTest.kt`

**Errors:**
- Lines 86-87: `No parameter with name 'totalAmount' found`

**Status:** NOT FIXED

**What's Wrong:**
- Test is using old parameter name that doesn't exist in current code
- Parameter was likely renamed during refactoring but test wasn't updated

**Fix Pattern:**
```kotlin
// ❌ WRONG (old parameter name)
val metrics = RevenueMetrics(
    totalAmount = 1000L,  // This parameter doesn't exist!
    ...
)

// ✅ RIGHT (check actual constructor)
val metrics = RevenueMetrics(
    totalRevenue = 1000L,  // New parameter name
    ...
)
```

**Action Items:**
- [ ] Check `RevenueMetrics` data class definition
- [ ] Find correct parameter name
- [ ] Update test lines 86-87 with correct parameter names
- [ ] Run test to verify compilation

**Time Estimate:** 10 minutes

---

#### 6. **CreateInvoiceScreenV2IntegrationTest.kt** (2 errors)
**File:** `app/src/test/java/com/emul8r/bizap/ui/gui2/invoices/CreateInvoiceScreenV2IntegrationTest.kt`

**Errors:**
- Lines 73, 238: `Argument type mismatch: actual type is 'kotlin.String?', but 'kotlin.String' was expected`

**Status:** NOT FIXED

**What's Wrong:**
- Test is passing nullable String (`String?`) where non-nullable String is required
- Need to either make the parameter nullable or provide non-null value

**Fix Pattern:**
```kotlin
// ❌ WRONG
val nullableString: String? = null
someFunction(nullableString)  // Requires non-null String!

// ✅ RIGHT (Option 1: Use non-null value)
val nonNullString: String = "value"
someFunction(nonNullString)

// ✅ RIGHT (Option 2: Make parameter nullable)
someFunction(nullableString ?: "default")
```

**Action Items:**
- [ ] Check what's being passed on lines 73 and 238
- [ ] Determine if it should be nullable or non-null
- [ ] Either provide non-null value or add null coalescing (`?:`)
- [ ] Run test to verify compilation

**Time Estimate:** 10 minutes

---

#### 7. **OfflineQueueServiceSuite4Test.kt** (1 error)
**File:** `app/src/test/java/com/emul8r/bizap/data/service/OfflineQueueServiceSuite4Test.kt`

**Errors:**
- Line 7: `Unresolved reference 'any'`

**Status:** NOT FIXED

**What's Wrong:**
- Import for `io.mockk.any` is missing or not resolving

**Fix Pattern:**
```kotlin
// Add to imports
import io.mockk.any
```

**Action Items:**
- [ ] Verify `import io.mockk.any` exists at top of file
- [ ] If missing, add it
- [ ] Run test to verify compilation

**Time Estimate:** 5 minutes

---

#### 8. **SyncWorkerTest.kt** (1 error)
**File:** `app/src/test/java/com/emul8r/bizap/data/worker/SyncWorkerTest.kt`

**Errors:**
- Line 9: `Unresolved reference 'any'`

**Status:** NOT FIXED

**What's Wrong:**
- Same as OfflineQueueServiceSuite4Test - missing import

**Action Items:**
- [ ] Verify `import io.mockk.any` exists
- [ ] If missing, add it
- [ ] Run test to verify compilation

**Time Estimate:** 5 minutes

---

## 🔄 FIX EXECUTION PLAN

### Phase 1: Quick Fixes (30 minutes)
```bash
1. OfflineQueueServiceSuite4Test.kt     - Add import
2. SyncWorkerTest.kt                    - Add import
3. DualGUINavigationTest.kt             - Replace dataStore.edit calls
4. DashboardViewModelTest.kt            - Fix parameter names
5. CreateInvoiceScreenV2IntegrationTest.kt - Fix type mismatches
```

### Phase 2: Medium Fixes (45 minutes)
```bash
1. CreateCustomerViewModelTest.kt       - Move advanceUntilIdle into runTest
2. RecordPaymentUseCaseTest.kt          - Replace io.mockk.* calls
```

### Phase 3: Complex Fix (30 minutes)
```bash
1. PaymentRepositoryTest.kt             - Rewrite to test real repository
```

### Phase 4: Verification (30 minutes)
```bash
1. Run full test suite: ./gradlew testDebugUnitTest
2. Fix any runtime errors that appear
3. Verify all tests pass
```

**Total Time:** ~2.5 hours for compilation fixes + 2-3 hours for runtime fixes = **4-5 hours total**

---

## ✅ VERIFICATION CHECKLIST

After completing all fixes, verify:

- [ ] `./gradlew testDebugUnitTest` runs without errors
- [ ] All 200+ unit tests pass
- [ ] No compilation warnings
- [ ] `./gradlew clean build` succeeds without `-x test` flag
- [ ] CI/CD pipeline can run (if applicable)
- [ ] Documentation updated to reflect test status

---

## 📊 COMPLETION TRACKING

| File | Errors | Status | Time Est. | Actual Time |
|------|--------|--------|-----------|-------------|
| NavigationTest.kt | 2 | ✅ DONE | 5 min | - |
| InvoiceRepositoryTest.kt | 1 | ✅ DONE | 5 min | - |
| PaymentRepositoryTest.kt | 5 | ⏳ PARTIAL | 30 min | - |
| RecordPaymentUseCaseTest.kt | 20+ | ⏳ TODO | 20 min | - |
| DualGUINavigationTest.kt | 4 | ⏳ TODO | 10 min | - |
| CreateCustomerViewModelTest.kt | 4 | ⏳ TODO | 15 min | - |
| DashboardViewModelTest.kt | 2 | ⏳ TODO | 10 min | - |
| CreateInvoiceScreenV2IntegrationTest.kt | 2 | ⏳ TODO | 10 min | - |
| OfflineQueueServiceSuite4Test.kt | 1 | ⏳ TODO | 5 min | - |
| SyncWorkerTest.kt | 1 | ⏳ TODO | 5 min | - |

**Overall Progress:** 2/10 files fixed | 3/40+ errors fixed | **7.5% complete**

---

## 🎯 NEXT ACTION

**Pick one file from "Quick Fixes" phase and start:**

1. Open the file in Android Studio
2. Review the error message
3. Apply the fix from this checklist
4. Run `./gradlew testDebugUnitTest` to verify
5. Move to next file

**Estimated time to full completion:** 4-5 hours working systematically

Good luck! 🚀

