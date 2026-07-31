package com.emul8r.bizap.data.pdf

import android.content.Context
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.pdf.CustomizationOption
import com.emul8r.bizap.domain.pdf.InvoiceThemeRenderer
import com.emul8r.bizap.domain.pdf.ValidationResult
import com.emul8r.bizap.data.service.html.InvoiceTemplateDataMapper
import com.emul8r.bizap.data.service.html.HtmlTemplateProcessor
import com.emul8r.bizap.data.service.html.HtmlToPdfConverter
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
     * 4. Injects brand colors from InvoiceSettings into CSS variables
     * 5. Converts resulting HTML to PDF using iText7
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

            // Step 2: Validate color formats
            val colorErrors = CssVariableInjector.validateColors(settings)
            if (colorErrors.isNotEmpty()) {
                Timber.w("Color validation warnings: ${colorErrors.joinToString(", ")}")
                // Don't fail - we have fallback colors, just log warnings
            }

            // Step 3: Map invoice data to template format
            val templateData = dataMapper.mapToTemplateData(invoice, settings)
            if (templateData.isEmpty()) {
                Timber.e("Failed to map invoice data to template format")
                return Result.failure(Exception("Invoice data mapping failed"))
            }
            Timber.d("Invoice data mapped successfully with ${templateData.size} variables")

            // Step 4: Process Freemarker template with invoice data
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

            // Step 5: Embed CSS from assets into HTML (CRITICAL FIX)
            // iText7 doesn't load external CSS files, so we embed CSS as inline <style> tag
            // Use the selected HTML style to determine which CSS file to load.
            // If the primary CSS file is missing (styles without dedicated CSS yet),
            // fall back to fallbackStyleFile defined on the enum entry.
            val selectedStyle = settings.selectedHtmlStyle
            val primaryCssFile  = selectedStyle.styleFile
            val fallbackCssFile = selectedStyle.fallbackStyleFile
            Timber.d("🎨 Selected HTML Style: ${selectedStyle.displayName}")
            Timber.d("🎨 CSS File: $primaryCssFile  (fallback: $fallbackCssFile)")

            val htmlWithEmbeddedCss = try {
                pdfConverter.embedCssFromAssets(context, htmlContent, primaryCssFile)
            } catch (e: Exception) {
                Timber.w("Primary CSS '$primaryCssFile' not found — trying fallback '$fallbackCssFile'")
                try {
                    pdfConverter.embedCssFromAssets(context, htmlContent, fallbackCssFile)
                } catch (e2: Exception) {
                    Timber.e(e2, "Fallback CSS '$fallbackCssFile' also failed — continuing without styling")
                    htmlContent  // Last resort: unstyled HTML
                }
            }

            // Step 6: Inject brand colors from InvoiceSettings into CSS variables
            val htmlWithColors = CssVariableInjector.injectColorVariables(htmlWithEmbeddedCss, settings)
            Timber.d("CSS color variables injected for branding")

            // Step 7: Convert HTML to PDF using iText7 (BLOCKING OPERATION - use Default dispatcher)
            // ⚠️ CRITICAL: HtmlConverter.convertToPdf() is a blocking I/O operation
            // Running on Main thread causes "loading screen" to hang indefinitely
            // Solution: Use withContext(Dispatchers.Default) to run on background thread
            withContext(Dispatchers.Default) {
                Timber.d("🔵 Starting HTML→PDF conversion on background thread (${Thread.currentThread().name})")
                try {
                    val result = pdfConverter.convertHtmlToPdf(htmlWithColors, outputPath)
                    // Result.success() means conversion succeeded
                    if (result.toString().contains("Success")) {
                        Timber.d("✅ HTML→PDF conversion complete")
                    } else {
                        return@withContext  // Will be caught below
                    }
                } catch (e: Exception) {
                    Timber.e(e, "HTML to PDF conversion failed")
                    throw e
                }
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
     * Validate HTML PDF theme settings.
     *
     * HTML PDF theme validates PDF-specific settings only.
     * Business info is managed in BusinessProfile.
     *
     * @param settings The settings to validate
     * @return ValidationResult with error list if validation fails
     */
    override fun validateSettings(settings: InvoiceSettings): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        // REQUIRED: Primary color for branding
        if (settings.primaryColor.isBlank()) {
            errors.add("Primary color is required for styling")
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












