# Testing Guide — Bizap (EmuBiz1)

**Last Updated:** 2026-03-08  
**Test Framework:** JUnit 4 + MockK + Kotlin Coroutines Test

---

## Table of Contents

1. [Test Infrastructure Overview](#1-test-infrastructure-overview)
2. [Running Tests](#2-running-tests)
3. [Unit Test Setup](#3-unit-test-setup)
4. [Integration Test Setup](#4-integration-test-setup)
5. [Test Data Factories](#5-test-data-factories)
6. [Testing Patterns](#6-testing-patterns)
7. [Test Coverage Summary](#7-test-coverage-summary)
8. [Writing New Tests](#8-writing-new-tests)

---

## 1. Test Infrastructure Overview

```
Bizap/app/src/
├── test/java/                          # JVM unit tests (47 files)
│   └── com/emul8r/bizap/
│       ├── data/
│       │   ├── repository/             # Repository unit tests
│       │   └── local/dao/              # DAO unit tests (with mocks)
│       ├── domain/
│       │   ├── usecase/                # Use case tests
│       │   └── validation/             # Validation rule tests
│       └── ui/
│           ├── gui2/                   # GUI2 ViewModel tests
│           └── templates/              # Template ViewModel tests
│
└── androidTest/java/                   # Instrumented tests (11 files)
    └── com/emul8r/bizap/
        ├── data/local/migrations/      # Room migration tests (6 files)
        └── e2e/                        # End-to-end tests (3 files)
```

**Key Dependencies (test scope):**

| Library | Purpose |
|---------|---------|
| `JUnit 4` | Test runner |
| `MockK` | Kotlin-idiomatic mocking |
| `kotlinx-coroutines-test` | Coroutine testing utilities |
| `turbine` | Flow testing |
| `androidx.test.ext:junit` | Android JUnit extensions |
| `Room testing` | In-memory Room DB for migration tests |

---

## 2. Running Tests

### Unit Tests

```bash
# Run all unit tests
cd Bizap && ./gradlew :app:testDebugUnitTest

# Run a specific test class
./gradlew :app:testDebugUnitTest --tests "com.emul8r.bizap.data.repository.InvoiceRepositoryTest"

# Run with continuous output
./gradlew :app:testDebugUnitTest --info

# Run tests and open HTML report
./gradlew :app:testDebugUnitTest && open app/build/reports/tests/testDebugUnitTest/index.html
```

### Instrumented/Integration Tests

```bash
# Run instrumented tests (requires connected device or emulator)
cd Bizap && ./gradlew :app:connectedDebugAndroidTest

# Run a specific instrumented test
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.emul8r.bizap.data.local.migrations.Migration23To24Test
```

### CI/Build

The CI workflow runs unit tests automatically on every pull request:

```yaml
# .github/workflows/android-ci.yml
- name: Run unit tests
  run: ./gradlew :app:testDebugUnitTest
```

---

## 3. Unit Test Setup

### 3.1 Base Test Class

```kotlin
// BaseUnitTest.kt
abstract class BaseUnitTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Test dispatcher for coroutines
    protected val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
```

### 3.2 Testing ViewModels with StateFlow

```kotlin
@Test
fun `invoice list loads successfully`() = runTest {
    // Arrange
    val invoices = listOf(testInvoice())
    every { mockInvoiceRepository.getInvoices(BUSINESS_ID) } returns flowOf(invoices)

    val viewModel = InvoiceListViewModelV2(mockInvoiceRepository)

    // Act
    viewModel.load(BUSINESS_ID)
    advanceUntilIdle()

    // Assert
    assertIs<UiState.Success<List<Invoice>>>(viewModel.uiState.value)
    assertEquals(invoices, (viewModel.uiState.value as UiState.Success).data)
}
```

### 3.3 Testing Use Cases with Result<T>

```kotlin
@Test
fun `RecordPaymentUseCase returns failure when amount exceeds outstanding`() = runTest {
    val useCase = RecordPaymentUseCase(mockPaymentRepository)

    val result = useCase.invoke(
        invoiceId = 1L,
        businessId = 1L,
        amount = 200_00L,         // $200 in cents
        outstanding = 100_00L,    // only $100 outstanding
        paymentDate = System.currentTimeMillis(),
        invoiceDate = System.currentTimeMillis() - 86400_000L,
        notes = ""
    )

    assertTrue(result.isFailure)
    assertContains(result.exceptionOrNull()!!.message!!, "exceeds outstanding")
}
```

### 3.4 Testing Repository with MockK

```kotlin
class InvoiceRepositoryTest : BaseUnitTest() {

    private val invoiceDao = mockk<InvoiceDao>()
    private val businessProfileRepository = mockk<BusinessProfileRepository>()
    private val analyticsDao = mockk<AnalyticsDao>(relaxed = true)
    private val paymentDao = mockk<InvoicePaymentDao>(relaxed = true)
    private val snapshotSyncHelper = mockk<SnapshotSyncHelper>(relaxed = true)

    private lateinit var repository: InvoiceRepositoryImpl

    @Before
    fun setup() {
        repository = InvoiceRepositoryImpl(
            invoiceDao,
            businessProfileRepository,
            analyticsDao,
            paymentDao,
            snapshotSyncHelper
        )
    }

    @Test
    fun `saveInvoice returns success with new ID`() = runTest {
        // Arrange
        val invoice = TestDataFactory.createInvoice()
        coEvery { businessProfileRepository.getActiveBusinessId() } returns 1L
        coEvery { invoiceDao.insertInvoice(any()) } returns 42L

        // Act
        val result = repository.saveInvoice(invoice)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(42L, result.getOrNull())
    }
}
```

---

## 4. Integration Test Setup

### 4.1 Room Migration Tests

Migration tests use Room's `MigrationTestHelper` with an in-memory database.

```kotlin
@RunWith(AndroidJUnit4::class)
class Migration23To24Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migrate23To24() {
        // Create v23 database
        helper.createDatabase(TEST_DB, 23).apply {
            // Insert test data for v23 schema
            execSQL("INSERT INTO invoices (id, businessProfileId, totalAmount, status) VALUES (1, 1, 10000, 'DRAFT')")
            close()
        }

        // Run migration
        val db = helper.runMigrationsAndValidate(TEST_DB, 24, true, MIGRATION_23_24)

        // Verify new schema
        db.query("SELECT * FROM invoices WHERE id = 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            // Verify new columns added in v24
        }
    }
}
```

### 4.2 End-to-End Tests

E2E tests use Espresso with the full app stack.

```kotlin
@RunWith(AndroidJUnit4::class)
class CreateCustomerE2ETest : BaseE2ETest() {

    @Test
    fun createCustomer_happyPath() {
        // Navigate to create customer screen
        onView(withId(R.id.fab_add_customer)).perform(click())

        // Fill in form
        onView(withId(R.id.field_name)).perform(typeText("Acme Corp"))
        onView(withId(R.id.field_email)).perform(typeText("contact@acme.com"))

        // Submit
        onView(withId(R.id.btn_save)).perform(click())

        // Verify customer appears in list
        onView(withText("Acme Corp")).check(matches(isDisplayed()))
    }
}
```

---

## 5. Test Data Factories

### 5.1 `TestDataFactory` (located in `util/` and `domain/validation/`)

```kotlin
object TestDataFactory {

    fun createInvoice(
        id: Long = 1L,
        businessProfileId: Long = 1L,
        customerId: Long? = 1L,
        totalAmount: Long = 10000L,   // $100.00
        amountPaid: Long = 0L,
        status: InvoiceStatus = InvoiceStatus.DRAFT,
        invoiceNumber: String = "INV-2024-001"
    ) = Invoice(
        id = id,
        businessProfileId = businessProfileId,
        customerId = customerId,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        status = status,
        invoiceNumber = invoiceNumber,
        lineItems = listOf(createLineItem()),
        date = System.currentTimeMillis()
    )

    fun createLineItem(
        id: Long = 1L,
        invoiceId: Long = 1L,
        description: String = "Consulting Services",
        quantity: Double = 1.0,
        unitPrice: Long = 10000L   // $100.00
    ) = LineItem(
        id = id,
        invoiceId = invoiceId,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice
    )

    fun createCustomer(
        id: Long = 1L,
        businessProfileId: Long = 1L,
        name: String = "Test Customer",
        email: String = "test@example.com"
    ) = Customer(
        id = id,
        businessProfileId = businessProfileId,
        name = name,
        email = email,
        isActive = true
    )

    fun createBusinessProfile(
        id: Long = 1L,
        businessName: String = "Test Business",
        defaultTaxRate: Double = 0.10
    ) = BusinessProfile(
        id = id,
        businessName = businessName,
        defaultTaxRate = defaultTaxRate,
        isTaxRegistered = true
    )
}
```

### 5.2 `TestDataProvider` (domain/test/)

Provides pre-built test scenarios (e.g., partially-paid invoice, overdue invoice).

---

## 6. Testing Patterns

### 6.1 Testing Flows with Turbine

```kotlin
@Test
fun `customer list emits loading then success`() = runTest {
    val viewModel = CustomerListViewModelV2(mockRepository)

    viewModel.uiState.test {
        assertEquals(UiState.Loading, awaitItem())
        val success = awaitItem()
        assertIs<UiState.Success<*>>(success)
        cancelAndIgnoreRemainingEvents()
    }
}
```

### 6.2 Testing Coroutines

```kotlin
@Test
fun `payment submission updates loading state`() = runTest {
    val viewModel = RecordPaymentViewModel(mockUseCase)
    viewModel.initFor(invoiceId = 1L, businessId = 1L, invoiceTotal = 10000L, amountPaid = 0L, invoiceDate = 0L)
    viewModel.onAmountChanged("50.00")

    // Before submit
    assertFalse(viewModel.formState.value.isLoading)

    viewModel.submit()
    advanceUntilIdle()

    // After submit
    assertFalse(viewModel.formState.value.isLoading)
    // Verify use case was called
    coVerify { mockUseCase.invoke(any(), any(), 5000L, any(), any(), any(), any()) }
}
```

### 6.3 Testing `Result<T>` error paths

```kotlin
@Test
fun `recordPayment failure updates formState with error`() = runTest {
    coEvery { mockUseCase.invoke(any(), any(), any(), any(), any(), any(), any()) } returns
        Result.failure(IllegalArgumentException("Payment amount exceeds outstanding balance"))

    viewModel.submit()
    advanceUntilIdle()

    assertNotNull(viewModel.formState.value.submissionError)
    assertContains(viewModel.formState.value.submissionError!!, "exceeds outstanding")
}
```

---

## 7. Test Coverage Summary

### Unit Tests (47 files)

| Category | Files | Key Classes Tested |
|----------|-------|-------------------|
| Repositories | 8 | `InvoiceRepositoryImpl`, `OfflineQueueRepositoryImpl`, `RevenueRepositoryImpl` |
| Use Cases | 4 | `SaveInvoiceUseCase`, `SyncPendingOperationsUseCase`, `RecordPaymentUseCase` |
| ViewModels | 7 | `CreateInvoiceViewModel`, `RevenueDashboardViewModel`, `RecordPaymentViewModel` |
| Validation | 3 | `ValidationRules`, `InputValidator`, `PaymentValidation` |
| DAOs | 2 | `OfflineOperationDao` |
| Templates | 5 | `TemplateFormState`, `InvoiceTemplateRepository`, `CustomFieldValidation` |
| Navigation | 2 | `DualGUINavigation`, `NavigationTest` |
| Integration | 5 | `DashboardIntegration`, `EndToEndJourney`, `CrossGUISync` |

### Instrumented Tests (11 files)

| Category | Files |
|----------|-------|
| Migration Tests | 6 (v23→v24 through v28→v29) |
| E2E Tests | 3 (BaseE2ETest, CreateCustomer, CreateInvoice) |

### Known Coverage Gaps

- Payment recording ViewModel (`RecordPaymentViewModel`) — partial coverage
- GUI2 ViewModels (`DashboardViewModelV2`, analytics ViewModels) — not fully covered
- Migration tests for v29→v30, v30→v31, v31→v32 — not yet implemented
- PDF generation use case — no unit tests

---

## 8. Writing New Tests

### Checklist for New Tests

- [ ] Extend `BaseUnitTest` for proper coroutine dispatcher setup
- [ ] Use `mockk()` for dependencies (not `Mockito`)
- [ ] Use `mockk(relaxed = true)` for analytics/snapshot helpers to avoid boilerplate stubbing
- [ ] Use `runTest` for coroutine-based tests
- [ ] Use `advanceUntilIdle()` after triggering async operations
- [ ] Use `turbine` for testing `Flow` emissions
- [ ] Test both success and failure paths
- [ ] Use `TestDataFactory` for consistent test data
- [ ] Follow naming convention: `` `method under test_given state_expected behaviour`() ``

### Example Test File Template

```kotlin
package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BaseUnitTest
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class MyRepositoryTest : BaseUnitTest() {

    private val mockDao = mockk<MyDao>()
    private lateinit var repository: MyRepositoryImpl

    @Before
    fun setup() {
        repository = MyRepositoryImpl(mockDao)
    }

    @Test
    fun `method_givenValidInput_returnsSuccess`() = runTest {
        // Arrange
        // Act
        val result = repository.someMethod()
        // Assert
        assertTrue(result.isSuccess)
    }
}
```
