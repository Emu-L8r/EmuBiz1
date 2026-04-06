# 🔧 PHASE 2 DETAILED ACTION PLAN - TEST FIXES

**Objective:** Fix all broken tests one by one  
**Priority:** Fix critical tests first (Invoice, Payment, Analytics)  
**Status:** STARTING PHASE 2

---

## 📋 TRIAGE RESULTS & FIX PLAN

### TIER 1: CRITICAL (Core Business Logic)

#### Fix #1: InvoiceRepositoryImplEnhancedTest.kt
**Severity:** 🔴 CRITICAL  
**Affected Feature:** Invoice CRUD operations  
**File:** `app/src/test/java/com/emul8r/bizap/data/repository/InvoiceRepositoryImplEnhancedTest.kt`

**Root Cause Analysis:**
- Test uses mocked DAOs with potentially wrong signatures
- PerformanceMetrics is being used - check if it still exists
- SnapshotSyncHelper is mocked - may have changed
- TestDataFactory usage - verify it still exists

**Fix Strategy:**
1. Verify all mock DAO methods match actual signatures
2. Check PerformanceMetrics class exists
3. Update snapshot mock setup
4. Run test: `./gradlew testDebugUnitTest -k InvoiceRepositoryImplEnhanced`

**Expected Outcome:** Test PASSES or shows specific error to fix

---

#### Fix #2: PaymentRepositoryTest.kt
**Severity:** 🔴 CRITICAL  
**Affected Feature:** Payment recording  
**File:** `app/src/test/java/com/emul8r/bizap/data/repository/PaymentRepositoryTest.kt`

**Root Cause Analysis:**
- Uses in-memory Room database (good!)
- SnapshotSyncHelper is mocked
- PaymentRepositoryV2 constructor dependencies may have changed
- Invoice creation helpers may reference deleted fields

**Fix Strategy:**
1. Verify PaymentRepositoryV2 constructor parameters
2. Verify InvoiceEntity test data creation
3. Check SnapshotSyncHelper mock is compatible
4. Run test individually to see specific errors

**Expected Outcome:** Test PASSES with real database operations

---

### TIER 2: HIGH (Analytics)

#### Fix #3: RevenueRepositoryV2Test.kt
**Severity:** 🟠 HIGH  
**Affected Feature:** Revenue analytics  
**File:** `app/src/test/java/com/emul8r/bizap/gui2/RevenueRepositoryV2Test.kt`

**Root Cause Analysis:**
- Uses stubRevenueMetrics helper (custom mock setup)
- DAO V2 method signatures may have changed
- DailyRevenueTrendV2 entity structure may have changed

**Fix Strategy:**
1. Update stubRevenueMetrics to match actual DAO methods
2. Verify DailyRevenueTrendV2 constructor
3. Check InvoiceStatusCountV2 usage
4. Run test to validate

**Expected Outcome:** Analytics tests PASS

---

### TIER 3: MEDIUM (Navigation & State)

#### Fix #4: LandingPageTest.kt
**Severity:** 🟡 MEDIUM  
**Affected Feature:** GUI mode selection  
**File:** `app/src/test/java/com/emul8r/bizap/ui/landing/LandingPageTest.kt`

**Root Cause Analysis:**
- DataStore mock may be using wrong method names
- GuiMode enum may have changed
- setupBase() function may need updating

**Fix Strategy:**
1. Verify DataStore.data property is mocked correctly
2. Check GuiMode enum still exists and has same values
3. Update mock setup if needed
4. Run test

**Expected Outcome:** Navigation tests PASS

---

#### Fix #5: NavigationTest.kt  
**Severity:** 🟡 MEDIUM  
**Affected Feature:** App navigation  
**File:** `app/src/test/java/com/emul8r/bizap/navigation/DualGUINavigationTest.kt` (or similar)

**Root Cause Analysis:**
- DataStore mocking issues (similar to LandingPageTest)
- Activity intent extras may have changed
- Route names may have changed

**Fix Strategy:**
1. Use same DataStore mock pattern as LandingPageTest (if fixed)
2. Verify activity intent structure
3. Check route definitions
4. Run test

**Expected Outcome:** Navigation tests PASS

---

### TIER 4: DELETE (Non-Critical)

#### Delete #1: InvoiceTemplateRepositoryTest.kt
**Severity:** 🟢 LOW  
**Reason:** Tests non-existent feature  
**Action:** DELETE entire file

