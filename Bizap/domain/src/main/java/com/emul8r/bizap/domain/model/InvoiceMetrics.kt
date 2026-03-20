package com.emul8r.bizap.domain.model

data class InvoiceMetrics(
    val subtotal: Long,             // cents
    val taxAmount: Long,            // cents
    val discountAmount: Long = 0L,  // cents (future)
    val totalAmount: Long           // cents
) {
    init {
        require(subtotal >= 0) { "Subtotal cannot be negative" }
        require(taxAmount >= 0) { "Tax amount cannot be negative" }
        require(totalAmount >= 0) { "Total amount cannot be negative" }
    }
}
