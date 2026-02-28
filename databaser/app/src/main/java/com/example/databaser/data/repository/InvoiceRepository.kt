package com.example.databaser.data.repository

import com.example.databaser.data.model.Invoice
import com.example.databaser.data.model.InvoiceLineItem
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun getInvoiceStream(id: Long): Flow<Invoice?>
    fun getLineItemsStream(invoiceId: Long): Flow<List<InvoiceLineItem>>
    suspend fun saveInvoice(invoice: Invoice, lineItems: List<InvoiceLineItem>)
    suspend fun deleteLineItem(lineItemId: Long)
}
