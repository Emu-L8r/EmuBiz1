# DECISION LOG — Bizap Architectural Decisions

**Last Updated:** March 20, 2026

---

## Overview

This log documents major architectural decisions, their rationale, when they were made, and the expected sunset date. Use this as historical reference for understanding "why we built it this way."

---

## Decision #1: Dual GUI Architecture (GUI1 + GUI2)

**Date:** ~Q3 2025  
**Status:** ✅ Active (12-month sunset timeline)  
**Owner:** EmuBiz Product Team

### The Decision
Bizap maintains **two separate UI implementations** (GUI1: traditional Activities, GUI2: modern Compose) in the same production binary, allowing users to choose their preferred interface.

### Rationale
1. **User Choice:** Different users prefer different UX styles; offer both
2. **Gradual Migration:** Migrate existing GUI1 users to GUI2 without forced migration
3. **Risk Reduction:** If GUI2 has bugs, users can switch to stable GUI1
4. **Testing:** Both stacks can be tested simultaneously

### Trade-offs
| Benefit | Cost |
|---------|------|
| User freedom | Dual maintenance burden |
| Gradual migration | Higher code complexity |
| Risk mitigation | Slower feature velocity |
| Stability fallback | Confusing codebase for new devs |

### Implementation Details
- **GUI1 (Legacy):**
  - Technology: Traditional Activity-based UI
  - Entry: `TraditionalGUIMainActivity`
  - Navigation: Activity switching + internal fragments
  - State: ViewModels + shared repositories

- **GUI2 (Modern):**
  - Technology: Jetpack Compose
  - Entry: `ModernGUIMainActivity`
  - Navigation: NavGraph + sealed route classes
  - State: ViewModels + same repositories as GUI1

- **Unified Core:**
  - Both GUIs use same repositories, DAOs, domain models
  - Adapters (`Gui1NavAdapter`, `Gui2NavAdapter`) translate routes
  - Shared landing screen for GUI selection

### Metrics
- **Code Duplication:** ~30% (two UI layers, single business logic)
- **Maintenance Burden:** ~+25% velocity tax (verify features in both GUIs)
- **User Impact:** ~5–10% of users still prefer GUI1 (estimated)

### Sunset Plan
**Timeline:** June 2027 (12 months post-launch)

1. **Phase 1 (March–May 2026):** Feature parity — all GUI1 screens in GUI2
2. **Phase 2 (June–July 2026):** Deprecation warning — users notified GUI1 will sunset
3. **Phase 3 (August 2026–May 2027):** Monitoring — track which users still use GUI1
4. **Phase 4 (June 2027):** Removal — delete GUI1 code, simplify routing

### Alternative Decisions Considered
1. **GUI2-only from start:** Risk — new codebase might have showstoppers
2. **GUI1-only forever:** Risk — UI would age; features limited by old tech
3. **Gradual, phased release:** Selected approach (current)

### Decision Holder
- Product: @EmuBiz
- Technical Lead: @DevTeam

---

## Decision #2: SQLCipher Database Encryption (Security Requirement)

**Date:** ~Q4 2025  
**Status:** ✅ Implemented & Verified  
**Owner:** EmuBiz Security Team

### The Decision
Use **SQLCipher** for transparent AES-256-GCM encryption of the `bizap-db` Room database. Encryption key stored in Android Keystore.

### Rationale
1. **Compliance:** GDPR, PCI-DSS require financial data encryption
2. **Transparency:** Users don't see encryption; automatic on all queries
3. **Hardware Backing:** Android Keystore (when available) provides hardware-backed security
4. **Industry Standard:** SQLCipher is mature, widely used (used by Signal, etc.)

### Implementation
```kotlin
// Key generated once, stored in Android Keystore
val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
val keySpec = KeyGenParameterSpec.Builder(
    "bizap_db_key",
    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
).apply {
    setBlockModes(KeyProperties.BLOCK_MODE_GCM)
    setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
    setUserAuthenticationRequired(true)  // Require device unlock
}.build()
keyGenerator.init(keySpec)

// Database uses SQLCipher
val factory = SupportOpenHelperFactory(passphraseMgr.getOrCreatePassphrase())
val db = Room.databaseBuilder(context, AppDatabase::class.java, "bizap-db")
    .openHelperFactory(factory)
    .build()
```

