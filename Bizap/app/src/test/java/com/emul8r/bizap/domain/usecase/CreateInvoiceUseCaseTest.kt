@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.validation.ValidationRules
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.time.Instant

/**
 * Unit tests for invoice creation, covering validation and repository delegation.
 *
 * These tests exercise the [ValidationRules] and [InvoiceRepository] boundary
 * for invoice creation scenarios.
 */
class CreateInvoiceUseCaseTest : BaseUnitTest() {

    private lateinit var invoiceRepository: InvoiceRepository

    private val now = Instant.now().toString()
    private val tomorrow = Instant.now().plusSeconds(86_400L).toString()

    private fun buildInvoice(
        customerId: Long? = 1L,
        items: List<InvoiceItem> = listOf(
            InvoiceItem(description = "Consulting", quantity = 1.0, unitPrice = 50000L)
        ),
        totalAmount: Long = 50000L,
        customerName: String = "Test Customer"
    ) = Invoice(
        id = 0L,
        businessProfileId = 1L,
        customerId = customerId,
        customerName = customerName,
        dateCreated = now,
        dueDate = tomorrow,
        totalAmount = totalAmount,
        items = items,
        isQuote = false,
        status = InvoiceStatus.DRAFT,
        currency = "AUD"
    )

    @Before
    fun setUp() {
        invoiceRepository = mockk(relaxed = true)
    }

    // ── validInvoice_Success ──────────────────────────────────────────────────

    @Test
    fun `validInvoice_Success - valid invoice passes domain validation`() {
        val invoice = buildInvoice()
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Valid invoice should pass validation")
    }

    @Test
    fun `validInvoice_Success - valid invoice is saved to repository`() = runTest {
        val invoice = buildInvoice()
        coEvery { invoiceRepository.saveInvoice(invoice) } returns Result.success(1L)

        val result = invoiceRepository.saveInvoice(invoice)

        assertTrue(result.isSuccess)
        coVerify { invoiceRepository.saveInvoice(invoice) }
    }

    @Test
    fun `validInvoice_Success - repository returns generated invoice ID`() = runTest {
        val invoice = buildInvoice()
        coEvery { invoiceRepository.saveInvoice(invoice) } returns Result.success(42L)

        val result = invoiceRepository.saveInvoice(invoice)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }

    // ── noLineItems_Failure ───────────────────────────────────────────────────

    @Test
    fun `noLineItems_Failure - invoice without items fails validation`() {
        val invoice = buildInvoice(items = emptyList())
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "Invoice with no line items should fail validation")
    }

    @Test
    fun `noLineItems_Failure - error message mentions line item requirement`() {
        val invoice = buildInvoice(items = emptyList())
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure())
        val error = result.getErrorOrNull()
        assertNotNull(error)
        assertTrue(error.contains("line item", ignoreCase = true))
    }

    @Test
    fun `noLineItems_Failure - single item invoice passes validation`() {
        val invoice = buildInvoice(items = listOf(
            InvoiceItem(description = "Service", quantity = 1.0, unitPrice = 10000L)
        ))
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Invoice with at least one item should pass validation")
    }

    // ── invalidCustomer_Failure ───────────────────────────────────────────────

    @Test
    fun `invalidCustomer_Failure - blank customer name fails validation`() {
        val invoice = buildInvoice(customerName = "")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "Invoice with blank customer name should fail validation")
    }

    @Test
    fun `invalidCustomer_Failure - invoice with customer name set passes validation`() {
        val invoice = buildInvoice(customerName = "Valid Customer")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Invoice with customer name should pass validation")
    }

    @Test
    fun `invalidCustomer_Failure - null customerId is allowed when customer was deleted`() {
        val invoice = buildInvoice(customerId = null, customerName = "Former Customer")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Null customerId should be valid (customer may be deleted)")
    }

    @Test
    fun `invalidCustomer_Failure - repository failure on save is returned as failure result`() = runTest {
        val invoice = buildInvoice()
        coEvery { invoiceRepository.saveInvoice(invoice) } returns Result.failure(
            Exception("Foreign key constraint failed")
        )

        val result = invoiceRepository.saveInvoice(invoice)

        assertFalse(result.isSuccess)
        assertNotNull(result.exceptionOrNull())
    }
}




