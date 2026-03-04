# Test-Driven Development (TDD) Learning Guide

**Date:** March 5, 2026  
**Focus:** Writing Effective Unit Tests for ValidationRules

---

## 🎯 What is Test-Driven Development?

### The TDD Cycle

```
1. RED: Write a test that fails
        ↓
2. GREEN: Write code to make it pass
        ↓
3. REFACTOR: Improve code quality
        ↓
        (Repeat)
```

### Benefits

```
✅ Forces you to think about requirements first
✅ Catches bugs before they reach production
✅ Documents how code should behave
✅ Makes refactoring safer (tests verify changes)
✅ Improves code design (easier to test = better design)
```

---

## 📝 Anatomy of a Unit Test

### Basic Structure

```kotlin
@Test  // JUnit annotation: marks this method as a test
fun testName_inputCondition_expectedResult() {
    
    // ARRANGE: Set up test data
    val testData = createTestData()
    
    // ACT: Execute the code being tested
    val result = testedFunction(testData)
    
    // ASSERT: Verify the result is correct
    assertEquals(expectedValue, result)
}
```

### Test Naming Convention

```
testName_inputCondition_expectedResult

Examples:
✅ validateInvoice_validInvoice_returnsSuccess
✅ validateInvoice_emptyItems_returnsFailure
✅ validateInvoice_zeroAmount_returnsFailure
❌ test1 (unclear what's being tested)
❌ validateInvoice (incomplete)
```

---

## 🧪 Your First 5 Tests - Detailed Breakdown

### Test 1: Valid Invoice (Happy Path)

```kotlin
@Test
fun validateInvoice_validInvoice_returnsSuccess() {
    // ========================================
    // ARRANGE: Create valid test data
    // ========================================
    // This is our "happy path" - everything is correct
    
    val now = System.currentTimeMillis()
    
    val invoice = Invoice(
        businessProfileId = 1,                    // Required: which business
        customerId = 1,                           // Required: which customer
        customerName = "John Doe",                // Required: not blank
        date = now,                               // Required: invoice created today
        dueDate = now + 86400000,                 // Required: due tomorrow (valid date range)
        totalAmount = 10000,                      // Required: $100 in cents (positive)
        items = listOf(
            LineItem(
                description = "Consulting Services",  // Required: not blank
                quantity = 1.0,                      // Required: positive
                unitPrice = 10000                    // Required: positive (in cents)
            )
        ),
        isQuote = false,                          // Not a quote
        status = InvoiceStatus.DRAFT,             // Draft status
        currencyCode = "AUD"                      // Required: 3 letters (ISO 4217)
    )
    
    // ========================================
    // ACT: Call the function being tested
    // ========================================
    // We're testing: does ValidationRules.validateInvoice() return Success?
    
    val result = ValidationRules.validateInvoice(invoice)
    
    // ========================================
    // ASSERT: Verify the result
    // ========================================
    // Check that the result is Success (not Failure)
    
    assertTrue("Invoice should be valid", result.isSuccess())
    
    // Optional: also verify it's NOT a failure
    assertFalse("Invoice should not fail", result.isFailure())
    
    // Optional: verify error is null
    assertNull("No error message should exist", result.getErrorOrNull())
}
```

**What This Tests:**
- ✅ Valid data passes all 6 invoice rules
- ✅ No error message when data is correct
- ✅ Result.Success is returned

**Why This Matters:**
- Confirms the "happy path" works
- Baseline for comparing against failure cases
- Proves validation doesn't reject valid data

---

### Test 2: Invalid Invoice (Empty Items)

```kotlin
@Test
fun validateInvoice_emptyItems_returnsFailure() {
    // ========================================
    // ARRANGE: Create invoice with NO items
    // ========================================
    // This violates rule #1: "Must have at least one item"
    
    val now = System.currentTimeMillis()
    
    val invoice = Invoice(
        businessProfileId = 1,
        customerId = 1,
        customerName = "John Doe",
        date = now,
        dueDate = now + 86400000,
        totalAmount = 0,                          // ❌ No items = zero total
        items = emptyList(),                      // ❌ EMPTY - THIS IS THE BUG
        isQuote = false,
        status = InvoiceStatus.DRAFT,
        currencyCode = "AUD"
    )
    
    // ========================================
    // ACT: Call validation
    // ========================================
    
    val result = ValidationRules.validateInvoice(invoice)
    
    // ========================================
    // ASSERT: Verify it FAILS with right message
    // ========================================
    
    // First: confirm it's a Failure (not Success)
    assertTrue("Invoice should fail validation", result.isFailure())
    
    // Second: confirm error message is helpful
    val errorMessage = result.getErrorOrNull()
    assertNotNull("Error message should exist", errorMessage)
    assertTrue(
        "Error should mention line items",
        errorMessage?.contains("line item") ?: false
    )
    
    // Alternatively, check for exact error message:
    assertEquals(
        "Invoice must have at least one line item",
        errorMessage
    )
}
```

