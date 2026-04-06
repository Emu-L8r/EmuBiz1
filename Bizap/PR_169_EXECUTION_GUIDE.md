# 🚀 PR 169 EXECUTION ROADMAP - STEP-BY-STEP GUIDE

**Last Updated:** April 6, 2026  
**Status:** Ready for Testing  
**Build:** ✅ SUCCESSFUL (app-debug.apk 48.11 MB)

---

## 📋 PRE-VERIFICATION CHECKLIST

Before you start testing, confirm these prerequisites:

```powershell
# ✅ 1. Verify APK exists
Get-ChildItem "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk"

# ✅ 2. Verify Android Emulator is running
adb devices

# ✅ 3. Verify Gradle daemon is cleaned
./gradlew --stop

# ✅ 4. Verify google-services.json is present
Get-ChildItem "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\google-services.json"
```

---

## 🎯 QUICK START (5 minutes)

### Step 1: Install APK on Emulator
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Expected Output:**
```
Success
```

### Step 2: Launch App
- Tap **Bizap** icon in emulator launcher
- Wait 2-3 seconds for Dashboard to load
- ✅ **PASS if:** Dashboard appears with tabs at bottom

### Step 3: Check Logcat for Initialization
```powershell
# Open new terminal window
adb logcat | findstr "Firebase\|Crashlytics\|Timber"
```

**Look for:**
```
D/BizapApp: Firebase Initialized Successfully
D/BizapApp: Crashlytics configured
```

### Step 4: Test Basic Navigation
- [ ] Tap **Invoices** tab
- [ ] Tap **Customers** tab
- [ ] Tap **Analytics** tab
- [ ] Tap **Settings** tab
- [ ] ✅ All tabs load without crashes

**If any tab crashes, check Logcat for exceptions.**

---

## 🏗️ FULL VERIFICATION FLOW (60-80 minutes)

### PHASE 1: IMMEDIATE VERIFICATION (5-10 min)

**Terminal Step 1.1:**
```powershell
# Verify APK file
ls "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap\app\build\outputs\apk\debug\app-debug.apk" | Select-Object Name, @{Name="SizeMB";Expression={[math]::Round($_.Length/1MB,2)}}
```

**Expected:** Size = ~48.11 MB

**Terminal Step 1.2:**
```powershell
# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Expected:** `Success`

**Emulator Step 1.3:**
- Open app from launcher
- Dashboard should appear in 2-3 seconds
- No red error banners
- Bottom navigation visible with 5 tabs

**Terminal Step 1.4:**
```powershell
# Check Crashlytics initialization
adb logcat | findstr /I "crashlytics\|firebase"
```

**Expected:** See "Initialized" or "configured" messages

---

### PHASE 2: CORE FUNCTIONALITY (15-20 min)

**Test Case 2.1: Create Customer**

In Emulator:
1. Tap **Customers** tab
2. Tap **+ New** or **Add Customer** button
3. Fill form:
   - Name: `Test Customer Alpha`
   - Email: `alpha@example.com`
   - Phone: `+1-555-0001`
   - Address: `123 Main St`
4. Tap **Save**
5. ✅ Customer appears in list

**Test Case 2.2: Create Invoice**

In Emulator:
1. Tap **Invoices** tab
2. Tap **+ New** or **Create Invoice** button
3. Fill form:
   - Customer: `Test Customer Alpha`
   - Invoice #: `INV-001`
   - Date: Today
   - Due Date: 30 days out
4. Add Line Item:
   - Description: `Consulting Services`
   - Quantity: `1`
   - Rate: `1000.00`
5. Verify Total: `1000.00`
6. Tap **Save**
7. ✅ Invoice appears in list

**Test Case 2.3: View Invoice**

In Emulator:
1. Tap on `INV-001` from list
2. Verify details screen shows:
   - Customer name
   - Invoice number
   - Line items
   - Total amount
   - Status: "Unpaid"
3. ✅ All details display correctly

**Test Case 2.4: Record Payment**

In Emulator:
1. From invoice detail, tap **Record Payment**
2. Fill form:
   - Amount: `500.00`
   - Date: Today
   - Method: `Bank Transfer`
3. Tap **Save** or **Confirm**
4. ✅ Invoice status changes to "Partially Paid"
5. ✅ Remaining balance shows: `500.00`

**Test Case 2.5: Export PDF**

In Emulator:
1. From invoice detail, tap **Export** or **PDF**
2. Wait for PDF generation
3. ✅ See success message or file saved notification
4. PDF should appear in Files app or download notification

---

### PHASE 3: ANALYTICS & DASHBOARD (10-15 min)

**Test Case 3.1: Dashboard**

In Emulator:
1. Tap **Dashboard** tab
2. Verify cards display:
   - [ ] Total Revenue: `1000.00` (from test invoice)
   - [ ] Outstanding: `500.00` (partially paid)
   - [ ] Overdue: `0` (due date is future)
   - [ ] Top Customers: `Test Customer Alpha`
3. ✅ Data is correct (matches created invoices)

**Test Case 3.2: Analytics Tab**

In Emulator:
1. Tap **Analytics** tab
2. Verify sections exist:
   - [ ] Revenue chart
   - [ ] Invoice aging
   - [ ] Customer metrics
3. Tap **Revenue** section
4. Verify time-series chart displays
5. ✅ No blank charts or loading spinners

**Test Case 3.3: Date Filters**

In Emulator:
1. Look for date range selector
2. Change from "This Month" to "Last 90 Days"
3. Verify chart updates
4. ✅ Filtering works

---

### PHASE 4: DATA PERSISTENCE (10 min)

**Test Case 4.1: Kill App and Restart**

In Emulator:
1. Swipe app up from recent apps (kill it)
2. Wait 2 seconds
3. Tap app icon to reopen
4. Dashboard loads
5. Tap **Invoices**
6. ✅ `INV-001` still exists
7. ✅ Customer `Test Customer Alpha` still exists

**Test Case 4.2: Verify Encryption**

In Terminal:
```powershell
# Check database file
adb shell ls -la /data/data/com.emul8r.bizap/databases/

