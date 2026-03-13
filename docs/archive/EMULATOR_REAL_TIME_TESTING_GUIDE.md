# 🚀 EMULATOR TESTING GUIDE - REAL-TIME VERIFICATION

**Status:** App is running on emulator  
**Date:** March 9, 2026  
**PR:** #56 (AccountingService merged)  

---

## ✅ WHAT YOU SHOULD BE SEEING RIGHT NOW

### On Startup (Login/Dashboard Screen)
- [ ] **SyncStatusIndicator** at top of screen
  - Should show: "You are online" (if connected)
  - Color: Green background
  - No pending operations badge (queue is empty)

- [ ] **Dashboard Content**
  - GUI1 or GUI2 dashboard visible
  - Show financial metrics (revenue, outstanding, etc.)
  - Invoice list or summary

### What to Test (10-15 minutes)

---

## 🎯 TEST 1: Verify SyncStatusIndicator Appears

**Steps:**
1. Open the app
2. Look at **top of screen** for status banner
3. **Expected:** See one of these:
   - ✅ "You are online" (green banner, no sync in progress)
   - ⚠️ "X pending operations" (orange banner with spinner, if you queued invoices)
   - ❌ "You are currently offline" (red banner with wifi icon)

**What it proves:**
- SyncStatusIndicator component is wired
- NetworkMonitor is working
- OfflineQueueRepository integration is live

**If you DON'T see it:**
- Component not integrated (likely - based on code review)
- Not added to MainActivity or Dashboard
- Need to integrate manually

---

## 🎯 TEST 2: Create Invoice (Offline Simulation)

