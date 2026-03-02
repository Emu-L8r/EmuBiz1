package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.Customer
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level contract for Customer data operations.
 * Pure interface with no Android dependencies.
 */
interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    fun getCustomerById(id: Long): Flow<Customer?>
    suspend fun insert(customer: Customer): Long
    suspend fun updateCustomer(customer: Customer)
    suspend fun deleteCustomer(id: Long)
}

