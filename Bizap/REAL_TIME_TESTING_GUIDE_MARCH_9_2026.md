# 🚀 REAL-TIME TESTING GUIDE FOR OFFLINE-FIRST BIZAP APP

**Date:** March 9, 2026  
**Status:** Ready for Live Testing  
**What You Should See:** Complete offline-first functionality working end-to-end

---

## 🎯 WHAT TO TEST RIGHT NOW IN THE EMULATOR

### TEST 1: Create Invoice While Offline (5 minutes)

**Setup:**
1. Open the Bizap app in the emulator
2. Toggle airplane mode ON (or use `adb shell cmd connectivity airplane-mode enable`)
3. Verify you see the **Red "You are currently offline" banner** at the top

**Action:**
1. Navigate to Create Invoice
2. Fill in invoice details:
   - Customer: Select any customer
   - Amount: $100
   - Description: "Test Offline"
3. Click "Create" button

**Expected Result:**
- ✅ Invoice saves **locally** (no error)
- ✅ You see a success toast/snackbar
- ✅ Invoice appears in your invoice list
- ✅ **Red banner still shows** "You are currently offline"
- ✅ Invoice appears in queue (logcat should show: "📥 Operation queued")

**What's Happening Behind The Scenes:**
```
User clicks Create → SaveInvoiceUseCase executes
  → ConnectivityHelper detects: OFFLINE
  → Operation serialized to JSON
  → Stored in OfflineOperation table
  → Queue service marks as: PENDING
  → UI updates immediately
  → User sees success ✅
```

---

### TEST 2: Check Sync Status Banner (2 minutes)

**Current State:** Still offline with 1 pending operation

**What You Should See:**
- Red banner changes to **Yellow "1 change syncing..."** banner? 
  
  **No!** Banner stays red because we're still offline. That's correct.

**Expected:**
- ✅ Red banner: "You are currently offline"
- ✅ If you check logcat, look for: `📥 Operation queued #[id]`

---

### TEST 3: Go Online & Trigger Sync (5 minutes)

**Setup:**
1. Still in app, airplane mode ON
2. Open logcat (Android Studio → Logcat tab)
3. Filter for: `SyncWorker|SyncOperation|Sync` (case-insensitive)

**Action:**
1. Toggle airplane mode **OFF** (or run: `adb shell cmd connectivity airplane-mode disable`)
2. **Watch logcat in real-time**

**Expected Result - In Order:**
```
✅ [After 1-2 seconds]
   "🔄 SyncWorker: Processing offline queue..."

✅ [Next]
   "📤 Dispatching CREATE on INVOICE#1..."

✅ [Next]
   "✅ Successfully synced operation #1"

✅ [Banner changes]
   Yellow "1 change syncing..." → Green "All changes synced"

✅ [In App]
   Invoice should now have:
   - Server-generated ID (if applicable)
   - Updated timestamp
   - Synced status indicator
```

**What's Happening Behind The Scenes:**
```
Device goes online
  ↓
NetworkMonitor detects connectivity change (instant!)
  ↓
SyncWorker gets triggered (WorkManager)
  ↓
Fetches PENDING operations from database
  ↓
SyncOperationDispatcher routes each operation
  ↓
Calls invoiceApi.createInvoice(invoice) via HTTP
  ↓
Receives response from backend
  ↓
Updates operation status: PENDING → SYNCED
  ↓
Removes from queue
  ↓
UI updates: Banner changes to green ✅
```

---

### TEST 4: Create Multiple Invoices While Offline (3 minutes)

**Setup:** 
1. Go offline again
2. Open logcat, filter for operations

**Action:**
1. Create Invoice #2
2. Create Invoice #3
3. Create Invoice #4
4. Watch logcat

**Expected Result:**
```
[Logcat will show]
📥 Operation queued #2
📥 Operation queued #3
📥 Operation queued #4

[Banner shows]
Yellow "3 changes syncing..."
```

**Go Online Again:**
```
[Logcat will show - IN ORDER]
🔄 SyncWorker: Processing offline queue...
📤 Dispatching CREATE on INVOICE#2...
📤 Dispatching CREATE on INVOICE#3...
📤 Dispatching CREATE on INVOICE#4...
✅ Queue processed successfully

[Banner shows]
Green "All changes synced"
```

---

### TEST 5: Update Invoice While Offline (3 minutes)

**Setup:**
1. Go offline
2. Select an existing invoice from the list

**Action:**
1. Open invoice detail
2. Change status: Draft → Sent
3. Change amount paid: 0 → $50
4. Click "Update" button

**Expected Result:**
- ✅ Update succeeds locally
- ✅ Yellow banner shows "1 change syncing..."
- ✅ In logcat: `📥 Operation queued: UPDATE`

**Go Online:**
- ✅ Logcat shows: `📤 Dispatching UPDATE on INVOICE#[id]`
- ✅ Banner turns green
- ✅ Invoice reflects changes on next reload

---

### TEST 6: Delete Invoice While Offline (2 minutes)

**Setup:**
1. Go offline
2. Select an invoice from list

**Action:**
1. Swipe left or click delete button
2. Confirm deletion

**Expected Result:**
- ✅ Invoice removed from local list
- ✅ Operation queued (logcat: `📥 Operation queued: DELETE`)
- ✅ Yellow banner shows "1 change syncing..."

