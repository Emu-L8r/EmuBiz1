package com.emul8r.bizap.ui.settings.invoice_theme

import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.pdf.ValidationResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages multiple invoice themes (Canvas and HTML-to-PDF).
 *
 * Provides:
 * - Theme factory/registry
 * - Theme switching
 * - Theme validation
 * - PDF generation abstraction
 *
 * Phase 6: Theme manager for dual-theme support
 */
@Singleton
class InvoiceThemeManager @Inject constructor(
    // Theme implementations injected as needed
) {

    /**
     * Get theme by type.
     *
     * @param themeType Theme type to retrieve
     * @return InvoiceTheme instance
     */
    fun getTheme(themeType: InvoiceTheme): InvoiceTheme {
        Timber.d("Getting theme: $themeType")
        return when (themeType) {
            InvoiceTheme.HTML_PDF -> {
                // Phase 6 Step 2: Implement HTML-to-PDF theme
                Timber.w("HTML-to-PDF theme not yet implemented")
                throw NotImplementedError("HTML-to-PDF theme will be implemented in Phase 6 Step 2")
            }
            InvoiceTheme.CANVAS -> {
                // Canvas theme is the current implementation
                Timber.d("Using Canvas theme")
                throw NotImplementedError("Canvas theme implementation in progress")
            }
        }
    }

    /**
     * Get all available themes.
     *
     * @return List of available theme types
     */
    fun getAvailableThemes(): List<InvoiceTheme> {
        return listOf(
            InvoiceTheme.CANVAS,
            InvoiceTheme.HTML_PDF
        )
    }

    /**
     * Get theme info for display.
     *
     * @param themeType Theme type
     * @return Theme information
     */
    fun getThemeInfo(themeType: InvoiceTheme): ThemeInfo {
        return when (themeType) {
            InvoiceTheme.CANVAS -> ThemeInfo(
                type = InvoiceTheme.CANVAS,
                name = "Canvas Style",
                description = "Clean, traditional invoice design"
            )
            InvoiceTheme.HTML_PDF -> ThemeInfo(
                type = InvoiceTheme.HTML_PDF,
                name = "Modern HTML Style",
                description = "Professional modern invoice with HTML/CSS styling (Phase 6)"
            )
        }
    }

    /**
     * Validate theme for settings.
     *
     * @param themeType Theme to validate
     * @param settings Settings to validate
     * @return ValidationResult
     */
    fun validateThemeForSettings(
        themeType: InvoiceTheme,
        settings: InvoiceSettings
    ): ValidationResult {
        return when (themeType) {
            InvoiceTheme.CANVAS -> ValidationResult(isValid = true)
            InvoiceTheme.HTML_PDF -> {
                // Phase 6 Step 2: Validate HTML-to-PDF theme settings
                // For now, just basic validation
                val errors = mutableListOf<String>()
                if (settings.businessName.isBlank()) errors.add("Business name required")
                if (settings.primaryColor.isBlank()) errors.add("Primary color required")
                ValidationResult(isValid = errors.isEmpty(), errors = errors)
            }
        }
    }
}

/**
 * Theme information for display.
 */
data class ThemeInfo(
    val type: InvoiceTheme,
    val name: String,
    val description: String
)

