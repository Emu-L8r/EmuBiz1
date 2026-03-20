package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.emul8r.bizap.data.local.entities.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * GUI2 DAO for customer operations.
 * Supports full CRUD with soft delete via [isActive] flag and
 * businessId-scoped reactive queries.
 */
@Dao
interface CustomerDaoV2 {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(customer: CustomerEntity): Long

    @Update
    suspend fun update(customer: CustomerEntity)

    /**
     * Soft-deletes a customer by setting [isActive] to false.
     * The record is retained in the database for audit/analytics purposes.
     */
    @Query("UPDATE customers SET isActive = 0 WHERE id = :customerId")
    suspend fun softDelete(customerId: Long)

    /**
     * Observes all active customers for the given business, ordered by name.
     */
    @Query("SELECT * FROM customers WHERE businessProfileId = :businessId AND isActive = 1 ORDER BY name ASC")
    fun observeAllCustomers(businessId: Long): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :customerId AND isActive = 1")
    fun observeCustomerById(customerId: Long): Flow<CustomerEntity?>
}
