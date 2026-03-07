package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.MIGRATION_21_22
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertFalse

/**
 * Migration 21 → 22: Drop pending_operations table.
 *
 * Purpose: Offline sync logic was removed from the app. The pending_operations table
 * stored queued write operations for server sync which is no longer needed.
 *
 * Data Impact: All pending operations are discarded (intentional — they were never executed).
 */
@RunWith(AndroidJUnit4::class)
class Migration21To22Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration21To22_pendingOperationsTableDropped() {
        // 1. Create database at version 21 with a pending_operations record
        val db = helper.createDatabase(TEST_DB, 21)
        db.execSQL(
            """
            INSERT INTO pending_operations (
                id, operationType, entityType, entityId, businessProfileId,
                payload, status, retryCount, maxRetries, createdAt, updatedAt, lastError
            ) VALUES (
                'op-001', 'CREATE', 'INVOICE', 1, 1,
                '{"invoiceId":1}', 'PENDING', 0, 3, 1700000000000, 1700000000000, NULL
            )
            """.trimIndent()
        )

        // Verify record exists before migration
        val cursorBefore = db.query("SELECT COUNT(*) FROM pending_operations")
        cursorBefore.moveToFirst()
        assert(cursorBefore.getInt(0) == 1) { "Expected 1 pending operation before migration" }
        cursorBefore.close()
        db.close()

        // 2. Run migration 21 → 22
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 22, true, MIGRATION_21_22
        )

        // 3. Verify pending_operations table no longer exists
        val tableCheck = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='pending_operations'"
        )
        assertFalse(tableCheck.moveToFirst(), "pending_operations table should have been dropped")
        tableCheck.close()
        migratedDb.close()
    }

    companion object {
        private const val TEST_DB = "migration_21_22_test.db"
    }
}
