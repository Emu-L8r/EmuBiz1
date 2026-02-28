package com.emu.emubizwax.domain.model

data class ExportData(
    val header: BusinessInfo,
    val customer: Customer,
    val invoice: Invoice,
    val items: List<LineItem>
)
