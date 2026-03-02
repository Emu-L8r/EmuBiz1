package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emul8r.bizap.data.local.entities.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT rate FROM exchange_rates WHERE baseCurrencyCode = :base AND targetCurrencyCode = :target ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getRate(base: String, target: String): Double?
    
    @Query("SELECT * FROM exchange_rates WHERE baseCurrencyCode = :base ORDER BY lastUpdated DESC")
    fun getRatesForBase(base: String): Flow<List<ExchangeRateEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRate(rate: ExchangeRateEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRates(rates: List<ExchangeRateEntity>)
    
    @Query("DELETE FROM exchange_rates WHERE lastUpdated < :timestamp")
    suspend fun deleteOldRates(timestamp: Long)
}

