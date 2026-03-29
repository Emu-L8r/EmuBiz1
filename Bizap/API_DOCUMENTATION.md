# 📚 BIZAP API DOCUMENTATION

**Version:** 1.0  
**Last Updated:** March 29, 2026  
**Status:** Production Ready

---

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [ViewModel Layer](#viewmodel-layer)
3. [Repository Layer](#repository-layer)
4. [UseCase Layer](#usecase-layer)
5. [Data Models](#data-models)
6. [Common Patterns](#common-patterns)

---

## Architecture Overview

### 3-Layer Clean Architecture

```
┌─────────────────────────────────────┐
│         UI Layer (Compose)          │
│  - Screens, Dialogs, Components     │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  ViewModel Layer (MVI Pattern)      │
│  - State Management (StateFlow)     │
│  - Event Handling                   │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│   Repository Layer (Data Access)   │
│  - Room Database                    │
│  - API Calls (if needed)            │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Domain Layer (Business Logic)   │
│  - UseCases                         │
│  - Models                           │
└─────────────────────────────────────┘
```

### Design Principles
- **Offline-First:** All data stored locally in Room
- **Type-Safe:** Kotlin + Room typed SQL queries
- **Reactive:** Coroutines + Flow for async operations
- **Testable:** Constructor injection via Hilt

---

## ViewModel Layer

### Pattern: MVI (Model-View-Intent)

```kotlin
// Example: CreateInvoiceViewModelV2

@HiltViewModel
class CreateInvoiceViewModelV2 @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {
    
    // State: Current UI state
    private val _uiState = MutableStateFlow<CreateInvoiceUiStateV2>(...)
    val uiState: StateFlow<CreateInvoiceUiStateV2> = _uiState.asStateFlow()
    
    // Intent: User actions
    fun onCustomerSelected(customerId: Long) { ... }
    fun onAmountChanged(amount: String) { ... }
    fun onSaveClicked() { ... }
}
```

### Key ViewModels

#### 1. **CreateInvoiceViewModelV2**
Creates new invoices with customer selection and line items.

**Methods:**
- `onCustomerSelected(Long)` - Select customer
- `onAddLineItem(String, Long, Long)` - Add item description, qty, price (cents)
- `onSaveClicked()` - Save invoice
- `clearError()` - Clear error message

**State:**
```kotlin
sealed class CreateInvoiceUiStateV2 {
    object Loading : CreateInvoiceUiStateV2()
    data class Success(...) : CreateInvoiceUiStateV2()
    data class Error(val message: String) : CreateInvoiceUiStateV2()
}
```

#### 2. **RecordPaymentViewModelV2**
Records payments against invoices with validation.

**Methods:**
- `initFor(invoiceId, businessId, ...)` - Initialize for invoice
- `onAmountChanged(String)` - Update payment amount
- `onDateChanged(Long)` - Update payment date
- `recordPayment()` - Submit payment

**Key Feature:** Same-day payment support (date normalization fix)

#### 3. **PaymentAnalyticsViewModelV2**
Displays payment metrics with filtering.

**Methods:**
- `setDateRange(Long, Long)` - Filter by date
- `setStatusFilter(InvoiceStatus)` - Filter by status
- `exportAsCSV()` - Export data

#### 4. **DashboardMetricsViewModel** (Future)
Manages dashboard metric calculations.

**Methods:**
- `getDashboardMetrics()` - Get unpaid, overdue, paid counts
- `refresh()` - Recalculate metrics

---

## Repository Layer

### Pattern: Repository Pattern

Repositories abstract data sources and provide a clean API for ViewModels.

### Key Repositories

#### 1. **InvoiceRepository**
```kotlin
interface InvoiceRepository {
    suspend fun saveInvoice(invoice: Invoice): Result<Long>
    fun getInvoiceWithItemsById(id: Long): Flow<Invoice?>
    fun getAllInvoicesWithItems(): Flow<List<Invoice>>
    suspend fun deleteInvoice(id: Long): Result<Unit>
    suspend fun getDashboardMetrics(businessId: Long): Result<DashboardMetrics>
}
```

#### 2. **CustomerRepository**
```kotlin
interface CustomerRepository {
    suspend fun saveCustomer(customer: Customer): Result<Long>
    fun getCustomerById(id: Long): Flow<Customer?>
    fun getAllCustomers(): Flow<List<Customer>>
    suspend fun deleteCustomer(id: Long): Result<Unit>
}
```

#### 3. **PaymentRepositoryV2**
```kotlin
interface PaymentRepository {
    suspend fun recordPayment(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        paymentDate: Long,
        notes: String?
    ): Result<Unit>
    
    fun getPaymentHistory(invoiceId: Long): Flow<List<Payment>>
    suspend fun getPaymentMetrics(businessId: Long): Result<PaymentMetricsV2>
}
```

#### 4. **ExchangeRateRepository** (Offline-First)
```kotlin
interface ExchangeRateRepository {
    suspend fun getRate(fromCurrency: String, toCurrency: String): Result<Double>
    suspend fun cacheRate(from: String, to: String, rate: Double)
    fun getRatesFlow(): Flow<List<ExchangeRateEntity>>
}
```

**Implementation:** Room database with offline caching

---

## UseCase Layer

### Pattern: Single Responsibility

Each UseCase handles one business operation.

### Key UseCases

#### 1. **RecordPaymentUseCase**
Validates and records a payment for an invoice.

```kotlin
suspend operator fun invoke(
    invoiceId: Long,
    businessId: Long,
    amount: Long,              // cents
    trueOutstanding: Long,     // cents
    paymentDate: Long,         // ms (midnight)
    invoiceDate: Long,         // ms
    invoiceStatus: InvoiceStatus,
    notes: String? = null
): Result<Unit>
```

**Validation Rules:**
- ✅ Amount > 0 and ≤ outstanding balance
- ✅ Payment date ≤ today (no future payments)
- ✅ Payment date ≥ invoice date (with midnight normalization)
- ✅ Invoice must not be DRAFT status

**Bug Fix:** Normalizes `invoiceDate` to midnight before comparing with `paymentDate` (both midnight) to allow same-day payments.

#### 2. **CalculateInvoiceMetricsUseCase**
Calculates totals, tax, and subtotal for an invoice.

```kotlin
suspend operator fun invoke(
    items: List<LineItem>,
    taxRate: Double = 0.0
): Result<InvoiceMetrics>
```

Returns:
```kotlin
data class InvoiceMetrics(
    val subtotal: Long,        // cents
    val taxAmount: Long,       // cents
    val totalAmount: Long      // cents
)
```

---

## Data Models

### Core Entities

#### Invoice
```kotlin
@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val id: Long = 0,
    val businessId: Long,
    val customerId: Long,
    val invoiceNumber: String,
    val status: String,           // DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE
    val totalAmount: Long,        // cents
    val amountPaid: Long = 0,     // cents
    val createdAt: Long,
    val dueDate: Long,
    val notes: String? = null
)
```

#### Customer
```kotlin
@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey val id: Long = 0,
    val businessId: Long,
    val name: String,
    val email: String? = null,    // Now optional!
    val phone: String? = null,
    val address: String? = null,
    val createdAt: Long
)
```

#### Payment (in 'payment_records' table)
```kotlin
@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey val id: Long = 0,
    val invoiceId: Long,
    val amount: Long,              // cents
    val paymentMethod: String,     // CASH, CHECK, ACH_TRANSFER, etc.
    val notes: String? = null,
    val recordedAt: Long
)
```

#### DashboardMetrics (Calculated DTO)
```kotlin
data class DashboardMetrics(
    val unpaidInvoiceCount: Int,   // Number of unpaid invoices
    val unpaidAmount: Long,        // Total unpaid amount (cents)
    val overdueAmount: Long,       // Count of overdue invoices (stored as Long)
    val paidThisMonth: Long,       // Count of invoices sent this month
    val totalCustomersOwed: Long,  // Total outstanding (cents)
    val lastUpdatedMs: Long
)
```

#### PaymentMetricsV2 (Analysis DTO)
```kotlin
data class PaymentMetricsV2(
    val businessProfileId: Long,
    val totalInvoices: Int,
    val paidCount: Int,
    val sentCount: Int,
    val overdueCount: Int,
    val partiallyPaidCount: Int,
    val draftCount: Int,
    val outstandingAmount: Long,       // cents
    val collectedAmount: Long,         // cents
    val collectionRate: Double,        // 0.0-100.0
    val averageDaysToPayment: Double,
    val statusBreakdown: List<StatusBreakdownV2>
)
```

---

## Common Patterns

### 1. Result Pattern (Error Handling)
```kotlin
// Success: Result.success(data)
// Failure: Result.failure(exception)

val result = repository.saveInvoice(invoice)
result.onSuccess { invoiceId ->
    // Handle success
}.onFailure { exception ->
    // Handle error
}
```

### 2. StateFlow Pattern (State Management)
```kotlin
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Update state
_uiState.update { currentState ->
    currentState.copy(/* new values */)
}

// Collect in UI
val state by viewModel.uiState.collectAsStateWithLifecycle()
```

### 3. Flow Pattern (Data Streams)
```kotlin
fun getInvoices(): Flow<List<Invoice>> {
    return invoiceDao.getAllInvoices()
        .map { entities ->
            entities.toDomainModel()
        }
        .catch { error ->
            Timber.e(error)
        }
}
```

### 4. Offline-First Pattern
All data is:
- ✅ Stored in local Room database
- ✅ Synced when online (optional)
- ✅ Queried from local DB first
- ✅ Never requires network

---

## Dependencies & Injection

### Hilt Setup
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideInvoiceRepository(
        invoiceDao: InvoiceDao,
        database: AppDatabase
    ): InvoiceRepository {
        return InvoiceRepositoryImpl(invoiceDao, database)
    }
    
    @Provides
    @Singleton
    fun providePaymentRepository(
        database: AppDatabase,
        invoiceDao: InvoiceDao
    ): PaymentRepository {
        return PaymentRepositoryV2(database, invoiceDao)
    }
}
```

### Usage in ViewModels
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    // ViewModels receive dependencies via constructor
}
```

---

## Testing

### Base Test Class
```kotlin
abstract class BaseUnitTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    protected val testDispatchers = TestDispatchers()
    
    @Before
    fun setupDispatchers() {
        Dispatchers.setMain(testDispatchers.mainDispatcher)
    }
}
```

### Example Test
```kotlin
@Test
fun recordPayment_ValidData_Success() = runTest {
    // Arrange
    val result = useCase(
        invoiceId = 1L,
        amount = 50000L,
        paymentDate = todayMidnight,
        ...
    )
    
    // Assert
    assertTrue(result.isSuccess)
    coVerify { paymentRepository.recordPayment(...) }
}
```

---

## Quick Reference: Common Tasks

### Create Invoice
```kotlin
val invoice = Invoice(
    customerId = selectedCustomer.id,
    invoiceNumber = "INV-001",
    totalAmount = 10000L,  // $100.00
    dueDate = System.currentTimeMillis() + 30_days
)
viewModel.onSaveClicked()
```

### Record Payment
```kotlin
val result = recordPaymentUseCase(
    invoiceId = 123L,
    amount = 5000L,  // $50.00
    paymentDate = System.currentTimeMillis(),
    ...
)
```

### Query Dashboard Metrics
```kotlin
val metrics = invoiceRepository.getDashboardMetrics(businessId)
// Returns: unpaidInvoiceCount, overdueAmount, etc.
```

### Filter Payments
```kotlin
paymentAnalyticsViewModel.setDateRange(startMs, endMs)
paymentAnalyticsViewModel.setStatusFilter(InvoiceStatus.SENT)
```

---

## Resources

- **Database Schema:** See `AppDatabase.kt`
- **Entity Models:** `data/local/entities/*.kt`
- **DAOs:** `data/local/dao/*.kt`
- **ViewModels:** `ui/gui2/*ViewModel*.kt`
- **Tests:** `src/test/java/**/*Test.kt`

---

**Questions?** Refer to test files for usage examples.


