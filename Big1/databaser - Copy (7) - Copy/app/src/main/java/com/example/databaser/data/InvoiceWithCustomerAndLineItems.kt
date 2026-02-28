package com.example.databaser.data

import androidx.room.Embedded
import androidx.room.Relation

data class InvoiceWithCustomerAndLineItems(
    @Embedded
    val invoice: Invoice,

    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: Customer,

    @Relation(parentColumn = "id", entityColumn = "invoiceId")
    val lineItems: List<LineItem>
)
