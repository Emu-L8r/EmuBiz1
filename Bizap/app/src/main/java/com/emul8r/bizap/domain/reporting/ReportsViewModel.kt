package com.emul8r.bizap.domain.reporting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for managing report generation and display.
 *
 * **Responsibilities**:
 * - Generate various business reports
 * - Manage report state
 * - Handle report caching
 * - Provide data to UI
 *
 * **Supported Reports**:
 * - Cash Flow Forecasts (30/60/90 day)
 * - Risk Analysis
 * - Invoice Aging
 * - Customer Performance
 * - Revenue Forecasts
 */
@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val reportService: ReportGenerationService
) : ViewModel() {

    // State for cash flow report
    private val _cashFlowReport = MutableStateFlow<CashFlowReport?>(null)
    val cashFlowReport: StateFlow<CashFlowReport?> = _cashFlowReport.asStateFlow()

    // State for risk analysis report
    private val _riskAnalysisReport = MutableStateFlow<RiskAnalysisReport?>(null)
    val riskAnalysisReport: StateFlow<RiskAnalysisReport?> = _riskAnalysisReport.asStateFlow()

    // State for invoice aging report
    private val _invoiceAgingReport = MutableStateFlow<InvoiceAgingReport?>(null)
    val invoiceAgingReport: StateFlow<InvoiceAgingReport?> = _invoiceAgingReport.asStateFlow()

    // State for revenue forecast report
    private val _revenueForecastReport = MutableStateFlow<RevenueForecastReport?>(null)
    val revenueForecastReport: StateFlow<RevenueForecastReport?> = _revenueForecastReport.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        Timber.d("🚀 ReportsViewModel initialized")
        generateAllReports()
    }

    /**
     * Generate all available reports.
     */
    fun generateAllReports() {
        Timber.d("📊 Generating all reports")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Get all invoices
                invoiceRepository.getAllInvoicesWithItems().collect { invoices ->
                    try {
                        val businessId = 1L // TODO: Get from context

                         // Generate cash flow report
                         _cashFlowReport.value = reportService.generateCashFlowReport(invoices, businessId) as? CashFlowReport
                         Timber.d("✅ Cash flow report generated")

                         // Generate risk analysis report
                         _riskAnalysisReport.value = reportService.generateRiskAnalysisReport(invoices, businessId) as? RiskAnalysisReport
                         Timber.d("✅ Risk analysis report generated")

                         // Generate invoice aging report
                         _invoiceAgingReport.value = reportService.generateInvoiceAgingReport(invoices, businessId) as? InvoiceAgingReport
                         Timber.d("✅ Invoice aging report generated")

                         // Generate revenue forecast report
                         _revenueForecastReport.value = reportService.generateRevenueForcastReport(invoices, businessId) as? RevenueForecastReport
                         Timber.d("✅ Revenue forecast report generated")

                        _isLoading.value = false
                    } catch (e: Exception) {
                        Timber.e(e, "Error generating reports")
                        _error.value = "Failed to generate reports: ${e.message}"
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in report generation")
                _error.value = "Failed to load invoices: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Generate cash flow report only.
     */
    fun generateCashFlowReport() {
        Timber.d("💰 Generating cash flow report")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                invoiceRepository.getAllInvoicesWithItems().collect { invoices ->
                    _cashFlowReport.value = reportService.generateCashFlowReport(invoices, 1L) as? CashFlowReport
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Timber.e(e, "Error generating cash flow report")
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear all reports.
     */
    fun clearReports() {
        Timber.d("🗑️ Clearing all reports")
        _cashFlowReport.value = null
        _riskAnalysisReport.value = null
        _invoiceAgingReport.value = null
        _revenueForecastReport.value = null
        _error.value = null
    }

    /**
     * Clear error message.
     */
    fun clearError() {
        _error.value = null
    }
}



