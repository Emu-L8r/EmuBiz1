package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 35 → 36: Add analytics events table for event tracking.
 *
 * Changes:
 * 1. Create `analytics_events` table to store invoice analytics events
 *    - Tracks InvoiceCreated, InvoiceViewed, StatusChanged, PaymentRecorded events
 * 2. Add indexes for common queries (business_id, event_type, timestamp)
 * 3. Enables Week 2 implementation of event-based analytics and reporting
 */
val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 35→36: Add analytics events table")

        // Create analytics_events table for event tracking
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `analytics_events` (
                `id`                INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `business_id`       INTEGER NOT NULL,
                `event_type`        TEXT    NOT NULL,
                `event_data`        TEXT    NOT NULL,
                `timestamp`         INTEGER NOT NULL,
                `created_at`        INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Create indexes for common queries
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `idx_business_event_time` 
            ON `analytics_events`(`business_id`, `event_type`, `timestamp`)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `idx_business_timestamp` 
            ON `analytics_events`(`business_id`, `timestamp` DESC)
            """.trimIndent()
        )

        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS `idx_event_type` 
            ON `analytics_events`(`event_type`)
            """.trimIndent()
        )

        Timber.i("✅ Migration 35→36 completed successfully")
    }
}

