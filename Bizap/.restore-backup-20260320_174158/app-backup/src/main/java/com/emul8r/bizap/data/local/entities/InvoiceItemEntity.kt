package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * GUI2 line-item entity for the `invoice_items` table.
 *
 * Each row represents one line on an invoice.
 * All monetary values are stored as integer cents (e.g. 4999 = $49.99).
 */
@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(name = "idx_invoice_items_invoice", value = ["invoiceId"]),
        Index(name = "idx_invoice_items_business", value = ["businessId"])
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** Business this item belongs to (mirrors the parent invoice's businessProfileId). */
    val businessId: Long,
    /** Parent invoice. Deleted automatically when the invoice is deleted (CASCADE). */
    val invoiceId: Long,
    val description: String,
    val quantity: Double,
    val unitPrice: Long,   // cents
    val totalPrice: Long,  // cents = round(quantity * unitPrice)
    val createdAt: Long = System.currentTimeMillis()
)
