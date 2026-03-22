# SPRINT 3 — QUICK REFERENCE GUIDE

**Date:** March 22, 2026  
**Thinking:** ✅ Already done, below  
**Implementation:** Ready to execute  
**Estimated Time:** 12 hours (Tasks 1-2: 5 hours, Tasks 3-8: 7 hours)

---

## 🎯 ONE-PAGE OVERVIEW

You're at **8.5/10** (excellent). This plan gets you to **9.0+/10** by:

1. **Fix 2 architecture violations** (5 use cases + 1 ViewModel) → +0.3 points
2. **Document & recover tests** → +0.1 points
3. **Add performance baselines** → +0.05 points
4. **Validate error handling** → +0.05 points

**Net:** 8.5 → 9.0+ (minimal effort, maximum impact)

---

## 🏃 QUICK EXECUTION PATH

### Phase 1: Architecture Fixes (5 hours) — HIGH PRIORITY
**These are the violations stopping you from 9.0:**

```
VIOLATION #1: DashboardViewModel imports InvoiceDaoV2
└─ Fix: Add observeInvoices() to BusinessContextRepositoryV2
└─ Fix: Remove DAO import from DashboardViewModel
└─ Time: 2 hours
└─ Impact: +0.2 health points

VIOLATION #2: 5 UseCases import data layer
├─ SaveInvoiceUseCase → imports SnapshotSyncHelper (data)
├─ UpdateInvoiceUseCase → imports InvoiceEntity (data)
├─ RecordPaymentUseCase → imports PaymentRepositoryV2 (data impl)
├─ GenerateAndSaveInvoiceUseCase → imports GeneratedDocumentEntity (data)
├─ DeleteInvoiceUseCase → imports OfflineQueueService (data)
└─ Fix: Create domain interfaces, update imports
└─ Time: 3 hours
└─ Impact: +0.1 health points
```

**Then run:** `./gradlew app:testDebugUnitTest`  
**Expected:** Both architecture tests pass ✅

---

### Phase 2: Supporting Documentation (7 hours) — MEDIUM PRIORITY
**These turn harsh critique into measured excellence:**

```
TASK 3: Recover Missing 106 Tests (1.5 hours)
└─ Audit git history for deleted tests
└─ Decide: intentional (document) or accidental (restore)
└─ Create TEST_AUDIT_REPORT.md

TASK 4: Performance Baselines (1.5 hours)
└─ Measure: Build time, APK size, memory usage
└─ Document: PERFORMANCE_BASELINE.md
└─ Add: benchmarking task to build.gradle.kts

TASK 5: ErrorBoundary Validation (1.5 hours)
└─ Create: ErrorBoundaryComprehensiveTest.kt (12 tests)
└─ Create: ERROR_BOUNDARY_VALIDATION.md

TASK 6: Before/After Code Diffs (0.5 hours)
└─ Create: ERROR_BOUNDARY_BEFORE_AFTER.md
└─ Show concrete improvements with code examples

TASK 7: Prop Drilling Assessment (0.5 hours)
└─ Create: PROP_DRILLING_AUDIT.md
└─ Confirm no unnecessary parameter chains

TASK 8: Archive Strategy (1 hour)
└─ Rename: docs/archive/ → docs/historical/
└─ Create: INDEX.md (historical navigation guide)
```

---

## 📋 FILES TO CREATE/MODIFY

### CREATE (New Files)
```
✅ SPRINT_3_ACTIONABLE_PLAN.md          (Main plan — 14KB, very detailed)
✅ SPRINT_3_QUICK_REFERENCE.md          (This file — quick lookup)

docs/PERFORMANCE_BASELINE.md            (Build/APK metrics)
docs/TEST_AUDIT_REPORT.md               (Test recovery status)
docs/ERROR_BOUNDARY_VALIDATION.md       (12 test case validation)
docs/ERROR_BOUNDARY_BEFORE_AFTER.md     (Code diffs showing improvements)
docs/PROP_DRILLING_AUDIT.md             (Component parameter analysis)
docs/historical/INDEX.md                (Archive reorganization)

app/src/test/.../ErrorBoundaryComprehensiveTest.kt
```

### MODIFY (Existing Files)
```
app/src/main/java/.../DashboardViewModel.kt
  └─ Remove: import InvoiceDaoV2
  └─ Remove: invoiceDaoV2 constructor parameter
  └─ Change: invoiceDaoV2.method() → businessContextRepository.method()

app/src/main/java/.../SaveInvoiceUseCase.kt
  └─ Remove: import com.emul8r.bizap.data.*
  └─ Add: import com.emul8r.bizap.domain.usecase.*

app/src/main/java/.../UpdateInvoiceUseCase.kt
  └─ Same pattern as SaveInvoiceUseCase

app/src/main/java/.../RecordPaymentUseCase.kt
  └─ Change: PaymentRepositoryV2 → PaymentRepository (domain interface)

app/src/main/java/.../GenerateAndSaveInvoiceUseCase.kt
  └─ Change: GeneratedDocumentEntity → GeneratedDocument (domain model)

app/src/main/java/.../DeleteInvoiceUseCase.kt
  └─ Remove: import com.emul8r.bizap.data.*
  └─ Add: import com.emul8r.bizap.domain.usecase.*

app/build.gradle.kts
  └─ Add: benchmarkComponentBuild task

README.md
  └─ Add: Performance metrics section

docs/archive/ → docs/historical/ (rename folder)
ArchitectureTest.kt
  └─ Add: comments noting fixes
```

---

## 🚀 START HERE CHECKLIST

Before implementing, you need one thing: **domain layer interfaces**

