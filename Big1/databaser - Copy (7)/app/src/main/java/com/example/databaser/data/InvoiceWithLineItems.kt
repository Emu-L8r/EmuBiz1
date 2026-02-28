package com.example.databaser.data

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithLineItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val lineItems: List<LineItem>
)
