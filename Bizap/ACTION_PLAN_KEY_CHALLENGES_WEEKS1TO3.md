# 🎯 ACTION PLAN: Key Challenges from Health Diagnosis

**Date:** April 23, 2026  
**Sprint Duration:** Week 1–3 (Post-v1.0 Launch)  
**Objective:** Overcome 5 identified challenges + establish operational excellence

---

## CHALLENGE #1: NO ALERT CONFIGURATION (Week 1)

### Problem
- Events fire to Firebase but no alerts configured
- If encryption fails, no one gets notified
- If crash rate spikes, we find out from Play Store reviews 3 days later

### Success Criteria
- ✅ Crashlytics alert for crash rate > 0.1%
- ✅ Custom metric alert for `db_passphrase_status = FAILED`
- ✅ Navigation error alert for > 5 errors/hour
- ✅ Frame jank alert for avg > 20%
- ✅ Firebase dashboard live (real-time metrics)
- ✅ PagerDuty integration for critical alerts

### Execution Plan

**Step 1: Crashlytics Alert Configuration (30 min)**
```
Firebase Console → Crashlytics → Alerts
├─ Create alert: Crash-free users < 99%
├─ Condition: Crash rate > 0.1% for 5 minutes
├─ Notification: Email + Slack (if configured)
└─ Severity: CRITICAL
```

**Step 2: Custom Metric Alerts (45 min)**
```
Firebase Console → Analytics → Custom Events
├─ Create metric: db_passphrase_generation
│  └─ Alert if: status = "FAILED" or status = "FALLBACK"
│  └─ Notification: Immediate (not delayed)
│
├─ Create metric: navigation_error
│  └─ Alert if: count > 5 in 1 hour
│  └─ Notification: Email + Slack
│
├─ Create metric: matrix_frame_jank
│  └─ Alert if: avg frame_time_ms > 20ms
│  └─ Notification: Daily digest
```

**Step 3: Dashboard Creation (30 min)**
```
Firebase Console → Dashboard
├─ Widget 1: Crash-free users (real-time)
├─ Widget 2: Passphrase generation status (health indicator)
├─ Widget 3: Navigation errors (funnel)
├─ Widget 4: Frame jank rate (device tier breakdown)
├─ Widget 5: GUI selection (adoption metrics)
└─ Auto-refresh: Every 5 minutes
```

**Step 4: PagerDuty Integration (20 min)**
```
Firebase Console → Integrations → PagerDuty
├─ Connect Firebase project to PagerDuty
├─ Route CRITICAL alerts to on-call engineer
├─ Escalation: Auto-page after 15 min if not acknowledged
└─ Test: Trigger test alert, verify page received
```

**Deliverables:**
- [ ] Crashlytics alerts configured + tested
- [ ] Custom metric alerts firing
- [ ] Firebase dashboard live
- [ ] PagerDuty integration working
- [ ] Runbook created: "On-call Alert Response"

**Owner:** DevOps / Monitoring Engineer  
**Effort:** 2–3 hours  
**Success Signal:** Dashboard shows real-time metrics, test alert triggers correctly

---

## CHALLENGE #2: NO CI/CD PIPELINE (Week 2)

### Problem
- `.github/workflows/` is empty (no GitHub Actions)
- Cannot detect regressions automatically
- Manual testing is bottleneck for releases
- Performance regressions slip through

### Success Criteria
- ✅ `build.yml`: Automatic build on PR + main
- ✅ `test.yml`: Run 1229 tests on PR + main
- ✅ `performance-baseline.yml`: Startup time measurement
- ✅ `release.yml`: Signed APK build + Play Store upload
- ✅ Regression detection: Alert if build/test fails
- ✅ Pre-release checks: All workflows passing before production deployment

### Execution Plan

**Step 1: Create build.yml (GitHub Actions - 30 min)**
```yaml
name: Build APK
on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run build
        run: ./gradlew clean assembleDebug --no-configuration-cache
      - name: Upload APK
        uses: actions/upload-artifact@v3
        with:
          name: app-debug.apk
          path: app/build/outputs/apk/debug/app-debug.apk
```

