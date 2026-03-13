# 🧪 EMULATOR TESTING & VALIDATION PLAN (March 11, 2026)

**Purpose:** Verify PR #72 (GUI2 customer dropdown fix) against validation findings  
**Date:** March 11, 2026  
**Tester:** Copilot Agent  

---

## 📋 TEST SCOPE

Based on QUICK_REFERENCE_ASSESSMENT_VALIDATION_MARCH_11_2026.md, we need to verify:

### **Critical Bug #1: GUI2 Customer Dropdown** ✅ IMPLEMENTED
- **Status:** Fixed in PR #72
- **What changed:** CustomerRepository injected + CustomerDropdown UI added
- **Test:** User can select customer when creating invoice in GUI2

### **Critical Bug #2: Dashboard Revenue Display** ⚠️ NOT ADDRESSED IN THIS PR
- **Status:** Requires separate investigation (potential snapshot sync issue)
- **Test:** Dashboard should show actual MTD revenue (not $0.00)

### **Critical Bug #3: Snapshot Sync Race Condition** ⚠️ NOT ADDRESSED IN THIS PR
- **Status:** Requires investigation of Room Flow timing
- **Test:** Change invoice status → verify snapshot updates → verify dashboard updates

---

## 🎯 TEST PLAN

### **PHASE 1: GUI2 Invoice Creation Flow (Core Fix)**

**Test Scenario 1.1:** Create invoice with customer selection
```
Steps:
1. Launch app → Navigate to GUI2 Dashboard
2. Create new business (if none exists)
3. Create 2-3 customers
4. Go to Invoices tab
5. Tap "Create Invoice" FAB
6. Verify customer dropdown appears
7. Select a customer from dropdown
8. Fill in invoice details (amount, items, etc.)
9. Save invoice
10. Verify invoice appears in list with correct customer name

Expected Result: ✅ Invoice saved with correct customer association
```

**Test Scenario 1.2:** Verify customer dropdown loads customers
```
Steps:
1. Tap "Create Invoice" 
2. Check dropdown shows all customers
3. Verify customer names are correct
4. Verify customer count matches database

Expected Result: ✅ All active customers appear in dropdown
```

**Test Scenario 1.3:** Test dropdown error handling
```
Steps:
1. Create invoice without selecting customer
2. Try to save
3. Verify error message appears
4. Verify UI guides user to select customer

Expected Result: ✅ Cannot save invoice without customer; clear error message
```

---

### **PHASE 2: Dashboard Metrics (Secondary Issue)**

**Test Scenario 2.1:** Check dashboard revenue display
```
Steps:
1. Go to Dashboard (GUI1)
2. Look at Revenue card
3. Check if it shows $0.00
4. Create/mark invoice as PAID with known amount
5. Verify dashboard updates

Expected Result: ⚠️ If still $0.00 → Revenue calculation broken (separate fix needed)
                ✅ If shows actual amount → Snapshots are working
```

**Test Scenario 2.2:** Check snapshot data
```
Steps:
1. Create invoice with $100 amount
2. Mark as PAID
3. Use Android Studio DB Inspector or adb to query:
   SELECT * FROM invoice_analytics_snapshots
   SELECT * FROM daily_revenue_snapshots
4. Verify records exist with correct amounts

Expected Result: ✅ Snapshots populated with correct data
                ❌ If empty → Snapshot sync broken (needs investigation)
```

---

### **PHASE 3: Invoice Status Updates (Tertiary Issue)**

**Test Scenario 3.1:** Change invoice status
```
Steps:
1. Create invoice in DRAFT status
2. View invoice detail
3. Change status to SENT
4. Verify status updates in UI
5. Go back to list, reopen → status persists
6. Mark as PAID
7. Verify snapshot updates

Expected Result: ✅ Status changes persist
                ⚠️ Check timing of dashboard update
```

---

## 📊 VALIDATION CHECKLIST

### **GUI2 Dropdown Fix (PR #72 Scope)**
- [ ] Build succeeds without errors
- [ ] App installs on emulator
- [ ] GUI2 dashboard loads
- [ ] Customer creation works
- [ ] Invoice creation opens
- [ ] Customer dropdown appears
- [ ] Can select customer
- [ ] Invoice saves with customer
- [ ] Cannot save without customer (validation works)
- [ ] Customer name displays in invoice list

### **Dashboard Revenue (Separate Issue)**
- [ ] Dashboard loads
- [ ] Revenue card visible
- [ ] Shows $0.00 OR actual amount
- [ ] Amount updates when invoice marked PAID
- [ ] Snapshots populated in database

### **Status Updates & Snapshots (Separate Issue)**
- [ ] Can change invoice status
- [ ] Status persists on reload
- [ ] Snapshot tables populate
- [ ] Dashboard reflects changes (with timing observed)

---

## 🚨 KNOWN ISSUES (From Validation)

### **Issue #1: Revenue Dashboard Empty**
- **Root Cause:** Revenue snapshots may not be populated or queries may have filter errors
- **Impact:** Validation finding - Dashboard shows $0.00
- **Action:** Investigate if this PR fixes it (unlikely) or if separate fix needed

### **Issue #2: Snapshot Sync Timing**
- **Root Cause:** Room Flow emissions may race with snapshot writes
- **Impact:** Dashboard may show stale data after status changes
- **Action:** Monitor timing of status change → dashboard update

---

## ✅ APPROVAL CRITERIA

**For approving/merging this PR:**

1. ✅ **GUI2 Dropdown** fully functional (all Phase 1 tests pass)
2. ✅ **Build succeeds** without errors or warnings
3. ✅ **No regression** in other workflows (GUI1 still works)
4. ⚠️ **Dashboard revenue** - If still broken, document as separate issue
5. ⚠️ **Snapshot sync** - If timing issues detected, document as separate issue

---

## 📝 TEST EXECUTION NOTES

**Build Status:** In progress (assembleDebug)  
**Emulator:** emulator-5554 (running)  
**APK Path:** app/build/outputs/apk/debug/app-debug.apk  

---

## 🔄 NEXT STEPS AFTER TESTING

1. **If all tests pass:** Approve and merge PR #72 ✅
2. **If GUI2 dropdown works but revenue/sync issues exist:** 
   - Approve PR #72 (it's a separate concern)
   - Create separate PRs for bugs #2 and #3
3. **If build fails:** Debug and fix before merging
4. **If regressions detected:** Revert and investigate

---

**Test Start Time:** March 11, 2026, ~22:00  
**Tester:** Copilot Agent  
**Status:** READY TO EXECUTE  


