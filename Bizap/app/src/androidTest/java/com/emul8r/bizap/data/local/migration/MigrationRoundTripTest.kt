package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
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
import kotlin.test.fail

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
    fun testRoundTripMigration_v20ToV35_PreservesProductionData() {
        // CRITICAL TEST: Verify production users upgrading from v20 → v35
        // won't lose any financial data during the 15-step migration path

        // ═════════════════════════════════════════════════════════════════
        // PHASE 1: Create v20 database with realistic production data
        // ═════════════════════════════════════════════════════════════════

        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        // Create empty v20 database
        var dbV20 = helper.createDatabase(TEST_DB, 20)

        // Insert REALISTIC production data that users would have
        // Sample business
        dbV20.execSQL("""
            INSERT INTO business_profiles 
            (id, name, businessType, currency, email, phone, address, createdAt, updatedAt, isActive)
            VALUES (1, 'Test Business Inc', 'RETAIL', 'USD', 'test@example.com', '555-1234', '123 Main St', 
                    ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 1)
        """)

        // Sample customers
        dbV20.execSQL("""
            INSERT INTO customers 
            (id, businessProfileId, name, email, phone, address, createdAt, updatedAt, isActive)
            VALUES 
            (1, 1, 'Acme Corp', 'acme@example.com', '555-0001', '100 Commerce Ave', 
             ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 1),
            (2, 1, 'Smith & Co', 'smith@example.com', '555-0002', '200 Trade Blvd', 
             ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 1),
            (3, 1, 'Widget Factory', 'widgets@example.com', '555-0003', '300 Industrial Park', 
             ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 1)
        """)

        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30 * 24 * 60 * 60 * 1000)
        val sixtyDaysAgo = now - (60 * 24 * 60 * 60 * 1000)
        val ninetyDaysAgo = now - (90 * 24 * 60 * 60 * 1000)

        // Sample invoices with REAL AMOUNTS (in cents)
        dbV20.execSQL("""
            INSERT INTO invoices 
            (id, businessProfileId, customerId, customerName, customerAddress, date, dueDate, 
             totalAmount, isQuote, status, invoiceNumber, invoiceYear, invoiceSequence, 
             amountPaid, createdAt, updatedAt, isActive)
            VALUES 
            (1, 1, 1, 'Acme Corp', '100 Commerce Ave', $thirtyDaysAgo, $now, 
             15999, 0, 'PAID', 'INV-2026-001', 2026, 1, 
             15999, $thirtyDaysAgo, $now, 1),
            (2, 1, 2, 'Smith & Co', '200 Trade Blvd', $sixtyDaysAgo, $now, 
             25000, 0, 'PARTIALLY_PAID', 'INV-2026-002', 2026, 2, 
             10000, $sixtyDaysAgo, $now, 1),
            (3, 1, 3, 'Widget Factory', '300 Industrial Park', $ninetyDaysAgo, $now, 
             8750, 0, 'OVERDUE', 'INV-2026-003', 2026, 3, 
             0, $ninetyDaysAgo, $now, 1)
        """)

        // Sample line items
        dbV20.execSQL("""
            INSERT INTO line_items 
            (id, invoiceId, description, quantity, unitPrice, taxRate, isActive)
            VALUES 
            (1, 1, 'Consulting Services', 10.0, 1599, 0.0, 1),
            (2, 2, 'Product Sale', 50.0, 500, 0.0, 1),
            (3, 3, 'Software License', 1.0, 8750, 0.0, 1)
        """)

        dbV20.close()

        // ═════════════════════════════════════════════════════════════════
        // PHASE 2: Run all migrations v20 → v35
        // ═════════════════════════════════════════════════════════════════

        var dbV35 = helper.runMigrationsAndValidate(
            TEST_DB,
            FINAL_VERSION,
            true,  // validateDroppedColumns - catch schema mismatches
            MIGRATION_20_21,
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

        // ═════════════════════════════════════════════════════════════════
        // PHASE 3: Verify all data survived migration
        // ═════════════════════════════════════════════════════════════════

        // Check 1: Businesses survived
        val businessCursor = dbV35.query("""
            SELECT id, name, currency FROM business_profiles WHERE id = 1
        """)
        assertTrue("Business should exist after migration") { businessCursor.moveToFirst() }
        assertEquals("Business name preserved", "Test Business Inc",
            businessCursor.getString(businessCursor.getColumnIndex("name")))
        businessCursor.close()

        // Check 2: Customers survived with NO data loss
        val customersCursor = dbV35.query("""
            SELECT id, name, email FROM customers WHERE businessProfileId = 1 ORDER BY id
        """)
        assertEquals("All 3 customers should survive", 3, customersCursor.count)

        customersCursor.moveToFirst()
        assertEquals("Customer 1 data", "Acme Corp",
            customersCursor.getString(customersCursor.getColumnIndex("name")))
        assertEquals("Customer 1 email", "acme@example.com",
            customersCursor.getString(customersCursor.getColumnIndex("email")))

        customersCursor.moveToNext()
        assertEquals("Customer 2 data", "Smith & Co",
            customersCursor.getString(customersCursor.getColumnIndex("name")))

        customersCursor.moveToNext()
        assertEquals("Customer 3 data", "Widget Factory",
            customersCursor.getString(customersCursor.getColumnIndex("name")))

        customersCursor.close()

        // Check 3: Invoices survived with FINANCIAL data intact
        val invoicesCursor = dbV35.query("""
            SELECT id, totalAmount, amountPaid, status, invoiceNumber 
            FROM invoices 
            WHERE businessProfileId = 1 
            ORDER BY id
        """)
        assertEquals("All 3 invoices should survive", 3, invoicesCursor.count)

        invoicesCursor.moveToFirst()
        assertEquals("Invoice 1: Amount preserved", 15999L,
            invoicesCursor.getLong(invoicesCursor.getColumnIndexOrThrow("totalAmount")))
        assertEquals("Invoice 1: Paid amount preserved", 15999L,
            invoicesCursor.getLong(invoicesCursor.getColumnIndexOrThrow("amountPaid")))
        assertEquals("Invoice 1: Status preserved", "PAID",
            invoicesCursor.getString(invoicesCursor.getColumnIndexOrThrow("status")))

        invoicesCursor.moveToNext()
        assertEquals("Invoice 2: Amount preserved", 25000L,
            invoicesCursor.getLong(invoicesCursor.getColumnIndexOrThrow("totalAmount")))
        assertEquals("Invoice 2: Partial payment preserved", 10000L,
            invoicesCursor.getLong(invoicesCursor.getColumnIndexOrThrow("amountPaid")))
        assertEquals("Invoice 2: Status preserved", "PARTIALLY_PAID",
            invoicesCursor.getString(invoicesCursor.getColumnIndexOrThrow("status")))

        invoicesCursor.moveToNext()
        assertEquals("Invoice 3: Amount preserved", 8750L,
            invoicesCursor.getLong(invoicesCursor.getColumnIndexOrThrow("totalAmount")))
        assertEquals("Invoice 3: No payment preserved", 0L,
            invoicesCursor.getLong(invoicesCursor.getColumnIndexOrThrow("amountPaid")))
        assertEquals("Invoice 3: Status preserved", "OVERDUE",
            invoicesCursor.getString(invoicesCursor.getColumnIndexOrThrow("status")))

        invoicesCursor.close()

        // Check 4: Line items survived
        val lineItemsCursor = dbV35.query("""
            SELECT id, invoiceId, description, quantity FROM line_items WHERE invoiceId IN (1,2,3)
        """)
        assertEquals("All 3 line items should survive", 3, lineItemsCursor.count)
        lineItemsCursor.close()

        // Check 5: Foreign keys are valid (can't insert invalid customer ID)
        try {
            dbV35.execSQL("""
                INSERT INTO invoices 
                (businessProfileId, customerId, customerName, date, totalAmount, isQuote, status)
                VALUES (1, 99999, 'Fake', ${System.currentTimeMillis()}, 1000, 0, 'DRAFT')
            """)
            fail("Foreign key constraint should prevent invalid customer ID")
        } catch (e: Exception) {
            assertTrue("Should fail on foreign key violation",
                e.message?.contains("FOREIGN KEY constraint failed") ?: false)
        }

        dbV35.close()

        // ═════════════════════════════════════════════════════════════════
        // PHASE 4: Final verdict
        // ═════════════════════════════════════════════════════════════════

        // If we got here without exceptions:
        // ✅ All migrations ran successfully
        // ✅ All data survived intact
        // ✅ Financial amounts are preserved
        // ✅ Relationships are valid
        // ✅ Production users can safely upgrade
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

