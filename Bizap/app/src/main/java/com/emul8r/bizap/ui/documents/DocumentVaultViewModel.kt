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

/**
 * Single document vault item with metadata.
 *
 * **Fields:**
 * - [id]: Database ID for operations
 * - [invoice]: Associated invoice
 * - [fileSize]: Document file size in bytes
 * - [status]: Document processing status
 * - [fileType]: MIME type (pdf, image, etc.)
 * - [absolutePath]: Full file system path
 *
 * @property id Document database ID
 * @property invoice Related invoice data
 * @property fileSize Document size
 * @property status Processing status
 * @property fileType MIME type
 * @property absolutePath File path
 */
data class DocumentVaultItem(
    val id: Long,
    val invoice: Invoice,
    val fileSize: Long,
    val status: DocumentStatus,
    val fileType: String,
    val absolutePath: String
)

/**
 * UI state for document vault screen.
 *
 * Represents document list organized by month/year.
 *
 * @see DocumentVaultViewModel
 */
sealed interface DocumentVaultUiState {
    /**
     * Loading state while fetching documents.
     *
     * UI displays loading spinner or skeleton.
     */
    object Loading : DocumentVaultUiState

    /**
     * Success state with documents grouped by month.
     *
     * Key format: "MMMM yyyy" (e.g., "March 2026")
     *
     * @param documents Documents grouped by month and year
     */
    data class Success(val documents: Map<String, List<DocumentVaultItem>>) : DocumentVaultUiState

    /**
     * Error state when document fetch fails.
     *
     * @param message Error message to display
     */
    data class Error(val message: String): DocumentVaultUiState
}

/**
 * Manages document vault (document storage and retrieval) for invoices.
 *
 * **Purpose:**
 * Provides searchable, filterable access to all stored invoice documents.
 * Documents are organized chronologically and can be searched by invoice details.
 *
 * **Architecture:**
 * - Queries documents from DocumentRepository
 * - Joins with invoice data from InvoiceRepository
 * - Validates file existence
 * - Organizes by month/year for chronological browsing
 * - Supports full-text search on invoice details
 *
 * **Document Organization:**
 * ```
 * Grouped by Month/Year:
 * "March 2026"
 *   ├─ Invoice #2026-001
 *   ├─ Invoice #2026-002
 *   └─ Invoice #2026-003
 * "February 2026"
 *   ├─ Invoice #2026-101
 *   └─ Invoice #2026-102
 * ```
 *
 * **Search Capability:**
 * - Real-time search as user types
 * - Searches invoice number, customer name, amount
 * - Filters documents to matching invoices
 * - Results organized by date
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun DocumentVaultScreen() {
 *     val viewModel: DocumentVaultViewModel = hiltViewModel()
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     val searchTerm by viewModel.searchTerm.collectAsStateWithLifecycle()
 *
 *     Column {
 *         SearchBar(
 *             value = searchTerm,
 *             onValueChange = { viewModel.updateSearchTerm(it) }
 *         )
 *
 *         when (uiState) {
 *             DocumentVaultUiState.Loading -> LoadingScreen()
 *             is DocumentVaultUiState.Success -> {
 *                 val documents = (uiState as DocumentVaultUiState.Success).documents
 *                 DocumentVaultList(documents)
 *             }
 *             is DocumentVaultUiState.Error -> {
 *                 val message = (uiState as DocumentVaultUiState.Error).message
 *                 ErrorScreen(message)
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @param invoiceRepository Access to invoice data
 * @param documentRepository Access to stored documents
 *
 * @see DocumentVaultItem
 * @see DocumentRepository
 * @see InvoiceRepository
 */
@HiltViewModel
class DocumentVaultViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    /**
     * Current search term entered by user.
     *
     * Used to filter documents by invoice details.
     * Updates trigger real-time filtering of document list.
     */
    private val _searchTerm = MutableStateFlow("")
    val searchTerm = _searchTerm.asStateFlow()

    /**
     * Date format for grouping documents by month/year.
     *
     * Format: "MMMM yyyy" (e.g., "March 2026")
     */
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    /**
     * Document vault UI state with search filtering.
     *
     * **Data Flow:**
     * DocumentRepository.getAllDocuments()
     *     ↓
     * Combine with searchTerm
     *     ↓
     * Filter by file existence
     *     ↓
     * Join with invoice data
     *     ↓
     * Apply search filter
     *     ↓
     * Group by month/year
     *     ↓
     * Transform to UiState
     *     ↓
     * StateFlow emits
     */
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

    /**
     * Updates document processing status.
     *
     * **Operation:**
     * - Updates status in DocumentRepository
     * - Launched on viewModelScope (async)
     * - Errors logged but don't crash app
     * - UI automatically updates via uiState
     *
     * **When to call:**
     * - After user marks document as processed
     * - After successful document validation
     * - When document status changes in backend
     *
     * @param id Document ID to update
     * @param newStatus New status value
     */
    fun updateDocumentStatus(id: Long, newStatus: DocumentStatus) {
        viewModelScope.launch {
            documentRepository.updateDocumentStatus(id, newStatus)
                .onFailure { e -> Timber.e(e, "Failed to update document status for id=$id") }
        }
    }
}
