package com.emul8r.bizap.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class CustomerWithInvoices(
    @Embedded val customer: CustomerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val invoices: List<InvoiceEntity>
)
