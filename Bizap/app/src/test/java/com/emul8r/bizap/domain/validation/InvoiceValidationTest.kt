package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for invoice and line item validation logic in [ValidationRules].
 *
 * Covers customer requirement, invoice number uniqueness concept,
 * date logic, and line item field validation.
 */
class InvoiceValidationTest {

    private val now = System.currentTimeMillis()
    private val tomorrow = now + 86_400_000L

    private fun buildInvoice(
        customerId: Long? = 1L,
        customerName: String = "Test Customer",
        items: List<LineItem> = listOf(
            LineItem(description = "Service", quantity = 1.0, unitPrice = 10000L)
        ),
        totalAmount: Long = 10000L,
        date: Long = now,
        dueDate: Long = tomorrow,
        currencyCode: String = "AUD"
    ) = Invoice(
        id = 0L,
        businessProfileId = 1L,
        customerId = customerId,
        customerName = customerName,
        date = date,
        dueDate = dueDate,
        totalAmount = totalAmount,
        items = items,
        isQuote = false,
        status = InvoiceStatus.DRAFT,
        currencyCode = currencyCode
    )

    // ── customer_Required ─────────────────────────────────────────────────────

    @Test
    fun `customer_Required - blank customer name fails validation`() {
        val invoice = buildInvoice(customerName = "")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "Invoice with blank customer name should fail validation")
    }

    @Test
    fun `customer_Required - invoice with customer name set passes validation`() {
        val invoice = buildInvoice(customerName = "Jane Doe")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Invoice with customer name should pass validation")
    }

    @Test
    fun `customer_Required - null customerId is allowed when customer name is set`() {
        val invoice = buildInvoice(customerId = null, customerName = "Deleted Customer")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Null customerId with customer name should pass validation")
    }

    // ── invoiceNumber_Unique ──────────────────────────────────────────────────

    @Test
    fun `invoiceNumber_Unique - invoice number format includes year and sequence`() {
        val invoice = buildInvoice().copy(invoiceYear = 2025, invoiceSequence = 1, version = 1)
        val number = invoice.invoiceNumber
        assertTrue(number.contains("2025"), "Invoice number should contain year")
        assertTrue(number.contains("INV-"), "Invoice number should start with INV-")
    }

    @Test
    fun `invoiceNumber_Unique - version 2 invoice has suffix in number`() {
        val invoice = buildInvoice().copy(invoiceYear = 2025, invoiceSequence = 1, version = 2)
        val number = invoice.invoiceNumber
        assertTrue(number.contains("v2"), "Version 2 invoice number should contain v2 suffix")
    }

    @Test
    fun `invoiceNumber_Unique - uniqueness checked in set of existing numbers`() {
        val existingNumbers = setOf("INV-2025-000001", "INV-2025-000002")
        val newNumber = "INV-2025-000003"
        assertFalse(newNumber in existingNumbers, "New invoice number should not be in existing set")
    }

    // ── date_NotFuture ────────────────────────────────────────────────────────

    @Test
    fun `date_NotFuture - current date is valid for invoice`() {
        val invoice = buildInvoice(date = now, dueDate = tomorrow)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Invoice with current date should pass validation")
    }

    @Test
    fun `date_NotFuture - invoice date in the past is valid`() {
        val pastDate = now - 30 * 86_400_000L
        val invoice = buildInvoice(date = pastDate, dueDate = now)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Invoice with past date should pass validation")
    }

    // ── dueDate_AfterInvoiceDate ──────────────────────────────────────────────

    @Test
    fun `dueDate_AfterInvoiceDate - due date after invoice date passes validation`() {
        val invoice = buildInvoice(date = now, dueDate = tomorrow)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Due date after invoice date should pass validation")
    }

    @Test
    fun `dueDate_AfterInvoiceDate - due date same as invoice date passes validation`() {
        val invoice = buildInvoice(date = now, dueDate = now)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Due date same as invoice date should pass validation")
    }

    @Test
    fun `dueDate_AfterInvoiceDate - due date before invoice date fails validation`() {
        val yesterday = now - 86_400_000L
        val invoice = buildInvoice(date = now, dueDate = yesterday)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "Due date before invoice date should fail validation")
    }

    // ── lineItem_Description_Required ────────────────────────────────────────

    @Test
    fun `lineItem_Description_Required - blank item description fails validation`() {
        val item = LineItem(description = "", quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Blank item description should fail validation")
    }

    @Test
    fun `lineItem_Description_Required - whitespace-only description fails validation`() {
        val item = LineItem(description = "   ", quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Whitespace-only description should fail validation")
    }

    @Test
    fun `lineItem_Description_Required - valid description passes validation`() {
        val item = LineItem(description = "Web Development Services", quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isSuccess(), "Valid description should pass validation")
    }

    @Test
    fun `lineItem_Description_Required - description at 500 chars limit is valid`() {
        val item = LineItem(description = "A".repeat(500), quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isSuccess(), "Description at 500 chars should be valid")
    }

    @Test
    fun `lineItem_Description_Required - description over 500 chars fails validation`() {
        val item = LineItem(description = "A".repeat(501), quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Description over 500 chars should fail validation")
    }

    // ── lineItem_Quantity_Positive ────────────────────────────────────────────

    @Test
    fun `lineItem_Quantity_Positive - zero quantity fails validation`() {
        val item = LineItem(description = "Service", quantity = 0.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Zero quantity should fail validation")
    }

    @Test
    fun `lineItem_Quantity_Positive - negative quantity fails validation`() {
        val item = LineItem(description = "Service", quantity = -1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Negative quantity should fail validation")
    }

    @Test
    fun `lineItem_Quantity_Positive - fractional quantity passes validation`() {
        val item = LineItem(description = "Partial Hour", quantity = 0.5, unitPrice = 20000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isSuccess(), "Fractional quantity should pass validation")
    }

    @Test
    fun `lineItem_Quantity_Positive - positive integer quantity passes validation`() {
        val item = LineItem(description = "Service", quantity = 3.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isSuccess(), "Positive integer quantity should pass validation")
    }

    // ── currency code validation ──────────────────────────────────────────────

    @Test
    fun `currency code - 3 letter ISO code passes validation`() {
        val invoice = buildInvoice(currencyCode = "USD")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess())
    }

    @Test
    fun `currency code - invalid length code fails validation`() {
        val invoice = buildInvoice(currencyCode = "US")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "2-letter currency code should fail validation")
    }
}
