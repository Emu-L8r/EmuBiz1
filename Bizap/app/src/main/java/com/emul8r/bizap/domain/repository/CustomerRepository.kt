package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.Customer
import kotlinx.coroutines.flow.Flow

/**
 * Domain-level contract for Customer data operations.
 */
interface CustomerRepository {
    fun getAllCustomers(): Flow<List<Customer>>
    fun getCustomerById(id: Long): Flow<Customer?>
    
    /**
     * Inserts a customer locally.
     */
    suspend fun insert(customer: Customer): Result<Long>
    
    /**
     * Updates a customer locally.
     */
    suspend fun updateCustomer(customer: Customer): Result<Unit>
    
    /**
     * Deletes a customer locally.
     */
    suspend fun deleteCustomer(id: Long): Result<Unit>

    /**
     * Deletes all customers locally.
     * Note: invoices previously belonging to deleted customers will remain (orphaned).
     */
    suspend fun deleteAllCustomers(): Result<Unit>

    // --- PHASE 2: Remote Sync ---

    suspend fun createCustomerRemote(customer: Customer): Result<Customer>
    suspend fun updateCustomerRemote(customer: Customer): Result<Customer>
    suspend fun deleteCustomerRemote(id: Long): Result<Unit>
    suspend fun getCustomerRemote(id: Long): Result<Customer>
}
