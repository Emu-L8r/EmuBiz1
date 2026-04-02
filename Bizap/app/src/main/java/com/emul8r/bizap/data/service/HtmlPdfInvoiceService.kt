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
        Timber.d("📝 HtmlPdfInvoiceService.generatePdf() START")
        Timber.d("   isQuote: $isQuote")
        Timber.d("   theme: ${theme?.name ?: "NULL"}")
        Timber.d("   selectedHtmlStyle: ${settings?.selectedHtmlStyle?.displayName ?: "NULL (will use MODERN)"}")

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
     * @return CSS content as String
     */
    private fun loadSelectedStyleCss(): String {
        val selectedStyle = settings?.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN
        val cssFileName = selectedStyle.styleFile

        Timber.d("Loading CSS for style: ${selectedStyle.displayName} (file: $cssFileName)")

        return try {
            val inputStream = context.assets.open("invoices/html-theme/$cssFileName")
            inputStream.use { stream ->
                InputStreamReader(stream).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load CSS file: $cssFileName, falling back to MODERN style")
            // Fallback to modern style if selected style file doesn't exist
            try {
                val inputStream = context.assets.open("invoices/html-theme/invoice-styles.css")
                inputStream.use { stream ->
                    InputStreamReader(stream).use { reader ->
                        reader.readText()
                    }
                }
            } catch (fallbackError: Exception) {
                Timber.e(fallbackError, "Failed to load fallback CSS file")
                ""  // Return empty CSS if all fails
            }
        }
    }

    /**
     * Embed CSS content into HTML as <style> tag.
     *
     * Replaces the placeholder style tag with the actual CSS content.
     *
     * @param htmlContent HTML document
     * @param cssContent CSS to embed
     * @return HTML with embedded CSS
     */
    private fun embedCssIntoHtml(htmlContent: String, cssContent: String): String {
        // Find and replace the style tag placeholder
        val styleTagStart = htmlContent.indexOf("<style>")
        val styleTagEnd = htmlContent.indexOf("</style>", styleTagStart) + "</style>".length

        return if (styleTagStart >= 0 && styleTagEnd > styleTagStart) {
            htmlContent.substring(0, styleTagStart) +
            "<style>\n$cssContent\n</style>" +
            htmlContent.substring(styleTagEnd)
        } else {
            htmlContent  // Return unchanged if style tag not found
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
        val documentType = if (isQuote) "QUOTE" else "INVOICE"

        // Convert cents to dollars
        val subtotalDollars = snapshot.subtotal / 100.0
        val taxDollars = snapshot.taxAmount / 100.0
        val totalDollars = snapshot.totalAmount / 100.0

        val itemsHtml = snapshot.items.joinToString("\n") { item ->
            val amountDollars = item.total / 100.0
            val unitPriceDollars = item.unitPrice / 100.0
            """
                <tr class="table-row">
                    <td class="col-description">${item.description}</td>
                    <td class="col-quantity">${String.format("%.2f", item.quantity)}</td>
                    <td class="col-unit-price">${String.format("${'$'}%.2f", unitPriceDollars)}</td>
                    <td class="col-amount">${String.format("${'$'}%.2f", amountDollars)}</td>
                </tr>
            """.trimIndent()
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$documentType ${snapshot.invoiceId}</title>
                <!-- CSS will be injected here by embedCssIntoHtml() -->
                <style>
                    /* PLACEHOLDER - Will be replaced with actual CSS from invoice-styles*.css files */
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
        """.trimIndent()
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
            Timber.d("🎨 Starting HTML-to-PDF conversion (iText7): ${file.name}")

            // Use iText7 to convert HTML to real PDF binary
            val pdfWriter = com.itextpdf.kernel.pdf.PdfWriter(file)
            val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)

            // Configure page (A4)
            val pageSize = com.itextpdf.kernel.geom.PageSize.A4
            pdfDocument.defaultPageSize = pageSize

            // Set PDF metadata
            val pdfMetaInfo = pdfDocument.documentInfo
            pdfMetaInfo.title = "Invoice"
            pdfMetaInfo.author = "Bizap"
            pdfMetaInfo.creator = "Bizap HTML-to-PDF"

            // Configure HTML converter properties
            val converterProperties = com.itextpdf.html2pdf.ConverterProperties()
            converterProperties.setBaseUri("file://")

            // Convert HTML string to PDF
            val htmlBytes = htmlContent.toByteArray(Charsets.UTF_8)
            val htmlInputStream = java.io.ByteArrayInputStream(htmlBytes)

            Timber.d("🔄 Converting ${htmlBytes.size} bytes of HTML to PDF...")
            com.itextpdf.html2pdf.HtmlConverter.convertToDocument(
                htmlInputStream,
                pdfDocument,
                converterProperties
            )

            pdfDocument.close()

            Timber.d("✅ HTML-to-PDF conversion SUCCESSFUL: ${file.name}")
            Timber.d("✅ Real PDF file created with embedded HTML styling")

        } catch (e: Exception) {
            Timber.e(e, "❌ HTML-to-PDF conversion FAILED")
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


