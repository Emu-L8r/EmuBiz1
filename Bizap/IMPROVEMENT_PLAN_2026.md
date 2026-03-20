# EmuBiz Build Improvement Plan 2026
## Strategic Roadmap: Performance First, Robustness Second

**Status**: March 20, 2026 - Stable Build Baseline v1.0.3  
**Tag**: `v1.0.3-stable-build-20260320`  
**Build Health**: ✅ All systems operational

---

## Executive Summary

The project has recovered from a critical module extraction failure and is now in a **stable, working state**. However, 10 significant architectural and operational flaws have been identified. This plan prioritizes quick wins that deliver measurable improvements without destabilizing the current build.

**Key Principles**:
- **Performance First**: Focus on build speed, runtime efficiency, and developer productivity
- **Robustness Second**: Improve reliability, reduce "magic numbers", eliminate fail-silent patterns
- **Non-Breaking**: All improvements maintain backward compatibility and current build success
- **Incremental**: Each improvement can be merged independently as a separate PR

---

## Priority Matrix

| Priority | Issue | Impact | Effort | ROI | Timeline |
|----------|-------|--------|--------|-----|----------|
| **P0** | Clean Architecture Violation | High | Low | 9/10 | Week 1 |
| **P0** | Magic Number Fallbacks | High | Low | 9/10 | Week 1 |
| **P1** | Hardcoded TopAppBar Titles | Medium | Low | 8/10 | Week 1 |
| **P1** | Redundant Vector Config | Low | Very Low | 8/10 | Week 1 |
| **P1** | Fail-Silent API Key | High | Low | 9/10 | Week 1 |
| **P2** | Lifecycle-Injection Guards | Medium | Medium | 7/10 | Week 2 |
| **P2** | Cyclomatic Complexity Boot | Medium | Medium | 7/10 | Week 2 |
| **P2** | Assertion Fragment | Medium | Medium | 6/10 | Week 2 |
| **P3** | Workflow Glue Script | Low | Low | 5/10 | Week 3 |
| **P3** | Insecure Signing Key Path | Medium | Low | 6/10 | Week 3 |

---

## PHASE 1: Quick Wins (Week 1) - High ROI, Low Risk

### Issue #1: Clean Architecture Violation (Domain Leakage)
**Problem**: Domain module depends on `androidx.room` and `androidx.paging`

**Current State**:
```
domain/build.gradle.kts
├── implementation(androidx.room:room-common)
├── implementation(androidx.paging:paging-common)
└── Uses @Entity, @Dao, @Query annotations
```

**Why This Breaks Architecture**:
- Domain should be pure Kotlin, framework-agnostic
- Creates coupling between business logic and persistence layer
- Violates dependency inversion principle
- Makes testing harder (must mock Room APIs)

**Solution**: Extract Room-specific types to Data layer  
**Effort**: 1-2 hours  
**Impact**: Cleaner separation, easier testing, better reusability  

**Action Plan**:
1. Create `domain/models/` with POJO/data classes only
2. Move `@Entity` models to `data/local/entities/`
3. Create mappers in data layer: `EntityToModel` converters
4. Remove Room dependencies from domain
5. Update imports across codebase

**Estimated PR**: `refactor/clean-domain-architecture`

---

### Issue #2: Magic Number Fallbacks (1L Business ID)
**Problem**: MainActivity hardcodes `startBusinessId = 1L` with no validation

**Current Risk**:
```kotlin
val startBusinessId = activeBusinessId ?: 1L  // ⚠️ Assumes ID 1 always exists
```

**Why This Fails**:
- Fresh install: no business with ID 1
- Multi-profile scenario: wrong business selected
- Silent data corruption risk
- No error feedback to user

**Solution**: Explicit validation with user feedback  
**Effort**: 30 minutes  
**Impact**: Prevents silent bugs, improves UX  

**Action Plan**:
1. Create `BusinessIdValidator` domain usecase
2. When business ID is null:
   - Query `getAllBusinesses()`
   - If empty → show onboarding dialog
   - If one → use it
   - If multiple → show business picker
