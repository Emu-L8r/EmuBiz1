# 🎯 FIREBASE CRASHLYTICS - POWERSHELL QUICK REFERENCE CARD

## COPY-PASTE READY COMMANDS

### 1️⃣ STEP ONE: IDENTIFY YOUR DEVICE

**List all connected devices:**
```powershell
adb devices -l
```

**Look for serial like:** `emulator-5554` or `192.168.1.100:5037`

Then set this at the top of every command below:
```powershell
$DEVICE = "emulator-5554"    # CHANGE THIS to your actual serial
```

---

### 2️⃣ STEP TWO: CLEAR APP DATA (Fresh State)

```powershell
$DEVICE = "emulator-5554"

adb -s $DEVICE shell pm clear com.emul8r.bizap
Write-Host "✅ App data cleared"
```

---

### 3️⃣ STEP THREE: LAUNCH APP & START MONITORING

**Terminal Window A** - Start this FIRST and keep it open:
```powershell
$DEVICE = "emulator-5554"

Write-Host "🔴 Starting Logcat monitoring... (Press Ctrl+C to stop)"
Write-Host "Watching for: 'Completed report upload'`n"

adb -s $DEVICE logcat | ForEach-Object {
    if ($_ -match "Completed report upload") {
        Write-Host "✅✅✅ UPLOAD CONFIRMED: $_" -ForegroundColor Green -BackgroundColor Black
    } elseif ($_ -match "FirebaseCrashlytics|Uploading crash") {
        Write-Host $_ -ForegroundColor Cyan
    } elseif ($_ -match "Error|Failed|Exception") {
        Write-Host $_ -ForegroundColor Red
    }
}
```

---

### 4️⃣ STEP FOUR: TRIGGER TEST CRASH

**Terminal Window B** - Run this after app is loaded:
```powershell
$DEVICE = "emulator-5554"

# Launch app
adb -s $DEVICE shell am start -n com.emul8r.bizap/.MainActivity
Write-Host "⏳ App loading... (5 seconds)"
Start-Sleep -Seconds 5

Write-Host "🔴 APP READY - TAP THE RED BUTTON IN BOTTOM-RIGHT"
Read-Host "Press ENTER after app crashes"
```

---

### 5️⃣ STEP FIVE: RELAUNCH & CAPTURE UPLOAD

**Terminal Window B** - After crash, run this:
```powershell
$DEVICE = "emulator-5554"

Write-Host "🚀 Relaunching app to trigger upload..."
adb -s $DEVICE shell am start -n com.emul8r.bizap/.MainActivity

Write-Host "Watch Terminal Window A for upload confirmation (next 30 seconds)"
Start-Sleep -Seconds 30
```

**You should see in Terminal Window A:**
```
D/FirebaseCrashlytics: Uploading crash report...
✅✅✅ UPLOAD CONFIRMED: D/FirebaseCrashlytics: Completed report upload
```

---

## 🔧 DIAGNOSTIC COMMANDS

### Check Device Status
```powershell
$DEVICE = "emulator-5554"
adb -s $DEVICE shell getprop ro.build.version.release
```
**Expected:** Android version number (e.g., `14`)

### Verify App Installed
```powershell
$DEVICE = "emulator-5554"
adb -s $DEVICE shell pm list packages | Select-String "bizap"
```
**Expected:** `package:com.emul8r.bizap`

### Test Network Connectivity
```powershell
$DEVICE = "emulator-5554"
adb -s $DEVICE shell ping -c 4 8.8.8.8
```
**Expected:** Output with `icmp_seq=1,2,3,4` and times

### Force-Stop App
```powershell
$DEVICE = "emulator-5554"
adb -s $DEVICE shell am force-stop com.emul8r.bizap
```

### View All Logcat (Last 100 lines)
```powershell
$DEVICE = "emulator-5554"
adb -s $DEVICE logcat -d | Select-Object -Last 100
```

### Filter Logcat for Firebase Only
```powershell
$DEVICE = "emulator-5554"
adb -s $DEVICE logcat -d | Select-String "FirebaseCrashlytics"
```

### Filter Logcat for Errors
```powershell
$DEVICE = "emulator-5554"
adb -s $DEVICE logcat -d | Select-String "Error|Exception|Failed"
```

### Filter for Your Specific Log Message
```powershell
$DEVICE = "emulator-5554"
adb -s $DEVICE logcat -d | Select-String "🔴 TEST CRASH"
```

---

## 🚨 COMMON PROBLEMS & FIXES

### Problem: "more than one device/emulator"
**Fix:** Make sure you have `$DEVICE = "..."` set correctly before running commands

```powershell
# List devices to find your serial
adb devices -l

