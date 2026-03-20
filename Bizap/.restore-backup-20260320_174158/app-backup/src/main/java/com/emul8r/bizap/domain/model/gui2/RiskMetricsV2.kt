package com.emul8r.bizap.domain.model.gui2

/**
 * Risk analytics metrics for GUI2 dashboard.
 * Invoices are classified into risk tiers based on overdue age.
 */
data class RiskMetricsV2(
    val businessProfileId: Long,
    val highRiskCount: Int,     // Overdue by 60+ days
    val atRiskCount: Int,       // Overdue by 30–59 days
    val healthyCount: Int,      // Paid or not yet due
    val overdueCount: Int,      // All overdue invoices
    val totalOutstandingCents: Long
)
