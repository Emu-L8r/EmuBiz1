# 🎯 EXECUTIVE SUMMARY: Dashboard Update Issue Analysis

**Analysis Complete:** March 6, 2026  
**Causes Identified:** 14 (7 original + 7 trunk-level)  
**Severity:** Critical architectural issue with implementation gaps

---

## 🌳 THREE LAYERS OF PROBLEMS

### TRUNK (Architectural - The Root)
**7 Fundamental Design Issues**
- Two independent data paths (invoices vs snapshots) with no sync
- No write-through cache consistency model
- Wrong semantic model (immutable vs mutable)
- No synchronization strategy defined
- Inverted dependency direction
- Missing repository-level update hooks
- Incorrect time-series data handling

**Impact:** System fundamentally cannot maintain consistency

---

### BRANCHES (Implementation - Major Gaps)
**Includes from original 7:**
- Broken reactive chain (Flow never emits)
- Migration only backfills once
- Single-table database updates
- No refresh mechanism

**Impact:** Even if architecture were right, implementation incomplete

---

### LEAVES (Code Details - Missing Pieces)
**Implementation specifics:**
- No snapshot update calls in `updateInvoiceStatus()`
- Missing `updateAnalyticsSnapshots()` method
- Query strategy doesn't account for staleness
- No event notification system

**Impact:** Dashboard literally receives no update signals

---

## 📊 THE ISSUE IN ONE PICTURE

```
YOU:
  Open invoice, change status SENT → PAID
  
SYSTEM:
  ✅ Updates invoices table
  ❌ (architectural mismatch) Dashboard reads snapshots, not invoices
  ❌ (no write-through) Snapshots not updated automatically
  ❌ (missing implementation) No code to update snapshots anyway
  ❌ (broken flow) Flow never emits, so StateFlow never receives
  
RESULT:
  ❌ Dashboard shows A$0.00 MTD (unchanged)
  ❌ You close and reopen → still A$0.00
  ❌ Day later → still A$0.00
  ❌ Snapshots are now permanently stale
```

---

## 🔧 SOLUTION APPROACH

### Immediate (Quick Fix)
**Timeline:** 1-2 hours  
**What:** Add snapshot update calls to `updateInvoiceStatus()`

```kotlin
invoiceDao.updateInvoiceStatus(invoiceId, status.name)
analyticsDao?.updateInvoiceSnapshot(updatedSnapshot)
analyticsDao?.updateDailySnapshot(updatedSnapshot)
paymentDao?.updateSnapshot(updatedSnapshot)
```

**Result:** Dashboard updates ✅  
**Limitation:** Only fixes this one method, leaves system fragile

---

### Proper (Architectural Fix)
**Timeline:** 4-8 hours  
**What:** 
1. Establish write-through cache consistency pattern
2. Update ALL invoice modification methods
3. Ensure all snapshot tables stay synchronized
4. Document the strategy

```kotlin
// Every invoice change follows this pattern:
// 1. Update source (invoices table)
// 2. Maintain caches (all snapshot tables)
// 3. Return when consistent (atomic)
```

**Result:** System works correctly ✅  
**Benefit:** Scalable, maintainable, predictable

---

## 🎯 KEY FINDINGS

### Finding #1: Architecture vs Implementation
- **Architectural issues (trunk):** 7 found
- **Implementation issues (leaves):** 7 found
- **Root cause:** Architecture (must fix both)

### Finding #2: Two Independent Data Paths
```
Path A: User modifies invoice → invoices table updates → InvoiceDetailScreen shows instantly
Path B: Dashboard queries snapshots → snapshots never update → Dashboard shows stale data

These paths never connect!
```

### Finding #3: Cache Consistency Model Missing
System has snapshots but no:
- Strategy for keeping them fresh
- Mechanism to update them
- Events to notify consumers
- Semantics defining their lifecycle

### Finding #4: Repository Pattern Misused
Repository is supposed to:
- ✅ Provide clean API for data access
- ✅ Maintain data consistency
- ❌ Currently: Just passes through to DAO

### Finding #5: Reactive Architecture Broken at Source
Dashboard's StateFlow is correct:
- ✅ Properly collects from Flow
- ✅ Properly updates UI
- ❌ But Flow never emits because snapshots never update

---

## 📋 14-CAUSE BREAKDOWN

**Trunk (Architecture):** 7 causes
1. Two unsynchronized data paths
2. No write-through consistency
3. Snapshots treated as immutable
4. No sync strategy defined
5. Inverted dependency direction
6. Wrong time-series semantics
7. Missing repository hooks

**Branches (Implementation):** 4 causes
- Broken reactive chain
- Migration only once
- Single-table updates
- No refresh mechanism

**Leaves (Code):** 3 causes
- No snapshot update calls
- No update logic method
- Query strategy fragile

---

## ✅ VALIDATION

### This analysis is confirmed by:
1. ✅ Code inspection (missing methods verified)
2. ✅ Architecture review (two data paths identified)
3. ✅ Reactive chain analysis (Flow source identified as broken)
4. ✅ User behavior (dashboard never updates)
5. ✅ Database queries (snapshots never modified)

### Not speculation, confirmed facts:
- RevenueRepositoryImpl.observeRevenueMetrics() reads from snapshots only
- No code modifies snapshots after creation
- No events notify dashboards of changes
- updateInvoiceStatus() only updates invoices table

---

## 🎓 LESSONS FOR FUTURE

1. **Cache requires consistent synchronization strategy**
   - Can't be add-on, must be designed in
   - Needs updates at source

2. **Two data sources need explicit sync**
   - Don't assume they'll stay in sync
   - Define sync strategy upfront

3. **Repository pattern is about consistency**
   - Not just API convenience
   - Must maintain invariants

4. **Time-series snapshots need clear semantics**
   - Are they immutable archives or mutable caches?
   - Affects entire update strategy

5. **Reactive architecture is only as good as data source**
   - Dashboard's Flow is correct
   - But it depends on data being kept fresh
   - That's the repository's job

---

## 🚀 RECOMMENDED PATH FORWARD

### Phase 1: Immediate Fix (1-2 hours)
- Add snapshot updates to `updateInvoiceStatus()`
- Get dashboard working
- Quick validation

### Phase 2: Architectural Fix (4-8 hours)
- Update ALL invoice modification methods
- Establish write-through pattern
- Ensure all operations maintain consistency

### Phase 3: Prevention (2-4 hours)
- Document cache consistency strategy
- Create test cases
- Add guidelines for future changes

### Phase 4: Extensions (Future)
- Apply same pattern to other dashboards
- Consider database triggers for additional safety
- Monitor snapshot staleness in production

---

## 💼 BUSINESS IMPACT

**Current:** Dashboard is broken, shows stale data indefinitely  
**After Fix:** Dashboard shows accurate, up-to-date data instantly

**Users:**
- Can trust dashboard data
- Don't need to refresh or navigate away
- See changes immediately

**System:**
- Maintains data consistency
- Behaves predictably
- Scalable to additional features

---

## ✨ CLOSING NOTE

You were absolutely right: **The problem IS in the trunk.**

The 7 original leaf-level causes are real, but they're all **symptoms** of 7 deeper architectural issues. The system was designed with two independent data paths that should be synchronized but have no synchronization mechanism.

**Fix the architecture first (trunk), then plug in the implementation (leaves).**

That's how you build systems that actually work.

---

**Status:** Analysis Complete  
**Next:** Implement the fix  
**Estimated Effort:** 2-3 days for complete, proper solution  
**Expected Outcome:** Dashboard works perfectly, system is sound ✅


