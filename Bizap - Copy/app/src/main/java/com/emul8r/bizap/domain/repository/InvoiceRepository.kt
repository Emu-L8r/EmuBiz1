package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>
    fun searchInvoices(query: String, status: InvoiceStatus?): Flow<List<InvoiceWithItems>>
    fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceWithItems>>
    suspend fun saveInvoice(
        customer: CustomerEntity,
        items: List<LineItem>,
        header: String,
        subheader: String,
        notes: String,
        footer: String,
        photoUris: List<String>,
        isQuote: Boolean
    ): Long
    fun getInvoiceWithItemsById(id: Long): Flow<InvoiceWithItems?>
    suspend fun updateInvoiceStatus(invoiceId: Long, status: String)
    suspend fun updatePdfPath(id: Long, path: String)
}
