package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

class OfflineInvoicesRepository(private val invoiceDao: InvoiceDao, private val lineItemDao: LineItemDao) : InvoiceRepository {
    override fun getAllInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getAllInvoices()

    override fun getInvoiceStream(id: Int): Flow<InvoiceWithCustomerAndLineItems?> = invoiceDao.getInvoiceById(id)

    override fun getInvoicesForCustomerStream(customerId: Int): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getInvoicesForCustomer(customerId)

    override suspend fun insertInvoice(invoice: Invoice): Long = invoiceDao.insert(invoice)

    override suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.delete(invoice)

    override suspend fun updateInvoice(invoice: Invoice) = invoiceDao.update(invoice)

    override suspend fun getQuoteCount(): Int = invoiceDao.getQuoteCount()

    override suspend fun getInvoiceCountForDate(startOfDay: Long, endOfDay: Long): Int = invoiceDao.getInvoiceCountForDate(startOfDay, endOfDay)

    override suspend fun insertLineItem(lineItem: LineItem): Long = lineItemDao.insert(lineItem)
}
