package com.emul8r.bizap.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import com.emul8r.bizap.domain.config.BizapConfig
import com.emul8r.bizap.domain.model.gui2.RevenueMetricsV2
import com.emul8r.bizap.domain.usecase.DateChangeTickerManager
import com.emul8r.bizap.domain.usecase.DateChangeTickerObserver
import com.emul8r.bizap.utils.FirebaseEventTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Dashboard revenue state for GUI1 dashboard screen.
 *
 * Represents all possible states for revenue metrics display.
 *
 * @see DashboardViewModel
 */
sealed class DashboardRevenueState {
    /**
     * Initial loading state.
     *
     * UI displays loading spinner or skeleton.
     */
    object Loading : DashboardRevenueState()

    /**
     * Successfully loaded revenue metrics.
     *
     * @param metrics Revenue data for display (cash flow, trends, etc.)
     */
    data class Success(val metrics: RevenueMetricsV2) : DashboardRevenueState()

    /**
     * Error loading revenue metrics.
     *
     * @param message Error message to display to user
     */
    data class Error(val message: String) : DashboardRevenueState()
}

/**
 * Manages dashboard screen state and revenue metrics aggregation.
 *
 * **Purpose:**
 * Displays comprehensive business dashboard with revenue metrics, invoice status summaries,
 * and performance indicators. Works for both GUI1 and GUI2 dashboards.
 *
 * **Architecture:**
 * - Observes active business context from BusinessContextRepositoryV2
 * - Queries revenue metrics from RevenueRepository
 * - Observes date changes to refresh daily calculations
 * - Transforms raw metrics into dashboard-friendly state
 * - Multi-business support via context awareness
 *
 * **Key Features:**
 * 1. **Revenue Metrics:** Cash flow, revenue trends, forecasts
 * 2. **Invoice Status Counts:** PAID, SENT, OVERDUE, DRAFT status breakdown
 * 3. **Date-aware Refresh:** Auto-refresh at midnight (DateChangeTickerObserver)
 * 4. **Multi-business:** Auto-switches dashboard when business context changes
 *
 * **Data Flow:**
 * ```
 * Active Business ID observed
 *     ↓
 * Revenue Repository queries metrics
 *     ↓
 * Database computes aggregations
 *     ↓
 * Transform to DashboardRevenueState
 *     ↓
 * StateFlow emits updates
 *     ↓
 * UI displays metrics
 * ```
 *
 * **Midnight Refresh:**
 * Implements [DateChangeTickerObserver] to receive date-change notifications at midnight.
 * When date changes, refresh trigger fires, causing revenueState to re-query with new date params.
 * This ensures daily calculations (e.g., "days overdue") remain accurate without app restart.
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun DashboardScreen() {
 *     val viewModel: DashboardViewModel = hiltViewModel()
 *     val revenueState by viewModel.revenueState.collectAsStateWithLifecycle()
 *     val statusCounts by viewModel.statusCounts.collectAsStateWithLifecycle()
 *     val topCustomers by viewModel.topCustomers.collectAsStateWithLifecycle()
 *
 *     when (revenueState) {
 *         DashboardRevenueState.Loading -> LoadingScreen()
 *         is DashboardRevenueState.Success -> {
 *             val metrics = (revenueState as DashboardRevenueState.Success).metrics
 *             DashboardContent(
 *                 metrics = metrics,
 *                 statusCounts = statusCounts,
 *                 topCustomers = topCustomers
 *             )
 *         }
 *         is DashboardRevenueState.Error -> {
 *             val message = (revenueState as DashboardRevenueState.Error).message
 *             ErrorScreen(message)
 *         }
 *     }
 * }
 * ```
 *
 * @param revenueRepository Provides revenue metrics and aggregations
 * @param businessContextRepository Provides active business ID context
 * @param dateChangeTickerManager Notifies at midnight for date-dependent refresh
 * @param bizapConfig Application configuration
 *
 * @see DateChangeTickerObserver
 * @see RevenueRepository
 * @see RevenueMetricsV2
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val revenueRepository: RevenueRepository,
    private val businessContextRepository: BusinessContextRepositoryV2,
    private val dateChangeTickerManager: DateChangeTickerManager,
    private val bizapConfig: BizapConfig,
    val eventTracker: FirebaseEventTracker
) : ViewModel(), DateChangeTickerObserver {

    /**
     * Trigger for revenue query refresh.
     *
     * Emitting to this flow causes revenueState to restart the query with fresh parameters.
     * Used when:
     * - Date changes (midnight) → calls [onDateChanged]
     * - Business context switches → automatic via combine
     * - Manual refresh needed → emit from public method
     *
     * Replay=1 ensures the latest refresh signal is maintained.
     */
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1).also { it.tryEmit(Unit) }

    /**
     * Reactive active business ID stream.
     *
     * When user switches business context, this emits new ID
     * and all downstream metrics automatically recalculate.
     */
    private val activeBusinessId: Flow<Long> = businessContextRepository.observeActiveBusinessId()

    /**
     * Revenue metrics as reactive state flow.
     *
     * **Data Flow:**
     * activeBusinessId + refreshTrigger
     *     ↓
     * Combine & observe business ID
     *     ↓
     * Query RevenueRepository.observeRevenueMetrics()
     *     ↓
     * Transform success → DashboardRevenueState.Success
     * Transform failure → DashboardRevenueState.Error
     *     ↓
     * StateFlow emits updates
     *
     * **Subscription:** WhileSubscribed (5-second timeout)
     * **Initial:** Loading
     */
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
                businessContextRepository.observeInvoiceCountByStatus(businessId)
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

    /**
     * Initialization block.
     *
     * Registers for date-change notifications if configured.
     * This enables auto-refresh at midnight for date-dependent calculations.
     */
    init {
        if (bizapConfig.dashboardRefreshOnDateChange && bizapConfig.enableAutoRefresh) {
            dateChangeTickerManager.registerObserver(this)
            dateChangeTickerManager.startWatching()
            tickerStarted = true
        }
    }

    /**
     * Called automatically at midnight when the date changes.
     *
     * **Behavior:**
     * - Emits to refreshTrigger
     * - Causes revenueState to restart query with new date
     * - Auto-refreshes date-dependent calculations
     * - No user action needed
     *
     * @param newDate The new date after midnight
     */
    override suspend fun onDateChanged(newDate: LocalDate) {
        Timber.d("DashboardViewModel: Date changed to $newDate, refreshing dashboard data")
        _refreshTrigger.emit(Unit)
    }

    /**
     * Manually trigger a dashboard data refresh.
     *
     * **Use when:**
     * - User pulls to refresh
     * - Business context changes
     * - Manual refresh button clicked
     * - After background data sync
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
