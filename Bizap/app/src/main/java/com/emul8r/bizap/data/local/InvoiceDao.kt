package com.emul8r.bizap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.local.entities.LineItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data class for calculated payment metrics.
 * Represents metrics calculated directly from invoices table (single source of truth).
 */
data class CalculatedMetrics(
    val totalInvoices: Int,
    val paidInvoices: Int,
    val unpaidInvoices: Int,
    val totalAmount: Long,
    val paidAmount: Long,
    val totalOutstanding: Long,
    val collectionRate: Double
)

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices WHERE businessProfileId = :businessId ORDER BY date DESC")
    fun getInvoicesByBusinessId(businessId: Long): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE customerId = :customerId AND businessProfileId = :businessId")
    fun getInvoicesForCustomer(customerId: Long, businessId: Long): Flow<List<InvoiceWithItems>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Upsert
    suspend fun insertLineItems(items: List<LineItemEntity>)

    @Transaction
    suspend fun insert(invoice: InvoiceEntity, items: List<LineItemEntity>): Long {
        val id = insertInvoice(invoice)
        if (invoice.id != 0L) {
            deleteLineItems(invoice.id)
        }
        val itemsWithId = items.map { it.copy(invoiceId = id) }
        insertLineItems(itemsWithId)
        return id
    }

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Query("UPDATE invoices SET amountPaid = :amountPaid WHERE id = :id")
    suspend fun updateAmountPaid(id: Long, amountPaid: Long)

    suspend fun updateStatus(id: Long, status: com.emul8r.bizap.domain.model.InvoiceStatus) {
        updateInvoiceStatus(id, status.name)
    }

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :id")
    fun getInvoiceWithItemsById(id: Long): Flow<InvoiceWithItems?>

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateInvoiceStatus(id: Long, status: String)

    @Query("UPDATE invoices SET pdfUri = :path WHERE id = :id")
    suspend fun updatePdfPath(id: Long, path: String)

    @Query("UPDATE invoices SET currencyCode = :code WHERE id = :id")
    suspend fun updateInvoiceCurrency(id: Long, code: String)

    @Query("DELETE FROM line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteLineItems(invoiceId: Long)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    @Transaction
    suspend fun deleteInvoiceWithItems(id: Long) {
        deleteLineItems(id)
        deleteInvoice(id)
    }

    @Query("SELECT COALESCE(MAX(invoiceSequence), 0) FROM invoices WHERE invoiceYear = :year AND businessProfileId = :businessId")
    suspend fun getMaxSequenceForYear(year: Int, businessId: Long): Int

    @Query("""
        SELECT * FROM invoices 
        WHERE invoiceYear = :invoiceYear AND invoiceSequence = :invoiceSequence AND businessProfileId = :businessId
        ORDER BY version ASC
    """)
    fun getInvoiceGroupWithVersions(invoiceYear: Int, invoiceSequence: Int, businessId: Long): Flow<List<InvoiceEntity>>

    // ==================== DIRECT REVENUE QUERIES ====================

    /**
     * Revenue trend data class for direct-from-invoices queries.
     * Represents one row per (date, currency) combination from the invoices table.
     */
    data class DailyRevenueTrend(
        val dateString: String,
        val revenue: Long,
        val invoiceCount: Int,
        val paidCount: Int,
        val currencyCode: String
    )

    @Query("""
        SELECT 
            COALESCE(SUM(totalAmount), 0) as mtdRevenue
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
        AND DATE(date/1000, 'unixepoch') >= date('now', 'start of month')
    """)
    fun observeMTDRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT 
            COALESCE(SUM(totalAmount), 0) as ytdRevenue
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
        AND strftime('%Y', date/1000, 'unixepoch') = strftime('%Y', 'now')
    """)
    fun observeYTDRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT 
            COALESCE(SUM(totalAmount), 0) as weeklyRevenue
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
        AND DATE(date/1000, 'unixepoch') >= date('now', '-7 days')
    """)
    fun observeWeeklyRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT 
            COALESCE(SUM(totalAmount), 0) as totalPaidRevenue
        FROM invoices
        WHERE businessProfileId = :businessId
        AND status = 'PAID'
    """)
    fun observeTotalPaidRevenue(businessId: Long): Flow<Long>

    @Query("""
        SELECT 
            DATE(date/1000, 'unixepoch') as dateString,
            COALESCE(SUM(CASE WHEN status = 'PAID' THEN totalAmount ELSE 0 END), 0) as revenue,
            COUNT(*) as invoiceCount,
            COUNT(CASE WHEN status = 'PAID' THEN 1 END) as paidCount,
            currencyCode
        FROM invoices
        WHERE businessProfileId = :businessId
        AND DATE(date/1000, 'unixepoch') >= date('now', '-30 days')
        GROUP BY dateString, currencyCode
        ORDER BY dateString DESC
    """)
    fun observeLast30DaysRevenueTrend(businessId: Long): Flow<List<DailyRevenueTrend>>

    // ==================== HEALTH CHECK QUERIES ====================

    /**
     * Count total number of invoices.
     * Used for snapshot consistency checks.
     */
    @Query("SELECT COUNT(*) FROM invoices")
    suspend fun count(): Int

    /**
     * Count distinct customers across all invoices.
     * Used to verify customer analytics snapshot coverage.
     */
    @Query("SELECT COUNT(DISTINCT customerId) FROM invoices")
    suspend fun countDistinctCustomers(): Int

    // ==================== VALIDATION TEST QUERY ====================

    /**
     * Calculate payment metrics directly from invoices table.
     * Used to compare against snapshot-based calculations.
     * This helps validate whether snapshots are stale or accurate.
     */
    @Query("""
        SELECT 
            COUNT(*) as totalInvoices,
            SUM(CASE WHEN status = 'PAID' THEN 1 ELSE 0 END) as paidInvoices,
            SUM(CASE WHEN status != 'PAID' THEN 1 ELSE 0 END) as unpaidInvoices,
            SUM(totalAmount) as totalAmount,
            SUM(amountPaid) as paidAmount,
            SUM(totalAmount - amountPaid) as totalOutstanding,
            CASE 
                WHEN SUM(totalAmount) > 0 THEN ROUND((SUM(amountPaid) / CAST(SUM(totalAmount) AS REAL)) * 100.0, 1)
                ELSE 0.0
            END as collectionRate
        FROM invoices
        WHERE businessProfileId = :businessId
    """)
    suspend fun calculatePaymentMetrics(businessId: Long): CalculatedMetrics?
}
