package com.emul8r.bizap.ui.invoices

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.GenerateAndSaveInvoiceUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for CreateInvoiceViewModel.
 * Tests critical business logic for invoice creation.
 */
class CreateInvoiceViewModelTest : BaseUnitTest() {

    @get:Rule
    val instantTaskExecutor = InstantTaskExecutorRule()

    private lateinit var viewModel: CreateInvoiceViewModel
    private lateinit var invoiceRepository: InvoiceRepository
    private lateinit var customerRepository: CustomerRepository
    private lateinit var businessProfileRepository: BusinessProfileRepository
    private lateinit var generateAndSaveInvoiceUseCase: GenerateAndSaveInvoiceUseCase

    @Before
    fun setup() {
        super.setupBase()

        // Mock repositories
        invoiceRepository = mockk()
        customerRepository = mockk()
        businessProfileRepository = mockk()
        generateAndSaveInvoiceUseCase = mockk()

        // Setup default mock responses
        every { customerRepository.getAllCustomers() } returns flowOf(
            listOf(
                Customer(id = 1, name = "John Doe", email = "john@example.com"),
                Customer(id = 2, name = "Jane Smith", email = "jane@example.com")
            )
        )

        viewModel = CreateInvoiceViewModel(
            invoiceRepository,
            customerRepository,
            businessProfileRepository,
            mockk(), // CurrencyRepository
            generateAndSaveInvoiceUseCase
        )
    }

    @Test
    fun addLineItem_shouldIncreaseItemsListSize() {
        val initialSize = viewModel.uiState.value.items.size

        viewModel.addLineItem()

        val newSize = viewModel.uiState.value.items.size
        assertEquals(initialSize + 1, newSize)
    }

    @Test
    fun addLineItem_multipleTimesShouldIncreaseSizeEachTime() {
        val initialSize = viewModel.uiState.value.items.size

        viewModel.addLineItem()
        viewModel.addLineItem()
        viewModel.addLineItem()

        val finalSize = viewModel.uiState.value.items.size
        assertEquals(initialSize + 3, finalSize)
    }

    @Test
    fun removeLineItem_shouldDecreaseItemsListSize() {
        // First add an item with a known transientId
        viewModel.addLineItem()
        val itemToRemove = viewModel.uiState.value.items.lastOrNull()
        val sizeBeforeRemove = viewModel.uiState.value.items.size

        // Remove the item
        viewModel.removeLineItem(itemToRemove!!.transientId)

        val sizeAfterRemove = viewModel.uiState.value.items.size
        assertEquals(sizeBeforeRemove - 1, sizeAfterRemove)
    }

    @Test
    fun selectCustomer_shouldUpdateSelectedCustomerInState() {
        val customer = Customer(id = 1, name = "John Doe", email = "john@example.com")

        viewModel.selectCustomer(customer)

        val selectedCustomer = viewModel.uiState.value.selectedCustomer
        assertEquals(customer, selectedCustomer)
    }

    @Test
    fun selectCustomer_shouldReplaceWithNewCustomer() {
        val customer1 = Customer(id = 1, name = "John Doe", email = "john@example.com")
        val customer2 = Customer(id = 2, name = "Jane Smith", email = "jane@example.com")

        viewModel.selectCustomer(customer1)
        assertEquals(customer1, viewModel.uiState.value.selectedCustomer)

        viewModel.selectCustomer(customer2)
        assertEquals(customer2, viewModel.uiState.value.selectedCustomer)
    }

    @Test
    fun updateLineItem_shouldModifyExistingItem() {
        viewModel.addLineItem()
        val transientId = viewModel.uiState.value.items.firstOrNull()!!.transientId

        viewModel.updateLineItem(
            transientId = transientId,
            description = "Updated Item",
            quantity = 5.0,
            unitPrice = 10000L // $100.00
        )

        val updatedItem = viewModel.uiState.value.items.find { it.transientId == transientId }
        assertEquals(updatedItem?.description, "Updated Item")
        assertEquals(5.0, updatedItem?.quantity ?: 0.0, 0.01)
        assertEquals(10000L, updatedItem?.unitPrice ?: 0L)
    }

    @Test
    fun onHeaderChange_shouldUpdateHeaderInState() {
        val header = "Invoice #2026-001"

        viewModel.onHeaderChange(header)

        assertEquals(header, viewModel.uiState.value.header)
    }

    @Test
    fun onSubheaderChange_shouldUpdateSubheaderInState() {
        val subheader = "Service Invoice"

        viewModel.onSubheaderChange(subheader)

        assertEquals(subheader, viewModel.uiState.value.subheader)
    }

    @Test
    fun onNotesChange_shouldUpdateNotesInState() {
        val notes = "Please pay by end of month"

        viewModel.onNotesChange(notes)

        assertEquals(notes, viewModel.uiState.value.notes)
    }

    @Test
    fun isSaving_shouldBeInitiallyFalse() {
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun error_shouldBeNullInitially() {
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun itemsListShouldStartWithOneEmptyLineItem() {
        val items = viewModel.uiState.value.items
        assertEquals(1, items.size)
        assertTrue(items[0].description.isEmpty())
        assertEquals(0L, items[0].unitPrice)
    }

    @Test
    fun currencyCodeShouldDefaultToAUD() {
        assertEquals("AUD", viewModel.uiState.value.selectedCurrencyCode)
    }

    @Test
    fun removeLineItem_withUnknownId_shouldNotCrash() {
        val initialSize = viewModel.uiState.value.items.size

        // Remove with a UUID that doesn't match any item — should not crash or change size
        viewModel.removeLineItem(java.util.UUID.randomUUID())

        val finalSize = viewModel.uiState.value.items.size
        assertEquals(initialSize, finalSize)
    }
}