# Expected output includes:
# bizap.db (encrypted file)
```

**Test Case 4.3: Offline Mode**

In Emulator:
1. Open Settings > Airplane Mode
2. Enable Airplane Mode
3. Return to Bizap app
4. ✅ App continues to work
5. ✅ Can view invoices and customers
6. Disable Airplane Mode
7. ✅ Sync resumes (if applicable)

---

### PHASE 5: CRASHLYTICS & ERROR HANDLING (5 min)

**Test Case 5.1: Force Crash (DEBUG ONLY)**

In Emulator:
1. Look at bottom-right corner of screen
2. Should see red **"Force Crash"** button
3. Tap the button
4. ✅ App crashes and closes

**Test Case 5.2: Reopen and Verify Report**

In Emulator:
1. Tap app icon to reopen
2. Wait 10 seconds for Crashlytics to upload
3. Check Logcat:
```powershell
adb logcat | findstr "Crashlytics"
```

**Terminal Step 5.3: Verify in Firebase Console**

```
1. Open https://console.firebase.google.com/
2. Select your project (Bizap or EmuBiz)
3. Left menu > Crashlytics
4. Look for test crash in the list
5. ✅ Crash appears within 1 minute
6. Verify stack trace is visible
```

---

### PHASE 6: BUILD ARTIFACTS (5 min)

**Terminal Step 6.1:**
```powershell
# Check APK size
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
(Get-Item "app/build/outputs/apk/debug/app-debug.apk").Length / 1MB | Write-Output
```

**Expected:** ~48 MB ✅

**Terminal Step 6.2:**
```powershell
# Check build time from last build
# (Gradle prints "BUILD SUCCESSFUL in X ms" at end)

# For next build, measure:
Measure-Command { ./gradlew clean build -x test 2>&1 | Out-Null }
```

**Expected:** Clean build <5 min, Incremental build <2 min

---

### PHASE 7: GIT STATUS (5 min)

**Terminal Step 7.1:**
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
git status
```

**Expected Output:**
```
On branch main
Your branch is up to date with 'origin/main'.
nothing to commit, working tree clean
```

**Terminal Step 7.2:**
```powershell
git log --oneline -5
```

**Expected Output (should include recent commits):**
```
f0ccb3d fix: Upgrade Hilt to 2.52 to support Kotlin 2.1.0 metadata
8c0ec49 feat: Add Crashlytics mapping file upload configuration
...
```

