package com.emu.emubizwax.data.mapper

import com.emu.emubizwax.data.local.entities.LineItemEntity
import com.emu.emubizwax.domain.model.LineItem
import javax.inject.Inject

class LineItemMapper @Inject constructor() {

    fun toDomain(entity: LineItemEntity): LineItem {
        return LineItem(
            id = entity.id,
            invoiceId = entity.invoiceId,
            description = entity.description,
            quantity = entity.quantity,
            unitPrice = entity.unitPrice
        )
    }

    fun toEntity(domain: LineItem): LineItemEntity {
        return LineItemEntity(
            id = domain.id,
            invoiceId = domain.invoiceId,
            description = domain.description,
            quantity = domain.quantity,
            unitPrice = domain.unitPrice
        )
    }
}
