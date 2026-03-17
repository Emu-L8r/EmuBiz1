package com.emul8r.bizap.domain.validation

/**
 * Validates a business name supplied by the user.
 *
 * Rules:
 * - Must not be blank.
 * - Between 1 and 100 characters.
 */
object BusinessNameValidator {

    private const val MAX_LENGTH = 100

    fun validate(name: String): ValidationResult<String> = when {
        name.isBlank() ->
            ValidationResult.Failure("Business name cannot be empty")
        name.length > MAX_LENGTH ->
            ValidationResult.Failure("Business name must be $MAX_LENGTH characters or less")
        else -> ValidationResult.Success(name.trim())
    }
}
