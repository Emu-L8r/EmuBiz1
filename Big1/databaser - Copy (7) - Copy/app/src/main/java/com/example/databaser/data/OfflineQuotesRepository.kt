package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

class OfflineQuotesRepository(private val quoteDao: QuoteDao, private val lineItemDao: LineItemDao) : QuoteRepository {
    override fun getAllQuotesStream(): Flow<List<Quote>> = quoteDao.getAllQuotes()

    override fun getQuoteStream(id: Int): Flow<Quote?> = quoteDao.getQuote(id)

    override suspend fun insertQuote(quote: Quote): Long = quoteDao.insert(quote)

    override suspend fun deleteQuote(quote: Quote) = quoteDao.delete(quote)

    override suspend fun updateQuote(quote: Quote) = quoteDao.update(quote)

    override fun getLineItemsForQuote(quoteId: Int): Flow<List<LineItem>> = lineItemDao.getLineItemsForQuote(quoteId)
}