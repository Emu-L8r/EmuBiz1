package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_27_28
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Migration 27 → 28: Backfill analytics snapshots from existing invoices.
 *
 * Purpose: Populate three snapshot tables with data derived from the invoices table.
 * Existing invoices were created before snapshot sync logic was implemented, so this
 * migration ensures dashboards show correct historical data.
 *
 * Tables populated:
 * - invoice_analytics_snapshots: Financial and status data for each invoice
 * - daily_revenue_snapshots: Aggregated daily revenue per business/date
 * - invoice_payment_snapshots: Payment status and aging data for each invoice
 *
 * Data Impact: Creates snapshot records from all invoices that don't already have one.
 * Existing snapshots are left unchanged (WHERE NOT EXISTS guard).
 */
@RunWith(AndroidJUnit4::class)
class Migration27To28Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration27To28_invoiceAnalyticsSnapshotsBackfilled() {
        // 1. Create database at version 27 with invoice data
        val db = helper.createDatabase(TEST_DB, 27)
        // Insert a PAID invoice
        db.execSQL(
            """
            INSERT INTO invoices (
                id, businessProfileId, customerId, customerName, customerAddress,
                customerEmail, date, totalAmount, isQuote, status, header, subheader,
                notes, footer, photoUris, pdfUri, dueDate, taxRate, taxAmount,
                companyLogoPath, updatedAt, amountPaid, parentInvoiceId, version,
                invoiceYear, invoiceSequence, currencyCode, templateId, templateSnapshot,
                customFieldValues
            ) VALUES (
                1, 1, 10, 'Acme Corp', '123 Business St',
                'billing@acme.com', 1700000000000, 10000, 0, 'PAID', NULL, NULL,
                NULL, NULL, NULL, NULL, 1702592000000, 0.1, 1000,
                NULL, 1700001000000, 10000, NULL, 1,
                2024, 1, 'AUD', NULL, NULL,
                NULL
            )
            """.trimIndent()
        )
        // Insert a SENT invoice (unpaid)
        db.execSQL(
            """
            INSERT INTO invoices (
                id, businessProfileId, customerId, customerName, customerAddress,
                customerEmail, date, totalAmount, isQuote, status, header, subheader,
                notes, footer, photoUris, pdfUri, dueDate, taxRate, taxAmount,
                companyLogoPath, updatedAt, amountPaid, parentInvoiceId, version,
                invoiceYear, invoiceSequence, currencyCode, templateId, templateSnapshot,
                customFieldValues
            ) VALUES (
                2, 1, 11, 'Beta Ltd', '456 Market Ave',
                'accounts@beta.com', 1700000000000, 25000, 0, 'SENT', NULL, NULL,
                NULL, NULL, NULL, NULL, 1702592000000, 0.1, 2500,
                NULL, 1700000000000, 0, NULL, 1,
                2024, 2, 'AUD', NULL, NULL,
                NULL
            )
            """.trimIndent()
        )

        // Verify no snapshots exist before migration
        val snapshotsBefore = db.query("SELECT COUNT(*) FROM invoice_analytics_snapshots")
        snapshotsBefore.moveToFirst()
        assertEquals(0, snapshotsBefore.getInt(0), "No snapshots should exist before migration")
        snapshotsBefore.close()
        db.close()

