package com.emul8r.bizap.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.local.entities.DocumentStatus
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.repository.DocumentRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import timber.log.Timber

data class DocumentVaultItem(
    val id: Long, // ADDED: Keep the database ID
    val invoice: Invoice,
    val fileSize: Long,
    val status: DocumentStatus,
    val fileType: String,
    val absolutePath: String
)

sealed interface DocumentVaultUiState {
    object Loading : DocumentVaultUiState
    data class Success(val documents: Map<String, List<DocumentVaultItem>>) : DocumentVaultUiState
    data class Error(val message: String): DocumentVaultUiState
}

@HiltViewModel
class DocumentVaultViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _searchTerm = MutableStateFlow("")
    val searchTerm = _searchTerm.asStateFlow()

    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val uiState: StateFlow<DocumentVaultUiState> = 
        documentRepository.getAllDocuments()
            .combine(_searchTerm) { documents, term ->
                val items = documents
                    .filter { File(it.absolutePath).exists() }
                    .mapNotNull { doc ->
                        invoiceRepository.getInvoiceWithItemsById(doc.relatedInvoiceId).first()?.let { invoice ->
                            if (invoice.customerName.contains(term, ignoreCase = true) ||
                                invoice.invoiceId.toString().contains(term)) {
                                DocumentVaultItem(
                                    id = doc.id, // Map the ID here
                                    invoice = invoice,
                                    fileSize = File(doc.absolutePath).length(),
                                    status = doc.status,
                                    fileType = doc.fileType,
                                    absolutePath = doc.absolutePath
                                )
                            } else {
                                null
                            }
                        }
                    }
                
                // --- DEBUGGING DUPLICATES ---
                val paths = items.map { it.absolutePath }
                val duplicatePaths = paths.groupingBy { it }.eachCount().filter { it.value > 1 }
                if (duplicatePaths.isNotEmpty()) {
                    Timber.d("Duplicate paths found: $duplicatePaths")
                }

                val ids = items.map { it.id }
                val duplicateIds = ids.groupingBy { it }.eachCount().filter { it.value > 1 }
                if (duplicateIds.isNotEmpty()) {
                    Timber.d("Duplicate IDs found: $duplicateIds")
                }
                // ----------------------------

                items.groupBy { monthYearFormat.format(Date(it.invoice.date)) }
            }
            .map<Map<String, List<DocumentVaultItem>>, DocumentVaultUiState> { DocumentVaultUiState.Success(it) }
            .catch { emit(DocumentVaultUiState.Error(it.message ?: "An error occurred")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DocumentVaultUiState.Loading
            )

    fun onSearchTermChange(term: String) {
        _searchTerm.value = term
    }

    fun updateDocumentStatus(id: Long, newStatus: DocumentStatus) {
        viewModelScope.launch {
            documentRepository.updateDocumentStatus(id, newStatus)
        }
    }
}
