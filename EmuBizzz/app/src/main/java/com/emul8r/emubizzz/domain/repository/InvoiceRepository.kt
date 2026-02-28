package com.emul8r.emubizzz.domain.repository

import com.emul8r.emubizzz.data.local.db.entities.InvoiceEntity
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun getInvoiceById(invoiceId: Long): Flow<InvoiceEntity>
    fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceEntity>>
    suspend fun insert(invoice: InvoiceEntity): Long
    suspend fun update(invoice: InvoiceEntity)
    suspend fun delete(invoiceId: Long)
}