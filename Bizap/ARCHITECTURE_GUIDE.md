# 🏗️ BIZAP ARCHITECTURE GUIDE

**Version:** 1.0  
**Last Updated:** March 29, 2026  
**Pattern:** Clean Architecture + MVI

---

## Quick Overview

```
USER
  │
  ▼
┌─────────────────────────────────────┐
│     PRESENTATION LAYER (Compose)    │  ← Screens, Dialogs, Components
├─────────────────────────────────────┤
│   APPLICATION LAYER (ViewModels)    │  ← State, Events, Business Logic
├─────────────────────────────────────┤
│     DATA LAYER (Repositories)       │  ← Database, APIs, Caching
├─────────────────────────────────────┤
│    DOMAIN LAYER (UseCases/Models)   │  ← Pure Business Rules
└─────────────────────────────────────┘
  │
  ▼
LOCAL DATABASE (Room - Offline First)
```

---

## Layer Responsibilities

### 1️⃣ PRESENTATION LAYER

**Location:** `ui/gui2/`, `ui/gui2/invoices/`, `ui/gui2/dashboard/`, etc.

**Responsibility:** Display data and capture user input

**Components:**
- **Screens:** Full-screen Composables (CreateInvoiceScreenV2, DashboardScreenV2)
- **Dialogs:** Modal components (RecordPaymentDialogV2, AddPhotoDialogV2)
- **Components:** Reusable UI elements (MetricsCard, QuickActionButtons)
- **Widgets:** Specialized components (DashboardMetricsWidget, InvoiceStatusChart)

**Rules:**
- ✅ ONLY display data, don't calculate
- ✅ Pass user actions to ViewModel
- ✅ Use StateFlow for state collection
- ✅ Use Jetpack Compose for UI
- ❌ NO direct database access
- ❌ NO business logic

**Example: CreateInvoiceScreenV2**
```kotlin
@Composable
fun CreateInvoiceScreenV2(
    businessId: Long,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    viewModel: CreateInvoiceViewModelV2 = hiltViewModel()
) {
    // 1. Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 2. Display UI based on state
    when (uiState) {
        is Success -> ShowForm(state.invoice)
        is Error -> ShowError(state.message)
        is Loading -> ShowLoadingIndicator()
    }
    
    // 3. Send user actions to ViewModel
    OnCustomerSelected { customerId ->
        viewModel.onCustomerSelected(customerId)
    }
}
```

---

### 2️⃣ APPLICATION LAYER (ViewModels)

**Location:** `ui/gui2/*ViewModel*.kt`

**Responsibility:** Manage UI state and handle user events

**Pattern:** MVI (Model-View-Intent)

```
    User Input (Intent)
           │
           ▼
    ┌──────────────┐
    │  ViewModel   │
    │  - Validates │
    │  - Calls Use │
    │    Cases     │
    └──────────────┘
           │
           ▼
    State (Model) → Display
```

**Components:**
- **State:** Current UI state (data + UI hints)
- **Events:** User actions (onSaveClicked, onAmountChanged)
- **Methods:** Handlers for events

**Rules:**
- ✅ Hold state in StateFlow<UiState>
- ✅ Validate user input
- ✅ Call UseCases for business logic
- ✅ Update state based on results
- ❌ NO direct database access
- ❌ NO UI rendering

**Example: RecordPaymentViewModelV2**
```kotlin
@HiltViewModel
class RecordPaymentViewModelV2 @Inject constructor(
    private val recordPaymentUseCase: RecordPaymentUseCase,
    private val eventTracker: FirebaseEventTracker
) : ViewModel() {
    
    // State
    private val _formState = MutableStateFlow(PaymentFormState())
    val formState: StateFlow<PaymentFormState> = _formState.asStateFlow()
    
    // Event: User enters amount
    fun onAmountChanged(amount: String) {
        _formState.update { 
            it.copy(amount = amount, amountError = validateAmount(amount))
        }
    }
    
    // Event: User clicks Save
    fun recordPayment() {
        viewModelScope.launch {
            // Call UseCase
            val result = recordPaymentUseCase(
                invoiceId = invoiceId,
                amount = amountInput.toLong(),
                paymentDate = selectedDate,
                ...
            )
            
            result.onSuccess {
                eventTracker.logPaymentRecorded()
                _events.emit(PaymentEvent.Success)
            }.onFailure { error ->
                _formState.update { it.copy(error = error.message) }
            }
        }
    }
}
```

