package com.emul8r.bizap.ui.invoices

data class LineItemUiModel(
    val originalId: Long,
    val type: String, // e.g., "INV" or "QT"
    val clientName: String,
    val date: String,
    val counter: Int
) {
    val stableId: String = "$type-$clientName-$date-${counter.toString().padStart(3, '0')}"
}
