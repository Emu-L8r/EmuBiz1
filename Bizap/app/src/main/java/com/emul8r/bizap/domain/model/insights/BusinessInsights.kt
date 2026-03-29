package com.emul8r.bizap.domain.model.insights

import kotlinx.serialization.Serializable

/**
 * Business health insight for display on dashboard.
 */
@Serializable
data class BusinessInsight(
    val id: String,
    val title: String,
    val description: String,
    val severity: InsightSeverity,
    val actionUrl: String = "",
    val icon: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
enum class InsightSeverity {
    INFO, WARNING, CRITICAL
}

/**
 * Cash flow prediction for the next 30 days.
 */
@Serializable
data class CashFlowPrediction(
    val predictedBalance: Long,  // in cents
    val confidence: Double,  // 0-100%
    val forecastDays: Int = 30,
    val projectedInflows: Long,
    val projectedOutflows: Long,
    val riskLevel: RiskLevel,
    val recommendations: List<String>
)

enum class RiskLevel {
    HEALTHY, CAUTION, RISK
}

/**
 * At-risk customer alert.
 */
@Serializable
data class AtRiskCustomerAlert(
    val customerId: Long,
    val customerName: String,
    val riskFactors: List<String>,
    val totalOutstanding: Long,
    val daysSinceDue: Int,
    val suggestedAction: String,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Dashboard insights summary.
 */
@Serializable
data class BusinessInsightsSummary(
    val insights: List<BusinessInsight>,
    val cashFlowPrediction: CashFlowPrediction,
    val atRiskCustomers: List<AtRiskCustomerAlert>,
    val healthScore: Int,  // 0-100
    val generatedAt: Long = System.currentTimeMillis()
) {
    val hasWarnings: Boolean get() = insights.any { it.severity == InsightSeverity.WARNING }
    val hasCritical: Boolean get() = insights.any { it.severity == InsightSeverity.CRITICAL }
}

/**
 * Key performance indicator for business health.
 */
@Serializable
data class HealthKpi(
    val name: String,
    val value: Double,
    val target: Double,
    val unit: String,
    val status: KpiStatus
) {
    val percentOfTarget: Double get() = (value / target) * 100
}

enum class KpiStatus {
    EXCELLENT, GOOD, FAIR, POOR
}

