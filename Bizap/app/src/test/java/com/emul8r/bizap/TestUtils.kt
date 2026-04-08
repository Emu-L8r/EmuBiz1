package com.emul8r.bizap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.*
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Test Utilities for Bizap v1.1
 * 
 * This file provides common test utilities, helpers, and extensions
 * to simplify unit testing across the codebase.
 * 
 * Created: March 22, 2026
 * Phase: 1 (Foundation + Baseline)
 */

// ============================================================================
// COROUTINES TEST UTILITIES
// ============================================================================

/**
 * Main Dispatcher Rule for Coroutines Testing
 * 
 * Replaces the main dispatcher with a test dispatcher for unit tests.
 * This ensures coroutines launched on the main thread run synchronously in tests.
 * 
 * Usage:
 * ```kotlin
 * @get:Rule
 * val mainDispatcherRule = MainDispatcherRule()
 * 
 * @Test
 * fun `test suspending function`() = runTest {
 *     // Your test code
 * }
 * ```
 */
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * LiveData Rule
 * 
 * Executes LiveData operations synchronously in tests.
 * Required for testing ViewModels that use LiveData.
 * 
 * Usage:
 * ```kotlin
 * @get:Rule
 * val instantExecutorRule = InstantTaskExecutorRule()
 * ```
 */
// Note: InstantTaskExecutorRule is already provided by androidx.arch.core:core-testing
// This is just a documentation reference. Use it directly in tests.

// ============================================================================
// FLOW TEST UTILITIES
// ============================================================================

/**
 * Collect Flow values into a list for testing
 * 
 * Usage:
 * ```kotlin
 * @Test
 * fun `test flow emissions`() = runTest {
 *     val values = flow.toList()
 *     assertEquals(expected, values)
 * }
 * ```
 */
suspend fun <T> Flow<T>.toList(): List<T> {
    val list = mutableListOf<T>()
    collect { value ->
        list.add(value)
    }
    return list
}

/**
 * Collect first N values from a Flow
 * 
 * Usage:
 * ```kotlin
 * val firstThree = flow.takeValues(3)
 * ```
 */
suspend fun <T> Flow<T>.takeValues(count: Int): List<T> {
    val list = mutableListOf<T>()
    var collected = 0
    collect { value ->
        if (collected < count) {
            list.add(value)
            collected++
        }
        if (collected >= count) {
            return@collect
        }
    }
    return list
}

// ============================================================================
// STATEFLOW STUBS & MOCKS
// ============================================================================

/**
 * Create a test StateFlow with an initial value
 * 
 * Usage:
 * ```kotlin
 * val testFlow = testStateFlow(initialValue = "Test")
 * ```
 */
fun <T> testStateFlow(initialValue: T): MutableStateFlow<T> {
    return MutableStateFlow(initialValue)
}

/**
 * Create multiple StateFlow stubs for testing
 * 
 * Usage:
 * ```kotlin
 * val (flow1, flow2, flow3) = createStateFlows(
 *     "initial1",
 *     "initial2", 
 *     "initial3"
 * )
 * ```
 */
fun <T> createStateFlows(vararg initialValues: T): List<MutableStateFlow<T>> {
    return initialValues.map { MutableStateFlow(it) }
}

// ============================================================================
// COMMON TEST DATA BUILDERS
// ============================================================================

/**
 * Test Data Builders
 * 
 * These provide common test data objects to avoid repetition across tests.
 * Add more builders as needed for different domain models.
 */

object TestDataBuilders {
    
