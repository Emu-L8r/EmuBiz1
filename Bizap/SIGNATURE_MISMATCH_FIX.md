# 🔧 SIGNATURE MISMATCH FIX - INSTALLATION TROUBLESHOOTING

**Problem:** `INSTALL_FAILED_UPDATE_INCOMPATIBLE: Existing package signatures do not match`

**Cause:** Your device has an old debug-signed APK. The new release APK has a different signature (release key vs debug key).

---

## ✅ SOLUTION: 3 STEPS

### **Step 1: Uninstall Old Version**

```bash
adb uninstall com.emul8r.bizap
```

**Expected output:**
```
Success
```

---

### **Step 2: Install New Release APK**

```bash
adb install "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\release\app-release.apk"
```

**Expected output:**
```
Performing Streamed Install
Success
```

---

### **Step 3: Verify Installation & Launch**

```bash
# Check it's installed
adb shell pm list packages | findstr bizap

# Launch the app
adb shell am start -n com.emul8r.bizap/.MainActivity

# Wait a few seconds, then check logs
timeout 5 adb logcat | findstr /i "bizap"
```

**Expected output:**
```
package:com.emul8r.bizap
Starting: Intent { ... }
[Timber logs appear here]
```

---

## 🎯 FULL STEP-BY-STEP COMMAND

Copy and paste this entire block into PowerShell:

```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

Write-Output "1️⃣ Uninstalling old version..."
adb uninstall com.emul8r.bizap

Write-Output "2️⃣ Installing production APK..."
adb install "app\build\outputs\apk\release\app-release.apk"

Write-Output "3️⃣ Verifying installation..."
adb shell pm list packages | findstr bizap

Write-Output "4️⃣ Launching app..."
adb shell am start -n com.emul8r.bizap/.MainActivity

Write-Output "5️⃣ Waiting for app to initialize..."
Start-Sleep -Seconds 3

Write-Output "6️⃣ Capturing logs..."
adb logcat -d | Select-String -Pattern "bizap|database|migration|snapshot|Exception" -CaseSensitive:$false | Select-Object -Last 20
```

---

## 🔍 WHAT TO LOOK FOR IN LOGS

### **Good Signs ✅**

```
✅ "Starting: Intent { ... }"  (app launched)
✅ "Database migration successful"
✅ "Created analytics snapshots"
✅ "Invoice saved successfully"
```

### **Bad Signs ❌**

```
❌ "Exception" or "Error"
❌ "Database version mismatch"
❌ "Failed to create snapshots"
❌ App crashes immediately
```

---

## 🚨 IF PROBLEM PERSISTS

### **Option A: Clear App Data & Try Again**

```bash
adb uninstall com.emul8r.bizap
adb install "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\release\app-release.apk"
adb shell am start -n com.emul8r.bizap/.MainActivity
```

### **Option B: Check Device Compatibility**

```bash
# Get device info
adb shell getprop ro.build.version.release  # Android version
adb shell getprop ro.product.model           # Device model

# App requires: Android 8.0+ (API 26+)
# If your device is older, app won't work
```

### **Option C: Clear ADB Cache**

```bash
adb kill-server
adb start-server
adb devices
# Then try install again
```

---

## ✅ TESTING AFTER SUCCESSFUL INSTALL

Once app launches, test:

1. **App opens without crash?** ✅
2. **Splash screen appears?** ✅
3. **Dashboard loads?** ✅
4. **Can create invoice?** ✅
5. **Dashboard shows numbers (not $0.00)?** ✅
6. **Close app & reopen → data persists?** ✅

If all 6 pass → **READY FOR PLAY STORE** 🚀

---

## 📋 QUICK REFERENCE

| Issue | Fix |
|-------|-----|
| `INSTALL_FAILED_UPDATE_INCOMPATIBLE` | Uninstall old version first |
| App won't start | Check logs for exceptions |
| Dashboard shows $0.00 | Check `createAnalyticsSnapshots` logs |
| Data disappears on reopen | Database issue - check migration logs |
| Crashes immediately | Share full crash log |

---

**Created:** March 17, 2026  
**Purpose:** Fix signature mismatch during production testing


