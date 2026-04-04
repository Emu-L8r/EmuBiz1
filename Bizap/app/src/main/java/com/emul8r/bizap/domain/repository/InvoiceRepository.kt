package com.emul8r.bizap.domain.repository

import androidx.paging.PagingData
import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow

/**
 * Dashboard metrics for the quick stats widget.
 * Provides a snapshot of critical business metrics.
 */
data class DashboardMetrics(
    val unpaidInvoiceCount: Int,
    val unpaidAmount: Long,
    val overdueAmount: Long,
    val paidThisMonth: Long,
    val totalCustomersOwed: Long,
    val lastUpdatedMs: Long = System.currentTimeMillis()
)

interface InvoiceRepository {
    /**
     * Saves a new invoice or updates an existing one locally.
     *
     * @return [Result.success] containing the row ID on success, or [Result.failure].
     */
    suspend fun saveInvoice(invoice: Invoice): Result<Long>

    fun getInvoiceWithItemsById(id: Long): Flow<Invoice?>
    fun getAllInvoicesWithItems(): Flow<List<Invoice>>

    /**
     * Returns a paged stream of invoices for the given business, ordered by date descending.
     * Use [androidx.paging.cachedIn] in the ViewModel to survive config changes.
     */
    fun getInvoicesPaged(businessId: Long): Flow<PagingData<Invoice>>

    // --- PHASE 3A: Management & Versioning ---
    fun getInvoiceGroupWithVersions(year: Int, sequence: Int): Flow<List<Invoice>>

    /**
     * Updates the amount paid for an invoice locally.
     *
     * @return [Result.success] on success, or [Result.failure].
     */
    suspend fun updateAmountPaid(invoiceId: Long, amount: Long): Result<Unit>

    /**
     * Creates a correction (new version) of an existing invoice locally.
     */
    suspend fun createCorrection(originalInvoiceId: Long): Result<Long>

    fun getBusinessProfile(): Flow<BusinessProfile>

    /**
     * Updates the status of an invoice locally.
     */
    suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit>

    /**
     * Updates the stored PDF path for an invoice locally.
     */
    suspend fun updatePdfPath(invoiceId: Long, pdfPath: String): Result<Unit>

    /**
     * Deletes an invoice and all its associated line items locally.
     */
    suspend fun deleteInvoice(id: Long): Result<Unit>

    // --- QUICK WINS: Dashboard Metrics (NEW) ---

    /**
     * Gets dashboard metrics for the quick stats widget.
     *
     * Provides key business metrics at a glance:
     * - Count of unpaid invoices
     * - Total amount unpaid
     * - Overdue amount (past due date)
     * - Amount collected this month
     * - Total amount customers owe
     *
     * Used by GUI2 dashboard for status widget display.
     *
     * @param businessId The business profile ID
     * @return [Result.success] with DashboardMetrics, or [Result.failure] on error
     */
    suspend fun getDashboardMetrics(businessId: Long): Result<DashboardMetrics>

    // --- PHASE 2: Remote Sync ---

    suspend fun createInvoiceRemote(invoice: Invoice): Result<Invoice>
    suspend fun updateInvoiceRemote(invoice: Invoice): Result<Invoice>
    suspend fun deleteInvoiceRemote(id: Long): Result<Unit>
    suspend fun getInvoiceRemote(id: Long): Result<Invoice>
    suspend fun recordPaymentRemote(
        invoiceId: Long,
        amount: Long,
        paymentDate: Long,
        notes: String?
    ): Result<Unit>

    /**
     * Observes payment history snapshots for an invoice.
     *
     * Returns a reactive stream of payment snapshots ordered by date (newest first).
     * Snapshots capture the payment state at each update.
     *
     * **Data Consistency Guarantees:**
     * - Filters by invoiceId to ensure invoice-specific data only
     * - Filters by businessId to prevent cross-tenant data leaks (multi-tenant safety)
     * - Both parameters are required and must be > 0
     * - Returns empty Flow if invoice doesn't exist or doesn't belong to business
     *
     * **Behavior:**
     * - Validates parameters before executing query
     * - Returns empty list if no payments recorded yet
     * - Automatically updates when new payments are recorded
     * - Orders results by lastUpdatedMs DESC (newest first)
     *
     * @param invoiceId The invoice to observe payments for (must be > 0)
     * @param businessId The business profile ID for multi-tenant filtering (must be > 0)
     * @return Flow emitting payment history snapshots filtered by both invoiceId and businessId
     * @throws IllegalArgumentException if invoiceId or businessId <= 0
     */
    fun observePaymentHistory(invoiceId: Long, businessId: Long): Flow<List<com.emul8r.bizap.data.local.entities.InvoicePaymentSnapshot>>

    /**
     * Deletes all invoices, line items, and payment records.
     * Customer records are preserved.
     */
    suspend fun deleteAllInvoices(): Result<Unit>
}


