# 📋 FIREBASE CRASHLYTICS - EXECUTION CHECKLIST

**Project:** Bizap (com.emul8r.bizap)  
**Date Started:** ___________  
**Status:** Ready to Execute

---

## PART 1: PRE-EXECUTION (One-Time Setup)

### System Check
- [ ] Windows PowerShell is available
- [ ] ADB is installed and working (`adb --version` shows version)
- [ ] Emulator is running OR physical device is connected
- [ ] USB debugging enabled (for physical devices)

### PowerShell Setup
- [ ] Run: `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`
- [ ] Answer "Y" when prompted
- [ ] Verify: `Get-ExecutionPolicy` shows "RemoteSigned"

### File Preparation
- [ ] All script files are in: `C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\`
- [ ] All 6 PowerShell scripts present:
  - [ ] test-crashlytics-full.ps1
  - [ ] detect-devices.ps1
  - [ ] run-diagnostics.ps1
  - [ ] monitor-logcat.ps1
  - [ ] trigger-crash.ps1
  - [ ] relaunch-app.ps1

### Device Verification
- [ ] Run: `adb devices -l`
- [ ] Device appears in list
- [ ] Device status is "device" (not "offline")
- [ ] Copy device serial: ___________________

---

## PART 2: DIAGNOSTICS (5 minutes)

### Get Device Serial
- [ ] Run: `.\detect-devices.ps1`
- [ ] Identify your device serial: ___________________
- [ ] Note the serial for next steps

### Run Full Diagnostics
- [ ] Run: `.\run-diagnostics.ps1 -Device "[YOUR-SERIAL]"`
- [ ] Check all 6 tests pass with ✅:
  - [ ] [TEST 1/6] Device Connection - ✅
  - [ ] [TEST 2/6] Package Installation - ✅
  - [ ] [TEST 3/6] Network Connectivity - ✅
  - [ ] [TEST 4/6] Firebase Initialization - ✅
  - [ ] [TEST 5/6] Critical Errors - ✅
  - [ ] [TEST 6/6] Google Services Configuration - ✅

### If Diagnostics Fail
- [ ] Note which test failed: _______________________
- [ ] Consult troubleshooting section in START_HERE_CRASHLYTICS_EXECUTION.md
- [ ] Fix issue and re-run diagnostics
- [ ] Verify all ✅ before proceeding

---

## PART 3: CRASH TEST - EXECUTION (10 minutes)

### Option A: Fully Automated (Easiest)
- [ ] Run: `.\test-crashlytics-full.ps1 -Device "[YOUR-SERIAL]"`
- [ ] Script launches app
- [ ] Script displays instructions
- [ ] Find the red 🔴 button on device/emulator screen
- [ ] TAP the red button
- [ ] App crashes and force-closes
- [ ] Press ENTER in PowerShell window
- [ ] Script relaunches app
- [ ] Watch for green "UPLOAD CONFIRMED" line
- [ ] Crash upload confirmed ✅

**If using Option A, skip to PART 4**

---

### Option B: Step-by-Step (More Control)

#### Step 1: Start Logcat Monitoring
- [ ] Open a **NEW PowerShell window**
- [ ] Change to project directory: `cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap`
- [ ] Run: `.\monitor-logcat.ps1 -Device "[YOUR-SERIAL]"`
- [ ] Window shows "Starting Logcat stream..." message
- [ ] Leave this window open for the rest of the test
- [ ] You'll see logs streaming

#### Step 2: Trigger Crash
- [ ] In **original PowerShell window**, run:
  `.\trigger-crash.ps1 -Device "[YOUR-SERIAL]"`
- [ ] Wait for instructions on screen
- [ ] Look at device/emulator screen for app
- [ ] Find red 🔴 button (bottom-right corner)
- [ ] TAP the red button
- [ ] App crashes and force-closes
- [ ] Press ENTER in PowerShell when app crashes

#### Step 3: Relaunch App
- [ ] In **original PowerShell window**, run:
  `.\relaunch-app.ps1 -Device "[YOUR-SERIAL]"`
- [ ] App relaunches on device/emulator
- [ ] Switch to **Logcat window** (the one from Step 1)

#### Step 4: Verify Upload
- [ ] In **Logcat window**, watch for lines like:
  ```
  D/FirebaseCrashlytics: Enabled
  D/FirebaseCrashlytics: Initializing Crashlytics...
  D/FirebaseCrashlytics: Uploading crash report...
  ```
- [ ] Look for **GREEN** highlighted line:
  ```
  🟢 ✅ UPLOAD CONFIRMED: D/FirebaseCrashlytics: Completed report upload
  ```
- [ ] Time this appeared: ___________________
- [ ] Upload confirmed ✅

---

## PART 4: VERIFICATION (Immediate)

### Logcat Check
- [ ] ✅ See "Completed report upload" in green in Logcat window
- [ ] Time from app relaunch to upload: ~__________ seconds
- [ ] Expected: 15-30 seconds ✅

### Device Check
- [ ] App is running normally on device/emulator
- [ ] No force-close errors visible
- [ ] App is responsive

### Success Indicators
- [ ] 🟢 Logcat shows upload confirmation ✅
- [ ] ✅ All diagnostics passed earlier ✅
- [ ] ✅ App didn't crash again after relaunch ✅

---

## PART 5: FIREBASE CONSOLE VERIFICATION (5-10 minutes)

### Wait for Processing
- [ ] Note current time: ___________________
- [ ] Wait 5-10 minutes for Firebase to process
- [ ] Continue with other work during wait

### Check Firebase Console
- [ ] Open browser
- [ ] Go to: https://console.firebase.google.com/project/bizap-801c0/crashlytics
- [ ] Sign in if needed
- [ ] Click "Crashlytics" tab
- [ ] Click "Refresh" button (or press Ctrl+R)
- [ ] Look for new crash entry
- [ ] Time crash appeared in console: ___________________

### Verify Crash Details
- [ ] Exception Type: ✅ RuntimeException
- [ ] Message contains: ✅ "INTENTIONAL TEST CRASH"
- [ ] Custom Keys visible: 
  - [ ] test_crash_triggered = true ✅
  - [ ] crash_reason = "Manual test..." ✅
- [ ] Breadcrumb visible:
  - [ ] "🔴 TEST CRASH: User pressed Force Crash button" ✅
- [ ] Timestamp matches your test ✅
- [ ] Device/Android version correct ✅

---

## PART 6: FINAL CONFIRMATION

### Complete Timeline
```
Logcat Upload Confirmed:        _______________
Firebase Console Updated:        _______________
Total Time from Crash to Console: _____ minutes
```

### All Tests Passed
- [ ] Diagnostics: All 6 tests ✅
- [ ] Crash Trigger: App crashed ✅
- [ ] Crash Upload: "Completed report upload" in Logcat ✅
- [ ] Firebase Display: Crash visible in console ✅
- [ ] Crash Details: All information correct ✅

### Success Status
- [ ] ✅ CRASHLYTICS VERIFICATION COMPLETE
- [ ] ✅ FIREBASE UPLOAD WORKING
- [ ] ✅ CONFIGURATION VERIFIED

---

## TROUBLESHOOTING LOG

If you encounter any issues, note them here:

### Issue #1
**Problem:** _________________________________  
**Diagnosed:** _________________________________  
**Fixed By:** _________________________________  
**Resolution Time:** ____________

### Issue #2
**Problem:** _________________________________  
**Diagnosed:** _________________________________  
**Fixed By:** _________________________________  
**Resolution Time:** ____________

### Issue #3
**Problem:** _________________________________  
**Diagnosed:** _________________________________  
**Fixed By:** _________________________________  
**Resolution Time:** ____________

---

## PERFORMANCE METRICS

```
Device Serial:                  __________________
Device Model:                   __________________
Android Version:                __________________
App Build Type:                 __________________
Network Type (WiFi/Mobile/Eth):  __________________

