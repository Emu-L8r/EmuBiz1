package com.emul8r.bizap.domain.model

/**
 * RESULT SEALED CLASS - Railway-Oriented Programming
 *
 * This is a functional alternative to throwing exceptions for EXPECTED errors.
 * Instead of crashing on validation failures, we return a Result that either
 * contains successful data or an error message.
 *
 * WHY THIS PATTERN?
 * ================
 * 1. Explicit: Caller MUST handle both success and failure cases
 * 2. Type-safe: Compiler enforces error handling (unlike exceptions)
 * 3. Composable: Can chain operations with map(), flatMap(), etc.
 * 4. Functional: Enables Railway-Oriented Programming
 * 5. Testable: Easy to test both success and failure paths
 *
 * WHEN TO USE:
 * ===========
 * - Validation failures (expected, user can fix)
 * - Network timeouts (expected, retry-able)
 * - Business logic failures (expected, user-facing)
 *
 * WHEN NOT TO USE:
 * ===============
 * - Unexpected errors (bugs, use exceptions)
 * - Null reference exceptions (use Optional/null-safety)
 * - Out of memory (use exceptions)
 *
 * RAILWAY ANALOGY:
 * ===============
 *  ┌─ Success Track ──→ Success Track ──→ Success Track ──→ Final Success
 *  │
 *  ├─ Failure Track ──→ Failure Track ──→ Failure Track ──→ Final Failure
 *  │
 *  Each operation either continues on success track or switches to failure track.
 *  Once on failure track, you stay there until the end.
 */
sealed class Result<T> {

    // ===============================
    // DATA CLASSES
    // ===============================

    /**
     * Success<T> - Contains successful data
     *
     * Example:
     *   Result.Success(invoice)
     *   Result.Success(unit)  // For operations with no return value
     */
    data class Success<T>(val data: T) : Result<T>()

    /**
     * Failure<T> - Contains error message, no data
     *
     * Example:
     *   Result.Failure<Unit>("Customer name is required")
     *   Result.Failure<Invoice>("Invoice must have at least one item")
     */
    data class Failure<T>(val error: String) : Result<T>()

    // ===============================
    // FUNCTIONAL COMPOSITION
    // ===============================

    /**
     * MAP - Transform success data into a different type
     *
     * WHY MAP?
     * ========
     * Allows chaining transformations on the success path.
     * If this is a Failure, map() is skipped (stays on failure track).
     *
     * EXAMPLE:
     * --------
     * val invoiceResult: Result<Invoice> = createInvoice()
     *
     * val pdgResult: Result<String> = invoiceResult.map { invoice ->
     *     generatePdf(invoice)  // Only runs if invoiceResult is Success
     * }
     *
     * If invoiceResult was Failure("No customer"),
     * pdfResult is also Failure("No customer") - generatePdf never runs.
     */
    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(this.data))
        is Failure -> Failure(this.error)
    }

    /**
     * FLATMAP - Chain operations that return Result
     *
     * WHY FLATMAP?
     * ============
     * When transformation itself returns a Result.
     * Automatically flattens nested Results into one.
     *
     * EXAMPLE:
     * --------
     * val invoiceResult: Result<Invoice> = validateInvoice(invoice)
     *
     * val savedResult: Result<Invoice> = invoiceResult.flatMap { validInvoice ->
     *     saveInvoice(validInvoice)  // Also returns Result<Invoice>
     * }
     *
     * This automatically unwraps both Results.
     *
     * WITHOUT FLATMAP (what NOT to do):
     * ----------------------------------
     * val result: Result<Result<Invoice>> = invoiceResult.map { validInvoice ->
     *     saveInvoice(validInvoice)  // Returns Result<Invoice>, nested!
     * }  // ❌ Nested Results are hard to work with
     */
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = when (this) {
        is Success -> transform(this.data)
        is Failure -> Failure(this.error)
    }

    /**
     * RECOVER - Use a default value on failure
     *
     * WHY RECOVER?
     * ============
     * Sometimes you want to continue despite an error,
     * using a fallback value instead of the error.
     *
     * EXAMPLE:
     * --------
     * val taxRate: Result<Double> = getTaxRate()
     * val actualRate: Result<Double> = taxRate.recover { error ->
     *     0.1  // Default to 10% if tax rate fetch fails
     * }
     */
    inline fun recover(fallback: (String) -> T): Result<T> = when (this) {
        is Success -> this
        is Failure -> Success(fallback(this.error))
    }

    // ===============================
    // EXTRACTION HELPERS
    // ===============================

    /**
     * GET - Extract success data or throw exception
     *
     * USE: When you want to crash if there's an error
     *      (rare, but sometimes correct at top level)
     *
     * EXAMPLE:
     * --------
     * val invoice = result.get()  // Throws if Failure
     */
    fun get(): T = when (this) {
        is Success -> data
        is Failure -> throw IllegalStateException("Result is Failure: $error")
    }

    /**
     * GET_OR_NULL - Extract data or return null
     *
     * USE: When you want null on failure
     *
     * EXAMPLE:
     * --------
     * val invoice: Invoice? = result.getOrNull()
     * if (invoice == null) {
     *     // Handle failure
     * }
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Failure -> null
    }

    /**
     * GET_ERROR_OR_NULL - Extract error or return null
     *
     * USE: When you want to see what went wrong
     *
     * EXAMPLE:
     * --------
     * val error: String? = result.getErrorOrNull()
     * if (error != null) {
     *     Timber.e("Validation failed: $error")
     * }
     */
    fun getErrorOrNull(): String? = when (this) {
        is Success -> null
        is Failure -> error
    }

    /**
     * IS_SUCCESS - Check if this is Success
     *
     * EXAMPLE:
     * --------
     * if (result.isSuccess()) {
     *     proceed()
     * } else {
     *     showError(result.getErrorOrNull())
     * }
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * IS_FAILURE - Check if this is Failure
     *
     * EXAMPLE:
     * --------
     * if (result.isFailure()) {
     *     showError(result.getErrorOrNull())
     * }
     */
    fun isFailure(): Boolean = this is Failure
}

// ===============================
// EXTENSION FUNCTIONS
// ===============================

/**
 * FOLD - Pattern match on both Success and Failure
 *
 * WHY FOLD?
 * =========
 * Instead of writing:
 *   if (result is Success) { ... }
 *   else if (result is Failure) { ... }
 *
 * You write:
 *   result.fold(
 *       onSuccess = { data -> ... },
 *       onFailure = { error -> ... }
 *   )
 *
 * Much cleaner and less error-prone.
 */
inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (String) -> R
): R = when (this) {
    is Result.Success -> onSuccess(this.data)
    is Result.Failure -> onFailure(this.error)
}

/**
 * ON_SUCCESS - Execute action only if Success
 *
 * WHY?
 * ====
 * For side effects without changing the result.
 * Chain multiple onSuccess calls.
 *
 * EXAMPLE:
 * --------
 * validateInvoice(invoice)
 *     .onSuccess { Timber.d("✅ Invoice valid: ${it.id}") }
 *     .flatMap { saveInvoice(it) }
 *     .onSuccess { Timber.d("✅ Invoice saved") }
 *     .onFailure { Timber.e("❌ Save failed: $it") }
 */
inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> = apply {
    if (this is Result.Success) {
        action(this.data)
    }
}

/**
 * ON_FAILURE - Execute action only if Failure
 *
 * WHY?
 * ====
 * For side effects without changing the result.
 * Log errors, show toast messages, etc.
 */
inline fun <T> Result<T>.onFailure(action: (String) -> Unit): Result<T> = apply {
    if (this is Result.Failure) {
        action(this.error)
    }
}

