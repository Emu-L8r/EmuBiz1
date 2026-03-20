package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.Currency
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    fun getEnabledCurrencies(): Flow<List<Currency>>
    fun getCurrencyByCode(code: String): Flow<Currency?>
    suspend fun getExchangeRate(from: String, to: String): Double?
    suspend fun seedDefaultCurrencies()
    
    // NEW for Stage 2C
    suspend fun updateExchangeRates(): Result<Unit>
    suspend fun convertAmount(amount: Double, fromCurrency: String, toCurrency: String): Double?
    suspend fun getLastRateUpdate(): Long?
}
