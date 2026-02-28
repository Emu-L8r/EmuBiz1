package com.emul8r.bizap.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.util.Log

/**
 * Migration from version 2 to version 3
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE customers ADD COLUMN notes TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE customers ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
        db.execSQL("ALTER TABLE customers ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
    }
}

/**
 * Migration from version 3 to version 4
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM generated_documents WHERE id NOT IN (SELECT MAX(id) FROM generated_documents GROUP BY relatedInvoiceId, fileType)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_generated_documents_relatedInvoiceId_fileType ON generated_documents(relatedInvoiceId, fileType)")
    }
}

/**
 * Migration from version 4 to version 5
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE generated_documents ADD COLUMN statusUpdatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
        db.execSQL("UPDATE generated_documents SET status = 'ARCHIVED' WHERE status LIKE 'ARCHIVED%'")
        db.execSQL("UPDATE generated_documents SET status = 'SENT' WHERE status LIKE 'EXPORTED%'")
    }
}

/**
 * Migration from version 5 to version 6
 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE invoices ADD COLUMN customerAddress TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE invoices ADD COLUMN customerEmail TEXT")
        db.execSQL("ALTER TABLE invoices ADD COLUMN invoiceNumber TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE invoices ADD COLUMN dueDate INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
        db.execSQL("ALTER TABLE invoices ADD COLUMN taxRate REAL NOT NULL DEFAULT 0.1")
        db.execSQL("ALTER TABLE invoices ADD COLUMN taxAmount REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE invoices ADD COLUMN companyLogoPath TEXT")
        db.execSQL("ALTER TABLE invoices ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
        db.execSQL("UPDATE invoices SET invoiceNumber = 'INV-' || strftime('%Y', date / 1000, 'unixepoch') || '-' || printf('%05d', id) WHERE invoiceNumber = ''")
        db.execSQL("UPDATE invoices SET dueDate = date + (30 * 24 * 60 * 60 * 1000) WHERE dueDate = ${System.currentTimeMillis()}")
    }
}

/**
 * Migration from version 6 to version 7
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE invoices ADD COLUMN amountPaid REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE invoices ADD COLUMN parentInvoiceId INTEGER")
        db.execSQL("ALTER TABLE invoices ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_invoices_parentInvoiceId ON invoices(parentInvoiceId)")
    }
}

/**
 * Migration from version 7 to version 8
 */
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE invoices ADD COLUMN invoiceYear INTEGER NOT NULL DEFAULT 2026")
        db.execSQL("ALTER TABLE invoices ADD COLUMN invoiceSequence INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_invoice_year_sequence ON invoices(invoiceYear, invoiceSequence)")
        Log.i("Migration", "✅ MIGRATION_7_8 completed: Added invoiceYear and invoiceSequence fields")
    }
}

/**
 * Migration from version 8 to version 9
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE invoices_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                customerId INTEGER NOT NULL,
                customerName TEXT NOT NULL,
                customerAddress TEXT NOT NULL,
                customerEmail TEXT,
                date INTEGER NOT NULL,
                totalAmount REAL NOT NULL,
                isQuote INTEGER NOT NULL,
                status TEXT NOT NULL,
                header TEXT,
                subheader TEXT,
                notes TEXT,
                footer TEXT,
                photoUris TEXT,
                pdfUri TEXT,
                dueDate INTEGER NOT NULL,
                taxRate REAL NOT NULL,
                taxAmount REAL NOT NULL,
                companyLogoPath TEXT,
                updatedAt INTEGER NOT NULL,
                amountPaid REAL NOT NULL,
                parentInvoiceId INTEGER,
                version INTEGER NOT NULL,
                invoiceYear INTEGER NOT NULL,
                invoiceSequence INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("""
            INSERT INTO invoices_new 
            SELECT id, customerId, customerName, customerAddress, customerEmail, date,
                   totalAmount, isQuote, status, header, subheader, notes, footer,
                   photoUris, pdfUri, dueDate, taxRate, taxAmount, companyLogoPath,
                   updatedAt, amountPaid, parentInvoiceId, version, invoiceYear, invoiceSequence
            FROM invoices
        """.trimIndent())
        db.execSQL("DROP TABLE invoices")
        db.execSQL("ALTER TABLE invoices_new RENAME TO invoices")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_invoice_year_sequence ON invoices(invoiceYear, invoiceSequence)")
        Log.i("Migration", "✅ MIGRATION_8_9 completed: Removed redundant invoiceNumber column")
    }
}

/**
 * Migration from version 9 to version 10
 */
val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS business_profiles (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                businessName TEXT NOT NULL,
                abn TEXT NOT NULL,
                email TEXT NOT NULL,
                phone TEXT NOT NULL,
                address TEXT NOT NULL,
                website TEXT NOT NULL,
                bsbNumber TEXT,
                accountNumber TEXT,
                accountName TEXT,
                bankName TEXT,
                logoBase64 TEXT,
                signatureUri TEXT,
                createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}
            )
        """.trimIndent())
        db.execSQL("ALTER TABLE invoices ADD COLUMN businessProfileId INTEGER NOT NULL DEFAULT 1")
        db.execSQL("""
            INSERT INTO business_profiles (id, businessName, abn, email, phone, address, website, createdAt)
            VALUES (1, 'Default Business', '00 000 000 000', 'admin@bizap.com', '0000 000 000', '123 Business Way', 'www.bizap.com', ${System.currentTimeMillis()})
        """.trimIndent())
        Log.i("Migration", "✅ MIGRATION_9_10 completed: Multi-business foundation established")
    }
}

/**
 * Migration from version 10 to version 11
 * 1. Creates currencies table for multi-currency support.
 * 2. Creates exchange_rates table.
 * 3. Adds currencyCode to invoices table.
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Create currencies table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS currencies (
                code TEXT PRIMARY KEY NOT NULL,
                symbol TEXT NOT NULL,
                name TEXT NOT NULL,
                isSystemDefault INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        // 2. Create exchange_rates table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS exchange_rates (
                baseCurrencyCode TEXT PRIMARY KEY NOT NULL,
                targetCurrencyCode TEXT NOT NULL,
                rate REAL NOT NULL,
                lastUpdated INTEGER NOT NULL
            )
        """.trimIndent())

        // 3. Add currencyCode to invoices
        db.execSQL("ALTER TABLE invoices ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'AUD'")

        // 4. Seed initial currencies
        db.execSQL("INSERT INTO currencies (code, symbol, name, isSystemDefault) VALUES ('AUD', '$', 'Australian Dollar', 1)")
        db.execSQL("INSERT INTO currencies (code, symbol, name, isSystemDefault) VALUES ('USD', '$', 'US Dollar', 0)")

        Log.i("Migration", "✅ MIGRATION_10_11 completed: Multi-currency foundation established")
    }
}
