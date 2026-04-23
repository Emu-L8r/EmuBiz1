# ⚡ PERFORMANCE BASELINE QUICK START
**April 23, 2026 — Establish Startup Time Metrics**

---

## OVERVIEW

Measure and document app startup time on multiple devices to establish baseline for tracking performance regressions.

**Time:** 1-1.5 hours  
**Prerequisites:** 2 connected Android devices or emulators  
**Tools:** Android Studio + Android Profiler

---

## PREPARATION (10 minutes)

### Prerequisites Checklist
```
[ ] Android Studio open
[ ] 2 devices/emulators available
[ ] App built in debug mode
[ ] Both devices charged (or plugged in)
[ ] ADB working properly
```

### Device Setup
```
# Device 1 (Reference - Premium)
Recommended: Pixel 6a or Pixel 7
OS: Latest stable (Android 14+)
Storage: At least 5GB free

# Device 2 (Mid-range or Budget)
Recommended: Moto G or mid-range device
OS: Android 11+
Storage: At least 5GB free
```

### Verify ADB Connectivity
```powershell
adb devices -l
```

Expected output:
```
List of attached devices
emulator-5554    device  product:sdk_google_phone_x86_64 ...
192.168.1.x:5555 device  ...
```

---

## MEASUREMENT PROCESS (30 minutes)

### Phase 1: Clear Cache (Device 1)
```powershell
# Clear all app data
adb -s <device1> shell pm clear com.emul8r.bizap

# Verify cleared
adb -s <device1> shell pm path com.emul8r.bizap
```

### Phase 2: Measure Startup - Run 1
```
1. Open Android Studio → Run → Select Device 1
2. Build → Run (Shift+F10)
3. Watch for app launch in Profiler
4. Record startup time from when app becomes visible
5. Expected: 1.5s - 2.5s on premium device
6. Note the time in spreadsheet
7. Close app (back button)
8. Wait 10 seconds for memory cleanup
```

**Startup Time Run 1:** _____ ms

### Phase 3: Measure Startup - Run 2
```
1. Clear cache again: adb -s <device1> shell pm clear com.emul8r.bizap
2. Build → Run again
3. Record startup time
4. Close app
5. Wait 10 seconds
```

**Startup Time Run 2:** _____ ms

### Phase 4: Measure Startup - Run 3
```
1. Clear cache again: adb -s <device1> shell pm clear com.emul8r.bizap
2. Build → Run again
3. Record startup time
4. Close app
5. Wait 10 seconds
```

**Startup Time Run 3:** _____ ms

### Phase 5: Calculate Average (Device 1)
```
Device 1 (Premium) Results:
  Run 1: _____ ms
  Run 2: _____ ms
  Run 3: _____ ms
  Average: _____ ms
  
Performance Target: < 2000 ms (2 seconds)
Status: [ ] PASS [ ] NEEDS OPTIMIZATION
```

### Phase 6: Repeat for Device 2 (Mid-range)
```
Repeat Phases 1-5 with Device 2

Device 2 (Mid-range) Results:
  Run 1: _____ ms
  Run 2: _____ ms
  Run 3: _____ ms
  Average: _____ ms
  
Performance Target: < 2500 ms (2.5 seconds)
Status: [ ] PASS [ ] NEEDS OPTIMIZATION
```

---

## USING ANDROID PROFILER (Alternative Method)

For more detailed metrics:

```
1. Open Android Studio
2. View → Tool Windows → Profiler
3. Select Device 1
4. Build and Run app
5. In Profiler → System Trace tab:
   - Record button starts
   - When app launches, watch Main Thread
   - Look for "Activity.onCreate" → "first frame"
   - Time difference = startup time
6. Compare across runs
```

**Expected Metrics:**
```
Optimistic (Cold Start):       1000-1500ms
Standard (Warm Start):         800-1200ms
Poor (Many Deps Loading):      2000-3000ms
```

---

## RESULTS TEMPLATE

Create file: `docs/PERFORMANCE_BASELINE.md`

