package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
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
    private val repository: InvoiceRepository
) : ViewModel() {

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

sealed interface InvoiceListUiState {
    object Loading : InvoiceListUiState
    object Empty : InvoiceListUiState
    data class Success(val invoices: List<Invoice>) : InvoiceListUiState
    data class Error(val message: String) : InvoiceListUiState
}