# Manual Testing Checklist

**Purpose**: Comprehensive manual test flow to verify data consistency across all dashboards after Phase 1–3 implementation.  
**Frequency**: Run after each release candidate build.  
**Estimated time**: 45–60 minutes for full suite.

---

## Pre-requisites

- [ ] Install latest debug APK on a test device or emulator
- [ ] Clear app data to start with a fresh state (Settings → Apps → Bizap → Clear Data)
- [ ] Ensure device is **online** for Part 1–3
- [ ] Have another device ready for offline testing (Part 4) or use Airplane Mode

---

## PART 1: Create Invoice → Verify All Dashboards

### 1.1 Create a Test Invoice

1. [ ] Open Bizap → Select business profile
2. [ ] Navigate to Invoices → Create New Invoice
3. [ ] Fill in:
   - Customer: Test Customer
   - Items: 2 line items (e.g. $500 + $1,000)
   - Total: $1,500
   - Status: SENT
4. [ ] Save the invoice
5. [ ] Note the invoice number (e.g. INV-2026-000001)

### 1.2 Check Dashboard (GUI1)

6. [ ] Navigate to Dashboard (GUI1)
7. [ ] **Verify**: Total invoices count increased by 1
8. [ ] **Verify**: Outstanding amount includes $1,500 (150,000¢)
9. [ ] **Verify**: Revenue totals updated

### 1.3 Check Revenue Dashboard (GUI1)

10. [ ] Navigate to Revenue Dashboard
11. [ ] **Verify**: MTD Revenue shows the new invoice amount
12. [ ] **Verify**: YTD Revenue shows the new invoice amount
13. [ ] **Verify**: 30-day trend chart has a data point for today

### 1.4 Check Payment Analytics (GUI2)

14. [ ] Switch to GUI2 mode
15. [ ] Navigate to Payment Analytics
16. [ ] **Verify**: Outstanding amount = $1,500
17. [ ] **Verify**: Sent count = 1
18. [ ] **Verify**: Collection rate = 0% (no payments yet)

### 1.5 Check Risk Dashboard (GUI1)

19. [ ] Navigate to Risk Dashboard (GUI1)
20. [ ] **Verify**: Invoice appears in "Current" category (not overdue)

---

## PART 2: Record Payment → Verify All Dashboards

### 2.1 Record a Partial Payment

1. [ ] Find the invoice created in Part 1
2. [ ] Record a partial payment: $750 (50%)
3. [ ] Note payment date (today)
4. [ ] Confirm payment is saved

### 2.2 Verify Dashboards Updated

5. [ ] Navigate to Payment Analytics (GUI2)
6. [ ] **Verify**: Outstanding amount = $750 (down from $1,500)
7. [ ] **Verify**: Collected amount = $750
8. [ ] **Verify**: Invoice status = PARTIALLY_PAID
9. [ ] **Verify**: Collection rate = 50%

10. [ ] Navigate to Revenue Dashboard (GUI1)
11. [ ] **Verify**: Total paid revenue updated to include $750

### 2.3 Record Full Payment

12. [ ] Record remaining payment: $750
13. [ ] **Verify**: Invoice status = PAID
14. [ ] **Verify**: Outstanding amount = $0
15. [ ] **Verify**: Collected amount = $1,500
16. [ ] **Verify**: Collection rate = 100%

---

## PART 3: Change Invoice Status → Verify Risk Dashboard

### 3.1 Create an Overdue Invoice

1. [ ] Create a new invoice with:
   - Due date: **in the past** (e.g. 30 days ago)
   - Amount: $500
   - Status: SENT
2. [ ] Manually update status to OVERDUE (or wait for overdue detection)

### 3.2 Verify Risk Classification

3. [ ] Navigate to Risk Dashboard
4. [ ] **Verify**: Invoice appears in overdue list
5. [ ] **Verify**: At-risk count increased by 1 (overdue 30 days)
6. [ ] **Verify**: Outstanding amount includes $500

### 3.3 Verify Cross-Dashboard Consistency

7. [ ] Check Dashboard (GUI1): outstanding includes new invoice
8. [ ] Check Payment Analytics (GUI2): overdue count matches
9. [ ] **Critical**: All three dashboards must show the **same outstanding total**

---

