package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 44 → 45: Add invoice numbering columns for compact date-based format
 *
 * New Format: YY-MMDD-SEQ-CUSTOMER[-vVERSION]
 * Example: 26-0410-01-Smith (April 10, 2026, invoice #1 for Smith)
 *
 * Changes:
 * 1. Add `dailySequence` (INT) - Daily sequence number (1-99)
 * 2. Add `invoiceYear` (INT) - Invoice year for yearly sequences
 * 3. Add `invoiceNumber` (TEXT) - Full formatted invoice number (unique per business)
 *
 * Why this migration:
 * - Enables compact, date-embedded invoice numbering
 * - Supports daily sequence resets
 * - Includes customer identifier in number
 * - Supports version tracking for invoice corrections
 */
val MIGRATION_44_45 = object : Migration(44, 45) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 44→45: Add invoice numbering columns")

        try {
            // Add dailySequence column if not exists
            database.execSQL(
                "ALTER TABLE invoices ADD COLUMN dailySequence INTEGER NOT NULL DEFAULT 1"
            )
            Timber.d("✅ Added dailySequence column")

            // Add invoiceYear column if not exists
            database.execSQL(
                "ALTER TABLE invoices ADD COLUMN invoiceYear INTEGER NOT NULL DEFAULT 2026"
            )
            Timber.d("✅ Added invoiceYear column")

            // Add invoiceNumber column if not exists
            database.execSQL(
                "ALTER TABLE invoices ADD COLUMN invoiceNumber TEXT NOT NULL DEFAULT ''"
            )
            Timber.d("✅ Added invoiceNumber column")

            // Create index for invoiceNumber searches
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_invoices_number ON invoices(invoiceNumber)"
            )
            Timber.d("✅ Created invoiceNumber index")

            Timber.i("✅ Migration 44→45 completed successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ Error during Migration 44→45")
            throw e
        }
    }
}

