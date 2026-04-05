package com.emul8r.bizap.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration: Add show_signature_field column to invoice_settings
 *
 * Database version: 40 → 41
 * Adds the Phase 2 signature visibility toggle:
 * - show_signature_field (Boolean, default true) — controls whether the signature /
 *   authorization section is rendered in premium PDF templates
 */
object MIGRATION_AddSignatureField : Migration(
    startVersion = 40,
    endVersion = 41
) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            ALTER TABLE invoice_settings 
            ADD COLUMN show_signature_field INTEGER NOT NULL DEFAULT 1
            """.trimIndent()
        )
    }
}
