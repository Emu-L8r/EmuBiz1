# 🚀 FIREBASE CRASHLYTICS VERIFICATION GUIDE - WINDOWS POWERSHELL

**Status:** Implementation Ready  
**Date:** April 6, 2026  
**Project:** Bizap (com.emul8r.bizap)  
**Firebase Project:** bizap-801c0

---

## QUICK START (5 Minutes)

### Phase 1: List Your Devices
```powershell
adb devices -l
```

**Expected Output:**
```
List of devices attached
emulator-5554          device usb:1-1 product:sdk_google_phone_armv7 model:Android_SDK_built_for_x86 device:generic_x86 transport_id:1
192.168.1.100:5037     device usb:1-2 ...
```

**Your Task:** Copy the device serial (e.g., `emulator-5554` or `192.168.1.100:5037`)

---

### Phase 2: Set Target Serial (CRITICAL - Fixes "more than one device" Error)
```powershell
# Windows PowerShell - Create a variable for your device serial
$DEVICE = "emulator-5554"  # CHANGE THIS to your actual serial from Phase 1

# Verify it works
adb -s $DEVICE shell getprop ro.build.version.release
```

**Expected Output:**
```
14
```

If you get "more than one device/emulator" error, you haven't correctly set `$DEVICE`.

---

### Phase 3: Clear App Data (Fresh Start)
```powershell
$DEVICE = "emulator-5554"  # Your serial here

# Clear Crashlytics state
adb -s $DEVICE shell pm clear com.emul8r.bizap
Write-Host "✅ App data cleared - Crashlytics state reset"

# Wait 3 seconds
Start-Sleep -Seconds 3

# Verify package is installed
$result = adb -s $DEVICE shell pm list packages | Select-String "bizap"
if ($result) {
    Write-Host "✅ Package com.emul8r.bizap is installed"
} else {
    Write-Host "❌ Package NOT found - need to rebuild/install"
}
```

---

### Phase 4: Start Logcat Monitoring (KEEP THIS RUNNING)
```powershell
$DEVICE = "emulator-5554"  # Your serial here

Write-Host "🔴 Starting Logcat - Watching for Crashlytics upload..."
Write-Host "Leave this window OPEN. You'll see: 'Completed report upload'"
Write-Host "`nPress CTRL+C to stop monitoring`n"

# Clear logcat buffer
adb -s $DEVICE logcat -c

# Start real-time monitoring with color-coded output
adb -s $DEVICE logcat | ForEach-Object {
    if ($_ -match "Completed report upload") {
        Write-Host $_ -ForegroundColor Green -BackgroundColor Black
    } elseif ($_ -match "FirebaseCrashlytics") {
        Write-Host $_ -ForegroundColor Cyan
    } elseif ($_ -match "Error|Exception|Failed") {
        Write-Host $_ -ForegroundColor Red
    } else {
        Write-Host $_
    }
}
```

---

### Phase 5: Trigger Test Crash (In Separate PowerShell Window)
```powershell
$DEVICE = "emulator-5554"  # Your serial here

Write-Host "📱 Launching app..."
adb -s $DEVICE shell am start -n com.emul8r.bizap/.MainActivity

Write-Host "⏳ Waiting 5 seconds for app to load..."
Start-Sleep -Seconds 5

Write-Host "🔴 APP IS READY - TAP THE 🔴 RED CIRCLE BUTTON IN BOTTOM-RIGHT"
Write-Host "   The button is ONLY visible in DEBUG builds"
Read-Host "Press ENTER after you see the app crash and force-close"
```

---

### Phase 6: Relaunch App (Triggers Upload)
```powershell
$DEVICE = "emulator-5554"  # Your serial here

Write-Host "🚀 Relaunching app to trigger crash report upload..."
adb -s $DEVICE shell am start -n com.emul8r.bizap/.MainActivity

Write-Host "⏳ App launching... (if Logcat window is open, watch for 'Completed report upload')"
Write-Host "   Expected timeframe: 15-30 seconds from now"
```