**Setup:**
1. Enable airplane mode on emulator (Settings → Airplane Mode ON)
2. Keep app running (don't close it)

**Steps:**
1. Navigate to "Create Invoice" screen
2. Fill in sample data:
   - Customer: Any name
   - Amount: 100.00
   - Status: DRAFT
3. Click "Save Invoice"

**Expected Results:**
```
✅ Invoice saved locally (no error)
✅ SyncStatusIndicator shows "1 pending operation" (orange banner)
✅ No network error displayed
✅ UI responsive (no freezing)
```

**What this proves:**
- Offline queue is capturing operations
- App handles offline gracefully
- OfflineQueueService.queueState is emitting updates

**If you see errors:**
- Operation didn't queue
- Backend URL issue (trying to connect when offline)
- Queue service not wired properly

---

## 🎯 TEST 3: Go Online & Watch Sync

**Setup:**
From previous test (airplane mode still ON, pending operation queued)

**Steps:**
1. Disable airplane mode (Settings → Airplane Mode OFF)
2. **Immediately watch SyncStatusIndicator**
3. Expected: Show sync in progress (spinner + "Syncing...")

**Expected Results:**
```
✅ SyncStatusIndicator changes to "Syncing..." with spinner
✅ After 2-5 seconds: Banner shows "Sync complete" or disappears
✅ Pending count returns to 0
✅ Invoice appears in list
```

**What this proves:**
- SyncWorker triggered by network change
- SyncPendingOperationsUseCase is running
- Backend API call attempted (will fail if URL is wrong)
- UI updates reactively

**If you see errors:**
- Sync banner stays "Pending" forever = API call failed
- Network request timeout = backend not responding
- "Sync failed" message = API returned error

---

## 🎯 TEST 4: Check Financial Calculations

**What to verify:**

### Dashboard Values
1. Look at main dashboard numbers:
   - Total Revenue
   - Outstanding Amount
   - Paid Count / Unpaid Count
   - Collection Rate %

2. **Expected:**
   - Revenue should match PAID invoices only
   - Outstanding = Total - Paid (for SENT invoices)
   - Collection Rate = Paid / (Paid + Outstanding)
   - DRAFT invoices should NOT be included

3. **Example:**
   ```
   If you have:
   - Invoice A: $100, PAID
   - Invoice B: $50, SENT
   - Invoice C: $30, DRAFT (just created offline)
   
   Dashboard should show:
   - Revenue: $100 (only A, which is PAID)
   - Outstanding: $50 (only B, which is SENT)
   - Paid Count: 1 (only A)
   - Unpaid Count: 1 (only B - C is DRAFT, excluded)
   - Collection Rate: 66.7% (100/(100+50))
   ```

**What this proves:**
- AccountingService logic is working
- DRAFT exclusion is implemented
- Financial rules are enforced

---

## 🎯 TEST 5: Check Customer Segments (GUI1)

**Steps:**
1. Navigate to "Customer Segments" or "Customers" section
2. Look at top customer
3. Check: **"Paid: $X.XX"**

**Expected:**
- Shows actual paid amount from PAID invoices
- Not $0 (unless customer has no paid invoices)
- Matches dashboard revenue for that customer

**What this proves:**
- Customer segment queries working
- Data consistency with main dashboard

---

## 📊 INTERPRETATION GUIDE

### If Everything Works ✅
```
SyncStatusIndicator visible
  ↓
Offline queue captures operations
  ↓
Sync triggers when online
  ↓
Financial calculations correct
  ↓
→ Phase 2 is 80% ready, just need backend URL
```

### If SyncStatusIndicator Missing ⚠️
```
SyncStatusIndicator NOT visible
  ↓
Component exists in code but not integrated
  ↓
Need to add to:
  - MainActivity.kt
  - DashboardScreen.kt
  - NavGraph
  ↓
→ 2-3 hour fix
```

### If Sync Fails ❌
```
"Pending" status doesn't clear
  OR
"Sync failed" error message
  ↓
Retrofit base URL issue
  ↓
.baseUrl("https://CHANGE_ME_TO_ACTUAL_BACKEND_URL/api/")
  ↓
Need real backend URL from backend team
```

### If Financial Numbers Wrong ❌
```
Revenue includes DRAFT invoices
  OR
Outstanding amount is wrong
  OR
Collection rate doesn't match
  ↓
Either:
1. DRAFT exclusion not applied
2. AccountingService not wired
3. Query filters wrong
  ↓
→ Check code, likely in InvoiceDaoV2 queries
```

---

## 🔍 WHAT TO LOOK FOR (Visual Checklist)

### ✅ Good Signs (App is working)
- [ ] App loads without crashing
- [ ] Dashboard displays numbers
- [ ] SyncStatusIndicator visible (top of screen)
- [ ] Can create invoice (offline works)
- [ ] Sync banner appears when toggling airplane mode
- [ ] Numbers make financial sense

### ❌ Bad Signs (Issues to fix)
- [ ] App crashes on startup
- [ ] No SyncStatusIndicator visible
- [ ] "Pending" status never clears (API unreachable)
- [ ] Financial numbers don't add up
- [ ] Customer segments show $0 when should show amounts
- [ ] "Sync failed" error messages

---

## 📋 DATA YOU NEED TO COLLECT

While testing, note down:

```
[ ] SyncStatusIndicator visible? YES/NO
[ ] Can create invoice offline? YES/NO
[ ] Sync triggers when online? YES/NO
[ ] Sync completes or fails? COMPLETE/FAIL
[ ] Error messages (if any): ___________
[ ] Dashboard revenue correct? YES/NO
[ ] Outstanding amount correct? YES/NO
[ ] Collection rate makes sense? YES/NO
[ ] Customer segments show paid? YES/NO
```

---

## 🎯 YOUR IMMEDIATE ACTIONS

**Right now (while app is running):**

1. **Look at top of screen**
   - Is there a status banner? YES/NO
   - What does it say?

2. **Try to create an invoice**
   - Does it save? YES/NO
   - Does pending count increase? YES/NO

3. **Check dashboard numbers**
   - Do they make sense? YES/NO
   - Are DRAFT invoices included? YES/NO

4. **Report back with:**
   - Screenshots of what you see
   - Error messages (if any)
   - Which tests passed/failed

---

## ⏭️ IF EVERYTHING WORKS

Congratulations! Phase 2 is functionally complete. You just need:
1. Real backend URL
2. API contracts verified
3. Conflict resolution tested
4. E2E edge cases validated

---

## ⏭️ IF THINGS ARE BROKEN

Identify the issue:
1. **No SyncStatusIndicator** → Integrate component (2-3 hours)
2. **Sync fails** → Update Retrofit URL to real backend (1 hour)
3. **Wrong numbers** → Check AccountingService filters (2-4 hours)

---

**Status:** Awaiting your test results  
**Next Step:** Tell me what you see and we'll fix whatever's broken  


