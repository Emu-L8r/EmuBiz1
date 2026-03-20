package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 33 → 34: Add invoice display name fields (v1.0.1).
 *
 * Changes:
 * 1. `dailyCounter` (INTEGER NOT NULL DEFAULT 0) — daily reset counter per invoice.
 * 2. `displayName`  (TEXT NOT NULL DEFAULT '')   — computed name: customername-ddMMyyyy-01.
 * 3. Index on `createdAt` for efficient daily-counter queries.
 */
val MIGRATION_33_34 = object : Migration(33, 34) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 33→34: Add invoice displayName fields")

        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN dailyCounter INTEGER NOT NULL DEFAULT 0"
        )
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN displayName TEXT NOT NULL DEFAULT ''"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_createdAt ON invoices(createdAt)"
        )

        Timber.i("✅ Migration 33→34 COMPLETE")
    }
}
