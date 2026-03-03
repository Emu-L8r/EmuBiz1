# Quick Reference: Phase 1 Installation Commands

## Copy-Paste Ready (for PowerShell)

### Option A: Step-by-Step (Recommended for First Time)
```powershell
# Step 1: Uninstall old version
adb uninstall com.emul8r.bizap

# Step 2: Install the APK
adb install -r "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk"

# Wait for installation to complete, then:

# Step 3: Clear logcat buffer
adb logcat -c

# Step 4: Launch the app
adb shell am start -n com.emul8r.bizap/.MainActivity

# Step 5: Monitor logs (run in new terminal, let it run 10-15 seconds)
adb logcat | findstr "com.emul8r.bizap"
```

### Option B: One-Line (All at Once)
```powershell
adb uninstall com.emul8r.bizap; adb install -r "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk"; adb logcat -c; adb shell am start -n com.emul8r.bizap/.MainActivity; adb logcat | findstr "com.emul8r.bizap"
```

---

## Expected Outputs

### Successful Installation
```
Installing app...
Success
```

### Successful Launch
```
Starting: Intent { cmp=com.emul8r.bizap/.MainActivity }
```

### Clean Startup Logcat
```
D/com.emul8r.bizap: Application started
D/com.emul8r.bizap: MainActivity created
D/com.emul8r.bizap: Loading data...
[UI appears on screen without crashes]
```

---

## What to Report Back

### Example Success Report
```
✅ Installation: SUCCESS
✅ App Launch: SUCCESS
✅ Visible UI: YES (shows empty customer list)

Logcat (first 20 lines):
[paste logcat output here]

Observations:
- App opened to main screen
- No error dialogs
- Lists are empty (expected - no data yet)
- App is responsive
```

### Example Failure Report
```
❌ Installation: FAILED
Error: [paste error message]

or

✅ Installation: SUCCESS
❌ App Launch: CRASHED

Crash Stack Trace:
[paste full logcat crash here]

Last known good log:
[paste lines before crash]
```

---

## Troubleshooting Quick Links

| Issue | Solution |
|-------|----------|
| `adb: command not found` | Add Android SDK `platform-tools` to PATH |
| `device not found` | Run `adb devices` - ensure emulator/device connected |
| `Installation failed` | Try `adb kill-server` then `adb devices` |
| `Permission denied` | Ensure app has write permissions (grant at runtime on Android 6+) |
| App shows blank screen | Check logcat for errors - likely data loading issue |
| App crashes immediately | Check logcat for crash stack trace |

---

## APK Location (Verify Before Installing)
```
C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk
```

If file doesn't exist:
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
./gradlew assembleDebug
```

---

## Quick Health Check (Before Phase 1)

```powershell
# Verify ADB connectivity
adb devices

# Expected output:
# List of attached devices
# emulator-5554    device
# or your device name
```

---

## After Phase 1 Completes

**If Phase 1 PASSES** (app launches cleanly):
- Report success
- Move to Phase 2 (Customer CRUD testing)

**If Phase 1 FAILS** (app crashes):
- Report crash details + logcat
- We'll diagnose and fix
- Then retry Phase 1

---

## Key Files to Reference

- **Installation**: `PHASE_1_INSTALL_GUIDE.md`
- **Troubleshooting**: `PHASE_1_TROUBLESHOOTING.md`
- **Architecture**: `ARCHITECTURE.md`
- **Full Testing**: `TESTING_CHECKLIST.md`
- **Project Status**: `PROJECT_STATUS.md`

---

## Ready?

1. Copy a command from above
2. Paste into PowerShell terminal
3. Watch emulator/device screen
4. Capture logcat output
5. Report results

**Estimated time: 5 minutes**

---

Last Updated: 2026-02-27
APK Build Time: 36 seconds
Status: ✅ Ready for testing

