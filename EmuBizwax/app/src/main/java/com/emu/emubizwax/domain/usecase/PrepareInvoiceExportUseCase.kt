package com.emu.emubizwax.domain.usecase

import com.emu.emubizwax.domain.model.ExportData
import com.emu.emubizwax.domain.repository.InvoiceRepository
import com.emu.emubizwax.domain.repository.UserRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class PrepareInvoiceExportUseCase @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val userRepo: UserRepository
) {
    suspend fun execute(invoiceId: Long): ExportData {
        val invoiceData = invoiceRepo.getInvoiceWithItems(invoiceId).firstOrNull()
            ?: throw Exception("Invoice not found")
        val businessInfo = userRepo.getUserProfile().firstOrNull()
            ?: throw Exception("User profile not found")

        return ExportData(
            header = businessInfo,
            invoice = invoiceData.invoice,
            items = invoiceData.items
        )
    }
}
