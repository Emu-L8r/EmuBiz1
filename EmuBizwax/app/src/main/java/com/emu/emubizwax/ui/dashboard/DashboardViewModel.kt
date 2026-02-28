package com.emu.emubizwax.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emu.emubizwax.domain.usecase.GetDashboardStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    getDashboardStats: GetDashboardStatsUseCase
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = getDashboardStats()
        .map { stats -> DashboardUiState.Success(stats.toUiModel()) }
        .catch { emit(DashboardUiState.Error(it.message ?: "An unknown error occurred")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState.Loading
        )

    private fun com.emu.emubizwax.domain.model.DashboardStats.toUiModel(): DashboardStats {
        return DashboardStats(
            totalCustomers = totalCustomers,
            activeInvoices = activeInvoices,
            pendingQuotes = pendingQuotes,
            totalRevenue = totalRevenue.toDouble()
        )
    }
}
