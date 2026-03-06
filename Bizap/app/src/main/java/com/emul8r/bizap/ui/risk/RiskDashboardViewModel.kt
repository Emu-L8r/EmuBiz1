package com.emul8r.bizap.ui.risk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.invoice.model.InvoicePaymentStatus
import com.emul8r.bizap.domain.invoice.usecase.IdentifyRiskInvoicesUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Risk Dashboard.
 * ✅ FIXED: Now properly supports refresh when invoice data changes
 */
@HiltViewModel
class RiskDashboardViewModel @Inject constructor(
    private val identifyRiskInvoicesUseCase: IdentifyRiskInvoicesUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RiskUiState>(RiskUiState.Loading)
    val uiState: StateFlow<RiskUiState> = _uiState.asStateFlow()

    init {
        loadRiskInvoices()
    }

    /**
     * Load risk invoices from the database.
     * ✅ IMPROVED: Made public so it can be called to refresh data
     */
    fun loadRiskInvoices() {
        viewModelScope.launch {
            try {
                Timber.d("⚠️  RiskDashboardViewModel: Loading risk invoices")
                _uiState.value = RiskUiState.Loading
                val businessId = businessProfileRepository.getActiveBusinessId()
                val risks = identifyRiskInvoicesUseCase.execute(businessId)
                Timber.d("✅ RiskDashboardViewModel: Loaded ${risks.size} risk invoices")
                _uiState.value = RiskUiState.Success(risks)
            } catch (e: Exception) {
                Timber.e(e, "❌ RiskDashboardViewModel: Failed to load risk invoices")
                _uiState.value = RiskUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    /**
     * ✅ IMPROVED: Better naming - call this after invoice operations
     * Refreshes the list of at-risk invoices
     */
    fun refreshRiskInvoices() {
        Timber.d("🔄 RiskDashboardViewModel: Refreshing risk invoices after invoice operation")
        loadRiskInvoices()
    }
}

sealed class RiskUiState {
    object Loading : RiskUiState()
    data class Success(val riskInvoices: List<InvoicePaymentStatus>) : RiskUiState()
    data class Error(val message: String) : RiskUiState()
}