**Go back to Phase 4 Logcat window and watch for:**
```
D/FirebaseCrashlytics: Uploading crash report...
D/FirebaseCrashlytics: Completed report upload      ← THIS IS SUCCESS
D/FirebaseCrashlytics: Crash report uploaded successfully
```

---

## DETAILED DIAGNOSTICS (If Something Goes Wrong)

### Diagnostic 1: Verify Device Connectivity
```powershell
$DEVICE = "emulator-5554"

# Test basic connectivity
Write-Host "Testing device connectivity..."
adb -s $DEVICE shell echo "OK"
```

**Expected:** `OK`  
**Failure:** Device not found or unresponsive

---

### Diagnostic 2: Verify App is Installed
```powershell
$DEVICE = "emulator-5554"

Write-Host "Checking if Bizap is installed..."
$packages = adb -s $DEVICE shell pm list packages
$bizap = $packages | Select-String "bizap"

if ($bizap) {
    Write-Host "✅ com.emul8r.bizap is installed"
    Write-Host $bizap
} else {
    Write-Host "❌ com.emul8r.bizap NOT found"
    Write-Host "Run: ./gradlew clean :app:installDebug"
}
```

---

### Diagnostic 3: Check Network Connectivity (Device Can Reach Internet)
```powershell
$DEVICE = "emulator-5554"

Write-Host "Testing device internet connectivity (ping 8.8.8.8)..."
$ping = adb -s $DEVICE shell ping -c 4 8.8.8.8 2>&1 | Out-String

if ($ping -match "icmp_seq") {
    Write-Host "✅ Device HAS internet access"
    Write-Host $ping
} else {
    Write-Host "❌ Device CANNOT reach internet"
    Write-Host "For emulator: Go to Extended Controls > Network > use 'Automatic'"
    Write-Host $ping
}
```

---

### Diagnostic 4: Verify Firebase Project ID is Found
```powershell
$DEVICE = "emulator-5554"

Write-Host "Searching Logcat for Firebase Project ID..."
Write-Host "This will run for 10 seconds..."

adb -s $DEVICE logcat -c
Start-Sleep -Seconds 2
adb -s $DEVICE shell am start -n com.emul8r.bizap/.MainActivity
Start-Sleep -Seconds 8

$logs = adb -s $DEVICE logcat -d

$projectFound = $logs | Select-String "bizap-801c0"
$firebaseInit = $logs | Select-String "FirebaseApp"

if ($projectFound) {
    Write-Host "✅ Project ID 'bizap-801c0' found in logs"
} else {
    Write-Host "⚠️  Project ID not found - google-services.json may not be loading"
}

if ($firebaseInit) {
    Write-Host "✅ Firebase initialization detected"
} else {
    Write-Host "⚠️  Firebase not initializing"
}
```

---

### Diagnostic 5: Full Crash Report Validation
```powershell
$DEVICE = "emulator-5554"

Write-Host "Running full crash validation sequence..."
Write-Host "`n[Step 1] Clearing app data..."
adb -s $DEVICE shell pm clear com.emul8r.bizap
Start-Sleep -Seconds 2

Write-Host "[Step 2] Launching app..."
adb -s $DEVICE shell am start -n com.emul8r.bizap/.MainActivity
Start-Sleep -Seconds 5

Write-Host "[Step 3] Dumping current logcat..."
$logs = adb -s $DEVICE logcat -d

Write-Host "`n=== FIREBASE INITIALIZATION LOGS ==="
$logs | Select-String "FirebaseCrashlytics|FirebaseApp" | Select-Object -First 20

Write-Host "`n=== TIMBER LOGS ==="
$logs | Select-String "Timber" | Select-Object -First 20

Write-Host "`n=== ERRORS/EXCEPTIONS ==="
$logs | Select-String "Error|Exception|Failed" | Select-Object -First 20

Write-Host "`nValidation complete. Check above for issues."
```

---

### Diagnostic 6: Check if Crash File Exists on Device
```powershell
$DEVICE = "emulator-5554"

