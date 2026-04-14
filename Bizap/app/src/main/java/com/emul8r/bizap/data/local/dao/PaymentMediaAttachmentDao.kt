package com.emul8r.bizap.data.local.dao

import androidx.room.*
import com.emul8r.bizap.data.local.entities.PaymentMediaAttachment
import kotlinx.coroutines.flow.Flow

/**
 * Data access for payment proof media attachments.
 *
 * ✅ NEW: Supports attaching receipts, check photos, and payment proofs
 * to invoice payments for complete payment audit trail.
 *
 * Features:
 * - Attach photos/documents to payment records
 * - Caption and organize proof materials
 * - Query all media for a payment or invoice
 * - Cascade delete when payment is removed
 */
@Dao
interface PaymentMediaAttachmentDao {

    /**
     * Insert a new media attachment for a payment
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: PaymentMediaAttachment)

    /**
     * Insert multiple attachments
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<PaymentMediaAttachment>)

    /**
     * Get all media attachments for a payment
     */
    @Query("""
        SELECT * FROM payment_media_attachments
        WHERE paymentId = :paymentId
        ORDER BY uploadedAtMs DESC
    """)
    suspend fun getAttachmentsForPayment(paymentId: Long): List<PaymentMediaAttachment>

    /**
     * Observe all media for a payment (reactive)
     */
    @Query("""
        SELECT * FROM payment_media_attachments
        WHERE paymentId = :paymentId
        ORDER BY uploadedAtMs DESC
    """)
    fun observeAttachmentsForPayment(paymentId: Long): Flow<List<PaymentMediaAttachment>>

    /**
     * Get all media attachments for an invoice
     * (includes all payments on that invoice)
     */
    @Query("""
        SELECT * FROM payment_media_attachments
        WHERE invoiceId = :invoiceId
        ORDER BY uploadedAtMs DESC
    """)
    suspend fun getAttachmentsForInvoice(invoiceId: Long): List<PaymentMediaAttachment>

    /**
     * Observe all media for an invoice (reactive)
     */
    @Query("""
        SELECT * FROM payment_media_attachments
        WHERE invoiceId = :invoiceId
        ORDER BY uploadedAtMs DESC
    """)
    fun observeAttachmentsForInvoice(invoiceId: Long): Flow<List<PaymentMediaAttachment>>

    /**
     * Update an attachment (caption, etc.)
     */
    @Update
    suspend fun updateAttachment(attachment: PaymentMediaAttachment)

    /**
     * Delete a single attachment
     */
    @Delete
    suspend fun deleteAttachment(attachment: PaymentMediaAttachment)

    /**
     * Delete all attachments for a payment
     */
    @Query("DELETE FROM payment_media_attachments WHERE paymentId = :paymentId")
    suspend fun deleteAttachmentsForPayment(paymentId: Long)

    /**
     * Delete all attachments for an invoice
     */
    @Query("DELETE FROM payment_media_attachments WHERE invoiceId = :invoiceId")
    suspend fun deleteAttachmentsForInvoice(invoiceId: Long)

    /**
     * Count attachments for a payment
     */
    @Query("SELECT COUNT(*) FROM payment_media_attachments WHERE paymentId = :paymentId")
    suspend fun countAttachmentsForPayment(paymentId: Long): Int

    /**
     * Count attachments for an invoice
     */
    @Query("SELECT COUNT(*) FROM payment_media_attachments WHERE invoiceId = :invoiceId")
    suspend fun countAttachmentsForInvoice(invoiceId: Long): Int

    /**
     * Calculate total size of media for an invoice (for storage management)
     */
    @Query("SELECT COALESCE(SUM(mediaSize), 0) FROM payment_media_attachments WHERE invoiceId = :invoiceId")
    suspend fun getTotalMediaSizeForInvoice(invoiceId: Long): Long

    /**
     * Health check: Find media with missing payments
     */
    @Query("""
        SELECT DISTINCT pma.paymentId FROM payment_media_attachments pma
        LEFT JOIN invoice_payments ip ON pma.paymentId = ip.id
        WHERE ip.id IS NULL
    """)
    suspend fun getOrphanedMediaPaymentIds(): List<Long>

    /**
     * Cleanup: Delete all orphaned media attachments
     */
    @Query("""
        DELETE FROM payment_media_attachments
        WHERE paymentId NOT IN (SELECT id FROM invoice_payments)
    """)
    suspend fun deleteOrphanedMedia()
}

