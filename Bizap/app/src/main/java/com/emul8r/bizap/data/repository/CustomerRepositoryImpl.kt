package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.CustomerDao
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.mapper.toEntity
import com.emul8r.bizap.data.remote.api.CustomerApi
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.customer.repository.CustomerAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Repository implementation for customers.
 * Ensures that customer changes are synchronized to analytics snapshots.
 */
class CustomerRepositoryImpl @Inject constructor(
    private val customerDao: CustomerDao,
    private val customerAnalyticsRepository: CustomerAnalyticsRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val customerApi: CustomerApi
) : CustomerRepository {
    
    override fun getAllCustomers(): Flow<List<Customer>> = 
        customerDao.getAllCustomers().map { list -> 
            list.map { it.toDomain() } 
        }

    override suspend fun insert(customer: Customer): Result<Long> = runCatching {
        // ✅ NULL SAFETY: Validate customer before insert
        require(customer.name.isNotBlank()) { "Customer name cannot be blank" }
        // ✅ EMAIL IS OPTIONAL - No validation required

        val id = customerDao.insert(customer.toEntity())
        require(id > 0) { "Failed to insert customer: DAO returned invalid ID $id" }

        // ✅ CREATE ANALYTICS SNAPSHOT when customer is created
        try {
            val businessId = businessProfileRepository.getActiveBusinessId()
            require(businessId > 0) { "Invalid business ID: $businessId" }

            customerAnalyticsRepository.createInitialSnapshot(
                customerId = id,
                businessId = businessId,
                customerName = customer.name,
                customerEmail = customer.email ?: ""
            ).onSuccess {
                Timber.d("✅ Created customer analytics snapshot for ID $id")
            }.onFailure { e ->
                Timber.w(e, "⚠️ Failed to create customer analytics snapshot (non-blocking)")
                // Non-blocking: don't fail the operation if snapshot creation fails
            }
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Exception creating customer snapshot: ${e.message}")
        }

        id
    }.onFailure { error ->
        // Detect and enhance UNIQUE constraint violations
        val message = error.message ?: ""
        if (message.contains("UNIQUE constraint failed") && message.contains("email")) {
            Timber.e(error, "UNIQUE constraint violation on email: ${customer.email}")
            throw Exception("Email address is already in use. Please use a different email.")
        } else if (message.contains("email is required")) {
            Timber.e("Email validation failed: email is blank")
            throw Exception("Email is required. Please enter a valid email address.")
        }
        Timber.e(error, "Failed to insert customer: ${error.message}")
    }

    override fun getCustomerById(id: Long): Flow<Customer?> = 
        customerDao.getCustomerById(id).map { it?.toDomain() }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> = runCatching {
        // ✅ NULL SAFETY: Validate customer before update
        require(customer.id > 0) { "Customer ID must be positive, got ${customer.id}" }
        require(customer.name.isNotBlank()) { "Customer name cannot be blank" }
        // Email is optional - no validation required

        customerDao.update(customer.toEntity())

        // ✅ SYNC ANALYTICS when customer is updated
        try {
            val businessId = businessProfileRepository.getActiveBusinessId()
            require(businessId > 0) { "Invalid business ID: $businessId" }

            // Recalculate churn risks for the business
            customerAnalyticsRepository.recalculateChurnRisks(businessId)
            Timber.d("✅ Recalculated churn risks for customer ${customer.id}")
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Exception updating customer analytics: ${e.message}")
            // Non-blocking: don't fail the operation if analytics update fails
        }

        Unit
    }

    override suspend fun deleteCustomer(id: Long): Result<Unit> = runCatching {
        // ✅ NULL SAFETY: Validate customer ID before deletion
        require(id > 0) { "Customer ID must be positive for deletion, got $id" }

        customerDao.deleteCustomer(id)

        // ✅ CLEANUP ANALYTICS snapshot when customer is deleted
        try {
            customerAnalyticsRepository.deleteCustomerSnapshot(id)
                .onSuccess {
                    Timber.d("✅ Deleted customer analytics snapshot for ID $id")
                }.onFailure { e ->
                    Timber.w(e, "⚠️ Failed to delete customer analytics snapshot (non-blocking)")
                    // Non-blocking: customer already deleted, snapshot cleanup is best-effort
                }
        } catch (e: Exception) {
            Timber.w(e, "⚠️ Exception deleting customer analytics: ${e.message}")
        }

        Unit
    }

    override suspend fun deleteAllCustomers(): Result<Unit> = runCatching {
        Timber.w("⚠️ Deleting ALL customers — invoices will become orphaned")
        customerDao.deleteAllCustomers()
        Timber.d("✅ All customers deleted")
    }

    // --- PHASE 2: Remote Sync ---

    override suspend fun createCustomerRemote(customer: Customer): Result<Customer> = runCatching {
        // ✅ NULL SAFETY: Validate before remote call
        require(customer.name.isNotBlank()) { "Customer name cannot be blank for remote create" }
        // Email is optional - no validation required

        val response = customerApi.createCustomer(customer)
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Empty response body from createCustomerRemote")
        } else {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun updateCustomerRemote(customer: Customer): Result<Customer> = runCatching {
        // ✅ NULL SAFETY: Validate before remote call
        require(customer.id > 0) { "Customer ID must be positive for remote update" }
        require(customer.name.isNotBlank()) { "Customer name cannot be blank for remote update" }
        // Email is optional - no validation required

        // Using updatedAt as a simple optimistic lock version (assuming server expects timestamp)
        val response = customerApi.updateCustomer(customer.id, customer, customer.updatedAt)
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Empty response body from updateCustomerRemote")
        } else {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun deleteCustomerRemote(id: Long): Result<Unit> = runCatching {
        // ✅ NULL SAFETY: Validate before remote call
        require(id > 0) { "Customer ID must be positive for remote delete" }

        val response = customerApi.deleteCustomer(id)
        if (!response.isSuccessful) {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }

    override suspend fun getCustomerRemote(id: Long): Result<Customer> = runCatching {
        // ✅ NULL SAFETY: Validate before remote call
        require(id > 0) { "Customer ID must be positive for remote fetch" }

        val response = customerApi.getCustomer(id)
        if (response.isSuccessful) {
            response.body() ?: throw IllegalStateException("Empty response body from getCustomerRemote")
        } else {
            throw Exception("API Error: ${response.code()} ${response.message()}")
        }
    }
}
