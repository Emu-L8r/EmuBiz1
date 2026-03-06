# 🔍 QUICK REFERENCE: Status Update Issue

## The Problem in One Sentence
When you change an invoice status, the dashboard shows stale data because the code that synchronizes snapshot tables (which dashboards read) doesn't exist.

---

## The Two Data Paths

```
PATH 1: Details (Works)           PATH 2: Analytics (Broken)
────────────────────────────      ──────────────────────────
Invoice Detail Screen             Dashboard/Analytics Screens
       ↓                                  ↓
Reads: invoices table ✅           Reads: snapshot tables ❌
Updated when status changes        Never updated when status changes
Shows current data ✅              Shows stale data ❌
```

---

## What Happens When You Change Status

```
✅ HAPPENS:                    ❌ MISSING:
invoices table updated         Snapshot tables NOT updated
  
Flow<Invoice> emits            Flow<Snapshot> never emits
  ↓                              ↓
Invoice screen updates         Dashboard stuck on old data
  ✅ Works                        ❌ Broken
```

---

## The Reactive Chain (Broken)

```
Database Change
       ↓
Flow Emission ← STOPS HERE (no database change in snapshots)
       ↓
ViewModel Update ← Never happens
       ↓
UI Recompose ← Never happens
```

---

## Root Causes (Pick Any)

| # | Cause | Type |
|---|-------|------|
| 1 | Two data sources never synchronized | Architecture |
| 2 | No code updates snapshots on status change | Implementation |
| 3 | updateInvoiceStatus() only updates invoices | Implementation |
| 4 | Reactive Flow never gets signal to emit | Architecture |
| 5 | Dashboard reads snapshots not invoices | Design |
| 6 | Snapshot update methods exist but not called | Implementation |
| 7 | No synchronization mechanism defined | Architecture |

---

## The Fix (High Level)

When status changes:
1. Update invoices table ✅ (already done)
2. Update snapshot tables ❌ (MISSING)
   - Recalculate daily revenue
   - Update status in analytics snapshot
3. Room notifies Flow observers ✅ (automatic if step 2 done)
4. Dashboard receives new data ✅ (automatic if step 3 happens)
5. UI updates ✅ (automatic if step 4 happens)

---

## Why Snapshots Exist

**Purpose:** Pre-aggregate data for fast dashboard queries

**Trade-off:**
- Fast queries: ✅ Good
- Must stay in sync: ❌ Not implemented

---

## Impact

Everything that reads snapshots is broken:
- [ ] Revenue Dashboard
- [ ] Payment Analytics
- [ ] Risk Dashboard
- [ ] Customer Segments
- [ ] Dunning Notices

---

## Code Location of Issue

**File:** `InvoiceRepositoryImpl.kt`  
**Method:** `updateInvoiceStatus()`  
**Problem:** Ends after updating invoices table, no snapshot sync

```kotlin
override suspend fun updateInvoiceStatus(...) {
    invoiceDao.updateInvoiceStatus(invoiceId, status)
    // ❌ MISSING: analyticsDao.updateDailySnapshot(...)
    // ❌ MISSING: analyticsDao.updateInvoiceSnapshot(...)
}
```

---

## Why User Sees Old Data

```
Think of it like a news feed:
- Database: The actual news (updated)
- Dashboard: A cached snapshot from yesterday (not updated)
- Dashboard shows yesterday's news (stale)
```

---

## One More Analogy

```
Your home thermostat:
- Temperature sensor: Updates real-time (like invoices table)
- Wall thermometer: Shows cached value (like snapshots)
- You read wall thermometer: Always shows old temp (stale)
- Real temperature changed but wall thermometer not updated
- Result: You see wrong temperature
```

---

## Questions Answered

**Q: Is the dashboard code broken?**  
A: No, dashboard code is fine. It's reading from the wrong source.

**Q: Is the reactive pattern broken?**  
A: No, reactive pattern works perfectly when data changes.

**Q: Is the database broken?**  
A: No, database is fine. It's just not being asked to update snapshots.

**Q: Why don't snapshots update?**  
A: Because the code that would update them doesn't exist.

**Q: Can I just query invoices instead of snapshots?**  
A: That would work but defeat the performance benefit of snapshots.

**Q: When will this be fixed?**  
A: When snapshot sync code is added to `updateInvoiceStatus()`.

---

## Files To Read (In Order)

1. **START HERE:** `SUMMARY_STATUS_UPDATE_REASONING.md` (this level of detail)
2. **UNDERSTANDING:** `VISUAL_BREAKDOWN_STATUS_UPDATE_ISSUE.md` (diagrams)
3. **DEEP DIVE:** `REASONING_STATUS_NOT_UPDATING_DASHBOARDS.md` (complete explanation)
4. **ARCHITECTURE:** `TRUNK_ANALYSIS_ARCHITECTURAL_ISSUES.md` (system design)

---

**Status:** Analysis Complete ✅  
**Confidence:** 100% (Verified by Code Inspection)  
**Next Step:** Implement snapshot synchronization


