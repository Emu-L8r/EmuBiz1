package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getAllQuotesStream(): Flow<List<Quote>>

    fun getQuoteStream(id: Int): Flow<Quote?>

    suspend fun insertQuote(quote: Quote): Long

    suspend fun deleteQuote(quote: Quote)

    suspend fun updateQuote(quote: Quote)

    fun getLineItemsForQuote(quoteId: Int): Flow<List<LineItem>>
}
