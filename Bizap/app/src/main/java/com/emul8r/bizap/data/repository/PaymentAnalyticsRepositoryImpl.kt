package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.dao.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot
import com.emul8r.bizap.domain.invoice.model.*
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Repository implementation for payment analytics.
 */
class PaymentAnalyticsRepositoryImpl @Inject constructor(
    private val paymentDao: InvoicePaymentDao,
    private val invoiceDao: InvoiceDao
) : PaymentAnalyticsRepository {

    override suspend fun getPaymentAnalytics(businessId: Long): PaymentAnalyticsSummary {
        Timber.d("PaymentAnalyticsRepositoryImpl: Fetching analytics for business $businessId")

        return try {
            val snapshots = paymentDao.getAllSnapshots(businessId)

            if (snapshots.isEmpty()) {
                return PaymentAnalyticsSummary(
                    businessProfileId = businessId,
                    totalInvoices = 0,
                    paidInvoices = 0,
                    unpaidInvoices = 0,
                    overdueInvoices = 0,
                    totalInvoiceAmount = 0.0,
                    totalPaidAmount = 0.0,
                    totalOutstandingAmount = 0.0,
                    collectionRate = 0.0,
                    averagePaymentTime = 0.0,
                    outstandingByAging = OutstandingByAging(0.0, 0.0, 0.0, 0.0, 0.0),
                    riskInvoices = emptyList(),
                    cashFlowForecast = emptyList()
                )
            }

            val metricsRow = paymentDao.getPaymentMetrics(businessId)
            val agingRow = paymentDao.getOutstandingByAging(businessId)

            PaymentAnalyticsSummary(
                businessProfileId = businessId,
                totalInvoices = metricsRow.totalInvoices,
                paidInvoices = paymentDao.countByStatus(businessId, "PAID"),
                unpaidInvoices = paymentDao.countByStatus(businessId, "UNPAID"),
                overdueInvoices = paymentDao.countByStatus(businessId, "OVERDUE"),
                totalInvoiceAmount = metricsRow.totalAmount,
                totalPaidAmount = metricsRow.paidAmount,
                totalOutstandingAmount = metricsRow.outstanding,
                collectionRate = (metricsRow.paidAmount / metricsRow.totalAmount) * 100.0,
                averagePaymentTime = 0.0,
                outstandingByAging = OutstandingByAging(
                    current = agingRow.current,
                    past30 = agingRow.past30,
                    past60 = agingRow.past60,
                    past90 = agingRow.past90,
                    totalOutstanding = metricsRow.outstanding
                ),
                riskInvoices = getRiskInvoices(businessId),
                cashFlowForecast = emptyList()
            )
        } catch (e: Exception) {
            Timber.e(e, "Error fetching payment analytics")
            throw e
        }
    }

    override suspend fun getRiskInvoices(businessProfileId: Long): List<InvoicePaymentStatus> {
        return paymentDao.getRiskInvoices(businessProfileId).map { it.toDomain() }
    }

    override suspend fun generateDunningNotices(businessProfileId: Long): List<DunningNotice> {
        return emptyList()
    }

    override suspend fun forecastCashFlow(businessProfileId: Long, days: Int): List<CashFlowForecast> {
        return emptyList()
    }

    override suspend fun recordPayment(
        invoiceId: Long,
        amountPaid: Double,
        paymentDate: LocalDate,
        paymentMethod: PaymentMethod,
        reference: String
    ) {
        val payment = com.emul8r.bizap.data.local.entities.InvoicePaymentEntity(
            invoiceId = invoiceId,
            amountPaid = amountPaid,
            paymentDate = paymentDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
            paymentMethod = paymentMethod.name,
            transactionReference = reference
        )
        paymentDao.insertPayment(payment)
    }

    private fun InvoicePaymentSnapshot.toDomain() = InvoicePaymentStatus(
        invoiceId = invoiceId,
        invoiceNumber = invoiceNumber,
        customerId = customerId,
        customerName = customerName,
        invoiceDate = LocalDate.now(),
        dueDate = LocalDate.now(),
        totalAmount = totalAmount,
        paidAmount = paidAmount,
        outstandingAmount = outstandingAmount,
        paymentStatus = PaymentStatus.valueOf(paymentStatus),
        ageingBucket = AgeingBucket.valueOf(ageingBucket),
        daysOverdue = daysOverdue,
        lastPaymentDate = null,
        paymentHistory = emptyList()
    )
}

