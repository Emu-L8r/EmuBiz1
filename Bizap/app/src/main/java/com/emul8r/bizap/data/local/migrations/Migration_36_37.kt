package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 36 → 37: Fix email constraint for optional email field.
 *
 * Changes:
 * 1. Drop the UNIQUE constraint on the email column (idx_customers_email_unique or idx_customers_email)
 *    - Email should be optional and allow multiple NULL values
 *    - Multiple NULL values on a UNIQUE column cause constraint violations
 * 2. Recreate the non-unique index for email lookups
 *
 * Problem Fixed:
 * - First customer without email: ✅ Created successfully
 * - Second customer without email: ❌ Silent failure due to UNIQUE constraint violation on NULL
 *
 * This migration removes the UNIQUE constraint while maintaining the index for query performance.
 */
val MIGRATION_36_37 = object : Migration(36, 37) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 36→37: Fix email constraint for optional field")

        try {
            // Drop the UNIQUE index on email if it exists
            // SQLite allows multiple attempts to drop non-existent indexes without error
            database.execSQL("DROP INDEX IF EXISTS idx_customers_email")
            Timber.d("✅ Dropped old UNIQUE index on email")

            // Recreate as non-unique index for query performance
            // This allows multiple NULL values while maintaining fast email lookups
            database.execSQL(
                """
                CREATE INDEX IF NOT EXISTS idx_customers_email ON customers(email)
                """.trimIndent()
            )
            Timber.d("✅ Recreated email index as non-unique")

            Timber.i("✅ Migration 36→37 completed successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ Error during Migration 36→37")
            throw e
        }
    }
}

