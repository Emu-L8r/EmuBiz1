package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.domain.invoice.model.*
import com.emul8r.bizap.domain.invoice.repository.PaymentAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Repository implementation for payment analytics.
 *
 * PHASE 3B FIX: Now delegates to PaymentAnalyticsRepositoryV2 to ensure GUI1 and GUI2
 * use the same data source (invoices table, not stale snapshots).
 */
class PaymentAnalyticsRepositoryImpl @Inject constructor(
    private val paymentDao: InvoicePaymentDao,
    private val invoiceDao: InvoiceDao,
    private val repositoryV2: PaymentAnalyticsRepositoryV2
) : PaymentAnalyticsRepository {

    override fun observePaymentAnalytics(businessId: Long): Flow<PaymentAnalyticsSummary> {
        // PHASE 3B FIX: Delegate to V2 repository to ensure single source of truth
        // V2 queries invoices table directly and excludes DRAFT invoices
        return repositoryV2.observePaymentMetrics(businessId)
            .map { result ->
                result.fold(
                    onSuccess = { metricsV2 ->
                        Timber.d("PaymentAnalyticsRepositoryImpl: Using V2 metrics for business $businessId (collection rate: ${metricsV2.collectionRate}%)")

                        // Calculate unpaid count: SENT + PARTIALLY_PAID + OVERDUE
                        val unpaidCount = metricsV2.sentCount + metricsV2.partiallyPaidCount + metricsV2.overdueCount

                        // Convert PaymentMetricsV2 to PaymentAnalyticsSummary for backwards compatibility
                        PaymentAnalyticsSummary(
                            businessProfileId = businessId,
                            totalInvoices = metricsV2.totalInvoices,
                            paidInvoices = metricsV2.paidCount,
                            unpaidInvoices = unpaidCount,
                            overdueInvoices = metricsV2.overdueCount,
                            totalInvoiceAmount = (metricsV2.outstandingAmount + metricsV2.collectedAmount).toDouble() / 100.0,
                            totalPaidAmount = metricsV2.collectedAmount.toDouble() / 100.0,
                            totalOutstandingAmount = metricsV2.outstandingAmount.toDouble() / 100.0,
                            collectionRate = metricsV2.collectionRate,
                            averagePaymentTime = metricsV2.averageDaysToPayment,
                            outstandingByAging = OutstandingByAging(0.0, 0.0, 0.0, 0.0, metricsV2.outstandingAmount.toDouble() / 100.0),
                            riskInvoices = emptyList(),
                            cashFlowForecast = emptyList()
                        )
                    },
                    onFailure = { error ->
                        Timber.e(error, "PaymentAnalyticsRepositoryImpl: Failed to load metrics for business $businessId")
                        // Return empty summary on failure
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
                    }
                )
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

            // Check if both are non-null before comparing
            if (calculated != null && metricsRow != null) {
                @Suppress("SENSELESS_COMPARISON")  // Compiler false positive - null check is necessary for safety
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
            // Query invoices table directly (source of truth) instead of snapshots
            val calculated = invoiceDao.calculatePaymentMetrics(businessId)

            if (calculated == null || calculated.totalInvoices == 0) {
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

            return PaymentAnalyticsSummary(
                businessProfileId = businessId,
                totalInvoices = calculated.totalInvoices,
                paidInvoices = calculated.paidInvoices,
                unpaidInvoices = calculated.unpaidInvoices,
                overdueInvoices = 0,
                totalInvoiceAmount = calculated.totalAmount.toDouble() / 100.0,
                totalPaidAmount = calculated.paidAmount.toDouble() / 100.0,
                totalOutstandingAmount = calculated.totalOutstanding.toDouble() / 100.0,
                collectionRate = calculated.collectionRate,
                averagePaymentTime = 0.0,
                outstandingByAging = OutstandingByAging(0.0, 0.0, 0.0, 0.0, calculated.totalOutstanding.toDouble() / 100.0),
                riskInvoices = emptyList(),
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
