package com.example.databaser.data

import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomersStream(): Flow<List<Customer>>
    fun getCustomerStream(id: Int): Flow<Customer?>
    suspend fun insertCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)
    suspend fun updateCustomer(customer: Customer)
    fun getDeletedCustomersStream(): Flow<List<Customer>>
    suspend fun restoreCustomer(customer: Customer)
    suspend fun permanentlyDeleteCustomer(customer: Customer)
    fun searchCustomers(query: String): Flow<List<Customer>>
}
