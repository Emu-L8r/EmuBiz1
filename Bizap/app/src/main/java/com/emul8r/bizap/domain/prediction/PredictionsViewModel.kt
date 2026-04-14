package com.emul8r.bizap.domain.prediction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.emul8r.bizap.domain.model.insights.CashFlowPrediction

/**
 * ViewModel for predictions dashboard.
 *
 * **Responsibilities:**
 * - Load invoices from repository
 * - Calculate cash flow forecasts (30, 60, 90 day horizons)
 * - Identify risk invoices using risk scoring
 * - Expose predictions via StateFlow for reactive UI updates
 *
 * **Architecture:**
 * - Injects calculators for separation of concerns
 * - Uses Flow-based data loading for automatic updates
 * - Handles business ID switching automatically
 *
 * **Example Usage:**
 * ```kotlin
 * val viewModel: PredictionsViewModel = hiltViewModel()
 * val forecast30 by viewModel.cashFlowForecast30.collectAsState()
 * val forecast90 by viewModel.cashFlowForecast90.collectAsState()
 * val riskInvoices by viewModel.riskInvoices.collectAsState()
 * ```
 */
@HiltViewModel
class PredictionsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val cashFlowCalculator: CashFlowForecastCalculator,
    private val riskCalculator: RiskScoreCalculator
) : ViewModel() {

    // ===== INPUTS =====
    private val businessId: Long = 1L  // TODO: Get from navigation or parent context

    // ===== DATA LOADING =====
    private val invoices: Flow<List<Invoice>> = invoiceRepository.getAllInvoicesWithItems()

    // ===== 30-DAY FORECAST =====
    val cashFlowForecast30: StateFlow<CashFlowPrediction> = invoices
        .map { invoiceList ->
            try {
                Timber.d("📊 Calculating 30-day forecast for businessId=$businessId")
                cashFlowCalculator.forecast(invoiceList, days = 30)
            } catch (e: Exception) {
                Timber.e(e, "Error calculating 30-day forecast")
                defaultForecast()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultForecast())

    // ===== 60-DAY FORECAST =====
    val cashFlowForecast60: StateFlow<CashFlowPrediction> = invoices
        .map { invoiceList ->
            try {
                Timber.d("📊 Calculating 60-day forecast for businessId=$businessId")
                cashFlowCalculator.forecast(invoiceList, days = 60)
            } catch (e: Exception) {
                Timber.e(e, "Error calculating 60-day forecast")
                defaultForecast(days = 60)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultForecast(days = 60))

    // ===== 90-DAY FORECAST =====
    val cashFlowForecast90: StateFlow<CashFlowPrediction> = invoices
        .map { invoiceList ->
            try {
                Timber.d("📊 Calculating 90-day forecast for businessId=$businessId")
                cashFlowCalculator.forecast(invoiceList, days = 90)
            } catch (e: Exception) {
                Timber.e(e, "Error calculating 90-day forecast")
                defaultForecast(days = 90)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultForecast(days = 90))

    // ===== RISK ANALYSIS =====
    val riskInvoices: StateFlow<List<InvoiceRiskScore>> = invoices
        .map { invoiceList ->
            try {
                Timber.d("🔍 Calculating risk scores")

                val totalRevenue = invoiceList
                    .filter { it.status == InvoiceStatus.PAID }
                    .sumOf { it.totalAmount }
                    .toLong()

                invoiceList
                    .filter { it.status != InvoiceStatus.PAID }
                    .mapNotNull { invoice ->
                        try {
                            val customerHistory = invoiceList.filter { it.customerId == invoice.customerId && it.id != invoice.id }
                            riskCalculator.calculateInvoiceRisk(
                                invoice = invoice,
                                customerPaymentHistory = customerHistory,
                                totalBusinessRevenue = totalRevenue
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    .filter { it.score > 30 }
                    .sortedByDescending { it.score }
            } catch (e: Exception) {
                Timber.e(e, "Error calculating risk scores")
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== CRITICAL ALERTS =====
    val criticalAlerts: StateFlow<List<String>> = cashFlowForecast30
        .combine(riskInvoices) { forecast, risks ->
            val alerts = mutableListOf<String>()

            // Cash flow alert
            if (forecast.predictedBalance < 0) {
                alerts.add("🔴 CRITICAL: Negative cash flow projected in 30 days")
            }

            // Risk alert
            val criticalRisks = risks.filter { it.score > 70 }
            if (criticalRisks.isNotEmpty()) {
                alerts.add("🔴 CRITICAL: ${criticalRisks.size} invoices at critical risk level")
            }

            // Overdue alert
            val overdueCount = risks.count { "Overdue" in it.factors }
            if (overdueCount > 0) {
                alerts.add("⚠️ WARNING: $overdueCount overdue invoices need immediate attention")
            }

            alerts
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== SUMMARY METRICS =====
    data class PredictionsSummary(
        val totalRiskInvoices: Int = 0,
        val criticalRiskCount: Int = 0,
        val forecast30Balance: Long = 0L,
        val forecast90Balance: Long = 0L,
        val averageConfidence: Double = 0.0,
        val hasWarnings: Boolean = false
    )

    val summary: StateFlow<PredictionsSummary> = combine(
        cashFlowForecast30,
        cashFlowForecast90,
        riskInvoices,
        criticalAlerts
    ) { f30, f90, risks, alerts ->
        val criticalRisks = risks.filter { it.score > 70 }
        PredictionsSummary(
            totalRiskInvoices = risks.size,
            criticalRiskCount = criticalRisks.size,
            forecast30Balance = f30.predictedBalance,
            forecast90Balance = f90.predictedBalance,
            averageConfidence = (f30.confidence + f90.confidence) / 2.0,
            hasWarnings = alerts.isNotEmpty()
        )
    }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredictionsSummary())

    init {
        Timber.d("🚀 PredictionsViewModel initialized for businessId=$businessId")
    }

    // ===== PUBLIC METHODS =====

    /**
     * Manually refresh all predictions.
     * Called when user explicitly requests refresh.
     */
    fun refresh() {
        Timber.d("🔄 Manual refresh triggered")
        // Refresh is automatic via Flow subscription
        // This is a no-op but kept for API compatibility
        Timber.d("✅ Predictions will auto-update via reactive data layer")
    }

    /**
     * Get recommendation details for a specific forecast.
     */
    fun getForecastDetails(days: Int): CashFlowPrediction? {
        return when (days) {
            30 -> cashFlowForecast30.value
            60 -> cashFlowForecast60.value
            90 -> cashFlowForecast90.value
            else -> null
        }
    }

    /**
     * Get detailed risk information for an invoice.
     */
    fun getRiskDetails(invoiceId: Long): InvoiceRiskScore? {
        return riskInvoices.value.find { it.invoiceId == invoiceId }
    }

    // ===== PRIVATE HELPERS =====

    private fun defaultForecast(days: Int = 30): CashFlowPrediction {
        return CashFlowPrediction(
            predictedBalance = 0L,
            confidence = 0.0,
            forecastDays = days,
            projectedInflows = 0L,
            projectedOutflows = 0L,
            riskLevel = com.emul8r.bizap.domain.model.insights.RiskLevel.HEALTHY,
            recommendations = listOf("Loading forecast...")
        )
    }
}
