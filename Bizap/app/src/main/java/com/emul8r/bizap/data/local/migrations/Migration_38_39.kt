package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 38 → 39: Add Canvas template selection and HTML style selection to invoice_settings.
 *
 * Changes:
 * 1. Add `selected_html_style` column (HTML template selection, defaulting to MODERN)
 * 2. Add `selected_canvas_template` column (Canvas template selection, defaulting to MODERN)
 */
val MIGRATION_38_39 = object : Migration(38, 39) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 38→39: Add template selection columns to invoice_settings")

        try {
            // Add selected_html_style column if not already present
            database.execSQL(
                "ALTER TABLE invoice_settings ADD COLUMN selected_html_style TEXT NOT NULL DEFAULT 'MODERN'"
            )
            Timber.d("✅ Added selected_html_style column")
        } catch (e: Exception) {
            // Column may already exist from a prior schema rebuild; log and continue
            Timber.w("⚠️ selected_html_style column may already exist: ${e.message}")
        }

        try {
            // Add selected_canvas_template column for the new 4-template Canvas system
            database.execSQL(
                "ALTER TABLE invoice_settings ADD COLUMN selected_canvas_template TEXT NOT NULL DEFAULT 'MODERN'"
            )
            Timber.d("✅ Added selected_canvas_template column")
        } catch (e: Exception) {
            Timber.w("⚠️ selected_canvas_template column may already exist: ${e.message}")
        }

        Timber.i("✅ Migration 38→39 completed successfully")
    }
}
