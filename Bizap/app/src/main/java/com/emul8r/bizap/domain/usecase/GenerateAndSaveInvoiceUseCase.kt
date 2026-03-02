package com.emul8r.bizap.domain.usecase

import com.emul8r.bizap.data.local.entities.GeneratedDocumentEntity
import com.emul8r.bizap.data.service.InvoicePdfService
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceSnapshot
import com.emul8r.bizap.domain.repository.DocumentRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenerateAndSaveInvoiceUseCase @Inject constructor(
    private val pdfService: InvoicePdfService,
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
            // 1. Generate the physical file from the Snapshot
            generatedFile = pdfService.generateInvoice(
                snapshot = snapshot,
                isQuote = isQuote,
                overwriteExisting = overwriteExisting
            )

            // 2. Record the generation in the database
            val fileType = if (isQuote) "Quote" else "Invoice"
            documentRepository.insertDocument(
                GeneratedDocumentEntity(
                    relatedInvoiceId = invoice.id,
                    fileName = generatedFile.name,
                    absolutePath = generatedFile.absolutePath,
                    fileType = fileType
                )
            )

            Result.success(generatedFile)
        } catch (e: Exception) {
            // 3. FAIL-SAFE ROLLBACK: Cleanup orphaned file if DB insert fails
            generatedFile?.let { if (it.exists()) it.delete() }
            Result.failure(e)
        }
    }
}

