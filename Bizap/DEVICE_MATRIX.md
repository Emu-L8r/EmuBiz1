# DEVICE MATRIX - BIZAP v1.1 TESTING

**Last Updated:** March 22, 2026  
**Project:** Bizap Android Invoicing App  
**Phase:** Phase 1 (Foundation + Baseline)  
**Purpose:** Define test device matrix for performance validation

---

## Overview

This document identifies the three-tier device matrix used for testing Bizap v1.1. Testing across low-end, mid-range, and high-end devices ensures the app performs well for all users.

**Testing Philosophy:**
- ✅ Test on real devices (preferred) or emulators
- ✅ Cover Android 11, 13, 14 (80% of user base)
- ✅ Test low RAM scenarios (2GB minimum)
- ✅ Validate performance across all tiers
- ✅ Identify device-specific issues early

---

## Device Selection Criteria

### Low-End Device Requirements
- **Android Version:** 11 (API level 30)
- **RAM:** 2GB
- **Storage:** 32GB
- **Purpose:** Worst-case performance testing
- **Target Users:** Budget-conscious small businesses

### Mid-Range Device Requirements
- **Android Version:** 13 (API level 33)
- **RAM:** 6GB
- **Storage:** 128GB
- **Purpose:** Average user experience testing
- **Target Users:** Majority of Bizap users

### High-End Device Requirements
- **Android Version:** 14 (API level 34)
- **RAM:** 8GB+
- **Storage:** 256GB+
- **Purpose:** Best-case performance, future-proofing
- **Target Users:** Power users, large businesses

---

## Device Matrix

### Tier 1: Low-End Device (Budget Tier)

#### Option A: Physical Device
**Recommended Device:** Samsung Galaxy A14 5G or equivalent

| Specification | Value |
|--------------|-------|
| **Model** | Samsung Galaxy A14 5G |
| **Android Version** | 11 (upgradeable to 13) |
| **RAM** | 4GB (2GB variant available in some regions) |
| **Storage** | 64GB |
| **Processor** | MediaTek Dimensity 700 |
| **Screen** | 6.6" FHD+ (1080 x 2408) |
| **Price** | ~$150-200 USD |
| **Availability** | Widely available |

**Alternative Devices:**
- Motorola Moto G Power (2021) - Android 11, 3GB RAM
- Nokia G20 - Android 11, 4GB RAM
- Realme C25s - Android 11, 4GB RAM

#### Option B: Android Emulator (Low-End)
**Emulator Configuration:**

```
AVD Name: Bizap_LowEnd_Android11
Device: Pixel 4a
System Image: Android 11 (API 30) x86_64
RAM: 2048 MB
Internal Storage: 4096 MB
Heap: 256 MB
Graphics: Automatic (or Software if hardware acceleration unavailable)
```

**Create Emulator (Android Studio):**
```bash
# Via AVD Manager GUI
1. Open Android Studio
2. Tools > Device Manager
3. Create Virtual Device
4. Select "Pixel 4a"
5. Select "R" (Android 11, API 30)
6. Advanced Settings:
   - RAM: 2048 MB
   - Internal Storage: 4096 MB
   - Graphics: Automatic
7. Finish

# Or via command line
android create avd -n Bizap_LowEnd_Android11 \
  -k "system-images;android-30;google_apis;x86_64" \
  -d "pixel_4a" \
  -c 4096M \
  --ram 2048
```

**Performance Profile:**
- ⚠️ Slower app startup (2-3 seconds)
- ⚠️ Visible UI lag on complex screens
- ⚠️ Memory pressure during PDF generation
- ⚠️ Battery drain more noticeable
- ✅ Perfect for stress testing

---

### Tier 2: Mid-Range Device (Mainstream Tier)

#### Option A: Physical Device
**Recommended Device:** Google Pixel 6a or Samsung Galaxy A54 5G

| Specification | Value |
|--------------|-------|
| **Model** | Google Pixel 6a |
| **Android Version** | 13 (upgradeable to 14) |
| **RAM** | 6GB |
| **Storage** | 128GB |
| **Processor** | Google Tensor |
| **Screen** | 6.1" OLED (1080 x 2400) |
| **Price** | ~$350-400 USD |
| **Availability** | Widely available |

**Alternative Devices:**
- Samsung Galaxy A54 5G - Android 13, 6GB RAM
- OnePlus Nord N20 5G - Android 12, 6GB RAM
- Xiaomi Redmi Note 12 Pro - Android 13, 8GB RAM

#### Option B: Android Emulator (Mid-Range)
**Emulator Configuration:**

```
AVD Name: Bizap_MidRange_Android13
Device: Pixel 6
System Image: Android 13 (API 33) x86_64
RAM: 6144 MB
Internal Storage: 8192 MB
Heap: 512 MB
Graphics: Hardware - GLES 3.0
```

**Create Emulator:**
```bash
# Via command line
android create avd -n Bizap_MidRange_Android13 \
  -k "system-images;android-33;google_apis;x86_64" \
  -d "pixel_6" \
  -c 8192M \
  --ram 6144
```

