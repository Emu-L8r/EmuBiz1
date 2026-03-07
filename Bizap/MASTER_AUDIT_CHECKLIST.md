# 🔍 BIZAP COMPREHENSIVE PROJECT HEALTH AUDIT

## MASTER VERIFICATION PROMPT

Copy this entire section and use it whenever you want to do a full health check:

---

# BIZAP FULL PROJECT HEALTH AUDIT - MASTER VERIFICATION PROMPT

**Date:** March 7, 2026  
**Project:** Emu-L8r/EmuBiz1 (Bizap - Invoice Management App)  
**Purpose:** Comprehensive verification that no critical issues exist before release

---

## PHASE 1: BUILD SYSTEM VERIFICATION

### 1.1 Gradle & AGP Configuration
- [ ] What is the current Gradle version? (Expected: 9.2.1)
- [ ] What is the current AGP version? (Expected: 8.7.3+)
- [ ] What is the Kotlin version? (Expected: 2.0.21+)
- [ ] Are there any compilation errors? (Expected: 0)
- [ ] Are there any blocking warnings? (Expected: 0, soft deprecations acceptable)
- [ ] Does `./gradlew clean build` succeed? (Expected: ✅ SUCCESS)
- [ ] Does `./gradlew testDebugUnitTest` succeed? (Expected: ✅ ALL TESTS PASS)
- [ ] What is the APK size? (Expected: <50MB, record actual)
- [ ] Build time acceptable? (Expected: <2 minutes for clean, <30s incremental)

### 1.2 Dependency Chain
- [ ] Are all dependencies explicitly declared in `gradle/libs.versions.toml`?
- [ ] Are there any unused dependencies? List them:
- [ ] Are there any transitive dependency conflicts?
- [ ] Is firebase-bom pinned to a stable version?
- [ ] Is Room database library included? (Expected: androidx.room:room-runtime)
- [ ] Is Hilt DI properly configured? (Expected: at app level only)
- [ ] Is KSP registered before Hilt plugin in app/build.gradle.kts?

### 1.3 Plugin Configuration
- [ ] Is `com.android.application` applied to app module only?
- [ ] Is `dagger.hilt.android` in app/build.gradle.kts (not root)?
- [ ] Is `google.ksp` in app/build.gradle.kts?
- [ ] Are Firebase plugins present? (google-services, firebase-crashlytics)
- [ ] Is kotlin-compose plugin registered?
- [ ] Is kotlin-serialization plugin registered?

### 1.4 SDK & Compatibility
- [ ] Compile SDK: 35? (Android 15)
- [ ] Target SDK: 35?
- [ ] Min SDK: 26? (Android 8.0+)
- [ ] Java compatibility: JDK 17?
- [ ] Kotlin target: 1.9+?
- [ ] Are API 35+ features properly gated with version checks?

---

## PHASE 2: ARCHITECTURE VERIFICATION

### 2.1 Project Structure
- [ ] Root: `Bizap/` directory exists?
- [ ] Source: `Bizap/app/src/main/java/com/emul8r/bizap/`?
- [ ] Tests: `Bizap/app/src/test/java/`?
- [ ] Resources: `Bizap/app/src/main/res/`?
- [ ] Database schemas: `Bizap/app/schemas/` exists?
- [ ] Gradle wrapper: `Bizap/gradle/wrapper/` exists?

### 2.2 Clean Architecture Layers
- [ ] **Data Layer:** `data/` with `local/`, `repository/`, `datasource/` subdirs?
- [ ] **Domain Layer:** `domain/` with `model/`, `repository/`, `usecase/`, `validation/`?
- [ ] **UI Layer:** `ui/` with `screens/`, `components/`, `viewmodel/`?
- [ ] **Main Entry:** `MainActivity.kt` exists?

### 2.3 Database Layer
- [ ] Room database class `AppDatabase.kt` exists?
- [ ] Database version documented? (Current: v25?)
- [ ] Migrations exist for all version jumps?
- [ ] All DAOs registered in AppDatabase?
- [ ] Schema JSON exported to `app/schemas/`?
- [ ] Is database migration v24→v25 properly registered?

