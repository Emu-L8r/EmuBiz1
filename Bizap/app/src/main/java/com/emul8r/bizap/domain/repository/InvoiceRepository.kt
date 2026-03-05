package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    /**
     * Saves a new invoice or updates an existing one.
     *
     * @return [Result.success] containing the row ID on success, or [Result.failure] wrapping
     * the database exception (e.g. SQLiteConstraintException for foreign-key violations).
     */
    suspend fun saveInvoice(invoice: Invoice): Result<Long>

    fun getInvoiceWithItemsById(id: Long): Flow<Invoice?>
    fun getAllInvoicesWithItems(): Flow<List<Invoice>>

    // --- PHASE 3A: Management & Versioning ---
    fun getInvoiceGroupWithVersions(year: Int, sequence: Int): Flow<List<Invoice>>

    /**
     * Updates the amount paid for an invoice.
     *
     * @return [Result.success] on success, or [Result.failure] if the database operation fails.
     */
    suspend fun updateAmountPaid(invoiceId: Long, amount: Long): Result<Unit>

    /**
     * Creates a correction (new version) of an existing invoice.
     *
     * @return [Result.success] containing the new invoice ID, or [Result.failure] if the original
     * invoice is not found or the database operation fails.
     */
    suspend fun createCorrection(originalInvoiceId: Long): Result<Long>

    fun getBusinessProfile(): Flow<BusinessProfile>

    /**
     * Updates the status of an invoice.
     *
     * @return [Result.success] on success, or [Result.failure] if the database operation fails.
     */
    suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit>

    /**
     * Updates the stored PDF path for an invoice.
     *
     * @return [Result.success] on success, or [Result.failure] if the database operation fails.
     */
    suspend fun updatePdfPath(invoiceId: Long, pdfPath: String): Result<Unit>

    /**
     * Deletes an invoice and all its associated line items.
     *
     * @return [Result.success] on success, or [Result.failure] if the database operation fails.
     */
    suspend fun deleteInvoice(id: Long): Result<Unit>
}
