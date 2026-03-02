package com.emul8r.bizap.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey val baseCurrencyCode: String, // e.g., "AUD"
    val targetCurrencyCode: String,           // e.g., "USD"
    val rate: Double,                         // e.g., 0.65
    val lastUpdated: Long = System.currentTimeMillis()
)

