# How to Verify Production Readiness — Bizap v1.0.0

**Purpose:** Step-by-step commands to independently verify the production readiness claims made in this PR.

---

## Prerequisites

```bash
# Java 17+ required
java -version

# Android SDK required (ANDROID_HOME set)
echo $ANDROID_HOME

# Navigate to the Bizap module
cd Bizap
```

---

## Verification 1: All Tests Pass

```bash
# Run the full unit test suite
./gradlew :app:testDebugUnitTest

# Expected output:
# BUILD SUCCESSFUL
# 936 tests, 936 passed, 0 failed
```

To see which tests ran:
```bash
./gradlew :app:testDebugUnitTest --info | grep "tests were run"
```

---

## Verification 2: Revenue Queries Include PAID + PARTIALLY_PAID

```bash
# Check InvoiceDaoV2 revenue queries
grep -A 10 "observeMTDRevenue\|observeYTDRevenue\|observeWeeklyRevenue\|observeTotalPaidRevenue" \
  app/src/main/java/com/emul8r/bizap/data/local/dao/InvoiceDaoV2.kt

# Expected: All queries should show:
# AND (status = 'PAID' OR status = 'PARTIALLY_PAID')

# Also verify the original InvoiceDao
grep -A 10 "observeMTDRevenue\|observeYTDRevenue" \
  app/src/main/java/com/emul8r/bizap/data/local/InvoiceDao.kt
```

---

## Verification 3: Exception Handling on Critical Paths

```bash
# Check that critical paths re-throw exceptions
grep -B 2 -A 5 "throw e" \
  app/src/main/java/com/emul8r/bizap/data/repository/InvoiceRepositoryImpl.kt

# Expected output: Should show "throw e" statements in:
# - invoice creation (createAnalyticsSnapshots block)
# - invoice deletion block

# Check that non-critical paths (snapshot sync) are intentionally non-blocking
grep -B 2 -A 5 "non-blocking\|DO NOT re-throw" \
  app/src/main/java/com/emul8r/bizap/data/repository/InvoiceRepositoryImpl.kt
```

---

## Verification 4: No Snapshot Dependency (Option C)

```bash
# Verify SnapshotCachePolicy
cat app/src/main/java/com/emul8r/bizap/data/repository/SnapshotCachePolicy.kt

# Expected: USE_SNAPSHOTS_FOR_DASHBOARDS = false

# Verify RevenueRepositoryV2 uses direct DAO queries
head -30 app/src/main/java/com/emul8r/bizap/data/repository/gui2/RevenueRepositoryV2.kt

# Expected: Comment "Option C — no snapshot dependency"
```

---

## Verification 5: GUI1 and GUI2 Share the Same Data Source

```bash
# Check that AnalyticsRepositoryBridge unifies both GUIs
cat app/src/main/java/com/emul8r/bizap/data/repository/AnalyticsRepositoryBridge.kt

# Check that CrossGUISyncTest validates consistency
cat app/src/test/java/com/emul8r/bizap/ui/gui2/integration/CrossGUISyncTest.kt

# Run only the consistency and integration tests
./gradlew :app:testDebugUnitTest \
  --tests "com.emul8r.bizap.consistency.*" \
  --tests "com.emul8r.bizap.ui.gui2.integration.*"
```

---

## Verification 6: SingleSourceOfTruthTest

```bash
# Run the Single Source of Truth validation tests
./gradlew :app:testDebugUnitTest \
  --tests "com.emul8r.bizap.consistency.SingleSourceOfTruthTest"

# These tests verify:
# - DRAFT invoices excluded from outstanding and revenue
# - PAID + PARTIALLY_PAID included in revenue
# - Collection rate is amount-based (not count-based)
# - GUI1 and GUI2 show consistent data
```

---

## Verification 7: Build Succeeds with Zero Errors

```bash
# Clean build
./gradlew clean :app:assembleDebug

# Expected:
# BUILD SUCCESSFUL
# APK at: app/build/outputs/apk/debug/app-debug.apk

# Check APK size (should be < 50 MB)
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

---

## Verification 8: No Hardcoded Secrets

```bash
# Check for common secret patterns
grep -r "apiKey\|api_key\|password\|secret\|token" \
  app/src/main/java/com/emul8r/bizap/ \
  --include="*.kt" \
  | grep -v "// " | grep -v "test" | grep -v "TODO"

# Expected: No results (or only documentation comments, not values)
```

---

## Verification 9: Database Version and Migrations

```bash
# Check current database version
grep "version = " app/src/main/java/com/emul8r/bizap/data/local/AppDatabase.kt

# Expected: version = 32

# List all migrations
ls app/src/main/java/com/emul8r/bizap/data/local/migrations/

# Expected: Migration files from 21 through 32
```

---

## Verification 10: Offline Queue Implementation

```bash
# Verify OfflineQueueService exists and has required methods
grep "fun queue\|fun sync" \
  app/src/main/java/com/emul8r/bizap/data/local/offline/OfflineQueueService.kt

# Verify SyncWorker is registered
grep "SyncWorker" \
  app/src/main/java/com/emul8r/bizap/data/worker/SyncWorker.kt | head -5
```

---

## Quick All-in-One Verification

```bash
cd Bizap

# Run all verifications in sequence
echo "=== Running Tests ===" && \
./gradlew :app:testDebugUnitTest && \
echo "=== Checking Revenue Queries ===" && \
grep "PARTIALLY_PAID" app/src/main/java/com/emul8r/bizap/data/local/dao/InvoiceDaoV2.kt | wc -l && \
echo "=== Checking Snapshot Policy ===" && \
grep "USE_SNAPSHOTS_FOR_DASHBOARDS" app/src/main/java/com/emul8r/bizap/data/repository/SnapshotCachePolicy.kt && \
echo "=== All verifications complete ==="
```

Expected final output:
```
BUILD SUCCESSFUL
5           (5 revenue queries include PARTIALLY_PAID)
USE_SNAPSHOTS_FOR_DASHBOARDS = false
All verifications complete
```
