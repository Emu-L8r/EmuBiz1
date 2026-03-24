# 🚀 90-DAY ACTION ROADMAP — BIZAP IMPROVEMENT PLAN
**Start Date:** March 24, 2026  
**Target Completion:** June 24, 2026  
**Goal:** Move from 8.5/10 → 9.5/10 health score

---

## EXECUTIVE SUMMARY

**Current State:** Production-ready with minor gaps  
**Target State:** Polished, optimized, zero known gaps  
**Phases:** 4 concurrent streams (can run in parallel)  
**Expected Outcome:** App Store launch + sustained 9.5/10 health

---

## PHASE 1: CRITICAL FIXES (Weeks 1-2)

### 1.1 Exchange Rate API Integration
**Owner:** Backend Lead  
**Effort:** 15 minutes  
**Priority:** 🔴 CRITICAL  
**Impact:** Enables multi-currency features

**Tasks:**
- [ ] Get free API key from https://www.exchangerate-api.com/
- [ ] Add to `gradle.properties`: `EXCHANGE_RATE_API_KEY=your_key`
- [ ] Verify build logs show no warnings
- [ ] Test multi-currency invoice creation
- [ ] Document in `docs/EXCHANGE_RATE_API_GUIDE.md` (already exists)

**Definition of Done:**
```
✅ BUILD logs show no EXCHANGE_RATE_API_KEY warnings
✅ Multi-currency dropdown functional in invoice creation
✅ Rates auto-populate on invoice load
```

### 1.2 Payment History UI Implementation
**Owner:** Frontend Lead  
**Effort:** 2 hours  
**Priority:** 🔴 CRITICAL  
**Impact:** Completes payment tracking feature

**Design:**
```
Invoice Detail Screen
  ├── Existing tabs: Details, Items, Status
  └── NEW: Payment History tab
      ├── Timeline view
      ├── Payment card: Amount, Date, Status, Notes
      └── Export button
```

**Tasks:**
- [ ] Design payment history screen (UI spec + wireframe)
- [ ] Create `PaymentHistoryScreen.kt` Composable (GUI2)
- [ ] Create `PaymentHistoryActivity.kt` (GUI1)
- [ ] ViewModel: `PaymentHistoryViewModel.kt` (fetch snapshots)
- [ ] Add route to navigation graph
- [ ] Wire to invoice detail screen "View Payments" button
- [ ] Write integration tests (3-5 scenarios)
- [ ] Manual QA on emulator

**Data Source:** `invoice_payment_snapshots` table (already populated)

**Definition of Done:**
```
✅ Payment history tab visible on invoice detail (both GUIs)
✅ Timeline displays all payment records chronologically
✅ Tests pass: timeline, filtering, export
✅ No crashes or data inconsistencies
```

### 1.3 Integration Test Suite Addition
**Owner:** QA Lead  
**Effort:** 1 day  
**Priority:** 🟠 HIGH  
**Impact:** Catches regressions before production

**Test Coverage:**
- [ ] GUI1 → GUI2 navigation flow (switch + data sync)
- [ ] GUI2 → GUI1 navigation flow (switch + data sync)
- [ ] Invoice creation in GUI1, view in GUI2 (data consistency)
- [ ] Payment recording across GUI boundary
- [ ] Settings changes propagation

**Files to Create:**
- `app/src/test/java/com/emul8r/bizap/ui/integration/GUISwitchingTest.kt`
- `app/src/test/java/com/emul8r/bizap/ui/integration/CrossGUIDataSyncTest.kt`

**Definition of Done:**
```
✅ 5+ integration tests written and passing
✅ Coverage: GUI switching, data sync, navigation
✅ No flakes (tests pass 100 times)
✅ CI integration ready
```

---

## PHASE 2: TECHNICAL DEBT RESOLUTION (Weeks 2-3)

### 2.1 Gradle 10 Migration
**Owner:** DevOps Lead  
**Effort:** 2-3 days  
**Priority:** 🟠 HIGH  
**Impact:** Prevents build failures in 6-12 months

**Deprecated Features to Fix:**
1. `org.gradle.configuration-cache=false` → May re-enable with fixes
2. `ksp.incremental=false` → Re-enable after KSP updates
3. `android.enableIncrementalBuilds=false` → Update to use new API
4. R8/ProGuard settings → Migrate to AGP 8.3+ format

**Tasks:**
- [ ] Create `docs/GRADLE_10_MIGRATION_PLAN.md`
- [ ] Test with Gradle 10 preview (dry run)
- [ ] Update `build.gradle.kts` for compatibility
- [ ] Run `./gradlew build --warning-mode all` and fix warnings
- [ ] Update CI/CD (if using GitHub Actions)
- [ ] Verify all tests still pass

**Reference:** `docs/GRADLE_MIGRATION_ROADMAP.md` (already exists)