Write-Host "Checking device storage for crash data..."

# Crashlytics stores crash data in app-specific directory
$crashDir = adb -s $DEVICE shell find /data/data/com.emul8r.bizap -name "*crash*" 2>&1

Write-Host "Crash files found:"
Write-Host $crashDir

if ($crashDir -like "*No such*" -or -not $crashDir) {
    Write-Host "No crash files found yet (this is normal if crash hasn't been triggered)"
} else {
    Write-Host "Crash files exist - should be uploaded on next launch"
}
```

---

## TROUBLESHOOTING TABLE

| Problem | Solution |
|---------|----------|
| "more than one device/emulator" | Set `$DEVICE = "your-serial"` at top of each script |
| "grep: command not found" | Use `Select-String` instead of `grep` |
| Device "offline" | Disconnect/reconnect USB or restart emulator |
| App doesn't have crash button | Rebuild in DEBUG mode: `./gradlew clean :app:installDebug` |
| Upload doesn't appear in Logcat | Check Diagnostic 3 (network) and Diagnostic 4 (Firebase init) |
| Crash doesn't appear in Firebase after 10 min | Package name mismatch (see Diagnostic 1 in attachment doc) |

---

## PowerShell Script: Full Automated Sequence

Save this as `C:\Users\Saucey\test-crashlytics.ps1`:

```powershell
# ============================================================
# FIREBASE CRASHLYTICS - AUTOMATED TEST SEQUENCE
# Windows PowerShell Version
# Usage: Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process; .\test-crashlytics.ps1
# ============================================================

# Configuration
$DEVICE = "emulator-5554"  # CHANGE TO YOUR DEVICE SERIAL

function Test-DeviceConnected {
    $result = adb -s $DEVICE shell echo "OK" 2>&1
    return $result -eq "OK"
}

function Get-PackageStatus {
    $packages = adb -s $DEVICE shell pm list packages 2>&1
    return $packages | Select-String "bizap"
}

function Clear-AppData {
    Write-Host "🧹 Clearing app data..." -ForegroundColor Yellow
    adb -s $DEVICE shell pm clear com.emul8r.bizap 2>&1 | Out-Null
    Start-Sleep -Seconds 2
    Write-Host "✅ App data cleared" -ForegroundColor Green
}

function Launch-App {
    Write-Host "🚀 Launching app..." -ForegroundColor Yellow
    adb -s $DEVICE shell am start -n com.emul8r.bizap/.MainActivity 2>&1 | Out-Null
    Start-Sleep -Seconds 5
    Write-Host "✅ App launched" -ForegroundColor Green
}

function Monitor-Logcat {
    param([int]$DurationSeconds = 60)
    
    Write-Host "📊 Monitoring Logcat for $DurationSeconds seconds..." -ForegroundColor Cyan
    Write-Host "Looking for: 'Completed report upload'" -ForegroundColor Cyan
    
    adb -s $DEVICE logcat -c
    Start-Sleep -Seconds 1
    
    $endTime = (Get-Date).AddSeconds($DurationSeconds)
    $foundUpload = $false
    
    adb -s $DEVICE logcat | ForEach-Object {
        if ((Get-Date) -gt $endTime) {
            exit
        }
        
        if ($_ -match "Completed report upload") {
            Write-Host "🟢 UPLOAD CONFIRMED: $_" -ForegroundColor Green -BackgroundColor Black
            $foundUpload = $true
        } elseif ($_ -match "Uploading crash report") {
            Write-Host "⚙️ UPLOADING: $_" -ForegroundColor Yellow
        } elseif ($_ -match "FirebaseCrashlytics") {
            Write-Host $_ -ForegroundColor Cyan
        } elseif ($_ -match "Error|Exception|Failed") {
            Write-Host "🔴 ERROR: $_" -ForegroundColor Red
        }
    }
    
    return $foundUpload
}

