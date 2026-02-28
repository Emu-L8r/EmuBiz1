package com.example.databaser.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LineItemDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(lineItem: LineItem): Long

    @Update
    suspend fun update(lineItem: LineItem)

    @Delete
    suspend fun delete(lineItem: LineItem)

    @Query("SELECT * FROM line_items WHERE invoiceId = :invoiceId")
    fun getLineItemsForInvoice(invoiceId: Int): Flow<List<LineItem>>

    @Query("SELECT * FROM line_items WHERE id = :lineItemId")
    fun getLineItemById(lineItemId: Int): Flow<LineItem?>

    @Query("DELETE FROM line_items")
    suspend fun deleteAll()
}