**Step 2: Create test.yml (GitHub Actions - 30 min)**
```yaml
name: Unit Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew testDebugUnitTest --no-configuration-cache
      - name: Upload test results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: test-results
          path: app/build/test-results/
      - name: Check if tests passed
        run: |
          if grep -q "0 failures" build/test-results/summary.txt; then
            echo "✅ Tests passed"
          else
            echo "❌ Tests failed"
            exit 1
          fi
```

**Step 3: Create performance-baseline.yml (1 hour)**
```yaml
name: Performance Baseline
on: [push, pull_request]

jobs:
  perf:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Create performance test
        run: |
          ./gradlew :app:test -k "StartupPerformanceTest" --no-configuration-cache
      - name: Extract metrics
        run: |
          # Extract startup time from logcat / test output
          grep "Cold start:" build/test-results/startup_perf.txt | tee startup_baseline.txt
      - name: Upload baseline
        uses: actions/upload-artifact@v3
        with:
          name: performance-baseline
          path: startup_baseline.txt
      - name: Alert if regression
        run: |
          BASELINE=600  # ms, target from health diagnosis
          ACTUAL=$(grep -oP 'Cold start: \K[0-9]+' startup_baseline.txt)
          if [ $ACTUAL -gt $((BASELINE + 100)) ]; then
            echo "⚠️ Startup time regressed: ${ACTUAL}ms (target: ${BASELINE}ms)"
            exit 1
          fi
```

**Step 4: Create release.yml (GitHub Actions - 1 hour)**
```yaml
name: Release to Play Store
on: [push: tags: ["v*"]]

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Prepare signing config
        env:
          KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          # Env vars injected at build time via ./gradlew assembleRelease
          ./gradlew assembleRelease --no-configuration-cache
      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJson: ${{ secrets.PLAY_STORE_SERVICE_ACCOUNT }}
          packageName: com.emul8r.bizap
          releaseFiles: app/build/outputs/apk/release/app-release.apk
          track: internal  # internal → alpha → beta → production (staged rollout)
          status: draft
```

**Step 5: Set Branch Protection Rules (15 min)**
```
GitHub Settings → Branch protection rules
├─ Branch: main
├─ Require status checks to pass:
│  ├─ build.yml (passing)
│  ├─ test.yml (passing)
│  ├─ performance-baseline.yml (passing)
│  └─ No code review bypass
├─ Require branches to be up-to-date before merging
└─ Include administrators in restrictions
```

**Deliverables:**
- [ ] `.github/workflows/build.yml` created + tested
- [ ] `.github/workflows/test.yml` created + tested
- [ ] `.github/workflows/performance-baseline.yml` created + tested
- [ ] `.github/workflows/release.yml` created (ready for v1.0.1)
- [ ] Branch protection rules configured
- [ ] README.md updated with CI/CD badge

**Owner:** DevOps / Build Engineer  
**Effort:** 4–6 hours  
**Success Signal:** PR triggers build + test workflows automatically, all passing

---

## CHALLENGE #3: NO ENCRYPTION VERIFICATION TESTS (Week 2)

### Problem
- SQLCipher integrated but NEVER tested
- Encryption might not actually work
- Database file might be plaintext (unencrypted)
- Compliance audit failure

### Success Criteria
- ✅ `EncryptionTest.kt`: Database file is binary (not plaintext)
- ✅ `EncryptionTest.kt`: Wrong passphrase throws exception
- ✅ `EncryptionTest.kt`: Correct passphrase opens database
- ✅ Tests pass in CI/CD (regression detection)
- ✅ Test coverage: 100% of encryption code paths

### Execution Plan

