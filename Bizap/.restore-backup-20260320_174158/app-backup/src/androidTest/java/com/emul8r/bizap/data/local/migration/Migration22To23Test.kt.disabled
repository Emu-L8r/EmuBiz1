package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_22_23
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * Migration 22 → 23: Add currencyCode column to line_items table.
 *
 * Purpose: Support multi-currency invoices by tracking which currency each line item
 * was priced in. Previously, line items had no currency context.
 *
 * Data Impact: All existing line items receive a default currency code of 'AUD'.
 */
@RunWith(AndroidJUnit4::class)
class Migration22To23Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration22To23_currencyCodeColumnAdded_withDefaultAUD() {
        // 1. Create database at version 22 and insert line items (no currencyCode column yet)
        val db = helper.createDatabase(TEST_DB, 22)
        db.execSQL(
            """
            INSERT INTO line_items (id, invoiceId, description, quantity, unitPrice)
            VALUES (1, 100, 'Consulting Services', 5.0, 20000)
            """.trimIndent()
        )
        db.execSQL(
            """
            INSERT INTO line_items (id, invoiceId, description, quantity, unitPrice)
            VALUES (2, 100, 'Travel Expenses', 1.0, 5000)
            """.trimIndent()
        )

        // Verify no currencyCode column before migration
        val cursorBefore = db.query("SELECT * FROM line_items WHERE id = 1")
        cursorBefore.moveToFirst()
        val columnIndexBefore = cursorBefore.getColumnIndex("currencyCode")
        assertEquals(-1, columnIndexBefore, "currencyCode column should not exist before migration")
        cursorBefore.close()
        db.close()

        // 2. Run migration 22 → 23
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 23, true, MIGRATION_22_23
        )

        // 3. Verify currencyCode column exists and defaults to 'AUD'
        val cursor = migratedDb.query("SELECT id, currencyCode FROM line_items ORDER BY id")
        cursor.moveToFirst()
        assertEquals(1, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        assertEquals("AUD", cursor.getString(cursor.getColumnIndexOrThrow("currencyCode")))

        cursor.moveToNext()
        assertEquals(2, cursor.getInt(cursor.getColumnIndexOrThrow("id")))
        assertEquals("AUD", cursor.getString(cursor.getColumnIndexOrThrow("currencyCode")))

        cursor.close()
        migratedDb.close()
    }

    @Test
    fun migration22To23_emptyTable_noDataLoss() {
        // 1. Create database at version 22 with no line items
        val db = helper.createDatabase(TEST_DB + "_empty", 22)
        db.close()

        // 2. Run migration 22 → 23 on empty table
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB + "_empty", 23, true, MIGRATION_22_23
        )

        // 3. Verify the column exists even on empty table
        val cursor = migratedDb.query("SELECT COUNT(*) FROM line_items")
        cursor.moveToFirst()
        assertEquals(0, cursor.getInt(0), "Empty table should stay empty after migration")
        cursor.close()
        migratedDb.close()
    }

    companion object {
        private const val TEST_DB = "migration_22_23_test.db"
    }
}
