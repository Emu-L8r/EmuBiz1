package com.emul8r.bizap.domain.invoice.usecase

import com.emul8r.bizap.domain.invoice.model.InvoicePaymentStatus
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class IdentifyRiskInvoicesUseCase @Inject constructor(
    private val repository: PaymentAnalyticsRepository
) {
    fun execute(businessId: Long): Flow<List<InvoicePaymentStatus>> {
        return repository.observeRiskInvoices(businessId)
    }
}
