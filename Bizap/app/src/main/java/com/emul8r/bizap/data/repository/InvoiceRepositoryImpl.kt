package com.emul8r.bizap.data.repository

import android.util.Log
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override fun getAllInvoicesWithItems(): Flow<List<Invoice>> {
        return invoiceDao.getAllInvoices().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getInvoiceWithItemsById(id: Long): Flow<Invoice?> {
        return invoiceDao.getInvoiceWithItemsById(id).map { it?.toDomain() }
    }

    override suspend fun saveInvoice(invoice: Invoice): Long {
        var invoiceToSave = invoice
        
        // Handle professional numbering for NEW invoices
        if (invoice.id == 0L) {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val nextSequence = invoiceDao.getMaxSequenceForYear(currentYear) + 1
            invoiceToSave = invoice.copy(
                invoiceYear = currentYear,
                invoiceSequence = nextSequence,
                version = 1
            )
            Log.i("InvoiceRepository", "🔢 Assigning new invoice number: INV-$currentYear-${nextSequence.toString().padStart(6, '0')}")
        }

        val invoiceEntity = invoiceToSave.toEntity()
        val lineItemEntities = invoiceToSave.items.map { it.toEntity(invoiceToSave.id) }
        return invoiceDao.insert(invoiceEntity, lineItemEntities)
    }

    override fun getBusinessProfile(): Flow<BusinessProfile> {
        return kotlinx.coroutines.flow.flowOf(BusinessProfile())
    }

    override suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus) {
        invoiceDao.updateInvoiceStatus(invoiceId, status.name)
    }

    override suspend fun updatePdfPath(invoiceId: Long, pdfPath: String) {
        invoiceDao.updatePdfPath(invoiceId, pdfPath)
    }

    override suspend fun deleteInvoice(id: Long) {
        invoiceDao.deleteInvoiceWithItems(id)
    }
}
