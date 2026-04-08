package com.emul8r.bizap.domain.service

import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.model.InvoiceTheme
import com.emul8r.bizap.domain.pdf.HeaderSection
import com.emul8r.bizap.domain.pdf.SubheaderSection
import com.emul8r.bizap.domain.pdf.PdfColorScheme
import java.io.File

/**
 * Domain-level contract for PDF generation.
 *
 * **Purpose:**
 * Abstracts PDF generation logic away from the data layer,
 * allowing domain use cases to depend only on domain interfaces.
 *
 * **Responsibility:**
 * Generate PDF files from invoice snapshots and return the File path.
 * Handles internal storage management and file validation.
 * Supports multiple themes (Canvas, HTML-to-PDF, etc.).
 *
 * **Implementation:**
 * Implemented by `InvoicePdfService` in the data layer,
 * but injected as this interface type to maintain architecture purity.
 */
interface PdfGenerationService {

    /**
     * Generates BOTH Invoice and Quote PDFs from the same data
     *
     * This is the new unified method used by both Modern (GUI2) and Classic (GUI1) interfaces.
     * Generates two professional PDFs simultaneously:
     * - invoice_XXXX.pdf (labeled "INVOICE")
     * - quote_XXXX.pdf (labeled "QUOTE")
     *
     * @param snapshot         Invoice snapshot containing all data needed for PDF rendering
     * @param header          Optional header section (e.g., company name)
     * @param subheader       Subheader section (e.g., location, dept, shop number)
     * @param overwriteExisting If true, overwrites existing PDFs for this invoice
     * @param theme           Optional theme selection (CANVAS or HTML_PDF)
     * @param colorScheme     Optional custom color scheme (uses app theme if null)
     *
     * @return A [Pair] of (invoicePdf, quotePdf) files
     *
     * @throws IllegalStateException if either PDF file is empty or inaccessible
     * @throws Exception for other PDF generation failures
     */
    suspend fun generateDualPdf(
        snapshot: InvoiceSnapshot,
        header: HeaderSection? = null,
        subheader: SubheaderSection = SubheaderSection(),
        overwriteExisting: Boolean = true,
        theme: InvoiceTheme? = null,
        colorScheme: PdfColorScheme? = null
    ): Pair<File, File>

    /**
     * Generates a PDF invoice or quote from an invoice snapshot.
     *
     * Legacy/backward-compatible method. For new code, use generateDualPdf().
     *
     * @param snapshot         Invoice snapshot containing all data needed for PDF rendering
     * @param isQuote          If true, generates a Quote PDF; if false, generates an Invoice PDF
     * @param overwriteExisting If true, overwrites existing PDF for this invoice; if false, versions it
     * @param theme            Optional theme selection (CANVAS or HTML_PDF). Defaults to CANVAS if null.
     *
     * @return A [File] object pointing to the generated PDF file in internal storage
     *
     * @throws IllegalStateException if the PDF file is empty or inaccessible after generation
     * @throws Exception for other PDF generation failures (rendering errors, I/O errors, etc.)
     */
    suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean = true,
        theme: InvoiceTheme? = null
    ): File
}
