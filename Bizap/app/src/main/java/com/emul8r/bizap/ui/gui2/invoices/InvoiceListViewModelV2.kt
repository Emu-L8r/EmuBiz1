package com.emul8r.bizap.ui.gui2.invoices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class InvoiceListViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val route: ScreenV2.Invoices = savedStateHandle.toRoute()
    val businessId: Long = route.businessId

    val uiState: StateFlow<InvoiceListUiStateV2> = invoiceRepository
        .getAllInvoices()
        .map { invoices ->
            Timber.d("InvoiceListViewModelV2: Loaded ${invoices.size} invoices")
            InvoiceListUiStateV2.Success(invoices) as InvoiceListUiStateV2
        }
        .catch { exception ->
            Timber.e(exception, "InvoiceListViewModelV2: Failed to load invoices")
            emit(InvoiceListUiStateV2.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InvoiceListUiStateV2.Loading
        )
}