---

### 3️⃣ DATA LAYER (Repositories)

**Location:** `data/repository/`

**Responsibility:** Abstract data sources and provide clean API

**Pattern:** Repository Pattern

**Components:**
- **Repositories:** Interfaces defining data contracts
- **Implementations:** Room queries, API calls, caching
- **DAOs:** Database access objects
- **Entities:** Room database models

**Rules:**
- ✅ Provide interfaces for dependency injection
- ✅ Implement offline-first caching
- ✅ Return Result<T> for error handling
- ✅ Return Flow<T> for reactive streams
- ❌ NO business logic
- ❌ NO direct UI access

**Example: InvoiceRepositoryImpl**
```kotlin
class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val database: AppDatabase
) : InvoiceRepository {
    
    // Get single invoice with items
    override fun getInvoiceWithItemsById(id: Long): Flow<Invoice?> {
        return invoiceDao.getInvoiceWithItemsById(id)
            .map { entity -> entity?.toDomainModel() }
    }
    
    // Save invoice (offline-first)
    override suspend fun saveInvoice(invoice: Invoice): Result<Long> {
        return runCatching {
            val entity = invoice.toEntity()
            invoiceDao.insertInvoice(entity)
        }
    }
    
    // Get dashboard metrics
    override suspend fun getDashboardMetrics(businessId: Long): Result<DashboardMetrics> {
        return runCatching {
            val invoices = invoiceDao.getAllInvoicesByBusiness(businessId)
            
            val unpaidCount = invoices.count { 
                it.amountPaid < it.totalAmount 
            }
            val overdueCount = invoices.count {
                it.dueDate < System.currentTimeMillis() &&
                it.amountPaid < it.totalAmount
            }
            
            DashboardMetrics(
                unpaidInvoiceCount = unpaidCount,
                overdueAmount = overdueCount.toLong(),
                ...
            )
        }
    }
}
```

---

### 4️⃣ DOMAIN LAYER (UseCases)

**Location:** `domain/usecase/`

**Responsibility:** Pure business logic, independent of frameworks

**Pattern:** UseCase Pattern (Single Responsibility)

**Components:**
- **UseCases:** Operator functions with business logic
- **Models:** Business domain models
- **Interfaces:** Repository contracts (for di)

**Rules:**
- ✅ Implement ONE business operation
- ✅ Use operator fun invoke() for single operation
- ✅ Validate business rules
- ✅ Return Result<T> for error handling
- ❌ NO Android dependencies
- ❌ NO framework imports
- ❌ NO direct data access

**Example: RecordPaymentUseCase**
```kotlin
class RecordPaymentUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(
        invoiceId: Long,
        businessId: Long,
        amount: Long,
        trueOutstanding: Long,
        paymentDate: Long,
        invoiceDate: Long,
        invoiceStatus: InvoiceStatus,
        notes: String? = null
    ): Result<Unit> {
        // RULE 1: Validate amount
        if (amount <= 0) {
            return Result.failure(
                IllegalArgumentException("Amount must be > 0")
            )
        }
        
        // RULE 2: Validate amount doesn't exceed outstanding
        if (amount > trueOutstanding) {
            return Result.failure(
                IllegalArgumentException("Payment exceeds balance")
            )
        }
        
        // RULE 3: Validate date (with normalization for same-day payments)
        val todayMidnight = todayMidnightMs()
        if (paymentDate > todayMidnight) {
            return Result.failure(
                IllegalArgumentException("Cannot record future payment")
            )
        }
        
        val invoiceDateMidnight = Calendar.getInstance().apply {
            timeInMillis = invoiceDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        if (paymentDate < invoiceDateMidnight) {
            return Result.failure(
                IllegalArgumentException("Payment cannot be before invoice date")
            )
        }
        
        // RULE 4: Call repository
        return paymentRepository.recordPayment(
            invoiceId = invoiceId,
            businessId = businessId,
            amount = amount,
            paymentDate = paymentDate,
            notes = notes
        )
    }
}
```

