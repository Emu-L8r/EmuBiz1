package com.emul8r.bizap.domain.repository

import com.emul8r.bizap.domain.model.BusinessProfile
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    /**
     * Saves a new invoice or updates an existing one locally.
     *
     * @return [Result.success] containing the row ID on success, or [Result.failure].
     */
    suspend fun saveInvoice(invoice: Invoice): Result<Long>

    fun getInvoiceWithItemsById(id: Long): Flow<Invoice?>
    fun getAllInvoicesWithItems(): Flow<List<Invoice>>

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
}
