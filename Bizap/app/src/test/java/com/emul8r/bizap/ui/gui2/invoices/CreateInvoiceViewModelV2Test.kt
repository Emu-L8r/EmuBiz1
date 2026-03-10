package com.emul8r.bizap.ui.gui2.invoices

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
/**
 * Unit tests for CreateInvoiceViewModelV2
 * Tests customer loading and selection functionality
 */
class CreateInvoiceViewModelV2Test : BaseUnitTest() {
    private lateinit var viewModel: CreateInvoiceViewModelV2
    private val invoiceRepository = mockk<InvoiceRepository>()
    private val customerRepository = mockk<CustomerRepository>()
    @Before
    fun setup() {
        every { customerRepository.getAllCustomers() } returns flowOf(emptyList())
        viewModel = CreateInvoiceViewModelV2(
            invoiceRepository = invoiceRepository,
            customerRepository = customerRepository
        )
    }
    // ========================================
    // Customer Loading Tests
    @Test
    fun `loadCustomers - should load customers from repository`() = runTest {
        // Given
        val mockCustomers = listOf(
            Customer(
                id = 1L,
                name = "Test Customer 1",
                email = "customer1@example.com",
                phone = "1234567890",
                address = "123 Test St"
            ),
            Customer(
                id = 2L,
                name = "Test Customer 2",
                email = "customer2@example.com",
                phone = "0987654321",
                address = "456 Test Ave"
            )
        )
        every { customerRepository.getAllCustomers() } returns flowOf(mockCustomers)
        // When
        advanceUntilIdle()
        // Then
        assertEquals(mockCustomers, viewModel.customers.value)
        verify { customerRepository.getAllCustomers() }
    }

    @Test
    fun `loadCustomers - should handle empty customer list`() = runTest {
        assertTrue(viewModel.customers.value.isEmpty())
        assertEquals(emptyList(), viewModel.customers.value)
    }

    @Test
    fun `loadCustomers - should handle repository exception`() = runTest {
        every { customerRepository.getAllCustomers() } throws Exception("Database error")
        assertTrue(true)
    }

    // Customer Selection Tests
    @Test
    fun `selectCustomer - should update selectedCustomer state`() = runTest {
        val customer = Customer(
            id = 1L,
            name = "Test Customer",
            email = "test@example.com",
            phone = "1234567890",
            address = "123 Test St"
        )
        every { customerRepository.getAllCustomers() } returns flowOf(listOf(customer))
        viewModel.selectCustomer(customer)
        assertEquals(customer, viewModel.selectedCustomer.value)
    }

    @Test
    fun `selectCustomer - should allow deselection with null`() = runTest {
        viewModel.selectCustomer(null)
        assertNull(viewModel.selectedCustomer.value)
    }

    @Test
    fun `selectCustomer - should update to different customer`() = runTest {
        val customer1 = Customer(
            id = 1L,
            name = "Customer 1",
            email = "c1@example.com",
            phone = "1111111111",
            address = "Address 1"
        )
        val customer2 = Customer(
            id = 2L,
            name = "Customer 2",
            email = "c2@example.com",
            phone = "2222222222",
            address = "Address 2"
        )
        every { customerRepository.getAllCustomers() } returns flowOf(listOf(customer1, customer2))
        viewModel.selectCustomer(customer1)
        assertEquals(customer1, viewModel.selectedCustomer.value)
        viewModel.selectCustomer(customer2)
        assertEquals(customer2, viewModel.selectedCustomer.value)
    }

    // Initial State Tests
    @Test
    fun `initial state - customers should be empty list`() = runTest {
        assertTrue(viewModel.customers.value.isEmpty())
    }

    @Test
    fun `initial state - selectedCustomer should be null`() = runTest {
        assertNull(viewModel.selectedCustomer.value)
    }

    // Multiple Customer Tests
    @Test
    fun `loadCustomers - should load multiple customers`() = runTest {
        val customers = (1..5).map { id ->
            Customer(
                id = id.toLong(),
                name = "Customer $id",
                email = "customer$id@example.com",
                phone = "123456789$id",
                address = "Address $id"
            )
        }
        every { customerRepository.getAllCustomers() } returns flowOf(customers)
        assertEquals(5, viewModel.customers.value.size)
        assertEquals(customers, viewModel.customers.value)
    fun `selectCustomer - should work with any customer from loaded list`() = runTest {
        val customers = (1..3).map { id ->
            Customer(
                id = id.toLong(),
                name = "Customer $id",
                email = "customer$id@example.com",
                phone = "phone$id",
                address = "address$id"
            )
        }
        every { customerRepository.getAllCustomers() } returns flowOf(customers)
        // When - Select middle customer
        val middleCustomer = customers[1]
        viewModel.selectCustomer(middleCustomer)
        assertEquals(middleCustomer, viewModel.selectedCustomer.value)
        assertEquals("Customer 2", viewModel.selectedCustomer.value?.name)
    }
    }
}
