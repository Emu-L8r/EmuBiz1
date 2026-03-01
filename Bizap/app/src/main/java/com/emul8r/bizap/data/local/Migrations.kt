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
 */
val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
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

/**
 * Migration from version 14 to version 15
 */
val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `customer_analytics_snapshots`")
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `customer_analytics_snapshots` (
                `customerId` INTEGER NOT NULL PRIMARY KEY,
                `businessProfileId` INTEGER NOT NULL,
                `customerName` TEXT NOT NULL,
                `customerEmail` TEXT,
                `totalRevenue` REAL NOT NULL DEFAULT 0.0,
                `invoiceCount` INTEGER NOT NULL DEFAULT 0,
                `paidInvoiceCount` INTEGER NOT NULL DEFAULT 0,
                `overdueInvoiceCount` INTEGER NOT NULL DEFAULT 0,
                `averageInvoiceAmount` REAL NOT NULL DEFAULT 0.0,
                `customerLifetimeValue` REAL NOT NULL DEFAULT 0.0,
                `estimatedLTV` REAL NOT NULL DEFAULT 0.0,
                `isTopCustomer` INTEGER NOT NULL DEFAULT 0,
                `segment` TEXT NOT NULL DEFAULT 'NEW',
                `purchaseVelocity` REAL NOT NULL DEFAULT 0.0,
                `averageDaysBetweenPurchases` REAL NOT NULL DEFAULT 0.0,
                `daysSinceLastPurchase` INTEGER NOT NULL DEFAULT 0,
                `churnRiskScore` REAL NOT NULL DEFAULT 0.0,
                `isPredictedToChurn` INTEGER NOT NULL DEFAULT 0,
                `churnRiskFactors` TEXT NOT NULL DEFAULT '[]',
                `lastInvoiceDateMs` INTEGER,
                `lastPaymentDateMs` INTEGER,
                `isActive` INTEGER NOT NULL DEFAULT 1,
                `riskScore` INTEGER NOT NULL DEFAULT 0,
                `snapshotCreatedAtMs` INTEGER NOT NULL,
                `lastUpdatedMs` INTEGER NOT NULL
            )
        """.trimIndent())
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_cust_analytics_business` ON `customer_analytics_snapshots` (`businessProfileId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_cust_analytics_ltv` ON `customer_analytics_snapshots` (`customerLifetimeValue`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_cust_analytics_segment` ON `customer_analytics_snapshots` (`segment`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_cust_analytics_churn` ON `customer_analytics_snapshots` (`isPredictedToChurn`)")
        Log.i("Migration", "✅ MIGRATION_14_15 completed: Advanced Customer Analytics established")
    }
}

/**
 * Migration from version 15 to version 16
 * Add Invoice Payment Analytics tables
 */
val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create invoice_payments table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `invoice_payments` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `invoiceId` INTEGER NOT NULL,
                `amountPaid` REAL NOT NULL,
                `paymentDate` INTEGER NOT NULL,
                `paymentMethod` TEXT NOT NULL,
                `transactionReference` TEXT NOT NULL,
                `notes` TEXT,
                `createdAtMs` INTEGER NOT NULL,
                `updatedAtMs` INTEGER NOT NULL,
                FOREIGN KEY(`invoiceId`) REFERENCES `invoices`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create invoice_payment_snapshots table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `invoice_payment_snapshots` (
                `invoiceId` INTEGER PRIMARY KEY NOT NULL,
                `businessProfileId` INTEGER NOT NULL,
                `customerId` INTEGER NOT NULL,
                `customerName` TEXT NOT NULL,
                `invoiceNumber` TEXT NOT NULL,
                `invoiceDate` INTEGER NOT NULL,
                `dueDate` INTEGER NOT NULL,
                `totalAmount` REAL NOT NULL,
                `paidAmount` REAL NOT NULL,
                `outstandingAmount` REAL NOT NULL,
                `paymentStatus` TEXT NOT NULL,
                `ageingBucket` TEXT NOT NULL,
                `daysOverdue` INTEGER NOT NULL,
                `daysSinceDue` INTEGER NOT NULL,
                `lastPaymentDate` INTEGER,
                `lastPaymentAmount` REAL NOT NULL DEFAULT 0.0,
                `paymentCount` INTEGER NOT NULL DEFAULT 0,
                `isAtRisk` INTEGER NOT NULL DEFAULT 0,
                `riskScore` REAL NOT NULL DEFAULT 0.0,
                `riskFactors` TEXT NOT NULL DEFAULT '',
                `lastUpdatedMs` INTEGER NOT NULL,
                `snapshotDateMs` INTEGER NOT NULL,
                FOREIGN KEY(`invoiceId`) REFERENCES `invoices`(`id`) ON DELETE CASCADE,
                FOREIGN KEY(`customerId`) REFERENCES `customers`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create daily_payment_snapshots table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `daily_payment_snapshots` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `businessProfileId` INTEGER NOT NULL,
                `snapshotDate` INTEGER NOT NULL,
                `paymentsReceivedCount` INTEGER NOT NULL DEFAULT 0,
                `paymentsReceivedAmount` REAL NOT NULL DEFAULT 0.0,
                `invoicesDueCount` INTEGER NOT NULL DEFAULT 0,
                `invoicesDueAmount` REAL NOT NULL DEFAULT 0.0,
                `invoicesOverdueCount` INTEGER NOT NULL DEFAULT 0,
                `invoicesOverdueAmount` REAL NOT NULL DEFAULT 0.0,
                `outstandingCurrent` REAL NOT NULL DEFAULT 0.0,
                `outstandingPast30` REAL NOT NULL DEFAULT 0.0,
                `outstandingPast60` REAL NOT NULL DEFAULT 0.0,
                `outstandingPast90` REAL NOT NULL DEFAULT 0.0,
                `collectionRate` REAL NOT NULL DEFAULT 0.0,
                `averagePaymentTime` REAL NOT NULL DEFAULT 0.0,
                `projectedMonthlyRevenue` REAL NOT NULL DEFAULT 0.0,
                `createdAtMs` INTEGER NOT NULL
            )
        """.trimIndent())

        // Create collection_metrics table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `collection_metrics` (
                `businessProfileId` INTEGER PRIMARY KEY NOT NULL,
                `metricsDate` INTEGER NOT NULL,
                `totalInvoicesIssued` INTEGER NOT NULL DEFAULT 0,
                `totalInvoiceAmount` REAL NOT NULL DEFAULT 0.0,
                `totalPaidAmount` REAL NOT NULL DEFAULT 0.0,
                `totalOutstandingAmount` REAL NOT NULL DEFAULT 0.0,
                `collectionRate` REAL NOT NULL DEFAULT 0.0,
                `ageingCurrent` REAL NOT NULL DEFAULT 0.0,
                `ageingPast30` REAL NOT NULL DEFAULT 0.0,
                `ageingPast60` REAL NOT NULL DEFAULT 0.0,
                `ageingPast90` REAL NOT NULL DEFAULT 0.0,
                `averageDaysToPayment` REAL NOT NULL DEFAULT 0.0,
                `medianDaysToPayment` REAL NOT NULL DEFAULT 0.0,
                `overdueInvoiceCount` INTEGER NOT NULL DEFAULT 0,
                `overdueAmount` REAL NOT NULL DEFAULT 0.0,
                `collectionRateTrend` REAL NOT NULL DEFAULT 0.0,
                `overdueTrend` REAL NOT NULL DEFAULT 0.0,
                `projectedCollectionRate30Days` REAL NOT NULL DEFAULT 0.0,
                `projectedOutstanding30Days` REAL NOT NULL DEFAULT 0.0,
                `lastUpdatedMs` INTEGER NOT NULL
            )
        """.trimIndent())

        // Create indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_invoice_payments_invoiceId` ON `invoice_payments` (`invoiceId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_invoice_payment_snapshots_business` ON `invoice_payment_snapshots` (`businessProfileId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_invoice_payment_snapshots_customer` ON `invoice_payment_snapshots` (`customerId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_invoice_payment_snapshots_status` ON `invoice_payment_snapshots` (`paymentStatus`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_invoice_payment_snapshots_aging` ON `invoice_payment_snapshots` (`ageingBucket`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_invoice_payment_snapshots_risk` ON `invoice_payment_snapshots` (`isAtRisk`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_daily_payment_snapshots_business` ON `daily_payment_snapshots` (`businessProfileId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `idx_daily_payment_snapshots_date` ON `daily_payment_snapshots` (`snapshotDate`)")
        
        Log.i("Migration", "✅ MIGRATION_15_16 completed: Payment Analytics established")
    }
}

/**
 * Migration from version 16 to version 17
 * Add missing businessProfileId column to customers table and recreate with proper schema
 */
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Drop existing indices
        db.execSQL("DROP INDEX IF EXISTS idx_customers_business")
        db.execSQL("DROP INDEX IF EXISTS idx_customers_email")

        // Add the missing column
        db.execSQL("ALTER TABLE customers ADD COLUMN businessProfileId INTEGER NOT NULL DEFAULT 1")

        // Recreate indices to match entity definition
        db.execSQL("CREATE INDEX `idx_customers_business` ON `customers` (`businessProfileId`)")
        db.execSQL("CREATE INDEX `idx_customers_email` ON `customers` (`email`)")

        Log.i("Migration", "✅ MIGRATION_16_17 completed: Added businessProfileId to customers table")
    }
}

/**
 * Migration from version 17 to version 18
 * Add Invoice Templates and Custom Fields tables
 */
val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create invoiceTemplates table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `invoiceTemplates` (
              `id` TEXT NOT NULL PRIMARY KEY,
              `businessProfileId` INTEGER NOT NULL,
              `name` TEXT NOT NULL,
              `designType` TEXT NOT NULL,
              `logoFileName` TEXT,
              `primaryColor` TEXT NOT NULL DEFAULT '#FF5722',
              `secondaryColor` TEXT NOT NULL DEFAULT '#FFF9C4',
              `fontFamily` TEXT NOT NULL DEFAULT 'SANS_SERIF',
              `companyName` TEXT NOT NULL DEFAULT '',
              `companyAddress` TEXT NOT NULL DEFAULT '',
              `companyPhone` TEXT NOT NULL DEFAULT '',
              `companyEmail` TEXT NOT NULL DEFAULT '',
              `taxId` TEXT,
              `bankDetails` TEXT,
              `hideLineItems` INTEGER NOT NULL DEFAULT 0,
              `hidePaymentTerms` INTEGER NOT NULL DEFAULT 0,
              `isDefault` INTEGER NOT NULL DEFAULT 0,
              `isActive` INTEGER NOT NULL DEFAULT 1,
              `createdAt` INTEGER NOT NULL,
              `updatedAt` INTEGER NOT NULL,
              FOREIGN KEY(`businessProfileId`) REFERENCES `business_profiles`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create indices for invoiceTemplates
        db.execSQL("CREATE INDEX `idx_invoiceTemplates_businessProfileId` ON `invoiceTemplates`(`businessProfileId`)")
        db.execSQL("CREATE INDEX `idx_invoiceTemplates_businessProfileId_isDefault` ON `invoiceTemplates`(`businessProfileId`, `isDefault`)")
        db.execSQL("CREATE INDEX `idx_invoiceTemplates_businessProfileId_isActive` ON `invoiceTemplates`(`businessProfileId`, `isActive`)")

        // Create invoiceCustomFields table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `invoiceCustomFields` (
              `id` TEXT NOT NULL PRIMARY KEY,
              `templateId` TEXT NOT NULL,
              `label` TEXT NOT NULL,
              `fieldType` TEXT NOT NULL,
              `isRequired` INTEGER NOT NULL DEFAULT 0,
              `displayOrder` INTEGER NOT NULL,
              `isActive` INTEGER NOT NULL DEFAULT 1,
              FOREIGN KEY(`templateId`) REFERENCES `invoiceTemplates`(`id`) ON DELETE CASCADE
            )
        """.trimIndent())

        // Create indices for invoiceCustomFields
        db.execSQL("CREATE INDEX `idx_invoiceCustomFields_templateId` ON `invoiceCustomFields`(`templateId`)")
        db.execSQL("CREATE INDEX `idx_invoiceCustomFields_templateId_displayOrder` ON `invoiceCustomFields`(`templateId`, `displayOrder`)")

        Log.i("Migration", "✅ MIGRATION_17_18 completed: Invoice Templates and Custom Fields established")
    }
}