### 2.4 Dependency Injection
- [ ] `@HiltAndroidApp` on MainActivity or custom Application class?
- [ ] Is Hilt ViewModel used for all ViewModels?
- [ ] Module files in `di/` directory?
- [ ] DatabaseModule properly configured?
- [ ] RepositoryModule properly configured?
- [ ] All @Provides methods documented?

### 2.5 Business Logic
- [ ] Validation logic in `domain/validation/`?
- [ ] InputValidator.kt exists with 8+ validation functions?
- [ ] Use cases in `domain/usecase/`?
- [ ] Result<T> pattern used for error handling?
- [ ] Are all error cases properly handled?

---

## PHASE 3: FEATURES & FUNCTIONALITY

### 3.1 Invoice Management
- [ ] Invoice creation working?
- [ ] Invoice editing working?
- [ ] Invoice deletion working?
- [ ] Invoice archiving working?
- [ ] Invoice status tracking (DRAFT, SENT, PAID, OVERDUE)?
- [ ] Invoice numbering format correct? (INV-YYYY-NNNNNN)
- [ ] Multi-business invoice isolation working?

### 3.2 Customer Management
- [ ] Customer creation working?
- [ ] Customer editing working?
- [ ] Customer deletion working?
- [ ] Customer list displays all customers?
- [ ] Customer validation (email, phone)?
- [ ] Multi-business customer isolation working?

### 3.3 Currency & Exchange Rates
- [ ] Exchange rate API integration working?
- [ ] Multi-currency support enabled?
- [ ] Real-time rate updates?
- [ ] Rate caching strategy implemented?
- [ ] Fallback handling if API unavailable?

### 3.4 PDF Generation
- [ ] PDF generation working?
- [ ] PDF text rendering (no overlap)?
- [ ] Business details included in PDF?
- [ ] Tax calculations included?
- [ ] Logo rendering working?

### 3.5 Analytics & Dashboards
- [ ] Analytics dashboard loading?
- [ ] Data refresh on business switch?
- [ ] Payment analytics working?
- [ ] Snapshot health checks in place?

### 3.6 Authentication & Security
- [ ] No hardcoded secrets in code?
- [ ] API keys in gradle.properties or environment?
- [ ] ProGuard/R8 minification configured for release?
- [ ] Data extraction rules configured?
- [ ] allowBackup=false in AndroidManifest.xml?

---

## PHASE 4: TESTING VERIFICATION

### 4.1 Unit Tests
- [ ] Total test count: _____ (Expected: 200+)
- [ ] Passing tests: _____% (Expected: 100%)
- [ ] Failed tests: _____ (Expected: 0)
- [ ] Coverage: ___% (Expected: ≥40%)
- [ ] Test frameworks present? (JUnit4, MockK, Coroutines Test, Robolectric)
- [ ] Unit tests for validation logic?
- [ ] Unit tests for repository layer?
- [ ] Unit tests for ViewModels?

### 4.2 Integration Tests
- [ ] End-to-end tests exist?
- [ ] Customer creation flow tested?
- [ ] Invoice creation flow tested?
- [ ] Database migration tested?
- [ ] Multi-business isolation tested?

### 4.3 Manual Test Coverage
- [ ] Business profile creation?
- [ ] Multi-business switching?
- [ ] Invoice creation & PDF generation?
- [ ] Payment recording?
- [ ] Dashboard updates?
- [ ] Error scenarios?

---

## PHASE 5: DOCUMENTATION VERIFICATION

### 5.1 README & Setup
- [ ] README.md exists and is current?
- [ ] SETUP.md exists with clear instructions?
- [ ] CONTRIBUTING.md exists?
- [ ] CODE_OF_CONDUCT.md exists?