---

## Data Flow Example: Create Invoice

```
USER TAPS "Save"
    │
    ▼
CreateInvoiceScreenV2.onSaveClicked()
    │
    ▼
viewModel.onSaveClicked()  [APPLICATION LAYER]
    │
    ├─ Validate form data
    └─ Call saveInvoiceUseCase()
        │
        ▼
SaveInvoiceUseCase.invoke()  [DOMAIN LAYER]
    │
    ├─ Validate business rules
    └─ Call invoiceRepository.saveInvoice()
        │
        ▼
InvoiceRepositoryImpl.saveInvoice()  [DATA LAYER]
    │
    ├─ Convert to entity
    └─ invoiceDao.insert()
        │
        ▼
Room Database  [LOCAL - NO NETWORK]
    │
    ▼
SAVED ✓ (Offline-First)
```

---

## Offline-First Architecture

### Key Principle
**All data is stored locally. Network is optional.**

```
App Startup
    │
    ▼
┌─────────────────────────────┐
│ Load from Room Database     │  ✅ Always works (offline)
│ (already synced on creation)│
└─────────────────────────────┘
    │
    ▼
User Creates Invoice
    │
    ▼
┌─────────────────────────────┐
│ Save to Room Database       │  ✅ Works offline
│ (local persistence)         │
└─────────────────────────────┘
    │
    ▼
Is Device Online?
    │
    ├─── YES ──→ Sync to Server (optional)
    │
    └─── NO  ──→ Queue for later sync
```

### Benefits
- ✅ **Works offline** - No internet required
- ✅ **Fast** - All queries local
- ✅ **Reliable** - No network errors
- ✅ **Simple** - No sync complexity

---

## Dependency Injection (Hilt)

### Module Structure
```
di/
├── RepositoryModule.kt     → Repositories
├── DatabaseModule.kt       → Room Database
├── UseCaseModule.kt        → UseCases
└── DispatchersModule.kt    → Coroutine dispatchers
```

### Example: RepositoryModule
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideInvoiceRepository(
        invoiceDao: InvoiceDao,
        database: AppDatabase
    ): InvoiceRepository = InvoiceRepositoryImpl(invoiceDao, database)
    
    @Provides
    @Singleton
    fun providePaymentRepository(
        database: AppDatabase,
        invoiceDao: InvoiceDao
    ): PaymentRepository = PaymentRepositoryV2(database, invoiceDao)
}
```

### Using in ViewModel
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {
    // Dependencies are injected automatically
}
```

---

## Error Handling: Result Pattern

```kotlin
// All suspend functions return Result<T>

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure<T>(val exception: Exception) : Result<T>()
}

// Usage
val result = repository.saveInvoice(invoice)

result.onSuccess { invoiceId ->
    // Handle success (invoiceId available)
}.onFailure { exception ->
    // Handle error (exception details available)
}

// Or with when
when (result) {
    is Result.Success -> { /* handle success */ }
    is Result.Failure -> { /* handle failure */ }
}
```

---

## State Management: StateFlow Pattern

```kotlin
// ViewModel holds state
private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Update state immutably
_uiState.update { currentState ->
    currentState.copy(newField = newValue)
}

// UI collects state
val state by viewModel.uiState.collectAsStateWithLifecycle()
when (state) {
    is UiState.Loading -> { /* show loading */ }
    is UiState.Success -> { /* show data */ }
    is UiState.Error -> { /* show error */ }
}
```

---

## Testing Architecture

### Test Pyramid
```
          ▲
        /   \
       /  E2E \       EndToEndTest (slow, comprehensive)
      /__________\
        /    \
       / Integ\      IntegrationTest (ViewModels + Repos)
      /________\
        /    \
       /  Unit \     UnitTest (UseCases, Models)
      /________\
```

