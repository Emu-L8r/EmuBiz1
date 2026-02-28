package com.emu.emubizwax.ui.invoices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emu.emubizwax.domain.model.InvoiceDetailsData
import com.emu.emubizwax.domain.repository.ThemeRepository
import com.emu.emubizwax.domain.usecase.GenerateInvoicePdfUseCase
import com.emu.emubizwax.domain.usecase.GetInvoiceDetailsUseCase
import com.emu.emubizwax.ui.navigation.InvoiceDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvoiceDetailsViewModel @Inject constructor(
    private val getInvoiceDetailsUseCase: GetInvoiceDetailsUseCase,
    private val generateInvoicePdfUseCase: GenerateInvoicePdfUseCase,
    private val themeRepository: ThemeRepository,
    private val shareService: ShareService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val invoiceId: Long = savedStateHandle.toRoute<InvoiceDetails>().invoiceId

    private val _invoiceState = MutableStateFlow<InvoiceDetailsData?>(null)
    val invoiceState = _invoiceState.asStateFlow()

    private val _pdfGenerationState = MutableStateFlow<PdfGenerationUiState>(PdfGenerationUiState.Idle)
    val pdfGenerationState = _pdfGenerationState.asStateFlow()

    init {
        viewModelScope.launch {
            getInvoiceDetailsUseCase(invoiceId).collect { details ->
                _invoiceState.value = details
            }
        }
    }

    fun generateAndSharePdf() {
        viewModelScope.launch(Dispatchers.IO) {
            _pdfGenerationState.value = PdfGenerationUiState.Loading
            try {
                val invoiceData = invoiceState.value ?: throw Exception("Invoice data not available")
                val themeColor = themeRepository.themeConfig.first().primaryColorHex
                
                val pdfFile = generateInvoicePdfUseCase.execute(invoiceId, themeColor, invoiceData.customer.name)
                
                shareService.sharePdf(pdfFile, invoiceData.customer.name)
                _pdfGenerationState.value = PdfGenerationUiState.Idle
            } catch (e: Exception) {
                _pdfGenerationState.value = PdfGenerationUiState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}