**What This Tests:**
- ✅ Empty items list causes failure
- ✅ Error message is correct
- ✅ Error message is user-friendly

**Why This Matters:**
- Prevents invoices without items
- Users understand why it failed ("line item" mentioned)
- Documents the business rule in code

---

### Test 3: Invalid Invoice (Zero Amount)

```kotlin
@Test
fun validateInvoice_zeroAmount_returnsFailure() {
    // ========================================
    // ARRANGE: Create invoice with $0 total
    // ========================================
    // This violates rule #2: "Amount must be > 0"
    
    val now = System.currentTimeMillis()
    
    val invoice = Invoice(
        businessProfileId = 1,
        customerId = 1,
        customerName = "John Doe",
        date = now,
        dueDate = now + 86400000,
        totalAmount = 0,                          // ❌ ZERO - THE BUG
        items = listOf(
            LineItem(
                description = "Free service",
                quantity = 1.0,
                unitPrice = 0                    // ❌ $0 = 0 cents
            )
        ),
        isQuote = false,
        status = InvoiceStatus.DRAFT,
        currencyCode = "AUD"
    )
    
    // ========================================
    // ACT: Call validation
    // ========================================
    
    val result = ValidationRules.validateInvoice(invoice)
    
    // ========================================
    // ASSERT: Verify it fails correctly
    // ========================================
    
    assertTrue("Invoice should fail", result.isFailure())
    
    val error = result.getErrorOrNull()
    assertTrue(
        "Error should mention positive amount",
        error?.contains("greater than zero") ?: false
    )
}
```

**What This Tests:**
- ✅ Zero amounts are rejected
- ✅ Error message mentions "greater than zero"
- ✅ Prevents nonsensical invoices

**Why This Matters:**
- Business rule: invoices must have value
- Prevents accidental $0 invoices
- Clear error helps users understand the problem

---

### Test 4: Valid Customer (Happy Path)

```kotlin
@Test
fun validateCustomer_validCustomer_returnsSuccess() {
    // ========================================
    // ARRANGE: Create valid customer
    // ========================================
    // All required fields present, valid format
    
    val customer = Customer(
        name = "Jane Smith",                      // Required: 2-100 chars
        email = "jane.smith@example.com",         // Optional but valid if provided
        phone = "+61298765432"                    // Optional but valid if provided
    )
    
    // ========================================
    // ACT: Call validation
    // ========================================
    
    val result = ValidationRules.validateCustomer(customer)
    
    // ========================================
    // ASSERT: Verify success
    // ========================================
    
    assertTrue("Customer should be valid", result.isSuccess())
    assertFalse("Customer should not fail", result.isFailure())
}
```

**What This Tests:**
- ✅ Valid customer name passes
- ✅ Valid email format passes
- ✅ Valid phone format passes

**Why This Matters:**
- Confirms validation allows correct data
- Email/phone formats are accepted
- Optional fields don't cause issues

---

### Test 5: Invalid Customer (Bad Email)

```kotlin
@Test
fun validateCustomer_invalidEmail_returnsFailure() {
    // ========================================
    // ARRANGE: Create customer with bad email
    // ========================================
    // This violates the email validation rule:
    // "Email must have @ AND . (dot)"
    
    val customer = Customer(
        name = "Jane Smith",                      // ✅ Valid name
        email = "not-an-email"                    // ❌ No @ or .
    )
    
    // ========================================
    // ACT: Call validation
    // ========================================
    
    val result = ValidationRules.validateCustomer(customer)
    
    // ========================================
    // ASSERT: Verify failure
    // ========================================
    
    // Confirm it failed
    assertTrue("Customer should fail", result.isFailure())
    
    // Confirm error message is helpful
    val error = result.getErrorOrNull()
    assertTrue(
        "Error should mention email",
        error?.contains("email") ?: false
    )
    
    // Check it's about format (not just "error")
    assertTrue(
        "Error should mention format",
        error?.contains("format") ?: false
    )
}
```