### Test Examples

**Unit Test (UseCase)**
```kotlin
@Test
fun recordPayment_ValidData_Success() = runTest {
    val result = useCase(
        invoiceId = 1L,
        amount = 50000L,
        paymentDate = todayMidnight,
        ...
    )
    
    assertTrue(result.isSuccess)
}
```

**Integration Test (ViewModel)**
```kotlin
@Test
fun onSaveClicked_ValidInvoice_EmitsSuccess() = runTest {
    viewModel.onCustomerSelected(1L)
    viewModel.onAmountChanged("100.00")
    viewModel.onSaveClicked()
    
    advanceUntilIdle()
    
    val state = viewModel.uiState.value
    assertTrue(state is CreateInvoiceUiStateV2.Success)
}
```

**End-to-End Test**
```kotlin
@Test
fun createInvoiceAndPayment_FullJourney() = runTest {
    // Create invoice
    invoiceViewModel.onCustomerSelected(1L)
    invoiceViewModel.onSaveClicked()
    advanceUntilIdle()
    
    // Record payment
    paymentViewModel.initFor(invoiceId, businessId, ...)
    paymentViewModel.onAmountChanged("100.00")
    paymentViewModel.recordPayment()
    advanceUntilIdle()
    
    // Verify
    val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
    assertEquals(10000L, invoice.amountPaid)
}
```

---

## Folder Structure

```
app/src/main/java/com/emul8r/bizap/
├── MainActivity.kt                 ← Entry point
├── di/                            ← Dependency injection modules
│   ├── RepositoryModule.kt
│   ├── DatabaseModule.kt
│   └── UseCaseModule.kt
├── data/                          ← DATA LAYER
│   ├── local/
│   │   ├── dao/                   ← Database queries
│   │   ├── entities/              ← Room models
│   │   └── AppDatabase.kt         ← Database config
│   └── repository/                ← Repository implementations
├── domain/                        ← DOMAIN LAYER
│   ├── model/                     ← Business models
│   ├── repository/                ← Repository interfaces
│   └── usecase/                   ← Business logic
├── ui/                            ← PRESENTATION LAYER
│   ├── gui2/                      ← Modern interface (Compose)
│   │   ├── dashboard/
│   │   ├── invoices/
│   │   ├── customers/
│   │   ├── settings/
│   │   └── navigation/
│   ├── gui1/                      ← Legacy interface (XML)
│   └── common/                    ← Shared components
└── utils/                         ← Helpers

app/src/test/java/
└── com/emul8r/bizap/
    ├── domain/usecase/*Test.kt
    ├── ui/gui2/*ViewModelTest.kt
    ├── ui/gui2/integration/*IntegrationTest.kt
    └── util/                      ← Test utilities
```

---

## Design Decisions

### 1. Offline-First
**Decision:** All data stored locally, network optional  
**Rationale:** Simple, fast, reliable, works without internet  
**Trade-off:** No real-time sync (not needed for invoicing app)

### 2. MVI Pattern
**Decision:** Model-View-Intent state management  
**Rationale:** Clear data flow, easy to test, unidirectional  
**Trade-off:** More boilerplate than MVVM

### 3. Clean Architecture
**Decision:** Separated layers (Presentation, Application, Data, Domain)  
**Rationale:** Testable, maintainable, independent of frameworks  
**Trade-off:** Requires more setup than monolithic

### 4. Result Pattern
**Decision:** All operations return Result<T> instead of throwing  
**Rationale:** Explicit error handling, composable  
**Trade-off:** More verbose than exceptions

---

## Future Improvements

- [ ] Add network sync layer (optional cloud)
- [ ] Implement caching strategy for analytics
- [ ] Add pagination for large lists
- [ ] Implement search indexing
- [ ] Add push notifications
- [ ] Implement cloud backup (optional)

---

**This architecture enables:** Offline-first app, Clean separation of concerns, Easy testing, Safe error handling, Reactive data flows


