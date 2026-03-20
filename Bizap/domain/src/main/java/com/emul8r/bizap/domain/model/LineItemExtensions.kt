package com.emul8r.bizap.domain.model

import kotlin.math.roundToLong

fun LineItem.calculateTotal(): Long {
    val exactTotal = this.unitPrice.toDouble() * this.quantity
    return exactTotal.roundToLong()
}

fun LineItem.calculateTotalAsDouble(): Double {
    return this.unitPrice.toDouble() * this.quantity
}

fun LineItem.isValid(): Boolean {
    return quantity > 0 && unitPrice > 0 && description.isNotBlank()
}
