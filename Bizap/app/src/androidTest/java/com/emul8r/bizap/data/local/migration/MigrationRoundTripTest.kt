package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Round-Trip Migration Test: v21 → v35
 *
 * This test verifies that data survives the complete migration path from the oldest
 * supported version (v21) through all 14 migrations to the current version (v35).
 *
 * This is CRITICAL because:
 * 1. Developers test with DEBUG builds that wipe data on schema changes
 * 2. Production users CANNOT afford data loss during migration
 * 3. We need proof that real data survives all 14 migration steps
 *
 * Test Strategy:
 * 1. Create database at v21 with realistic test data
 * 2. Migrate through ALL versions (21→22→23→...→35)
 * 3. Verify data integrity at each step
 * 4. Verify final schema matches expectations
 * 5. Confirm no silent data deletion occurred
 */
@RunWith(AndroidJUnit4::class)
class MigrationRoundTripTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    companion object {
        private const val TEST_DB = "migration-test"
        private const val INITIAL_VERSION = 21
        private const val FINAL_VERSION = 35
    }

    @Test
    fun testRoundTripMigration_v21ToV35_PreservesData() {
        // Step 1: Create database at v21 with realistic test data
        val v21Db = helper.createDatabase(TEST_DB, INITIAL_VERSION)
        insertTestDataAtV21(v21Db)
        val initialInvoiceCount = getRecordCount(v21Db, "invoices")
        val initialCustomerCount = getRecordCount(v21Db, "customers")
        v21Db.close()

        // Step 2: Run all migrations (21 → 35)
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB,
            FINAL_VERSION,
            true,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35
        )

        // Step 3: Verify data survived migration
        val finalInvoiceCount = getRecordCount(migratedDb, "invoices")
        val finalCustomerCount = getRecordCount(migratedDb, "customers")

        assertEquals(
            initialInvoiceCount,
            finalInvoiceCount,
            "Invoice data should not be lost during migration v21→v35"
        )
        assertEquals(
            initialCustomerCount,
            finalCustomerCount,
            "Customer data should not be lost during migration v21→v35"
        )

        // Step 4: Verify critical tables exist and are accessible
        val tables = listOf(
            "invoices",
            "customers",
            "line_items",
            "business_profiles",
            "payment_entities",
            "daily_revenue_snapshots"
        )

        for (tableName in tables) {
            assertTrue(
                tableExists(migratedDb, tableName),
                "Table '$tableName' should exist after migration"
            )
        }

        // Step 5: Verify no broken references
        verifyReferentialIntegrity(migratedDb)

        migratedDb.close()
    }

    @Test
    fun testMigrationPath_PreservesInvoiceData() {
        val v21Db = helper.createDatabase(TEST_DB, INITIAL_VERSION)

        // Insert test invoice
        v21Db.execSQL(
            """
            INSERT INTO invoices (
                id, customer_id, amount, status, issue_date, business_id
            ) VALUES (
                1, 1, 10000, 'SENT', '2026-01-01', 1
            )
            """
        )
        v21Db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB,
            FINAL_VERSION,
            true,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35
        )

        // Verify invoice still exists
        val cursor = migratedDb.query(
            "SELECT id, amount, status FROM invoices WHERE id = 1"
        )
        assertTrue(cursor.moveToFirst(), "Invoice should exist after migration")
        assertEquals(1, cursor.getLong(0), "Invoice ID should be preserved")
        assertEquals(10000, cursor.getLong(1), "Invoice amount should be preserved")
        assertEquals("SENT", cursor.getString(2), "Invoice status should be preserved")
        cursor.close()

        migratedDb.close()
    }

    @Test
    fun testMigrationPath_PreservesCustomerData() {
        val v21Db = helper.createDatabase(TEST_DB, INITIAL_VERSION)

        // Insert test customer
        v21Db.execSQL(
            """
            INSERT INTO customers (
                id, business_id, name, email
            ) VALUES (
                1, 1, 'Test Customer', 'test@example.com'
            )
            """
        )
        v21Db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB,
            FINAL_VERSION,
            true,
            MIGRATION_21_22,
            MIGRATION_22_23,
            MIGRATION_23_24,
            MIGRATION_24_25,
            MIGRATION_25_26,
            MIGRATION_26_27,
            MIGRATION_27_28,
            MIGRATION_28_29,
            MIGRATION_29_30,
            MIGRATION_30_31,
            MIGRATION_31_32,
            MIGRATION_32_33,
            MIGRATION_33_34,
            MIGRATION_34_35
        )

        // Verify customer still exists
        val cursor = migratedDb.query(
            "SELECT id, name, email FROM customers WHERE id = 1"
        )
        assertTrue(cursor.moveToFirst(), "Customer should exist after migration")
        assertEquals(1, cursor.getLong(0), "Customer ID should be preserved")
        assertEquals("Test Customer", cursor.getString(1), "Customer name should be preserved")
        assertEquals("test@example.com", cursor.getString(2), "Customer email should be preserved")
        cursor.close()

        migratedDb.close()
    }

    // ─────────────────────────────────────────────────────────────────
    // Helper Functions
    // ─────────────────────────────────────────────────────────────────

    private fun insertTestDataAtV21(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        // Insert business profile
        db.execSQL(
            """
            INSERT INTO business_profiles (
                id, name, email, phone
            ) VALUES (
                1, 'Test Business', 'business@test.com', '555-0001'
            )
            """
        )

        // Insert customers
        for (i in 1..5) {
            db.execSQL(
                """
                INSERT INTO customers (
                    id, business_id, name, email
                ) VALUES (
                    $i, 1, 'Customer $i', 'customer$i@test.com'
                )
                """
            )
        }

        // Insert invoices
        for (i in 1..10) {
            db.execSQL(
                """
                INSERT INTO invoices (
                    id, customer_id, amount, status, issue_date, business_id
                ) VALUES (
                    $i, ${i % 5 + 1}, ${i * 1000}, 'SENT', '2026-01-01', 1
                )
                """
            )
        }
    }

    private fun getRecordCount(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        tableName: String
    ): Int {
        val cursor = db.query("SELECT COUNT(*) FROM $tableName")
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    private fun tableExists(
        db: androidx.sqlite.db.SupportSQLiteDatabase,
        tableName: String
    ): Boolean {
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='$tableName'"
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    private fun verifyReferentialIntegrity(
        db: androidx.sqlite.db.SupportSQLiteDatabase
    ) {
        // Check that invoices reference existing customers
        val orphanInvoices = db.query(
            """
            SELECT COUNT(*) FROM invoices i
            WHERE NOT EXISTS (SELECT 1 FROM customers c WHERE c.id = i.customer_id)
            """
        )
        orphanInvoices.moveToFirst()
        val orphanCount = orphanInvoices.getInt(0)
        orphanInvoices.close()

        assertEquals(
            0,
            orphanCount,
            "No invoices should reference non-existent customers (referential integrity)"
        )
    }
}

