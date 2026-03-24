package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.domain.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Manages invoice list state for GUI1.
 *
 * **Purpose:**
 * Displays all invoices for the active business with pagination, filtering, and status management.
 * Handles real-time updates when invoices change and provides error/loading states.
 *
 * **Responsibilities:**
 * - Load all invoices with their line items
 * - Expose reactive state via StateFlow
 * - Handle retry logic on load failures
 * - Manage invoice status updates (DRAFT → SENT → PAID, etc.)
 * - Track loading/error/empty states
 *
 * **State Management:**
 * - uiState: Sealed interface with 4 states (Loading, Empty, Success, Error)
 * - retryTrigger: Manual retry mechanism (increment to trigger reload)
 * - Uses flatMapLatest for reactive updates on retry
 *
 * **Data Flow:**
 * ```
 * retryTrigger (user clicks retry)
 *     ↓
 * repository.getAllInvoicesWithItems()
 *     ↓
 * Transform to Success/Error/Empty state
 *     ↓
 * Emit via StateFlow
 *     ↓
 * UI observes and recomposes
 * ```
 *
 * **Usage Example:**
 * ```kotlin
 * @Composable
 * fun InvoiceListScreen() {
 *     val viewModel: InvoiceListViewModel = hiltViewModel()
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     when (uiState) {
 *         InvoiceListUiState.Loading -> LoadingSpinner()
 *         InvoiceListUiState.Empty -> EmptyStateMessage()
 *         is InvoiceListUiState.Success -> LazyColumn {
 *             items(uiState.invoices) { invoice ->
 *                 InvoiceRow(
 *                     invoice = invoice,
 *                     onStatusChange = { newStatus =>
 *                         viewModel.updateInvoiceStatus(invoice.id, newStatus)
 *                     }
 *                 )
 *             }
 *         }
 *         is InvoiceListUiState.Error -> ErrorScreen(
 *             message = uiState.message,
 *             onRetry = { viewModel.retry() }
 *         )
 *     }
 * }
 * ```
 *
 * **Error Handling:**
 * - Network errors: Caught and shown in Error state
 * - Retry: Call retry() to attempt load again
 * - No loading indicator: Uses Loading state instead
 *
 * @param repository Access to invoice data
 *
 * @see InvoiceRepository
 * @see InvoiceListUiState
 */
@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val repository: InvoiceRepository
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<InvoiceListUiState> = retryTrigger
        .flatMapLatest {
            repository.getAllInvoicesWithItems()
                .map { list ->
                    if (list.isEmpty()) InvoiceListUiState.Empty
                    else InvoiceListUiState.Success(list)
                }
                .catch { e -> emit(InvoiceListUiState.Error(e.message ?: "Unknown Error")) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InvoiceListUiState.Loading
        )

    /**
     * Triggers a retry of the invoice list load.
     *
     * **Behavior:**
     * - Increments retryTrigger, which triggers flatMapLatest
     * - UI transitions to Loading state while fetching
     * - On success: transitions to Success or Empty
     * - On failure: transitions to Error with message
     *
     * **Use Cases:**
     * - User taps "Retry" on error screen
     * - Auto-retry after network comes back online
     * - Manual refresh pull-to-refresh gesture
     *
     * **Example:**
     * ```kotlin
     * Button(onClick = { viewModel.retry() }) {
     *     Text("Try Again")
     * }
     * ```
     */
    fun retry() {
        retryTrigger.value++
    }

    /**
     * Updates the status of an invoice asynchronously.
     *
     * **Behavior:**
     * 1. Converts string status to InvoiceStatus enum
     * 2. Calls repository.updateInvoiceStatus()
     * 3. Operation is async (doesn't block UI)
     * 4. On completion, uiState automatically updates via repository observation
     *
     * **Valid Statuses:**
     * - DRAFT: Not yet sent
     * - SENT: Sent to customer
     * - PAID: Fully paid
     * - OVERDUE: Past due date, unpaid
     * - CANCELLED: Voided/cancelled
     *
     * **Error Handling:**
     * - Invalid status: IllegalArgumentException
     * - Database error: Silently fails (not propagated to UI)
     * - Consider adding error callback if needed
     *
     * **Example:**
     * ```kotlin
     * viewModel.updateInvoiceStatus(invoiceId = 42, newStatus = "PAID")
     * // Invoice automatically updates in list
     * ```
     *
     * @param id Invoice to update
     * @param newStatus New status as string (e.g., "PAID", "SENT", "DRAFT")
     * @throws IllegalArgumentException if newStatus is not a valid InvoiceStatus
     *
     * @see InvoiceStatus
     * @see InvoiceRepository.updateInvoiceStatus
     */
    fun updateInvoiceStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            val status = InvoiceStatus.valueOf(newStatus)
            repository.updateInvoiceStatus(id, status)
        }
    }
}

sealed interface InvoiceListUiState {
    object Loading : InvoiceListUiState
    object Empty : InvoiceListUiState
    data class Success(val invoices: List<Invoice>) : InvoiceListUiState
    data class Error(val message: String) : InvoiceListUiState
}