package com.emu.emubizwax.data.repository

import com.emu.emubizwax.data.local.dao.InvoiceDao
import com.emu.emubizwax.data.local.dao.LineItemDao
import com.emu.emubizwax.data.mapper.InvoiceMapper
import com.emu.emubizwax.data.mapper.LineItemMapper
import com.emu.emubizwax.domain.model.Invoice
import com.emu.emubizwax.domain.model.InvoiceStatus
import com.emu.emubizwax.domain.model.InvoiceWithItems
import com.emu.emubizwax.domain.model.LineItem
import com.emu.emubizwax.domain.repository.InvoiceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InvoiceRepositoryImpl @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val lineItemDao: LineItemDao,
    private val invoiceMapper: InvoiceMapper,
    private val lineItemMapper: LineItemMapper
) : InvoiceRepository {

    override fun getInvoices(): Flow<List<Invoice>> {
        return invoiceDao.getInvoices().map { entities ->
            entities.map { invoiceMapper.toDomain(it) }
        }
    }

    override fun getInvoicesByCustomer(customerId: Long): Flow<List<Invoice>> {
        return invoiceDao.getInvoicesForCustomer(customerId).map { entities ->
            entities.map { invoiceMapper.toDomain(it) }
        }
    }

    override fun getInvoiceWithItems(invoiceId: Long): Flow<InvoiceWithItems?> {
        return invoiceDao.getInvoiceWithItems(invoiceId).map { entity ->
            entity?.let { invoiceMapper.toDomain(it) }
        }
    }

    override suspend fun upsertInvoice(invoice: Invoice, items: List<LineItem>) {
        val invoiceId = invoiceDao.upsertInvoice(invoiceMapper.toEntity(invoice))
        val lineItemsWithInvoiceId = items.map { it.copy(invoiceId = invoiceId) }
        lineItemDao.insertAll(lineItemsWithInvoiceId.map { lineItemMapper.toEntity(it) })
    }

    override suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus) {
        invoiceDao.updateInvoiceStatus(invoiceId, status.name)
    }
}
