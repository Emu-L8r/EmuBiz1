package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 21 → 22: Remove offline sync subsystem
 *
 * Deletes the pending_operations table since offline sync is being removed.
 * This is an offline-only local database app with no server sync.
 * If server sync is needed in the future, it can be redesigned from scratch.
 */
val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop the pending_operations table
        // (The table is dropped, not recreated, because the feature is removed)
        database.execSQL("DROP TABLE IF EXISTS pending_operations")
    }
}

