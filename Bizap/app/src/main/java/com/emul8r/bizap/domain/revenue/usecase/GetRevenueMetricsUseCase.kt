package com.emul8r.bizap.domain.revenue.usecase

import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import javax.inject.Inject

/**
 * UseCase to fetch and prepare high-level revenue metrics.
 */
class GetRevenueMetricsUseCase @Inject constructor(
    private val repository: RevenueRepository
) {
    suspend operator fun invoke(businessId: Long): RevenueMetrics {
        return repository.getRevenueMetrics(businessId)
    }
}