### 5.2 Technical Documentation
- [ ] Architecture documentation?
- [ ] Database schema documentation?
- [ ] API documentation (if applicable)?
- [ ] Gradle build documentation?
- [ ] Testing strategy documented?

### 5.3 Release & Checklists
- [ ] RELEASE_CHECKLIST.md exists and is current?
- [ ] Version numbers updated? (versionCode, versionName)
- [ ] Changelog updated?
- [ ] Git tags prepared for release?

---

## PHASE 6: GIT & VERSION CONTROL

### 6.1 Repository Health
- [ ] Current branch: main?
- [ ] All changes committed?
- [ ] No uncommitted files? (`git status` clean)
- [ ] Last commit message meaningful?
- [ ] Recent commits on main? (within last 24h)

### 6.2 Ignore Files
- [ ] .gitignore exists?
- [ ] Local files ignored? (`local.properties`, `.gradle`, `build/`)
- [ ] Secrets not committed? (no API keys, firebase config)
- [ ] google-services.json gitignored?

### 6.3 Branch Management
- [ ] On correct branch: main?
- [ ] No stale branches? (delete old feature branches)
- [ ] Pull latest? (`git pull origin main`)

---

## PHASE 7: FIREBASE & MONITORING

### 7.1 Firebase Configuration
- [ ] google-services.json exists? (gitignored)
- [ ] Firebase project ID correct?
- [ ] Crashlytics enabled in Firebase console?
- [ ] Analytics enabled?
- [ ] Build works without Firebase config? (debug builds should)

### 7.2 Logging & Monitoring
- [ ] Timber logging configured?
- [ ] Timber tags meaningful?
- [ ] Debug logs in debug builds only? (no release leaks)
- [ ] Crash reporting working? (Firebase Crashlytics)
- [ ] Analytics events firing?

---

## PHASE 8: PERFORMANCE VERIFICATION

### 8.1 Build Performance
- [ ] Clean build time: _____ seconds (Expected: <120s)
- [ ] Incremental build: _____ seconds (Expected: <30s)
- [ ] Parallel builds enabled?
- [ ] Build cache enabled?
- [ ] Configuration cache enabled? (optional, but check if available)

### 8.2 App Performance
- [ ] APK size: _____ MB (Expected: <50MB)
- [ ] Startup time acceptable?
- [ ] Memory usage reasonable?
- [ ] No obvious ANR risks?
- [ ] Database queries optimized? (indexes in place)

### 8.3 Network Performance
- [ ] API timeouts configured?
- [ ] Retry logic implemented?
- [ ] Rate limiting respected?
- [ ] Caching strategy in place?

---

## PHASE 9: SECURITY SCAN

### 9.1 Code Security
- [ ] No hardcoded credentials? (grep for "password", "key", "secret")
- [ ] No SQL injection risks? (using Room parameterized queries)
- [ ] No XSS risks? (if webview used)
- [ ] No insecure permissions requested?
- [ ] Network security config file exists?

### 9.2 Dependency Security
- [ ] Dependencies up-to-date?
- [ ] Known vulnerabilities scanned? (OWASP, Snyk, etc.)
- [ ] Third-party libraries vetted?
- [ ] Open-source licenses documented?

### 9.3 API Security
- [ ] API endpoints HTTPS only?
- [ ] Certificate pinning implemented? (if high-security)
- [ ] Rate limiting on server-side?
- [ ] Input validation on both client & server?

---

## PHASE 10: CRITICAL BLOCKERS CHECK

### 10.1 Show Stoppers
- [ ] App crashes on startup? (YES/NO)
- [ ] Build fails? (YES/NO)
- [ ] Tests fail? (YES/NO)
- [ ] Missing critical features? (YES/NO)
- [ ] Data loss risks? (YES/NO)
- [ ] Security vulnerabilities? (YES/NO)

### 10.2 Known Issues
- [ ] List all known issues: _____
- [ ] Are they documented? (in GitHub issues)
- [ ] Are they accepted risks? (for v0.1.0)
- [ ] Future fix planned? (in milestone)

