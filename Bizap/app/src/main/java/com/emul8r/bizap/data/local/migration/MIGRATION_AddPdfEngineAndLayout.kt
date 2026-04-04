package com.emul8r.bizap.data.local.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration: Add PDF engine and page layout columns to invoice_settings
 *
 * Database version: 39 → 40
 * Adds new columns for three-tier PDF architecture:
 * - selected_pdf_engine (CANVAS, HTML_CSS)
 * - selected_page_layout (CLASSIC, MODERN)
 * - preview_with_placeholder (Boolean flag for preview mode)
 */
object MIGRATION_AddPdfEngineAndLayout : Migration(
    startVersion = 39,
    endVersion = 40
) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add selected_pdf_engine column
        database.execSQL(
            """
            ALTER TABLE invoice_settings 
            ADD COLUMN selected_pdf_engine TEXT NOT NULL DEFAULT 'HTML_CSS'
            """.trimIndent()
        )

        // Add selected_page_layout column
        database.execSQL(
            """
            ALTER TABLE invoice_settings 
            ADD COLUMN selected_page_layout TEXT NOT NULL DEFAULT 'MODERN'
            """.trimIndent()
        )

        // Add preview_with_placeholder column
        database.execSQL(
            """
            ALTER TABLE invoice_settings 
            ADD COLUMN preview_with_placeholder INTEGER NOT NULL DEFAULT 0
            """.trimIndent()
        )

        // Create index on selected_pdf_engine for query optimization
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_invoice_settings_pdf_engine 
            ON invoice_settings(selected_pdf_engine)
            """.trimIndent()
        )

        // Create index on selected_page_layout for query optimization
        database.execSQL(
            """
            CREATE INDEX IF NOT EXISTS idx_invoice_settings_page_layout 
            ON invoice_settings(selected_page_layout)
            """.trimIndent()
        )
    }
}


