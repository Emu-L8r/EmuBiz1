package com.emul8r.bizap.domain.error

/**
 * BizapException - Sealed class for all app errors
 *
 * WHY SEALED CLASS?
 * =================
 * Instead of generic Exception, we use sealed class to:
 *
 * 1. TYPE SAFETY
 *    ❌ Generic: throw Exception("Some error")  // What error? Unclear!
 *    ✅ Sealed: throw ValidationError(field = "email")  // Clear what failed!
 *
 * 2. EXHAUSTIVE WHEN
 *    ❌ Generic: when(e) { is Exception -> ... }  // Covers everything
 *    ✅ Sealed: when(e) { is ValidationError -> ...; is NetworkError -> ... }
 *              // Compiler ensures ALL error types handled
 *
 * 3. PATTERN MATCHING
 *    Can destructure error data in when statement:
 *    ✅ when(e) {
 *       is ValidationError -> showFieldError(e.field, e.message)
 *       is NetworkError -> retryWithBackoff(e.retryCount)
 *    }
 *
 * 4. LOGGING CONTEXT
 *    Each error type carries relevant data for logging:
 *    ✅ NetworkError knows: statusCode, retryCount, endpoint
 *    ✅ ValidationError knows: field, actualValue, validationRule
 *
 * 5. USER MESSAGING
 *    Can map each error type to appropriate user message:
 *    ✅ ValidationError -> "Please fix: {field}"
 *    ✅ NetworkError -> "Connection problem, retrying..."
 *    ✅ DatabaseError -> "Failed to save, try again"
 */
