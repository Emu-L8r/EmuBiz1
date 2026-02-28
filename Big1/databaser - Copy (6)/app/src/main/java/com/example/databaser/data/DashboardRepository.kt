package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getAllDocumentsStream(): Flow<List<InvoiceWithCustomerAndLineItems>>
}
