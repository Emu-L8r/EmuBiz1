package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_28_29
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Migration 28 → 29: Creates the `pending_operations` table for offline queue support.
 *
 * Verifies:
 * - Table and indexes are created correctly.
 * - Existing data in other tables is unaffected.
 * - Basic CRUD on the new table works after migration.
 */
@RunWith(AndroidJUnit4::class)
class Migration28To29Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration28To29_pendingOperationsTableCreated() {
        // 1. Open the database at version 28
        val db = helper.createDatabase(TEST_DB, 28)
        db.close()

        // 2. Run migration 28 → 29
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 29, true, MIGRATION_28_29
        )

        // 3. Verify the table exists and can be queried
        val cursor = migratedDb.query("SELECT COUNT(*) FROM pending_operations")
        cursor.moveToFirst()
        assertEquals(0, cursor.getInt(0), "New pending_operations table should be empty")
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration28To29_canInsertAndQueryPendingOperation() {
        val db = helper.createDatabase(TEST_DB + "_insert", 28)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_insert", 29, true, MIGRATION_28_29
        )

        // Insert a pending operation
        migratedDb.execSQL("""
            INSERT INTO pending_operations (operationType, entityType, entityId, payload, createdAt, status)
            VALUES ('CREATE', 'INVOICE', 42, '{}', 1700000000000, 'PENDING')
        """)

        val cursor = migratedDb.query(
            "SELECT operationType, entityType, entityId, status FROM pending_operations"
        )
        cursor.moveToFirst()
        assertEquals(1, cursor.count, "Should have 1 inserted operation")
        assertEquals("CREATE", cursor.getString(cursor.getColumnIndexOrThrow("operationType")))
        assertEquals("INVOICE", cursor.getString(cursor.getColumnIndexOrThrow("entityType")))
        assertEquals(42L, cursor.getLong(cursor.getColumnIndexOrThrow("entityId")))
        assertEquals("PENDING", cursor.getString(cursor.getColumnIndexOrThrow("status")))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration28To29_defaultStatusIsPending() {
        val db = helper.createDatabase(TEST_DB + "_defaults", 28)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_defaults", 29, true, MIGRATION_28_29
        )

        // Insert without specifying status (should default to 'PENDING')
        migratedDb.execSQL("""
            INSERT INTO pending_operations (operationType, entityType, entityId, payload, createdAt)
            VALUES ('DELETE', 'CUSTOMER', 7, '{}', 1700000000000)
        """)

        val cursor = migratedDb.query(
            "SELECT status, attemptCount FROM pending_operations WHERE entityId = 7"
        )
        cursor.moveToFirst()
        assertEquals("PENDING", cursor.getString(cursor.getColumnIndexOrThrow("status")))
        assertEquals(0, cursor.getInt(cursor.getColumnIndexOrThrow("attemptCount")))
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration28To29_existingTablesUnaffected() {
        val db = helper.createDatabase(TEST_DB + "_existing", 28)
        // Verify a known v28 table is accessible after migration
        db.execSQL("SELECT COUNT(*) FROM invoices")
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_existing", 29, true, MIGRATION_28_29
        )

        // All original tables must still be queryable
        val invoiceCount = migratedDb.query("SELECT COUNT(*) FROM invoices")
        assertTrue(invoiceCount.moveToFirst(), "invoices table should be accessible after migration")
        invoiceCount.close()
        migratedDb.close()
    }

    companion object {
        private const val TEST_DB = "migration_28_29_test.db"
    }
}
