package com.emul8r.bizap.ui.templates

import com.emul8r.bizap.data.local.entities.InvoiceCustomField

/**
 * Data class representing the state of template form
 * Used by CreateTemplateScreen and EditTemplateScreen
 */
data class TemplateFormState(
    val id: String = "",
    val name: String = "",
    val designType: String = "PROFESSIONAL",
    val logoFileName: String? = null,
    val primaryColor: String = "#FF5722",
    val secondaryColor: String = "#FFF9C4",
    val fontFamily: String = "SANS_SERIF",
    val companyName: String = "",
    val companyAddress: String = "",
    val companyPhone: String = "",
    val companyEmail: String = "",
    val taxId: String? = null,
    val bankDetails: String? = null,
    val hideLineItems: Boolean = false,
    val hidePaymentTerms: Boolean = false,
    val customFields: List<InvoiceCustomField> = emptyList(),
    val isLoading: Boolean = false,
    val errors: Map<String, String> = emptyMap(),
    val isValid: Boolean = false
) {
    /**
     * Validate form state
     * Returns list of validation errors
     */
    fun validate(): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        // Name is required
        if (name.isBlank()) {
            errors["name"] = "Template name is required"
        }

        // Name length validation
        if (name.length > 100) {
            errors["name"] = "Template name must be 100 characters or less"
        }

        // Primary color must be valid hex
        if (!isValidHexColor(primaryColor)) {
            errors["primaryColor"] = "Invalid primary color"
        }

        // Secondary color must be valid hex
        if (!isValidHexColor(secondaryColor)) {
            errors["secondaryColor"] = "Invalid secondary color"
        }

        // Design type must be valid
        val validDesignTypes = listOf("PROFESSIONAL", "MINIMAL", "BRANDED")
        if (designType !in validDesignTypes) {
            errors["designType"] = "Invalid design type"
        }

        // Font family must be valid
        val validFonts = listOf("SANS_SERIF", "SERIF")
        if (fontFamily !in validFonts) {
            errors["fontFamily"] = "Invalid font family"
        }

        // Company name
        if (companyName.isBlank()) {
            errors["companyName"] = "Company name is required"
        }

        // Email validation
        if (companyEmail.isNotBlank() && !isValidEmail(companyEmail)) {
            errors["companyEmail"] = "Invalid email format"
        }

        // Custom field validation
        if (customFields.size > 50) {
            errors["customFields"] = "Maximum 50 custom fields allowed"
        }

        return errors
    }

    /**
     * Check if form is valid
     */
    fun isFormValid(): Boolean {
        return validate().isEmpty() && name.isNotBlank() && companyName.isNotBlank()
    }

    /**
     * Create copy with updated values (for form field changes)
     */
    fun updateField(field: String, value: Any?): TemplateFormState {
        return when (field) {
            "name" -> copy(name = value as? String ?: "")
            "designType" -> copy(designType = value as? String ?: "PROFESSIONAL")
            "logoFileName" -> copy(logoFileName = value as? String)
            "primaryColor" -> copy(primaryColor = value as? String ?: "#FF5722")
            "secondaryColor" -> copy(secondaryColor = value as? String ?: "#FFF9C4")
            "fontFamily" -> copy(fontFamily = value as? String ?: "SANS_SERIF")
            "companyName" -> copy(companyName = value as? String ?: "")
            "companyAddress" -> copy(companyAddress = value as? String ?: "")
            "companyPhone" -> copy(companyPhone = value as? String ?: "")
            "companyEmail" -> copy(companyEmail = value as? String ?: "")
            "taxId" -> copy(taxId = value as? String)
            "bankDetails" -> copy(bankDetails = value as? String)
            "hideLineItems" -> copy(hideLineItems = value as? Boolean ?: false)
            "hidePaymentTerms" -> copy(hidePaymentTerms = value as? Boolean ?: false)
            else -> this
        }
    }

    companion object {
        /**
         * Validate hex color format
         */
        private fun isValidHexColor(color: String): Boolean {
            return color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
        }

        /**
         * Basic email validation
         */
        private fun isValidEmail(email: String): Boolean {
            return email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))
        }
    }
}

/**
 * Design type enum
 */
enum class DesignType {
    PROFESSIONAL, MINIMAL, BRANDED
}

/**
 * Font family enum
 */
enum class FontFamily {
    SANS_SERIF, SERIF
}

