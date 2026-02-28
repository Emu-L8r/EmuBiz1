package com.emul8r.bizap.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.emul8r.bizap.data.local.entities.InvoiceEntity
import com.emul8r.bizap.data.local.entities.InvoiceWithItems
import com.emul8r.bizap.data.local.entities.LineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Transaction
    @Query("SELECT * FROM invoices ORDER BY date DESC")
    fun getAllInvoices(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("""
        SELECT * FROM invoices 
        WHERE (customerName LIKE '%' || :query || '%' OR invoiceId LIKE :query)
        AND (:status IS NULL OR status = :status)
        ORDER BY date DESC
    """)
    fun searchInvoices(query: String, status: String?): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE customerId = :customerId")
    fun getInvoicesForCustomer(customerId: Long): Flow<List<InvoiceWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Upsert
    suspend fun insertLineItems(items: List<LineItemEntity>)

    @Transaction
    suspend fun insert(invoice: InvoiceEntity, items: List<LineItemEntity>): Long {
        val id = insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(parentInvoiceId = id) }
        insertLineItems(itemsWithId)
        return id
    }

    @Transaction
    @Query("SELECT * FROM invoices WHERE invoiceId = :id")
    fun getInvoiceWithItemsById(id: Long): Flow<InvoiceWithItems?>

    @Query("UPDATE invoices SET status = :status WHERE invoiceId = :invoiceId")
    suspend fun updateInvoiceStatus(invoiceId: Long, status: String)

    @Query("UPDATE invoices SET pdfUri = :path WHERE invoiceId = :id")
    suspend fun updatePdfPath(id: Long, path: String)
}
