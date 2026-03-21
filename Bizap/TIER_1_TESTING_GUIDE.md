# 🧪 TIER 1 TESTING GUIDE
**Status:** Ready for Device Testing  
**Expected Duration:** 15-20 minutes per device  
**Goal:** Verify no crashes, loading states working, APK smaller

---

## Quick Start (5 minutes)

```bash
# 1. Build fresh APK
./gradlew clean assembleDebug -x test

# 2. Install on device
./gradlew installDebug

# 3. Open Logcat to watch for crashes
adb logcat -s AndroidRuntime:E

# 4. Launch app from terminal
adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

## Test Cases

### Test 1: App Launch (2 minutes)
**Goal:** Verify app launches without crashing

**Steps:**
1. `adb shell am start -n com.emul8r.bizap/.MainActivity`
2. Watch for:
   - ✅ Splash screen appears
   - ✅ Dashboard appears (or Loading screen)
   - ❌ No crashes in logcat
   - ❌ No "App stopped" dialog

**Expected Result:** App fully loads in <5 seconds

---

### Test 2: Loading States (3 minutes)
**Goal:** Verify loading screens work while data loads

**Steps:**
1. Launch app
2. Watch Dashboard while loading
3. Look for:
   - ✅ "Loading business profile..." message (if loading)
   - ✅ Spinner animation
   - ✅ Then content appears
   - ❌ No blank screen
   - ❌ No crashes

**Expected Result:** See spinner → then content (no blanks/crashes)

---

### Test 3: Profile Display (2 minutes)
**Goal:** Verify profile displays safely

**Steps:**
1. Wait for Dashboard to load
2. Check header for:
   - ✅ Business name displays (or "Default Business")
   - ✅ ABN displays (or "Not Set")
   - ❌ No crashes
   - ❌ No null pointer exceptions

**Expected Result:** Business profile displays without crashes

---

### Test 4: Navigation (3 minutes)
**Goal:** Verify navigation works end-to-end

**Steps:**
1. Dashboard loaded
2. Click "View Revenue Analytics" (if available)
3. Click back
4. Click on an invoice (if available)
5. Click back
6. Watch for:
   - ✅ Smooth navigation
   - ✅ No crashes
   - ✅ No loading issues

**Expected Result:** All navigation works smoothly

---

### Test 5: Theme Switching (2 minutes)
**Goal:** Verify theme switching still works

**Steps:**
1. Navigate to Settings
2. Find theme toggle (Classic ↔ Modern)
3. Toggle theme
4. Watch for:
   - ✅ Theme changes immediately
   - ✅ No crash
   - ✅ Navigation still works
   - ❌ No data loss

**Expected Result:** Instant theme switch, no crashes

---

### Test 6: Logcat Verification (2 minutes)
**Goal:** Verify no errors in system logs

**Steps:**
```bash
# 1. Clear logcat
adb logcat -c

# 2. Restart app
adb shell am start -n com.emul8r.bizap/.MainActivity

# 3. Wait 10 seconds for full load
sleep 10

# 4. Check for errors
adb logcat -s AndroidRuntime:E
```

**Look For:**
- ❌ NO "UnsatisfiedLinkError"
- ❌ NO "NullPointerException"
- ❌ NO "ClassCastException"
- ❌ NO "java.lang.Exception"

**Expected Result:** Only info/warning logs, no errors

---

## Crash Analysis Flowchart

```
Does app crash on launch?
├─ YES: Check Logcat for error type
│  ├─ UnsatisfiedLinkError → Native library issue (shouldn't happen)
│  ├─ NullPointerException → Need more null checks
│  ├─ ClassCastException → ViewModel injection issue
│  └─ Other → Report the error
│
└─ NO: App launched successfully ✅
   │
   └─ Does Dashboard load?
      ├─ NO: Check if "Loading profile..." shows
      │  ├─ YES: Wait 5 seconds
      │  │  ├─ Loads: Good ✅
      │  │  └─ Crashes: Report error
      │  └─ NO: Something's missing
      │
      └─ YES: Dashboard loaded ✅
         │
         └─ Verify profile displays (business name, ABN)
            ├─ YES: All good ✅✅✅
            └─ NO: Null safety may need improvement
```

---

## Success Criteria

✅ **Minimal:** All 6 tests pass without crashes  
✅ **Good:** <3 seconds load time  
✅ **Excellent:** <1 second load time  

---

## If Crashes Occur

**Steps:**
1. Note the exact error from Logcat
2. Note which screen crashed
3. Note what action caused it
4. Report in format:

```
CRASH REPORT:
- Device: [Pixel/Emulator]
- Screen: [Dashboard/Invoice List/etc]
- Action: [Clicked X, Navigated to Y, etc]
- Error: [Copy from Logcat]
- Expected: [What should happen]
```

---

## APK Size Verification

```bash
# Check debug APK size
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Expected: 15-18 MB (down from 33 MB with release build)
# If > 20 MB: Native library exclusion may not have worked
```

---

## Performance Metrics

| Metric | Expected | Check |
|--------|----------|-------|
| **App Launch** | <5 sec | Start time |
| **Dashboard Load** | <3 sec | Loading spinner duration |
| **Theme Switch** | <500 ms | Visual lag |
| **Navigation** | <1 sec | Between screens |
| **Crashes** | 0 | Logcat errors |

---

## Troubleshooting

### "App stops immediately after launch"
```bash
# Check full logcat
adb logcat -d | grep -i "error\|exception\|crash"

# Likely causes:
# 1. Database initialization (wait 5 sec)
# 2. Missing profile (should show "Loading...")
# 3. Data load issue (check Logcat)
```

### "Loading screen shows forever"
```bash
# Check ViewModel state
adb logcat -d | grep "ViewModel\|StateFlow\|collect"

# Likely causes:
# 1. Data not loading (check network)
# 2. ViewModel not emitting (check code)
# 3. State loading error (check database)
```

### "Some fields show blank instead of loading"
- This is OK! Means null safety is working
- Blank fields = safe degradation
- Should not crash

---

## Next Steps After Testing

1. **If All Pass:** ✅ Ready for Tier 2 implementation
2. **If Some Fail:** Report crash, apply fix, retest
3. **If Performance Slow:** Acceptable for dev build, will improve in release

---

**Ready to Test?** Run the Quick Start commands above!  
**Found a Crash?** Report using the template above  
**All Passed?** Start Tier 2 implementation
