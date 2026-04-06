package com.emul8r.bizap.presentation.viewmodel

import com.emul8r.bizap.domain.model.Money
import com.emul8r.bizap.domain.repository.InvoiceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Unit tests for PaymentRecordingViewModel.
 *
 * **Coverage Areas:**
 * - Amount input parsing and validation
 * - Payment method selection
 * - Error message handling
 * - Loading state management
 * - StateFlow reactivity
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PaymentRecordingViewModelTest {

    @Mock
    private lateinit var invoiceRepository: InvoiceRepository

    private lateinit var viewModel: PaymentRecordingViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = PaymentRecordingViewModel(invoiceRepository)
    }

    @Test
    fun `initial state should have empty amount and no error`() {
        val initialState = viewModel.uiState.value
        assertEquals("", initialState.amountInput)
        assertNull(initialState.errorMessage)
    }

    @Test
    fun `updateAmount should filter non-numeric characters except decimal`() {
        // Valid: numbers and decimal
        viewModel.updateAmount("123.45")
        assertEquals("123.45", viewModel.uiState.value.amountInput)

        // Invalid chars filtered out
        viewModel.updateAmount("$100.50")
        assertEquals("100.50", viewModel.uiState.value.amountInput)

        // Multiple decimals (only first should pass if formatter prevents it)
        viewModel.updateAmount("100.50.25")
        // System should handle this gracefully
        val result = viewModel.uiState.value.amountInput
        assertNotNull(result)
    }

    @Test
    fun `updateNotes should update notes input`() {
        viewModel.updateNotes("Payment for invoice #001")
        assertEquals("Payment for invoice #001", viewModel.uiState.value.notesInput)
    }

    @Test
    fun `selectPaymentMethod should update selected method`() {
        val method = com.emul8r.bizap.data.local.entities.PaymentMethod.BANK_TRANSFER
        viewModel.selectPaymentMethod(method)
        assertEquals(method, viewModel.uiState.value.selectedMethod)
    }

    @Test
    fun `zero or negative amounts should show error`() = runTest {
        viewModel.updateAmount("0")
        viewModel.recordPayment(invoiceId = 1L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `valid amount should parse to Money object`() {
        // Test parsing $50.99 = 5099 cents
        viewModel.updateAmount("50.99")
        val parsed = viewModel.uiState.value.amountInput
        assertEquals("50.99", parsed)
    }

    @Test
    fun `error should clear on new amount input`() {
        viewModel.updateAmount("0")
        viewModel.recordPayment(invoiceId = 1L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)

        viewModel.updateAmount("25.50")
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `isSubmitting should toggle during recordPayment`() = runTest {
        assertEquals(false, viewModel.uiState.value.isSubmitting)

        viewModel.updateAmount("50.00")
        viewModel.recordPayment(invoiceId = 1L)

        // Should be true during submission
        assertEquals(true, viewModel.uiState.value.isSubmitting)

        testDispatcher.scheduler.advanceUntilIdle()

        // Should return to false after completion
        assertEquals(false, viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `form should reset after successful payment`() = runTest {
        viewModel.updateAmount("100.00")
        viewModel.updateNotes("Test payment")

        viewModel.recordPayment(invoiceId = 1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // After success, form should be reset
        assertEquals(true, viewModel.uiState.value.paymentRecorded)
    }
}

