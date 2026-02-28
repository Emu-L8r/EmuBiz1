package com.emul8r.bizap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emul8r.bizap.data.local.entities.PrefilledItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrefilledItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PrefilledItemEntity)

    @Query("SELECT * FROM prefilled_items ORDER BY description ASC")
    fun getAllItems(): Flow<List<PrefilledItemEntity>>

    @Query("DELETE FROM prefilled_items WHERE id = :itemId")
    suspend fun deleteItem(itemId: Long)
}
