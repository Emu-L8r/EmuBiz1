# 🎯 PHASE 3B STAGE 1C - ISOLATION TEST CHECKLIST

**Test Date:** February 28, 2026  
**Feature:** Multi-Business Scoped Invoice Loading  
**Goal:** Verify each business has isolated data and sequences

---

## 📋 PRE-TEST SETUP

```
DEPLOYMENT STATUS:
  □ APK built successfully
  □ APK installed on device/emulator
  □ App launches without crash
  □ Dashboard visible
```

---

## 🧪 ISOLATION TEST PROTOCOL

### **STEP A: ESTABLISH BASELINE - DEFAULT BUSINESS**

**Actions:**
1. Look at Dashboard header (top of screen)
2. Note the business name displayed
3. Note the ABN/business identifier

**Verification Checklist:**
```
□ Header shows: "Default Business" (or similar)
□ ABN displayed (e.g., "ABN 12345678901")
□ Dashboard loaded successfully
□ No error messages visible
```

**Expected Result:** ✅ Dashboard shows default business context

**Your Result:**
```
Business Name: _____________________
ABN Displayed: _____________________
Status: ☐ PASS / ☐ FAIL
Notes: _____________________________
```

---

### **STEP B: CREATE INVOICE IN DEFAULT BUSINESS**

**Actions:**
1. Navigate to "Create Invoice" or "+" button
2. Fill in invoice details:
   - Customer: (select any available customer)
   - Amount: **$1234.56** (use this exact amount for tracking)
   - Description: "Stage 1C Test - Business A"
3. Click "Save" or "Create"
4. Note the assigned invoice number

**Verification Checklist:**
```
□ Invoice creation screen opened
□ Form filled successfully
□ Save/Create succeeded (no errors)
□ Invoice appears in Vault/Invoice list
□ Invoice number assigned
□ Amount shows as $1234.56
```

**Expected Invoice Number:** INV-2026-000001 (or next in sequence)

**Your Result:**
```
Invoice Created: ☐ YES / ☐ NO
Invoice Number: _____________________
Amount Correct: ☐ YES / ☐ NO
Status: ☐ PASS / ☐ FAIL
Notes: _____________________________
```

---

### **STEP C: SWITCH TO "EMU GLOBAL B" BUSINESS**

**Actions:**
1. Locate business switcher button (usually 🐛 icon or menu)
2. Tap the switcher button
3. Select "Emu Global B" from the list
4. Wait for screen to refresh
5. Verify header updates

**Verification Checklist:**
```
□ Business switcher button found
□ Dropdown/dialog opened
□ "Emu Global B" visible in list
□ Successfully switched
□ Header text updated to "Emu Global B"
□ ABN changed (different from Step A)
□ No errors during switch
```

**Expected Result:** ✅ Header shows "Emu Global B" and screen refreshes

**Your Result:**
```
Switcher Found: ☐ YES / ☐ NO
Business B Visible: ☐ YES / ☐ NO
Header Updated: ☐ YES / ☐ NO
New Header Text: _____________________
Status: ☐ PASS / ☐ FAIL
Notes: _____________________________
```

---

### **STEP D: ⭐⭐⭐ CRITICAL TEST - VERIFY VAULT IS EMPTY**

**Actions:**
1. Navigate to Vault / Invoice List
2. Observe the list of invoices
3. Count how many invoices are visible

**Verification Checklist:**
```
□ Vault screen opened successfully
□ Invoice list is EMPTY (0 invoices shown)
□ The $1234.56 invoice from Business A is NOT visible
□ Empty state message (if applicable)
□ No errors
```

**Expected Result:** ✅ Vault is EMPTY - This proves data scoping works!

**CRITICAL INTERPRETATION:**
```
✅ CORRECT: Vault is empty
   → Data is properly scoped by business
   → InvoiceDao queries are filtering correctly
   
❌ WRONG: Business A's invoice is visible
   → Scoping is broken
   → Repository not filtering by businessProfileId
```

**Your Result:**
```
Vault State: ☐ EMPTY / ☐ HAS INVOICES

If HAS INVOICES:
  Invoice Count: _____
  Is Business A invoice visible? ☐ YES / ☐ NO
  Invoice numbers visible: _____________________

Status: ☐ PASS / ☐ FAIL [CRITICAL]

If FAIL, describe what you see:
_________________________________________________
_________________________________________________
```

