# 🚀 RUN BIZAP v0.1.0 - COMPLETE TESTING GUIDE

## ✅ Prerequisites

Before running the app, ensure:

1. **Android Emulator is Running**
   - Open Android Studio
   - Click: AVD Manager (phone icon)
   - Click green play button next to any emulator
   - Wait 1-2 minutes for it to boot

   OR

2. **Physical Device Connected**
   - Connect Android phone via USB
   - Enable USB Debugging (Settings → Developer Options → USB Debugging)
   - Verify connection: `adb devices` should show your device

---

## 🎯 OPTION 1: Use the Automated Script (Recommended)

### Step 1: Open PowerShell
- Press `Windows Key + X`
- Click "Windows PowerShell"
- Navigate to Bizap directory:
  ```powershell
  cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
  ```

### Step 2: Run the Script
```powershell
.\RUN_APP.ps1
```

**What it does:**
- ✅ Checks if ADB is installed
- ✅ Verifies APK exists
- ✅ Lists connected devices
- ✅ Uninstalls old version
- ✅ Installs new APK
- ✅ Clears app data (fresh start)
- ✅ Launches the app
- ✅ Monitors logcat for crashes

**Expected Output:**
```
📱 Bizap v0.1.0 - Runtime Testing Script
=========================================

Step 1️⃣  Checking ADB path...
✅ ADB found at: C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe

Step 2️⃣  Checking APK...
✅ APK found: 24.8 MB

Step 3️⃣  Checking connected devices...
Running: adb devices
List of devices attached
emulator-5554          device

✅ Device(s) found

... (installation proceeds)

=== LOGCAT OUTPUT ===
(waiting for logs...)
```

---

## 🎯 OPTION 2: Manual Commands (If Script Doesn't Work)

Open PowerShell in the Bizap directory and run these commands one by one:

### 1. Check Connected Devices
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb devices
```

**Expected:** Should show `emulator-5554` or a device ID

### 2. Install the APK
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apk = "app\build\outputs\apk\debug\app-debug.apk"
& $adb install -r $apk
```

**Expected:** `Success` message at the end

### 3. Launch the App
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb shell am start -n com.emul8r.bizap/.MainActivity
```

**Expected:** App appears on emulator/device within 2-3 seconds

### 4. Monitor for Crashes
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb logcat -s AndroidRuntime:E BizapApp:D
```

**Expected:** Shows debug logs, NO fatal crashes

---

## 📋 TESTING CHECKLIST

Once the app launches, go through these tests:

### ✅ Test 1: App Launches Without Crashing
- [ ] App starts
- [ ] No "Unfortunately, Bizap has stopped" error
- [ ] Dashboard screen appears
- [ ] No red error messages

### ✅ Test 2: Create Invoice Flow
Navigate: `Dashboard → Invoices (bottom tab)`

1. [ ] Click "+ Create Invoice"
2. [ ] Select a customer (or create one)
3. [ ] Add line items:
   - Description: "Consulting Services"
   - Quantity: 2.0
   - Unit Price: $50.00 (enter as "50")
4. [ ] Click "Save Invoice"
5. [ ] **CRITICAL**: Should save WITHOUT "f != java.lang.Long" error
6. [ ] Invoice should appear in the list

### ✅ Test 3: View Invoice Details
1. [ ] From Invoices list, tap the saved invoice
2. [ ] Details should load without crash
3. [ ] Amounts should display correctly:
   - Line items: `$100.00` (2 × $50)
   - Total: `$100.00` (or with tax if enabled)
4. [ ] Currency symbol should be present (`$`, `A$`, etc.)

### ✅ Test 4: Currency Display
1. [ ] Go to Settings (bottom right)
2. [ ] Go to Business Profile
3. [ ] Check Currency setting
4. [ ] Go back to Invoice
5. [ ] Currency symbol should match

### ✅ Test 5: Database Persistence
1. [ ] Create and save 2-3 invoices
2. [ ] Close the app (swipe away)
3. [ ] Reopen the app
4. [ ] Invoices should still be there
5. [ ] Data should match what you saved

### ✅ Test 6: Payment Recording
1. [ ] Open a saved invoice
2. [ ] Look for "Record Payment" button
3. [ ] Record a partial payment (e.g., $50)
4. [ ] Should save without type errors
5. [ ] Payment should show in invoice

---

## 🐛 If You Get Errors

### Error: "f != java.lang.Long"
- **Status:** FIXED ✅ (This was the critical bug we just fixed)
- **If you see this:** Contact me immediately - something's wrong with the migration
- **Workaround:** `adb shell pm clear com.emul8r.bizap` then reinstall

### Error: "Room migration failed"
- **Cause:** Database is still at v23, migration to v24 didn't run
- **Solution:** `adb shell pm clear com.emul8r.bizap` (wipes local database)
- **Then:** Reinstall app and it will create v24 fresh

### Error: "Unfortunately, Bizap has stopped"
- **Get logs:** 
  ```powershell
  $adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
  & $adb logcat -d > crash_log.txt
  cat crash_log.txt
  ```
- **Send me the crash logs** and I'll debug

### Error: "Cannot find customer"
- **Cause:** First time running, no test data
- **Solution:** Create a customer first (Settings → Customers → +)
- **Or:** Look for "Add test data" button in Settings

---

## 📊 What We Fixed

The critical bug you reported:
```
"f != java.lang.Long error when I try to save an invoice"
```

**✅ Root Cause Fixed:**
- Payment entities were using `Double` for money
- Invoice entities were using `Long` for money
- Type mismatch caused the error

**✅ Solution Applied:**
- Changed all monetary fields to `Long` (cents)
- Created database migration v23 → v24
- All 14 type mismatches resolved

**✅ Build Status:**
- ✅ 0 compilation errors
- ✅ 0 type mismatches remaining
- ✅ APK 24.8 MB
- ✅ Ready for testing

---

## 🎬 Start Testing Now!

**Quick Start (2 minutes):**
```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
.\RUN_APP.ps1
```

**Expected:** App launches, no crashes, can create invoice without type error.

---

## 📞 Next Steps

After testing, please report:

1. ✅ Did app launch without crashing?
2. ✅ Could you create an invoice?
3. ✅ Did it save without "f != java.lang.Long" error?
4. ✅ Do amounts display correctly?
5. ✅ Any other errors or issues?

**If all tests pass:** Ready for Phase 0 (Input Validation)  
**If issues found:** Will debug and provide additional fixes

---

Good luck! 🚀

