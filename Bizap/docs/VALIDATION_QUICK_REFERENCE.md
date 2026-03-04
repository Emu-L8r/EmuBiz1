# Domain Validation - Quick Reference Card

**Use this as a cheat sheet while coding**

---

## 🎯 Quick Start

### Basic Validation

```kotlin
// 1. Import
import com.emul8r.bizap.domain.validation.ValidationRules
import com.emul8r.bizap.domain.model.Result

// 2. Validate
val result = ValidationRules.validateInvoice(invoice)

// 3. Check
if (result.isFailure()) {
    showError(result.getErrorOrNull()!!)
    return
}

// 4. Proceed
repository.save(invoice)
```

---

## 📋 Validation Rules

### Invoice (6 Rules)
```
✅ items.isNotEmpty()
✅ totalAmount > 0
✅ dueDate >= date
✅ customerName.isNotBlank()
✅ items all valid
✅ currencyCode.length == 3
```

### Customer (6 Rules)
```
✅ name.length in 2..100
✅ name.isNotBlank()
✅ email valid (if provided)
✅ phone 5-20 chars (if provided)
✅ businessName <= 100 (if provided)
```

### LineItem (5 Rules)
```
✅ description.length in 1..500
✅ quantity > 0
✅ unitPrice > 0
✅ total < 100,000,000
```

---

## 💻 Result API Cheat Sheet

### Create
```kotlin
Result.Success(data)              // ✅ Success with data
Result.Failure("error message")   // ❌ Failure with message
```

### Check
```kotlin
result.isSuccess()                // Boolean: is Success?
result.isFailure()                // Boolean: is Failure?
result.getOrNull()                // T?: data or null
result.getErrorOrNull()           // String?: error or null
result.get()                      // T: data or throw
```

### Transform
```kotlin
result.map { it * 2 }             // Transform success data
result.flatMap { ... }            // Chain operations
result.recover { defaultValue }   // Use fallback on failure
```

### Handle
```kotlin
result.fold(
    onSuccess = { ... },
    onFailure = { ... }
)

result.onSuccess { ... }          // Execute if success
result.onFailure { ... }          // Execute if failure
```

---

## 🔄 Common Patterns

### Pattern 1: Simple Check
```kotlin
val result = ValidationRules.validateInvoice(invoice)
if (result.isFailure()) {
    _errorState.emit(result.getErrorOrNull()!!)
    return@launch
}
// Safe to proceed
```

### Pattern 2: With Logging
```kotlin
ValidationRules.validateInvoice(invoice)
    .onSuccess { Timber.d("✅ Invoice valid") }
    .onFailure { error -> 
        Timber.w("⚠️ Validation failed: $error")
        _errorState.emit(error)
    }
```

### Pattern 3: Chained Operations
```kotlin
validateInvoice(invoice)
    .flatMap { repository.save(it) }
    .onSuccess { id -> _savedState.emit(id) }
    .onFailure { error -> _errorState.emit(error) }
```

### Pattern 4: Multiple Validations
```kotlin
val invoiceResult = ValidationRules.validateInvoice(invoice)
val customerResult = ValidationRules.validateCustomer(customer)

if (invoiceResult.isFailure() || customerResult.isFailure()) {
    showError(invoiceResult.getErrorOrNull() ?: customerResult.getErrorOrNull()!!)
    return
}
```

---

## 🧪 Testing

### Test Valid Data
```kotlin
@Test
fun validateInvoice_validInvoice_returnsSuccess() {
    val invoice = Invoice(...)  // Valid
    val result = ValidationRules.validateInvoice(invoice)
    assertTrue(result.isSuccess())
}
```

### Test Invalid Data
```kotlin
@Test
fun validateInvoice_emptyItems_returnsFailure() {
    val invoice = Invoice(..., items = emptyList())
    val result = ValidationRules.validateInvoice(invoice)
    assertTrue(result.isFailure())
    assertTrue(result.getErrorOrNull()?.contains("item") ?: false)
}
```

### Test Edge Cases
```kotlin
@Test
fun validateInvoice_dueDateBeforeInvoiceDate_returnsFailure() {
    val invoice = Invoice(..., dueDate = invoiceDate - 1)
    val result = ValidationRules.validateInvoice(invoice)
    assertTrue(result.isFailure())
}
```

---

## ⚠️ Common Mistakes

### ❌ Not checking result
```kotlin
ValidationRules.validateInvoice(invoice)  // Validation ignored!
repository.save(invoice)                   // Might save invalid data
```

### ✅ Always check
```kotlin
val result = ValidationRules.validateInvoice(invoice)
if (result.isFailure()) {
    showError(result.getErrorOrNull()!!)
    return
}
repository.save(invoice)  // Safe now
```

---

## 📍 Where to Add Validation

### In ViewModels (Before Save)
```kotlin
fun onSaveClicked() {
    val result = ValidationRules.validateInvoice(invoice)
    if (result.isFailure()) { /* show error */ return }
    // ... save code ...
}
```

### In Use Cases (Before Business Logic)
```kotlin
suspend operator fun invoke(invoice: Invoice): Result<Long> {
    val validation = ValidationRules.validateInvoice(invoice)
    if (validation.isFailure()) {
        return Result.Failure(validation.getErrorOrNull()!!)
    }
    // ... business logic ...
}
```

### In Repositories (Before Persistence)
```kotlin
fun save(invoice: Invoice): Long {
    val result = ValidationRules.validateInvoice(invoice)
    if (result.isFailure()) throw Exception(result.getErrorOrNull())
    // ... insert into database ...
}
```

---

## 🎯 Error Messages (User-Friendly)

Good error messages:
```
"Invoice must have at least one line item"
"Customer name must be 2-100 characters"
"Payment cannot exceed balance remaining"
```

Bad error messages:
```
"Validation failed"
"Invalid input"
"Error"
```

---

## 📊 Quick Decision Tree

```
Is data valid?
├─ YES → Result.Success(Unit)
│        └─ Proceed with operation
│
└─ NO → Result.Failure("specific error message")
       └─ Show error to user
       └─ Return without saving
```

---

## 🔗 Related Files

| File | Purpose |
|------|---------|
| Result.kt | Sealed class + operations |
| ValidationRules.kt | All validation logic |
| ValidationRulesTest.kt | 30+ test cases |
| CreateInvoiceViewModel.kt | Integration example |
| DOMAIN_VALIDATION_COMPLETE.md | Full documentation |

---

## 💡 Pro Tips

1. **Use fold() for clean code**
   ```kotlin
   result.fold(
       onSuccess = { proceed() },
       onFailure = { error -> showError(error) }
   )
   ```

2. **Chain operations with flatMap()**
   ```kotlin
   validate().flatMap { save() }.flatMap { notify() }
   ```

3. **Always provide context in errors**
   ```kotlin
   ❌ "Failed"
   ✅ "Invoice must have at least one line item"
   ```

4. **Validate early, fail fast**
   ```kotlin
   // First thing in save method
   val result = ValidationRules.validate(data)
   if (result.isFailure()) return
   ```

5. **Log all validations**
   ```kotlin
   Timber.d("✅ Invoice validation passed")
   Timber.w("⚠️ Validation failed: $error")
   ```

---

## 🚀 You're Ready!

Print this card and keep it handy while:
- Writing validation code
- Integrating validators into ViewModels
- Writing tests
- Adding custom validators

Good luck! 🎉


