package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_24_25
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Migration 24 → 25: Add performance indexes to invoices, customers, and line_items.
 *
 * Purpose: Speed up frequently queried columns by eliminating full table scans as the
 * data set grows. All indexes use IF NOT EXISTS for idempotency.
 *
 * New Indexes:
 * - invoices: businessProfileId, customerId, status, (businessProfileId, status),
 *             (invoiceYear, invoiceSequence, businessProfileId), date, (customerId, date)
 * - customers: businessProfileId, email (UNIQUE), (businessProfileId, name)
 * - line_items: currencyCode
 *
 * Data Impact: None — indexes are metadata only, no row data changes.
 */
@RunWith(AndroidJUnit4::class)
class Migration24To25Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration24To25_invoiceIndexesCreated() {
        // 1. Create database at version 24
        val db = helper.createDatabase(TEST_DB, 24)
        db.close()

        // 2. Run migration 24 → 25
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 25, true, MIGRATION_24_25
        )

        // 3. Verify invoice indexes exist
        val expectedInvoiceIndexes = listOf(
            "idx_invoices_business",
            "idx_invoices_customer",
            "idx_invoices_status",
            "idx_invoices_business_status",
            "idx_invoices_year_sequence",
            "idx_invoices_date",
            "idx_invoices_customer_date"
        )
        val existingIndexes = getExistingIndexes(migratedDb)
        expectedInvoiceIndexes.forEach { indexName ->
            assertTrue(
                indexName in existingIndexes,
                "Expected index '$indexName' to exist after migration"
            )
        }
        migratedDb.close()
    }

    @Test
    fun migration24To25_customerIndexesCreated() {
        // 1. Create database at version 24
        val db = helper.createDatabase(TEST_DB + "_customers", 24)
        db.close()

        // 2. Run migration 24 → 25
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_customers", 25, true, MIGRATION_24_25
        )

        // 3. Verify customer indexes exist
        val expectedCustomerIndexes = listOf(
            "idx_customers_business",
            "idx_customers_email",
            "idx_customers_business_name"
        )
        val existingIndexes = getExistingIndexes(migratedDb)
        expectedCustomerIndexes.forEach { indexName ->
            assertTrue(
                indexName in existingIndexes,
                "Expected index '$indexName' to exist after migration"
            )
        }
        migratedDb.close()
    }

    @Test
    fun migration24To25_lineItemIndexCreated() {
        // 1. Create database at version 24
        val db = helper.createDatabase(TEST_DB + "_lineitems", 24)
        db.close()

        // 2. Run migration 24 → 25
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_lineitems", 25, true, MIGRATION_24_25
        )

        // 3. Verify line_items currency index exists
        val existingIndexes = getExistingIndexes(migratedDb)
        assertTrue(
            "idx_line_items_currency_code" in existingIndexes,
            "Expected index 'idx_line_items_currency_code' to exist after migration"
        )
        migratedDb.close()
    }

    @Test
    fun migration24To25_existingDataPreserved() {
        // 1. Create database at version 24 with data
        val db = helper.createDatabase(TEST_DB + "_data", 24)
        db.execSQL(
            """
            INSERT INTO invoices (
                id, businessProfileId, customerId, customerName, customerAddress,
                customerEmail, date, totalAmount, isQuote, status, header, subheader,
                notes, footer, photoUris, currencyCode, subtotalAmount, taxAmount,
                taxRate, dueDate, invoiceNumber, invoiceYear, invoiceSequence,
                amountPaid, updatedAt, createdAt
            ) VALUES (
                1, 1, 1, 'Test Corp', '123 Main St',
                'test@corp.com', 1700000000000, 10000, 0, 'SENT', '', '',
                '', '', '[]', 'AUD', 9000, 1000,
                0.1, 1702592000000, 'INV-001', 2024, 1,
                0, 1700000000000, 1700000000000
            )
            """.trimIndent()
        )
        db.close()

        // 2. Run migration 24 → 25
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_data", 25, true, MIGRATION_24_25
        )

        // 3. Verify data is still present
        val cursor = migratedDb.query("SELECT COUNT(*) FROM invoices")
        cursor.moveToFirst()
        assertTrue(cursor.getInt(0) == 1, "Invoice data should be preserved after adding indexes")
        cursor.close()
        migratedDb.close()
    }

    private fun getExistingIndexes(db: androidx.sqlite.db.SupportSQLiteDatabase): Set<String> {
        val indexes = mutableSetOf<String>()
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='index'")
        while (cursor.moveToNext()) {
            indexes.add(cursor.getString(0))
        }
        cursor.close()
        return indexes
    }

    companion object {
        private const val TEST_DB = "migration_24_25_test.db"
    }
}
