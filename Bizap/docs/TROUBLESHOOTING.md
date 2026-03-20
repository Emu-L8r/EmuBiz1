# TROUBLESHOOTING — Bizap Common Issues & Solutions

**Last Updated:** March 20, 2026

---

## Build Issues

### FileSystemAlreadyExistsException During Release Build

**Error:**
```
Execution failed for task ':app:shrinkReleaseRes'.
> A failure occurred while executing com.android.build.gradle.internal.transforms.ShrinkProtoResourcesAction
  > java.nio.file.FileSystemAlreadyExistsException (no error message)
```

**Why It Happens:**
Proto resource shrinking task conflicts with resource naming. Low priority for MVP.

**Solution:**
✅ Already fixed in `app/build.gradle.kts`:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = false  // ← Disabled to prevent crash
        proguardFiles(...)
    }
}
```

**Result:** Release APK builds successfully. Slightly larger APK size (~2–5%), but fully functional.

**Next Steps:** None required for MVP. Can revisit in future release if storage matters.

---

## Navigation Issues

### App Crashes When Switching from GUI1 to GUI2

**Error:** App closes or hangs when tapping "Switch to GUI2" in Settings

**Root Cause:** GUI switch not properly routing through Landing Screen

**Solution:**
1. Verify `TraditionalGUIMainActivity` has this callback:
```kotlin
MainScreen(
    onSwitchGui = {
        landingViewModel.resetMode()
        startActivity(Intent(this@TraditionalGUIMainActivity, MainActivity::class.java))
        finish()
    }
)
```

2. If missing, add it (see `docs/archive/BIDIRECTIONAL_GUI_SWITCHING_COMPLETE.md` for details)

3. Restart app and test: GUI1 Settings → Switch to GUI2 → Should show Landing Screen

**Verification:**
- ✅ Landing Screen appears
- ✅ "Get Started" button shows (GUI2)
- ✅ Can tap and enter GUI2
- ✅ No crashes in Logcat

---

### App Crashes When Switching from GUI2 to GUI1

**Error:** App closes when tapping "Switch to GUI1" in Settings (GUI2)

**Root Cause:** Similar to above; GUI2's `onSwitchToGui1` callback incomplete

**Solution:**
1. Verify `ModernGUIMainActivity` has this callback in `GuiV2NavGraph`:
```kotlin
GuiV2NavGraph(
    navController = navController,
    startBusinessId = resolvedBusinessId,
    onSwitchToGui1 = {
        landingViewModel.resetMode()
        startActivity(Intent(this@ModernGUIMainActivity, MainActivity::class.java))
        finish()
    }
)
```

2. If missing, add it

3. Test: GUI2 Settings → Switch to GUI1 → Landing Screen should appear

---

### "Navigation Argument Missing" Error

**Error:**
```
java.lang.IllegalArgumentException: Navigation destination does not have an action from the current destination
```

**Root Cause:** Screen registered in one nav graph but not the other, or adapter returns `null`

**Solution:**
1. Check which GUI you're in (Logcat should show activity name)
2. Verify screen is registered in that GUI's nav graph
   - GUI1: Check `MainActivity.kt` NavHost for `composable<Screen.YourScreen>`
   - GUI2: Check `GuiV2NavGraph.kt` for `composable<ScreenV2.YourScreen>`
3. Verify adapter maps correctly:
   - Run: `./gradlew test -k "Gui1NavAdapterTest"` or `"Gui2NavAdapterTest"`
   - Check for your screen in adapter test output

**Prevention:**
When adding a new screen, register in BOTH nav graphs immediately (don't defer one).

---

### Infinite Loading / Stuck on Screen

**Symptom:** Screen shows "Loading..." indefinitely, or blank screen

**Debug Steps:**
1. Check Logcat for errors (search for your ViewModel class name)
2. If ViewModel uses `StateFlow`, verify initial state is not `Loading`:
```kotlin
val uiState: StateFlow<MyUiState> = repository.observe()
    .map { MyUiState.Success(it) }
    .catch { MyUiState.Error(it.message ?: "Unknown") }
    .stateIn(viewModelScope, SharingStarted.Eagerly, MyUiState.Loading)  // ← OK
