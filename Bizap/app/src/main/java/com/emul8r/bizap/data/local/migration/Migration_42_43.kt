package com.emul8r.bizap.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration: DB version 42 → 43
 *
 * Changes:
 * 1. Delete all existing invoices (v1.0 not yet released publicly)
 * 2. Add 3 new columns to `invoice_settings` table for Phase 3 customization layer:
 *    - `selected_color_scheme` (TEXT): Color palette selection (PROFESSIONAL, VIBRANT, etc.)
 *    - `selected_spacing_profile` (TEXT): Spacing preset (TIGHT, NORMAL, GENEROUS, PREMIUM)
 *    - `visual_accents_json` (TEXT): JSON-serialized visual accent toggles
 *
 * ## Rationale
 * - Invoice deletion: Since app hasn't been publicly released, deleting test data is acceptable
 * - New columns: Enable enhanced PDF customization with 4-layer design system
 */
object MIGRATION_42_43 : Migration(startVersion = 42, endVersion = 43) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.d("🔄 Executing migration v42→v43: Add customization layers + delete old invoices")

        // 1. Delete all invoices (no production data to preserve)
        Timber.d("🗑️ Deleting all invoices...")
        database.execSQL("DELETE FROM invoices")
        database.execSQL("DELETE FROM line_items")
        database.execSQL("DELETE FROM InvoiceFTS")  // FTS index also needs clearing

        // 2. Add new customization columns to invoice_settings table
        Timber.d("📝 Adding new customization columns to invoice_settings...")

        // Color scheme column (defaults to PROFESSIONAL)
        database.execSQL(
            """
            ALTER TABLE invoice_settings
            ADD COLUMN selected_color_scheme TEXT NOT NULL DEFAULT 'PROFESSIONAL'
            """.trimIndent()
        )

        // Spacing profile column (defaults to NORMAL)
        database.execSQL(
            """
            ALTER TABLE invoice_settings
            ADD COLUMN selected_spacing_profile TEXT NOT NULL DEFAULT 'NORMAL'
            """.trimIndent()
        )

        // Visual accents JSON column (defaults to all enabled)
        database.execSQL(
            """
            ALTER TABLE invoice_settings
            ADD COLUMN visual_accents_json TEXT NOT NULL DEFAULT '{"showBorders":true,"showShadows":true,"showDividers":true,"highlightTotals":true,"useGradients":false}'
            """.trimIndent()
        )

        Timber.d("✅ Migration v42→v43 completed successfully")
    }
}

