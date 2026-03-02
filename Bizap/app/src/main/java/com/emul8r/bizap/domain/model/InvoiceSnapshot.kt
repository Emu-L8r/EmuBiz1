package com.emul8r.bizap.domain.model

/**
 * IMMUTABLE SNAPSHOT OF INVOICE AT GENERATION TIME
 * All monetary amounts are stored as Long (cents)
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
    val subtotal: Long,            // Cents
    val taxRate: Double,
    val taxAmount: Long,           // Cents
    val totalAmount: Long,         // Cents
    val businessName: String,
    val businessAbn: String,
    val businessEmail: String,
    val businessPhone: String,
    val businessAddress: String,
    val logoBase64: String?,
    val currencyCode: String = "AUD"
)

data class LineItemSnapshot(
    val description: String,
    val quantity: Double,
    val unitPrice: Long,           // Cents
    val total: Long                // Cents (unitPrice * quantity)
)

