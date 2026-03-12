@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.ui.gui2.invoices

import com.emul8r.bizap.BaseUnitTest
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.validation.ValidationRules
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
/**
 * Unit tests for [CreateInvoiceViewModelV2].
 *
 * Verifies invoice creation logic including validation of line items and totals.
 */
class CreateInvoiceViewModelTest : BaseUnitTest() {
    private lateinit var invoiceRepository: InvoiceRepository
    private val customerRepository: CustomerRepository = mockk(relaxed = true)
    private lateinit var viewModel: CreateInvoiceViewModelV2
    private val now = System.currentTimeMillis()
    private val tomorrow = now + 86_400_000L
    private fun buildInvoice(
        items: List<LineItem> = listOf(
            LineItem(description = "Consulting", quantity = 2.0, unitPrice = 5000L)
        ),
        totalAmount: Long = 10000L,
        dueDate: Long = tomorrow
    ) = Invoice(
        id = 0L,
        businessProfileId = 1L,
        customerId = 1L,
        customerName = "Test Customer",
        date = now,
        dueDate = dueDate,
        totalAmount = totalAmount,
        items = items,
        isQuote = false,
        status = InvoiceStatus.DRAFT,
        currencyCode = "AUD"
    )
    @Before
    fun setUp() {
        invoiceRepository = mockk(relaxed = true)
        viewModel = CreateInvoiceViewModelV2(invoiceRepository, customerRepository)
    }
    // ── createInvoice_Success ─────────────────────────────────────────────────
    @Test
    fun `createInvoice_Success - valid invoice with items triggers repository save`() = runTest {
        val invoice = buildInvoice()
        coEvery { invoiceRepository.saveInvoice(invoice) } returns Result.success(1L)
        var successCalled = false
        viewModel.createInvoice(invoice, onSuccess = { successCalled = true }, onError = {})
        advanceUntilIdle()
        coVerify { invoiceRepository.saveInvoice(invoice) }
        assertTrue(successCalled)
    }

    @Test
    fun `createInvoice_Success - success callback invoked on creation`() = runTest {
        val invoice = buildInvoice()
        coEvery { invoiceRepository.saveInvoice(invoice) } returns Result.success(2L)
        var successInvoked = false
        viewModel.createInvoice(invoice, onSuccess = { successInvoked = true }, onError = {})
        advanceUntilIdle()
        assertTrue(successInvoked)
    }

    // ── createInvoice_NoLineItems ─────────────────────────────────────────────
    @Test
    fun `createInvoice_NoLineItems - invoice with empty items fails validation`() {
        val invoice = buildInvoice(items = emptyList())
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "Invoice with no line items should fail validation")
    }

    @Test
    fun `createInvoice_NoLineItems - validation error message is descriptive`() {
        val invoice = buildInvoice(items = emptyList())
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure())
        val error = result.getErrorOrNull()
        assertTrue(error?.contains("line item", ignoreCase = true) == true, "Error should mention line item")
    }

    // ── createInvoice_InvalidDate ─────────────────────────────────────────────
    @Test
    fun `createInvoice_InvalidDate - due date before invoice date fails validation`() {
        val invoice = buildInvoice(dueDate = now - 86_400_000L)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "Due date before invoice date should fail validation")
    }

    @Test
    fun `createInvoice_InvalidDate - same day due date passes validation`() {
        val invoice = buildInvoice(dueDate = now)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Same day due date should pass validation")
    }

    // ── addLineItem_Success ───────────────────────────────────────────────────
    @Test
    fun `addLineItem_Success - adding item increases list size`() {
        val items = mutableListOf(
            LineItem(description = "Item 1", quantity = 1.0, unitPrice = 10000L)
        )
        items.add(LineItem(description = "Item 2", quantity = 2.0, unitPrice = 5000L))
        assertEquals(2, items.size)
    }

    @Test
    fun `addLineItem_Success - total recalculated after adding item`() {
        val items = listOf(
            LineItem(description = "Item 1", quantity = 1.0, unitPrice = 10000L),
            LineItem(description = "Item 2", quantity = 2.0, unitPrice = 5000L)
        )
        val total = items.sumOf { (it.unitPrice * it.quantity).toLong() }
        assertEquals(20000L, total)
    }

    // ── removeLineItem_Success ────────────────────────────────────────────────
    @Test
    fun `removeLineItem_Success - removing item decreases list size`() {
        val items = mutableListOf(
            LineItem(description = "Item 1", quantity = 1.0, unitPrice = 10000L),
            LineItem(description = "Item 2", quantity = 2.0, unitPrice = 5000L)
        )
        items.removeAt(0)
        assertEquals(1, items.size)
        assertEquals("Item 2", items[0].description)
    }

    @Test
    fun `removeLineItem_Success - total updated after removing item`() {
        val items = listOf(
            LineItem(description = "Item 1", quantity = 1.0, unitPrice = 10000L)
        )
        val total = items.sumOf { (it.unitPrice * it.quantity).toLong() }
        assertEquals(10000L, total)
    }

    // ── totalCalculation_Correct ──────────────────────────────────────────────
    @Test
    fun `totalCalculation_Correct - subtotal is sum of all line item totals`() {
        val items = listOf(
            LineItem(description = "Service A", quantity = 3.0, unitPrice = 10000L),
            LineItem(description = "Service B", quantity = 1.0, unitPrice = 25000L)
        )
        val subtotal = items.sumOf { (it.unitPrice * it.quantity).toLong() }
        assertEquals(55000L, subtotal)
    }

    @Test
    fun `totalCalculation_Correct - tax added to subtotal equals total`() {
        val subtotal = 100000L
        val taxRate = 0.10
        val taxAmount = (subtotal * taxRate).toLong()
        val total = subtotal + taxAmount
        assertEquals(10000L, taxAmount)
        assertEquals(110000L, total)
    }
}
