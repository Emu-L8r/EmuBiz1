package com.emul8r.bizap.domain.customer.usecase

import com.emul8r.bizap.domain.customer.model.CustomerAnalyticsSummary
import com.emul8r.bizap.domain.customer.repository.CustomerAnalyticsRepository
import javax.inject.Inject

class GetCustomerAnalyticsUseCase @Inject constructor(
    private val repository: CustomerAnalyticsRepository
) {
    suspend fun execute(businessId: Long): CustomerAnalyticsSummary {
        return repository.getAnalyticsSummary(businessId)
    }
}

