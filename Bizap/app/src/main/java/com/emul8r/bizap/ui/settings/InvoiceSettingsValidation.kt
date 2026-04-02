package com.emul8r.bizap.ui.settings

import com.emul8r.bizap.domain.model.InvoiceSettings

/**
 * Validation rules and error messages for invoice settings.
 */
object InvoiceSettingsValidation {

    /**
     * Validation error for a specific field.
     */
    data class FieldError(
        val field: String,
        val message: String
    )

    /**
     * Validation result containing all field errors.
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: Map<String, String> = emptyMap()
    ) {
        fun getError(field: String): String? = errors[field]
        fun hasError(field: String): Boolean = errors.containsKey(field)
    }

    /**
     * Validate all invoice settings.
     * NOTE: Business information validation happens in BusinessProfile, not here.
     * This only validates PDF-specific settings.
     */
    fun validateSettings(settings: InvoiceSettings): ValidationResult {
        val errors = mutableMapOf<String, String>()


        // Validate color
        validateColor(settings.primaryColor)?.let {
            errors["primaryColor"] = it
        }

        // Validate tax rate
        validateTaxRate(settings.taxRate)?.let {
            errors["taxRate"] = it
        }

        // Validate payment terms
        validatePaymentTerms(settings.paymentTermsDays)?.let {
            errors["paymentTermsDays"] = it
        }

        // Validate tax name
        validateTaxName(settings.taxName)?.let {
            errors["taxName"] = it
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    /**
     * Validate business name.
     * Rules:
     * - Not empty
     * - Max 100 characters
     */
    fun validateBusinessName(name: String): String? {
        return when {
            name.isEmpty() -> "Company name is required"
            name.length > 100 -> "Company name cannot exceed 100 characters"
            else -> null
        }
    }

    /**
     * Validate email address.
     * Rules:
     * - Not empty
     * - Valid email format
     * - Max 100 characters
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isEmpty() -> "Email address is required"
            !isValidEmail(email) -> "Please enter a valid email address"
            email.length > 100 -> "Email cannot exceed 100 characters"
            else -> null
        }
    }

    /**
     * Validate phone number (optional).
     * Rules:
     * - If provided, must contain at least 10 digits
     * - Max 20 characters
     */
    fun validatePhone(phone: String): String? {
        return when {
            phone.isEmpty() -> null // Phone is optional
            phone.length > 20 -> "Phone number cannot exceed 20 characters"
            !containsDigits(phone, minCount = 10) -> "Phone number must contain at least 10 digits"
            else -> null
        }
    }

    /**
     * Validate hex color code.
     * Rules:
     * - Valid hex format (#RRGGBB)
     */
    fun validateColor(color: String): String? {
        return when {
            color.isEmpty() -> "Color is required"
            !isValidHexColor(color) -> "Please enter a valid hex color (e.g., #FF5722)"
            else -> null
        }
    }

    /**
     * Validate tax rate.
     * Rules:
     * - Between 0 and 100
     */
    fun validateTaxRate(rate: Double): String? {
        return when {
            rate < 0 -> "Tax rate cannot be negative"
            rate > 100 -> "Tax rate cannot exceed 100%"
            else -> null
        }
    }

    /**
     * Validate payment terms in days.
     * Rules:
     * - Greater than or equal to 0
     * - Not excessively large (max 365 days)
     */
    fun validatePaymentTerms(days: Int): String? {
        return when {
            days < 0 -> "Payment terms cannot be negative"
            days > 365 -> "Payment terms cannot exceed 365 days"
            else -> null
        }
    }

    /**
     * Validate tax name.
     * Rules:
     * - Not empty
     * - Max 50 characters
     */
    fun validateTaxName(name: String): String? {
        return when {
            name.isEmpty() -> "Tax name is required (e.g., GST, VAT, Sales Tax)"
            name.length > 50 -> "Tax name cannot exceed 50 characters"
            else -> null
        }
    }

    /**
     * Check if string is a valid email address.
     */
    private fun isValidEmail(email: String): Boolean {
        return email.contains("@") && email.contains(".") &&
                email.indexOf("@") < email.lastIndexOf(".") &&
                email.indexOf("@") > 0
    }

    /**
     * Check if string is a valid hex color code.
     * Valid format: #RRGGBB or #AARRGGBB
     */
    private fun isValidHexColor(color: String): Boolean {
        return (color.startsWith("#") && (color.length == 7 || color.length == 9)) &&
                color.drop(1).all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    /**
     * Count how many digits are in the string.
     */
    private fun containsDigits(text: String, minCount: Int): Boolean {
        return text.count { it.isDigit() } >= minCount
    }
}

