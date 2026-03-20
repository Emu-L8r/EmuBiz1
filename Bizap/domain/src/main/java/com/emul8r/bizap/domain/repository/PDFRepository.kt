package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.Invoice

/**
 * Domain-level contract for PDF generation and storage operations.
 *
 * Constraints:
 * - Invoices must have at least 1 line item to generate a PDF.
 * - Saved files must not exceed [MAX_PDF_SIZE_BYTES] bytes.
 */
interface PDFRepository {

    /**
     * Generates a PDF for an invoice.
     *
     * @param invoice The invoice to render as a PDF.
     * @return [Result.success] with the PDF bytes on success, or [Result.failure] if the invoice
     * has no line items or PDF generation fails.
     */
    suspend fun generateInvoicePdf(invoice: Invoice): Result<ByteArray>

    /**
     * Generates a PDF for a quote (same as invoice but renders as quote).
     *
     * @param invoice The quote/invoice to render as a PDF.
     * @return [Result.success] with the PDF bytes on success, or [Result.failure] if the invoice
     * has no line items or PDF generation fails.
     */
    suspend fun generateQuotePdf(invoice: Invoice): Result<ByteArray>

    /**
     * Saves a PDF byte array to the device's Downloads folder.
     *
     * @param pdf The PDF content to save.
     * @param fileName The desired file name (without directory path).
     * @return [Result.success] with the absolute file path on success, or [Result.failure] if
     * the file exceeds [MAX_PDF_SIZE_BYTES] or the save operation fails.
     */
    suspend fun savePdfToDownloads(pdf: ByteArray, fileName: String): Result<String>

    /**
     * Exports a list of invoices as a single ZIP archive containing their PDFs.
     *
     * @param invoices The list of invoices to export.
     * @return [Result.success] with the ZIP bytes on success, or [Result.failure] if any invoice
     * has no line items, the archive exceeds [MAX_PDF_SIZE_BYTES], or generation fails.
     */
    suspend fun exportInvoicesAsZip(invoices: List<Invoice>): Result<ByteArray>

    companion object {
        /** Maximum allowed file size in bytes (50 MB). */
        const val MAX_PDF_SIZE_BYTES = 50 * 1024 * 1024L
    }
}
