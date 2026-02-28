package com.emu.emubizwax.ui.invoices

import android.net.Uri
import com.emu.emubizwax.domain.model.Customer
import java.util.UUID

data class InvoiceFormState(
    val selectedCustomer: Customer? = null,
    val date: Long = System.currentTimeMillis(),
    val items: List<LineItem> = listOf(LineItem()), // Start with one empty row
    val taxRate: Double = 0.10,
    val isQuote: Boolean = false,
    val photoUris: List<Uri> = emptyList()
) {
    val subtotal: Double get() = items.sumOf { it.quantity * it.price }
    val taxAmount: Double get() = subtotal * taxRate
    val grandTotal: Double get() = subtotal + taxAmount
}

data class LineItem(
    val id: String = UUID.randomUUID().toString(),
    val description: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
)
