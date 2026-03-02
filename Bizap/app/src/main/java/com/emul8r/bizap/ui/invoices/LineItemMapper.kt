package com.emul8r.bizap.ui.invoices

import com.emul8r.bizap.data.local.entities.LineItemEntity
import com.emul8r.bizap.domain.model.LineItem
import javax.inject.Inject

class LineItemMapper @Inject constructor() {

    fun toEntity(lineItem: LineItem, parentId: Long): LineItemEntity {
        return LineItemEntity(
            id = lineItem.id,
            invoiceId = parentId,
            description = lineItem.description,
            quantity = lineItem.quantity,
            unitPrice = lineItem.unitPrice
        )
    }

    fun toForm(entity: LineItemEntity): LineItemForm {
        return LineItemForm(
            id = entity.id,
            description = entity.description,
            quantity = entity.quantity,
            unitPrice = entity.unitPrice
        )
    }
}

