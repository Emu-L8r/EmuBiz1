# Release Build Verification Report

## Build Info
- Gradle: 9.2.1
- AGP: 8.5.0
- Kotlin: 2.0.21
- R8 Enabled: YES
- Resource Shrinking: YES
- Min SDK: 26 | Target SDK: 35
- App Version: 1.0 (versionCode 2)

---

## Build Results

### Release APK Build
- [ ] Build Successful: YES / NO
- Build Time: ___ seconds
- APK Size: ___ MB (expected 24–28 MB)
- Build command: `cd Bizap && ./gradlew clean assembleRelease --stacktrace`
- APK location: `Bizap/app/build/outputs/apk/release/app-release.apk`

### ProGuard / R8 Configuration (`Bizap/app/proguard-rules.pro`)
- [x] proguard-rules.pro exists: YES
- [x] Rules include Hilt: YES (entry points, generated classes, @AndroidEntryPoint, @HiltViewModel)
- [x] Rules include Room: YES (entities, DAOs, RoomDatabase subclasses, column annotations)
- [x] Rules include SQLCipher: YES (`net.zetetic.**` and native methods)
- [x] Rules include Android Keystore / Security Crypto: YES (`android.security.keystore.**`, `androidx.security.crypto.**`)
- [x] Rules include Kotlin Coroutines: YES (`kotlinx.coroutines.**`, `kotlin.coroutines.**`)
- [x] Rules include WorkManager: YES (Worker, CoroutineWorker, ListenableWorker)
- [x] Rules include Firebase / Crashlytics: YES
- [x] Rules include Retrofit / OkHttp: YES
- [x] Rules include Kotlin Serialization: YES
- [x] Rules include Timber: YES (d/v/i removed in release)
- [x] Optimization set: 3 passes (no `-dontoptimize`)
- [ ] No R8 warnings: YES / NO
  - If warnings exist:
    ```
    [paste warnings here]
    ```

---

## Device Installation & Testing

### How to Install Release APK
```bash
# Uninstall any existing debug version
adb uninstall com.emul8r.bizap 2>/dev/null

# Install release APK
adb install -r Bizap/app/build/outputs/apk/release/app-release.apk
```

### APK Installation
- [ ] adb install succeeded: YES / NO
- [ ] APK file size: ___ MB
- [ ] Installation time: ___ seconds

---

### Test Results (Sequential)

Execute this exact sequence on device or emulator and record results.

| Test | Expected | Actual | Status |
|------|----------|--------|--------|
| 1. App Launch | App opens, no crash | [result] | ✅ / ❌ |
| 2. First-Time Setup | Business profile / PIN screen shows | [result] | ✅ / ❌ |
| 3. Database Access | Invoice list visible (even if empty) | [result] | ✅ / ❌ |
| 4. Create Invoice | Invoice saves successfully | [result] | ✅ / ❌ |
| 5. Read Data | Invoice appears in list | [result] | ✅ / ❌ |
| 6. Close App | App closes cleanly | [result] | ✅ / ❌ |
| 7. Reopen App | App relaunches, no crash | [result] | ✅ / ❌ |
| 8. Data Persists | Invoice still visible after restart | [result] | ✅ / ❌ |

---

### Logcat Analysis

```bash
# Clear previous logs then run the test sequence above, then:
adb logcat -c
adb logcat > /tmp/release_apk_logcat.txt &

# After testing:
grep -i "error\|exception\|crash\|fatal" /tmp/release_apk_logcat.txt
grep -i "hilt\|room\|sqlcipher\|native\|zetetic" /tmp/release_apk_logcat.txt
```

**Errors found:**
```
[paste any error messages from logcat]
```

**R8/Obfuscation issues:**
```
[paste any obfuscation-related errors]
```

**Native/JNI errors:**
```
[paste any native code errors]
```

---

## Common Issues & Solutions

| Issue | Symptom | Root Cause | Fix |
|-------|---------|-----------|-----|
| Hilt broken | App crashes on launch | Missing keep rules for generated classes | Rules added: `**_Hilt_*`, `**_Factory`, `**_MembersInjector` |
| Room broken | Database access fails | DAO/Entity obfuscated | Rules added: `@Room.Dao`, `@Room.Entity`, `RoomDatabase` subclasses |
| SQLCipher broken | `UnsatisfiedLinkError` | Native methods / JNI stripped | Rules added: `net.zetetic.**` + `native <methods>` |
| Keystore broken | `SecurityException` on PIN | KeyStore class obfuscated | Rules added: `android.security.keystore.**`, `androidx.security.crypto.**` |
| WorkManager broken | Background sync fails | Worker subclass not found | Rules added: `CoroutineWorker`, `ListenableWorker` subclasses |
| Coroutines broken | Suspend functions crash | Coroutine internals stripped | Rules added: `kotlinx.coroutines.**` |

---

## Issues Found & Fixed

### ProGuard / R8 Rule Gaps (Fixed in this PR)
- **Issue:** SQLCipher native classes (`net.zetetic.**`) were not kept — would cause `UnsatisfiedLinkError` at runtime.
- **Fix:** Added `-keep class net.zetetic.** { *; }` and `-keep interface net.zetetic.** { *; }`.

- **Issue:** Android Keystore / Security Crypto classes not kept — `DatabasePassphraseManager` would fail to encrypt/decrypt the database passphrase.
- **Fix:** Added `-keep class android.security.keystore.** { *; }` and `-keep class androidx.security.crypto.** { *; }`.

- **Issue:** Kotlin Coroutines not kept — suspend functions and Flow could be broken by aggressive R8 optimisation.
- **Fix:** Added `-keep class kotlinx.coroutines.** { *; }` and related rules.

- **Issue:** WorkManager `Worker`/`CoroutineWorker` subclasses not kept — `SyncWorker` and `SnapshotRepairWorker` would fail to be instantiated by WorkManager's reflection-based factory.
- **Fix:** Added keep rules for all `Worker`, `CoroutineWorker`, and `ListenableWorker` subclasses.

- **Issue:** Contradictory `-optimizationpasses 5` followed by `-dontoptimize` — the optimization passes setting was silently ignored.
- **Fix:** Removed `-dontoptimize` and set `-optimizationpasses 3` for balanced safety and shrinking.

---

## Final Verification Checklist

### Build
- [ ] `./gradlew clean assembleRelease` = BUILD SUCCESSFUL
- [ ] Zero compilation errors
- [ ] R8 warnings reviewed and understood
- [ ] APK generated at `Bizap/app/build/outputs/apk/release/app-release.apk`

### Device Testing
- [ ] App launches without crash
- [ ] First-time setup (business profile + PIN) works
- [ ] Can view invoice list (SQLCipher + Room working)
- [ ] Can create and save an invoice (Hilt + Room + Database working)
- [ ] Invoice appears in list after creation
- [ ] App closes and relaunches cleanly
- [ ] Data persists after close/reopen
- [ ] No errors in logcat
- [ ] Features work identically to debug APK

### Documentation
- [x] `docs/RELEASE_BUILD_VERIFICATION.md` created
- [ ] Test results filled in above
- [ ] Issues found & fixed documented
- [ ] Final status: APPROVED or NEEDS FIXES

---

## Conclusion

- [ ] **READY FOR PLAY STORE: YES / NO**

**Verified by:** _______________  
**Date:** _______________  
**Status:** APPROVED / NEEDS FIXES

---

*Generated as part of Phase 1: Release Build Hardening.*
