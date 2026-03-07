package com.emul8r.bizap.ui.gui2.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CreateInvoiceViewModelV2 @Inject constructor(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    fun createInvoice(
        invoice: Invoice,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                Timber.d("CreateInvoiceViewModelV2: Creating invoice for ${invoice.customerName}")
                invoiceRepository.saveInvoice(invoice)
                Timber.d("CreateInvoiceViewModelV2: Invoice created successfully")
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "CreateInvoiceViewModelV2: Failed to create invoice")
                onError(e.message ?: "Unknown error")
            }
        }
    }
}

