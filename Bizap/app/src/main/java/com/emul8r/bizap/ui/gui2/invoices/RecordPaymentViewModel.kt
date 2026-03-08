package com.emul8r.bizap.ui.gui2.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.usecase.RecordPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for [RecordPaymentDialogV2].
 *
 * Manages form state for recording a payment: amount, date, notes.
 * Validates inputs in real-time and delegates persistence to [RecordPaymentUseCase].
 */
@HiltViewModel
class RecordPaymentViewModel @Inject constructor(
    private val recordPaymentUseCase: RecordPaymentUseCase
) : ViewModel() {

    // ── Invoice context (set by the screen before showing the dialog) ──────────

    private var invoiceId: Long = -1L
    private var businessId: Long = -1L
    private var invoiceTotal: Long = 0L
    private var invoiceDate: Long = 0L

    // ── UI state ───────────────────────────────────────────────────────────────

    private val _formState = MutableStateFlow(PaymentFormState())
    val formState: StateFlow<PaymentFormState> = _formState.asStateFlow()

    private val _events = MutableSharedFlow<PaymentEvent>()
    val events: SharedFlow<PaymentEvent> = _events.asSharedFlow()

    // ── Initialisation ─────────────────────────────────────────────────────────

    /**
     * Binds this ViewModel to a specific invoice. Must be called before the dialog
     * is shown. Safe to call multiple times (re-initialises form state).
     */
    fun initFor(
        invoiceId: Long,
        businessId: Long,
        invoiceTotal: Long,
        amountPaid: Long,
        invoiceDate: Long
    ) {
        this.invoiceId = invoiceId
        this.businessId = businessId
        this.invoiceTotal = invoiceTotal
        this.invoiceDate = invoiceDate

        val outstanding = (invoiceTotal - amountPaid).coerceAtLeast(0L)
        _formState.value = PaymentFormState(
            outstanding = outstanding,
            paymentDate = todayMidnightMs()
        )
        Timber.d("RecordPaymentViewModel: initFor invoice=$invoiceId outstanding=$outstanding")
    }

    // ── Field change handlers ──────────────────────────────────────────────────

    fun onAmountChanged(raw: String) {
        _formState.update { current ->
            val amountCents = raw.toDoubleOrNull()?.let { (it * 100).toLong() }
            val amountError = amountErrorMessage(amountCents, current.outstanding)
            current.copy(
                amountRaw = raw,
                amountCents = amountCents,
                amountError = amountError,
                isFormValid = amountError == null &&
                    amountCents != null &&
                    amountCents > 0 &&
                    dateErrorMessage(current.paymentDate) == null
            )
        }
    }

    fun onDateChanged(dateMs: Long) {
        _formState.update { current ->
            val dateError = dateErrorMessage(dateMs)
            current.copy(
                paymentDate = dateMs,
                dateError = dateError,
                isFormValid = current.amountError == null &&
                    current.amountCents != null &&
                    current.amountCents > 0 &&
                    dateError == null
            )
        }
    }

    fun onNotesChanged(notes: String) {
        _formState.update { it.copy(notes = notes.take(500)) }
    }

    // ── Submission ─────────────────────────────────────────────────────────────

    fun submit() {
        val state = _formState.value
        if (invoiceId == -1L) {
            // ViewModel not yet initialised; should not happen in normal usage
            Timber.w("RecordPaymentViewModel.submit() called before initFor()")
            return
        }
        if (!state.isFormValid || state.amountCents == null) return

        _formState.update { it.copy(isLoading = true, submissionError = null) }

        viewModelScope.launch {
            val result = recordPaymentUseCase(
                invoiceId = invoiceId,
                businessId = businessId,
                amount = state.amountCents,
                trueOutstanding = state.outstanding,
                paymentDate = state.paymentDate,
                invoiceDate = invoiceDate,
                notes = state.notes.ifBlank { null }
            )

            _formState.update { it.copy(isLoading = false) }

            result.fold(
                onSuccess = {
                    Timber.d("RecordPaymentViewModel: payment submitted successfully")
                    _events.emit(PaymentEvent.Success)
                },
                onFailure = { error ->
                    val msg = error.message ?: "Payment failed"
                    Timber.e(error, "RecordPaymentViewModel: payment failed")
                    _formState.update { it.copy(submissionError = msg) }
                    _events.emit(PaymentEvent.Error(msg))
                }
            )
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun amountErrorMessage(amountCents: Long?, outstanding: Long): String? = when {
        amountCents == null -> "Enter a valid amount"
        amountCents <= 0 -> "Amount must be greater than \$0"
        amountCents > outstanding -> "Payment exceeds the outstanding balance"
        else -> null
    }

    private fun dateErrorMessage(dateMs: Long): String? {
        // Normalize both sides to midnight so today is always valid (date-level comparison only)
        val todayMidnight = todayMidnightMs()
        return when {
            dateMs > todayMidnight -> "Payment date cannot be in the future"
            invoiceDate > 0 && dateMs < invoiceDate -> "Payment date cannot be before the invoice date"
            else -> null
        }
    }

    private fun todayMidnightMs(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

// ── Data classes ───────────────────────────────────────────────────────────────

/**
 * Immutable snapshot of the payment form's current state.
 */
data class PaymentFormState(
    /** Remaining balance of the invoice in cents. */
    val outstanding: Long = 0L,
    /** Raw string the user typed into the amount field. */
    val amountRaw: String = "",
    /** Parsed amount in cents, or null if the raw string is not a valid number. */
    val amountCents: Long? = null,
    /** Field-level error for the amount input, or null if valid. */
    val amountError: String? = null,
    /** Selected payment date as a Unix timestamp (ms). Defaults to today. */
    val paymentDate: Long = System.currentTimeMillis(),
    /** Field-level error for the date picker, or null if valid. */
    val dateError: String? = null,
    /** Optional payment notes (max 500 chars). */
    val notes: String = "",
    /** True when all fields pass validation and the form can be submitted. */
    val isFormValid: Boolean = false,
    /** True while the use-case coroutine is in-flight. */
    val isLoading: Boolean = false,
    /** Non-null when the last submission attempt returned an error. Cleared on next submit. */
    val submissionError: String? = null
)

/**
 * One-shot UI events emitted after a submission attempt.
 */
sealed class PaymentEvent {
    data object Success : PaymentEvent()
    data class Error(val message: String) : PaymentEvent()
}
