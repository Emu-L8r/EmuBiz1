package com.emul8r.bizap.data.service.pdf_services

import android.content.Context
import com.emul8r.bizap.data.service.html.HtmlToPdfConverter
import com.emul8r.bizap.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 3C: PDF Preview Manager with File Generation
 *
 * Manages live PDF preview generation for the Invoice Settings screen.
 * Applies current user settings (ColorScheme, SpacingProfile, VisualAccents, etc.)
 * to generate CSS and create actual PDF files.
 *
 * Features:
 * - Debounced preview generation (1000ms)
 * - Dynamic CSS generation with current settings
 * - HTML-to-PDF conversion using iText7
 * - File-based preview generation
 * - Error handling and logging
 * - Reactive Flow-based API
 */
@Singleton
class PdfPreviewManager @Inject constructor(
    private val context: Context,
    private val cssGenerator: CssGenerator
) {

    private val htmlToPdfConverter = HtmlToPdfConverter(context)

    /**
     * Observe PDF preview with current settings.
     * Generates CSS based on current settings, debounced to prevent excessive regeneration.
     * Creates actual PDF file for preview.
     *
     * Phase 3C: Generates PDF files from HTML + dynamic CSS
     *
     * @return Flow emitting preview File or null on error
     */
    fun observePreview(
        colorScheme: ColorScheme = ColorScheme.PROFESSIONAL,
        spacingProfile: SpacingProfile = SpacingProfile.NORMAL,
        visualAccents: VisualAccents = VisualAccents.default(),
        totalBoxStyle: TotalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
        enableAlternatingRows: Boolean = true,
        enableDividers: Boolean = true,
        enableGradientHeader: Boolean = true
    ): Flow<File?> = flow {
        Timber.d("Phase 3C: Generating PDF preview with $colorScheme + $spacingProfile")

        try {
            // Step 1: Generate dynamic CSS with current settings
            val css = cssGenerator.generateCss(
                colorScheme = colorScheme,
                spacingProfile = spacingProfile,
                visualAccents = visualAccents,
                totalBoxStyle = totalBoxStyle,
                enableAlternatingRows = enableAlternatingRows,
                enableDividers = enableDividers,
                enableGradientHeader = enableGradientHeader
            )

            Timber.d("✅ CSS generated: ${css.length} characters")

            // Step 2: Generate sample HTML with the CSS
            val sampleHtml = generateSampleInvoiceHtml(css)

            // Step 3: Create PDF from HTML
            val previewFile = generatePreviewPdf(sampleHtml)

            if (previewFile != null) {
                Timber.d("✅ PDF preview generated: ${previewFile.absolutePath} (${previewFile.length()} bytes)")
                emit(previewFile)
            } else {
                Timber.w("⚠️ PDF generation returned null")
                emit(null)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Preview generation failed")
            emit(null)
        }
    }.debounce(1000)  // Don't regenerate more than once per second

    /**
     * Generate sample invoice HTML with embedded CSS.
     * This demonstrates how the PDF will look with current settings.
     */
    private fun generateSampleInvoiceHtml(css: String): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Invoice Preview - Sample</title>
    <style>
        $css
    </style>
</head>
<body>
    <div class="page">
        <div class="invoice-container">
            <!-- Header -->
            <div class="invoice-header">
                <div class="company-info">
                    <h1>Your Company Name</h1>
                    <p>hello@company.com | (02) 1234 5678</p>
                    <p>123 Business Street, Sydney NSW 2000</p>
                    <p>ABN: 12 345 678 901</p>
                </div>
                <div class="invoice-title">
                    <h2>INVOICE</h2>
                    <p>#INV-2026-001</p>
                </div>
            </div>

            <!-- Bill To & Details -->
            <div class="details-section">
                <div class="bill-to">
                    <h3>BILL TO</h3>
                    <p><strong>Sample Client Ltd</strong></p>
                    <p>456 Client Avenue, Melbourne VIC 3000</p>
                    <p>contact@sample.com.au</p>
                </div>
                <div class="invoice-details">
                    <h3>INVOICE DETAILS</h3>
                    <p><strong>Invoice Date:</strong> 09-05-2026</p>
                    <p><strong>Due Date:</strong> 08-06-2026</p>
                    <p><strong>Currency:</strong> AUD</p>
                </div>
            </div>

            <!-- Items Table -->
            <table class="items-table">
                <thead>
                    <tr>
                        <th class="description">Description</th>
                        <th class="quantity" style="text-align: right;">Qty</th>
                        <th class="price" style="text-align: right;">Unit Price</th>
                        <th class="total" style="text-align: right;">Total</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="description">Professional Services - Development</td>
                        <td class="quantity" style="text-align: right;">40.00</td>
                        <td class="price" style="text-align: right;">${'$'}150.00</td>
                        <td class="total" style="text-align: right;">${'$'}6,000.00</td>
                    </tr>
                    <tr>
                        <td class="description">UI/UX Design - Mockups</td>
                        <td class="quantity" style="text-align: right;">16.00</td>
                        <td class="price" style="text-align: right;">${'$'}125.00</td>
                        <td class="total" style="text-align: right;">${'$'}2,000.00</td>
                    </tr>
                    <tr>
                        <td class="description">Project Management</td>
                        <td class="quantity" style="text-align: right;">10.00</td>
                        <td class="price" style="text-align: right;">${'$'}100.00</td>
                        <td class="total" style="text-align: right;">${'$'}1,000.00</td>
                    </tr>
                </tbody>
            </table>

            <!-- Totals -->
            <div class="totals-section">
                <div class="totals-box">
                    <div class="totals-row">
                        <span>Subtotal:</span>
                        <span>${'$'}9,000.00</span>
                    </div>
                    <div class="totals-row">
                        <span>Tax (10%):</span>
                        <span>${'$'}900.00</span>
                    </div>
                    <div class="totals-row total-amount">
                        <span>TOTAL DUE:</span>
                        <span>${'$'}9,900.00</span>
                    </div>
                </div>
            </div>

            <!-- Payment Details -->
            <div class="payment-section">
                <h3>PAYMENT DETAILS</h3>
                <p>Please transfer payment to:</p>
                <div class="payment-details">
                    <div>
                        <p><strong>Bank:</strong> National Bank of Australia</p>
                        <p><strong>Account Name:</strong> Your Company Name</p>
                    </div>
                    <div>
                        <p><strong>BSB:</strong> 123456</p>
                        <p><strong>Account Number:</strong> 98765432101</p>
                    </div>
                </div>
            </div>

            <!-- Notes -->
            <div class="notes-section">
                <h3>NOTES</h3>
                <p>Thank you for your business. Payment is due within 30 days of invoice date. This is a sample preview showing how your invoice will appear with the selected settings.</p>
            </div>
        </div>

        <!-- Footer -->
        <div class="footer">
            <p>Generated on 09-05-2026 14:30 | Preview Sample</p>
        </div>
    </div>
</body>
</html>
        """
    }

    /**
     * Phase 3C: Generate actual PDF file from HTML content.
     * Creates a temporary preview PDF in the cache directory.
     */
    private fun generatePreviewPdf(htmlContent: String): File? {
        return try {
            // Create a temporary PDF file in cache directory
            val cacheDir = context.cacheDir
            val previewDir = File(cacheDir, "pdf_previews")
            previewDir.mkdirs()

            // Use timestamp to ensure unique filenames
            val timestamp = System.currentTimeMillis()
            val previewFile = File(previewDir, "preview_$timestamp.pdf")

            // Convert HTML to PDF
            val success = htmlToPdfConverter.convertHtmlToPdf(
                htmlContent = htmlContent,
                outputPath = previewFile.absolutePath
            )

            if (success && previewFile.exists()) {
                Timber.d("✅ PDF file created: ${previewFile.absolutePath}")
                previewFile
            } else {
                Timber.e("Failed to create PDF file")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception during PDF generation")
            null
        }
    }

    /**
     * Clean up old preview files to save space.
     * Call periodically to prevent cache bloat.
     */
    fun cleanupOldPreviews(maxAgeMs: Long = 24 * 60 * 60 * 1000) {
        try {
            val cacheDir = context.cacheDir
            val previewDir = File(cacheDir, "pdf_previews")

            if (previewDir.exists()) {
                val now = System.currentTimeMillis()
                previewDir.listFiles()?.forEach { file ->
                    if (now - file.lastModified() > maxAgeMs) {
                        file.delete()
                        Timber.d("Deleted old preview: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up preview files")
        }
    }
}


