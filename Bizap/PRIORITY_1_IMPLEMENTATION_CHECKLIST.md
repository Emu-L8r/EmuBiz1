# 📋 BIZAP PROJECT - PRIORITY 1 IMPLEMENTATION CHECKLIST

**Target Completion:** 2 weeks  
**Estimated Effort:** 25 hours  
**Expected Impact:** Production-ready foundation

---

## ✅ TASK 1: ADD TIMBER + FIREBASE CRASHLYTICS (2 hours)

### **Step 1.1: Add Dependencies**
- [ ] Open `gradle/libs.versions.toml`
- [ ] Add: `timber = "5.0.1"`
- [ ] Add: `firebase-bom = "33.0.0"`
- [ ] Add to `[libraries]`:
  ```toml
  timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
  firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase-bom" }
  firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics-ktx" }
  ```

### **Step 1.2: Update build.gradle.kts**
- [ ] Add `id("com.google.gms.google-services") version "4.4.0"` to plugins
- [ ] Add `id("com.google.firebase.crashlytics") version "3.0.1"` to plugins
- [ ] Add to dependencies:
  ```kotlin
  implementation(libs.timber)
  implementation(platform(libs.firebase-bom))
  implementation(libs.firebase-crashlytics)
  ```

### **Step 1.3: Create CrashlyticsTree**
- [ ] Create new file: `app/src/main/java/com/emul8r/bizap/util/CrashlyticsTree.kt`
- [ ] Content:
  ```kotlin
  package com.emul8r.bizap.util
  
  import android.util.Log
  import com.google.firebase.crashlytics.FirebaseCrashlytics
  import timber.log.Timber
  
  class CrashlyticsTree : Timber.Tree() {
      override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
          if (priority == Log.VERBOSE || priority == Log.DEBUG) return
          
          val crashlytics = FirebaseCrashlytics.getInstance()
          crashlytics.log("[$tag] $message")
          
          if (t != null) {
              crashlytics.recordException(t)
          }
      }
  }
  ```

### **Step 1.4: Initialize in Application Class**
- [ ] Open `app/src/main/java/com/emul8r/bizap/BizapApplication.kt`
- [ ] Add initialization:
  ```kotlin
  override fun onCreate() {
      super.onCreate()
      
      if (BuildConfig.DEBUG) {
          Timber.plant(Timber.DebugTree())
      } else {
          Timber.plant(CrashlyticsTree())
      }
  }
  ```

### **Step 1.5: Add Firebase Config**
- [ ] Download `google-services.json` from Firebase Console
- [ ] Place in `app/` directory
- [ ] Verify: File exists at `app/google-services.json`

### **Step 1.6: Test**
- [ ] Run: `./gradlew clean assembleDebug`
- [ ] Verify: No Firebase errors
- [ ] Deploy and check: Firebase Console shows app connected

---

## ✅ TASK 2: CREATE TEST FOUNDATION (2 hours)

### **Step 2.1: Add Test Dependencies**
- [ ] Update `gradle/libs.versions.toml`:
  ```toml
  mockk = "1.13.10"
  coroutines-test = "1.7.1"
  arch-core-test = "2.2.0"
  robolectric = "4.11.1"
  ```

### **Step 2.2: Update build.gradle.kts**
- [ ] Add to dependencies:
  ```kotlin
  testImplementation(libs.junit)
  testImplementation("io.mockk:mockk:1.13.10")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
  testImplementation("androidx.arch.core:core-testing:2.2.0")
  testImplementation("org.robolectric:robolectric:4.11.1")
  ```

### **Step 2.3: Create Test Helpers**
- [ ] Create: `app/src/test/java/com/emul8r/bizap/util/TestDispatchers.kt`
- [ ] Create: `app/src/test/java/com/emul8r/bizap/util/TestFactories.kt`

### **Step 2.4: Verify Setup**
- [ ] Run: `./gradlew test`
- [ ] Result: No errors (0 tests = success)

---

## ✅ TASK 3: WRITE CRITICAL UNIT TESTS (8 hours)

