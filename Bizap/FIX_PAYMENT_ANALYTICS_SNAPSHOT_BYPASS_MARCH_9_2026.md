# Fix: Payment Analytics Snapshot Bypass - March 9, 2026

**Date:** March 9, 2026  
**Issue:** Payment Analytics showing stale/incorrect data (outstanding amounts 100x off, wrong collection rates, 0 paid invoices)  
**Root Cause:** Payment Analytics was querying stale `invoice_payment_snapshots` table instead of live `invoices` table  
**Solution:** Bypass snapshots entirely - query invoices table directly as source of truth

---

## The Problem

When you marked an invoice as PAID:
- Payment Analytics showed: **Outstanding = 82200** (should be 0)
- Collection Rate: **37.8%** (should be 100%)
- Paid Count: **0 of 3** (should be 1)
- Customer Segments: **Top customer paid 0 dollars** (should show actual amount)
- GUI2 Dashboard: **$322 outstanding, $500 paid** (wrong values)

### Why This Happened

The `PaymentAnalyticsRepositoryImpl.getPaymentAnalytics()` method was:
1. Querying `invoice_payment_snapshots` table
2. Snapshots weren't being updated when you changed invoice status
3. If snapshot didn't exist for an invoice, old/stale data persisted
4. Payment Analytics showed outdated numbers

**The snapshots are a cache that was getting out of sync with the source of truth (invoices table).**

---

## The Fix: Three Changes

### 1. Payment Analytics Now Queries Invoices Table Directly

**File:** `PaymentAnalyticsRepositoryImpl.kt` (lines 142-177)

**Changed from:**
```kotlin
// ❌ OLD: Query stale snapshots
val metricsRow = paymentDao.getPaymentMetrics(businessId)
val agingRow = paymentDao.getOutstandingByAging(businessId)
// Returns stale data if snapshots weren't updated
```

**Changed to:**
```kotlin
// ✅ NEW: Query invoices table directly (source of truth)
val calculated = invoiceDao.calculatePaymentMetrics(businessId)

// Returns real-time metrics calculated from actual invoice records
PaymentAnalyticsSummary(
    totalInvoices = calculated.totalInvoices,
    paidInvoices = calculated.paidInvoices,
    totalInvoiceAmount = calculated.totalAmount.toDouble() / 100.0,  // Convert cents to dollars
    totalPaidAmount = calculated.paidAmount.toDouble() / 100.0,
    totalOutstandingAmount = calculated.totalOutstanding.toDouble() / 100.0,
    collectionRate = calculated.collectionRate
)
```

**Why:** The `invoices` table is the single source of truth. When you change an invoice status, the invoices table updates immediately. Snapshots are optional caching and shouldn't block analytics.

### 2. Revenue Queries Filter Correctly

**File:** `InvoiceDaoV2.kt` (lines 60-100)

**Fixed:**
```kotlin
// ✅ Only count PAID and PARTIALLY_PAID invoices
WHERE (status = 'PAID' OR status = 'PARTIALLY_PAID')

// ❌ Was counting SENT invoices too:
// WHERE status != 'DRAFT'
```

**Why:** Revenue should only include invoices you've actually received payment for (or partial payment). SENT invoices haven't been paid yet.

### 3. Payment Recording Auto-Updates Status

**File:** `InvoiceDetailViewModelV2.kt` (lines 72-82)

**Fixed:**
```kotlin
// When payment is recorded, auto-update status
val newStatus = if (newAmountPaid >= invoice.totalAmount) {
    InvoiceStatus.PAID  // Full payment received
} else {
    InvoiceStatus.PARTIALLY_PAID  // Partial payment received
}
invoiceDao.updateStatus(invoiceId, newStatus)
```

**Why:** DRAFT invoices that receive payment should automatically transition to PARTIALLY_PAID or PAID. This ensures status accurately reflects payment state.

### 4. Added Direct Outstanding Calculation Method

