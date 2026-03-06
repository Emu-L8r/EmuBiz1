package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.CustomerDao
import com.emul8r.bizap.data.mapper.toDomain
import com.emul8r.bizap.data.mapper.toEntity
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
    private val businessProfileRepository: BusinessProfileRepository
) : CustomerRepository {
    
    override fun getAllCustomers(): Flow<List<Customer>> = 
        customerDao.getAllCustomers().map { list -> 
            list.map { it.toDomain() } 
        }

    override suspend fun insert(customer: Customer): Result<Long> = runCatching {
        val id = customerDao.insert(customer.toEntity())

        // ✅ CREATE ANALYTICS SNAPSHOT when customer is created
        try {
            val businessId = businessProfileRepository.getActiveBusinessId()
            customerAnalyticsRepository.createInitialSnapshot(
                customerId = id,
                businessId = businessId,
                customerName = customer.name,
                customerEmail = customer.email
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
    }

    override fun getCustomerById(id: Long): Flow<Customer?> = 
        customerDao.getCustomerById(id).map { it?.toDomain() }

    override suspend fun updateCustomer(customer: Customer): Result<Unit> = runCatching {
        customerDao.update(customer.toEntity())

        // ✅ SYNC ANALYTICS when customer is updated
        try {
            val businessId = businessProfileRepository.getActiveBusinessId()
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
}
