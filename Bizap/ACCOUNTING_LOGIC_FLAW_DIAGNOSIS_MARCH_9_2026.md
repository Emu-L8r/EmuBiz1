# 🔴 CRITICAL: ACCOUNTING LOGIC FLAW DIAGNOSIS

**Date:** March 9, 2026  
**Issue:** Revenue calculation mixing Cash and Accrual basis accounting  
**Severity:** CRITICAL - Financial reporting is incorrect  
**Status:** IDENTIFIED - Ready for fix

---

## THE EXACT PROBLEM

Your system is **mixing two different accounting methods** in the same query:

### What You Have
```
Invoice #1: $100 total, $50 paid → Status: PARTIALLY_PAID
Invoice #2: $100 total, $50 paid → Status: PARTIALLY_PAID
```

### What Cash Basis Says (Correct for "Revenue Collected")
```
Total Paid = $50 + $50 = $100 ✅
```

### What Accrual Basis Says (Correct for "Revenue Recognized")
```
Total Billed = $100 + $100 = $200 ✅
```

### What the Current Query Does (WRONG!)
```sql
SELECT SUM(amountPaid) 
FROM invoices 
WHERE status IN ('PAID', 'PARTIALLY_PAID')
```

**Result:** $100 ✅ (This part is actually correct!)

So if the query is correct, **why isn't the dashboard showing $100 collected?**

---

## DIAGNOSIS: Find the Root Cause

The issue is that your 2 PARTIALLY_PAID invoices with $50 each are **not being counted**.

This means one of these is true:

### Possibility 1: Status Filter (Most Likely)
The query might be filtering by `status = 'PAID'` only, excluding PARTIALLY_PAID!

**Check:** Look for queries with:
```sql
WHERE status = 'PAID'  ← BAD! Ignores partially paid
WHERE status IN ('PAID', 'PARTIALLY_PAID')  ← GOOD!
```

### Possibility 2: Date Filter
SQLite's `DATE()` function might be timezone-aware, excluding today's invoices.

**Check:** Look for SQLite timezone issues:
```sql
DATE(date/1000, 'unixepoch')  ← May have timezone issues
```

### Possibility 3: isActive Flag
Invoices might not have `isActive = 1` set.

**Check:** Verify your invoices have `isActive = 1`

### Possibility 4: businessProfileId Mismatch
The query might be filtering by wrong business ID.

**Check:** Verify businessId parameter is correct

---

## HOW TO DEBUG THIS

### Step 1: Run the Diagnostic Query

I've added debug methods to InvoiceDaoV2. Call this from your ViewModel or Fragment:

```kotlin
// Get all invoices
val allInvoices = invoiceDaoV2.debugAllInvoices(businessId)

// Get summary by status
val byStatus = invoiceDaoV2.debugInvoicesByStatus(businessId)

// Log it
for (invoice in allInvoices) {
    Timber.d("Invoice: ${invoice["id"]}, Status: ${invoice["status"]}, Paid: ${invoice["amountPaid"]}")
}

for (row in byStatus) {
    Timber.d("Status: ${row["status"]}, Count: ${row["count"]}, Total Billed: ${row["totalBilled"]}, Total Paid: ${row["totalPaid"]}")
}
```

### Step 2: Check the Logcat Output

Look for lines like:
```
Invoice: 1, Status: PARTIALLY_PAID, Paid: 5000
Invoice: 2, Status: PARTIALLY_PAID, Paid: 5000
Status: PARTIALLY_PAID, Count: 2, Total Billed: 10000, Total Paid: 10000
```

### Step 3: Verify the MTD Query

Then run the actual MTD query:
```kotlin
val mtdRevenue = invoiceDaoV2.observeMTDRevenue(businessId).first()
Timber.d("MTD Revenue: $mtdRevenue cents = $${"%.2f".format(mtdRevenue / 100.0)}")
```

Should show: `100.00`

---

## THE REAL ARCHITECTURAL FLAW

Even if we fix the immediate query issue, there's a deeper problem:

### Current Architecture (BROKEN)
```
Different screens use different queries:
├─ Dashboard Revenue: SUM(amountPaid) WHERE PAID OR PARTIALLY_PAID
├─ Payment Analytics: (outstanding + collected) calculation  
├─ Invoice Detail: amountPaid / totalAmount ratio
└─ Snapshots: Old cached data (stale!)

Result: Same data interpreted 4 different ways! 🚨
```

### Required Architecture (CORRECT)
```
Single Accounting Service:
├─ Definition: "Revenue" = Cash collected from invoices
├─ All queries → Same formula: SUM(amountPaid) WHERE (PAID OR PARTIALLY_PAID)
├─ All screens → Consume same service
└─ No snapshots, no cached calculations
```

---

## THE FIX (Once Root Cause Identified)

### If it's a Status Filter Issue:
Change all revenue queries from:
```sql
-- WRONG
WHERE status = 'PAID'

-- RIGHT
WHERE status IN ('PAID', 'PARTIALLY_PAID')
```

### If it's a Timezone Issue:
Change date filtering from:
```sql
-- Potentially wrong
DATE(date/1000, 'unixepoch') >= DATE('now', 'start of month')

-- Better
date >= datetime('now', 'start of month', 'utc')
```

### If it's an isActive Issue:
Ensure all payment recording code sets `isActive = 1`:
```kotlin
val updated = invoiceEntity.copy(
    amountPaid = newAmount,
    isActive = 1  ← Make sure this is set!
)
```

---

## NEXT IMMEDIATE ACTIONS

1. **Add the diagnostic queries** (already done in InvoiceDaoV2)
2. **Call them from your ViewModel**
3. **Check the Logcat output**
4. **Identify which case applies** (status filter, date filter, isActive, etc.)
5. **Fix the root cause**
6. **Verify revenue now includes $100**

---

## THE BIGGER PICTURE

This incident reveals a fundamental architectural issue: **Your app calculates financial metrics in multiple places using different logic.**

The proper solution is a **centralized AccountingService** that:
- Defines what each metric means (cash, accrual, outstanding, etc.)
- Has one implementation per metric
- All screens consume from this service
- Tests verify consistency across definitions

---

## FILES THAT NEED AUDITING

1. **InvoiceDaoV2.kt** - Check all revenue queries
2. **PaymentAnalyticsRepositoryImpl.kt** - Check metric calculations
3. **RevenueRepositoryV2.kt** - Check revenue combinations
4. **InvoiceDetailViewModel.kt** - Check how it calculates progress
5. **Any snapshot-based queries** - These are the culprit!

---

## URGENT: Run These Queries NOW

Add this temporary debugging code to see what's actually in your database:

```kotlin
viewModelScope.launch {
    val all = invoiceDaoV2.debugAllInvoices(businessId)
    val byStatus = invoiceDaoV2.debugInvoicesByStatus(businessId)
    
    Timber.d("=== ALL INVOICES ===")
    all.forEach { Timber.d(it.toString()) }
    
    Timber.d("=== BY STATUS ===")
    byStatus.forEach { Timber.d(it.toString()) }
    
    val mtd = invoiceDaoV2.observeMTDRevenue(businessId).first()
    Timber.d("MTD Revenue from query: $mtd cents")
}
```

**Run this and paste the logcat output** - that will tell us exactly what's wrong!

---

**Status: Awaiting diagnostic results to confirm root cause**

