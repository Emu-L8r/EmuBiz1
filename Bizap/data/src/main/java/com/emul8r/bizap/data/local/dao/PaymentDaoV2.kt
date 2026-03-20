package com.emul8r.bizap.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.emul8r.bizap.data.local.entities.PaymentEntity
import kotlinx.coroutines.flow.Flow

/**
 * GUI2 DAO for payment transactions stored in the `payments` table.
 *
 * All monetary values are in cents (Long).
 */
@Dao
interface PaymentDaoV2 {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(payment: PaymentEntity): Long

    /**
     * Observe all payments for a given invoice, newest first.
     */
    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY paymentDate DESC")
    fun observePaymentsForInvoice(invoiceId: Long): Flow<List<PaymentEntity>>

    /**
     * Observe all payments for a business, newest first.
     */
    @Query("SELECT * FROM payments WHERE businessId = :businessId ORDER BY paymentDate DESC")
    fun observeAllPayments(businessId: Long): Flow<List<PaymentEntity>>

    /**
     * Sum of all payments recorded for an invoice, in cents.
     */
    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE invoiceId = :invoiceId")
    fun observeTotalPaid(invoiceId: Long): Flow<Long>

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun delete(paymentId: Long)
}
