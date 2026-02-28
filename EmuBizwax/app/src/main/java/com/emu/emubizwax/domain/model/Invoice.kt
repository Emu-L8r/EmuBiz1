package com.emu.emubizwax.domain.model

import java.math.BigDecimal

data class Invoice(
    val id: Long = 0,
    val customerId: Long,
    val date: Long,
    val status: InvoiceStatus,
    val isQuote: Boolean,
    val taxRate: Double,
    val photoUris: List<String>,
    val totalAmount: BigDecimal = BigDecimal.ZERO
) {
    val subtotal: BigDecimal
        get() = totalAmount.divide(BigDecimal(1 + taxRate), 2, BigDecimal.ROUND_HALF_UP)
}