---

### **STEP E: ⭐⭐⭐ CRITICAL TEST - CREATE INVOICE IN BUSINESS B**

**Actions:**
1. While on Business B, click "Create Invoice"
2. Fill in details:
   - Customer: (select any available)
   - Amount: **$5678.90** (use this exact amount)
   - Description: "Stage 1C Test - Business B"
3. Click "Save" or "Create"
4. **CAREFULLY NOTE THE INVOICE NUMBER**

**Verification Checklist:**
```
□ Invoice creation succeeded
□ Invoice appears in Vault
□ Amount shows as $5678.90
□ Invoice number assigned
□ Status is DRAFT (or appropriate initial status)
```

**CRITICAL SEQUENCE ISOLATION CHECK:**

```
Expected Invoice Number: INV-2026-000001

WHY?
  - Business B is starting its own sequence
  - Should NOT continue from Business A's sequence
  - Each business has independent numbering
  
Business A had: INV-2026-000001 (or higher)
Business B should have: INV-2026-000001 (starts fresh)

✅ CORRECT: Business B invoice is INV-2026-000001
   → Sequences are isolated per business
   → generateInvoiceNumber() is scoped correctly
   
❌ WRONG: Business B invoice is INV-2026-000002 (or continues from A)
   → Sequences are NOT isolated
   → Sequence generation is broken
```

**Your Result:**
```
Invoice Created: ☐ YES / ☐ NO
Invoice Number: _____________________
Amount Correct: ☐ YES / ☐ NO

Sequence Isolation Check:
  Business A Invoice Number: _____________________ (from Step B)
  Business B Invoice Number: _____________________ (this step)
  
  Are they INDEPENDENT sequences? ☐ YES / ☐ NO
  
  Expected: Both start at 000001
  Actual: _____________________

Status: ☐ PASS / ☐ FAIL [CRITICAL]

If FAIL, describe the issue:
_________________________________________________
_________________________________________________
```

---

### **STEP F: VERIFY BUSINESS B VAULT SHOWS ONLY BUSINESS B DATA**

**Actions:**
1. Stay on Business B
2. Navigate to Vault (if not already there)
3. Count invoices visible
4. Verify only Business B invoice is shown

**Verification Checklist:**
```
□ Vault shows exactly 1 invoice
□ Invoice amount is $5678.90
□ Invoice description is "Stage 1C Test - Business B"
□ Business A's $1234.56 invoice is NOT visible
□ No duplicates
□ No errors
```

**Expected Result:** ✅ Only Business B invoice visible

**Your Result:**
```
Invoice Count: _____
Business B invoice visible: ☐ YES / ☐ NO
Business A invoice visible: ☐ YES / ☐ NO

If Business A visible (WRONG):
  This indicates scoping on READ is broken
  
Status: ☐ PASS / ☐ FAIL
Notes: _____________________________
```

---

### **STEP G: ⭐⭐⭐ CRITICAL TEST - REACTIVE SWITCHING**

**Actions:**
1. Tap business switcher button
2. Select "Default Business" (the original business)
3. **OBSERVE CAREFULLY:** Does the screen refresh instantly?
4. Navigate to Vault
5. Check if Business A's invoice reappears

**Verification Checklist:**
```
□ Switched back to Default Business
□ Header updated to show "Default Business"
□ Screen refreshed (immediately or within 1-2 seconds)
□ Navigate to Vault
□ Business A's $1234.56 invoice is VISIBLE again
□ Business B's $5678.90 invoice is NOT visible
□ All original invoices reappeared
□ No manual refresh needed
```

**CRITICAL REACTIVE SWITCHING CHECK:**

```
Expected Behavior:
  1. Switch to Business A
  2. flatMapLatest() cancels Business B query
  3. New query executes: WHERE businessProfileId = 1
  4. UI updates instantly with Business A data
  
✅ CORRECT: Data reappears instantly (<1 second)
   → Reactive streams working correctly
   → flatMapLatest is wired properly
   → InvoiceRepository is observing activeProfile
   
⚠️ ACCEPTABLE: Data reappears after 1-3 seconds
   → Feature works but could be optimized
   
❌ WRONG: Data doesn't reappear or manual refresh needed
   → Reactive switching is broken
   → Flow not observing activeProfile changes
```