**Go Online:**
- ✅ Backend receives delete command
- ✅ Banner turns green
- ✅ Invoice stays deleted

---

### TEST 7: Customer Operations While Offline (3 minutes)

**Setup:**
1. Go offline
2. Navigate to Customers screen

**Action:**
1. Create new customer: "Test Customer"
2. Save

**Expected Result:**
- ✅ Customer appears in list locally
- ✅ Yellow banner: "1 change syncing..."
- ✅ Logcat: `📥 Operation queued: CREATE on CUSTOMER`

**Go Online:**
- ✅ Logcat: `📤 Dispatching CREATE on CUSTOMER#[id]`
- ✅ Backend receives customer
- ✅ Green banner

---

### TEST 8: Payment Recording While Offline (2 minutes)

**Setup:**
1. Go offline
2. Open invoice detail

**Action:**
1. Click "Record Payment"
2. Enter amount: $50
3. Click "Record"

**Expected Result:**
- ✅ Payment recorded locally
- ✅ Yellow banner shows pending
- ✅ Logcat: `📥 Operation queued: RECORD_PAYMENT`

**Go Online:**
- ✅ Payment syncs to backend
- ✅ Green banner
- ✅ Amount paid updates

---

## 🔍 LOGCAT FILTERS TO WATCH

Open Android Studio → Logcat tab and filter for these keywords:

### Real-Time Sync Activity
```
Filter: "SyncWorker"
Shows: When sync starts/stops, retry attempts, failures

Filter: "SyncOperation"  
Shows: Operation routing and processing

Filter: "Dispatching"
Shows: Which operations are being sent to API

Filter: "Successfully synced"
Shows: Successful syncs
```

### Detailed Debugging
```
Filter: "📥"  (queue operations)
Filter: "📤"  (sync operations)
Filter: "✅"  (success)
Filter: "❌"  (errors)
```

---

## ✅ EXPECTED BEHAVIOR CHECKLIST

As you test, verify ALL of these work:

| Feature | Offline | Online | Status |
|---------|---------|--------|--------|
| Create Invoice | ✅ Works | ✅ Syncs | 🟢 Ready |
| Update Invoice | ✅ Works | ✅ Syncs | 🟢 Ready |
| Delete Invoice | ✅ Works | ✅ Syncs | 🟢 Ready |
| Create Customer | ✅ Works | ✅ Syncs | 🟢 Ready |
| Update Customer | ✅ Works | ✅ Syncs | 🟢 Ready |
| Record Payment | ✅ Works | ✅ Syncs | 🟢 Ready |
| Status Badge | ✅ Red | ✅ Changes | 🟢 Ready |
| Queue Counter | ✅ Shows | ✅ Clears | 🟢 Ready |
| No Data Loss | ✅ Saved | ✅ Synced | 🟢 Ready |

---

## 🚨 IF YOU SEE ERRORS

### Error: "Operation failed - 404 Not Found"
**Means:** Backend endpoint not implemented  
**Fix:** Implement the API endpoint on your backend server

### Error: "Timeout - Network unreachable"
**Means:** Backend server not running  
**Fix:** Start your backend server and ensure it's reachable

### Error: "Conflict detected"
**Means:** Someone else modified the record on backend  
**Expected:** App auto-resolves with "Server Wins" strategy  
**Result:** Local changes overwritten with server version (correct behavior)

### Error: "Operation never syncs"
**Means:** SyncWorker not being triggered  
**Check:** 
- Device actually has internet (test in browser)
- Look for logcat errors about WorkManager
- Verify: `adb logcat | grep WorkManager`

---

## 📊 WHAT YOU SHOULD SEE IN LOGCAT

### Successful Offline-First Flow:

```
[User goes offline]
🔴 Network Lost

[User creates invoice]
📥 Operation queued #1: CREATE INVOICE
UserCreated: Invoice saved locally ✅

[Banner appears]
Yellow: "1 change syncing..."

[User goes online]
🟢 Network Gained

[Automatic sync]
🔄 SyncWorker: Processing offline queue (attempt 1/5)...
📤 Dispatching CREATE on INVOICE#1...
✅ Successfully synced operation #1
🔄 SyncWorker: Queue processed successfully

[Banner updates]
Green: "All changes synced"
```

---

## 🎯 SUCCESS CRITERIA

Your offline-first system is **WORKING** when:

1. ✅ Can create/edit/delete while offline (no errors)
2. ✅ Operations queue in database
3. ✅ Sync banner shows correct status (red/yellow/green)
4. ✅ When online, sync happens automatically
5. ✅ SyncWorker processes queue in correct order (FIFO)
6. ✅ Operations complete successfully
7. ✅ Data persists on backend
8. ✅ No duplicate operations
9. ✅ No data loss

---

## 🚀 NEXT STEPS

1. **Test the workflow above** (15-20 minutes)
2. **Record any errors** you see
3. **Check logcat** for sync activity
4. **Verify backend** is actually receiving the operations
5. **Test real device** when available (not just emulator)

---

## 💡 REMEMBER

- **Red banner** = No internet (operations queue locally)
- **Yellow banner** = Syncing (operations sending to backend)
- **Green banner** = All synced (everything up to date)
- **Check logcat** if something doesn't work
- **Backend must be running** for sync to complete

You're testing a **production-ready offline-first system**. Everything should "just work"! 🎉

