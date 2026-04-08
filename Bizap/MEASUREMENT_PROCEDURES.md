# MEASUREMENT PROCEDURES - BIZAP v1.1 OPTIMIZATION

**Last Updated:** March 22, 2026  
**Project:** Bizap Android Invoicing App  
**Phase:** Phase 1 (Foundation + Baseline)  
**Purpose:** Standardized measurement procedures for weekly performance tracking

---

## Table of Contents

1. [Overview](#overview)
2. [Weekly Measurement Schedule](#weekly-measurement-schedule)
3. [Build Time Measurement](#build-time-measurement)
4. [Test Coverage Measurement](#test-coverage-measurement)
5. [Startup Time Measurement](#startup-time-measurement)
6. [Memory Usage Measurement](#memory-usage-measurement)
7. [Battery Drain Measurement](#battery-drain-measurement)
8. [Go/No-Go Decision Criteria](#gono-go-decision-criteria)
9. [Reporting Template](#reporting-template)

---

## Overview

This document defines standardized procedures for measuring Bizap's performance metrics. These measurements establish baselines in Phase 1 and track improvements throughout Phases 2-4.

**Key Principles:**
- ✅ Consistent measurement methodology
- ✅ Reproducible results
- ✅ Weekly tracking
- ✅ Data-driven decisions
- ✅ Clear success criteria

---

## Weekly Measurement Schedule

**When:** Every Friday at 5:00 PM  
**Who:** Development team  
**Duration:** ~30 minutes  
**Output:** `WEEK_X_PERFORMANCE_REPORT.md`

**Weekly Checklist:**
```
[ ] Measure build time (3 runs, average)
[ ] Measure test coverage
[ ] Measure startup time (on all 3 devices)
[ ] Measure memory usage (on all 3 devices)
[ ] Measure battery drain (on all 3 devices)
[ ] Generate performance report
[ ] Compare with previous week
[ ] Identify regressions
[ ] Update baseline if needed
```

---

## Build Time Measurement

### Purpose
Track how long it takes to build the app from a clean state. Faster builds improve developer productivity.

### Baseline (Week 1)
- **Build Time:** 122 seconds
- **Date:** March 22, 2026
- **Machine:** CI environment (GitHub Actions)

### Measurement Procedure

#### Method 1: Local Measurement (Development Machine)
```bash
# Navigate to project directory
cd /path/to/Bizap

# Clean previous builds
./gradlew clean

# Measure full build time
time ./gradlew :app:assembleDebug --no-daemon

# Record the "real" time from output
```

#### Method 2: CI Measurement (GitHub Actions)
```bash
# Trigger workflow manually or via PR
# Check workflow run time in GitHub Actions UI
# Record total job duration from "Build debug APK" step
```

#### Best Practices
- Run measurement 3 times and average
- Use same machine/environment for consistency
- Clear Gradle cache between runs
- Disable other running applications
- Measure at same time of day

#### Recording Template
```
Date: YYYY-MM-DD
Build 1: ___s
Build 2: ___s
Build 3: ___s
Average: ___s
Change from last week: ±___s (±___%)
```

### Success Criteria
- ✅ Build time ≤ 120 seconds (target)
- ⚠️ Build time 121-150 seconds (acceptable)
- ❌ Build time > 150 seconds (regression)

---

## Test Coverage Measurement

### Purpose
Ensure adequate test coverage and track testing health.

### Baseline (Week 1)
- **Total Tests:** 994
- **Passing:** 994/994 (100%)
- **Coverage:** To be measured

### Measurement Procedure

#### Step 1: Run All Tests
```bash
cd /path/to/Bizap

# Run all unit tests
./gradlew :app:testDebugUnitTest --no-daemon

# Check test summary in output
```

#### Step 2: Generate Coverage Report (Optional)
```bash
# Add JaCoCo plugin to build.gradle.kts (if not present)
# Run tests with coverage
./gradlew :app:testDebugUnitTestCoverage

# View report at:
# app/build/reports/coverage/test/debug/index.html
```

#### Step 3: Record Metrics
```
Total Tests: _____
Passing: _____
Failing: _____
Skipped: _____
Pass Rate: _____%
Coverage (if available): _____%
```

### Success Criteria
- ✅ 100% tests passing
- ✅ No test regressions
- ✅ Coverage ≥ 70% (target for v1.1)
- ⚠️ Coverage 50-70% (acceptable)
- ❌ Coverage < 50% (needs improvement)

---

## Startup Time Measurement

### Purpose
Measure how long the app takes to launch and display first screen. Fast startup improves user experience.

### Baseline (Week 1)
- **Cold Start:** To be measured
- **Warm Start:** To be measured
- **Hot Start:** To be measured

### Measurement Procedure

#### Prerequisites
- Physical device or emulator (see DEVICE_MATRIX.md)
- ADB installed and device connected
- App already installed on device

#### Step 1: Enable Startup Profiling
```bash
# Enable activity launch time logging
adb shell setprop debug.firebase.analytics.app com.emul8r.bizap
```

#### Step 2: Measure Cold Start
```bash
# Force stop app
adb shell am force-stop com.emul8r.bizap

# Clear app data (optional, for true cold start)
adb shell pm clear com.emul8r.bizap

# Start app and measure
adb shell am start -W -n com.emul8r.bizap/.MainActivity

# Record "TotalTime" from output
```

**Expected Output:**
```
Starting: Intent { cmp=com.emul8r.bizap/.MainActivity }
Status: ok
Activity: com.emul8r.bizap/.MainActivity
ThisTime: 650
TotalTime: 850  ← Record this value
WaitTime: 870
Complete
```

#### Step 3: Measure Warm Start
```bash
# App is in background
adb shell am force-stop com.emul8r.bizap

# Start app (data still in memory)
adb shell am start -W -n com.emul8r.bizap/.MainActivity

# Record "TotalTime"
```

#### Step 4: Measure Hot Start
```bash
# Press Home to send app to background
adb shell input keyevent KEYCODE_HOME

# Immediately bring app back to foreground
adb shell am start -W -n com.emul8r.bizap/.MainActivity

# Record "TotalTime"
```

#### Step 5: Repeat on All Devices
- Low-end device (Android 11, 2GB RAM)
- Mid-range device (Android 13, 6GB RAM)
- High-end device (Android 14, 8GB RAM)

### Recording Template
```
DEVICE: [Low/Mid/High]-end
Cold Start (5 runs, average): ___ms
Warm Start (5 runs, average): ___ms
Hot Start (5 runs, average): ___ms
Change from last week: ±___ms
```

### Success Criteria
**Cold Start:**
- ✅ < 1000ms (excellent)
- ⚠️ 1000-2000ms (acceptable)
- ❌ > 2000ms (needs optimization)

**Warm Start:**
- ✅ < 500ms (excellent)
- ⚠️ 500-1000ms (acceptable)
- ❌ > 1000ms (needs optimization)

**Hot Start:**
- ✅ < 300ms (excellent)
- ⚠️ 300-500ms (acceptable)
- ❌ > 500ms (needs optimization)

---

## Memory Usage Measurement

### Purpose
Track app memory consumption to prevent OOM crashes and ensure smooth performance on low-end devices.

### Baseline (Week 1)
- **Peak Memory:** To be measured
- **Average Memory:** To be measured
- **Memory Leaks:** None detected

### Measurement Procedure

#### Method 1: ADB Command (Quick Check)
```bash
# While app is running, check memory
adb shell dumpsys meminfo com.emul8r.bizap

# Look for "TOTAL PSS" value (in KB)
```

#### Method 2: Android Studio Profiler (Detailed)
1. Open Android Studio
2. Open Profiler (View > Tool Windows > Profiler)
3. Select Bizap process
4. Click "Memory"
5. Perform typical user flow (2 minutes):
   - Navigate to Dashboard
   - Create invoice
   - View invoice list
   - Generate PDF
   - Switch themes
6. Force GC (via Profiler)
7. Record peak memory usage

#### User Flow for Consistency
```
1. Launch app (cold start)
2. View Dashboard (10 seconds)
3. Navigate to Invoice List (10 seconds)
4. Create new invoice (20 seconds)
5. Add 3 line items (20 seconds)
6. Save invoice (5 seconds)
7. View invoice details (10 seconds)
8. Generate PDF (15 seconds)
9. Return to Dashboard (10 seconds)
10. Switch theme (10 seconds)
11. Wait idle (20 seconds)
Total: ~2 minutes
```

### Recording Template
```
DEVICE: [Low/Mid/High]-end
Peak Memory (MB): ___MB
Average Memory (MB): ___MB
Native Memory (MB): ___MB
Java/Kotlin Heap (MB): ___MB
GC Events (count): _____
Total GC Time (ms): ___ms
Memory Leaks: [Yes/No]
Change from last week: ±___MB
```

### Success Criteria
**Low-End Device (2GB RAM):**
- ✅ Peak < 150MB (excellent)
- ⚠️ Peak 150-250MB (acceptable)
- ❌ Peak > 250MB (optimization needed)

**Mid/High-End Device:**
- ✅ Peak < 200MB (excellent)
- ⚠️ Peak 200-300MB (acceptable)
- ❌ Peak > 300MB (optimization needed)

**Memory Leaks:**
- ✅ Zero leaks detected
- ❌ Any memory leak = immediate fix required

---

## Battery Drain Measurement

### Purpose
Ensure app doesn't excessively drain battery, especially in background.

### Baseline (Week 1)
- **Battery Drain:** To be measured
- **Screen-On Time:** To be measured

### Measurement Procedure

#### Prerequisites
- Fully charged device (100%)
- Battery stats reset
- 60-minute test session

#### Step 1: Prepare Device
```bash
# Reset battery stats
adb shell dumpsys batterystats --reset

# Verify battery level
adb shell dumpsys battery | grep level
# Should show: level: 100
```

#### Step 2: Perform Test Session (60 minutes)
**Active Usage (30 minutes):**
- Use app continuously
- Create invoices
- Navigate screens
- Generate PDFs
- Typical workflow

**Background Usage (30 minutes):**
- Send app to background
- Let device idle
- Monitor background activity

#### Step 3: Capture Battery Report
```bash
# After 60 minutes, generate bugreport
adb bugreport bugreport.zip

# Unzip and analyze
unzip bugreport.zip
# Upload bugreport-*.txt to https://www.batteryhistorian.com/
```

#### Step 4: Record Metrics
```bash
# Or quick check via ADB
adb shell dumpsys batterystats com.emul8r.bizap

# Look for:
# - Battery drain percentage
# - Wakelock duration
# - CPU usage
```

### Recording Template
```
DEVICE: [Low/Mid/High]-end
Initial Battery: 100%
Final Battery: ___%
Total Drain: ___%
Drain Per Hour: ___% /hr
Active Usage Time: 30 min
Background Time: 30 min
Screen-On Time: ~30 min
Estimated Full-Day Battery Life: ___hrs
Change from last week: ±___%/hr
```

### Success Criteria
**Active Usage:**
- ✅ < 3% drain/hour (excellent)
- ⚠️ 3-5% drain/hour (acceptable)
- ❌ > 5% drain/hour (needs optimization)

**Background Usage:**
- ✅ < 0.5% drain/hour (excellent)
- ⚠️ 0.5-1% drain/hour (acceptable)
- ❌ > 1% drain/hour (optimization needed)

---

## Go/No-Go Decision Criteria

### Phase Gate Criteria

#### GATE 1: End of Phase 1 (March 31, 2026)
**Decision:** Ready for Phase 2 refactoring?

**GO Criteria (ALL must be met):**
- ✅ All 8 deliverables complete
- ✅ Baseline metrics captured
- ✅ Profiling tools working
- ✅ Device matrix identified
- ✅ Test infrastructure ready
- ✅ Team trained and confident
- ✅ No critical issues

**NO-GO Criteria (ANY of these):**
- ❌ Profiling tools not working
- ❌ Baseline metrics missing
- ❌ Test infrastructure broken
- ❌ Team lacks confidence
- ❌ Critical bugs discovered

#### GATE 2: End of Phase 2 (April 12, 2026)
**Decision:** Ready for Phase 3 optimization?

**GO Criteria:**
- ✅ Design system components extracted
- ✅ State management unified
- ✅ Build time improved or maintained
- ✅ All tests passing
- ✅ No performance regressions

**NO-GO Criteria:**
- ❌ Build time regression > 20%
- ❌ Tests failing after refactor
- ❌ Memory leaks introduced
- ❌ Critical functionality broken

#### GATE 3: End of Phase 3 (May 3, 2026)
**Decision:** Ready for Phase 4 validation?

**GO Criteria:**
- ✅ Performance optimizations complete
- ✅ Build time < 100 seconds
- ✅ Startup time improved
- ✅ Memory usage reduced
- ✅ Battery drain minimized

**NO-GO Criteria:**
- ❌ Performance targets not met
- ❌ New bugs introduced
- ❌ User experience degraded

#### GATE 4: End of Phase 4 (May 17, 2026)
**Decision:** Ready for production release?

**GO Criteria:**
- ✅ All device testing complete
- ✅ Performance targets met
- ✅ No critical bugs
- ✅ Code reviewed and approved
- ✅ Documentation complete
- ✅ Team sign-off

**NO-GO Criteria:**
- ❌ Critical bugs remaining
- ❌ Device compatibility issues
- ❌ Performance regressions
- ❌ Team not confident

---

## Reporting Template

### Weekly Performance Report Template

Save as: `WEEK_X_PERFORMANCE_REPORT.md`

```markdown
# Week X Performance Report
**Date:** YYYY-MM-DD  
**Phase:** X  
**Reporting Period:** MM/DD - MM/DD

## Summary
- 📊 Overall Status: [On Track / At Risk / Blocked]
- 📈 Key Improvements: [List]
- 📉 Regressions: [List]
- 🎯 Next Week Focus: [List]

## Metrics

### Build Time
| Metric | Week X | Week X-1 | Change | Status |
|--------|--------|----------|--------|--------|
| Average Build Time | ___s | ___s | ±___s | ✅/⚠️/❌ |

### Test Coverage
| Metric | Week X | Week X-1 | Change | Status |
|--------|--------|----------|--------|--------|
| Total Tests | ___ | ___ | ±___ | ✅/⚠️/❌ |
| Pass Rate | ___% | ___% | ±___% | ✅/⚠️/❌ |
| Coverage | ___% | ___% | ±___% | ✅/⚠️/❌ |

### Startup Time (Low-End Device)
| Metric | Week X | Week X-1 | Change | Status |
|--------|--------|----------|--------|--------|
| Cold Start | ___ms | ___ms | ±___ms | ✅/⚠️/❌ |
| Warm Start | ___ms | ___ms | ±___ms | ✅/⚠️/❌ |
| Hot Start | ___ms | ___ms | ±___ms | ✅/⚠️/❌ |

### Memory Usage (Low-End Device)
| Metric | Week X | Week X-1 | Change | Status |
|--------|--------|----------|--------|--------|
| Peak Memory | ___MB | ___MB | ±___MB | ✅/⚠️/❌ |
| Average Memory | ___MB | ___MB | ±___MB | ✅/⚠️/❌ |
| Memory Leaks | Yes/No | Yes/No | - | ✅/❌ |

### Battery Drain (Low-End Device)
| Metric | Week X | Week X-1 | Change | Status |
|--------|--------|----------|--------|--------|
| Active Drain | ___%/hr | ___%/hr | ±___%/hr | ✅/⚠️/❌ |
| Background Drain | ___%/hr | ___%/hr | ±___%/hr | ✅/⚠️/❌ |

## Analysis
[Detailed analysis of metrics, trends, and insights]

## Issues Identified
1. [Issue 1]
2. [Issue 2]

## Action Items
- [ ] [Action 1]
- [ ] [Action 2]

## Gate Decision
- **Gate X Status:** GO / NO-GO
- **Confidence Level:** ___% 
- **Blockers:** [None / List blockers]
```

---

## Change Log

| Date | Version | Changes |
|------|---------|---------|
| 2026-03-22 | 1.0 | Initial measurement procedures created |

---

**Document Owner:** Development Team  
**Review Frequency:** Weekly (Fridays)  
**Next Review:** March 29, 2026
