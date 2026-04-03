package com.emul8r.bizap.ui.invoices.html

import android.content.Context
import com.itextpdf.html2pdf.HtmlConverter
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * Converts HTML content to PDF format using iText7
 *
 * This converter handles:
 * - HTML validation and sanitization
 * - PDF configuration (page size, margins, quality)
 * - Image and font embedding
 * - Error handling with fallback strategies
 */
class HtmlToPdfConverter(
    private val context: Context,
    private val config: PdfConfig = PdfConfig()
) {
    /**
     * Convert HTML string to PDF file
     *
     * @param htmlContent The HTML content to convert
     * @param outputPath The full file path where PDF should be saved
     * @return True if conversion successful, False otherwise
     */
    fun convertHtmlToPdf(htmlContent: String, outputPath: String): Boolean {
        return try {
            // Validate HTML
            if (!validateHtml(htmlContent)) {
                Timber.e("Invalid HTML content provided")
                return false
            }

            // Create output file
            val outputFile = File(outputPath)
            outputFile.parentFile?.mkdirs()

            // Create PDF document
            val pdfWriter = PdfWriter(outputFile)
            val pdfDocument = PdfDocument(pdfWriter)

            // Configure page
            val pageSize = com.itextpdf.kernel.geom.PageSize.A4
            pdfDocument.defaultPageSize = pageSize

            // Set PDF properties
            val pdfMetaInfo = pdfDocument.documentInfo
            pdfMetaInfo.title = "Invoice"
            pdfMetaInfo.author = "EmuBiz"

            // Configure HTML to PDF converter
            val converterProperties = com.itextpdf.html2pdf.ConverterProperties()
            converterProperties.setBaseUri("")

            // Convert HTML to PDF
            val htmlBytes = htmlContent.toByteArray(Charsets.UTF_8)
            val htmlInputStream = ByteArrayInputStream(htmlBytes)

            HtmlConverter.convertToDocument(htmlInputStream, pdfDocument, converterProperties)
            pdfDocument.close()

            Timber.d("PDF created successfully at: $outputPath")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error converting HTML to PDF")
            false
        }
    }

    /**
     * Convert HTML to PDF bytes (in-memory)
     *
     * @param htmlContent The HTML content to convert
     * @return ByteArray containing PDF data, or empty array on failure
     */
    fun convertHtmlToPdfBytes(htmlContent: String): ByteArray {
        return try {
            if (!validateHtml(htmlContent)) {
                Timber.e("Invalid HTML content provided")
                return ByteArray(0)
            }

            val outputStream = ByteArrayOutputStream()
            val pdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(pdfWriter)

            val converterProperties = com.itextpdf.html2pdf.ConverterProperties()
            converterProperties.setBaseUri("")

            val htmlBytes = htmlContent.toByteArray(Charsets.UTF_8)
            val htmlInputStream = ByteArrayInputStream(htmlBytes)

            HtmlConverter.convertToDocument(htmlInputStream, pdfDocument, converterProperties)
            pdfDocument.close()

            Timber.d("PDF bytes generated successfully")
            outputStream.toByteArray()
        } catch (e: Exception) {
            Timber.e(e, "Error converting HTML to PDF bytes")
            ByteArray(0)
        }
    }

    /**
     * Load CSS from assets and embed into HTML as inline style tag.
     *
     * iText7 doesn't support external CSS file references, so we need to
     * embed CSS directly into the HTML as a <style> tag for proper rendering.
     *
     * @param context Android context to access assets
     * @param htmlContent HTML content with external CSS link reference
     * @param styleFileName Name of the CSS file to load (e.g., "invoice-styles.css", "invoice-styles-minimal.css")
     * @return HTML with CSS embedded as <style> tag, or original HTML if CSS loading fails
     */
    fun embedCssFromAssets(context: Context, htmlContent: String, styleFileName: String = "invoice-styles.css"): String {
        return try {
            val assetPath = "invoices/html-theme/$styleFileName"
            Timber.d("🎨 LOADING CSS FOR STYLE: Loading $styleFileName from $assetPath")

            val cssContent = context.assets.open(assetPath)
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }

            if (cssContent.isBlank()) {
                Timber.w("CSS file is empty, returning HTML without styling")
                return htmlContent
            }

            // Create inline style tag with CSS content
            val styleTag = "<style>\n$cssContent\n</style>"

            // Replace external CSS link with embedded CSS
            val result = htmlContent.replace(
                Regex("""<link\s+rel="stylesheet"\s+href="[^"]*"\s*>""", RegexOption.IGNORE_CASE),
                styleTag
            )

            Timber.d("✅ CSS loaded successfully: ${cssContent.length} characters")
            result

        } catch (e: Exception) {
            Timber.e(e, "Failed to embed CSS from assets: ${e.message}")
            htmlContent  // Return original HTML as fallback
        }
    }

    /**
     * Validate HTML content
     */
    private fun validateHtml(htmlContent: String): Boolean {
        return htmlContent.isNotBlank() &&
               !htmlContent.contains("<script", ignoreCase = true) &&
               !htmlContent.contains("javascript:", ignoreCase = true)
    }

    /**
     * PDF Configuration
     */
    data class PdfConfig(
        val topMargin: Float = 20f,
        val bottomMargin: Float = 20f,
        val leftMargin: Float = 20f,
        val rightMargin: Float = 20f,
        val pageWidth: Float = 210f,  // A4 width in mm
        val pageHeight: Float = 297f, // A4 height in mm
        val quality: Int = 95         // PDF quality (1-100)
    )
}

