package com.emul8r.bizap.ui.risk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.invoice.model.InvoicePaymentStatus
import com.emul8r.bizap.domain.invoice.usecase.IdentifyRiskInvoicesUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Risk Dashboard.
 * Uses reactive StateFlow to auto-update when invoice risk data changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RiskDashboardViewModel @Inject constructor(
    private val identifyRiskInvoicesUseCase: IdentifyRiskInvoicesUseCase,
    private val businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    val uiState: StateFlow<RiskUiState> = businessProfileRepository.activeProfile
        .flatMapLatest { businessProfile ->
            identifyRiskInvoicesUseCase.execute(businessProfile.id)
                .map { risks ->
                    Timber.d("✅ RiskDashboardViewModel: Loaded ${risks.size} risk invoices reactively")
                    RiskUiState.Success(risks) as RiskUiState
                }
                .catch { error ->
                    Timber.e(error, "❌ RiskDashboardViewModel: Failed to load risk invoices")
                    emit(RiskUiState.Error(error.message ?: "Unknown Error"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RiskUiState.Loading
        )
}

sealed class RiskUiState {
    object Loading : RiskUiState()
    data class Success(val riskInvoices: List<InvoicePaymentStatus>) : RiskUiState()
    data class Error(val message: String) : RiskUiState()
}
