package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.data.local.entities.PrefilledItemEntity
import kotlinx.coroutines.flow.Flow

interface PrefilledItemRepository {
    fun getAllItems(): Flow<List<PrefilledItemEntity>>
    suspend fun insertItem(item: PrefilledItemEntity)
    suspend fun deleteItem(itemId: Long)
}