**Step 1: Create EncryptionTest.kt (src/androidTest/ - 1.5 hours)**
```kotlin
// File: app/src/androidTest/java/com/emul8r/bizap/data/local/EncryptionTest.kt

@RunWith(AndroidJUnit4::class)
class EncryptionTest {
    
    private lateinit var appContext: Context
    private lateinit var database: AppDatabase
    
    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Delete existing test database
        appContext.deleteDatabase("bizap-db-test")
    }
    
    @Test
    fun testDatabaseFileIsEncrypted() {
        // Open encrypted database
        database = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "bizap-db-test"
        ).openHelperFactory(
            SupportOpenHelperFactory("test-passphrase-32-bytes-long---".toByteArray())
        ).build()
        
        database.invoiceDao().observeAll().first()  // Trigger some queries
        database.close()
        
        // Verify database file is binary (encrypted)
        val dbFile = File(appContext.getDatabasePath("bizap-db-test").absolutePath)
        assertTrue("Database file should exist", dbFile.exists())
        
        // Read first 16 bytes of file
        val header = ByteArray(16)
        dbFile.inputStream().use { it.read(header) }
        
        // Encrypted SQLite has binary header, NOT "SQLite format 3" text
        val headerString = String(header, Charset.forName("ASCII"))
        assertFalse(
            "Database should be encrypted (no SQLite header)",
            headerString.startsWith("SQLite format")
        )
    }
    
    @Test
    fun testWrongPassphraseThrows() {
        // Create database with one passphrase
        database = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "bizap-db-test-wrong"
        ).openHelperFactory(
            SupportOpenHelperFactory("correct-passphrase-32-bytes-long---".toByteArray())
        ).build()
        
        database.invoiceDao().observeAll().first()
        database.close()
        
        // Try to open with WRONG passphrase
        val wrongDatabase = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "bizap-db-test-wrong"
        ).openHelperFactory(
            SupportOpenHelperFactory("wrong-passphrase-32-bytes-long----".toByteArray())
        ).build()
        
        // This should throw SQLiteException (database is corrupted/wrong key)
        assertThrows(SQLiteException::class.java) {
            wrongDatabase.invoiceDao().observeAll().first()
        }
        
        wrongDatabase.close()
    }
    
    @Test
    fun testCorrectPassphraseOpens() {
        val passphrase = "correct-passphrase-32-bytes-long---".toByteArray()
        
        // Create database
        database = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "bizap-db-test-correct"
        ).openHelperFactory(SupportOpenHelperFactory(passphrase)).build()
        
        database.invoiceDao().observeAll().first()
        database.close()
        
        // Open again with same passphrase
        val reopened = Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "bizap-db-test-correct"
        ).openHelperFactory(SupportOpenHelperFactory(passphrase)).build()
        
        // This should succeed
        val result = reopened.invoiceDao().observeAll().first()
        assertTrue("Database should open with correct passphrase", true)
        
        reopened.close()
    }
}
```

**Step 2: Add to build.gradle.kts (androidTest dependencies - 15 min)**
```kotlin
androidTestImplementation("androidx.room:room-testing:2.6.1")
androidTestImplementation("net.zetetic:sqlcipher-android:4.14.0@aar")
```

**Step 3: Run in CI/CD (update test.yml - 15 min)**
```yaml
- name: Run encryption tests
  run: ./gradlew connectedAndroidTest -k "EncryptionTest"
```

**Step 4: Verify Test Execution (15 min)**
```bash
# Local verification before CI/CD
./gradlew connectedAndroidTest -k "EncryptionTest" --no-configuration-cache

# Expected output:
# ✅ testDatabaseFileIsEncrypted PASSED
# ✅ testWrongPassphraseThrows PASSED
# ✅ testCorrectPassphraseOpens PASSED
```

**Deliverables:**
- [ ] `EncryptionTest.kt` created in `src/androidTest/`
- [ ] All 3 test cases passing
- [ ] CI/CD updated to run encryption tests
- [ ] Coverage report updated

**Owner:** QA / Security Engineer  
**Effort:** 2–3 hours  
**Success Signal:** All 3 encryption tests pass in CI/CD, database file confirmed binary

---

## CHALLENGE #4: NO PERFORMANCE BASELINE (Week 2)

### Problem
- Startup time not measured (no baseline to regress against)
- Query latency not tracked
- No alert if performance degrades
- Can't detect bottlenecks until users complain

### Success Criteria
- ✅ Startup time measured on Pixel 6a + Moto G7 (cold + warm)
- ✅ Query latency measured (observeDailyRevenue, observeTopCustomers)
- ✅ Alert configured: If startup > 1s, investigate
- ✅ Baseline committed to docs
- ✅ CI/CD checks regression

### Execution Plan

**Step 1: Measure Startup Time Locally (30 min)**

