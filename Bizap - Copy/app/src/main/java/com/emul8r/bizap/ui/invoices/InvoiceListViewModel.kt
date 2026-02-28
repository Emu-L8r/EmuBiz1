package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val repository: InvoiceRepository // Ensure this has getInvoicesWithItems()
) : ViewModel() {

    // 1. Observe the Flow from Room and convert to StateFlow for Compose
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

    // 2. Action to update status (Mark as Paid)
    fun updateInvoiceStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateInvoiceStatus(id, newStatus)
        }
    }
}

sealed interface InvoiceListUiState {
    object Loading : InvoiceListUiState
    object Empty : InvoiceListUiState
    data class Success(val invoices: List<InvoiceWithItems>) : InvoiceListUiState
    data class Error(val message: String) : InvoiceListUiState
}