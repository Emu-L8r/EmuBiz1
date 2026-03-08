# API Reference — Bizap (EmuBiz1)

**Last Updated:** 2026-03-08  
**Scope:** Public repository methods, ViewModel state objects, use case signatures, and error types.

---

## Table of Contents

1. [Error Types](#1-error-types)
2. [Repository APIs](#2-repository-apis)
3. [Use Case APIs](#3-use-case-apis)
4. [ViewModel State Objects](#4-viewmodel-state-objects)
5. [GUI2 Repository APIs](#5-gui2-repository-apis)

---

## 1. Error Types

### `Result<T>` Conventions

All repository methods and use cases return Kotlin's standard `Result<T>`.

```kotlin
// Success
Result.success(value: T)

// Failure — wraps an exception or domain error
Result.failure(exception: Throwable)
```

**Handling in ViewModels:**
```kotlin
result.fold(
    onSuccess = { data -> uiState = UiState.Success(data) },
    onFailure = { error -> uiState = UiState.Error(error.message ?: "Unknown error") }
)
```

### Common Exception Types

| Exception | When Thrown |
|-----------|-------------|
| `IllegalArgumentException` | Validation failure (e.g. amount ≤ 0, empty name) |
| `IllegalStateException` | Invalid state transition |
| `SQLiteException` | Database write failure |
| `IOException` | PDF/file operation failure |
| `Exception("No business profile found")` | Missing business context |

---

## 2. Repository APIs

### 2.1 `InvoiceRepository`

Interface location: `domain/repository/`  
Implementation: `data/repository/InvoiceRepositoryImpl`

```kotlin
interface InvoiceRepository {

    /** Returns a Flow of all invoices for the given business. */
    fun getInvoices(businessId: Long): Flow<List<Invoice>>

    /** Returns a Flow of a single invoice by ID. */
    fun getInvoiceById(invoiceId: Long): Flow<Invoice?>

    /** Saves a new invoice. Returns the new invoice ID on success. */
    suspend fun saveInvoice(invoice: Invoice): Result<Long>

    /** Updates an existing invoice. Returns the invoice ID on success. */
    suspend fun updateInvoice(invoice: Invoice): Result<Long>

    /** Deletes (soft-deletes) an invoice. */
    suspend fun deleteInvoice(invoiceId: Long, businessId: Long): Result<Unit>

    /** Returns invoices filtered by status. */
    fun getInvoicesByStatus(businessId: Long, status: InvoiceStatus): Flow<List<Invoice>>

    /** Returns invoices for a specific customer. */
    fun getInvoicesByCustomer(customerId: Long): Flow<List<Invoice>>

    /** Updates the status of an invoice. */
    suspend fun updateInvoiceStatus(invoiceId: Long, newStatus: InvoiceStatus): Result<Unit>

    /** Generates the next invoice number for the business/year. */
    suspend fun generateInvoiceNumber(businessId: Long, year: Int): String
}
```

---

### 2.2 `CustomerRepository`

Interface location: `domain/repository/`  
Implementation: `data/repository/CustomerRepositoryImpl`

```kotlin
interface CustomerRepository {

    /** Returns a Flow of all active customers for the given business. */
    fun getCustomers(businessId: Long): Flow<List<Customer>>

    /** Returns a Flow of a single customer by ID. */
    fun getCustomerById(customerId: Long): Flow<Customer?>

    /** Creates a new customer. Returns the new customer ID on success. */
    suspend fun createCustomer(customer: Customer): Result<Long>

    /** Updates an existing customer. Returns the customer ID on success. */
    suspend fun updateCustomer(customer: Customer): Result<Long>

    /** Soft-deletes a customer (sets isActive = false). */
    suspend fun deleteCustomer(customerId: Long): Result<Unit>

    /** Checks if an email is already in use for the given business. */
    suspend fun isEmailInUse(email: String, businessId: Long, excludeId: Long?): Boolean

    /** Returns a customer with all their invoices. */
    suspend fun getCustomerWithInvoices(customerId: Long): CustomerWithInvoices?
}
```

---

### 2.3 `BusinessProfileRepository`

Interface location: `domain/repository/`  
Implementation: `data/repository/BusinessProfileRepositoryImpl`

```kotlin
interface BusinessProfileRepository {

    /** Returns a Flow of the active business profile. */
    fun getBusinessProfile(): Flow<BusinessProfile?>

    /** Returns the active business profile ID. */
    suspend fun getActiveBusinessId(): Long?

    /** Saves or updates the business profile. */
    suspend fun saveBusinessProfile(profile: BusinessProfile): Result<Long>

    /** Returns all business profiles (multi-business support). */
    fun getAllProfiles(): Flow<List<BusinessProfile>>
}
```

---

### 2.4 `DocumentRepository`

Interface location: `domain/repository/`  
Implementation: `data/repository/DocumentRepositoryImpl`

```kotlin
interface DocumentRepository {

    /** Saves a generated document record. */
    suspend fun saveDocument(document: GeneratedDocumentEntity): Result<Long>

    /** Returns the document associated with an invoice. */
    fun getDocumentByInvoiceId(invoiceId: Long): Flow<GeneratedDocumentEntity?>

    /** Deletes a document record (does not delete the file). */
    suspend fun deleteDocument(documentId: Long): Result<Unit>

    /** Returns all documents for a business. */
    fun getAllDocuments(businessId: Long): Flow<List<GeneratedDocumentEntity>>
}
```

---

### 2.5 `RevenueRepository`

Interface location: `domain/revenue/repository/`

```kotlin
interface RevenueRepository {

    /** Returns a Flow of daily revenue snapshots for trend charts. */
    fun getDailyRevenueTrend(businessId: Long, days: Int): Flow<List<DailyRevenueTrendV2>>

    /** Returns a Flow of invoice count by status. */
    fun getInvoiceStatusCounts(businessId: Long): Flow<List<InvoiceStatusCountV2>>

    /** Returns a Flow of total outstanding amount. */
    fun getTotalOutstanding(businessId: Long): Flow<Long?>
}
```

---

### 2.6 `OfflineQueueRepository`

Interface location: `domain/repository/`  
Implementation: `data/repository/OfflineQueueRepositoryImpl`

```kotlin
interface OfflineQueueRepository {

    /** Returns all pending offline operations. */
    suspend fun getPendingOperations(businessId: Long): List<OfflineOperation>

    /** Marks an operation as completed. */
    suspend fun markCompleted(operationId: Long)

    /** Marks an operation as failed with an error message. */
    suspend fun markFailed(operationId: Long, error: String)

    /** Clears all completed operations. */
    suspend fun clearCompleted(businessId: Long)
}
```

---

## 3. Use Case APIs

### 3.1 `RecordPaymentUseCase`

```kotlin
class RecordPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepositoryV2
) {
    /**
     * Validates and records a payment against an invoice.
     *
     * @param invoiceId    Target invoice ID
     * @param businessId   Owning business ID
     * @param amount       Payment amount in cents (must be > 0 and ≤ outstanding)
     * @param outstanding  Remaining outstanding balance in cents
     * @param paymentDate  Payment date as Unix milliseconds (must be ≤ today, ≥ invoiceDate)
     * @param invoiceDate  Invoice creation date as Unix milliseconds
     * @param notes        Optional payment notes (max 500 chars)
     * @return Result.success(Unit) on success, Result.failure(exception) on validation error or DB failure
     */
    suspend operator fun invoke(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        outstanding: Long,
        paymentDate: Long,
        invoiceDate: Long,
        notes: String
    ): Result<Unit>
}
```

**Validation errors returned via `Result.failure`:**
- `"Payment amount must be greater than zero"`
- `"Payment amount (X) exceeds outstanding balance (Y)"`
- `"Payment date cannot be in the future"`
- `"Payment date cannot be before the invoice date"`

---

### 3.2 `SaveInvoiceUseCase`

```kotlin
class SaveInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val snapshotSyncHelper: SnapshotSyncHelper,
    private val offlineQueueService: OfflineQueueService,
    private val connectivityHelper: ConnectivityHelper
) {
    /**
     * Saves a new invoice with offline-first support.
     *
     * @param invoice Invoice to save (must have ≥ 1 line item and a non-empty customer name)
     * @return Result<Long> with the new invoice ID on success
     */
    suspend operator fun invoke(invoice: Invoice): Result<Long>
}
```

---

### 3.3 `UpdateInvoiceUseCase`

```kotlin
class UpdateInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val snapshotSyncHelper: SnapshotSyncHelper,
    private val offlineQueueService: OfflineQueueService,
    private val connectivityHelper: ConnectivityHelper
) {
    /**
     * Updates an existing invoice with offline-first support.
     *
     * @param invoice Invoice to update
     * @return Result<Long> with the invoice ID on success
     */
    suspend operator fun invoke(invoice: Invoice): Result<Long>
}
```

---

### 3.4 `DeleteInvoiceUseCase`

```kotlin
class DeleteInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val offlineQueueService: OfflineQueueService,
    private val connectivityHelper: ConnectivityHelper
) {
    /**
     * Deletes (soft-deletes) an invoice with offline-first support.
     *
     * @param invoiceId  Invoice ID to delete
     * @param businessId Owning business ID
     * @return Result<Unit> on success
     */
    suspend operator fun invoke(invoiceId: Long, businessId: Long): Result<Unit>
}
```

---

### 3.5 `GenerateAndSaveInvoiceUseCase`

```kotlin
class GenerateAndSaveInvoiceUseCase @Inject constructor(
    private val pdfService: InvoicePdfService,
    private val documentRepository: DocumentRepository
) {
    /**
     * Generates a PDF for the given invoice and saves the document record.
     *
     * @param invoice          Invoice domain model
     * @param snapshot         Invoice snapshot for PDF rendering
     * @param isQuote          If true, generates a Quote (not Invoice)
     * @param overwriteExisting If true, overwrites existing PDF for this invoice
     * @return Result<File> with the generated PDF File on success
     */
    suspend operator fun invoke(
        invoice: Invoice,
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean
    ): Result<File>
}
```

---

### 3.6 `SyncPendingOperationsUseCase`

```kotlin
class SyncPendingOperationsUseCase @Inject constructor(
    private val offlineQueueRepository: OfflineQueueRepository
) {
    /**
     * Processes all pending offline operations in FIFO order.
     * Called by SyncWorker on a 15-minute schedule.
     * Marks operations as COMPLETED or FAILED based on the result.
     */
    suspend operator fun invoke()
}
```

---

## 4. ViewModel State Objects

### 4.1 Generic `UiState<T>`

All ViewModels use a sealed class pattern:

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

---

### 4.2 `PaymentFormState` — `RecordPaymentViewModel`

```kotlin
data class PaymentFormState(
    val outstanding: Long = 0L,          // Outstanding balance in cents
    val amountRaw: String = "",          // Raw text input
    val amountCents: Long = 0L,          // Parsed amount in cents
    val amountError: String? = null,     // Validation error for amount
    val paymentDate: Long = today,       // Selected date (Unix ms)
    val dateError: String? = null,       // Validation error for date
    val notes: String = "",              // Payment notes (max 500 chars)
    val isFormValid: Boolean = false,    // All fields valid
    val isLoading: Boolean = false,      // Submission in progress
    val submissionError: String? = null  // Error after submission attempt
)
```

**ViewModel public API:**
```kotlin
// Must call before showing dialog
fun initFor(invoiceId: Long, businessId: Long, invoiceTotal: Long, amountPaid: Long, invoiceDate: Long)

// Called on user input
fun onAmountChanged(raw: String)
fun onDateChanged(dateMs: Long)
fun onNotesChanged(notes: String)

// Submit the payment
fun submit()
```

---

### 4.3 `DashboardStateV2` — `DashboardViewModelV2`

```kotlin
data class DashboardStateV2(
    val businessContext: BusinessContextV2?,
    val revenueMetrics: RevenueMetricsV2?,
    val paymentMetrics: PaymentMetricsV2?,
    val riskMetrics: RiskMetricsV2?,
    val isLoading: Boolean = false,
    val error: String? = null
)
```

---

### 4.4 `RevenueMetricsV2`

```kotlin
data class RevenueMetricsV2(
    val mtdRevenue: Long,           // Month-to-Date revenue (cents)
    val ytdRevenue: Long,           // Year-to-Date revenue (cents)
    val weeklyRevenue: Long,        // Last 7 days revenue (cents)
    val totalPaidRevenue: Long,     // All-time paid revenue (cents)
    val last30DaysTrend: List<DailyRevenueTrendV2>  // Daily data points
)
```

---

### 4.5 `PaymentMetricsV2`

```kotlin
data class PaymentMetricsV2(
    val totalOutstanding: Long,           // Total outstanding (cents)
    val totalCollected: Long,             // Total collected (cents)
    val invoiceCountByStatus: Map<String, Int>,  // Status → count
    val overdueCount: Int,                // Count of overdue invoices
    val averageDaysToPayment: Double      // Average DSO
)
```

---

### 4.6 `RiskMetricsV2`

```kotlin
data class RiskMetricsV2(
    val highRiskCount: Int,       // High-risk invoice count
    val atRiskCount: Int,         // At-risk invoice count
    val healthyCount: Int,        // Healthy invoice count
    val overdueCount: Int,        // Overdue invoice count
    val totalOutstanding: Long    // Outstanding amount (cents)
)
```

---

### 4.7 `BusinessContextV2`

```kotlin
data class BusinessContextV2(
    val businessId: Long,
    val businessName: String,
    val defaultTaxRate: Double,
    val isTaxRegistered: Boolean,
    val currencyCode: String
)
```

---

## 5. GUI2 Repository APIs

### 5.1 `PaymentRepositoryV2`

```kotlin
class PaymentRepositoryV2 @Inject constructor(
    private val database: AppDatabase,
    private val invoiceDaoV2: InvoiceDaoV2,
    private val paymentDaoV2: PaymentDaoV2
) {
    /**
     * Records a payment atomically (inserts payment + updates invoice amountPaid and status).
     */
    suspend fun recordPayment(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        paymentDate: Long,
        notes: String
    ): Result<Unit>

    /**
     * Returns a Flow of all payments for the given invoice.
     */
    fun observePaymentsByInvoice(invoiceId: Long): Flow<List<PaymentEntity>>
}
```

---

### 5.2 `RevenueRepositoryV2`

```kotlin
class RevenueRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2
) {
    /**
     * Returns a Flow of combined revenue metrics (MTD, YTD, weekly, total, trend).
     */
    fun observeRevenueMetrics(businessId: Long): Flow<RevenueMetricsV2>
}
```

---

### 5.3 `PaymentAnalyticsRepositoryV2`

```kotlin
class PaymentAnalyticsRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2
) {
    /**
     * Returns a Flow of combined payment analytics metrics.
     */
    fun observePaymentMetrics(businessId: Long): Flow<PaymentMetricsV2>
}
```

---

### 5.4 `RiskAnalyticsRepositoryV2`

```kotlin
class RiskAnalyticsRepositoryV2 @Inject constructor(
    private val invoiceDaoV2: InvoiceDaoV2
) {
    /**
     * Returns a Flow of risk classification metrics.
     */
    fun observeRiskMetrics(businessId: Long): Flow<RiskMetricsV2>
}
```

---

### 5.5 `BusinessContextRepositoryV2`

```kotlin
class BusinessContextRepositoryV2 @Inject constructor(
    private val businessProfileRepository: BusinessProfileRepository
) {
    /** Flow of the currently active business context. */
    val activeContext: Flow<BusinessContextV2>

    /** Flow of all business profiles as contexts. */
    val allContexts: Flow<List<BusinessContextV2>>
}
```
