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
 */
val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS currencies (
                code TEXT PRIMARY KEY NOT NULL,
                symbol TEXT NOT NULL,
                name TEXT NOT NULL,
                isSystemDefault INTEGER NOT NULL DEFAULT 0,
                isEnabled INTEGER NOT NULL DEFAULT 1
            )
        """.trimIndent())
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS exchange_rates (
                baseCurrencyCode TEXT PRIMARY KEY NOT NULL,
                targetCurrencyCode TEXT NOT NULL,
                rate REAL NOT NULL,
                lastUpdated INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("ALTER TABLE invoices ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'AUD'")
        db.execSQL("INSERT INTO currencies (code, symbol, name, isSystemDefault, isEnabled) VALUES ('AUD', '$', 'Australian Dollar', 1, 1)")
        db.execSQL("INSERT INTO currencies (code, symbol, name, isSystemDefault, isEnabled) VALUES ('USD', '$', 'US Dollar', 0, 1)")
        Log.i("Migration", "✅ MIGRATION_10_11 completed: Multi-currency foundation established")
    }
}

/**
 * Migration from version 11 to version 12
 */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE currencies RENAME TO currencies_old")
        database.execSQL("""
            CREATE TABLE currencies (
                code TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                symbol TEXT NOT NULL,
                isSystemDefault INTEGER NOT NULL DEFAULT 0,
                isEnabled INTEGER NOT NULL DEFAULT 1
            )
        """)
        database.execSQL("""
            INSERT INTO currencies (code, name, symbol, isSystemDefault, isEnabled)
            SELECT code, name, symbol, isSystemDefault, 1
            FROM currencies_old
        """)
        database.execSQL("DROP TABLE currencies_old")
        Log.i("Migration", "✅ MIGRATION_11_12 completed: Corrected currencies table schema")
    }
}

/**
 * Migration from version 12 to version 13
 * Adds pending_operations table for offline mode support.
 */
val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS pending_operations (
                id TEXT PRIMARY KEY NOT NULL,
                operationType TEXT NOT NULL,
                entityType TEXT NOT NULL,
                entityId INTEGER,
                businessProfileId INTEGER NOT NULL,
                payload TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                retryCount INTEGER NOT NULL DEFAULT 0,
                maxRetries INTEGER NOT NULL DEFAULT 3,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL,
                lastError TEXT
            )
        """.trimIndent())
        
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_pending_status ON pending_operations(status)")
        
        Log.i("Migration", "✅ MIGRATION_12_13 completed: Offline Operation Queue established")
    }
}

/**
 * Migration from version 13 to version 14
 * Analytics foundation tables
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create invoice_analytics_snapshots table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS invoice_analytics_snapshots (
                invoiceId INTEGER PRIMARY KEY NOT NULL,
                businessProfileId INTEGER NOT NULL,
                customerId INTEGER NOT NULL,
                customerName TEXT NOT NULL,
                invoiceNumber TEXT NOT NULL,
                currencyCode TEXT NOT NULL,
                subtotal REAL NOT NULL,
                taxAmount REAL NOT NULL,
                totalAmount REAL NOT NULL,
                status TEXT NOT NULL,
                isPaid INTEGER NOT NULL,
                isOverdue INTEGER NOT NULL,
                invoiceDateMs INTEGER NOT NULL,
                createdAtMs INTEGER NOT NULL,
                paidAtMs INTEGER,
                daysPending INTEGER NOT NULL,
                lineItemCount INTEGER NOT NULL,
                snapshotCreatedAtMs INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_analytics_business ON invoice_analytics_snapshots(businessProfileId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_analytics_date ON invoice_analytics_snapshots(invoiceDateMs)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_analytics_status ON invoice_analytics_snapshots(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_analytics_currency ON invoice_analytics_snapshots(currencyCode)")

        // Create daily_revenue_snapshots table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS daily_revenue_snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                businessProfileId INTEGER NOT NULL,
                dateString TEXT NOT NULL,
                dateMs INTEGER NOT NULL,
                totalRevenue REAL NOT NULL,
                invoiceCount INTEGER NOT NULL,
                paidInvoiceCount INTEGER NOT NULL,
                draftInvoiceCount INTEGER NOT NULL,
                averageInvoiceAmount REAL NOT NULL,
                currencyBreakdown TEXT NOT NULL,
                dayOverDayGrowth REAL NOT NULL,
                weekOverWeekGrowth REAL NOT NULL,
                snapshotCreatedAtMs INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_daily_business ON daily_revenue_snapshots(businessProfileId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_daily_date ON daily_revenue_snapshots(dateString)")

        // Create customer_analytics_snapshots table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS customer_analytics_snapshots (
                customerId INTEGER PRIMARY KEY NOT NULL,
                businessProfileId INTEGER NOT NULL,
                customerName TEXT NOT NULL,
                customerEmail TEXT,
                totalRevenue REAL NOT NULL,
                invoiceCount INTEGER NOT NULL,
                paidInvoiceCount INTEGER NOT NULL,
                overdueInvoiceCount INTEGER NOT NULL,
                averageInvoiceAmount REAL NOT NULL,
                customerLifetimeValue REAL NOT NULL,
                isTopCustomer INTEGER NOT NULL,
                averageDaysToPayment INTEGER NOT NULL,
                paymentRate REAL NOT NULL,
                lastInvoiceDateMs INTEGER,
                lastPaymentDateMs INTEGER,
                isActive INTEGER NOT NULL,
                riskScore INTEGER NOT NULL,
                snapshotCreatedAtMs INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cust_analytics_business ON customer_analytics_snapshots(businessProfileId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_cust_analytics_ltv ON customer_analytics_snapshots(customerLifetimeValue)")

        // Create business_health_metrics table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS business_health_metrics (
                businessProfileId INTEGER PRIMARY KEY NOT NULL,
                healthScore INTEGER NOT NULL,
                healthStatus TEXT NOT NULL,
                monthlyRecurringRevenue REAL NOT NULL,
                totalOutstandingAmount REAL NOT NULL,
                monthlyAverageRevenue REAL NOT NULL,
                monthOverMonthGrowth REAL NOT NULL,
                yearOverYearGrowth REAL NOT NULL,
                onTimePaymentRate REAL NOT NULL,
                averageDaysOutstanding INTEGER NOT NULL,
                overduePercentage REAL NOT NULL,
                activeCustomerCount INTEGER NOT NULL,
                churnedCustomerCount INTEGER NOT NULL,
                newCustomersThisMonth INTEGER NOT NULL,
                invoicesIssuedThisMonth INTEGER NOT NULL,
                averageInvoiceValue REAL NOT NULL,
                largestInvoiceValue REAL NOT NULL,
                lastCalculatedAtMs INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("CREATE INDEX IF NOT EXISTS idx_health_business ON business_health_metrics(businessProfileId)")

        Log.i("Migration", "✅ MIGRATION_13_14 completed: Analytics foundation established")
    }
}

