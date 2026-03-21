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

            // Check if API key is configured
            if (appId.isBlank()) {
                val error = ExchangeRateErrorHandler.ApiError.MissingApiKey
                ExchangeRateErrorHandler.logError(error)
                
                // Graceful degradation: Check if cached rates are available
                val cachedRates = exchangeRateDao.getAllRates()
                if (cachedRates.isNotEmpty()) {
                    val mostRecent = cachedRates.maxByOrNull { it.lastUpdated }
                    val acceptable = ExchangeRateErrorHandler.areCachedRatesAcceptable(mostRecent?.lastUpdated)
                    
                    if (acceptable) {
                        Timber.i("✅ Using cached rates (${cachedRates.size} rates available)")
                        return@withContext Result.success()
                    } else {
                        Timber.w("⚠️ Cached rates are stale (>7 days old)")
                    }
                }
                
                // No API key and no acceptable cached rates
                Timber.w("⚠️ No API key and no cached rates available")
                return@withContext Result.success() // Don't fail - app can still function
            }
            
            // Fetch fresh rates from API
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
            
            Timber.d("✅ Exchange rates synced successfully (${rateEntities.size} rates updated)")
            Result.success()
        } catch (e: Exception) {
            // Classify and log error with appropriate context
            val error = ExchangeRateErrorHandler.classifyError(e)
            ExchangeRateErrorHandler.logError(error)
            
            // Check if cached rates can be used as fallback
            val cachedRates = exchangeRateDao.getAllRates()
            if (cachedRates.isNotEmpty()) {
                val mostRecent = cachedRates.maxByOrNull { it.lastUpdated }
                val acceptable = ExchangeRateErrorHandler.areCachedRatesAcceptable(mostRecent?.lastUpdated)
                
                if (acceptable) {
                    Timber.i("✅ Falling back to cached rates (${cachedRates.size} rates, ${getDaysOld(mostRecent?.lastUpdated)} days old)")
                    
                    // Return success with retry if error is retryable
                    return@withContext if (error.isRetryable) {
                        Timber.d("🔄 Will retry rate sync on next scheduled run")
                        Result.retry()
                    } else {
                        Result.success()
                    }
                } else {
                    Timber.w("⚠️ Cached rates too old (>7 days), but will still use them")
                    // Still use stale cached rates rather than failing
                    return@withContext if (error.isRetryable) Result.retry() else Result.success()
                }
            } else {
                Timber.w("⚠️ No cached rates available - first sync failed")
                // Retry if error is retryable, otherwise succeed to avoid blocking WorkManager queue
                return@withContext if (error.isRetryable) Result.retry() else Result.success()
            }
        }
    }
    
    /**
     * Calculate age of cached rates in days
     */
    private fun getDaysOld(lastUpdateMillis: Long?): Int {
        if (lastUpdateMillis == null) return -1
        val ageInMillis = System.currentTimeMillis() - lastUpdateMillis
        return (ageInMillis / (24 * 60 * 60 * 1000)).toInt()
    }
}
