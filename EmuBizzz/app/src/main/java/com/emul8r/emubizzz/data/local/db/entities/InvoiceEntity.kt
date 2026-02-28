package com.emul8r.emubizzz.data.local.db.entities

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
            onDelete = ForeignKey.CASCADE // If customer is deleted, invoices follow
        )
    ],
    indices = [Index("customerId")]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val invoiceId: Long = 0,
    val customerId: Long,
    val date: Long,
    val totalAmount: Double,
    val isQuote: Boolean,
    val status: String, // "DRAFT", "SENT", "PAID"
    val photoUris: String // Stored as JSON string via TypeConverter
)
