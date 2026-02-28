package com.emu.emubizwax.domain.model

import java.math.BigDecimal

data class LineItem(
    val id: Long = 0,
    val invoiceId: Long,
    val description: String,
    val quantity: BigDecimal,
    val unitPrice: BigDecimal
)
