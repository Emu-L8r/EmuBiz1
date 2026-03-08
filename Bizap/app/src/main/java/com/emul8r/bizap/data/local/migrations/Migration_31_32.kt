package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 31 → 32: Phase 2 Invoice Management — database layer.
 *
 * Changes:
 * 1. `invoices` table — add three GUI2 fields:
 *    - `invoiceNumber`  TEXT NOT NULL DEFAULT ''  — human-readable number, unique per business
 *    - `isActive`       INTEGER NOT NULL DEFAULT 1 — soft-delete flag
 *    - `createdAt`      INTEGER NOT NULL DEFAULT 0 — creation timestamp (ms)
 *    Plus a supporting index on (invoiceNumber, businessProfileId).
 *
 * 2. New `invoice_items` table — GUI2 line items (separate from `line_items`).
 *
 * 3. New `payments` table — GUI2 payment transactions.
 */
val MIGRATION_31_32 = object : Migration(31, 32) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 31→32: Phase 2 Invoice Management")

        // 1. Add GUI2 fields to the existing invoices table
        database.execSQL("ALTER TABLE invoices ADD COLUMN invoiceNumber TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE invoices ADD COLUMN isActive INTEGER NOT NULL DEFAULT 1")
        database.execSQL("ALTER TABLE invoices ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")

        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoices_number_business " +
                "ON invoices(invoiceNumber, businessProfileId)"
        )

        // 2. Create the invoice_items table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS invoice_items (
                id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                businessId  INTEGER NOT NULL,
                invoiceId   INTEGER NOT NULL,
                description TEXT NOT NULL,
                quantity    REAL NOT NULL,
                unitPrice   INTEGER NOT NULL,
                totalPrice  INTEGER NOT NULL,
                createdAt   INTEGER NOT NULL,
                FOREIGN KEY (invoiceId) REFERENCES invoices(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice ON invoice_items(invoiceId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_invoice_items_business ON invoice_items(businessId)"
        )

        // 3. Create the payments table
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS payments (
                id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                businessId  INTEGER NOT NULL,
                invoiceId   INTEGER NOT NULL,
                amount      INTEGER NOT NULL,
                paymentDate INTEGER NOT NULL,
                notes       TEXT,
                createdAt   INTEGER NOT NULL,
                FOREIGN KEY (invoiceId) REFERENCES invoices(id) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_payments_invoice ON payments(invoiceId)"
        )
        database.execSQL(
            "CREATE INDEX IF NOT EXISTS idx_payments_business ON payments(businessId)"
        )

        Timber.i("✅ Migration 31→32 COMPLETE")
    }
}
