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
import kotlinx.coroutines.flow.first
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

    override fun getInvoiceGroupWithVersions(year: Int, sequence: Int): Flow<List<Invoice>> {
        return invoiceDao.getInvoiceGroupWithVersions(year, sequence).map { list ->
            list.map { entity ->
                // Note: This requires a new mapper helper for raw Entity to Domain without items
                // For now, mapping via standard entity mapper (items will be empty in picker)
                // In production, we'd use a specific lightweight entity for the picker
                val placeholderWithItems = InvoiceWithItems(entity, emptyList())
                placeholderWithItems.toDomain()
            }
        }
    }

    override suspend fun saveInvoice(invoice: Invoice): Long {
        var invoiceToSave = invoice
        
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

    override suspend fun updateAmountPaid(invoiceId: Long, amount: Double) {
        // This requires a specific DAO query for amountPaid. Adding logic to fetch and update.
        val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId).first()
        invoiceWithItems?.let {
            val updatedEntity = it.invoice.copy(amountPaid = amount)
            invoiceDao.insertInvoice(updatedEntity)
        }
    }

    override suspend fun createCorrection(originalInvoiceId: Long): Long {
        val original = invoiceDao.getInvoiceWithItemsById(originalInvoiceId).first() 
            ?: throw Exception("Original invoice not found")
        
        // 1. Prepare cloned metadata with version incremented
        val correctionEntity = original.invoice.copy(
            id = 0, // Fresh ID
            status = InvoiceStatus.DRAFT.name,
            version = original.invoice.version + 1,
            amountPaid = 0.0, // Fresh version has zero payments
            parentInvoiceId = originalInvoiceId,
            date = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // 2. Clone line items
        val lineItemEntities = original.items.map { it.copy(id = 0) }

        // 3. Persist atomically
        return invoiceDao.insert(correctionEntity, lineItemEntities)
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
