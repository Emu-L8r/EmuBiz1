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
