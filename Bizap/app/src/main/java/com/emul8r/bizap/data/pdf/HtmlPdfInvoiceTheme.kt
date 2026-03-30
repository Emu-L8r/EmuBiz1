package com.emul8r.bizap.data.pdf

import android.content.Context
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.pdf.CustomizationOption
import com.emul8r.bizap.domain.pdf.InvoiceThemeRenderer
import com.emul8r.bizap.domain.pdf.ValidationResult
import com.emul8r.bizap.ui.invoices.html.InvoiceTemplateDataMapper
import com.emul8r.bizap.ui.invoices.html.HtmlTemplateProcessor
import com.emul8r.bizap.ui.invoices.html.HtmlToPdfConverter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HTML-to-PDF Invoice Theme Implementation
 *
 * Generates professional, modern invoices using HTML templates and iText7 PDF conversion.
 *
 * **Features:**
 * - Modern, artistic design with professional styling
 * - Brand color customization
 * - Professional typography and spacing
 * - Responsive layout optimized for print and screen
 * - High-quality PDF output
 * - Comprehensive error handling and validation
 *
 * **Workflow:**
 * Invoice + Settings → DataMapper → Template Processing → HTML-to-PDF → PDF File
 *
 * **Phase 6:** Full implementation complete with data mapping and template processing
 */
@Singleton
class HtmlPdfInvoiceTheme @Inject constructor(
    @ApplicationContext private val context: Context
) : InvoiceThemeRenderer {

    private val dataMapper = InvoiceTemplateDataMapper()
    private val templateProcessor = HtmlTemplateProcessor()
    private val pdfConverter = HtmlToPdfConverter(context)

    /**
     * Generate PDF invoice using HTML template.
     *
     * Orchestrates the complete process of converting invoice data into a professional PDF:
     * 1. Validates all required settings
     * 2. Maps invoice data to template variables
     * 3. Processes Freemarker template with dynamic data
     * 4. Converts resulting HTML to PDF using iText7
     *
     * @param invoice The invoice to generate PDF for
     * @param settings Invoice settings for customization (colors, branding, etc.)
     * @param outputPath Full file path where PDF should be saved
     * @return Result.success(outputPath) on success, Result.failure(exception) on error
     */
    override suspend fun generatePdf(
        invoice: Invoice,
        settings: InvoiceSettings,
        outputPath: String
    ): Result<String> {
        return try {
            Timber.d("Generating HTML-to-PDF invoice: ${invoice.invoiceNumber} → $outputPath")

            // Step 1: Validate settings before processing
            val validationResult = validateSettings(settings)
            if (!validationResult.isValid) {
                Timber.e("Settings validation failed: ${validationResult.errors.joinToString(", ")}")
                return Result.failure(
                    Exception("Invalid settings: ${validationResult.errors.joinToString(", ")}")
                )
            }

            // Step 2: Map invoice data to template format
            val templateData = dataMapper.mapToTemplateData(invoice, settings)
            if (templateData.isEmpty()) {
                Timber.e("Failed to map invoice data to template format")
                return Result.failure(Exception("Invoice data mapping failed"))
            }
            Timber.d("Invoice data mapped successfully with ${templateData.size} variables")

            // Step 3: Process Freemarker template with invoice data
            val htmlContent = try {
                templateProcessor.processTemplate("invoice-template.html", templateData)
            } catch (e: Exception) {
                Timber.e(e, "Template processing failed")
                return Result.failure(Exception("Template processing failed: ${e.message}"))
            }

            if (htmlContent.isBlank()) {
                Timber.e("Template processing returned empty HTML")
                return Result.failure(Exception("Template processing returned empty content"))
            }
            Timber.d("Template processed successfully, generated ${htmlContent.length} characters of HTML")

            // Step 4: Convert HTML to PDF using iText7
            val conversionSuccess = try {
                pdfConverter.convertHtmlToPdf(htmlContent, outputPath)
            } catch (e: Exception) {
                Timber.e(e, "HTML to PDF conversion failed")
                return Result.failure(Exception("PDF conversion failed: ${e.message}"))
            }

            if (!conversionSuccess) {
                Timber.e("PDF conversion failed")
                return Result.failure(Exception("PDF conversion failed: unknown reason"))
            }

            // Verify file was created
            val pdfFile = File(outputPath)
            if (!pdfFile.exists() || pdfFile.length() == 0L) {
                Timber.e("PDF file was not created or is empty")
                return Result.failure(Exception("PDF conversion failed: file not created"))
            }

            // Success!
            Timber.d("PDF generated successfully: $outputPath")
            Result.success(outputPath)

        } catch (e: Exception) {
            Timber.e(e, "Unexpected error in HTML-to-PDF invoice generation")
            Result.failure(e)
        }
    }

    /**
     * Validate invoice settings before PDF generation.
     *
     * Ensures all required fields are present and valid for HTML-to-PDF generation.
     * Required fields:
     * - Business name (for company header)
     * - Primary color (for styling and branding)
     *
     * Recommended fields (warnings if missing):
     * - Business email
     * - Business phone
     * - Business address
     *
     * @param settings The settings to validate
     * @return ValidationResult with error list if validation fails
     */
    override fun validateSettings(settings: InvoiceSettings): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // REQUIRED: Business name
        if (settings.businessName.isBlank()) {
            errors.add("Business name is required")
        }

        // REQUIRED: Primary color for branding
        if (settings.primaryColor.isBlank()) {
            errors.add("Primary color is required for styling")
        }

        // RECOMMENDED: Business email
        if (settings.businessEmail.isBlank()) {
            warnings.add("Business email not set - recommended for invoice visibility")
            Timber.w("Business email not set")
        }

        // RECOMMENDED: Phone
        if (settings.businessPhone.isBlank()) {
            warnings.add("Business phone not set - recommended for invoice visibility")
            Timber.w("Business phone not set")
        }

        // RECOMMENDED: Address
        if (settings.businessAddress.isBlank()) {
            warnings.add("Business address not set - recommended for invoice visibility")
            Timber.w("Business address not set")
        }

        val isValid = errors.isEmpty()
        Timber.d("Settings validation: ${if (isValid) "PASSED" else "FAILED"} - ${errors.size} errors, ${warnings.size} warnings")

        return ValidationResult(
            isValid = isValid,
            errors = errors,
            warnings = warnings
        )
    }

    /**
     * Get the display name of this theme.
     *
     * Used in UI for theme selection dropdown and display.
     *
     * @return Human-readable theme name
     */
    override fun getThemeName(): String {
        return "Modern HTML Style"
    }

    /**
     * Get a description of this theme.
     *
     * Used in UI to describe theme capabilities and features.
     *
     * @return Detailed theme description for UI display
     */
    override fun getThemeDescription(): String {
        return "Professional modern invoice with HTML/CSS styling, responsive design, and brand color customization"
    }

    /**
     * Get list of customization options supported by this theme.
     *
     * These define what settings can be customized for this particular theme.
     * Users can adjust these in the settings page.
     *
     * @return List of CustomizationOption enum values supported by this theme
     */
    override fun getSupportedCustomizations(): List<CustomizationOption> {
        return listOf(
            CustomizationOption.PRIMARY_COLOR,
            CustomizationOption.SECONDARY_COLOR,
            CustomizationOption.ACCENT_COLOR,
            CustomizationOption.FONT_FAMILY,
            CustomizationOption.LOGO,
            CustomizationOption.LAYOUT,
            CustomizationOption.TYPOGRAPHY
        )
    }

    companion object {
        private const val TAG = "HtmlPdfInvoiceTheme"
    }
}








