package com.emul8r.bizap.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emul8r.bizap.BuildConfig
import com.emul8r.bizap.data.local.ExchangeRateDao
import com.emul8r.bizap.data.local.entities.ExchangeRateEntity
import com.emul8r.bizap.data.remote.ExchangeRateService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class ExchangeRateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val exchangeRateService: ExchangeRateService,
    private val exchangeRateDao: ExchangeRateDao
) : CoroutineWorker(appContext, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("🌍 Syncing exchange rates from API...")
            
            val appId = BuildConfig.EXCHANGE_RATE_API_KEY

            if (appId.isBlank()) {
                Timber.w("ExchangeRateWorker: No API key configured. Skipping rate sync.")
                return@withContext Result.success()
            }
            
            val response = exchangeRateService.fetchRates(
                appId = appId,
                base = "USD"
            )
            
            val currentTime = System.currentTimeMillis()
            val rateEntities = response.rates.map { (targetCurrency, rate) ->
                ExchangeRateEntity(
                    baseCurrencyCode = "USD", // Fixed for free tier
                    targetCurrencyCode = targetCurrency,
                    rate = rate,
                    lastUpdated = currentTime
                )
            }
            
            exchangeRateDao.insertRates(rateEntities)
            
            // Cleanup: Keep only last 30 days of data
            val thirtyDaysAgo = currentTime - (30L * 24 * 60 * 60 * 1000)
            exchangeRateDao.deleteOldRates(thirtyDaysAgo)
            
            Timber.d("✅ Exchange rates synced successfully.")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to sync rates: ${e.message}")
            Result.failure()
        }
    }
}
