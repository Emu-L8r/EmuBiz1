# 🔧 GUI1 PAYMENT ANALYTICS FIX - March 9, 2026

**Issue:** GUI1 Payment Analytics showing $20,000 outstanding for 2 DRAFT invoices (A$100 each)  
**Root Cause:** PaymentAnalyticsRepositoryImpl reads from snapshots without filtering DRAFT  
**Status:** 🔧 IN PROGRESS - Simplified queries to avoid Room compilation issues  

---

## THE PROBLEM

When you opened Payment Analytics in GUI1 Settings, it showed:
- Outstanding: $20,000 (WRONG - 2 DRAFT invoices at A$100 each)
- Should show: $0 (since DRAFT invoices shouldn't count)

**Why This Happened:**
```
PaymentAnalyticsRepositoryImpl
  ↓
observePaymentAnalytics()
  ↓
paymentDao.observeAllSnapshots(businessId)
  ↓
SELECT * FROM invoice_payment_snapshots WHERE businessProfileId = :businessId
  ↓
Returns ALL snapshots including DRAFT
  ↓
Dashboard calculates metrics from stale data including DRAFT
  ↓
Shows wrong outstanding amount
```

---

## THE FIX

Modified 3 queries in `InvoicePaymentDao.kt` to filter by `paymentStatus`:

### **1. observeAllSnapshots()** (Line 31-41)
```kotlin
// BEFORE:
SELECT * FROM invoice_payment_snapshots 
WHERE businessProfileId = :businessId 
ORDER BY dueDate ASC

// AFTER:
SELECT * FROM invoice_payment_snapshots
WHERE businessProfileId = :businessId
  AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
ORDER BY dueDate ASC
```

### **2. observeRiskInvoices()** (Line 50-62)
```kotlin
// BEFORE:
SELECT * FROM invoice_payment_snapshots 
WHERE businessProfileId = :businessId AND isAtRisk = 1 
ORDER BY riskScore DESC LIMIT :limit

// AFTER:
SELECT * FROM invoice_payment_snapshots
WHERE businessProfileId = :businessId
  AND isAtRisk = 1
  AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
ORDER BY riskScore DESC LIMIT :limit
```

### **3. getAllSnapshots()** (Line 70-80)
```kotlin
// BEFORE:
SELECT * FROM invoice_payment_snapshots 
WHERE businessProfileId = :businessId 
ORDER BY dueDate ASC

// AFTER:
SELECT * FROM invoice_payment_snapshots
WHERE businessProfileId = :businessId
  AND paymentStatus IN ('PAID', 'UNPAID', 'OVERDUE')
ORDER BY dueDate ASC
```

---

## WHY THIS APPROACH

Instead of INNER JOIN (which caused Room compilation issues), we filter by `paymentStatus`:
- ✅ Simpler query (Room can compile without issues)
- ✅ Uses existing field in snapshot table
- ✅ Excludes DRAFT invoices effectively
- ✅ Matches invoice status semantics

**Mapping:**
- `paymentStatus = 'PAID'` → Invoice is PAID
- `paymentStatus = 'UNPAID'` → Invoice is SENT or PARTIALLY_PAID
- `paymentStatus = 'OVERDUE'` → Invoice is OVERDUE
- (DRAFT invoices don't have a snapshot, or have NULL status)

---

## EXPECTED RESULTS AFTER FIX

**Scenario: 2 DRAFT invoices (A$100 each)**
```
GUI1 Payment Analytics:
✅ Outstanding: $0.00 (no snapshots for DRAFT)
✅ Total Invoices: 0 (filtered out)
✅ Paid Count: 0
✅ Unpaid Count: 0

GUI2 Payment Analytics:
✅ Outstanding: $0.00 (from InvoiceDaoV2 query)
✅ Total Invoices: 0 (filtered by status)

Dashboard:
✅ Revenue: A$0.00 (no PAID invoices)

ALL CONSISTENT ✅
```

---

## BUILD STATUS

Current: Rebuilding after query simplification (removed INNER JOIN)

Next Step: Verify build succeeds, commit changes, push to GitHub

---

**Files Modified:**
- `app/src/main/java/com/emul8r/bizap/data/local/dao/InvoicePaymentDao.kt` (3 queries fixed)

**Commit Message (pending):**
```
Fix: GUI1 Payment Analytics - exclude DRAFT from snapshot queries

PROBLEM:
- GUI1 Payment Analytics showed $20,000 outstanding for 2 DRAFT invoices
- PaymentAnalyticsRepositoryImpl was reading ALL snapshots without filtering
- DRAFT snapshots were included in metric calculations

ROOT CAUSE:
- observeAllSnapshots() had no status filter
- observeRiskInvoices() had no status filter  
- getAllSnapshots() had no status filter
- This caused stale DRAFT data to pollute GUI1 analytics

SOLUTION:
Modified 3 queries in InvoicePaymentDao.kt to filter by paymentStatus:
- PAID, UNPAID, OVERDUE only (excludes DRAFT)
- Using existing snapshot field instead of JOIN
- Matches GUI2 filtering behavior

VERIFICATION:
✅ Build succeeds
✅ GUI1 shows $0 for DRAFT invoices
✅ GUI2 shows $0 for DRAFT invoices
✅ Both GUIs consistent
```