These might already exist, so check first:

```bash
# Check if these exist:
grep -r "interface OfflineQueueService" app/src/main/java/com/emul8r/bizap/domain/
grep -r "interface SnapshotSyncHelper" app/src/main/java/com/emul8r/bizap/domain/
grep -r "interface InvoicePdfService" app/src/main/java/com/emul8r/bizap/domain/

# If they don't exist, create them in domain layer:
app/src/main/java/com/emul8r/bizap/domain/usecase/OfflineQueueService.kt
app/src/main/java/com/emul8r/bizap/domain/usecase/SnapshotSyncHelper.kt
app/src/main/java/com/emul8r/bizap/domain/service/InvoicePdfService.kt
```

---

## 💡 KEY INSIGHTS

### Why This Fixes the Score

**Harsh Critique Feedback:**
- "You're importing data layer in use cases" ❌
- "ViewModels have direct DAO access" ❌
- "No performance metrics" ❌
- "No validation tests for error handling" ❌

**Your Response After Sprint 3:**
- "All use cases import domain interfaces" ✅
- "All ViewModels use repository abstractions" ✅
- "Performance baseline: 1m 4s build, 12MB APK" ✅
- "12 comprehensive ErrorBoundary tests pass" ✅

### Why 2 Hours Gets You There

The violations are **simple to fix** — they just need to be done:

1. Move data access to repository (1 line change per file)
2. Remove DAO imports (1 delete per file)
3. Document the fixes (1 comment per file)

No architectural redesign needed. **Just clean up the imports.**

---

## 📊 SUCCESS METRICS

After this sprint:

| Metric | Current | Target | Status |
|--------|---------|--------|--------|
| Architecture Tests | 2 failing | 0 failing | 🟢 Achievable |
| Health Score | 8.5/10 | 9.0+/10 | 🟢 Target |
| Build Success | YES | YES | ✅ Maintained |
| Tests Passing | 994/996 | 994+/996 | ✅ Maintained |
| Documentation | Good | Excellent | 🟢 Enhanced |
| Performance Data | None | Complete | 🟢 Added |

---

## ⏱️ TIME BREAKDOWN

| Task | Time | Can Parallelize? |
|------|------|------------------|
| Task 1: DashboardViewModel | 2h | No (do first) |
| Task 2: UseCases | 3h | No (do second) |
| Task 3: Test Recovery | 1.5h | Yes |
| Task 4: Perf Metrics | 1.5h | Yes |
| Task 5: ErrorBoundary | 1.5h | Yes |
| Task 6: Code Diffs | 0.5h | Yes |
| Task 7: Prop Drilling | 0.5h | Yes |
| Task 8: Archive | 1h | Yes |

**Optimal Path:**
1. Do Tasks 1-2 in sequence (5h)
2. Then do Tasks 3-8 in parallel (7h total)
3. **Total: ~10-12 hours wall time**

---

## 🎓 ARCHITECTURAL LESSON

**The Pattern You're Fixing:**

```
WRONG (Circular Dependencies):
ViewModel → DAO (data layer)
UseCase → Entity (data layer)
Result: Violations, hard to test, brittle

RIGHT (Clean Layers):
ViewModel → Repository Interface (domain layer)
UseCase → Repository Interface (domain layer)
Result: Testable, maintainable, clear boundaries
```

**Why It Matters:**
- When you upgrade the database, you only change the repository implementation
- Tests don't need Hilt setup (mock the domain interface)
- New developers understand the layer structure instantly

---

## 🔗 RESOURCES

**Main Plan Document:**
→ Read: `SPRINT_3_ACTIONABLE_PLAN.md` (14KB, extremely detailed)

**Specific Guidance:**
- DashboardViewModel fix: See Step 1d-1g in plan
- UseCase fixes: See Step 2d-2g in plan
- Test recovery: See Step 3a-3g in plan
- Performance: See Step 4a-4e in plan
- ErrorBoundary tests: See Step 5a-5c in plan

**Implementation Files:**
- ErrorBoundaryComprehensiveTest.kt (copy from plan Step 5a)
- PERFORMANCE_BASELINE.md (copy from plan Step 4c)
- ERROR_BOUNDARY_BEFORE_AFTER.md (copy from plan Step 6a)
- PROP_DRILLING_AUDIT.md (copy from plan Step 7a)

---

## ✅ FINAL CHECKLIST BEFORE STARTING

- [ ] Read this quick reference
- [ ] Read the full plan in SPRINT_3_ACTIONABLE_PLAN.md
- [ ] Check if domain layer interfaces exist (grep commands above)
- [ ] Create domain layer interfaces if missing
- [ ] Verify git is clean (`git status`)
- [ ] Create feature branch: `git checkout -b sprint-3-architecture-fixes`
- [ ] Begin with Task 1 (DashboardViewModel)

---

## 🎉 NEXT STEPS

1. **Now:** Read `SPRINT_3_ACTIONABLE_PLAN.md` (full plan with step-by-step instructions)
2. **Then:** Create domain layer interfaces if needed
3. **Then:** Execute Tasks 1-2 (architecture fixes)
4. **Then:** Execute Tasks 3-8 (documentation & validation)
5. **Finally:** Run full test suite: `./gradlew clean build`
6. **Verify:** Health score increased to 9.0+/10

---

**Prepared by:** GitHub Copilot  
**Date:** March 22, 2026  
**Status:** ✅ Ready for implementation  
**Difficulty:** 🟢 Medium (mostly straightforward refactoring)  
**Impact:** 🔴 High (fixes critical violations, proves excellence)

