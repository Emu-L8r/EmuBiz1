package com.emul8r.bizap.ui.gui2.invoices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.emul8r.bizap.data.local.dao.InvoicePaymentDao
import com.emul8r.bizap.utils.CentsFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * UI state for payment history display.
 *
 * Contains aggregated payment data for a single invoice:
 * - Total amount on invoice
 * - Amount paid to date
 * - Outstanding balance
 * - Timeline of payment records
 */
data class PaymentHistoryUiState(
    val invoiceId: Long,
    val invoiceName: String,
    val totalAmount: Long,
    val paidAmount: Long,
    val outstandingAmount: Long,
    val paymentHistory: List<PaymentHistoryItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

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
 * - Loading payment snapshots from database
 * - Transforming into UI-friendly format
 * - Exposing reactive state via Flow
 *
 * **Data Flow:**
 * ```
 * Database (invoice_payment_snapshots)
 *     ↓
 * InvoicePaymentDao.observePaymentHistory()
 *     ↓
 * Transform to PaymentHistoryUiState
 *     ↓
 * UI observes via collectAsStateWithLifecycle()
 * ```
 *
 * @see PaymentHistoryScreen for UI consumption
 * @see InvoicePaymentDao for data layer
 */
@HiltViewModel
class PaymentHistoryViewModel @Inject constructor(
    private val invoicePaymentDao: InvoicePaymentDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val invoiceId: Long = checkNotNull(savedStateHandle["invoiceId"])
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    /**
     * Reactive stream of payment history state.
     *
     * Emits whenever payment data changes in the database.
     *
     * **Behavior:**
     * - Loads snapshots ordered by date (newest first)
     * - Transforms to UI state
     * - Emits null state if no data found
     *
     * **Example:**
     * ```kotlin
     * val state by viewModel.paymentHistory.collectAsStateWithLifecycle(
     *     initialValue = PaymentHistoryUiState(...)
     * )
     * ```
     */
    val paymentHistory: Flow<PaymentHistoryUiState> =
        invoicePaymentDao.observePaymentHistory(invoiceId)
            .map { snapshots ->
                if (snapshots.isEmpty()) {
                    Timber.d("📋 No payment history for invoice $invoiceId")
                    PaymentHistoryUiState(
                        invoiceId = invoiceId,
                        invoiceName = "",
                        totalAmount = 0,
                        paidAmount = 0,
                        outstandingAmount = 0,
                        isLoading = false
                    )
                } else {
                    // Latest snapshot has current state
                    val latest = snapshots.first()

                    Timber.d(
                        "📋 Loaded ${snapshots.size} payment records for invoice $invoiceId: " +
                        "Total=${latest.totalAmount}, Paid=${latest.paidAmount}, Outstanding=${latest.outstandingAmount}"
                    )

                    PaymentHistoryUiState(
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
                        },
                        isLoading = false,
                        error = null
                    )
                }
            }
}


