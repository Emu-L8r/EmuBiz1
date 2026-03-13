# 🪟 PHASE 1 EXECUTION - WINDOWS POWERSHELL GUIDE

## The Issue You're Seeing

You're copying **bash/Linux commands** into **Windows PowerShell**. They won't work directly. Here's how to do it properly on Windows.

---

## STEP 1: Build Release APK (Windows-Safe Command)

**In PowerShell (WITHOUT the `$` symbol):**

```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
./gradlew clean assembleRelease
```

**What to expect:**
- Duration: 3-5 minutes
- Output: "BUILD SUCCESSFUL" at the end
- APK location: `app\build\outputs\apk\release\app-release-unsigned.apk`

**If you see errors:**
- Look for "ProGuard" or "R8 transform" errors
- Note them down for debugging

---

## STEP 2: Verify APK Was Created

```powershell
ls app\build\outputs\apk\release\*.apk
```

Should show something like:
```
Mode    LastWriteTime         Length Name
----    ---------------         ------ ----
-a---   3/13/2026 2:30:00 PM  25000000 app-release-unsigned.apk
```

**If the file doesn't exist:**
- Build failed. Check for ProGuard errors in the build output above.

---

## STEP 3: Check if Android SDK is Installed

Before you can use `adb`, you need to find your Android SDK.

```powershell
# Option 1: Check if ANDROID_HOME is set
$env:ANDROID_HOME
```

If it returns nothing, do this:

```powershell
# Option 2: Check common Android Studio locations
$androidStudioPath = "$env:APPDATA\..\Local\Android\Sdk"
if (Test-Path $androidStudioPath) {
    Write-Host "Found Android SDK at: $androidStudioPath"
    $env:ANDROID_HOME = $androidStudioPath
}
```

---

## STEP 4: Set Up adb Properly (One-Time Setup)

If you don't have adb in your PATH, add it:

```powershell
# Check if adb exists
$adbPath = "$env:ANDROID_HOME\platform-tools\adb.exe"
if (Test-Path $adbPath) {
    Write-Host "✅ adb found at: $adbPath"
} else {
    Write-Host "❌ adb not found. Android SDK may not be installed."
}
```

If adb is found, add it to PATH permanently:

```powershell
# Add to PATH for this session only (temporary)
$env:Path += ";$env:ANDROID_HOME\platform-tools"

# Verify
adb version
```

---

## STEP 5: Connect Your Android Device

**Prerequisites:**
- Android device connected via USB cable
- USB debugging enabled on device (Settings > Developer Options > USB Debugging)

**Check connection:**

```powershell
adb devices
```

Should output something like:
```
List of attached devices
192.168.1.100:5555     device
```

**If no devices listed:**
- Check USB cable is connected
- Check USB debugging is enabled
- Try "adb kill-server" then "adb devices" again

---

## STEP 6: Uninstall Old Version (If Needed)

```powershell
adb uninstall com.emul8r.bizap
```

Expected output:
```
Success
```

---

## STEP 7: Sign the APK

**Generate a keystore (one-time):**

```powershell
keytool -genkey -v -keystore bizap-release.keystore `
  -keyalg RSA -keysize 2048 -validity 10000 `
  -alias bizap_release `
  -dname "CN=Bizap,O=EmuBiz,C=US"
```

When prompted for password, enter: `bizap2026`

**Sign the APK:**

```powershell
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA256 `
  -keystore bizap-release.keystore `
  app\build\outputs\apk\release\app-release-unsigned.apk `
  bizap_release
```

When prompted, enter password: `bizap2026`

---

## STEP 8: Install APK on Device

```powershell
adb install app\build\outputs\apk\release\app-release-unsigned.apk
```

Expected output:
```
Success
```

---

## STEP 9: Test App on Device

**Open the app:**
- Look for "Bizap" icon on your device
- Tap it to launch
- Watch for crashes or errors

**Test checklist:**
- [ ] App launches without crashing
- [ ] Splash screen appears
- [ ] PIN entry screen loads
- [ ] Can create invoice
- [ ] Dashboard displays
- [ ] Images load properly
- [ ] No error messages

