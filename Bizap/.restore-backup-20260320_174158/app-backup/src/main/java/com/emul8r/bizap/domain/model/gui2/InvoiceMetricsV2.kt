package com.emul8r.bizap.domain.model.gui2

/**
 * Invoice count metrics for the GUI2 dashboard.
 * Tracks total invoices sent plus paid/pending breakdown.
 */
data class InvoiceMetricsV2(
    val businessProfileId: Long,
    val totalInvoices: Int,    // All invoices excluding DRAFT
    val paidCount: Int,        // Invoices with PAID status
    val pendingCount: Int      // Invoices with SENT status (awaiting payment)
)
