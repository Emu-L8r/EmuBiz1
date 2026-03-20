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
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditInvoiceViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val route: ScreenV2.EditInvoice = savedStateHandle.toRoute()
    val businessId: Long = route.businessId
    private val invoiceId: Long = route.invoiceId

    val uiState: StateFlow<EditInvoiceUiStateV2> = invoiceRepository
        .getInvoiceWithItemsById(invoiceId)
        .map { invoice ->
            Timber.d("EditInvoiceViewModelV2: Loaded invoice $invoiceId")
            if (invoice != null) {
                EditInvoiceUiStateV2.Success(invoice) as EditInvoiceUiStateV2
            } else {
                EditInvoiceUiStateV2.Error("Invoice not found")
            }
        }
        .catch { exception ->
            Timber.e(exception, "EditInvoiceViewModelV2: Failed to load invoice")
            emit(EditInvoiceUiStateV2.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditInvoiceUiStateV2.Loading
        )

    fun updateInvoice(
        invoice: Invoice,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                Timber.d("EditInvoiceViewModelV2: Updating invoice $invoiceId")
                invoiceRepository.saveInvoice(invoice)
                Timber.d("EditInvoiceViewModelV2: Invoice updated successfully")
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "EditInvoiceViewModelV2: Failed to update invoice")
            }
        }
    }
}