sealed class BizapException(
    message: String = "An unknown error occurred",
    cause: Throwable? = null
) : Exception(message, cause) {

    // ============================================
    // VALIDATION ERRORS - User input is invalid
    // ============================================

    /**
     * ValidationError - User input doesn't meet requirements
     *
     * WHEN TO THROW:
     * - User enters invalid email format
     * - Customer name is blank
     * - Invoice total is zero
     * - Due date is before invoice date
     *
     * @param field Which field failed validation (e.g., "email", "customerName")
     * @param message Why it failed (e.g., "Invalid email format")
     * @param actualValue What user entered (for logging)
     * @param validationRule What was required (e.g., "Must contain @")
     *
     * EXAMPLE:
     *   throw ValidationError(
     *     field = "email",
     *     message = "Email must contain @ symbol",
     *     actualValue = "notanemail",
     *     validationRule = "Must match email regex"
     *   )
     */
    data class ValidationError(
        val field: String,
        override val message: String,
        val actualValue: String? = null,
        val validationRule: String? = null
    ) : BizapException(message)

    /**
     * InvalidInvoiceError - Invoice has logical errors
     *
     * WHEN TO THROW:
     * - No line items but trying to save
     * - Total amount doesn't match sum of items
     * - Customer deleted after selection
     *
     * @param reason What's wrong with the invoice
     * @param invoiceId Which invoice (if already exists)
     * @param requiredFix What user needs to do
     *
     * EXAMPLE:
     *   throw InvalidInvoiceError(
     *     reason = "Invoice has no line items",
     *     requiredFix = "Add at least one line item before saving"
     *   )
     */
    data class InvalidInvoiceError(
        val reason: String,
        val invoiceId: Long? = null,
        val requiredFix: String? = null,
        override val message: String = "Invalid invoice: $reason"
    ) : BizapException(message)

    // ============================================
    // DATABASE ERRORS - Data persistence failure
    // ============================================

    /**
     * DatabaseError - Room database operation failed
     *
     * WHEN TO THROW:
     * - Failed to save invoice to database
     * - Failed to query customers
     * - Database is corrupted
     * - Migration failed
     *
     * @param operation What operation failed (e.g., "INSERT", "SELECT")
     * @param table Which table (e.g., "invoices")
     * @param details Error details
     *
     * EXAMPLE:
     *   throw DatabaseError(
     *     operation = "INSERT",
     *     table = "invoices",
     *     details = "Constraint violation: foreign key"
     *   )
     */
    data class DatabaseError(
        val operation: String,
        val table: String,
        val details: String
    ) : BizapException("Database $operation on $table failed: $details")

    /**
     * MigrationError - Database schema migration failed
     *
     * WHEN TO THROW:
     * - v21→22 migration fails on app startup
     * - Data conversion during migration fails
     * - Schema validation fails after migration
     *
     * @param fromVersion Schema version migrating from
     * @param toVersion Schema version migrating to
     * @param reason Why migration failed
     *
     * EXAMPLE:
     *   throw MigrationError(
     *     fromVersion = 23,
     *     toVersion = 24,
     *     reason = "Type conversion failed for invoice_payments.amountPaid"
     *   )
     */
    data class MigrationError(
        val fromVersion: Int,
        val toVersion: Int,
        val reason: String,
        override val message: String = "Migration $fromVersion→$toVersion failed: $reason"
    ) : BizapException(message)

    // ============================================
    // NETWORK ERRORS - API call or connectivity
    // ============================================

    /**
     * NetworkError - HTTP request failed or connectivity issue
     *
     * WHEN TO THROW:
     * - Exchange rate API is unreachable
     * - HTTP 500 server error
     * - Timeout waiting for response
     * - No internet connection
     *
     * @param endpoint Which API endpoint (e.g., "/api/rates")
     * @param statusCode HTTP status (200-599) or null if no response
     * @param message Error details
     * @param retryCount How many times we've already retried
     * @param isRetryable Whether calling code should retry
     *
     * EXAMPLE:
     *   throw NetworkError(
     *     endpoint = "https://openexchangerates.org/api/latest",
     *     statusCode = 503,
     *     message = "Service temporarily unavailable",
     *     isRetryable = true
     *   )
     */
    data class NetworkError(
        val endpoint: String,
        val statusCode: Int? = null,
        override val message: String,
        val retryCount: Int = 0,
        val isRetryable: Boolean = true
    ) : BizapException("Network error at $endpoint: $message")

    /**
     * TimeoutError - Request took too long
     *
     * WHEN TO THROW:
     * - Exchange rate API didn't respond in 30 seconds
     * - Database query is hanging
     *
     * @param endpoint Which operation timed out
     * @param timeoutMs How long we waited (in milliseconds)
     *
     * EXAMPLE:
     *   throw TimeoutError(
     *     endpoint = "POST /api/sync",
     *     timeoutMs = 30000
     *   )
     */
    data class TimeoutError(
        val endpoint: String,
        val timeoutMs: Long,
        override val message: String = "Request to $endpoint timed out after ${timeoutMs}ms"
    ) : BizapException(message)

    /**
     * ConnectivityError - No internet connection
     *
     * WHEN TO THROW:
     * - Device is in airplane mode
     * - WiFi/cellular is off
     * - Check before making network requests
     *
     * EXAMPLE:
     *   throw ConnectivityError(
     *     message = "No internet connection. App is offline."
     *   )
     */
    data class ConnectivityError(
        override val message: String = "No internet connection"
    ) : BizapException(message)

    // ============================================
    // FILE ERRORS - PDF/document generation
    // ============================================

    /**
     * FileError - Failed to read/write file
     *
     * WHEN TO THROW:
     * - PDF generation failed
     * - Can't read logo image
     * - Can't write to Downloads folder
     *
     * @param operation What failed (e.g., "PDF_GENERATION", "LOGO_READ")
     * @param filePath Which file
     * @param reason Why it failed
     *
     * EXAMPLE:
     *   throw FileError(
     *     operation = "PDF_GENERATION",
     *     filePath = "/storage/emulated/0/Documents/invoice_123.pdf",
     *     reason = "No write permission to Documents folder"
     *   )
     */
    data class FileError(
        val operation: String,
        val filePath: String,
        val reason: String,
        override val message: String = "File operation $operation failed on $filePath: $reason"
    ) : BizapException(message)

    /**
     * StorageError - Device storage issues
     *
     * WHEN TO THROW:
     * - Device storage is full
     * - Can't create temp directory
     * - Insufficient space for PDF
     *
     * @param message Why storage operation failed
     *
     * EXAMPLE:
     *   throw StorageError(
     *     message = "Device storage is full. Free up space to save PDFs."
     *   )
     */
    data class StorageError(
        override val message: String = "Insufficient storage space"
    ) : BizapException(message)

    // ============================================
    // BUSINESS LOGIC ERRORS - Violates app rules
    // ============================================

    /**
     * BusinessLogicError - Action violates business rules
     *
     * WHEN TO THROW:
     * - Trying to edit invoice after it's locked
     * - Trying to mark paid more than invoice total
     * - Customer deleted but invoice still references it
     *
     * @param rule Which business rule was violated
     * @param action What user tried to do
     * @param reason Why it's not allowed
     *
     * EXAMPLE:
     *   throw BusinessLogicError(
     *     rule = "Cannot modify locked invoices",
     *     action = "Edit invoice in PAID status",
     *     reason = "Invoice has been paid and is locked from changes"
     *   )
     */
    data class BusinessLogicError(
        val rule: String,
        val action: String,
        val reason: String,
        override val message: String = "Business rule violated: $reason"
    ) : BizapException(message)

    /**
     * DuplicateError - Entity already exists
     *
     * WHEN TO THROW:
     * - Customer with same email already exists
     * - Invoice number is not unique
     *
     * @param entityType What kind of entity (e.g., "Customer", "Invoice")
     * @param identifier What makes it unique (e.g., email, invoice number)
     * @param existingId ID of the existing entity
     *
     * EXAMPLE:
     *   throw DuplicateError(
     *     entityType = "Customer",
     *     identifier = "john@example.com",
     *     existingId = 42
     *   )
     */
    data class DuplicateError(
        val entityType: String,
        val identifier: String,
        val existingId: Long,
        override val message: String = "$entityType with '$identifier' already exists"
    ) : BizapException(message)

    /**
     * NotFoundError - Entity doesn't exist
     *
     * WHEN TO THROW:
     * - Trying to open invoice that was deleted
     * - Customer ID doesn't exist
     *
     * @param entityType What we're looking for (e.g., "Invoice", "Customer")
     * @param identifier What we searched for (e.g., ID, name)
     *
     * EXAMPLE:
     *   throw NotFoundError(
     *     entityType = "Invoice",
     *     identifier = "123"
     *   )
     */
    data class NotFoundError(
        val entityType: String,
        val identifier: String,
        override val message: String = "$entityType '$identifier' not found"
    ) : BizapException(message)

    // ============================================
    // UNKNOWN/UNEXPECTED ERRORS
    // ============================================

    /**
     * UnknownError - Something unexpected happened
     *
     * WHEN TO THROW:
     * - Catch-all for unexpected exceptions
     * - Wrap unchecked exceptions (e.g., NullPointerException)
     * - Fallback when specific error type is unclear
     *
     * @param message What happened
     * @param originalException The underlying exception
     * @param context What the app was doing (e.g., "Loading invoices")
     *
     * EXAMPLE:
     *   try {
     *     // Some code
     *   } catch (e: Throwable) {
     *     throw UnknownError(
     *       message = "Unexpected error while loading invoices",
     *       originalException = e,
     *       context = "InvoiceListViewModel.onCreate()"
     *     )
     *   }
     */
    data class UnknownError(
        override val message: String,
        val originalException: Throwable? = null,
        val context: String? = null
    ) : BizapException(message, originalException)
}

