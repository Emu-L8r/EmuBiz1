package com.emul8r.bizap.data.local.entities

/**
 * Query result model for invoice count grouped by status.
 * Returned by InvoiceDaoV2.observeInvoiceCountByStatus().
 */
data class InvoiceStatusCountV2(
    val status: String,  // e.g. "PAID", "SENT", "OVERDUE", "DRAFT", "PARTIALLY_PAID"
    val count: Int
)
