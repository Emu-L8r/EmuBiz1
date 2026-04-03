# PHASE 1: MEASUREMENT PROCEDURES

## 📊 Build Time Measurement

### Procedure
```bash
# Clear gradle cache
./gradlew clean

# Measure clean build (3x, take average)
time ./gradlew build -x test 2>&1 | grep -E "BUILD|Time"

# Expected: ~122 seconds
```

### Tools
- Built-in `time` command
- Gradle task timing output

---

## ⚡ Startup Time Measurement (Android Profiler)

### Prerequisites
- Device connected via USB
- USB debugging enabled
- Android Studio with Profiler installed

### Step-by-Step

**1. Launch Android Profiler**
```
Android Studio → View → Tool Windows → Profiler
```

**2. Connect Device**
- Select your device from dropdown
- Wait for "Connected" status

**3. Install & Launch App**
```bash
./gradlew installDebug
adb shell am start -n com.emul8r.bizap/.ui.main.MainActivity
```

**4. Measure (Cold Start)**
- Profiler auto-records when app launches
- Look for: First system event → First frame drawn
- Record time in milliseconds
- Check "System Trace" for frame rendering details

**5. Repeat 3x**
- Close app completely: `adb shell am force-stop com.emul8r.bizap`
- Wait 10 seconds
- Repeat measurement
- Calculate average

### Metrics to Record
- **Cold Start**: App not in memory (most realistic)
- **Warm Start**: App in memory, background
- **Hot Start**: App in foreground (fastest)

### Expected Results
```
Low-end (2GB RAM):   3000-5000ms cold start
Mid-range (6GB RAM): 1500-2500ms cold start
High-end (8GB RAM):  1000-1500ms cold start
```

---

## 💾 Memory Usage Measurement (Android Profiler)

### Prerequisites
- Device connected via USB
- App installed

### Step-by-Step

**1. Launch Memory Profiler**
```
Android Studio → View → Tool Windows → Profiler
→ Select "Memory" tab
```

**2. Launch App**
```bash
./gradlew installDebug
adb shell am start -n com.emul8r.bizap/.ui.main.MainActivity
```

**3. Wait for Stable State** (Dashboard loaded)
- Record memory at this point (idle memory)

**4. Scroll Payment List**
- Navigate to payment list
- Scroll to bottom (100+ items)
- Watch memory peak in Profiler
- Record peak memory value

**5. After Scroll**
- Wait 5 seconds for GC
- Record idle memory after scroll

**6. Repeat on All 3 Devices**

### Metrics to Record
- Memory at app launch
- Memory after dashboard load
- Peak memory during scroll
- Memory after GC

### Expected Results
```
Low-end:   150-250 MB peak
Mid-range: 200-350 MB peak
High-end:  250-400 MB peak
```

---

## 🔋 Battery Drain Measurement (Battery Historian)

### Prerequisites
- Device connected via USB
- `adb` command-line tools
- Battery Historian installed (Python tool)

### Installation
```bash
# Install Python 3.x first, then:
git clone https://github.com/google/battery-historian.git
cd battery-historian
python historian.py
```

### Step-by-Step Procedure

**Day 1: Baseline Capture**
```bash
# Reset battery stats
adb shell dumpsys batterystats --reset

# Capture initial state
adb shell dumpsys batterystats > before.txt

# Record initial battery % (Settings → Battery)
# Expected: ~100%
```

**Day 2-6: Active Usage (5 hours total)**

Hour 1: Dashboard Viewing
```
- Open app
- View dashboard (2 min)
- Idle in app (5 min)
- View dashboard again (2 min)
- Repeat 5x
- Total: 1 hour at varying activity
```

Hour 2: Payment List Scrolling
```
- Navigate to payment list
- Scroll to bottom (3 min)
- Idle (2 min)
- Scroll to top (2 min)
- Idle (3 min)
- Repeat for 1 hour
```

Hour 3: Analytics View
```
- Navigate to analytics
- View graphs (5 min)
- Scroll analytics (3 min)
- Idle (2 min)
- Repeat for 1 hour
```

