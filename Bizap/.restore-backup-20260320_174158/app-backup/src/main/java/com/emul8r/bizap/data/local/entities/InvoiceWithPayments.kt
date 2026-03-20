package com.emul8r.bizap.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Room relationship: one [InvoiceEntity] with its [PaymentEntity] payment records (1:N).
 */
data class InvoiceWithPayments(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoiceId"
    )
    val payments: List<PaymentEntity>
)
