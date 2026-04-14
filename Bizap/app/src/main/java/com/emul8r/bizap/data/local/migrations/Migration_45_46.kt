package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 45 → 46: Add Payment Media Attachment Table
 *
 * ✅ Enables proof-of-payment feature:
 * - Receipt photos
 * - Check images
 * - Transaction confirmations
 * - Before/after photos
 *
 * Tracks all media attachments for payment records with:
 * - Payment reference (FK to invoice_payments)
 * - Invoice denormalization (for queries)
 * - Media metadata (type, size, caption)
 * - Automatic cascade deletion
 */
object Migration_45_46 : Migration(45, 46) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create payment_media_attachments table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS payment_media_attachments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                paymentId INTEGER NOT NULL,
                invoiceId INTEGER NOT NULL,
                mediaUri TEXT NOT NULL,
                mediaType TEXT NOT NULL,
                caption TEXT,
                mediaSize INTEGER NOT NULL DEFAULT 0,
                uploadedAtMs INTEGER NOT NULL,
                FOREIGN KEY(paymentId) REFERENCES invoice_payments(id) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create indices for common queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_payment_media_paymentId
            ON payment_media_attachments(paymentId)
        """.trimIndent())

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_payment_media_invoiceId
            ON payment_media_attachments(invoiceId)
        """.trimIndent())

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_payment_media_uploadedAtMs
            ON payment_media_attachments(uploadedAtMs)
        """.trimIndent())
    }
}