        // 2. Run migration 27 → 28
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 28, true, MIGRATION_27_28
        )

        // 3. Verify invoice_analytics_snapshots were created
        val snapshotsAfter = migratedDb.query(
            "SELECT invoiceId, status, isPaid FROM invoice_analytics_snapshots ORDER BY invoiceId"
        )
        assertEquals(2, snapshotsAfter.count, "Should have 2 invoice analytics snapshots")

        snapshotsAfter.moveToFirst()
        assertEquals(1L, snapshotsAfter.getLong(snapshotsAfter.getColumnIndexOrThrow("invoiceId")))
        assertEquals("PAID", snapshotsAfter.getString(snapshotsAfter.getColumnIndexOrThrow("status")))
        assertEquals(1, snapshotsAfter.getInt(snapshotsAfter.getColumnIndexOrThrow("isPaid")))

        snapshotsAfter.moveToNext()
        assertEquals(2L, snapshotsAfter.getLong(snapshotsAfter.getColumnIndexOrThrow("invoiceId")))
        assertEquals("SENT", snapshotsAfter.getString(snapshotsAfter.getColumnIndexOrThrow("status")))
        assertEquals(0, snapshotsAfter.getInt(snapshotsAfter.getColumnIndexOrThrow("isPaid")))

        snapshotsAfter.close()
        migratedDb.close()
    }

    @Test
    fun migration27To28_dailyRevenueSnapshotsBackfilled() {
        // 1. Create database at version 27 with invoice data
        val db = helper.createDatabase(TEST_DB + "_revenue", 27)
        // Insert invoices from the same day
        db.execSQL(
            """
            INSERT INTO invoices (
                id, businessProfileId, customerId, customerName, customerAddress,
                customerEmail, date, totalAmount, isQuote, status, header, subheader,
                notes, footer, photoUris, pdfUri, dueDate, taxRate, taxAmount,
                companyLogoPath, updatedAt, amountPaid, parentInvoiceId, version,
                invoiceYear, invoiceSequence, currencyCode, templateId, templateSnapshot,
                customFieldValues
            ) VALUES (
                3, 1, 10, 'Revenue Corp', '789 Revenue Rd',
                NULL, 1705276800000, 50000, 0, 'PAID', NULL, NULL,
                NULL, NULL, NULL, NULL, 1707868800000, 0.1, 5000,
                NULL, 1705276800000, 50000, NULL, 1,
                2024, 3, 'AUD', NULL, NULL,
                NULL
            )
            """.trimIndent()
        )
        db.close()

        // 2. Run migration 27 → 28
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_revenue", 28, true, MIGRATION_27_28
        )

        // 3. Verify daily_revenue_snapshots were created
        val revenueCursor = migratedDb.query(
            "SELECT COUNT(*) FROM daily_revenue_snapshots WHERE businessProfileId = 1"
        )
        revenueCursor.moveToFirst()
        assertTrue(revenueCursor.getInt(0) >= 1,
            "Should have at least 1 daily revenue snapshot after migration")
        revenueCursor.close()
        migratedDb.close()
    }

    @Test
    fun migration27To28_invoicePaymentSnapshotsBackfilled() {
        // 1. Create database at version 27 with invoice data
        val db = helper.createDatabase(TEST_DB + "_payments", 27)
        db.execSQL(
            """
            INSERT INTO invoices (
                id, businessProfileId, customerId, customerName, customerAddress,
                customerEmail, date, totalAmount, isQuote, status, header, subheader,
                notes, footer, photoUris, pdfUri, dueDate, taxRate, taxAmount,
                companyLogoPath, updatedAt, amountPaid, parentInvoiceId, version,
                invoiceYear, invoiceSequence, currencyCode, templateId, templateSnapshot,
                customFieldValues
            ) VALUES (
                4, 1, 12, 'Payment Co', '101 Payment Blvd',
                NULL, 1700000000000, 30000, 0, 'PARTIALLY_PAID', NULL, NULL,
                NULL, NULL, NULL, NULL, 1702592000000, 0.1, 3000,
                NULL, 1700000000000, 15000, NULL, 1,
                2024, 4, 'AUD', NULL, NULL,
                NULL
            )
            """.trimIndent()
        )
        db.close()

        // 2. Run migration 27 → 28
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_payments", 28, true, MIGRATION_27_28
        )

        // 3. Verify invoice_payment_snapshots were created
        val paymentCursor = migratedDb.query(
            "SELECT invoiceId, paymentStatus, paidAmount FROM invoice_payment_snapshots WHERE invoiceId = 4"
        )
        paymentCursor.moveToFirst()
        assertEquals(1, paymentCursor.count, "Should have 1 invoice payment snapshot")
        assertEquals(4L, paymentCursor.getLong(paymentCursor.getColumnIndexOrThrow("invoiceId")))
        assertEquals(15000L, paymentCursor.getLong(paymentCursor.getColumnIndexOrThrow("paidAmount")))
        paymentCursor.close()
        migratedDb.close()
    }

    @Test
    fun migration27To28_existingSnapshotsNotOverwritten() {
        // 1. Create database at version 27 with an invoice AND an existing snapshot
        val db = helper.createDatabase(TEST_DB + "_existing", 27)
        db.execSQL(
            """
            INSERT INTO invoices (
                id, businessProfileId, customerId, customerName, customerAddress,
                customerEmail, date, totalAmount, isQuote, status, header, subheader,
                notes, footer, photoUris, pdfUri, dueDate, taxRate, taxAmount,
                companyLogoPath, updatedAt, amountPaid, parentInvoiceId, version,
                invoiceYear, invoiceSequence, currencyCode, templateId, templateSnapshot,
                customFieldValues
            ) VALUES (
                5, 1, 13, 'Existing Corp', '202 Snapshot St',
                NULL, 1700000000000, 20000, 0, 'SENT', NULL, NULL,
                NULL, NULL, NULL, NULL, 1702592000000, 0.1, 2000,
                NULL, 1700000000000, 0, NULL, 1,
                2024, 5, 'AUD', NULL, NULL,
                NULL
            )
            """.trimIndent()
        )
        // Pre-insert a snapshot to verify it's not overwritten
        db.execSQL(
            """
            INSERT INTO invoice_analytics_snapshots (
                invoiceId, businessProfileId, customerId, customerName, invoiceNumber,
                currencyCode, subtotal, taxAmount, totalAmount, status, isPaid,
                isOverdue, invoiceDateMs, createdAtMs, paidAtMs, daysPending,
                lineItemCount, snapshotCreatedAtMs
            ) VALUES (
                5, 1, 13, 'Existing Corp', 'INV-005',
                'AUD', 18000, 2000, 20000, 'SENT', 0,
                0, 1700000000000, 1700000000000, NULL, 0,
                0, 1699999000000
            )
            """.trimIndent()
        )
        db.close()

        // 2. Run migration 27 → 28
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_existing", 28, true, MIGRATION_27_28
        )

        // 3. Verify existing snapshot was NOT overwritten (snapshotCreatedAtMs is still old)
        val cursor = migratedDb.query(
            "SELECT snapshotCreatedAtMs FROM invoice_analytics_snapshots WHERE invoiceId = 5"
        )
        cursor.moveToFirst()
        assertEquals(1, cursor.count, "Should still have exactly 1 snapshot for invoice 5")
        // The old snapshot should be preserved (WHERE NOT EXISTS guard)
        assertEquals(1699999000000L, cursor.getLong(0),
            "Existing snapshot should not be overwritten by migration")
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration27To28_emptyDatabase_noErrors() {
        // 1. Create empty database at version 27
        val db = helper.createDatabase(TEST_DB + "_empty", 27)
        db.close()

        // 2. Run migration 27 → 28 on empty database
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_empty", 28, true, MIGRATION_27_28
        )

        // 3. Verify all snapshot tables are still empty (no invoices to backfill from)
        val analyticsCount = migratedDb.query("SELECT COUNT(*) FROM invoice_analytics_snapshots")
        analyticsCount.moveToFirst()
        assertEquals(0, analyticsCount.getInt(0),
            "Analytics snapshots should be empty when no invoices exist")
        analyticsCount.close()
        migratedDb.close()
    }

    companion object {
        private const val TEST_DB = "migration_27_28_test.db"
    }
}