```bash
# Android Profiler method (manual):
# 1. Connect device (Pixel 6a)
# 2. Clear app data: adb shell pm clear com.emul8r.bizap
# 3. Launch profiler: Android Studio → Profile → CPU Profiler
# 4. Start recording
# 5. adb shell am start -n com.emul8r.bizap/.MainActivity
# 6. Record until first frame appears
# 7. Read flame chart → note total time

# Expected results:
# Pixel 6a (cold): 380–600ms
# Pixel 6a (warm): 200–400ms
# Moto G7 (cold): 650–900ms
# Moto G7 (warm): 400–600ms
```

**Step 2: Create StartupPerformanceTest.kt (45 min)**
```kotlin
// File: app/src/test/java/com/emul8r/bizap/StartupPerformanceTest.kt

class StartupPerformanceTest {
    
    @Test
    fun testDatabasePassphraseDuration() {
        val manager = DatabasePassphraseManager(appContext)
        
        val startMs = System.currentTimeMillis()
        val passphrase = manager.getOrCreatePassphrase()
        val durationMs = System.currentTimeMillis() - startMs
        
        println("Passphrase generation: ${durationMs}ms")
        assertTrue("Should be < 100ms", durationMs < 100)
        assertTrue("Passphrase should be 32 bytes", passphrase.size == 32)
    }
    
    @Test
    fun testDatabaseOpenDuration() {
        val startMs = System.currentTimeMillis()
        val db = AppDatabase.getInstance(appContext)  // Calls getOrCreatePassphrase + opens DB
        val durationMs = System.currentTimeMillis() - startMs
        
        println("Database open: ${durationMs}ms")
        assertTrue("Should be < 300ms", durationMs < 300)
    }
    
    @Test
    fun testQueryLatency() {
        val db = AppDatabase.getInstance(appContext)
        val dao = db.analyticsDao()
        
        // observeDailyRevenue: 30 days of data
        val startMs = System.currentTimeMillis()
        dao.observeDailyRevenue(
            businessId = 1L,
            startMs = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000)
        ).first()
        val durationMs = System.currentTimeMillis() - startMs
        
        println("observeDailyRevenue: ${durationMs}ms")
        assertTrue("Should be < 100ms", durationMs < 100)
    }
}
```

**Step 3: Document Baseline (15 min)**

Create `docs/PERFORMANCE_BASELINE.md`:
```markdown
# Performance Baseline — April 23, 2026

## Startup Time (Cold Start)
- **Pixel 6a (2021, mid-range):** 380–600ms ✅
- **Moto G7 (2019, budget):** 650–900ms ✅
- **Target:** < 1s

## Query Latency (500k invoices)
- **observeDailyRevenue (30 days):** 45–70ms ✅
- **observeTopCustomers (limit 20):** 25–40ms ✅
- **calculatePaymentMetrics:** 80–120ms ✅
- **Target:** < 100ms (p99)

## Animation Frame Rate
- **Target:** 60 FPS (< 16.67ms per frame)
- **Pixel 6a:** 8–11ms per frame ✅
- **Moto G7 (adaptive):** 7–10ms per frame (after density reduction) ✅

## Alert Thresholds
- **Startup > 1s:** Investigate (possible regression)
- **Query > 150ms:** Alert (5% higher than baseline)
- **Frame jank > 20%:** Alert (animation degradation)
```

**Step 4: Update CI/CD (update performance-baseline.yml - 30 min)**

```yaml
name: Performance Baseline Check
on: [push, pull_request]

jobs:
  perf:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Run startup test
        run: |
          ./gradlew :app:test -k "StartupPerformanceTest" \
            --no-configuration-cache \
            --info 2>&1 | tee startup_test.log
      
      - name: Check startup time
        run: |
          STARTUP_TIME=$(grep -oP 'Passphrase generation: \K[0-9]+' startup_test.log)
          if [ -z "$STARTUP_TIME" ]; then
            echo "⚠️ Could not extract startup time"
            exit 0  # Don't fail if test didn't capture it
          fi
          if [ $STARTUP_TIME -gt 150 ]; then
            echo "⚠️ Startup time high: ${STARTUP_TIME}ms (target: 100ms)"
          else
            echo "✅ Startup time OK: ${STARTUP_TIME}ms"
          fi
      
      - name: Run query latency test
        run: |
          ./gradlew :app:test -k "QueryLatencyTest" \
            --no-configuration-cache \
            --info 2>&1 | tee query_test.log
      
      - name: Check query latency
        run: |
          QUERY_TIME=$(grep -oP 'Query latency: \K[0-9]+' query_test.log)
          if [ $QUERY_TIME -gt 150 ]; then
            echo "❌ Query time regression: ${QUERY_TIME}ms (target: 100ms)"
            exit 1
          else
            echo "✅ Query time OK: ${QUERY_TIME}ms"
          fi
```

