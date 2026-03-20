package com.emul8r.bizap.domain.model.gui2

/**
 * Combined dashboard state for GUI2 main screen.
 * Aggregates all key metrics into one model.
 */
data class DashboardStateV2(
    val businessContext: BusinessContextV2,
    val revenueMetrics: RevenueMetricsV2,
    val paymentMetrics: PaymentMetricsV2,
    val riskMetrics: RiskMetricsV2,
    val invoiceMetrics: InvoiceMetricsV2 = InvoiceMetricsV2(0, 0, 0, 0)
)