**Definition of Done:**
```
✅ Build succeeds with Gradle 9.2.1
✅ Build succeeds with Gradle 10 preview (warnings addressed)
✅ No deprecated feature warnings
✅ Build performance unchanged
```

### 2.2 KDoc & Documentation Completion
**Owner:** Tech Writer  
**Effort:** 3-5 days  
**Priority:** 🟡 MEDIUM  
**Impact:** Easier onboarding, better IDE support

**Coverage:**
- [ ] All public ViewModel functions documented (8+ methods each)
- [ ] All use cases documented (9 use cases × 3-5 methods)
- [ ] Repository interfaces documented (16 interfaces)
- [ ] Validator classes documented (10 validators)
- [ ] Data models documented (20+ models)

**Target:** 80%+ public API coverage

**Definition of Done:**
```
✅ 80%+ of public APIs have KDoc
✅ IDE shows descriptions on hover
✅ Generated KDoc HTML builds successfully
```

### 2.3 Firebase Analytics Events Setup
**Owner:** Analytics Lead  
**Effort:** 2 days  
**Priority:** 🟡 MEDIUM  
**Impact:** User behavior insights

**Key Events to Track:**
1. **User Lifecycle**
   - First launch
   - GUI selection (GUI1 vs GUI2)
   - Authentication success/failure
   - Session start/end

2. **Feature Usage**
   - Invoice created
   - Payment recorded
   - Report viewed
   - Settings changed

3. **Error Events**
   - Offline queue sync failed
   - PDF generation failed
   - Validation error (generic)

**Tasks:**
- [ ] Create `FirebaseAnalyticsManager.kt`
- [ ] Define event schema (Event name, parameters)
- [ ] Instrument key screens and operations
- [ ] Test events fire correctly
- [ ] Document in `FIREBASE_ANALYTICS_GUIDE.md`

**Definition of Done:**
```
✅ 10+ events configured and firing
✅ Firebase console shows event data
✅ No crashes from analytics code
```

---

## PHASE 3: UI/UX POLISH & TESTING (Weeks 3-4)

### 3.1 Compose UI Testing (Espresso/ComposeTest)
**Owner:** QA Lead  
**Effort:** 2-3 days  
**Priority:** 🟠 HIGH  
**Impact:** Catches UI bugs early

**Scope:** GUI2 primary screens (20+ screens)

**Test Types:**
- Interaction tests (button clicks, input)
- Navigation tests (screen transitions)
- State tests (data display, updates)
- Accessibility tests (content descriptions)

**Framework:** AndroidX Compose Test API + Espresso

**Tasks:**
- [ ] Set up test infrastructure
- [ ] Write tests for 5-10 critical screens
- [ ] Run on emulator + real device
- [ ] Document patterns for team

**Files to Create:**
- `app/src/androidTest/java/com/emul8r/bizap/ui/gui2/CreateInvoiceScreenTest.kt`
- `app/src/androidTest/java/com/emul8r/bizap/ui/gui2/DashboardScreenTest.kt`
- `app/src/androidTest/java/com/emul8r/bizap/ui/gui2/SettingsScreenTest.kt`

**Definition of Done:**
```
✅ 10+ UI tests written and passing
✅ Tests run on emulator in <5 min
✅ No flakes (consistent pass rate)
```

### 3.2 Accessibility Audit (a11y)
**Owner:** UX Lead  
**Effort:** 1-2 days  
**Priority:** 🟡 MEDIUM  
**Impact:** Inclusive app, potential legal compliance

**Checks:**
- [ ] Content descriptions on all icons
- [ ] Text contrast ratios (WCAG AA)
- [ ] Touch target sizes (48dp minimum)
- [ ] Screen reader support (TalkBack)
- [ ] Keyboard navigation

**Tool:** Android Accessibility Scanner

**Tasks:**
- [ ] Run scanner on all screens
- [ ] Document findings (spreadsheet)
- [ ] Fix high-priority issues
- [ ] Add accessibility tests

**Definition of Done:**
```
✅ All icons have content descriptions
✅ Text contrast ratios WCAG AA
✅ 90%+ accessibility scanner issues resolved
```

### 3.3 Performance Profiling & Optimization
**Owner:** Performance Lead  
**Effort:** 2-3 days  
**Priority:** 🟡 MEDIUM  
**Impact:** Smoother user experience

**Profiling Targets:**
1. **Startup time** (current: ~2-3s → target: <2s)
   - Lazy load modules
   - Defer non-critical initialization
   
2. **Memory usage** (current: ~60-80MB → target: <60MB)
   - Profile with Android Profiler
   - Identify leaks
   - Optimize large objects

3. **Frame rate** (current: target 60fps)
   - Use Compose Compiler Metrics
   - Identify recomposition hotspots
   - Optimize with Stability annotations

