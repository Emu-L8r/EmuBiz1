# Phase 1 Troubleshooting: Common Startup Issues

## Issue: App Crashes Immediately

### Symptom
Logcat shows:
```
E/AndroidRuntime: FATAL EXCEPTION: main
    java.lang.RuntimeException: Unable to start activity ComponentInfo{...}: ...
```

### Common Causes & Solutions

#### 1. Database Migration Failure
**Error**: `java.lang.IllegalStateException: Migration didn't properly handle...`

**Root Cause**: Room database version mismatch or migration script broken

**Solution**:
- Check `AppDatabase.kt` for version number
- If version changed, migration script must exist
- Current: Version 8 (check `ARCHITECTURE.md`)

**Quick Fix** (development only):
```kotlin
// In AppDatabase.kt
.fallbackToDestructiveMigration() // Deletes old DB, starts fresh
```

---

#### 2. Hilt DI Initialization Failure
**Error**: `java.lang.RuntimeException: Could not find EntryPoint...`

**Root Cause**: Missing Hilt setup, missing @HiltAndroidApp

**Solution**:
- Verify `AndroidManifest.xml` has activity with `@HiltAndroidActivity`
- Verify `Application` class has `@HiltAndroidApp`

**Check** (grep command):
```bash
grep -r "@HiltAndroidApp" app/src/main/java
```

Should return a result. If not, add to your Application class.

---

#### 3. Missing Permissions
**Error**: `java.lang.SecurityException: Permission Denial...`

**Root Cause**: App needs runtime permissions not granted

**Solution**:
- Check `AndroidManifest.xml` for required permissions
- On Android 6+, grant permissions at runtime
- Required: `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE` (for PDF)

**Fix**:
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" /> <!-- if needed later -->
```

---

#### 4. Room Database Codec Issue
**Error**: `java.lang.IllegalStateException: Unable to read Kotlin metadata...`

**Root Cause**: Serialization codec mismatch (though shouldn't happen - we fixed this)

**Solution**:
- This would indicate the build wasn't clean
- Try: `./gradlew clean assembleDebug` again

---

### If None of the Above Match

**Provide the full stack trace**, and we'll diagnose the actual cause.

---

## Issue: App Opens But Shows Blank Screen

### Symptom
- App launches successfully
- No crash dialog
- But UI is completely blank (white/black screen)

### Common Causes & Solutions

#### 1. Data Loading Blocked
**Root Cause**: Repository query hanging or UI state not updating

**Check Logcat**:
```
D/myapp: Loading customers...
[then nothing happens]
```

**Solution**:
- Check if `InvoiceDetailViewModel.loadInvoice()` is being called
- Verify `Flow.collectAsStateWithLifecycle()` is working
- Possible database lock or transaction issue

**Quick Diagnosis**:
```bash
adb logcat | findstr "Loading"
```

Look for logs like "Loading customers" or "Database query..."

---

#### 2. Navigation/Routing Issue
**Root Cause**: Navigation graph not configured, route serialization broken

**Symptom**:
- App opens to blank screen
- Logcat shows no navigation events

**Solution**:
- Verify `MainActivity` has `NavHost` configured
- Check `@Serializable` screen objects are accessible
- Verify navigation routes match screen names

---

#### 3. ViewModel Not Injected
**Root Cause**: `hiltViewModel()` call failing silently

**Error Pattern**:
```
W/ViewModel: No ViewModel factory found
```

**Solution**:
- Ensure ViewModel has `@HiltViewModel`
- Ensure constructor parameters are injected
- Verify Hilt sees the ViewModel class

**Check**:
```bash
grep -r "@HiltViewModel" app/src/main/java/com/emul8r/bizap/ui/
```

Should show all ViewModels.

---

#### 4. Theme/Compose Issue
**Root Cause**: Material 3 theme not properly initialized

**Symptom**:
- App opens but UI elements invisible (white text on white background)

**Solution**:
- Check `Theme.kt` exists and is applied
- Verify `@Composable` preview functions work in Android Studio
- Try: Restart emulator

---

### If Blank Screen Persists

**Provide**:
1. Full logcat output (all lines, not filtered)
2. Screenshots from emulator
3. Any ERROR or WARNING lines

---

## Issue: Specific Features Fail

### PDF Generation Fails
**Error**: `java.io.FileNotFoundException: Cannot access file`

**Solution**:
- Check `InvoicePdfService.kt` for file path logic
- Ensure `context.getFilesDir()` is writable
- Verify PDF generation permissions granted

---

### Navigation Crash (Route Change)
**Error**: `java.lang.IllegalArgumentException: Route must not be null`

**Solution**:
- Check `Screen` serialization in navigation
- Verify `@Serializable` on all route objects
- Check parameter passing in navigation calls

---

### Data Not Persisting (Restart App = Data Lost)
**Error**: No error, but data disappears

**Solution**:
- Check Room transactions are complete (`suspend` functions)
- Verify `DAO.insert()` is being awaited
- Check `saveInvoice()` completes before navigation

---

## Phase 1 Success Criteria

✅ App installs without errors
✅ App launches without crashing
✅ Logcat shows normal startup (no FATAL or ERROR)
✅ UI is visible (not blank)
✅ No permission dialogs or crashes

---

## When to Report Issues

**If you hit any issue**, provide:

1. **Error Type**: Crash / Blank Screen / Feature Broken
2. **Full Logcat Output**: Everything after app launch
3. **Steps to Reproduce**: What did you tap?
4. **Expected vs Actual**: What should happen vs what happened

This will enable rapid diagnosis and fix.

---

**Ready to install and test?**

