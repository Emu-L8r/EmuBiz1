# 🔍 APP CRASH DIAGNOSTIC GUIDE

**Symptom:** App crashes immediately after starting  
**Probable Causes:** Firebase initialization, missing API keys, Hilt injection issues

---

## 🔧 COMMON CRASH CAUSES & SOLUTIONS

### **Cause 1: Missing Exchange Rate API Key**

**Most Likely!** The app uses an API key for currency exchange rates.

**Check:**
```bash
# Look in your local.properties file
cat local.properties
```

**Should have:**
```properties
EXCHANGE_RATE_API_KEY=your_key_here
```

**If missing:**
1. Get free API key from: https://exchangerate-api.com/
2. Add to `local.properties`:
```properties
EXCHANGE_RATE_API_KEY=your_actual_key
```
3. Rebuild APK
4. Reinstall

---

### **Cause 2: Firebase Issues**

The app uses Firebase (Crashlytics, Analytics).

**Check:**
1. `app/google-services.json` exists?
2. File is valid JSON?
3. Package name matches: `com.emul8r.bizap`

**If google-services.json is missing or invalid:**
```
This could cause:
- Firebase initialization crash
- SecurityException
- ClassNotFoundException
```

**Fix:**
1. Get correct `google-services.json` from Firebase Console
2. Place in: `app/google-services.json`
3. Rebuild and reinstall

---

### **Cause 3: Hilt Dependency Injection Issues**

The app uses Hilt for DI.

**Potential Issues:**
- Missing Hilt-generated classes
- Scope mismatches
- Missing @HiltAndroidApp annotation

**Check MainActivity.kt:**
```kotlin
// Should have:
@HiltViewModel
class MainActivity : ComponentActivity() {
    ...
}

// BizapApplication should have:
@HiltAndroidApp
class BizapApplication : Application() {
    ...
}
```

---

### **Cause 4: ProGuard Minification Issues**

We added Hilt keep rules, but something might be stripped.

**Check:**
```
Missing classes like Hilt_MainActivity or similar
```

**Fix:**
1. Rebuild with debug APK (not minified)
2. APK is already debug, so this is unlikely

---

## 🚀 DIAGNOSTIC STEPS

### Step 1: Get the Exact Error

**Run this command to see full crash log:**

```bash
adb logcat > crash_log.txt
# Let it run for 10 seconds
# Then open crash_log.txt and look for:
# - FATAL EXCEPTION
# - E/AndroidRuntime
# - Caused by:
```

### Step 2: Check for Common Patterns

**Look for:**
- `FileNotFoundException` → Missing files/configs
- `NullPointerException` → Uninitialized objects
- `ClassNotFoundException` → Missing classes (Hilt issue)
- `SecurityException` → Missing permissions or Firebase
- `IllegalStateException` → State/initialization issues

### Step 3: Check App Requirements

**Verify you have:**
```bash
# 1. local.properties with API key
cat local.properties

# 2. google-services.json exists
ls -la app/google-services.json

# 3. Check logcat for specific error
adb logcat
```

---

## 📝 MOST LIKELY SOLUTION

**99% chance it's the Exchange Rate API Key!**

**Quick fix:**

```bash
# 1. Create/update local.properties
echo "EXCHANGE_RATE_API_KEY=dummykey123" > local.properties

# 2. Rebuild
./gradlew assembleDebug

# 3. Reinstall
adb uninstall com.emul8r.bizap
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. Launch
adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

## 🎯 QUICK CHECKLIST

- [ ] Check `local.properties` for `EXCHANGE_RATE_API_KEY`
- [ ] Verify `app/google-services.json` exists
- [ ] Run `adb logcat` to capture exact error
- [ ] Look for "Caused by:" section in logs
- [ ] Search error message in our guides

---

## 📱 VIEW DETAILED CRASH LOG

```bash
# Clear previous logs
adb logcat -c

# Start app to trigger crash
adb shell am start -n com.emul8r.bizap/.MainActivity

# Wait 5 seconds, then dump logs
adb logcat -d > crash_full.txt

# View the file
cat crash_full.txt | grep -A 50 "FATAL EXCEPTION"
```

---

## ✨ NEXT STEPS

1. **Get the exact error** (use logcat)
2. **Check if it's API key** (most likely)
3. **If API key issue**: Add to local.properties and rebuild
4. **If Firebase issue**: Get correct google-services.json
5. **If Hilt issue**: Verify annotations in code
6. **Report back** with the exact error from logcat

**What error message do you see in the crash?**


