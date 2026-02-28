package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    suspend fun saveCustomer(customer: CustomerEntity)
    suspend fun getCustomerById(id: Long): CustomerEntity?
}
