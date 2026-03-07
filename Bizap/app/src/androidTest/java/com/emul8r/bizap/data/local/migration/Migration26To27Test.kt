package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_26_27
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Migration 26 → 27: Add optimistic-locking columns to daily_revenue_snapshots.
 *
 * Purpose: Support concurrent snapshot updates without data races. The version column
 * enables optimistic locking: readers load the version, writers include it in the WHERE
 * clause, and a non-update signals a conflict that triggers a retry.
 *
 * New Columns:
 * - version (INTEGER NOT NULL DEFAULT 1): incremented on every update
 * - updatedAtMs (INTEGER NOT NULL DEFAULT 0): timestamp of the last update
 *
 * Data Impact: All existing snapshot rows get version=1 and updatedAtMs=0.
 */
@RunWith(AndroidJUnit4::class)
class Migration26To27Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration26To27_versionColumnAdded_withDefault1() {
        // 1. Create database at version 26 with a daily_revenue_snapshot
        val db = helper.createDatabase(TEST_DB, 26)
        db.execSQL(
            """
            INSERT INTO daily_revenue_snapshots (
                id, businessProfileId, dateString, dateMs, totalRevenue, invoiceCount,
                paidInvoiceCount, draftInvoiceCount, averageInvoiceAmount,
                currencyBreakdown, dayOverDayGrowth, weekOverWeekGrowth, snapshotCreatedAtMs
            ) VALUES (
                1, 1, '2024-01-15', 1705276800000, 150000, 5,
                3, 1, 30000,
                '{"AUD": 150000}', 0.05, 0.10, 1705276800000
            )
            """.trimIndent()
        )

        // Verify no version column before migration
        val cursorBefore = db.query("PRAGMA table_info(daily_revenue_snapshots)")
        val columnNames = mutableListOf<String>()
        while (cursorBefore.moveToNext()) {
            columnNames.add(cursorBefore.getString(cursorBefore.getColumnIndexOrThrow("name")))
        }
        cursorBefore.close()
        assertTrue("version" !in columnNames, "version column should not exist before migration")
        assertTrue("updatedAtMs" !in columnNames, "updatedAtMs column should not exist before migration")
        db.close()

        // 2. Run migration 26 → 27
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 27, true, MIGRATION_26_27
        )

        // 3. Verify version and updatedAtMs columns added with correct defaults
        val cursor = migratedDb.query(
            "SELECT id, version, updatedAtMs FROM daily_revenue_snapshots WHERE id = 1"
        )
        cursor.moveToFirst()
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        assertEquals(1L, cursor.getLong(cursor.getColumnIndexOrThrow("version")),
            "version should default to 1 for existing rows")
        assertEquals(0L, cursor.getLong(cursor.getColumnIndexOrThrow("updatedAtMs")),
            "updatedAtMs should default to 0 for existing rows")
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration26To27_multipleExistingRows_allGetDefaults() {
        // 1. Create database at version 26 with multiple snapshots
        val db = helper.createDatabase(TEST_DB + "_multi", 26)
        for (i in 1..3) {
            db.execSQL(
                """
                INSERT INTO daily_revenue_snapshots (
                    id, businessProfileId, dateString, dateMs, totalRevenue, invoiceCount,
                    paidInvoiceCount, draftInvoiceCount, averageInvoiceAmount,
                    currencyBreakdown, dayOverDayGrowth, weekOverWeekGrowth, snapshotCreatedAtMs
                ) VALUES (
                    $i, 1, '2024-01-1$i', 1705276800000, ${i * 50000}, $i,
                    $i, 0, 50000,
                    '{"AUD": ${i * 50000}}', 0.0, 0.0, 1705276800000
                )
                """.trimIndent()
            )
        }
        db.close()

        // 2. Run migration 26 → 27
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_multi", 27, true, MIGRATION_26_27
        )

        // 3. Verify all rows get version=1 and updatedAtMs=0
        val cursor = migratedDb.query(
            "SELECT COUNT(*) FROM daily_revenue_snapshots WHERE version = 1 AND updatedAtMs = 0"
        )
        cursor.moveToFirst()
        assertEquals(3, cursor.getInt(0),
            "All 3 existing rows should have version=1 and updatedAtMs=0")
        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration26To27_emptyTable_noErrors() {
        // 1. Create database at version 26 with no snapshots
        val db = helper.createDatabase(TEST_DB + "_empty", 26)
        db.close()

        // 2. Run migration 26 → 27 on empty table
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_empty", 27, true, MIGRATION_26_27
        )

        // 3. Verify table is still empty and schema is correct
        val cursor = migratedDb.query("SELECT COUNT(*) FROM daily_revenue_snapshots")
        cursor.moveToFirst()
        assertEquals(0, cursor.getInt(0), "Empty table should stay empty after migration")
        cursor.close()
        migratedDb.close()
    }

    companion object {
        private const val TEST_DB = "migration_26_27_test.db"
    }
}
