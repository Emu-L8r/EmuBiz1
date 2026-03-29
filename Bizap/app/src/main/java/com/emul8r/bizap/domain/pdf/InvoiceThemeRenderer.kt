package com.emul8r.bizap.domain.pdf

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSettings
import kotlin.Result

/**
 * Theme interface for invoice PDF generation.
 *
 * Allows multiple theme implementations (Canvas, HTML-to-PDF, future themes).
 * Each theme implements this interface to provide its own PDF generation logic
 * while maintaining a consistent contract for callers.
 */
interface InvoiceThemeRenderer {

    /**
     * Generate PDF for the given invoice with settings.
     *
     * @param invoice The invoice data to render
     * @param settings The invoice settings (branding, colors, etc.)
     * @param outputPath Where to save the PDF file
     * @return Result with file path on success or error message on failure
     */
    suspend fun generatePdf(
        invoice: Invoice,
        settings: InvoiceSettings,
        outputPath: String
    ): Result<String>

    /**
     * Validate settings compatibility with this theme.
     */
    fun validateSettings(settings: InvoiceSettings): ValidationResult

    /**
     * Get user-friendly theme name.
     */
    fun getThemeName(): String

    /**
     * Get theme description for UI display.
     */
    fun getThemeDescription(): String

    /**
     * List customization options supported by theme.
     */
    fun getSupportedCustomizations(): List<CustomizationOption>
}

/**
 * Validation result for settings.
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)

/**
 * Customization options available in a theme.
 */
enum class CustomizationOption {
    PRIMARY_COLOR,
    SECONDARY_COLOR,
    ACCENT_COLOR,
    FONT_FAMILY,
    LOGO,
    LAYOUT,
    TYPOGRAPHY
}

/**
 * Theme manager factory for selecting and managing themes.
 */
interface InvoiceThemeManager {
    fun getTheme(theme: com.emul8r.bizap.domain.model.InvoiceTheme): InvoiceThemeRenderer
    fun listAvailableThemes(): List<com.emul8r.bizap.domain.model.InvoiceTheme>
}

