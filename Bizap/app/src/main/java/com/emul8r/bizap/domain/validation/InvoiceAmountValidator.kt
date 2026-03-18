package com.emul8r.bizap.domain.validation

/**
 * Validates an invoice amount expressed in cents.
 *
 * Rules:
 * - Must be greater than zero.
 * - Must not exceed 999,999,999 cents (~$10 million).
 */
object InvoiceAmountValidator {

    private const val MAX_AMOUNT_CENTS = 999_999_999L

    fun validate(amountCents: Long): ValidationResult<Long> = when {
        amountCents <= 0 ->
            ValidationResult.Failure("Amount must be greater than zero")
        amountCents > MAX_AMOUNT_CENTS ->
            ValidationResult.Failure("Amount is too large (maximum \$9,999,999.99)")
        else -> ValidationResult.Success(amountCents)
    }
}
