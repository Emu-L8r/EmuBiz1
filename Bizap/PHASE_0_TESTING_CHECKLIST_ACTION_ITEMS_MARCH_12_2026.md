# ✅ PHASE 0 TESTING CHECKLIST - YOUR ACTION ITEMS (March 12, 2026)

**Status:** Code changes complete. Ready for testing.  
**Your Task:** Verify fixes work on emulator  
**Estimated Time:** 2-3 hours  

---

## 🎯 YOUR IMMEDIATE CHECKLIST

### **Phase 1: Build Setup (15 minutes)**

- [ ] Open terminal in project root: `C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap`
- [ ] Run: `./gradlew clean assembleDebug`
- [ ] Verify: `BUILD SUCCESSFUL` in output
- [ ] Verify: No errors or compilation failures
- [ ] Note: APK location: `app/build/outputs/apk/debug/app-debug.apk`

### **Phase 2: Emulator Deployment (10 minutes)**

- [ ] Start emulator or connect device
- [ ] Run: `./gradlew installDebug`
- [ ] Verify: App installs successfully
- [ ] Launch app on emulator
- [ ] Verify: App starts without crashing

### **Phase 3: Test Bug #1 - Dashboard Revenue (30 minutes)**

**Setup:**
- [ ] Open app in GUI2 (Modern GUI)
- [ ] Navigate to Dashboard
- [ ] Note initial MTD revenue (should be $0 if no invoices)

**Test Scenario 1: Create Invoice & Record Payment**
- [ ] Create new invoice: amount = $100.00
- [ ] Save invoice
- [ ] Record payment: amount = $100.00
- [ ] Wait 2 seconds for dashboard refresh
- [ ] **VERIFY:** Dashboard MTD revenue = $100.00 (NOT $0.00)
- [ ] **Check Logcat:**
  ```bash
  adb logcat | grep "RevenueRepository"
  ```
- [ ] **VERIFY:** See log like: `MTD: 10000 cents ($100.00 ✅)`

**Test Scenario 2: Create Second Invoice**
- [ ] Create another invoice: amount = $50.00
- [ ] Record payment: amount = $50.00
- [ ] **VERIFY:** Dashboard MTD revenue = $150.00
- [ ] **Check Logcat:** Verify log shows MTD = 15000 cents

**If Dashboard Still Shows $0.00:**
- [ ] Check logcat for actual MTD value
- [ ] Run SQL query: `SELECT COUNT(*) FROM invoices WHERE status = 'PAID';`
- [ ] If count > 0 but MTD = 0: Invoice timestamp issue
- [ ] Document issue and create GitHub issue

### **Phase 4: Test Bug #2 - Snapshot Sync (30 minutes)**

**Setup:**
- [ ] Keep same invoices from Bug #1 test
- [ ] Have adb logcat open

**Test Scenario: Record Payment & Verify Snapshots**
- [ ] Perform payment recording
- [ ] **Check Logcat for snapshot sync message:**
  ```bash
  adb logcat | grep "Snapshots synced"
  ```
- [ ] **VERIFY:** See log: `✅ Snapshots synced after payment for invoice=X`
- [ ] **Check Database:**
  ```sql
  SELECT * FROM daily_revenue_snapshots 
  WHERE businessProfileId = 1 
  AND dateString = '2026-03-12';
  ```
- [ ] **VERIFY:** Snapshot exists with correct totalRevenue

**If Snapshots Not Syncing:**
- [ ] Check logcat for SnapshotSyncHelper exceptions
- [ ] Verify SnapshotSyncHelper is injected properly
- [ ] Check if AnalyticsDao methods exist
- [ ] Document issue and create GitHub issue

### **Phase 5: Test Bug #3 - GUI Divergence (30 minutes)**

**Setup:**
- [ ] Have invoices with payments from previous tests
- [ ] Keep app running

**Test Scenario 1: Compare Both GUIs**
- [ ] Open GUI2 Dashboard
- [ ] Note MTD revenue: **Amount A**
- [ ] Switch to GUI1 (Classic)
- [ ] Navigate to equivalent dashboard
- [ ] Note MTD revenue: **Amount B**
- [ ] **VERIFY:** Amount A = Amount B
- [ ] If different: Note the difference

**Test Scenario 2: Cross-GUI Payment Recording**
- [ ] Record payment in GUI2: $25
- [ ] **VERIFY:** GUI2 dashboard updates
- [ ] Switch to GUI1
- [ ] **VERIFY:** GUI1 dashboard also updated
- [ ] Switch back to GUI2
- [ ] **VERIFY:** Still shows same amount
- [ ] **Check Logcat:** Both should reference same repositories

