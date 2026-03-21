# Database Migration Strategy

## Overview

Bizap uses Room Persistence Library for local data storage. This document outlines the migration strategy for evolving the database schema from v35 (current) to future versions.

## Current State

**Database Version:** 35
**Database Name:** `bizap-db`
**Location:** `app/src/main/java/com/emul8r/bizap/data/local/AppDatabase.kt`

### Entities (39 tables)
- Customer management (CustomerEntity)
- Invoice management (InvoiceEntity, LineItemEntity, InvoiceItemEntity)
- Business profiles (BusinessProfileEntity)
- Currency & exchange rates (CurrencyEntity, ExchangeRateEntity)
- Analytics snapshots (InvoiceAnalyticsSnapshot, DailyRevenueSnapshot, etc.)
- Templates (InvoiceTemplate, InvoiceCustomField)
- Offline operations (OfflineOperation, PendingOperationEntity)
- Payments (PaymentEntity, InvoicePaymentEntity)
- Notes (NoteEntity)
- Documents (GeneratedDocumentEntity)

## Migration Principles

### 1. Zero Data Loss
- All migrations must preserve existing data
- Use `ALTER TABLE ADD COLUMN` with defaults
- Never `DROP TABLE` without backup
- Test migrations thoroughly before deployment

### 2. Atomicity
- Migrations run in a transaction
- If migration fails, database rolls back
- App won't start with mismatched schema

### 3. Rollback Support
- Always create database backup before migration
- Document rollback procedures
- Test rollback path

### 4. Backward Compatibility
- New columns should have default values
- Don't remove columns still used by older code
- Use `@Deprecated` annotations before removal

## Migration Workflow

### Creating a Migration

**Step 1: Update Entity**
```kotlin
@Entity(tableName = "invoices")
data class InvoiceEntity(
    // ... existing columns ...
    
    // NEW: Added in v36
    @ColumnInfo(name = "discount_amount")
    val discountAmount: Long? = null
)
```

**Step 2: Increment Database Version**
```kotlin
@Database(
    entities = [ /* ... */ ],
    version = 36,  // Increment from 35
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() { }
```

**Step 3: Create Migration File**

Location: `app/src/main/java/com/emul8r/bizap/data/local/migrations/Migration_35_36.kt`

```kotlin
package com.emul8r.bizap.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_35_36 = object : Migration(35, 36) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add new column with default value
        db.execSQL(
            "ALTER TABLE invoices ADD COLUMN discount_amount INTEGER DEFAULT 0"
        )
    }
}
```

**Step 4: Register Migration**

In `app/src/main/java/com/emul8r/bizap/di/DatabaseModule.kt`:

```kotlin
@Provides
@Singleton
fun provideDatabase(
    @ApplicationContext context: Context,
    passphraseMgr: DatabasePassphraseManager
): AppDatabase {
    return Room.databaseBuilder(context, AppDatabase::class.java, "bizap-db")
        .openHelperFactory(factory)
        .addMigrations(
            // ... existing migrations ...
            MIGRATION_35_36  // Add new migration
        )
        .build()
}
```

**Step 5: Export Schema**

Room automatically generates schema JSON files at:
```
app/schemas/com.emul8r.bizap.data.local.AppDatabase/36.json
```

Version control this file for reference.

## Testing Migrations

### Automated Migration Tests

Location: `app/src/androidTest/java/com/emul8r/bizap/database/MigrationTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class Migration35to36Test {
    
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )
    
    @Test
    fun migrate35To36_preservesInvoices() {
        // Create database at version 35
        val db = helper.createDatabase(TEST_DB, 35).apply {
            execSQL("""
                INSERT INTO invoices (id, businessProfileId, customerId, 
                    invoiceNumber, totalAmount, status)
                VALUES (1, 1, 1, 'INV-001', 100000, 'SENT')
            """)
            close()
        }
        
        // Run migration
        val migratedDb = helper.runMigrationsAndValidate(
            TEST_DB, 36, true, MIGRATION_35_36
        )
        
        // Verify data preserved
        val cursor = migratedDb.query("SELECT * FROM invoices WHERE id = 1")
        cursor.moveToFirst()
        assertEquals(100000, cursor.getLong(cursor.getColumnIndex("totalAmount")))
        assertEquals(0, cursor.getLong(cursor.getColumnIndex("discount_amount")))
        cursor.close()
    }
}
```

### Manual Testing

1. Install app with old version (v35)
2. Create test data
3. Upgrade to new version (v36)
4. Verify:
   - App starts without crash
   - Data is preserved
   - New features work
   - Old features still work

## Current Migration History

| Version | From | To | Description | Date |
|---------|------|----|----- |------|
| v35 | 34 | 35 | Added notes support | 2024-12 |
| v34 | 33 | 34 | Payment entity updates | 2024-11 |
| v33 | 32 | 33 | Invoice versioning | 2024-10 |
| v32 | 31 | 32 | Analytics snapshots | 2024-09 |
| v31 | 30 | 31 | Offline queue | 2024-08 |