**Performance Profile:**
- ✅ Smooth app startup (1-2 seconds)
- ✅ Fluid UI animations
- ✅ Comfortable memory headroom
- ✅ Good battery life
- ✅ Represents majority of users

---

### Tier 3: High-End Device (Premium Tier)

#### Option A: Physical Device
**Recommended Device:** Google Pixel 8 Pro or Samsung Galaxy S24

| Specification | Value |
|--------------|-------|
| **Model** | Google Pixel 8 Pro |
| **Android Version** | 14 |
| **RAM** | 12GB |
| **Storage** | 256GB |
| **Processor** | Google Tensor G3 |
| **Screen** | 6.7" LTPO OLED (1344 x 2992) |
| **Price** | ~$900-1000 USD |
| **Availability** | Widely available |

**Alternative Devices:**
- Samsung Galaxy S24 - Android 14, 8GB RAM
- OnePlus 11 - Android 14, 12GB RAM
- Xiaomi 14 - Android 14, 12GB RAM

#### Option B: Android Emulator (High-End)
**Emulator Configuration:**

```
AVD Name: Bizap_HighEnd_Android14
Device: Pixel 8 Pro
System Image: Android 14 (API 34) x86_64
RAM: 8192 MB (8GB)
Internal Storage: 12288 MB (12GB)
Heap: 1024 MB
Graphics: Hardware - GLES 3.1
```

**Create Emulator:**
```bash
# Via command line
android create avd -n Bizap_HighEnd_Android14 \
  -k "system-images;android-34;google_apis;x86_64" \
  -d "pixel_8_pro" \
  -c 12288M \
  --ram 8192
```

**Performance Profile:**
- ✅ Instant app startup (<1 second)
- ✅ Silky smooth animations
- ✅ Ample memory for all operations
- ✅ Excellent battery life
- ✅ Future-proof validation

---

## Quick Test Procedure

### Pre-Test Setup (All Devices)

```bash
# 1. Install latest APK
adb install -r app-debug.apk

# 2. Reset battery stats
adb shell dumpsys batterystats --reset

# 3. Clear app data (fresh start)
adb shell pm clear com.emul8r.bizap

# 4. Verify device ready
adb shell getprop ro.build.version.sdk  # Check Android version
adb shell cat /proc/meminfo | grep MemTotal  # Check RAM
```

### Standard Test Flow (10 minutes per device)

#### Test Scenario: New User Experience
**Duration:** 10 minutes  
**Purpose:** Validate typical user journey

```
┌─────────────────────────────────────────────────────────────┐
│ STEP │ ACTION                        │ TIME   │ VERIFY     │
├─────────────────────────────────────────────────────────────┤
│  1   │ Launch app (cold start)       │ 0:00   │ Startup    │
│  2   │ View Landing screen           │ 0:05   │ UI loads   │
│  3   │ Setup business profile        │ 0:30   │ Save works │
│  4   │ View Dashboard                │ 1:00   │ Analytics  │
│  5   │ Create first customer         │ 2:00   │ Validation │
│  6   │ Create first invoice          │ 3:00   │ Form works │
│  7   │ Add 3 line items              │ 4:00   │ Calc OK    │
│  8   │ Save invoice                  │ 5:00   │ Persists   │
│  9   │ View invoice details          │ 5:30   │ Data OK    │
│ 10   │ Generate PDF                  │ 6:00   │ PDF works  │
│ 11   │ Switch theme (Classic/Modern) │ 7:00   │ No crash   │
│ 12   │ Navigate all main screens     │ 8:00   │ Smooth     │
│ 13   │ Send app to background        │ 9:00   │ Persists   │
│ 14   │ Bring back to foreground      │ 9:30   │ Restored   │
│ 15   │ Final verification            │ 10:00  │ All OK     │
└─────────────────────────────────────────────────────────────┘
```

#### Test Scenario: Power User Workflow
**Duration:** 10 minutes  
**Purpose:** Stress test with realistic workload

```
1. Create 10 customers (bulk entry)
2. Create 20 invoices (rapid fire)
3. Add 5 line items each (100 total)
4. Generate 5 PDFs simultaneously
5. Search/filter invoice list
6. Update customer details
7. Record payments on 10 invoices
8. View analytics/dashboard
9. Export data
10. Switch themes multiple times
```

### Performance Checklist (Per Device)

```
DEVICE: [Low/Mid/High]-End
DATE: YYYY-MM-DD
TESTER: [Name]

[ ] App installs successfully
[ ] Cold start time: ___ms (< 2000ms target)
[ ] Warm start time: ___ms (< 1000ms target)
[ ] Hot start time: ___ms (< 500ms target)
[ ] UI is responsive (no lag)
[ ] No crashes during test flow
[ ] PDF generation works
[ ] Theme switching works
[ ] Data persists correctly
[ ] Memory usage acceptable (< 250MB low-end)
[ ] No memory leaks detected
[ ] Battery drain reasonable (< 5%/hr active)
[ ] All animations smooth
[ ] No visual glitches
[ ] Offline mode works
[ ] Network errors handled gracefully

PASS/FAIL: [PASS / FAIL]
NOTES: [Any issues or observations]
```

