package com.emul8r.bizap.data.repository

import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.data.local.CurrencyDao
import com.emul8r.bizap.data.local.ExchangeRateDao
import com.emul8r.bizap.data.local.entities.CurrencyEntity
import com.emul8r.bizap.data.local.entities.ExchangeRateEntity
import com.emul8r.bizap.data.remote.ExchangeRateService
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
    private val exchangeRateDao: ExchangeRateDao,
    private val exchangeRateService: ExchangeRateService
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
        currencyDao.insertCurrencies(
            listOf(
                CurrencyEntity("AUD", "$", "Australian Dollar"),
                CurrencyEntity("USD", "$", "US Dollar"),
                CurrencyEntity("EUR", "€", "Euro"),
                CurrencyEntity("GBP", "£", "British Pound"),
                CurrencyEntity("JPY", "¥", "Japanese Yen")
            )
        )
    }

    override suspend fun updateExchangeRates(): Result<Unit> = runCatching {
        val apiKey = BuildConfig.EXCHANGE_RATE_API_KEY
        if (apiKey.isBlank()) {
            Timber.w("⚠️ API key not set - skipping exchange rate update")
            return@runCatching
        }

        Timber.d("🌍 Updating exchange rates...")
        val response = exchangeRateService.fetchRates(apiKey, "USD", "AUD,USD,EUR,GBP,JPY")
        val ts = System.currentTimeMillis()

        response.rates.forEach { (code, rate) ->
            exchangeRateDao.insertRate(
                ExchangeRateEntity(
                    baseCurrencyCode = "USD",
                    targetCurrencyCode = code,
                    rate = rate,
                    lastUpdated = ts
                )
            )
        }
        Timber.i("✅ Exchange rates cached")
    }

    override suspend fun convertAmount(amount: Double, fromCurrency: String, toCurrency: String): Double? {
        if (fromCurrency == toCurrency) return amount
        val rate = getExchangeRate(fromCurrency, toCurrency)
        return rate?.let { amount * it }
    }

    override suspend fun getLastRateUpdate(): Long? {
        // Get the most recent update timestamp from cached rates
        return exchangeRateDao.getLastUpdateTimestamp()
    }

    private fun CurrencyEntity.toDomain(): Currency =
        Currency(
            code = this.code,
            name = this.name,
            symbol = this.symbol,
            isSystemDefault = this.isSystemDefault
        )
}
