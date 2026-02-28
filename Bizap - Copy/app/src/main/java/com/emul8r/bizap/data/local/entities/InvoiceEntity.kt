package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL // Keep invoice if customer is deleted
        )
    ],
    indices = [Index("customerId")]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val invoiceId: Long = 0,
    val customerId: Long?,
    val customerName: String, // Denormalized for historical accuracy
    val date: Long,
    val totalAmount: Double,
    val isQuote: Boolean,
    val status: String, // "DRAFT", "SENT", "PAID", "VOID"
    val photoUris: String, // Stored as JSON string via TypeConverter
    val header: String? = null,
    val subheader: String? = null,
    val notes: String? = null,
    val footer: String? = null,
    val pdfUri: String? = null // Store the internal file path here
)