```
3. If `catch` block is missing, add it (errors need to emit an error state)
4. Restart app: `./gradlew installDebug`

---

## Database Issues

### "Database Locked" Error

**Error:**
```
android.database.sqlite.SQLiteDatabaseLockedException: database is locked
```

**Why It Happens:**
Multiple transactions writing simultaneously (rare in single-app)

**Solution:**
1. Restart app (usually clears locks)
2. Clear app data: `adb shell pm clear com.emul8r.bizap`
3. If persists, check for infinite loops in DAO queries

---

### SQLCipher Decryption Fails (Database Corrupted)

**Error:** App won't open or crashes on startup with database error

**Symptom:** Can't decrypt `bizap-db` (encrypted database)

**Why It Happens:**
- Keystore key was regenerated or cleared
- Device factory reset (Android Keystore cleared)
- Database file corrupted

**Solution:**
1. **Safe option:** Uninstall and reinstall app (fresh database created)
   ```bash
   adb uninstall com.emul8r.bizap
   ./gradlew installDebug
   ```

2. **Alternative:** Clear app data
   ```bash
   adb shell pm clear com.emul8r.bizap
   ```

3. **Last resort:** Check if backup exists
   - See `DatabasePassphraseManager.kt` for encryption details
   - No backup mechanism currently; data is lost on key loss

**Prevention:** Use Android device backup service (users should enable Play account backup)

---

## Test Issues

### "Mock Error" or Hilt Injection Failure in Tests

**Error:**
```
kotlin.mock.MockKException: no match found for: ...
```

**Why It Happens:**
Test class not using `@HiltAndroidTest` or dependency not mocked correctly

**Solution:**
1. Add to test class:
```kotlin
@HiltAndroidTest
class MyScreenTest : BaseUnitTest() {
    // ...
}
```

2. Use proper mock setup:
```kotlin
val repository = mockk<MyRepository>()
every { repository.observe() } returns flowOf(data)
```

3. Run test: `./gradlew test -k "MyScreenTest"`

---

### "Keystore Entry Not Found" in Tests

**Error:**
```
java.security.KeyStoreException: no such algorithm
```

**Why It Happens:**
Tests run in JVM, not Android; Android Keystore not available

**Solution:**
1. Mock the `DatabasePassphraseManager`:
```kotlin
val passphraseManager = mockk<DatabasePassphraseManager>()
every { passphraseManager.getOrCreatePassphrase() } returns "test-passphrase"
```

2. Or use integration test with real Android runtime:
```bash
./gradlew connectedAndroidTest  # Runs on device/emulator
```

3. See existing tests in `src/androidTest/` for examples

---

## Performance Issues

### App Lags / Slow Invoice List

**Symptom:** Scrolling invoice list is janky; loads slowly

**Debug:**
1. Check Logcat for query errors
2. Run: `./gradlew test -k "InvoiceRepositoryImplEnhancedTest"` (verify queries are optimized)
3. Use Android Profiler:
   - Run app on emulator/device
   - Android Studio → View → Android Profiler
   - Watch CPU, memory, and DB thread during scroll
4. Check if N+1 queries are happening (common with Room)

**Quick Fixes:**
- Add `@Transaction` to DAO queries
- Use `@Relation` for joins
- Batch queries with `combine()` or `zip()`

See `InvoiceDaoV2.kt` for examples of optimized queries.

---

## Signing & Release Issues

### "Keystore File Not Found"

**Error:**
```
❌ Release signing configuration missing!
Either:
1. Set environment variables: KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
2. Place a development keystore at: ../release-key.jks
```

**Why It Happens:**
Signing credentials not configured for release build

**Solution:**
1. **For production:** Set environment variables
```bash
export KEYSTORE_PATH=/path/to/your/release-key.jks
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password
```

2. **For development:** Create local keystore
```bash
keytool -genkey -v -keystore ../release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias bizap-key \
  -storepass bizap123 -keypass bizap123