Hour 4: Creating Invoices
```
- Create new invoice (10 min)
- Edit invoice (5 min)
- Save (1 min)
- View created invoice (3 min)
- Idle (1 min)
- Repeat for 1 hour
```

Hour 5: Idle with Sync
```
- Open app
- Let background sync run
- Keep screen on (use display timeout settings)
- Total: 1 hour idle
```

**After 5 Hours: Final Capture**
```bash
# Capture final state
adb shell dumpsys batterystats > after.txt

# Record final battery % (Settings → Battery)
# Expected: 70-80% (depending on baseline drain)
```

**Generate Report**
```bash
# Convert to HTML for analysis
adb shell dumpsys batterystats > dump.txt
# Upload to Battery Historian web interface
```

### Calculate Battery Drain
```
Battery Drain = (Initial % - Final %) / Hours
               = (100 - 80) / 5 hours
               = 20 / 5
               = 4%/hour (active use)

Acceptable Range:
- Low-end:   4-6 %/hour
- Mid-range: 2-4 %/hour
- High-end:  1-3 %/hour
```

---

## 📋 Measurement Checklist

### Week 1 Setup
- [ ] Build time baseline: 122s ✅
- [ ] Profiling tools verified working
- [ ] Device drivers installed
- [ ] ADB connectivity confirmed
- [ ] Battery Historian installed

### Week 2 Measurements (Per Device)

#### Low-End Device (2GB RAM, Android 10)
- [ ] Startup time: ____ ms (3x average)
- [ ] Memory peak: ____ MB
- [ ] Battery drain: ____%/hr
- [ ] Status: ✅ Complete

#### Mid-Range Device (6GB RAM, Android 12)
- [ ] Startup time: ____ ms (3x average)
- [ ] Memory peak: ____ MB
- [ ] Battery drain: ____%/hr
- [ ] Status: ✅ Complete

#### High-End Device (8GB RAM, Android 14)
- [ ] Startup time: ____ ms (3x average)
- [ ] Memory peak: ____ MB
- [ ] Battery drain: ____%/hr
- [ ] Status: ✅ Complete

### Documentation
- [ ] Results documented in PHASE_1_STARTUP_METRICS.md
- [ ] Results documented in PHASE_1_MEMORY_METRICS.md
- [ ] Results documented in PHASE_1_BATTERY_METRICS.md
- [ ] Analysis complete

---

## 📊 Recording Template

### Startup Time Results
```
Device: [Device Name]
RAM: [2GB/6GB/8GB]
Android: [10/12/14]

Test 1: ____ ms
Test 2: ____ ms
Test 3: ____ ms
Average: ____ ms

Notes: [Any observations]
```

### Memory Results
```
Device: [Device Name]
RAM: [2GB/6GB/8GB]
Android: [10/12/14]

Idle Memory: ____ MB
After Dashboard: ____ MB
Peak During Scroll: ____ MB
After GC: ____ MB

Notes: [Any observations]
```

### Battery Results
```
Device: [Device Name]
RAM: [2GB/6GB/8GB]
Android: [10/12/14]

Initial Battery: ____ %
Final Battery: ____ %
Hours Tested: 5 hours
Drain: ____%/hour

Breakdown:
- Hour 1 (Dashboard): ____%
- Hour 2 (Scrolling): ____%
- Hour 3 (Analytics): ____%
- Hour 4 (Creating): ____%
- Hour 5 (Idle): ____%

Notes: [Any observations]
```

---

## ⚠️ Important Notes

### Cold vs Warm vs Hot Start
- **Cold Start**: App not in memory, most realistic measurement
- **Warm Start**: App closed but still in RAM
- **Hot Start**: App in background/foreground

Always measure **cold start** for app launch time.

### Battery Testing Variables
- Screen brightness: Keep at 50%
- WiFi: Enable (constant connection)
- Mobile: Disable (avoid network switching)
- Location: Disable
- Background apps: Close all non-essential apps

### Repeating Measurements
- Always test 3x minimum
- Take average of 3 tests
- Discard outliers only if clear anomaly
- Document any anomalies

### When to Re-measure
- After code changes to critical path
- Every Friday (weekly checkpoint)
- Before/after optimization
- If kill switch triggered