**What This Tests:**
- ✅ Invalid email format is caught
- ✅ Error message mentions "email"
- ✅ Error message mentions "format"

**Why This Matters:**
- Prevents storing bad email addresses
- Users know it's an email format issue
- Prevents sending emails to invalid addresses

---

## 🏭 Test Data Factory Pattern

Instead of creating test data in every test, use a factory:

```kotlin
/**
 * TEST DATA FACTORY
 * 
 * Centralized place to create realistic test data.
 * Change once, all tests use the updated data.
 */
object TestDataFactory {
    
    /**
     * Create a valid invoice with sensible defaults
     * 
     * Use this as a base, then modify specific fields in your test:
     *   val invoice = createValidInvoice().copy(totalAmount = 0)
     */
    fun createValidInvoice(): Invoice {
        val now = System.currentTimeMillis()
        
        return Invoice(
            businessProfileId = 1,
            customerId = 1,
            customerName = "Test Customer",
            date = now,
            dueDate = now + 86400000,
            totalAmount = 10000,  // $100
            items = listOf(
                LineItem(
                    description = "Test Item",
                    quantity = 1.0,
                    unitPrice = 10000
                )
            ),
            isQuote = false,
            status = InvoiceStatus.DRAFT,
            currencyCode = "AUD"
        )
    }
    
    /**
     * Create a valid customer with sensible defaults
     */
    fun createValidCustomer(): Customer {
        return Customer(
            name = "John Doe",
            email = "john@example.com",
            phone = "+61298765432"
        )
    }
    
    /**
     * Create a valid line item
     */
    fun createValidLineItem(): LineItem {
        return LineItem(
            description = "Service",
            quantity = 1.0,
            unitPrice = 10000
        )
    }
}
```

### Using the Factory in Tests

```kotlin
@Test
fun validateInvoice_validInvoice_returnsSuccess() {
    // ARRANGE: Use factory, then modify what you need
    val invoice = TestDataFactory.createValidInvoice()
    
    // ACT & ASSERT
    val result = ValidationRules.validateInvoice(invoice)
    assertTrue(result.isSuccess())
}

@Test
fun validateInvoice_emptyItems_returnsFailure() {
    // ARRANGE: Start with valid, then override items
    val invoice = TestDataFactory.createValidInvoice().copy(items = emptyList())
    
    // ACT & ASSERT
    val result = ValidationRules.validateInvoice(invoice)
    assertTrue(result.isFailure())
}
```

---

## 🧬 JUnit Annotations & Assertions

### Important Annotations

```kotlin
@Test
// Tells JUnit: "This is a test method, run it"
// Must have this on every test
fun myTest() { ... }

@Before
// Runs BEFORE each test
fun setup() { ... }  // Initialize common test data

@After
// Runs AFTER each test
fun cleanup() { ... }  // Clean up resources

@BeforeClass
// Runs ONCE before all tests in the class
// (rarely used)

@Ignore
// Skip this test temporarily
@Ignore("Not implemented yet")
fun futureTest() { ... }
```

### Common Assertions

```kotlin
// Check a boolean is true
assertTrue("message", condition)
assertTrue(result.isSuccess())

// Check a boolean is false
assertFalse("message", condition)
assertFalse(result.isFailure())

// Check two values are equal
assertEquals("message", expected, actual)
assertEquals("error message", "message", error)

// Check value is null
assertNull("message", value)
assertNull(result.getErrorOrNull())

// Check value is NOT null
assertNotNull("message", value)
assertNotNull(error)

// Check a condition (throws if false)
assert(condition)
assert(result.isSuccess())

// Check a string contains substring
assertTrue(message.contains("text"))

// Check two objects are the same (reference equality)
assertSame(obj1, obj2)

// Check objects are different
assertNotSame(obj1, obj2)
```

---

## 🚀 Running Tests

### From Command Line

