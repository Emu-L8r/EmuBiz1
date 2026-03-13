# 📊 **CRASHLYTICS SETUP & REVIEW GUIDE - MARCH 13, 2026**

---

## ✅ **Current Status**

Your Bizap app already has Firebase Crashlytics fully configured:

| Component | Status | Details |
|-----------|--------|---------|
| **Firebase Plugin** | ✅ Added | `alias(libs.plugins.firebase.crashlytics)` |
| **Crashlytics Dependency** | ✅ Added | `implementation(libs.firebase.crashlytics)` |
| **Timber Integration** | ✅ Complete | CrashlyticsTree sends logs to Firebase |
| **BizapApplication** | ✅ Initialized | Timber configured for DEBUG/RELEASE |
| **google-services.json** | ⚠️ Check | Needed to see crashes in Firebase Console |

---

## 🚀 **STEP 1: Verify google-services.json**

### Check if file exists:
```bash
# In PowerShell:
Test-Path "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\google-services.json"
```

**If file exists** → ✅ Firebase is connected  
**If file NOT found** → ⚠️ You need to download it from Firebase Console

### How to get google-services.json:

1. **Go to Firebase Console**
   - https://console.firebase.google.com/
   - Select your project (or create one)

2. **Download google-services.json**
   - Project Settings → Select "Android" app
   - Download the `google-services.json` file

3. **Place in your app folder**
   ```
   C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\google-services.json
   ```

4. **Rebuild and reinstall**
   ```bash
   ./gradlew clean assembleDebug
   ./gradlew installDebug
   ```

---

## 🔍 **STEP 2: Review Recent Crashes in Firebase Console**

### Access Crashlytics Dashboard:

1. **Open Firebase Console**
   - https://console.firebase.google.com/

2. **Select your project**
   - "Bizap" (or your project name)

3. **Navigate to Crashlytics**
   - Left sidebar → "Quality" → "Crashlytics"

4. **View crash list**
   - Shows all crashes reported from users/devices
   - Sorted by: Most recent first
   - Shows: Stack trace, affected versions, devices

### What You'll See:

```
📊 Crashlytics Dashboard
├─ Recent Issues (last 24 hours)
│  ├─ NullPointerException in InvoiceRepository.kt:250
│  │  └─ 3 instances, Latest: 2 hours ago
│  └─ IllegalStateException in CreateInvoiceViewModel
│     └─ 1 instance, Latest: 5 minutes ago
│
├─ Affected Devices
│  └─ Android 14, Pixel 6, Samsung Galaxy S23
│
└─ Version Breakdown
   ├─ v1.0 (build 2): 2 crashes
   └─ v1.0 (build 1): 1 crash
```

---

## 🧪 **STEP 3: Test Crashlytics (Optional)**

### Add a Test Crash Button

If you want to verify Crashlytics is working, add this to `SettingsScreen.kt`:

```kotlin
@Composable
fun SettingsScreenContent() {
    Column {
        // ... existing settings ...
        
        // DEBUG ONLY - Test Crash Button
        if (BuildConfig.DEBUG) {
            Button(
                onClick = {
                    throw RuntimeException("🧪 Test crash from Settings screen")
                },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text("🧪 Test Crash (Debug Only)")
            }
        }
    }
}
```

### Steps to test:

1. **Build and install RELEASE build**
   ```bash
   ./gradlew clean :app:assembleRelease
   ./gradlew installRelease
   ```

2. **Launch app on device/emulator**

3. **Open Settings → Tap "Test Crash"**

4. **Wait 5-10 seconds** (Crashlytics uploads)

5. **Check Firebase Console** (new crash should appear)

---

## 📋 **STEP 4: Understand Crash Reports**

### Crash Details Panel (Click Any Crash):

| Section | What It Shows |
|---------|--------------|
| **Stack Trace** | Exact line numbers where crash occurred |
| **Breadcrumbs** | All Timber logs BEFORE the crash (from `CrashlyticsTree`) |
| **Device Info** | OS version, manufacturer, RAM, etc. |
| **App Info** | Version number, build number, session ID |
| **Frequency** | How many users affected, when it occurs |

