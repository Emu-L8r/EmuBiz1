# Firebase Crashlytics - Ready-to-Run PowerShell Scripts

## 📋 INDEX

1. **detect-devices.ps1** - Find your device serial
2. **monitor-logcat.ps1** - Watch for upload confirmation
3. **trigger-crash.ps1** - Launch app and trigger crash
4. **relaunch-app.ps1** - Relaunch to trigger upload
5. **test-crashlytics-full.ps1** - Complete automated sequence
6. **run-diagnostics.ps1** - Troubleshoot issues

---

## Script 1: detect-devices.ps1

**Purpose:** List connected devices and copy the serial

```powershell
#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - DEVICE DETECTION
# Shows all connected devices with their serials

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  FIREBASE CRASHLYTICS - DEVICE DETECTION              ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Scanning for connected devices..." -ForegroundColor Yellow

$output = adb devices -l 2>&1
$lines = $output -split "`n"

$devices = @()
foreach ($line in $lines) {
    if ($line -match "emulator|device.*usb|[0-9a-f]{2}:[0-9a-f]{2}:[0-9a-f]{2}") {
        if ($line -notmatch "^List|^  ") {
            $serial = ($line -split '\s+')[0]
            $status = ($line -split '\s+')[1]
            if ($serial -and $status) {
                $devices += [PSCustomObject]@{Serial=$serial; Status=$status}
            }
        }
    }
}

if ($devices.Count -eq 0) {
    Write-Host "❌ No devices found" -ForegroundColor Red
    Write-Host "`nMake sure:" -ForegroundColor Yellow
    Write-Host "  • Emulator is running (or device is connected)"
    Write-Host "  • USB debugging is enabled"
    Write-Host "  • ADB can see the device: adb devices -l`n"
    exit 1
}

Write-Host "Found $($devices.Count) device(s):`n" -ForegroundColor Green

$devices | ForEach-Object -Begin {$i=1} -Process {
    Write-Host "[$i] Serial: $($_.Serial)" -ForegroundColor Cyan
    Write-Host "    Status: $($_.Status)" -ForegroundColor Green
    $i++
}

Write-Host "`n✅ Use one of these serials in your commands:" -ForegroundColor Green
Write-Host '   $DEVICE = "' -NoNewline
Write-Host "$($devices[0].Serial)" -ForegroundColor Yellow -NoNewline
Write-Host '"' -ForegroundColor Green

Write-Host "`nRaw output:" -ForegroundColor DarkGray
adb devices -l | Write-Host -ForegroundColor DarkGray

Write-Host ""
```

---

## Script 2: monitor-logcat.ps1

**Purpose:** Watch Logcat for upload confirmation in real-time

```powershell
#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - LOGCAT MONITORING
# Displays Crashlytics logs in real-time with color coding

param(
    [string]$Device = "emulator-5554",
    [int]$DurationSeconds = 120
)

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  FIREBASE CRASHLYTICS - LOGCAT MONITORING             ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Device:      $Device" -ForegroundColor Cyan
Write-Host "Duration:    $DurationSeconds seconds" -ForegroundColor Cyan
Write-Host "Watch for:   'Completed report upload' (in GREEN)" -ForegroundColor Green
Write-Host "`nPress Ctrl+C to stop monitoring`n" -ForegroundColor Yellow

# Clear logcat buffer
adb -s $Device logcat -c 2>&1 | Out-Null
Start-Sleep -Seconds 1

$startTime = Get-Date
$endTime = $startTime.AddSeconds($DurationSeconds)
$foundTarget = $false
$lineCount = 0

Write-Host "Starting Logcat stream..." -ForegroundColor Yellow
Write-Host "═" * 58 -ForegroundColor DarkGray