**File:** `InvoiceDaoV2.kt` (lines 207-215)

**Added:**
```kotlin
@Query("""
    SELECT COALESCE(SUM(totalAmount - amountPaid), 0)
    FROM invoices
    WHERE businessProfileId = :businessId
      AND (status = 'SENT' OR status = 'PARTIALLY_PAID' OR status = 'OVERDUE')
      AND isActive = 1
""")
fun observeActualOutstanding(businessId: Long): Flow<Long>
```

**Why:** Provides a clean method to calculate actual outstanding (invoices with unpaid balances) directly from the invoices table.

---

## Architecture After Fix

### Before (Problematic)
```
User marks invoice PAID
    ↓
invoiceDao.updateStatus(PAID) ✅ Invoice table updated
    ↓
Payment snapshot update (IF exists) ⚠️ May not exist or be stale
    ↓
Payment Analytics queries snapshots ❌ Gets wrong data
    ↓
User sees: Outstanding = 82200 (WRONG!)
```

### After (Fixed)
```
User marks invoice PAID
    ↓
invoiceDao.updateStatus(PAID) ✅ Invoice table updated
    ↓
Payment Analytics queries invoices directly ✅ Reads actual state
    ↓
User sees: Outstanding = $0 (CORRECT!)
```

---

## Impact Analysis

| Component | Before | After | Fix Type |
|-----------|--------|-------|----------|
| **Payment Analytics** | Stale snapshot data | Live invoice table | Dependency bypass |
| **Revenue Queries** | Includes SENT invoices | Only PAID/PARTIALLY_PAID | Filter fix |
| **Payment Recording** | Didn't update status | Auto-updates to PAID/PARTIALLY_PAID | Status sync |
| **Customer Segments** | Calculated from bad snapshots | Uses corrected revenue data | Cascading fix |
| **GUI2 Dashboard** | Wrong outstanding/paid amounts | Queries invoice table directly | Uses GUI2RepositoryV2 (already correct) |

---

## Testing Changes

After rebuild, verify:

1. **Create invoice for $222**
2. **Mark as PAID**
3. **Check Payment Analytics (GUI1)**
   - Outstanding: Should be **$0** (not $82,200)
   - Collection Rate: Should be **100%** (not 37.8%)
   - Paid: Should be **1 of X** (not 0)

4. **Check GUI2 Dashboard**
   - Outstanding: Should be **$0**
   - Total Paid: Should show actual collected amount

5. **Check Customer Segments**
   - Top customer paid: Should show actual amount (not $0)

---

## Why Snapshots Aren't Deleted

Snapshots are still useful for:
- Historical reporting (if needed later)
- Risk scoring (marked with `isAtRisk` flag)
- Aging analysis (CURRENT, PAST_30, PAST_60, PAST_90 buckets)

But Payment Analytics no longer **depends** on them being in sync. It queries the invoices table directly.

---

## Code Quality

- ✅ Zero additional dependencies
- ✅ Simpler code path (no conditional snapshot existence checks)
- ✅ Single source of truth (invoices table)
- ✅ Real-time data (no stale cache issues)
- ✅ Cascading fix (fixes Customer Segments via corrected revenue calculations)

---

## Commits Made

Files modified:
- `PaymentAnalyticsRepositoryImpl.kt` - Changed `getPaymentAnalytics()` to use invoices table
- `InvoiceDaoV2.kt` - Fixed revenue filters, added `observeActualOutstanding()`
- `InvoicePaymentDao.kt` - Added cents-to-dollars conversion in queries
- `InvoiceDetailViewModelV2.kt` - Auto-update status when recording payment

---

## Next Steps

If issues persist:
1. Clear app cache and reinstall APK
2. Verify invoices table has correct `status` values
3. Check that `calculatePaymentMetrics()` returns sensible values
4. Monitor logcat for error messages in PaymentAnalyticsRepositoryImpl

---

**Status:** ✅ COMPLETE - Ready for testing

