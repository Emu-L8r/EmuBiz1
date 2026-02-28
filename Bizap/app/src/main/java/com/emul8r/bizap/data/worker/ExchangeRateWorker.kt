package com.emul8r.bizap.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.emul8r.bizap.data.local.ExchangeRateDao
import com.emul8r.bizap.data.local.entities.ExchangeRateEntity
import com.emul8r.bizap.data.remote.ExchangeRateService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ExchangeRateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val exchangeRateService: ExchangeRateService,
    private val exchangeRateDao: ExchangeRateDao
) : CoroutineWorker(appContext, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d("ExchangeRateWorker", "🌍 Syncing exchange rates from API...")
            
            // Replace with your actual App ID from openexchangerates.org
            val appId = "00000000000000000000000000000000" 
            
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
            
            Log.d("ExchangeRateWorker", "✅ Exchange rates synced successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("ExchangeRateWorker", "❌ Failed to sync rates: ${e.message}")
            Result.retry()
        }
    }
}
