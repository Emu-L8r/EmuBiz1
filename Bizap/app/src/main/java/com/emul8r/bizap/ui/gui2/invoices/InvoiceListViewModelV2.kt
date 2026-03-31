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
        .getAllInvoicesWithItems()
        .map { invoices ->
            // Filter invoices by current business ID
            Timber.d("🔍 InvoiceListViewModelV2: Received ${invoices.size} total invoices from repository")
            Timber.d("   Filter criteria: businessProfileId == $businessId")

            if (invoices.isNotEmpty()) {
                Timber.d("   Available invoices:")
                invoices.take(5).forEach { invoice ->  // Log first 5 for diagnostic purposes
                    Timber.d("      - ID=${invoice.id}, businessId=${invoice.businessProfileId}, customer=${invoice.customerName}")
                }
                if (invoices.size > 5) {
                    Timber.d("      ... and ${invoices.size - 5} more invoices")
                }
            }

            val filteredInvoices = invoices.filter { it.businessProfileId == businessId }

            Timber.d("✅ InvoiceListViewModelV2: Filtered to ${filteredInvoices.size} invoices for business $businessId")
            if (filteredInvoices.isEmpty() && invoices.isNotEmpty()) {
                Timber.w("⚠️ WARNING: No invoices matched the filter!")
                Timber.w("   Total invoices in repo: ${invoices.size}")
                Timber.w("   Expected businessId: $businessId")
                Timber.w("   Available businessIds: ${invoices.map { it.businessProfileId }.distinct()}")
                Timber.w("   This indicates a businessProfileId mismatch between save and load!")
            }

            InvoiceListUiStateV2.Success(filteredInvoices) as InvoiceListUiStateV2
        }
        .catch { exception ->
            Timber.e(exception, "❌ InvoiceListViewModelV2: Failed to load invoices")
            emit(InvoiceListUiStateV2.Error(exception.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InvoiceListUiStateV2.Loading
        )
}


sealed interface InvoiceListUiStateV2 {
    object Loading : InvoiceListUiStateV2
    data class Error(val message: String) : InvoiceListUiStateV2
    data class Success(val invoices: List<com.emul8r.bizap.domain.model.Invoice>) : InvoiceListUiStateV2
}
