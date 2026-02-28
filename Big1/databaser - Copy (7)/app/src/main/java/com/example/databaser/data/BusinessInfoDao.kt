package com.example.databaser.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(businessInfo: BusinessInfo)

    @Query("SELECT * FROM business_info WHERE id = 1")
    fun getBusinessInfo(): Flow<BusinessInfo?>
}
