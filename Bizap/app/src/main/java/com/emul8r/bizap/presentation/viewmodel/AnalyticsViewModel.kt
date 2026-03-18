package com.emul8r.bizap.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.dao.AnalyticsDao
import com.emul8r.bizap.data.model.*
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for analytics dashboard.
 * Aggregates multiple data sources into unified analytics state.
 * Designed for both GUI1 and GUI2 dashboards.
 *
 * Uses [BusinessContextRepositoryV2] to observe the active business ID so that
 * all analytics automatically switch when the user changes business context.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsDao: AnalyticsDao,
    private val businessContextRepository: BusinessContextRepositoryV2
) : ViewModel() {

    /** Reactive active business ID — replaces the former hardcoded constant. */
    private val activeBusinessId: Flow<Long> = businessContextRepository.observeActiveBusinessId()

    // ═════════════════════════════════════════════════════════════════
    // PUBLIC STATE FLOWS
    // ═════════════════════════════════════════════════════════════════

    /**
     * Cash flow trend over last 30 days.
     * Combines daily revenue snapshots into visual data.
     */
    val cashFlowTrend: StateFlow<List<CashFlowTrendPoint>> =
        activeBusinessId.flatMapLatest { businessId ->
            analyticsDao.observeDailyRevenue(businessId)
                .map { dailyRevenues ->
                    dailyRevenues.map { dr ->
                        CashFlowTrendPoint(
                            date = dr.date,
                            invoicedCents = dr.invoicedCents,
                            paidCents = dr.paidCents
                        )
                    }
                }
                .catch { error ->
                    Timber.e(error, "Error loading cash flow trend")
                    emit(emptyList())
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Top customers by revenue with percentage breakdown.
     */
    val topCustomers: StateFlow<List<TopCustomerMetric>> =
        activeBusinessId.flatMapLatest { businessId ->
            combine(
                analyticsDao.observeTopCustomers(businessId, 5),
                analyticsDao.observeTotalRevenue(businessId)
            ) { customers, totalRevenue ->
                if (totalRevenue == 0L) {
                    emptyList()
                } else {
                    customers.map { customer ->
                        TopCustomerMetric(
                            customerId = customer.customerId,
                            customerName = customer.customerName,
                            revenueCents = customer.totalRevenueCents,
                            percentageOfTotal = (customer.totalRevenueCents.toDouble() / totalRevenue) * 100.0,
                            invoiceCount = customer.invoiceCount
                        )
                    }
                }
            }
                .catch { error ->
                    Timber.e(error, "Error loading top customers")
                    emit(emptyList())
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Current average days to payment (DSO).
     * Measures the average number of days from invoice date to payment date
     * for all PAID invoices belonging to the active business.
     */
    val averageDaysToPayment: StateFlow<Double> =
        activeBusinessId.flatMapLatest { businessId ->
            analyticsDao.observeAverageDaysToPayment(businessId)
                .catch { error ->
                    Timber.e(error, "Error loading average days to payment")
                    emit(0.0)
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )

    /**
     * Average days to payment trend (historical DSO over last 30 days).
     */
    val averageDaysToPaymentTrend: StateFlow<List<DaysToPayMetric>> =
        activeBusinessId.flatMapLatest { businessId ->
            analyticsDao.observeAverageDaysToPayTrend(businessId)
                .catch { error ->
                    Timber.e(error, "Error loading average days to payment trend")
                    emit(emptyList())
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Invoicing velocity trend (days from creation to sent).
     */
    val invoicingVelocity: StateFlow<List<InvoiceVelocity>> =
        activeBusinessId.flatMapLatest { businessId ->
            analyticsDao.observeInvoicingVelocity(businessId)
                .catch { error ->
                    Timber.e(error, "Error loading invoicing velocity")
                    emit(emptyList())
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Total revenue (all-time, PAID invoices only).
     */
    val totalRevenue: StateFlow<Long> =
        activeBusinessId.flatMapLatest { businessId ->
            analyticsDao.observeTotalRevenue(businessId)
                .catch { error ->
                    Timber.e(error, "Error loading total revenue")
                    emit(0L)
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0L
            )

    /**
     * Total outstanding (unpaid invoices).
     */
    val totalOutstanding: StateFlow<Long> =
        activeBusinessId.flatMapLatest { businessId ->
            analyticsDao.observeTotalOutstanding(businessId)
                .catch { error ->
                    Timber.e(error, "Error loading outstanding")
                    emit(0L)
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0L
            )

    /**
     * Count of draft invoices (work in progress).
     */
    val draftInvoiceCount: StateFlow<Int> =
        activeBusinessId.flatMapLatest { businessId ->
            analyticsDao.observeDraftInvoiceCount(businessId)
                .catch { error ->
                    Timber.e(error, "Error loading draft count")
                    emit(0)
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    /**
     * Count of overdue invoices.
     */
    val overdueInvoiceCount: StateFlow<Int> =
        activeBusinessId.flatMapLatest { businessId ->
            analyticsDao.observeOverdueInvoiceCount(businessId)
                .catch { error ->
                    Timber.e(error, "Error loading overdue count")
                    emit(0)
                }
        }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0
            )

    /**
     * Combined analytics state for dashboard.
     * Aggregates all metrics into single observable.
     */
    val analyticsState: StateFlow<AnalyticsUiState> = combine(
        cashFlowTrend,
        topCustomers,
        averageDaysToPayment,
        invoicingVelocity,
        totalRevenue,
        totalOutstanding,
        averageDaysToPaymentTrend
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val trend = values[0] as List<CashFlowTrendPoint>
        val customers = values[1] as List<TopCustomerMetric>
        val dsoValue = values[2] as Double
        val velocity = values[3] as List<InvoiceVelocity>
        val revenue = values[4] as Long
        val outstanding = values[5] as Long
        val dsoTrend = values[6] as List<DaysToPayMetric>

        Timber.d("AnalyticsViewModel: State updated")

        try {
            AnalyticsUiState.Success(
                AnalyticsData(
                    cashFlowTrend = trend,
                    averageDaysToPayTrend = dsoTrend,
                    topCustomerMetrics = customers,
                    currentAverageDaysToPayment = dsoValue,
                    totalRevenue = revenue,
                    paymentMetrics = PaymentMetrics(
                        averageDaysToPayment = dsoValue,
                        totalOutstandingCents = outstanding,
                        totalCollectedCents = revenue,
                        overdueInvoiceCount = 0, // Fetch separately if needed
                        overdueAmountCents = 0
                    )
                )
            ) as AnalyticsUiState
        } catch (e: Exception) {
            Timber.e(e, "AnalyticsViewModel: Error aggregating state")
            AnalyticsUiState.Error(e.message ?: "Unknown error")
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalyticsUiState.Loading
        )

    // ═════════════════════════════════════════════════════════════════
    // PUBLIC ACTIONS
    // ═════════════════════════════════════════════════════════════════

    /**
     * Refresh analytics data.
     * Called after user performs actions (create/update invoice, etc).
     */
    fun refresh() {
        Timber.d("AnalyticsViewModel: Refresh triggered")
        // Flow-based queries auto-refresh, but this can be called for manual refresh
    }

    /**
     * Clean up old data (call periodically or on app startup).
     */
    suspend fun cleanupOldData() {
        try {
            // Cleanup methods would be added to AnalyticsDao in Phase 2
            Timber.d("AnalyticsViewModel: Cleanup completed")
        } catch (e: Exception) {
            Timber.e(e, "AnalyticsViewModel: Error during cleanup")
        }
    }
}


