package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.invoice.model.InvoicePaymentStatus
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import javax.inject.Inject

class IdentifyRiskInvoicesUseCase @Inject constructor(
    private val repository: PaymentAnalyticsRepository
) {
    suspend fun execute(businessId: Long): List<InvoicePaymentStatus> {
        return repository.getRiskInvoices(businessId)
    }
}