---

## STEP 10: Capture Logs if It Crashes

```powershell
# Start logging
adb logcat > release_test.log

# [Let the app run for 30-60 seconds]
# Then press Ctrl+C to stop logging

# View the log
cat release_test.log
```

**Look for errors like:**
```
ClassNotFoundException
NoSuchMethodError
ProGuard
Hilt
Room
```

---

## Quick Reference: PowerShell Syntax

**Important**: In PowerShell, these differences matter:

| Task | Bash | PowerShell |
|------|------|-----------|
| **Navigate** | `cd path` | `cd path` |
| **List files** | `ls` | `ls` or `dir` |
| **Concatenate paths** | `path/to/file` | `path\to\file` |
| **Run command** | `./gradlew clean` | `./gradlew clean` |
| **Pipe output** | `command \| grep text` | `command \| Select-String text` |
| **Backticks** | `\` | `` ` `` (backtick) |
| **Variables** | `$var` | `$var` |

---

## Complete Windows-Safe Phase 1 Script

Create a file named `phase1.ps1`:

```powershell
# Phase 1: Release Build Testing (Windows)

Write-Host "=== PHASE 1: RELEASE BUILD TESTING ===" -ForegroundColor Green
Write-Host ""

# Step 1: Build
Write-Host "Step 1: Building release APK..." -ForegroundColor Yellow
./gradlew clean assembleRelease
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    exit 1
}

# Step 2: Verify APK
Write-Host ""
Write-Host "Step 2: Verifying APK..." -ForegroundColor Yellow
$apk = "app\build\outputs\apk\release\app-release-unsigned.apk"
if (Test-Path $apk) {
    $size = (Get-Item $apk).Length / 1MB
    Write-Host "✅ APK created: $($size)MB" -ForegroundColor Green
} else {
    Write-Host "❌ APK not found!" -ForegroundColor Red
    exit 1
}

# Step 3: Check adb
Write-Host ""
Write-Host "Step 3: Checking adb..." -ForegroundColor Yellow
try {
    adb version | Out-Null
    Write-Host "✅ adb is available" -ForegroundColor Green
} catch {
    Write-Host "❌ adb not found. Set ANDROID_HOME environment variable." -ForegroundColor Red
    exit 1
}

# Step 4: List devices
Write-Host ""
Write-Host "Step 4: Checking connected devices..." -ForegroundColor Yellow
adb devices

Write-Host ""
Write-Host "Ready to proceed with installation and testing!" -ForegroundColor Green
Write-Host "Next: Run Phase 1 tests manually on your device" -ForegroundColor Yellow
```

**Run it:**
```powershell
.\phase1.ps1
```

---

## If You're Still Stuck

**Problem: adb not found**

Solution:
1. Install Android Studio (includes SDK)
2. Or download Android SDK Platform Tools
3. Set `ANDROID_HOME` environment variable:
   ```powershell
   # Permanently:
   [Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\path\to\android\sdk", "User")
   # Then restart PowerShell
   ```

**Problem: Device not detected**

Solution:
1. Enable "Developer Options" on phone (tap Build Number 7 times)
2. Enable "USB Debugging" in Developer Options
3. Disconnect and reconnect USB cable
4. Tap "Always allow from this computer" on phone

**Problem: Build failed with ProGuard error**

Solution:
1. Check the error message carefully
2. Add the missing rule to `app/proguard-rules.pro`
3. Rebuild: `./gradlew clean assembleRelease`

---

## Next: Report Back With

When you're ready, run the Phase 1 script and tell me:

1. **Did the build succeed?** (Yes/No)
2. **APK file size?** (e.g., 25MB)
3. **Can you see your device?** (adb devices output)
4. **Did app launch without crashing?** (Yes/No)
5. **Any errors in logcat?** (Copy/paste the errors)

Then I'll help you fix any issues! 🚀

---

**Date**: March 13, 2026  
**Status**: Ready to execute Phase 1 on Windows

