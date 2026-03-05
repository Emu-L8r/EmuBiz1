# 🚀 OPTION B - AUTOMATED INSTALLATION SCRIPT

**Status:** ✅ Installation script created and ready  
**Script:** `install_app.sh`  
**Usage:** `bash install_app.sh`

---

## 🎯 WHAT THE SCRIPT DOES

The `install_app.sh` script automates the entire Option B installation process:

```
1. ✅ Checks device connection (adb devices)
2. ✅ Uninstalls old APK (if present)
3. ✅ Verifies fresh APK file exists
4. ✅ Installs fresh APK with -r flag (replace)
5. ✅ Launches the app (am start)
6. ✅ Monitors logcat for 20 seconds
7. ✅ Checks if app is running
8. ✅ Reports SUCCESS or displays crash logs
```

---

## 📝 HOW TO RUN

### In Git Bash / WSL / Terminal:

```bash
cd ~/Documents/GitHub/EmuBiz/Bizap
bash install_app.sh
```

### That's it! The script handles everything.

---

## ✅ EXPECTED OUTPUT

When successful, you'll see:

```
🚀 BIZAP APK INSTALLATION & TESTING - OPTION B
==============================================

1️⃣  Checking device connection...
✅ Device connected

2️⃣  Uninstalling old APK...
✅ Old APK removed (or was not installed)

3️⃣  Verifying APK file...
✅ APK found (Size: 24.0M)

4️⃣  Installing fresh APK...
✅ APK installed successfully

5️⃣  Launching app...
✅ App launch command sent

6️⃣  Monitoring logs for 20 seconds...
Look for:
  ✅ No 'FATAL EXCEPTION' or 'ClassNotFoundException'
  ✅ 'BizapApplication: 🚀' message
  ✅ 'MainActivity: onCreate()' message

[Logcat output with app startup logs...]

7️⃣  Final verification...
✅ App is running!

🎉 SUCCESS! The app launched without crashing!

==============================================
Installation complete!
```

---

## ❌ IF IT FAILS

If you see crash logs, the script will display them automatically. Look for:

```
FATAL EXCEPTION: main
ClassNotFoundException: Hilt_BizapApplication
```

If you see this, then something went wrong with the build. Run:

```bash
./gradlew --stop
rm -rf .gradle app/build
./gradlew clean assembleDebug
bash install_app.sh
```

---

## 🔍 WHAT THE SCRIPT CHECKS FOR

| Check | What It Does |
|-------|-------------|
| **Device Connected** | Verifies adb can reach device |
| **APK File Exists** | Ensures app/build/.../app-debug.apk exists |
| **Installation** | Installs APK with `adb install -r` |
| **App Launch** | Starts MainActivity |
| **Logcat Monitor** | Watches for crash messages |
| **Process Check** | Verifies app is actually running |

---

## 💡 MANUAL COMMANDS (If you prefer)

If you want to run the commands manually instead of using the script:

```bash
# 1. Uninstall old version
adb uninstall com.emul8r.bizap

# 2. Install fresh APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity

# 4. Monitor logs
adb logcat -s AndroidRuntime:E BizapApplication:D MainActivity:D
```

---

## 🎯 YOUR NEXT STEPS

### Option 1: Automatic (Recommended)
```bash
bash install_app.sh
```

### Option 2: Manual
```bash
adb uninstall com.emul8r.bizap
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

## ✨ AFTER INSTALLATION

Once the app is running:

1. **Verify it works:**
   - App should appear on device
   - Main screen should display
   - No crash should occur

2. **Test features:**
   - Navigate between screens
   - Try creating an invoice
   - Check if saving works

3. **Review logs:**
   - Should show "BizapApplication: 🚀"
   - Should show "Firebase Analytics initialized"
   - No RED error messages

---

**Ready? Run the script now:**

```bash
bash install_app.sh
```

**Let me know the output and if the app launches successfully!** 🎉

