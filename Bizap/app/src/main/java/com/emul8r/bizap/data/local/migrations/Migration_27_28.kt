package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

/**
 * Migration 27 → 28: Backfill all analytics snapshots for existing invoices.
 *
 * This migration populates three snapshot tables with data from existing invoices:
 * - invoice_analytics_snapshots: Financial and status data for each invoice
 * - daily_revenue_snapshots: Daily aggregated revenue
 * - invoice_payment_snapshots: Payment status and aging data
 *
 * CRITICAL: This is PATHWAY 1 - The immediate fix for stale snapshot data.
 * Existing invoices were created before snapshot sync logic was implemented,
 * so their snapshots are stale or missing. This backfill ensures dashboards
 * show correct historical data.
 */
val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(database: SupportSQLiteDatabase) {
        Timber.i("🔄 Starting Migration 27→28: Backfill analytics snapshots")

        try {
            // ══════════════════════════════════════════════════════════════════════════
            // STEP 1: Backfill invoice_analytics_snapshots
            // ══════════════════════════════════════════════════════════════════════════
            Timber.d("📸 Step 1: Backfilling invoice_analytics_snapshots")

            database.execSQL("""
                INSERT OR REPLACE INTO invoice_analytics_snapshots (
                    invoiceId,
                    businessProfileId,
                    customerId,
                    customerName,
                    invoiceNumber,
                    currencyCode,
                    subtotal,
                    taxAmount,
                    totalAmount,
                    status,
                    isPaid,
                    isOverdue,
                    invoiceDateMs,
                    createdAtMs,
                    paidAtMs,
                    daysPending,
                    lineItemCount,
                    snapshotCreatedAtMs
                )
                SELECT
                    i.id,
                    i.businessProfileId,
                    i.customerId,
                    i.customerName,
                    'INV-' || i.invoiceYear || '-' || PRINTF('%04d', i.invoiceSequence),
                    i.currencyCode,
                    (i.totalAmount - i.taxAmount),
                    i.taxAmount,
                    i.totalAmount,
                    i.status,
                    CASE WHEN i.status IN ('PAID', 'PARTIALLY_PAID') THEN 1 ELSE 0 END,
                    CASE 
                        WHEN i.dueDate < (strftime('%s', 'now') * 1000) 
                        AND i.status NOT IN ('PAID', 'CANCELLED') THEN 1 
                        ELSE 0 
                    END,
                    i.date,
                    i.updatedAt,
                    CASE WHEN i.status = 'PAID' THEN i.updatedAt ELSE NULL END,
                    CASE 
                        WHEN i.status IN ('SENT', 'PARTIALLY_PAID')
                        THEN CAST(((strftime('%s', 'now') * 1000) - i.date) / 86400000 AS INTEGER)
                        ELSE 0
                    END,
                    COALESCE((SELECT COUNT(*) FROM line_items WHERE invoiceId = i.id), 0),
                    (strftime('%s', 'now') * 1000)
                FROM invoices i
                WHERE NOT EXISTS (
                    SELECT 1 FROM invoice_analytics_snapshots ias 
                    WHERE ias.invoiceId = i.id
                )
            """)

            Timber.d("✅ Backfilled invoice_analytics_snapshots")

            // ══════════════════════════════════════════════════════════════════════════
            // STEP 2: Backfill daily_revenue_snapshots
            // ══════════════════════════════════════════════════════════════════════════
            Timber.d("📊 Step 2: Backfilling daily_revenue_snapshots")

            database.execSQL("""
                INSERT OR REPLACE INTO daily_revenue_snapshots (
                    businessProfileId,
                    dateString,
                    dateMs,
                    totalRevenue,
                    invoiceCount,
                    paidInvoiceCount,
                    draftInvoiceCount,
                    averageInvoiceAmount,
                    currencyBreakdown,
                    dayOverDayGrowth,
                    weekOverWeekGrowth,
                    snapshotCreatedAtMs
                )
                SELECT
                    i.businessProfileId,
                    DATE(i.date / 1000, 'unixepoch'),
                    i.date,
                    SUM(CASE 
                        WHEN i.status IN ('PAID', 'PARTIALLY_PAID') 
                        THEN i.amountPaid 
                        ELSE 0 
                    END),
                    COUNT(*),
                    SUM(CASE WHEN i.status = 'PAID' THEN 1 ELSE 0 END),
                    SUM(CASE WHEN i.status = 'DRAFT' THEN 1 ELSE 0 END),
                    CASE 
                        WHEN COUNT(*) > 0 
                        THEN CAST(AVG(i.totalAmount) AS INTEGER)
                        ELSE 0
                    END,
                    '{"' || i.currencyCode || '": ' || 
                    SUM(CASE 
                        WHEN i.status IN ('PAID', 'PARTIALLY_PAID') 
                        THEN i.amountPaid 
                        ELSE 0 
                    END) || '}',
                    0.0,
                    0.0,
                    (strftime('%s', 'now') * 1000)
                FROM invoices i
                GROUP BY i.businessProfileId, DATE(i.date / 1000, 'unixepoch'), i.currencyCode
            """)

            Timber.d("✅ Backfilled daily_revenue_snapshots")

            // ══════════════════════════════════════════════════════════════════════════
            // STEP 3: Backfill invoice_payment_snapshots
            // ══════════════════════════════════════════════════════════════════════════
            Timber.d("💰 Step 3: Backfilling invoice_payment_snapshots")

            database.execSQL("""
                INSERT OR REPLACE INTO invoice_payment_snapshots (
                    invoiceId,
                    businessProfileId,
                    customerId,
                    customerName,
                    invoiceNumber,
                    invoiceDate,
                    dueDate,
                    totalAmount,
                    paidAmount,
                    outstandingAmount,
                    paymentStatus,
                    ageingBucket,
                    daysOverdue,
                    daysSinceDue,
                    lastPaymentDate,
                    lastPaymentAmount,
                    paymentCount,
                    isAtRisk,
                    riskScore,
                    riskFactors,
                    lastUpdatedMs,
                    snapshotDateMs
                )
                SELECT
                    i.id,
                    i.businessProfileId,
                    i.customerId,
                    i.customerName,
                    'INV-' || i.invoiceYear || '-' || PRINTF('%04d', i.invoiceSequence),
                    i.date,
                    i.dueDate,
                    i.totalAmount,
                    i.amountPaid,
                    (i.totalAmount - i.amountPaid),
                    CASE 
                        WHEN i.status = 'PAID' THEN 'PAID'
                        WHEN i.status = 'PARTIALLY_PAID' THEN 'PARTIALLY_PAID'
                        WHEN i.status = 'SENT' THEN 'UNPAID'
                        WHEN i.status = 'OVERDUE' THEN 'OVERDUE'
                        ELSE 'UNPAID'
                    END,
                    CASE 
                        WHEN ((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 <= 0 THEN 'CURRENT'
                        WHEN ((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 <= 30 THEN 'PAST_30'
                        WHEN ((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 <= 60 THEN 'PAST_60'
                        ELSE 'PAST_90'
                    END,
                    CASE 
                        WHEN i.dueDate < (strftime('%s', 'now') * 1000)
                        THEN CAST(((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 AS INTEGER)
                        ELSE 0
                    END,
                    CASE 
                        WHEN i.dueDate < (strftime('%s', 'now') * 1000)
                        THEN CAST(((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 AS INTEGER)
                        ELSE 0
                    END,
                    CASE WHEN i.amountPaid > 0 THEN i.updatedAt ELSE NULL END,
                    CASE WHEN i.amountPaid > 0 THEN i.amountPaid ELSE 0 END,
                    CASE WHEN i.amountPaid > 0 THEN 1 ELSE 0 END,
                    CASE 
                        WHEN i.dueDate < (strftime('%s', 'now') * 1000) 
                        AND i.status NOT IN ('PAID', 'CANCELLED') THEN 1 
                        ELSE 0 
                    END,
                    CASE 
                        WHEN ((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 <= 0 THEN 0.0
                        WHEN ((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 <= 30 THEN 0.3
                        WHEN ((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 <= 60 THEN 0.6
                        WHEN ((strftime('%s', 'now') * 1000) - i.dueDate) / 86400000 <= 90 THEN 0.8
                        ELSE 1.0
                    END,
                    '',
                    (strftime('%s', 'now') * 1000),
                    (strftime('%s', 'now') * 1000)
                FROM invoices i
                WHERE NOT EXISTS (
                    SELECT 1 FROM invoice_payment_snapshots ips
                    WHERE ips.invoiceId = i.id
                )
            """)

            Timber.d("✅ Backfilled invoice_payment_snapshots")

            Timber.i("✅ Migration 27→28 COMPLETE - All snapshots backfilled successfully")

        } catch (e: Exception) {
            Timber.e(e, "❌ Migration 27→28 FAILED")
            throw e
        }
    }
}
