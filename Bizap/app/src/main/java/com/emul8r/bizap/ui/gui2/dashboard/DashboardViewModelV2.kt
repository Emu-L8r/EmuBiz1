package com.emul8r.bizap.ui.gui2.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.InvoiceMetricsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import com.emul8r.bizap.domain.analytics.AnalyticsRepository
import com.emul8r.bizap.domain.analytics.InvoiceAnalyticsEvent
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.NoteRepository
import com.emul8r.bizap.domain.revenue.repository.RevenueRepository
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the GUI2 dashboard.
 * businessId is guaranteed non-null — extracted from the navigation route.
 *
 * Provides:
 * - uiState: Combined dashboard metrics (revenue, payment, risk, invoices)
 * - statusCounts: Invoice status breakdown (PAID, SENT, DRAFT, etc.) for pie chart
 * - currentNotesCount: Count of current notes for notes card
 */
@HiltViewModel
class DashboardViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revenueRepository: RevenueRepository,
    private val paymentRepository: PaymentAnalyticsRepositoryV2,
    private val riskRepository: RiskAnalyticsRepositoryV2,
    private val businessContextRepository: BusinessContextRepositoryV2,
    private val invoiceMetricsRepository: InvoiceMetricsRepositoryV2,
    private val invoiceRepository: InvoiceRepository,
    private val noteRepository: NoteRepository,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val route: ScreenV2.Dashboard = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    init {
        // Log dashboard view event
        viewModelScope.launch {
            analyticsRepository.logEvent(
                InvoiceAnalyticsEvent.InvoiceViewed(
                    businessId = businessId,
                    invoiceId = 0L,  // 0 indicates dashboard view (not a specific invoice)
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // ===== MAIN DASHBOARD STATE =====
    val uiState: StateFlow<DashboardUiStateV2> = combine(
        businessContextRepository.activeContext,
        revenueRepository.observeRevenueMetrics(businessId),
        paymentRepository.observePaymentMetrics(businessId),
        riskRepository.observeRiskMetrics(businessId),
        invoiceMetricsRepository.observeInvoiceMetrics(businessId)
    ) { context, revenueResult, paymentResult, riskResult, invoiceMetricsResult ->
        val failure = revenueResult.exceptionOrNull()
            ?: paymentResult.exceptionOrNull()
            ?: riskResult.exceptionOrNull()
            ?: invoiceMetricsResult.exceptionOrNull()

        if (failure != null) {
            Timber.e(failure, "DashboardViewModelV2: error loading state")
            DashboardUiStateV2.Error(failure.message ?: "Unknown error") as DashboardUiStateV2
        } else {
            val revenue = revenueResult.getOrThrow()
            val payment = paymentResult.getOrThrow()
            Timber.d("DashboardViewModelV2: state updated for businessId=$businessId")
            DashboardUiStateV2.Success(
                DashboardStateV2(
                    businessContext = context,
                    revenueMetrics = revenue.copy(
                        outstandingAmount = payment.outstandingAmount,
                        collectedAmount = payment.collectedAmount
                    ),
                    paymentMetrics = payment,
                    riskMetrics = riskResult.getOrThrow(),
                    invoiceMetrics = invoiceMetricsResult.getOrThrow()
                )
            ) as DashboardUiStateV2
        }
    }
    .catch { error ->
        Timber.e(error, "DashboardViewModelV2: unexpected error loading state")
        emit(DashboardUiStateV2.Error(error.message ?: "Unknown error"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiStateV2.Loading
    )

    // ===== INVOICE STATUS COUNTS FOR PIE CHART =====
    val statusCounts: StateFlow<Map<String, Int>> = invoiceRepository
        .getAllInvoicesWithItems()
        .map { invoices ->
            invoices.groupingBy { it.status.toString() }.eachCount()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    // ===== NOTES COUNT FOR NOTES CARD =====
    val currentNotesCount: StateFlow<Int> = noteRepository
        .getCurrentNotesCount(businessId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )
}

sealed class DashboardUiStateV2 {
    object Loading : DashboardUiStateV2()
    data class Success(val state: DashboardStateV2) : DashboardUiStateV2()
    data class Error(val message: String) : DashboardUiStateV2()
}
