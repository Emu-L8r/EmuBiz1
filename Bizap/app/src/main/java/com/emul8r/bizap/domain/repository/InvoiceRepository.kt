package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    suspend fun saveInvoice(invoice: Invoice): Long
    fun getInvoiceWithItemsById(id: Long): Flow<Invoice?>
    fun getAllInvoicesWithItems(): Flow<List<Invoice>>
    fun getBusinessProfile(): Flow<BusinessProfile>
    suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus)
    suspend fun updatePdfPath(invoiceId: Long, pdfPath: String)
    suspend fun deleteInvoice(id: Long)
}
