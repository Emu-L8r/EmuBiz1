package com.example.databaser.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_info")
data class BusinessInfo(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val address: String,
    val contactNumber: String,
    val email: String? = null,
    val logoPath: String? = null,
    val abn: String = "",
    val generalNotes: String? = null,
    val paymentDetails: String? = null,
    val dateFormat: String = "dd/MM/yyyy",
    val defaultDueDateDays: Int = 14,
    val currencySymbol: String = "$",
    val useDarkMode: Boolean = false,
    val theme: String = "Default"
)