# Main sequence
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║  FIREBASE CRASHLYTICS - AUTOMATED TEST SEQUENCE       ║" -ForegroundColor Magenta
Write-Host "║  Device: $DEVICE" -ForegroundColor Magenta
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Magenta

Write-Host "`n[1/5] Verifying device connection..." -ForegroundColor Yellow
if (-not (Test-DeviceConnected)) {
    Write-Host "❌ Device '$DEVICE' not found or offline" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Device connected" -ForegroundColor Green

Write-Host "`n[2/5] Checking app installation..." -ForegroundColor Yellow
$pkg = Get-PackageStatus
if ($pkg) {
    Write-Host "✅ App installed: $pkg" -ForegroundColor Green
} else {
    Write-Host "❌ App not installed. Run: ./gradlew :app:installDebug" -ForegroundColor Red
    exit 1
}

Write-Host "`n[3/5] Clearing app data (fresh Crashlytics state)..." -ForegroundColor Yellow
Clear-AppData

Write-Host "`n[4/5] Launching app..." -ForegroundColor Yellow
Launch-App

Write-Host "`n[5/5] Starting Logcat monitoring..." -ForegroundColor Yellow
Write-Host "`n⚠️  IMPORTANT: Open app and tap the 🔴 RED BUTTON in bottom-right" -ForegroundColor Yellow
Write-Host "   Then close the app and relaunch it." -ForegroundColor Yellow
Write-Host "   Watch this window for upload confirmation (60 seconds)..`n" -ForegroundColor Yellow

$uploadFound = Monitor-Logcat -DurationSeconds 60

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
if ($uploadFound) {
    Write-Host "║  ✅ CRASH UPLOAD SUCCESSFUL                         ║" -ForegroundColor Green
} else {
    Write-Host "║  ⚠️  Upload not detected - check diagnostics above   ║" -ForegroundColor Yellow
}
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Magenta

Write-Host "`nNext: Check Firebase Console in 5-10 minutes:"
Write-Host "https://console.firebase.google.com/project/bizap-801c0/crashlytics" -ForegroundColor Cyan
```

**To run this script:**
```powershell
# Allow script execution for this session
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process

# Run the script
C:\Users\Saucey\test-crashlytics.ps1
```

---

## VERIFICATION CHECKLIST

- [ ] Device serial identified (adb devices -l)
- [ ] `$DEVICE` variable set correctly
- [ ] Package installed (diagnostic 2)
- [ ] Network accessible (diagnostic 3)
- [ ] App launches successfully
- [ ] Force Crash button visible (red circle, bottom-right)
- [ ] Crash triggered and app force-closes
- [ ] App relaunched
- [ ] "Completed report upload" appears in Logcat
- [ ] Firebase Console updated (5-10 minutes later)

---

## SUCCESS INDICATORS

### ✅ In Logcat (30 seconds after relaunch):
```
D/FirebaseCrashlytics: Enabled
D/FirebaseCrashlytics: Initializing Crashlytics...
D/FirebaseCrashlytics: Uploading crash report...
D/FirebaseCrashlytics: Completed report upload      ← THIS IS THE KEY LINE
D/FirebaseCrashlytics: Crash report uploaded successfully
```

### ✅ In Firebase Console (5-10 minutes later):
- Crash appears under Crashlytics tab
- Exception type: `RuntimeException`
- Message contains: "INTENTIONAL TEST CRASH"
- Custom keys visible: `test_crash_triggered`, `crash_reason`
- Breadcrumb shows: "🔴 TEST CRASH: User pressed Force Crash button"

---

## FINAL NOTES

- **Timeline:** Logcat upload confirmation (20-30s) → Firebase Dashboard update (5-10 min)
- **Network Required:** Device must have internet access. Emulator requires network configuration.
- **Package Matching:** `com.emul8r.bizap` must match exactly in google-services.json AND AndroidManifest.xml
- **Debug Build Required:** Force Crash button only appears in DEBUG builds (not release)

**Status:** Ready for immediate testing. Your infrastructure is correctly configured.

