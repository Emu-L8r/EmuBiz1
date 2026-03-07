# ✅ PHASE 2 DAY 1 QUICK START CHECKLIST

**Date:** March 8, 2026  
**Task:** Create OfflineOperation Entity & DAO  
**Estimated Time:** 2-3 hours  
**Difficulty:** Easy  

---

## 📋 TODAY'S MISSION

Create the foundation for offline sync: the database tables and data access layer for queuing offline operations.

---

## 🎯 WHAT YOU'LL BUILD TODAY

1. **OfflineOperation.kt** - Entity class
2. **OfflineOperationDao.kt** - Room DAO with CRUD operations
3. **Database Migration** - Add to AppDatabase
4. **Unit Tests** - Verify DAO works

---

## 📝 CHECKLIST

### **Step 1: Create OfflineOperation Entity** ✅

**File to create:** `app/src/main/java/com/emul8r/bizap/data/local/entities/OfflineOperation.kt`

```kotlin
@Entity(tableName = "offline_operations")
data class OfflineOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "operation_type")
    val operationType: String, // CREATE_INVOICE, UPDATE_INVOICE, RECORD_PAYMENT, DELETE_INVOICE, UPDATE_STATUS
    
    @ColumnInfo(name = "entity_id")
    val entityId: Long, // Invoice ID
    
    @ColumnInfo(name = "entity_data")
    val entityData: String, // Serialized JSON data
    
    @ColumnInfo(name = "business_profile_id")
    val businessProfileId: Long,
    
    @ColumnInfo(name = "timestamp_ms")
    val timestampMs: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "status")
    val status: String = "PENDING", // PENDING, SYNCING, SYNCED, FAILED
    
    @ColumnInfo(name = "retry_count")
    val retryCount: Int = 0,
    
    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null
)
```

**Checklist:**
- [ ] File created in correct location
- [ ] All fields have proper Room annotations
- [ ] Compiles without errors
- [ ] Follows existing entity patterns

---

### **Step 2: Create OfflineOperationDao** ✅

**File to create:** `app/src/main/java/com/emul8r/bizap/data/local/dao/OfflineOperationDao.kt`

```kotlin
@Dao
interface OfflineOperationDao {
    
    @Insert
    suspend fun insert(operation: OfflineOperation): Long
    
    @Update
    suspend fun update(operation: OfflineOperation)
    
    @Delete
    suspend fun delete(operation: OfflineOperation)
    
    @Query("SELECT * FROM offline_operations WHERE id = :id")
    suspend fun getById(id: Long): OfflineOperation?
    
    @Query("SELECT * FROM offline_operations WHERE business_profile_id = :businessId AND status = 'PENDING' ORDER BY timestamp_ms ASC")
    suspend fun getPendingOperations(businessId: Long): List<OfflineOperation>
    
    @Query("SELECT * FROM offline_operations WHERE business_profile_id = :businessId ORDER BY timestamp_ms DESC LIMIT 50")
    suspend fun getRecentOperations(businessId: Long): List<OfflineOperation>
    
    @Query("SELECT * FROM offline_operations WHERE status = 'FAILED' LIMIT 10")
    suspend fun getFailedOperations(): List<OfflineOperation>
    
    @Query("UPDATE offline_operations SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: Long, newStatus: String)
    
    @Query("DELETE FROM offline_operations WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM offline_operations WHERE status = 'SYNCED' AND business_profile_id = :businessId")
    suspend fun deleteSuccessfullySyncedOperations(businessId: Long)
}
```

**Checklist:**
- [ ] All CRUD operations present
- [ ] Query annotations correct
- [ ] Suspend functions for coroutines
- [ ] Compiles without errors
- [ ] Follows existing DAO patterns

---

### **Step 3: Register DAO in AppDatabase** ✅

**File to modify:** `app/src/main/java/com/emul8r/bizap/data/local/AppDatabase.kt`

Find the `AppDatabase` class and add:
```kotlin
@Dao
abstract fun offlineOperationDao(): OfflineOperationDao
```

And add to the `@Database` entities list:
```kotlin
entities = [
    // ...existing entities...
    OfflineOperation::class
]
```

**Checklist:**
- [ ] DAO registered in abstract class
- [ ] Entity added to @Database annotation
- [ ] Compiles without errors

---

### **Step 4: Create Database Migration** ✅

**File to create:** `app/src/main/java/com/emul8r/bizap/data/local/migrations/Migration_XX_YY.kt`

```kotlin
val MIGRATION_FROM_XX_TO_YY = object : Migration(XX, YY) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS offline_operations (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                operation_type TEXT NOT NULL,
                entity_id INTEGER NOT NULL,
                entity_data TEXT NOT NULL,
                business_profile_id INTEGER NOT NULL,
                timestamp_ms INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                retry_count INTEGER NOT NULL DEFAULT 0,
                error_message TEXT
            )
        """.trimIndent())
        
        // Add index for faster queries
        database.execSQL("CREATE INDEX idx_offline_ops_status ON offline_operations(status)")
        database.execSQL("CREATE INDEX idx_offline_ops_business ON offline_operations(business_profile_id)")
    }
}
```

