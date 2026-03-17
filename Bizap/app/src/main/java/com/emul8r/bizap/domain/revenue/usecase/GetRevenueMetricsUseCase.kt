package com.emul8r.bizap.domain.revenue.usecase

import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to fetch and prepare high-level revenue metrics reactively.
 */
class GetRevenueMetricsUseCase @Inject constructor(
    private val repository: RevenueRepository
) {
    operator fun invoke(businessId: Long): Flow<Result<RevenueMetricsV2>> {
        return repository.observeRevenueMetrics(businessId)
    }
}
