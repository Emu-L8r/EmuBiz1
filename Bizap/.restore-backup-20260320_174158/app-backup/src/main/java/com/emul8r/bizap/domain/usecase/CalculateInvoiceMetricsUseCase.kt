package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceMetrics
import com.emul8r.bizap.domain.model.calculateTotal
import javax.inject.Inject

/**
 * Single source of truth for all invoice calculation logic.
 * Used by both GUI1 and GUI2 to ensure consistent results.
 *
 * CALCULATION FORMULA:
 * 1. subtotal = sum(lineItem.unitPrice * lineItem.quantity) for all items
 * 2. taxAmount = subtotal * taxRate (if taxRate > 0)
 * 3. totalAmount = subtotal + taxAmount - discountAmount
 */
class CalculateInvoiceMetricsUseCase @Inject constructor() {

    operator fun invoke(invoice: Invoice): InvoiceMetrics {
        // 1. Calculate subtotal
        val subtotal = invoice.items.sumOf { it.calculateTotal() }

        // 2. Calculate tax (taxRate > 0 indicates a tax-registered business)
        val taxAmount = if (invoice.taxRate > 0) {
            (subtotal.toDouble() * invoice.taxRate).toLong()
        } else {
            0L
        }

        // TODO: Implement discount calculation when Invoice model is extended with a discountAmount field
        val discountAmount = 0L

        // 3. Calculate total
        val totalAmount = subtotal + taxAmount - discountAmount

        return InvoiceMetrics(
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount
        )
    }
}
