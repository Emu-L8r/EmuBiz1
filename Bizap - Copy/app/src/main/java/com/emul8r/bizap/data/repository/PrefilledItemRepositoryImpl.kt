package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.PrefilledItemDao
import com.emul8r.bizap.data.local.entities.PrefilledItemEntity
import com.emul8r.bizap.domain.repository.PrefilledItemRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PrefilledItemRepositoryImpl @Inject constructor(
    private val dao: PrefilledItemDao
) : PrefilledItemRepository {
    override fun getAllItems(): Flow<List<PrefilledItemEntity>> = dao.getAllItems()
    override suspend fun insertItem(item: PrefilledItemEntity) = dao.insertItem(item)
    override suspend fun deleteItem(itemId: Long) = dao.deleteItem(itemId)
}
