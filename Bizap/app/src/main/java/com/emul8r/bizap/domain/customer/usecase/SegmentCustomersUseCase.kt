package com.emul8r.bizap.domain.customer.usecase

import com.emul8r.bizap.domain.customer.repository.CustomerAnalyticsRepository
import javax.inject.Inject

class SegmentCustomersUseCase @Inject constructor(
    private val repository: CustomerAnalyticsRepository
) {
    suspend fun execute(businessId: Long) {
        repository.recalculateChurnRisks(businessId)
    }
}

