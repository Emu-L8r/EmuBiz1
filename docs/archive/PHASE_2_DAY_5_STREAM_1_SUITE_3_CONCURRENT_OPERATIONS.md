# 🧪 PHASE 2 - DAY 5 - STREAM 1 - TEST SUITE 3: CONCURRENT OPERATIONS
**Date:** March 7, 2026  
**Objective:** Verify the queue handles multiple concurrent operations (invoices + customers) with data integrity  
**Prerequisite:** Suite 2 PASSING (✅ To be confirmed)

---

## 📋 TEST SUITE 3 OVERVIEW

### **Goal**
This is the **stress test** that proves the system is robust enough for real-world use:
- ✅ Multiple operations fire simultaneously
- ✅ Queue maintains FIFO ordering
- ✅ Database stays consistent
- ✅ No race conditions
- ✅ UI handles high-frequency updates

### **Timeline**
- **Expected Duration:** 20-25 minutes
- **Complexity:** Medium (coordinating multiple operations)

---

## 🎯 CRITICAL FLOW

### **Test Scenario: "Invoice for New Customer - Offline"**
This mimics the real-world user journey:
1. User offline, wants to create invoice
2. Customer doesn't exist yet
3. User creates customer + invoice + records payment all while offline
4. All 3 operations queue in correct order

### **Queue Order Expected**
```
[1] CREATE_CUSTOMER (Customer "Acme Corp")
[2] CREATE_INVOICE (Invoice for Acme Corp)
[3] RECORD_PAYMENT (Payment received)

When online, sync worker will execute: [1] → [2] → [3]
```

---

## 🎯 TEST 3.1: CREATE CUSTOMER + CREATE INVOICE (BACK-TO-BACK)

### **Setup**
```
1. App in Airplane Mode (still offline from previous tests)
2. Clear logcat: adb logcat -c
3. Open "Customers" tab
```

### **Test Steps**

#### **Phase A: Create Customer (30 seconds)**
```
Step 1: Add New Customer
  - Name: "Acme Corporation" (unique)
  - Email: "acme@example.com"
  - Phone: "555-9999"
  - Address: "789 Business Ave"
  - Save button

Step 2: Observe
  - Customer appears in list with "⏳ Pending Sync"
  - Note the timestamp of creation
```

#### **Phase B: Create Invoice for Same Customer (30 seconds)**
```
Step 1: Go to Invoices tab
  
Step 2: Create New Invoice
  - Customer: "Acme Corporation" (the one you just created)
  - Item: "Professional Services"
  - Amount: A$2500
  - Status: SENT
  - Save button

Step 3: Observe
  - Invoice appears in list
  - "⏳ Pending Sync" badge visible
```

#### **Phase C: Verify Queue Order (Immediate)**
```
Step 1: Check Logcat
  adb logcat | grep "Offline\|Queued"
  
  Expected output (in order):
  📶 Offline detected. Queueing customer for sync.
  📶 Offline detected. Queueing invoice for sync.

Step 2: Check Database
  Android Studio → Database Inspector → offline_operations
  
  Should see:
  [1] operation_id: 1, operation_type: CREATE_CUSTOMER, status: PENDING, created_at: T1
  [2] operation_id: 2, operation_type: CREATE_INVOICE, status: PENDING, created_at: T2
  
  Where T1 < T2 (CREATE_CUSTOMER before CREATE_INVOICE)

Step 3: Verify Data Integrity
  - Customer data serialized correctly
  - Invoice references customer_id correctly
  - No null fields, no corruption
```

### **Expected Outcomes**

| Check | Expected | Status |
|-------|----------|--------|
| **Customer Created** | ✅ YES (appears in list) | ? |
| **Invoice Created** | ✅ YES (appears in list) | ? |
| **Queue Order Correct** | ✅ CUSTOMER (id:1) before INVOICE (id:2) | ? |
| **Both Badges Visible** | ✅ YES ("⏳ Pending Sync" on both) | ? |
| **Logcat Shows Both** | ✅ YES (2 "Offline detected" messages) | ? |
| **No Crashes** | ✅ YES (app stable) | ? |
| **Data Not Corrupted** | ✅ YES (JSON valid, no null fields) | ? |

### **Pass/Fail Decision**

**✅ PASS if:**
- 2 operations in database
- Correct order (CREATE_CUSTOMER before CREATE_INVOICE)
- Both have PENDING status
- Both visible in UI
- No data corruption

**❌ FAIL if:**
- Only 1 operation queued (other lost)
- Wrong order (INVOICE before CUSTOMER)
- Any null fields in JSON data
- App crashes or hangs

---

