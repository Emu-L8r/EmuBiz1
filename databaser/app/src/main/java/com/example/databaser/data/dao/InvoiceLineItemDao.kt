package com.example.databaser.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.databaser.data.model.InvoiceLineItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceLineItemDao {
    @Query("SELECT * FROM invoice_line_items WHERE invoiceId = :invoiceId")
    fun getLineItemsForInvoice(invoiceId: Long): Flow<List<InvoiceLineItem>>

    @Insert
    suspend fun insert(lineItem: InvoiceLineItem): Long

    @Update
    suspend fun update(lineItem: InvoiceLineItem)

    @Query("DELETE FROM invoice_line_items WHERE id = :lineItemId")
    suspend fun delete(lineItemId: Long)
}
