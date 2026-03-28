package com.emul8r.bizap.domain.model.gui2

/**
 * GUI2 Customer Analytics Metrics
 *
 * Provides customer segmentation and lifetime value insights.
 */
data class CustomerMetricsV2(
    val totalCustomers: Int = 0,
    val vipCount: Int = 0,
    val regularCount: Int = 0,
    val atRiskCount: Int = 0,
    val dormantCount: Int = 0,
    val averageLTV: Double = 0.0,
    val churnRate: Double = 0.0
) {
    val vipPercentage: Double get() = if (totalCustomers > 0) (vipCount.toDouble() / totalCustomers * 100) else 0.0
    val regularPercentage: Double get() = if (totalCustomers > 0) (regularCount.toDouble() / totalCustomers * 100) else 0.0
    val atRiskPercentage: Double get() = if (totalCustomers > 0) (atRiskCount.toDouble() / totalCustomers * 100) else 0.0
    val dormantPercentage: Double get() = if (totalCustomers > 0) (dormantCount.toDouble() / totalCustomers * 100) else 0.0
}

