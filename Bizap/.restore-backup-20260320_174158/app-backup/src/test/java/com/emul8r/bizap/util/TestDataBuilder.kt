package com.emul8r.bizap.util

import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem

/**
 * Builder utility for creating test data objects.
 *
 * Provides default-valued factory methods for all domain objects, enabling
 * concise test setup while allowing individual field overrides as needed.
 */
object TestDataBuilder {

    private val now = System.currentTimeMillis()

    // ── Customer ──────────────────────────────────────────────────────────────

    fun buildCustomer(
        id: Long = 1L,
        name: String = "Test Customer",
        email: String? = "test@example.com",
        phone: String? = "0412345678",
        address: String? = "123 Test Street, Sydney NSW 2000",
        businessName: String? = null,
        businessNumber: String? = null,
        city: String? = "Sydney",
        postalCode: String? = "2000",
        notes: String = ""
    ) = Customer(
        id = id,
        name = name,
        email = email,
        phone = phone,
        address = address,
        businessName = businessName,
        businessNumber = businessNumber,
        city = city,
        postalCode = postalCode,
        notes = notes
    )

    // ── LineItem ──────────────────────────────────────────────────────────────

    fun buildLineItem(
        id: Long = 1L,
        description: String = "Consulting Services",
        quantity: Double = 1.0,
        unitPrice: Long = 10000L  // $100.00 in cents
    ) = LineItem(
        id = id,
        description = description,
        quantity = quantity,
        unitPrice = unitPrice
    )

    fun buildLineItems(count: Int = 2): List<LineItem> {
        return (1..count).map { index ->
            buildLineItem(
                id = index.toLong(),
                description = "Service Item $index",
                quantity = index.toDouble(),
                unitPrice = 5000L * index
            )
        }
    }

    // ── Invoice ───────────────────────────────────────────────────────────────

    fun buildInvoice(
        id: Long = 1L,
        businessProfileId: Long = 1L,
        customerId: Long? = 1L,
        customerName: String = "Test Customer",
        totalAmount: Long = 100000L,  // $1000 in cents
        amountPaid: Long = 0L,
        status: InvoiceStatus = InvoiceStatus.DRAFT,
        currencyCode: String = "AUD",
        taxRate: Double = 0.10,
        taxAmount: Long = 10000L,
        date: Long = now,
        dueDate: Long = now + 30 * 86_400_000L,
        isQuote: Boolean = false,
        items: List<LineItem> = listOf(
            buildLineItem(unitPrice = 90000L)
        )
    ) = Invoice(
        id = id,
        businessProfileId = businessProfileId,
        customerId = customerId,
        customerName = customerName,
        date = date,
        dueDate = dueDate,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        items = items,
        isQuote = isQuote,
        status = status,
        currencyCode = currencyCode,
        taxRate = taxRate,
        taxAmount = taxAmount
    )

    fun buildPaidInvoice(
        id: Long = 1L,
        totalAmount: Long = 100000L
    ) = buildInvoice(
        id = id,
        totalAmount = totalAmount,
        amountPaid = totalAmount,
        status = InvoiceStatus.PAID
    )

    fun buildPartiallyPaidInvoice(
        id: Long = 1L,
        totalAmount: Long = 100000L,
        amountPaid: Long = 50000L
    ) = buildInvoice(
        id = id,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        status = InvoiceStatus.PARTIALLY_PAID
    )

    fun buildOverdueInvoice(
        id: Long = 1L,
        totalAmount: Long = 100000L,
        daysOverdue: Int = 45
    ) = buildInvoice(
        id = id,
        totalAmount = totalAmount,
        status = InvoiceStatus.OVERDUE,
        date = now - (daysOverdue + 30) * 86_400_000L,
        dueDate = now - daysOverdue * 86_400_000L
    )

    // ── Collections ───────────────────────────────────────────────────────────

    fun buildCustomerList(count: Int = 5): List<Customer> {
        return (1..count).map { index ->
            buildCustomer(
                id = index.toLong(),
                name = "Customer $index",
                email = "customer$index@example.com"
            )
        }
    }

    fun buildInvoiceList(
        count: Int = 5,
        customerId: Long = 1L,
        businessProfileId: Long = 1L
    ): List<Invoice> {
        return (1..count).map { index ->
            buildInvoice(
                id = index.toLong(),
                customerId = customerId,
                businessProfileId = businessProfileId,
                totalAmount = 10000L * index,
                status = when (index % 4) {
                    0 -> InvoiceStatus.PAID
                    1 -> InvoiceStatus.SENT
                    2 -> InvoiceStatus.PARTIALLY_PAID
                    else -> InvoiceStatus.DRAFT
                }
            )
        }
    }
}
