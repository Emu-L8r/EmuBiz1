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
}
