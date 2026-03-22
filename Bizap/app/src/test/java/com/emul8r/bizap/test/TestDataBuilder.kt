package com.emul8r.bizap.test

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.invoice.model.PaymentRecord
import com.emul8r.bizap.domain.invoice.model.PaymentMethod
import java.time.LocalDate

/**
 * Factory for building test data objects.
 * Use these in unit tests to create realistic domain models.
 */
object TestDataBuilder {

    /**
     * Builds a test Invoice with sensible defaults.
     */
    fun buildInvoice(
        id: Long = 1L,
        businessProfileId: Long = 1L,
        customerId: Long = 1L,
        customerName: String = "Test Customer",
        totalAmount: Long = 100000L,  // In cents (1000.00)
        amountPaid: Long = 0L,
        status: InvoiceStatus = InvoiceStatus.SENT,
        dueDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
        date: Long = System.currentTimeMillis(),
        updatedAt: Long = System.currentTimeMillis(),
        invoiceYear: Int = 2026,
        invoiceSequence: Int = 1,
        isQuote: Boolean = false,
        items: List<LineItem> = listOf(
            buildLineItem(description = "Test Item", quantity = 1.0, unitPrice = 100000L)
        )
    ) = Invoice(
        id = id,
        businessProfileId = businessProfileId,
        customerId = customerId,
        customerName = customerName,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        status = status,
        dueDate = dueDate,
        date = date,
        updatedAt = updatedAt,
        invoiceYear = invoiceYear,
        invoiceSequence = invoiceSequence,
        isQuote = isQuote,
        items = items
    )

    /**
     * Builds a test LineItem with sensible defaults.
     */
    fun buildLineItem(
        id: Long = 1L,
        description: String = "Item",
        quantity: Double = 1.0,
        unitPrice: Long = 100000L  // In cents
    ) = LineItem(
        id = id,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice
    )

    /**
     * Builds a test PaymentRecord with sensible defaults.
     */
    fun buildPaymentRecord(
        id: Long = 1L,
        amount: Double = 500.0,
        date: LocalDate = LocalDate.now(),
        method: PaymentMethod = PaymentMethod.BANK_TRANSFER,
        reference: String = "REF-001",
        notes: String? = null
    ) = PaymentRecord(
        id = id,
        amount = amount,
        date = date,
        method = method,
        reference = reference,
        notes = notes
    )
}


