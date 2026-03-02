package com.emul8r.bizap.domain.revenue.usecase

import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to fetch and prepare high-level revenue metrics.
 */
class GetRevenueMetricsUseCase @Inject constructor(
    private val repository: RevenueRepository
) {
    operator fun invoke(businessId: Long): Flow<RevenueMetrics> {
        return repository.getRevenueMetrics(businessId)
    }
}
