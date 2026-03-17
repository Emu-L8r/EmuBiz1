package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_25_26
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Migration 25 → 26: Fix schema mismatch from previous migration.
 *
 * Purpose: The previous migration (24→25) created indexes with wrong names.
 * This migration corrects index naming to match the entity definitions exactly,
 * ensuring schema alignment and preventing Room validation errors.
 *
 * Changes:
 * - Drops any incorrectly named customer email index (idx_customers_email_unique)
 * - Recreates all required indexes with canonical names
 *
 * Data Impact: None — only index metadata is changed, no row data changes.
 */
@RunWith(AndroidJUnit4::class)
class Migration25To26Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration25To26_correctIndexNamesExist() {
        // 1. Create database at version 25
        val db = helper.createDatabase(TEST_DB, 25)
        db.close()

        // 2. Run migration 25 → 26
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 26, true, MIGRATION_25_26
        )

        // 3. Verify all correct indexes exist
        val existingIndexes = getExistingIndexes(migratedDb)
        val expectedIndexes = listOf(
            "idx_customers_business",
            "idx_customers_email",
            "idx_customers_business_name",
            "idx_invoices_business",
            "idx_invoices_customer",
            "idx_invoices_status",
            "idx_invoices_business_status",
            "idx_invoices_year_sequence",
            "idx_invoices_date",
            "idx_invoices_customer_date",
            "idx_line_items_currency_code"
        )
        expectedIndexes.forEach { indexName ->
            assertTrue(
                indexName in existingIndexes,
                "Expected index '$indexName' to exist after migration"
            )
        }

        migratedDb.close()
    }

    @Test
    fun migration25To26_incorrectIndexNameDropped() {
        // 1. Create database at version 25 and manually add the wrong-named index
        val db = helper.createDatabase(TEST_DB + "_wrong", 25)
        // Simulate the wrong index from migration 24→25
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS idx_customers_email_unique ON customers(email)"
        )
        db.close()

        // 2. Run migration 25 → 26
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_wrong", 26, true, MIGRATION_25_26
        )

        // 3. Verify wrong-named index is gone
        val existingIndexes = getExistingIndexes(migratedDb)
        assertFalse(
            "idx_customers_email_unique" in existingIndexes,
            "Wrong-named index 'idx_customers_email_unique' should have been dropped"
        )
        // Correct name should still be present
        assertTrue(
            "idx_customers_email" in existingIndexes,
            "Correct index 'idx_customers_email' should exist"
        )

        migratedDb.close()
    }

    @Test
    fun migration25To26_dataIntegrityPreserved() {
        // 1. Create database at version 25 with test data
        val db = helper.createDatabase(TEST_DB + "_data", 25)
        db.execSQL(
            """
            INSERT INTO invoices (
                id, businessProfileId, customerId, customerName, customerAddress,
                customerEmail, date, totalAmount, isQuote, status, header, subheader,
                notes, footer, photoUris, currencyCode, subtotalAmount, taxAmount,
                taxRate, dueDate, invoiceNumber, invoiceYear, invoiceSequence,
                amountPaid, updatedAt, createdAt
            ) VALUES (
                1, 1, 1, 'Data Corp', '456 Oak Ave',
                'data@corp.com', 1700000000000, 50000, 0, 'DRAFT', '', '',
                '', '', '[]', 'AUD', 45000, 5000,
                0.1, 1702592000000, 'INV-002', 2024, 2,
                0, 1700000000000, 1700000000000
            )
            """.trimIndent()
        )
        db.close()

        // 2. Run migration 25 → 26
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_data", 26, true, MIGRATION_25_26
        )

        // 3. Verify data is preserved
        val cursor = migratedDb.query("SELECT customerName FROM invoices WHERE id = 1")
        cursor.moveToFirst()
        assertTrue(cursor.count == 1, "Invoice data should be preserved after migration")
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
        private const val TEST_DB = "migration_25_26_test.db"
    }
}
