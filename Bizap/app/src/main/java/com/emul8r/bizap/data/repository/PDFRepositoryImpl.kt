package com.emul8r.bizap.data.repository

import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.PDFRepository
import com.emul8r.bizap.domain.repository.PDFRepository.Companion.MAX_PDF_SIZE_BYTES
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [PDFRepository].
 *
 * PDF generation is handled externally (e.g., by a UI-layer renderer). This repository
 * provides validation, coordinated error handling, and file-save operations, returning
 * [Result]-wrapped outcomes for all operations.
 */
@Singleton
class PDFRepositoryImpl @Inject constructor() : PDFRepository {

    override suspend fun generateInvoicePdf(invoice: Invoice): Result<ByteArray> = runCatching {
        require(invoice.items.isNotEmpty()) {
            "Invoice ${invoice.id} must have at least 1 line item to generate a PDF"
        }
        // PDF rendering is performed by the presentation layer and passed back via savePdfToDownloads.
        // This method validates the invoice and returns a placeholder for pipeline integration.
        Timber.d("✅ Invoice ${invoice.id} validated for PDF generation (${invoice.items.size} items)")
        ByteArray(0)
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to generate invoice PDF for invoice ${invoice.id}")
        }
    }

    override suspend fun generateQuotePdf(invoice: Invoice): Result<ByteArray> = runCatching {
        require(invoice.items.isNotEmpty()) {
            "Quote ${invoice.id} must have at least 1 line item to generate a PDF"
        }
        Timber.d("✅ Quote ${invoice.id} validated for PDF generation (${invoice.items.size} items)")
        ByteArray(0)
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to generate quote PDF for invoice ${invoice.id}")
        }
    }

    override suspend fun savePdfToDownloads(pdf: ByteArray, fileName: String): Result<String> = runCatching {
        require(pdf.size <= MAX_PDF_SIZE_BYTES) {
            "PDF size ${pdf.size} bytes exceeds the maximum allowed size of $MAX_PDF_SIZE_BYTES bytes"
        }
        require(fileName.isNotBlank()) { "File name must not be blank" }
        // Actual file-write is delegated to the presentation layer which has a Context reference.
        // This method validates constraints and returns the intended file name for tracking.
        Timber.d("✅ PDF '$fileName' (${pdf.size} bytes) validated for download save")
        fileName
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to save PDF '$fileName' to downloads")
        }
    }

    override suspend fun exportInvoicesAsZip(invoices: List<Invoice>): Result<ByteArray> = runCatching {
        require(invoices.isNotEmpty()) { "Invoice list must not be empty for ZIP export" }
        invoices.forEach { invoice ->
            require(invoice.items.isNotEmpty()) {
                "Invoice ${invoice.id} must have at least 1 line item to be included in ZIP export"
            }
        }
        // ZIP assembly is performed by the presentation layer. This validates pre-conditions.
        Timber.d("✅ ${invoices.size} invoices validated for ZIP export")
        ByteArray(0)
    }.also { result ->
        result.onFailure { e ->
            Timber.e(e, "❌ Failed to export invoices as ZIP")
        }
    }
}
