# MANUAL REVIEW CHECKLIST - Bizap v0.1.0-stabilized
**Date**: March 3, 2026  
**APK Version**: 24.8 MB debug build  
**Tag**: v0.1.0-stabilized  

---

## 📱 DEVICE/EMULATOR CHECKLIST

### Prerequisites Before Starting Review
- [ ] Device/emulator is running
- [ ] APK has been installed (`./gradlew :app:installDebug`)
- [ ] App has been launched (`adb shell am start -n com.emul8r.bizap/.MainActivity`)
- [ ] Wait 5-10 seconds for app to fully load

---

## ✅ TEST 1: CURRENCY DISPLAY (Bug #11 + #12 - FIXED in PR #3)

**What to Test**: Currency formatting with correct symbol and decimal handling

### Steps:
1. **Navigate to Invoices tab** (bottom navigation)
   - [ ] Tab appears and responds to tap
   - [ ] Invoices list loads (may be empty)

2. **Create New Invoice**
   - [ ] Tap "Create" or "+" button
   - [ ] Invoice form opens
   - [ ] All fields visible (customer, items, totals)

3. **Add Line Item**
   - [ ] Tap "Add Item" button
   - [ ] Item form appears
   - [ ] Fill in:
     - Quantity: `2`
     - Unit Price: `49.99` (or similar)
   - [ ] Tap "Add" or "Save"

4. **Save Invoice**
   - [ ] Item appears in list with calculated amount
   - [ ] Total shows correctly: `99.98` (2 × 49.99)
   - [ ] Tap "Save" or "Done"

5. **View Invoice Detail**
   - [ ] Invoice appears in list
   - [ ] Tap to open detail view
   - [ ] **KEY CHECK**: Line item amount displays as:
     - ✅ **CORRECT**: `A$99.98` (or your currency symbol)
     - ❌ **BUGGY** (SHOULD BE FIXED): `$9998.00` (100x conversion error)

6. **Test Currency Change** (if supported)
   - [ ] Go to Settings
   - [ ] Change currency to USD or EUR
   - [ ] Go back to invoice
   - [ ] [ ] Currency symbol updates
   - [ ] [ ] Amounts recalculate correctly

**Expected Outcome**: ✅ Amounts display with correct symbol (A$, $, €, etc.) and proper decimal handling  
**Previous Bug** (FIXED): Would show 100x conversion (e.g., $9998.00 instead of $99.98)

---

## ✅ TEST 2: BUSINESS PROFILE REACTIVITY (Bug #7 - FIXED in PR #3)

**What to Test**: Real-time updates when profile changes without manual refresh

### Steps:
1. **Go to Settings**
   - [ ] Tap Settings tab (bottom navigation)
   - [ ] Settings screen loads

2. **Access Business Profile**
   - [ ] Find "Business Profile" or "Company Settings"
   - [ ] Tap to open
   - [ ] Current business info displays

3. **Edit Business Name**
   - [ ] Tap edit button or "Edit" option
   - [ ] Name field becomes editable
   - [ ] Change name to test value (e.g., "Test Company 2026")
   - [ ] Tap "Save" or "Done"

4. **Return to Dashboard WITHOUT Refresh**
   - [ ] Tap Dashboard tab (bottom navigation)
   - [ ] **KEY CHECK**: Business name updates immediately
   - [ ] ✅ **CORRECT**: Name shows new value without manual refresh
   - [ ] ❌ **BUGGY** (SHOULD BE FIXED): Name stays same until app is restarted

5. **Test Multi-Business Switch** (if supported)
   - [ ] Go back to Settings
   - [ ] If there's a business switcher, try switching
   - [ ] [ ] Dashboard data changes immediately
   - [ ] [ ] Profile info updates reactively

**Expected Outcome**: ✅ Profile changes appear immediately on Dashboard (reactive flow)  
**Previous Bug** (FIXED): Would require app restart or manual refresh to see changes

---

## ✅ TEST 3: PAYMENT PROGRESS BAR (Bug #11 sub-fix - FIXED in PR #3)

**What to Test**: Progress bar shows accurate payment percentage

### Steps:
1. **Open Invoice Detail**
   - [ ] Go to Invoices tab
   - [ ] Select an invoice (or create one from Test 1)
   - [ ] Invoice detail view opens

2. **Record Partial Payment**
   - [ ] Find "Record Payment" or "Add Payment" button
   - [ ] Tap to open payment form
   - [ ] Enter payment amount (e.g., 50% of total, or specific amount)
   - [ ] Tap "Save" or "Record"

3. **Verify Progress Bar**
   - [ ] Progress bar appears near total
   - [ ] **KEY CHECK**: Bar fill percentage matches payment
     - If you paid 50%: Bar shows ~50% full
     - If you paid 100%: Bar shows ~100% full (filled)
     - If you paid 25%: Bar shows ~25% full
   - [ ] ✅ **CORRECT**: Proportional fill
   - [ ] ❌ **BUGGY** (SHOULD BE FIXED): Stuck at 0% or always full

