package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 28 → 29: Add offline operation queue table.
 *
 * Creates the `pending_operations` table used by [OfflineQueueRepository]
 * to persist create / update / delete operations while the device is
 * offline. WorkManager processes the queue once connectivity is restored.
 */
val MIGRATION_28_29 = object : Migration(28, 29) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 28→29: Add offline operation queue")

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS pending_operations (
                id            INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                operationType TEXT    NOT NULL,
                entityType    TEXT    NOT NULL,
                entityId      INTEGER NOT NULL,
                payload       TEXT    NOT NULL,
                createdAt     INTEGER NOT NULL,
                attemptCount  INTEGER NOT NULL DEFAULT 0,
                lastAttemptAt INTEGER,
                status        TEXT    NOT NULL DEFAULT 'PENDING',
                errorMessage  TEXT
            )
        """)

        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_pending_status  ON pending_operations (status)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_pending_entity  ON pending_operations (entityType, entityId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_pending_created ON pending_operations (createdAt)"
        )

        Timber.i("✅ Migration 28→29 COMPLETE - pending_operations table created")
    }
}
