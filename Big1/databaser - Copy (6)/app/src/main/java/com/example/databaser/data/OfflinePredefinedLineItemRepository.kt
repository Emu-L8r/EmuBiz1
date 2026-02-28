package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

class OfflinePredefinedLineItemRepository(private val predefinedLineItemDao: PredefinedLineItemDao) : PredefinedLineItemRepository {
    override fun getAllPredefinedLineItemsStream(): Flow<List<PredefinedLineItem>> = predefinedLineItemDao.getAllPredefinedLineItems()

    override suspend fun insertPredefinedLineItem(predefinedLineItem: PredefinedLineItem) = predefinedLineItemDao.insert(predefinedLineItem)

    override suspend fun deletePredefinedLineItem(predefinedLineItem: PredefinedLineItem) = predefinedLineItemDao.delete(predefinedLineItem)

    override suspend fun updatePredefinedLineItem(predefinedLineItem: PredefinedLineItem) = predefinedLineItemDao.update(predefinedLineItem)
}
