package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emul8r.bizap.data.local.entities.PendingOperation
import kotlinx.coroutines.flow.Flow

/**
 * DAO for managing the offline pending operations queue.
 */
@Dao
interface PendingOperationDao {
    
    @Insert
    suspend fun insertOperation(operation: PendingOperation): Long
    
    @Update
    suspend fun updateOperation(operation: PendingOperation)
    
    @Delete
    suspend fun deleteOperation(operation: PendingOperation)
    
    @Query("SELECT * FROM pending_operations WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<PendingOperation>
    
    @Query("SELECT * FROM pending_operations WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun observePendingOperations(): Flow<List<PendingOperation>>
    
    @Query("SELECT COUNT(*) FROM pending_operations WHERE status = 'PENDING'")
    fun observePendingCount(): Flow<Int>
    
    @Query("UPDATE pending_operations SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)
}

