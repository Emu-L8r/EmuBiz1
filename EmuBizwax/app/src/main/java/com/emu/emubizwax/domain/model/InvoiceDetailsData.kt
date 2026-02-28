package com.emu.emubizwax.domain.model

data class InvoiceDetailsData(
    val customer: Customer,
    val invoice: InvoiceWithItems
)
