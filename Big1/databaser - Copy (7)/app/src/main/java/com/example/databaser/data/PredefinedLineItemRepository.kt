package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

interface PredefinedLineItemRepository {
    fun getAllPredefinedLineItemsStream(): Flow<List<PredefinedLineItem>>
    suspend fun insertPredefinedLineItem(predefinedLineItem: PredefinedLineItem)
    suspend fun deletePredefinedLineItem(predefinedLineItem: PredefinedLineItem)
    suspend fun updatePredefinedLineItem(predefinedLineItem: PredefinedLineItem)
}
