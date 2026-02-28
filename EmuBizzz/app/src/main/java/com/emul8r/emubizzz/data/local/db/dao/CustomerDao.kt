package com.emul8r.emubizzz.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.emul8r.emubizzz.data.local.db.entities.CustomerEntity
import com.emul8r.emubizzz.data.local.db.relations.CustomerWithInvoices
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Transaction
    @Query("SELECT * FROM customers")
    fun getCustomersWithInvoices(): Flow<List<CustomerWithInvoices>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?
}