---

## Device Testing Matrix

### Weekly Testing Schedule

| Device Tier | Mon | Tue | Wed | Thu | Fri |
|-------------|-----|-----|-----|-----|-----|
| Low-End     | -   | -   | ✅  | -   | ✅  |
| Mid-Range   | -   | ✅  | -   | -   | ✅  |
| High-End    | ✅  | -   | -   | -   | ✅  |

**Rationale:**
- Monday: High-end (quick validation after weekend)
- Tuesday: Mid-range (mid-week check)
- Wednesday: Low-end (thorough stress test)
- Friday: All tiers (weekly performance report)

### Pre-Release Testing (Phase 4)

Before any release, test on ALL devices:

```
[ ] Low-End Device - Android 11
    [ ] Functionality test
    [ ] Performance test
    [ ] Battery test
    [ ] Stress test

[ ] Mid-Range Device - Android 13
    [ ] Functionality test
    [ ] Performance test
    [ ] Battery test
    [ ] Stress test

[ ] High-End Device - Android 14
    [ ] Functionality test
    [ ] Performance test
    [ ] Battery test
    [ ] Stress test

[ ] Cross-device validation
    [ ] Data sync between devices
    [ ] Theme consistency
    [ ] Feature parity
```

---

## ADB Testing Commands

### Quick Device Info
```bash
# Device model
adb shell getprop ro.product.model

# Android version
adb shell getprop ro.build.version.release

# API level
adb shell getprop ro.build.version.sdk

# RAM (total memory in KB)
adb shell cat /proc/meminfo | grep MemTotal

# CPU info
adb shell cat /proc/cpuinfo

# Screen density
adb shell wm density

# Screen resolution
adb shell wm size
```

### Performance Monitoring
```bash
# Monitor memory in real-time
adb shell dumpsys meminfo com.emul8r.bizap | grep -A 10 "App Summary"

# Monitor CPU usage
adb shell top -m 10 | grep bizap

# Monitor battery
adb shell dumpsys battery

# Monitor frame rate
adb shell dumpsys gfxinfo com.emul8r.bizap framestats
```

### Testing Utilities
```bash
# Simulate low memory condition
adb shell am send-trim-memory com.emul8r.bizap RUNNING_CRITICAL

# Simulate battery saver mode
adb shell settings put global low_power 1

# Reset to normal
adb shell settings put global low_power 0

# Simulate slow network
# (Requires root or emulator)
adb shell tc qdisc add dev wlan0 root netem delay 200ms

# Simulate network failure
adb shell svc wifi disable
adb shell svc data disable

# Restore network
adb shell svc wifi enable
adb shell svc data enable
```

---

## Device Farm Options

### Option 1: Firebase Test Lab
- **Pros:** Real devices, automated, integrates with CI/CD
- **Cons:** Costs money, limited free tier
- **Setup:** See Firebase documentation

### Option 2: BrowserStack App Live
- **Pros:** Large device selection, real devices
- **Cons:** Expensive, manual testing
- **Use Case:** Pre-release validation

### Option 3: Local Device Farm
- **Pros:** No cost, full control
- **Cons:** Manual setup, maintenance overhead
- **Recommendation:** Start with emulators, add 1-2 physical devices

---

## Success Criteria

### Phase 1 (Current)
- ✅ Device matrix identified
- ✅ Testing procedures documented
- ✅ Emulators configured (or physical devices ready)
- ✅ Team can run tests on all tiers

### Phase 4 (Final Validation)
- ✅ All tests pass on low-end device
- ✅ All tests pass on mid-range device
- ✅ All tests pass on high-end device
- ✅ Performance targets met across all tiers
- ✅ No device-specific bugs
- ✅ Battery/memory acceptable on all devices

---

## Appendix: Emulator Management

### Start Emulators via Command Line
```bash
# List available AVDs
emulator -list-avds

# Start specific emulator
emulator -avd Bizap_LowEnd_Android11 &
emulator -avd Bizap_MidRange_Android13 &
emulator -avd Bizap_HighEnd_Android14 &

# Start with specific settings
emulator -avd Bizap_LowEnd_Android11 \
  -gpu host \
  -memory 2048 \
  -cores 2 \
  -no-snapshot-load
```

### Emulator Performance Tips
1. Enable hardware acceleration (Intel HAXM or Hyper-V)
2. Use x86_64 images (faster than ARM)
3. Allocate enough RAM to host machine
4. Close other applications during testing
5. Use SSD for AVD storage
6. Consider running emulators in headless mode for CI

---

## Change Log

| Date | Version | Changes |
|------|---------|---------|
| 2026-03-22 | 1.0 | Initial device matrix created |

---

**Document Owner:** Development Team  
**Review Frequency:** Monthly  
**Next Review:** April 22, 2026