    /**
     * Create a test customer with default values
     */
    fun createTestCustomer(
        id: Long = 1L,
        name: String = "Test Customer",
        email: String = "test@example.com",
        phone: String = "123-456-7890"
    ) = com.emul8r.bizap.domain.model.Customer(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = "123 Test St",
        city = "Test City",
        state = "TS",
        zipCode = "12345",
        country = "Test Country",
        businessId = 1L,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    
    /**
     * Create a test invoice with default values
     */
    fun createTestInvoice(
        id: Long = 1L,
        invoiceNumber: String = "INV-001",
        customerId: Long = 1L,
        total: Double = 100.0
    ) = com.emul8r.bizap.domain.model.Invoice(
        id = id,
        invoiceNumber = invoiceNumber,
        customerId = customerId,
        businessId = 1L,
        issueDate = System.currentTimeMillis(),
        dueDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000, // 30 days
        subtotal = total,
        taxRate = 0.0,
        taxAmount = 0.0,
        total = total,
        amountPaid = 0.0,
        status = "DRAFT",
        notes = "Test notes",
        currency = "USD",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        items = emptyList(),
        payments = emptyList(),
        photoUris = emptyList()
    )
    
    /**
     * Create a test line item with default values
     */
    fun createTestLineItem(
        id: Long = 1L,
        invoiceId: Long = 1L,
        description: String = "Test Item",
        quantity: Double = 1.0,
        price: Double = 100.0
    ) = com.emul8r.bizap.domain.model.InvoiceItem(
        id = id,
        invoiceId = invoiceId,
        description = description,
        quantity = quantity,
        price = price,
        amount = quantity * price
    )
    
    /**
     * Create a test business profile with default values
     */
    fun createTestBusinessProfile(
        id: Long = 1L,
        businessName: String = "Test Business"
    ) = com.emul8r.bizap.domain.model.BusinessProfile(
        id = id,
        businessName = businessName,
        ownerName = "Test Owner",
        email = "business@example.com",
        phone = "123-456-7890",
        address = "123 Business St",
        city = "Test City",
        state = "TS",
        zipCode = "12345",
        country = "Test Country",
        currency = "USD",
        taxId = "12-3456789",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

// ============================================================================
// MOCK SETUP HELPERS
// ============================================================================

/**
 * Mock Setup Helpers
 * 
 * Common patterns for setting up mocks with MockK.
 * These reduce boilerplate in test files.
 */

object MockHelpers {
    
    /**
     * Create a relaxed mock that returns default values for all methods
     * 
     * Usage:
     * ```kotlin
     * val mockRepository = relaxedMock<CustomerRepository>()
     * ```
     */
    inline fun <reified T : Any> relaxedMock(): T {
        return io.mockk.mockk(relaxed = true)
    }
    
    /**
     * Create a spy that delegates to real implementation but can be verified
     * 
     * Usage:
     * ```kotlin
     * val spy = spy(realObject)
     * ```
     */
    inline fun <reified T : Any> spy(obj: T): T {
        return io.mockk.spyk(obj)
    }
}

// ============================================================================
// ASSERTION HELPERS
// ============================================================================

/**
 * Assertion Helpers
 * 
 * Custom assertions for common patterns in Bizap tests.
 */

object AssertionHelpers {
    
    /**
     * Assert that a StateFlow emits a specific value
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun <T> assertStateFlowEmits(
        stateFlow: StateFlow<T>,
        expectedValue: T,
        timeout: Long = 1000L
    ) {
        kotlinx.coroutines.withTimeout(timeout) {
            while (stateFlow.value != expectedValue) {
                kotlinx.coroutines.delay(10)
            }
        }
        assert(stateFlow.value == expectedValue) {
            "Expected StateFlow to emit $expectedValue, but was ${stateFlow.value}"
        }
    }
    
    /**
     * Assert that a value is within a range
     */
    fun <T : Comparable<T>> assertInRange(
        actual: T,
        min: T,
        max: T,
        message: String? = null
    ) {
        val baseMessage = message ?: "Expected $actual to be between $min and $max"
        assert(actual >= min && actual <= max) { baseMessage }
    }
    
    /**
     * Assert that a collection contains all expected items
     */
    fun <T> assertContainsAll(
        actual: Collection<T>,
        expected: Collection<T>,
        message: String? = null
    ) {
        val baseMessage = message ?: "Expected collection to contain all items"
        assert(actual.containsAll(expected)) {
            "$baseMessage. Missing: ${expected.filterNot { it in actual }}"
        }
    }
}

// ============================================================================
// TEST EXTENSIONS
// ============================================================================

/**
 * Extension function to run a test with a custom timeout
 * 
 * Usage:
 * ```kotlin
 * @Test
 * fun `long running test`() = runTestWithTimeout(5000) {
 *     // Test code
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun runTestWithTimeout(
    timeout: Long = 2000L,
    testBody: suspend TestScope.() -> Unit
) {
    runTest(timeout = timeout.milliseconds, testBody = testBody)
}

/**
 * Extension to convert milliseconds to Duration
 */
@OptIn(ExperimentalCoroutinesApi::class)
private val Long.milliseconds: kotlinx.coroutines.test.TestDispatcher.Companion.timeout
    get() = kotlinx.coroutines.test.TestDispatcher.Companion.timeout

// ============================================================================
// LOGGING HELPERS FOR TESTS
// ============================================================================

/**
 * Simple test logger to debug test execution
 * 
 * Usage:
 * ```kotlin
 * testLog("Test started")
 * ```
 */
fun testLog(message: String) {
    println("[TEST] $message")
}

/**
 * Log test section for better readability
 * 
 * Usage:
 * ```kotlin
 * testSection("Given") {
 *     // Setup code
 * }
 * ```
 */
inline fun <T> testSection(name: String, block: () -> T): T {
    println("[TEST] === $name ===")
    return block()
}

// ============================================================================
// COMMON TEST SCENARIOS
// ============================================================================

/**
 * Common test scenario templates
 */
object TestScenarios {
    
    /**
     * Standard ViewModel test structure
     * 
     * Usage:
     * ```kotlin
     * @Test
     * fun `test ViewModel`() = viewModelTest {
     *     // Given
     *     val viewModel = MyViewModel()
     *     
     *     // When
     *     viewModel.doSomething()
     *     
     *     // Then
     *     assertEquals(expected, viewModel.state.value)
     * }
     * ```
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun viewModelTest(
        testBody: suspend TestScope.() -> Unit
    ) {
        runTest(testBody = testBody)
    }
    
    /**
     * Repository test structure with database
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun repositoryTest(
        testBody: suspend TestScope.() -> Unit
    ) {
        runTest(testBody = testBody)
    }
}

// ============================================================================
// END OF TEST UTILS
// ============================================================================
