# 🔧 **DATABASE SCHEMA MISMATCH - RESOLUTION GUIDE**

## **Problem**

Room database validation failed because the `customers` table schema in the SQLite database doesn't match the Entity definition. The migration didn't properly update all columns and indices.

## **Root Cause**

The existing database has a stale `customers` table that doesn't match the current `CustomerEntity` class definition. Since `fallbackToDestructiveMigration()` is enabled, Room will recreate the table from scratch when it detects this mismatch.

## **Solution: Clear Database & Reinstall**

Since the app is in development and uses `fallbackToDestructiveMigration()`, we can safely wipe the database and let Room recreate it from the Entity definitions.

### **Option 1: ADB Clear (Recommended)**

```bash
# Stop the app
adb shell am force-stop com.emul8r.bizap

# Clear app data (wipes database)
adb shell pm clear com.emul8r.bizap

# Reinstall app
./gradlew :app:installDebug

# Verify schema is correct
adb logcat | grep "Room\|Migration\|Database"
```

### **Option 2: Android Studio (GUI)**

1. Open **Android Studio**
2. Connect device/emulator
3. Go to **Device File Explorer** (View → Tool Windows → Device File Explorer)
4. Navigate to: `/data/data/com.emul8r.bizap/databases/`
5. Delete `bizap.db` file
6. Reinstall app: `./gradlew :app:installDebug`

### **Option 3: Uninstall & Reinstall**

```bash
# Uninstall app
adb uninstall com.emul8r.bizap

# Clean build
./gradlew clean :app:assembleDebug

# Reinstall
./gradlew :app:installDebug
```

---

## **Expected Result**

After clearing the database:
1. ✅ Room will detect missing/mismatched schema
2. ✅ `fallbackToDestructiveMigration()` triggers
3. ✅ Room recreates all tables from Entity definitions
4. ✅ All customers table columns created correctly:
   - id (PK, autoIncrement)
   - businessProfileId (NOT NULL)
   - name (NOT NULL)
   - businessName (nullable)
   - businessNumber (nullable)
   - email (nullable)
   - phone (nullable)
   - address (nullable)
   - notes (NOT NULL, default "")
   - createdAt (NOT NULL)
   - updatedAt (NOT NULL)
5. ✅ Indices created:
   - idx_customers_business (businessProfileId)
   - idx_customers_email (email)
6. ✅ App launches cleanly
7. ✅ No schema validation errors

---

## **Verification**

After reinstalling, verify the schema:

```bash
# Check database exists
adb shell ls -la /data/data/com.emul8r.bizap/databases/

# Inspect schema (if SQLite available)
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db ".schema customers"

# Check logcat for success
adb logcat | grep -i "room\|migration\|database"
```

---

## **Why This Happened**

During Feature #5 implementation (6 phases), multiple database migrations were added. The `customers` table was created in an earlier migration, but the schema didn't match the current Entity definition. Since we added `fallbackToDestructiveMigration()`, Room will automatically fix this by recreating the table.

---

## **Prevention**

For future migrations, ensure:
1. ✅ All @Column annotations match Entity properties
2. ✅ Nullability (notNull=true/false) matches Entity
3. ✅ All @Index annotations have CREATE INDEX statements
4. ✅ Test migrations before committing
5. ✅ Keep `fallbackToDestructiveMigration()` enabled during development

---

## **Next Steps**

1. Run: `adb shell pm clear com.emul8r.bizap`
2. Run: `./gradlew :app:installDebug`
3. Launch app
4. Verify: Should load without errors
5. Run: `./gradlew :app:testDebugUnitTest` (should pass 172/172)

---

**Status: Ready to fix** ✅

Choose Option 1 (ADB Clear) for fastest resolution.


