package com.emul8r.bizap.domain.validation

import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceItem
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.Result
import org.junit.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for [ValidationRules] — invoice, customer, and line item validation.
 */
class ValidationRulesTest {

    // ── helpers ────────────────────────────────────────────────────────────

    private fun validItem() = InvoiceItem(
        description = "Consulting",
        quantity = 1.0,
        unitPrice = 10_000L
    )

    private fun validInvoice() = Invoice(
        customerId = 1L,
        customerName = "John Doe",
        dateCreated = "2026-01-01T00:00:00Z",
        dueDate = "2026-01-31T00:00:00Z",
        totalAmount = 10_000L,
        currency = "AUD",
        items = listOf(validItem())
    )

    private fun validCustomer() = Customer(
        name = "Acme Corp",
        email = "acme@example.com"
    )

    // ── validateInvoice — success ──────────────────────────────────────────

    @Test
    fun `validateInvoice passes for valid invoice`() {
        val result = ValidationRules.validateInvoice(validInvoice())
        assertIs<Result.Success<Unit>>(result)
    }

    // ── validateInvoice — failure cases ───────────────────────────────────

    @Test
    fun `validateInvoice fails when no line items`() {
        val invoice = validInvoice().copy(items = emptyList())
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
        assertTrue((result as Result.Failure).error.contains("line item"))
    }

    @Test
    fun `validateInvoice fails when total amount is zero`() {
        val invoice = validInvoice().copy(totalAmount = 0L)
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateInvoice fails when total amount is negative`() {
        val invoice = validInvoice().copy(totalAmount = -100L)
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateInvoice fails when customer name is blank`() {
        val invoice = validInvoice().copy(customerName = "")
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
        assertTrue((result as Result.Failure).error.contains("Customer name"))
    }

    @Test
    fun `validateInvoice fails when due date is before invoice date`() {
        val invoice = validInvoice().copy(
            dateCreated = "2026-02-01T00:00:00Z",
            dueDate = "2026-01-01T00:00:00Z"
        )
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateInvoice fails when currency code is invalid`() {
        val invoice = validInvoice().copy(currency = "US")
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateInvoice fails when currency code contains digits`() {
        val invoice = validInvoice().copy(currency = "US1")
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateInvoice passes when due date equals invoice date`() {
        val invoice = validInvoice().copy(
            dateCreated = "2026-01-01T00:00:00Z",
            dueDate = "2026-01-01T00:00:00Z"
        )
        val result = ValidationRules.validateInvoice(invoice)
        assertIs<Result.Success<Unit>>(result)
    }

    // ── validateCustomer — success ─────────────────────────────────────────

    @Test
    fun `validateCustomer passes for valid customer`() {
        val result = ValidationRules.validateCustomer(validCustomer())
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validateCustomer passes with no email`() {
        val customer = validCustomer().copy(email = null)
        val result = ValidationRules.validateCustomer(customer)
        assertIs<Result.Success<Unit>>(result)
    }

    // ── validateCustomer — failure cases ──────────────────────────────────

    @Test
    fun `validateCustomer fails when name is blank`() {
        val customer = validCustomer().copy(name = "")
        val result = ValidationRules.validateCustomer(customer)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateCustomer fails when name is too short`() {
        val customer = validCustomer().copy(name = "A")
        val result = ValidationRules.validateCustomer(customer)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateCustomer fails when name exceeds 100 characters`() {
        val customer = validCustomer().copy(name = "A".repeat(101))
        val result = ValidationRules.validateCustomer(customer)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateCustomer fails when email is invalid format`() {
        val customer = validCustomer().copy(email = "notanemail")
        val result = ValidationRules.validateCustomer(customer)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateCustomer passes when name is exactly 2 characters`() {
        val customer = validCustomer().copy(name = "Li")
        val result = ValidationRules.validateCustomer(customer)
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validateCustomer passes when name is exactly 100 characters`() {
        val customer = validCustomer().copy(name = "A".repeat(100))
        val result = ValidationRules.validateCustomer(customer)
        assertIs<Result.Success<Unit>>(result)
    }

    // ── validateLineItem — success ─────────────────────────────────────────

    @Test
    fun `validateLineItem passes for valid item`() {
        val result = ValidationRules.validateLineItem(validItem())
        assertIs<Result.Success<Unit>>(result)
    }

    // ── validateLineItem — failure cases ──────────────────────────────────

    @Test
    fun `validateLineItem fails when description is blank`() {
        val item = validItem().copy(description = "")
        val result = ValidationRules.validateLineItem(item)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateLineItem fails when description exceeds 500 characters`() {
        val item = validItem().copy(description = "A".repeat(501))
        val result = ValidationRules.validateLineItem(item)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateLineItem fails when quantity is zero`() {
        val item = validItem().copy(quantity = 0.0)
        val result = ValidationRules.validateLineItem(item)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateLineItem fails when quantity is negative`() {
        val item = validItem().copy(quantity = -1.0)
        val result = ValidationRules.validateLineItem(item)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateLineItem fails when unit price is zero`() {
        val item = validItem().copy(unitPrice = 0L)
        val result = ValidationRules.validateLineItem(item)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateLineItem fails when unit price is negative`() {
        val item = validItem().copy(unitPrice = -500L)
        val result = ValidationRules.validateLineItem(item)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateLineItem fails when total is unreasonably large`() {
        val item = validItem().copy(quantity = 1_000_000.0, unitPrice = 100_000L)
        val result = ValidationRules.validateLineItem(item)
        assertIs<Result.Failure<Unit>>(result)
    }

    @Test
    fun `validateLineItem passes for fractional quantity`() {
        val item = validItem().copy(quantity = 0.5)
        val result = ValidationRules.validateLineItem(item)
        assertIs<Result.Success<Unit>>(result)
    }
}


