package com.emul8r.bizap.ui.risk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.invoice.model.InvoicePaymentStatus
import com.emul8r.bizap.domain.invoice.usecase.IdentifyRiskInvoicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RiskDashboardViewModel @Inject constructor(
    private val identifyRiskInvoicesUseCase: IdentifyRiskInvoicesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<RiskUiState>(RiskUiState.Loading)
    val uiState: StateFlow<RiskUiState> = _uiState.asStateFlow()

    private val businessId = 1L

    init {
        refreshRiskInvoices()
    }

    fun refreshRiskInvoices() {
        viewModelScope.launch {
            try {
                _uiState.value = RiskUiState.Loading
                val risks = identifyRiskInvoicesUseCase.execute(businessId)
                _uiState.value = RiskUiState.Success(risks)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load risk invoices")
                _uiState.value = RiskUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }
}

sealed class RiskUiState {
    object Loading : RiskUiState()
    data class Success(val riskInvoices: List<InvoicePaymentStatus>) : RiskUiState()
    data class Error(val message: String) : RiskUiState()
}

