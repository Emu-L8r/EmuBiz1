package com.emul8r.bizap.data.repository

import com.emul8r.bizap.data.local.dao.dao.CustomerAnalyticsDao
import com.emul8r.bizap.data.local.dao.InvoiceDao
import com.emul8r.bizap.data.local.dao.CustomerDao
import com.emul8r.bizap.data.local.entities.CustomerAnalyticsSnapshot
import com.emul8r.bizap.domain.customer.model.*
import com.emul8r.bizap.domain.customer.repository.CustomerAnalyticsRepository
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.min

/**
 * Repository implementation for customer analytics.
 */
class CustomerAnalyticsRepositoryImpl @Inject constructor(
    private val analyticsDao: CustomerAnalyticsDao,
    private val invoiceDao: InvoiceDao,
    private val customerDao: CustomerDao
) : CustomerAnalyticsRepository {

    override suspend fun getAnalyticsSummary(businessProfileId: Long): CustomerAnalyticsSummary {
        Timber.d("CustomerAnalyticsRepositoryImpl: Fetching analytics for business $businessProfileId")

        return try {
            val snapshots = analyticsDao.getAllCustomerSnapshots(businessProfileId)

            if (snapshots.isEmpty()) {
                return CustomerAnalyticsSummary(
                    totalCustomers = 0,
                    vipCount = 0,
                    regularCount = 0,
                    atRiskCount = 0,
                    dormantCount = 0,
                    averageLTV = 0.0,
                    totalRevenue = 0.0,
                    churnRate = 0.0,
                    topCustomers = emptyList(),
                    atRiskCustomers = emptyList()
                )
            }

            val topCustomers = analyticsDao.getTopValueCustomers(businessProfileId, 10)
                .map { it.toLtvModel() }

            val atRiskCustomers = analyticsDao.getAtRiskCustomers(businessProfileId)
                .map { it.toChurnModel() }

            CustomerAnalyticsSummary(
                totalCustomers = snapshots.size,
                vipCount = snapshots.count { it.segment == "VIP" },
                regularCount = snapshots.count { it.segment == "REGULAR" },
                atRiskCount = atRiskCustomers.size,
                dormantCount = snapshots.count { it.segment == "DORMANT" },
                averageLTV = snapshots.map { it.customerLifetimeValue.toDouble() / 100.0 }.average(),
                totalRevenue = snapshots.sumOf { it.totalRevenue.toDouble() } / 100.0,
                churnRate = (atRiskCustomers.size.toDouble() / snapshots.size) * 100.0,
                topCustomers = topCustomers,
                atRiskCustomers = atRiskCustomers
            )
        } catch (e: Exception) {
            Timber.e(e, "Error fetching analytics summary")
            throw e
        }
    }

    override suspend fun getCustomerProfile(customerId: Long): CustomerAnalyticsProfile {
        val snapshot = analyticsDao.getCustomerSnapshot(customerId)
            ?: throw IllegalArgumentException("Snapshot not found for customer $customerId")

        return CustomerAnalyticsProfile(
            customerId = snapshot.customerId,
            customerName = snapshot.customerName,
            email = snapshot.customerEmail,
            segment = CustomerSegment.valueOf(snapshot.segment),
            ltv = snapshot.toLtvModel(),
            frequency = snapshot.toFrequencyModel(),
            churnRisk = snapshot.toChurnModel(),
            totalInvoices = snapshot.invoiceCount,
            totalPaid = snapshot.totalRevenue.toDouble() / 100.0,
            totalOutstanding = 0.0
        )
    }

    override suspend fun recalculateChurnRisks(businessProfileId: Long) {
        val snapshots = analyticsDao.getAllCustomerSnapshots(businessProfileId)
        val updated = snapshots.map { it.copy(lastUpdatedMs = System.currentTimeMillis()) }
        analyticsDao.insertSnapshots(updated)
    }

    // --- Internal Mappers ---

    private fun CustomerAnalyticsSnapshot.toLtvModel() = CustomerLifetimeValue(
        customerId = customerId,
        totalRevenue = totalRevenue.toDouble() / 100.0,
        transactionCount = invoiceCount,
        averageOrderValue = averageInvoiceAmount.toDouble() / 100.0,
        firstPurchaseDate = LocalDate.now(),
        lastPurchaseDate = LocalDate.now(),
        daysSinceLastPurchase = daysSinceLastPurchase,
        estimatedLTV = estimatedLTV.toDouble() / 100.0
    )

    private fun CustomerAnalyticsSnapshot.toChurnModel() = ChurnRiskIndicator(
        customerId = customerId,
        riskScore = churnRiskScore,
        daysSinceLastPurchase = daysSinceLastPurchase,
        declineInFrequency = purchaseVelocity,
        declineInSpending = 0.0,
        isPredictedToChurn = isPredictedToChurn,
        riskFactors = listOf("Recency")
    )

    private fun CustomerAnalyticsSnapshot.toFrequencyModel() = PurchaseFrequency(
        customerId = customerId,
        totalTransactions = invoiceCount,
        transactionsLast30Days = 0,
        transactionsLast90Days = 0,
        averageDaysBetweenPurchases = averageDaysBetweenPurchases,
        purchaseVelocity = purchaseVelocity
    )
}

