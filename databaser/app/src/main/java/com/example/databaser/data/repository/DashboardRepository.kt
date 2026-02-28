package com.example.databaser.data.repository

import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getCustomerCount(): Flow<Int>
    fun getInvoiceCount(): Flow<Int>
    fun getQuoteCount(): Flow<Int>
}
