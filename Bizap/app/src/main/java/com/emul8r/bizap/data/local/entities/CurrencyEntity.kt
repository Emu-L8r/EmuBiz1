package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey val code: String, // e.g., "AUD"
    val symbol: String,           // e.g., "$"
    val name: String,             // e.g., "Australian Dollar"
    val isSystemDefault: Boolean = false,
    val isEnabled: Boolean = true // Added for Stage 2B
)

