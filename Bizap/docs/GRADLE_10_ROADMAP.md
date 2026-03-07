# Gradle 10 Migration Roadmap

**Project:** Bizap  
**Document Version:** 1.0  
**Created:** March 7, 2026  
**Status:** Future — no action required until Q4 2026

---

## Current State

| Component | Version | Notes |
|---|---|---|
| Gradle Wrapper | 9.2.1 | Stable, in active support |
| Android Gradle Plugin (AGP) | 8.5.0 | Compatible with Gradle 9.x |
| Kotlin | 2.0.21 | Compatible |
| Java toolchain | 17 | JVM target 17 |

### Current Deprecation Warnings (Non-Blocking)

There are 5 soft deprecation warnings from Gradle 9.2.1 that will become errors in Gradle 10:

1. **`Project.buildDir` property** — Deprecated in Gradle 8.8, removed in Gradle 10.
   - Current usage: referenced in some build scripts for output paths
   - Fix: Replace with `project.layout.buildDirectory`

2. **`Project.convention` access** — Convention mapping deprecated.
   - Current usage: indirect use via some Gradle plugins
   - Fix: Upgrade affected plugins when Gradle 10 versions are available

3. **`sourceSets.main.java.srcDirs` old API** — Replaced by newer `sourceSets` configuration DSL.
   - Fix: Migrate to `sourceSets { main { java { srcDir(...) } } }` style

4. **`DependencyHandler.add()` with `String` configuration** — Configuration name inference deprecated.
   - Fix: Use explicit configuration references

5. **Test filtering with `--tests` flag inconsistencies** — Minor API change in task configuration.
   - Fix: Update `build.gradle.kts` task configurations post-upgrade

> **None of these warnings affect the current build.** They are informational only in Gradle 9.x.

---

## Why Wait Until Q4 2026?

- Gradle 10 was not released as of March 2026
- AGP 8.x officially supports Gradle 9.x
- Breaking changes in Gradle 10 require AGP updates (typically 3–6 months after Gradle release)
- Early migration carries risk of incompatible plugins

---

## Migration Plan (When Gradle 10 Is Released)

### Phase 1: Preparation (2 weeks before migration)

- [ ] Check Gradle 10 release notes for breaking changes
- [ ] Verify AGP compatibility matrix at https://developer.android.com/studio/releases/gradle-plugin
- [ ] Audit all Gradle plugins for Gradle 10 compatibility
- [ ] Review all build scripts for deprecated API usage
- [ ] Create a dedicated migration branch

### Phase 2: Gradle Wrapper Upgrade

```bash
# Update gradle-wrapper.properties
cd Bizap
./gradlew wrapper --gradle-version=10.x.x --distribution-type=all
```

- [ ] Update `distributionUrl` in `gradle/wrapper/gradle-wrapper.properties`
- [ ] Run `./gradlew build` and capture all deprecation warnings/errors
- [ ] Fix each error in order of severity

### Phase 3: Fix Breaking Changes

For each of the 5 known soft-deprecation warnings:

#### 1. `buildDir` → `layout.buildDirectory`
```kotlin
// Before (deprecated)
val outputDir = project.buildDir

// After (Gradle 10 compatible)
val outputDir = project.layout.buildDirectory.get().asFile
```

#### 2. Convention mapping
- Update or replace any plugins using convention mapping
- Prefer Gradle extensions and service registration

#### 3. SourceSets API
```kotlin
// Before
sourceSets.main.java.srcDirs("src/main/kotlin")

// After
sourceSets {
    main {
        java {
            srcDir("src/main/kotlin")
        }
    }
}
```

#### 4. DependencyHandler string configurations
```kotlin
// Before
dependencies.add("implementation", ...)

// After
dependencies {
    implementation(...)
}
```

#### 5. Task configuration updates
- Review any custom task configuration using deprecated APIs
- Update to use `tasks.register {}` and `tasks.named {}` patterns

### Phase 4: AGP Upgrade

- [ ] Update AGP to the version compatible with Gradle 10 (check AGP release notes)
- [ ] Run `./gradlew build` — resolve any new AGP-specific errors
- [ ] Update Kotlin Gradle Plugin to the compatible version

### Phase 5: Verification

- [ ] Full build succeeds: `./gradlew assembleRelease`
- [ ] All 279+ unit tests pass: `./gradlew test`
- [ ] No new Lint errors: `./gradlew lint`
- [ ] APK size regression check (should remain ~24MB)
- [ ] Build time regression check (should remain <66s)

### Phase 6: Merge & Release

- [ ] Merge migration branch to main
- [ ] Update `Bizap/docs/GRADLE_10_ROADMAP.md` to mark as complete
- [ ] Document final Gradle and AGP versions in README

---

## Timeline

| Milestone | Target Date | Dependency |
|---|---|---|
| Gradle 10 GA release | Q3–Q4 2026 (estimated) | Gradle project |
| AGP compatible with Gradle 10 | Q4 2026 (estimated) | Google Android team |
| Begin migration | Q4 2026 | Gradle 10 + AGP available |
| Migration complete | Q4 2026 (2–3 weeks work) | Phase 1–5 above |

---

## References

- Gradle Release Notes: https://docs.gradle.org/current/release-notes.html
- AGP Compatibility Matrix: https://developer.android.com/studio/releases/gradle-plugin#updating-plugin
- Gradle 10 Migration Guide: (to be published by Gradle team)
