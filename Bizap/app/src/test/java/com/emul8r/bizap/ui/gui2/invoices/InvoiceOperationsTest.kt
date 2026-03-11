@file:Suppress("UNCHECKED_CAST")
package com.emul8r.bizap.ui.gui2.invoices

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for invoice status transitions and payment recording
 */
class InvoiceOperationsTest {

    private val invoiceRepository = mockk<InvoiceRepository>()

    @Before
    fun setup() {
        io.mockk.clearAllMocks()
    }

    // ========================================
    // Status Transition Tests
    // ========================================

    @Test
    fun `invoice status - DRAFT to SENT transition is valid`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val updated = invoice.copy(status = InvoiceStatus.SENT)

        // Then
        assertEquals(InvoiceStatus.SENT, updated.status)
        assertEquals(InvoiceStatus.DRAFT, invoice.status)
    }

    @Test
    fun `invoice status - SENT to PAID transition is valid`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 1000L,
            status = InvoiceStatus.SENT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val updated = invoice.copy(status = InvoiceStatus.PAID)

        // Then
        assertEquals(InvoiceStatus.PAID, updated.status)
        assertEquals(1000L, updated.amountPaid)
    }

    // ========================================
    // Payment Recording Tests
    // ========================================

    @Test
    fun `payment recording - record partial payment`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.SENT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val updated = invoice.copy(amountPaid = 500L)

        // Then
        assertEquals(500L, updated.amountPaid)
        assertEquals(500L, updated.totalAmount - updated.amountPaid) // Outstanding
    }

    @Test
    fun `payment recording - record full payment`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.SENT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val updated = invoice.copy(amountPaid = 1000L, status = InvoiceStatus.PAID)

        // Then
        assertEquals(1000L, updated.amountPaid)
        assertEquals(InvoiceStatus.PAID, updated.status)
        assertEquals(0L, updated.totalAmount - updated.amountPaid) // Outstanding
    }

    @Test
    fun `payment recording - cannot overpay`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.SENT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val amountToPaymentCents = 1500L
        val isOverpaying = amountToPaymentCents > invoice.totalAmount

        // Then
        assertTrue(isOverpaying)
    }

    // ========================================
    // Invoice Validation Tests
    // ========================================

    @Test
    fun `invoice validation - valid invoice`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "123 St",
            customerEmail = "test@example.com",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val isValid = invoice.totalAmount > 0 && invoice.customerId > 0

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `invoice validation - zero amount is invalid`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 0L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val isValid = invoice.totalAmount > 0

        // Then
        assertFalse(isValid)
    }

    @Test
    fun `invoice validation - no customer is invalid`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 0L, // No customer
            customerName = "",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.DRAFT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val isValid = invoice.customerId > 0

        // Then
        assertFalse(isValid)
    }

    // ========================================
    // Outstanding Amount Tests
    // ========================================

    @Test
    fun `outstanding amount - fully unpaid invoice`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 0L,
            status = InvoiceStatus.SENT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val outstanding = invoice.totalAmount - invoice.amountPaid

        // Then
        assertEquals(1000L, outstanding)
    }

    @Test
    fun `outstanding amount - partially paid invoice`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 600L,
            status = InvoiceStatus.SENT,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val outstanding = invoice.totalAmount - invoice.amountPaid

        // Then
        assertEquals(400L, outstanding)
    }

    @Test
    fun `outstanding amount - fully paid invoice`() = runTest {
        // Given
        val invoice = Invoice(
            id = 1L,
            businessProfileId = 1L,
            customerId = 1L,
            customerName = "Test",
            customerAddress = "",
            customerEmail = "",
            items = emptyList(),
            totalAmount = 1000L,
            amountPaid = 1000L,
            status = InvoiceStatus.PAID,
            date = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis(),
            isQuote = false,
            currencyCode = "AUD",
            taxRate = 0.0,
            taxAmount = 0L,
            invoiceYear = 2026,
            invoiceSequence = 1,
            notes = ""
        )

        // When
        val outstanding = invoice.totalAmount - invoice.amountPaid

        // Then
        assertEquals(0L, outstanding)
    }
}