### **Test 3.1: InvoiceMapperTest** (1 hour)
- [ ] Create: `app/src/test/java/com/emul8r/bizap/data/mapper/InvoiceMapperTest.kt`
- [ ] Tests needed:
  - [ ] `testInvoiceToEntityMapping()` - Verify all fields map
  - [ ] `testEntityToInvoiceDomainMapping()` - Verify reverse mapping
  - [ ] `testRoundTripMapping()` - Verify entity→domain→entity
  - [ ] `testBusinessProfileIdPreserved()` - Critical for scoping

### **Test 3.2: CustomerMapperTest** (1 hour)
- [ ] Create: `app/src/test/java/com/emul8r/bizap/data/mapper/CustomerMapperTest.kt`
- [ ] Same structure as Invoice mapper

### **Test 3.3: CurrencyMapperTest** (0.5 hour)
- [ ] Create: `app/src/test/java/com/emul8r/bizap/data/mapper/CurrencyMapperTest.kt`

### **Test 3.4: InvoiceRepositoryTest** (2 hours)
- [ ] Create: `app/src/test/java/com/emul8r/bizap/data/repository/InvoiceRepositoryTest.kt`
- [ ] Mock: InvoiceDao, BusinessProfileRepository
- [ ] Tests:
  - [ ] `testGetInvoicesByBusinessId()` - Verify scoping
  - [ ] `testSaveInvoice()` - Verify persistence
  - [ ] `testDeleteInvoice()` - Verify deletion

### **Test 3.5: MultiBusinessIsolationTest** (2 hours)
- [ ] Create: `app/src/test/java/com/emul8r/bizap/feature/MultiBusinessIsolationTest.kt`
- [ ] Tests:
  - [ ] `testVaultEmptyWhenSwitchingBusiness()` - Scoping
  - [ ] `testInvoiceSequenceIsolatedPerBusiness()` - Sequences
  - [ ] `testReactiveRefreshOnBusinessSwitch()` - Reactivity

### **Test 3.6: ValidationLayerTest** (1.5 hours)
- [ ] Create: `app/src/test/java/com/emul8r/bizap/domain/validation/ValidationTest.kt`

---

## ✅ TASK 4: REMOVE fallbackToDestructiveMigration() (1 hour)

### **Step 4.1: Create Migration v11→v12**
- [ ] Create: `app/src/main/java/com/emul8r/bizap/data/local/migrations/Migration_11_12.kt`
- [ ] Content:
  ```kotlin
  val MIGRATION_11_12 = object : Migration(11, 12) {
      override fun migrate(database: SupportSQLiteDatabase) {
          // No schema changes in this migration
          // Just verifying data integrity
          database.query("SELECT COUNT(*) FROM invoices").use { cursor ->
              if (cursor.moveToFirst()) {
                  Log.i("Migration", "Invoices preserved: ${cursor.getInt(0)}")
              }
          }
      }
  }
  ```

### **Step 4.2: Update AppDatabase**
- [ ] Open `AppDatabase.kt`
- [ ] Change: `version = 12`
- [ ] Add migration: `.addMigrations(MIGRATION_11_12)`
- [ ] Remove: `.fallbackToDestructiveMigration()`

### **Step 4.3: Test Migration**
- [ ] Run app on existing database (v11)
- [ ] Verify: No data loss
- [ ] Check logcat: "Invoices preserved: X" message

---

## ✅ TASK 5: ADD INPUT VALIDATION LAYER (4 hours)

### **Step 5.1: Create Validation Domain Model**
- [ ] Create: `app/src/main/java/com/emul8r/bizap/domain/validation/ValidationError.kt`
- [ ] Create: `app/src/main/java/com/emul8r/bizap/domain/validation/InvoiceValidator.kt`
- [ ] Create: `app/src/main/java/com/emul8r/bizap/domain/validation/CustomerValidator.kt`

### **Step 5.2: Implement Validators**
- [ ] InvoiceValidator should validate:
  - [ ] Amount > 0
  - [ ] Amount < 1,000,000
  - [ ] Description not empty
  - [ ] At least 1 line item
  - [ ] All line items valid

- [ ] CustomerValidator should validate:
  - [ ] Name not empty (1-100 chars)
  - [ ] Email valid format (if provided)
  - [ ] Phone valid (if provided)

