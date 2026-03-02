package com.emul8r.bizap.domain.model

data class DashboardSettings(
    val showRevenueTrend: Boolean = true,
    val showMtdCard: Boolean = true,
    val showYtdCard: Boolean = true,
    val showTotalClientsCard: Boolean = true,
    val showCurrencyBreakdown: Boolean = true,
    val showRecentInvoices: Boolean = true
)
