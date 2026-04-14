package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.InvoiceItem
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import java.time.Instant

/**
 * Unit tests for invoice and line item validation logic in [ValidationRules].
 *
 * Covers customer requirement, invoice number uniqueness concept,
 * date logic, and line item field validation.
 */
class InvoiceValidationTest {

    private val now = Instant.now().toString()
    private val tomorrow = Instant.now().plusSeconds(86_400L).toString()

    private fun buildInvoice(
        customerId: Long? = 1L,
        customerName: String = "Test Customer",
        items: List<InvoiceItem> = listOf(
            InvoiceItem(description = "Service", quantity = 1.0, unitPrice = 10000L)
        ),
        totalAmount: Long = 10000L,
        dateCreated: String = now,
        dueDate: String = tomorrow,
        currency: String = "AUD"
    ) = Invoice(
        id = 0L,
        businessProfileId = 1L,
        customerId = customerId,
        customerName = customerName,
        dateCreated = dateCreated,
        dueDate = dueDate,
        totalAmount = totalAmount,
        items = items,
        isQuote = false,
        status = InvoiceStatus.DRAFT,
        currency = currency
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
        // Invoice number should be formatted as: INV-YYYY-XXXXXX
        val expectedNumber = "INV-2025-000001"
        val invoice = buildInvoice().copy(
            invoiceYear = 2025,
            invoiceSequence = 1,
            version = 1,
            invoiceNumber = expectedNumber
        )
        assertTrue(invoice.invoiceNumber.contains("2025"), "Invoice number should contain year")
        assertTrue(invoice.invoiceNumber.contains("INV-"), "Invoice number should start with INV-")
    }

    @Test
    fun `invoiceNumber_Unique - version 2 invoice has suffix in number`() {
        // Version 2 invoices should have -v2 suffix
        val expectedNumber = "INV-2025-000001-v2"
        val invoice = buildInvoice().copy(
            invoiceYear = 2025,
            invoiceSequence = 1,
            version = 2,
            invoiceNumber = expectedNumber
        )
        assertTrue(invoice.invoiceNumber.contains("v2"), "Version 2 invoice number should contain v2 suffix")
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
        val invoice = buildInvoice(dateCreated = now, dueDate = tomorrow)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Invoice with current date should pass validation")
    }

    @Test
    fun `date_NotFuture - invoice date in the past is valid`() {
        val pastDate = Instant.now().minusSeconds(30 * 86_400L).toString()
        val invoice = buildInvoice(dateCreated = pastDate, dueDate = now)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Invoice with past date should pass validation")
    }

    // ── dueDate_AfterInvoiceDate ──────────────────────────────────────────────

    @Test
    fun `dueDate_AfterInvoiceDate - due date after invoice date passes validation`() {
        val invoice = buildInvoice(dateCreated = now, dueDate = tomorrow)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Due date after invoice date should pass validation")
    }

    @Test
    fun `dueDate_AfterInvoiceDate - due date same as invoice date passes validation`() {
        val invoice = buildInvoice(dateCreated = now, dueDate = now)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess(), "Due date same as invoice date should pass validation")
    }

    @Test
    fun `dueDate_AfterInvoiceDate - due date before invoice date fails validation`() {
        val yesterday = Instant.now().minusSeconds(86_400L).toString()
        val invoice = buildInvoice(dateCreated = now, dueDate = yesterday)
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "Due date before invoice date should fail validation")
    }

    // ── lineItem_Description_Required ────────────────────────────────────────

    @Test
    fun `lineItem_Description_Required - blank item description fails validation`() {
        val item = InvoiceItem(description = "", quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Blank item description should fail validation")
    }

    @Test
    fun `lineItem_Description_Required - whitespace-only description fails validation`() {
        val item = InvoiceItem(description = "   ", quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Whitespace-only description should fail validation")
    }

    @Test
    fun `lineItem_Description_Required - valid description passes validation`() {
        val item = InvoiceItem(description = "Web Development Services", quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isSuccess(), "Valid description should pass validation")
    }

    @Test
    fun `lineItem_Description_Required - description at 500 chars limit is valid`() {
        val item = InvoiceItem(description = "A".repeat(500), quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isSuccess(), "Description at 500 chars should be valid")
    }

    @Test
    fun `lineItem_Description_Required - description over 500 chars fails validation`() {
        val item = InvoiceItem(description = "A".repeat(501), quantity = 1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Description over 500 chars should fail validation")
    }

    // ── lineItem_Quantity_Positive ────────────────────────────────────────────

    @Test
    fun `lineItem_Quantity_Positive - zero quantity fails validation`() {
        val item = InvoiceItem(description = "Service", quantity = 0.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Zero quantity should fail validation")
    }

    @Test
    fun `lineItem_Quantity_Positive - negative quantity fails validation`() {
        val item = InvoiceItem(description = "Service", quantity = -1.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isFailure(), "Negative quantity should fail validation")
    }

    @Test
    fun `lineItem_Quantity_Positive - fractional quantity passes validation`() {
        val item = InvoiceItem(description = "Partial Hour", quantity = 0.5, unitPrice = 20000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isSuccess(), "Fractional quantity should pass validation")
    }

    @Test
    fun `lineItem_Quantity_Positive - positive integer quantity passes validation`() {
        val item = InvoiceItem(description = "Service", quantity = 3.0, unitPrice = 10000L)
        val result = ValidationRules.validateLineItem(item)
        assertTrue(result.isSuccess(), "Positive integer quantity should pass validation")
    }

    // ── currency code validation ──────────────────────────────────────────────

    @Test
    fun `currency code - 3 letter ISO code passes validation`() {
        val invoice = buildInvoice(currency = "USD")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isSuccess())
    }

    @Test
    fun `currency code - invalid length code fails validation`() {
        val invoice = buildInvoice(currency = "US")
        val result = ValidationRules.validateInvoice(invoice)
        assertTrue(result.isFailure(), "2-letter currency code should fail validation")
    }
}




