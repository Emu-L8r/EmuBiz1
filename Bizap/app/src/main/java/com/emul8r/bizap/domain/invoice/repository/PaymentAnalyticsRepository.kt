package com.emul8r.bizap.domain.invoice.repository

import com.emul8r.bizap.domain.invoice.model.*
import java.time.LocalDate

/**
 * Repository interface for payment analytics.
 */
interface PaymentAnalyticsRepository {
    suspend fun getPaymentAnalytics(businessId: Long): PaymentAnalyticsSummary
    suspend fun getRiskInvoices(businessProfileId: Long): List<InvoicePaymentStatus>
    suspend fun generateDunningNotices(businessProfileId: Long): List<DunningNotice>
    suspend fun forecastCashFlow(businessProfileId: Long, days: Int): List<CashFlowForecast>
    suspend fun recordPayment(
        invoiceId: Long,
        amountPaid: Double,
        paymentDate: LocalDate,
        paymentMethod: PaymentMethod,
        reference: String
    )
}

