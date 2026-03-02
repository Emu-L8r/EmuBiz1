package com.emul8r.bizap.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",      // The Primary Key in InvoiceEntity
        entityColumn = "invoiceId" // The Foreign Key in LineItemEntity
    )
    val items: List<LineItemEntity>
) {
    // Subtotal in cents: sum of (unitPrice * quantity) for each item
    // Result: Long (in cents)
    val subtotal: Long
        get() = items.sumOf { (it.unitPrice * it.quantity).toLong() }
}
