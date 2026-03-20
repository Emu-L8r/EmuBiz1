package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 26 → 27: Add optimistic-locking columns to daily_revenue_snapshots
 *
 * Adds:
 *   - `version`    INTEGER NOT NULL DEFAULT 1  – incremented on every update so
 *                  concurrent writers can detect and retry on conflict.
 *   - `updatedAtMs` INTEGER NOT NULL DEFAULT 0 – timestamp of the last update,
 *                  used for staleness checks and audit logging.
 */
val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE daily_revenue_snapshots ADD COLUMN version INTEGER NOT NULL DEFAULT 1"
        )
        database.execSQL(
            "ALTER TABLE daily_revenue_snapshots ADD COLUMN updatedAtMs INTEGER NOT NULL DEFAULT 0"
        )
    }
}
