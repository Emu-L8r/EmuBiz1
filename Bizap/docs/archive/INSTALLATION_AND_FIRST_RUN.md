# 🚀 BIZAP - INSTALLATION & FIRST RUN GUIDE

**Date:** March 5, 2026  
**Purpose:** Get the app running for testing  
**Status:** Ready to deploy

---

## 📋 PRE-INSTALLATION CHECKLIST

### Required
- [ ] Android SDK 35 installed
- [ ] ADB (Android Debug Bridge) installed
- [ ] Device or emulator available and connected
- [ ] APK file: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] USB debugging enabled (if physical device)

### Optional
- [ ] Android Studio (for logcat viewing)
- [ ] Terminal/Command prompt
- [ ] ~100MB free storage on device

---

## 🔧 STEP 1: VERIFY DEVICE CONNECTION

### Check if Device is Connected
```bash
# List connected devices
adb devices

# Expected output example:
# List of attached devices
# emulator-5554          device
# OR
# SM-G991B               device
```

**Troubleshooting:**
- If no devices listed:
  - [ ] Physical device: Enable USB debugging
    - Settings → Developer Options → USB Debugging
  - [ ] Emulator: Start Android emulator from Android Studio
  - [ ] Reconnect USB cable
  - [ ] Install ADB drivers if needed

### Verify ADB Works
```bash
# Test ADB connection
adb shell echo "ADB is working"

# Expected: "ADB is working"
```

---

## 📦 STEP 2: INSTALL APK

### Option A: Clean Install
```bash
# First uninstall (if already installed)
adb uninstall com.emul8r.bizap

# Then install fresh
adb install C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk

# Expected output:
# Success
```

### Option B: Reinstall (Keep Data)
```bash
# Reinstall over existing version
adb install -r C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk

# -r flag: Reinstall even if package exists
# Expected output:
# Success
```

### Installation Issues?

**Error: "device unauthorized"**
```
Solution:
1. Check device - there should be authorization prompt
2. Tap "OK" to authorize computer
3. Try adb install again
```

**Error: "cannot install on this device"**
```
Solution:
1. Verify Android version: Settings → About → Android version
2. Should be Android 8.0 (API 26) or higher
3. Device compile SDK must match (API 35)
```

**Error: "insufficient storage"**
```
Solution:
1. Free up ~100MB of storage on device
2. Uninstall unused apps if needed
3. Clear app cache: Settings → Apps → [App] → Storage → Clear Cache
```

---

## 🚀 STEP 3: LAUNCH THE APP

### Start the App
```bash
# Launch the app
adb shell am start -n com.emul8r.bizap/.MainActivity

# Expected:
# Activity launched successfully
# (App appears on device screen)
```

### What You Should See
```
✅ Bizap app icon on home screen
✅ App opens
✅ Main dashboard displays
✅ No crash messages
✅ Navigation buttons visible
```

### Troubleshooting First Launch

**App crashes immediately:**
```
Solution:
1. Check logcat: adb logcat -s BizapApp:E
2. Look for exception or error message
3. Review CRASH_LOG.txt for details
```

**App opens but blank screen:**
```
Solution:
1. Wait 2-3 seconds (might be initializing)
2. Force close: adb shell am force-stop com.emul8r.bizap
3. Clear data: adb shell pm clear com.emul8r.bizap
4. Reinstall app
5. Check logs for initialization errors
```

**App opens slowly:**
```
Solution:
1. This might be normal for first run
2. Allow 5-10 seconds for database initialization
3. Subsequent launches should be faster
4. Check device memory: should have >1GB free RAM
```

---

## 📊 STEP 4: ENABLE LOGGING

### Setup Log Monitoring
```bash
# View app logs in real-time
adb logcat -s BizapApp:D

# This shows:
# ✅ Debug messages
# ✅ Normal operations
# ✅ Warnings
# ✅ Errors
```

### Log Output Examples

**Successful startup:**
```
BizapApp: App starting...
BizapApp: Database initialized
BizapApp: Loading invoices...
BizapApp: Found 0 invoices
BizapApp: UI ready
```

**Validation error:**
```
BizapApp: Validation error - no items
BizapApp: Cannot save empty invoice
BizapApp: Showing error message to user
```

**Database operation:**
```
BizapApp: Saving invoice...
BizapApp: Database insert successful
BizapApp: Invoice ID: 12345
BizapApp: Notification sent to UI
```

### Save Logs to File
```bash
# Capture logs to file
adb logcat -s BizapApp:D > bizap_testing.log

# Log file location: bizap_testing.log in current directory
# Use for later analysis
```

---

## ✅ STEP 5: FIRST RUN CHECKLIST

### Basic Functionality
- [ ] App launches without crash
- [ ] Main screen displays correctly
- [ ] All buttons are visible
- [ ] Text is readable
- [ ] No graphical glitches

### Navigation
- [ ] Can tap "Create Invoice" button
- [ ] Create screen opens
- [ ] Can navigate back
- [ ] Can access menu (if present)
- [ ] Can view saved invoices (even if empty)

### Data Entry
- [ ] Can type in text fields
- [ ] Keyboard appears for text input
- [ ] Number fields only accept numbers
- [ ] Date picker works
- [ ] Dropdown menus function

