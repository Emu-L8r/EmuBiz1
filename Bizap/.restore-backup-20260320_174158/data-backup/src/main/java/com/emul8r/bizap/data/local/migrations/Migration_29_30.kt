package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 29 → 30: Add offline_operations table for Phase 2 Offline-First Reliability.
 */
val MIGRATION_29_30 = object : Migration(29, 30) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 29→30: Add offline_operations table")

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_operations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                operation_type TEXT NOT NULL,
                entity_id INTEGER NOT NULL,
                entity_data TEXT NOT NULL,
                business_profile_id INTEGER NOT NULL,
                timestamp_ms INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                retry_count INTEGER NOT NULL DEFAULT 0,
                error_message TEXT
            )
        """.trimIndent())
        
        // Add index for faster queries
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_offline_ops_status ON offline_operations(status)")
        database.execSQL("CREATE INDEX IF NOT EXISTS idx_offline_ops_business ON offline_operations(business_profile_id)")

        Timber.i("✅ Migration 29→30 COMPLETE - offline_operations table created")
    }
}
