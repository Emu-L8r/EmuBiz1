package com.emul8r.bizap.ui.gui2.invoices

/**
 * UI state for edit invoice screen.
 */
data class EditInvoiceUiState(
    val invoiceId: Long = 0L,
    val invoiceNumber: String = "",
    val customerName: String = "",
    val totalAmount: Long = 0L,
    val isSubmitting: Boolean = false,
    val generalError: String? = null,
    val invoiceUpdated: Boolean = false
)

