# 👨‍💻 BIZAP DEVELOPER GUIDE

**Version:** 1.0  
**Last Updated:** March 29, 2026  
**For:** Contributors and maintainers

---

## Table of Contents
1. [Getting Started](#getting-started)
2. [Project Structure](#project-structure)
3. [Development Workflow](#development-workflow)
4. [Running Tests](#running-tests)
5. [Adding Features](#adding-features)
6. [Debugging](#debugging)
7. [Common Tasks](#common-tasks)
8. [Best Practices](#best-practices)

---

## Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Kotlin 1.9+
- Android SDK 28+ (target API 35)

### Setup

1. **Clone the repository**
```bash
git clone https://github.com/EmuBiz/Bizap.git
cd Bizap
```

2. **Open in Android Studio**
```bash
# Or open with Android Studio
Android Studio Bizap/
```

3. **Build the project**
```bash
# In Android Studio: Build → Make Project
# Or from command line:
./gradlew build
```

4. **Run on emulator/device**
```bash
./gradlew installDebug
```

### First Run
- The app creates a local Room database automatically
- No internet connection required
- Default business profile created on first launch

---

## Project Structure

```
Bizap/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/emul8r/bizap/
│   │   │   │   ├── MainActivity.kt              ← Entry point
│   │   │   │   ├── data/                       ← Data layer
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/                ← Database queries
│   │   │   │   │   │   ├── entities/           ← Room models
│   │   │   │   │   │   └── AppDatabase.kt
│   │   │   │   │   └── repository/             ← Repo implementations
│   │   │   │   ├── domain/                     ← Domain layer
│   │   │   │   │   ├── model/                  ← Business models
│   │   │   │   │   ├── repository/             ← Repo interfaces
│   │   │   │   │   └── usecase/                ← Business logic
│   │   │   │   ├── ui/                         ← Presentation layer
│   │   │   │   │   ├── gui2/                   ← Modern (Compose)
│   │   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   ├── invoices/
│   │   │   │   │   │   ├── customers/
│   │   │   │   │   │   ├── analytics/
│   │   │   │   │   │   ├── settings/
│   │   │   │   │   │   └── navigation/
│   │   │   │   │   ├── gui1/                   ← Legacy (XML)
│   │   │   │   │   └── common/
│   │   │   │   ├── di/                         ← Dependency injection
│   │   │   │   ├── utils/                      ← Helpers
│   │   │   │   ├── domain/                     ← Old domain code
│   │   │   │   └── ...other packages...
│   │   │   └── res/
│   │   │       ├── layout/                     ← XML layouts (GUI1)
│   │   │       ├── drawable/                   ← Images
│   │   │       ├── values/                     ← Colors, strings
│   │   │       └── ...
│   │   └── test/
│   │       └── java/com/emul8r/bizap/
│   │           ├── domain/usecase/*Test.kt    ← UseCase tests
│   │           ├── ui/gui2/*Test.kt           ← ViewModel tests
│   │           ├── ui/gui2/integration/*      ← Integration tests
│   │           └── util/                      ← Test utilities
│   └── build.gradle.kts                        ← Build config
└── gradle/
    └── wrapper/
        └── gradle-wrapper.jar                  ← Gradle version
```

---

## Development Workflow

### 1. Starting a Feature

**Step 1: Create a branch**
```bash
git checkout -b feature/customer-search
git checkout -b bugfix/payment-validation
```

**Step 2: Create the ViewModel** (if UI feature)
```kotlin
@HiltViewModel
class MyFeatureViewModel @Inject constructor(
    private val repository: SomeRepository,
    private val useCase: SomeUseCase
) : ViewModel() {
    // State
    private val _uiState = MutableStateFlow<UiState>(Loading)
    val uiState = _uiState.asStateFlow()
    
    // Event handlers
    fun onSomethingClicked() { }
}
```

**Step 3: Create the Compose Screen**
```kotlin
@Composable
fun MyFeatureScreen(
    viewModel: MyFeatureViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold { padding ->
        when (state) {
            is Loading -> LoadingIndicator()
            is Success -> ShowContent(state.data)
            is Error -> ShowError(state.message)
        }
    }
}
```

**Step 4: Wire to Navigation**
```kotlin
// In GuiV2NavGraph.kt
composable<ScreenV2.MyFeature> { backStackEntry ->
    MyFeatureScreen(
        onBack = { navController.popBackStack() }
    )
}
```

**Step 5: Add Test**
```kotlin
@Test
fun onSomethingClicked_ValidData_UpdatesState() = runTest {
    viewModel.onSomethingClicked()
    advanceUntilIdle()
    
    assertEquals(UiState.Success, viewModel.uiState.value)
}
```

**Step 6: Commit and push**
```bash
git add .
git commit -m "feat: Add customer search feature"
git push origin feature/customer-search
```

---

## Running Tests

### Unit Tests (UseCases, Models)

```bash
# Run all tests
./gradlew test

# Run specific test file
./gradlew test --tests MyUseCaseTest

# Run specific test method
./gradlew test --tests MyUseCaseTest.onSomething_ValidData_Success

# Run with debug output
./gradlew test --info
```

### Integration Tests (ViewModels + Repositories)

```bash
# Run integration tests
./gradlew testDebug

# These live in src/test/java/ui/gui2/integration/
```

### Instrumented Tests (UI on device/emulator)

```bash
# Run on connected device
./gradlew connectedAndroidTest

# This runs tests from src/androidTest/
```

### Test Coverage

```bash
# Generate coverage report
./gradlew testDebugUnitTestCoverage

# Find report at: app/build/reports/coverage/debug/index.html
```

---

## Adding Features

### Example: Add "Export Invoice" Feature

#### Step 1: Create UseCase
```kotlin
// File: domain/usecase/ExportInvoiceUseCase.kt
class ExportInvoiceUseCase @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) {
    suspend operator fun invoke(
        invoiceId: Long,
        format: String  // "pdf" or "csv"
    ): Result<File> = runCatching {
        val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId)
            .first() ?: throw Exception("Invoice not found")
        
        val file = when (format) {
            "pdf" -> exportAsPdf(invoice)
            "csv" -> exportAsCsv(invoice)
            else -> throw IllegalArgumentException("Unknown format")
        }
        
        file
    }
    
    private fun exportAsPdf(invoice: Invoice): File { /* impl */ }
    private fun exportAsCsv(invoice: Invoice): File { /* impl */ }
}
```

#### Step 2: Update ViewModel
```kotlin
// File: ui/gui2/invoice/InvoiceDetailViewModelV2.kt
@HiltViewModel
class InvoiceDetailViewModelV2 @Inject constructor(
    // ... existing deps ...
    private val exportInvoiceUseCase: ExportInvoiceUseCase
) : ViewModel() {
    
    fun onExportClicked(format: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            
            val result = exportInvoiceUseCase(invoiceId, format)
            
            result.onSuccess { file ->
                _events.emit(ExportEvent.Success(file))
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
            
            _uiState.update { it.copy(isExporting = false) }
        }
    }
}
```

#### Step 3: Update UI
```kotlin
// In InvoiceDetailScreenV2.kt
when (state) {
    is Success -> {
        // Add export button
        Button(
            onClick = { viewModel.onExportClicked("pdf") },
            enabled = !state.isExporting
        ) {
            if (state.isExporting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp))
            } else {
                Icon(Icons.Default.Download, null)
                Text("Export PDF")
            }
        }
    }
}
```

#### Step 4: Test
```kotlin
@Test
fun onExportClicked_ValidInvoice_EmitsSuccess() = runTest {
    coEvery { exportUseCase(1L, "pdf") } returns Result.success(testFile)
    
    viewModel.onExportClicked("pdf")
    advanceUntilIdle()
    
    assert(viewModel.uiState.value is Success)
    coVerify { exportUseCase(1L, "pdf") }
}
```

---

## Debugging

### View Database (Room)

**Option 1: Android Studio Database Inspector**
```
1. Device Explorer → databases
2. Right-click database
3. "Open in Database Inspector"
4. Run SQL queries directly
```

**Option 2: ADB Shell**
```bash
adb shell
sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db
.tables                    # List all tables
SELECT * FROM invoices;   # Query data
```

### View Logs

```bash
# All logs
./gradlew logcat

# Filter by tag
adb logcat | grep "bizap"

# Filter by level
adb logcat *:E            # Errors only
adb logcat *:W            # Warnings only
```

### Debug ViewModel State

Add logging to StateFlow:
```kotlin
val state = viewModel.uiState
    .onEach { Timber.d("State: $it") }
    .stateIn(viewModelScope, SharingStarted.Lazily, Initial)
```

### Breakpoints

1. Click line number to set breakpoint
2. Run app in debug mode
3. Android Studio pauses at breakpoint
4. Inspect variables in "Variables" panel

---

## Common Tasks

### Task 1: Add a New Invoice Status

```kotlin
// 1. Add to enum
enum class InvoiceStatus {
    DRAFT, SENT, PARTIALLY_PAID, PAID, OVERDUE, ARCHIVED
}

// 2. Update database query
invoiceDao.getByStatus(InvoiceStatus.ARCHIVED)

// 3. Update UI display
when (invoice.status) {
    InvoiceStatus.ARCHIVED -> showArchivedUI()
}

// 4. Test
@Test
fun getByStatus_Archived_ReturnsArchivedInvoices()
```

### Task 2: Add a New Payment Method

```kotlin
// 1. Add to enum
enum class PaymentMethod {
    CASH, CHECK, ACH_TRANSFER, WIRE_TRANSFER, CARD, APPLE_PAY, GOOGLE_PAY
}

// 2. Add to database
data class PaymentRecord(
    val paymentMethod: PaymentMethod = PaymentMethod.CASH
)

// 3. Add to UI dropdown
ExposedDropdownMenu(
    items = PaymentMethod.values(),
    selectedItem = paymentMethod,
    onItemSelected = { viewModel.onPaymentMethodChanged(it) }
)
```

### Task 3: Add a New Dashboard Metric

```kotlin
// 1. Update data model
data class DashboardMetrics(
    // ... existing fields ...
    val averageInvoiceAmount: Long = 0  // NEW
)

// 2. Update query
override suspend fun getDashboardMetrics(businessId: Long): Result<DashboardMetrics> {
    val invoices = invoiceDao.getAllByBusiness(businessId)
    val avgAmount = invoices.map { it.totalAmount }.average().toLong()
    
    return Result.success(
        DashboardMetrics(
            // ... existing values ...
            averageInvoiceAmount = avgAmount
        )
    )
}

// 3. Display in UI
MetricsCard(
    title = "Avg Invoice",
    value = formatCents(metrics.averageInvoiceAmount),
    icon = Icons.Default.TrendingUp
)
```

---

## Best Practices

### 1. State Management

✅ **DO:**
```kotlin
// Immutable state updates
_uiState.update { current ->
    current.copy(field = newValue)
}
```

❌ **DON'T:**
```kotlin
// Mutable state modifications
_uiState.value.field = newValue  // This won't notify observers!
```

### 2. Error Handling

✅ **DO:**
```kotlin
val result = useCase(...)
result.onSuccess { data ->
    // Handle success
}.onFailure { error ->
    Timber.e(error, "Operation failed")
    // Handle error
}
```

❌ **DON'T:**
```kotlin
try {
    val result = repository.getData()
} catch (e: Exception) {
    // Losing context about what failed
}
```

### 3. Testing

✅ **DO:**
```kotlin
@Test
fun createInvoice_WithValidData_SavesSuccessfully() = runTest {
    // Clear test name, arrange-act-assert
}
```

❌ **DON'T:**
```kotlin
@Test
fun test() {
    // Unclear what's being tested
}
```

### 4. Naming

✅ **DO:**
```kotlin
fun onSaveClicked() { }              // User action
fun updateInvoiceState() { }          // Internal operation
val isLoading: Flow<Boolean> = ...   // Boolean states
```

❌ **DON'T:**
```kotlin
fun save() { }                        // Ambiguous
fun process() { }                     // Too generic
val loading: String = "true"         // Type mismatch
```

### 5. Logging

✅ **DO:**
```kotlin
Timber.d("Invoice saved: ${invoice.id}")
Timber.e(exception, "Failed to record payment")
```

❌ **DON'T:**
```kotlin
println("Invoice saved")              // Not visible in logcat
Log.d("TAG", "success")              // Use Timber instead
```

### 6. Dependency Injection

✅ **DO:**
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository  // Injected
) : ViewModel()
```

❌ **DON'T:**
```kotlin
val repository = RepositoryImpl()      // Hard dependency, not testable
```

### 7. Database Queries

✅ **DO:**
```kotlin
// Room handles threading automatically
fun getInvoices(): Flow<List<Invoice>> {
    return invoiceDao.getAllInvoices()
}
```

❌ **DON'T:**
```kotlin
// Manual threading is error-prone
fun getInvoices(): List<Invoice> {
    return invoiceDao.getAllInvoices()  // Blocks UI thread!
}
```

---

## Code Review Checklist

Before submitting a pull request:

- [ ] Code follows naming conventions
- [ ] All new public APIs have KDoc comments
- [ ] Unit tests written for new logic
- [ ] No hardcoded strings (use resources)
- [ ] No `println()` or `Log.d()` (use Timber)
- [ ] Proper error handling (Result pattern)
- [ ] Database changes are backward compatible
- [ ] No memory leaks (check lifecycle)
- [ ] Builds without warnings
- [ ] Tests pass locally

---

## Resources

- **Kotlin Docs:** https://kotlinlang.org/docs/
- **Android Docs:** https://developer.android.com/docs
- **Jetpack Compose:** https://developer.android.com/jetpack/compose
- **Room Database:** https://developer.android.com/training/data-storage/room
- **Hilt DI:** https://developer.android.com/training/dependency-injection/hilt-android
- **Coroutines:** https://kotlinlang.org/docs/coroutines-overview.html

---

## Getting Help

1. **Check the codebase** - Similar features might exist
2. **Read architecture docs** - `ARCHITECTURE_GUIDE.md`
3. **Look at tests** - Test code shows usage patterns
4. **Check Kotlin docs** - Language-specific issues
5. **Create an issue** - If stuck, document and ask

---

**Happy coding! 🚀**


