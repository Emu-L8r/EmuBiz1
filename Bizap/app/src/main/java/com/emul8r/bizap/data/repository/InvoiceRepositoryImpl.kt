package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val businessProfileRepository: BusinessProfileRepository
) : InvoiceRepository {
    
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getAllInvoicesWithItems(): Flow<List<Invoice>> {
        // Scoped to active business
        return businessProfileRepository.activeProfile.flatMapLatest { business ->
            invoiceDao.getInvoicesByBusinessId(business.id).map { list ->
                list.map { it.toDomain() }
            }
        }
    }

    override fun getInvoiceWithItemsById(id: Long): Flow<Invoice?> {
        return invoiceDao.getInvoiceWithItemsById(id).map { it?.toDomain() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getInvoiceGroupWithVersions(year: Int, sequence: Int): Flow<List<Invoice>> {
        return businessProfileRepository.activeProfile.flatMapLatest { business ->
            invoiceDao.getInvoiceGroupWithVersions(year, sequence, business.id).map { list ->
                list.map { entity ->
                    val placeholderWithItems = InvoiceWithItems(entity, emptyList())
                    placeholderWithItems.toDomain()
                }
            }
        }
    }

    override suspend fun saveInvoice(invoice: Invoice): Long {
        val activeBusinessId = businessProfileRepository.getActiveBusinessId()
        var invoiceToSave = invoice.copy(businessProfileId = activeBusinessId)
        
        if (invoice.id == 0L) {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            val nextSequence = invoiceDao.getMaxSequenceForYear(currentYear, activeBusinessId) + 1
            invoiceToSave = invoiceToSave.copy(
                invoiceYear = currentYear,
                invoiceSequence = nextSequence,
                version = 1
            )
            Timber.i("🔢 Assigning scoped invoice number: INV-$currentYear-${nextSequence.toString().padStart(6, '0')} for business $activeBusinessId")
        }

        val invoiceEntity = invoiceToSave.toEntity()
        val lineItemEntities = invoiceToSave.items.map { it.toEntity(invoiceToSave.id) }
        return invoiceDao.insert(invoiceEntity, lineItemEntities)
    }

    override suspend fun updateAmountPaid(invoiceId: Long, amount: Long) {
        val invoiceWithItems = invoiceDao.getInvoiceWithItemsById(invoiceId).first()
        invoiceWithItems?.let {
            val updatedEntity = it.invoice.copy(amountPaid = amount)
            invoiceDao.insertInvoice(updatedEntity)
        }
    }

    override suspend fun createCorrection(originalInvoiceId: Long): Long {
        val original = invoiceDao.getInvoiceWithItemsById(originalInvoiceId).first() 
            ?: throw Exception("Original invoice not found")
        
        val correctionEntity = original.invoice.copy(
            id = 0,
            status = InvoiceStatus.DRAFT.name,
            version = original.invoice.version + 1,
            amountPaid = 0L,
            parentInvoiceId = originalInvoiceId,
            date = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val lineItemEntities = original.items.map { it.copy(id = 0) }
        return invoiceDao.insert(correctionEntity, lineItemEntities)
    }

    override fun getBusinessProfile(): Flow<BusinessProfile> {
        return businessProfileRepository.activeProfile
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

