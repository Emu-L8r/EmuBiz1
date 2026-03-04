# Firebase Crashlytics Verification Guide

**Goal:** Confirm Firebase Crashlytics is properly connected and working  
**Prerequisites:** google-services.json in `Bizap/app/` folder

---

## Step 1: Build the App

### Command
```bash
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

# Clean build to ensure everything is fresh
.\gradlew.bat clean :app:assembleDebug --no-daemon
```

### Expected Output
```
> Task :app:processDebugGoogleServices
> Task :app:compileDebugKotlin
> Task :app:assembleDebug
BUILD SUCCESSFUL in XX seconds
```

**What This Checks:**
- ✅ google-services.json is readable
- ✅ Google Services plugin processed it
- ✅ Firebase libraries are linked
- ✅ No compilation errors

### If Build Fails

**Error: "google-services.json not found"**
```
Solution:
1. Download from Firebase Console:
   - Firebase Console → Your Project → Project Settings (gear icon)
   - Download google-services.json
2. Place at: Bizap/app/google-services.json
3. Rebuild: ./gradlew clean :app:assembleDebug
```

**Error: "Could not find firebase-bom"**
```
Solution:
1. Check gradle/libs.versions.toml has:
   firebase-bom = "34.9.0"
2. Check app/build.gradle.kts has:
   implementation(platform(libs.firebase.bom))
3. Run: ./gradlew --refresh-dependencies
4. Rebuild
```

---

## Step 2: Install on Device/Emulator

### Check Device Connection
```bash
$env:ANDROID_HOME = "C:\Users\Saucey\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\platform-tools\adb.exe" devices
```

### Expected Output
```
List of attached devices
emulator-5554          device
```

**What This Checks:**
- ✅ Emulator is running
- ✅ ADB can see the device
- ✅ Device is in "device" state (not "unauthorized")

### If Device Not Found

**Solution 1: Start Emulator**
```bash
# List available emulators
& "$env:ANDROID_HOME\emulator\emulator.exe" -list-avds

# Start emulator (example: Pixel_4_API_30)
& "$env:ANDROID_HOME\emulator\emulator.exe" -avd Pixel_4_API_30
```

**Solution 2: Check Physical Device**
```bash
# Enable USB debugging on phone
# Connect via USB
# Run: adb devices
# Authorize the computer on the phone
```

### Install the App
```bash
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

# Install debug APK
.\gradlew.bat :app:installDebug

# Or manually:
& "$env:ANDROID_HOME\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```

### Expected Output
```
> Task :app:installDebug
Installed on device
BUILD SUCCESSFUL
```

---

## Step 3: Launch the App

```bash
$env:ANDROID_HOME = "C:\Users\Saucey\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\platform-tools\adb.exe" shell am start -n com.emul8r.bizap/.MainActivity
```

### Expected Output
```
Starting: Intent { cmp=com.emul8r.bizap/.MainActivity }
```

**What This Does:**
- ✅ Launches the app on the device
- ✅ Triggers `BizapApplication.onCreate()`
- ✅ Initializes Timber and Firebase

---

## Step 4: Monitor Logcat

### Open Logcat View
```
Android Studio Menu:
  → View → Tool Windows → Logcat
  
Or press: Alt + 6
```

### Filter by Your App
```
Search box: tag:"^Bizap|^CreateInvoiceViewModel|^CustomerViewModel"

Or search for emoji:
  Search box: "✅|❌|⚠️"
```

### Expected Logs on App Launch
```
D/BizapApplication: 🚀 Bizap initialized in DEBUG mode. Full logging enabled.
D/BizapApplication: ✅ Firebase Analytics initialized
```

**What This Means:**
- ✅ Timber is initialized with DebugTree
- ✅ Firebase Analytics is ready
- ✅ App started without crashes

---

## Step 5: Test Timber Logging

### Create Test Scenario
**Navigate to:** Create Invoice screen

