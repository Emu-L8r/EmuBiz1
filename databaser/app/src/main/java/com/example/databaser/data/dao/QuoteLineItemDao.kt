package com.example.databaser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.databaser.data.model.QuoteLineItem
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteLineItemDao {
    @Query("SELECT * FROM quote_line_items WHERE quoteId = :quoteId")
    fun getLineItemsForQuote(quoteId: Long): Flow<List<QuoteLineItem>>

    @Insert
    suspend fun insert(lineItem: QuoteLineItem): Long

    @Update
    suspend fun update(lineItem: QuoteLineItem)

    @Query("DELETE FROM quote_line_items WHERE id = :lineItemId")
    suspend fun delete(lineItemId: Long)
}
