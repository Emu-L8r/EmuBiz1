package com.example.databaser.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.databaser.MyApplication
import com.example.databaser.data.Invoice
import com.example.databaser.data.InvoiceRepository
import com.example.databaser.data.MySavesRepository
import com.example.databaser.ui.PdfFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class  SavedDocument(val name: String, val uri: Uri, val invoice: Invoice?)

class MySavesViewModel(application: Application, private val mySavesRepository: MySavesRepository, private val invoiceRepository: InvoiceRepository) : AndroidViewModel(application) {
    private val _savedDocuments = MutableStateFlow<List<SavedDocument>>(emptyList())
    val savedDocuments: StateFlow<List<SavedDocument>> = _savedDocuments.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadSavedDocuments()
    }

    fun loadSavedDocuments() {
        viewModelScope.launch {
            val pdfFilesFlow = mySavesRepository.getSavedPdfFilesStream(getApplication())
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
        val parts = fileName.split("_")
        return if (parts.size > 2) parts[2].substringBefore(".") else parts.getOrNull(1)?.substringBefore(".")
    }

    fun updateInvoiceStatus(invoice: Invoice) {
        viewModelScope.launch(Dispatchers.IO) {
            invoiceRepository.updateInvoice(invoice)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MyApplication)
                val mySavesRepository = application.container.mySavesRepository
                val invoiceRepository = application.container.invoiceRepository
                MySavesViewModel(application, mySavesRepository, invoiceRepository)
            }
        }
    }
}
