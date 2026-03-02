package com.emul8r.bizap.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS exchange_rates")
        db.execSQL("""
            CREATE TABLE exchange_rates (
                baseCurrencyCode TEXT NOT NULL,
                targetCurrencyCode TEXT NOT NULL,
                rate REAL NOT NULL,
                lastUpdated INTEGER NOT NULL,
                PRIMARY KEY(baseCurrencyCode, targetCurrencyCode)
            )
        """.trimIndent())
        Timber.i("MIGRATION_21_22: Recreated exchange_rates with composite PK")
    }
}

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new customization columns to invoiceTemplates
        db.execSQL("ALTER TABLE invoiceTemplates ADD COLUMN showPhone INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE invoiceTemplates ADD COLUMN showEmail INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE invoiceTemplates ADD COLUMN showAddress INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE invoiceTemplates ADD COLUMN showTaxId INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE invoiceTemplates ADD COLUMN marginPreset TEXT NOT NULL DEFAULT 'NORMAL'")
        db.execSQL("ALTER TABLE invoiceTemplates ADD COLUMN fontSizePreset TEXT NOT NULL DEFAULT 'NORMAL'")
        db.execSQL("ALTER TABLE invoiceTemplates ADD COLUMN showZebraStripes INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE invoiceTemplates ADD COLUMN footerMessage TEXT NOT NULL DEFAULT 'Thank you for your business!'")
        
        Timber.i("MIGRATION_22_23: Added customization fields to invoiceTemplates")
    }
}
