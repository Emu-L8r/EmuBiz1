package com.example.databaser.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineInvoicesRepository @Inject constructor(private val invoiceDao: InvoiceDao, private val lineItemDao: LineItemDao) : InvoiceRepository {
    override fun getAllInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getAllInvoices()

    override fun getInvoiceStream(id: Int): Flow<InvoiceWithCustomerAndLineItems?> = invoiceDao.getInvoiceById(id)

    override fun getInvoicesForCustomerStream(customerId: Int): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getInvoicesForCustomer(customerId)

    override fun getUnsentInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getUnsentInvoices()

    override fun getSentInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getSentInvoices()

    override fun getPaidInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getPaidInvoices()

    override fun getUnsentQuotesStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getUnsentQuotes()

    override fun getSentQuotesStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getSentQuotes()

    override suspend fun insertInvoice(invoice: Invoice): Long = invoiceDao.insert(invoice)

    override suspend fun deleteInvoice(invoice: Invoice) = invoiceDao.delete(invoice)

    override suspend fun updateInvoice(invoice: Invoice) = invoiceDao.update(invoice)

    override suspend fun getQuoteCount(): Int = invoiceDao.getQuoteCount()

    override suspend fun getInvoiceCountForDate(startOfDay: Long, endOfDay: Long): Int = invoiceDao.getInvoiceCountForDate(startOfDay, endOfDay)

    override suspend fun insertLineItem(lineItem: LineItem): Long = lineItemDao.insert(lineItem)

    override suspend fun deleteLineItemsByInvoiceId(invoiceId: Int) = invoiceDao.deleteLineItemsByInvoiceId(invoiceId)
}