*Full history: See `app/src/main/java/com/emul8r/bizap/data/local/migrations/`*

## Rollback Procedures

### Automatic Rollback (Migration Failure)

Room automatically rolls back if migration fails:
```
E/Room: Migration failed
E/Room: Database will be recreated
```

**Result:** Database is wiped and recreated (data loss!)

**Prevention:** Test migrations thoroughly before release.

### Manual Rollback (App Downgrade)

If users downgrade the app:

1. Room detects version mismatch
2. App crashes with `IllegalStateException`
3. User must:
   - Uninstall app (loses data)
   - OR update to compatible version

**Prevention:** Don't allow downgrades in production.

### Backup and Restore

See `DatabaseBackupManager.kt` for automated backups:

```kotlin
val backupManager = DatabaseBackupManager(context)

// Create backup before migration
val backup = backupManager.createBackup(database).getOrThrow()

// If migration fails, restore
backupManager.restoreBackup(backup).getOrThrow()
```

## Database Backup Strategy

### Automatic Backups
- Created daily (if app is used)
- Stored in `app files/backups/`
- Retention: 30 days
- Max backups: 30

### Manual Backups
Users can trigger backup from Settings:
- Settings → Backup & Restore → Create Backup

### Backup Format
- SQLite database file (encrypted with SQLCipher)
- Filename: `bizap-db-backup-YYYY-MM-DD_HH-mm-ss.db`
- Size: ~1-10 MB (depending on data)

### Restore Procedure
1. Close database connection
2. Copy backup file to database location
3. Restart app
4. Database loads from restored file

## Migration Best Practices

### ✅ DO

1. **Add columns with defaults**
   ```sql
   ALTER TABLE customers ADD COLUMN tags TEXT DEFAULT '[]'
   ```

2. **Create indexes for performance**
   ```sql
   CREATE INDEX idx_invoices_status ON invoices(status)
   ```

3. **Preserve foreign keys**
   ```sql
   CREATE TABLE new_table (
       id INTEGER PRIMARY KEY,
       customer_id INTEGER,
       FOREIGN KEY(customer_id) REFERENCES customers(id)
   )
   ```

4. **Log migration progress**
   ```kotlin
   override fun migrate(db: SupportSQLiteDatabase) {
       Timber.i("Starting migration 35→36")
       db.execSQL("...")
       Timber.i("Migration 35→36 complete")
   }
   ```

### ❌ DON'T

1. **Drop tables with data**
   ```sql
   DROP TABLE customers  -- ❌ Data loss!
   ```

2. **Remove columns still in use**
   ```sql
   -- ❌ Can't remove columns in SQLite
   -- Must create new table and copy data
   ```

3. **Change column types without migration**
   ```kotlin
   // ❌ Type mismatch will crash
   val amount: String  // was Long
   ```

4. **Assume migration order**
   ```kotlin
   // ❌ Users might skip versions
   // Always support full migration path
   ```

## Troubleshooting

### "Migration didn't properly handle: table_name"

**Cause:** Entity definition doesn't match database schema

**Solution:**
1. Check entity annotations
2. Verify migration SQL
3. Export and compare schema JSON

### "Expected [X], found [Y]"

**Cause:** Column type mismatch

**Solution:**
1. Fix migration to match entity
2. Or fix entity to match migration

### "Foreign key constraint failed"

**Cause:** Referenced table doesn't exist yet

**Solution:**
1. Create referenced table first
2. Then create table with foreign key

## Future Migrations (Planned)

### v36 (Q2 2025)
- Add invoice templates support
- Add custom fields to invoices
- Add discount tracking

### v37 (Q3 2025)
- Add recurring invoice support
- Add invoice reminders
- Add payment gateway integration

### v38 (Q4 2025)
- Add multi-user support
- Add team collaboration
- Add role-based permissions

## Recovery from Failed Migration

If a migration fails in production:

1. **Immediate Response**
   - Rollback app to previous version
   - Create hotfix release
   - Notify users

2. **Investigation**
   - Collect crash reports
   - Analyze database state
   - Identify root cause

3. **Fix**
   - Correct migration script
   - Test thoroughly
   - Release patched version

4. **Prevention**
   - Add migration to test suite
   - Improve testing coverage
   - Document edge case

## Database Encryption

All database files are encrypted with **SQLCipher 4.5.x**.

Migration considerations:
- Encrypted databases work same as unencrypted
- Passphrase is preserved across migrations
- No special handling needed

See `ENCRYPTION_AND_BACKUP_GUIDE.md` for details.

## Summary

✅ **Current Status:**
- Database version: 35
- Migrations: v21→35 (all tested)
- Backup system: Operational
- Encryption: Active (SQLCipher)

✅ **Migration Strategy:**
- Zero data loss principle
- Automated testing
- Rollback support
- Daily backups

✅ **Next Migration (v36):**
- Planned for Q2 2025
- Will add invoice templates
- Will add custom fields
- Will add discount tracking

**Documentation updated:** 2024-12-21
