# Manual QA Checklist — Bizap v1.0.0

**Purpose:** Real-device testing scenarios to verify production readiness before App Store submission.  
**Required:** Minimum 3 physical Android devices (different manufacturers/API levels recommended).

---

## Test Environment

| Device | OS Version | Required |
|--------|-----------|----------|
| Device 1 | Android 8.0+ (API 26+) | ✅ Minimum supported |
| Device 2 | Android 12 (API 31) | ✅ Common target |
| Device 3 | Android 14 (API 34) | ✅ Latest stable |

---

## Section 1: First Launch & Authentication

### 1.1 Fresh Install
- [ ] Install APK on clean device (no prior version)
- [ ] App launches without crash
- [ ] PIN setup screen appears on first launch
- [ ] PIN is accepted and stored securely
- [ ] Main screen (Landing) appears after PIN setup

### 1.2 PIN Authentication
- [ ] Correct PIN allows entry
- [ ] Incorrect PIN shows error message
- [ ] Session timeout works (app locks after inactivity period)
- [ ] Re-entering correct PIN after timeout works
- [ ] App does not crash on wrong PIN attempts

### 1.3 GUI Selection
- [ ] Landing page shows option to select GUI1 (Classic) or GUI2 (Modern)
- [ ] Selection persists across app restarts

---

## Section 2: Invoice Management (Core Feature)

### 2.1 Create Invoice
- [ ] Open invoice creation form
- [ ] Add invoice items (name, quantity, unit price)
- [ ] Select customer from existing customers
- [ ] Invoice number is auto-generated (e.g., INV-2026-000001)
- [ ] Save draft invoice
- [ ] Invoice appears in invoice list

### 2.2 Invoice Status Flow
- [ ] Create invoice → Status = DRAFT
- [ ] Send invoice → Status = SENT
- [ ] Record partial payment → Status = PARTIALLY_PAID
- [ ] Record full payment → Status = PAID
- [ ] All status changes reflect immediately in invoice list

### 2.3 Dashboard Revenue Verification (CRITICAL)
- [ ] Create a DRAFT invoice for $100
  - Dashboard revenue: **Should NOT change** (DRAFT excluded)
- [ ] Mark invoice as SENT
  - Dashboard outstanding: **Should show $100** (SENT is outstanding)
  - Dashboard revenue: **Should still be $0** (not yet paid)
- [ ] Record full payment of $100
  - Dashboard revenue: **Should show $100** (PAID counts as revenue)
  - Dashboard outstanding: **Should show $0**
- [ ] Create new invoice, record partial payment of $60 on $100 invoice
  - Dashboard revenue: **Should show $60** (amountPaid from PARTIALLY_PAID)
  - Dashboard outstanding: **Should show $40** (remaining balance)

### 2.4 Edit Invoice
- [ ] Edit existing DRAFT invoice (change amount)
- [ ] Dashboard updates to reflect edited amount
- [ ] Edit SENT invoice
- [ ] Cannot edit PAID invoice (verify appropriate restriction)

### 2.5 PDF Generation
- [ ] Generate PDF for a DRAFT invoice
- [ ] PDF contains correct customer name, invoice number, line items
- [ ] PDF contains correct totals
- [ ] PDF contains business logo (if uploaded)
- [ ] Share/export PDF works

---

## Section 3: Customer Management

### 3.1 Create Customer
- [ ] Create new customer with name, email, address
- [ ] Customer appears in customer list
- [ ] Customer is selectable when creating invoice

### 3.2 Customer Validation
- [ ] Empty name is rejected with error message
- [ ] Invalid email format is rejected
- [ ] Duplicate customer name shows warning

### 3.3 Customer Dashboard Updates
- [ ] Creating customer appears in GUI1 and GUI2 customer list
- [ ] Deleting customer removes from both UIs

---

## Section 4: Multi-GUI Consistency (CRITICAL)

### 4.1 Revenue Parity Test
1. [ ] In GUI1, note the total revenue shown on dashboard
2. [ ] Switch to GUI2 (via Landing page)
3. [ ] Note the total revenue shown on GUI2 dashboard
4. [ ] **MUST MATCH** (within display rounding)

### 4.2 Invoice Count Parity
1. [ ] In GUI1, note total invoice count
2. [ ] Switch to GUI2
3. [ ] Verify same invoice count
4. [ ] **MUST MATCH**

### 4.3 Live Update Parity
1. [ ] Open GUI1 dashboard
2. [ ] In another test session/tab, create a new invoice
3. [ ] GUI1 dashboard should update
4. [ ] Switch to GUI2
5. [ ] GUI2 dashboard should show same new data

---

## Section 5: Offline Functionality

### 5.1 Create Invoice Offline
1. [ ] Enable airplane mode
2. [ ] SyncStatusIndicator shows "Offline" at top of screen
3. [ ] Create a new invoice
4. [ ] Invoice saves locally (no error)
5. [ ] Disable airplane mode
6. [ ] SyncStatusIndicator shows syncing, then clears
7. [ ] Invoice appears in server-synced state

### 5.2 Record Payment Offline
1. [ ] Enable airplane mode
2. [ ] Record payment on an invoice
3. [ ] Payment status updates locally
4. [ ] Disable airplane mode
5. [ ] Payment syncs to server

### 5.3 Network Recovery
- [ ] App handles repeated network drops without crashing
- [ ] Queue processes pending operations in correct order after reconnect

---

## Section 6: Analytics Dashboard

### 6.1 Revenue Metrics
- [ ] MTD (Month-to-Date) revenue shows correct amount
- [ ] YTD (Year-to-Date) revenue shows correct amount
- [ ] Weekly revenue shows correct amount
- [ ] Revenue trend chart shows last 30 days

### 6.2 Outstanding Metrics
- [ ] Outstanding amount only includes SENT + PARTIALLY_PAID invoices
- [ ] DRAFT invoices do NOT appear in outstanding
- [ ] PAID invoices do NOT appear in outstanding

### 6.3 Collection Rate
- [ ] Collection rate displayed as percentage
- [ ] Rate is reasonable given paid vs total invoiced amounts

---

## Section 7: Performance

### 7.1 App Startup
- [ ] Cold start < 3 seconds
- [ ] Warm start < 1 second
- [ ] No ANR (Application Not Responding) on startup

### 7.2 Large Data Sets
- [ ] Test with 100+ invoices — invoice list scrolls smoothly
- [ ] Dashboard loads in < 1 second with 100+ invoices
- [ ] PDF generation completes in < 5 seconds

### 7.3 Memory
- [ ] App does not crash after extended use (30+ minutes)
- [ ] Memory usage stays below 150 MB in normal use

---

## Section 8: Edge Cases

- [ ] Zero-amount invoice (no line items) — handled gracefully
- [ ] Very large amounts ($999,999.99) — displays correctly
- [ ] Long customer names — truncated properly in UI
- [ ] Special characters in invoice notes — displayed correctly
- [ ] Rotating device during invoice creation — data preserved

---

## Sign-Off

| Tester | Device | OS | Date | Pass/Fail |
|--------|--------|----|------|-----------|
| | | | | |
| | | | | |
| | | | | |

**Overall QA Result:** ☐ PASS  ☐ FAIL  
**Sign-off Date:** ___________  
**Signed by:** ___________
