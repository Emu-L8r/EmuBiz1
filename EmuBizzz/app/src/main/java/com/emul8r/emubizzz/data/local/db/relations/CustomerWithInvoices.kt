package com.emul8r.emubizzz.data.local.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.emul8r.emubizzz.data.local.db.entities.CustomerEntity
import com.emul8r.emubizzz.data.local.db.entities.InvoiceEntity

data class CustomerWithInvoices(
    @Embedded val customer: CustomerEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val invoices: List<InvoiceEntity>
)