### Example Crash Report:

```
❌ NullPointerException
   File: InvoiceRepositoryImpl.kt
   Line: 250
   Method: saveInvoice()
   
📋 Breadcrumb Trail (logs before crash):
   D: 🔢 Invoice display name: customer-03131-01
   D: 💾 INSERT new invoice for business 1
   E: ❌ CRITICAL: Failed to create snapshots  ← Problem here
   
📱 Device: Samsung Galaxy S23, Android 14
🔧 Version: 1.0 (build 2)
👥 Affected Users: 3
⏰ First Occurrence: 2 hours ago
⏰ Latest Occurrence: 5 minutes ago
```

### How to Fix Issues from Crashes:

1. **Read the stack trace** → Find the exact line
2. **Review breadcrumbs** → What was happening before crash?
3. **Check logs in Timber** → Match logs to breadcrumbs
4. **Reproduce locally** → Add same conditions in dev environment
5. **Fix and test** → Verify fix in debug mode
6. **Deploy new version** → Upload to Firebase/App Store

---

## 🔐 **STEP 5: Enable Data Collection Settings**

### In Firebase Console:

1. **Go to Project Settings**
   - ⚙️ (gear icon) → Project Settings

2. **Data Privacy**
   - Ensure "Crashlytics data collection" is ✅ Enabled

3. **Crash Reporting**
   - ✅ Automatically collect crash data
   - ✅ Send crash reports to Crashlytics

---

## 📊 **STEP 6: Monitor Ongoing Issues**

### Set Up Alerts (Optional):

In Firebase Console:

1. **Crashlytics → Issue Settings**
2. **Enable Alerts**:
   - ✅ Alert when new issue occurs
   - ✅ Alert when issue increases in frequency
   - ✅ Alert when issue affects X% of users

3. **Notification Method**:
   - Email notifications
   - Slack integration (if configured)

---

## 🎯 **YOUR CURRENT SETUP**

### What's Already Configured:

✅ **BizapApplication.kt**
- Timber initialized
- DEBUG mode → `Timber.DebugTree()` (Logcat)
- RELEASE mode → `CrashlyticsTree()` (Firebase)
- Firebase Analytics enabled

✅ **CrashlyticsTree.kt**
- Captures WARN/ERROR level logs
- Sends to Firebase Crashlytics
- Records exceptions with full context

✅ **Gradle Dependencies**
- Firebase BOM configured
- Crashlytics library added
- Google Services plugin enabled

### What You Need to Do:

1. ⚠️ **Get google-services.json** from Firebase Console
2. ✅ **Place it in `app/` folder**
3. ✅ **Rebuild and install**
4. ✅ **Go to Firebase Console → Crashlytics**

---

## 🚀 **QUICK COMMANDS**

```bash
# Build and install for testing crashes
./gradlew clean :app:assembleRelease
./gradlew installRelease

# Build debug for development
./gradlew clean :app:assembleDebug
./gradlew installDebug

# View logs in real-time
adb logcat -s "Timber" "*" | findstr "com.emul8r.bizap"
```

---

## ✅ **CHECKLIST: Ready for Production**

- [ ] google-services.json downloaded from Firebase
- [ ] File placed in `app/google-services.json`
- [ ] Build successful: `./gradlew clean assembleRelease`
- [ ] App installed on test device
- [ ] Firebase Console accessible
- [ ] Crashlytics tab visible in Firebase
- [ ] Test crash button works (optional)
- [ ] Alerts configured in Firebase (optional)

---

## 📞 **FIREBASE RESOURCES**

- **Firebase Console**: https://console.firebase.google.com/
- **Crashlytics Docs**: https://firebase.google.com/docs/crashlytics
- **Timber Docs**: https://github.com/JakeWharton/timber
- **Android Logging**: https://developer.android.com/studio/debug/logcat

---

**Setup Complete!** 🎉  
Your Crashlytics is ready to monitor production crashes in real-time.

