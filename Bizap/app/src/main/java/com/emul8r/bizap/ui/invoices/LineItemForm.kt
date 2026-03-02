package com.emul8r.bizap.ui.invoices

import androidx.compose.runtime.Stable
import java.util.UUID

@Stable
data class LineItemForm(
    val id: Long? = null, // NULL for new items, Long for existing
    val transientId: UUID = UUID.randomUUID(), // Unique key for unsaved items
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Long = 0   // Cents (e.g., 4999 = $49.99)
)