**If GUIs Show Different Amounts:**
- [ ] Add logging to identify which repositories each uses
- [ ] Create GitHub issue with exact divergence numbers
- [ ] Proceed to Bug #3 investigation (see BUG_3_FIX_STRATEGY document)

---

## 🔍 LOGCAT COMMANDS FOR DEBUGGING

```bash
# Watch all revenue-related logs
adb logcat | grep "RevenueRepository"

# Watch snapshot sync logs
adb logcat | grep "Snapshots synced"

# Watch all dashboard logs
adb logcat | grep "Dashboard"

# Watch all Bizap logs
adb logcat | grep "bizap"

# Clear logcat before test
adb logcat -c

# Save logcat to file
adb logcat > logcat_output.txt
```

---

## 📊 EXPECTED TEST RESULTS

### **Bug #1: PASS Condition**
```
✅ Dashboard shows $100.00 after recording $100 payment
✅ Logcat shows: "MTD: 10000 cents ($100.00 ✅)"
✅ Second payment updates total to $150.00
```

### **Bug #2: PASS Condition**
```
✅ Logcat shows: "✅ Snapshots synced after payment"
✅ daily_revenue_snapshots updated with payment amount
✅ No exceptions in logcat related to snapshot sync
```

### **Bug #3: PASS Condition**
```
✅ GUI1 shows $100.00
✅ GUI2 shows $100.00
✅ Both update when payment recorded
✅ Both show identical amounts when switching
```

---

## 📋 IF TESTS FAIL

### **Build Fails:**
- [ ] Check for Calendar import errors
- [ ] Verify all three files were edited correctly
- [ ] Check `build/reports/problems/problems-report.html`
- [ ] Post error to GitHub issue

### **Dashboard Still $0.00:**
- [ ] Check if invoices exist: `SELECT * FROM invoices;`
- [ ] Check if they have PAID status
- [ ] Check logcat MTD value
- [ ] May need to verify Calendar date calculation

### **Snapshots Not Syncing:**
- [ ] Check if SnapshotSyncHelper properly injected
- [ ] Check if AnalyticsDao methods exist
- [ ] Check logcat for exceptions in snapshot sync
- [ ] May need to verify DAO method signatures

### **GUIs Show Different Numbers:**
- [ ] Add logging to identify data source
- [ ] Check which repositories each uses
- [ ] Create issue with exact divergence details
- [ ] Proceed to Bug #3 investigation

---

## ✅ AFTER SUCCESSFUL TESTS

Once all bugs pass testing:

1. [ ] Take screenshots of both dashboards
2. [ ] Save logcat output showing successful logs
3. [ ] Create commit:
   ```bash
   git checkout -b phase0/fix-3-critical-bugs
   git add -A
   git commit -m "Phase 0: Fix 3 critical bugs - Dashboard, Snapshot, GUI consistency"
   ```
4. [ ] Push to GitHub:
   ```bash
   git push origin phase0/fix-3-critical-bugs
   ```
5. [ ] Create pull request
6. [ ] Get code review
7. [ ] Merge to main

---

## 🎯 NEXT STEPS AFTER PHASE 0

**If All Tests Pass:**
- ✅ Phase 0 complete
- ✅ Foundation validated
- → Proceed to Phase 1 (Authentication - Week 2)

**If Tests Fail on Bug #1 or #2:**
- ⚠️ Debug using provided logs
- ⚠️ Fix issues in code
- → Re-test

**If Tests Fail on Bug #3:**
- ⏳ Proceed to Bug #3 investigation
- ⏳ Add logging to identify GUI1 data source
- ⏳ Refactor GUI1 to use V2 repositories
- → Re-test

---

## 📞 SUPPORT

**Documents to Reference:**
- `BUG_1_FIX_EXECUTED_DASHBOARD_REVENUE_MARCH_12_2026.md` - Dashboard fix details
- `BUG_2_FIX_EXECUTED_SNAPSHOT_SYNC_MARCH_12_2026.md` - Snapshot sync details
- `BUG_3_FIX_STRATEGY_GUI1_VS_GUI2_MARCH_12_2026.md` - GUI divergence strategy

**If Stuck:**
- Check logcat output
- Verify database state with SQL queries
- Review code changes in the three modified files
- Create GitHub issue with detailed logs

---

**Ready to test?**

**Start with:** `./gradlew clean assembleDebug`

**Then verify on emulator!**

---

**Phase 0 Testing Checklist Created: March 12, 2026**  
**Estimated Time: 2-3 hours of testing**  
**Target Completion: This week**


