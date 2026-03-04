# Unit Testing Quick Cheat Sheet

**Use this while writing tests**

---

## 🎯 Quick Test Template

```kotlin
@Test
fun testName_scenario_expectedResult() {
    // ARRANGE: Set up test data
    val data = TestDataFactory.createValidInvoice()
    
    // ACT: Execute the function
    val result = ValidationRules.validateInvoice(data)
    
    // ASSERT: Verify the result
    assertTrue(result.isSuccess())
}
```

---

## 📋 Invoice Test Examples

### Valid Invoice (Should Pass)

```kotlin
@Test
fun validateInvoice_validInvoice_returnsSuccess() {
    val invoice = TestDataFactory.createValidInvoice()
    val result = ValidationRules.validateInvoice(invoice)
    
    assertTrue(result.isSuccess())
}
```

### Empty Items (Should Fail)

```kotlin
@Test
fun validateInvoice_emptyItems_returnsFailure() {
    val invoice = TestDataFactory.createInvoiceWithEmptyItems()
    val result = ValidationRules.validateInvoice(invoice)
    
    assertTrue(result.isFailure())
    assertTrue(result.getErrorOrNull()?.contains("item") ?: false)
}
```

### Zero Amount (Should Fail)

```kotlin
@Test
fun validateInvoice_zeroAmount_returnsFailure() {
    val invoice = TestDataFactory.createInvoiceWithZeroAmount()
    val result = ValidationRules.validateInvoice(invoice)
    
    assertTrue(result.isFailure())
    assertTrue(result.getErrorOrNull()?.contains("greater than zero") ?: false)
}
```

### Invalid Due Date (Should Fail)

```kotlin
@Test
fun validateInvoice_dueDateBeforeInvoiceDate_returnsFailure() {
    val invoice = TestDataFactory.createInvoiceWithInvalidDueDate()
    val result = ValidationRules.validateInvoice(invoice)
    
    assertTrue(result.isFailure())
}
```

---

## 👥 Customer Test Examples

### Valid Customer (Should Pass)

```kotlin
@Test
fun validateCustomer_validCustomer_returnsSuccess() {
    val customer = TestDataFactory.createValidCustomer()
    val result = ValidationRules.validateCustomer(customer)
    
    assertTrue(result.isSuccess())
}
```

### Blank Name (Should Fail)

```kotlin
@Test
fun validateCustomer_blankName_returnsFailure() {
    val customer = TestDataFactory.createCustomerWithBlankName()
    val result = ValidationRules.validateCustomer(customer)
    
    assertTrue(result.isFailure())
}
```

### Name Too Short (Should Fail)

```kotlin
@Test
fun validateCustomer_nameTooShort_returnsFailure() {
    val customer = TestDataFactory.createCustomerWithNameTooShort()
    val result = ValidationRules.validateCustomer(customer)
    
    assertTrue(result.isFailure())
    assertTrue(result.getErrorOrNull()?.contains("2 characters") ?: false)
}
```

### Invalid Email (Should Fail)

```kotlin
@Test
fun validateCustomer_invalidEmail_returnsFailure() {
    val customer = TestDataFactory.createCustomerWithInvalidEmail()
    val result = ValidationRules.validateCustomer(customer)
    
    assertTrue(result.isFailure())
    assertTrue(result.getErrorOrNull()?.contains("email") ?: false)
}
```

### Blank Optional Email (Should Pass)

```kotlin
@Test
fun validateCustomer_blankOptionalEmail_returnsSuccess() {
    val customer = TestDataFactory.createCustomerWithBlankEmail()
    val result = ValidationRules.validateCustomer(customer)
    
    assertTrue(result.isSuccess())
}
```

---

## 📦 Line Item Test Examples

### Valid Item (Should Pass)

```kotlin
@Test
fun validateLineItem_validItem_returnsSuccess() {
    val item = TestDataFactory.createValidLineItem()
    val result = ValidationRules.validateLineItem(item)
    
    assertTrue(result.isSuccess())
}
```

### Blank Description (Should Fail)

```kotlin
@Test
fun validateLineItem_blankDescription_returnsFailure() {
    val item = TestDataFactory.createLineItemWithBlankDescription()
    val result = ValidationRules.validateLineItem(item)
    
    assertTrue(result.isFailure())
}
```

### Zero Quantity (Should Fail)

```kotlin
@Test
fun validateLineItem_zeroQuantity_returnsFailure() {
    val item = TestDataFactory.createLineItemWithZeroQuantity()
    val result = ValidationRules.validateLineItem(item)
    
    assertTrue(result.isFailure())
}
```

---

## 🔄 Advanced Patterns

### Modify One Field

```kotlin
@Test
fun validateInvoice_modifiedAmount_returnsFailure() {
    // Start with valid invoice
    val invoice = TestDataFactory.createValidInvoice()
    
    // Override just ONE field
    val modified = invoice.copy(totalAmount = 0)
    
    // Test the modification
    val result = ValidationRules.validateInvoice(modified)
    assertTrue(result.isFailure())
}
```