**Tasks:**
- [ ] Profile startup with Profiler
- [ ] Profile memory with Profiler
- [ ] Analyze frame drops (UI Profiler)
- [ ] Implement top 5 optimizations
- [ ] Re-measure and document baseline

**Definition of Done:**
```
✅ Startup time <2 seconds
✅ Memory baseline <60MB
✅ 60 FPS on 90% of interactions
✅ Performance report generated
```

---

## PHASE 4: ADVANCED FEATURES & POLISH (Weeks 4+)

### 4.1 Cloud Backup Foundation (Optional)
**Owner:** Backend Lead  
**Effort:** 5-7 days  
**Priority:** 🟢 LOW (Post-launch)  
**Impact:** Data resilience, multi-device sync

**Architecture:**
```
Local DB (SQLCipher)
    ↓ Sync Engine
Cloud API (Firebase Realtime DB or custom)
    ↓
Remote Storage
```

**Tasks:**
- [ ] Design sync protocol (bidirectional, conflict resolution)
- [ ] Implement upload service (invoices, customers, payments)
- [ ] Implement download service (restore, sync)
- [ ] Handle network failures gracefully
- [ ] Write unit + integration tests

**Definition of Done:**
```
✅ Data syncs to cloud on creation
✅ Cloud data syncs to local on app startup
✅ Conflict resolution works (last-write-wins or custom)
✅ Tests pass: upload, download, conflict handling
```

### 4.2 Push Notifications & Reminders (Optional)
**Owner:** Backend Lead  
**Effort:** 3-5 days  
**Priority:** 🟢 LOW (Post-launch)  
**Impact:** User engagement, timely reminders

**Use Cases:**
1. Overdue invoice reminder
2. Payment received notification
3. Invoice sent confirmation
4. Low business context reminder

**Implementation:**
- Firebase Cloud Messaging (FCM) for delivery
- WorkManager for scheduling
- Local notifications for testing

**Tasks:**
- [ ] Set up FCM in Firebase Console
- [ ] Create notification service
- [ ] Add notification preferences screen
- [ ] Test with emulator

**Definition of Done:**
```
✅ Notifications send to device
✅ Clicking notification opens relevant screen
✅ Users can disable notifications
```

### 4.3 GUI1 Sunset Preparation (Weeks 4+)
**Owner:** Product + Eng Lead  
**Effort:** 3-5 days  
**Priority:** 🟡 MEDIUM (Post-launch)  
**Impact:** Sets timeline for cleanup

**Tasks:**
- [ ] Create `GUI1_SUNSET_ROADMAP.md` (user communication)
- [ ] Add deprecation banner to GUI1 (after Phase B)
- [ ] Plan feature parity completion (any remaining)
- [ ] Set up automated deprecation warnings

**Definition of Done:**
```
✅ Sunset roadmap documented
✅ User communication plan ready
✅ Feature parity assessment updated
```

---

## TIMELINE VISUALIZATION

```
Week 1   Week 2   Week 3   Week 4
|--------|--------|--------|--------|

PHASE 1: CRITICAL (Weeks 1-2)
├─ Exchange Rate API    [████      ]  (15 min, Weeks 1-2)
├─ Payment History UI   [████████  ]  (2 hrs, Weeks 1-2)
└─ Integration Tests    [████████████](1 day, Weeks 1-2)

PHASE 2: TECH DEBT (Weeks 2-3)
├─ Gradle 10 Migration  [    ████████](2-3 days, Weeks 2-3)
├─ KDoc Completion      [    ████████████](3-5 days, Weeks 2-3)
└─ Firebase Events      [    ████████](2 days, Weeks 2-3)

PHASE 3: POLISH (Weeks 3-4)
├─ Compose UI Tests     [        ████████](2-3 days, Weeks 3-4)
├─ Accessibility Audit  [        ████](1-2 days, Weeks 3-4)
└─ Performance Profile  [        ████████](2-3 days, Weeks 3-4)

PHASE 4: ADVANCED (Week 4+)
├─ Cloud Backup         [            ████████████](5-7 days, opt)
├─ Notifications        [            ██████████](3-5 days, opt)
└─ GUI1 Sunset Plan     [            ████████](3-5 days, opt)
```

---

## RESOURCE ALLOCATION

### Estimated Team Needs
```
Android Engineer (Backend/Data):  50% capacity
- Exchange Rate API (15 min)
- Gradle migration (2-3 days)
- Cloud backup (5-7 days opt)
- Firebase events (2 days)

Android Engineer (Frontend/UI):   100% capacity
- Payment History UI (2 hours)
- UI testing (2-3 days)
- Accessibility audit (1-2 days)

QA Engineer:                       50% capacity
- Integration tests (1 day)
- Compose UI tests support (1 day)
- Accessibility scanner (1 day)

DevOps/Build:                      20% capacity
- Gradle migration support
- CI/CD updates

Total Effort: ~20-25 person-days spread over 4 weeks
```

