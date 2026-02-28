package com.example.databaser.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.databaser.data.Invoice
import com.example.databaser.data.InvoiceRepository
import com.example.databaser.data.MySavesRepository
import com.example.databaser.ui.PdfFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

data class  SavedDocument(val name: String, val uri: Uri, val invoice: Invoice?)

@HiltViewModel
class MySavesViewModel @Inject constructor(application: Application, private val mySavesRepository: MySavesRepository, private val invoiceRepository: InvoiceRepository) : AndroidViewModel(application) {
    private val _savedDocuments = MutableStateFlow<List<SavedDocument>>(emptyList())
    val savedDocuments: StateFlow<List<SavedDocument>> = _savedDocuments.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadSavedDocuments()
    }

    fun loadSavedDocuments() {
        viewModelScope.launch {
            val pdfFilesFlow = mySavesRepository.getSavedPdfFilesStream()
            val allInvoicesFlow = invoiceRepository.getAllInvoicesStream()

            pdfFilesFlow.combine(allInvoicesFlow) { pdfFiles, invoices ->
                pdfFiles.map { pdfFile ->
                    val invoice = findInvoiceForPdf(pdfFile, invoices)
                    SavedDocument(pdfFile.name, Uri.parse(pdfFile.absolutePath), invoice)
                }.sortedByDescending { it.invoice?.id ?: 0 }
            }
            .flowOn(Dispatchers.IO)
            .onStart { _isRefreshing.value = true }
            .collectLatest { // collectLatest will cancel previous collections
                _savedDocuments.value = it
                _isRefreshing.value = false
            }
        }
    }

    private fun findInvoiceForPdf(pdfFile: PdfFile, invoices: List<com.example.databaser.data.InvoiceWithCustomerAndLineItems>): Invoice? {
        val invoiceNumber = getInvoiceNumberFromFileName(pdfFile.name)
        return invoices.find { it.invoice.invoiceNumber == invoiceNumber }?.invoice
    }

    private fun getInvoiceNumberFromFileName(fileName: String): String? {
        // This regex looks for a pattern like "ddMMyyyy-N" or "Q-ddMMyyyy-N" which matches
        // the invoice and quote numbering format. This is more robust than splitting by underscores.
        val regex = "(Q-)?\\d{8}-\\d+".toRegex()
        return regex.find(fileName)?.value
    }

    fun updateInvoiceStatus(invoice: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceRepository.updateInvoice(invoice)
        }
    }
}
