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
 * 2. discountAmount = invoice.discountAmount (in cents, clamped to ≥ 0)
 * 3. taxAmount = (subtotal - discountAmount) * taxRate (if taxRate > 0)
 * 4. totalAmount = (subtotal - discountAmount) + taxAmount
 */
class CalculateInvoiceMetricsUseCase @Inject constructor() {

    operator fun invoke(invoice: Invoice): InvoiceMetrics {
        // 1. Calculate subtotal
        val subtotal = invoice.items.sumOf { it.calculateTotal() }

        // 2. Calculate discount
        val discountAmount = invoice.discountAmount.coerceAtLeast(0L)

        // 3. Calculate tax on discounted subtotal (discount is applied before tax)
        val discountedSubtotal = (subtotal - discountAmount).coerceAtLeast(0L)
        val taxAmount = if (invoice.taxRate > 0) {
            (discountedSubtotal.toDouble() * invoice.taxRate).toLong()
        } else {
            0L
        }

        // 4. Calculate total
        val totalAmount = discountedSubtotal + taxAmount

        return InvoiceMetrics(
            subtotal = subtotal,
            taxAmount = taxAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount
        )
    }
}
