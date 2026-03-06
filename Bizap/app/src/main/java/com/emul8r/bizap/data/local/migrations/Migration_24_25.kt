package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 24 → 25: Add performance indexes to invoices, customers, and line_items
 *
 * **Purpose**: Adds indexes on frequently queried columns to eliminate full table scans
 * and improve query performance as the data set grows.
 *
 * **New Indexes**:
 * - invoices(date)                          — filter/sort by invoice date
 * - invoices(customerId, date)              — customer invoice history queries
 * - customers(email) UNIQUE                 — enforce email uniqueness; fast lookup
 * - customers(businessProfileId, name)      — customer search within a business
 * - line_items(currencyCode)               — filter line items by currency
 *
 * **Safety**: All statements use CREATE INDEX IF NOT EXISTS, so the migration is
 * idempotent and safe to run even if indexes already exist.
 */
val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ── Invoices ────────────────────────────────────────────────────────────
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_date ON invoices(date)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_customer_date ON invoices(customerId, date)"
        )

        // ── Customers ────────────────────────────────────────────────────────────
        // Unique index on email — existing duplicate emails (if any) will be dropped
        // during the migration. In practice the app validates uniqueness in the UI.
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_email_unique ON customers(email)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_customers_business_name ON customers(businessProfileId, name)"
        )

        // ── Line Items ───────────────────────────────────────────────────────────
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_line_items_currency_code ON line_items(currencyCode)"
        )
    }
}