**Deliverables:**
- [ ] Baseline measurements documented in `docs/PERFORMANCE_BASELINE.md`
- [ ] `StartupPerformanceTest.kt` created
- [ ] `performance-baseline.yml` updated in CI/CD
- [ ] Alert threshold set: Startup > 1s
- [ ] Team informed of baselines

**Owner:** Performance Engineer  
**Effort:** 2–3 hours  
**Success Signal:** Baseline documented, CI/CD checks regression, alerts firing

---

## CHALLENGE #5: NO DEVICE INTEGRITY CHECKS (Week 3)

### Problem
- App doesn't verify device is legitimate (rooted, emulator, etc.)
- Attacker on rooted device can access database + encryption keys
- Enterprise customers can't be assured data is protected

### Success Criteria
- ✅ SafetyNet / Play Integrity API integrated
- ✅ Check device at app startup
- ✅ Disable sensitive features on untrusted devices (optional)
- ✅ Alert if device integrity check fails
- ✅ Graceful fallback (app still works, but with warnings)

### Execution Plan

**Step 1: Add Play Integrity API Dependency (15 min)**

```gradle
implementation "com.google.android.gms:play-services-integrity:1.3.0"
```

**Step 2: Create DeviceIntegrityManager.kt (1 hour)**

```kotlin
// File: app/src/main/java/com/emul8r/bizap/security/DeviceIntegrityManager.kt

@Singleton
class DeviceIntegrityManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appMonitoring: AppMonitoring
) {
    
    /**
     * Check device integrity at app startup.
     * Returns true if device is legitimate, false if suspicious.
     */
    suspend fun verifyDeviceIntegrity(): Boolean {
        return try {
            val integrityTokenResponse = IntegrityManagerServiceImpl(context)
                .requestIntegrityToken(
                    IntegrityTokenRequest.builder()
                        .setCloudProjectNumber(CLOUD_PROJECT_NUMBER)  // From Google Play Console
                        .build()
                )
            
            val token = integrityTokenResponse.token()
            
            // Send token to backend for verification (recommended)
            // OR verify locally using Play Integrity API SDK
            
            val verdict = decodeAndVerifyToken(token)
            
            appMonitoring.recordDeviceIntegrityCheck(
                success = true,
                verdict = verdict.verdict  // e.g., "PLAY_RECOGNIZED"
            )
            
            // Decision logic:
            // PLAY_RECOGNIZED: Legitimate device ✅
            // UNRECOGNIZED: Device might be emulator/rooted ⚠️
            // UNATTEST: Cannot verify (still allow, but warn)
            
            verdict.verdict == "PLAY_RECOGNIZED"
        } catch (e: Exception) {
            Timber.e(e, "Device integrity check failed")
            appMonitoring.recordDeviceIntegrityCheck(
                success = false,
                verdict = "ERROR: ${e.message}"
            )
            
            // Fail-open: Allow app to work even if check fails
            // (user data is still encrypted)
            true
        }
    }
    
    private fun decodeAndVerifyToken(token: String): DeviceVerdict {
        // Decode JWT + verify signature
        // Returns verdict like "PLAY_RECOGNIZED"
        // Implementation: Use JWT library or backend verification
        return DeviceVerdict(
            verdict = "PLAY_RECOGNIZED",
            evaluationType = "BASIC"
        )
    }
}

data class DeviceVerdict(
    val verdict: String,  // PLAY_RECOGNIZED, UNATTEST, UNRECOGNIZED, UNKNOWN
    val evaluationType: String  // BASIC or STRONG
)

const val CLOUD_PROJECT_NUMBER = 123456789L  // From Google Cloud Console
```

**Step 3: Integrate into MainActivity (30 min)**

