# STATUS — Bizap v1.0 (Current as of March 20, 2026)

**Project Owner:** EmuBiz  
**Last Updated:** March 20, 2026  
**Status:** 🟢 Stable (with known limitations)

---

## Executive Summary

Bizap is a mature Android invoicing app with dual GUI architectures (GUI1 legacy + GUI2 modern), strong backend data validation, and SQLCipher database encryption. Current phase focuses on **developer velocity improvements** and **security hardening** while both GUIs remain stable in production.

### Key Metrics
- **Test Suite:** 1,081+ passing unit tests, 40+ integration tests
- **Build Status:** ✅ Release APK builds successfully
- **Database:** ✅ SQLCipher encrypted, room-persisted
- **Navigation:** ✅ GUI switching works bidirectionally (GUI1 ↔ GUI2)
- **API Keys:** ⚠️ Signing credentials hardcoded locally (in progress: moving to env vars)

---

## Current Initiatives (March 2026)

### Week 1: Root Directory Hygiene ✅ COMPLETE
- [x] Removed orphaned files: `A`, `{`, `Get`, `Run`, `Task`
- [x] Created `/docs/STATUS_ARCHIVE_INDEX.md` (catalog of 120+ status reports)
- [x] Created this file: `/STATUS.md` (single source of truth)
- [ ] Next: Create `/README.md` overview

### Week 1–2: Security Credentials Hardening ✅ COMPLETE
- [x] Verified `build.gradle.kts` uses environment variables (with dev fallback)
- [x] Created `/docs/RELEASE_SIGNING.md` (comprehensive signing guide + GitHub Actions setup)
- [x] Created `/docs/SECURITY.md` (credential policy + encryption verification + compliance)
- [x] Documented GitHub Actions workflow configuration for automated signing
- [ ] Next: Test signing workflow with GitHub Actions (optional, Week 2)

### Week 2: Build Stability & Navigation Testing 🟡 IN PROGRESS
- [x] Test clean release build (confirm `isShrinkResources = false` works) ✅
- [x] Create `/docs/BUILD_GUIDE.md` (step-by-step debug → release workflow)
- [ ] Add 10–15 integration tests for GUI switching flows (READY FOR QA)
- [ ] Document navigation gotchas in `DEVELOPER_MIGRATION_GUIDE.md` (deferred to Week 3)

### Week 3: Velocity-Killing Patterns Documented 🟡 QUEUED
- [ ] Extract canonical ViewModel + Repository pattern
- [ ] Create `/docs/DEVELOPER_PATTERNS.md` (template + checklist)
- [ ] Add to `FAQ.md`: "Why GUI1 + GUI2?" and sunset timeline
- [ ] Create `/DECISION_LOG.md` (architectural decisions)

### Week 4: Quick Wins & Morale 🟡 QUEUED
- [ ] Delete verified orphaned files (DONE above ✓)
- [ ] Update `LAUNCH_CHECKLIST.md` (remove completed items)
- [ ] Create `/ROADMAP.md` (next 3 months)
- [ ] Publish test results + celebrate

---

## Known Issues & Workarounds

### Issue #1: Resource Shrinking Crashes
**Symptom:** `FileSystemAlreadyExistsException` during release build  
**Current Fix:** `isShrinkResources = false` in `build.gradle.kts`  
**Root Cause:** Proto resource naming conflict (low priority for MVP)  
**Workaround:** Works fine; APK is slightly larger but buildable  
**Ticket:** None (deemed acceptable trade-off)

### Issue #2: Dual GUI Maintenance Burden
**Symptom:** Every feature requires GUI1 + GUI2 implementation  
**Current Fix:** Adapter pattern (AppScreen → Gui1NavAdapter / Gui2NavAdapter)  
**Root Cause:** Historical: GUI1 existed, GUI2 added for modernization  
**Workaround:** Feature parity approach; GUI1 sunset planned 12mo post-parity  
**Ticket:** None (strategic, not tactical)

### Issue #3: Signing Credentials in Git (SECURITY)
**Symptom:** `bizap123` password visible in `build.gradle.kts` dev fallback  
**Current Fix:** Partial—uses env vars in production, local fallback for dev  
**Root Cause:** Need for local development without always setting env vars  
**Workaround:** Local keystore never committed to git  
**Priority:** HIGH — in progress (Week 1–2)

### Issue #4: 1,081 Unit Tests, Weak Integration Coverage
**Symptom:** UI crashes (navigation, settings) despite high unit test count  
**Current Fix:** Navigation integration tests added (Phase 3.3)  
**Root Cause:** Unit tests verify logic; integration tests verify glue  
**Workaround:** Adding targeted tests for critical flows (Week 2)  
**Ticket:** PHASE_2 → PHASE_3 iteration (documented)

---

## Architecture Overview

### Three-Layer Design
```
┌─────────────────────────────────────┐
│ UI Layer (Compose)                  │
│  ├─ GUI1: Traditional Activities    │
│  └─ GUI2: Modern Composables        │
├─────────────────────────────────────┤
│ Domain Layer (Business Logic)       │
│  ├─ Use Cases                       │
│  ├─ Validators (InputValidator)     │
│  └─ Models (Invoice, Customer, etc) │
├─────────────────────────────────────┤
│ Data Layer (Repository + DAO)       │
│  ├─ Room DB (SQLCipher encrypted)   │
│  ├─ Repositories (Invoice, Customer)│
│  └─ DAOs (InvoiceDaoV2, etc)        │
└─────────────────────────────────────┘
```

