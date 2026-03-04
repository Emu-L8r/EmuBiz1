# Running Tests: Complete Guide

**Date:** March 5, 2026  
**For:** First-time test runners

---

## 🚀 Quick Start (30 seconds)

```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
./gradlew test
```

**Expected output:**
```
BUILD SUCCESSFUL
10 passed
```

Done! Your tests passed! 🎉

---

## 📋 The 7 Most Important Commands

### 1. Run ALL Tests
```bash
./gradlew test
```

**What it does:**
- Runs all unit tests in your project
- Compiles test code
- Executes each @Test method
- Reports results

**Takes:** 30-60 seconds first time, 5-10 seconds subsequent

**Output location:** `build/reports/tests/testDebugUnitTest/index.html`

---

### 2. Run ONE Test File
```bash
./gradlew :app:testDebugUnitTest -k CoreUnitTests
```

**What it does:**
- Runs only tests in CoreUnitTests.kt
- Ignores all other test files

**Takes:** 5-10 seconds

**Use when:** You're working on one test class

---

### 3. Run ONE Test Method
```bash
./gradlew :app:testDebugUnitTest -k "createInvoice_validData"
```

**What it does:**
- Runs ONLY the test named `createInvoice_validData_savesSuccessfully`
- Ignores all other tests

**Takes:** 3-5 seconds

**Use when:** You're debugging a specific test

---

### 4. Run Tests WITH Coverage Report
```bash
./gradlew testDebugUnitTestCoverage
```

**What it does:**
- Runs all tests
- Measures code coverage (what % of code is tested)
- Generates HTML report

**Takes:** 60-90 seconds

**Output location:** `app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html`

---

### 5. Run Tests WITH Detailed Output
```bash
./gradlew test --info
```

**What it does:**
- Runs all tests
- Shows MORE details (import statements, setup, etc.)

**Takes:** 30-60 seconds + extra time for logging

**Use when:** Debugging mysterious failures

---

### 6. Run Tests and STOP on First Failure
```bash
./gradlew test --fail-fast
```

**What it does:**
- Runs tests
- Stops immediately when any test fails
- Doesn't run remaining tests

**Takes:** Varies (stops early)

**Use when:** You have many failing tests and want to focus on first failure

---

### 7. Run Tests in Watch Mode (Continuous)
```bash
./gradlew test --watch
```

**What it does:**
- Watches for file changes
- Re-runs tests automatically when you save a file
- Useful for TDD workflow

**Takes:** Continuous (until you stop)

**Use when:** Writing tests and immediately seeing results

---

## 📊 Understanding Test Output

### SUCCESS Output

```
> Task :app:testDebugUnitTest
...
✓ createInvoice_validData_savesSuccessfully
✓ createInvoice_emptyItems_validationFails
✓ saveCustomer_validData_savesAndRetrieves
✓ calculateInvoiceTotal_multipleItems_calculatesCorrect
✓ formatCurrency_centsToDisplay_formatsCorrectly
✓ getAllCustomers_withData_returnsAll
✓ validateCustomer_invalidEmail_fails
✓ getActiveBusinessProfile_hasProfile_returnsActive
✓ switchTheme_newColor_updates
✓ queryInvoicesByCustomer_multipleInvoices_returnsAll

BUILD SUCCESSFUL
10 passed in 1.2s
```

**What this means:**
- ✓ All 10 tests passed
- BUILD SUCCESSFUL = Everything is working
- 1.2s = How long tests took

**Your next action:** Celebrate! 🎉

---

### FAILURE Output

```
> Task :app:testDebugUnitTest
...
✗ createInvoice_validData_savesSuccessfully
  AssertionError: expected success but got failure
  at CoreUnitTests.kt:93
  
Expected:
  123
  
Actual:
  null

BUILD FAILED
1 failed, 9 passed in 2.1s
```

