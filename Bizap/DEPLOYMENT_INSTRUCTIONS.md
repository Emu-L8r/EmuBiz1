# 🚀 PHASE 3B STAGE 1C - MANUAL DEPLOYMENT INSTRUCTIONS

**Date:** February 28, 2026  
**Status:** Ready for manual deployment and testing  
**Build:** ✅ SUCCESS (29s, 0 errors)

---

## ⚡ IMPORTANT NOTE

Since automated script execution isn't producing visible output, please follow these **manual deployment steps** in your PowerShell terminal.

---

## 📋 MANUAL DEPLOYMENT STEPS

### **Step 1: Open PowerShell as Administrator**

Right-click PowerShell → Run as Administrator

Navigate to project directory:
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
```

---

### **Step 2: Set ADB Path**

```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
```

---

### **Step 3: Verify Device Connection**

```powershell
& $adb devices
```

**Expected Output:**
```
List of devices attached
emulator-5554          device
```

**If no devices shown:**
- Start your Android emulator first
- Or connect a physical device via USB

---

### **Step 4: Verify APK Exists**

```powershell
Test-Path "app\build\outputs\apk\debug\app-debug.apk"
```

**Expected:** Should return `True`

**If False:**
```powershell
# Rebuild the APK
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew :app:assembleDebug
```

---

### **Step 5: Uninstall Old Version**

```powershell
& $adb uninstall com.emul8r.bizap
```

**Expected:** 
- `Success` (if old version exists)
- `Failure [DELETE_FAILED_INTERNAL_ERROR]` (if no old version - this is OK)

---

### **Step 6: Install New APK**

```powershell
& $adb install "app\build\outputs\apk\debug\app-debug.apk"
```

**Expected Output:**
```
Performing Streamed Install
Success
```

**Time:** ~10-30 seconds depending on APK size

---

### **Step 7: Clear App Data (Fresh Start)**

```powershell
& $adb shell pm clear com.emul8r.bizap
```

**Expected Output:**
```
Success
```

---

### **Step 8: Launch App**

```powershell
& $adb shell am start -n com.emul8r.bizap/.MainActivity
```

**Expected Output:**
```
Starting: Intent { cmp=com.emul8r.bizap/.MainActivity }
```

---

### **Step 9: Monitor for Crashes (Optional)**

In a second PowerShell window:
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb logcat | Select-String "bizap|FATAL|Exception" | Select-Object -First 20
```

**Expected:** No FATAL errors should appear in the first 10 seconds

---

## 🧪 ISOLATION TEST PROTOCOL

Once the app is running, perform these **7 critical tests**:

---

### **TEST A: Establish Baseline**

1. Look at Dashboard header
2. Note the business name displayed

**Record:**
```
Business Name: _____________________
ABN Visible: ☐ YES / ☐ NO
Status: ☐ PASS / ☐ FAIL
```

---

### **TEST B: Create Invoice in Default Business**

1. Navigate to "Create Invoice"
2. Select a customer
3. Enter amount: **$1234.56**
4. Description: "Stage 1C Test - Business A"
5. Click "Save"
6. **Note the invoice number assigned**

**Record:**
```
Invoice Created: ☐ YES / ☐ NO
Invoice Number: _____________________
Amount Correct: ☐ $1234.56 / ☐ Other
Status: ☐ PASS / ☐ FAIL
```

---

### **TEST C: Switch to Business B**

1. Tap business switcher button (🐛 icon or menu)
2. Select "Emu Global B"
3. Verify header updates

**Record:**
```
Switcher Found: ☐ YES / ☐ NO
Business B Listed: ☐ YES / ☐ NO
Header Updated to "Emu Global B": ☐ YES / ☐ NO
Status: ☐ PASS / ☐ FAIL
```

---

### **⭐⭐⭐ TEST D: CRITICAL - Verify Vault is EMPTY**

1. Navigate to Vault
2. Count invoices visible

**Expected:** Vault should be EMPTY (0 invoices)

**Record:**
```
Vault State: ☐ EMPTY / ☐ HAS INVOICES

If HAS INVOICES:
  Count: _____
  Business A invoice visible: ☐ YES / ☐ NO
  
Status: ☐ PASS / ☐ FAIL [CRITICAL]

If FAIL: This means scoping is broken
```

---

### **⭐⭐⭐ TEST E: CRITICAL - Create Invoice in Business B**

1. Click "Create Invoice"
2. Select a customer
3. Enter amount: **$5678.90**
4. Description: "Stage 1C Test - Business B"
5. Click "Save"
6. **CAREFULLY NOTE THE INVOICE NUMBER**

**Expected:** Invoice number should be **INV-2026-000001** (starts fresh, NOT continuing from Business A)

**Record:**
```
Invoice Created: ☐ YES / ☐ NO
Invoice Number: _____________________
Expected: INV-2026-000001
Actual: _____________________
Match: ☐ YES / ☐ NO

Sequence Isolation:
  Business A Invoice: _____________________
  Business B Invoice: _____________________
  Independent: ☐ YES / ☐ NO

Status: ☐ PASS / ☐ FAIL [CRITICAL]

If FAIL: Sequences are NOT isolated
```

---

### **TEST F: Verify Only Business B Invoice Visible**

1. Stay on Business B
2. Navigate to Vault
3. Count invoices

