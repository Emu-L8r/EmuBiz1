package com.emul8r.bizap.data.service

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.HtmlInvoiceStyle
import com.emul8r.bizap.domain.service.PdfGenerationService
import com.emul8r.bizap.utils.DocumentNamingUtils
import timber.log.Timber
import java.io.File
import java.io.InputStreamReader

/**
 * HTML-to-PDF implementation of PDF generation service.
 *
 * Modern, professional invoice PDF generation using HTML templates with style selection.
 *
 * Features:
 * - Professional, modern design with gradients and styling
 * - Multiple style options (Modern, Minimal, Corporate, Creative)
 * - Style selection based on InvoiceSettings.selectedHtmlStyle
 * - Clean visual hierarchy
 * - Color customization support
 * - Responsive layout
 * - Better readability than Canvas version
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class HtmlPdfInvoiceService(
    private val context: Context,
    private val settings: InvoiceSettings? = null
) : PdfGenerationService {

    companion object {
        private const val TAG = "HtmlPdfInvoiceService"
    }

    /**
     * Generate PDF from invoice snapshot using HTML template and selected style.
     *
     * @param snapshot Invoice data to convert to PDF
     * @param isQuote Whether this is a quote or invoice
     * @param overwriteExisting Whether to overwrite existing files
     * @param theme Invoice theme (kept for interface compatibility)
     * @return Generated PDF file
     */
    override suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean,
        theme: com.emul8r.bizap.domain.model.InvoiceTheme?
    ): File {
        Timber.d("════════════════════════════════════════════════════════════════════")
        Timber.d("📝 HtmlPdfInvoiceService.generatePdf() START")
        Timber.d("════════════════════════════════════════════════════════════════════")
        Timber.d("Input parameters:")
        Timber.d("   isQuote: $isQuote")
        Timber.d("   theme: ${theme?.name ?: "NULL"}")
        
        // FIX #3: Validate settings BEFORE any processing
        Timber.d("")
        Timber.d("═ VALIDATION PHASE ═════════════════════════════════════════════════")
        Timber.d("Checking if settings object exists...")
        if (settings == null) {
            Timber.e("❌ CRITICAL ERROR: Settings object is NULL")
            Timber.e("   This means selectedHtmlStyle cannot be retrieved")
            Timber.e("   PDF generation will FAIL (not use silent MODERN default)")
            throw IllegalStateException(
                "HtmlPdfInvoiceService requires settings to be passed in constructor, " +
                "but received NULL. This prevents application of selectedHtmlStyle."
            )
        }
        
        Timber.d("✅ Settings object exists")
        Timber.d("")
        Timber.d("📋 SETTINGS CONTENT:")
        Timber.d("   Selected Theme: ${settings.selectedTheme.name}")
        Timber.d("   Selected HTML Style: ${settings.selectedHtmlStyle.displayName}")
        Timber.d("   Style Enum Value: ${settings.selectedHtmlStyle.name}")
        Timber.d("   Style CSS File: ${settings.selectedHtmlStyle.styleFile}")
        
        // Validate selectedHtmlStyle is not null (shouldn't happen, but check anyway)
        if (settings.selectedHtmlStyle == null) {
            Timber.e("❌ ERROR: selectedHtmlStyle field is NULL")
            throw IllegalStateException(
                "Settings loaded but selectedHtmlStyle is NULL. " +
                "This indicates a data model corruption or deserialization error."
            )
        }
        
        Timber.d("✅ All validations passed")
        Timber.d("════════════════════════════════════════════════════════════════════")

        return try {
            val fileType = if (isQuote) "Quote" else "Invoice"
            val baseFileName = DocumentNamingUtils.generateFileName(
                snapshot.customerName, snapshot.date, snapshot.invoiceId.toInt(), fileType
            )
            Timber.d("   baseFileName: $baseFileName")

            val existingFile = File(context.filesDir, "documents/$baseFileName")
            if (!overwriteExisting && existingFile.exists()) {
                Timber.d("   📌 Using existing file (not overwriting)")
                return existingFile
            }

            Timber.d("   🔄 Generating HTML content...")
            // Generate HTML content from invoice snapshot
            val htmlContent = generateHtmlContent(snapshot, isQuote)
            Timber.d("   ✅ HTML generated: ${htmlContent.length} characters")

            Timber.d("   🔄 Embedding CSS from selected style...")
            // Load the appropriate CSS file based on selected style
            val cssContent = loadSelectedStyleCss()
            Timber.d("   ✅ CSS loaded: ${cssContent.length} characters")

            // Embed CSS into HTML
            val htmlWithCss = embedCssIntoHtml(htmlContent, cssContent)
            Timber.d("   ✅ CSS embedded into HTML")

            Timber.d("   🔄 Converting HTML to PDF...")
            // Convert HTML to PDF and save
            val pdfFile = convertHtmlToPdf(htmlWithCss, baseFileName)
            Timber.d("   ✅ PDF file created: ${pdfFile.name}")
            Timber.d("   📦 PDF file size: ${pdfFile.length()} bytes")

            Timber.d("✅ HtmlPdfInvoiceService.generatePdf() SUCCESS")
            pdfFile
        } catch (e: Exception) {
            Timber.e(e, "❌ HtmlPdfInvoiceService.generatePdf() FAILED")
            throw e
        }
    }

    /**
     * Load the CSS file for the selected HTML invoice style.
     *
     * CRITICAL: This method determines which CSS file gets applied to the PDF.
     * If this fails, ALL PDFs will look the same (using fallback style).
     *
     * @return CSS content as String
     */
    private fun loadSelectedStyleCss(): String {
        val selectedStyle = settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN
        val cssFileName = selectedStyle.styleFile

        Timber.d("🎨 ══════════════════════════════════════════════════════════════════")
        Timber.d("🎨 CRITICAL: LOADING CSS FOR INVOICE STYLE")
        Timber.d("🎨 ══════════════════════════════════════════════════════════════════")
        Timber.d("🎨 Selected Style: ${selectedStyle.displayName} (ENUM: ${selectedStyle.name})")
        Timber.d("🎨 Expected CSS File: $cssFileName")
        Timber.d("🎨 Full Asset Path: invoices/html-theme/$cssFileName")
        Timber.d("🎨 Settings Object: ${if (settings != null) "✅ Present" else "❌ NULL (using default)"}")
        if (settings != null) {
            Timber.d("🎨 Settings.selectedHtmlStyle actual: ${settings.selectedHtmlStyle.displayName}")
        }
        Timber.d("🎨 ══════════════════════════════════════════════════════════════════")

        return try {
            val inputStream = context.assets.open("invoices/html-theme/$cssFileName")
            val cssContent = inputStream.use { stream ->
                InputStreamReader(stream).use { reader ->
                    reader.readText()
                }
            }
            Timber.d("✅ CSS LOADED SUCCESSFULLY for ${selectedStyle.displayName}")
            Timber.d("✅ CSS file size: ${cssContent.length} characters")
            Timber.d("✅ This CSS will be embedded into the HTML and applied to the PDF")
            cssContent
        } catch (e: Exception) {
            Timber.e(e, "❌ FAILED TO LOAD CSS FILE: $cssFileName")
            Timber.e("❌ Error: ${e.message}")
            Timber.e("❌ The selected style will NOT appear in the PDF!")
            // Fallback to modern style if selected style file doesn't exist
            try {
                Timber.d("🔄 FALLBACK: Attempting to load MODERN style (invoice-styles.css)...")
                val inputStream = context.assets.open("invoices/html-theme/invoice-styles.css")
                val fallbackCss = inputStream.use { stream ->
                    InputStreamReader(stream).use { reader ->
                        reader.readText()
                    }
                }
                Timber.d("✅ FALLBACK SUCCESSFUL: Modern CSS loaded (${fallbackCss.length} characters)")
                Timber.d("⚠️ Note: PDF will show MODERN style instead of selected style")
                fallbackCss
            } catch (fallbackError: Exception) {
                Timber.e(fallbackError, "❌ FALLBACK FAILED: Even Modern CSS could not be loaded!")
                Timber.e("❌ PDF will render with NO STYLING - plain HTML only")
                ""  // Return empty CSS if all fails
            }
        }
    }

    /**
     * Embed CSS content into HTML as <style> tag.
     *
     * Replaces the placeholder style tag with the actual CSS content.
     * This is CRITICAL - without this, the selected CSS style won't be applied to the PDF.
     *
     * @param htmlContent HTML document
     * @param cssContent CSS to embed
     * @return HTML with embedded CSS
     */
    private fun embedCssIntoHtml(htmlContent: String, cssContent: String): String {
        Timber.d("════════════════════════════════════════════════════════════════════")
        Timber.d("🎨 STEP 3: EMBEDDING CSS INTO HTML")
        Timber.d("════════════════════════════════════════════════════════════════════")

        Timber.d("📊 INPUT SIZES:")
        Timber.d("   HTML: ${htmlContent.length} characters")
        Timber.d("   CSS: ${cssContent.length} characters")
        Timber.d("   CSS is empty: ${cssContent.isBlank()}")

        Timber.d("🔍 SEARCHING FOR STYLE TAGS:")
        // Find and replace the style tag placeholder
        val styleTagStart = htmlContent.indexOf("<style>")
        val styleTagEnd = htmlContent.indexOf("</style>", styleTagStart)

        Timber.d("   <style> tag position: $styleTagStart")
        Timber.d("   </style> tag position: $styleTagEnd")
        Timber.d("   Both tags found: ${styleTagStart >= 0 && styleTagEnd > styleTagStart}")

        return if (styleTagStart >= 0 && styleTagEnd > styleTagStart) {
            Timber.d("✅ STYLE TAGS FOUND - EMBEDDING CSS:")
            Timber.d("   Extracting HTML before: $styleTagStart chars")
            Timber.d("   Extracting HTML after: ${htmlContent.length - (styleTagEnd + 8)} chars")

            val beforeStyle = htmlContent.substring(0, styleTagStart)
            val afterStyle = htmlContent.substring(styleTagEnd + "</style>".length)
            val result = beforeStyle + "<style>\n" + cssContent + "\n</style>" + afterStyle

            Timber.d("✅ RESULT:")
            Timber.d("   Result HTML size: ${result.length} characters")
            Timber.d("   Size increase: ${result.length - htmlContent.length} characters (CSS + tags)")
            Timber.d("   CSS is now embedded in the HTML")
            "════════════════════════════════════════════════════════════════════"
            result
        } else {
            Timber.e("❌ CRITICAL ERROR: STYLE TAGS NOT FOUND!")
            Timber.e("   styleTagStart: $styleTagStart (should be >= 0)")
            Timber.e("   styleTagEnd: $styleTagEnd (should be > $styleTagStart)")
            Timber.e("   HTML Content Sample: ${htmlContent.take(500).replace("\n", "\\n")}")
            Timber.e("   ⚠️  CSS STYLING WILL NOT BE APPLIED TO PDF!")
            Timber.e("   This is why the PDF might look blank or unstyled")
            "════════════════════════════════════════════════════════════════════"
            htmlContent  // Return unchanged if style tag not found (CSS won't work)
        }
    }

    /**
     * Generate professional HTML content for the invoice.
     *
     * This generates a clean HTML structure that will be styled by the selected CSS file.
     * The CSS styling is applied AFTER this HTML is generated, allowing theme switching.
     */
    private fun generateHtmlContent(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean
    ): String {
        Timber.d("════════════════════════════════════════════════════════════════════")
        Timber.d("📝 GENERATING HTML CONTENT FROM INVOICE DATA")
        Timber.d("════════════════════════════════════════════════════════════════════")

        val documentType = if (isQuote) "QUOTE" else "INVOICE"

        // CRITICAL DEBUG LOGGING: Verify invoice data exists
        Timber.e("════════════════════════════════════════════════════════════════")
        Timber.e("⚠️  CRITICAL: PDF DATA VERIFICATION")
        Timber.e("════════════════════════════════════════════════════════════════")
        Timber.e("Invoice snapshot check:")
        Timber.e("   Items count: ${snapshot.items.size}")
        Timber.e("   Total amount: ${snapshot.totalAmount} cents")
        Timber.e("   Customer name: ${snapshot.customerName}")
        Timber.e("   Business name: ${snapshot.businessName}")

        if (snapshot.items.isEmpty()) {
            Timber.e("   ❌ PROBLEM: Invoice has ZERO items!")
            Timber.e("   Result: PDF will show blank page because table has no rows")
        } else {
            snapshot.items.forEach { item ->
                Timber.e("   ✓ Item: ${item.description} | Qty: ${item.quantity} | Price: ${item.unitPrice} cents | Total: ${item.total} cents")
            }
        }

        if (snapshot.totalAmount <= 0) {
            Timber.e("   ❌ PROBLEM: Total amount is ${snapshot.totalAmount} cents (zero or negative)!")
            Timber.e("   Result: Amounts will not calculate correctly")
        }

        Timber.e("════════════════════════════════════════════════════════════════")

        // Verify basic invoice data
        Timber.d("✅ INVOICE METADATA:")
        Timber.d("   Invoice ID: ${snapshot.invoiceId}")
        Timber.d("   Type: $documentType")
        Timber.d("   Business: ${snapshot.businessName}")
        Timber.d("   Customer: ${snapshot.customerName}")
        Timber.d("   Date: ${snapshot.date}")
        Timber.d("   Due Date: ${snapshot.dueDate ?: "Upon Receipt"}")

        // Convert cents to dollars
        val subtotalDollars = snapshot.subtotal / 100.0
        val taxDollars = snapshot.taxAmount / 100.0
        val totalDollars = snapshot.totalAmount / 100.0

        Timber.d("✅ FINANCIAL DATA:")
        Timber.d("   Subtotal: \$${String.format("%.2f", subtotalDollars)} (${snapshot.subtotal} cents)")
        Timber.d("   Tax: \$${String.format("%.2f", taxDollars)} (${snapshot.taxAmount} cents)")
        Timber.d("   Total: \$${String.format("%.2f", totalDollars)} (${snapshot.totalAmount} cents)")

        // Process line items
        Timber.d("✅ LINE ITEMS DATA:")
        Timber.d("   Total Items: ${snapshot.items.size}")

        val itemsHtml = snapshot.items.joinToString("\n") { item ->
            val amountDollars = item.total / 100.0
            val unitPriceDollars = item.unitPrice / 100.0

            Timber.d("   ✓ Item: ${item.description}")
            Timber.d("     - Qty: ${String.format("%.2f", item.quantity)}")
            Timber.d("     - Unit Price: \$${String.format("%.2f", unitPriceDollars)}")
            Timber.d("     - Total: \$${String.format("%.2f", amountDollars)}")

            """
                <tr class="table-row">
                    <td class="col-description">${item.description}</td>
                    <td class="col-quantity">${String.format("%.2f", item.quantity)}</td>
                    <td class="col-unit-price">${String.format("${'$'}%.2f", unitPriceDollars)}</td>
                    <td class="col-amount">${String.format("${'$'}%.2f", amountDollars)}</td>
                </tr>
            """.trimIndent()
        }

        if (snapshot.items.isEmpty()) {
            Timber.w("⚠️  WARNING: Invoice has NO line items! PDF will show empty table")
        }

        Timber.d("✅ HTML GENERATION:")
        Timber.d("   Items HTML size: ${itemsHtml.length} characters")
        Timber.d("   Items HTML is empty: ${itemsHtml.isBlank()}")

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$documentType ${snapshot.invoiceId}</title>
                <!-- CSS will be injected here by embedCssIntoHtml() -->
                <style>
/* PLACEHOLDER_CSS_CONTENT_START - Do not modify this line */
/* The selected CSS stylesheet will be embedded here during PDF generation */
/* This ensures the correct visual style is applied to the invoice */
/* PLACEHOLDER_CSS_CONTENT_END */
                </style>
            </head>
            <body>
                <div class="invoice-container">
                    <!-- Header with Company Info -->
                    <div class="invoice-header">
                        <div class="header-content">
                            <div class="company-info">
                                <div class="company-name">${snapshot.businessName}</div>
                                <div class="company-detail">${snapshot.businessEmail}</div>
                                <div class="company-detail">${snapshot.businessPhone}</div>
                            </div>
                            <div class="invoice-title">
                                <h2>$documentType</h2>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Metadata Section: Invoice # and Date -->
                    <div class="invoice-metadata">
                        <div class="metadata-grid">
                            <div class="metadata-item">
                                <div class="metadata-label">Invoice #</div>
                                <div class="metadata-value">${snapshot.invoiceId}</div>
                            </div>
                            <div class="metadata-item">
                                <div class="metadata-label">Date</div>
                                <div class="metadata-value">${snapshot.date}</div>
                            </div>
                            <div class="metadata-item">
                                <div class="metadata-label">Due Date</div>
                                <div class="metadata-value">${snapshot.dueDate ?: "Upon Receipt"}</div>
                            </div>
                            <div class="metadata-item">
                                <div class="metadata-label">Status</div>
                                <div class="metadata-value">UNPAID</div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Bill To Section -->
                    <div class="bill-to-section">
                        <div class="bill-to-container">
                            <div class="bill-to-block">
                                <h3>Bill To</h3>
                                <div class="customer-name">${snapshot.customerName}</div>
                                ${if (!snapshot.customerEmail.isNullOrBlank()) "<div class=\"customer-detail\">${snapshot.customerEmail}</div>" else ""}
                            </div>
                            <div class="bill-to-block">
                                <h3>Ship To</h3>
                                <div class="customer-name">${snapshot.customerName}</div>
                                ${if (!snapshot.customerEmail.isNullOrBlank()) "<div class=\"customer-detail\">${snapshot.customerEmail}</div>" else ""}
                            </div>
                        </div>
                    </div>
                    
                    <!-- Items Table -->
                    <div class="items-section">
                        <table class="items-table">
                            <thead class="table-header">
                                <tr>
                                    <th class="col-description">Description</th>
                                    <th class="col-quantity">Qty</th>
                                    <th class="col-unit-price">Unit Price</th>
                                    <th class="col-amount">Amount</th>
                                </tr>
                            </thead>
                            <tbody>
                                $itemsHtml
                            </tbody>
                        </table>
                    </div>
                    
                    <!-- Totals Section -->
                    <div class="totals-section">
                        <div class="totals-container">
                            <div class="totals-summary">
                                <div class="summary-row">
                                    <span class="summary-label">Subtotal</span>
                                    <span class="summary-value">${String.format("${'$'}%.2f", subtotalDollars)}</span>
                                </div>
                                <div class="summary-row">
                                    <span class="summary-label">Tax (10%)</span>
                                    <span class="summary-value">${String.format("${'$'}%.2f", taxDollars)}</span>
                                </div>
                                <div class="summary-row total-due">
                                    <span class="total-label">Total Due</span>
                                    <span class="total-value">${String.format("${'$'}%.2f", totalDollars)}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Payment Section -->
                    <div class="payment-section">
                        <h3>Payment Instructions</h3>
                        <div class="payment-content">
                            <div class="payment-item">
                                <span class="payment-label">Bank Transfer:</span>
                                <span class="payment-value">Details available upon request</span>
                            </div>
                            <div class="payment-item">
                                <span class="payment-label">Due Date:</span>
                                <span class="payment-value">Net 30</span>
                            </div>
                        </div>
                    </div>
                    
                    <!-- Notes Section -->
                    <div class="notes-section">
                        <h3>Notes</h3>
                        <div class="notes-content">
                            Thank you for your business! Payment terms are Net 30 days from invoice date.
                        </div>
                    </div>
                    
                    <!-- Footer -->
                    <div class="invoice-footer">
                        <div class="footer-message">Thank you for your business!</div>
                        <div class="footer-website">Generated by Bizap • ${snapshot.date}</div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent().also {
            Timber.d("════════════════════════════════════════════════════════════════════")
            Timber.d("✅ HTML CONTENT GENERATION COMPLETE")
            Timber.d("   Total HTML size: ${it.length} characters")
            Timber.d("════════════════════════════════════════════════════════════════════")
        }
    }

    /**
     * Convert HTML to PDF using iText7.
     *
     * This is the CRITICAL bridge that turns HTML into actual PDF files.
     * Without this working correctly, the theme selection would have no visible effect.
     *
     * @param htmlContent The complete HTML document to convert
     * @param baseFileName The base filename for the PDF
     * @return A File object pointing to the generated PDF
     */
    private fun convertHtmlToPdf(
        htmlContent: String,
        baseFileName: String
    ): File {
        val pdfFileName = baseFileName.replace(".pdf", "_html.pdf")
        val file = File(context.filesDir, "documents/$pdfFileName")
        file.parentFile?.mkdirs()

        try {
            Timber.d("════════════════════════════════════════════════════════════════════")
            Timber.d("🔄 STEP 4: HTML-TO-PDF CONVERSION (iText7)")
            Timber.d("════════════════════════════════════════════════════════════════════")

            Timber.d("📋 INPUT:")
            Timber.d("   HTML size: ${htmlContent.length} characters")
            Timber.d("   HTML starts with: ${htmlContent.take(100).replace("\n", " ")}...")
            Timber.d("   Has <body>: ${htmlContent.contains("<body>")}")
            Timber.d("   Has invoice-container: ${htmlContent.contains("invoice-container")}")
            Timber.d("   Has table rows: ${htmlContent.contains("<tr class=\"table-row\">")}")

            Timber.d("")
            Timber.d("🔄 4.1a: Creating PdfWriter and PdfDocument...")
            // Use iText7 to convert HTML to real PDF binary
            val pdfWriter = com.itextpdf.kernel.pdf.PdfWriter(file)
            val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
            Timber.d("   ✅ PdfDocument created successfully")

            Timber.d("🔄 4.1b: Configuring page size (A4)...")
            // Configure page (A4)
            val pageSize = com.itextpdf.kernel.geom.PageSize.A4
            pdfDocument.defaultPageSize = pageSize
            Timber.d("   ✅ Page size: A4 (${pageSize.width}x${pageSize.height} points)")

            Timber.d("🔄 4.1c: Setting PDF metadata...")
            // Set PDF metadata
            val pdfMetaInfo = pdfDocument.documentInfo
            pdfMetaInfo.title = "Invoice"
            pdfMetaInfo.author = "Bizap"
            pdfMetaInfo.creator = "Bizap HTML-to-PDF"
            Timber.d("   ✅ Metadata set")

            Timber.d("🔄 4.1d: Configuring HTML converter properties...")
            // Configure HTML converter properties
            val converterProperties = com.itextpdf.html2pdf.ConverterProperties()
            converterProperties.setBaseUri("file://")
            Timber.d("   ✅ Converter properties configured")

            Timber.d("🔄 4.1e: Converting HTML to PDF (may take a few seconds)...")
            Timber.d("   Converting ${htmlContent.length} bytes of HTML...")

            // Convert HTML string to PDF
            val htmlBytes = htmlContent.toByteArray(Charsets.UTF_8)
            val htmlInputStream = java.io.ByteArrayInputStream(htmlBytes)

            com.itextpdf.html2pdf.HtmlConverter.convertToDocument(
                htmlInputStream,
                pdfDocument,
                converterProperties
            )
            Timber.d("   ✅ HTML parsed and converted to PDF document")
            Timber.d("   ✅ Page count: ${pdfDocument.numberOfPages}")

            Timber.d("🔄 4.1f: Closing and flushing PDF to disk...")
            pdfDocument.close()
            Timber.d("   ✅ PDF document closed and flushed")

            Timber.d("")
            Timber.d("════════════════════════════════════════════════════════════════════")
            Timber.d("✅ HTML-TO-PDF CONVERSION SUCCESSFUL")
            Timber.d("════════════════════════════════════════════════════════════════════")

            val fileSize = file.length()
            Timber.d("📦 OUTPUT PDF:")
            Timber.d("   File name: ${file.name}")
            Timber.d("   File path: ${file.absolutePath}")
            Timber.d("   File size: $fileSize bytes (${String.format("%.1f", fileSize / 1024.0)} KB)")
            Timber.d("   File exists: ${file.exists()}")

            if (fileSize == 0L) {
                Timber.e("❌ WARNING: PDF file is 0 bytes - may be empty or conversion failed silently!")
            } else if (fileSize < 5000) {
                Timber.w("⚠️  WARNING: PDF is very small (${String.format("%.1f", fileSize / 1024.0)} KB) - may contain minimal content")
            } else {
                Timber.d("✅ PDF file size looks reasonable")
            }

            Timber.d("════════════════════════════════════════════════════════════════════")

        } catch (e: Exception) {
            Timber.e(e, "❌ HTML-TO-PDF CONVERSION FAILED")
            Timber.e("   Error type: ${e::class.simpleName}")
            Timber.e("   Error message: ${e.message}")
            Timber.e("   Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")

            // Clean up incomplete file
            if (file.exists()) {
                file.delete()
                Timber.d("🧹 Deleted incomplete PDF file")
            }
            throw IllegalStateException("HTML-to-PDF conversion failed: ${e.message}", e)
        }

        return file
    }
}
