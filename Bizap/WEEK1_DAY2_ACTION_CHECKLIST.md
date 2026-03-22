# 📋 WEEK 1, DAY 2 - IMMEDIATE ACTION CHECKLIST
## March 23, 2026 - Profiling Infrastructure Setup

**Status**: Ready for Tomorrow  
**Focus**: Setup profiling tools and measure first real metrics  
**Estimated Time**: 4-5 hours  

---

## MORNING STANDUP (9:00 AM)

```
TEAM AGENDA (15 minutes):
  1. Review Day 1 results (baseline captured ✅)
  2. Confirm today's objectives
  3. Assign tasks if team involved
  4. Identify blockers
```

---

## TASK 1: Android Profiler Setup (9:15-10:00 AM)

### Goal
Learn how to use Android Studio Profiler to measure CPU, Memory, and Network performance.

### Steps

**Step 1: Launch Profiler**
```
1. Open Android Studio
2. Click "Profile" in top menu → "Profile or Debug"
3. Select target app (Bizap)
4. Profiler window opens (should show CPU, Memory, Network tabs)
```

**Step 2: Capture CPU Profile**
```
1. Click "CPU" tab
2. Build and run app on emulator/device: ./gradlew installDebug
3. Start app and navigate through screens
4. Record for 1-2 minutes
5. Click "Stop" button
6. Analyze results:
   - Look for high CPU spikes
   - Note which methods consume most CPU
   - Record findings
```

**Step 3: Capture Memory Profile**
```
1. Click "Memory" tab
2. App should already be running
3. Record for 1-2 minutes while:
   - Scrolling invoice list
   - Changing themes
   - Navigating between screens
4. Take note of:
   - Peak memory usage (___MB)
   - Memory leaks (should be stable over time)
   - Garbage collection events
```

**Step 4: Document Findings**
```
Create: PROFILER_DAY2_FINDINGS.txt

Record:
  - CPU Profiler:
    * Highest consuming method: _____________
    * Peak CPU usage: ___%
    * Normal CPU usage: ___%
  
  - Memory Profiler:
    * Peak memory: ___MB
    * Baseline memory: ___MB
    * GC events: ___
    * Any leaks detected: Yes/No
```

---

## TASK 2: Battery Historian Installation (10:00-11:00 AM)

### Goal
Setup Battery Historian to analyze battery drain in detail.

### Steps

**Option A: Local Installation (Recommended)**

```
1. Check if you have Python 3 installed:
   python --version
   
2. Download Battery Historian:
   git clone https://github.com/google/battery-historian.git
   cd battery-historian
   
3. Install dependencies:
   pip install -r requirements.txt
   
4. Run locally:
   python charge.py
   
5. Open browser: http://localhost:9000

6. Generate battery dump from device:
   adb shell dumpsys batterystats > battery.txt
   
7. Upload battery.txt to Battery Historian web interface
   
8. Analyze results:
   - Look for power drain sources
   - Note high battery consumers
   - Record findings
```

**Option B: Cloud Installation (If local fails)**

```
1. Go to: https://www.batteryhistorian.com/
2. Upload battery dump (instructions on site)
3. Analyze online
```

### Document Findings

```
Create: BATTERY_HISTORIAN_DAY2_FINDINGS.txt

Record:
  - Top battery consumer: _____________
  - Total drain over test period: ___%
  - Estimated screen-on time: ___hrs
  - Any unusual battery spikes: Yes/No
```

---

## TASK 3: Measurement Procedures Documentation (11:00 AM-12:00 PM)

### Goal
Create a guide for how to consistently measure and compare metrics week-to-week.

### Create File: MEASUREMENT_PROCEDURES.md

```markdown
# How to Measure Performance Metrics

## Weekly Measurement Schedule

Every Friday at 5:00 PM, run these measurements:

### 1. Build Time
```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
$start = Get-Date
./gradlew clean build -x test
$end = Get-Date
Write-Host "Build time: $(($end - $start).TotalSeconds) seconds"
```
Record: _____ seconds

### 2. Test Coverage
```bash
./gradlew app:testDebugUnitTestCoverage
# Look at report: app/build/reports/jacoco/testDebugUnitTestCoverage/html/index.html
```
Record: ____% coverage

### 3. Device Metrics
**On real device (Android 13, Pixel 4a if available):**

Startup Time:
- Force stop app
- Time app from tap to fully loaded
- Record: _____ ms

Memory:
- Open Profiler
- Record peak memory while scrolling list
- Record: _____ MB

Battery (if device available):
- Run app for 5 minutes of normal use
- Measure battery percentage drop
- Record: ___% drain

### 4. Create Weekly Report

File: WEEK_X_PERFORMANCE_REPORT.md

```
| Metric | Week X | Week X-1 | Trend |
|--------|--------|----------|-------|
| Build Time | ___s | ___s | → |
| Coverage | __% | __% | → |
| Startup | ___ms | ___ms | → |
| Memory | ___MB | ___MB | → |
| Battery | ___%/hr | ___%/hr | → |
```

### 5. Analyze

GREEN 🟢 (proceed):
- Build time: ±10% of previous
- Coverage: ≥ previous week
- All metrics stable or better

YELLOW 🟡 (monitor):
- Build time: 10-30% slower
- Startup: 5-20% slower
- Memory: 5-15% higher

RED 🔴 (STOP and investigate):
- Build time: > 30% slower
- Startup: > 20% slower
- Memory: > 15% higher
- Battery: > 2% increase
```