Add to `AppDatabase.kt`:
```kotlin
addMigrations(
    // ...existing migrations...
    MIGRATION_FROM_XX_TO_YY
)
```

**Checklist:**
- [ ] Migration file created
- [ ] SQL syntax correct
- [ ] Indexes created for performance
- [ ] Added to AppDatabase migrations
- [ ] Database version incremented

---

### **Step 5: Write Unit Tests** ✅

**File to create:** `app/src/test/java/com/emul8r/bizap/data/local/dao/OfflineOperationDaoTest.kt`

```kotlin
@RunWith(RobolectricTestRunner::class)
class OfflineOperationDaoTest {
    
    private lateinit var db: AppDatabase
    private lateinit var dao: OfflineOperationDao
    
    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = db.offlineOperationDao()
    }
    
    @After
    fun tearDown() {
        db.close()
    }
    
    @Test
    fun testInsertOperation() = runBlocking {
        val op = OfflineOperation(
            operationType = "CREATE_INVOICE",
            entityId = 1L,
            entityData = "{}",
            businessProfileId = 1L
        )
        
        val id = dao.insert(op)
        assertThat(id).isGreaterThan(0)
    }
    
    @Test
    fun testGetPendingOperations() = runBlocking {
        val op1 = OfflineOperation(
            operationType = "CREATE_INVOICE",
            entityId = 1L,
            entityData = "{}",
            businessProfileId = 1L,
            status = "PENDING"
        )
        
        dao.insert(op1)
        val pending = dao.getPendingOperations(1L)
        assertThat(pending).hasSize(1)
    }
    
    @Test
    fun testUpdateStatus() = runBlocking {
        val op = OfflineOperation(
            operationType = "CREATE_INVOICE",
            entityId = 1L,
            entityData = "{}",
            businessProfileId = 1L
        )
        
        val id = dao.insert(op)
        dao.updateStatus(id, "SYNCED")
        
        val updated = dao.getById(id)
        assertThat(updated?.status).isEqualTo("SYNCED")
    }
}
```

**Checklist:**
- [ ] Test file created
- [ ] Uses Robolectric for Android runtime
- [ ] Tests all DAO methods
- [ ] Assertions verify behavior
- [ ] Tests compile and pass

---

### **Step 6: Build & Verify** ✅

Run these commands:

```bash
# Clean and build
./gradlew clean compileDebugKotlin

# Run tests
./gradlew testDebugUnitTest

# Check for errors
# Look for: "BUILD SUCCESSFUL"
```

**Checklist:**
- [ ] `./gradlew compileDebugKotlin` → SUCCESS
- [ ] `./gradlew testDebugUnitTest` → All tests pass
- [ ] No new compilation errors
- [ ] Database migration valid

---

### **Step 7: Commit & Push** ✅

```bash
git add -A
git commit -m "Phase 2 Day 1: Create OfflineOperation entity and DAO

- Created OfflineOperation entity with all required fields
- Created OfflineOperationDao with CRUD operations
- Added database migration for offline_operations table
- Added performance indexes
- Created comprehensive unit tests

Tests: All passing
Build: Clean compilation"

git push origin main
```

**Checklist:**
- [ ] Files staged and committed
- [ ] Commit message clear
- [ ] Pushed to GitHub

---

## 📊 SUCCESS METRICS FOR DAY 1

```
Code:
[✅] OfflineOperation.kt created
[✅] OfflineOperationDao.kt created
[✅] Database migration added
[✅] AppDatabase updated

Tests:
[✅] DAO tests passing (5+ tests)
[✅] Build compiling clean
[✅] No regressions in existing tests

Commits:
[✅] All changes pushed to GitHub
[✅] Clear commit message
```

---

## ⏱️ TIME BREAKDOWN

- OfflineOperation entity: 15-20 min
- OfflineOperationDao: 20-30 min
- Database migration: 15-20 min
- Unit tests: 30-45 min
- Build, test, commit: 20-30 min

**Total: 2-2.5 hours**

---

## 🎯 READY TO START?

Tomorrow morning (March 8, 2026):

1. Open Android Studio
2. Navigate to `app/src/main/java/com/emul8r/bizap/data/local/`
3. Create `entities/OfflineOperation.kt`
4. Create `dao/OfflineOperationDao.kt`
5. Follow the code above
6. Build and test
7. Commit and push
8. You're done for Day 1! 🎉

---

## 💡 TIPS

- Start with entity first (simplest)
- Copy patterns from `InvoiceEntity.kt` and `InvoiceDao.kt`
- Use Room's SQL validation in Android Studio
- Run tests frequently to catch issues early
- If stuck, reference PHASE_2_IMPLEMENTATION_GUIDE.md

---

## 🚀 YOU'VE GOT THIS!

Phase 1 foundation is solid. Day 1 is straightforward.

By end of today: Database layer is ready for business logic.

**Let's build offline sync!** 💪

---

**Day 1 Status:** Ready to begin  
**Difficulty:** Easy  
**Confidence:** High (copy-paste patterns from Phase 1)  