```bash
# Run ALL tests in the project
./gradlew test

# Run tests for a specific module
./gradlew :app:test

# Run specific test class
./gradlew :app:testDebugUnitTest -k ValidationRulesTest

# Run specific test method
./gradlew :app:testDebugUnitTest -k "validateInvoice_emptyItems"

# Run with verbose output
./gradlew test --info

# Run tests and see coverage
./gradlew testDebugUnitTestCoverage
```

### From Android Studio

```
1. Right-click on test file → "Run 'ValidationRulesTest'"
2. Right-click on specific test → "Run 'testName()'"
3. Press Ctrl+Shift+F10 (Run Gradle Test)
4. Use Test Runner pane to see results
```

---

## 📊 Test Output Explained

### Success Output

```
✓ validateInvoice_validInvoice_returnsSuccess
✓ validateInvoice_emptyItems_returnsFailure
✓ validateInvoice_zeroAmount_returnsFailure
✓ validateCustomer_validCustomer_returnsSuccess
✓ validateCustomer_invalidEmail_returnsFailure

BUILD SUCCESSFUL
5 passed in 250ms
```

### Failure Output

```
✗ validateInvoice_emptyItems_returnsFailure
  AssertionError: expected failure but got success
  at com.example.ValidationRulesTest:67
  
Expected:
  Result.Failure("Invoice must have at least one line item")
  
Actual:
  Result.Success(Unit)

BUILD FAILED
1 failed, 4 passed
```

---

## 💡 Testing Best Practices

### ✅ DO

```kotlin
✅ Use descriptive test names
   fun validateInvoice_emptyItems_returnsFailure() { ... }

✅ Test one thing per test
   @Test fun validateInvoice_emptyItems_returnsFailure()

✅ Use ARRANGE-ACT-ASSERT pattern
   // ARRANGE
   // ACT
   // ASSERT

✅ Use test factories for common data
   val invoice = TestDataFactory.createValidInvoice()

✅ Test both success and failure paths
   fun validateInvoice_validInvoice_returnsSuccess()
   fun validateInvoice_emptyItems_returnsFailure()

✅ Check error messages
   assertTrue(error?.contains("item") ?: false)
```

### ❌ DON'T

```kotlin
❌ Test multiple things in one test
   @Test fun validateInvoiceAndCustomer() { ... }

❌ Use vague names
   @Test fun test1() { ... }

❌ Create complex test data inline
   val invoice = Invoice(...100 lines...)

❌ Test implementation details
   assertEquals(invoice.id, 1)  // Don't care about ID

❌ Skip error message checks
   assertTrue(result.isFailure())  // Also check message!

❌ Ignore failing tests
   // They won't fix themselves
```

---

## 🎓 Test Coverage

### What to Measure

```
Line Coverage:        How many lines are executed?
Branch Coverage:      Both if/else paths tested?
Method Coverage:      All methods called?

Target: 80%+ coverage for business logic
```

### Check Coverage

```bash
./gradlew testDebugUnitTestCoverage

# View HTML report:
# app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html
```

---

## 📋 Your Testing Checklist

- [ ] All test files in `src/test/java/`
- [ ] Test class named `*Test` (e.g., `ValidationRulesTest`)
- [ ] Each test has `@Test` annotation
- [ ] Each test follows ARRANGE-ACT-ASSERT
- [ ] Test names are descriptive
- [ ] Happy path tests (valid data)
- [ ] Failure path tests (invalid data)
- [ ] Edge case tests (boundary values)
- [ ] Error messages are checked
- [ ] Tests pass locally before pushing
- [ ] Coverage >= 80%

---

## 🎯 Next: Write Your Own Tests

### Test Template

```kotlin
@Test
fun describeWhatYouTest_inputCondition_expectedResult() {
    // ARRANGE: Set up test data
    val testData = TestDataFactory.createTestData()
        .copy(fieldToTest = testValue)
    
    // ACT: Execute the function
    val result = FunctionUnderTest(testData)
    
    // ASSERT: Verify the result
    assertTrue("What you expect", result.isSuccess())
    assertEquals("Exact message", expectedValue, actualValue)
}
```

---

## 🚀 You're Ready!

You now understand:
- ✅ TDD cycle (Red → Green → Refactor)
- ✅ Test structure (ARRANGE-ACT-ASSERT)
- ✅ JUnit annotations and assertions
- ✅ Test naming conventions
- ✅ How to run tests
- ✅ Interpreting test output
- ✅ Best practices

**Start writing tests today!** 🎉


