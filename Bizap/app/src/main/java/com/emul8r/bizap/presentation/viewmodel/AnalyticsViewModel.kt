package com.emul8r.bizap.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.AnalyticsDao
import com.emul8r.bizap.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * ViewModel for analytics dashboard.
 * Aggregates multiple data sources into unified analytics state.
 * Designed for both GUI1 and GUI2 dashboards.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsDao: AnalyticsDao
) : ViewModel() {

    // Default to businessId = 1 (primary business)
    private val businessId = 1L

    // ═════════════════════════════════════════════════════════════════
    // PUBLIC STATE FLOWS
    // ═════════════════════════════════════════════════════════════════

    /**
     * Cash flow trend over last 30 days.
     * Combines daily revenue snapshots into visual data.
     */
    val cashFlowTrend: StateFlow<List<CashFlowTrendPoint>> =
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
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Top customers by revenue with percentage breakdown.
     */
    val topCustomers: StateFlow<List<TopCustomerMetric>> =
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
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    /**
     * Current average days to payment (DSO).
     */
    val averageDaysToPayment: StateFlow<Double> =
        analyticsDao.observeAverageDaysToPayment(businessId)
            .catch { error ->
                Timber.e(error, "Error loading average days to payment")
                emit(0.0)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = 0.0
            )

    /**
     * Invoicing velocity trend (days from creation to sent).
     */
    val invoicingVelocity: StateFlow<List<InvoiceVelocity>> =
        analyticsDao.observeInvoicingVelocity(businessId)
            .catch { error ->
                Timber.e(error, "Error loading invoicing velocity")
                emit(emptyList())
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
        analyticsDao.observeTotalRevenue(businessId)
            .catch { error ->
                Timber.e(error, "Error loading total revenue")
                emit(0L)
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
        analyticsDao.observeTotalOutstanding(businessId)
            .catch { error ->
                Timber.e(error, "Error loading outstanding")
                emit(0L)
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
        analyticsDao.observeDraftInvoiceCount(businessId)
            .catch { error ->
                Timber.e(error, "Error loading draft count")
                emit(0)
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
        analyticsDao.observeOverdueInvoiceCount(businessId)
            .catch { error ->
                Timber.e(error, "Error loading overdue count")
                emit(0)
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
        totalOutstanding
    ) { trend, customers, dsoValue, velocity, revenue, outstanding ->
        Timber.d("AnalyticsViewModel: State updated for businessId=$businessId")

        AnalyticsUiState.Success(
            AnalyticsData(
                cashFlowTrend = trend,
                averageDaysToPayTrend = emptyList(), // Will be populated separately
                topCustomerMetrics = customers,
                currentAverageDaysToPayment = dsoValue,
                totalRevenue = revenue,
                paymentMetrics = PaymentMetrics(
                    averageDaysToPayment = dsoValue,
                    totalOutstandingCents = outstanding,
                    totalCollectedCents = revenue,
                    overdueInvoiceCount = 0, // Fetch separately if needed
                    overdueAmountCents = 0,
                    invoiceCountByStatus = emptyMap()
                )
            )
        )
    }
        .catch { error ->
            Timber.e(error, "AnalyticsViewModel: Error aggregating state")
            emit(AnalyticsUiState.Error(error.message ?: "Unknown error"))
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
        Timber.d("AnalyticsViewModel: Refresh triggered for businessId=$businessId")
        // Flow-based queries auto-refresh, but this can be called for manual refresh
    }

    /**
     * Clean up old data (call periodically or on app startup).
     */
    suspend fun cleanupOldData() {
        try {
            analyticsDao.cleanupOldDailyRevenue()
            analyticsDao.cleanupOldVelocityMetrics()
            analyticsDao.cleanupStaleCustomerRevenue(System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000))
            Timber.d("AnalyticsViewModel: Cleanup completed")
        } catch (e: Exception) {
            Timber.e(e, "AnalyticsViewModel: Error during cleanup")
        }
    }
}

/**
 * UI state for analytics screen.
 * Sealed class ensures exhaustive when statements.
 */
sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    data class Success(val data: AnalyticsData) : AnalyticsUiState()
    data class Error(val message: String) : AnalyticsUiState()
}

