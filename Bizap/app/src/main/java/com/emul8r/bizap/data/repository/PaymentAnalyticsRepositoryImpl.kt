package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot
import com.emul8r.bizap.domain.invoice.model.*
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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

    override fun observePaymentAnalytics(businessId: Long): Flow<PaymentAnalyticsSummary> {
        return paymentDao.observeAllSnapshots(businessId)
            .map { snapshots ->
                Timber.d("PaymentAnalyticsRepositoryImpl: Reactive update with ${snapshots.size} snapshots")
                if (snapshots.isEmpty()) {
                    PaymentAnalyticsSummary(
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
                } else {
                    val totalAmount = snapshots.sumOf { it.totalAmount.toDouble() }
                    val paidAmount = snapshots.sumOf { it.paidAmount.toDouble() }
                    val outstanding = snapshots.sumOf { it.outstandingAmount.toDouble() }
                    val paidCount = snapshots.count { it.paymentStatus == "PAID" }
                    val unpaidCount = snapshots.count { it.paymentStatus == "UNPAID" }
                    val overdueCount = snapshots.count { it.paymentStatus == "OVERDUE" }
                    val agingCurrent = snapshots.filter { it.ageingBucket == "CURRENT" }.sumOf { it.outstandingAmount.toDouble() }
                    val agingPast30 = snapshots.filter { it.ageingBucket == "PAST_30" }.sumOf { it.outstandingAmount.toDouble() }
                    val agingPast60 = snapshots.filter { it.ageingBucket == "PAST_60" }.sumOf { it.outstandingAmount.toDouble() }
                    val agingPast90 = snapshots.filter { it.ageingBucket == "PAST_90" }.sumOf { it.outstandingAmount.toDouble() }
                    PaymentAnalyticsSummary(
                        businessProfileId = businessId,
                        totalInvoices = snapshots.size,
                        paidInvoices = paidCount,
                        unpaidInvoices = unpaidCount,
                        overdueInvoices = overdueCount,
                        totalInvoiceAmount = totalAmount,
                        totalPaidAmount = paidAmount,
                        totalOutstandingAmount = outstanding,
                        collectionRate = if (totalAmount > 0.0) ((paidAmount / totalAmount) * 100.0).coerceIn(0.0, 100.0) else 0.0,
                        averagePaymentTime = 0.0,
                        outstandingByAging = OutstandingByAging(
                            current = agingCurrent,
                            past30 = agingPast30,
                            past60 = agingPast60,
                            past90 = agingPast90,
                            totalOutstanding = outstanding
                        ),
                        riskInvoices = snapshots.filter { it.isAtRisk }.map { it.toDomain() },
                        cashFlowForecast = emptyList()
                    )
                }
            }
    }

    override fun observeRiskInvoices(businessProfileId: Long): Flow<List<InvoicePaymentStatus>> {
        return paymentDao.observeRiskInvoices(businessProfileId)
            .map { snapshots -> snapshots.map { it.toDomain() } }
    }

    override suspend fun getPaymentAnalytics(businessId: Long): PaymentAnalyticsSummary {
        Timber.d("PaymentAnalyticsRepositoryImpl: Fetching analytics for business $businessId")

        // 🧪 VALIDATION TEST: Compare calculated vs snapshot-based metrics
        try {
            val calculated = invoiceDao.calculatePaymentMetrics(businessId)
            val metricsRow = paymentDao.getPaymentMetrics(businessId)

            if (calculated != null && metricsRow != null) {
                val snapshotCollectionRate = if (metricsRow.totalAmount > 0.0) {
                    ((metricsRow.paidAmount / metricsRow.totalAmount) * 100.0).coerceIn(0.0, 100.0)
                } else 0.0

                // Log comparison for analysis
                Timber.d("""
                    ┌─── METRICS COMPARISON ───┐
                    │ Source: Invoices Table (Calculated)
                    │   Total: ${calculated.totalInvoices} invoices
                    │   Paid: ${calculated.paidInvoices} invoices
                    │   Outstanding: ${calculated.totalOutstanding} cents
                    │   Collection Rate: ${calculated.collectionRate}%
                    │
                    │ Source: Snapshot Tables
                    │   Total: ${metricsRow.totalInvoices} invoices
                    │   Paid Amount: ${metricsRow.paidAmount}
                    │   Outstanding: ${metricsRow.outstanding} cents
                    │   Collection Rate: $snapshotCollectionRate%
                    │
                    │ DISCREPANCIES:
                    │   Invoice Count Match: ${calculated.totalInvoices == metricsRow.totalInvoices}
                    │   Outstanding Match: ${calculated.totalOutstanding == metricsRow.outstanding.toLong()}
                    └─────────────────────────┘
                """.trimIndent())

                // If there are discrepancies, flag them
                if (calculated.totalInvoices != metricsRow.totalInvoices ||
                    calculated.totalOutstanding != metricsRow.outstanding.toLong()) {

                    Timber.w("""
                        ⚠️ SNAPSHOT INCONSISTENCY DETECTED!
                        Snapshots may be stale or incomplete.
                        Calculated metrics from invoices table should be used.
                    """.trimIndent())
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during validation test - continuing with snapshot metrics")
        }

        try {
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
                collectionRate = if (metricsRow.totalAmount > 0.0) {
                    ((metricsRow.paidAmount / metricsRow.totalAmount) * 100.0).coerceIn(0.0, 100.0)
                } else {
                    0.0
                },
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
        amountPaid: Long,  // Cents (e.g., 14999 = $149.99)
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
        totalAmount = totalAmount.toDouble() / 100.0,      // Convert cents to dollars
        paidAmount = paidAmount.toDouble() / 100.0,        // Convert cents to dollars
        outstandingAmount = outstandingAmount.toDouble() / 100.0,  // Convert cents to dollars
        paymentStatus = PaymentStatus.valueOf(paymentStatus),
        ageingBucket = AgeingBucket.valueOf(ageingBucket),
        daysOverdue = daysOverdue,
        lastPaymentDate = null,
        paymentHistory = emptyList()
    )
}
