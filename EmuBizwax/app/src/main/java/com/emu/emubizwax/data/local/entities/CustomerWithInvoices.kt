package com.emu.emubizwax.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

// Data class for the relationship (used in DAOs)
data class CustomerWithInvoices(
    @Embedded val customer: CustomerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val invoices: List<InvoiceEntity>
)
