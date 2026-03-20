# BUILD GUIDE — Bizap Debug & Release Workflows

**Last Updated:** March 20, 2026  
**Status:** ✅ Complete

---

## Quick Reference

| Task | Command | Output | Time |
|------|---------|--------|------|
| **Debug Build** | `./gradlew build` | All tests + Debug APK | ~2 min |
| **Debug Install** | `./gradlew installDebug` | App on device/emulator | ~30 sec |
| **Debug Run** | `./gradlew installDebug -x test` | Skip tests, faster | ~15 sec |
| **Release Build** | `./gradlew clean assembleRelease` | Signed Release APK | ~3 min |
| **Tests Only** | `./gradlew test` | 1,081+ unit tests | ~1–2 min |
| **Integration Tests** | `./gradlew connectedAndroidTest` | Real device/emulator | ~15–20 sec |

---

## Table of Contents

1. [Debug Build & Install](#debug-build--install)
2. [Release Build](#release-build)
3. [Testing](#testing)
4. [Troubleshooting](#troubleshooting)
5. [Performance Tips](#performance-tips)
6. [GitHub Actions](#github-actions)

---

## Debug Build & Install

### First Time Setup

```bash
# Clone repository
git clone https://github.com/EmuBiz/Bizap.git
cd Bizap

# Sync Gradle (download dependencies)
./gradlew sync

# Build and run tests (comprehensive check)
./gradlew build
```

### Regular Development

```bash
# Build debug APK + run tests (full cycle)
./gradlew build

# Skip tests (faster, for UI iteration)
./gradlew assembleDebug

# Install on device/emulator (auto-builds if needed)
./gradlew installDebug

# Install + launch app
./gradlew installDebug --launch-app  # Depends on plugin
# OR manually: adb shell am start -n com.emul8r.bizap/.MainActivity
```

### One-Liner: Build → Install → Launch

```bash
# Linux/Mac
./gradlew installDebug && adb shell am start -n com.emul8r.bizap/.MainActivity

# Windows PowerShell
./gradlew installDebug; adb shell am start -n com.emul8r.bizap/.MainActivity
```

### Connecting to Emulator/Device

```bash
# List connected devices
adb devices

# Check device logs in real-time
adb logcat | grep -E "(Bizap|ERROR|Exception)"

# Clear app data (fresh start)
adb shell pm clear com.emul8r.bizap
```

---

## Release Build

### One-Time Setup

```bash
# Generate release keystore (see docs/RELEASE_SIGNING.md)
keytool -genkey -v \
  -keystore bizap-release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias bizap-key

# Move to secure location
mv bizap-release.keystore ~/Secure/

# Verify
keytool -list -v -keystore ~/Secure/bizap-release.keystore
```

### Build Release APK (Production)

#### Method 1: With Environment Variables

```bash
# Set credentials (for production build)
export KEYSTORE_PATH="$HOME/Secure/bizap-release.keystore"
export KEYSTORE_PASSWORD="your_password"
export KEY_ALIAS="bizap-key"
export KEY_PASSWORD="your_password"

# Build signed release APK
./gradlew clean assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

#### Method 2: Dev Keystore (Local Testing)

```bash
# Create dev keystore at project root (auto-detected)
keytool -genkey -v \
  -keystore ../release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias bizap-dev-key \
  -storepass bizap123 -keypass bizap123

# Build (will use ../release-key.jks)
./gradlew clean assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Verify Release APK

```bash
# Check signature is valid
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk

# Output should say: "jar verified."

# View certificate details
keytool -printcert -jarfile app/build/outputs/apk/release/app-release.apk
```

### Install Release APK

```bash
# Install on device/emulator
adb install app/build/outputs/apk/release/app-release.apk

# Launch
adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

## Testing

### Unit Tests (Fast)

```bash
# All unit tests
./gradlew test

# Specific test file
./gradlew test -k "InvoiceRepositoryImplEnhancedTest"

# Specific test method
./gradlew test -k "InvoiceRepositoryImplEnhancedTest.testCreateInvoice"

# Run with output
./gradlew test --info
```

**Time:** ~1–2 minutes  
**Count:** 1,081 tests  
**Coverage:** 70% (logic layer)

### Integration Tests (Slower, Real Device)

```bash
# Connect device/emulator first
adb devices

# Run integration tests
./gradlew connectedAndroidTest

# Specific test class
./gradlew connectedAndroidTest -k "NavigationIntegrationTest"
```

**Time:** ~15–20 minutes  
**Count:** 40+ tests  
**Coverage:** 20% (UI/navigation layer)  
**Requirements:** Physical device or emulator running

### All Tests

```bash
# Unit + integration tests (comprehensive)
./gradlew build
```

**Time:** ~3–5 minutes total

### View Test Reports

```bash
# After running tests, open report
open app/build/reports/tests/debug/index.html  # Mac
xdg-open app/build/reports/tests/debug/index.html  # Linux
start app\build\reports\tests\debug\index.html  # Windows
```

---

## Troubleshooting

### Build Fails: "Gradle Out of Memory"

**Error:**
```
Exception: OutOfMemoryError: Java heap space
```

**Fix:**
```bash
# Increase Gradle heap size in gradle.properties
echo "org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m" >> gradle.properties

# Rebuild
./gradlew clean build
```

### Build Fails: "Keystore File Not Found"

**Error:**
```
❌ Release signing configuration missing!
```

**Fix:**
```bash
# For dev: create local keystore
keytool -genkey -v \
  -keystore ../release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias bizap-dev-key \
  -storepass bizap123 -keypass bizap123

# Or for production: set env vars
export KEYSTORE_PATH="$HOME/Secure/bizap-release.keystore"
export KEYSTORE_PASSWORD="password"
export KEY_ALIAS="bizap-key"
export KEY_PASSWORD="password"
```

### Build Fails: "Gradle Sync Failed"

**Fix:**
```bash
# Clean Gradle cache
./gradlew clean

# Invalidate IDE cache
# Android Studio: File → Invalidate Caches... → Invalidate and Restart

# Resync
File → Sync Now (or Alt+Ctrl+S)
```

### Tests Fail: "No Device Found"

**For integration tests:**
```bash
# Check device is connected
adb devices

# If empty, troubleshoot:
# - Emulator: Start from Android Studio → Device Manager
# - Physical device: Enable USB debugging in Settings
```

### App Crashes on Startup

**Debug steps:**
```bash
# Clear app data
adb shell pm clear com.emul8r.bizap

# Check logs
adb logcat | grep -E "(Bizap|ERROR|Exception|Crash)"

# Rebuild and reinstall
./gradlew clean installDebug
```

---

## Performance Tips

### Faster Debug Builds (During Development)

```bash
# Skip tests (faster)
./gradlew assembleDebug

# Skip tests + minification (very fast)
./gradlew assembleDebug --no-verify

# Incremental build (after first build)
./gradlew build  # Gradle only rebuilds changed files
```

### Gradle Daemon (Keep JVM Running)

```bash
# Enabled by default; speeds up consecutive builds

# Disable if having issues
./gradlew --no-daemon build

# Check daemon status
jps -l  # Look for GradleDaemon
```

### Parallel Build

```bash
# Enable in gradle.properties
org.gradle.parallel=true

# Use all CPU cores
org.gradle.workers.max=4  # or your CPU count
```

### Reduce APK Size (Debug)

```bash
# Disable minification + resource shrinking (faster builds)
# Already disabled in debug buildType in build.gradle.kts

# Check AAB size
./gradlew bundleDebug
# Output: app/build/outputs/bundle/debug/app-debug.aab
```

---

## GitHub Actions

### Automatic Release Build

GitHub Actions automatically builds and signs release APK on:
- Push to `main` branch
- Tag push matching `v*` (e.g., `v1.0`)
- Manual workflow dispatch

### Workflow File

Location: `.github/workflows/release-signing.yml`

Runs:
1. Checkout code
2. Setup JDK 17
3. Decode Base64 keystore (from secrets)
4. Build + sign release APK (env vars injected)
5. Upload signed APK as artifact
6. Clean up keystore file

### View Results

1. Go to GitHub → Actions tab
2. Click workflow run
3. See build log + download APK artifact

### Manual Trigger

```bash
# Locally, you can manually invoke GitHub Actions workflow
# GitHub CLI required
gh workflow run release-signing.yml
```

---

## Build Configuration

### Debug Build Configuration

From `app/build.gradle.kts`:

```kotlin
buildTypes {
    debug {
        isMinifyEnabled = false           // No code shrinking
        isShrinkResources = false         // No resource shrinking
        debuggable = true                 // Enable debugger
        // ... logging, etc.
    }
}
```

**Result:** Unoptimized APK, fast build, debuggable

### Release Build Configuration

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true            // Shrink unused code (~30%)
        isShrinkResources = false         // Resource shrinking disabled (causes crashes)
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
        debuggable = false                // Production: no debug features
    }
}
```

**Result:** Optimized APK (~12–15 MB), slower build, non-debuggable

---

## Advanced: Custom Build Variants

Create custom flavor for testing:

```kotlin
// In app/build.gradle.kts
flavorDimensions += "environment"

productFlavors {
    create("dev") {
        dimension = "environment"
        versionNameSuffix = "-dev"
        applicationIdSuffix = ".dev"
    }
    create("prod") {
        dimension = "environment"
        applicationId = "com.emul8r.bizap"
    }
}

// Build specific variant
./gradlew assembleProdRelease  // Production release
./gradlew assembleDevDebug     // Development debug
```

---

## Deployment Checklist

Before submitting to Play Store:

- [ ] Release APK built: `./gradlew clean assembleRelease`
- [ ] Signature verified: `jarsigner -verify app-release.apk` → "jar verified"
- [ ] Tested on real device (not just emulator)
- [ ] Version code incremented in `build.gradle.kts`
- [ ] Version name updated (e.g., `1.0` → `1.1`)
- [ ] Release notes written
- [ ] Screenshots captured (if applicable)
- [ ] Privacy policy updated (if needed)
- [ ] Changelog entry added

---

## References

- [Android Gradle Plugin Documentation](https://developer.android.com/studio/build)
- [Signing Your App](https://developer.android.com/studio/publish/app-signing)
- [Building and Running Your App](https://developer.android.com/studio/run)
- [Gradle Build Optimization](https://developer.android.com/studio/build/optimize-your-build)

---

**Last Updated:** March 20, 2026  
**Status:** ✅ Complete  
**Maintainer:** EmuBiz Development Team

