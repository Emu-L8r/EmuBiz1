# 🧪 BIZAP ERROR TESTING GUIDE - COMPREHENSIVE

**Date:** March 5, 2026  
**Purpose:** Systematic error testing and app review  
**Status:** Ready for testing  

---

## 📋 PRE-TESTING SETUP

### Step 1: Verify APK Ready ✅
```
File: app/build/outputs/apk/debug/app-debug.apk
Size: 24.8 MB
Status: ✅ Built and ready
```

### Step 2: Device Connection
```bash
# Check for connected devices
adb devices

# Expected output:
# List of attached devices
# emulator-5554    device  (if using emulator)
# OR your-device    device  (if using physical device)
```

### Step 3: Install APK
```bash
# Install the app
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Expected: Success (or reinstalled if already present)
```

### Step 4: Launch App
```bash
# Start the app
adb shell am start -n com.emul8r.bizap/.MainActivity

# Expected: App opens on device/emulator
```

### Step 5: Enable Logging
```bash
# Monitor app logs for errors
adb logcat -s BizapApp:D

# This will show all debug logs from the app
```

---

## 🧪 ERROR TEST CASES

### TEST 1: Empty Invoice Creation
**Purpose:** Test validation with no data

**Steps:**
1. Launch app
2. Navigate to "Create Invoice" screen
3. Click "Save Invoice" button WITHOUT entering any data
4. **Expected:** Validation error message (e.g., "Please add at least one line item")

**Check logs for:**
```
BizapApp: Validation error - no items
BizapApp: Invoice validation failed
```

---

### TEST 2: Missing Customer Information
**Purpose:** Test customer validation

**Steps:**
1. Create invoice
2. Add line items (skip customer)
3. Try to save
4. **Expected:** Error: "Customer information required"

**What should happen:**
- Cannot save without customer
- Error message displays clearly
- Fields highlight which are missing

---

### TEST 3: Invalid Currency Code
**Purpose:** Test currency validation

**Steps:**
1. Create invoice with valid customer and items
2. Try to set currency to invalid code (e.g., "XXX")
3. **Expected:** Error or validation rejection

**Check logs for:**
```
BizapApp: Invalid currency: XXX
BizapApp: Currency validation failed
```

---

### TEST 4: Zero or Negative Amounts
**Purpose:** Test amount validation

**Steps:**
1. Add line item with:
   - Quantity: 0 or negative
   - OR Unit Price: 0 or negative
2. Try to save
3. **Expected:** Validation error

**Expected errors:**
```
"Quantity must be greater than 0"
"Unit price must be greater than 0"
"Total amount must be positive"
```

---

### TEST 5: Extremely Large Numbers
**Purpose:** Test overflow/boundary conditions

**Steps:**
1. Create invoice with:
   - Quantity: 999999
   - Unit Price: 999999
   - Total: Should exceed reasonable limit
2. Try to save
3. **Expected:** Either validation error or graceful handling

**Check logs:**
```
BizapApp: Amount exceeds maximum: XXX
BizapApp: Validation failed - amount too large
```

---

### TEST 6: Special Characters in Names
**Purpose:** Test input sanitization

**Steps:**
1. Add customer with name containing:
   - SQL injection: `'; DROP TABLE--`
   - Special chars: `<script>alert('xss')</script>`
   - Unicode: `你好世界`
2. Save invoice
3. **Expected:** Data saved safely (no execution/injection)

**Check logs:**
```
BizapApp: Customer name: [properly escaped]
BizapApp: Database insert successful
```

---

### TEST 7: Rapid Save Clicks
**Purpose:** Test concurrency/race conditions

**Steps:**
1. Create invoice with valid data
2. Click "Save" button multiple times rapidly
3. **Expected:** Only one invoice created (or error on duplicates)

**Check logs:**
```
BizapApp: Duplicate save prevented
BizapApp: Invoice saved once (no duplicates)
```

---

### TEST 8: Missing Required Fields
**Purpose:** Test field validation

**Steps:**
1. Try to create invoice with:
   - No customer name
   - No line items
   - No dates
2. Try each alone and in combination
3. **Expected:** Clear error messages for each

**Expected messages:**
```
"Customer name is required"
"At least one line item required"
"Invoice date required"
```

---

### TEST 9: Database Persistence
**Purpose:** Test data actually saves

**Steps:**
1. Create and save an invoice with unique data
2. Close app completely
3. Reopen app
4. **Expected:** Invoice still appears in list

**Check logs:**
```
BizapApp: Loading invoices from database
BizapApp: Found X invoices
BizapApp: Invoice display complete
```

---

### TEST 10: Network Errors (if applicable)
**Purpose:** Test exchange rate API error handling

**Steps:**
1. Turn off WiFi/mobile data
2. Create invoice
3. Try to fetch exchange rates
4. **Expected:** Graceful error or cached rates

**Check logs:**
```
BizapApp: Network error fetching rates
BizapApp: Using cached exchange rates
BizapApp: Exchange rate fetch failed - using default
```

---

## 🔍 VALIDATION ERROR MESSAGES TO LOOK FOR

### Expected Error Messages
```
✅ "Please add at least one line item"
✅ "Customer information is required"
✅ "Invoice date cannot be in the future"
✅ "Due date must be after invoice date"
✅ "Quantity must be greater than 0"
✅ "Unit price must be greater than 0"
✅ "Total cannot exceed $999,999"
✅ "Invalid currency code"
✅ "Email format is invalid"
✅ "Phone number format is invalid"
```

