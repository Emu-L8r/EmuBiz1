package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_34_35
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Migration 34 → 35: Refactor analytics snapshot tables.
 *
 * Verifies:
 * - Legacy tables `invoice_velocity_metrics` and `customer_revenue` are dropped.
 * - `daily_revenue_snapshots` is recreated with the extended schema.
 * - Existing rows in `daily_revenue_snapshots` are migrated to the new schema.
 * - New indices `idx_daily_business` and `idx_daily_date` exist.
 * - Unrelated tables (e.g. `invoices`) remain accessible after migration.
 */
@RunWith(AndroidJUnit4::class)
class Migration34To35Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration34To35_legacyTablesDropped() {
        val db = helper.createDatabase(TEST_DB, 34)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 35, true, MIGRATION_34_35
        )

        // invoice_velocity_metrics should be gone
        val velocityCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='invoice_velocity_metrics'"
        )
        assertEquals(0, velocityCursor.count, "invoice_velocity_metrics table should be dropped")
        velocityCursor.close()

        // customer_revenue should be gone
        val customerRevenueCursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='customer_revenue'"
        )
        assertEquals(0, customerRevenueCursor.count, "customer_revenue table should be dropped")
        customerRevenueCursor.close()

        migratedDb.close()
    }

    @Test
    fun migration34To35_dailySnapshotTableRecreated() {
        val db = helper.createDatabase(TEST_DB + "_recreate", 34)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_recreate", 35, true, MIGRATION_34_35
        )

        // New table should be queryable with the new columns
        val cursor = migratedDb.query(
            "SELECT id, businessProfileId, dateString, dateMs, totalRevenue, " +
            "invoiceCount, paidInvoiceCount, draftInvoiceCount, averageInvoiceAmount, " +
            "currencyBreakdown, dayOverDayGrowth, weekOverWeekGrowth, " +
            "snapshotCreatedAtMs, version, updatedAtMs " +
            "FROM daily_revenue_snapshots LIMIT 1"
        )
        // Just verify the query succeeds (column names are correct)
        assertTrue(cursor.count >= 0, "daily_revenue_snapshots table should be queryable")
        cursor.close()

        migratedDb.close()
    }

    @Test
    fun migration34To35_existingSnapshotRowsMigrated() {
        val db = helper.createDatabase(TEST_DB + "_rows", 34)

        // Insert a row into the old daily_revenue_snapshots schema
        db.execSQL(
            "INSERT INTO daily_revenue_snapshots " +
            "(businessId, date, invoicedCents, paidCents, invoiceCount, paidCount, createdAt) " +
            "VALUES (1, '2026-03-17', 100000, 80000, 5, 4, 1742000000000)"
        )
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_rows", 35, true, MIGRATION_34_35
        )

        val cursor = migratedDb.query(
            "SELECT businessProfileId, dateString, totalRevenue, invoiceCount, paidInvoiceCount " +
            "FROM daily_revenue_snapshots WHERE businessProfileId = 1"
        )
        assertTrue(cursor.moveToFirst(), "Migrated row should exist")
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("businessProfileId")))
        assertEquals("2026-03-17", cursor.getString(cursor.getColumnIndexOrThrow("dateString")))
        assertEquals(100000, cursor.getInt(cursor.getColumnIndexOrThrow("totalRevenue")))
        assertEquals(5, cursor.getInt(cursor.getColumnIndexOrThrow("invoiceCount")))
        assertEquals(4, cursor.getInt(cursor.getColumnIndexOrThrow("paidInvoiceCount")))
        cursor.close()

        migratedDb.close()
    }

    @Test
    fun migration34To35_newIndicesExist() {
        val db = helper.createDatabase(TEST_DB + "_indices", 34)
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_indices", 35, true, MIGRATION_34_35
        )

        val cursor = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='index' " +
            "AND tbl_name='daily_revenue_snapshots' " +
            "AND name IN ('idx_daily_business', 'idx_daily_date')"
        )
        assertEquals(2, cursor.count, "Both new indices should exist on daily_revenue_snapshots")
        cursor.close()

        migratedDb.close()
    }

    @Test
    fun migration34To35_existingTablesUnaffected() {
        val db = helper.createDatabase(TEST_DB + "_existing", 34)
        db.execSQL("SELECT COUNT(*) FROM invoices")
        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_existing", 35, true, MIGRATION_34_35
        )

        val invoiceCount = migratedDb.query("SELECT COUNT(*) FROM invoices")
        assertTrue(invoiceCount.moveToFirst(), "invoices table should be accessible after migration")
        invoiceCount.close()

        migratedDb.close()
    }

    companion object {
        private const val TEST_DB = "migration_34_35_test.db"
    }
}