3. Remove magic number from MainActivity

**Estimated PR**: `fix/explicit-business-id-validation`

---

### Issue #3: Hardcoded TopAppBar Titles
**Problem**: Massive `when` block in MainActivity mapping routes to strings

**Current Pain**:
```kotlin
when {
    currentDestination?.hasRoute<Screen.InvoiceList>() -> "Invoices"
    currentDestination?.hasRoute<Screen.CustomerList>() -> "Customers"
    currentDestination?.hasRoute<Screen.Dashboard>() -> "Dashboard"
    // ... 30+ more cases, all hardcoded
}
```

**Why This Scales Badly**:
- Every new screen requires manual update
- Easy to forget, causing missing titles
- Mixes UI strings with navigation logic
- Not localizable for i18n

**Solution**: Route metadata + resource lookup  
**Effort**: 45 minutes  
**Impact**: Eliminates future manual updates, enables i18n  

**Action Plan**:
1. Create `NavMetadata` sealed class:
   ```kotlin
   sealed class Screen {
       abstract val titleRes: Int
       data object InvoiceList : Screen() { 
           override val titleRes = R.string.screen_invoices 
       }
   }
   ```
2. Create `strings.xml` entries for all screens
3. Replace when block with `currentDestination?.titleRes`
4. Add unit test for coverage

**Estimated PR**: `refactor/route-metadata-titles`

---

### Issue #4: Fail-Silent API Key Configuration
**Problem**: `EXCHANGE_RATE_API_KEY` defaults to empty string silently

**Current Failure Mode**:
```kotlin
buildConfigField("String", "EXCHANGE_RATE_API_KEY",
    project.findProperty("EXCHANGE_RATE_API_KEY")?.toString() ?: "\"\""
)
// App builds successfully but crashes at runtime if key missing
```

**Why This Is Dangerous**:
- Build succeeds with no indication of missing key
- Runtime crash in production is hard to diagnose
- No guidance to developer on how to fix
- Silent failure in CI/CD

**Solution**: Fail-fast with clear guidance  
**Effort**: 20 minutes  
**Impact**: Prevents production crashes, improves DX  

**Action Plan**:
1. In `build.gradle.kts`:
   ```kotlin
   val exchangeRateKey = project.findProperty("EXCHANGE_RATE_API_KEY")
   if (exchangeRateKey == null) {
       throw GradleException("""
           ❌ EXCHANGE_RATE_API_KEY not found!
           
           Add to local.properties:
           EXCHANGE_RATE_API_KEY=your_api_key_here
           
           Get key: https://openexchangerates.org/signup
       """.trimIndent())
   }
   buildConfigField("String", "EXCHANGE_RATE_API_KEY", "\"$exchangeRateKey\"")
   ```
2. Create `README_CONFIG.md` with setup instructions

**Estimated PR**: `fix/fail-fast-api-key-validation`

---

### Issue #5: Redundant Vector Config
**Problem**: `vectorDrawables { useSupportLibrary = true }` with minSdk 26

**Current Code**:
```kotlin
android {
    defaultConfig {
        minSdk = 26  // ✅ Supports native vector drawables
        vectorDrawables { useSupportLibrary = true }  // ❌ Unnecessary
    }
}
```

**Why This Matters**:
- Native support since API 21
- Adds unnecessary dependency (`appcompat`)
- Increases APK size
- Slows down build slightly

**Solution**: Remove legacy support  
**Effort**: 5 minutes  
**Impact**: Cleaner build config, smaller APK  

**Action Plan**:
1. Remove `vectorDrawables { useSupportLibrary = true }` block
2. Verify no build errors
3. Measure APK size reduction

**Estimated PR**: `refactor/remove-redundant-vector-support`

---

## PHASE 2: Foundation Building (Week 2) - Medium ROI, Medium Effort

### Issue #6: Lifecycle-Injection Timing Guards
**Problem**: Race condition between Android lifecycle and Hilt injection

**Current Workaround**:
```kotlin
override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    if (::authManager.isInitialized) {  // ⚠️ Race condition check
        // ... handle touch
    }
    return super.dispatchTouchEvent(ev)
}
```

