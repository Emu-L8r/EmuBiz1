package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "line_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["invoiceId"],
            childColumns = ["parentInvoiceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("parentInvoiceId")]
)
data class LineItemEntity(
    @PrimaryKey(autoGenerate = true) val itemId: Long = 0,
    val parentInvoiceId: Long,
    val description: String,
    val quantity: Double,
    val unitPrice: Double
) {
    val total: Double get() = quantity * unitPrice
}