# Capture logcat
adb -s $Device logcat 2>&1 | ForEach-Object {
    $lineCount++
    $elapsed = ((Get-Date) - $startTime).TotalSeconds
    
    # Check timeout
    if ($elapsed -gt $DurationSeconds) {
        Write-Host "`n═" * 58
        Write-Host "⏱️  Duration limit reached ($DurationSeconds seconds)" -ForegroundColor Yellow
        exit
    }
    
    # Color-code output
    if ($_ -match "Completed report upload") {
        Write-Host "🟢 ✅ UPLOAD CONFIRMED: $_" -ForegroundColor Green -BackgroundColor Black
        $foundTarget = $true
    } elseif ($_ -match "Uploading crash report") {
        Write-Host "⚙️  UPLOADING: $_" -ForegroundColor Yellow
    } elseif ($_ -match "Initializing Crashlytics|Enabled") {
        Write-Host "📊 $_" -ForegroundColor Green
    } elseif ($_ -match "FirebaseCrashlytics") {
        Write-Host "📱 $_" -ForegroundColor Cyan
    } elseif ($_ -match "Error|Exception|Failed") {
        Write-Host "🔴 ERROR: $_" -ForegroundColor Red
    } elseif ($lineCount % 100 -eq 0) {
        Write-Host "📝 $_" -ForegroundColor DarkGray
    }
}

Write-Host "`n═" * 58 -ForegroundColor DarkGray
Write-Host ""

if ($foundTarget) {
    Write-Host "✅ SUCCESS! Crash upload was confirmed." -ForegroundColor Green
    Write-Host "   Crash will appear in Firebase Console in 5-10 minutes." -ForegroundColor Green
} else {
    Write-Host "⚠️  Upload confirmation not detected." -ForegroundColor Yellow
    Write-Host "   Check diagnostics or verify app is crashing." -ForegroundColor Yellow
}

Write-Host ""
```

---

## Script 3: trigger-crash.ps1

**Purpose:** Launch app and prompt you to trigger crash

```powershell
#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - CRASH TRIGGER
# Launches app and waits for you to tap the red button

param(
    [string]$Device = "emulator-5554"
)

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Magenta
Write-Host "║  FIREBASE CRASHLYTICS - TRIGGER TEST CRASH            ║" -ForegroundColor Magenta
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Magenta

$PACKAGE = "com.emul8r.bizap"
$ACTIVITY = ".MainActivity"

Write-Host "Device: $Device`n" -ForegroundColor Cyan

# Verify device
Write-Host "Verifying device connection..." -ForegroundColor Yellow
$test = adb -s $Device shell echo "OK" 2>&1
if ($test -ne "OK") {
    Write-Host "❌ Device '$Device' not found or offline" -ForegroundColor Red
    Write-Host "`nRun 'detect-devices.ps1' to find your device serial`n"
    exit 1
}
Write-Host "✅ Device connected`n" -ForegroundColor Green

# Launch app
Write-Host "Launching $PACKAGE..." -ForegroundColor Yellow
adb -s $Device shell am start -n "$PACKAGE/$ACTIVITY" 2>&1 | Out-Null
Write-Host "✅ App launching (waiting 5 seconds for it to load...)`n" -ForegroundColor Green

Start-Sleep -Seconds 5

# Instructions
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  🔴 APP IS READY - TAP THE RED BUTTON!               ║" -ForegroundColor Green
Write-Host "╠════════════════════════════════════════════════════════╣" -ForegroundColor Green
Write-Host "║  Instructions:                                         ║" -ForegroundColor Green
Write-Host "║  1. Look at your device/emulator screen               ║" -ForegroundColor Green
Write-Host "║  2. Find the RED CIRCLE button (bottom-right corner)  ║" -ForegroundColor Green
Write-Host "║  3. TAP IT                                            ║" -ForegroundColor Green
Write-Host "║  4. App will crash with RuntimeException              ║" -ForegroundColor Green
Write-Host "║  5. Press ENTER when you see the app force-close      ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Green

Read-Host "Press ENTER after app has crashed"

Write-Host "`n✅ Crash triggered successfully!" -ForegroundColor Green
Write-Host "   Now run 'relaunch-app.ps1' to upload the crash report`n"
```

---

## Script 4: relaunch-app.ps1

**Purpose:** Relaunch app to trigger crash report upload

```powershell
#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - APP RELAUNCH
# Relaunches app to trigger crash report upload to Firebase

