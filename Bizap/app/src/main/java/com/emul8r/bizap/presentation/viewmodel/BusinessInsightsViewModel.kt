package com.emul8r.bizap.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.model.insights.*
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for business insights and health monitoring.
 */
@HiltViewModel
class BusinessInsightsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route: ScreenV2.BusinessInsights = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    private val _insightsSummaryState = MutableStateFlow<InsightsSummaryState>(
        InsightsSummaryState.Loading
    )
    val insightsSummaryState: StateFlow<InsightsSummaryState> = _insightsSummaryState.asStateFlow()

    private val _cashFlowState = MutableStateFlow<CashFlowState>(
        CashFlowState.Loading
    )
    val cashFlowState: StateFlow<CashFlowState> = _cashFlowState.asStateFlow()

    private val _atRiskCustomersState = MutableStateFlow<AtRiskCustomersState>(
        AtRiskCustomersState.Loading
    )
    val atRiskCustomersState: StateFlow<AtRiskCustomersState> = _atRiskCustomersState.asStateFlow()

    init {
        loadInsights()
    }

    private fun loadInsights() {
        try {
            _insightsSummaryState.value = InsightsSummaryState.Loading
            _cashFlowState.value = CashFlowState.Loading
            _atRiskCustomersState.value = AtRiskCustomersState.Loading

            // Load all insights
            try {
                val summary = generateBusinessInsightsSummary()
                _insightsSummaryState.value = InsightsSummaryState.Success(summary)
            } catch (e: Exception) {
                _insightsSummaryState.value = InsightsSummaryState.Error(e.message ?: "Unknown error")
            }

            try {
                val cashFlow = generateCashFlowPrediction()
                _cashFlowState.value = CashFlowState.Success(cashFlow)
            } catch (e: Exception) {
                _cashFlowState.value = CashFlowState.Error(e.message ?: "Unknown error")
            }

            try {
                val atRiskCustomers = generateAtRiskCustomers()
                _atRiskCustomersState.value = AtRiskCustomersState.Success(atRiskCustomers)
            } catch (e: Exception) {
                _atRiskCustomersState.value = AtRiskCustomersState.Error(e.message ?: "Unknown error")
            }

            Timber.d("Business insights loaded for businessId=$businessId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to load business insights")
        }
    }

    private fun generateBusinessInsightsSummary(): BusinessInsightsSummary {
        val insights = listOf(
            BusinessInsight(
                id = "payment_rate_1",
                title = "Payment Rate Increasing",
                description = "Your payment rate has improved by 5% this month",
                severity = InsightSeverity.INFO,
                icon = "trending_up"
            ),
            BusinessInsight(
                id = "overdue_1",
                title = "Overdue Invoices Present",
                description = "You have 2 invoices overdue by more than 30 days",
                severity = InsightSeverity.WARNING,
                actionUrl = "invoices?filter=overdue"
            )
        )

        val cashFlow = CashFlowPrediction(
            predictedBalance = 50000_00,
            confidence = 85.0,
            projectedInflows = 75000_00,
            projectedOutflows = 30000_00,
            riskLevel = RiskLevel.HEALTHY,
            recommendations = listOf(
                "Continue monitoring outstanding invoices",
                "Consider follow-up on invoices over 45 days old"
            )
        )

        return BusinessInsightsSummary(
            insights = insights,
            cashFlowPrediction = cashFlow,
            atRiskCustomers = emptyList(),
            healthScore = 78
        )
    }

    private fun generateCashFlowPrediction(): CashFlowPrediction {
        return CashFlowPrediction(
            predictedBalance = 50000_00,
            confidence = 85.0,
            forecastDays = 30,
            projectedInflows = 75000_00,
            projectedOutflows = 30000_00,
            riskLevel = RiskLevel.HEALTHY,
            recommendations = listOf(
                "Cash flow looks healthy",
                "Consider investing in growth"
            )
        )
    }

    private fun generateAtRiskCustomers(): List<AtRiskCustomerAlert> {
        return emptyList()
    }

    fun dismissInsight(insightId: String) {
        try {
            val currentState = _insightsSummaryState.value
            if (currentState is InsightsSummaryState.Success) {
                val updated = currentState.summary.copy(
                    insights = currentState.summary.insights.filter { it.id != insightId }
                )
                _insightsSummaryState.value = InsightsSummaryState.Success(updated)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to dismiss insight")
        }
    }

    fun refreshInsights() {
        loadInsights()
    }
}

sealed class InsightsSummaryState {
    object Loading : InsightsSummaryState()
    data class Success(val summary: BusinessInsightsSummary) : InsightsSummaryState()
    data class Error(val message: String) : InsightsSummaryState()
}

sealed class CashFlowState {
    object Loading : CashFlowState()
    data class Success(val prediction: CashFlowPrediction) : CashFlowState()
    data class Error(val message: String) : CashFlowState()
}

sealed class AtRiskCustomersState {
    object Loading : AtRiskCustomersState()
    data class Success(val customers: List<AtRiskCustomerAlert>) : AtRiskCustomersState()
    data class Error(val message: String) : AtRiskCustomersState()
}
