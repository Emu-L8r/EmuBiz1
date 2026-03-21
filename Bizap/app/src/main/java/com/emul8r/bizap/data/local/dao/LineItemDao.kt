package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.LineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LineItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineItem(lineItem: LineItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLineItems(lineItems: List<LineItemEntity>)

    @Query("SELECT * FROM line_items WHERE invoiceId = :invoiceId ORDER BY id ASC")
    fun getLineItemsForInvoice(invoiceId: Long): Flow<List<LineItemEntity>>

    @Query("SELECT * FROM line_items WHERE id = :itemId")
    suspend fun getLineItem(itemId: Long): LineItemEntity?

    @Update
    suspend fun updateLineItem(lineItem: LineItemEntity)

    @Delete
    suspend fun deleteLineItem(lineItem: LineItemEntity)

    @Query("DELETE FROM line_items WHERE invoiceId = :invoiceId")
    suspend fun deleteLineItemsForInvoice(invoiceId: Long)

    @Query("SELECT COUNT(*) FROM line_items WHERE invoiceId = :invoiceId")
    suspend fun getLineItemCountForInvoice(invoiceId: Long): Int
}

