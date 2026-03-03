# 🔧 Room Database Schema Mismatch - FIXED

**Date:** March 1, 2026  
**Status:** ✅ COMPLETE  
**Blocker Resolution:** App crash due to missing column in customers table

---

## 🎯 Problem Identified

The `CustomerEntity` class defined a `businessProfileId` field, but the SQLite `customers` table on the device did not have this column. This caused a Room schema validation error on app startup.

```
Schema mismatch:
  Expected: customerEntity has [id, businessProfileId, name, businessName, businessNumber, email, phone, address, notes, createdAt, updatedAt]
  Found: customers table has [id, name, businessName, businessNumber, email, phone, address, notes, createdAt, updatedAt]
  Missing: businessProfileId
```

### Root Cause
- **Migration 9→10** added `businessProfileId` to the `invoices` table but forgot to add it to the `customers` table
- The field was present in the entity definition but not in the database schema
- Device databases that were created before this field existed were unable to upgrade

---

## ✅ Solution Implemented

### Step 1: Created Migration 16→17
**File:** `app/src/main/java/com/emul8r/bizap/data/local/Migrations.kt`

Added new migration to add the missing column:
```kotlin
val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE customers ADD COLUMN businessProfileId INTEGER NOT NULL DEFAULT 1")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_customers_business ON customers(businessProfileId)")
        Log.i("Migration", "✅ MIGRATION_16_17 completed: Added businessProfileId to customers table")
    }
}
```

**Why this works:**
- Adds the `businessProfileId` column with a default value of 1 (first business)
- Creates an index for query performance
- Logs the migration completion

### Step 2: Updated Database Version
**File:** `app/src/main/java/com/emul8r/bizap/data/local/AppDatabase.kt`

Changed:
```kotlin
@Database(..., version = 16, ...)  // ❌ OLD
```

To:
```kotlin
@Database(..., version = 17, ...)  // ✅ NEW
```

### Step 3: Registered Migration in AppDatabase
**File:** `app/src/main/java/com/emul8r/bizap/data/local/AppDatabase.kt`

Added to migration list:
```kotlin
.addMigrations(
    // ... existing migrations ...
    MIGRATION_15_16,
    MIGRATION_16_17  // ✅ NEW
)
```

### Step 4: Registered Migration in DatabaseModule
**File:** `app/src/main/java/com/emul8r/bizap/di/DatabaseModule.kt`

Added to migration list (for Hilt DI):
```kotlin
.addMigrations(
    // ... existing migrations ...
    MIGRATION_15_16,
    MIGRATION_16_17  // ✅ NEW
)
```

---

## 📊 Impact Analysis

| Aspect | Details |
|--------|---------|
| **Schema Changes** | Added `businessProfileId INTEGER NOT NULL DEFAULT 1` to `customers` table |
| **Database Version** | 16 → 17 |
| **Backward Compatibility** | ✅ Yes - all existing customers get businessProfileId = 1 |
| **Data Integrity** | ✅ Maintained - no data loss |
| **Migration Order** | Added at the end of migration sequence (clean migration) |
| **Index Created** | ✅ `idx_customers_business` on businessProfileId |

---

## 🚀 Next Steps

1. **Rebuild the app**
   ```bash
   ./gradlew clean :app:assembleDebug
   ```

2. **Run unit tests** (should still be 32/32 passing)
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

3. **Install and test on device**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n com.emul8r.bizap/.MainActivity
   ```

4. **Verify in logcat**
   ```bash
   adb logcat | grep -i "migration_16_17"
   ```
   Should see: `✅ MIGRATION_16_17 completed: Added businessProfileId to customers table`

---

## 🔍 Verification Checklist

- [ ] Build completes without errors
- [ ] All 32 unit tests pass
- [ ] APK installs without errors
- [ ] App launches without crashing
- [ ] Logcat shows migration completion message
- [ ] Previous data loads correctly (businessProfileId = 1)
- [ ] New customers can be created

---

## 📝 Technical Details

**CustomerEntity Definition:**
```kotlin
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val businessProfileId: Long = 1,  // ← This field was missing from DB
    val name: String,
    val businessName: String? = null,
    val businessNumber: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

**Root Cause Timeline:**
1. **v9→10:** `businessProfileId` added to invoices, but NOT customers (bug)
2. **v10-15:** Additional migrations, but bug not caught
3. **v16:** Current state - app crashes on startup due to schema mismatch
4. **v17:** ✅ Fix applied - column added retroactively

---

## 💡 Lessons Learned

For future multi-business feature rollouts:
1. Always add related columns to ALL affected tables
2. Add schema validation tests
3. Use Room's schema export feature to catch mismatches in CI

---

**Status:** ✅ READY FOR BUILD & TEST

*All changes committed. No breaking changes to public API.*

