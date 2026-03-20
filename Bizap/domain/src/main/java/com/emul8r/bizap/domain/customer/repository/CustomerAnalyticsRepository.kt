package com.emul8r.bizap.domain.customer.repository

import com.emul8r.bizap.domain.customer.model.CustomerAnalyticsProfile
import com.emul8r.bizap.domain.customer.model.CustomerAnalyticsSummary

/**
 * Repository interface for customer analytics.
 */
interface CustomerAnalyticsRepository {
    suspend fun getAnalyticsSummary(businessProfileId: Long): CustomerAnalyticsSummary
    suspend fun getCustomerProfile(customerId: Long): CustomerAnalyticsProfile
    suspend fun recalculateChurnRisks(businessProfileId: Long)

    /**
     * Creates an initial analytics snapshot for a newly created customer.
     * Assigns "NEW" segment with zero values for all metrics.
     */
    suspend fun createInitialSnapshot(
        customerId: Long,
        businessId: Long,
        customerName: String,
        customerEmail: String?
    ): Result<Unit>

    /**
     * Deletes analytics snapshot for a customer.
     * Called when customer is deleted to clean up orphaned snapshot data.
     */
    suspend fun deleteCustomerSnapshot(customerId: Long): Result<Unit>
}