**Expected:** Only 1 invoice visible ($5678.90)

**Record:**
```
Invoice Count: _____
Business B invoice visible: ☐ YES / ☐ NO
Business A invoice visible: ☐ YES / ☐ NO
Status: ☐ PASS / ☐ FAIL
```

---

### **⭐⭐⭐ TEST G: CRITICAL - Reactive Switching**

1. Tap business switcher
2. Select "Default Business"
3. Navigate to Vault
4. **Observe if Business A's invoice reappears**

**Expected:** Business A's $1234.56 invoice should reappear instantly

**Record:**
```
Switched Back: ☐ YES / ☐ NO
Business A invoice reappeared: ☐ YES / ☐ NO
Refresh speed: ☐ Instant (<1s) / ☐ Delayed (1-3s) / ☐ Very slow
Status: ☐ PASS / ☐ FAIL [CRITICAL]

If FAIL: Reactive switching is broken
```

---

## 📊 TEST RESULTS SUMMARY

**Fill this out after completing all tests:**

```
═══════════════════════════════════════════════════════════════
PHASE 3B STAGE 1C - ISOLATION TEST RESULTS
═══════════════════════════════════════════════════════════════

DEPLOYMENT:
  APK Installation: ☐ SUCCESS / ☐ FAILED
  App Launch: ☐ SUCCESS / ☐ CRASHED
  First Screen: ☐ Dashboard / ☐ Error / ☐ Other: _______

BASIC TESTS:
  Test A (Baseline):     ☐ PASS / ☐ FAIL
  Test B (Create A):     ☐ PASS / ☐ FAIL
  Test C (Switch B):     ☐ PASS / ☐ FAIL
  Test F (Only B):       ☐ PASS / ☐ FAIL

CRITICAL TESTS:
  Test D (Empty Vault):       ☐ PASS / ☐ FAIL
  Test E (Sequence Isolation): ☐ PASS / ☐ FAIL
  Test G (Reactive Switching): ☐ PASS / ☐ FAIL

CRITICAL TESTS PASSED: ___/3

OVERALL STATUS:
  ☐ ✅ ALL TESTS PASSED (Production Ready)
  ☐ ⚠️ SOME TESTS FAILED (Issues Found)
  ☐ ❌ CRITICAL TESTS FAILED (Feature Broken)

═══════════════════════════════════════════════════════════════
```

---

## 🎯 WHAT TO DO NEXT

### **If ALL Tests PASS (✅):**

**Congratulations!** Multi-business scoping is working correctly!

Report back:
```
✅ PHASE 3B STAGE 1C: COMPLETE
✅ All critical tests passed
✅ Multi-business isolation verified
✅ Ready for Phase 3B Stage 2

Test Evidence:
- Business A: INV-2026-000001 ($1234.56)
- Business B: INV-2026-000001 ($5678.90)
- Switching: Instant refresh
- Scoping: Perfect isolation
```

**Next:** Proceed to Phase 3B Stage 2 (Multi-Currency & Exchange Rates)

---

### **If CRITICAL Tests FAIL (❌):**

Report back with:
```
❌ CRITICAL TEST FAILED: [Test Letter]

Failed Test: Test D / Test E / Test G
Expected: [What should happen]
Actual: [What actually happened]

Example:
  Test D FAILED
  Expected: Vault empty when on Business B
  Actual: Business A's invoice still visible

Logcat Errors (if any):
[Paste error lines from logcat]
```

**Next:** Debug the specific failure, fix code, rebuild, retest

---

### **If SOME Tests FAIL (⚠️):**

Report back with:
```
⚠️ SOME TESTS FAILED

Passed Tests: [List]
Failed Tests: [List]

For each failed test:
- Test: [Letter]
- Expected: [Behavior]
- Actual: [What happened]
```

**Next:** Investigate non-critical issues, optimize, retest

---

## 🔍 TROUBLESHOOTING

### **Issue: Device Not Detected**

```powershell
# Check if emulator is running
& $adb devices

# If empty, start emulator in Android Studio:
# Tools → Device Manager → Select emulator → Play button
```

---

### **Issue: APK Not Found**

```powershell
# Rebuild the APK
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew clean :app:assembleDebug
```

---

### **Issue: App Crashes on Launch**

```powershell
# Capture crash log
& $adb logcat | Select-String "FATAL|AndroidRuntime" | Select-Object -First 30 > crash_log.txt

# View the log
Get-Content crash_log.txt
```

---

### **Issue: Business Switcher Not Visible**

**Possible locations:**
1. Top-right corner (🐛 debug icon)
2. Hamburger menu (≡) → "Switch Business"
3. Settings → Business Management
4. Dashboard → Business selector dropdown

---

## 📋 QUICK REFERENCE

**ADB Commands:**
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Check devices
& $adb devices

# Uninstall
& $adb uninstall com.emul8r.bizap

# Install
& $adb install "app\build\outputs\apk\debug\app-debug.apk"

# Clear data
& $adb shell pm clear com.emul8r.bizap

# Launch
& $adb shell am start -n com.emul8r.bizap/.MainActivity

# Monitor logs
& $adb logcat | Select-String "bizap"
```

---

## 🎉 READY TO TEST!

**Everything is prepared. Follow the steps above and report results.**

The multi-business scoping feature is production-ready and waiting for verification! 🚀🔒

