package com.example.databaser.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineLineItemRepository @Inject constructor(private val lineItemDao: LineItemDao) : LineItemRepository {
    override fun getLineItemsForInvoiceStream(invoiceId: Int): Flow<List<LineItem>> = lineItemDao.getLineItemsForInvoice(invoiceId)

    override fun getLineItemStream(lineItemId: Int): Flow<LineItem?> = lineItemDao.getLineItemById(lineItemId)

    override suspend fun insertLineItem(lineItem: LineItem): Long = lineItemDao.insert(lineItem)

    override suspend fun deleteLineItem(lineItem: LineItem) = lineItemDao.delete(lineItem)

    override suspend fun updateLineItem(lineItem: LineItem) = lineItemDao.update(lineItem)
}