### Perform These Actions
1. Create a new invoice (you'll see logs in real-time)
2. Watch Logcat for logs like:
   ```
   D/CreateInvoiceViewModel: 🔵 INVOICE SAVE STARTED
   D/CreateInvoiceViewModel: ✅ Customer selected: Test Customer
   D/CreateInvoiceViewModel: ✅ Subtotal calculated: 14999 cents
   D/CreateInvoiceViewModel: ✅ INVOICE SAVE COMPLETE - SUCCESS
   ```

### What This Verifies
- ✅ Timber is routing logs to Logcat
- ✅ DebugTree is working
- ✅ App is running without crashes

---

## Step 6: Test Exception Logging (Optional)

### Add Test Exception to Settings Screen

Edit: `app/src/main/java/com/emul8r/bizap/ui/settings/SettingsHubScreen.kt`

Add this temporary button (for testing only):
```kotlin
Button(
    onClick = {
        throw RuntimeException("Test crash for Firebase - remove after testing")
    }
) {
    Text("🧪 Test Crash (Remove After Testing)")
}
```

### Or Test from Terminal
```bash
# Force an exception from adb shell
adb shell am dumpheap com.emul8r.bizap /data/local/tmp/test.hprof

# Simpler: Just force-stop the app
adb shell am force-stop com.emul8r.bizap

# Then restart
adb shell am start -n com.emul8r.bizap/.MainActivity
```

### What You'd See in Logcat
```
W/AndroidRuntime: FATAL EXCEPTION: main
W/AndroidRuntime: Process: com.emul8r.bizap, PID: 12345
E/AndroidRuntime: java.lang.RuntimeException: Test crash for Firebase
E/AndroidRuntime:   at com.emul8r.bizap.ui.settings.SettingsHubScreen.lambda()
```

---

## Step 7: Check Firebase Console

### Navigate to Crashlytics Dashboard
```
1. Open: https://console.firebase.google.com
2. Select your project
3. Left sidebar: Crashlytics
4. You should see:
   - Dashboard overview
   - Alerts (if there are crashes)
   - Issues (list of crashes)
```

### What You Should See

**Best Case (with crashes):**
```
Crashlytics Dashboard shows:
- Crash rate: X%
- Top crashes list
- Timeline of crashes
- Device/OS information
- App version affected
```

**For Logging Breadcrumbs:**
```
1. Click on any crash
2. Look for "Logs" tab
3. You should see:
   ✅ 2026-03-05 10:15:23 "Customer selected: John"
   ✅ 2026-03-05 10:15:24 "Subtotal calculated: 14999"
   ❌ 2026-03-05 10:15:25 "CRASH: NullPointerException"
```

### If You See "No Data"

This is NORMAL for the first 5-30 minutes!

**Timeline:**
- 0-5 minutes: Firebase syncing data
- 5-15 minutes: Data appears in Console
- 15-30 minutes: All details visible (breadcrumbs, etc.)

**Solution: Wait and Refresh**
```
1. Close Firebase Console completely
2. Wait 10 minutes
3. Reopen: https://console.firebase.google.com
4. Select Crashlytics again
5. You should see data now
```

---

## Step 8: Verify Firebase Configuration

### Check Project ID
```bash
# In Bizap/app/google-services.json, look for:
"project_info": {
    "project_number": "123456789",
    "project_id": "your-project-id"
}
```

### Verify in Console
```
1. Firebase Console → Your Project → Project Settings
2. Verify "Project ID" matches what's in google-services.json
3. Verify "Project Number" matches
```

### Check Firebase Libraries
```bash
# In app/build.gradle.kts, verify:
implementation(platform(libs.firebase.bom))  // ✅ BOM manages versions
implementation(libs.firebase.crashlytics)    // ✅ Crashlytics
implementation(libs.firebase.analytics)      // ✅ Analytics
```

---

## Step 9: Monitor Both Build Types

### DEBUG Build (Development)
```bash
.\gradlew.bat :app:assembleDebug

# What happens:
- BuildConfig.DEBUG = true
- Timber.plant(DebugTree())
- Logs go to: Logcat (you see everything)
- Firebase: Still enabled (receives logs too)
```

### RELEASE Build (Production)
```bash
.\gradlew.bat :app:assembleRelease

# What happens:
- BuildConfig.DEBUG = false
- Timber.plant(CrashlyticsTree())
- Logs go to: Firebase Crashlytics (only WARN/ERROR)
- Logcat: Doesn't see DEBUG logs
```

**To Test RELEASE:**
```bash
# Build release
.\gradlew.bat :app:assembleRelease

# Install
adb install -r app\build\outputs\apk\release\app-release.apk

# Run
adb shell am start -n com.emul8r.bizap/.MainActivity

# Watch Logcat - should be QUIET (no D/ logs)
# But create an error and check Firebase Console
```

---

## Step 10: .gitignore Configuration

### Should google-services.json be in .gitignore?

**Answer: YES** ⚠️ SECURITY CRITICAL

### Add to .gitignore
```bash
# Navigate to project root
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

# Add to .gitignore
echo "app/google-services.json" >> .gitignore

# Verify it was added
cat .gitignore
```

### Why Ignore google-services.json?

```
google-services.json contains:
✅ Project ID (OK to share)
⚠️ API keys (could allow unauthorized access)
⚠️ Project number (specific identifier)
❌ Should NOT be committed to Git
```

### If Already Committed
```bash
# Remove from git history (IMPORTANT!)
git rm --cached app/google-services.json
git commit -m "chore: Remove google-services.json from tracking"

# Each developer should have their own:
git update-index --assume-unchanged app/google-services.json

# Push
git push origin main
```

### Each Developer Needs Their Own
```
1. Download google-services.json from Firebase Console
2. Place at: Bizap/app/google-services.json
3. This file is already in .gitignore (not tracked)
4. Build locally
```

---

## Complete Verification Checklist

### ✅ Compilation
- [ ] `./gradlew clean :app:assembleDebug` builds successfully
- [ ] No Firebase-related compilation errors
- [ ] APK generated: `app/build/outputs/apk/debug/app-debug.apk`

### ✅ Installation
- [ ] `adb devices` shows your device
- [ ] `./gradlew :app:installDebug` succeeds
- [ ] App icon appears on device home screen

### ✅ Launch
- [ ] `adb shell am start -n com.emul8r.bizap/.MainActivity` succeeds
- [ ] App starts without crashing
- [ ] Logcat shows initialization logs

### ✅ Timber Logging
- [ ] Logcat shows: "🚀 Bizap initialized in DEBUG mode"
- [ ] Logcat shows: "✅ Firebase Analytics initialized"
- [ ] Create invoice → see logs in Logcat
- [ ] Logs have emoji prefixes (✅, ❌, ⚠️)

### ✅ Firebase Console
- [ ] Firebase Console loads without errors
- [ ] Your project is selected
- [ ] Crashlytics dashboard is visible
- [ ] (After 5-15 min) Data starts appearing

### ✅ Configuration
- [ ] `app/google-services.json` exists
- [ ] Project ID in file matches Firebase Console
- [ ] `.gitignore` contains `app/google-services.json`
- [ ] File is NOT committed to Git

---

## Troubleshooting Quick Matrix

| Issue | Check | Solution |
|-------|-------|----------|
| App won't build | google-services.json | Verify file exists, valid JSON |
| Logcat empty | Device connected | Run `adb devices` |
| No logs visible | Logcat filter | Clear filters, search for "Bizap" |
| Firebase shows "No data" | Time passed | Wait 10+ minutes, refresh |
| Firebase shows 0 crashes | Run the app | Create an invoice to generate logs |
| Logs not in Firebase | Build type | Test RELEASE build, not DEBUG |
| App crashes | Check Logcat | Look for stack trace before crash |

---

## Commands Quick Reference

```bash
# Build
.\gradlew.bat clean :app:assembleDebug --no-daemon

# Install
.\gradlew.bat :app:installDebug

# Launch
adb shell am start -n com.emul8r.bizap/.MainActivity

# Watch Logcat
# (Use Android Studio GUI: View → Tool Windows → Logcat)

# Check Devices
adb devices

# Clear App Data (fresh start)
adb shell pm clear com.emul8r.bizap

# View Logs from Command Line
adb logcat | grep -i "Bizap\|CreateInvoiceViewModel"

# Stop App
adb shell am force-stop com.emul8r.bizap
```

---

## Expected Timeline

```
Time    Action                              What You See
----    ------                              ----
T+0     Build app                          BUILD SUCCESSFUL
T+1     Install                            App installed
T+2     Launch app                         App opens
T+3     Create invoice                     Logcat logs appear
T+5     Check Firebase Console             "Loading..." (syncing)
T+10    Refresh Firebase Console           Your logs appear ✅
T+15    Check breadcrumbs                  Full timeline visible
```

---

## Success Indicators

### ✅ Tier 1: Basic Setup Works
- App builds without errors
- App installs successfully
- App launches without crash
- BizapApplication logs appear in Logcat

### ✅ Tier 2: Logging Works
- Timber logs appear in Logcat during operations
- Logs have emoji prefixes
- No Firebase errors in Logcat

### ✅ Tier 3: Firebase Connected
- Firebase Console shows your project
- Crashlytics dashboard is accessible
- Data appears after 5-15 minutes

### ✅ Tier 4: Full Integration
- Breadcrumb trails visible in Firebase
- Exceptions recorded separately
- Can trace full operation timeline

---

## Next Steps After Verification

### If Everything Works ✅
1. Read: `docs/BIZAPAPPLICATION_TIMBER_GUIDE.md`
2. Add Timber logging to 5+ other ViewModels
3. Build and test RELEASE version
4. Deploy to test users and monitor

### If Something Fails ❌
1. Check the Troubleshooting Matrix above
2. Verify google-services.json validity
3. Check Firebase Console project settings
4. Ensure Project ID matches

---

## Pro Tips

### 1. Keep Terminal Open
```bash
# Run logcat in separate terminal to watch in real-time
adb logcat | grep "✅\|❌\|⚠️"
```

### 2. Test Different Scenarios
```
- Successful operation: Should log ✅ messages
- Failed operation: Should log ❌ messages
- Warnings: Should log ⚠️ messages
```

### 3. Monitor Exact Timestamps
Firebase logs include timestamps, compare with Logcat timestamps:
```
Logcat: D/CreateInvoiceViewModel: ✅ Customer selected (10:15:24.123)
Firebase: ✅ Customer selected (2026-03-05 10:15:24)
```

### 4. Use Firebase Console Search
```
1. Firebase Console → Crashlytics
2. Look for "Search" or "Filter" button
3. Search by date, version, device type
```

---

## You're All Set!

Once you see data in Firebase Console, your verification is complete. You now have:

✅ Working Timber logging  
✅ Connected Firebase Crashlytics  
✅ Breadcrumb trails enabled  
✅ Analytics initialized  
✅ Both DEBUG and RELEASE paths ready  

**Next: Start adding Timber logs throughout your codebase!** 🎯


