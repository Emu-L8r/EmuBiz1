package com.emul8r.bizap.ui.gui2.invoices

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.data.local.entities.PaymentMethod
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.RecordPaymentUseCase
import com.emul8r.bizap.presentation.viewmodel.PaymentRecordingViewModel
import io.mockk.mockk
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for [PaymentRecordingViewModel].
 * Verifies form state management and input validation logic.
 */
class RecordPaymentViewModelTest : BaseUnitTest() {

    private val invoiceRepository: InvoiceRepository = mockk(relaxed = true)
    private val recordPaymentUseCase: RecordPaymentUseCase = mockk(relaxed = true)

    private lateinit var viewModel: PaymentRecordingViewModel

    @Before
    fun setUp() {
        viewModel = PaymentRecordingViewModel(invoiceRepository, recordPaymentUseCase)
    }

    // ── initial state ──────────────────────────────────────────────────────

    @Test
    fun `initial state has empty amount input`() {
        assertEquals("", viewModel.uiState.value.amountInput)
    }

    @Test
    fun `initial state is not submitting`() {
        assertFalse(viewModel.uiState.value.isSubmitting)
    }

    @Test
    fun `initial state has no error message`() {
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `initial state has no payment recorded`() {
        assertFalse(viewModel.uiState.value.paymentRecorded)
    }

    // ── updateAmount ───────────────────────────────────────────────────────

    @Test
    fun `updateAmount sets amount in state`() {
        viewModel.updateAmount("100.50")
        assertEquals("100.50", viewModel.uiState.value.amountInput)
    }

    @Test
    fun `updateAmount strips non-numeric non-decimal characters`() {
        viewModel.updateAmount("$1,000.00abc")
        assertEquals("1000.00", viewModel.uiState.value.amountInput)
    }

    @Test
    fun `updateAmount clears error message`() {
        viewModel.recordPayment(1L) // trigger validation error (blank amount)
        viewModel.updateAmount("50.00")
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `updateAmount allows decimal point`() {
        viewModel.updateAmount("99.99")
        assertEquals("99.99", viewModel.uiState.value.amountInput)
    }

    @Test
    fun `updateAmount with empty string results in empty state`() {
        viewModel.updateAmount("")
        assertEquals("", viewModel.uiState.value.amountInput)
    }

    // ── updatePaymentMethod ────────────────────────────────────────────────

    @Test
    fun `updatePaymentMethod sets selected method`() {
        viewModel.updatePaymentMethod(PaymentMethod.ACH_TRANSFER)
        assertEquals(PaymentMethod.ACH_TRANSFER, viewModel.uiState.value.selectedMethod)
    }

    @Test
    fun `updatePaymentMethod to CASH updates state`() {
        viewModel.updatePaymentMethod(PaymentMethod.CASH)
        assertEquals(PaymentMethod.CASH, viewModel.uiState.value.selectedMethod)
    }

    // ── updateNotes ────────────────────────────────────────────────────────

    @Test
    fun `updateNotes sets notes in state`() {
        viewModel.updateNotes("Partial payment received via bank")
        assertEquals("Partial payment received via bank", viewModel.uiState.value.notesInput)
    }

    @Test
    fun `updateNotes with empty string clears notes`() {
        viewModel.updateNotes("some note")
        viewModel.updateNotes("")
        assertEquals("", viewModel.uiState.value.notesInput)
    }

    // ── recordPayment validation ───────────────────────────────────────────

    @Test
    fun `recordPayment with blank amount sets error message`() = runUnitTest {
        viewModel.updateAmount("")
        viewModel.recordPayment(42L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }

    @Test
    fun `recordPayment with zero amount sets error message`() = runUnitTest {
        viewModel.updateAmount("0")
        viewModel.recordPayment(42L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }

    @Test
    fun `recordPayment with negative amount sets error message`() = runUnitTest {
        viewModel.updateAmount("-10.00")
        viewModel.recordPayment(42L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }

    @Test
    fun `recordPayment with non-numeric amount sets error message`() = runUnitTest {
        // After stripping non-numeric, this becomes empty
        viewModel.updateAmount("abc")
        viewModel.recordPayment(42L)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage != null)
    }
}


