# Investigation Report: GUI2 Zero Revenue and Payment Metrics

## 📝 Issue Summary
The GUI2 dashboard displays **$0.00** for MTD Revenue and Total Paid, even when invoices marked as "PAID" exist in the system. This indicates a disconnect between the modern UI analytics engine and the underlying data.

---

## 🔴 Potential Root Causes

### 1. Business ID Mismatch (Highest Probability)
In `MainActivity.kt`, the application defaults to **Business ID 1L** if the profile hasn't finished loading:
```kotlin
startBusinessId = businessProfile.id.takeIf { it > 0 } ?: 1L
```
If your invoices were created under a different ID (e.g., ID 2 or 15) in GUI1, the GUI2 dashboard will filter exclusively for ID 1 and return **$0.00** because it cannot "see" invoices belonging to other business profiles.

### 2. Accrual vs. Cash Basis Logic
The current logic for **Revenue** (MTD/YTD) is strictly **Cash Basis**. It only sums the `totalAmount` of invoices where the status is explicitly `'PAID'` or `'PARTIALLY_PAID'`:
```sql
WHERE status = 'PAID' OR status = 'PARTIALLY_PAID'
```
If you have 100 invoices in **'SENT'** status, your revenue will correctly show as **$0.00** because you haven't technically collected the money yet.

### 3. Missing `amountPaid` Data
The **Total Paid** metric sums the `amountPaid` column. 
- If an invoice was manually moved to "PAID" status via a dropdown but no actual **payment transaction** was recorded, the `amountPaid` column remains at `0`.
- **Result**: The dashboard reports the invoice as "Paid" (count), but the money as "$0.00" (value).

### 4. Timestamp Range & Month-to-Date Filtering
The **MTD Revenue** query uses a SQLite date filter:
```sql
AND DATE(date/1000, 'unixepoch') >= DATE('now', 'start of month')
```
If your test invoices have dates from a previous month or incorrect timestamps (e.g., 0 or future dates), they are mathematically excluded from the "Month-to-Date" calculation.

### 5. `isActive` Soft-Delete Filter
The new GUI2 queries strictly filter for `isActive = 1`. If invoices were created in an older version of the app before this flag was standard, they might be defaulting to `0` or `null`, making them invisible to the modern dashboard.

---

## ✅ Recommended Actions

1.  **Verify Business Identity**: Check the name at the top of the GUI2 Dashboard. If it says "Default Business" but your invoices are for "My Company," you are looking at the wrong business profile.
2.  **Audit the DAO Filter**: I recommend changing the Revenue query to **Accrual Basis** (counting all issued invoices regardless of payment) if you want to see your "Total Billed" amount on the dashboard.
3.  **Fix ID Fallback**: Update `MainActivity` to reactively update the dashboard when the real profile loads, rather than hardcoding a `1L` fallback.

---
**Status**: 🔍 INVESTIGATION COMPLETE  
**Next Step**: Align Business ID loading and Accounting Logic.
