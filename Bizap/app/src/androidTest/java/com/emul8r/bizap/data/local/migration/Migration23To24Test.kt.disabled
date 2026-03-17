package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_23_24
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * Migration 23 → 24: Fix monetary type inconsistencies (Double → Long cents).
 *
 * Purpose: Prevent floating-point rounding errors in monetary calculations. All monetary
 * fields are converted from Double (dollars) to Long (cents, i.e., multiply by 100).
 *
 * Tables changed:
 * - invoice_payments: amountPaid
 * - invoice_payment_snapshots: totalAmount, paidAmount, outstandingAmount, lastPaymentAmount
 * - daily_payment_snapshots: all monetary amounts
 * - collection_metrics: all monetary amounts
 *
 * Data Impact: Existing values multiplied by 100 (e.g. 149.99 → 14999).
 */
@RunWith(AndroidJUnit4::class)
class Migration23To24Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration23To24_invoicePayments_amountConvertedToCents() {
        // 1. Create database at version 23 with invoice_payments using REAL (Double) amounts
        val db = helper.createDatabase(TEST_DB, 23)

        // Insert with Double (dollars) values
        db.execSQL(
            """
            INSERT INTO invoice_payments (
                id, invoiceId, amountPaid, paymentDate, paymentMethod,
                transactionReference, notes, createdAtMs, updatedAtMs
            ) VALUES (
                1, 100, 149.99, 1700000000000, 'BANK_TRANSFER',
                'REF-001', NULL, 1700000000000, 1700000000000
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO invoice_payments (
                id, invoiceId, amountPaid, paymentDate, paymentMethod,
                transactionReference, notes, createdAtMs, updatedAtMs
            ) VALUES (
                2, 101, 250.0, 1700000000000, 'CREDIT_CARD',
                'REF-002', NULL, 1700000000000, 1700000000000
            )
            """.trimIndent()
        )
        db.close()

        // 2. Run migration 23 → 24
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 24, true, MIGRATION_23_24
        )

        // 3. Verify amounts converted to cents (Long)
        val cursor = migratedDb.query(
            "SELECT id, amountPaid FROM invoice_payments ORDER BY id"
        )

        cursor.moveToFirst()
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        // 149.99 * 100 = 14999 (truncated to integer)
        assertEquals(14999L, cursor.getLong(cursor.getColumnIndexOrThrow("amountPaid")))

        cursor.moveToNext()
        assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        // 250.0 * 100 = 25000
        assertEquals(25000L, cursor.getLong(cursor.getColumnIndexOrThrow("amountPaid")))

        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration23To24_invoicePaymentSnapshots_allMonetaryFieldsConverted() {
        // 1. Create database at version 23 with invoice_payment_snapshots using REAL amounts
        val db = helper.createDatabase(TEST_DB + "_snapshots", 23)
        db.execSQL(
            """
            INSERT INTO invoice_payment_snapshots (
                invoiceId, businessProfileId, customerId, customerName, invoiceNumber,
                invoiceDate, dueDate, totalAmount, paidAmount, outstandingAmount,
                paymentStatus, ageingBucket, daysOverdue, daysSinceDue,
                lastPaymentDate, lastPaymentAmount, paymentCount, isAtRisk,
                riskScore, riskFactors, lastUpdatedMs, snapshotDateMs
            ) VALUES (
                200, 1, 10, 'Test Customer', 'INV-001',
                1700000000000, 1702592000000, 500.0, 100.0, 400.0,
                'PARTIALLY_PAID', 'CURRENT', 0, 0,
                1700000000000, 100.0, 1, 0,
                0.0, '', 1700000000000, 1700000000000
            )
            """.trimIndent()
        )
        db.close()

        // 2. Run migration 23 → 24
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_snapshots", 24, true, MIGRATION_23_24
        )

        // 3. Verify all monetary amounts converted to cents
        val cursor = migratedDb.query(
            "SELECT totalAmount, paidAmount, outstandingAmount, lastPaymentAmount FROM invoice_payment_snapshots WHERE invoiceId = 200"
        )
        cursor.moveToFirst()
        assertEquals(50000L, cursor.getLong(0), "totalAmount should be 500.0 * 100 = 50000")
        assertEquals(10000L, cursor.getLong(1), "paidAmount should be 100.0 * 100 = 10000")
        assertEquals(40000L, cursor.getLong(2), "outstandingAmount should be 400.0 * 100 = 40000")
        assertEquals(10000L, cursor.getLong(3), "lastPaymentAmount should be 100.0 * 100 = 10000")
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration23To24_emptyTables_noErrors() {
        // 1. Create database at version 23 with no data
        val db = helper.createDatabase(TEST_DB + "_empty", 23)
        db.close()

        // 2. Migration should succeed on empty tables
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_empty", 24, true, MIGRATION_23_24
        )

        // 3. Verify tables still exist and are empty
        val cursor = migratedDb.query("SELECT COUNT(*) FROM invoice_payments")
        cursor.moveToFirst()
        assertEquals(0, cursor.getInt(0), "invoice_payments should be empty")
        cursor.close()
        migratedDb.close()
    }

    companion object {
        private const val TEST_DB = "migration_23_24_test.db"
    }
}