/**
 * Migration from version 18 to version 19
 * Adds template integration fields to invoices table
 */
val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add template reference field
        db.execSQL("ALTER TABLE invoices ADD COLUMN templateId TEXT")

        // Add template snapshot (JSON) field
        db.execSQL("ALTER TABLE invoices ADD COLUMN templateSnapshot TEXT")

        // Add custom field values (JSON map) field
        db.execSQL("ALTER TABLE invoices ADD COLUMN customFieldValues TEXT")

        // Create index for template lookups
        db.execSQL("CREATE INDEX `idx_invoices_templateId` ON `invoices`(`templateId`)")

        Log.i("Migration", "✅ MIGRATION_18_19 completed: Invoice template integration added")
    }
}

/**
 * Migration from version 19 to version 20
 * Adds tax registration fields to business_profiles table
 */
val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add tax registration flag (default false - not registered)
        db.execSQL("ALTER TABLE business_profiles ADD COLUMN isTaxRegistered INTEGER NOT NULL DEFAULT 0")

        // Add default tax rate (default 0.10 = 10%)
        db.execSQL("ALTER TABLE business_profiles ADD COLUMN defaultTaxRate REAL NOT NULL DEFAULT 0.10")

        Log.i("Migration", "✅ MIGRATION_19_20 completed: Tax registration toggle added")
    }
}

