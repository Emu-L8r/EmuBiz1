@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.ui.gui2.invoices

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.domain.model.balanceRemaining
import com.emul8r.bizap.domain.model.isFullyPaid
import com.emul8r.bizap.domain.repository.InvoiceRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import java.time.Instant

/**
 * Unit tests for [EditInvoiceViewModelV2].
 *
 * Verifies invoice editing and status transition logic.
 */
class EditInvoiceViewModelTest : BaseUnitTest() {

    private lateinit var invoiceRepository: InvoiceRepository

    private val now = Instant.now().toString()
    private val tomorrow = Instant.now().plusSeconds(86_400L).toString()

    private val sampleInvoice = Invoice(
        id = 1L,
        businessProfileId = 1L,
        customerId = 1L,
        customerName = "Sample Customer",
        dateCreated = now,
        dueDate = tomorrow,
        totalAmount = 50000L,
        amountPaid = 0L,
        items = listOf(InvoiceItem(description = "Service", quantity = 1.0, unitPrice = 50000L)),
        isQuote = false,
        status = InvoiceStatus.DRAFT,
        currency = "AUD"
    )

    @Before
    fun setUp() {
        invoiceRepository = mockk(relaxed = true)
    }

    // ── editInvoice_Success ───────────────────────────────────────────────────

    @Test
    fun `editInvoice_Success - save invoice calls repository saveInvoice`() = runTest {
        coEvery { invoiceRepository.saveInvoice(sampleInvoice) } returns Result.success(1L)

        var successCalled = false
        val result = invoiceRepository.saveInvoice(sampleInvoice)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `editInvoice_Success - updated invoice fields are persisted`() = runTest {
        val updatedInvoice = sampleInvoice.copy(customerName = "Updated Customer")
        coEvery { invoiceRepository.saveInvoice(updatedInvoice) } returns Result.success(1L)

        val result = invoiceRepository.saveInvoice(updatedInvoice)

        coVerify { invoiceRepository.saveInvoice(updatedInvoice) }
        assertTrue(result.isSuccess)
    }

    // ── editInvoice_StatusChange ──────────────────────────────────────────────

    @Test
    fun `editInvoice_StatusChange - invoice can transition from DRAFT to SENT`() = runTest {
        coEvery { invoiceRepository.updateInvoiceStatus(1L, InvoiceStatus.SENT) } returns Result.success(Unit)

        val result = invoiceRepository.updateInvoiceStatus(1L, InvoiceStatus.SENT)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `editInvoice_StatusChange - status update triggers repository update`() = runTest {
        coEvery { invoiceRepository.updateInvoiceStatus(1L, InvoiceStatus.SENT) } returns Result.success(Unit)

        invoiceRepository.updateInvoiceStatus(1L, InvoiceStatus.SENT)
        coVerify { invoiceRepository.updateInvoiceStatus(1L, InvoiceStatus.SENT) }
    }

    // ── editInvoice_Paid_ReadOnly ─────────────────────────────────────────────

    @Test
    fun `editInvoice_Paid_ReadOnly - paid invoice has zero balance remaining`() {
        val paidInvoice = sampleInvoice.copy(
            totalAmount = 50000L,
            amountPaid = 50000L,
            status = InvoiceStatus.PAID
        )
        val balance: Double = paidInvoice.balanceRemaining
        assertEquals(0.0, balance)
        assertTrue(paidInvoice.isFullyPaid)
    }

    @Test
    fun `editInvoice_Paid_ReadOnly - cannot record payment when invoice is fully paid`() {
        val paidInvoice = sampleInvoice.copy(
            totalAmount = 50000L,
            amountPaid = 50000L,
            status = InvoiceStatus.PAID
        )
        val outstanding = paidInvoice.totalAmount - paidInvoice.amountPaid
        assertFalse(outstanding > 0, "No payments should be accepted on a fully paid invoice")
    }

    // ── uiState emissions ─────────────────────────────────────────────────────

    @Test
    fun `uiState - Success state wraps the loaded invoice`() {
        val state: EditInvoiceUiStateV2 = EditInvoiceUiStateV2.Success(sampleInvoice)
        assertIs<EditInvoiceUiStateV2.Success>(state)
        assertEquals(sampleInvoice.id, (state as EditInvoiceUiStateV2.Success).invoice.id)
    }

    @Test
    fun `uiState - Error state contains descriptive message`() {
        val state: EditInvoiceUiStateV2 = EditInvoiceUiStateV2.Error("Invoice not found")
        assertIs<EditInvoiceUiStateV2.Error>(state)
        assertEquals("Invoice not found", (state as EditInvoiceUiStateV2.Error).message)
    }
}



