package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 32 → 33: Add Notes feature.
 *
 * Changes:
 * 1. New `notes` table — user-created notes, optionally linked to a customer or invoice.
 */
val MIGRATION_32_33 = object : Migration(32, 33) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 32→33: Add notes table")

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS notes (
                id                INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                businessProfileId INTEGER NOT NULL,
                customerId        INTEGER,
                invoiceId         INTEGER,
                title             TEXT NOT NULL,
                content           TEXT NOT NULL,
                isCurrent         INTEGER NOT NULL DEFAULT 1,
                createdAt         INTEGER NOT NULL,
                updatedAt         INTEGER NOT NULL,
                isActive          INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )

        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_notes_business ON notes(businessProfileId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_notes_customer ON notes(customerId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_notes_invoice ON notes(invoiceId)"
        )

        Timber.i("✅ Migration 32→33 COMPLETE")
    }
}
