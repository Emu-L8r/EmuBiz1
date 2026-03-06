# 🔧 ADB SETUP GUIDE (For Command Line Use)

**Note:** You don't need this RIGHT NOW. Use Android Studio method above first.  
**Use this IF:** You want to use command line for ADB later.

---

## 🎯 OPTION 1: Add ADB to PATH (Easiest)

### Step 1: Find Your Android SDK
```powershell
# Android SDK is usually in one of these locations:
$possiblePaths = @(
    "$env:LOCALAPPDATA\Android\sdk\platform-tools",
    "C:\Android\sdk\platform-tools",
    "C:\Program Files\Android\sdk\platform-tools",
    "$env:USERPROFILE\AppData\Local\Android\sdk\platform-tools"
)

# Check which one exists:
foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        Write-Host "Found at: $path"
        ls $path | grep adb
    }
}
```

### Step 2: Add to PATH
**Option A: Temporary (for this session only)**
```powershell
# Set ADB path temporarily
$env:Path += ";C:\Users\Saucey\AppData\Local\Android\sdk\platform-tools"

# Verify it works
adb devices
```

**Option B: Permanent (recommended)**
```powershell
# Edit system environment variables
# Method 1: GUI
1. Press Win + X → System
2. Advanced System Settings
3. Environment Variables
4. Add to Path:
   C:\Users\[YourUsername]\AppData\Local\Android\sdk\platform-tools
5. Click OK → Restart PowerShell

# Method 2: PowerShell (as Administrator)
$adbPath = "C:\Users\Saucey\AppData\Local\Android\sdk\platform-tools"
$currentPath = [Environment]::GetEnvironmentVariable("Path", "User")
$newPath = "$currentPath;$adbPath"
[Environment]::SetEnvironmentVariable("Path", $newPath, "User")

# Restart PowerShell and verify
adb devices
```

---

## 🎯 OPTION 2: Use ADB Directly (Full Path)

If you don't want to add to PATH, use the full path:

```powershell
# List devices
C:\Users\Saucey\AppData\Local\Android\sdk\platform-tools\adb devices

# Install APK
C:\Users\Saucey\AppData\Local\Android\sdk\platform-tools\adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
C:\Users\Saucey\AppData\Local\Android\sdk\platform-tools\adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

## 🎯 OPTION 3: Create a Helper Script

Save as `adb-helper.bat`:

```batch
@echo off
REM Find and run ADB with full path
set ADB_PATH=C:\Users\Saucey\AppData\Local\Android\sdk\platform-tools\adb.exe

if not exist "%ADB_PATH%" (
    echo ADB not found at: %ADB_PATH%
    echo Please check your Android SDK installation
    exit /b 1
)

"%ADB_PATH%" %*
```

Then use it:
```powershell
.\adb-helper devices
.\adb-helper install -r app.apk
```

---

## ✅ VERIFY ADB WORKS

```powershell
adb devices
# Should show:
# List of attached devices
# emulator-5554   device
# (or your device ID)
```

If you see devices listed, you're ready to use:

```powershell
# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity

# View logs
adb logcat | findstr BizapApplication
```

---

## 🐛 TROUBLESHOOTING

### "adb: command not found"
- Check if Android SDK is installed
- Verify path is correct
- Restart PowerShell after adding to PATH

### "List of devices attached" is empty
- Check USB connection
- Enable USB Debugging on device
- Restart adb: `adb kill-server` then `adb devices`

### Permission denied on device
- Tap "Allow" on device when prompted
- Or restart adb daemon: `adb kill-server`

---

## 📝 MOST COMMON ADB COMMANDS

```powershell
# Show connected devices
adb devices

# Install APK
adb install -r file.apk

# Uninstall app
adb uninstall com.example.app

# Launch activity
adb shell am start -n com.example.app/.MainActivity

# View logs
adb logcat

# View app-specific logs
adb logcat | findstr APP_NAME

# Clear app data
adb shell pm clear com.example.app

# List installed packages
adb shell pm list packages | findstr keyword

# File operations
adb push local_file /sdcard/remote_file
adb pull /sdcard/remote_file local_file

# Reboot device
adb reboot

# Stop daemon
adb kill-server

# Start daemon
adb start-server
```

---

## 🚀 FOR FUTURE USE

Once ADB is set up properly, you can use command line:

```powershell
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.emul8r.bizap/.MainActivity
adb logcat | findstr ERROR
```

---

## ⚠️ FOR NOW

**Don't worry about this right now!**

Use Android Studio to run the app instead:
→ See `RUN_APP_ANDROID_STUDIO.md`

This ADB setup is for LATER if you want command-line control.

