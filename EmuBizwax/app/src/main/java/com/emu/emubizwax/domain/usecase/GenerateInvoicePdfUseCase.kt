package com.emu.emubizwax.domain.usecase

import com.emu.emubizwax.domain.service.PdfGeneratorService
import java.io.File
import javax.inject.Inject

class GenerateInvoicePdfUseCase @Inject constructor(
    private val prepareInvoiceExportUseCase: PrepareInvoiceExportUseCase,
    private val pdfGeneratorService: PdfGeneratorService
) {
    suspend fun execute(invoiceId: Long, primaryColor: String, customerName: String): File {
        val exportData = prepareInvoiceExportUseCase.execute(invoiceId)
        return pdfGeneratorService.generateInvoice(exportData, primaryColor, customerName)
    }
}
