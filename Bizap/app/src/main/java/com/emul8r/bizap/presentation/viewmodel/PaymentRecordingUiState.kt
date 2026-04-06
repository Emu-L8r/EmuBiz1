package com.emul8r.bizap.presentation.viewmodel

import com.emul8r.bizap.data.local.entities.PaymentMethod

/**
 * UI state for the payment recording screen.
 *
 * Centralized state management for form inputs, validation, and submission status.
 */
data class PaymentRecordingUiState(
    val amountInput: String = "",
    val selectedMethod: PaymentMethod = PaymentMethod.ACH_TRANSFER,
    val notesInput: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val paymentRecorded: Boolean = false
)
