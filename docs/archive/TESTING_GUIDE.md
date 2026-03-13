# Testing Guide

## Overview

Bizap uses **JUnit 4** + **MockK** for unit tests. All tests run on the JVM (no emulator required) using `Robolectric` for Android-specific APIs where needed.

## Running Tests

### All unit tests
```bash
cd Bizap
./gradlew :app:testDebugUnitTest
```

### Specific test class
```bash
./gradlew :app:testDebugUnitTest --tests "com.emul8r.bizap.CoreUnitTests"
```

### Specific test method
```bash
./gradlew :app:testDebugUnitTest --tests "*.createInvoice_validData_savesSuccessfully"
```

### With continuous run (re-runs on file change)
```bash
./gradlew :app:testDebugUnitTest --continuous
```

## Test Reports

After running tests:
- **HTML Report**: `app/build/reports/tests/testDebugUnitTest/index.html`
- **XML Results**: `app/build/test-results/testDebugUnitTest/*.xml`

## Test Structure

```
app/src/test/java/com/emul8r/bizap/
├── BaseUnitTest.kt                   # Base class (coroutine dispatcher setup)
├── CoreUnitTests.kt                  # 10 core business-logic tests
├── data/
│   ├── repository/                   # Repository tests
│   │   ├── InvoiceRepositoryTest.kt
│   │   ├── InvoiceRepositoryImplEnhancedTest.kt
│   │   ├── PaymentRepositoryTest.kt
│   │   ├── PaymentValidationTest.kt
│   │   └── OfflineQueueRepositoryImplTest.kt
│   ├── service/                      # Offline queue service tests
│   │   ├── OfflineQueueServiceSuite2Test.kt  # Customer operations
│   │   ├── OfflineQueueServiceSuite3Test.kt
│   │   └── OfflineQueueServiceSuite4Test.kt
│   ├── worker/SyncWorkerTest.kt      # Background sync worker
│   └── network/ErrorInterceptorTest.kt
├── domain/
│   ├── usecase/                      # Use case tests
│   │   ├── RecordPaymentUseCaseTest.kt
│   │   ├── SaveInvoiceUseCaseTest.kt
│   │   └── SyncPendingOperationsUseCaseTest.kt
│   └── validation/                   # Validation rule tests
│       ├── ValidationRulesTest.kt
│       ├── InvoiceValidationTest.kt
│       ├── CustomerValidationTest.kt
│       └── PaymentValidationTest.kt
├── ui/
│   ├── gui2/invoices/                # Invoice ViewModel tests
│   │   ├── CreateInvoiceViewModelTest.kt
│   │   ├── CreateInvoiceViewModelV2Test.kt
│   │   ├── EditInvoiceViewModelTest.kt
│   │   └── RecordPaymentViewModelTest.kt
│   ├── gui2/customers/               # Customer ViewModel tests
│   │   ├── CreateCustomerViewModelTest.kt
│   │   └── CreateCustomerViewModelV2Test.kt
│   └── landing/                      # Landing page tests
│       ├── LandingPageTest.kt
│       └── NavigationTest.kt
├── gui2/
│   ├── GuiModeTest.kt
│   └── RevenueRepositoryV2Test.kt
└── performance/PerformanceBaselineTest.kt
```

## Base Test Class

All ViewModel tests should extend `BaseUnitTest`:

```kotlin
abstract class BaseUnitTest {
    @get:Rule
    val instantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    protected val testDispatcher = StandardTestDispatcher()

    @Before fun setupBase() { Dispatchers.setMain(testDispatcher) }
    @After  fun tearDownBase() { Dispatchers.resetMain() }
}
```

This ensures `viewModelScope` coroutines run on the test dispatcher and `advanceUntilIdle()` works correctly.

## Mocking Conventions

All tests use **MockK** (not Mockito):

```kotlin
// Create a mock
val repo = mockk<InvoiceRepository>()
val relaxedRepo = mockk<InvoiceRepository>(relaxed = true)  // auto-stubs all calls

// Stub suspend functions
coEvery { repo.saveInvoice(any()) } returns Result.success(1L)

// Stub regular functions returning Flow
every { repo.getAllCustomers() } returns flowOf(listOf(customer))

// Verify calls
coVerify { repo.saveInvoice(invoice) }
verify { repo.getAllCustomers() }
```

## Coroutine Testing

```kotlin
@Test
fun `my test`() = runTest {
    // viewModelScope coroutines are controlled by testDispatcher
    viewModel.loadData()
    advanceUntilIdle()  // Runs all pending coroutines

    assertEquals(expected, viewModel.state.value)
}
```

## Common Patterns

### Testing a ViewModel
```kotlin
class MyViewModelTest : BaseUnitTest() {
    private val repo: MyRepository = mockk(relaxed = true)
    private lateinit var viewModel: MyViewModel

    @Before fun setUp() {
        viewModel = MyViewModel(repo)
    }

    @Test
    fun `loads data on init`() = runTest {
        every { repo.getData() } returns flowOf(listOf(item))
        advanceUntilIdle()
        assertEquals(listOf(item), viewModel.items.value)
    }
}
```

### Testing a UseCase
```kotlin
class MyUseCaseTest : BaseUnitTest() {
    private val repo: MyRepository = mockk()
    private val useCase = MyUseCase(repo)

    @Test
    fun `returns success on valid input`() = runTest {
        coEvery { repo.save(any()) } returns Result.success(1L)
        val result = useCase(validInput)
        assertTrue(result.isSuccess)
    }
}
```

## Test Dependencies

All test dependencies are declared in `app/build.gradle.kts`:

```kotlin
testImplementation(libs.junit)               // JUnit 4
testImplementation(libs.mockk)               // MockK
testImplementation(libs.mockito.core)        // Mockito (available if needed)
testImplementation(libs.mockito.kotlin)      // Mockito Kotlin extension
testImplementation(libs.coroutines.test)     // kotlinx-coroutines-test
testImplementation(libs.arch.core.test)      // InstantTaskExecutorRule
testImplementation(libs.robolectric)         // Android API on JVM
testImplementation(kotlin("test"))           // kotlin.test assertions
```

## Troubleshooting

| Error | Cause | Fix |
|-------|-------|-----|
| `IllegalStateException: Module with the Main dispatcher had failed to initialize` | `Dispatchers.Main` not set | Extend `BaseUnitTest` |
| `MockKException: no answer found` | Method not stubbed on non-relaxed mock | Add `coEvery`/`every` stub or use `relaxed = true` |
| `advanceUntilIdle` has no effect | Wrong dispatcher | Ensure `Dispatchers.setMain(testDispatcher)` is called |
| Compilation error on `getAllCustomers()` | Using non-existent method | Use `getAllCustomers()` returning `Flow<List<Customer>>` |
