package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.Result
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Invoice-focused validation tests for [ValidationRules.validateInvoice].
 * Covers edge cases and business rule correctness.
 */
class InvoiceValidationTest {

    // ── helpers ────────────────────────────────────────────────────────────

    private fun item(desc: String = "Service", qty: Double = 1.0, price: Long = 10_000L) =
        InvoiceItem(description = desc, quantity = qty, unitPrice = price)

    private fun baseInvoice(vararg items: InvoiceItem = arrayOf(item())) = Invoice(
        customerId = 10L,
        customerName = "Test Client",
        dateCreated = "2026-01-01T00:00:00Z",
        dueDate = "2026-02-01T00:00:00Z",
        totalAmount = 10_000L,
        currency = "USD",
        items = items.toList(),
        status = InvoiceStatus.DRAFT
    )

    // ── passing cases ──────────────────────────────────────────────────────

    @Test
    fun `valid invoice with single item passes`() {
        assertIs<Result.Success<Unit>>(ValidationRules.validateInvoice(baseInvoice()))
    }

    @Test
    fun `valid invoice with multiple items passes`() {
        val invoice = baseInvoice(item("Design"), item("Development"), item("Hosting"))
        assertIs<Result.Success<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    @Test
    fun `invoice with USD currency passes`() {
        assertIs<Result.Success<Unit>>(ValidationRules.validateInvoice(baseInvoice()))
    }

    @Test
    fun `invoice with EUR currency passes`() {
        val invoice = baseInvoice().copy(currency = "EUR")
        assertIs<Result.Success<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    @Test
    fun `invoice with GBP currency passes`() {
        val invoice = baseInvoice().copy(currency = "GBP")
        assertIs<Result.Success<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    // ── item failures ──────────────────────────────────────────────────────

    @Test
    fun `invoice fails when items list is empty`() {
        val invoice = baseInvoice().copy(items = emptyList())
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `invoice fails when an item has blank description`() {
        val invoice = baseInvoice(item(desc = ""))
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `invoice fails when an item has zero quantity`() {
        val invoice = baseInvoice(item(qty = 0.0))
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `invoice fails when an item has zero price`() {
        val invoice = baseInvoice(item(price = 0L))
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    // ── amount failures ────────────────────────────────────────────────────

    @Test
    fun `invoice fails when total amount is zero`() {
        val invoice = baseInvoice().copy(totalAmount = 0L)
        assertIs<Result.Failure<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    @Test
    fun `invoice fails when total amount is negative`() {
        val invoice = baseInvoice().copy(totalAmount = -1L)
        assertIs<Result.Failure<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    // ── date failures ──────────────────────────────────────────────────────

    @Test
    fun `invoice fails when due date is before invoice date`() {
        val invoice = baseInvoice().copy(
            dateCreated = "2026-06-01T00:00:00Z",
            dueDate = "2026-01-01T00:00:00Z"
        )
        assertIs<Result.Failure<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    @Test
    fun `invoice passes when due date equals invoice date`() {
        val invoice = baseInvoice().copy(
            dateCreated = "2026-03-15T00:00:00Z",
            dueDate = "2026-03-15T00:00:00Z"
        )
        assertIs<Result.Success<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    @Test
    fun `invoice skips date validation when either date is blank`() {
        val invoice = baseInvoice().copy(dateCreated = "", dueDate = "")
        // Date check is skipped when blank — should still pass other rules
        assertIs<Result.Success<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    // ── customer name failures ─────────────────────────────────────────────

    @Test
    fun `invoice fails when customer name is blank`() {
        val invoice = baseInvoice().copy(customerName = "")
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
        assertTrue((result as Result.Failure).error.contains("Customer name"))
    }

    @Test
    fun `invoice fails when customer name is only whitespace`() {
        val invoice = baseInvoice().copy(customerName = "   ")
        assertIs<Result.Failure<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    // ── currency code failures ─────────────────────────────────────────────

    @Test
    fun `invoice fails when currency code is 2 characters`() {
        val invoice = baseInvoice().copy(currency = "US")
        assertIs<Result.Failure<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    @Test
    fun `invoice fails when currency code is 4 characters`() {
        val invoice = baseInvoice().copy(currency = "USDX")
        assertIs<Result.Failure<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    @Test
    fun `invoice fails when currency code is blank`() {
        val invoice = baseInvoice().copy(currency = "")
        assertIs<Result.Failure<Unit>>(ValidationRules.validateInvoice(invoice))
    }

    @Test
    fun `invoice fails when currency code contains numbers`() {
        val invoice = baseInvoice().copy(currency = "U1D")
        assertIs<Result.Failure<Unit>>(ValidationRules.validateInvoice(invoice))
    }
}

