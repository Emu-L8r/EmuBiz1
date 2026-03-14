# 🚀 QUICK GUIDE: Test Crashlytics in 5 Minutes

## The Situation

✅ **Crashlytics is configured correctly**  
❌ **You're running DEBUG build (which doesn't use Firebase)**  
✅ **RELEASE build will use Firebase**

---

## STEP 1: Build Release APK (2 min)

```bash
./gradlew assembleRelease
```

**Expected output:**
```
BUILD SUCCESSFUL in X seconds
```

**File created:**
```
app/build/outputs/apk/release/app-release.apk
```

---

## STEP 2: Install Release APK (1 min)

```bash
# First, uninstall the DEBUG version
adb uninstall com.emul8r.bizap

# Then install the RELEASE version
adb install app\build\outputs\apk\release\app-release.apk
```

**Expected output:**
```
Success
```

---

## STEP 3: Trigger a Test Crash (1 min)

In the running app, do something that will cause a crash. For example:

**Option A: Create an invoice and intentionally cause error**
- The app uses Crashlytics, so any unhandled exception will be captured

**Option B: Add test crash code temporarily** (to a button click):
```kotlin
throw RuntimeException("Test crash from Crashlytics")
```

---

## STEP 4: Check Firebase Console (1 min)

```
https://console.firebase.google.com/project/bizap-801c0/crashlytics
```

**What you'll see:**
- Crash report appears within 30 seconds
- Full stack trace
- All Timber logs before the crash
- Device info (model, OS, app version)

---

## ✅ That's It!

If the crash appears in Firebase Console, Crashlytics is working perfectly.

---

## 📝 WHAT FIREBASE WILL CAPTURE

When users install your app from Play Store:
1. ✅ Any uncaught exceptions
2. ✅ ANRs (app freezes >5 seconds)
3. ✅ All Timber logs before the crash
4. ✅ Device info (model, Android version, RAM, etc.)
5. ✅ App version
6. ✅ Timestamp

---

## 🎯 REMEMBER

- **DEBUG builds:** Crashes go to Logcat (Android Studio)
- **RELEASE builds:** Crashes go to Firebase Crashlytics
- **Play Store:** Users get RELEASE build → Crashes appear in Firebase

This is the expected and correct behavior. ✅


