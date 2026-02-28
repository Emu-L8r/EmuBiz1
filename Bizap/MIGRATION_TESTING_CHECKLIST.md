# 🧪 Migration Testing Checklist - Version 5 → Version 6

**App Status:** Running  
**Version:** 6 (v5→v6 migration implemented)  
**Build:** ✅ Successful (32s)  

---

## ✅ QUICK START TESTING

### Step 1: Verify App is Running
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb shell am start -n com.emul8r.bizap/.MainActivity
```

**Expected:** "Activity not started, intent has been delivered to currently running top-most instance"  
(This means app is already running ✅)

---

## 📋 TEST SUITE

### TEST 1: Fresh Invoice Creation (Post-Migration)
**Objective:** Verify new snapshot fields are populated on new invoices

Steps:
1. [ ] Open app (if not already open)
2. [ ] Go to **Invoices** tab
3. [ ] Click **Create Invoice**
4. [ ] Select a customer (e.g., "John Doe")
5. [ ] Add line item: "Widget A" / Qty: 1 / Price: $50
6. [ ] Click **Save Invoice**
7. [ ] Reopen the invoice you just created

**Verify:**
- [ ] Invoice shows customer name ✅
- [ ] Invoice shows an **invoice number** (e.g., "INV-2026-...")
- [ ] Invoice shows a **due date** (30 days from today)
- [ ] No crashes during creation or reopening

**Pass/Fail:** [ ] PASS [ ] FAIL  
**Notes:** ___________________________________________

---

### TEST 2: Data Integrity - Existing Invoices
**Objective:** Verify migration preserved all old invoices

Steps:
1. [ ] Go to **Invoices** tab
2. [ ] Look at the invoice list
3. [ ] Count how many invoices are shown

**Verify:**
- [ ] Same number of invoices as before ✅
- [ ] All invoice data appears intact (names, amounts, dates)
- [ ] No "NULL" or missing values visible

**Pass/Fail:** [ ] PASS [ ] FAIL  
**Notes:** ___________________________________________

---

### TEST 3: Reopen Old Invoice
**Objective:** Verify old invoices still work and have new fields populated

Steps:
1. [ ] Select any **existing invoice** (created before migration)
2. [ ] Click to open it
3. [ ] View the invoice details

**Verify:**
- [ ] Invoice opens without crash ✅
- [ ] All old data is there (customer, amount, items)
- [ ] New fields are visible (invoiceNumber, dueDate, taxRate)
- [ ] New fields have sensible defaults (not NULL, not blank)

**Pass/Fail:** [ ] PASS [ ] FAIL  
**Notes:** ___________________________________________

---

### TEST 4: PDF Generation
**Objective:** Verify PDF generation still works post-migration

Steps:
1. [ ] Open any invoice (new or old)
2. [ ] Click **Generate PDF** button
3. [ ] Wait for PDF to be generated
4. [ ] Open **Vault** tab
5. [ ] Verify PDFs appear in the list

**Verify:**
- [ ] PDF generates without error ✅
- [ ] PDF appears in Vault
- [ ] PDF can be opened/viewed
- [ ] PDF shows invoice details correctly

**Pass/Fail:** [ ] PASS [ ] FAIL  
**Notes:** ___________________________________________

---

### TEST 5: Database Integrity (Advanced)
**Objective:** Verify database wasn't corrupted by migration

Run these commands:
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Count invoices
$count = & $adb shell "sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db 'SELECT COUNT(*) FROM invoices;'"
Write-Host "Total invoices: $count"

# Check new fields are populated
& $adb shell "sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db 'SELECT id, invoiceNumber, dueDate, taxRate FROM invoices LIMIT 3;'"
```

**Verify:**
- [ ] Count matches expected number ✅
- [ ] invoiceNumber is NOT empty (e.g., "INV-2026-00001")
- [ ] dueDate is NOT 0 or NULL (e.g., 1711622400000)
- [ ] taxRate is populated (e.g., 0.1)

**Pass/Fail:** [ ] PASS [ ] FAIL  
**Database Count:** ___________  
**Sample Output:** ___________________________________________

---

## 🎯 LOGCAT MONITORING

**Optional:** Monitor migration execution in real-time

```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Clear logcat
& $adb logcat -c

# Restart app (triggers migration)
& $adb shell am force-stop com.emul8r.bizap
& $adb shell am start -n com.emul8r.bizap/.MainActivity

# Watch for migration messages (Ctrl+C to stop)
& $adb logcat | Select-String "migration|Migration|ALTER TABLE|Room"
```

**Expected Logs:**
```
D/Room: Running migration from version 5 to 6
D/Room: ALTER TABLE invoices ADD COLUMN customerAddress TEXT NOT NULL DEFAULT ''
D/Room: ...more ALTER TABLE statements...
D/Room: Migration from 5 to 6 successful
```

---

## 📊 TEST RESULTS SUMMARY

### Overall Status
- [ ] All tests PASSED ✅
- [ ] Some tests FAILED ⚠️
- [ ] Critical issues found ❌

### Breakdown
| Test | Result | Notes |
|------|--------|-------|
| Fresh Invoice | [ ] PASS / [ ] FAIL | __________________ |
| Data Integrity | [ ] PASS / [ ] FAIL | __________________ |
| Old Invoice Reopen | [ ] PASS / [ ] FAIL | __________________ |
| PDF Generation | [ ] PASS / [ ] FAIL | __________________ |
| Database Check | [ ] PASS / [ ] FAIL | __________________ |

---

## 🔴 IF A TEST FAILS

**Document:**
1. Which test failed?
2. What exactly happened? (screenshot or description)
3. Any error messages? (copy from logcat)
4. Can you reproduce it? (yes/no)

**Example Failure Report:**
```
Test: PDF Generation
Result: FAILED
Issue: PDF file not appearing in Vault
Error: "Cannot find file at path: /storage/emulated/0/..."
Reproducible: Yes - happens every time
```

---

## ✨ SUCCESS CRITERIA

Migration is **COMPLETE** if:
- ✅ App launches without crashes
- ✅ Existing invoices preserved
- ✅ New invoices have snapshot fields
- ✅ PDF generation still works
- ✅ Database has correct schema

**If all 5 tests PASS → Migration is successful!** 🎉

---

## 📝 NOTES FOR NEXT PHASE

Once migration is verified working:

**Phase 2A: Capture Customer Snapshot**
- Update CreateInvoiceViewModel
- Copy customer address + email to invoice at creation time

**Phase 2B: Use Snapshot in PDF**
- Update InvoicePdfService
- Use invoice.customerAddress instead of looking up Customer
- Display tax breakdown using taxRate + taxAmount
- Add company logo using companyLogoPath

**Phase 2C: UI Updates**
- Show invoiceNumber and dueDate on invoice details
- Display tax information
- Show formatted invoice number in list

---

**Ready to test? Run TEST 1 and report results!** 🚀

