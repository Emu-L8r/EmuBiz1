package com.emu.emubizwax.data.local.entities

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
            onDelete = ForeignKey.RESTRICT // Prevent deleting customer if invoices exist
        )
    ],
    indices = [Index("customerId")]
)
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val date: Long, // Timestamp
    val status: String, // Draft, Sent, Paid
    val isQuote: Boolean, // Toggle between Invoice vs Quote
    val taxRate: Double = 0.10, // Default 10%
    val photoUris: List<String> = emptyList() // Requires TypeConverter
)
