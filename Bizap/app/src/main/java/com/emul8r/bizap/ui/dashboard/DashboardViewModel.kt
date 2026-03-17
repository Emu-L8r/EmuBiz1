package com.emul8r.bizap.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.dao.InvoiceDaoV2
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import com.emul8r.bizap.domain.config.BizapConfig
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.usecase.DateChangeTickerManager
import com.emul8r.bizap.domain.usecase.DateChangeTickerObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

sealed class DashboardRevenueState {
    object Loading : DashboardRevenueState()
    data class Success(val metrics: RevenueMetricsV2) : DashboardRevenueState()
    data class Error(val message: String) : DashboardRevenueState()
}

/**
 * ViewModel for the GUI1 Dashboard screen.
 * Uses the same V2 repositories as GUI2 to ensure data consistency
 * between both GUIs.
 *
 * Implements [DateChangeTickerObserver] to automatically refresh revenue data
 * at midnight when date-dependent calculations become stale.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val revenueRepository: RevenueRepositoryV2,
    private val businessContextRepository: BusinessContextRepositoryV2,
    private val invoiceDaoV2: InvoiceDaoV2,
    private val dateChangeTickerManager: DateChangeTickerManager,
    private val bizapConfig: BizapConfig
) : ViewModel(), DateChangeTickerObserver {

    /**
     * Emitting to this flow causes the revenue query to restart with fresh date parameters.
     */
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    private val activeBusinessId: Flow<Long> = businessContextRepository.observeActiveBusinessId()

    val revenueState: StateFlow<DashboardRevenueState> =
        combine(
            activeBusinessId,
            _refreshTrigger
        ) { businessId, _ -> businessId }
            .flatMapLatest { businessId ->
                revenueRepository.observeRevenueMetrics(businessId)
                    .map { result ->
                        result.fold(
                            onSuccess = { metrics ->
                                Timber.d("DashboardViewModel: Revenue metrics updated for business $businessId")
                                DashboardRevenueState.Success(metrics)
                            },
                            onFailure = { error ->
                                Timber.e(error, "DashboardViewModel: Failed to load revenue metrics")
                                DashboardRevenueState.Error(error.message ?: "Unknown error")
                            }
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DashboardRevenueState.Loading
            )

    /**
     * Invoice counts grouped by status, computed at the database layer to avoid
     * O(n) groupBy operations on the UI thread during recomposition.
     *
     * Keys are invoice status names (e.g. "PAID", "SENT", "OVERDUE", "DRAFT").
     */
    val statusCounts: StateFlow<Map<String, Int>> =
        activeBusinessId
            .flatMapLatest { businessId ->
                invoiceDaoV2.observeInvoiceCountByStatus(businessId)
                    .map { counts -> counts.associate { it.status to it.count } }
                    .catch { e ->
                        Timber.e(e, "DashboardViewModel: Failed to load invoice status counts")
                        emit(emptyMap())
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyMap()
            )

    private var tickerStarted = false

    init {
        if (bizapConfig.dashboardRefreshOnDateChange && bizapConfig.enableAutoRefresh) {
            dateChangeTickerManager.registerObserver(this)
            dateChangeTickerManager.startWatching()
            tickerStarted = true
        }
    }

    /**
     * Called automatically at midnight when the date changes.
     * Triggers a dashboard refresh so date-dependent revenue calculations are updated.
     */
    override suspend fun onDateChanged(newDate: LocalDate) {
        Timber.d("DashboardViewModel: Date changed to $newDate, refreshing dashboard data")
        _refreshTrigger.emit(Unit)
    }

    /**
     * Manually trigger a dashboard data refresh.
     */
    fun manualRefresh() {
        _refreshTrigger.tryEmit(Unit)
    }

    override fun onCleared() {
        if (tickerStarted) {
            dateChangeTickerManager.stopWatching()
        }
        super.onCleared()
    }
}