### Test Multiple Conditions

```kotlin
@Test
fun validateInvoice_multipleBreach_returnsFirFailure() {
    val invoice = TestDataFactory.createValidInvoice().copy(
        items = emptyList(),      // Breach #1
        totalAmount = 0,          // Breach #2
        customerName = ""         // Breach #3
    )
    
    val result = ValidationRules.validateInvoice(invoice)
    
    // Should fail (catches first breach)
    assertTrue(result.isFailure())
    
    // Message should mention items (first check)
    assertTrue(result.getErrorOrNull()?.contains("item") ?: false)
}
```

### Test Batch Validation

```kotlin
@Test
fun validateCustomers_allValid_returnsSuccess() {
    val customers = TestDataFactory.createValidCustomers(5)
    val result = ValidationRules.validateCustomers(customers)
    
    assertTrue(result.isSuccess())
}

@Test
fun validateCustomers_oneInvalid_returnsFailure() {
    val customers = listOf(
        TestDataFactory.createValidCustomer(),
        TestDataFactory.createValidCustomer(),
        TestDataFactory.createCustomerWithBlankName(),  // Invalid!
        TestDataFactory.createValidCustomer()
    )
    
    val result = ValidationRules.validateCustomers(customers)
    assertTrue(result.isFailure())
}
```

---

## ✅ Assertion Patterns

### Check Success

```kotlin
assertTrue(result.isSuccess())
assertFalse(result.isFailure())
assertNull(result.getErrorOrNull())
```

### Check Failure

```kotlin
assertTrue(result.isFailure())
assertFalse(result.isSuccess())
assertNotNull(result.getErrorOrNull())
```

### Check Error Message

```kotlin
val error = result.getErrorOrNull()
assertTrue(error?.contains("specific word") ?: false)
assertEquals("exact message", error)
assertTrue(error?.startsWith("Invoice") ?: false)
```

### Check Data Equality

```kotlin
assertEquals(expected, actual)
assertEquals("message", expectedValue, actualValue)
assertNotEquals(value1, value2)
```

---

## 🚀 Run Tests

### All Tests
```bash
./gradlew test
```

### Specific Class
```bash
./gradlew :app:testDebugUnitTest -k ValidationRulesTest
```

### Specific Test
```bash
./gradlew :app:testDebugUnitTest -k "validateInvoice_emptyItems"
```

### With Details
```bash
./gradlew test --info
```

---

## 📊 Expected Output

### All Pass ✅
```
✓ validateInvoice_validInvoice_returnsSuccess
✓ validateInvoice_emptyItems_returnsFailure
✓ validateCustomer_validCustomer_returnsSuccess

BUILD SUCCESSFUL
3 passed
```

### One Fails ❌
```
✗ validateInvoice_emptyItems_returnsFailure
  AssertionError: expected failure but got success

BUILD FAILED
1 failed, 2 passed
```

---

## 💡 Common Issues

### ❌ Test Passes But Shouldn't

```kotlin
// WRONG - always passes
@Test fun testAlwaysPasses() {
    assertTrue(true)  // Always true!
}

// RIGHT - actually tests something
@Test fun testActualLogic() {
    val result = ValidationRules.validateInvoice(invoice)
    assertTrue(result.isSuccess())  // Depends on actual logic
}
```

### ❌ Test is Flaky (Passes Sometimes)

```kotlin
// WRONG - uses current time
val invoice = Invoice(..., date = System.currentTimeMillis())
// This changes every time test runs!

// RIGHT - uses fixed time
val invoice = TestDataFactory.createValidInvoice()
// Time is consistent every run
```

### ❌ Test Checks Wrong Thing

```kotlin
// WRONG - doesn't check what's being tested
val result = ValidationRules.validateInvoice(invoice)
assertTrue(true)  // Always passes, doesn't verify result!

// RIGHT - actually verifies the result
val result = ValidationRules.validateInvoice(invoice)
assertTrue(result.isSuccess())  // Checks the actual result
```

---

## ✨ Pro Tips

1. **Use factory methods** - Less code, more readable
   ```kotlin
   val invoice = TestDataFactory.createValidInvoice()
   ```

2. **Use .copy() to modify** - Makes changes clear
   ```kotlin
   val modified = invoice.copy(totalAmount = 0)
   ```

3. **Check error messages** - Don't just check success/failure
   ```kotlin
   assertTrue(error?.contains("item") ?: false)
   ```

4. **Test boundary values** - Empty, single, large
   ```kotlin
   items = emptyList()        // Minimum
   items = listOf(item)       // Single
   items = listOf(item, item) // Multiple
   ```

5. **Name tests clearly** - Should read like documentation
   ```kotlin
   validateInvoice_emptyItems_returnsFailure()
   validateInvoice_zeroAmount_returnsFailure()
   validateInvoice_dueDateBeforeInvoiceDate_returnsFailure()
   ```

---

**Now go write tests! 🎉**


