package com.emu.emubizwax.ui.dashboard

sealed interface DashboardUiState {
    object Loading : DashboardUiState
    data class Success(val stats: DashboardStats) : DashboardUiState
    data class Error(val message: String) : DashboardUiState
}

data class DashboardStats(
    val totalCustomers: Int = 0,
    val activeInvoices: Int = 0,
    val pendingQuotes: Int = 0,
    val totalRevenue: Double = 0.0
)
