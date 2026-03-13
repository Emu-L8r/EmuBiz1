# 🚀 BIZAP DEPLOYMENT GUIDE

**Date**: March 10, 2026  
**Version**: 1.0  
**Status**: Complete

---

## TABLE OF CONTENTS

1. [Prerequisites](#prerequisites)
2. [Build Instructions](#build-instructions)
3. [Pre-Deployment Testing](#pre-deployment-testing)
4. [Deployment Steps](#deployment-steps)
5. [Verification](#verification)
6. [Troubleshooting](#troubleshooting)
7. [Rollback Procedures](#rollback-procedures)

---

## PREREQUISITES

### Required Tools

```
✅ Android Studio 2023.1 or later
✅ Java Development Kit (JDK) 17+
✅ Android SDK API 35
✅ Gradle 9.2.1
✅ Git for version control
✅ Android emulator or physical device (API 26+)
```

### System Requirements

```
Memory:     8GB RAM minimum (16GB recommended)
Disk:       5GB free space for SDK and builds
OS:         Windows 10/11, macOS, or Linux
Network:    Internet connection for dependencies
```

### Environment Setup

**1. Install Android Studio**
```bash
# Download from: https://developer.android.com/studio
# Follow official installation guide
# Install: Android SDK, emulator, build-tools
```

**2. Set up Android SDK**
```bash
# In Android Studio:
# Tools → SDK Manager
# Install:
#  - Android SDK 35 (API 35)
#  - Android SDK Build-Tools 35.0.0
#  - Android Emulator
#  - Intel HAXM (Windows/macOS) or KVM (Linux)
```

**3. Configure Environment Variables**
```powershell
# Windows PowerShell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Users\YourUsername\AppData\Local\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "User")

# Verify
echo $env:ANDROID_HOME
echo $env:JAVA_HOME
```

**4. Clone Repository**
```bash
cd C:\Users\YourUsername\Documents\GitHub\EmuBiz
git clone https://github.com/Emu-L8r/EmuBiz.git
cd Bizap
```

---

## BUILD INSTRUCTIONS

### Development Build

**Clean Build**:
```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
./gradlew clean build -q
# Expected output: BUILD SUCCESSFUL in XXs
# Result: app/build/outputs/apk/debug/app-debug.apk
```

**Incremental Build**:
```bash
./gradlew build -q
# Faster for testing, uses cached components
```

### Release Build

**Create Release APK**:
```bash
./gradlew clean assembleRelease -q
# Result: app/build/outputs/apk/release/app-release.apk
# Size: ~25 MB
```

**Check Build Size**:
```bash
ls -lh app/build/outputs/apk/*/app-*.apk
# Expected: ~25-30 MB for debug, ~20-25 MB for release
```

### Build Verification

**Verify Success**:
```bash
./gradlew clean build
# Should complete without:
#  ❌ Compilation errors
#  ❌ Test failures
#  ❌ Critical warnings
```

**Check APK Contents**:
```bash
cd app/build/outputs/apk/debug
unzip -t app-debug.apk | head -20
# Verify: AndroidManifest.xml, classes.dex, resources.arsc present
```

---

## PRE-DEPLOYMENT TESTING

### Step 1: Run Unit Tests

```bash
./gradlew testDebugUnitTest -q
# Expected: 421+ tests PASSING (100% pass rate)
```

**Verify Results**:
```bash
# Check output for:
# BUILD SUCCESSFUL
# 421 tests completed
# 0 failures
# 0 errors
```

### Step 2: Run Lint Analysis

```bash
./gradlew lint -q
# Check for code quality issues
```

**Review Lint Report**:
```bash
# Open: app/build/reports/lint-results.html
# Expected:
#  ✅ 0 critical issues
#  ✅ 0 high priority issues
#  ⚠️ Any yellow warnings can be addressed later
```

### Step 3: Code Coverage Check

```bash
./gradlew testDebugUnitTest jacocoTestReport -q
# Open: app/build/reports/jacoco/jacocoTestReport/html/index.html
# Verify: 80%+ coverage maintained
```

### Step 4: Manual Testing Checklist

**Invoice Management**:
```
☐ Create invoice with valid data
☐ Create invoice without customer (should fail with error)
☐ Create invoice with invalid amount (should fail with error)
☐ Edit existing invoice
☐ Delete invoice
☐ Record payment (partial and full)
☐ Change invoice status (DRAFT → SENT → PAID)
```

**Customer Management**:
```
☐ Create new customer
☐ View customer list
☐ Edit customer details
☐ Delete customer
☐ Search customers
```

**Analytics & Dashboards**:
```
☐ Dashboard loads correctly
☐ Revenue displays accurately
☐ Payment analytics shows correct data
☐ Customer segments calculated correctly
☐ Data updates on invoice changes
```

**Error Handling**:
```
☐ Invalid input shows error messages
☐ Network error handled gracefully
☐ Database error displays user feedback
☐ Error dialogs appear and dismiss correctly
☐ Error messages are clear and actionable
```

**Performance**:
```
☐ App starts in < 3 seconds
☐ Screen navigation is smooth
☐ List scrolling is fluid
☐ No crashes or ANRs observed
☐ Memory usage is stable
```

---

## DEPLOYMENT STEPS

### Option 1: Android Emulator

**Create/Start Emulator**:
```bash
# List available emulators
emulator -list-avds

# Create if needed (in Android Studio: AVD Manager)

# Start emulator
emulator -avd Pixel_4_API_35
```

**Install APK**:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
# Expected: Success

# Verify installation
adb shell pm list packages | grep bizap
# Should show: com.emul8r.bizap
```

**Launch App**:
```bash
adb shell am start -n com.emul8r.bizap/.MainActivity
# App should launch without crashes
```

### Option 2: Physical Device

**Enable Developer Mode**:
```
1. Go to Settings → About Phone
2. Tap Build Number 7 times
3. Go to Settings → Developer Options
4. Enable USB Debugging
5. Connect via USB
```

**Install APK**:
```bash
adb devices
# Verify device appears in list

adb install -r app/build/outputs/apk/debug/app-debug.apk
# Wait for completion
```

**Launch App**:
```bash
adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

## VERIFICATION

### Post-Deployment Verification

**Check App Installation**:
```bash
adb shell pm dump com.emul8r.bizap | grep version
# Should show version code and name
```

**Verify Permissions**:
```bash
adb shell pm list permissions | grep bizap
# Should show requested permissions granted
```

**Check Logs**:
```bash
adb logcat -c
# Clear previous logs

adb shell am start -n com.emul8r.bizap/.MainActivity
# Launch app

adb logcat | grep bizap
# Monitor for:
#  ✅ Application started
#  ✅ Database initialized
#  ✅ No ERROR level messages
```

### Functional Verification

**Create Test Invoice**:
```
1. Open app
2. Create new invoice
3. Select test customer
4. Enter amount: 100.00
5. Click Create
6. Verify success notification
```

**Record Payment**:
```
1. Open created invoice
2. Click Record Payment
3. Enter amount: 50.00
4. Verify outstanding updated
```

**Check Analytics**:
```
1. Open Analytics/Dashboard
2. Verify revenue displayed correctly
3. Verify outstanding amount shown
4. Create new invoice and verify update
```

---

## TROUBLESHOOTING

### Build Failures

**Issue**: `Build failed with exception: A failure occurred while executing org.gradle.work`

**Solution**:
```bash
# Clear Gradle cache
./gradlew clean

# Rebuild with verbose output
./gradlew build --info

# Check for specific error
# Common causes:
#  - JDK version mismatch
#  - Gradle cache corruption
#  - Missing SDK components
```

**Issue**: `Compilation failed: Unresolved reference`

**Solution**:
```bash
# Sync Gradle files
# Android Studio: File → Sync Now

# Clear IDE cache
# Close Android Studio
# Delete: C:\Users\YourUsername\.gradle
# Delete: .idea folder in project
# Reopen Android Studio
```

### Installation Failures

**Issue**: `adb: command not found`

**Solution**:
```bash
# Set PATH to Android SDK tools
set PATH=%PATH%;C:\Users\YourUsername\AppData\Local\Android\Sdk\platform-tools

# Verify
adb version
# Should show ADB version
```

**Issue**: `Device not found`

**Solution**:
```bash
# List connected devices
adb devices

# If no devices:
#  1. Check USB cable
#  2. Enable USB Debugging on device
#  3. Restart ADB server
adb kill-server
adb start-server

# Try again
adb devices
```

### Runtime Issues

**Issue**: `App crashes on startup`

**Solution**:
```bash
# Check logs for error
adb logcat | grep -i error

# Common causes:
#  - Database migration failure
#  - Missing runtime permissions
#  - Null pointer exceptions

# Fix:
#  1. Check Timber logs
#  2. Run tests to verify logic
#  3. Verify database schema
```

**Issue**: `Data not displaying`

**Solution**:
```bash
# Verify database populated
# Check Timber logs for:
#  - ✅ Database initialized
#  - ✅ Data queries successful
#  - ❌ Any exceptions

# If data missing:
#  1. Create sample data via app UI
#  2. Verify snapshots syncing
#  3. Check analytics calculations
```

---

## ROLLBACK PROCEDURES

### If Deployment Fails

**Step 1: Stop Current App**
```bash
adb shell am force-stop com.emul8r.bizap
```

**Step 2: Uninstall Current Version**
```bash
adb uninstall com.emul8r.bizap
```

**Step 3: Checkout Previous Version**
```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
git log --oneline -5
# Find last good commit

git checkout <commit-hash>
```

**Step 4: Rebuild Previous Version**
```bash
./gradlew clean build

adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Step 5: Verify Previous Version**
```bash
adb shell am start -n com.emul8r.bizap/.MainActivity
# Verify app works
```

### Data Recovery

**If Database Corrupted**:
```bash
# Delete app data
adb shell pm clear com.emul8r.bizap

# Reinstall
adb install -r app/build/outputs/apk/debug/app-debug.apk

# App will reinitialize database
adb shell am start -n com.emul8r.bizap/.MainActivity
```

**Database Backup**:
```bash
# Before deployment, backup database
adb pull /data/data/com.emul8r.bizap/databases/bizap.db ./backup/

# To restore
adb push ./backup/bizap.db /data/data/com.emul8r.bizap/databases/
```

---

## DEPLOYMENT CHECKLIST

### Pre-Deployment

- [ ] All tests passing (421+)
- [ ] Code coverage 80%+
- [ ] No compilation errors
- [ ] Lint analysis green
- [ ] Documentation complete
- [ ] Git repository clean
- [ ] Latest changes committed

### Deployment

- [ ] APK built successfully
- [ ] APK size acceptable (~25MB)
- [ ] App installs without error
- [ ] App launches without crash
- [ ] Logs show no errors

### Post-Deployment

- [ ] Create test invoice
- [ ] Record test payment
- [ ] Check analytics display
- [ ] Verify error handling
- [ ] Monitor logs (24 hours)

---

## SUPPORT CONTACTS

**Build Issues**: Check build.gradle.kts, Gradle version  
**Test Failures**: Review test files in app/src/test/  
**Runtime Errors**: Check Timber logs in logcat  
**Architecture Questions**: See ARCHITECTURE_DOCUMENTATION_COMPLETE.md

---

**Last Updated**: March 10, 2026  
**Version**: 1.0  
**Status**: Complete & Ready for Deployment


