package com.emul8r.bizap.ui.invoice.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.SnapshotRebuildService
import com.emul8r.bizap.domain.invoice.model.PaymentAnalyticsSummary
import com.emul8r.bizap.domain.invoice.usecase.GetPaymentAnalyticsUseCase
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for Payment Analytics Dashboard.
 * Uses reactive StateFlow to auto-update when payment data changes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PaymentAnalyticsViewModel @Inject constructor(
    private val getPaymentAnalyticsUseCase: GetPaymentAnalyticsUseCase,
    private val businessProfileRepository: BusinessProfileRepository,
    private val snapshotRebuildService: SnapshotRebuildService
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)
    private val _overrideBusinessId = MutableStateFlow<Long?>(null)

    val state: StateFlow<PaymentAnalyticsUiState> = combine(
        businessProfileRepository.activeProfile,
        _refreshTrigger,
        _overrideBusinessId
    ) { profile, _, override ->
        override ?: profile.id
    }
        .flatMapLatest { businessId ->
            getPaymentAnalyticsUseCase(businessId)
                .map { analytics ->
                    Timber.d("✅ PaymentAnalyticsViewModel: Analytics updated reactively")
                    PaymentAnalyticsUiState.Success(analytics) as PaymentAnalyticsUiState
                }
                .catch { error ->
                    Timber.e(error, "❌ PaymentAnalyticsViewModel: Error loading analytics")
                    emit(PaymentAnalyticsUiState.Error("Failed to load analytics: ${error.message}"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PaymentAnalyticsUiState.Loading
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // ✅ FIX 3: Accept businessId from navigation
    fun setBusinessId(businessId: Long?) {
        if (businessId != null) {
            _overrideBusinessId.value = businessId
            Timber.d("📍 PaymentAnalyticsViewModel: Using business context $businessId from navigation")
        }
    }

    fun forceRefresh() {
        _refreshTrigger.value++
        Timber.d("🔄 PaymentAnalyticsViewModel: Force refresh triggered")
    }

    fun rebuildSnapshots() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val report = snapshotRebuildService.rebuildAllSnapshots()
                Timber.d("📊 Rebuild complete: $report")
                _snackbarMessage.emit("Analytics rebuilt: ${report.snapshotsSynced} invoices processed")
                forceRefresh()
            } catch (e: Exception) {
                Timber.e(e, "❌ Rebuild failed")
                _snackbarMessage.emit("Rebuild failed: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

sealed class PaymentAnalyticsUiState {
    object Loading : PaymentAnalyticsUiState()
    data class Success(val analytics: PaymentAnalyticsSummary) : PaymentAnalyticsUiState()
    data class Error(val message: String) : PaymentAnalyticsUiState()
}







