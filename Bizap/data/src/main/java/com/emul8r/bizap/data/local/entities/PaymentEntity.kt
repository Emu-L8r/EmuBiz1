package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * GUI2 payment entity for the `payments` table.
 *
 * Records an individual payment transaction applied to an invoice.
 * All monetary values are stored as integer cents (e.g. 14999 = $149.99).
 */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "idx_payments_invoice", value = ["invoiceId"]),
        Index(name = "idx_payments_business", value = ["businessId"])
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Business this payment belongs to. */
    val businessId: Long,
    /** Invoice this payment is applied to. Deleted when the invoice is deleted (CASCADE). */
    val invoiceId: Long,
    val amount: Long,        // cents
    val paymentDate: Long,   // Unix timestamp (milliseconds)
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
