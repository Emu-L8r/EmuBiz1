package com.emul8r.bizap.domain.validation

import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injectable validation service that centralises form-field validation.
 *
 * All validation logic is delegated to the pure-logic validators in this
 * package ([InputValidator], [BusinessNameValidator],
 * [InvoiceAmountValidator]) so that business rules are defined in exactly
 * one place.
 *
 * **Why a service?**
 * Marking this class `@Singleton` and injecting it via Hilt allows
 * ViewModels to receive it through their constructor instead of calling
 * static objects directly, which makes tests easier to write.
 *
 * **Usage in a ViewModel:**
 * ```kotlin
 * @HiltViewModel
 * class CreateInvoiceViewModel @Inject constructor(
 *     private val invoiceRepository: InvoiceRepository,
 *     private val validationService: ValidationService
 * ) : ViewModel() {
 *
 *     fun onAmountChanged(cents: Long) {
 *         val result = validationService.validateInvoiceAmount(cents)
 *         _amountError.value = result.getErrorOrNull()
 *     }
 * }
 * ```
 */
@Singleton
class ValidationService @Inject constructor() {

    // ── Business fields ───────────────────────────────────────────────────────

    fun validateBusinessName(name: String): ValidationResult<String> {
        val result = BusinessNameValidator.validate(name)
        Timber.d("ValidationService: validateBusinessName('%s') → %s", name, result)
        return result
    }

    // ── Invoice fields ────────────────────────────────────────────────────────

    fun validateInvoiceNumber(number: String): ValidationResult<String> {
        val result = InputValidator.validateInvoiceNumber(number)
        Timber.d("ValidationService: validateInvoiceNumber → %s", result)
        return result
    }

    fun validateInvoiceAmount(amountCents: Long): ValidationResult<Long> {
        val result = InvoiceAmountValidator.validate(amountCents)
        Timber.d("ValidationService: validateInvoiceAmount(%d) → %s", amountCents, result)
        return result
    }

    fun validateQuantity(qty: Double): ValidationResult<Double> =
        InputValidator.validateQuantity(qty)

    fun validateTaxRate(rate: Double): ValidationResult<Double> =
        InputValidator.validateTaxRate(rate)

    // ── Customer fields ───────────────────────────────────────────────────────

    fun validateCustomerName(name: String): ValidationResult<String> =
        InputValidator.validateCustomerName(name)

    fun validateEmail(email: String): ValidationResult<String> =
        InputValidator.validateEmail(email)

    fun validatePhone(phone: String?): ValidationResult<String?> =
        InputValidator.validatePhone(phone)
}
