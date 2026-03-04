# Week 3: Quick Reference Card

**Print this or keep open while learning**

---

## 🚀 Get Started in 5 Minutes

```bash
# Step 1: Check database migrations are safe
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
grep -r "fallbackToDestructiveMigration" app/
# Expected: No results ✅

# Step 2: Run existing tests
./gradlew test
# Expected: BUILD SUCCESSFUL ✅

# Step 3: Open first learning document
# File: docs/WEEK3_MIGRATIONS_AND_TESTING.md
# Read: "10 Essential Unit Tests" section (30 min)

# Step 4: Create first test
# Copy from: CoreUnitTests.kt line 52-93
# Run: ./gradlew :app:testDebugUnitTest -k "createInvoice_validData"
# Expected: 1 passed ✅

# Step 5: Celebrate! 🎉
```

---

## 📋 10 Tests You're Writing

```
1. ✅ CreateInvoice_ValidData_SavesSuccessfully
2. ✅ CreateInvoice_EmptyItems_ValidationFails
3. ✅ SaveCustomer_ValidData_SavesAndRetrieves
4. ✅ CalculateInvoiceTotal_MultipleItems_CalculatesCorrect
5. ✅ FormatCurrency_CentsToDisplay_FormatsCorrectly
6. ✅ GetAllCustomers_WithData_ReturnsAll
7. ✅ ValidateCustomer_InvalidEmail_Fails
8. ✅ GetActiveBusinessProfile_HasProfile_ReturnsActive
9. ✅ SwitchTheme_NewColor_Updates
10. ✅ QueryInvoicesByCustomer_MultipleInvoices_ReturnsAll
```

---

## 🗂️ Your Files

```
Learn:
  docs/WEEK3_MIGRATIONS_AND_TESTING.md ......... 2000+ lines, everything
  docs/MIGRATION_TESTING_GUIDE.md ............ How to test migrations
  docs/WEEK3_COMPLETE.md ................... Summary + paths
  docs/TESTING_INDEX.md .................... Navigation hub

Code:
  app/src/test/java/com/emul8r/bizap/CoreUnitTests.kt
    └─ 10 complete test methods (copy-paste ready)
  
  app/src/test/java/com/emul8r/bizap/domain/validation/TestDataFactory.kt
    └─ All test data factories (already updated)
```

---

## ⚡ Most Important Commands

```bash
# Run all tests
./gradlew test

# Run just your CoreUnitTests
./gradlew :app:testDebugUnitTest -k CoreUnitTests

# Run one test
./gradlew :app:testDebugUnitTest -k "createInvoice_validData"

# See coverage
./gradlew testDebugUnitTestCoverage

# View HTML report
# File: app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html
```

---

## 🎯 Your Database Status

```
✅ Migrations: SAFE
   - No fallbackToDestructiveMigration()
   - Using explicit migrations (v21→22→23→24)
   - All documented

⚠️ Migration v23→24: NEEDS TESTING
   - Converts Double to Long (100x multiplier)
   - Critical: test before shipping
   - Templates in MIGRATION_TESTING_GUIDE.md

✅ Test Factories: READY
   - Invoice factories (6 types)
   - Customer factories (8 types)
   - LineItem factories (6 types)
   - BusinessProfile factories (3 types)
```

---

## 📝 Test Template (Copy This)

```kotlin
@Test
fun testName_scenario_expectedResult() {
    // ARRANGE: Set up test data
    val input = TestDataFactory.createValidXxx()
    
    // ACT: Execute function
    val result = functionUnderTest(input)
    
    // ASSERT: Verify result
    assertTrue("What we expect", result.isSuccess())
}
```

---

## 🔍 What Each Test Is Testing

| Test | What | Why |
|------|------|-----|
| 1 | Invoice creation | Core feature, happy path |
| 2 | Validation | Catch invalid data |
| 3 | Customer save | Core feature |
| 4 | Total calculation | Math correctness |
| 5 | Currency format | User-facing display |
| 6 | Database query | Data persistence |
| 7 | Email validation | Input validation |
| 8 | Active profile | Business logic |
| 9 | Theme switching | User preferences |
| 10 | Customer invoices | Complex query |

---

## ❌ Common Mistakes (Avoid These)

```kotlin
// ❌ DON'T: Use System.currentTimeMillis() in tests
val invoice = Invoice(date = System.currentTimeMillis())
// Results differ each run - non-deterministic!

// ✅ DO: Use TestDataFactory
val invoice = TestDataFactory.createValidInvoice()
// Same data every time - deterministic!

// ❌ DON'T: Test implementation details
assertTrue(invoice.id == 1)

// ✅ DO: Test behavior
assertTrue(result.isSuccess())

// ❌ DON'T: Multiple assertions without context
assertEquals(1, value1)
assertEquals(2, value2)
assertEquals(3, value3)

// ✅ DO: One thing per test
@Test
fun testOne() { assertEquals(1, value1) }
@Test  
fun testTwo() { assertEquals(2, value2) }
```

---

## 📊 Test Anatomy

```kotlin
@Test
fun testName_condition_result() {
    // ARRANGE ← Set up (TestDataFactory helps here)
    val customer = TestDataFactory.createValidCustomer()
    
    // ACT ← Do the thing
    val saved = customerRepository.save(customer)
    
    // ASSERT ← Verify
    assertNotNull(saved)
    assertEquals(123, saved)
}
```

Every test follows this: **Arrange → Act → Assert**

---

## 🎓 Key Terms

| Term | Meaning |
|------|---------|
| **Unit Test** | Test one thing in isolation |
| **@Test** | JUnit annotation, marks test method |
| **Mock** | Fake object that records calls |
| **TestDataFactory** | Creates consistent test data |
| **assertEquals()** | Assert two values are equal |
| **assertTrue()** | Assert value is true |
| **Migration** | Database schema change |
| **ARRANGE-ACT-ASSERT** | Test structure pattern |

---

## 🚦 Test Execution Status Codes

```
✓ Test passed
✗ Test failed
⊙ Test skipped
⚠ Test warning

BUILD SUCCESSFUL = All tests passed ✅
BUILD FAILED = At least one test failed ❌
```

---

## 💾 Saving Your Progress

```bash
# After you write a test, save it:
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
git add app/src/test/java/com/emul8r/bizap/CoreUnitTests.kt
git commit -m "feat: Add unit test for [feature name]"
git push origin main

# Before writing next test:
git pull origin main
```

---

## 📞 Where to Find Help

| Problem | Solution |
|---------|----------|
| Import errors | Read TDD_LEARNING_GUIDE.md section "JUnit Annotations" |
| Test won't run | Check file location: `app/src/test/java/` |
| Assertion fails | Read test comments, understand what it tests |
| Factory not found | Make sure TestDataFactory.kt is updated |
| Migration questions | Read MIGRATION_TESTING_GUIDE.md |
| General questions | Read WEEK3_MIGRATIONS_AND_TESTING.md |

---

## ✅ Before You Start

```bash
# Checklist:
[ ] Have read WEEK3_COMPLETE.md
[ ] Know which learning path you'll follow
[ ] Have all docs open in browser/editor
[ ] Know where CoreUnitTests.kt is
[ ] Can run: ./gradlew test successfully
[ ] Understand ARRANGE-ACT-ASSERT pattern
[ ] Know how to use TestDataFactory
```

---

## 🎯 Success Metric

```
Week 3 Success = Running 10 tests successfully

$ ./gradlew test
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

BUILD SUCCESSFUL ✅
10 passed in 1.2s
```

---

**Now go write your first test!** 🚀