**Root Cause**: Activity used before Hilt injection complete

**Solution**: Proper lifecycle coordination  
**Effort**: 2-3 hours  
**Impact**: Eliminates race condition, more robust startup  

**Action Plan**:
1. Move authManager usage to `onStart()` or `onCreate()`
2. Use `ViewModel` for state (Hilt-ready earlier)
3. Guard with `isInitialized` only if absolutely necessary
4. Add integration test for startup race condition

**Estimated PR**: `refactor/fix-lifecycle-injection-timing`

---

### Issue #7: Cyclomatic Complexity at Boot
**Problem**: Massive nested `when` for AppState + GuiMode selection

**Current Code Structure**:
```kotlin
when (appState) {
    is AppState.Splash → SplashScreen()
    is AppState.PINEntry → PINSetupScreen()
    is AppState.Login → LoginScreen()
    is AppState.Warning → FirstLaunchWarningDialog()
    is AppState.Ready → when (guiMode) {
        GuiMode.GUI1 → GuiV1NavGraph()
        GuiMode.GUI2 → GuiV2NavGraph()
    }
}
```

**Why This Is Hard to Test**:
- 7+ nested levels of state
- Difficult to unit test each transition
- Hard to add new states
- Composition explosion

**Solution**: State machine pattern with separate handlers  
**Effort**: 3-4 hours  
**Impact**: Easier testing, clearer logic  

**Action Plan**:
1. Create `StartupStateHandler` sealed class:
   ```kotlin
   sealed class StartupState {
       data object SplashState : StartupState()
       data object PINState : StartupState()
       data class ReadyState(val guiMode: GuiMode) : StartupState()
   }
   ```
2. Create composable handler per state
3. Simplify MainActivity `setContent` to single dispatch
4. Add unit tests for each state transition

**Estimated PR**: `refactor/startup-state-machine`

---

### Issue #8: Assertion Style Fragmentation
**Problem**: Multiple assertion libraries in test dependencies

**Current Mess**:
```gradle
testImplementation(kotlin("test"))  // kotlin.test.assertEquals
testImplementation(junit:junit)     // org.junit.Assert
testImplementation(io.mockk:mockk)  // io.mockk assertions
testImplementation(org.mockito:mockito-core)  // org.mockito assertions
```

**Why This Matters**:
- 4 different assertion styles
- Inconsistent test code
- Harder to review and maintain
- Developer confusion

**Solution**: Standardize on one assertion library  
**Effort**: 2-3 hours  
**Impact**: Consistent tests, easier maintenance  

**Action Plan**:
1. Choose standard: `AssertJ` or `Truth`
2. Add single assertion library to deps
3. Replace all assertions with standard style
4. Update test templates/documentation
5. Run tests to verify no regressions

**Estimated PR**: `refactor/standardize-test-assertions`

---

## PHASE 3: Security & Stability (Week 3) - Lower ROI, Important

### Issue #9: Workflow Script Overreliance
**Problem**: Root directory filled with "fix" scripts (fragile build indicator)

**Current State**:
```
./fix-build.ps1
./quick-recovery.ps1
./verify.sh
./deploy-apk-stream1.ps1
```

**Why This Is Bad**:
- Indicates build is brittle
- Scripts become outdated quickly
- Not runnable in CI/CD
- Creates "tribal knowledge"

**Solution**: Proper Gradle tasks + CI/CD  
**Effort**: 4-5 hours  
**Impact**: Professional build pipeline, CI/CD ready  

**Action Plan**:
1. Create `buildSrc/build-tasks.gradle.kts`:
   - `verifyBuild`: Clean, build, test
   - `fixBuild`: Clean, rebuild with diagnostics
   - `deployDebug`: Build APK, install on emulator
2. Document in `DEVELOPER_GUIDE.md`
3. Archive old scripts to `scripts-archive/`
4. Add GitHub Actions workflow

**Estimated PR**: `infra/migrate-scripts-to-gradle-tasks`

---

