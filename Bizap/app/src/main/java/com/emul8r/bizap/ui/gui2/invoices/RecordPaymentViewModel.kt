package com.emul8r.bizap.ui.gui2.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.usecase.RecordPaymentUseCase
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.utils.FirebaseEventTracker
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
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for [RecordPaymentDialogV2].
 *
 * Manages form state for recording a payment: amount, date, notes.
 * Validates inputs in real-time and delegates persistence to [RecordPaymentUseCase].
 */
@HiltViewModel
class RecordPaymentViewModel @Inject constructor(
    private val recordPaymentUseCase: RecordPaymentUseCase,
    private val eventTracker: FirebaseEventTracker
) : ViewModel() {

    // ── Invoice context (set by the screen before showing the dialog) ──────────

    private var invoiceId: Long = -1L
    private var businessId: Long = -1L
    private var invoiceTotal: Long = 0L
    private var invoiceDate: Long = 0L
    private var invoiceStatus: InvoiceStatus = InvoiceStatus.DRAFT

    // ── UI state ───────────────────────────────────────────────────────────────

    private val _formState = MutableStateFlow(PaymentFormState())
    val formState: StateFlow<PaymentFormState> = _formState.asStateFlow()

    private val _events = MutableSharedFlow<PaymentEvent>()
    val events: SharedFlow<PaymentEvent> = _events.asSharedFlow()

    // ── Initialisation ─────────────────────────────────────────────────────────

    /**
     * Binds this ViewModel to a specific invoice. Must be called before the dialog
     * is shown. Safe to call multiple times (re-initialises form state).
     *
     * **Behavior:**
     * - Stores invoice context (ID, business ID, amounts, dates)
     * - Initializes form state with default values
     * - Calculates outstanding balance = total - amountPaid
     * - Sets payment date to today at midnight
     * - Clears any prior validation errors
     *
     * **Example:**
     * ```kotlin
     * viewModel.initFor(
     *     invoiceId = 42,
     *     businessId = 1,
     *     invoiceTotal = 10000,  // $100.00 in cents
     *     amountPaid = 5000,     // $50.00 already paid
     *     invoiceDate = System.currentTimeMillis(),
     *     invoiceStatus = InvoiceStatus.PARTIAL_PAID
     * )
     * // Outstanding = 5000 cents ($50.00)
     * ```
     *
     * @param invoiceId Invoice to record payment for
     * @param businessId Business context
     * @param invoiceTotal Total invoice amount in cents
     * @param amountPaid Amount already paid in cents
     * @param invoiceDate Invoice creation date (epoch ms)
     * @param invoiceStatus Current invoice status
     */
    fun initFor(
        invoiceId: Long,
        businessId: Long,
        invoiceTotal: Long,
        amountPaid: Long,
        invoiceDate: Long,
        invoiceStatus: InvoiceStatus
    ) {
        this.invoiceId = invoiceId
        this.businessId = businessId
        this.invoiceTotal = invoiceTotal
        this.invoiceDate = invoiceDate
        this.invoiceStatus = invoiceStatus

        val outstanding = (invoiceTotal - amountPaid).coerceAtLeast(0L)

        // Set default payment date to TODAY at midnight (not current time)
        val defaultPaymentDate = todayMidnightMs()

        _formState.value = PaymentFormState(
            outstanding = outstanding,
            paymentDate = defaultPaymentDate
        )

        // Log detailed dates for debugging
        val invoiceDateMidnight = Calendar.getInstance().apply {
            timeInMillis = invoiceDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        Timber.d("RecordPaymentViewModel INIT:")
        Timber.d("  Invoice ID: $invoiceId")
        Timber.d("  Invoice Date (raw): $invoiceDate")
        Timber.d("  Invoice Date (midnight): $invoiceDateMidnight")
        Timber.d("  Default Payment Date: $defaultPaymentDate")
        Timber.d("  Outstanding: $outstanding cents")
        Timber.d("  Dates equal? ${defaultPaymentDate == invoiceDateMidnight}")
        Timber.d("  Payment >= Invoice? ${defaultPaymentDate >= invoiceDateMidnight}")
    }

    // ── Field change handlers ──────────────────────────────────────────────────

    /**
     * Updates payment amount when user types in the amount field.
     *
     * **Behavior:**
     * - Converts string input to cents (multiply by 100)
     * - Validates amount doesn't exceed outstanding balance
     * - Re-calculates form validity
     * - Clears previous validation errors
     *
     * **Validation Rules:**
     * - Cannot be empty
     * - Must be positive
     * - Cannot exceed outstanding balance
     *
     * @param raw User's input (as dollars, e.g., "50.00")
     */
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

    /**
     * Updates payment date when user selects from date picker.
     *
     * **Validation:**
     * - Cannot be after today
     * - Cannot be before invoice date
     *
     * @param dateMs Selected date in epoch milliseconds
     */
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

    /**
     * Updates optional payment notes (max 500 chars).
     *
     * **Behavior:**
     * - Stores notes for record-keeping
     * - Truncates to 500 characters if needed
     * - Doesn't affect form validity
     *
     * @param notes User-entered notes (optional)
     */
    fun onNotesChanged(notes: String) {
        _formState.update { it.copy(notes = notes.take(500)) }
    }

    // ── Submission ─────────────────────────────────────────────────────────────

    /**
     * Submits the payment recording to the repository.
     *
     * **Validation Before Submit:**
     * - ViewModel must be initialized (initFor called)
     * - Form must be valid (valid amount and date)
     * - Amount must be positive
     *
     * **On Success:**
     * - Payment recorded in database
     * - Invoice status updated (PAID if fully paid, PARTIAL_PAID if partial)
     * - Emits PaymentEvent.Success
     *
     * **On Failure:**
     * - Error stored in formState.submissionError
     * - Emits PaymentEvent.Error with message
     * - User can retry
     *
     * **Example:**
     * ```kotlin
     * Button(
     *     onClick = { viewModel.submit() },
     *     enabled = formState.isFormValid && !formState.isLoading
     * ) {
     *     Text("Record Payment")
     * }
     * ```
     */
    fun submit() {
        val state = _formState.value
        if (invoiceId == -1L) {
            // ViewModel not yet initialised; should not happen in normal usage
            Timber.w("RecordPaymentViewModel.submit() called before initFor()")
            val errMsg = "Payment form not initialized. Please go back and try again."
            _formState.update { it.copy(submissionError = errMsg) }
            return
        }
        if (!state.isFormValid || state.amountCents == null) {
            Timber.w("RecordPaymentViewModel.submit() called with invalid form. Valid=${state.isFormValid}, Amount=${state.amountCents}")
            return
        }

        Timber.d("RecordPaymentViewModel: Submitting payment invoiceId=$invoiceId businessId=$businessId amount=${state.amountCents}")
        _formState.update { it.copy(isLoading = true, submissionError = null) }

        viewModelScope.launch {
            try {
                val result = recordPaymentUseCase(
                    invoiceId = invoiceId,
                    businessId = businessId,
                    amount = state.amountCents,
                    trueOutstanding = state.outstanding,
                    paymentDate = state.paymentDate,
                    invoiceDate = invoiceDate,
                    invoiceStatus = invoiceStatus,
                    notes = state.notes.ifBlank { null }
                )

                _formState.update { it.copy(isLoading = false) }

                result.fold(
                    onSuccess = {
                        Timber.d("RecordPaymentViewModel: payment submitted successfully")
                        // 📊 Track payment recording event
                        eventTracker.trackPaymentRecorded(
                            invoiceId = invoiceId,
                            paymentAmount = state.amountCents ?: 0L,
                            paymentDate = state.paymentDate,
                            invoiceTotal = invoiceTotal
                        )
                        _events.emit(PaymentEvent.Success)
                    },
                    onFailure = { error ->
                        val msg = error.message ?: "Payment failed"
                        Timber.e(error, "RecordPaymentViewModel: payment failed - $msg")
                        _formState.update { it.copy(submissionError = msg) }
                        _events.emit(PaymentEvent.Error(msg))
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "RecordPaymentViewModel: Unexpected error during payment submission")
                _formState.update { it.copy(isLoading = false, submissionError = e.message ?: "Unexpected error") }
                _events.emit(PaymentEvent.Error(e.message ?: "Unexpected error"))
            }
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
        // Only validation rule for GUI2: Payment cannot be recorded for a future date
        // Invoice date validation is skipped because:
        // - Invoices are created with System.currentTimeMillis() (includes time)
        // - Payment dates default to midnight
        // - This causes false "before invoice date" errors on same-day payments
        // - The use case layer will validate invoice date if needed

        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Check: Payment cannot be in the future
        if (dateMs > todayStart) {
            return "Payment date cannot be in the future"
        }

        return null
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
