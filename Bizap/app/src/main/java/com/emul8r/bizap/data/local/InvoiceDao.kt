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
    @Query("SELECT * FROM invoices WHERE businessProfileId = :businessId ORDER BY date DESC")
    fun getInvoicesByBusinessId(businessId: Long): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE customerId = :customerId AND businessProfileId = :businessId")
    fun getInvoicesForCustomer(customerId: Long, businessId: Long): Flow<List<InvoiceWithItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

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
}
