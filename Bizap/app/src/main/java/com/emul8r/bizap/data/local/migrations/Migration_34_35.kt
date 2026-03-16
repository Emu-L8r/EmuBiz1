package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 34 → 35: Add notes table.
 *
 * Creates the `notes` table to support the Note entity,
 * which can be linked to either a Customer or an Invoice.
 */
val MIGRATION_34_35 = object : Migration(34, 35) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 34→35: Add notes table")

        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `notes` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `businessProfileId` INTEGER NOT NULL,
                `customerId` INTEGER,
                `invoiceId` INTEGER,
                `title` TEXT NOT NULL,
                `content` TEXT NOT NULL,
                `isCurrent` INTEGER NOT NULL DEFAULT 1,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                `isActive` INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )

        Timber.i("✅ Migration 34→35 COMPLETE")
    }
}
