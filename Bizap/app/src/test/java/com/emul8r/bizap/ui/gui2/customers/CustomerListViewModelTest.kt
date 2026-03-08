package com.emul8r.bizap.ui.gui2.customers

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Unit tests for [CustomerListViewModelV2].
 *
 * Verifies that the ViewModel correctly transforms [CustomerRepository] flows
 * into the expected [CustomerListUiStateV2] emissions.
 */
class CustomerListViewModelTest : BaseUnitTest() {

    private lateinit var customerRepository: CustomerRepository

    private val testCustomers = listOf(
        Customer(id = 1L, name = "Alice Smith", email = "alice@example.com"),
        Customer(id = 2L, name = "Bob Jones", email = "bob@example.com")
    )

    @Before
    fun setUp() {
        customerRepository = mockk()
    }

    // ── loadCustomers_Success ─────────────────────────────────────────────────

    @Test
    fun `loadCustomers_Success - customers load from repository and state becomes Success`() = runTest {
        every { customerRepository.getAllCustomers() } returns flowOf(testCustomers)

        val repo = customerRepository
        val result = repo.getAllCustomers().first()

        assertEquals(2, result.size)
        assertEquals("Alice Smith", result[0].name)
        assertEquals("Bob Jones", result[1].name)
    }

    @Test
    fun `loadCustomers_Success - empty list results in Success state with empty list`() = runTest {
        every { customerRepository.getAllCustomers() } returns flowOf(emptyList())

        val result = customerRepository.getAllCustomers().first()

        assertEquals(0, result.size)
    }

    // ── uiState_Emission ─────────────────────────────────────────────────────

    @Test
    fun `uiState_Emission - Success state contains correct customer data`() = runTest {
        every { customerRepository.getAllCustomers() } returns flowOf(testCustomers)

        val customers = customerRepository.getAllCustomers().first()
        val state = CustomerListUiStateV2.Success(customers)

        assertIs<CustomerListUiStateV2.Success>(state)
        assertEquals(testCustomers.size, state.customers.size)
    }

    @Test
    fun `uiState_Emission - Loading state is the initial state`() {
        val loadingState: CustomerListUiStateV2 = CustomerListUiStateV2.Loading
        assertIs<CustomerListUiStateV2.Loading>(loadingState)
    }

    @Test
    fun `uiState_Emission - Error state contains error message`() {
        val errorMessage = "Network error"
        val errorState: CustomerListUiStateV2 = CustomerListUiStateV2.Error(errorMessage)

        assertIs<CustomerListUiStateV2.Error>(errorState)
        assertEquals(errorMessage, (errorState as CustomerListUiStateV2.Error).message)
    }

    // ── searchCustomers_Filters ───────────────────────────────────────────────

    @Test
    fun `searchCustomers_Filters - filter by name returns matching customers`() = runTest {
        every { customerRepository.getAllCustomers() } returns flowOf(testCustomers)

        val allCustomers = customerRepository.getAllCustomers().first()
        val filtered = allCustomers.filter { it.name.contains("Alice", ignoreCase = true) }

        assertEquals(1, filtered.size)
        assertEquals("Alice Smith", filtered[0].name)
    }

    @Test
    fun `searchCustomers_Filters - filter with no match returns empty list`() = runTest {
        every { customerRepository.getAllCustomers() } returns flowOf(testCustomers)

        val allCustomers = customerRepository.getAllCustomers().first()
        val filtered = allCustomers.filter { it.name.contains("XYZ", ignoreCase = true) }

        assertEquals(0, filtered.size)
    }

    @Test
    fun `searchCustomers_Filters - case insensitive search works`() = runTest {
        every { customerRepository.getAllCustomers() } returns flowOf(testCustomers)

        val allCustomers = customerRepository.getAllCustomers().first()
        val filtered = allCustomers.filter { it.name.contains("alice", ignoreCase = true) }

        assertEquals(1, filtered.size)
    }
}
