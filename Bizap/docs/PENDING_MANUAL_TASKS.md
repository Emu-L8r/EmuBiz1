# Pending Manual Tasks

**Document Version:** 1.0  
**Created:** March 7, 2026  
**Status:** Reference document — update as tasks are completed

---

This document lists all pending manual cleanup tasks that require human action or are outside the scope of automated fixes. Each task includes shell commands where applicable.

---

## Task 1: Verify Stale Files Are Deleted ✅ (Done in this PR)

The following stale files were deleted in the March 7, 2026 PR:

| File | Reason |
|---|---|
| `Bizap/app/src/main/java/com/emul8r/bizap/data/local/AnalyticsDao.kt` | Deprecated stub — moved to `data/local/dao/AnalyticsDao.kt` |
| `Bizap/app/src/main/java/com/emul8r/bizap/data/local/entity/CustomerAnalyticsSnapshot.kt` | Old version with `Double` field types — replaced by `data/local/entities/CustomerAnalyticsSnapshot.kt` |

**Verification command:**
```bash
# These should return "No such file or directory"
ls Bizap/app/src/main/java/com/emul8r/bizap/data/local/AnalyticsDao.kt 2>&1
ls Bizap/app/src/main/java/com/emul8r/bizap/data/local/entity/ 2>&1
```

---

## Task 2: Verify Reactive Patterns Are in Place ✅ (Done previously)

These issues were fixed in a previous PR. Verify they remain correct after any future merges:

### 2a. BusinessProfileRepository Domain Interface
All ViewModels should import `com.emul8r.bizap.domain.repository.BusinessProfileRepository`, NOT a concrete class.

**Verification:**
```bash
# All imports should show domain.repository.BusinessProfileRepository
grep -r "import.*BusinessProfileRepository" Bizap/app/src/main/java --include="*.kt"
# Should NOT show any concrete class imports (data.repository.BusinessProfileRepository)
```

### 2b. Reactive activeProfile Flow
`BusinessProfileRepositoryImpl` should use a reactive Flow (not a one-shot `flow {}`).

**Verification:**
```bash
grep -A 10 "override val activeProfile" Bizap/app/src/main/java/com/emul8r/bizap/data/repository/BusinessProfileRepositoryImpl.kt
```

### 2c. Line Item UUID-Based Operations
`CreateInvoiceViewModel` should use `transientId` (UUID) for `updateLineItem` and `removeLineItem`.

**Verification:**
```bash
grep -n "transientId\|updateLineItem\|removeLineItem" Bizap/app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceViewModel.kt
```

---

## Task 3: Run Final Verification Build

Before the v0.1.0 release, run the full build and test suite:

```bash
cd Bizap

# 1. Clean build
./gradlew clean

# 2. Run all unit tests (should be 279+ passing)
./gradlew test

# 3. Build release APK (requires signing config)
./gradlew assembleRelease

# 4. Check APK size (should be ~24MB)
ls -lh app/build/outputs/apk/release/

# 5. Run lint
./gradlew lint
```

**Expected outcomes:**
- All tests pass (0 failures)
- Build time < 66 seconds
- APK size ~24MB (< 50MB hard limit)
- No new Lint errors

---

## Task 4: Generate and Commit Database Schema (If Version Changed)

If AppDatabase version was bumped in this or a future PR:

```bash
cd Bizap
./gradlew generateRoomSchemas

# Commit the generated schema file
git add app/schemas/
git commit -m "chore: export Room database schema v{VERSION}"
```

---

## Task 5: Verify No Secrets in Codebase

```bash
# Check for potential API keys or secrets
grep -r "api_key\|apiKey\|secret\|password\|token" Bizap/app/src/main --include="*.kt" | grep -v "//\|test\|Test"

# Check local.properties is in .gitignore
grep "local.properties" .gitignore
```

**Expected:** No hardcoded secrets found. `local.properties` is in `.gitignore`.

---

## Task 6: Update CHANGELOG Before Release

Edit `CHANGELOG.md` (or create it if missing) with release notes for v0.1.0:

```markdown
## [0.1.0] - 2026-Q1

### Added
- Invoice creation, editing, and PDF generation
- Customer management (CRUD)
- Multi-currency support (AUD, USD, GBP, EUR)
- Business profile management with switcher
- Revenue dashboard with total paid revenue display
- Analytics snapshots with optimistic locking

### Fixed
- Dashboard revenue card now correctly sums ALL paid invoices
- Removed stale AnalyticsDao.kt stub
- Removed duplicate CustomerAnalyticsSnapshot.kt (old Double types)
```

---

## Status Legend

| Symbol | Meaning |
|---|---|
| ✅ | Completed |
| 🔄 | In progress |
| ⏳ | Pending — not started |
| ❌ | Blocked |
