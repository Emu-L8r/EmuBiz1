package com.emul8r.bizap.data.local.entities

import androidx.room.Entity

@Entity(
    tableName = "exchange_rates",
    primaryKeys = ["baseCurrencyCode", "targetCurrencyCode"]
)
data class ExchangeRateEntity(
    val baseCurrencyCode: String,
    val targetCurrencyCode: String,
    val rate: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
