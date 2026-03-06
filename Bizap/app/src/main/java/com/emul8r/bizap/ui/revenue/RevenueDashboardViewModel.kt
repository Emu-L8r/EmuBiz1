package com.emul8r.bizap.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.SnapshotRebuildService
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.revenue.model.RevenueMetrics
import com.emul8r.bizap.domain.revenue.usecase.GetRevenueMetricsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RevenueDashboardViewModel @Inject constructor(
    private val getRevenueMetricsUseCase: GetRevenueMetricsUseCase,
    private val businessProfileRepository: BusinessProfileRepository,
    private val snapshotRebuildService: SnapshotRebuildService
) : ViewModel() {

    private val _refreshTrigger = MutableStateFlow(0)

    val uiState: StateFlow<RevenueDashboardUiState> = combine(
        businessProfileRepository.activeProfile,
        _refreshTrigger
    ) { profile, _ -> profile }
        .flatMapLatest { businessProfile ->
            getRevenueMetricsUseCase(businessProfile.id)
                .map { metrics ->
                    Timber.d("✅ RevenueDashboardViewModel: Metrics updated reactively")
                    RevenueDashboardUiState.Success(metrics) as RevenueDashboardUiState
                }
                .catch { error ->
                    Timber.e(error, "❌ RevenueDashboardViewModel: Failed to load metrics")
                    emit(RevenueDashboardUiState.Error(error.message ?: "Unknown Error"))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RevenueDashboardUiState.Loading
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    fun forceRefresh() {
        _refreshTrigger.value++
        Timber.d("🔄 RevenueDashboardViewModel: Force refresh triggered")
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

sealed class RevenueDashboardUiState {
    object Loading : RevenueDashboardUiState()
    data class Success(val metrics: RevenueMetrics) : RevenueDashboardUiState()
    data class Error(val message: String) : RevenueDashboardUiState()
}
