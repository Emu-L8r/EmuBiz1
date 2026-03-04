# Migration Testing Guide - Week 3

**Date:** March 5, 2026

---

## What is Migration Testing?

Database migrations are **permanent**. Once they reach production, you cannot change them.

Migration tests verify:
- ✅ Schema changes work correctly
- ✅ Data is migrated properly (no loss, correct conversions)
- ✅ Foreign key constraints remain valid
- ✅ Migrations are idempotent (safe to run multiple times)

---

## Your Current Migrations

### Migration 21→22: Drop Sync Table

```sql
DROP TABLE IF EXISTS pending_operations
```

**What to test:**
- Table exists before migration
- Table doesn't exist after migration
- No data loss (table was empty, feature removed)

### Migration 22→23: Add Currency Column

```sql
ALTER TABLE line_items 
ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'AUD'
```

**What to test:**
- Column doesn't exist before migration
- Column exists after migration
- New rows can be inserted
- Existing rows get 'AUD' as default

### Migration 23→24: Fix Monetary Types

Converts Double → Long for:
- `invoice_payments.amountPaid`
- `invoice_payment_snapshots.*`
- `daily_payment_snapshots.*`
- `collection_metrics.*`

**What to test:**
- Data converted correctly (100x)
- $149.99 becomes 14999 cents
- Foreign keys still work
- No duplicate data

---

## Example: Testing Migration 21→22

```kotlin
@RunWith(AndroidTestRunner::class)
class Migration21To22Test {
    
    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    fun migrate21To22_dropsPendingOperationsTable() {
        // 1. Create DB at v21 with existing data
        val db = migrationTestHelper.createDatabase("test.db", 21)
        
        // Optional: Insert test data to verify it's cleaned up
        // db.execSQL("INSERT INTO pending_operations ...")
        
        // 2. Verify table exists
        val cursor = db.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='pending_operations'"
        )
        assertTrue("Table should exist before migration", cursor.count > 0)
        
        // 3. Close and migrate
        db.close()
        
        // 4. Validate migration
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            "test.db",
            22,
            true,  // validateDroppedTables
            MIGRATION_21_22
        )
        
        // 5. Verify table is gone
        val cursorAfter = migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='pending_operations'"
        )
        assertTrue("Table should be deleted", cursorAfter.count == 0)
        
        migratedDb.close()
    }
}
```

---

## Example: Testing Migration 22→23 (Add Column)

```kotlin
@RunWith(AndroidTestRunner::class)
class Migration22To23Test {
    
    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    fun migrate22To23_addsPreferredCurrencyColumn() {
        // Create DB at v22
        val db = migrationTestHelper.createDatabase("test.db", 22)
        
        // Insert test line item (without new column)
        db.execSQL("""
            INSERT INTO line_items (id, invoiceId, description, quantity, unitPrice)
            VALUES (1, 1, 'Test Item', 2.0, 5000)
        """)
        
        db.close()
        
        // Migrate to v23
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            "test.db",
            23,
            true,
            MIGRATION_22_23
        )
        
        // Verify new column exists
        val cursor = migratedDb.query(
            "PRAGMA table_info(line_items) WHERE name='currencyCode'"
        )
        assertTrue("currencyCode column should exist", cursor.count > 0)
        
        // Verify existing data got default value
        val dataCursor = migratedDb.query(
            "SELECT currencyCode FROM line_items WHERE id = 1"
        )
        dataCursor.moveToFirst()
        assertEquals("Default should be AUD", "AUD", dataCursor.getString(0))
        
        migratedDb.close()
    }
}
```

---

## Example: Testing Migration 23→24 (Type Conversion)

```kotlin
@RunWith(AndroidTestRunner::class)
class Migration23To24Test {
    
    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )
    
    @Test
    fun migrate23To24_convertsMonetaryDoubleToLong() {
        // Create DB at v23
        val db = migrationTestHelper.createDatabase("test.db", 23)
        
        // Insert test data: 149.99 dollars
        db.execSQL("""
            INSERT INTO invoice_payments (invoiceId, amountPaid, paymentDate, paymentMethod, transactionReference, notes, createdAtMs, updatedAtMs)
            VALUES (1, 149.99, 1000, 'Card', 'TX123', 'Test payment', 2000, 3000)
        """)
        
        db.close()
        
        // Migrate to v24
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            "test.db",
            24,
            true,
            MIGRATION_23_24
        )
        
        // Verify data was converted: 149.99 → 14999 cents
        val cursor = migratedDb.query(
            "SELECT amountPaid FROM invoice_payments WHERE id = 1"
        )
        cursor.moveToFirst()
        assertEquals("Should convert 149.99 to 14999 cents", 14999L, cursor.getLong(0))
        
        migratedDb.close()
    }
}
```

---

## When to Write Migration Tests

- ✅ **Always** when modifying schema (ADD/DROP/ALTER)
- ✅ **Always** when converting data types
- ✅ **Always** when changing constraints
- ✅ **Always** before shipping to production
- ❌ **Skip** for development-only migrations (that won't be in production)

---

## Best Practices

1. **Test the migration itself**, not the code that uses it
2. **Test edge cases**: empty tables, NULL values, duplicates
3. **Test data integrity**: foreign keys, constraints
4. **Test backwards**: Can you undo the migration? (Usually not, but verify)
5. **Test on real data**: If possible, use actual production schema

---

## Your Migration Status

| Migration | Status | Risk | Testing |
|-----------|--------|------|---------|
| 21→22 | ✅ Complete | LOW | Recommended |
| 22→23 | ✅ Complete | LOW | Recommended |
| 23→24 | ✅ Complete | MED | **CRITICAL** |

For v23→24 (monetary type conversion), migration testing is **CRITICAL** because:
- Data conversion happens (100x multiplier)
- Multiple tables affected
- Easy to introduce bugs
- Hard to recover from in production

---

## How to Run Migration Tests

```bash
# Run all migration tests
./gradlew :app:testDebugUnitTest -k MigrationTest

# Run specific migration test
./gradlew :app:testDebugUnitTest -k Migration21To22Test
```

---

**Next step:** Write the migration tests for your v23→24 monetary conversion!