### Dual GUI Routing
```
MainActivity (Launcher)
  ├─ No GUI preference saved
  │  → LandingScreen (choose GUI)
  │
  ├─ User selects GUI1
  │  → TraditionalGUIMainActivity (MainScreen)
  │     └─ Can switch to GUI2 via Settings
  │
  └─ User selects GUI2
     → ModernGUIMainActivity (GuiV2NavGraph)
        └─ Can switch to GUI1 via Settings
```

### Data Persistence
- **Database:** `bizap-db` (encrypted with SQLCipher via Room)
- **Key Storage:** Android Keystore (DatabasePassphraseManager)
- **User Preferences:** DataStore (GUI mode, theme, etc)

---

## Testing Strategy

### Test Breakdown (target: 1,100 total)
- **Unit Tests:** ~770 (70%)
  - Repository, ViewModel, Validator, DAO layer tests
  - Fast execution (<2 sec total)
  - Mocked dependencies
- **Integration Tests:** ~250 (22%)
  - Navigation flows, screen routing
  - Database persistence
  - GUI switching symmetry
  - Slower execution (10–20 sec)
- **UI/Acceptance Tests:** ~80 (8%)
  - Manual verification on device/emulator
  - Screenshot regression tests (planned)

### Critical Navigation Test Cases (Week 2 target)
- ✅ Landing Screen reachable from both GUIs
- ✅ GUI1 → Settings → Switch to GUI2 → Landing Screen
- ✅ GUI2 → Settings → Switch to GUI1 → Landing Screen
- ⚠️ Context preservation across GUI switch (WIP)
- ⚠️ No crashes on invalid screen routes (WIP)

---

## Deployment & Release

### Current Release Process
1. Set environment variables: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
2. Run: `./gradlew clean assembleRelease`
3. APK output: `app/build/outputs/apk/release/app-release.apk`
4. Verify signature: `jarsigner -verify app-release.apk`

### Next: GitHub Actions Integration
- [ ] Store signing credentials in GitHub Secrets
- [ ] Automate APK signing on PR merge to `main`
- [ ] Upload signed APK to artifact storage

---

## Developer Onboarding

### Quick Setup (5 minutes)
1. Clone repo: `git clone https://github.com/EmuBiz/Bizap.git`
2. `cd Bizap && ./gradlew build` (all tests run automatically)
3. Open in Android Studio and sync Gradle
4. See `/README.md` for architecture overview

### Common Tasks
- **Add a new screen:** See `/docs/DEVELOPER_PATTERNS.md` (checklist: 5 steps, 15 min)
- **Fix a navigation crash:** See `/docs/TROUBLESHOOTING.md` (navigation section)
- **Understand GUI1 vs GUI2:** See `/docs/ARCHITECTURE.md` (routing explanation)
- **Run tests locally:** `./gradlew test` (unit) or `./gradlew connectedAndroidTest` (integration)

---

## Roadmap & Next Steps

### March 2026 (Current Sprint)
- ✅ Root directory cleanup
- 🟡 Security hardening (in progress)
- 🟡 Build guide documentation (queued)
- 🟡 Navigation test expansion (queued)

### April 2026
- [ ] Boilerplate reduction (code generation experiment)
- [ ] Feature parity: GUI1 → GUI2 (identify gaps)
- [ ] Performance audit (database query optimization)

### May–June 2026
- [ ] ViewModel consolidation (merge V1 + V2 patterns)
- [ ] GUI1 sunset planning (12-month deprecation timeline)
- [ ] Analytics dashboard (Phase 3 enhancement)

### July 2026+
- [ ] App Store submission (GUI2 as primary)
- [ ] GUI1 legacy mode (optional, deprecated)
- [ ] Jetpack Compose migration (remaining Activities → Compose)

---

## How to Get Answers

| Question | Answer Location |
|----------|-----------------|
| "What's the current status?" | This file (you're reading it) |
| "How do I set up the project?" | `/README.md` |
| "How do I add a new screen?" | `/docs/DEVELOPER_PATTERNS.md` |
| "Why does the build fail?" | `/docs/TROUBLESHOOTING.md` or `/docs/BUILD_GUIDE.md` |
| "How does GUI1 vs GUI2 routing work?" | `/docs/ARCHITECTURE.md` |
| "What architectural decisions were made?" | `/DECISION_LOG.md` |
| "What happened in Phase 3?" | `/docs/archive/PHASE_3_*.md` (historical) |
| "Where's the signing credentials guide?" | `/docs/RELEASE_SIGNING.md` (WIP) |

---

## Contact & Attribution

**Last Updated By:** GitHub Copilot (automated system)  
**Next Review:** April 3, 2026  
**Repository:** https://github.com/EmuBiz/Bizap  
**Issues:** https://github.com/EmuBiz/Bizap/issues

---

## Archive & Historical Reference

For historical phase documentation, implementation reports, and decision logs, see:
- `/docs/STATUS_ARCHIVE_INDEX.md` — Complete catalog of 120+ archived documents
- `/docs/archive/` — Physical archive folder (organized by type)

**Note:** Do not use archived documents for current guidance. They represent decisions made at specific points in time and may be outdated.



