# MIGRATION 21_22: Remove Offline Sync Subsystem

**Migration Version**: 21 → 22  
**Date**: March 3, 2026  
**Changes**: Delete PendingOperation table and all sync infrastructure

## Schema Changes

### Tables Deleted
- `pending_operations` table

### Tables Unchanged
All other tables remain unchanged. Database content is preserved.

## Migration Implementation

```kotlin
val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Drop the pending_operations table since offline sync is being removed
        // (This is an offline-only local database app, no server sync)
        database.execSQL("DROP TABLE IF EXISTS pending_operations")
    }
}
```

## Code Changes

### Files Deleted (9 files)
1. `app/src/main/java/com/emul8r/bizap/data/repository/OfflineSyncQueue.kt`
2. `app/src/main/java/com/emul8r/bizap/data/sync/SyncWorker.kt`
3. `app/src/main/java/com/emul8r/bizap/data/sync/SyncService.kt`
4. `app/src/main/java/com/emul8r/bizap/data/sync/SyncScheduler.kt`
5. `app/src/main/java/com/emul8r/bizap/data/sync/ConflictResolver.kt`
6. `app/src/main/java/com/emul8r/bizap/data/network/ConnectivityManager.kt`
7. `app/src/main/java/com/emul8r/bizap/data/local/entities/PendingOperation.kt`
8. `app/src/main/java/com/emul8r/bizap/data/local/PendingOperationDao.kt`
9. `app/src/test/java/com/emul8r/bizap/data/SyncTest.kt`

### Files Modified

#### `AppDatabase.kt`
- Removed `PendingOperation::class` from @Database entities
- Removed `abstract fun pendingOperationDao(): PendingOperationDao`
- Changed version from 21 to 22

#### `DatabaseModule.kt`
- Removed `@Provides fun providePendingOperationDao(...)`

#### `NetworkModule.kt`
- Removed `import com.emul8r.bizap.data.sync.SyncService`
- Removed `@Provides fun provideSyncService(retrofit: Retrofit): SyncService`
- (Kept `provideJson()` for future use or other serialization needs)

#### `WorkManagerInitializer.kt`
- **Can be deleted** (the manifest disables the default initializer, and BizapApplication.Configuration.Provider handles WorkManager setup for ExchangeRateWorker)
- OR kept for future multi-worker support

#### `BizapApplication.kt`
- No changes needed (WorkManager is still used by ExchangeRateWorker)

## Testing

### Unit Tests to Remove
- `SyncTest.kt` (tests the deleted OfflineSyncQueue)

### Integration Tests to Update
- Any test that references `PendingOperationDao` or `SyncWorker`
- Any test setup that creates a database and expects the `pending_operations` table

### Manual Test Steps
1. Delete app data: `adb shell pm clear com.emul8r.bizap`
2. Install fresh APK: `./gradlew :app:installDebug`
3. App should launch and create database with v22 schema
4. Create customers, invoices, generate PDFs
5. Verify no references to sync functionality remain

## Notes

- This is an **offline-only local database app** — no server sync
- If server sync is needed in the future, this subsystem can be rebuilt from scratch
- The `Json` provider in NetworkModule is kept for other serialization use cases
- ExchangeRateWorker continues to work (it's a legitimate periodic background task)

---

**Status**: Ready to implement

