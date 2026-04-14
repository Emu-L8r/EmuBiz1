package com.emul8r.bizap.domain.model

import kotlin.math.roundToLong

fun InvoiceItem.calculateTotal(): Long {
    val exactTotal = this.unitPrice.toDouble() * this.quantity
    return exactTotal.roundToLong()
}

fun InvoiceItem.calculateTotalAsDouble(): Double {
    return this.unitPrice.toDouble() * this.quantity
}

fun InvoiceItem.isValid(): Boolean {
    return quantity > 0 && unitPrice > 0 && description.isNotBlank()
}
