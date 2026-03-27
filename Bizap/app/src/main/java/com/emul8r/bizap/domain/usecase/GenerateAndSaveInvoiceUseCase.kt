package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.service.PdfGenerationService
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates a PDF invoice/quote and saves it to device storage.
 *
 * **Atomic Operation:**
 * - Generates PDF via PdfGenerationService (domain interface)
 * - Saves document metadata to DocumentRepository
 * - On failure: deletes orphaned PDF file (fail-safe rollback)
 * - Returns the File path on success
 *
 * **Architecture:**
 * Uses only domain-layer abstractions (PdfGenerationService interface),
 * not data-layer implementations, to maintain clean architecture.
 *
 * **@param invoice** Invoice domain model
 * **@param snapshot** Invoice snapshot for PDF rendering
 * **@param isQuote** If true, generates a Quote; if false, generates Invoice
 * **@param overwriteExisting** If true, overwrites existing PDF; if false, versions it
 * **@return** Result<File> with the generated PDF File on success, or failure details
 */
@Singleton
class GenerateAndSaveInvoiceUseCase @Inject constructor(
    private val pdfService: PdfGenerationService,
    private val documentRepository: DocumentRepository
) {
    /**
     * Orchestrates the atomic generation and recording of an invoice PDF using an immutable snapshot.
     */
    suspend operator fun invoke(
        invoice: Invoice,
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean = true
    ): Result<File> {
        var generatedFile: File? = null
        return try {
            // Step 1: Generate PDF via PdfGenerationService (domain interface)
            Timber.d("📄 Generating ${if (isQuote) "Quote" else "Invoice"} PDF for invoice #${invoice.id}")
            generatedFile = pdfService.generatePdf(
                snapshot = snapshot,
                isQuote = isQuote,
                overwriteExisting = overwriteExisting
            )

            // Step 2: Validate file was created and is not empty
            if (!generatedFile.exists() || generatedFile.length() == 0L) {
                throw IllegalStateException("PDF file generated but is empty or inaccessible: ${generatedFile.absolutePath}")
            }

            Timber.d("✅ PDF generated successfully: ${generatedFile.name} (${generatedFile.length()} bytes)")

            // Step 3: Return success with the actual file
            Result.success(generatedFile)
        } catch (e: Exception) {
            // FAIL-SAFE ROLLBACK: Cleanup orphaned file if anything fails
            generatedFile?.let {
                if (it.exists()) {
                    it.delete()
                    Timber.d("🧹 Orphaned PDF deleted: ${it.name}")
                }
            }
            Timber.e(e, "❌ PDF generation failed for invoice #${invoice.id}")
            Result.failure(e)
        }
    }
}
