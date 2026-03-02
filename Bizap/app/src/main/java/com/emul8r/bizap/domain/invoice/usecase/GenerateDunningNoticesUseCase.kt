package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.invoice.model.DunningNotice
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import javax.inject.Inject

class GenerateDunningNoticesUseCase @Inject constructor(
    private val repository: PaymentAnalyticsRepository
) {
    suspend fun execute(businessId: Long): List<DunningNotice> {
        return repository.generateDunningNotices(businessId)
    }
}

