package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 25 → 26: Fix schema mismatch from Migration 24→25
 *
 * The previous migration (24→25) created indexes with wrong names.
 * This migration:
 * 1. Drops the incorrectly named indexes
 * 2. Creates the correctly named indexes that match the entity definition
 * 3. Ensures complete schema alignment
 */
val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop incorrectly named indexes from migration 24→25
        database.execSQL(
            "DROP INDEX IF EXISTS idx_customers_email_unique"
        )

        // Ensure all correct indexes exist (recreate to handle various states)
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_customers_business ON customers(businessProfileId)"
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_email ON customers(email)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_customers_business_name ON customers(businessProfileId, name)"
        )

        // Ensure invoice indexes are correct
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_business ON invoices(businessProfileId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_customer ON invoices(customerId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_business_status ON invoices(businessProfileId, status)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_year_sequence ON invoices(invoiceYear, invoiceSequence, businessProfileId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_date ON invoices(date)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_customer_date ON invoices(customerId, date)"
        )

        // Line items indexes
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_line_items_currency_code ON line_items(currencyCode)"
        )
    }
}