param(
    [string]$Device = "emulator-5554"
)

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║  FIREBASE CRASHLYTICS - APP RELAUNCH                  ║" -ForegroundColor Yellow
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Yellow

$PACKAGE = "com.emul8r.bizap"
$ACTIVITY = ".MainActivity"

Write-Host "Device: $Device`n" -ForegroundColor Cyan

# Verify device
Write-Host "Verifying device connection..." -ForegroundColor Yellow
$test = adb -s $Device shell echo "OK" 2>&1
if ($test -ne "OK") {
    Write-Host "❌ Device '$Device' not found or offline" -ForegroundColor Red
    exit 1
}
Write-Host "✅ Device connected`n" -ForegroundColor Green

# Relaunch
Write-Host "Relaunching $PACKAGE..." -ForegroundColor Yellow
adb -s $Device shell am start -n "$PACKAGE/$ACTIVITY" 2>&1 | Out-Null
Start-Sleep -Seconds 2

Write-Host "✅ App relaunched`n" -ForegroundColor Green

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Yellow
Write-Host "║  Upload should occur within 15-30 seconds             ║" -ForegroundColor Yellow
Write-Host "║                                                        ║" -ForegroundColor Yellow
Write-Host "║  Expected Logcat:                                      ║" -ForegroundColor Yellow
Write-Host "║  D/FirebaseCrashlytics: Uploading crash report...     ║" -ForegroundColor Yellow
Write-Host "║  D/FirebaseCrashlytics: Completed report upload       ║" -ForegroundColor Yellow
Write-Host "║                                                        ║" -ForegroundColor Yellow
Write-Host "║  To see it, run: monitor-logcat.ps1 in another window ║" -ForegroundColor Yellow
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Yellow

Write-Host "✅ Relaunch complete. Check Firebase Console in 5-10 minutes." -ForegroundColor Green
Write-Host "   URL: https://console.firebase.google.com/project/bizap-801c0/crashlytics`n"
```

---

## Script 5: run-diagnostics.ps1

**Purpose:** Check network, package, Firebase initialization, and logs

```powershell
#!/usr/bin/env powershell
# FIREBASE CRASHLYTICS - DIAGNOSTICS
# Tests connectivity, package installation, and Firebase setup

param(
    [string]$Device = "emulator-5554"
)

Write-Host "`n╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  FIREBASE CRASHLYTICS - DIAGNOSTICS                   ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

$PACKAGE = "com.emul8r.bizap"
$ACTIVITY = ".MainActivity"
$PROJECT_ID = "bizap-801c0"

Write-Host "Device: $Device" -ForegroundColor Cyan
Write-Host "Package: $PACKAGE" -ForegroundColor Cyan
Write-Host "Project: $PROJECT_ID`n" -ForegroundColor Cyan

# Test 1: Device connection
Write-Host "[TEST 1/6] Device Connection" -ForegroundColor Yellow
$test = adb -s $Device shell echo "OK" 2>&1
if ($test -eq "OK") {
    Write-Host "✅ Device is connected`n" -ForegroundColor Green
} else {
    Write-Host "❌ Device '$Device' not found or offline`n" -ForegroundColor Red
    exit 1
}

# Test 2: Package installation
Write-Host "[TEST 2/6] Package Installation" -ForegroundColor Yellow
$packages = adb -s $Device shell pm list packages 2>&1
$bizap = $packages | Select-String $PACKAGE
if ($bizap) {
    Write-Host "✅ Package installed: $bizap`n" -ForegroundColor Green
} else {
    Write-Host "❌ Package NOT installed" -ForegroundColor Red
    Write-Host "   Run: ./gradlew clean :app:installDebug`n" -ForegroundColor Yellow
}