### **Step 5.3: Integrate in ViewModels**
- [ ] Open `CreateInvoiceViewModel.kt`
- [ ] Add: `@Inject lateinit var invoiceValidator: InvoiceValidator`
- [ ] In save: Call `invoiceValidator.validate()`
- [ ] Show error if validation fails

### **Step 5.4: Add UI Error Display**
- [ ] Update `CreateInvoiceScreen.kt`
- [ ] Add error Snackbar for validation failures

### **Step 5.5: Add Tests**
- [ ] Create: `app/src/test/java/com/emul8r/bizap/domain/validation/ValidationTest.kt`
- [ ] Test all validation rules

---

## ✅ TASK 6: ADD API ERROR HANDLING (3 hours)

### **Step 6.1: Create Error Types**
- [ ] Create: `app/src/main/java/com/emul8r/bizap/data/error/ApiError.kt`
- [ ] Errors:
  - [ ] `NetworkError`
  - [ ] `RateLimitError`
  - [ ] `ServerError`
  - [ ] `UnknownError`

### **Step 6.2: Create Retrofit Interceptor**
- [ ] Create: `app/src/main/java/com/emul8r/bizap/data/network/ErrorInterceptor.kt`
- [ ] Handle:
  - [ ] HTTP 429 (rate limit)
  - [ ] HTTP 5xx (server errors)
  - [ ] Network timeouts

### **Step 6.3: Add Retry Logic**
- [ ] Update currency rate fetching
- [ ] Add: Exponential backoff (max 3 retries)
- [ ] Add: Graceful fallback to cached rates

### **Step 6.4: Update ViewModel Error Handling**
- [ ] Catch API errors
- [ ] Show user-friendly messages
- [ ] Log with Timber

### **Step 6.5: Add Tests**
- [ ] Test rate limit handling
- [ ] Test network timeout handling
- [ ] Test retry logic

---

## ✅ TASK 7: INCREASE TEST COVERAGE TO 25% (4 hours)

### **Step 7.1: Run Coverage Report**
- [ ] Run: `./gradlew testDebugUnitTest --no-build-cache`
- [ ] Check: Current coverage %
- [ ] Goal: Reach 25%

### **Step 7.2: Priority Tests**
- [ ] Data layer (mappers, DAOs) - 10 tests
- [ ] Domain validation - 5 tests
- [ ] Repository isolation - 3 tests

### **Step 7.3: Create Coverage Dashboard**
- [ ] Use Jacoco for coverage tracking
- [ ] Document: Line coverage percentage

---

## ✅ VERIFICATION STEPS

### **After Each Task:**
- [ ] Code compiles: `./gradlew clean build`
- [ ] No errors in logcat
- [ ] Tests pass: `./gradlew test`
- [ ] Firebase connection verified (for Task 1)

### **Final Verification:**
- [ ] All Priority 1 tasks complete
- [ ] Build: ✅ SUCCESS
- [ ] Tests: ✅ PASS
- [ ] Coverage: ✅ 25%+
- [ ] Logging: ✅ WORKING
- [ ] Error Handling: ✅ WORKING

---

## 📊 COMPLETION TRACKING

| Task | Est. Hours | Status | Completed |
|------|-----------|--------|-----------|
| 1. Logging (Timber + Crashlytics) | 2 | ⬜ | |
| 2. Test Foundation | 2 | ⬜ | |
| 3. Critical Unit Tests | 8 | ⬜ | |
| 4. Remove Destructive Migration | 1 | ⬜ | |
| 5. Input Validation | 4 | ⬜ | |
| 6. API Error Handling | 3 | ⬜ | |
| 7. Coverage to 25% | 4 | ⬜ | |
| **TOTAL** | **24** | | |

---

## 🎯 SUCCESS CRITERIA

After completing all Priority 1 tasks:

✅ **App has logging** - Every error logged to Firebase  
✅ **App has tests** - 25% coverage (critical paths)  
✅ **App handles errors** - No silent failures  
✅ **Data is validated** - Invalid data rejected before save  
✅ **Migrations are safe** - No destructive wipes  
✅ **API failures graceful** - Fallback to cached data  

**Result: Production-ready foundation** 🚀

---

**Status:** Ready for implementation  
**Next Review:** After Task 1 complete  
**Support:** Contact: [Your contact here]


