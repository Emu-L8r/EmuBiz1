package com.emu.emubizwax.domain.usecase

import com.emu.emubizwax.domain.model.Invoice
import com.emu.emubizwax.domain.model.LineItem
import com.emu.emubizwax.domain.repository.InvoiceRepository
import javax.inject.Inject

class SaveInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(invoice: Invoice, items: List<LineItem>) {
        if (invoice.customerId == 0L) {
            throw Exception("A customer must be selected.")
        }
        if (items.isEmpty() || items.all { it.description.isBlank() }) {
            throw Exception("An invoice must have at least one line item.")
        }
        repository.upsertInvoice(invoice, items)
    }
}
