package com.example.databaser.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Insert
    suspend fun insert(quote: Quote): Long

    @Update
    suspend fun update(quote: Quote)

    @Delete
    suspend fun delete(quote: Quote)

    @Query("DELETE FROM quotes")
    suspend fun deleteAll()

    @Query("SELECT * from quotes WHERE id = :id")
    fun getQuote(id: Int): Flow<Quote?>

    @Query("SELECT * from quotes ORDER BY date DESC")
    fun getAllQuotes(): Flow<List<Quote>>
}