**What this means:**
- ✗ One test failed
- BUILD FAILED = At least one test failed
- 9 passed, 1 failed = Overall results
- File and line number shown (CoreUnitTests.kt:93)

**Your next action:** Click the link or read error message

---

### COMPILATION ERROR Output

```
> Task :app:compileDebugUnitTestKotlin FAILED
e: file:///C:/Users/Saucey/Documents/GitHub/EmuBiz/Bizap/app/src/test/java/com/emul8r/bizap/CoreUnitTests.kt:10:5
error: unresolved reference: TestDataFactory
```

**What this means:**
- Code won't compile
- TestDataFactory can't be found
- Tests didn't even run yet

**Your next action:** Fix import or file location

---

## 🔍 Where to Find Test Results

### 1. Console Output
```
Terminal/PowerShell shows results immediately
- Green ✓ = passed
- Red ✗ = failed
- Yellow ⚠ = warning
```

### 2. HTML Reports

**After** running tests, find these files:

**All tests results:**
```
app/build/reports/tests/testDebugUnitTest/index.html
```

Open in browser to see:
- Overall pass/fail
- Each test result
- Error details
- Stack traces

**Coverage report:**
```
app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html
```

Open in browser to see:
- What % of code is tested
- Which classes/methods lack coverage
- Coverage by package

---

## ⚠️ What Success Looks Like

### ✅ All Tests Pass

```bash
$ ./gradlew test
...
BUILD SUCCESSFUL ✅
10 passed in 1.2s
```

**Celebrate!** Everything is working correctly.

### ⚠️ Some Tests Fail

```bash
$ ./gradlew test
...
✗ createInvoice_validData_savesSuccessfully
  AssertionError: ...

BUILD FAILED ❌
1 failed, 9 passed
```

**Don't panic!** This is normal. Read the error message.

### 🔴 No Tests Run

```bash
$ ./gradlew test
...
BUILD SUCCESSFUL ✅
0 passed
```

**Problem:** Tests exist but didn't run. Check:
- Are test files in `app/src/test/java/`?
- Do test methods have `@Test` annotation?
- Did compilation error prevent running?

---

## 🐛 Debugging a Failing Test

### Step 1: Read the Error Message

```
AssertionError: expected success but got failure
at CoreUnitTests.kt:93
```

**This tells you:**
- WHAT failed: "expected success but got failure"
- WHERE failed: CoreUnitTests.kt, line 93
- Click the link in IDE to jump to that line

### Step 2: Read the Test Comments

In CoreUnitTests.kt, the test has comments:

```kotlin
/**
 * TEST 1: INVOICE CREATION - HAPPY PATH
 *
 * User Flow:
 * 1. Select customer
 * 2. Add items
 * 3. Click Save
 * 4. Expect: Success
 */
@Test
fun createInvoice_validData_savesSuccessfully() {
    // ARRANGE
    val invoice = TestDataFactory.createValidInvoice()
    
    // ACT
    val result = invoiceRepository.saveInvoice(invoice)
    
    // ASSERT
    assertEquals(123L, result)  // ← Line 93 (this assertion failed)
}
```

**Now understand:**
- What the test is testing
- What ARRANGE sets up
- What ACT does
- What ASSERT expects

### Step 3: Run Just This Test

```bash
./gradlew :app:testDebugUnitTest -k "createInvoice_validData"
```

**Why:** Faster feedback loop for debugging

### Step 4: Check Test Data

```kotlin
val invoice = TestDataFactory.createValidInvoice()
```

**Question:** Is this test data correct?
- Open TestDataFactory.kt
- Check createValidInvoice()
- Verify all fields are set

### Step 5: Check the Mock

```kotlin
whenever(invoiceRepository.saveInvoice(invoice)).thenReturn(123L)
```

**Question:** Is the mock configured correctly?
- Is saveInvoice being mocked?
- Does it return the right value?
- Is the assertion checking the right value?

### Step 6: Add Debug Logging

