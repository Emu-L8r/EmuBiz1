package com.emul8r.bizap.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LineItem(
    val id: Long = 0,
    val description: String = "",
    val quantity: Int = 1,
    val unitPrice: Double = 0.0,
    val notes: String = ""
) {
    val total: Double get() = quantity * unitPrice
}

