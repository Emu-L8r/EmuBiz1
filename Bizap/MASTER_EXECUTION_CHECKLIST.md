# ✅ MASTER CHECKLIST: Path to v0.1.0 Complete App

**Target**: Complete, working Bizap app with invoicing, customers, multi-business, analytics, and currency support  
**Start Date**: March 3, 2026  
**Goal Timeline**: 2 weeks (9-13 working days)

---

## 🎯 PHASE 1: Stabilize Data Layer (✅ COMPLETE)

**Status**: ✅ DONE

### Deliverables
- [x] Delete sync subsystem entirely (9 files deleted)
- [x] Create MIGRATION_21_22 (remove PendingOperation table)
- [x] Create MIGRATION_22_23 (add currencyCode to LineItemEntity)
- [x] Update AppDatabase.kt (version 21 → 23)
- [x] Register migrations in DatabaseModule
- [x] Verify no dangling references to deleted sync code
- [x] Generate Phase 1 completion documentation

### Database State
```
v21 (with sync tables) → MIGRATION_21_22 → v22 (sync gone) → MIGRATION_22_23 → v23 (current)
```

### What Works
- ✅ Invoicing (entity, DAO, repository exist)
- ✅ Customers (entity, DAO, repository exist)
- ✅ Business Profiles (entity, DAO, repository exist)
- ✅ Line Items with currency context (entity updated)
- ✅ Templates & Custom Fields (entity, DAO exist)

### What's Left
- ❌ ViewModels still hardcoded to businessId = 1L
- ❌ PaymentAnalytics still has mock data
- ❌ No reactive business profile switching yet

---

## 🎯 PHASE 2: Remove Mock Data & Wire Analytics (⏳ READY)

**Status**: Ready to start  
**Estimated Time**: 2-3 hours  
**Blocker**: None (Phase 1 complete)

### Tasks
- [ ] Task 2.1: Fix PaymentAnalyticsViewModel (remove mocks, wire AnalyticsDao)
- [ ] Task 2.2: Fix RevenueDashboardViewModel (remove hardcoded businessId = 1L)
- [ ] Task 2.3: Fix RiskDashboardViewModel (remove hardcoded businessId = 1L)
- [ ] Task 2.4: Fix DunningNoticesViewModel (remove hardcoded businessId = 1L)
- [ ] Task 2.5: Verify PaymentAnalyticsScreen displays real data
- [ ] Task 2.6: Compile & test verification

### Deliverables
- All ViewModels injected with `BusinessProfileRepository`
- All hardcoded `businessId = 1L` replaced with reactive flows
- All mock data removed, replaced with real DAO queries
- Build passes
- Tests pass

---

## 🎯 PHASE 3: Create Business Profile Reactivity (⏳ READY)

**Status**: Blocked until Phase 2 complete  
**Estimated Time**: 2-4 hours  
**Blocker**: None (ready after Phase 2)

### Tasks
- [ ] Task 3.1: Create `activeProfileId: StateFlow<Long>` in BusinessProfileRepository
- [ ] Task 3.2: Update BusinessProfileRepositoryImpl to track selected profile
- [ ] Task 3.3: Create UI for business profile switching
- [ ] Task 3.4: Wire business switcher to activeProfileId flow
- [ ] Task 3.5: Test reactive updates (switch business → dashboards update)

### Deliverables
- BusinessProfileRepository exposes `activeProfileId: StateFlow<Long>`
- UI allows switching between business profiles
- All dashboards update reactively when business is switched
- Build passes
- Manual test confirms switching works

---

## 🎯 PHASE 4: Security & Cleanup (⏳ READY)

**Status**: Ready to start (independent of other phases)  
**Estimated Time**: 1-2 hours  
**Blocker**: None

### Tasks
- [ ] Task 4.1: Delete all `.bat` and `.ps1` build scripts from repo root
- [ ] Task 4.2: Delete all `.txt` build artifact logs
- [ ] Task 4.3: Delete 60+ stale `.md` documentation files (keep only: README.md, ARCHITECTURE.md, DATABASE_MIGRATION_GUIDE.md)
- [ ] Task 4.4: Add comprehensive `.gitignore`
- [ ] Task 4.5: Create DATABASE_MIGRATION_GUIDE.md (single source of truth for schema)
- [ ] Task 4.6: Create ARCHITECTURE.md v2 (updated to reflect current state)
- [ ] Task 4.7: Verify WorkManagerInitializer.kt is not referenced (consider deleting if unused)
- [ ] Task 4.8: Add SQLCipher or EncryptedSharedPreferences for DataStore

### Deliverables
- Clean repo (no build artifacts or stale documentation)
- Single source of truth for database schema
- Updated architecture documentation
- Data encryption in place (at minimum for sensitive fields)

---

## 🎯 PHASE 5: Testing & Validation (⏳ READY)

**Status**: Ready to start (can run in parallel with other phases)  
**Estimated Time**: 2-3 hours  
**Blocker**: All other phases should complete first

