# Deployment Checklist — Bizap (EmuBiz1)

**Last Updated:** 2026-03-08  
**App Package:** `com.emul8r.bizap`  
**Current DB Version:** 32

---

## Table of Contents

1. [Pre-Deployment Checks](#1-pre-deployment-checks)
2. [Build Verification](#2-build-verification)
3. [Test Coverage Requirements](#3-test-coverage-requirements)
4. [Database Migration Verification](#4-database-migration-verification)
5. [Performance Requirements](#5-performance-requirements)
6. [Security Checklist](#6-security-checklist)
7. [Code Review Checklist](#7-code-review-checklist)
8. [Release Process](#8-release-process)

---

## 1. Pre-Deployment Checks

### Architecture Integrity

- [ ] Clean Architecture layers maintained (Data → Domain → Presentation; no reverse dependencies)
- [ ] No Android imports in the `domain/` package
- [ ] All new features use `@HiltViewModel` for ViewModels
- [ ] All state exposed as `StateFlow<UiState>` (no `LiveData` in new code)
- [ ] All async operations use Kotlin Coroutines/Flow (no `AsyncTask` or `Thread`)
- [ ] Repository pattern: all DB access goes through DAO → Repository → UseCase → ViewModel
- [ ] No direct DAO calls from ViewModels or UI layer

### Code Quality

- [ ] No unused imports or variables (run lint)
- [ ] Timber used for logging (no `Log.d/e/w` calls in production code)
- [ ] No hardcoded strings (all in `strings.xml`)
- [ ] No hardcoded dimensions (all in `dimens.xml` or `dp` references)
- [ ] No hardcoded colors (all from Material 3 theme tokens)
- [ ] All `TODO`/`FIXME` comments reviewed and resolved or tracked in issues
- [ ] No force-unwrap (`!!`) on nullable types — use `?.`, `?:`, or `Result`
- [ ] Error handling: all `suspend fun` calls wrapped in `try/catch` or return `Result<T>`

### Offline Behaviour

- [ ] All mutating operations check `ConnectivityHelper` before network calls
- [ ] Offline queue operations are idempotent (safe to replay)
- [ ] `SyncWorker` has exponential backoff configured
- [ ] Offline operations are cleared after successful sync

---

## 2. Build Verification

### Debug Build

```bash
cd Bizap && ./gradlew :app:assembleDebug
```

- [ ] Debug APK builds without errors
- [ ] No compilation warnings treated as errors
- [ ] APK size within acceptable range (< 30 MB)

### Release Build

```bash
cd Bizap && ./gradlew :app:assembleRelease
```

- [ ] Release APK builds without errors
- [ ] ProGuard/R8 rules configured for all reflection-using libraries
- [ ] Minification and shrinking enabled
- [ ] Signing configuration verified

### Lint

```bash
cd Bizap && ./gradlew :app:lint
```

- [ ] Zero lint errors
- [ ] Lint warnings reviewed and suppressed only with justification (`@SuppressLint` with comment)
- [ ] `lint.xml` baseline is current

---

## 3. Test Coverage Requirements

### Unit Tests

```bash
cd Bizap && ./gradlew :app:testDebugUnitTest
```

- [ ] All unit tests pass (zero failures)
- [ ] Key use cases tested (happy path + failure paths):
  - [ ] `RecordPaymentUseCase` — overpayment, future date, past-invoice date
  - [ ] `SaveInvoiceUseCase` — empty line items, no customer name, offline path
  - [ ] `UpdateInvoiceUseCase` — validation, offline path
  - [ ] `DeleteInvoiceUseCase` — offline path
- [ ] Repository tests cover both success and DB exception paths
- [ ] ViewModel tests verify `UiState` transitions (Loading → Success/Error)
- [ ] Validation tests cover all edge cases

### Instrumented Tests

```bash
cd Bizap && ./gradlew :app:connectedDebugAndroidTest
```

- [ ] All migration tests pass:
  - [ ] `Migration23To24Test`
  - [ ] `Migration24To25Test`
  - [ ] `Migration25To26Test`
  - [ ] `Migration26To27Test`
  - [ ] `Migration27To28Test`
  - [ ] `Migration28To29Test`
- [ ] E2E tests pass:
  - [ ] `CreateCustomerE2ETest`
  - [ ] `CreateInvoiceE2ETest`

### Coverage Minimums (Target)

| Layer | Minimum Coverage |
|-------|-----------------|
| Domain (Use Cases) | 80% |
| Data (Repositories) | 70% |
| Presentation (ViewModels) | 60% |
| Overall | 65% |

---

## 4. Database Migration Verification

- [ ] `AppDatabase.version` incremented for any schema change
- [ ] Migration class created in `data/local/migrations/`
- [ ] Migration registered in `DatabaseModule.kt` via `.addMigrations(...)`
- [ ] Migration test written in `androidTest/` (tests v(N-1) → v(N))
- [ ] Migration is backward-compatible (no data loss)
- [ ] `fallbackToDestructiveMigration()` is **NOT** enabled in production
- [ ] Schema export files are committed to `app/schemas/`

### Migration Verification Steps

1. Install the **previous** version of the app with existing data
2. Install the **new** version (upgrade flow)
3. Verify all existing data is intact
4. Verify new schema elements are correctly initialised

---

## 5. Performance Requirements

### Database Queries

- [ ] All frequently-called queries use `Flow` (reactive, not polling)
- [ ] Complex queries have appropriate indexes (verify with `EXPLAIN QUERY PLAN`)
- [ ] No N+1 queries — use `@Transaction` + `@Relation` for joined data
- [ ] Daily snapshot updates use optimistic locking (`AnalyticsDao.updateDailySnapshotWithOptimisticLock`)
- [ ] Large list queries paginated where appropriate (Paging 3)

### UI Performance

- [ ] No blocking calls on the main thread
- [ ] All I/O operations use `Dispatchers.IO`
- [ ] All CPU-intensive operations use `Dispatchers.Default`
- [ ] `LazyColumn` / `LazyRow` used for all lists (no `Column` with large datasets)
- [ ] Image loading uses async patterns (Coil or similar)
- [ ] Compose recompositions minimised (stable data classes, `key()` in lazy lists)

### Memory

- [ ] No memory leaks (verify with LeakCanary in debug build)
- [ ] ViewModel not holding references to `Context` or `View`
- [ ] Large bitmaps (logos) compressed before storage

---

## 6. Security Checklist

- [ ] No secrets, API keys, or credentials in source code
- [ ] `.gitignore` includes `local.properties`, `keystore.jks`, `*.jks`
- [ ] API keys managed via environment variables or `local.properties` (not committed)
- [ ] `NetworkSecurityConfig` configured for production (no clear-text HTTP)
- [ ] `FileProvider` configured correctly for PDF sharing (no world-readable files)
- [ ] Room database not backed up by default (or backup rules configured to exclude sensitive data)
- [ ] ProGuard rules prevent decompilation of sensitive business logic
- [ ] Input validation at all layers (UI, Use Case, Repository)
- [ ] SQL injection not possible (Room uses parameterised queries)

---

## 7. Code Review Checklist

### For Each PR

#### General

- [ ] PR description explains **what** and **why** (not just what)
- [ ] PR is focused on a single feature or fix
- [ ] No unrelated changes in the PR
- [ ] Breaking changes documented

#### Architecture

- [ ] New code follows Clean Architecture (correct layer placement)
- [ ] New repositories have corresponding interface in `domain/`
- [ ] New ViewModels use `@HiltViewModel` and constructor injection
- [ ] New use cases are in `domain/usecase/` with a single `operator fun invoke()`

#### Testing

- [ ] Unit tests added for new use cases
- [ ] Unit tests added for new repository methods
- [ ] ViewModel tests verify state transitions
- [ ] Migration test added for any schema change

#### Database

- [ ] Schema changes have a migration file
- [ ] Foreign keys defined with appropriate cascade rules
- [ ] Indexes added for new query columns

#### UI

- [ ] New screens have empty state
- [ ] New screens have loading state
- [ ] New screens have error state with retry option
- [ ] Compose preview annotations added for complex composables
- [ ] Accessibility: `contentDescription` on all interactive icons

---

## 8. Release Process

### Step 1 — Version Bump

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        versionCode = <increment by 1>
        versionName = "<major>.<minor>.<patch>"
    }
}
```

### Step 2 — Changelog Update

Update `RELEASE_CHECKLIST.md` and create a new entry in the changelog.

### Step 3 — Final Test Run

```bash
cd Bizap
./gradlew :app:testDebugUnitTest
./gradlew :app:lint
./gradlew :app:assembleRelease
```

### Step 4 — Tag the Release

```bash
git tag v<major>.<minor>.<patch>
git push origin v<major>.<minor>.<patch>
```

### Step 5 — Build Signed APK / AAB

```bash
./gradlew :app:bundleRelease
```

### Step 6 — Upload to Play Console

- Upload AAB to the appropriate track (Internal → Alpha → Beta → Production)
- Fill in release notes for each supported language
- Set rollout percentage for production releases

### Step 7 — Monitor

- Watch Firebase Crashlytics for new crashes (first 24 hours)
- Monitor ANR rates in Play Console
- Check user reviews for regression reports

---

## Appendix: Common Build Issues

| Issue | Fix |
|-------|-----|
| `Room schema export` error | Ensure `javaCompileOptions.annotationProcessorOptions.arguments["room.schemaLocation"]` is set |
| `Hilt injection failed` | Check all `@AndroidEntryPoint` / `@HiltViewModel` annotations are present |
| `Migration not found` | Add migration to `DatabaseModule.kt` `.addMigrations(...)` call |
| `Theme attribute not found` | Ensure `Theme.Bizap` extends `Theme.Material3.Light.NoActionBar` |
| `ProGuard` stripping Room | Add `-keep class com.emul8r.bizap.data.local.** { *; }` to ProGuard rules |