## PART 4: Toggle Offline → Create Invoice → Go Online → Check Sync

### 4.1 Create Invoice While Offline

1. [ ] Enable Airplane Mode on device
2. [ ] Open Bizap (should still work)
3. [ ] Create a new invoice:
   - Amount: $2,000
   - Status: SENT
4. [ ] **Verify**: Invoice is queued (you see a sync pending indicator or success message)
5. [ ] **Verify**: App does NOT crash

### 4.2 Restore Connectivity

6. [ ] Disable Airplane Mode
7. [ ] Wait 10–15 seconds for background sync
8. [ ] Navigate to Invoice List
9. [ ] **Verify**: Offline-created invoice appears in the list
10. [ ] **Verify**: Invoice has correct total ($2,000)

### 4.3 Verify Dashboard Reflects Synced Invoice

11. [ ] Navigate to Payment Analytics (GUI2)
12. [ ] **Verify**: Outstanding amount increased by $2,000
13. [ ] **Verify**: Total invoice count increased by 1

---

## PART 5: Switch Businesses → Verify Data Isolation

### 5.1 Setup

1. [ ] Ensure you have at least 2 business profiles configured
2. [ ] Note the metrics for Business A

### 5.2 Switch Business

3. [ ] Switch to Business B
4. [ ] **Verify**: All dashboards show Business B's data (not Business A's)
5. [ ] **Verify**: Invoice count is specific to Business B
6. [ ] **Verify**: Revenue metrics are specific to Business B

### 5.3 Switch Back

7. [ ] Switch back to Business A
8. [ ] **Verify**: Business A metrics are restored exactly as noted in Step 2
9. [ ] **Critical**: No data leakage between businesses

---

## PART 6: View Reports → Verify No Stale Data

### 6.1 Data Freshness Check

1. [ ] Create a new invoice (any amount)
2. [ ] Navigate immediately to Revenue Dashboard
3. [ ] **Verify**: Dashboard reflects the new invoice within 1–2 seconds (reactive update)
4. [ ] Do NOT require a manual refresh

### 6.2 Snapshot Health Check

5. [ ] If SnapshotHealthWarning appears, note the discrepancy
6. [ ] Verify the health warning disappears after data sync
7. [ ] **Verify**: No phantom $0 values on dashboards

### 6.3 Multi-Currency (If Applicable)

8. [ ] Create invoices in different currencies (USD, EUR, AUD)
9. [ ] **Verify**: Currency breakdown shows correct amounts
10. [ ] **Verify**: Totals are not mixed between currencies

---

## PART 7: Edge Cases

### 7.1 Large Numbers

1. [ ] Create invoice for $999,999.99
2. [ ] **Verify**: Amount displays correctly (no overflow)
3. [ ] **Verify**: Dashboard totals update correctly

### 7.2 Zero-Amount Invoice

4. [ ] Attempt to create invoice with $0 total
5. [ ] **Verify**: Validation prevents saving

### 7.3 Rapid Operations

6. [ ] Create 5 invoices quickly in succession
7. [ ] **Verify**: All 5 appear in the invoice list
8. [ ] **Verify**: Totals are accurate (no race conditions)

---

## Pass/Fail Criteria

| Section | Criteria | Pass | Fail |
|---------|----------|------|------|
| Part 1 | All dashboards show same outstanding | ✅ | ❌ |
| Part 2 | Outstanding reduces correctly after payment | ✅ | ❌ |
| Part 3 | Overdue invoices appear in risk dashboard | ✅ | ❌ |
| Part 4 | Offline invoice syncs successfully | ✅ | ❌ |
| Part 5 | Business data is isolated | ✅ | ❌ |
| Part 6 | Dashboards refresh reactively, no stale data | ✅ | ❌ |
| Part 7 | Edge cases handled without crashes | ✅ | ❌ |

---

## Known Issues / Notes

- If you see `$0` on the Revenue Dashboard when invoices exist, this is the snapshot bug this PR fixes. Report with exact steps to reproduce.
- The Snapshot Health Warning component will surface data inconsistencies automatically.
- All financial calculations now use `InvoiceDaoV2` direct queries (no snapshot dependency).

---

## Sign-off

| Tester | Date | Device | Pass/Fail | Notes |
|--------|------|--------|-----------|-------|
| | | | | |
| | | | | |
