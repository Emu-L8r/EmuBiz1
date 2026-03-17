package com.emul8r.bizap.data.local.migration

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.emul8r.bizap.data.local.AppDatabase
import com.emul8r.bizap.data.local.migrations.*
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * CRITICAL TEST: Migration v20→v35 preserves all financial data
 *
 * This is the ONE test that actually matters for production safety.
 * It proves users can safely upgrade without data loss.
 */
class MigrationTest {

    @Test
    fun migration_v20_to_v35_preserves_financial_data() {
        // Create v20 database with realistic test data
        val helper = MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            AppDatabase::class.java.canonicalName,
            FrameworkSQLiteOpenHelperFactory()
        )

        var dbV20 = helper.createDatabase("test", 20)

        // Insert sample business
        dbV20.execSQL("""
            INSERT INTO business_profiles 
            (name, businessType, currency, email, phone, address) 
            VALUES ('Test Business', 'RETAIL', 'USD', 'test@test.com', '555-1234', '123 Main')
        """)

        // Insert sample customers
        dbV20.execSQL("""
            INSERT INTO customers (name, email, phone, address) 
            VALUES 
            ('Acme Corp', 'acme@test.com', '555-0001', '100 Commerce'),
            ('Smith & Co', 'smith@test.com', '555-0002', '200 Trade'),
            ('Widget Factory', 'widgets@test.com', '555-0003', '300 Industrial')
        """)

        val now = System.currentTimeMillis()

        // Insert sample invoices with EXACT financial amounts
        dbV20.execSQL("""
            INSERT INTO invoices 
            (customerId, customerName, date, totalAmount, status, invoiceNumber, invoiceYear, invoiceSequence, amountPaid) 
            VALUES 
            (1, 'Acme Corp', $now, 15999, 'PAID', 'INV-2026-001', 2026, 1, 15999),
            (2, 'Smith & Co', $now, 25000, 'PARTIALLY_PAID', 'INV-2026-002', 2026, 2, 10000),
            (3, 'Widget Factory', $now, 8750, 'OVERDUE', 'INV-2026-003', 2026, 3, 0)
        """)

        dbV20.close()

        // Run all migrations v20 → v35
        var dbV35 = helper.runMigrationsAndValidate(
            "test", 35, true,
            MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25,
            MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30,
            MIGRATION_30_31, MIGRATION_31_32, MIGRATION_32_33, MIGRATION_33_34, MIGRATION_34_35
        )

        // Verify businesses exist
        val biz = dbV35.query("SELECT * FROM business_profiles LIMIT 1")
        assertTrue("Businesses should survive migration") { biz.moveToFirst() }
        biz.close()

        // Verify customers exist
        val customers = dbV35.query("SELECT COUNT(*) as cnt FROM customers")
        customers.moveToFirst()
        assertEquals(3, customers.getInt(0))
        customers.close()

        // CRITICAL: Verify invoices with exact financial amounts
        val invoices = dbV35.query("SELECT totalAmount, amountPaid, status FROM invoices ORDER BY id")

        invoices.moveToFirst()
        assertEquals(15999L, invoices.getLong(0), "Invoice 1 amount")
        assertEquals(15999L, invoices.getLong(1), "Invoice 1 paid")
        assertEquals("PAID", invoices.getString(2), "Invoice 1 status")

        invoices.moveToNext()
        assertEquals(25000L, invoices.getLong(0), "Invoice 2 amount")
        assertEquals(10000L, invoices.getLong(1), "Invoice 2 paid")
        assertEquals("PARTIALLY_PAID", invoices.getString(2), "Invoice 2 status")

        invoices.moveToNext()
        assertEquals(8750L, invoices.getLong(0), "Invoice 3 amount")
        assertEquals(0L, invoices.getLong(1), "Invoice 3 paid")
        assertEquals("OVERDUE", invoices.getString(2), "Invoice 3 status")

        invoices.close()
        dbV35.close()

        // RESULT: If we got here, migrations work and data is safe ✅
    }
}

