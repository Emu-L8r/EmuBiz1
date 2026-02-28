package com.example.databaser.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE is_deleted = 0 ORDER BY id DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE is_deleted = 0 AND (name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR address LIKE '%' || :query || '%' OR contactNumber LIKE '%' || :query || '%') ORDER BY id DESC")
    fun searchCustomers(query: String): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE is_deleted = 1 ORDER BY id DESC")
    fun getDeletedCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: Int): Flow<Customer?>

    @Insert
    suspend fun insert(customer: Customer)

    @Update
    suspend fun update(customer: Customer)

    @Query("UPDATE customers SET is_deleted = 1 WHERE id = :customerId")
    suspend fun softDelete(customerId: Int)

    @Query("UPDATE customers SET is_deleted = 0 WHERE id = :customerId")
    suspend fun restoreCustomer(customerId: Int)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun permanentlyDelete(customerId: Int)

    @Query("DELETE FROM customers")
    suspend fun deleteAll()
}