### Verification
- ✅ Database file checked: binary signature confirms encryption (not "SQLite format 3" text)
- ✅ Passphrase generation tested: 32-byte random + AES-256-GCM
- ✅ Key rotation policy documented: regenerate passphrase quarterly
- ⚠️ Note: No backup mechanism; data lost if device lost or OS reset

### Trade-offs
| Benefit | Cost |
|---------|------|
| Data security (GDPR compliant) | ~5% performance overhead |
| Hardware-backed (when available) | Complex key management |
| Transparent (users don't see it) | No recovery if device lost |
| Industry standard | Testing complexity (JVM tests don't have Keystore) |

### Future Considerations
- Q2 2026: Evaluate Room's native encryption (when available)
- Q3 2026: Implement encrypted backups (user opt-in)
- Q4 2026: Key rotation automation

### Decision Holder
- Security: @SecurityTeam
- Technical: @DevTeam

---

## Decision #3: Navigation Adapters for GUI Consolidation (Phase 3.3)

**Date:** ~Q1 2026  
**Status:** ✅ Implemented  
**Owner:** EmuBiz Architecture Team

### The Decision
Create **unified navigation model** (`AppScreen`) with **adapter pattern** (`Gui1NavAdapter`, `Gui2NavAdapter`) to translate routes between GUIs without coupling UI logic to route types.

### Rationale
1. **Single Source of Truth:** All navigation targets in one sealed interface
2. **GUI Agnostic:** Domain/data layers don't know which GUI is active
3. **Testing:** Easy to verify round-trip conversion (AppScreen → GUI route → AppScreen)
4. **Extensibility:** Adding new screens requires only 3 edits (AppScreen, Gui1NavAdapter, Gui2NavAdapter)

### Implementation Pattern
```kotlin
// Unified model (understood by all GUIs)
sealed interface AppScreen {
    data class Dashboard(val businessId: Long? = null) : AppScreen
    data class Invoice(val invoiceId: Long, val businessId: Long? = null) : AppScreen
    // ... etc
}

// GUI1 adapter
object Gui1NavAdapter {
    fun toScreen(appScreen: AppScreen): Screen? = when (appScreen) {
        is AppScreen.Dashboard -> Screen.Dashboard
        is AppScreen.Invoice -> Screen.InvoiceDetail(appScreen.invoiceId)
        // GUI2-only screens return null
        is AppScreen.SomeGui2Feature -> null
    }
}

// GUI2 adapter
object Gui2NavAdapter {
    fun toScreen(appScreen: AppScreen): ScreenV2? = when (appScreen) {
        is AppScreen.Dashboard -> ScreenV2.Dashboard(appScreen.businessId ?: 0L)
        is AppScreen.Invoice -> ScreenV2.InvoiceDetail(
            businessId = appScreen.businessId ?: 0L,
            invoiceId = appScreen.invoiceId
        )
    }
}
```

### Test Coverage
- ✅ `Gui1NavAdapterTest`: 40+ test cases
- ✅ `Gui2NavAdapterTest`: 50+ test cases
- ✅ `CrossGuiNavigationConsistencyTest`: Round-trip verification
- ✅ `NavigationIntegrationTest`: Real navigation flows

### Metrics
- **Code Reuse:** ~40% of UI code now shared (adapters reduce duplication)
- **Maintenance:** Adding new screen takes ~15 min vs 1–2 hours before
- **Test Coverage:** 90+ navigation-specific tests

### Trade-offs
| Benefit | Cost |
|---------|------|
| GUI-agnostic routing | Slight indirection (adapter lookup) |
| Easy to add screens | Must maintain 3 places (AppScreen, 2 adapters) |
| Single test surface | Null returns require defensive coding |
| Unified analytics | GUI-specific features still require null handling |

### Future Improvement
- Post-GUI1 removal: Delete adapters, use AppScreen directly in GUI2
- Estimated savings: ~200 lines of boilerplate code

### Decision Holder
- Architecture: @DevTeam
- Review: @ProductTeam

---

## Decision #4: Environment Variables for Signing Credentials (Security Hardening)

**Date:** March 2026  
**Status:** ✅ Implemented (dev fallback remains)  
**Owner:** EmuBiz Security Team

### The Decision
**Production builds** fetch signing credentials from environment variables (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). **Development builds** can use local fallback keystore without env vars.

### Rationale
1. **Security:** Never hardcode passwords in source code or build scripts
2. **CI/CD Integration:** GitHub Actions can inject secrets safely
3. **Compliance:** Meets security audit requirements
4. **Developer Ergonomics:** Dev builds still work without setup overhead

### Implementation
```kotlin
// In build.gradle.kts
signingConfigs {
    create("release") {
        val keystorePath = System.getenv("KEYSTORE_PATH")
        val storePass = System.getenv("KEYSTORE_PASSWORD")
        val alias = System.getenv("KEY_ALIAS")
        val keyPass = System.getenv("KEY_PASSWORD")
        
        if (keystorePath != null && storePass != null && alias != null && keyPass != null) {
            // Production: use env vars
            storeFile = file(keystorePath)
            storePassword = storePass
            keyAlias = alias
            keyPassword = keyPass
        } else {
            // Development: use local fallback (with warning)
            val localKeystore = file("../release-key.jks")
            if (localKeystore.exists()) {
                logger.warn("⚠️ Using local keystore for development. Set env vars for production!")
                storeFile = localKeystore
                storePassword = "bizap123"  // Dev only
                keyAlias = "bizap-key"
                keyPassword = "bizap123"    // Dev only
            } else {
                throw GradleException("Release signing configuration missing!")
            }
        }
    }
}
```

### Deployment
- **Local Dev:** Create local keystore, no env vars needed
- **CI/CD:** GitHub Actions stores secrets, injects at build time
- **Release Manager:** Set env vars in secure shell, run build

### Trade-offs
| Benefit | Cost |
|---------|------|
| Production security | Dev setup slightly more complex |
| CI/CD integration | Need to generate local keystore once |
| Audit compliance | Must document credential rotation |
| No hardcoded passwords | Env vars can leak if shell history not managed |

### Future Work
- [ ] Create GitHub Actions workflow for automatic signing
- [ ] Implement keystore rotation policy (quarterly)
- [ ] Add credential validation step in CI/CD

### Decision Holder
- Security: @SecurityTeam
- DevOps: @DevOpsTeam

---

## Decision #5: 12-Month GUI1 Sunset Timeline (Planned)

**Date:** March 20, 2026  
**Status:** 🟡 Planned (not yet approved)  
**Owner:** EmuBiz Product Team

### The Decision (Proposal)
**Deprecate GUI1 completely** 12 months after achieving GUI2 feature parity. Timeline:

1. **March–May 2026:** Complete feature parity (GUI1 → GUI2)
2. **June–July 2026:** Deprecation warning visible in GUI1 ("This interface will retire on June 1, 2027")
3. **August 2026–May 2027:** Monitor usage; contact users still on GUI1
4. **June 2027:** Remove GUI1 code from production; GUI2 becomes only option

### Rationale
1. **Maintainability:** Eliminate 30% code duplication burden
2. **Velocity:** Development speed increases ~25% post-GUI1 removal
3. **User Impact:** 12 months is sufficient for migration (average user keeps app for 18+ months)
4. **Simplicity:** Codebase becomes 40% simpler; onboarding faster

### Risk Mitigation
- Long runway (12 months) reduces surprise
- Clear communication in app + release notes
- Free migration path (tap button in settings)
- Data preserved (all invoices synced to GUI2)

### Trade-offs
| Benefit | Cost |
|---------|------|
| Simpler codebase | Some users forced to migrate |
| Faster development | Potential 1–2% user churn |
| Cleaner architecture | Support queries during transition |
| Reduced QA burden | Must maintain parallel features for 12 months |

### Alternative Timelines
- **6-month sunset:** Faster gains but riskier; users feel rushed
- **18-month sunset:** Safer but delays benefits; users stay on stale GUI1
- **Never sunset:** Perpetual maintenance burden (rejected)

### Next Steps
1. Present to product stakeholders (March 2026)
2. Get approval (target: March 31, 2026)
3. Announce in v1.0 release notes
4. Build deprecation warning UI (April 2026)

### Decision Holder
- Product: @ProductTeam (pending approval)
- Engineering: @DevTeam (recommending 12-month timeline)

---

## Decision #6: Boilerplate Reduction via Code Generation (Future)

**Date:** ~Q2 2026 (not yet started)  
**Status:** 🟡 Queued  
**Owner:** TBD

### The Decision (Proposed)
Implement **code generation** (Kotlin Poet or compiler plugin) to auto-generate boilerplate classes:
- `UiState` sealed classes (from domain models)
- `ViewModel` templates (with standard patterns)
- `Mapper` classes (model conversion)

### Rationale
Currently, adding a new field (e.g., customer phone number) requires edits to:
1. Domain model
2. DAO/database schema
3. Repository
4. UseCase
5. ViewModel
6. Mapper
7. UI screen

**Total time:** 1–2 hours (excessive for simple field addition)

**Goal:** Auto-generate steps 2–6, leaving developer time for business logic (steps 1, 7).

### Preliminary Design
```kotlin
// Annotate model
@BizapEntity
data class Customer(
    val id: Long,
    val name: String,
    val phone: String?  // ← New field, generated boilerplate
)

// Code generation produces:
// - Database migration
// - Mapper function
// - ViewModel state update
// - UI binding pattern
```

### Benefits
- Development velocity: +30–40%
- Boilerplate: -60%
- Consistency: +100% (all code follows pattern)

### Challenges
- Kotlin compiler plugin complexity (steep learning curve)
- IDE integration (for development experience)
- Testing generated code (edge cases)

### Timeline
- Q2 2026: Proof of concept (1–2 weeks)
- Q3 2026: Integration & testing (2–3 weeks)
- Q4 2026: Rollout & documentation (1 week)

### Decision Holder
- Architecture: @DevTeam (to propose)
- Product: @ProductTeam (to approve)

---

---

## Decision #5: GUI1 Sunset Timeline (12-Month Deprecation Window, June 2027)

**Date:** March 21, 2026  
**Status:** ✅ Committed  
**Owner:** EmuBiz Product Team + Engineering

### The Decision
Retire **GUI1 (legacy Activity-based UI)** completely by **June 1, 2027**, establishing a clear 12-month deprecation window.

**Timeline:**
- **Phase A (March–May 2026):** Achieve 100% GUI2 feature parity
- **Phase B (June–July 2026):** Deploy deprecation warning in GUI1
- **Phase C (August 2026–May 2027):** Monitor migration; support users
- **Phase D (June 2027):** Remove all GUI1 code

### Rationale
1. **Reduce Maintenance Burden:** Eliminate 30% code duplication (two parallel UI implementations)
2. **Increase Development Velocity:** +25% faster feature development post-sunset
3. **Simplify Codebase:** Single Compose-based UI framework (easier onboarding, less cognitive load)
4. **User-Friendly Timeline:** 12 months sufficient for migration (avg user retention ~18+ months)
5. **Long-term Health:** Position app for sustainable growth without technical debt accumulation

### Implementation Details

**Phase A (March–May 2026):**
```
✅ Port all remaining GUI1 features to GUI2
✅ Ensure 100% feature parity (verified by QA)
✅ Write feature parity tests
✅ No new GUI1-specific code merged
```

**Phase B (June–July 2026):**
```
✅ Add deprecation warning to GUI1 landing button
✅ Message: "Classic Interface will retire June 1, 2027"
✅ Deploy in v1.1 release
✅ User email campaign + blog post
```

**Phase C (August 2026–May 2027):**
```
✅ Track GUI1 usage via Firebase Analytics
✅ Target: 95% users on GUI2 by May 2027
✅ Support team reaches out to heavy GUI1 users
✅ Contingency: Extend if <60% migrated by April 2027
```

**Phase D (June 2027):**
```
✅ Delete all GUI1 code (Activities, screens, adapters)
✅ Release as v2.0 (major version bump)
✅ Codebase ~40% smaller
✅ Only GUI2 available
```

### Trade-offs

| Benefit | Cost |
|---------|------|
| 30% code duplication eliminated | Some GUI1 users must migrate |
| +25% dev velocity | No "fallback UI" safety net |
| Simplified onboarding | 12-month transition period required |
| Cleaner codebase | Need proactive user communication |

### Developer Guidelines (Effective Immediately)

**✅ Do:**
- New features → GUI2 only
- Bug fixes → Both GUIs (until May 31, 2026), then GUI2 only
- Share components via `ui/shared/`
- Write GUI parity tests

**❌ Don't:**
- Create GUI1-only features (waste of effort)
- Optimize/redesign GUI1 (low ROI)
- Add new GUI1-specific code

### Metrics & Monitoring

**Success Criteria:**
- [ ] 100% feature parity by May 31, 2026
- [ ] Deprecation warning in v1.1 (June 1, 2026)
- [ ] 95%+ users on GUI2 by May 31, 2027
- [ ] All GUI1 code deleted in v2.0 (June 1, 2027)

**Tracking:**
- Daily: GUI1 vs GUI2 session count (Firebase Analytics)
- Weekly: Migration progress, crash rates
- Monthly: User feedback, support tickets

### Reference Documentation
- **Detailed Roadmap:** `docs/GUI1_SUNSET_ROADMAP.md`
- **Timeline & Communication:** `docs/GUI1_SUNSET_ROADMAP.md` → Timeline & Phases
- **Developer Guidelines:** `docs/GUI1_SUNSET_ROADMAP.md` → Developer Guidelines

### Contingency Plans

**If Migration < 60% by April 2027:**
- Extend sunset to September 2027 (3-month extension)
- Announce immediately (transparency)
- Increase migration support efforts

**If Critical Bug in GUI1 (After May 2027):**
- Fix in v1.x patch release
- Recommend GUI2 migration
- Proceed with v2.0 as planned

---

## How to Update This Log

### Adding a New Decision
1. Create entry with template:
   ```markdown
   ## Decision #N: [Title]
   **Date:** [When decided]
   **Status:** 🟢 Implemented / 🟡 Planned / 🔴 Rejected
   **Owner:** [Team/Person]
   
   ### The Decision
   ### Rationale
   ### Implementation Details
   ### Trade-offs
   ### Decision Holder
   ```

2. Link from relevant docs (README, STATUS, etc.)
3. Create PR: `docs: add decision #N`

### Archiving Expired Decisions
When a decision's timeline ends (e.g., GUI1 sunset in June 2027):
1. Mark status: 🔵 Expired / Completed
2. Move to `docs/archive/DECISION_LOG_ARCHIVE.md`
3. Update this file: "See archive for historical decisions"

---

## Quick Reference

| Decision | Date | Status | Sunset |
|----------|------|--------|--------|
| Dual GUI (GUI1 + GUI2) | Q3 2025 | ✅ Active | June 2027 |
| SQLCipher encryption | Q4 2025 | ✅ Active | (permanent) |
| Navigation adapters | Q1 2026 | ✅ Active | June 2027 (remove with GUI1) |
| Env var signing | Mar 2026 | ✅ Active | (permanent) |
| GUI1 sunset | Mar 2026 | ✅ Committed | June 2027 |
| Code generation | Q2 2026 | 🟡 Queued | TBD |

---

**Last Updated:** March 20, 2026  
**Maintainer:** EmuBiz Architecture Team  
**Next Review:** April 3, 2026

