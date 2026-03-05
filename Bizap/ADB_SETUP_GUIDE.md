# 🔧 ADB INSTALLATION & SETUP GUIDE

**Issue:** ADB (Android Debug Bridge) is not installed or not in PATH

---

## ✅ SOLUTION 1: Quick Setup (Recommended)

### Step 1: Install Android SDK Platform-Tools

**Option A: Via Android Studio (Easiest)**
1. Open Android Studio
2. Go to **Tools → SDK Manager**
3. Select **SDK Tools** tab
4. Check "Android SDK Platform-Tools"
5. Click **Apply** and **OK**
6. Wait for installation to complete
7. Restart PowerShell

**Option B: Manual Download**
1. Go to: https://developer.android.com/tools/releases/platform-tools
2. Download **platform-tools for Windows**
3. Extract to: `C:\Android\Sdk\platform-tools\`
4. Add to PATH (see Step 2 below)

### Step 2: Add ADB to PATH

**Method 1: Set ANDROID_HOME (Permanent)**
```powershell
# Run as Administrator
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("PATH", "$env:PATH;C:\Android\Sdk\platform-tools", "User")
```

**Method 2: Use the Helper Script**
We've created `adb-helper.ps1` that finds ADB automatically

---

## ✅ SOLUTION 2: Using Our Helper Script

You now have `adb-helper.ps1` in the project directory!

### Commands:

**List devices:**
```powershell
.\adb-helper.ps1 devices
```

**Install APK:**
```powershell
.\adb-helper.ps1 install
```

**Launch app:**
```powershell
.\adb-helper.ps1 launch
```

---

## ✅ SOLUTION 3: Alternative - Run in Android Studio

If you don't have ADB set up:

1. Open Android Studio
2. Open this project folder: `C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap`
3. Click **Run** → **Run 'app'** (or press Shift+F10)
4. Select your device or emulator
5. Android Studio will handle APK building and installation automatically

---

## 📝 WHAT TO DO NOW

### Option 1: Use Helper Script (Fastest)
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
.\adb-helper.ps1 help
```

### Option 2: Install ADB Properly
1. Open Android Studio
2. Tools → SDK Manager
3. Install Android SDK Platform-Tools
4. Restart PowerShell

### Option 3: Open in Android Studio
1. File → Open
2. Navigate to: `C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap`
3. Click Run button

---

## 🔍 VERIFY ADB IS INSTALLED

```powershell
# Try to find ADB
Get-Command adb

# Or directly:
& "C:\Android\Sdk\platform-tools\adb.exe" version
```

---

## 📱 AFTER INSTALLATION

Once ADB is available, use these commands:

```powershell
# List connected devices
adb devices

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity

# View logs
adb logcat -s BizapApp:D
```

---

## ✨ RECOMMENDED: Open in Android Studio

Since you have Android Studio (it built the APK), the easiest way is:

1. **File → Open** in Android Studio
2. Select: `C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap`
3. Android Studio will:
   - Sync the project
   - Build the APK
   - Install it automatically
   - Launch it on your device/emulator
   - Show you the app running live!

---

## 📞 NEXT STEPS

Choose one:
1. **Fastest:** Use Android Studio's Run button
2. **If ADB is installed:** Use `.\adb-helper.ps1 install` then `.\adb-helper.ps1 launch`
3. **Most control:** Manual ADB commands after installation

**Which would you prefer?**


