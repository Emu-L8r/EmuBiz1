package com.emul8r.bizap.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "invoiceId",
        entityColumn = "parentInvoiceId"
    )
    val items: List<LineItemEntity>
) {
    val subtotal: Double
        get() = items.sumOf { it.unitPrice * it.quantity }
}