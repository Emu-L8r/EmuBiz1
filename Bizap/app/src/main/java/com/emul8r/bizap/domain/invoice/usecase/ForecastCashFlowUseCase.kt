package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.invoice.model.CashFlowForecast
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import javax.inject.Inject

class ForecastCashFlowUseCase @Inject constructor(
    private val repository: PaymentAnalyticsRepository
) {
    suspend fun execute(businessId: Long, days: Int): List<CashFlowForecast> {
        return repository.forecastCashFlow(businessId, days)
    }
}