---

## SUCCESS METRICS

### Week 1 Target: ✅ CRITICAL ITEMS COMPLETE
- [ ] Exchange Rate API working
- [ ] Payment History UI shipped
- [ ] Build passing, no API warnings

### Week 2 Target: ✅ TECH DEBT ADDRESSED
- [ ] Gradle migration plan finalized
- [ ] KDoc 50% complete
- [ ] Firebase events configured

### Week 3 Target: ✅ QUALITY GATES PASS
- [ ] UI tests passing (10+ tests)
- [ ] Accessibility: 90%+ issues fixed
- [ ] Performance baseline: <2s startup

### Week 4+ Target: 🚀 ADVANCED FEATURES IN FLIGHT
- [ ] Cloud backup in progress
- [ ] Notifications queued
- [ ] GUI1 sunset plan approved

### Final Health Score Target: 🎯 9.5/10
```
Before:                After:
Architecture:  9/10 → 9/10
Code Quality:  8.5  → 9/10  (+KDoc, +Testing)
Test Coverage: 8.5  → 9.5   (+Integration, +UI tests)
Performance:   8    → 9     (+Profiling optimizations)
Security:      8.5  → 8.5   (unchanged)
Documentation: 7.5  → 8.5   (+KDoc)
Feature Comp:  9    → 9.5   (+Payment History UI)
Build Stability: 9  → 9.5   (+Gradle 10 prep)
Dependency Mgmt: 8  → 8.5   (Gradle 10 migration started)
Dual GUI:      5    → 6     (Sunset plan ready)

OVERALL:       8.5/10 → 9.3/10 ✅
```

---

## RISK MITIGATION

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|-----------|
| Gradle migration breaks build | MEDIUM | HIGH | Test with preview, fallback branch |
| Payment History UI causes crashes | LOW | MEDIUM | Thorough testing + QA + code review |
| Performance optimizations regress tests | LOW | MEDIUM | A/B test, profile carefully |
| Integration tests are flaky | MEDIUM | MEDIUM | Run 100x locally before merge |
| Team bandwidth exceeded | MEDIUM | MEDIUM | Prioritize Phase 1 + 2; defer Phase 4 |

---

## APPROVAL & SIGN-OFF

**Prepared By:** Copilot (AI Assistant)  
**Date:** March 24, 2026  
**Status:** 📋 READY FOR REVIEW

**Approval Required:**
- [ ] Product Owner (feature prioritization)
- [ ] Tech Lead (architecture, implementation plan)
- [ ] QA Lead (testing strategy)
- [ ] DevOps Lead (CI/CD changes)

---

## APPENDIX: DETAILED TASK BREAKDOWN

### Appendix A: Exchange Rate API Integration (15 min)

**Step 1: Get API Key**
```bash
# Visit https://www.exchangerate-api.com/
# Sign up for free account
# Copy API key (1000 requests/month free)
```

**Step 2: Configure**
```
# File: gradle.properties (local)
EXCHANGE_RATE_API_KEY=pk_abc123xyz...
```

**Step 3: Verify**
```bash
./gradlew clean build
# Should NOT show "⚠️ EXCHANGE_RATE_API_KEY not found!"
```

**Step 4: Test**
- Open Bizap
- Create invoice with different currency
- Verify exchange rate populates automatically

### Appendix B: Payment History UI (2 hours)

**Step 1: Design** (30 min)
- Sketch UI (timeline, payment cards, export button)
- Decide on layout (vertical list vs grid)
- Define interactions (click → payment detail, export → PDF)

**Step 2: Implementation** (90 min)
```kotlin
// File: PaymentHistoryScreen.kt (GUI2)
@Composable
fun PaymentHistoryScreen(invoiceId: Long) {
    val viewModel: PaymentHistoryViewModel = hiltViewModel()
    val payments by viewModel.payments.collectAsStateWithLifecycle()
    
    LazyColumn {
        items(payments) { payment ->
            PaymentCard(payment)
        }
    }
}

// Add to InvoiceDetailScreen as new tab
// Wire navigation
```

**Step 3: Testing** (30 min)
- Write 3-5 integration tests
- Test on emulator
- Manual QA

---

## NEXT STEPS

1. **Review & Approve** this roadmap (stakeholder alignment)
2. **Assign Owners** for each phase
3. **Create Jira/GitHub Issues** for each task
4. **Kick off Week 1** with standup alignment
5. **Weekly Sync** on progress + blockers
6. **Mid-point Review** (after Week 2) for Phase 3-4 re-prioritization

**Questions?** Contact your Tech Lead or Product Owner.


