package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.CustomerEntity
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.model.LineItem
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.ui.invoices.LineItemMapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val lineItemMapper: LineItemMapper
) : InvoiceRepository {
    override fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>> {
        return invoiceDao.getAllInvoices()
    }

    override fun searchInvoices(query: String, status: InvoiceStatus?): Flow<List<InvoiceWithItems>> {
        return invoiceDao.searchInvoices(query, status?.name)
    }

    override fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceWithItems>> {
        return invoiceDao.getInvoicesForCustomer(customerId)
    }

    override suspend fun saveInvoice(
        customer: CustomerEntity,
        items: List<LineItem>,
        header: String,
        subheader: String,
        notes: String,
        footer: String,
        photoUris: List<String>,
        isQuote: Boolean
    ): Long {
        val invoice = InvoiceEntity(
            customerId = customer.id,
            customerName = customer.name,
            date = System.currentTimeMillis(),
            totalAmount = items.sumOf { it.quantity * it.unitPrice },
            isQuote = isQuote,
            status = "DRAFT",
            photoUris = photoUris.joinToString(","),
            header = header,
            subheader = subheader,
            notes = notes,
            footer = footer
        )
        val lineItemEntities = items.map { lineItemMapper.toEntity(it, invoice.invoiceId) }
        return invoiceDao.insert(invoice, lineItemEntities)
    }

    override fun getInvoiceWithItemsById(id: Long): Flow<InvoiceWithItems?> {
        return invoiceDao.getInvoiceWithItemsById(id)
    }

    override suspend fun updateInvoiceStatus(invoiceId: Long, status: String) {
        invoiceDao.updateInvoiceStatus(invoiceId, status)
    }

    override suspend fun updatePdfPath(id: Long, path: String) {
        invoiceDao.updatePdfPath(id, path)
    }
}