## 🎯 TEST 3.2: RAPID-FIRE INVOICES (BURST TEST)

### **Objective**
Create 5 invoices in rapid succession to test queue under load

### **Setup**
```
1. Still offline (Airplane Mode ON)
2. Clear logcat: adb logcat -c
3. Go to Invoices tab
4. Have customer list ready (you have at least 2-3 customers from previous tests)
```

### **Test Steps**
```
Step 1: Create 5 Invoices Rapidly
  Invoice 1: Customer A, A$500, DRAFT
  Invoice 2: Customer B, A$750, SENT
  Invoice 3: Customer C, A$1200, SENT
  Invoice 4: Customer A, A$600, DRAFT (same customer as Invoice 1)
  Invoice 5: Customer B, A$400, SENT (same customer as Invoice 2)
  
  ⏱️ Try to complete all 5 within 2-3 minutes
  Don't wait for UI updates, just keep saving

Step 2: Check Logcat After Each
  adb logcat -c  (clear before test)
  [Create all 5]
  adb logcat | grep "Queued\|Offline"
  
  Expected: 5 separate "Offline detected" messages
  (or consolidated batch message if optimization is in place)

Step 3: Verify Database
  Android Studio → Database Inspector → offline_operations
  
  Expected state:
  - 5+ CREATE_INVOICE entries
  - Plus any prior CREATE_CUSTOMER + CREATE_INVOICE from Test 3.1
  - Total: ~7-10 entries depending on earlier tests
  
  Example queue state:
  [1] CREATE_CUSTOMER (Acme Corporation)
  [2] CREATE_INVOICE (Acme invoice)
  [3] CREATE_CUSTOMER (John Doe) [if created in Suite 2]
  [4] CREATE_INVOICE (John invoice 1)
  [5] CREATE_INVOICE (John invoice 2)
  [6] CREATE_INVOICE (John invoice 3)
  [7] CREATE_INVOICE (John invoice 4)
  [8] CREATE_INVOICE (John invoice 5)

Step 4: Check UI State
  - All 5 invoices appear in list
  - All have "⏳ Pending Sync" badge
  - List is scrollable (no freeze)
  - No duplicates
```

### **Expected Outcomes**

| Check | Expected | Status |
|-------|----------|--------|
| **All 5 Created** | ✅ YES (all visible in list) | ? |
| **All Queued** | ✅ YES (5 CREATE_INVOICE entries) | ? |
| **Correct Order** | ✅ YES (by timestamp) | ? |
| **All PENDING** | ✅ YES (status column) | ? |
| **No Duplicates** | ✅ YES (IDs unique) | ? |
| **UI Responsive** | ✅ YES (no freezing) | ? |
| **No Data Loss** | ✅ YES (all 5 present) | ? |

### **Pass/Fail Decision**

**✅ PASS if:**
- All 5 invoices in database queue
- Correct order by timestamp
- All have PENDING status
- No duplicates (5 unique operation_ids)
- UI handles rapid updates smoothly

**❌ FAIL if:**
- Only 3-4 invoices queued (some lost)
- Wrong order or missing entries
- Duplicates in queue
- UI becomes unresponsive
- Crashes during rapid creation

---

## 🎯 TEST 3.3: MIXED OPERATIONS (INVOICES + CUSTOMERS + PAYMENTS)

### **Objective**
Create a realistic scenario with all 3 operation types interleaved

### **Setup**
```
1. Still offline (Airplane Mode ON)
2. Clear logcat: adb logcat -c
3. Open Invoices tab
```

### **Test Steps**

```
Step 1: Create Customer X
  Name: "Customer X", Email: "customerx@example.com"
  [Wait for UI to show customer]

Step 2: Create Invoice for Customer X
  Amount: A$3000, Status: SENT
  [Wait for UI to show invoice]

Step 3: Record Payment on Invoice X
  Amount: A$1500
  [Wait for UI to update outstanding]

Step 4: Create Another Invoice for Different Customer
  Customer: "Customer A"
  Amount: A$800, Status: DRAFT

Step 5: Update Customer X
  Go to Customers tab
  Find "Customer X"
  Change email to "customerx.updated@example.com"
  Save

Step 6: Record Another Payment (Invoice X)
  Go back to invoices
  Find Customer X invoice
  Record A$1500 more (should be fully paid now)

Step 7: Check Final Queue State
```

### **Expected Queue Order**
```
[1] CREATE_CUSTOMER (Customer X)
[2] CREATE_INVOICE (Customer X invoice, A$3000)
[3] RECORD_PAYMENT (Customer X, A$1500)
[4] CREATE_INVOICE (Customer A, A$800)
[5] UPDATE_CUSTOMER (Customer X email)
[6] RECORD_PAYMENT (Customer X, A$1500)

Timestamp rule: Operations in order they were created
```

