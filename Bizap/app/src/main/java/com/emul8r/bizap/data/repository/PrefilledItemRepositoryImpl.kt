package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.PrefilledItemDao
import com.emul8r.bizap.data.local.entities.PrefilledItemEntity
import com.emul8r.bizap.domain.model.PrefilledItem
import com.emul8r.bizap.domain.repository.PrefilledItemRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrefilledItemRepositoryImpl @Inject constructor(
    private val dao: PrefilledItemDao
) : PrefilledItemRepository {

    private fun PrefilledItemEntity.toDomain(): PrefilledItem = PrefilledItem(id = id, description = description, unitPrice = unitPrice)
    private fun PrefilledItem.toEntity(): PrefilledItemEntity = PrefilledItemEntity(id = id, description = description, unitPrice = unitPrice)

    override fun getAllItems(): Flow<List<PrefilledItem>> = dao.getAllItems().map { list -> list.map { it.toDomain() } }
    override suspend fun insertItem(item: PrefilledItem) = dao.insertItem(item.toEntity())
    override suspend fun deleteItem(itemId: Long) = dao.deleteItem(itemId)
}
