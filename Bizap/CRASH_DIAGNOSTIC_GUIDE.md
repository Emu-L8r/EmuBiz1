# 🔍 APP CRASH DIAGNOSTIC GUIDE

**Status:** App crashes immediately after launching  
**Goal:** Capture crash details to identify root cause

---

## 🚀 STEP 1: CAPTURE THE CRASH DETAILS

### Option A: From Android Studio (Easiest)
```
1. Open Android Studio
2. File → Open → Bizap project folder
3. Bottom of screen → "Logcat" tab
4. Ensure "Show only selected application" is checked
5. Clear logcat: Right-click → Clear All
6. Click Run ▶ to launch app
7. WAIT for crash (5-10 seconds)
8. Copy ALL red error text from Logcat
9. Paste here in response
```

### Option B: From Command Line
```bash
# Clear previous logs
adb logcat -c

# Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity

# Capture crash (30 seconds)
adb logcat -d -s AndroidRuntime:E BizapApplication:E Timber:E > crash_log.txt

# View
cat crash_log.txt
```

---

## 📊 WHAT TO LOOK FOR

When you see the crash, copy everything that looks like:

```
E  FATAL EXCEPTION: main
E  Process: com.emul8r.bizap, PID: xxxxx
E  java.lang.RuntimeException: ...
E    at ...
E    at ...
```

Or look for:

```
E  BizapApplication
E  CrashlyticsTree  
E  ExchangeRateWorker
E  FirebaseAnalytics
E  Any other RED E  lines
```

---

## 🎯 COPY THE FULL CRASH AND SHARE IT

Once you capture the crash:

1. **Scroll to the TOP** of the logcat output
2. **Copy ALL text** that mentions the crash
3. **Paste it in your response**

Example format (copy what you see):
```
2026-03-05 20:XX:XX.XXX XXXXX-XXXXX AndroidRuntime: E  FATAL EXCEPTION: main
2026-03-05 20:XX:XX.XXX XXXXX-XXXXX AndroidRuntime: E  Process: com.emul8r.bizap, PID: XXXXX
2026-03-05 20:XX:XX.XXX XXXXX-XXXXX AndroidRuntime: E  java.lang.SomeException: Message here
2026-03-05 20:XX:XX.XXX XXXXX-XXXXX AndroidRuntime: E    at com.emul8r.bizap.SomeClass.someMethod
...
```

---

## ✅ ONCE YOU SHARE THE CRASH LOG

I will:
1. ✅ Identify the exact error
2. ✅ Find which line of code is failing
3. ✅ Provide the fix
4. ✅ Apply it to your project
5. ✅ Rebuild and verify it works

---

**Ready? Go ahead and capture the crash log using Option A or B above, then paste it here.** 🎯

