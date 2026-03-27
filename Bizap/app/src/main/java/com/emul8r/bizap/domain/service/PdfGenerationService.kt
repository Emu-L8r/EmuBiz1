package com.emul8r.bizap.domain.service

import com.emul8r.bizap.domain.model.InvoiceSnapshot
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
 *
 * **Implementation:**
 * Implemented by `InvoicePdfService` in the data layer,
 * but injected as this interface type to maintain architecture purity.
 */
interface PdfGenerationService {

    /**
     * Generates a PDF invoice or quote from an invoice snapshot.
     *
     * @param snapshot         Invoice snapshot containing all data needed for PDF rendering
     * @param isQuote          If true, generates a Quote PDF; if false, generates an Invoice PDF
     * @param overwriteExisting If true, overwrites existing PDF for this invoice; if false, versions it
     *
     * @return A [File] object pointing to the generated PDF file in internal storage
     *
     * @throws IllegalStateException if the PDF file is empty or inaccessible after generation
     * @throws Exception for other PDF generation failures (rendering errors, I/O errors, etc.)
     */
    suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean = true
    ): File
}

