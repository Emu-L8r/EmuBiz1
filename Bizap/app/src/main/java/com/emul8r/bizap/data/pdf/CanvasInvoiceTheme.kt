package com.emul8r.bizap.data.pdf

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.pdf.CustomizationOption
import com.emul8r.bizap.domain.pdf.InvoiceThemeRenderer
import com.emul8r.bizap.domain.pdf.ValidationResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Canvas-based invoice theme implementation.
 *
 * Wraps the existing Canvas-based PDF generation (Phase 9 implementation)
 * and adapts it to the InvoiceThemeRenderer interface.
 *
 * This theme provides:
 * - Artistic layered design with overlapping shapes
 * - Premium floating cards with drop shadows
 * - Professional color customization
 * - Boutique/creative agency quality output
 *
 * Note: The actual PDF generation is handled by InvoicePdfService.generateInvoice()
 * which is called from the service layer, not directly from this theme class.
 */
@Singleton
class CanvasInvoiceTheme @Inject constructor() : InvoiceThemeRenderer {

    /**
     * Generate PDF using Canvas-based approach.
     *
     * Delegates to the existing PdfGenerationService which handles
     * all Phase 9 artistic design implementation.
     *
     * Note: This implementation requires InvoiceSnapshot conversion
     * which will be handled by the service layer caller.
     */
    override suspend fun generatePdf(
        invoice: Invoice,
        settings: InvoiceSettings,
        outputPath: String
    ): Result<String> {
        return try {
            // Since we don't have InvoiceSnapshot here, we return a message
            // indicating that the actual PDF generation should happen through
            // the service layer with proper snapshot conversion
            Result.failure(
                UnsupportedOperationException(
                    "Canvas theme PDF generation requires InvoiceSnapshot. " +
                    "Use PdfGenerationService.generatePdf(snapshot, isQuote) instead."
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Validate that settings are compatible with Canvas theme.
     *
     * Canvas theme validates PDF-specific settings only.
     * Business info is managed in BusinessProfile.
     */
    override fun validateSettings(settings: InvoiceSettings): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (settings.primaryColor.isBlank()) {
            warnings.add("Primary color not set, using default purple (#6B4C9A)")
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Get theme name for UI display.
     */
    override fun getThemeName(): String = "Canvas Style (Current)"

    /**
     * Get theme description for UI display.
     */
    override fun getThemeDescription(): String =
        "Professional layered invoice with artistic design (Phase 9 Canvas implementation)"

    /**
     * List customization options supported by Canvas theme.
     */
    override fun getSupportedCustomizations(): List<CustomizationOption> =
        listOf(
            CustomizationOption.PRIMARY_COLOR,
            CustomizationOption.LOGO,
            CustomizationOption.TYPOGRAPHY
        )
}


