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
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Round-Trip Migration Test: v21 → v35
 *
 * This test verifies that data survives the complete migration path from the oldest
 * supported version (v21) through all 14 migrations to the current version (v35).
 */
@RunWith(AndroidJUnit4::class)
class MigrationRoundTripTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    companion object {
        private const val TEST_DB = "migration-test"
        private const val FINAL_VERSION = 35
    }

    @Test
    fun testRoundTripMigration_v21ToV35_PreservesProductionData() {
        // ═════════════════════════════════════════════════════════════════
        // PHASE 1: Create v21 database with realistic production data
        // ═════════════════════════════════════════════════════════════════

        // Create empty v21 database
        var dbV21 = helper.createDatabase(TEST_DB, 21)

        // Insert REALISTIC production data
        dbV21.execSQL("""
            INSERT INTO business_profiles 
            (id, businessName, businessType, currencyCode, email, phone, address, createdAt, updatedAt, isActive)
            VALUES (1, 'Test Business Inc', 'RETAIL', 'USD', 'test@example.com', '555-1234', '123 Main St', 
                    ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 1)
        """)

        dbV21.execSQL("""
            INSERT INTO customers 
            (id, businessProfileId, name, email, phone, address, createdAt, updatedAt, isActive)
            VALUES 
            (1, 1, 'Acme Corp', 'acme@example.com', '555-0001', '100 Commerce Ave', 
             ${System.currentTimeMillis()}, ${System.currentTimeMillis()}, 1)
        """)

        dbV21.close()

        // ═════════════════════════════════════════════════════════════════
        // PHASE 2: Run all migrations v21 → v35
        // ═════════════════════════════════════════════════════════════════

        val dbV35 = helper.runMigrationsAndValidate(
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

        // ═════════════════════════════════════════════════════════════════
        // PHASE 3: Verify data survived
        // ═════════════════════════════════════════════════════════════════

        val businessCursor = dbV35.query("SELECT businessName FROM business_profiles WHERE id = 1")
        assertTrue(businessCursor.moveToFirst())
        assertEquals("Test Business Inc", businessCursor.getString(0))
        businessCursor.close()

        val customerCursor = dbV35.query("SELECT name FROM customers WHERE id = 1")
        assertTrue(customerCursor.moveToFirst())
        assertEquals("Acme Corp", customerCursor.getString(0))
        customerCursor.close()

        dbV35.close()
    }
}
