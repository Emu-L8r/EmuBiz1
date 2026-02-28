package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.CustomerDao
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.repository.BusinessProfileRepository
import com.emul8r.bizap.data.service.InvoicePdfService
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface InvoiceDetailUiState {
    object Loading : InvoiceDetailUiState
    data class Success(val data: InvoiceWithItems) : InvoiceDetailUiState
    data class Error(val message: String) : InvoiceDetailUiState
}

@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val invoiceRepo: InvoiceRepository,
    private val pdfService: InvoicePdfService,
    private val businessProfileRepository: BusinessProfileRepository,
    private val customerDao: CustomerDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<InvoiceDetailUiState>(InvoiceDetailUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _exportEvent = MutableSharedFlow<File>()
    val exportEvent = _exportEvent.asSharedFlow()

    fun loadInvoice(id: Long) {
        viewModelScope.launch {
            _uiState.value = InvoiceDetailUiState.Loading
            val data = invoiceRepo.getInvoiceWithItemsById(id)
            _uiState.value = if (data != null) {
                InvoiceDetailUiState.Success(data)
            } else {
                InvoiceDetailUiState.Error("Invoice not found")
            }
        }
    }

    fun markAsPaid(id: Long) {
        viewModelScope.launch {
            invoiceRepo.updateInvoiceStatus(id, "PAID")
            loadInvoice(id) // Refresh UI
        }
    }

    fun generateAndShare(invoiceWithItems: InvoiceWithItems) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val businessProfile = businessProfileRepository.profile.first()
                val customerId = invoiceWithItems.invoice.customerId 
                    ?: throw Exception("Customer ID not found for invoice")
                val customer = customerDao.getCustomerById(customerId)
                    ?: throw Exception("Customer not found")
                
                val file = pdfService.generateTaxInvoice(businessProfile, invoiceWithItems, customer)
                _exportEvent.emit(file)
            } catch (e: Exception) {
                // Handle error (e.g., show a Toast)
            }
        }
    }
}
