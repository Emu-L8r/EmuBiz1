package com.emul8r.bizap.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relationship: one [InvoiceEntity] with its [InvoiceItemEntity] line items (1:N).
 */
data class InvoiceWithInvoiceItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<InvoiceItemEntity>
)
