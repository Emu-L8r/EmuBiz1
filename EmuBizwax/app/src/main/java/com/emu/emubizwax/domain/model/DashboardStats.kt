package com.emu.emubizwax.domain.model

import java.math.BigDecimal

data class DashboardStats(
    val totalCustomers: Int,
    val activeInvoices: Int,
    val pendingQuotes: Int,
    val totalRevenue: BigDecimal
)