Timing Measurements:
  ├─ Time to crash after button tap:        _____ seconds
  ├─ Time to logcat upload confirmation:    _____ seconds
  ├─ Time to firebase console update:       _____ minutes
  ├─ Total time from crash to console:      _____ minutes
  └─ Average over multiple tests:           _____ minutes
```

---

## NOTES

```
Test Run #1 Date: _______________
Result: ________________________

Test Run #2 Date: _______________
Result: ________________________

Test Run #3 Date: _______________
Result: ________________________

Additional Notes:
_________________________________________________
_________________________________________________
_________________________________________________
```

---

## SIGN-OFF

### Test Conductor
**Name:** _____________________  
**Date:** _____________________  
**Signature:** _____________________

### Verification Results
✅ **All Tests Passed**  
⚠️  **Some Issues Found** (see troubleshooting log above)  
❌ **Tests Failed** (see troubleshooting log above)

### Final Status
**Firebase Crashlytics Verification:** 
- ✅ SUCCESSFUL
- ⚠️  PARTIAL
- ❌ FAILED

**Next Action:**
_________________________________________________

---

## REFERENCE

- **Firebase Console:** https://console.firebase.google.com/project/bizap-801c0/crashlytics
- **Execution Guide:** START_HERE_CRASHLYTICS_EXECUTION.md
- **Quick Reference:** CRASHLYTICS_POWERSHELL_QUICK_REF.md
- **Troubleshooting:** FIREBASE_CRASHLYTICS_POWERSHELL_GUIDE.md (section: Troubleshooting)

---

**Status:** Ready to begin. Good luck! 🚀

