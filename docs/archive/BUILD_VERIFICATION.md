# Build Verification Guide

## Quick Start

```bash
cd Bizap
./verify.sh          # Full verification (build + tests + lint)
./verify.sh build    # Debug APK only
./verify.sh test     # Unit tests only
./verify.sh lint     # Lint checks only
./verify.sh clean    # Clean then full verification
```

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK | 17 (Temurin) |
| Android SDK | API 35 |
| Gradle | 9.2.1 (wrapper auto-downloads) |
| `ANDROID_HOME` | Set to SDK path |

```bash
export ANDROID_HOME=/usr/local/lib/android/sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

## Manual Build Steps

### Debug APK
```bash
cd Bizap
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK
```bash
./gradlew :app:assembleRelease
# Requires signing configuration
```

### Run Unit Tests
```bash
./gradlew :app:testDebugUnitTest
# Report: app/build/reports/tests/testDebugUnitTest/index.html
```

### Lint
```bash
./gradlew :app:lintDebug
# Report: app/build/reports/lint-results-debug.html
# Note: abortOnError = false — build succeeds with lint warnings
```

## Build Configuration

### Key Settings (`gradle.properties`)

| Property | Value | Purpose |
|----------|-------|---------|
| `org.gradle.jvmargs` | `-Xmx4g` | JVM heap for compilation |
| `org.gradle.parallel` | `true` | Parallel module compilation |
| `org.gradle.caching` | `true` | Build cache for faster incremental builds |
| `org.gradle.workers.max` | `4` | Parallel worker limit |
| `org.gradle.configuration-cache` | `false` | Disabled: KSP/Hilt incompatibility |
| `ksp.incremental` | `true` | KSP incremental processing |

### R8/ProGuard

- **Debug**: R8 disabled (`isMinifyEnabled = false`)
- **Release**: R8 enabled with `proguard-rules.pro`
- Rules in `app/proguard-rules.pro` preserve Room entities, Hilt classes, and Kotlin metadata

## Dependency Overview

### Core Android
- Compose BOM `2024.12.01`
- Hilt `2.51.1` (DI)
- Room `2.6.1` (local DB)
- WorkManager `2.9.0` (background sync)

### Testing
- MockK `1.13.10` — Kotlin-first mocking
- kotlinx-coroutines-test `1.7.3` — Coroutine test utilities
- Robolectric `4.11.1` — Android API simulation in JVM tests
- JUnit `4.13.2`

## CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/android-ci.yml`) runs on every push and PR:

1. **Build debug APK** — `./gradlew :app:assembleDebug`
2. **Upload artifact** — APK stored as workflow artifact

### Troubleshooting CI Failures

| Symptom | Cause | Fix |
|---------|-------|-----|
| `Unresolved reference` | Missing import or wrong class | Check import and class signature |
| `CompilationErrorException` | Kotlin compile error | See full log for `e:` lines |
| `KSP` failures | Config cache incompatible | Ensure `org.gradle.configuration-cache=false` |
| UTF-16 in gradle.properties | Corrupted file encoding | Ensure file is UTF-8, no null bytes |

## Known Issues

| Issue | Status | Notes |
|-------|--------|-------|
| Lint warnings (~165) | ⚠️ Non-blocking | `abortOnError = false` set |
| `android.r8.fullMode` | ✅ Configured | Set in `gradle.properties` |