```kotlin
@Test
fun createInvoice_validData_savesSuccessfully() {
    // ARRANGE
    val invoice = TestDataFactory.createValidInvoice()
    println("DEBUG: Invoice created: $invoice")  // ← Add this
    
    // ACT
    val result = invoiceRepository.saveInvoice(invoice)
    println("DEBUG: Saved result: $result")  // ← Add this
    
    // ASSERT
    assertEquals(123L, result)
}
```

**Why:** See what values are actually being used

### Step 7: Fix the Test

Once you understand the issue:
- Fix the test data
- Fix the mock
- Fix the assertion
- Or fix the code being tested

---

## 🚨 Common Issues & Fixes

### Issue 1: "Unresolved reference: TestDataFactory"

**Error:**
```
error: unresolved reference: TestDataFactory
```

**Cause:** Missing import

**Fix:**
```kotlin
// Add this at top of file
import com.emul8r.bizap.domain.validation.TestDataFactory
```

### Issue 2: "Cannot find symbol: @Test"

**Error:**
```
error: cannot find symbol: @Test
```

**Cause:** Missing JUnit import

**Fix:**
```kotlin
// Add this at top of file
import org.junit.Test
import org.junit.Assert.*
```

### Issue 3: "Test file not found"

**Error:**
```
Task :app:testDebugUnitTest FAILED
No tests found
```

**Cause:** Test file in wrong location

**Fix:** Move test file to:
```
app/src/test/java/com/emul8r/bizap/CoreUnitTests.kt
       ↑ MUST be "test", not "main"
```

### Issue 4: "Gradle sync failed"

**Error:**
```
Failed to sync Gradle
```

**Cause:** Build configuration error

**Fix:**
1. Click "Sync Now" in Android Studio banner
2. Check for red squiggles in build.gradle.kts
3. Try: `./gradlew clean` then `./gradlew test`

### Issue 5: "Test won't run in IDE"

**Problem:** Command line `./gradlew test` works, but IDE won't run tests

**Fix:**
1. Right-click test class → "Run 'CoreUnitTests'"
2. Or right-click test method → "Run 'testName()'"
3. If that fails, try: File → Invalidate Caches → Restart

---

## 📱 Unit Tests vs Instrumented Tests

### Unit Tests (What You're Running)

**Location:** `app/src/test/java/`
**Run with:** `./gradlew test`
**Environment:** JVM (your computer)
**Speed:** Fast (1-2 seconds)
**No device needed:** ✅
**Can test:** Business logic, calculations, validation

**Example:**
```kotlin
@Test
fun calculateTotal_multipleItems_correct() {
    val total = calculateTotal(items)
    assertEquals(250, total)
}
```

### Instrumented Tests (Not what you're doing yet)

**Location:** `app/src/androidTest/java/`
**Run with:** `./gradlew connectedAndroidTest`
**Environment:** Android device or emulator
**Speed:** Slow (30-60 seconds per test)
**Requires device:** ❌ (Android emulator or phone)
**Can test:** UI, Android APIs, actual Android behavior

**Example:**
```kotlin
@Test
fun clickButton_navigationWorks() {
    onView(withId(R.id.save_button)).perform(click())
    onView(withText("Success")).check(matches(isDisplayed()))
}
```

**For Week 3:** You're writing **unit tests** only. That's correct! ✅

---

## 💾 Should I Commit Failing Tests?

### ❌ DON'T Commit Test Failures to Main

**Bad:**
```bash
git add -A
git commit -m "Add new tests"
git push origin main
# But some tests are failing! ❌
```

**Why:** Breaks CI/CD, confuses teammates, wastes time

### ✅ DO Commit Passing Tests Only

**Good:**
```bash
# 1. Write test
# 2. Run test
# 3. Fix test until it passes
$ ./gradlew test
BUILD SUCCESSFUL ✅

# 4. Commit passing test
git add app/src/test/java/com/emul8r/bizap/CoreUnitTests.kt
git commit -m "feat: Add invoice creation test"
git push origin main
```

