# 📚 BIZAP API DOCUMENTATION

**Date**: March 10, 2026  
**Version**: 1.0  
**Status**: Complete

---

## TABLE OF CONTENTS

1. [ViewModel Layer](#viewmodel-layer)
2. [Repository Layer](#repository-layer)
3. [UseCase Layer](#usecase-layer)
4. [Model Layer](#model-layer)

---

## VIEWMODEL LAYER

### CreateInvoiceViewModelV2

**Package**: `com.emul8r.bizap.ui.gui2.invoices`

**Purpose**: Manages invoice creation for GUI2, including customer loading and invoice persistence.

#### Constructor

```kotlin
CreateInvoiceViewModelV2(
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository
)
```

#### Properties

##### customers: StateFlow<List<Customer>>
- **Type**: StateFlow<List<Customer>>
- **Purpose**: Observable stream of available customers
- **Usage**: Observe to populate customer dropdown
- **Example**:
```kotlin
val customers by viewModel.customers.collectAsStateWithLifecycle()
// Use customers to populate UI dropdown
```

##### selectedCustomer: StateFlow<Customer?>
- **Type**: StateFlow<Customer?>
- **Purpose**: Track currently selected customer
- **Default**: null
- **Usage**: Bind to UI to show selected customer
- **Example**:
```kotlin
val selected by viewModel.selectedCustomer.collectAsStateWithLifecycle()
Text(selected?.name ?: "No customer selected")
```

#### Methods

##### selectCustomer(customer: Customer?)
- **Purpose**: Update the selected customer
- **Parameters**:
  - `customer: Customer?` - The customer to select (null to deselect)
- **Return**: Unit
- **Side Effects**: 
  - Updates selectedCustomer StateFlow
  - Logs selection to Timber
- **Example**:
```kotlin
viewModel.selectCustomer(customer)
// selectedCustomer will emit the new value
```

##### createInvoice(invoice: Invoice, onSuccess: () -> Unit, onError: (String) -> Unit)
- **Purpose**: Create a new invoice in the database
- **Parameters**:
  - `invoice: Invoice` - The invoice to create
  - `onSuccess: () -> Unit` - Callback on successful creation
  - `onError: (String) -> Unit` - Callback on error with error message
- **Return**: Unit (async via callbacks)
- **Exceptions Handled**:
  - Database errors → onError callback with message
  - Null exceptions → onError with "Unknown error"
- **Example**:
```kotlin
val invoice = Invoice(
    customerId = selectedCustomer.id,
    totalAmount = 10000L,
    // ... other fields
)
viewModel.createInvoice(
    invoice = invoice,
    onSuccess = { navigateBack() },
    onError = { error -> showErrorDialog(error) }
)
```

---

### CreateCustomerViewModelV2

**Package**: `com.emul8r.bizap.ui.gui2.customers`

**Purpose**: Manages customer creation for GUI2.

#### Methods

##### createCustomer(customer: Customer, onSuccess: () -> Unit, onError: (String) -> Unit)
- **Purpose**: Create a new customer in the database
- **Parameters**:
  - `customer: Customer` - The customer to create
  - `onSuccess: () -> Unit` - Callback on success
  - `onError: (String) -> Unit` - Callback on error
- **Return**: Unit (async via callbacks)
- **Validation**: None (assume UI has validated)
- **Example**:
```kotlin
val newCustomer = Customer(
    name = "ABC Corporation",
    email = "info@abc.com",
    // ... other fields
)
viewModel.createCustomer(
    customer = newCustomer,
    onSuccess = { showSuccess() },
    onError = { showError(it) }
)
```

---

## REPOSITORY LAYER

### InvoiceRepository

**Package**: `com.emul8r.bizap.domain.repository`

**Purpose**: Abstract repository interface for invoice operations.

#### Methods

##### saveInvoice(invoice: Invoice): Result<Unit>
- **Purpose**: Save/create or update an invoice
- **Parameters**:
  - `invoice: Invoice` - Invoice to save
- **Return**: Result<Unit> - Success or failure
- **Example**:
```kotlin
val result = invoiceRepository.saveInvoice(invoice)
when (result) {
    is Result.Success -> { /* Success */ }
    is Result.Failure -> { /* Handle error */ }
}
```

##### getInvoiceById(id: Long): Flow<Invoice?>
- **Purpose**: Observe a specific invoice
- **Parameters**:
  - `id: Long` - Invoice ID
- **Return**: Flow<Invoice?> - Stream of invoice data
- **Example**:
```kotlin
invoiceRepository.getInvoiceById(invoiceId)
    .collectLatest { invoice ->
        if (invoice != null) {
            updateUI(invoice)
        }
    }
```

##### getAllInvoices(): Flow<List<Invoice>>
- **Purpose**: Observe all invoices for current business
- **Return**: Flow<List<Invoice>> - Stream of all invoices
- **Example**:
```kotlin
invoiceRepository.getAllInvoices()
    .collectLatest { invoices ->
        updateInvoiceList(invoices)
    }
```

##### deleteInvoice(id: Long): Result<Unit>
- **Purpose**: Delete an invoice
- **Parameters**:
  - `id: Long` - Invoice ID to delete
- **Return**: Result<Unit> - Success or failure
- **Side Effects**: Also removes related snapshots
- **Example**:
```kotlin
val result = invoiceRepository.deleteInvoice(invoiceId)
```

---

### CustomerRepository

**Package**: `com.emul8r.bizap.domain.repository`

**Purpose**: Abstract repository interface for customer operations.

#### Methods

##### getCustomersByBusiness(businessId: Long): List<Customer>
- **Purpose**: Get all customers for a business (synchronous)
- **Parameters**:
  - `businessId: Long` - Business profile ID
- **Return**: List<Customer> - All customers for business
- **Throws**: Exception if database error
- **Example**:
```kotlin
val customers = customerRepository.getCustomersByBusiness(businessId)
customers.forEach { println(it.name) }
```

##### insert(customer: Customer): Unit
- **Purpose**: Create a new customer
- **Parameters**:
  - `customer: Customer` - Customer to create
- **Return**: Unit
- **Throws**: Exception if database error
- **Example**:
```kotlin
customerRepository.insert(customer)
// Customer now in database
```

---

## USECASE LAYER

### SaveInvoiceUseCase

**Package**: `com.emul8r.bizap.domain.usecase`

**Purpose**: Business logic for saving invoices, including offline queueing and snapshot sync.

#### Invoke Method

```kotlin
suspend operator fun invoke(
    invoice: Invoice,
    context: Context
): Result<Unit>
```

- **Purpose**: Save invoice with offline support
- **Parameters**:
  - `invoice: Invoice` - Invoice to save
  - `context: Context` - Android context for connectivity check
- **Return**: Result<Unit> - Success or failure
- **Behavior**:
  - If online: Saves to database and syncs snapshots
  - If offline: Queues for later sync
- **Example**:
```kotlin
val result = saveInvoiceUseCase(invoice, context)
result.onSuccess {
    showMessage("Invoice saved")
}.onFailure { error ->
    showError(error.message)
}
```

---

### RecordPaymentUseCase

**Package**: `com.emul8r.bizap.domain.usecase`

**Purpose**: Business logic for recording payments.

#### Invoke Method

```kotlin
suspend operator fun invoke(
    invoiceId: Long,
    amountPaid: Long,
    context: Context
): Result<Unit>
```

- **Purpose**: Record a payment for an invoice
- **Parameters**:
  - `invoiceId: Long` - Invoice being paid
  - `amountPaid: Long` - Amount paid in cents
  - `context: Context` - For offline detection
- **Return**: Result<Unit> - Success or failure
- **Validation**: Amount must be > 0
- **Example**:
```kotlin
val result = recordPaymentUseCase(invoiceId, 50000L, context)
// Records A$500.00 payment
```

---

## MODEL LAYER

### Invoice

**Package**: `com.emul8r.bizap.domain.model`

**Purpose**: Represents an invoice entity.

#### Properties

```kotlin
data class Invoice(
    val id: Long,                      // Unique identifier (0 for new)
    val businessProfileId: Long,       // Business this belongs to
    val customerId: Long,              // Customer being invoiced
    val customerName: String,          // Customer name (cached)
    val customerAddress: String,       // Customer address
    val customerEmail: String,         // Customer email
    val items: List<InvoiceLineItem>,  // Line items
    val totalAmount: Long,             // Total in cents
    val amountPaid: Long,              // Amount paid in cents
    val status: InvoiceStatus,         // DRAFT, SENT, PAID, OVERDUE
    val date: Long,                    // Invoice date (millis)
    val dueDate: Long,                 // Due date (millis)
    val isQuote: Boolean,              // Is this a quote?
    val currencyCode: String,          // Currency code (AUD, USD, etc)
    val taxRate: Double,               // Tax rate percentage
    val taxAmount: Long,               // Tax amount in cents
    val invoiceYear: Int,              // Year for numbering
    val invoiceSequence: Int,          // Sequence for numbering
    val notes: String = "",            // Optional notes
    val updatedAt: Long = 0L           // Last update time
)
```

#### Calculated Properties

```kotlin
// Outstanding amount
val outstanding: Long
    get() = totalAmount - amountPaid

// Is fully paid
val isPaid: Boolean
    get() = amountPaid >= totalAmount

// Is overdue
val isOverdue: Boolean
    get() = dueDate < System.currentTimeMillis() && status != InvoiceStatus.PAID
```

---

### Customer

**Package**: `com.emul8r.bizap.domain.model`

**Purpose**: Represents a customer entity.

#### Properties

```kotlin
data class Customer(
    val id: Long,                    // Unique identifier (0 for new)
    val businessProfileId: Long,     // Business this customer belongs to
    val name: String,                // Customer name (required)
    val email: String = "",          // Customer email (optional)
    val phone: String = "",          // Customer phone (optional)
    val address: String = ""         // Customer address (optional)
)
```

---

## ERROR HANDLING

### Result Type

All operations return `Result<T>` which can be:

```kotlin
sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure<T>(val error: Exception) : Result<T>()
}
```

### Usage

```kotlin
val result = invoiceRepository.saveInvoice(invoice)

when (result) {
    is Result.Success -> {
        showMessage("Invoice saved successfully")
    }
    is Result.Failure -> {
        showError("Failed to save: ${result.error.message}")
        Timber.e(result.error, "Invoice save failed")
    }
}
```

---

## VALIDATION RULES

### Invoice Validation

- **totalAmount**: Must be > 0
- **customerId**: Must be > 0 (customer required)
- **status**: Must be valid InvoiceStatus enum
- **dueDate**: Should be >= date (enforced in UI)

### Customer Validation

- **name**: Required, length >= 2
- **email**: Optional, must be valid format if provided
- **phone**: Optional
- **address**: Optional

### Amount Validation

- Format: Valid decimal number
- Value: Must convert to positive cents
- Example: "100.50" → 10050L cents

---

## LOGGING

### Timber Integration

All operations log appropriate events:

```kotlin
// Success
Timber.d("Invoice created successfully for customer: ${customer.name}")

// Warnings
Timber.w("Invoice creation failed: No customer selected")

// Errors
Timber.e("Failed to create invoice: ${error.message}")

// Exceptions
Timber.e(exception, "Unexpected error during invoice creation")
```

---

## TESTING

### Unit Test Examples

```kotlin
@Test
fun `createInvoice succeeds with valid data`() = runTest {
    // Given
    val invoice = createTestInvoice()
    
    // When
    val result = invoiceRepository.saveInvoice(invoice)
    
    // Then
    assertTrue(result is Result.Success)
}
```

---

## BEST PRACTICES

1. **Always handle Result**
   - Don't ignore success/failure
   - Log errors appropriately

2. **Use StateFlow in UI**
   - Observable data changes
   - Auto-updates UI

3. **Validate before calling**
   - Check for required fields
   - Validate formats

4. **Handle errors gracefully**
   - Show user-friendly messages
   - Log for debugging

5. **Test edge cases**
   - Invalid inputs
   - Null values
   - Exceptions

---

**Last Updated**: March 10, 2026  
**Version**: 1.0  
**Status**: Complete


