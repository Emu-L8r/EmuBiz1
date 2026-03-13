# 🧪 PHASE 2 - DAY 5 - STREAM 1 - TEST SUITE 2: CUSTOMER OPERATIONS
**Date:** March 7, 2026  
**Objective:** Verify that customer management operations (Create, Update, Delete) correctly queue when offline  
**Prerequisite:** Suite 1 PASSING (✅ Confirmed)

---

## 📋 TEST SUITE 2 OVERVIEW

### **Goal**
Expand the verified offline-first pattern from invoices to customers, ensuring:
- ✅ Customer creation queues when offline
- ✅ Customer updates queue when offline
- ✅ Customer deletion queues when offline
- ✅ Database operations are tracked correctly
- ✅ UI displays pending sync indicators

### **Timeline**
- **Expected Duration:** 15-20 minutes
- **Teardown:** Same clean install (don't uninstall app)

---

## 🎯 TEST 2.1: CREATE CUSTOMER OFFLINE

### **Setup**
```
1. App still in Airplane Mode (from Suite 1)
2. Clear logcat: adb logcat -c
3. Open "Customers" tab
4. Tap "+" or "Add Customer" button
```

### **Test Steps**
```
Step 1: Fill Customer Form
  - Name: "Offline Test Customer" (must be unique)
  - Email: "offline.test@example.com"
  - Phone: "555-1234"
  - ABN: "11223344556" (or leave blank if optional)
  - Address: "456 Offline St, City"

Step 2: Save Customer
  - Tap "Save" button
  - ⏳ Observe: Badge appears ("⏳ Pending Sync")

Step 3: Check Logcat
  adb logcat | grep "CREATE_CUSTOMER\|Offline\|customer"

Step 4: Verify Database
  Android Studio → Database Inspector
  → offline_operations table
  → Should have a CREATE_CUSTOMER entry with serialized customer JSON

Step 5: Verify UI
  - Customer appears in list
  - "⏳ Pending Sync" badge visible
  - No errors or warnings
```

### **Expected Outcomes**

| Item | Expected | Check |
|------|----------|-------|
| **Customer Created** | YES - appears in list | ✅ Observe in app |
| **Badge Visible** | YES - "⏳ Pending Sync" | ✅ Look for badge |
| **Logcat Message** | YES - "📶 Offline detected. Queueing customer for sync." | ✅ `adb logcat \| grep` |
| **Database Entry** | YES - 1 CREATE_CUSTOMER in offline_operations | ✅ Database Inspector |
| **Entry Type** | "CREATE_CUSTOMER" | ✅ Verify operation_type |
| **Entry Data** | Valid JSON (customer object) | ✅ Check data column |
| **Entry Status** | "PENDING" | ✅ Verify status column |
| **No Crashes** | App stable, no ANR | ✅ Observe smoothness |

### **Pass/Fail Decision**

**✅ PASS if:**
- Customer appears in list
- Badge visible
- Logcat shows create message
- Database has PENDING entry

**❌ FAIL if:**
- Customer doesn't appear (lost data)
- No badge shown (UI not synced)
- No logcat message (logging broken)
- No database entry (queue broken)

---

## 🎯 TEST 2.2: UPDATE CUSTOMER OFFLINE

### **Setup**
```
1. Still offline (Airplane Mode ON)
2. Clear logcat: adb logcat -c
3. Go to Customers list
4. Tap the customer you just created (or any existing customer)
```

### **Test Steps**
```
Step 1: Open Customer Edit Screen
  - Tap "Edit" button
  - Form should populate with customer data

Step 2: Modify Data
  - Change Name: Add " - OFFLINE UPDATED"
  - Change Email: Change domain to "updated@example.com"
  - Tap "Save"

Step 3: Check Logcat
  adb logcat | grep "UPDATE_CUSTOMER\|Offline"

Step 4: Verify Database
  Android Studio → Database Inspector
  → offline_operations table
  → Should have:
     - Previous CREATE_CUSTOMER entry (PENDING)
     - NEW UPDATE_CUSTOMER entry (PENDING)

Step 5: Verify UI
  - Customer list shows updated name immediately
  - "⏳ Pending Sync" badge still visible
  - No errors
```

### **Expected Outcomes**

| Item | Expected | Check |
|------|----------|-------|
| **Customer Updated** | YES - list shows new name | ✅ Observe in app |
| **Badge Still Visible** | YES - "⏳ Pending Sync" | ✅ Look for badge |
| **Logcat Message** | YES - "💰 Queued UPDATE_CUSTOMER" | ✅ `adb logcat \| grep` |
| **Database Entry** | 2 entries total (CREATE + UPDATE) | ✅ Count in table |
| **UPDATE Status** | "PENDING" | ✅ Verify status |
| **Operation Order** | CREATE before UPDATE (by timestamp) | ✅ Check created_at |

### **Pass/Fail Decision**

**✅ PASS if:**
- Customer name updated immediately
- Badge visible
- Logcat shows update message
- Database has 2 entries (CREATE + UPDATE)

**❌ FAIL if:**
- Update doesn't appear in list
- Badge disappears (state lost)
- No logcat message
- Only 1 database entry (UPDATE not queued)

---

## 🎯 TEST 2.3: DELETE CUSTOMER OFFLINE

### **Setup**
```
1. Still offline (Airplane Mode ON)
2. Clear logcat: adb logcat -c
3. Go to Customers list
4. Find a customer you DON'T need (create new one if needed)
```

### **Test Steps**
```
Step 1: Delete Customer
  - Tap and hold customer name (or tap delete icon)
  - Confirm deletion
  - Observe: Customer disappears from list

Step 2: Check Logcat
  adb logcat | grep "DELETE_CUSTOMER\|Offline"

Step 3: Verify Database
  Android Studio → Database Inspector
  → offline_operations table
  → Should have:
     - Previous entries (CREATE, UPDATE if any)
     - NEW DELETE_CUSTOMER entry

Step 4: Verify UI
  - Customer removed from list
  - Badge still visible
  - No crash or error
```

### **Expected Outcomes**

| Item | Expected | Check |
|------|----------|-------|
| **Customer Removed** | YES - disappears from list | ✅ Observe in app |
| **Logcat Message** | YES - "🗑️ Queued DELETE_CUSTOMER" | ✅ `adb logcat \| grep` |
| **Database Entry** | DELETE_CUSTOMER in offline_operations | ✅ Database Inspector |
| **Entry Status** | "PENDING" | ✅ Verify status |
| **Total Queue Count** | 3+ (CREATE + UPDATE + DELETE) | ✅ Count total |

### **Pass/Fail Decision**

**✅ PASS if:**
- Customer removed immediately
- Logcat shows delete message
- Database has DELETE entry

**❌ FAIL if:**
- Customer still appears
- No logcat message
- No database entry

---

## 🎯 TEST 2.4: MULTIPLE CUSTOMER OPERATIONS

### **Objective**
Verify the queue handles multiple customer operations in sequence

### **Test Steps**
```
Step 1: Create 3 new customers (all while offline)
  - Customer A: "John Doe" (john@example.com)
  - Customer B: "Jane Smith" (jane@example.com)
  - Customer C: "Bob Johnson" (bob@example.com)

Step 2: Check Logcat
  adb logcat | grep "Offline\|customer"
  - Should see 3 separate "📶 Offline detected" messages

Step 3: Check Database
  → offline_operations table
  → Count: Should be 3 CREATE_CUSTOMER entries

Step 4: Modify Customer A
  - Change name to "John Doe - Updated"
  - Save
  - Check queue: Should have CREATE + UPDATE for A

Step 5: Delete Customer C
  - Delete from list
  - Check queue: Should have CREATE + DELETE for C
```

### **Expected Outcomes**

| Item | Expected |
|------|----------|
| **Queue Count** | 4+ operations (3 CREATE + 1 UPDATE + 1 DELETE) |
| **Operations Ordered** | By timestamp (CREATE before UPDATE) |
| **All Statuses** | "PENDING" |
| **UI Consistency** | All customers shown except C |
| **No Data Loss** | All operations persisted |

---

## 📊 SUITE 2 SUMMARY TABLE

| Test | Result | Evidence | Pass |
|------|--------|----------|------|
| **2.1: Create Customer Offline** | ? | Badge + Logcat + DB | ? |
| **2.2: Update Customer Offline** | ? | Badge + Logcat + DB (2 entries) | ? |
| **2.3: Delete Customer Offline** | ? | Logcat + DB + Removed | ? |
| **2.4: Multiple Ops** | ? | 4+ Queue entries | ? |

---

## 🎬 EXECUTION CHECKLIST

Before you start, ensure:
- [ ] App is still installed
- [ ] Airplane Mode is still ON
- [ ] You can access Database Inspector
- [ ] Logcat terminal is ready
- [ ] You have 15-20 minutes

During testing:
- [ ] Screenshot each result
- [ ] Note any unexpected behavior
- [ ] Copy logcat output if issues arise
- [ ] Keep database inspector open side-by-side

---

## 🚀 AFTER SUITE 2 COMPLETE

1. **Document Results** → Update PHASE_2_DAY_5_STREAM_1_TEST_RESULTS.md
2. **Assess Pass/Fail** → Suite 2 PASSING = ready for Suite 3
3. **Next Step** → Suite 3: Concurrent Operations (same session)

---

## 🆘 TROUBLESHOOTING

### **Problem: Customer doesn't save**
- **Check:** Is app still running?
- **Fix:** Restart app with Airplane Mode ON

### **Problem: No logcat message**
- **Check:** Is Timber logging working?
- **Fix:** `adb logcat | grep -i "offline\|queue\|customer"`

### **Problem: No database entry**
- **Check:** Did you look in the right table?
- **Fix:** Verify `offline_operations` table, not `invoices` or `customers`

### **Problem: Badge doesn't appear**
- **Check:** Is StateFlow triggering UI updates?
- **Fix:** Return to invoices list, come back to customers

---

## 📝 NOTES FOR RESULTS

Keep track of:
1. **Exact logcat messages** you see
2. **Database state** (how many entries after each operation)
3. **UI behavior** (timing, responsiveness)
4. **Any anomalies** (crashes, lost data, wrong badges)

---

**Estimated Time: 15-20 minutes**  
**Difficulty: Easy (same pattern as Suite 1)**  
**Risk: Very Low (offline only, no network calls)**

Ready to execute Suite 2? Report back with results! 🎉


