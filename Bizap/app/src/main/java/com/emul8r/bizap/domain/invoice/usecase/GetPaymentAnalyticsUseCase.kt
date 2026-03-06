package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to fetch and prepare payment analytics reactively.
 */
class GetPaymentAnalyticsUseCase @Inject constructor(
    private val repository: PaymentAnalyticsRepository
) {
    operator fun invoke(businessId: Long): Flow<PaymentAnalyticsSummary> {
        return repository.observePaymentAnalytics(businessId)
    }
}
