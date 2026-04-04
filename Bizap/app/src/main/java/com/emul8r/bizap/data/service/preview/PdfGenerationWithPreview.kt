package com.emul8r.bizap.data.service.preview

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.emul8r.bizap.data.service.HtmlPdfInvoiceService
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceSettings
import com.emul8r.bizap.domain.model.InvoiceTheme
import timber.log.Timber
import java.io.File

/**
 * PDF Generation with Preview Mode Support
 *
 * Handles switching between real invoice data and placeholder preview data.
 * Used during PDF generation when preview mode is enabled.
 */
@RequiresApi(Build.VERSION_CODES.KITKAT)
class PdfGenerationWithPreview(
    private val context: Context,
    private val settings: InvoiceSettings
) {
    /**
     * Generate PDF, using preview data if enabled in settings.
     *
     * @param snapshot Real invoice data
     * @param isQuote Whether this is a quote or invoice
     * @param overwriteExisting Whether to overwrite existing file
     * @return Generated PDF file
     */
    suspend fun generatePdfWithPreviewSupport(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean = false,
        overwriteExisting: Boolean = true
    ): File {
        // PHASE 3: Use preview data if enabled
        val invoiceToUse = if (settings.previewWithPlaceholder) {
            Timber.d("🎨 Preview mode enabled - using placeholder data instead of real data")
            PlaceholderInvoiceGenerator.generatePreviewInvoice()
        } else {
            Timber.d("📝 Using real invoice data")
            snapshot
        }

        // Generate PDF using selected theme/engine
        val htmlPdfService = HtmlPdfInvoiceService(context, settings)
        return htmlPdfService.generatePdf(invoiceToUse, isQuote, overwriteExisting, settings.selectedTheme)
    }

    /**
     * Generate preview PDF for the settings UI.
     * Always uses placeholder data.
     *
     * @return Generated PDF file with sample data
     */
    suspend fun generatePreviewPdf(isQuote: Boolean = false): File {
        Timber.d("📸 Generating preview PDF with placeholder data")
        val previewInvoice = PlaceholderInvoiceGenerator.generatePreviewInvoice()
        val htmlPdfService = HtmlPdfInvoiceService(context, settings)
        return htmlPdfService.generatePdf(previewInvoice, isQuote, true, settings.selectedTheme)
    }

    /**
     * Generate minimal preview (for quick testing).
     */
    suspend fun generateMinimalPreviewPdf(): File {
        Timber.d("📸 Generating minimal preview PDF")
        val minimalPreview = PlaceholderInvoiceGenerator.generateMinimalPreviewInvoice()
        val htmlPdfService = HtmlPdfInvoiceService(context, settings)
        return htmlPdfService.generatePdf(minimalPreview, false, true, settings.selectedTheme)
    }
}

