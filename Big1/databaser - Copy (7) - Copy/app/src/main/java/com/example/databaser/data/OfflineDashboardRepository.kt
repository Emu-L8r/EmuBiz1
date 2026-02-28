package com.example.databaser.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineDashboardRepository @Inject constructor(private val invoiceDao: InvoiceDao) : DashboardRepository {
    override fun getAllDocumentsStream(): Flow<List<InvoiceWithCustomerAndLineItems>> = invoiceDao.getAllInvoices()
}
