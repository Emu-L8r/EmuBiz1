# 🎯 MASTER ACTION PLAN - Data Consistency Fixes

**Date:** March 9, 2026  
**Status:** Code changes made. Ready to build and test.  
**Objective:** Verify data consistency fixes work end-to-end.

---

## ✅ WHAT'S ALREADY FIXED

### Fix #1: GUI2 Analytics (InvoiceDaoV2.kt - Line 143-156)
**Query:** `observeInvoiceCountByStatus()`  
**Change:** Added `AND status IN ('PAID', 'PARTIALLY_PAID', 'SENT', 'OVERDUE', 'CANCELLED')`  
**Result:** Excludes DRAFT invoices from GUI2 analytics ✅

### Fix #2: GUI1 Analytics (InvoicePaymentDao.kt - 3 queries)
**Queries:**
1. `observeAllSnapshots()` (Line 31-41)
2. `observeRiskInvoices()` (Line 50-62)
3. `getAllSnapshots()` (Line 70-80)

**Change:** All 3 now filter by `paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')`  
**Result:** Excludes DRAFT snapshots from GUI1 analytics ✅

---

## 🚀 THREE SIMPLE STEPS

### Step 1: Kill Gradle & Clean (In Android Studio Terminal)
```bash
# Kill hanging gradle
taskkill /F /IM java.exe 2>nul || true

# Clean everything
rm -r app/build .gradle build 2>nul || true
```

### Step 2: Build in Android Studio UI
1. Click **Build** menu → **Clean Project**
2. Click **Build** menu → **Build 'app'** (or press Ctrl+F9)
3. Wait for build to complete (should show "BUILD SUCCESSFUL" in bottom panel)

### Step 3: Run on Emulator
1. Click **Run** menu → **Run 'app'** (or press Shift+F10)
2. Select emulator device
3. App will install and launch

---

## 🧪 TEST SCENARIOS

### Test 1: Two DRAFT Invoices
```
Setup:
- Create 2 invoices
- Leave both as DRAFT status
- Do NOT mark as SENT or PAID

Expected Results:
✅ Dashboard: Revenue = A$0.00
✅ Dashboard: Outstanding = A$0.00
✅ GUI1 Analytics: Outstanding = $0.00
✅ GUI2 Analytics: Outstanding = $0.00
✅ All screens MATCH
```

### Test 2: One PAID + One SENT + One DRAFT
```
Setup:
- Create Invoice A: A$500, mark PAID
- Create Invoice B: A$200, mark SENT  
- Create Invoice C: A$100, leave as DRAFT

Expected Results:
✅ Dashboard: Revenue = A$500 (only PAID)
✅ Dashboard: Outstanding = A$200 (only SENT)
✅ GUI1 Analytics: Collection Rate = 71.4% (500/(500+200))
✅ GUI2 Analytics: Collection Rate = 71.4%
✅ Both GUIs MATCH
```

### Test 3: Mixed Statuses
```
Setup:
- Create Invoice A: A$1000, PAID
- Create Invoice B: A$500, PARTIALLY_PAID
- Create Invoice C: A$300, SENT
- Create Invoice D: A$200, OVERDUE
- Create Invoice E: A$50, DRAFT

Expected Results:
✅ Outstanding = 300 + 200 = A$500 (SENT + OVERDUE only)
✅ Collected = 1000 + 500 (paid portion) = varies by partial amount
✅ Invoice Count = 4 (excludes DRAFT)
✅ GUI1 & GUI2 show same metrics
```

---

## 📋 VERIFICATION CHECKLIST

- [ ] Gradle clean succeeded
- [ ] Build completed (BUILD SUCCESSFUL message)
- [ ] No Room compilation errors
- [ ] No Hilt DI errors
- [ ] App installed on emulator
- [ ] App launched successfully
- [ ] Test 1 passed (2 DRAFT: all show $0)
- [ ] Test 2 passed (mixed statuses: match)
- [ ] Test 3 passed (complex scenario: match)
- [ ] GUI1 and GUI2 show identical metrics
- [ ] Dashboard and Analytics aligned

---

## ❌ IF BUILD FAILS

**Most Common Issue:** Gradle daemon hung

**Fix:**
```bash
# In Android Studio Terminal:
./gradlew --stop
./gradlew clean build -x test --no-daemon
```

**If still fails:** File permissions or disk space
- Restart Android Studio
- Restart computer if needed

---

## 💾 AFTER TESTING

Once tests pass:

### Commit Changes
```bash
git add -A
git commit -m "Fix: Exclude DRAFT invoices from GUI1 and GUI2 analytics

- InvoiceDaoV2: observeInvoiceCountByStatus() filters by status
- InvoicePaymentDao: observeAllSnapshots/observeRiskInvoices/getAllSnapshots filter by paymentStatus
- Both GUIs now show consistent metrics
- DRAFT invoices excluded from all financial calculations"

git push origin main
```

---

## 🎯 SUCCESS CRITERIA

✅ **Data Consistency:** Dashboard and Analytics show same metrics  
✅ **DRAFT Exclusion:** DRAFT invoices don't appear in financial calculations  
✅ **GUI Alignment:** GUI1 and GUI2 show identical metrics  
✅ **No Errors:** App builds and runs without crashes  

---

**If all tests pass → You're done. Push to GitHub.**  
**If tests fail → Check error messages and debug.**