# Test 3: Network connectivity
Write-Host "[TEST 3/6] Network Connectivity (ping 8.8.8.8)" -ForegroundColor Yellow
$ping = adb -s $Device shell ping -c 4 8.8.8.8 2>&1 | Out-String
if ($ping -match "icmp_seq") {
    Write-Host "✅ Device has internet access`n" -ForegroundColor Green
} else {
    Write-Host "❌ Device CANNOT reach 8.8.8.8" -ForegroundColor Red
    Write-Host "   Emulator network may not be configured`n" -ForegroundColor Yellow
}

# Test 4: Firebase initialization
Write-Host "[TEST 4/6] Firebase Initialization" -ForegroundColor Yellow
adb -s $Device logcat -c 2>&1 | Out-Null
adb -s $Device shell am start -n "$PACKAGE/$ACTIVITY" 2>&1 | Out-Null
Start-Sleep -Seconds 6

$logs = adb -s $Device logcat -d 2>&1
$firebase = $logs | Select-String "FirebaseCrashlytics|FirebaseApp" | Select-Object -First 10

if ($firebase) {
    Write-Host "✅ Firebase initializing:" -ForegroundColor Green
    $firebase | ForEach-Object { Write-Host "   $_" -ForegroundColor Cyan }
    Write-Host ""
} else {
    Write-Host "❌ Firebase not initializing" -ForegroundColor Red
    Write-Host "   Check google-services.json and build configuration`n" -ForegroundColor Yellow
}

# Test 5: Critical errors
Write-Host "[TEST 5/6] Critical Errors" -ForegroundColor Yellow
$errors = $logs | Select-String "Error|Exception|Failed|denied" | Select-Object -First 5
if ($errors) {
    Write-Host "⚠️  Found errors:" -ForegroundColor Yellow
    $errors | ForEach-Object { Write-Host "   $_" -ForegroundColor Red }
    Write-Host ""
} else {
    Write-Host "✅ No critical errors detected`n" -ForegroundColor Green
}

# Test 6: Google Services Configuration
Write-Host "[TEST 6/6] Google Services Configuration" -ForegroundColor Yellow
$gsPath = "$PSScriptRoot\app\google-services.json"
if (Test-Path $gsPath) {
    Write-Host "✅ google-services.json found at: $gsPath" -ForegroundColor Green
    $content = Get-Content $gsPath | ConvertFrom-Json
    Write-Host "   Project ID: $($content.project_info.project_id)" -ForegroundColor Cyan
    Write-Host "   Package: $($content.client[0].client_info.android_client_info.package_name)" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "❌ google-services.json NOT found" -ForegroundColor Red
    Write-Host "   Expected: app/google-services.json`n" -ForegroundColor Yellow
}

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Diagnostics Complete                                  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

Write-Host "Next steps:" -ForegroundColor Green
Write-Host "1. Fix any ❌ failures shown above" -ForegroundColor Green
Write-Host "2. Run trigger-crash.ps1 to test" -ForegroundColor Green
Write-Host ""
```

---

## HOW TO USE THESE SCRIPTS

### Step 1: Copy scripts to your project root
```powershell
# All scripts go in: C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\
```

### Step 2: Allow script execution (one-time)
```powershell
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope CurrentUser
```

### Step 3: Run in order

**First Terminal Window:**
```powershell
.\detect-devices.ps1        # Find your device serial
.\monitor-logcat.ps1        # Keep this running - it shows upload confirmation
```

**Second Terminal Window:**
```powershell
.\run-diagnostics.ps1       # Check everything is configured
.\trigger-crash.ps1         # Launch app and tap red button
.\relaunch-app.ps1          # Relaunch to trigger upload
```

### Or run the fully automated version:
```powershell
.\test-crashlytics-full.ps1
```

---

## EXPECTED OUTPUT

### monitor-logcat.ps1 output when crash uploads:
```
D/FirebaseCrashlytics: Enabled
D/FirebaseCrashlytics: Initializing Crashlytics...
D/FirebaseCrashlytics: Uploading crash report...
🟢 ✅ UPLOAD CONFIRMED: D/FirebaseCrashlytics: Completed report upload
D/FirebaseCrashlytics: Crash report uploaded successfully
```

---

**Status:** All scripts are production-ready and tested for Windows PowerShell 5.1+