# Then use it in commands
$DEVICE = "emulator-5554"  # YOUR SERIAL HERE
adb -s $DEVICE shell pm list packages
```

---

### Problem: "grep: command not found"
**Fix:** Use `Select-String` instead of `grep`

```powershell
# ❌ WRONG (Linux command)
adb logcat | grep FirebaseCrashlytics

# ✅ RIGHT (PowerShell command)
adb -s $DEVICE logcat | Select-String "FirebaseCrashlytics"
```

---

### Problem: Can't find the Crash Button
**Fix:** Button only appears in DEBUG builds. Rebuild:

```powershell
# From your Bizap directory
./gradlew clean :app:installDebug
```

---

### Problem: Crash doesn't appear in Firebase after 10 minutes
**Checklist:**
1. Did you see "Completed report upload" in Logcat? → If NO, check network
2. Is the package name correct? → Check `android:package` in AndroidManifest.xml matches `google-services.json`
3. Is the app crashing at all? → Try again, make sure you tap the 🔴 button
4. Is the device online? → Run `adb -s $DEVICE shell ping 8.8.8.8`

---

## 📊 AUTOMATED SCRIPT (One-Command Test)

**Save as `test-crashlytics-full.ps1`, then run:**
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\test-crashlytics-full.ps1
```

This script handles all 6 steps automatically.

---

## ✅ SUCCESS CHECKLIST

- [ ] Device serial identified and set in `$DEVICE`
- [ ] Logcat monitoring window open
- [ ] App launched successfully
- [ ] 🔴 Red button visible in app
- [ ] Button tapped, app crashed
- [ ] App relaunched
- [ ] "Completed report upload" appears in Logcat
- [ ] Firebase Console shows crash (5-10 min later)

---

## 📱 EXPECTED TIMELINE

| Time | Action | Expected Output |
|------|--------|-----------------|
| T+0s | Tap 🔴 button | RuntimeException in Logcat |
| T+2s | App force-closes | App disappears |
| T+10s | Relaunch app | App reappears |
| T+15s | Crashlytics initializes | `D/FirebaseCrashlytics: Enabled` |
| T+20s | Upload starts | `Uploading crash report...` |
| T+25s | **Upload complete** | **`Completed report upload`** ← SUCCESS |
| T+5-10min | Firebase processes | Crash appears in Console |

---

## 🔗 REFERENCE LINKS

- **Firebase Console:** https://console.firebase.google.com/project/bizap-801c0/crashlytics
- **Android Studio Logcat:** View → Tool Windows → Logcat
- **ADB Documentation:** https://developer.android.com/studio/command-line/adb

---

## 🎓 KEY CONCEPTS

### `-s <serial>` Flag
Targets a specific device when multiple are connected.

```powershell
# Without -s: tries all devices → "more than one device/emulator" error
adb shell pm list packages

# With -s: targets specific device → works fine
adb -s emulator-5554 shell pm list packages
```

### Select-String (PowerShell equivalent of grep)
Filters lines matching a pattern.

```powershell
# Get all lines containing "FirebaseCrashlytics"
adb -s $DEVICE logcat -d | Select-String "FirebaseCrashlytics"

# Get lines matching multiple patterns
adb -s $DEVICE logcat -d | Select-String "Error|Failed|Exception"

# Case-insensitive matching
adb -s $DEVICE logcat -d | Select-String -Pattern "crashlytics" -IgnoreCase
```

### Logcat Buffer Management
Fresh monitoring requires clearing the buffer:

```powershell
# Clear logcat buffer
adb -s $DEVICE logcat -c

# Wait for new logs
Start-Sleep -Seconds 2

# Start monitoring
adb -s $DEVICE logcat
```

---

## 📝 NOTES FOR FUTURE SESSIONS

- Device serial may change if you restart emulator
- Always run `adb devices -l` first to get current serial
- Network connectivity is critical - if "Completed report upload" doesn't appear, check device can ping 8.8.8.8
- Firebase Console updates 5-10 minutes AFTER successful Logcat upload confirmation
- Force Crash button only visible in DEBUG builds, not RELEASE

---

**Status:** Ready to execute. All commands tested for Windows PowerShell.

