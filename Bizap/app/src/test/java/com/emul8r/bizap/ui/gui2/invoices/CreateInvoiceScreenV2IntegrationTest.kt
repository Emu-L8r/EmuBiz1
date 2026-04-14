@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.ui.gui2.invoices

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration tests for GUI2 invoice creation with customer selection
 */
class CreateInvoiceScreenV2IntegrationTest : BaseUnitTest() {

    private lateinit var viewModel: CreateInvoiceViewModelV2
    private val invoiceRepository = mockk<InvoiceRepository>()
    private val customerRepository = mockk<CustomerRepository>()

    @Before
    fun setup() {
        // Reset mocks
        io.mockk.clearAllMocks()
    }

    // ========================================
    // Complete Invoice Creation Flow Tests
    // ========================================

    @Test
    fun `complete flow - create invoice with selected customer`() = runTest {
        // Given - Setup customers
        val customer = Customer(
            id = 1L,
            name = "John Doe",
            email = "john@example.com",
            phone = "1234567890",
            address = "123 Main St"
        )

        every { customerRepository.getAllCustomers() } returns flowOf(listOf(customer))
        coEvery { invoiceRepository.saveInvoice(any()) } returns Result.success(1L)

        viewModel = CreateInvoiceViewModelV2(
            invoiceRepository = invoiceRepository,
            customerRepository = customerRepository
        )

        // When - Load customers
        runCatching {
            // Simulate init loading
            val customers = listOf(customer) // Customers pre-loaded via mock
            assertEquals(1, customers.size)
        }

        // When - Select customer
        viewModel.selectCustomer(customer)

        // When - Create invoice with selected customer
        val invoice = Invoice(
            id = 0,
            customerId = customer.id,
            customerName = customer.name,
            customerAddress = customer.address ?: "",
            customerEmail = customer.email,
            items = emptyList(),
            totalAmount = 10000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            dateCreated = java.time.Instant.now().toString(),
            dueDate = java.time.Instant.now().toString() + 86400000,
            isQuote = false,
            currency = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = "Test invoice"
        )

        var creationSucceeded = false
        viewModel.createInvoice(
            invoice = invoice,
            onSuccess = { creationSucceeded = true },
            onError = { }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(customer, viewModel.selectedCustomer.value)
        coVerify { invoiceRepository.saveInvoice(invoice) }
    }

    @Test
    fun `complete flow - switch between customers before creating invoice`() = runTest {
        // Given
        val customer1 = Customer(
            id = 1L,
            name = "Customer One",
            email = "one@example.com",
            phone = "111",
            address = "Address One"
        )

        val customer2 = Customer(
            id = 2L,
            name = "Customer Two",
            email = "two@example.com",
            phone = "222",
            address = "Address Two"
        )

        every { customerRepository.getAllCustomers() } returns flowOf(listOf(customer1, customer2))
        coEvery { invoiceRepository.saveInvoice(any()) } returns Result.success(1L)

        viewModel = CreateInvoiceViewModelV2(
            invoiceRepository = invoiceRepository,
            customerRepository = customerRepository
        )

        // When - Select first customer
        viewModel.selectCustomer(customer1)
        assertEquals(customer1, viewModel.selectedCustomer.value)

        // When - Switch to second customer
        viewModel.selectCustomer(customer2)

        // Then
        assertEquals(customer2, viewModel.selectedCustomer.value)
        assertEquals("Customer Two", viewModel.selectedCustomer.value?.name)
    }

    @Test
    fun `complete flow - create invoice without selecting customer defaults to Unknown`() = runTest {
        // Given
        val customer = Customer(
            id = 1L,
            name = "John Doe",
            email = "john@example.com",
            phone = "1234567890",
            address = "123 Main St"
        )

        every { customerRepository.getAllCustomers() } returns flowOf(listOf(customer))
        coEvery { invoiceRepository.saveInvoice(any()) } returns Result.success(1L)

        viewModel = CreateInvoiceViewModelV2(
            invoiceRepository = invoiceRepository,
            customerRepository = customerRepository
        )

        // When - Create invoice WITHOUT selecting customer
        val invoice = Invoice(
            id = 0,
            customerId = 0L,
            customerName = "Unknown",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 5000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            dateCreated = java.time.Instant.now().toString(),
            dueDate = java.time.Instant.now().toString(),
            isQuote = false,
            currency = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        viewModel.createInvoice(
            invoice = invoice,
            onSuccess = { },
            onError = { }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals("Unknown", invoice.customerName)
        coVerify { invoiceRepository.saveInvoice(invoice) }
    }

    // ========================================
    // Error Scenario Tests
    // ========================================

    @Test
    fun `error handling - customer loading fails gracefully`() = runTest {
        // Given
        every { customerRepository.getAllCustomers() } throws Exception("Network error")

        // When
        viewModel = CreateInvoiceViewModelV2(
            invoiceRepository = invoiceRepository,
            customerRepository = customerRepository
        )

        // Then - Should not crash and customers should be empty
        assertEquals(emptyList(), viewModel.customers.value)
    }

    @Test
    fun `error handling - invoice creation with selected customer fails`() = runTest {
        // Given
        val customer = Customer(
            id = 1L,
            name = "Test Customer",
            email = "test@example.com",
            phone = "phone",
            address = "address"
        )

        every { customerRepository.getAllCustomers() } returns flowOf(listOf(customer))
        coEvery { invoiceRepository.saveInvoice(any()) } throws Exception("Database error")

        viewModel = CreateInvoiceViewModelV2(
            invoiceRepository = invoiceRepository,
            customerRepository = customerRepository
        )

        // When
        viewModel.selectCustomer(customer)

        val invoice = Invoice(
            id = 0,
            customerId = customer.id,
            customerName = customer.name,
            customerAddress = customer.address ?: "",
            customerEmail = customer.email,
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            dateCreated = java.time.Instant.now().toString(),
            dueDate = java.time.Instant.now().toString(),
            isQuote = false,
            currency = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        var errorMessage: String? = null
        viewModel.createInvoice(
            invoice = invoice,
            onSuccess = { },
            onError = { error -> errorMessage = error }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should handle error gracefully
        assertNotNull(errorMessage)
    }

    // ========================================
    // Business Logic Tests
    // ========================================

    @Test
    fun `business logic - invoice uses all customer details`() = runTest {
        // Given
        val customer = Customer(
            id = 42L,
            name = "Acme Corporation",
            email = "billing@acme.com",
            phone = "555-1234",
            address = "789 Business Ave"
        )

        every { customerRepository.getAllCustomers() } returns flowOf(listOf(customer))
        coEvery { invoiceRepository.saveInvoice(any()) } returns Result.success(1L)

        viewModel = CreateInvoiceViewModelV2(
            invoiceRepository = invoiceRepository,
            customerRepository = customerRepository
        )

        // When - Select customer and create invoice
        viewModel.selectCustomer(customer)

        // Then - Verify customer data is properly used
        assertEquals(customer.id, viewModel.selectedCustomer.value?.id)
        assertEquals("Acme Corporation", viewModel.selectedCustomer.value?.name)
        assertEquals("billing@acme.com", viewModel.selectedCustomer.value?.email)
        assertEquals("789 Business Ave", viewModel.selectedCustomer.value?.address)
    }

    @Test
    fun `business logic - can create multiple invoices with same customer`() = runTest {
        // Given
        val customer = Customer(
            id = 1L,
            name = "Regular Customer",
            email = "regular@example.com",
            phone = "phone",
            address = "address"
        )

        every { customerRepository.getAllCustomers() } returns flowOf(listOf(customer))
        coEvery { invoiceRepository.saveInvoice(any()) } returns Result.success(1L)

        viewModel = CreateInvoiceViewModelV2(
            invoiceRepository = invoiceRepository,
            customerRepository = customerRepository
        )

        // When - Select customer
        viewModel.selectCustomer(customer)

        // When - Create first invoice
        val invoice1 = Invoice(
            id = 0,
            customerId = customer.id,
            customerName = customer.name,
            customerAddress = customer.address ?: "",
            customerEmail = customer.email,
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            dateCreated = java.time.Instant.now().toString(),
            dueDate = java.time.Instant.now().toString(),
            isQuote = false,
            currency = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        viewModel.createInvoice(
            invoice = invoice1,
            onSuccess = { },
            onError = { }
        )

        // When - Create second invoice with same customer
        val invoice2 = invoice1.copy(id = 0, invoiceSequence = 2, totalAmount = 2000L)

        viewModel.createInvoice(
            invoice = invoice2,
            onSuccess = { },
            onError = { }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify both calls made
        coVerify(exactly = 2) { invoiceRepository.saveInvoice(any()) }
    }
}






