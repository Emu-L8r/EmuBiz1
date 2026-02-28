package com.emu.emubizwax.domain.repository

import com.emu.emubizwax.domain.model.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getCustomers(): Flow<List<Customer>>
    fun getCustomerById(id: Long): Flow<Customer?>
    suspend fun upsertCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)
}