### Issue #10: Insecure Signing Key Path
**Problem**: Keystore referenced outside project, plain-text passwords

**Current Risk**:
```gradle
signingConfigs {
    release {
        storeFile = file("../release-key.jks")  // Outside project!
        keyPassword = "hardcoded_password"       // ⚠️ Plain text!
    }
}
```

**Why This Fails Security**:
- Keystore outside project → accidentally shared
- Plain-text passwords in repo
- Weak key management
- Production release at risk

**Solution**: Secure key management with Gradle properties  
**Effort**: 1-2 hours  
**Impact**: Proper security, production-ready  

**Action Plan**:
1. Move signing to `local.properties` (gitignored):
   ```
   SIGNING_STORE_FILE=/secure/path/release-key.jks
   SIGNING_STORE_PASSWORD=***
   SIGNING_KEY_ALIAS=***
   SIGNING_KEY_PASSWORD=***
   ```
2. In `build.gradle.kts`:
   ```kotlin
   if (File("local.properties").exists()) {
       signingConfigs {
           release {
               storeFile = file(project.property("SIGNING_STORE_FILE"))
               storePassword = project.property("SIGNING_STORE_PASSWORD")
               keyAlias = project.property("SIGNING_KEY_ALIAS")
               keyPassword = project.property("SIGNING_KEY_PASSWORD")
           }
       }
   }
   ```
3. Update CI/CD to inject properties safely
4. Document in `SECURITY.md`

**Estimated PR**: `security/secure-signing-key-management`

---

## Implementation Timeline

```
Week 1 (Performance + Prevention):
├── Mon: Issue #1 (Domain Arch) - 2h
├── Mon: Issue #2 (Magic Numbers) - 0.5h
├── Tue: Issue #3 (Route Metadata) - 0.75h
├── Tue: Issue #4 (API Key Validation) - 0.33h
├── Wed: Issue #5 (Vector Config) - 0.08h
└── Wed-Thu: Review, test, merge (all 5 PRs)

Week 2 (Robustness):
├── Mon: Issue #6 (Lifecycle Guards) - 3h
├── Tue-Wed: Issue #7 (State Machine) - 4h
├── Thu: Issue #8 (Test Assertions) - 2h
└── Fri: Integration testing, review

Week 3 (Ops + Security):
├── Mon: Issue #9 (Gradle Tasks) - 4h
├── Tue: Issue #10 (Signing Keys) - 1.5h
├── Wed: CI/CD setup - 2h
└── Thu-Fri: Documentation, final review
```

---

## Success Metrics

| Metric | Current | Target | Timeline |
|--------|---------|--------|----------|
| Build Time | 4m 34s | 3m 30s | Week 1-2 |
| APK Size (Release) | TBD | -2-3% | Week 1 |
| Test Coverage | 50%+ | 70%+ | Week 2 |
| Architecture Score | 6/10 | 9/10 | Week 1 |
| Security Score | 4/10 | 8/10 | Week 3 |
| CI/CD Ready | No | Yes | Week 3 |

---

## Risk Mitigation

Each PR will:
1. ✅ Include comprehensive unit tests
2. ✅ Maintain backward compatibility
3. ✅ Include revert instructions in commit message
4. ✅ Run full test suite before merge
5. ✅ Build release APK to verify no regressions

**Rollback Strategy**: Tag before each phase, revert to `v1.0.3-stable-build-20260320` if needed

---

## Next Steps (Immediate)

1. **Review this plan** - 15 min
2. **Prioritize issues** - Am I aligned with your goals?
3. **Start Phase 1 PRs** - Begin with Issue #1 (Domain Arch)
4. **Weekly reviews** - Sync on progress and adjust

---

## Questions to Clarify

- **GUI1 vs GUI2**: Should we consolidate to single GUI or keep both?
- **Testing Strategy**: Focus on unit tests or E2E?
- **Release Cadence**: What's your deployment schedule?
- **Team Size**: How many developers will maintain this?

---

**Generated**: March 20, 2026  
**Status**: Ready for implementation  
**Owner**: Development Team  
**Next Review**: End of Week 1

