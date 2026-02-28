package com.emu.emubizwax.domain.repository

import com.emu.emubizwax.domain.model.Invoice
import com.emu.emubizwax.domain.model.InvoiceStatus
import com.emu.emubizwax.domain.model.InvoiceWithItems
import com.emu.emubizwax.domain.model.LineItem
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    fun getInvoices(): Flow<List<Invoice>>
    fun getInvoicesByCustomer(customerId: Long): Flow<List<Invoice>>
    fun getInvoiceWithItems(invoiceId: Long): Flow<InvoiceWithItems?>
    suspend fun upsertInvoice(invoice: Invoice, items: List<LineItem>)
    suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus)
}
