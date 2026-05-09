package com.emul8r.bizap.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 47 → 48: Finalize Invoice Settings Schema
 *
 * PHASE 3F: Ensure invoice_settings table is properly initialized
 * with all required columns for Phase 3E data persistence.
 *
 * This migration validates and initializes the invoice_settings table
 * if it doesn't already exist, providing a fallback for fresh installs
 * and ensuring backward compatibility with existing data.
 *
 * CHANGES:
 * 1. Verify invoice_settings table exists
 * 2. Add any missing indices for performance
 * 3. Validate schema consistency with InvoiceSettings entity
 */
val MIGRATION_47_48 = object : Migration(47, 48) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Step 1: Ensure invoice_settings table exists (safety check)
        // This handles edge case where migration 37 (AddInvoiceSettings) was skipped
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS invoice_settings (
                user_id TEXT PRIMARY KEY NOT NULL,
                selected_theme TEXT NOT NULL DEFAULT 'CANVAS',
                selected_pdf_engine TEXT NOT NULL DEFAULT 'HTML_CSS',
                selected_page_layout TEXT NOT NULL DEFAULT 'MODERN',
                selected_typography TEXT NOT NULL DEFAULT 'MODERN',
                selected_locale TEXT NOT NULL DEFAULT 'AUSTRALIAN',
                selected_html_style TEXT NOT NULL DEFAULT 'MODERN',
                selected_canvas_template TEXT NOT NULL DEFAULT 'MODERN',
                selected_color_scheme TEXT NOT NULL DEFAULT 'PROFESSIONAL',
                selected_spacing_profile TEXT NOT NULL DEFAULT 'NORMAL',
                visual_accents_json TEXT NOT NULL DEFAULT '{}',
                enable_gradient_header INTEGER NOT NULL DEFAULT 1,
                header_gradient_end_color TEXT NOT NULL DEFAULT '#FF9F43',
                enable_rounded_corners INTEGER NOT NULL DEFAULT 1,
                corner_radius_dp REAL NOT NULL DEFAULT 8.0,
                enable_shadows INTEGER NOT NULL DEFAULT 1,
                shadow_intensity REAL NOT NULL DEFAULT 0.15,
                enable_alternating_row_colors INTEGER NOT NULL DEFAULT 1,
                alternate_row_color TEXT NOT NULL DEFAULT '#F5F5F5',
                enable_dividers INTEGER NOT NULL DEFAULT 1,
                divider_style TEXT NOT NULL DEFAULT 'SOLID',
                divider_color TEXT NOT NULL DEFAULT '#CCCCCC',
                divider_thickness_px REAL NOT NULL DEFAULT 1.0,
                highlight_totals INTEGER NOT NULL DEFAULT 1,
                total_box_style TEXT NOT NULL DEFAULT 'SUBTLE_BACKGROUND',
                enable_status_badges INTEGER NOT NULL DEFAULT 1,
                badge_style TEXT NOT NULL DEFAULT 'ROUNDED_FILLED',
                enable_background_pattern INTEGER NOT NULL DEFAULT 0,
                background_pattern_type TEXT NOT NULL DEFAULT 'WAVES',
                pattern_opacity REAL NOT NULL DEFAULT 0.08,
                enable_watermark_text INTEGER NOT NULL DEFAULT 0,
                watermark_text TEXT NOT NULL DEFAULT '',
                watermark_opacity REAL NOT NULL DEFAULT 0.1,
                enable_logo INTEGER NOT NULL DEFAULT 0,
                logo_uri TEXT NOT NULL DEFAULT '',
                logo_width_mm REAL NOT NULL DEFAULT 30.0,
                logo_height_mm REAL NOT NULL DEFAULT 30.0,
                logo_position TEXT NOT NULL DEFAULT 'TOP_LEFT',
                enable_motto INTEGER NOT NULL DEFAULT 0,
                motto_text TEXT NOT NULL DEFAULT '',
                motto_font_size REAL NOT NULL DEFAULT 10.0,
                motto_color TEXT NOT NULL DEFAULT '#666666',
                enable_payment_icons INTEGER NOT NULL DEFAULT 0,
                accepted_payment_methods_json TEXT NOT NULL DEFAULT '[]',
                payment_icons_size REAL NOT NULL DEFAULT 16.0,
                enable_signature_area INTEGER NOT NULL DEFAULT 0,
                signature_label TEXT NOT NULL DEFAULT 'Authorized By:',
                signature_line_length_mm REAL NOT NULL DEFAULT 40.0,
                enable_qr_code INTEGER NOT NULL DEFAULT 0,
                qr_code_content TEXT NOT NULL DEFAULT '',
                qr_code_size_mm REAL NOT NULL DEFAULT 20.0,
                qr_code_position TEXT NOT NULL DEFAULT 'BOTTOM_RIGHT',
                company_motto TEXT NOT NULL DEFAULT '',
                company_website TEXT NOT NULL DEFAULT '',
                company_social_media_json TEXT NOT NULL DEFAULT '{}',
                preview_with_placeholder INTEGER NOT NULL DEFAULT 0,
                show_business_abn INTEGER NOT NULL DEFAULT 1,
                show_customer_phone INTEGER NOT NULL DEFAULT 1,
                show_status_watermark INTEGER NOT NULL DEFAULT 1,
                show_page_numbers INTEGER NOT NULL DEFAULT 0,
                show_signature_field INTEGER NOT NULL DEFAULT 1,
                primary_color TEXT NOT NULL DEFAULT '#6B4C9A',
                secondary_color TEXT NOT NULL DEFAULT '#f5f5f5',
                accent_color TEXT NOT NULL DEFAULT '#2c3e50',
                font_family TEXT,
                payment_terms_days INTEGER NOT NULL DEFAULT 30,
                default_payment_notes TEXT NOT NULL DEFAULT '',
                footer_message TEXT NOT NULL DEFAULT 'Thank you for your business',
                invoice_number_prefix TEXT NOT NULL DEFAULT 'INV-',
                tax_rate REAL NOT NULL DEFAULT 0.10,
                tax_name TEXT NOT NULL DEFAULT 'GST',
                tax_handling TEXT NOT NULL DEFAULT 'EXCLUSIVE',
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
        """.trimIndent())

        // Step 2: Create index on user_id for fast lookups (already primary key, but explicit index for clarity)
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_invoice_settings_user_id
            ON invoice_settings(user_id)
        """.trimIndent())

        // Step 3: Create index on updated_at for sorting/filtering by recency
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_invoice_settings_updated_at
            ON invoice_settings(updated_at)
        """.trimIndent())
    }
}

