package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.network.NetworkConnectivityManager
import com.emul8r.bizap.data.repository.OfflineSyncQueue
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val repository: InvoiceRepository,
    private val connectivityManager: NetworkConnectivityManager,
    private val syncQueue: OfflineSyncQueue
) : ViewModel() {

    val syncState: StateFlow<SyncUiState> = combine(
        connectivityManager.observeConnectivity(),
        syncQueue.pendingCount
    ) { isOnline, pendingCount ->
        SyncUiState(
            isOnline = isOnline,
            pendingCount = pendingCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SyncUiState(isOnline = true, pendingCount = 0)
    )

    val uiState: StateFlow<InvoiceListUiState> = repository.getAllInvoicesWithItems()
        .map { list ->
            if (list.isEmpty()) InvoiceListUiState.Empty
            else InvoiceListUiState.Success(list)
        }
        .catch { e -> emit(InvoiceListUiState.Error(e.message ?: "Unknown Error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InvoiceListUiState.Loading
        )

    fun updateInvoiceStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            val status = InvoiceStatus.valueOf(newStatus)
            repository.updateInvoiceStatus(id, status)
        }
    }
}

data class SyncUiState(
    val isOnline: Boolean,
    val pendingCount: Int
)

sealed interface InvoiceListUiState {
    data object Loading : InvoiceListUiState
    data object Empty : InvoiceListUiState
    data class Success(val invoices: List<Invoice>) : InvoiceListUiState
    data class Error(val message: String) : InvoiceListUiState
}