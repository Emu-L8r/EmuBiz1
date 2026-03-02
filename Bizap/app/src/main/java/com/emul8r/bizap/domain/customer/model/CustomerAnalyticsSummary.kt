package com.emul8r.bizap.domain.customer.model

import java.time.LocalDate

data class CustomerAnalyticsSummary(
    val totalCustomers: Int,
    val vipCount: Int,
    val regularCount: Int,
    val atRiskCount: Int,
    val dormantCount: Int,
    val averageLTV: Double,
    val totalRevenue: Double,
    val churnRate: Double,
    val topCustomers: List<CustomerLifetimeValue>,
    val atRiskCustomers: List<ChurnRiskIndicator>
)

data class CustomerAnalyticsProfile(
    val customerId: Long,
    val customerName: String,
    val email: String?,
    val segment: CustomerSegment,
    val ltv: CustomerLifetimeValue,
    val frequency: PurchaseFrequency,
    val churnRisk: ChurnRiskIndicator,
    val totalInvoices: Int,
    val totalPaid: Double,
    val totalOutstanding: Double
)

enum class CustomerSegment {
    NEW, REGULAR, VIP, AT_RISK, DORMANT
}

data class CustomerLifetimeValue(
    val customerId: Long,
    val totalRevenue: Double,
    val transactionCount: Int,
    val averageOrderValue: Double,
    val firstPurchaseDate: LocalDate,
    val lastPurchaseDate: LocalDate,
    val daysSinceLastPurchase: Int,
    val estimatedLTV: Double
)

data class PurchaseFrequency(
    val customerId: Long,
    val totalTransactions: Int,
    val transactionsLast30Days: Int,
    val transactionsLast90Days: Int,
    val averageDaysBetweenPurchases: Double,
    val purchaseVelocity: Double
)

data class ChurnRiskIndicator(
    val customerId: Long,
    val riskScore: Double,
    val daysSinceLastPurchase: Int,
    val declineInFrequency: Double,
    val declineInSpending: Double,
    val isPredictedToChurn: Boolean,
    val riskFactors: List<String>
)

