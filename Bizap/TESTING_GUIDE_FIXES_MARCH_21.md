# 🧪 TESTING GUIDE - Fix Verification

**Date:** March 21, 2026  
**Target:** Verify both critical fixes work correctly  
**Expected Duration:** 15-20 minutes

---

## ✅ CHECKLIST: Before You Start

- [ ] Android Studio is closed
- [ ] Emulator is running (`adb devices` shows connected device)
- [ ] Previous APK is uninstalled or you're okay with it being replaced
- [ ] You have 15 minutes for testing

---

## 🔧 QUICK START (5 minutes)

### Step 1: Invalidate Cache
1. Open Android Studio
2. **File → Invalidate Caches and Restart**
3. Click **Invalidate and Restart**
4. Wait for Android Studio to reopen

### Step 2: Clean Build
1. **Build → Clean Project**
2. Wait for completion
3. **Build → Rebuild Project**
4. Wait for completion

### Step 3: Uninstall Old APK (Optional but Recommended)
```powershell
adb uninstall com.emul8r.bizap
```

---

## 🧪 TEST #1: Android Studio Deployment (7 minutes)

### Objective
Verify the app launches without crashing when using the green play button.

### Steps
1. In Android Studio, click the **green play button** (Run → Run 'app')
2. Select your emulator if prompted
3. Wait for build to complete (should see "Running app")
4. **Observe:** Does the app launch?

### Expected Result
✅ **PASS:** App launches and you see:
- Splash screen or login screen
- No "Unfortunately app has stopped" error
- No FATAL EXCEPTION in logcat

❌ **FAIL:** App crashes or shows error dialog
- Check logcat for "FATAL EXCEPTION"
- Share the error stack trace

### If It Passes
```
🎉 FIX #1 VERIFIED!
Android Studio deployment works correctly.
```

### If It Fails
Fallback to CLI:
```powershell
.\gradlew installDebug
adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

## 🧪 TEST #2: Invoice Creation (8 minutes)

### Objective
Verify the app doesn't crash when creating a new invoice.

### Prerequisites
- App is running (from Test #1)
- You've logged in or completed onboarding
- You can see the main dashboard/home screen

### Steps

#### 2.1: Open Create Invoice Screen
1. Look for **"Create Invoice"** or **"+"** button
2. Tap it
3. Verify: You see an empty form with fields for:
   - Customer name (dropdown or text)
   - Amount
   - Items
   - Date
   - Other details

#### 2.2: Fill in Invoice Details
```
Customer:     [Select any customer, or create one if needed]
Amount:       $100.00 (or any amount)
Description:  Test invoice for verification
Date:         [Today's date - should auto-fill]
```

**Minimum required fields** (you'll see validation messages if something is missing):
- [ ] Customer selected
- [ ] At least one line item with description + amount
- [ ] Total amount > 0

#### 2.3: Save Invoice
1. Look for **"Save"**, **"Create"**, or **"Submit"** button
2. Tap it
3. **Observe:** Does the app crash?

### Expected Result
✅ **PASS:** You see:
- Success message ("Invoice created", "Invoice saved", etc.)
- Invoice appears in list
- Dialog closes and you're back to list/dashboard
- **No crash** ✅

❌ **FAIL:** One of these happens:
- "Unfortunately app has stopped" crash dialog
- Toast/Snackbar with error message
- App freezes for 5+ seconds
- FATAL EXCEPTION in logcat

### If It Passes
```
🎉 FIX #2 VERIFIED!
Invoice creation works correctly.
```

### If It Fails
Capture logs:
```powershell
adb logcat -d > crash_logs.txt
# Open crash_logs.txt and look for:
# - java.lang.Exception
# - java.lang.NullPointerException
# - Caused by:
```

---

## 📊 Test Results Template

Copy this and fill it out:

```
TEST DATE: [Date]
TESTER: [Your name]
BUILD: [Build number - see Android Studio bottom]
APK SIZE: [About 36 MB expected]

TEST #1: ANDROID STUDIO DEPLOYMENT
Status: ✅ PASS / ❌ FAIL
Notes: [Any observations]
Crash Details: [If failed, paste error message]

TEST #2: INVOICE CREATION
Status: ✅ PASS / ❌ FAIL
Notes: [Any observations]
Crash Details: [If failed, paste error message]

OVERALL RESULT: ✅ ALL PASS / ⚠️ PARTIAL / ❌ BROKEN
```

---

## 🐛 Troubleshooting

### Issue: "App keeps crashing on startup"
**Solution:**
```powershell
adb uninstall com.emul8r.bizap
.\gradlew installDebug
adb shell am start -n com.emul8r.bizap/.MainActivity
```

### Issue: "Can't see Create Invoice button"
**Possible Causes:**
- You're on a different screen (check navigation)
- No customers exist yet (create one first)

**Solution:**
- Look for bottom navigation, hamburger menu, or floating action button
- Check "Invoices" or "New" tabs
- If stuck, share screenshots

### Issue: "Getting validation errors on save"
**This is GOOD** - it means error handling works!

Fill in the missing fields:
- Missing customer?
- Missing items?
- Zero amount?

### Issue: "Still seeing crashes"
**Debug steps:**
1. Get full logcat:
   ```powershell
   adb logcat -d > full_logs.txt
   ```
2. Search for "FATAL EXCEPTION"
3. Copy entire stack trace (10-20 lines after FATAL EXCEPTION)
4. Share in Slack or report

---

## ✨ Success Criteria

### Minimum (Both fixes work)
- ✅ App launches from Studio without crashing
- ✅ Invoice can be created without crashing
- ✅ No "Unfortunately app has stopped" dialogs

### Ideal (All tests pass)
- ✅ All of above
- ✅ Invoice appears in list immediately after save
- ✅ No error messages
- ✅ Logcat shows clean startup (no ERROR or WARN)

---

## 📞 Need Help?

If anything fails:
1. Capture the **exact error message**
2. Run: `adb logcat -d > debug_logs.txt`
3. Share both the error and the logs

---

## ⏱️ Time Breakdown

| Phase | Time |
|-------|------|
| Invalidate Cache + Clean Build | 5 min |
| Test #1 (Studio Deployment) | 7 min |
| Test #2 (Invoice Creation) | 8 min |
| **Total** | **~20 min** |

---

**Status: Ready to verify! 🚀**


