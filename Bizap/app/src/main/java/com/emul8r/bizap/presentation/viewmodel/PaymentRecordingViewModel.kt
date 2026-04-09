package com.emul8r.bizap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.PaymentMethod
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.Money
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.usecase.RecordPaymentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

/**
 * ViewModel for recording invoice payments.
 *
 * **Responsibilities:**
 * - Manage form input state (amount, method, notes)
 * - Validate payment amount using Money value object
 * - Persist payment to database via repository
 * - Update invoice status (PAID/PARTIALLY_PAID)
 * - Handle typed exceptions with specific error messages
 *
 * **Architecture:**
 * - UI layer (RecordPaymentScreenV2) handles only rendering
 * - Business logic delegated here and to UseCase layer
 * - StateFlow exposes UI state reactively
 * - Money value object ensures no rounding errors
 */
@HiltViewModel
class PaymentRecordingViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val recordPaymentUseCase: RecordPaymentUseCase
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
     * - Amount is converted to Money value object (cents-based, no rounding errors)
     *
     * **Error Handling:**
     * - ValidationException: User input error (show retry)
     * - DatabaseException: Storage error (show "Try again")
     * - NetworkException: Connectivity error (show "Check connection")
     * - Other exceptions: Generic error (show troubleshoot)
     *
     * **Side Effects:**
     * - Saves payment to database
     * - Updates invoice status
     * - Sets paymentRecorded flag on success
     */
    fun recordPayment(invoiceId: Long) {
        val currentState = _uiState.value

        // Parse input and validate
        val moneyResult = parseAndValidateAmount(currentState.amountInput)
        if (moneyResult is ParseResult.Error) {
            _uiState.update { it.copy(errorMessage = moneyResult.message) }
            return
        }

        val amount = (moneyResult as ParseResult.Success).money

        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                Timber.d(
                    "Recording payment for invoice $invoiceId: " +
                    "amount=${amount.toDollars()}, " +
                    "method=${currentState.selectedMethod}, " +
                    "notes='${currentState.notesInput}'"
                )

                // Fetch invoice to get business ID, outstanding balance, dates, and status
                val invoice = invoiceRepository.getInvoiceWithItemsById(invoiceId).first()
                    ?: throw DatabaseException("Invoice $invoiceId not found")

                val trueOutstanding = invoice.totalAmount - invoice.amountPaid
                val paymentDate = todayMidnightMs()

                val result = recordPaymentUseCase(
                    invoiceId = invoiceId,
                    businessId = invoice.businessProfileId,
                    amount = amount.amountCents,
                    trueOutstanding = trueOutstanding,
                    paymentDate = paymentDate,
                    invoiceDate = invoice.date,
                    invoiceStatus = invoice.status,
                    notes = currentState.notesInput.takeIf { it.isNotBlank() }
                )

                if (result.isFailure) {
                    val error = result.exceptionOrNull()
                    throw ValidationException(
                        error?.message ?: "Payment validation failed", error
                    )
                }

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        paymentRecorded = true,
                        amountInput = "",
                        notesInput = ""
                    )
                }
                Timber.i("✅ Payment recorded successfully for invoice $invoiceId: ${amount.toDollars()}")
            } catch (e: ValidationException) {
                Timber.w(e, "Validation error recording payment for invoice $invoiceId")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Payment validation failed."
                    )
                }
            } catch (e: DatabaseException) {
                Timber.e(e, "Database error recording payment for invoice $invoiceId")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Database error. Please try again."
                    )
                }
            } catch (e: NetworkException) {
                Timber.e(e, "Network error recording payment for invoice $invoiceId")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "Network error. Check your connection."
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Unexpected error recording payment for invoice $invoiceId")
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = "An unexpected error occurred. Please contact support."
                    )
                }
            }
        }
    }

    /**
     * Parse string input and validate as money amount.
     * Returns Money value object (cents-based) to avoid floating-point errors.
     */
    private fun parseAndValidateAmount(input: String): ParseResult {
        if (input.isBlank()) {
            return ParseResult.Error("Please enter an amount")
        }

        val doubleAmount = input.toDoubleOrNull()
            ?: return ParseResult.Error("Invalid amount format")

        if (doubleAmount <= 0) {
            return ParseResult.Error("Amount must be greater than 0")
        }

        return try {
            val money = Money.fromDollars(doubleAmount)
            ParseResult.Success(money)
        } catch (e: IllegalArgumentException) {
            ParseResult.Error("Invalid amount: ${e.message}")
        }
    }

    private fun todayMidnightMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Sealed class for parsing results
     */
    private sealed class ParseResult {
        data class Success(val money: Money) : ParseResult()
        data class Error(val message: String) : ParseResult()
    }
}

/**
 * Domain-level exceptions for better error handling
 */
sealed class PaymentException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

class DatabaseException(message: String, cause: Throwable? = null) :
    PaymentException(message, cause)

class NetworkException(message: String, cause: Throwable? = null) :
    PaymentException(message, cause)

class ValidationException(message: String, cause: Throwable? = null) :
    PaymentException(message, cause)






