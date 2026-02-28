package com.emul8r.bizap.domain.model

data class Invoice(
    val id: Long = 0,
    val customerId: Long?,
    val customerName: String,
    val date: Long,
    val totalAmount: Double,
    val items: List<LineItem>,
    val isQuote: Boolean,
    val status: InvoiceStatus,
    val photoUris: List<String> = emptyList(),
    val header: String = "",
    val subheader: String = "",
    val notes: String = "",
    val footer: String = ""
)

data class LineItem(
    val id: Long = 0,
    val description: String,
    val quantity: Double,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}

enum class InvoiceStatus {
    DRAFT, SENT, PAID, VOID
}
