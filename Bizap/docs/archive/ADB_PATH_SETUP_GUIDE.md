# 🔧 ADD ADB TO PATH - WINDOWS SETUP GUIDE

## Option 1: Automatic Setup (PowerShell - Recommended)

Run this PowerShell command as Administrator:

```powershell
# Add Android SDK platform-tools to PATH (Current Session)
$androidSdkPath = "$env:APPDATA\..\Local\Android\Sdk\platform-tools"

if (Test-Path $androidSdkPath) {
    $env:Path += ";$androidSdkPath"
    Write-Host "✅ ADB added to PATH for this session"
    Write-Host "Location: $androidSdkPath"
    adb version
} else {
    Write-Host "❌ Android SDK not found at default location"
    Write-Host "Try alternative paths below"
}
```

**This works for current PowerShell session only. Restart PowerShell and it will be gone.**

---

## Option 2: Permanent Setup (Recommended for Development)

### Step 1: Find Your Android SDK Location

Your SDK is likely at one of these locations:
```
C:\Users\Saucey\AppData\Local\Android\Sdk          ← Most common
C:\Android\Sdk
C:\Program Files\Android\Sdk
```

### Step 2: Verify ADB Exists

Open PowerShell and run:
```powershell
Test-Path "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
```

Should return: `True`

### Step 3: Add to System PATH (Permanent)

**Method A: Using PowerShell (as Administrator)**

```powershell
# Run as Administrator
$androidSdkPath = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools"

if (Test-Path $androidSdkPath) {
    # Add to User PATH
    $currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
    $newPath = "$currentPath;$androidSdkPath"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    Write-Host "✅ ADB added to permanent PATH"
    Write-Host "Please restart PowerShell for changes to take effect"
} else {
    Write-Host "❌ Path not found: $androidSdkPath"
}
```

**Method B: Manual GUI Steps (Windows)**

1. **Press**: `Win + X` → Search "Environment Variables"
2. **Click**: "Edit the system environment variables"
3. **Click**: "Environment Variables" button (bottom right)
4. **Under "User variables for Saucey"**, click "New"
5. **Variable name**: `PATH`
6. **Variable value**: `C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools`
7. **Click**: OK three times
8. **Restart**: PowerShell completely (close and reopen)

### Step 4: Verify It Works

Close PowerShell completely and open a new window:

```powershell
adb version
```

Should output:
```
Android Debug Bridge version 1.0.xx
```

If you see this, ✅ **ADB is properly added to PATH!**

---

## Option 3: If Android SDK Is in Different Location

If your SDK is NOT at the default location, find it first:

```powershell
# Search for adb.exe
Get-ChildItem -Path "C:\Users\Saucey" -Recurse -Filter "adb.exe" -ErrorAction SilentlyContinue | 
  Select-Object -First 1 FullName
```

This will show you where adb.exe actually is. Copy that path (without the `\adb.exe` part) and use it in the steps above.

---

## Step 5: Verify Device Connection

```powershell
adb devices
```

Should show your connected device:
```
List of attached devices
YOUR_DEVICE_ID    device
```

---

## Troubleshooting

### "adb: command not found"
- ❌ ADB is not in PATH
- ✅ Follow Step 2 & 3 above
- ✅ Make sure to restart PowerShell after adding to PATH

### "Path not found"
- ❌ Android SDK not at default location
- ✅ Use the search command above to find adb.exe
- ✅ Use that path instead

### Device not showing up
- ❌ Device not connected or USB debugging disabled
- ✅ Connect USB cable
- ✅ Enable USB Debugging on phone (Settings > Developer Options > USB Debugging)
- ✅ Tap "Allow" on phone when prompted

---

## After ADB is Working

You can now run all Phase 1 commands:

```powershell
# Check devices
adb devices

# Install APK
adb install app\build\outputs\apk\release\app-release-unsigned.apk

# Uninstall app
adb uninstall com.emul8r.bizap

# View logs
adb logcat
```

---

## Quick Start (TL;DR)

**For permanent setup:**

1. Open PowerShell as Administrator
2. Run this command:
```powershell
$androidSdkPath = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools"
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$currentPath;$androidSdkPath", "User")
```
3. Close all PowerShell windows
4. Open new PowerShell window
5. Run: `adb version`
6. ✅ Done!

---

**Status**: Ready to set up  
**Time**: 2-5 minutes  
**Result**: `adb` command available everywhere in PowerShell