```
(Dev password only; never use in production)

3. Retry: `./gradlew assembleRelease`

See `/docs/RELEASE_SIGNING.md` for detailed guide.

---

### "APK Signature Invalid"

**Error:**
```
jarsigner -verify app-release.apk
> jar NOT verified.
```

**Why It Happens:**
APK was signed with wrong key or signing process interrupted

**Solution:**
1. Delete old APK: `rm app/build/outputs/apk/release/*.apk`
2. Rebuild: `./gradlew clean assembleRelease`
3. Verify: `jarsigner -verify app/build/outputs/apk/release/app-release.apk`
4. Should output: `jar verified.`

---

## Gradle & Build System

### "Gradle Sync Failed"

**Error:**
```
Could not determine the dependencies of task ':app:compileReleaseKotlin'
```

**Why It Happens:**
Dependency mismatch or corrupt `.gradle` cache

**Solution:**
1. Clean Gradle cache:
```bash
./gradlew clean
```

2. Invalidate IDE cache:
   - Android Studio → File → Invalidate Caches... → Invalidate and Restart

3. Resync Gradle:
   - File → Sync Now (or Alt+Ctrl+S)

4. If still failing, check `build.gradle.kts` for typos or version conflicts

---

### "Gradle Out of Memory"

**Error:**
```
Exception: OutOfMemoryError: Java heap space
```

**Why It Happens:**
Gradle process needs more memory

**Solution:**
1. Increase heap size in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=1024m
```

2. Retry build: `./gradlew clean build`

---

## IDE Issues

### Android Studio Won't Open Project

**Error:** "Project not found" or corruption message

**Solution:**
1. Close Android Studio
2. Delete IDE cache:
```bash
rm -rf .idea
rm -rf .gradle
```

3. Reopen project:
   - File → Open → Select Bizap folder
   - Wait for sync

---

### IntelliJ Code Inspection Errors (False Positives)

**Example:** "Variable is never used" (but it is, in Compose lambda)

**Solution:**
1. Ignore individual errors: Right-click → Suppress (add `@Suppress("UnusedVariable")`)
2. Or suppress file-wide at top:
```kotlin
@file:Suppress("UnusedVariable")

package com.emul8r.bizap.ui.screens
```

3. These don't affect actual build; just IDE analysis

---

## Still Stuck?

### Getting Help
1. **Check logs:**
   ```bash
   ./gradlew build 2>&1 | tee build-debug.log
   ```
   Share `build-debug.log` in issue or chat

2. **Check existing issues:** https://github.com/EmuBiz/Bizap/issues

3. **Ask the team:** Post in Slack/Discord with:
   - Device info (OS version, device model)
   - Steps to reproduce
   - Logcat output (search for "ERROR" or your screen name)

4. **Create a GitHub issue:**
   - Title: Descriptive (e.g., "Navigation crash when switching GUI2 → GUI1")
   - Description: Error message + steps to reproduce
   - Label: `ui`, `navigation`, `data`, etc.

---

## Performance Profiling

### Profile App Performance
```bash
# Install app
./gradlew installDebug

# Open Android Profiler (Android Studio)
View → Android Profiler

# Check:
# - CPU usage (should drop to ~10% when idle)
# - Memory (should stay <100 MB for invoice list)
# - GPU (smooth scrolling = no jank)
```

### Profile Database Queries
Enable SQL query logging:
```kotlin
// In DatabaseModule.kt, add for debugging:
.setQueryCallback { sqlQuery, bindArgs ->
    Timber.d("Query: $sqlQuery | Args: $bindArgs")
}
```

---

## Contributing Fixes

Found a fix? Help others:
1. Document in this file (add to relevant section)
2. Create PR: `docs: add troubleshooting for XYZ`
3. Tag with `documentation` label

---

**Last Updated:** March 20, 2026  
**Maintainer:** EmuBiz Development Team  
**Next Review:** April 3, 2026