### 10.3 Gradle 10 Readiness
- [ ] Current Gradle: 9.2.1 ✅
- [ ] Current AGP: 8.7.3+ ✅
- [ ] Gradle 10 warnings present? (Expected: 2-5, all soft)
- [ ] Are warnings documented?
- [ ] Upgrade plan for Q4 2026? (Expected: Yes, document it)

---

## PHASE 11: TEAM READINESS

### 11.1 Documentation for Team
- [ ] Setup guide understandable?
- [ ] Build process documented?
- [ ] Testing procedure clear?
- [ ] Contributing guidelines clear?
- [ ] Release process documented?

### 11.2 Knowledge Transfer
- [ ] Architecture explained?
- [ ] Dependency injection approach clear?
- [ ] Database schema documented?
- [ ] API integration documented?
- [ ] Common patterns explained?

---

## PHASE 12: FINAL SIGN-OFF

### 12.1 Release Readiness
- [ ] Build: ✅ PASS
- [ ] Tests: ✅ PASS (all)
- [ ] Docs: ✅ COMPLETE
- [ ] Security: ✅ REVIEWED
- [ ] Performance: ✅ ACCEPTABLE

### 12.2 Decision Gate

**Can you deploy NOW?** YES / NO

**If NO, what's blocking?**
1. _____
2. _____
3. _____

**Timeline to fix:** _____

**Risk assessment:** LOW / MEDIUM / HIGH

**Confidence level:** _____% ready

---

## SUMMARY SCORECARD

| Category | Score | Status | Notes |
|----------|-------|--------|-------|
| Build System | __/10 | 🟢🟡🔴 | |
| Architecture | __/10 | 🟢🟡🔴 | |
| Features | __/10 | 🟢🟡🔴 | |
| Testing | __/10 | 🟢🟡🔴 | |
| Documentation | __/10 | 🟢🟡🔴 | |
| Security | __/10 | 🟢🟡🔴 | |
| Performance | __/10 | 🟢🟡🔴 | |
| **OVERALL** | **__/70** | 🟢🟡🔴 | |

---

## SIGN-OFF

**Audited by:** _____  
**Date:** _____  
**Status:** ✅ READY FOR RELEASE / ⚠️ CONDITIONAL / 🔴 NOT READY

**Notes:**
_____

---

## 🎯 HOW TO USE THIS PROMPT

### **Quick Verification (15 minutes)**
Focus on these sections:
- PHASE 1: Build System (1.1-1.4)
- PHASE 4: Testing (4.1)
- PHASE 10: Critical Blockers (10.1)
- PHASE 12: Final Sign-Off

### **Standard Audit (45 minutes)**
Run all PHASE checks 1-10, skip PHASE 11 (team readiness)

### **Deep Audit (2 hours)**
Complete all 12 phases with detailed notes

### **Pre-Release Audit (1 hour)**
Focus on:
- PHASE 1: Build System
- PHASE 3: Features & Functionality
- PHASE 4: Testing
- PHASE 10: Critical Blockers
- PHASE 12: Final Sign-Off

---

## 🔄 SUPPLEMENTARY CHECKS

Once you run the master audit, run these verification steps:

1. **Dependency Security Check**
2. **Code Quality Scan**
3. **Architecture Violations Check**
4. **Configuration Issues Check**

---

## 📋 QUICK COMMAND REFERENCE

**Run full build verification:**
```bash
cd Bizap
./gradlew clean build --stacktrace 2>&1 | tee build_output.log
./gradlew testDebugUnitTest --stacktrace 2>&1 | tee test_output.log
```

**Check for common issues:**
```bash
# Unused imports
./gradlew lint

# Dependency conflicts
./gradlew dependencies --configuration debugRuntimeClasspath > deps.txt

# Build warnings only
./gradlew build --warning-mode all > warnings.log
```

**Check git status:**
```bash
git status
git log --oneline -10
git diff
```

---

**Last Updated:** March 7, 2026  
**Purpose:** Comprehensive health check before release


