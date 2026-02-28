package com.emul8r.emubizzz.data.repository

import com.emul8r.emubizzz.data.local.db.dao.InvoiceDao
import com.emul8r.emubizzz.data.local.db.entities.InvoiceEntity
import com.emul8r.emubizzz.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao
) : InvoiceRepository {
    override fun getInvoiceById(invoiceId: Long): Flow<InvoiceEntity> = invoiceDao.getInvoiceById(invoiceId)
    override fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceEntity>> = invoiceDao.getInvoicesForCustomer(customerId)
    override suspend fun insert(invoice: InvoiceEntity): Long = invoiceDao.insert(invoice)
    override suspend fun update(invoice: InvoiceEntity) = invoiceDao.update(invoice)
    override suspend fun delete(invoiceId: Long) = invoiceDao.delete(invoiceId)
}