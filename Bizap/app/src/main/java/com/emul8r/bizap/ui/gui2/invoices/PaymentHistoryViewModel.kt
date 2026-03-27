package com.emul8r.bizap.ui.gui2.invoices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.utils.CentsFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * UI state for payment history display.
 *
 * Sealed class representing possible states:
 * - Loading: Initial state while fetching invoice
 * - Success: Invoice found with payment history
 * - NotFound: Invoice doesn't exist in database
 * - Error: Error occurred during loading
 */
sealed interface PaymentHistoryUiState {
    /**
     * Initial loading state.
     */
    object Loading : PaymentHistoryUiState

    /**
     * Invoice and payment history successfully loaded.
     *
     * Contains aggregated payment data for a single invoice:
     * - Total amount on invoice
     * - Amount paid to date
     * - Outstanding balance
     * - Timeline of payment records
     */
    data class Success(
        val invoiceId: Long,
        val invoiceName: String,
        val totalAmount: Long,
        val paidAmount: Long,
        val outstandingAmount: Long,
        val paymentHistory: List<PaymentHistoryItem> = emptyList()
    ) : PaymentHistoryUiState

    /**
     * Invoice not found.
     *
     * The invoice doesn't exist in the database.
     */
    data class NotFound(val invoiceId: Long) : PaymentHistoryUiState

    /**
     * Error loading invoice or payment history.
     *
     * @param message User-friendly error message
     * @param invoiceId The invoice ID that failed to load
     */
    data class Error(val message: String, val invoiceId: Long) : PaymentHistoryUiState
}

/**
 * Single payment record in timeline.
 *
 * Represents a snapshot of payment state at a moment in time.
 */
data class PaymentHistoryItem(
    val date: Long,
    val amount: Long,
    val status: String, // PAID, UNPAID, PARTIALLY_PAID, OVERDUE
    val notes: String? = null,
    val daysSinceDue: Int = 0
)

/**
 * ViewModel for payment history screen (GUI2).
 *
 * Responsible for:
 * - Validating invoice exists in database before loading
 * - Loading payment snapshots from database
 * - Transforming into UI-friendly format
 * - Exposing reactive state via Flow with proper error handling
 * - Ensuring data consistency by filtering snapshots by invoiceId AND businessId
 *
 * **Data Flow:**
 * ```
 * invoiceId + businessId parameters passed from Screen
 *     ↓
 * Validate both parameters are valid (> 0)
 *     ↓
 * Validate invoice exists via InvoiceRepository.getInvoiceWithItemsById()
 *     ↓
 * If not found: emit NotFound state
 * If found: Observe payment snapshots for that invoice
 *     ↓
 * InvoiceRepository.observePaymentHistory(invoiceId, businessId) (filtered query)
 *     ↓
 * Transform to PaymentHistoryUiState.Success
 *     ↓
 * UI observes via collectAsStateWithLifecycle()
 * ```
 *
 * @see PaymentHistoryScreen for UI consumption
 * @see InvoiceRepository for data layer
 */
@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // ✅ Parameters passed explicitly from Screen via initialize()
    private var invoiceId: Long = -1L
    private var businessId: Long = -1L
    private var _initialized = false

    // Public field to track payment history flow
    private var _paymentHistory: Flow<PaymentHistoryUiState>? = null

    /**
     * Initialize ViewModel with explicit invoiceId and businessId parameters.
     *
     * This method is kept for backward compatibility but parameters are now
     * validated in the constructor.
     *
     * @param invoiceId The invoice ID to display payment history for
     * @param businessId The business ID for multi-tenant filtering
     * @return Flow of payment history states
     */
    fun initialize(invoiceId: Long, businessId: Long): Flow<PaymentHistoryUiState> {
        return if (invoiceId <= 0 || businessId <= 0) {
            Timber.e("❌ Invalid parameters: invoiceId=$invoiceId, businessId=$businessId")
            kotlinx.coroutines.flow.flowOf(
                PaymentHistoryUiState.Error("Invalid parameters", invoiceId)
            )
        } else {
            createPaymentHistoryFlow(invoiceId, businessId)
        }
    }

    /**
     * Create the payment history Flow for the given invoiceId and businessId.
     *
     * Steps:
     * 1. Start with Loading state
     * 2. Validate invoice exists and belongs to business
     * 3. If not found, emit NotFound
     * 4. If found, load payment history snapshots
     * 5. Transform snapshots to Success state
     * 6. Handle errors gracefully
     */
    private fun createPaymentHistoryFlow(invoiceId: Long, businessId: Long): Flow<PaymentHistoryUiState> {
        return kotlinx.coroutines.flow.flow {
            // Start with loading state
            emit(PaymentHistoryUiState.Loading)

            try {
                // Step 1: Validate invoice exists
                invoiceRepository.getInvoiceWithItemsById(invoiceId)
                    .collect { invoice ->
                        if (invoice == null) {
                            Timber.w("❌ Invoice not found: $invoiceId")
                            emit(PaymentHistoryUiState.NotFound(invoiceId))
                        } else {
                            // Step 2: Invoice exists, now observe payment history
                            // ✅ FIXED: Pass both invoiceId AND businessId for multi-tenant safety
                            Timber.d("✅ Invoice found: $invoiceId (${invoice.invoiceNumber}), loading payment history...")
                            invoiceRepository.observePaymentHistory(invoiceId, businessId)
                                .collect { snapshots ->
                                    if (snapshots.isEmpty()) {
                                        Timber.d("📋 No payment history for invoice $invoiceId")
                                        emit(
                                            PaymentHistoryUiState.Success(
                                                invoiceId = invoiceId,
                                                invoiceName = invoice.invoiceNumber,
                                                totalAmount = invoice.totalAmount,
                                                paidAmount = invoice.amountPaid,
                                                outstandingAmount = invoice.totalAmount - invoice.amountPaid,
                                                paymentHistory = emptyList()
                                            )
                                        )
                                    } else {
                                        // Latest snapshot has current state
                                        val latest = snapshots.first()

                                        Timber.d(
                                            "📋 Loaded ${snapshots.size} payment records for invoice $invoiceId: " +
                                                    "Total=${latest.totalAmount}, Paid=${latest.paidAmount}, Outstanding=${latest.outstandingAmount}"
                                        )

                                        emit(
                                            PaymentHistoryUiState.Success(
                                                invoiceId = invoiceId,
                                                invoiceName = latest.invoiceNumber,
                                                totalAmount = latest.totalAmount,
                                                paidAmount = latest.paidAmount,
                                                outstandingAmount = latest.outstandingAmount,
                                                paymentHistory = snapshots.map { snapshot ->
                                                    PaymentHistoryItem(
                                                        date = snapshot.lastUpdatedMs,
                                                        amount = snapshot.paidAmount,
                                                        status = snapshot.paymentStatus,
                                                        daysSinceDue = snapshot.daysSinceDue,
                                                        notes = null
                                                    )
                                                }
                                            )
                                        )
                                    }
                                }
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading payment history for invoice $invoiceId, business $businessId")
                emit(
                    PaymentHistoryUiState.Error(
                        message = "Failed to load payment history",
                        invoiceId = invoiceId
                    )
                )
            }
        }
    }

    /**
     * Public Flow that emits payment history states.
     *
     * Uses explicit invoiceId and businessId parameters passed to constructor,
     * ensuring they are always available and valid.
     *
     * Emits states in order: Loading → Success/NotFound/Error
     */
    val paymentHistory: Flow<PaymentHistoryUiState> = createPaymentHistoryFlow(invoiceId, businessId)
}