4. **Record Additional Payment**
   - [ ] Add another partial payment
   - [ ] [ ] Progress bar updates to new percentage
   - [ ] [ ] All previous payments counted

**Expected Outcome**: ✅ Progress bar shows proportional fill matching paid amount  
**Previous Bug** (FIXED): Would be stuck at 0% or 100% regardless of actual payment

---

## ✅ TEST 4: DOCUMENT VAULT (Feature - VERIFY)

**What to Test**: PDF generation and document viewing

### Steps:
1. **Open Invoice**
   - [ ] Go to Invoices tab
   - [ ] Select an invoice

2. **Generate PDF**
   - [ ] Find "Export", "Generate PDF", or "Print" button
   - [ ] Tap to generate
   - [ ] Loading indicator appears and completes
   - [ ] PDF viewer opens OR confirmation message

3. **Go to Document Vault**
   - [ ] Tap Vault tab (bottom navigation)
   - [ ] Vault screen loads
   - [ ] **KEY CHECK**: Generated PDF appears in list
   - [ ] [ ] Document shows thumbnail or filename
   - [ ] [ ] Tap document to preview

4. **Share Functionality** (if supported)
   - [ ] Long-press or tap menu on document
   - [ ] Find "Share" option
   - [ ] [ ] Share dialog appears
   - [ ] [ ] Can share via email, WhatsApp, etc.

**Expected Outcome**: ✅ PDFs generate and appear in Vault  
**Status**: No known bugs in this area

---

## ✅ TEST 5: GENERAL STABILITY & NAVIGATION

**What to Test**: App doesn't crash and navigation is smooth

### Complete Navigation Circuit:
1. **Dashboard Tab**
   - [ ] Loads successfully
   - [ ] Shows metrics/overview
   - [ ] No lag or freeze

2. **Customers Tab**
   - [ ] Loads successfully
   - [ ] Shows customer list (or empty state)
   - [ ] Can tap to view details
   - [ ] Back navigation works

3. **Invoices Tab**
   - [ ] Loads successfully
   - [ ] Shows invoice list
   - [ ] Can create new invoice
   - [ ] Can view invoice details

4. **Vault/Documents Tab**
   - [ ] Loads successfully
   - [ ] Shows documents or empty state
   - [ ] Can view document details

5. **Settings Tab**
   - [ ] Loads successfully
   - [ ] All options accessible
   - [ ] Profile editable
   - [ ] Settings persist

### Crash Detection:
- [ ] No "Unfortunately, Bizap has stopped" dialogs
- [ ] No exceptions in logcat
- [ ] Smooth transitions between tabs
- [ ] No freezing or ANR (Application Not Responding)
- [ ] Back button works consistently
- [ ] Device rotation doesn't crash app (if tested)

**Expected Outcome**: ✅ Smooth navigation with no crashes

---

## 📊 RESULTS SUMMARY

| Test | Result | Notes |
|------|--------|-------|
| Currency Display | ☐ PASS ☐ FAIL | Should show A$99.98, not $9998.00 |
| Business Profile | ☐ PASS ☐ FAIL | Should update immediately on Dashboard |
| Payment Progress | ☐ PASS ☐ FAIL | Should show proportional fill |
| Document Vault | ☐ PASS ☐ FAIL | PDF should appear after generation |
| General Stability | ☐ PASS ☐ FAIL | No crashes, smooth navigation |

---

## ⏱️ TIME ESTIMATE
- Total review time: **5-10 minutes**
- Create invoice & test currency: 2 minutes
- Edit profile & verify update: 1 minute
- Record payment & check progress: 1 minute
- Generate PDF & check vault: 1 minute
- Navigate all tabs: 1-2 minutes

---

## 🔍 IF YOU FIND ISSUES

### App Won't Install
```bash
adb uninstall com.emul8r.bizap
./gradlew :app:installDebug
```

### App Crashes on Launch
```bash
adb logcat -c  # Clear log
# Launch app again
adb logcat | grep -E "FATAL|AndroidRuntime"
```

### Specific Feature Not Working
Note the exact steps to reproduce and check logcat for errors:
```bash
adb logcat -d | grep "ERROR\|Exception\|FATAL"
```

---

## ✅ SIGN-OFF

After completing all tests, report:
- ✅ All 5 tests passed (no issues found)
- ⚠️ Some tests passed, issues found in: [list]
- ❌ Major issues preventing testing: [describe]

**Next Steps**:
1. Document any issues found
2. Commit the NetworkModule.kt fix to git
3. Close out the v0.1.0-stabilized review

---

**Good luck! The app should work smoothly. Enjoy testing! 🚀**

