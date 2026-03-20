package com.emul8r.bizap.ui.invoices

import com.emul8r.bizap.domain.model.LineItem
import kotlin.math.roundToLong

fun LineItemForm.toDomain(): LineItem {
    return LineItem(
        id = this.id ?: 0L,
        description = this.description,
        quantity = this.quantity,
        unitPrice = this.unitPrice
    )
}

fun LineItemForm.calculateTotal(): Long {
    val exactTotal = this.unitPrice.toDouble() * this.quantity
    return exactTotal.roundToLong()
}