### Error Types to Check For
```
✅ Validation errors (user input invalid)
✅ Database errors (storage failures)
✅ Network errors (API failures)
✅ Permission errors (file system access)
✅ Crash errors (app exceptions)
```

---

## 📊 TEST RESULTS TRACKING

### Create a Results Log

**Template:**
```
Test Case: [Name]
Date: March 5, 2026
Status: [PASS/FAIL]
Expected: [What should happen]
Actual: [What did happen]
Error Message: [Any error shown]
Logs: [Relevant log entries]
Notes: [Additional observations]
```

### Example Results
```
Test Case: Empty Invoice Creation
Status: PASS ✅
Expected: Validation error message
Actual: "Please add at least one line item" displayed
Error Message: Correct validation error shown
Notes: Error message is clear and helpful
```

---

## 🔧 COMMAND CHEAT SHEET

### Installation
```bash
# Install/reinstall APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Uninstall before fresh install
adb uninstall com.emul8r.bizap
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Running & Monitoring
```bash
# Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity

# View logs
adb logcat -s BizapApp:D

# View errors only
adb logcat -s BizapApp:E

# Save logs to file
adb logcat -s BizapApp:D > bizap_logs.txt
```

### Device Management
```bash
# List devices
adb devices

# Clear app data (resets everything)
adb shell pm clear com.emul8r.bizap

# Stop app
adb shell am force-stop com.emul8r.bizap

# Check app info
adb shell pm dump com.emul8r.bizap | grep -i "versionName"
```

---

## ✅ TESTING CHECKLIST

### Pre-Test
- [ ] APK built successfully
- [ ] Device/emulator connected
- [ ] ADB functioning
- [ ] Logging enabled

### Core Functionality
- [ ] App launches without crash
- [ ] Main screen displays
- [ ] Navigation works
- [ ] Create invoice screen accessible

### Validation Tests
- [ ] Empty fields show errors
- [ ] Required fields enforced
- [ ] Number validation works
- [ ] Currency validation works

### Data Persistence
- [ ] Data saves to database
- [ ] Data survives app restart
- [ ] Multiple invoices stored
- [ ] Correct data retrieved

### Error Handling
- [ ] Validation errors clear
- [ ] Error messages helpful
- [ ] No uncaught exceptions
- [ ] Graceful failure modes

### UI/UX
- [ ] Text is readable
- [ ] Buttons are clickable
- [ ] No layout issues
- [ ] Navigation intuitive

---

## 📸 SCREENSHOTS TO CAPTURE

For documentation, capture screenshots of:
1. Main app screen (empty state)
2. Create invoice screen
3. Add customer form
4. Add line item form
5. Invoice list view
6. Validation error messages
7. Saved invoice view

---

## 🚨 CRITICAL ERRORS TO WATCH FOR

### App Crashes
```
❌ App force closes
❌ Unresponsive UI
❌ Blank black screen
❌ ANR (Application Not Responding)
```

### Data Loss
```
❌ Data not saving
❌ Data disappears after restart
❌ Corrupted database entries
❌ Lost customer information
```

### Validation Bypass
```
❌ Saving empty invoices
❌ Accepting invalid amounts
❌ Missing error messages
❌ Bypassing required fields
```

---

## 📝 LOGGING GUIDE

### Key Log Messages to Monitor

```
// Successful operations
BizapApp: Invoice created: [invoice-id]
BizapApp: Customer saved: [name]
BizapApp: Validation passed
BizapApp: Database insert successful

// Validation failures
BizapApp: Validation error - [reason]
BizapApp: Invalid customer data
BizapApp: Invalid line items
BizapApp: Currency not found

// Database operations
BizapApp: Loading invoices...
BizapApp: Query returned X results
BizapApp: Database transaction complete
BizapApp: Update successful

// Errors
BizapApp: ERROR - [error description]
BizapApp: Exception: [exception type]
BizapApp: Database error: [error]
```

---

## 🎯 TESTING PRIORITIES

### Priority 1 (Must Pass)
1. App launches without crash
2. Can create invoice
3. Can save invoice
4. Can retrieve saved invoice
5. Validation errors show

### Priority 2 (Should Pass)
1. Field-by-field validation
2. Boundary value testing
3. Multiple data persistence
4. Graceful error handling
5. Clear error messages

### Priority 3 (Nice to Have)
1. Performance testing
2. Stress testing (lots of data)
3. Network error resilience
4. Concurrent operations
5. Edge cases

---

## 🔗 RESOURCES

### Documentation
- `README.md` - Setup guide
- `CLASSLOADER_FIX_SUMMARY.md` - Technical details
- `VALIDATION_IMPLEMENTATION_SUMMARY.md` - Validation rules

### Code References
- `ValidationRules.kt` - Validation logic (350+ lines)
- `Result.kt` - Error handling (260 lines)
- `MainActivity.kt` - UI entry point

---

## 📞 NEXT STEPS

1. **Install APK** on device/emulator
2. **Run through test cases** in sequence
3. **Capture results** in this checklist
4. **Save logs** from logcat
5. **Document findings** in error report
6. **Fix any issues** found
7. **Re-test** fixes
8. **Sign off** when complete

---

**Ready to start testing? Let's begin!** 🧪