**Terminal Step 7.3:**
```powershell
# Verify remote is in sync
git log origin/main -1 --oneline
```

**Expected:** Same commit hash as last local commit

---

## ✅ SUCCESS CRITERIA

**All Phases Must Pass:**

1. ✅ **Phase 1:** App launches, no crashes, Crashlytics initializes
2. ✅ **Phase 2:** Create invoice, customer, record payment, export PDF all work
3. ✅ **Phase 3:** Dashboard shows correct data, analytics charts render
4. ✅ **Phase 4:** Data persists after restart, offline mode works
5. ✅ **Phase 5:** Crash report appears in Firebase Console
6. ✅ **Phase 6:** APK ~48MB, build time acceptable
7. ✅ **Phase 7:** PR merged, clean git status

---

## 🚨 TROUBLESHOOTING

### Problem: App Won't Install
```powershell
# Solution 1: Clear old app
adb uninstall com.emul8r.bizap

# Solution 2: Reinstall
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Problem: App Crashes on Launch
```powershell
# Check logs
adb logcat | findstr "Exception\|Error" | head -20

# If it's a Crashlytics error:
# Verify google-services.json exists in app/ folder

# If it's a database error:
# Clear app data and restart
adb shell pm clear com.emul8r.bizap
```

### Problem: Dashboard Shows No Data
```
Solution: Create test invoice (Phase 2) first
The dashboard displays data from created invoices
If you haven't created any, it will show empty/zero values
```

### Problem: PDF Export Fails
```
Check permissions:
- App needs WRITE_EXTERNAL_STORAGE permission
- Check Settings > Apps > Bizap > Permissions > Storage
```

### Problem: Crash Not Appearing in Firebase
```
Solution:
1. Check internet connection (emulator must have connectivity)
2. Wait 5+ minutes for report to upload
3. Check Firebase project ID in google-services.json matches console
4. Try Force Crash button again
```

---

## 📊 FINAL VERIFICATION REPORT TEMPLATE

Copy and paste this into a new file when complete:

```markdown
# PR 169 Final Verification Report

**Date:** April 6, 2026
**Tester:** [Your Name]
**Build:** app-debug.apk (48.11 MB)
**Build Time:** [Record actual time]

## Phase Results

| Phase | Status | Notes |
|-------|--------|-------|
| 1. Immediate | ✅ PASS / ❌ FAIL | |
| 2. Core Functions | ✅ PASS / ❌ FAIL | |
| 3. Analytics | ✅ PASS / ❌ FAIL | |
| 4. Data Persistence | ✅ PASS / ❌ FAIL | |
| 5. Crashlytics | ✅ PASS / ❌ FAIL | |
| 6. Build Artifacts | ✅ PASS / ❌ FAIL | |
| 7. Git Status | ✅ PASS / ❌ FAIL | |

## Overall Status
✅ **ALL PHASES PASSED** / ❌ **FAILED - See below**

## Failed Tests (if any)
- Test Name: [Description]
- Root Cause: [What happened]
- Resolution: [How to fix]

## Recommendations for Next PR
1. [Optimization idea]
2. [Feature idea]
3. [Stability improvement]

## Sign-Off
✅ App is ready for production testing
or
❌ App needs fixes before deployment
```

---

## 🎉 NEXT STEPS

### If All Tests Pass ✅
1. Create a **GitHub Release** with version number
2. Document PR 169 changes in release notes
3. Start planning **PR 170** for optimization features:
   - LeakCanary (memory leak detection)
   - FTS5 (full-text search)
   - Recurring invoices
   - Multi-business support

### If Tests Fail ❌
1. Document which phase failed
2. Check Logcat for root cause
3. Create a bugfix commit
4. Re-run the failed phase only
5. Merge bugfix to main when passing

---

## 📞 SUPPORT RESOURCES

- **Gradle Build Issues:** `./gradlew clean; ./gradlew build --info 2>&1 | tail -100`
- **Logcat Errors:** `adb logcat | findstr "Exception"`
- **Firebase Issues:** Check `app/google-services.json` is valid
- **Database Issues:** Clear with `adb shell pm clear com.emul8r.bizap`
- **Emulator Not Responding:** Restart: `adb kill-server; adb start-server`

---

**Created:** April 6, 2026  
**Ready to Execute:** ✅ YES


