package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

interface LineItemRepository {
    fun getLineItemsForInvoiceStream(invoiceId: Int): Flow<List<LineItem>>
    fun getLineItemStream(lineItemId: Int): Flow<LineItem?>
    suspend fun insertLineItem(lineItem: LineItem): Long
    suspend fun deleteLineItem(lineItem: LineItem)
    suspend fun updateLineItem(lineItem: LineItem)
}
