package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emul8r.bizap.data.local.entities.CurrencyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Query("SELECT * FROM currencies WHERE isEnabled = 1 ORDER BY code")
    fun getEnabledCurrencies(): Flow<List<CurrencyEntity>>
    
    @Query("SELECT * FROM currencies WHERE code = :code")
    fun getCurrencyByCode(code: String): Flow<CurrencyEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrency(currency: CurrencyEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)
    
    @Query("SELECT * FROM currencies")
    fun getAllCurrencies(): Flow<List<CurrencyEntity>>
}

