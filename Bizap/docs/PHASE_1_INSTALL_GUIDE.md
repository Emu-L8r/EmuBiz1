# Phase 1: Installation & Launch Testing

## Objective
Verify the app launches without crashing and identify any startup issues.

## Prerequisites
- Android emulator running (or physical device connected via USB with debugging enabled)
- ADB in PATH or Android Studio open
- APK ready at: `C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk`

## Step 1: Clear Previous Installation (if exists)
```bash
adb uninstall com.emul8r.bizap
```

Expected output: `Success` or `Unknown package: com.emul8r.bizap` (both are fine)

## Step 2: Install the APK
```bash
adb install -r "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk"
```

Expected output:
```
Installing app...
Success
```

**If installation fails:**
- Check ADB connection: `adb devices`
- Ensure emulator is running
- Try: `adb kill-server` then `adb devices`

## Step 3: Clear Logcat Buffer (important for clean output)
```bash
adb logcat -c
```

## Step 4: Launch the App
```bash
adb shell am start -n com.emul8r.bizap/.MainActivity
```

Expected output:
```
Starting: Intent { cmp=com.emul8r.bizap/.MainActivity }
```

## Step 5: Capture Logcat (CRITICAL - do this immediately after launch)
```bash
adb logcat | findstr "com.emul8r.bizap"
```

Let this run for 10-15 seconds to capture all startup logs, then press `Ctrl+C` to stop.

**IMPORTANT**: Copy the ENTIRE output and paste it below.

## Step 6: Observe the App
While logcat is running, watch the emulator/device:
- Does the app appear?
- Does it stay open or crash?
- Do you see any error dialogs?

## What to Report Back

Please provide:

### 1. Installation Status
```
✅ APK installed successfully
or
❌ Installation failed: [error message]
```

### 2. App Launch Status
```
✅ App launched and is still open
or
❌ App crashed on launch
```

### 3. Logcat Output (from Step 5)
```
[Paste full logcat output here]
```

### 4. Visual Observations
```
What do you see on the screen?
- Blank screen?
- Loading spinner?
- Full UI with data?
- Error dialog?
```

## Expected Behavior (Phase 1 Success)

✅ APK installs successfully
✅ App launches without crashing
✅ Logcat shows normal startup messages (no ERROR or FATAL)
✅ App shows home screen (likely blank, awaiting CRUD operations)
✅ No crash dialogs

## If Phase 1 Fails

If the app crashes, you'll see something like:
```
E/AndroidRuntime: FATAL EXCEPTION: main
    Process: com.emul8r.bizap, PID: 12345
    java.lang.RuntimeException: ...
    ...stack trace...
```

**If you see a crash**: Copy the ENTIRE stack trace and report it.

---

## Command Reference (Quick Copy-Paste)

```powershell
# All in order
adb uninstall com.emul8r.bizap
adb install -r "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk"
adb logcat -c
adb shell am start -n com.emul8r.bizap/.MainActivity
adb logcat | findstr "com.emul8r.bizap"
```

Copy these commands, paste into PowerShell, and report the output.

---

**Ready to proceed with Phase 1?**

