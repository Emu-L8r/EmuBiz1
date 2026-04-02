package com.emul8r.bizap.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration: Add invoice_settings table
 *
 * Database version: 37 → 38
 * Adds new invoice_settings table for centralized invoice configuration.
 */
object MIGRATION_AddInvoiceSettings : Migration(
    startVersion = 37,
    endVersion = 38
) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS invoice_settings (
                user_id TEXT PRIMARY KEY NOT NULL,
                selected_theme TEXT NOT NULL DEFAULT 'CANVAS',
                business_name TEXT NOT NULL DEFAULT '',
                business_logo BLOB,
                business_email TEXT NOT NULL DEFAULT '',
                business_phone TEXT NOT NULL DEFAULT '',
                business_address TEXT NOT NULL DEFAULT '',
                business_website TEXT,
                business_abn TEXT,
                primary_color TEXT NOT NULL DEFAULT '#6B4C9A',
                secondary_color TEXT,
                accent_color TEXT,
                font_family TEXT,
                tax_id TEXT,
                tax_rate REAL NOT NULL DEFAULT 0.10,
                tax_name TEXT NOT NULL DEFAULT 'GST',
                tax_handling TEXT NOT NULL DEFAULT 'EXCLUSIVE',
                payment_terms_days INTEGER NOT NULL DEFAULT 30,
                default_payment_notes TEXT DEFAULT '',
                footer_message TEXT DEFAULT 'Thank you for your business',
                invoice_number_prefix TEXT NOT NULL DEFAULT 'INV-',
                bank_name TEXT,
                account_number TEXT,
                routing_code TEXT,
                account_holder TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Create index for faster lookups
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_invoice_settings_user_id ON invoice_settings(user_id)"
        )
    }
}

