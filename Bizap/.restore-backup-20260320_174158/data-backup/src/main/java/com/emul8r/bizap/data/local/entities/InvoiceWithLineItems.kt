package com.emul8r.bizap.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithLineItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val items: List<LineItemEntity>
)
