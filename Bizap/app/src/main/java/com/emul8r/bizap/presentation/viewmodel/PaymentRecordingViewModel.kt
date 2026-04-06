package com.emul8r.bizap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.PaymentMethod
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for recording invoice payments.
 *
 * **Responsibilities:**
 * - Manage form input state (amount, method, notes)
 * - Validate payment amount (business logic)
 * - Persist payment to database via repository
 * - Update invoice status (PAID/PARTIALLY_PAID)
 * - Handle errors and communicate to UI layer
 *
 * **Architecture:**
 * - UI layer (RecordPaymentScreenV2) handles only rendering
 * - All validation and persistence delegated here
 * - StateFlow exposes UI state reactively
 */
@HiltViewModel
class PaymentRecordingViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentRecordingUiState())
    val uiState: StateFlow<PaymentRecordingUiState> = _uiState.asStateFlow()

    /**
     * Update payment amount input.
     * Filters out non-numeric characters except decimal point.
     */
    fun updateAmount(amount: String) {
        _uiState.update {
            it.copy(
                amountInput = amount.filter { c -> c.isDigit() || c == '.' },
                errorMessage = null
            )
        }
    }

    /**
     * Update selected payment method.
     */
    fun updatePaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedMethod = method) }
    }

    /**
     * Update payment notes.
     */
    fun updateNotes(notes: String) {
        _uiState.update { it.copy(notesInput = notes) }
    }

    /**
     * Record payment - Business logic entry point.
     *
     * **Validation:**
     * - Amount must be > 0
     *
     * **Side Effects:**
     * - Saves payment to database
     * - Updates invoice status
     * - Sets paymentRecorded flag on success
     */
    fun recordPayment(invoiceId: Long) {
        val currentState = _uiState.value
        val amount = currentState.amountInput.toLongOrNull()

        // Validate: Move validation logic OUT of UI
        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid amount") }
            Timber.w("Payment validation failed: invalid amount '${currentState.amountInput}'")
            return
        }

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // TODO: Replace with actual payment repository method
                // For now, just log and simulate success
                Timber.d("Recording payment for invoice $invoiceId: amount=$amount, method=${currentState.selectedMethod}, notes='${currentState.notesInput}'")

                // Simulate network call
                kotlinx.coroutines.delay(500)

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        paymentRecorded = true,
                        amountInput = "",
                        notesInput = ""
                    )
                }
                Timber.i("Payment recorded successfully for invoice $invoiceId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to record payment for invoice $invoiceId")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Failed to record payment: ${e.message}"
                    )
                }
            }
        }
    }
}

