package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import javax.inject.Inject

/**
 * UseCase to fetch and prepare payment analytics.
 */
class GetPaymentAnalyticsUseCase @Inject constructor(
    private val repository: PaymentAnalyticsRepository
) {
    suspend operator fun invoke(businessId: Long): PaymentAnalyticsSummary {
        return repository.getPaymentAnalytics(businessId)
    }
}