### **Verification**
```
Check 1: Database Order
  Verify operations are in timestamps order [1] → [2] → [3] → [4] → [5] → [6]

Check 2: Operation Types
  Verify mix: CREATE_CUSTOMER + CREATE_INVOICE + RECORD_PAYMENT (3x)

Check 3: Data Integrity
  - Customer X email: "customerx.updated@example.com" (latest value)
  - Invoice outstanding: A$0 (fully paid: 3000 - 1500 - 1500)
  - No null fields

Check 4: UI State
  - Customer X shows updated email
  - Invoice shows A$0 outstanding
  - "⏳ Pending Sync" visible on all
```

### **Expected Outcomes**

| Check | Expected | Status |
|-------|----------|--------|
| **Total Queue Entries** | ✅ 6+ | ? |
| **Operation Variety** | ✅ 3 types (CREATE_CUSTOMER, CREATE_INVOICE, RECORD_PAYMENT, UPDATE_CUSTOMER) | ? |
| **Correct Order** | ✅ By timestamp | ? |
| **Data Integrity** | ✅ No corruption, no nulls | ? |
| **Financial Accuracy** | ✅ Outstanding = 0 for Customer X | ? |

---

## 📊 SUITE 3 SUMMARY TABLE

| Test | Result | Evidence | Pass |
|------|--------|----------|------|
| **3.1: Customer + Invoice Order** | ? | 2 entries in correct order | ? |
| **3.2: Rapid-Fire Invoices** | ? | 5 CREATE_INVOICE entries, no loss | ? |
| **3.3: Mixed Operations** | ? | 6+ entries, correct order, data integrity | ? |

---

## 🔬 ADVANCED VERIFICATION (Optional)

If you want to go deeper:

### **SQL Query Check**
```sql
-- In Android Studio Database Inspector, run:

-- Count operations by type
SELECT operation_type, COUNT(*) as count 
FROM offline_operations 
WHERE status = 'PENDING' 
GROUP BY operation_type;

-- Verify order
SELECT operation_id, operation_type, created_at, status 
FROM offline_operations 
ORDER BY created_at ASC;

-- Check data integrity (no nulls)
SELECT operation_id, operation_type, data 
FROM offline_operations 
WHERE data IS NULL OR data = '';
```

### **Expected SQL Results**
```
Operation Types:
  CREATE_CUSTOMER: 3-4
  CREATE_INVOICE: 5-8
  RECORD_PAYMENT: 2-3
  UPDATE_CUSTOMER: 1-2
  UPDATE_INVOICE: 0-1

Order Check:
  All operations ordered by created_at (ascending)

Data Integrity:
  0 rows with NULL data (all entries have valid JSON)
```

---

## 📝 KEY METRICS FOR SUITE 3

| Metric | Target | Actual |
|--------|--------|--------|
| **Queue Integrity** | 100% | ? |
| **Operation Order** | Correct (FIFO by timestamp) | ? |
| **Data Corruption** | 0 issues | ? |
| **UI Responsiveness** | No lag/freeze | ? |
| **App Stability** | 0 crashes | ? |

---

## 🎬 EXECUTION CHECKLIST

- [ ] Airplane Mode ON
- [ ] Logcat terminal ready
- [ ] Database Inspector open
- [ ] Phone not locked
- [ ] Have 20-25 minutes
- [ ] Read all steps before starting
- [ ] Note any anomalies immediately

---

## 🚀 AFTER SUITE 3 COMPLETE

**If all tests pass:**
1. Document results in PHASE_2_DAY_5_STREAM_1_TEST_RESULTS.md
2. Update the completion status
3. Assess readiness for SyncWorker (Week 2)
4. Proceed with Phase 2 Week 2 planning

**If any test fails:**
1. Capture logcat output
2. Screenshot database state
3. Document failure scenario
4. Report for debugging

---

## 💡 SUCCESS CRITERIA FOR SUITE 3

All tests PASS if:
- ✅ 0 operations lost (all created invoices/customers in queue)
- ✅ Correct FIFO order maintained
- ✅ No data corruption (valid JSON, no nulls)
- ✅ UI responsive (no freezing/lag)
- ✅ No crashes (app stable throughout)
- ✅ "⏳ Pending Sync" badges accurate

---

**Estimated Time: 20-25 minutes**  
**Difficulty: Medium (requires multi-step coordination)**  
**Risk: Very Low (offline only, stress testing a queue)**

When ready, proceed to Suite 3 and report results! 🎉