```markdown
# Performance Baseline
**Measured:** April 23, 2026

## Startup Time Results

### Device 1: Pixel 6a (Premium)
- Run 1: 1850 ms
- Run 2: 1720 ms
- Run 3: 1910 ms
- **Average: 1827 ms** ✅ PASS
- Target: < 2000 ms

### Device 2: Moto G31 (Mid-range)
- Run 1: 2140 ms
- Run 2: 2080 ms
- Run 3: 2250 ms
- **Average: 2157 ms** ✅ PASS
- Target: < 2500 ms

## Summary
- Premium device: 1827 ms (Excellent)
- Mid-range device: 2157 ms (Good)
- Overall: Performing within targets ✅

## Regression Testing
If startup time increases:
- > 2000 ms on premium device = INVESTIGATE
- > 2500 ms on mid-range device = INVESTIGATE
```

---

## NEXT STEPS FOR OPTIMIZATION

If baseline is slower than targets:

### Quick Wins (1-2 hours each)
1. **Defer non-critical initialization**
   - Move expensive operations to background
   - Lazy-load heavy libraries
   
2. **Optimize database startup**
   - Pre-warm Room database
   - Remove unnecessary migrations
   
3. **Reduce dependency load**
   - Use Hilt lazy initialization
   - Split large modules

### Medium-term (4-8 hours each)
4. **Profile with Android Profiler**
   - Find actual bottlenecks
   - Optimize hotspots

5. **Implement startup benchmarking**
   - Automated regression tests
   - CI/CD performance checks

---

## PERFORMANCE TARGETS

### Ideal Performance
```
Premium Device (Pixel 6a):    < 1500 ms
Mid-range Device (Moto G):    < 2000 ms
Budget Device (5yo phone):    < 3000 ms
```

### Acceptable Performance
```
Premium Device:   1500-2000 ms
Mid-range Device: 2000-2500 ms
Budget Device:    2500-3500 ms
```

### Action Required
```
Premium Device:   > 2000 ms
Mid-range Device: > 2500 ms
Budget Device:    > 3500 ms
```

---

## REGRESSION DETECTION

Add to CI/CD pipeline:

```kotlin
// In performance test
@Test
fun testStartupTime() {
    val startupTime = measureStartup()
    
    assertTrue("Startup too slow!", startupTime < 2000) // premium
    assertTrue("Mid-range startup too slow!", startupTime < 2500)
}
```

---

## DOCUMENTATION

After measurements complete:

1. **Create baseline file**
   - Location: `docs/PERFORMANCE_BASELINE.md`
   - Include: Devices, runs, averages
   - Add: Targets and status

2. **Commit to git**
   ```powershell
   git add docs/PERFORMANCE_BASELINE.md
   git commit -m "docs: Add performance baseline (Pixel 6a: 1827ms, Moto G: 2157ms)"
   ```

3. **Update team**
   - Share baseline with team
   - Explain targets
   - Link to optimization guide

4. **Set CI/CD gate**
   - Add performance check to build
   - Fail build if > baseline + 10%
   - Require perf review for changes

---

## SUCCESS CHECKLIST

```
[ ] Device 1 measurements complete
[ ] Device 2 measurements complete
[ ] Baselines calculated
[ ] Results within targets
[ ] docs/PERFORMANCE_BASELINE.md created
[ ] Changes committed to git
[ ] Team notified
[ ] CI/CD gate configured
[ ] Optimization plan documented
```

---

## QUICK REFERENCE

| Phase | Time | Task |
|-------|------|------|
| Prep | 10 min | Setup devices, verify ADB |
| Device 1 | 15 min | 3 runs + average |
| Device 2 | 15 min | 3 runs + average |
| Document | 10 min | Create baseline file |
| Commit | 5 min | Git commit + push |
| **Total** | **55 min** | **Baseline established** |

---

**Estimated Time:** 1-1.5 hours  
**Difficulty:** Easy  
**Impact:** Important  
**Next Step:** GitHub Actions CI/CD (2-3 hours)

---

**Status:** Ready to implement  
**Date:** April 23, 2026