---

## TASK 4: Device Matrix Identification (12:00-1:00 PM)

### Goal
Identify which devices will be used for testing throughout the 8 weeks.

### Create File: DEVICE_MATRIX.md

```markdown
# Device Testing Matrix

## Devices Needed

| Category | Device Name | Android | RAM | Status |
|----------|-------------|---------|-----|--------|
| Low-end | Motorola G9 | 11 | 2GB | ⏳ Identify |
| Mid-range | Pixel 4a | 13 | 6GB | ⏳ Identify |
| High-end | Pixel 6 | 14 | 8GB | ⏳ Identify |

## Instructions

### If you have physical devices:
1. List devices available to you
2. Note Android version (Settings → About → Android Version)
3. Note available RAM
4. Update table above

### If using emulator:
1. Create AVD for each scenario:
   - Low-end: 2GB RAM, 480x800 screen, Android 11
   - Mid-range: 6GB RAM, 1080x2340 screen, Android 13
   - High-end: 8GB RAM, 1440x3120 screen, Android 14
2. Document emulator names in table above

### Testing Schedule
```
WEEK 7 (May 1-3):
  Monday: Test on low-end device
  Tuesday: Test on mid-range device
  Wednesday: Test on high-end device
```

## Quick Test on Each Device

For each device, run this quick test:

```
1. Install app: ./gradlew installDebug
2. Measure cold start time (force stop then open)
3. Record: _____ ms
4. Scroll through invoice list (100+ items)
5. Check for jank/stuttering
6. Change theme (rapid clicks)
7. Note any crashes
```
```

---

## LUNCH (1:00-2:00 PM)

---

## AFTERNOON: TEAM TRAINING (2:00-5:00 PM)

### Session 1: Profiler Walkthrough (2:00-3:00 PM)

**If you have a team:**
```
1. Show profiler setup (30 min)
2. Demonstrate measurements (20 min)
3. Team practices on emulator (10 min)
```

**If solo:**
```
1. Practice profiler on emulator
2. Get comfortable with tool
3. Know where to find CPU/Memory tabs
```

### Session 2: Battery Historian Demo (3:00-4:00 PM)

**Walk through:**
```
1. How to generate battery dump
2. How to analyze results
3. Where to find battery consumers
4. How to interpret graphs
```

### Session 3: Measurement Procedures Review (4:00-5:00 PM)

```
1. Review MEASUREMENT_PROCEDURES.md
2. Discuss weekly measurement schedule
3. Confirm everyone understands go/no-go criteria
4. Answer questions
```

---

## END OF DAY CHECKPOINT (5:00 PM)

### Complete This Checklist

```
✅ Android Profiler Setup
  - [ ] Profiler opened successfully
  - [ ] CPU profile captured (1-2 min)
  - [ ] Memory profile captured (1-2 min)
  - [ ] Findings documented in PROFILER_DAY2_FINDINGS.txt
  
✅ Battery Historian Setup
  - [ ] Battery Historian installed/setup
  - [ ] Battery dump generated
  - [ ] Results analyzed
  - [ ] Findings documented in BATTERY_HISTORIAN_DAY2_FINDINGS.txt

✅ Documentation Created
  - [ ] MEASUREMENT_PROCEDURES.md created
  - [ ] DEVICE_MATRIX.md created
  - [ ] Device matrix populated

✅ Team Training
  - [ ] Profiler walkthrough completed
  - [ ] Battery Historian demo completed
  - [ ] Measurement procedures explained
  - [ ] Questions answered
  - [ ] Team confident with tools
```

---

## DELIVERABLES FOR DAY 2

By end of day, you should have:

```
✅ PROFILER_DAY2_FINDINGS.txt
   - CPU profiling results
   - Memory profiling results
   - Initial observations

✅ BATTERY_HISTORIAN_DAY2_FINDINGS.txt
   - Battery drain analysis
   - Top consumers identified
   - Baseline established

✅ MEASUREMENT_PROCEDURES.md
   - How to measure each metric
   - Weekly schedule
   - Go/no-go criteria

✅ DEVICE_MATRIX.md
   - Devices identified (3 devices, 2 Android versions)
   - Testing schedule planned
   - Quick test procedure documented
```

---

## IF YOU GET STUCK

### Profiler Issues?
- Check: Android Studio → Help → Check for Updates
- Tutorial: https://developer.android.com/studio/profile
- Try: Run pre-existing app to debug (helps learn tool)

### Battery Historian Issues?
- Check: Python installed? (`python --version`)
- Try: Web version instead (https://www.batteryhistorian.com/)
- Reference: https://github.com/google/battery-historian

### Need Help?
- Reach out to your team
- Check Android developer documentation
- Ask in Android development forums

---

## NEXT: DAY 3 (March 24)

```
Focus: Device setup and device farm creation
Task: Identify all 3 devices you'll test on
Task: Create device testing protocol
```

---

**DAY 2 ESTIMATED TIME**: 4-5 hours  
**STATUS**: Ready to begin tomorrow (March 23)  
**GOAL**: Master profiling tools & identify devices  
**CONFIDENCE**: Tools will work, plan is solid

