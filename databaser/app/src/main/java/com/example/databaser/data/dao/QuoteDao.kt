package com.example.databaser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.databaser.data.model.Quote
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Insert
    suspend fun insert(quote: Quote): Long

    @Update
    suspend fun update(quote: Quote)

    @Query("SELECT * FROM quotes")
    fun getAllQuotes(): Flow<List<Quote>>

    @Query("SELECT * FROM quotes WHERE id = :quoteId")
    fun getQuoteById(quoteId: Long): Flow<Quote>

    @Query("SELECT * FROM quotes WHERE customerId = :customerId")
    fun getQuotesForCustomer(customerId: Long): Flow<List<Quote>>

    @Query("DELETE FROM quotes WHERE id = :quoteId")
    suspend fun delete(quoteId: Long)

    @Query("SELECT COUNT(*) FROM quotes")
    fun getQuoteCount(): Flow<Int>
}
