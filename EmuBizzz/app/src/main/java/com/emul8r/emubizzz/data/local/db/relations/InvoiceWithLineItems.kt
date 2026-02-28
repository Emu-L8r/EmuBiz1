package com.emul8r.emubizzz.data.local.db.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.emul8r.emubizzz.data.local.db.entities.Invoice
import com.emul8r.emubizzz.data.local.db.entities.LineItem

data class InvoiceWithLineItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "invoiceId",
        entityColumn = "invoiceId"
    )
    val lineItems: List<LineItem>
)