/**
 * Helper extension function to check if error is retryable
 *
 * Use in retry logic:
 *   if (exception is BizapException && exception.isRetryable()) {
 *     retryWithBackoff()
 *   }
 */
fun BizapException.isRetryable(): Boolean = when (this) {
    // Network errors are retryable if marked as such
    is BizapException.NetworkError -> this.isRetryable
    is BizapException.TimeoutError -> true  // Always retry timeouts
    is BizapException.ConnectivityError -> true  // Retry when connection restored

    // These should never be retried (user input problem)
    is BizapException.ValidationError -> false
    is BizapException.InvalidInvoiceError -> false

    // These might be retryable (temporary database issue)
    is BizapException.DatabaseError -> this.operation in listOf("SELECT", "INSERT", "UPDATE")
    is BizapException.MigrationError -> false  // Never retry migrations

    // File errors usually not retryable (permission issue)
    is BizapException.FileError -> false
    is BizapException.StorageError -> true  // Might be retryable if user frees space

    // Business logic errors not retryable (rule violation)
    is BizapException.BusinessLogicError -> false
    is BizapException.DuplicateError -> false
    is BizapException.NotFoundError -> false

    // Unknown: be conservative and don't retry
    is BizapException.UnknownError -> false
}

/**
 * Helper extension function to get error severity
 *
 * Use to decide how to display error:
 *   when (exception.severity()) {
 *     ErrorSeverity.CRITICAL -> showErrorDialog()
 *     ErrorSeverity.WARNING -> showSnackbar()
 *     ErrorSeverity.INFO -> logOnly()
 *   }
 */
enum class ErrorSeverity {
    /** Critical: User action failed, data may be lost, must inform user */
    CRITICAL,

    /** High: User action failed, important data, must inform user */
    HIGH,

    /** Medium: Operation partially failed, should inform user */
    MEDIUM,

    /** Low: Informational, can be logged or shown quietly */
    LOW
}

fun BizapException.severity(): ErrorSeverity = when (this) {
    // Database and migration errors are always critical
    is BizapException.DatabaseError -> ErrorSeverity.CRITICAL
    is BizapException.MigrationError -> ErrorSeverity.CRITICAL

    // Validation errors are high (user action failed)
    is BizapException.ValidationError -> ErrorSeverity.HIGH
    is BizapException.InvalidInvoiceError -> ErrorSeverity.HIGH

    // Network errors depend on retry status
    is BizapException.NetworkError -> if (this.retryCount > 0) ErrorSeverity.MEDIUM else ErrorSeverity.LOW
    is BizapException.TimeoutError -> ErrorSeverity.MEDIUM
    is BizapException.ConnectivityError -> ErrorSeverity.MEDIUM

    // File errors are critical (data loss potential)
    is BizapException.FileError -> ErrorSeverity.CRITICAL
    is BizapException.StorageError -> ErrorSeverity.HIGH

    // Business logic errors are high (user needs to know)
    is BizapException.BusinessLogicError -> ErrorSeverity.HIGH
    is BizapException.DuplicateError -> ErrorSeverity.MEDIUM
    is BizapException.NotFoundError -> ErrorSeverity.HIGH

    // Unknown errors are critical (unexpected)
    is BizapException.UnknownError -> ErrorSeverity.CRITICAL
}

