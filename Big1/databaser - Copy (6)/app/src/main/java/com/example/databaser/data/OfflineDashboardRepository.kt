package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

class OfflineDashboardRepository(private val invoiceDao: InvoiceDao) : DashboardRepository {
    override fun getAllDocumentsStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getAllInvoices()
}
