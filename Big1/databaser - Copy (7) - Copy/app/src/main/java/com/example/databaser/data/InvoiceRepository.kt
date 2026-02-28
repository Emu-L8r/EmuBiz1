package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun getAllInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>>
    fun getInvoiceStream(id: Int): Flow<InvoiceWithCustomerAndLineItems?>
    fun getInvoicesForCustomerStream(customerId: Int): Flow<List<InvoiceWithCustomerAndLineItems>>
    fun getUnsentInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>>
    fun getSentInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>>
    fun getPaidInvoicesStream(): Flow<List<InvoiceWithCustomerAndLineItems>>
    fun getUnsentQuotesStream(): Flow<List<InvoiceWithCustomerAndLineItems>>
    fun getSentQuotesStream(): Flow<List<InvoiceWithCustomerAndLineItems>>
    suspend fun insertInvoice(invoice: Invoice): Long
    suspend fun deleteInvoice(invoice: Invoice)
    suspend fun getQuoteCount(): Int
    suspend fun getInvoiceCountForDate(startOfDay: Long, endOfDay: Long): Int
    suspend fun insertLineItem(lineItem: LineItem): Long
    suspend fun updateInvoice(invoice: Invoice)
    suspend fun deleteLineItemsByInvoiceId(invoiceId: Int)
}
