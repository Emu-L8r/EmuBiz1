package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.PrefilledItem
import kotlinx.coroutines.flow.Flow

interface PrefilledItemRepository {
    fun getAllItems(): Flow<List<PrefilledItem>>
    suspend fun insertItem(item: PrefilledItem)
    suspend fun deleteItem(itemId: Long)
}
