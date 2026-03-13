# Manual QA Test Checklist — PR C Verification

**Purpose:** Step-by-step manual tests to validate that PR A and PR B fixes are working
correctly in the live app before proceeding to Week 2 (Auth/Encryption).

**Prerequisites:**
- Install the latest debug APK on a device or emulator
- Ensure you start each test section with a fresh app state (clear app data if needed)
- All monetary values are in Australian Dollars (AUD) unless noted

---

## Test 1: Create & Record Payment Flow

**Objective:** Verify that creating an invoice and recording a payment shows correct data.

### Steps

1. **Create a new invoice**
   - Open the app → Navigate to Invoices
   - Tap "New Invoice"
   - Set: Customer = any, Amount = A$100.00, Status = SENT
   - Save the invoice

2. **Verify initial state**
   - Expected: Invoice appears in list with status "SENT"
   - Expected: Amount = A$100.00, Amount Paid = A$0.00, Outstanding = A$100.00

3. **Record a payment of A$50**
   - Open the invoice detail
   - Tap "Record Payment"
   - Enter amount = A$50.00
   - Confirm payment

4. **Verify after first payment**
   - Expected: Invoice status = PARTIALLY_PAID
   - Expected: Amount Paid = A$50.00
   - Expected: Outstanding = A$50.00

5. **Check Dashboard**
   - Navigate to Dashboard
   - Expected: Revenue collected includes A$50.00
   - Expected: Outstanding balance includes A$50.00

**Result:** ☐ Pass / ☐ Fail

---

## Test 2: GUI Consistency Verification (GUI1 ↔ GUI2)

**Objective:** Verify that both GUI1 and GUI2 show identical payment numbers with no divergence.

**Setup:** Using the invoice from Test 1 (A$50 paid on A$100 invoice).

### Steps

1. **Check GUI1 Payment Analytics**
   - Navigate to GUI1 (Traditional view)
   - Open Payment Analytics / Dashboard
   - Note: Outstanding = **A$50.00**, Collected = **A$50.00**

2. **Switch to GUI2**
   - Navigate to GUI2 (Modern view)
   - Open Payment Analytics / Dashboard
   - Expected: Outstanding = A$50.00 *(same as GUI1)*
   - Expected: Collected = A$50.00 *(same as GUI1)*

3. **Record another payment in GUI2**
   - In GUI2, open the invoice
   - Record payment of A$20.00
   - Expected: Outstanding = A$30.00, Status = PARTIALLY_PAID

4. **Switch back to GUI1**
   - Navigate back to GUI1
   - Expected: Outstanding = A$30.00 *(updated, matching GUI2)*
   - Expected: No stale/cached data showing A$50.00

5. **Verify collection rate**
   - GUI1 collection rate should match GUI2 collection rate
   - Formula: Collected / (Collected + Outstanding) × 100
   - Expected: 70 / 100 × 100 = 70%

**Result:** ☐ Pass / ☐ Fail

---

## Test 3: Atomic Transaction Verification

**Objective:** Verify that payment recording is fully atomic — either the entire transaction
succeeds or it fully rolls back. No partial state should ever be persisted.

### Steps

**Method A: Force-stop during payment (real device)**

1. Create a new invoice for A$100 (SENT status)
2. Open invoice and tap "Record Payment"
3. Enter amount = A$50.00
4. **At the exact moment you confirm payment**, force-stop the app:
   - Android: Settings → Apps → Bizap → Force Stop
   - OR: Pull battery (on older devices without USB power)
5. Reopen the app
6. Navigate to the invoice

**Expected outcome (either is correct):**
- ✅ **Payment fully recorded:** Invoice shows A$50 paid, status = PARTIALLY_PAID, payment history shows the payment
- ✅ **Payment fully rolled back:** Invoice shows A$0 paid, status = SENT, no payment in history

**NOT acceptable:**
- ❌ Invoice shows partial state (e.g., A$25 paid)
- ❌ Invoice shows A$50 paid but status is still SENT
- ❌ Payment in history but invoice amountPaid not updated

**Method B: Verify via automated test (recommended)**
```bash
./gradlew :app:testDebugUnitTest --tests "*PaymentRepositoryTest*"
```
Look for `recordPayment_Atomicity - transaction rolls back when invoice not found` test passing.
This confirms Room's `withTransaction {}` rolls back correctly on failure.

**Result:** ☐ Pass / ☐ Fail

---

## Test 4: DRAFT Invoice Exclusion

**Objective:** Verify that DRAFT invoices are excluded from all analytics and revenue metrics.

### Steps

1. **Create a DRAFT invoice**
   - Create new invoice for A$500
   - Leave status as DRAFT (do not send)
   - Save

