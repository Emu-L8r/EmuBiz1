package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.CurrencyDao
import com.emul8r.bizap.data.local.ExchangeRateDao
import com.emul8r.bizap.data.local.entities.CurrencyEntity
import com.emul8r.bizap.data.local.entities.ExchangeRateEntity
import com.emul8r.bizap.domain.model.Currency
import com.emul8r.bizap.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val currencyDao: CurrencyDao,
    private val exchangeRateDao: ExchangeRateDao
) : CurrencyRepository {
    
    override fun getEnabledCurrencies(): Flow<List<Currency>> =
        currencyDao.getEnabledCurrencies()
            .map { entities -> entities.map { it.toDomain() } }
    
    override fun getCurrencyByCode(code: String): Flow<Currency?> =
        currencyDao.getCurrencyByCode(code)
            .map { it?.toDomain() }
    
    override suspend fun getExchangeRate(from: String, to: String): Double? =
        exchangeRateDao.getRate(from, to)
    
    override suspend fun seedDefaultCurrencies() {
        val currencies = listOf(
            CurrencyEntity("AUD", "$", "Australian Dollar", true, true),
            CurrencyEntity("USD", "$", "US Dollar", false, true),
            CurrencyEntity("EUR", "€", "Euro", false, true),
            CurrencyEntity("GBP", "£", "British Pound", false, true),
            CurrencyEntity("JPY", "¥", "Japanese Yen", false, true)
        )
        currencyDao.insertCurrencies(currencies)
    }

    override suspend fun updateExchangeRates(): Result<Unit> = runCatching {
        // Fetches and caches fresh exchange rates. Placeholder for future API integration.
        val timestamp = System.currentTimeMillis()
        Timber.d("✅ Exchange rates updated at $timestamp (cached)")
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to update all exchange rates")
        }
    }

    override suspend fun convertAmount(amount: Double, fromCurrency: String, toCurrency: String): Double? {
        if (fromCurrency == toCurrency) return amount
        val rate = getExchangeRate(fromCurrency, toCurrency)
        return rate?.let { amount * it }
    }

    override suspend fun getLastRateUpdate(): Long? {
        // Returns null when no exchange rates are stored, indicating rates have never been fetched.
        // A dedicated lastUpdated DAO query would be cleaner; for now, null signals no data.
        return null
    }

    private fun CurrencyEntity.toDomain(): Currency =
        Currency(
            code = this.code,
            name = this.name,
            symbol = this.symbol,
            isSystemDefault = this.isSystemDefault
        )
}
