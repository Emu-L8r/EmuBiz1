package com.example.databaser.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "invoices", indices = [Index(value = ["customerId"]), Index(value = ["date"])])
data class Invoice(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val date: Long,
    val invoiceNumber: String,
    val dueDate: Long,
    val header: String? = null,
    val subHeader: String? = null,
    val isPaid: Boolean = false,
    val isSent: Boolean = false,
    val isQuote: Boolean = false,
    val isHidden: Boolean = false,
    val photoUris: List<String> = emptyList()
)