### First Invoice Creation
```
Step 1: Create Invoice
  - Click "New Invoice" or "Create Invoice"
  - Verify form loads

Step 2: Add Customer
  - Enter customer name: "Test Customer"
  - Enter email: "test@example.com"
  - Click "Add" or "Save"

Step 3: Add Line Item
  - Description: "Test Product"
  - Quantity: 1
  - Unit Price: 10.00
  - Click "Add Item"

Step 4: Review & Save
  - Verify all data is present
  - Verify total calculated correctly
  - Click "Save Invoice"

Step 5: Verify Success
  - Success message appears
  - Invoice appears in list
  - No errors in logcat
```

---

## 🔍 STEP 6: VERIFY DATA PERSISTENCE

### Test Data Survives App Restart
```bash
# 1. Create and save an invoice with unique data
#    (e.g., customer "Test User 12345")

# 2. Close the app
adb shell am force-stop com.emul8r.bizap

# 3. Relaunch the app
adb shell am start -n com.emul8r.bizap/.MainActivity

# 4. Check if invoice still exists
#    Expected: Yes, invoice is in list with same data
```

### What to Check
- [ ] Invoice still in list
- [ ] Customer name is correct
- [ ] Line items are preserved
- [ ] Total amount is correct
- [ ] Date information intact

### If Data is Missing
```
Troubleshooting:
1. Check logcat for database errors
2. Verify database permissions
3. Check app storage quota
4. Review DATA_LOSS_LOG.txt if created
```

---

## 🧪 QUICK TEST: HAPPY PATH

### Complete First Invoice (3-5 minutes)
```
START
├─ Open app
├─ Tap "Create Invoice"
├─ Customer: "Acme Corp"
├─ Email: "acme@example.com"
├─ Add Line Item:
│  ├─ Description: "Consulting"
│  ├─ Quantity: 5
│  └─ Unit Price: 50.00
├─ Review: Total should be 250.00
├─ Tap "Save"
├─ Success message appears
├─ Invoice in list
├─ Tap invoice to view detail
└─ END - Success ✅
```

---

## 🚨 COMMON ISSUES & SOLUTIONS

### Issue: "Activity not found"
```
Error: activity not found
Cause: App package name mismatch
Solution:
1. Verify package is: com.emul8r.bizap
2. Verify activity is: MainActivity
3. Correct command: adb shell am start -n com.emul8r.bizap/.MainActivity
```

### Issue: "Application has stopped"
```
Error: App force closes
Cause: Crash or exception
Solution:
1. Check logcat: adb logcat -s BizapApp:E
2. Look for: Exception, Error, NullPointerException
3. Document error and report
4. Reinstall app if persistent
```

### Issue: "App is running very slow"
```
Cause: Could be device, network, or app issue
Solution:
1. Wait for initial load (database init)
2. Check device storage (may be full)
3. Check device RAM (Device settings)
4. Close other apps
5. Restart emulator/device if virtual
```

### Issue: "Data not saving"
```
Cause: Database permission or storage issue
Solution:
1. Check logcat for database errors
2. Verify storage permissions: Settings → Apps → Permissions
3. Clear app data: adb shell pm clear com.emul8r.bizap
4. Reinstall app
5. Check available storage (>100MB needed)
```

---

## 📞 COMMAND REFERENCE

### Installation Commands
```bash
# Install APK
adb install [path-to-apk]

# Reinstall APK
adb install -r [path-to-apk]

# Uninstall app
adb uninstall com.emul8r.bizap
```

### Launching Commands
```bash
# Start app
adb shell am start -n com.emul8r.bizap/.MainActivity

# Stop app
adb shell am force-stop com.emul8r.bizap

# Clear app data (resets everything)
adb shell pm clear com.emul8r.bizap
```

### Monitoring Commands
```bash
# View logs
adb logcat -s BizapApp:D

# View errors only
adb logcat -s BizapApp:E

# Save logs to file
adb logcat -s BizapApp:D > logfile.txt

# Clear logcat
adb logcat -c
```

### Device Commands
```bash
# List devices
adb devices

# Get device info
adb shell pm dump com.emul8r.bizap

# Check Android version
adb shell getprop ro.build.version.sdk

# Get free storage
adb shell df /data
```

---

## ✅ SUCCESS CRITERIA

### Installation Success
- [ ] APK installs without errors
- [ ] Installation takes < 30 seconds
- [ ] No "Insufficient storage" error
- [ ] Package successfully installed message

### Launch Success
- [ ] App appears on device screen
- [ ] Main activity loads
- [ ] UI is visible and interactive
- [ ] No crash dialog appears

### Basic Operation Success
- [ ] Can create new invoice
- [ ] Can save data
- [ ] Data persists after restart
- [ ] Logs show normal operation

---

## 🎉 READY TO TEST

Once you complete all steps:
1. ✅ Device connected
2. ✅ APK installed
3. ✅ App launching
4. ✅ Logging enabled
5. ✅ First invoice created

**You're ready to begin error testing!** 🧪

---

## 📞 NEED HELP?

### Check These Files
- `ERROR_TESTING_GUIDE.md` - Error test cases
- `APP_REVIEW_GUIDE.md` - Full app review
- `README.md` - General setup
- Build logs in project root

### Emergency Reset
```bash
# Full fresh install
adb shell pm clear com.emul8r.bizap
adb uninstall com.emul8r.bizap
adb install C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

**Let's test the app!** 🚀


