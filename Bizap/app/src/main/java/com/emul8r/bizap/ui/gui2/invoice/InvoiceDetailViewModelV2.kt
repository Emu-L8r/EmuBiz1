package com.emul8r.bizap.ui.gui2.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the GUI2 invoice detail screen.
 * businessId and invoiceId are guaranteed non-null from the navigation route.
 */
@HiltViewModel
class InvoiceDetailViewModelV2 @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val invoiceDao: InvoiceDao
) : ViewModel() {

    private val route: ScreenV2.InvoiceDetail = savedStateHandle.toRoute()
    val businessId: Long = route.businessId
    val invoiceId: Long = route.invoiceId

    val uiState: StateFlow<InvoiceDetailUiStateV2> =
        invoiceDao.getInvoiceWithItemsById(invoiceId)
            .map<InvoiceWithItems?, InvoiceDetailUiStateV2> { invoice ->
                if (invoice == null) {
                    Timber.w("InvoiceDetailViewModelV2: invoice $invoiceId not found")
                    InvoiceDetailUiStateV2.NotFound
                } else {
                    Timber.d("InvoiceDetailViewModelV2: invoice $invoiceId loaded")
                    InvoiceDetailUiStateV2.Success(invoice)
                }
            }
            .catch { error ->
                Timber.e(error, "InvoiceDetailViewModelV2: error loading invoice $invoiceId")
                emit(InvoiceDetailUiStateV2.Error(error.message ?: "Unknown error"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InvoiceDetailUiStateV2.Loading
            )
}

sealed class InvoiceDetailUiStateV2 {
    object Loading : InvoiceDetailUiStateV2()
    object NotFound : InvoiceDetailUiStateV2()
    data class Success(val invoice: InvoiceWithItems) : InvoiceDetailUiStateV2()
    data class Error(val message: String) : InvoiceDetailUiStateV2()
}
