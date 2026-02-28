package com.emul8r.emubizzz.domain.repository

import com.emul8r.emubizzz.data.local.db.entities.CustomerEntity
import com.emul8r.emubizzz.data.local.db.relations.CustomerWithInvoices
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    fun getCustomerById(customerId: Long): Flow<CustomerEntity>
    fun getCustomersWithInvoices(): Flow<List<CustomerWithInvoices>>
    suspend fun insert(customer: CustomerEntity)
    suspend fun update(customer: CustomerEntity)
    suspend fun delete(customerId: Long)
}