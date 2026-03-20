package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.OfflineOperation

/**
 * Data Access Object for offline operations queue.
 * Part of Phase 2: Offline-First Reliability.
 */
@Dao
interface OfflineOperationDao {
    
    @Insert
    suspend fun insert(operation: OfflineOperation): Long
    
    @Update
    suspend fun update(operation: OfflineOperation)
    
    @Delete
    suspend fun delete(operation: OfflineOperation)
    
    @Query("SELECT * FROM offline_operations WHERE id = :id")
    suspend fun getById(id: Long): OfflineOperation?
    
    @Query("SELECT * FROM offline_operations WHERE business_profile_id = :businessId AND status = 'PENDING' ORDER BY timestamp_ms ASC")
    suspend fun getPendingOperations(businessId: Long): List<OfflineOperation>
    
    @Query("SELECT * FROM offline_operations WHERE business_profile_id = :businessId ORDER BY timestamp_ms DESC LIMIT 50")
    suspend fun getRecentOperations(businessId: Long): List<OfflineOperation>
    
    @Query("SELECT * FROM offline_operations WHERE status = 'FAILED' LIMIT 10")
    suspend fun getFailedOperations(): List<OfflineOperation>
    
    @Query("UPDATE offline_operations SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String)
    
    @Query("DELETE FROM offline_operations WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM offline_operations WHERE status = 'SYNCED' AND business_profile_id = :businessId")
    suspend fun deleteSuccessfullySyncedOperations(businessId: Long)
}
