package com.emul8r.bizap.ui.invoices

import com.emul8r.bizap.domain.model.LineItem

fun LineItemForm.toDomain(): LineItem {
    return LineItem(
        id = this.id ?: 0L,
        description = this.description,
        quantity = this.quantity,
        unitPrice = this.unitPrice
    )
}

