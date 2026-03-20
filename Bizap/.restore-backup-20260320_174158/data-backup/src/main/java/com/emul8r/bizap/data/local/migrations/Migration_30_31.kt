package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 30 → 31: Add customer management fields to support GUI2 CustomerDaoV2.
 *
 * Changes:
 * - `isActive` INTEGER NOT NULL DEFAULT 1 — soft-delete flag
 * - `city`       TEXT                       — optional city field
 * - `postalCode`  TEXT                       — optional postal code field
 */
val MIGRATION_30_31 = object : Migration(30, 31) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 30→31: Add isActive, city, postalCode to customers table")

        database.execSQL("ALTER TABLE customers ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE customers ADD COLUMN city TEXT")
        database.execSQL("ALTER TABLE customers ADD COLUMN postalCode TEXT")

        // Composite index to optimise GUI2 CustomerDaoV2.observeAllCustomers(businessId) query
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_customers_business_active_name " +
                "ON customers(businessProfileId, isActive, name)"
        )

        Timber.i("✅ Migration 30→31 COMPLETE")
    }
}
