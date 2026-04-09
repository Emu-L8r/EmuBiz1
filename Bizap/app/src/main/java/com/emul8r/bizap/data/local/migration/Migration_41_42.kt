package com.emul8r.bizap.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration: DB version 41 → 42
 *
 * Changes:
 * 1. Add `discount_amount` column to `invoices` table.
 *    Defaults to 0 (no discount) for all existing invoices.
 *
 * 2. Create `InvoiceFTS` virtual table (FTS4) for fast full-text search.
 *    The virtual table is populated from the `invoices` table so that
 *    searching by invoice number, customer name, and notes is O(log n)
 *    instead of the previous O(n) LIKE scan.
 */
object MIGRATION_41_42 : Migration(startVersion = 41, endVersion = 42) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Add discount_amount column to invoices
        database.execSQL(
            "ALTER TABLE invoices ADD COLUMN discount_amount INTEGER NOT NULL DEFAULT 0"
        )

        // 2. Create the FTS4 virtual table backed by the invoices table.
        //    Indexed columns: invoiceNumber, customerName, notes.
        database.execSQL(
            """
            CREATE VIRTUAL TABLE IF NOT EXISTS InvoiceFTS
            USING fts4(
                content=`invoices`,
                invoiceNumber,
                customerName,
                notes
            )
            """.trimIndent()
        )

        // 3. Populate FTS table from existing invoice rows.
        database.execSQL(
            """
            INSERT INTO InvoiceFTS(rowid, invoiceNumber, customerName, notes)
            SELECT id, invoiceNumber, customerName, notes FROM invoices
            """.trimIndent()
        )
    }
}
