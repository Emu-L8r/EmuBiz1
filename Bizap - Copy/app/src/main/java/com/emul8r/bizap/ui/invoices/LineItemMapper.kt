package com.emul8r.bizap.ui.invoices

import com.emul8r.bizap.data.local.entities.LineItemEntity
import com.emul8r.bizap.domain.model.LineItem
import javax.inject.Inject

class LineItemMapper @Inject constructor() {

    fun toEntity(lineItem: LineItem, parentId: Long): LineItemEntity {
        return LineItemEntity(
            itemId = lineItem.id ?: 0L,
            parentInvoiceId = parentId,
            description = lineItem.description,
            quantity = lineItem.quantity,
            unitPrice = lineItem.unitPrice
        )
    }

    fun toForm(entity: LineItemEntity): LineItemForm {
        return LineItemForm(
            id = entity.itemId,
            description = entity.description,
            quantity = entity.quantity,
            unitPrice = entity.unitPrice
        )
    }
}