2. **Check Dashboard (GUI1)**
   - Navigate to Dashboard / Revenue Analytics
   - Expected: The A$500 DRAFT invoice does NOT appear in revenue metrics
   - Expected: Revenue total unchanged from before creating the draft

3. **Check GUI1 Payment Analytics**
   - Navigate to GUI1 Payment Analytics
   - Expected: Outstanding amount does NOT include the A$500 DRAFT invoice
   - Expected: DRAFT invoices should be excluded

4. **Check GUI2 Payment Analytics**
   - Navigate to GUI2 Analytics
   - Expected: Same as GUI1 — DRAFT invoice excluded
   - Expected: Metrics identical to GUI1

5. **Verify DRAFT shows in invoice list**
   - The invoice list should still show the DRAFT invoice
   - Only analytics/metrics should exclude it

6. **Change DRAFT → SENT**
   - Update the invoice status to SENT
   - Check Dashboard: Should now include A$500 in outstanding
   - Expected: Both GUIs show A$500 outstanding

**Result:** ☐ Pass / ☐ Fail

---

## Test 5: Multi-Payment Accumulation

**Objective:** Verify that multiple payments on a single invoice accumulate correctly,
with correct outstanding balance at each step and correct final status.

### Steps

1. **Create invoice for A$100 (SENT)**
   - Expected initial state: Outstanding = A$100, Status = SENT

2. **Record first payment: A$30**
   - Record payment of A$30.00
   - Expected: Amount Paid = A$30, Outstanding = A$70, Status = PARTIALLY_PAID

3. **Record second payment: A$20**
   - Record payment of A$20.00
   - Expected: Amount Paid = A$50, Outstanding = A$50, Status = PARTIALLY_PAID

4. **Record third payment: A$50**
   - Record payment of A$50.00
   - Expected: Amount Paid = A$100, Outstanding = A$0, Status = PAID

5. **Verify payment history**
   - Open invoice detail → Payment History
   - Expected: 3 payment records visible:
     - Payment 1: A$30
     - Payment 2: A$20
     - Payment 3: A$50
   - Expected: Total = A$100

6. **Verify final state in both GUIs**
   - GUI1: Invoice shows PAID, A$0 outstanding
   - GUI2: Invoice shows PAID, A$0 outstanding
   - Both GUIs show identical state

7. **Verify Dashboard after full payment**
   - Revenue collected should include A$100
   - Outstanding should NOT include this invoice (it's PAID)

**Result:** ☐ Pass / ☐ Fail

---

## Test 6: GUI2 New Invoice Button

**Objective:** Verify that GUI2's "New Invoice" button is enabled and the invoice
creation flow works correctly end-to-end.

### Steps

1. Navigate to GUI2 (Modern view)
2. Locate the "New Invoice" button (FAB or toolbar button)
3. Verify the button is **visible and enabled** (not greyed out / silenced)
4. Tap the button
5. Expected: Invoice creation screen/dialog opens
6. Create an invoice with amount = A$75.00
7. Expected: Invoice saved and appears in GUI2 invoice list

**Result:** ☐ Pass / ☐ Fail

---

## Summary Checklist

| Test | Description | Result |
|------|-------------|--------|
| Test 1 | Create & Record Payment Flow | ☐ Pass / ☐ Fail |
| Test 2 | GUI Consistency (GUI1 ↔ GUI2) | ☐ Pass / ☐ Fail |
| Test 3 | Atomic Transaction Verification | ☐ Pass / ☐ Fail |
| Test 4 | DRAFT Invoice Exclusion | ☐ Pass / ☐ Fail |
| Test 5 | Multi-Payment Accumulation | ☐ Pass / ☐ Fail |
| Test 6 | GUI2 New Invoice Button | ☐ Pass / ☐ Fail |

---

## Automated Test Commands

Run the full PR C verification test suite:

```bash
# All unit tests (including PaymentRepositoryTest in-memory DB tests)
cd Bizap && ./gradlew :app:testDebugUnitTest

# Run only PaymentRepositoryTest (PR A verification)
./gradlew :app:testDebugUnitTest --tests "*PaymentRepositoryTest*"

# Run only GUI consistency tests (PR B verification)
./gradlew :app:testDebugUnitTest --tests "*GUI1_GUI2_PaymentConsistencyTest*"

# Run both verification test classes
./gradlew :app:testDebugUnitTest --tests "*PaymentRepositoryTest*" --tests "*GUI1_GUI2_PaymentConsistencyTest*"
```

**All tests should pass before marking this checklist complete.**

---

*Generated as part of PR C: Comprehensive Verification — March 2026*
