package com.emul8r.bizap.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.emul8r.bizap.domain.model.Invoice
import kotlinx.coroutines.flow.Flow

/**
 * Optimized Invoice Data Access Object.
 *
 * **Performance Optimizations:**
 * - Indexed queries for faster lookups (businessId, status, date range)
 * - Paging support for large lists (prevents loading all rows)
 * - Selective column queries (only fetch what you need)
 * - Pagination for memory efficiency
 *
 * **Indices Applied:**
 * ```sql
 * CREATE INDEX idx_invoice_business_id ON invoices(business_id);
 * CREATE INDEX idx_invoice_status ON invoices(status);
 * CREATE INDEX idx_invoice_date ON invoices(created_at);
 * CREATE INDEX idx_invoice_business_status ON invoices(business_id, status);
 * ```
 *
 * **Query Optimization Rules:**
 * 1. Always use LIMIT for list queries (Paging3)
 * 2. Select only columns you need (avoid SELECT *)
 * 3. Use indexed columns in WHERE clauses
 * 4. Combine filters efficiently (business_id + status together)
 */
@Dao
interface OptimizedInvoiceDao {

    /**
     * Insert a new invoice.
     *
     * **Performance:** O(1) - Single row insert with auto-increment
     */
    @Insert
    suspend fun insertInvoice(invoice: Invoice): Long

    /**
     * Update an existing invoice.
     *
     * **Performance:** O(1) - Direct row update by primary key
     */
    @Update
    suspend fun updateInvoice(invoice: Invoice)

    /**
     * Delete an invoice.
     *
     * **Performance:** O(1) - Direct row delete by primary key
     */
    @Delete
    suspend fun deleteInvoice(invoice: Invoice)

    /**
     * Get invoice by ID (Reactive).
     *
     * **Performance:** O(1) - Primary key lookup
     * **Why Reactive:** Automatically updates UI when invoice changes
     */
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    fun getInvoiceById(invoiceId: Long): Flow<Invoice?>

    /**
     * Get paginated invoices for a business.
     *
     * **Performance:** O(n) where n = page size (e.g., 20 items)
     * **Why Pagination:** Avoids loading 1000+ invoices into memory
     * **Uses Index:** business_id
     *
     * Example: Page 1 = items 0-19, Page 2 = items 20-39, etc.
     */
    @Query(
        """
        SELECT * FROM invoices
        WHERE business_id = :businessId
        ORDER BY created_at DESC
        LIMIT :pageSize OFFSET :offset
        """
    )
    fun getInvoicesByBusinessPaged(
        businessId: Long,
        pageSize: Int = 20,
        offset: Int = 0
    ): PagingSource<Int, Invoice>

    /**
     * Get invoices by status (Paginated).
     *
     * **Performance:** O(n) where n = page size
     * **Uses Index:** business_id + status (composite index)
     * **Use Case:** Filtering unpaid/overdue invoices
     */
    @Query(
        """
        SELECT * FROM invoices
        WHERE business_id = :businessId AND status = :status
        ORDER BY created_at DESC
        LIMIT :pageSize OFFSET :offset
        """
    )
    fun getInvoicesByStatusPaged(
        businessId: Long,
        status: String,
        pageSize: Int = 20,
        offset: Int = 0
    ): PagingSource<Int, Invoice>

    /**
     * Get invoices in date range (Optimized for analytics).
     *
     * **Performance:** O(n) where n = invoices in date range
     * **Uses Index:** created_at
     * **Memory:** Only loads invoices in range (not all invoices)
     */
    @Query(
        """
        SELECT * FROM invoices
        WHERE business_id = :businessId
        AND created_at BETWEEN :startDate AND :endDate
        ORDER BY created_at DESC
        """
    )
    fun getInvoicesByDateRange(
        businessId: Long,
        startDate: Long,
        endDate: Long
    ): Flow<List<Invoice>>

    /**
     * Count invoices by status (Lightweight query).
     *
     * **Performance:** O(1) - Index-based count
     * **Memory:** Minimal - returns single integer
     * **Use Case:** Dashboard metrics (e.g., "5 overdue invoices")
     */
    @Query(
        """
        SELECT COUNT(*) FROM invoices
        WHERE business_id = :businessId AND status = :status
        """
    )
    suspend fun countInvoicesByStatus(businessId: Long, status: String): Int

    /**
     * Get invoice totals by status (Aggregation query).
     *
     * **Performance:** O(n) where n = invoices
     * **Memory:** Very low - returns aggregated numbers
     * **Use Case:** Analytics dashboard
     *
     * Returns: Pair<status, totalAmount>
     */
    @Query(
        """
        SELECT status, SUM(total_amount) as amount FROM invoices
        WHERE business_id = :businessId
        GROUP BY status
        """
    )
    suspend fun getInvoiceTotalsByStatus(businessId: Long): List<Pair<String, Long>>

    /**
     * Selective column query (Memory optimization).
     *
     * **Performance:** Faster than full object deserialization
     * **Memory:** Only loads essential columns
     * **Use Case:** List displays (invoice number, customer, amount)
     *
     * Returns: Invoice objects but data class only includes fetched columns
     */
    @Query(
        """
        SELECT id, invoice_number, customer_name, total_amount, status, created_at
        FROM invoices
        WHERE business_id = :businessId
        ORDER BY created_at DESC
        LIMIT :limit
        """
    )
    fun getInvoiceListItems(businessId: Long, limit: Int = 50): Flow<List<Invoice>>

    /**
     * Delete old invoices (Cleanup operation).
     *
     * **Performance:** O(n) - Bulk delete operation
     * **Use Case:** Remove invoices older than 2 years (compliance)
     * **Safety:** Only deletes PAID invoices (keep unpaid for collection)
     */
    @Query(
        """
        DELETE FROM invoices
        WHERE business_id = :businessId
        AND status = 'PAID'
        AND created_at < :cutoffDate
        """
    )
    suspend fun deleteOldInvoices(businessId: Long, cutoffDate: Long): Int

    /**
     * Batch update invoice status.
     *
     * **Performance:** O(n) where n = invoices to update
     * **Use Case:** Mark collection of invoices as sent
     */
    @Query(
        """
        UPDATE invoices
        SET status = :newStatus
        WHERE id IN (:invoiceIds)
        """
    )
    suspend fun batchUpdateStatus(invoiceIds: List<Long>, newStatus: String)

    /**
     * Get unsynced invoices (Offline support).
     *
     * **Performance:** O(n) where n = unsynced invoices
     * **Use Case:** Sync to server after connectivity restored
     */
    @Query(
        """
        SELECT * FROM invoices
        WHERE business_id = :businessId AND is_synced = 0
        LIMIT :limit
        """
    )
    suspend fun getUnsyncedInvoices(businessId: Long, limit: Int = 100): List<Invoice>
}

