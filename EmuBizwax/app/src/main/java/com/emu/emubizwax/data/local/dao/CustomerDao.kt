package com.emu.emubizwax.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.emu.emubizwax.data.local.entities.CustomerEntity
import com.emu.emubizwax.data.local.entities.CustomerWithInvoices
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(customer: CustomerEntity): Long

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: Long): Flow<CustomerEntity>

    @Transaction
    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerWithInvoices(customerId: Long): Flow<CustomerWithInvoices>
}
