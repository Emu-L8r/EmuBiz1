package com.example.databaser.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PredefinedLineItemDao {
    @Query("SELECT * FROM predefined_line_items")
    fun getAllPredefinedLineItems(): Flow<List<PredefinedLineItem>>

    @Query("SELECT * FROM predefined_line_items")
    suspend fun getAllPredefinedLineItemsList(): List<PredefinedLineItem>

    @Insert
    suspend fun insert(predefinedLineItem: PredefinedLineItem)

    @Insert
    suspend fun insertAll(predefinedLineItems: List<PredefinedLineItem>)

    @Update
    suspend fun update(predefinedLineItem: PredefinedLineItem)

    @Delete
    suspend fun delete(predefinedLineItem: PredefinedLineItem)

    @Query("DELETE FROM predefined_line_items")
    suspend fun deleteAll()
}
