package com.emul8r.bizap.data.service

import android.content.Context
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.service.PdfGenerationService
import timber.log.Timber
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Invoice Theme Manager - Routes PDF generation based on selected theme.
 *
 * Manages the selection and instantiation of PDF generation strategies.
 * Supports:
 * - CANVAS: Existing Canvas-based PDF generation
 * - HTML_PDF: New HTML-to-PDF modern design
 *
 * This allows users to switch between themes and have their choice
 * automatically applied to all generated PDFs.
 */
@Singleton
class InvoiceThemeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val canvasPdfService: InvoicePdfService
) {
    companion object {
        private const val TAG = "InvoiceThemeManager"
    }

    private val htmlPdfService: HtmlPdfInvoiceService by lazy {
        HtmlPdfInvoiceService(context)
    }

    /**
     * Generate PDF using the appropriate theme/strategy.
     *
     * @param snapshot Invoice data to convert to PDF
     * @param settings User's invoice settings (includes theme preference and HTML style)
     * @param isQuote Whether this is a quote or invoice
     * @param overwriteExisting Whether to overwrite existing PDF
     * @return Generated PDF file
     */
    suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        settings: InvoiceSettings,
        isQuote: Boolean = false,
        overwriteExisting: Boolean = true
    ): File {
        return when (settings.selectedTheme) {
            InvoiceTheme.HTML_PDF -> {
                Timber.d("Generating PDF using HTML-to-PDF theme with style: ${settings.selectedHtmlStyle.displayName}")
                try {
                    // Create service instance with settings so it can access selectedHtmlStyle
                    val htmlService = HtmlPdfInvoiceService(context, settings)
                    htmlService.generatePdf(
                        snapshot = snapshot,
                        isQuote = isQuote,
                        overwriteExisting = overwriteExisting,
                        theme = InvoiceTheme.HTML_PDF
                    )
                } catch (e: Exception) {
                    Timber.e(e, "HTML-to-PDF generation failed, falling back to Canvas")
                    canvasPdfService.generatePdf(
                        snapshot = snapshot,
                        isQuote = isQuote,
                        overwriteExisting = overwriteExisting,
                        theme = InvoiceTheme.CANVAS
                    )
                }
            }
            InvoiceTheme.CANVAS -> {
                Timber.d("Generating PDF using Canvas theme")
                canvasPdfService.generatePdf(
                    snapshot = snapshot,
                    isQuote = isQuote,
                    overwriteExisting = overwriteExisting,
                    theme = InvoiceTheme.CANVAS
                )
            }
        }
    }

    /**
     * Get the service instance for a specific theme.
     * Useful for direct access to specific implementations.
     */
    fun getServiceForTheme(theme: InvoiceTheme): PdfGenerationService {
        return when (theme) {
            InvoiceTheme.HTML_PDF -> htmlPdfService
            InvoiceTheme.CANVAS -> canvasPdfService
        }
    }

    /**
     * Validate that a theme is available and working.
     */
    fun isThemeAvailable(theme: InvoiceTheme): Boolean {
        return try {
            when (theme) {
                InvoiceTheme.HTML_PDF -> true // Always available (HTML generation)
                InvoiceTheme.CANVAS -> true   // Always available
            }
        } catch (e: Exception) {
            Timber.e(e, "Theme availability check failed for $theme")
            false
        }
    }

    /**
     * Get available themes for the user to select from.
     */
    fun getAvailableThemes(): List<InvoiceTheme> {
        return listOf(
            InvoiceTheme.CANVAS,   // Traditional Canvas-based PDF
            InvoiceTheme.HTML_PDF  // Modern HTML-to-PDF design
        )
    }
}