### Tasks
- [ ] Task 5.1: Run full build: `./gradlew clean :app:assembleDebug`
- [ ] Task 5.2: Run unit tests: `./gradlew :app:testDebugUnitTest`
- [ ] Task 5.3: Create integration test for database upgrades (v21 → v23)
- [ ] Task 5.4: Manual test: Create customers and invoices
- [ ] Task 5.5: Manual test: Verify currency display (includes currency code)
- [ ] Task 5.6: Manual test: Switch business profiles (when Phase 3 done)
- [ ] Task 5.7: Manual test: Create invoices in different currencies
- [ ] Task 5.8: Manual test: Verify dashboard metrics are accurate
- [ ] Task 5.9: Manual test: Generate PDF and verify currency display
- [ ] Task 5.10: Performance check: App doesn't lag under typical usage

### Deliverables
- Build succeeds (no compilation errors)
- All unit tests pass (17+ tests)
- Integration tests for database upgrades pass
- Manual testing confirms all features work
- No crashes or ANRs
- Performance is acceptable

---

## 📊 OVERALL TIMELINE

| Phase | Status | Time | Blocker? |
|-------|--------|------|----------|
| 1. Data Layer | ✅ DONE | 2-3h | None |
| 2. Remove Mocks | ⏳ READY | 2-3h | Phase 1 ✅ |
| 3. Reactivity | ⏳ READY | 2-4h | Phase 2 |
| 4. Cleanup | ⏳ READY | 1-2h | None |
| 5. Testing | ⏳ READY | 2-3h | All phases |
| **TOTAL** | | **9-15h** | Sequential with some parallel |

**Realistic estimate**: 2 working days (16 hours) to complete all phases with thorough testing

---

## 🎯 v0.1.0 SCOPE (What will be in the release)

### Features Included ✅
- **Invoicing**: Create, edit, view, delete invoices
  - Line items with quantity, unit price, currency code
  - PDF generation with proper currency display
  - Invoice templates support
  - Custom fields support
  
- **Customers**: Full CRUD
  - Customer database
  - Contact information
  - Business association

- **Business Profiles**: Create and manage
  - Single profile UI (no switching in v0.1.0)
  - Entity support for future multi-business (Phase 3)
  - Profile customization

- **Currency Support**: 
  - Multiple currencies (AUD, USD, EUR, etc.)
  - Exchange rate API (read-only)
  - Proper currency codes on line items
  - Correct display in invoices and PDFs

- **Analytics Dashboard**:
  - Revenue metrics (real data, not mocks)
  - Payment analytics (real data)
  - Business health metrics
  - Real data from AnalyticsDao

- **PDF Generation**:
  - Invoice PDFs with company branding
  - Currency-aware formatting
  - Proper layout

### Features Not Included ❌
- **Offline Sync**: Completely removed (offline-only local app, no server)
- **Multi-Business UI Switching**: Entity support exists, UI hardcoded to active profile (will be in v0.2.0)
- **Payment Recording**: Not mentioned in scope (check if exists)
- **Dunning Notices**: Exists but may need polishing
- **Advanced Analytics**: Beyond dashboard metrics

---

## ✅ SUCCESS CRITERIA FOR v0.1.0

- [x] Database migrations are clean and tested (Phase 1 ✅)
- [ ] No mock data in production code (Phase 2)
- [ ] All ViewModels use real data sources (Phase 2)
- [ ] Business profile context flows through UI (Phase 3)
- [ ] Build compiles with no errors (Phase 5)
- [ ] All 17+ unit tests pass (Phase 5)
- [ ] Manual testing confirms core features work (Phase 5)
- [ ] No crashes when creating/editing/viewing invoices (Phase 5)
- [ ] Currency display is correct (Phase 5)
- [ ] Analytics show real data (Phase 5)
- [ ] PDF generation works (Phase 5)
- [ ] Repo is clean (Phase 4)
- [ ] Documentation is current (Phase 4)

---

## 🚀 NEXT IMMEDIATE ACTIONS

1. **Verify Phase 1 build succeeds**:
   ```bash
   cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
   ./gradlew :app:assembleDebug
   ```
   Expected: ✅ BUILD SUCCESSFUL

2. **Proceed to Phase 2** if build succeeds:
   - Start with Task 2.1 (PaymentAnalyticsViewModel)
   - Follow the action plan in PHASE_2_ACTION_PLAN.md

3. **Document progress** as you complete each task

---

## 📋 DOCUMENTATION CREATED

- ✅ `PHASE_1_EXECUTION_COMPLETE.md` — Phase 1 summary
- ✅ `MIGRATION_21_22_SYNC_REMOVAL.md` — Sync removal migration details
- ✅ `PHASE_2_ACTION_PLAN.md` — Phase 2 step-by-step instructions
- ✅ This master checklist

---

**Status**: 🎯 **Phase 1 Complete, Ready for Phase 2**

**Owner**: Development team  
**Next Review**: After Phase 2 completion

