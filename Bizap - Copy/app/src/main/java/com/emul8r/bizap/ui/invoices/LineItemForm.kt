package com.emul8r.bizap.ui.invoices

import androidx.compose.runtime.Stable

@Stable
data class LineItemForm(
    val id: Long? = null, // NULL for new items, Long for existing
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0
)
