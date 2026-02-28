package com.emul8r.bizap.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Invoice(
    val id: Long = 0,
    val businessProfileId: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerAddress: String = "",
    val customerEmail: String? = null,
    val date: Long,
    val dueDate: Long = 0,
    val totalAmount: Double,
    val items: List<LineItem>,
    val isQuote: Boolean,
    val status: InvoiceStatus,
    val header: String? = null,
    val subheader: String? = null,
    val notes: String? = null,
    val footer: String? = null,
    val photoUris: List<String> = emptyList(),
    val pdfUri: String? = null,
    val taxRate: Double = 0.1,
    val taxAmount: Double = 0.0,
    val companyLogoPath: String? = null,
    val updatedAt: Long = 0,
    val amountPaid: Double = 0.0,
    val parentInvoiceId: Long? = null,
    val version: Int = 1,
    val invoiceYear: Int = 0,
    val invoiceSequence: Int = 0,
    val currencyCode: String = "AUD" // NEW: Multi-currency support
) {
    val invoiceId: Long get() = id
    val total: Double get() = totalAmount
    val balanceRemaining: Double get() = totalAmount - amountPaid
    val isFullyPaid: Boolean get() = balanceRemaining <= 0.0

    val invoiceNumber: String
        get() {
            val base = "INV-$invoiceYear-${invoiceSequence.toString().padStart(6, '0')}"
            return if (version > 1) "$base-v$version" else base
        }

    fun getFormattedInvoiceNumber(): String {
        return invoiceNumber
    }
}

data class LineItem(
    val id: Long = 0,
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val transientId: String = java.util.UUID.randomUUID().toString()
) {
    val itemId: Long get() = id
}

enum class InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    OVERDUE,
    PARTIALLY_PAID
}
