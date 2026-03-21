# ⚡ QUICK START - Testing & Installation (2-Minute Version)

**Date:** March 21, 2026  
**Status:** ✅ Ready to test  
**Time to Test:** ~2 minutes to launch, ~30 minutes for full flow testing

---

## 🚀 The 2-Minute Test

### Step 1: Build (30 seconds)
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
.\gradlew clean assembleDebug
```
✅ Look for: `BUILD SUCCESSFUL` and `libsqlcipher.so` in output

### Step 2: Install (30 seconds)
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb uninstall com.emul8r.bizap
& $adb install "app/build/outputs/apk/debug/app-debug.apk"
```
✅ Look for: `Success`

### Step 3: Launch (30 seconds)
```powershell
& $adb shell am start -n com.emul8r.bizap/.MainActivity
Start-Sleep -Seconds 3
& $adb logcat -d | Select-String "FATAL"
```
✅ Look for: **No output** (no FATAL means no crashes!)

---

## 🎯 What You'll See

**Scenario A: Fresh Install (Most Likely)**
1. Splash screen appears (2-3 seconds)
2. Login screen appears
3. Enter PIN: 1234 (or create new one)
4. Enter business name and currency
5. Dashboard loads with empty sections

**Scenario B: Already Have Account**
1. Splash screen (2-3 seconds)
2. Dashboard loads immediately

**Scenario C: Something's Wrong** ❌
1. Screen appears but freezes
2. App crashes and returns to home screen
3. Black screen with no UI

---

## 📋 Success Checklist

- [ ] App builds successfully
- [ ] APK is ~36 MB
- [ ] Installation succeeds
- [ ] App launches without crashing
- [ ] Splash screen or login appears
- [ ] Can interact with UI (tap buttons)
- [ ] No error messages in logcat

**All checked?** → App is working! ✅

---

## 🆘 Troubleshooting (30 seconds)

**Q: App won't install**
```
A: Run: & $adb uninstall com.emul8r.bizap
   Then try install again
```

**Q: "App keeps crashing"**
```
A: Check logs:
   & $adb logcat -d > crash.txt
   Look for: "Exception" or "Error"
   Share crash.txt with me
```

**Q: "Stuck on splash screen"**
```
A: Wait 10 seconds (might be initializing)
   If still stuck: Check logcat for "Loading" messages
   If frozen >15s: It's a bug, share logs
```

**Q: "Can't find adb"**
```
A: Use full path:
   C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe
```

---

## 📱 Device Requirements

- **OS:** Android 8.0+ (minSdk 26)
- **RAM:** 2GB minimum (4GB+ recommended)
- **Storage:** 100MB free space
- **Architecture:** ARM64 (standard for modern phones)

---

## 💡 Pro Tip: Reusable Commands

Save this as `test.ps1`:
```powershell
# Rebuild, reinstall, and retest with one command
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Build
Write-Host "Building..."
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
.\gradlew clean assembleDebug

# Install
Write-Host "Installing..."
& $adb uninstall com.emul8r.bizap
Start-Sleep -Seconds 1
& $adb install "app/build/outputs/apk/debug/app-debug.apk"
Start-Sleep -Seconds 1

# Launch
Write-Host "Launching..."
& $adb logcat -c
& $adb shell am start -n com.emul8r.bizap/.MainActivity

# Monitor
Start-Sleep -Seconds 5
$output = & $adb logcat -d | Select-String "FATAL|AndroidRuntime"
if ($output) {
    Write-Host "❌ CRASH DETECTED!" -ForegroundColor Red
    $output
} else {
    Write-Host "✅ NO CRASHES DETECTED!" -ForegroundColor Green
}
```

Then run: `.\test.ps1`

---

## 🎯 What to Test (If App Launches)

1. **Navigation**
   - Tap "Customers" → should load
   - Tap "Invoices" → should load
   - Tap "Dashboard" → should load

2. **Create Customer**
   - Tap "+" button
   - Enter name: "Test Customer"
   - Enter email: "test@example.com"
   - Tap "Save"
   - Should appear in list

3. **Create Invoice**
   - Go to "Invoices"
   - Tap "+" button
   - Select customer
   - Add line item
   - Tap "Save"
   - Should appear in list

4. **Check Dashboard**
   - Go to "Dashboard"
   - Should see revenue summary
   - Should see charts

**If all above work:** App is functioning! ✅

---

## 📞 Report Back With

If things go wrong, tell me:
1. What happened? (crashed, froze, etc.)
2. When? (at launch, after X seconds, when I tapped Y)
3. Error message? (share from logcat)
4. Steps to reproduce?

---

## ✅ Summary

**You need to:**
1. Build the app
2. Install on device
3. Launch and watch for crashes
4. Report success or error

**Expected time:** ~2 minutes to test launch  
**Expected result:** App starts and dashboard appears  
**Current confidence:** 85% it works

---

**Ready? Start with:**
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
.\gradlew clean assembleDebug
```

**Let me know if it works!** 🚀

