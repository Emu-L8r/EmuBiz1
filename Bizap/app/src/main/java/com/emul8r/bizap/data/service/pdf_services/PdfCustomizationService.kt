package com.emul8r.bizap.data.service.pdf_services

import android.content.Context
import com.emul8r.bizap.data.service.html.HtmlToPdfConverter
import com.emul8r.bizap.domain.model.*
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Phase 3C: PDF Customization Service
 *
 * Complete PDF generation service that integrates:
 * - Dynamic CSS generation from user settings
 * - HTML template rendering
 * - PDF file generation with iText7
 * - File management and caching
 *
 * This is the production service used to generate actual invoice PDFs
 * with user-customized appearance (colors, spacing, visual accents, etc.)
 */
@Singleton
class PdfCustomizationService @Inject constructor(
    private val context: Context,
    private val cssGenerator: CssGenerator
) {

    private val htmlToPdfConverter = HtmlToPdfConverter(context)

    /**
     * Generate a customized PDF from an invoice snapshot with user settings.
     *
     * @param invoiceHtml Pre-rendered invoice HTML (from PageLayout)
     * @param settings User's PDF customization settings
     * @param outputPath Where to save the PDF file
     * @return True if successful, false otherwise
     */
    fun generateCustomizedPdf(
        invoiceHtml: String,
        settings: PdfCustomizationSettings,
        outputPath: String
    ): Boolean {
        return try {
            Timber.d("Phase 3C: Generating customized PDF with settings: $settings")

            // Step 1: Generate dynamic CSS from settings
            val css = cssGenerator.generateCss(
                colorScheme = settings.colorScheme,
                spacingProfile = settings.spacingProfile,
                visualAccents = settings.visualAccents,
                totalBoxStyle = settings.totalBoxStyle,
                enableAlternatingRows = settings.enableAlternatingRows,
                enableDividers = settings.enableDividers,
                enableGradientHeader = settings.enableGradientHeader
            )

            Timber.d("✅ CSS generated: ${css.length} characters")

            // Step 2: Inject CSS into HTML
            val htmlWithCss = injectCssIntoHtml(invoiceHtml, css)

            // Step 3: Convert to PDF
            val success = htmlToPdfConverter.convertHtmlToPdf(
                htmlContent = htmlWithCss,
                outputPath = outputPath
            )

            if (success) {
                val file = File(outputPath)
                Timber.d("✅ PDF created successfully: ${file.absolutePath} (${file.length()} bytes)")
            } else {
                Timber.e("❌ PDF conversion failed")
            }

            success
        } catch (e: Exception) {
            Timber.e(e, "❌ Exception during PDF generation")
            false
        }
    }

    /**
     * Generate a customized PDF and return the file object.
     *
     * @param invoiceHtml Pre-rendered invoice HTML
     * @param settings User's PDF customization settings
     * @param fileName Name for the output PDF file
     * @return PDF File if successful, null otherwise
     */
    fun generateCustomizedPdfFile(
        invoiceHtml: String,
        settings: PdfCustomizationSettings,
        fileName: String = "invoice_${System.currentTimeMillis()}.pdf"
    ): File? {
        return try {
            // Create output directory in app's cache/files
            val outputDir = File(context.cacheDir, "generated_pdfs")
            outputDir.mkdirs()

            // Create output file
            val outputFile = File(outputDir, fileName)

            // Generate PDF
            val success = generateCustomizedPdf(invoiceHtml, settings, outputFile.absolutePath)

            if (success && outputFile.exists()) {
                Timber.d("✅ PDF file ready: ${outputFile.absolutePath}")
                outputFile
            } else {
                Timber.e("Failed to generate PDF file")
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception creating PDF file")
            null
        }
    }

    /**
     * Inject dynamic CSS into HTML content.
     * Replaces or appends the <style> tag with generated CSS.
     */
    private fun injectCssIntoHtml(html: String, css: String): String {
        return if (html.contains("<style>")) {
            // Replace existing style tag
            html.replace(
                Regex("<style>.*?</style>", RegexOption.DOT_MATCHES_ALL),
                "<style>\n$css\n</style>"
            )
        } else if (html.contains("</head>")) {
            // Add new style tag before </head>
            html.replace(
                "</head>",
                "<style>\n$css\n</style>\n</head>"
            )
        } else {
            // Fallback: add at beginning (shouldn't happen with proper HTML)
            "<style>\n$css\n</style>\n$html"
        }
    }

    /**
     * Validate that PDF customization settings are reasonable.
     *
     * @return Error message if invalid, null if valid
     */
    fun validateSettings(settings: PdfCustomizationSettings): String? {
        return when {
            settings.colorScheme.primaryHex.isBlank() -> "Color scheme missing primary color"
            else -> null  // spacingProfile and totalBoxStyle are non-nullable, always present
        }
    }

    /**
     * Clean up old generated PDF files.
     * Call periodically to prevent storage bloat.
     *
     * @param maxAgeMs Files older than this are deleted (default: 7 days)
     */
    fun cleanupOldPdfs(maxAgeMs: Long = 7 * 24 * 60 * 60 * 1000) {
        try {
            val pdfDir = File(context.cacheDir, "generated_pdfs")

            if (pdfDir.exists()) {
                val now = System.currentTimeMillis()
                var deletedCount = 0

                pdfDir.listFiles()?.forEach { file ->
                    if (now - file.lastModified() > maxAgeMs) {
                        if (file.delete()) {
                            deletedCount++
                            Timber.d("Deleted old PDF: ${file.name}")
                        }
                    }
                }

                if (deletedCount > 0) {
                    Timber.d("Cleanup: Deleted $deletedCount old PDF files")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up old PDF files")
        }
    }

    /**
     * Get total size of generated PDF cache.
     * Useful for monitoring storage usage.
     */
    fun getPdfCacheSize(): Long {
        return try {
            val pdfDir = File(context.cacheDir, "generated_pdfs")
            if (pdfDir.exists()) {
                pdfDir.listFiles()?.sumOf { it.length() } ?: 0L
            } else {
                0L
            }
        } catch (e: Exception) {
            Timber.e(e, "Error calculating PDF cache size")
            0L
        }
    }

    /**
     * Get list of all generated PDFs (for debugging/management)
     */
    fun getGeneratedPdfs(): List<File> {
        return try {
            val pdfDir = File(context.cacheDir, "generated_pdfs")
            pdfDir.listFiles()?.toList() ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Error listing generated PDFs")
            emptyList()
        }
    }
}

/**
 * Data class holding all PDF customization settings from user preferences.
 * Can be serialized and persisted.
 */
data class PdfCustomizationSettings(
    val colorScheme: ColorScheme = ColorScheme.PROFESSIONAL,
    val spacingProfile: SpacingProfile = SpacingProfile.NORMAL,
    val totalBoxStyle: TotalBoxStyle = TotalBoxStyle.SUBTLE_BACKGROUND,
    val visualAccents: VisualAccents = VisualAccents.default(),
    val enableAlternatingRows: Boolean = true,
    val enableDividers: Boolean = true,
    val enableGradientHeader: Boolean = true
)

