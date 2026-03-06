# 🎯 DEEP DIVE SUMMARY: Dashboard Update Issue

**Issue:** Dashboards don't update when invoice status changes from SENT → PAID/PARTIALLY_PAID

**Root Cause:** Missing snapshot synchronization in InvoiceRepositoryImpl

---

## 7 SEPARATE CAUSES (Layer-by-Layer)

### **CAUSE #1: 🔴 CRITICAL - Invoice Status Updates Don't Trigger Snapshot Updates**
- **Layer:** Database (Data Persistence)
- **Problem:** `updateInvoiceStatus()` only updates `invoices` table, NOT snapshot tables
- **Impact:** Snapshots stay stale while invoices get updated
- **Evidence:** No code calls snapshot update methods after status change

### **CAUSE #2: 🔴 CRITICAL - No Snapshot Update Logic After Status Changes**
- **Layer:** Business Logic (Repository)
- **Problem:** Missing method to recalculate and update all 3 snapshot tables
- **Impact:** Even if code tried to update, there's no logic to do it
- **Missing:** `updateAnalyticsSnapshots()` method doesn't exist

### **CAUSE #3: 🟠 HIGH - UpdateInvoiceStatus Missing Database Transaction**
- **Layer:** Data Access (DAO)
- **Problem:** Only updates 1 table (`invoices`), leaves 3 snapshots un-updated
- **Impact:** Data inconsistency - invoices and snapshots diverge
- **Requires:** Multi-table atomic transaction

### **CAUSE #4: 🔴 CRITICAL - No Reactive Chain from Invoice Updates to Snapshots**
- **Layer:** Reactive Architecture (Flow)
- **Problem:** Break in the reactive chain between invoice updates and dashboard
- **Current:** `InvoiceDao` → (update) → `InvoiceRepository` → (missing link) → `AnalyticsDao`
- **Missing:** Snapshot update logic that triggers Flow emissions

### **CAUSE #5: 🟠 HIGH - Migration 24→25 Only Backfills Initial Data**
- **Layer:** Data Synchronization
- **Problem:** Migration creates snapshots once on app startup, never updates them again
- **Timeline:** Status changes after migration → snapshots never updated
- **Limitation:** One-time backfill, not an ongoing sync mechanism

### **CAUSE #6: 🟠 MEDIUM - RevenueRepository Queries Snapshots Instead of Recalculating**
- **Layer:** Query Strategy
- **Problem:** Dashboards depend on snapshot being current; if stale, dashboard is stale
- **Design:** Snapshots are denormalized cache for performance, but cache becomes invalid
- **Trade-off:** Fast queries (✅) but requires sync (❌ broken)

### **CAUSE #7: 🟠 MEDIUM - No Mechanism to Refresh Snapshots After Invoice Changes**
- **Layer:** Event Handling (UI/Presentation)
- **Problem:** No event/notification from invoice update to refresh snapshots/dashboards
- **Missing:** Connection between ViewModel event and cache invalidation
- **Would Help:** Event-driven refresh of analytics data

---

## 🔗 How They Connect

```
Cause #1 (No update call)
    ↓
Causes #2 & #3 (No update logic/transaction)
    ↓
Cause #4 (Flow broken)
    ↓
Cause #5 (Snapshots stale)
    ↓
Cause #6 (Query stale cache)
    ↓
Cause #7 (No refresh mechanism)
    ↓
DASHBOARD SHOWS OLD DATA
```

---

## ✅ THE FIX

**In InvoiceRepositoryImpl.updateInvoiceStatus():**

Add logic after updating the invoice to also update all 3 snapshot tables:
1. Update `invoice_analytics_snapshots`
2. Update `daily_revenue_snapshots`
3. Update `invoice_payment_snapshots`

This re-closes the reactive chain:
```
Status Update → invoices table updated
    ↓
    → snapshots updated
    ↓
    → Flow emits
    ↓
    → Dashboard receives new data ✅
```

**See:** `IMPLEMENTATION_FIX_DASHBOARD_UPDATES.md` for complete code

---

## 📊 Before & After

| When | Dashboard Shows |
|------|-----------------|
| Create invoice (SENT) | A$0.00 MTD ✅ |
| Change to PAID (BEFORE FIX) | A$0.00 MTD ❌ |
| Change to PAID (AFTER FIX) | A$100.00 MTD ✅ |

---

## 🎯 Key Insight

The reactive architecture is **correct and well-designed**. The problem is a **missing implementation** - the snapshot update logic was never connected to the status change flow.

**Architecture:** 10/10 ✅  
**Implementation:** 7/10 (missing one critical piece)


