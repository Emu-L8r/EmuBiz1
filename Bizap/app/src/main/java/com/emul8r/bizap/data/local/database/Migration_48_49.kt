package com.emul8r.bizap.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 48 → 49: Watermark Image Selection + Schema Safety Net
 *
 * PHASE: PDF Settings — Watermark Image Picker
 *
 * Adds the ability for users to enable/disable the brand watermark image
 * displayed on generated PDFs, and to choose which bundled image is used.
 *
 * CHANGES:
 * 1. Add `watermark_image` column (TEXT, default 'THSWA_LOGO') to invoice_settings
 * 2. Safety net: add `enable_brand_watermark` column if it is somehow missing
 *    (guards against older installs where table was created before this
 *    column existed in the entity, which could otherwise cause a Room
 *    schema-validation crash on app start).
 *
 * Each ALTER TABLE is wrapped in a try/catch and guarded by a pre-check of
 * `PRAGMA table_info` so re-running this migration (or resuming after a
 * partial failure) never crashes with "duplicate column name".
 */
val MIGRATION_48_49 = object : Migration(48, 49) {
    override fun migrate(db: SupportSQLiteDatabase) {
        addColumnIfMissing(
            db = db,
            table = "invoice_settings",
            column = "enable_brand_watermark",
            ddl = "ALTER TABLE invoice_settings ADD COLUMN enable_brand_watermark INTEGER NOT NULL DEFAULT 1"
        )

        addColumnIfMissing(
            db = db,
            table = "invoice_settings",
            column = "watermark_image",
            ddl = "ALTER TABLE invoice_settings ADD COLUMN watermark_image TEXT NOT NULL DEFAULT 'THSWA_LOGO'"
        )
    }

    private fun addColumnIfMissing(
        db: SupportSQLiteDatabase,
        table: String,
        column: String,
        ddl: String
    ) {
        try {
            val cursor = db.query("PRAGMA table_info(`$table`)")
            var exists = false
            cursor.use {
                val nameIndex = it.getColumnIndex("name")
                while (it.moveToNext()) {
                    if (nameIndex >= 0 && it.getString(nameIndex) == column) {
                        exists = true
                        break
                    }
                }
            }
            if (!exists) {
                db.execSQL(ddl)
                Timber.i("✅ Migration 48→49: added column '$column' to '$table'")
            } else {
                Timber.d("Migration 48→49: column '$column' already present on '$table', skipping")
            }
        } catch (e: Exception) {
            Timber.e(e, "Migration 48→49: failed adding column '$column' to '$table'")
            throw e
        }
    }
}

