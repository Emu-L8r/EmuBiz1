package com.emul8r.bizap.domain.model

/**
 * IMMUTABLE SNAPSHOT OF INVOICE AT GENERATION TIME
 */
data class InvoiceSnapshot(
    val invoiceId: Long,
    val invoiceNumber: String,
    val customerName: String,
    val customerAddress: String,
    val customerEmail: String?,
    val date: Long,
    val dueDate: Long,
    val items: List<LineItemSnapshot>,
    val subtotal: Double,
    val taxRate: Double,
    val taxAmount: Double,
    val totalAmount: Double,
    val businessName: String,
    val businessAbn: String,
    val businessEmail: String,
    val businessPhone: String,
    val businessAddress: String,
    val logoBase64: String?,
    val currencyCode: String = "AUD" // NEW: Multi-currency support
)

data class LineItemSnapshot(
    val description: String,
    val quantity: Double,
    val unitPrice: Double,
    val total: Double
)
