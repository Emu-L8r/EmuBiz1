package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 22 → 23: Add currency_code column to line_items table
 *
 * Previously, line items had no currency context, making historical prices ambiguous
 * in multi-currency invoices. This migration adds a currencyCode column to track
 * which currency each line item was priced in.
 */
val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add currency_code column to line_items table
        // Default to 'AUD' for existing line items
        database.execSQL("""
            ALTER TABLE line_items 
            ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'AUD'
        """)
    }
}

