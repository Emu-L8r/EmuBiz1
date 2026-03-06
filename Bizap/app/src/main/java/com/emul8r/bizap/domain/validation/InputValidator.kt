package com.emul8r.bizap.domain.validation

/**
 * Centralized input validator for all user-facing form fields.
 *
 * Returns [ValidationResult] so callers can display per-field error messages in the UI
 * without coupling to a specific UI framework.
 *
 * This complements [ValidationRules], which validates fully-constructed domain objects.
 * Use [InputValidator] at the form/ViewModel boundary (before building domain objects)
 * and [ValidationRules] at the domain/repository boundary.
 */
object InputValidator {

    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )
    private val PHONE_REGEX = Regex("^[0-9\\s\\-+()]{7,20}$")
    private val INVOICE_NUMBER_REGEX = Regex("^[A-Za-z0-9\\-_/]+$")

    // ── Invoice number ───────────────────────────────────────────────────────────

    fun validateInvoiceNumber(num: String): ValidationResult<String> = when {
        num.isBlank() ->
            ValidationResult.Failure("Invoice number is required")
        num.length > 50 ->
            ValidationResult.Failure("Invoice number must be 50 characters or less")
        !num.matches(INVOICE_NUMBER_REGEX) ->
            ValidationResult.Failure("Invoice number contains invalid characters")
        else -> ValidationResult.Success(num)
    }

    // ── Email ────────────────────────────────────────────────────────────────────

    fun validateEmail(email: String): ValidationResult<String> = when {
        email.isBlank() ->
            ValidationResult.Failure("Email is required")
        !email.matches(EMAIL_REGEX) ->
            ValidationResult.Failure("Invalid email format")
        email.length > 254 ->
            ValidationResult.Failure("Email is too long")
        else -> ValidationResult.Success(email)
    }

    // ── Customer name ────────────────────────────────────────────────────────────

    fun validateCustomerName(name: String): ValidationResult<String> = when {
        name.isBlank() ->
            ValidationResult.Failure("Customer name is required")
        name.length < 2 ->
            ValidationResult.Failure("Name must be at least 2 characters")
        name.length > 100 ->
            ValidationResult.Failure("Name must be 100 characters or less")
        else -> ValidationResult.Success(name)
    }

    // ── Phone (optional) ─────────────────────────────────────────────────────────

    fun validatePhone(phone: String?): ValidationResult<String?> = when {
        phone == null || phone.isBlank() ->
            ValidationResult.Success(null)
        !phone.matches(PHONE_REGEX) ->
            ValidationResult.Failure("Invalid phone number format")
        else -> ValidationResult.Success(phone)
    }

    // ── Amount (in cents) ────────────────────────────────────────────────────────

    fun validateAmount(amount: Long): ValidationResult<Long> = when {
        amount <= 0 ->
            ValidationResult.Failure("Amount must be greater than zero")
        amount > 999_999_999 ->
            ValidationResult.Failure("Amount is too large")
        else -> ValidationResult.Success(amount)
    }

    // ── Quantity ─────────────────────────────────────────────────────────────────

    fun validateQuantity(qty: Double): ValidationResult<Double> = when {
        qty <= 0 ->
            ValidationResult.Failure("Quantity must be greater than zero")
        qty > 99_999 ->
            ValidationResult.Failure("Quantity is too large")
        else -> ValidationResult.Success(qty)
    }

    // ── Tax rate (0–100 %) ───────────────────────────────────────────────────────

    fun validateTaxRate(rate: Double): ValidationResult<Double> = when {
        rate < 0 ->
            ValidationResult.Failure("Tax rate cannot be negative")
        rate > 100 ->
            ValidationResult.Failure("Tax rate cannot exceed 100%")
        else -> ValidationResult.Success(rate)
    }
}

// ── Result type ──────────────────────────────────────────────────────────────────

sealed class ValidationResult<out T> {
    data class Success<T>(val value: T) : ValidationResult<T>()
    data class Failure<T>(val message: String) : ValidationResult<T>()
}

// ── Extension helpers ────────────────────────────────────────────────────────────

fun <T> ValidationResult<T>.getOrNull(): T? = (this as? ValidationResult.Success)?.value
fun <T> ValidationResult<T>.getErrorOrNull(): String? = (this as? ValidationResult.Failure)?.message
fun <T> ValidationResult<T>.isSuccess(): Boolean = this is ValidationResult.Success
fun <T> ValidationResult<T>.isFailure(): Boolean = this is ValidationResult.Failure
