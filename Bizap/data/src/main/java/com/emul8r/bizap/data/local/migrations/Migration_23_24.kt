package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration 23 → 24: Fix monetary type inconsistencies (Double → Long cents)
 *
 * **CRITICAL FIX**: Several entity classes were using Double for monetary amounts,
 * while the core Invoice system uses Long (cents). This caused type mismatch errors
 * when saving invoices (error: "f != java.lang.Long").
 *
 * **Tables Changed**:
 * - invoice_payments: amountPaid Double → Long
 * - invoice_payment_snapshots: totalAmount, paidAmount, outstandingAmount, lastPaymentAmount Double → Long
 * - daily_payment_snapshots: All monetary amounts Double → Long
 * - collection_metrics: All monetary amounts Double → Long
 *
 * **Data Conversion**: All existing monetary values are multiplied by 100 during migration
 * (e.g., 149.99 → 14999 cents). If there are no records, migration is a no-op.
 */
val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // ================== FIX 1: invoice_payments ==================
        // SQLite doesn't support ALTER COLUMN type, so we:
        // 1. Create new column with correct type
        // 2. Copy data with conversion
        // 3. Drop old column
        // 4. Rename new column

        // Check if invoice_payments table exists and has amountPaid as REAL
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS invoice_payments_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                invoiceId INTEGER NOT NULL,
                amountPaid INTEGER NOT NULL,
                paymentDate INTEGER NOT NULL,
                paymentMethod TEXT NOT NULL,
                transactionReference TEXT NOT NULL,
                notes TEXT,
                createdAtMs INTEGER NOT NULL,
                updatedAtMs INTEGER NOT NULL,
                FOREIGN KEY(invoiceId) REFERENCES invoices(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """)

        // Copy data from old table to new table, converting Double to Long (cents)
        database.execSQL("""
            INSERT INTO invoice_payments_new 
            SELECT 
                id, 
                invoiceId, 
                CAST(amountPaid * 100 AS INTEGER) as amountPaid,
                paymentDate, 
                paymentMethod, 
                transactionReference, 
                notes, 
                createdAtMs, 
                updatedAtMs
            FROM invoice_payments
        """)

        // Drop old table and rename new one
        database.execSQL("DROP TABLE IF EXISTS invoice_payments")
        database.execSQL("ALTER TABLE invoice_payments_new RENAME TO invoice_payments")

        // Recreate index
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_invoice_payments_invoiceId 
            ON invoice_payments(invoiceId)
        """)

        // ================== FIX 2: invoice_payment_snapshots ==================
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS invoice_payment_snapshots_new (
                invoiceId INTEGER PRIMARY KEY NOT NULL,
                businessProfileId INTEGER NOT NULL,
                customerId INTEGER NOT NULL,
                customerName TEXT NOT NULL,
                invoiceNumber TEXT NOT NULL,
                invoiceDate INTEGER NOT NULL,
                dueDate INTEGER NOT NULL,
                totalAmount INTEGER NOT NULL,
                paidAmount INTEGER NOT NULL,
                outstandingAmount INTEGER NOT NULL,
                paymentStatus TEXT NOT NULL,
                ageingBucket TEXT NOT NULL,
                daysOverdue INTEGER NOT NULL,
                daysSinceDue INTEGER NOT NULL,
                lastPaymentDate INTEGER,
                lastPaymentAmount INTEGER NOT NULL,
                paymentCount INTEGER NOT NULL,
                isAtRisk INTEGER NOT NULL,
                riskScore REAL NOT NULL,
                riskFactors TEXT NOT NULL,
                lastUpdatedMs INTEGER NOT NULL,
                snapshotDateMs INTEGER NOT NULL,
                FOREIGN KEY(invoiceId) REFERENCES invoices(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(customerId) REFERENCES customers(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
        """)

        database.execSQL("""
            INSERT INTO invoice_payment_snapshots_new 
            SELECT 
                invoiceId,
                businessProfileId,
                customerId,
                customerName,
                invoiceNumber,
                invoiceDate,
                dueDate,
                CAST(totalAmount * 100 AS INTEGER),
                CAST(paidAmount * 100 AS INTEGER),
                CAST(outstandingAmount * 100 AS INTEGER),
                paymentStatus,
                ageingBucket,
                daysOverdue,
                daysSinceDue,
                lastPaymentDate,
                CAST(lastPaymentAmount * 100 AS INTEGER),
                paymentCount,
                CASE WHEN isAtRisk THEN 1 ELSE 0 END,
                riskScore,
                riskFactors,
                lastUpdatedMs,
                snapshotDateMs
            FROM invoice_payment_snapshots
        """)

        database.execSQL("DROP TABLE IF EXISTS invoice_payment_snapshots")
        database.execSQL("ALTER TABLE invoice_payment_snapshots_new RENAME TO invoice_payment_snapshots")

        // Recreate indices
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_invoice_payment_snapshots_businessProfileId 
            ON invoice_payment_snapshots(businessProfileId)
        """)
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_invoice_payment_snapshots_customerId 
            ON invoice_payment_snapshots(customerId)
        """)
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_invoice_payment_snapshots_paymentStatus 
            ON invoice_payment_snapshots(paymentStatus)
        """)
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_invoice_payment_snapshots_ageingBucket 
            ON invoice_payment_snapshots(ageingBucket)
        """)
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_invoice_payment_snapshots_isAtRisk 
            ON invoice_payment_snapshots(isAtRisk)
        """)

        // ================== FIX 3: daily_payment_snapshots ==================
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS daily_payment_snapshots_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                businessProfileId INTEGER NOT NULL,
                snapshotDate INTEGER NOT NULL,
                paymentsReceivedCount INTEGER NOT NULL,
                paymentsReceivedAmount INTEGER NOT NULL,
                invoicesDueCount INTEGER NOT NULL,
                invoicesDueAmount INTEGER NOT NULL,
                invoicesOverdueCount INTEGER NOT NULL,
                invoicesOverdueAmount INTEGER NOT NULL,
                outstandingCurrent INTEGER NOT NULL,
                outstandingPast30 INTEGER NOT NULL,
                outstandingPast60 INTEGER NOT NULL,
                outstandingPast90 INTEGER NOT NULL,
                collectionRate REAL NOT NULL,
                averagePaymentTime REAL NOT NULL,
                projectedMonthlyRevenue INTEGER NOT NULL,
                createdAtMs INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            INSERT INTO daily_payment_snapshots_new 
            SELECT 
                id,
                businessProfileId,
                snapshotDate,
                paymentsReceivedCount,
                CAST(paymentsReceivedAmount * 100 AS INTEGER),
                invoicesDueCount,
                CAST(invoicesDueAmount * 100 AS INTEGER),
                invoicesOverdueCount,
                CAST(invoicesOverdueAmount * 100 AS INTEGER),
                CAST(outstandingCurrent * 100 AS INTEGER),
                CAST(outstandingPast30 * 100 AS INTEGER),
                CAST(outstandingPast60 * 100 AS INTEGER),
                CAST(outstandingPast90 * 100 AS INTEGER),
                collectionRate,
                averagePaymentTime,
                CAST(projectedMonthlyRevenue * 100 AS INTEGER),
                createdAtMs
            FROM daily_payment_snapshots
        """)

        database.execSQL("DROP TABLE IF EXISTS daily_payment_snapshots")
        database.execSQL("ALTER TABLE daily_payment_snapshots_new RENAME TO daily_payment_snapshots")

        // Recreate indices
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_daily_payment_snapshots_businessProfileId 
            ON daily_payment_snapshots(businessProfileId)
        """)
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_daily_payment_snapshots_snapshotDate 
            ON daily_payment_snapshots(snapshotDate)
        """)

        // ================== FIX 4: collection_metrics ==================
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS collection_metrics_new (
                businessProfileId INTEGER PRIMARY KEY NOT NULL,
                metricsDate INTEGER NOT NULL,
                totalInvoicesIssued INTEGER NOT NULL,
                totalInvoiceAmount INTEGER NOT NULL,
                totalPaidAmount INTEGER NOT NULL,
                totalOutstandingAmount INTEGER NOT NULL,
                collectionRate REAL NOT NULL,
                ageingCurrent INTEGER NOT NULL,
                ageingPast30 INTEGER NOT NULL,
                ageingPast60 INTEGER NOT NULL,
                ageingPast90 INTEGER NOT NULL,
                averageDaysToPayment REAL NOT NULL,
                medianDaysToPayment REAL NOT NULL,
                overdueInvoiceCount INTEGER NOT NULL,
                overdueAmount INTEGER NOT NULL,
                collectionRateTrend REAL NOT NULL,
                overdueTrend REAL NOT NULL,
                projectedCollectionRate30Days REAL NOT NULL,
                projectedOutstanding30Days INTEGER NOT NULL,
                lastUpdatedMs INTEGER NOT NULL
            )
        """)

        database.execSQL("""
            INSERT INTO collection_metrics_new 
            SELECT 
                businessProfileId,
                metricsDate,
                totalInvoicesIssued,
                CAST(totalInvoiceAmount * 100 AS INTEGER),
                CAST(totalPaidAmount * 100 AS INTEGER),
                CAST(totalOutstandingAmount * 100 AS INTEGER),
                collectionRate,
                CAST(ageingCurrent * 100 AS INTEGER),
                CAST(ageingPast30 * 100 AS INTEGER),
                CAST(ageingPast60 * 100 AS INTEGER),
                CAST(ageingPast90 * 100 AS INTEGER),
                averageDaysToPayment,
                medianDaysToPayment,
                overdueInvoiceCount,
                CAST(overdueAmount * 100 AS INTEGER),
                collectionRateTrend,
                overdueTrend,
                projectedCollectionRate30Days,
                CAST(projectedOutstanding30Days * 100 AS INTEGER),
                lastUpdatedMs
            FROM collection_metrics
        """)

        database.execSQL("DROP TABLE IF EXISTS collection_metrics")
        database.execSQL("ALTER TABLE collection_metrics_new RENAME TO collection_metrics")
    }
}

