# ⚡ QUICK START: TEST PRODUCTION APK

**APK Location:** `app/build/outputs/apk/release/app-release.apk`

---

## 🚀 FASTEST WAY TO TEST (5 minutes)

### **If you have ADB installed:**

```bash
# Copy-paste these commands in PowerShell:

cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"

# Install on device
adb install "app\build\outputs\apk\release\app-release.apk"

# Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity

# Watch logs for errors
adb logcat | findstr /i "bizap timber error exception"
```

### **If you DON'T have ADB:**

```bash
# 1. Connect phone via USB
# 2. Open File Explorer
# 3. Navigate to: C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\release\
# 4. Copy app-release.apk to your phone
# 5. On phone: tap APK to install
```

---

## ✅ MINIMUM TESTING (2 minutes)

Just do this to verify it's not broken:

1. **App launches?** Tap icon → Should see splash screen → Should see dashboard
2. **Create invoice?** Tap "+" → Fill form → Save → Should appear in list
3. **Shows data?** Dashboard should show revenue metrics (not $0.00)
4. **Close/reopen?** Close app → Tap icon again → Data should still be there

**If all 4 pass → ✅ GOOD TO SHIP**

---

## 🔍 DETAILED TESTING (15 minutes)

See **PRODUCTION_BUILD_TESTING_GUIDE.md** for full checklist

---

## 🚨 CRITICAL CHECKS ONLY

Watch for these red flags:

```
❌ CRITICAL: App won't launch
   → Check Crashlytics, share crash log

❌ CRITICAL: Dashboard shows $0.00
   → Check if snapshots are created, see logs for "createAnalyticsSnapshots"

❌ CRITICAL: Data disappears on close/reopen
   → This would indicate database issue, check Crashlytics for migration errors

❌ CRITICAL: Can't create invoice
   → Check logs for validation errors, should see "INVOICE SAVE" logs
```

**Any critical issues → Share logs + I'll help fix**

---

## 📊 BUILD INFO

```
✅ Build Status: SUCCESSFUL
✅ APK Size: 33.05 MB
✅ Duration: 4m 26s
✅ Errors: 0
✅ Warnings: 2 (kotlin metadata - safe to ignore)
```

---

## 📝 WHAT TO REPORT BACK

After testing, tell me:

1. **Did it launch?** (yes/no)
2. **Could you create invoice?** (yes/no)
3. **Did dashboard show numbers?** (yes/no)
4. **Did data persist?** (yes/no)
5. **Any errors in Crashlytics?** (list them)

That's all I need to know if it's ready.


