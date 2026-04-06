# 🔥 FIREBASE CRASHLYTICS - QUICK START CARD

**Status:** Ready to Execute  
**Time Required:** 15 minutes  
**Success Indicator:** "Completed report upload" in Logcat

---

## 3-STEP SETUP (One-Time)

### Step 1: Allow PowerShell Scripts
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
# Answer: Y (yes)
```

### Step 2: Get Device Serial
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
.\detect-devices.ps1
```
**Copy the serial (e.g., `emulator-5554`)**

### Step 3: Choose Your Path

---

## 🚀 PATH A: AUTOMATED (Easiest - 10 minutes)

```powershell
.\test-crashlytics-full.ps1 -Device "emulator-5554"
```

**That's it.** Script handles everything:
1. Verifies device
2. Clears app data
3. Launches app
4. Waits for you to tap 🔴 button
5. Relaunches app
6. Monitors Logcat for upload

**Result:** See `🟢 ✅ UPLOAD CONFIRMED` in output

---

## 🎯 PATH B: STEP-BY-STEP (Most Control - 15 minutes)

### Terminal 1: Diagnostics
```powershell
.\run-diagnostics.ps1 -Device "emulator-5554"
```
**All checks should show ✅**

### Terminal 1: Logcat Monitoring (Keep Open!)
```powershell
.\monitor-logcat.ps1 -Device "emulator-5554"
```
**Watch for GREEN line with "UPLOAD CONFIRMED"**

### Terminal 2: Trigger Crash
```powershell
.\trigger-crash.ps1 -Device "emulator-5554"
```
**Follow on-screen instructions:**
1. Find red 🔴 button (bottom-right)
2. Tap it
3. App crashes
4. Press ENTER

### Terminal 2: Relaunch & Upload
```powershell
.\relaunch-app.ps1 -Device "emulator-5554"
```
**Watch Terminal 1 for upload confirmation**

---

## ✅ SUCCESS LOOKS LIKE THIS

### In Logcat (Terminal 1):
```
D/FirebaseCrashlytics: Enabled
D/FirebaseCrashlytics: Initializing Crashlytics...
D/FirebaseCrashlytics: Uploading crash report...
🟢 ✅ UPLOAD CONFIRMED: D/FirebaseCrashlytics: Completed report upload
```

### In Firebase Console (5-10 min later):
1. Go to: https://console.firebase.google.com/project/bizap-801c0/crashlytics
2. Refresh (Ctrl+R)
3. See crash with:
   - Exception: `RuntimeException`
   - Message: "INTENTIONAL TEST CRASH"
   - Custom Keys: `test_crash_triggered`, `crash_reason`

---

## 🚨 QUICK FIXES

| Problem | Fix |
|---------|-----|
| "more than one device" | Use `detect-devices.ps1` to get serial |
| Device "offline" | Reconnect USB or restart emulator |
| Can't find red button | Run `./gradlew clean :app:installDebug` |
| Upload not showing | Check `run-diagnostics.ps1` output |
| Still no upload after 10min | Check Firebase package matches AndroidManifest |

---

## 🔑 KEY LINES TO WATCH

**Logcat shows SUCCESS when:**
```
"Completed report upload"     ← This line = SUCCESS!
```

**Do NOT expect:**
```
grep                          ← Wrong OS syntax (use Select-String)
Firebase Console update < 5min ← Takes 5-10 min delay
```

---

## 📁 ALL FILES IN ONE PLACE

```
C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\
├── 00_MASTER_SUMMARY_CRASHLYTICS.md          ← Overview
├── START_HERE_CRASHLYTICS_EXECUTION.md        ← Start here
├── FIREBASE_CRASHLYTICS_POWERSHELL_GUIDE.md  ← Full guide
├── CRASHLYTICS_POWERSHELL_QUICK_REF.md       ← Quick ref
├── test-crashlytics-full.ps1                  ← Automated
├── detect-devices.ps1                         ← Find device
├── run-diagnostics.ps1                        ← Verify setup
├── monitor-logcat.ps1                         ← Watch logs
├── trigger-crash.ps1                          ← Crash app
└── relaunch-app.ps1                           ← Upload
```

---

## ⏱️ TIMELINE

```
You Do → System Does → Watch For → Success Indicator
─────────────────────────────────────────────────────
Tap 🔴  → App crashes  → Logcat    → RuntimeException
        ↓
Relaunch → Crash detected on startup
        ↓
        → Uploading  → Logcat    → "Uploading crash report..."
        ↓
        → Uploaded   → Logcat    → "Completed report upload" ✅
        ↓
        → Firebase processes (5-10 min)
        ↓
        → Dashboard updated → Firebase Console → Crash visible ✅
```

---

## 💡 IMPORTANT NOTES

- **Force Crash Button** is only visible in DEBUG builds
- **Upload confirmation** in Logcat appears within 30 seconds of relaunch
- **Firebase Console** update takes 5-10 minutes after successful Logcat upload
- **Network required** - Device must have internet access
- **Device serial** changes if you restart emulator - run `detect-devices.ps1` again

---

## 🎬 ACTION NOW

### Choose one:
```powershell
# Option A: Automated
.\test-crashlytics-full.ps1 -Device "emulator-5554"

# Option B: Step-by-step
.\run-diagnostics.ps1 -Device "emulator-5554"
```

**Replace `emulator-5554` with your device serial from `detect-devices.ps1`**

---

## ❓ QUESTIONS?

### Q: Where do I see the upload confirmation?
**A:** In Logcat (monitor-logcat.ps1 window) - green highlighted line

### Q: How long does Firebase take to update?
**A:** Logcat: 30 seconds. Firebase Console: 5-10 minutes

### Q: What if I don't see "Completed report upload"?
**A:** Run `run-diagnostics.ps1` - shows exactly what's wrong

### Q: Can I test multiple times?
**A:** Yes! Each run clears data for fresh test. Run as many times as needed.

### Q: Is this removing the button from production?
**A:** No, button is in `if (BuildConfig.DEBUG)` block - won't show in releases

---

**Status:** Ready. Pick a path and execute. You got this! 🚀

