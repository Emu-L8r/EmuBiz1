package com.emu.emubizwax.domain.model

data class InvoiceWithItems(
    val invoice: Invoice,
    val items: List<LineItem>
)