### ✅ OK: Commit Failing Tests on Feature Branch

**Acceptable:**
```bash
git checkout -b feature/new-tests
# Write failing tests
git push origin feature/new-tests
# Create PR
# Teammate reviews
# Fix tests on branch
# PR approved
# Merge to main
```

**Why:** PR reviewers see the journey from failing → passing

---

## 🎯 Test Running Workflow

### Day-to-Day Workflow

```bash
# 1. Write or modify test
# (Edit CoreUnitTests.kt)

# 2. Run the test immediately
./gradlew :app:testDebugUnitTest -k "testName"

# 3. Does it pass?
# YES → Commit
git add app/src/test/java/com/emul8r/bizap/CoreUnitTests.kt
git commit -m "feat: Add test for X"

# NO → Debug and fix
# (Read error, check test data, fix code)
# Then run test again
./gradlew :app:testDebugUnitTest -k "testName"
```

### Before Pushing to GitHub

```bash
# Run ALL tests
./gradlew test

# Expected: BUILD SUCCESSFUL ✅
# If not: Fix failing tests

# Then push
git push origin main
```

---

## 📊 Reading Test Output in Detail

### Verbose Output Example

```bash
$ ./gradlew test

> Task :app:preBuild
> Task :app:preDebugBuild
> Task :app:generateDebugBuildConfig
> Task :app:generateDebugResources
...
[many more tasks]
...
> Task :app:compileDebugUnitTestKotlin
> Task :app:testDebugUnitTest

✓ createInvoice_validData_savesSuccessfully
  in 234 ms

✓ createInvoice_emptyItems_validationFails
  in 156 ms

✓ saveCustomer_validData_savesAndRetrieves
  in 189 ms

✓ calculateInvoiceTotal_multipleItems_calculatesCorrect
  in 98 ms

✓ formatCurrency_centsToDisplay_formatsCorrectly
  in 67 ms

✓ getAllCustomers_withData_returnsAll
  in 145 ms

✓ validateCustomer_invalidEmail_fails
  in 123 ms

✓ getActiveBusinessProfile_hasProfile_returnsActive
  in 76 ms

✓ switchTheme_newColor_updates
  in 89 ms

✓ queryInvoicesByCustomer_multipleInvoices_returnsAll
  in 134 ms

BUILD SUCCESSFUL
10 passed in 1.2s
```

**What this means:**
- ✓ Each checkmark = one test passed
- Times show how long each test took
- 1.2s = Total time for all 10 tests
- BUILD SUCCESSFUL = All tests passed

---

## ✅ Checklist: Running Tests Successfully

- [ ] Can run: `./gradlew test`
- [ ] Understand output (✓ = passed, ✗ = failed)
- [ ] Know where to find results (console)
- [ ] Can run single test file: `./gradlew :app:testDebugUnitTest -k ClassName`
- [ ] Can run single test: `./gradlew :app:testDebugUnitTest -k "testName"`
- [ ] Know how to read error messages
- [ ] Know how to debug failures
- [ ] Will commit only passing tests
- [ ] Will run all tests before pushing to GitHub
- [ ] Understand difference between unit and instrumented tests

---

## 🎓 Key Learnings

1. **`./gradlew test`** runs all unit tests
2. **`-k "name"`** filters to specific tests (faster)
3. **Console output** shows results immediately
4. **HTML reports** provide detailed analysis
5. **All tests must pass** before committing
6. **Failures are normal** - debug and fix
7. **One test at a time** - focus on single failures
8. **Fast feedback loop** - run tests frequently

---

## 🚀 You're Ready!

You now know how to:
- ✅ Run all tests
- ✅ Run specific tests
- ✅ Read test output
- ✅ Debug failures
- ✅ Check coverage
- ✅ Commit results

**Go write and run your first test!** 🧪