**Your Result:**
```
Switched Back: ☐ YES / ☐ NO
Header Updated: ☐ YES / ☐ NO

Vault State After Switch:
  Business A invoice visible: ☐ YES / ☐ NO
  Business B invoice visible: ☐ YES / ☐ NO
  
  Invoice count: _____
  
Refresh Timing:
  ☐ Instant (<1 second)
  ☐ Delayed (1-3 seconds)
  ☐ Very slow (>3 seconds)
  ☐ Didn't refresh (manual refresh needed)

Status: ☐ PASS / ☐ FAIL [CRITICAL]

If FAIL, describe what happened:
_________________________________________________
_________________________________________________
```

---

## 📊 TEST RESULTS SUMMARY

### **Overall Test Status**

```
STEP A (Baseline):           ☐ PASS / ☐ FAIL
STEP B (Create A):           ☐ PASS / ☐ FAIL
STEP C (Switch B):           ☐ PASS / ☐ FAIL
STEP D (Empty) [CRITICAL]:   ☐ PASS / ☐ FAIL
STEP E (Sequence) [CRITICAL]:☐ PASS / ☐ FAIL
STEP F (Only B):             ☐ PASS / ☐ FAIL
STEP G (Reactive) [CRITICAL]:☐ PASS / ☐ FAIL

Critical Tests Passed: ___/3
Overall Tests Passed: ___/7
```

### **Feature Status**

```
☐ ALL TESTS PASSED ✅
  → Multi-business scoping is PRODUCTION-READY
  → Feature is COMPLETE
  → Ready for Phase 3B Stage 2
  
☐ SOME TESTS FAILED ⚠️
  → Issues require investigation
  → List failed tests below
  
☐ CRITICAL TESTS FAILED ❌
  → Feature is BROKEN
  → Requires debugging and fixes
```

---

## 🔍 DEBUGGING INFORMATION (If Tests Failed)

### **Failed Test Details**

```
Test Name: ___________________________
Expected: ____________________________
Actual: ______________________________
Error Message (if any): ______________
_______________________________________
```

### **Logcat Errors (If Applicable)**

```
Run this command to capture errors:
  adb logcat | Select-String "bizap|Exception|Error"

Paste relevant error lines:
_______________________________________
_______________________________________
_______________________________________
```

### **Screenshots (If Possible)**

```
Helpful screenshots to capture:
  □ Dashboard showing business name
  □ Vault when empty (Step D)
  □ Invoice creation with number (Step E)
  □ Vault showing only Business B (Step F)
  □ Vault after switching back (Step G)
```

---

## 🎯 FINAL DECLARATION

```
I, ________________, have completed the Phase 3B Stage 1C
Isolation Test on February 28, 2026.

Overall Result: ☐ PASS / ☐ FAIL

Confidence in Multi-Business Scoping: ☐ HIGH / ☐ MEDIUM / ☐ LOW

Ready to proceed to Stage 2: ☐ YES / ☐ NO

Additional Notes:
_________________________________________________
_________________________________________________
_________________________________________________
```

---

## 📋 NEXT STEPS

### **If ALL TESTS PASSED:**
```
✅ Phase 3B Stage 1C is COMPLETE
✅ Multi-business isolation works correctly
✅ Ready for Phase 3B Stage 2: Multi-Currency & Exchange Rates
```

### **If SOME TESTS FAILED:**
```
⚠️ Debug failed tests
⚠️ Fix issues
⚠️ Rebuild and retest
⚠️ Verify fixes before proceeding
```

### **If CRITICAL TESTS FAILED:**
```
❌ Stop here
❌ Full debugging session needed
❌ Review InvoiceRepositoryImpl, InvoiceDao, InvoiceMapper
❌ Check BusinessProfileRepository.activeProfile Flow
❌ Verify flatMapLatest implementation
```

---

**END OF ISOLATION TEST CHECKLIST**