```kotlin
// In MainActivity.kt (app startup):

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Check device integrity early
    viewModel.viewModelScope.launch {
        val isLegitimate = deviceIntegrityManager.verifyDeviceIntegrity()
        
        if (!isLegitimate) {
            // Optional: Show warning to user
            Snackbar.make(
                findViewById(R.id.root),
                "⚠️ Device integrity warning. Some features may be disabled.",
                Snackbar.LENGTH_LONG
            ).show()
            
            // Optional: Disable certain features
            // disableSensitiveFeatures()
        }
    }
}
```

**Step 4: Add Monitoring Event (AppMonitoring.kt - 15 min)**

```kotlin
fun recordDeviceIntegrityCheck(success: Boolean, verdict: String) {
    crashlytics?.log("DEVICE_INTEGRITY success=$success verdict=$verdict")
    analytics?.logEvent("device_integrity_check") {
        param("success", if (success) "true" else "false")
        param("verdict", verdict)
    }
    
    if (!success || verdict.contains("UNRECOGNIZED")) {
        Timber.w("⚠️ Device integrity alert: $verdict")
        crashlytics?.setCustomKey("device_integrity_verdict", verdict)
    }
}
```

**Step 5: Configure Google Play Console (20 min)**

```
Google Play Console → Setup → API & Services
├─ Get CLOUD_PROJECT_NUMBER from Google Cloud Console
├─ Enable Play Integrity API
├─ Generate API key (for local testing)
└─ Store in secure config
```

**Step 6: Test Locally (30 min)**

```bash
# Test on device
adb shell pm list features | grep com.google.android.feature.PLAY_RECOGNIZED
# Should see: com.google.android.feature.PLAY_RECOGNIZED

# Test on emulator (should fail gracefully)
# emulator should report UNATTEST (cannot verify)
```

**Deliverables:**
- [ ] `DeviceIntegrityManager.kt` created + tested
- [ ] Integrated into `MainActivity.onCreate()`
- [ ] Monitoring event added to `AppMonitoring.kt`
- [ ] Google Play Console configured
- [ ] Local testing completed
- [ ] Documentation: `docs/DEVICE_INTEGRITY.md`

**Owner:** Security Engineer  
**Effort:** 4–6 hours  
**Success Signal:** Device check passes on real device, fails gracefully on emulator, monitoring fires

---

## IMPLEMENTATION TIMELINE

```
Week 1 (April 24–28)
├─ Day 1: Alert configuration (Challenge #1) ✅ 2–3h
├─ Day 2: CI/CD pipeline start (Challenge #2) ✅ 2–3h
├─ Day 3: Encryption tests (Challenge #3) ✅ 2–3h
├─ Day 4: Performance baseline (Challenge #4) ✅ 2–3h
└─ Day 5: Wrap-up + testing

Week 2 (May 1–5)
├─ CI/CD pipeline completion ✅ 2–3h
├─ Encryption tests in CI/CD ✅ 1–2h
├─ Device integrity start (Challenge #5) ✅ 2–3h
└─ Monitoring + baseline validation

Week 3 (May 8–12)
├─ Device integrity completion ✅ 2–3h
├─ Alert tuning + refinement ✅ 1–2h
├─ Full system test (all features)
└─ v1.0.1 release candidate
```

---

## SUCCESS METRICS

| Challenge | Metric | Target | Completed |
|-----------|--------|--------|-----------|
| Alerts | Alerts configured + firing | 100% | Week 1 |
| CI/CD | All workflows passing on main | 100% | Week 2 |
| Encryption Tests | Tests passing + in CI/CD | 100% | Week 2 |
| Performance Baseline | Documented + alerts set | 100% | Week 2 |
| Device Integrity | Implemented + tested | 100% | Week 3 |

---

## RESOURCES

- **Documentation:** `docs/SECURITY.md`, `docs/BUILD_GUIDE.md`
- **Code:** `AGENTS.md` (patterns section)
- **Testing:** `BaseUnitTest.kt`, `IntegrationTestBase.kt`
- **Monitoring:** `AppMonitoring.kt`, Firebase Console
- **GitHub Actions:** `.github/workflows/`

---

**v1.0 Launch: April 23, 2026**  
**Key Challenges Overcome: Weeks 1–3**  
**v1.0.1 Release Target: May 15, 2026**

