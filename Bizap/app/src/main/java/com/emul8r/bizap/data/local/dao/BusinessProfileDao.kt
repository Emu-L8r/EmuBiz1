package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.BusinessProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessProfileDao {
    @Query("SELECT * FROM business_profiles")
    fun getAllProfiles(): Flow<List<BusinessProfileEntity>>

    @Query("SELECT * FROM business_profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): BusinessProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BusinessProfileEntity): Long

    @Delete
    suspend fun deleteProfile(profile: BusinessProfileEntity)
}

