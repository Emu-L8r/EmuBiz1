package com.emul8r.bizap.ui.gui2.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.repository.gui2.BusinessContextRepositoryV2
import com.emul8r.bizap.data.repository.gui2.PaymentAnalyticsRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RevenueRepositoryV2
import com.emul8r.bizap.data.repository.gui2.RiskAnalyticsRepositoryV2
import com.emul8r.bizap.domain.model.gui2.DashboardStateV2
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the GUI2 dashboard.
 * businessId is guaranteed non-null — extracted from the navigation route.
 */
@HiltViewModel
class DashboardViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revenueRepository: RevenueRepositoryV2,
    private val paymentRepository: PaymentAnalyticsRepositoryV2,
    private val riskRepository: RiskAnalyticsRepositoryV2,
    private val businessContextRepository: BusinessContextRepositoryV2
) : ViewModel() {

    private val route: ScreenV2.Dashboard = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    val uiState: StateFlow<DashboardUiStateV2> = combine(
        businessContextRepository.activeContext,
        revenueRepository.observeRevenueMetrics(businessId),
        paymentRepository.observePaymentMetrics(businessId),
        riskRepository.observeRiskMetrics(businessId)
    ) { context, revenue, payment, risk ->
        Timber.d("DashboardViewModelV2: state updated for businessId=$businessId")
        DashboardUiStateV2.Success(
            DashboardStateV2(
                businessContext = context,
                revenueMetrics = revenue.copy(
                    outstandingAmount = payment.outstandingAmount,
                    collectedAmount = payment.collectedAmount
                ),
                paymentMetrics = payment,
                riskMetrics = risk
            )
        ) as DashboardUiStateV2
    }
    .catch { error ->
        Timber.e(error, "DashboardViewModelV2: error loading state")
        emit(DashboardUiStateV2.Error(error.message ?: "Unknown error"))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiStateV2.Loading
    )
}

sealed class DashboardUiStateV2 {
    object Loading : DashboardUiStateV2()
    data class Success(val state: DashboardStateV2) : DashboardUiStateV2()
    data class Error(val message: String) : DashboardUiStateV2()
}
