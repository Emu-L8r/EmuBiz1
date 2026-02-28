package com.example.databaser.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Transaction
    @Query("SELECT * FROM invoices WHERE customerId = :customerId")
    fun getInvoicesForCustomer(customerId: Int): Flow<List<InvoiceWithCustomerAndLineItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    fun getInvoiceById(invoiceId: Int): Flow<InvoiceWithCustomerAndLineItems?>

    @Transaction
    @Query("SELECT * FROM invoices")
    fun getAllInvoices(): Flow<List<InvoiceWithCustomerAndLineItems>>

    @Transaction
    @Query("SELECT * FROM invoices")
    suspend fun getAllInvoicesList(): List<InvoiceWithCustomerAndLineItems>

    @Transaction
    @Query("SELECT * FROM invoices WHERE isQuote = 0 AND isSent = 0")
    fun getUnsentInvoices(): Flow<List<InvoiceWithCustomerAndLineItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE isQuote = 0 AND isSent = 1 AND isPaid = 0")
    fun getSentInvoices(): Flow<List<InvoiceWithCustomerAndLineItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE isQuote = 0 AND isPaid = 1")
    fun getPaidInvoices(): Flow<List<InvoiceWithCustomerAndLineItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE isQuote = 1 AND isSent = 0")
    fun getUnsentQuotes(): Flow<List<InvoiceWithCustomerAndLineItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE isQuote = 1 AND isSent = 1")
    fun getSentQuotes(): Flow<List<InvoiceWithCustomerAndLineItems>>

    @Insert
    suspend fun insert(invoice: Invoice): Long

    @Insert
    suspend fun insertAll(invoices: List<Invoice>)

    @Update
    suspend fun update(invoice: Invoice)

    @Delete
    suspend fun delete(invoice: Invoice)

    @Query("SELECT COUNT(*) FROM invoices WHERE date >= :startOfDay AND date < :endOfDay AND isQuote = 0")
    suspend fun getInvoiceCountForDate(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM invoices WHERE isQuote = 1")
    suspend fun getQuoteCount(): Int

    @Query("DELETE FROM invoices")
    suspend fun deleteAll()

    @Insert
    suspend fun insertLineItem(lineItem: LineItem): Long

    @Query("DELETE FROM line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteLineItemsByInvoiceId(invoiceId: Int)
}