```bash
# Move to archive instead of permanent delete
mkdir -p app/src/test_archive
git mv app/src/test/.../InvoiceTemplateRepositoryTest.kt app/src/test_archive/
git commit -m "test: Archive non-critical InvoiceTemplateRepositoryTest"
```

---

## 🛠️ EXECUTION CHECKLIST

### Step 1: Start with Most Critical Test
```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

# Run just the most critical test
./gradlew testDebugUnitTest -k InvoiceRepositoryImplEnhanced --debug 2>&1 | Tee-Object fix_attempt_1.log

# Watch for specific error messages
Select-String "error|Error|FAILED" fix_attempt_1.log
```

### Step 2: Fix Based on Error Message
- [ ] Read error carefully
- [ ] Identify what class/method is broken
- [ ] Check if it still exists in codebase
- [ ] Either: Update the mock OR delete the test

### Step 3: Verify Fix
```bash
./gradlew testDebugUnitTest -k InvoiceRepositoryImplEnhanced
# Should see: TEST PASSED
```

### Step 4: Repeat for Each Test (by Tier)
```
Tier 1 (2 tests):
  ✓ Fix #1: InvoiceRepositoryImplEnhancedTest
  ✓ Fix #2: PaymentRepositoryTest

Tier 2 (1 test):
  ✓ Fix #3: RevenueRepositoryV2Test

Tier 3 (2 tests):
  ✓ Fix #4: LandingPageTest
  ✓ Fix #5: NavigationTest

Tier 4 (1 test):
  ✓ Delete #1: InvoiceTemplateRepositoryTest
```

### Step 5: Final Validation
```bash
./gradlew clean testDebugUnitTest
# Expected: 100% PASS RATE
```

---

## 📝 COMMON FIX PATTERNS

### Pattern A: DAO Method Name Changed
**Error:** `Method doesn't exist`
```kotlin
// OLD (broken)
coEvery { invoiceDao.updateAmount(any()) } returns Unit

// NEW (fixed)  
coEvery { invoiceDao.updateAmountPaid(any(), any()) } just Runs
```

### Pattern B: DataStore Mock Configuration
**Error:** `MockKException during DataStore interaction`
```kotlin
// OLD (broken)
dataStore = mockk(relaxed = true)

// NEW (fixed)
dataStore = mockk()
every { dataStore.data } returns flowOf(emptyPreferences())
```

### Pattern C: Deleted Class
**Error:** `Class not found`
```kotlin
// OLD (broken)
val service = DeletedService()  // ❌ Doesn't exist

// NEW (fixed - delete test OR fix logic)
// OPTION 1: Delete test if feature not needed
// OPTION 2: Mock the dependency or refactor test
```

### Pattern D: Constructor Changed
**Error:** `Constructor parameter mismatch`
```kotlin
// OLD (broken)
PaymentRepositoryV2(oldParam1, oldParam2)

// NEW (fixed)
PaymentRepositoryV2(
    database = database,
    invoiceDaoV2 = database.invoiceDaoV2(),
    paymentDaoV2 = database.paymentDaoV2(),
    snapshotSyncHelper = mockSnapshotSyncHelper
)
```

---

## ⏱️ TIME ESTIMATE BY FIX

| Fix | Tier | Est. Time | Difficulty |
|-----|------|-----------|------------|
| #1: InvoiceRepositoryImplEnhancedTest | 1 | 1-2h | HIGH |
| #2: PaymentRepositoryTest | 1 | 1-2h | MEDIUM |
| #3: RevenueRepositoryV2Test | 2 | 30-45m | LOW |
| #4: LandingPageTest | 3 | 15-30m | LOW |
| #5: NavigationTest | 3 | 15-30m | LOW |
| Delete #1: InvoiceTemplateRepositoryTest | 4 | 5m | TRIVIAL |
| **TOTAL** | | **3-5 hours** | |

---

## 🎯 SUCCESS CRITERIA

✅ All Tier 1 & 2 tests PASS  
✅ All Tier 3 tests PASS  
✅ Tier 4 tests DELETED  
✅ No @Ignore or @Skip decorators remaining  
✅ Full test suite: `./gradlew testDebugUnitTest` = 100% PASS  
✅ No compilation errors  
✅ Zero test failures  

---

## 🚀 READY TO PROCEED?

Once tests pass, you can:
1. Delete GUI1 legacy code
2. Build release APK
3. Deploy to Google Play Store

**Next action:** Start with Fix #1 (InvoiceRepositoryImplEnhancedTest)


