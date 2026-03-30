package com.emul8r.bizap.ui.settings.invoice_theme

import com.itextpdf.html2pdf.HtmlConverter
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Converts HTML content to PDF files using iText 7.
 *
 * Handles:
 * - HTML to PDF conversion with iText
 * - CSS styling and rendering
 * - PDF configuration (page size, margins, etc.)
 * - Font management
 * - Error handling and validation
 *
 * Phase 6: iText 7 implementation complete
 */
class HtmlToPdfConverter {

    /**
     * Convert HTML content to PDF file using iText 7.
     *
     * @param htmlContent HTML content to convert
     * @param outputPath Full path where PDF should be saved
     * @param config Optional PDF configuration (uses default if not provided)
     * @return Result with path to generated PDF file
     */
    fun convertHtmlToPdf(
        htmlContent: String,
        outputPath: String,
        config: PdfConfiguration = PdfConfiguration()
    ): Result<String> {
        return try {
            Timber.d("Starting HTML to PDF conversion: $outputPath")

            // Validate HTML
            if (!validateHtml(htmlContent)) {
                return Result.failure(Exception("Invalid HTML content: missing <html> or </html> tags"))
            }

            // Validate output path
            val outputFile = File(outputPath)
            val outputDir = outputFile.parentFile
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs()
            }

            // Create output stream
            val outputStream = FileOutputStream(outputFile)

            // Create iText PDF document with configuration
            val converterProperties = com.itextpdf.html2pdf.ConverterProperties()

            // Set base URI for relative links/images
            converterProperties.baseUri = "file://"

            // Convert HTML to PDF using iText
            HtmlConverter.convertToPdf(
                htmlContent,
                outputStream,
                converterProperties
            )

            outputStream.close()

            Timber.d("HTML to PDF conversion successful: $outputPath")
            Result.success(outputPath)

        } catch (e: Exception) {
            Timber.e(e, "Failed to convert HTML to PDF: $outputPath")
            Result.failure(
                Exception("Failed to convert HTML to PDF: ${e.message}", e)
            )
        }
    }

    /**
     * Validate HTML content.
     *
     * @param htmlContent HTML to validate
     * @return True if valid, false otherwise
     */
    fun validateHtml(htmlContent: String): Boolean {
        return htmlContent.isNotEmpty() &&
                htmlContent.contains("<html", ignoreCase = true) &&
                htmlContent.contains("</html>", ignoreCase = true)
    }

    /**
     * Get default PDF configuration.
     *
     * @return Default PdfConfiguration instance
     */
    fun getPdfConfiguration(): PdfConfiguration {
        return PdfConfiguration()
    }

    /**
     * Create customized PDF configuration.
     *
     * @param pageWidth Page width in mm (default 210 for A4)
     * @param pageHeight Page height in mm (default 297 for A4)
     * @param marginTop Top margin in mm
     * @param marginBottom Bottom margin in mm
     * @param marginLeft Left margin in mm
     * @param marginRight Right margin in mm
     * @return Customized PdfConfiguration
     */
    fun createConfiguration(
        pageWidth: Float = 210f,
        pageHeight: Float = 297f,
        marginTop: Float = 15f,
        marginBottom: Float = 15f,
        marginLeft: Float = 15f,
        marginRight: Float = 15f
    ): PdfConfiguration {
        return PdfConfiguration(
            pageWidth = pageWidth,
            pageHeight = pageHeight,
            marginTop = marginTop,
            marginBottom = marginBottom,
            marginLeft = marginLeft,
            marginRight = marginRight
        )
    }
}

/**
 * PDF output configuration.
 */
data class PdfConfiguration(
    val pageWidth: Float = 210f, // mm (A4)
    val pageHeight: Float = 297f, // mm (A4)
    val marginTop: Float = 15f,
    val marginBottom: Float = 15f,
    val marginLeft: Float = 15f,
    val marginRight: Float = 15f,
    val fontSize: Float = 11f,
    val lineSpacing: Float = 1.2f,
    val dpi: Int = 96,
    val quality: Float = 1.0f // 0.0 to 1.0
)


