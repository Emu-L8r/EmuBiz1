package com.emul8r.bizap.ui.gui2.invoice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.emul8r.bizap.data.local.InvoiceDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2
import com.emul8r.bizap.utils.CentsFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    private val _paymentEvent = MutableSharedFlow<String>()
    val paymentEvent: SharedFlow<String> = _paymentEvent.asSharedFlow()

    fun recordPayment(amount: Long) {
        viewModelScope.launch {
            try {
                val invoice = invoiceDao.getInvoiceById(invoiceId) ?: return@launch
                val remaining = invoice.totalAmount - invoice.amountPaid
                if (amount <= 0) {
                    _paymentEvent.emit("Payment amount must be greater than zero.")
                    return@launch
                }
                if (amount > remaining) {
                    _paymentEvent.emit(
                        "Payment exceeds the outstanding balance of ${CentsFormatter.formatCents(remaining)}."
                    )
                    return@launch
                }
                val newAmountPaid = invoice.amountPaid + amount
                Timber.d("InvoiceDetailViewModelV2: Recording payment of $amount cents")
                invoiceDao.updateAmountPaid(invoiceId, newAmountPaid)

                // Update status based on payment
                val newStatus = if (newAmountPaid >= invoice.totalAmount) {
                    com.emul8r.bizap.domain.model.InvoiceStatus.PAID
                } else {
                    com.emul8r.bizap.domain.model.InvoiceStatus.PARTIALLY_PAID
                }
                invoiceDao.updateStatus(invoiceId, newStatus)
                Timber.d("InvoiceDetailViewModelV2: Status updated to $newStatus")

                Timber.d("InvoiceDetailViewModelV2: Payment recorded successfully")
                _paymentEvent.emit("Payment recorded successfully.")
            } catch (e: Exception) {
                Timber.e(e, "InvoiceDetailViewModelV2: Failed to record payment")
                _paymentEvent.emit("Failed to record payment: ${e.message}")
            }
        }
    }

    fun updateInvoiceStatus(newStatus: com.emul8r.bizap.domain.model.InvoiceStatus) {
        viewModelScope.launch {
            try {
                val invoice = invoiceDao.getInvoiceById(invoiceId) ?: return@launch
                Timber.d("InvoiceDetailViewModelV2: Updating status to $newStatus")
                invoiceDao.updateStatus(invoiceId, newStatus)
                Timber.d("InvoiceDetailViewModelV2: Status updated successfully")
            } catch (e: Exception) {
                Timber.e(e, "InvoiceDetailViewModelV2: Failed to update status")
            }
        }
    }
}

sealed class InvoiceDetailUiStateV2 {
    object Loading : InvoiceDetailUiStateV2()
    object NotFound : InvoiceDetailUiStateV2()
    data class Success(val invoice: InvoiceWithItems) : InvoiceDetailUiStateV2()
    data class Error(val message: String) : InvoiceDetailUiStateV2()
}
