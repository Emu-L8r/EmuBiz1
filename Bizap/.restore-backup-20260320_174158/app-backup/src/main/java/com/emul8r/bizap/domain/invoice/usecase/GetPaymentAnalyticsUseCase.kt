package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UseCase to fetch and prepare payment analytics reactively.
 *
 * PHASE 3B FIX: PaymentAnalyticsRepository now delegates to PaymentAnalyticsRepositoryV2
 * internally, ensuring GUI1 and GUI2 use the same data source (invoices table, not snapshots).
 */
class GetPaymentAnalyticsUseCase @Inject constructor(
    private val repository: PaymentAnalyticsRepository
) {
    operator fun invoke(businessId: Long): Flow<PaymentAnalyticsSummary> {
        return repository.observePaymentAnalytics(businessId)
    }
}
