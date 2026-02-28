package com.example.databaser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.databaser.data.model.Invoice
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Insert
    suspend fun insert(invoice: Invoice): Long

    @Update
    suspend fun update(invoice: Invoice)

    @Query("SELECT * FROM invoices")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    fun getInvoiceById(invoiceId: Long): Flow<Invoice>

    @Query("SELECT * FROM invoices WHERE customerId = :customerId")
    fun getInvoicesForCustomer(customerId: Long): Flow<List<Invoice>>

    @Query("DELETE FROM invoices WHERE id = :invoiceId")
    suspend fun delete(invoiceId: Long)

    @Query("SELECT COUNT(*) FROM invoices")
    fun getInvoiceCount(): Flow<Int>
}
