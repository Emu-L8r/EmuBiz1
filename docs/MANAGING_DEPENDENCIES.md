# Dependency Management Strategy

## Overview

Bizap uses a modern Gradle build system with version catalogs for centralized dependency management. This document outlines the strategy for managing dependencies, updates, and security.

## Version Catalog

Location: `gradle/libs.versions.toml`

### Structure

```toml
[versions]
# Core versions
kotlin = "2.0.21"
agp = "8.5.0"
...

[libraries]
# Libraries
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "core-ktx" }
...

[plugins]
# Gradle plugins
android-application = { id = "com.android.application", version.ref = "agp" }
...
```

## Version Pinning Strategy

We use a **three-tier pinning strategy** based on criticality:

### Tier 1: Production-Critical (Strict Pinning)

**Dependencies:**
- Room 2.6.1
- SQLCipher 4.5.x
- Kotlin 2.0.21
- AGP 8.5.0
- KSP 2.0.21-1.0.26

**Policy:**
- Exact version pinning
- Only update for critical security fixes
- Require full regression testing before update
- Quarterly review cycle

**Example:**
```toml
[versions]
room = "2.6.1"  # Exact version
```

### Tier 2: Feature-Critical (Flexible Pinning)

**Dependencies:**
- Compose BOM 2024.12.01
- Hilt 2.51.1
- Firebase BOM 34.9.0
- Navigation 2.8.5
- Lifecycle 2.8.7

**Policy:**
- Minor version pinning (patch updates allowed)
- Update monthly for features/fixes
- Moderate testing required
- Monthly review cycle

**Example:**
```toml
[versions]
compose-bom = "2024.12.01"  # Specific BOM version
```

### Tier 3: Utility Libraries (Open Pinning)

**Dependencies:**
- Timber 5.0.1
- Coroutines 1.7.3
- Coil 2.7.0
- MockK 1.13.10

**Policy:**
- Patch-level updates automatic
- Update as needed
- Minimal testing required
- As-needed review

**Example:**
```toml
[versions]
timber = "5.0.1"  # Will auto-update to 5.0.x
```

## Dependency Update Process

### Quarterly Security Updates

**Schedule:** First week of each quarter (Jan, Apr, Jul, Oct)

**Process:**
1. Run dependency check:
   ```bash
   ./gradlew dependencyUpdates
   ```

2. Review security advisories:
   ```bash
   ./gradlew dependencyCheckAnalyze
   ```

3. Update dependencies in order:
   - Tier 3 (utilities)
   - Tier 2 (features)
   - Tier 1 (critical) - only if necessary

4. Test each tier:
   ```bash
   ./gradlew clean build testDebugUnitTest
   ```

5. Create PR with updates

### Emergency Security Updates

**Trigger:** CVE/security advisory for any dependency

**Process:**
1. Assess severity (CVSS score)
2. If CVSS >= 7.0:
   - Update immediately
   - Test critical paths
   - Deploy hotfix
3. If CVSS < 7.0:
   - Schedule for next quarterly update
   - Monitor for exploits

## Breaking Change Protocol

When a dependency update introduces breaking changes:

### 1. Assessment Phase
- Identify breaking changes in changelog
- Estimate impact on codebase
- Determine if update is necessary

### 2. Planning Phase
- Create migration plan
- Allocate developer time
- Schedule for appropriate release

### 3. Implementation Phase
- Create feature branch
- Implement changes incrementally
- Update tests
- Update documentation

### 4. Testing Phase
- Run full test suite
- Manual regression testing
- Beta testing with users

### 5. Deployment Phase
- Merge to main
- Monitor crash reports
- Be ready to rollback

## Critical Dependencies

### Tier 1: Critical Infrastructure

| Dependency | Version | Purpose | Update Policy |
|------------|---------|---------|---------------|
| Gradle | 9.2.1 | Build system | Annual |
| AGP | 8.5.0 | Android plugin | Semi-annual |
| Kotlin | 2.0.21 | Language | Annual |
| Room | 2.6.1 | Database | As-needed |
| SQLCipher | 4.5.x | Encryption | Security-only |

**Rationale:** These form the foundation. Breaking changes require significant refactoring.

### Tier 2: Feature Dependencies

| Dependency | Version | Purpose | Update Policy |
|------------|---------|---------|---------------|
| Compose BOM | 2024.12.01 | UI framework | Quarterly |
| Hilt | 2.51.1 | DI framework | Quarterly |
| Firebase BOM | 34.9.0 | Analytics/Crash | Monthly |
| Navigation | 2.8.5 | Navigation | Quarterly |
| WorkManager | 2.9.0 | Background tasks | Quarterly |

**Rationale:** These enable features. Updates may add features or fix bugs.

### Tier 3: Utility Dependencies

| Dependency | Version | Purpose | Update Policy |
|------------|---------|---------|---------------|
| Timber | 5.0.1 | Logging | As-needed |
| Coroutines | 1.7.3 | Async | Monthly |
| Coil | 2.7.0 | Image loading | Monthly |
| MockK | 1.13.10 | Testing | As-needed |

**Rationale:** These are stable and updates rarely break compatibility.

## Fallback Plans

### Room Update Breaks

**Scenario:** Room 2.7.0 introduces breaking changes

**Fallback:**
1. Stay on 2.6.1 for current release
2. Test 2.7.0 in feature branch
3. Assess migration effort
4. Schedule migration for next minor version

**Timeline:** 1-2 months evaluation period

### Compose Update Breaks

**Scenario:** Compose BOM 2025.01.00 has breaking API changes

**Fallback:**
1. Stay on 2024.12.01
2. Wait for patch releases (2025.01.01, 2025.01.02)
3. Review community feedback
4. Update when stable

**Timeline:** 2-4 weeks after release

### AGP/Gradle Update Breaks

**Scenario:** AGP 9.0 requires Gradle 10

**Fallback:**
1. Stay on AGP 8.5 / Gradle 9.2
2. Evaluate benefits of AGP 9.0
3. Plan migration if benefits justify effort
4. Test thoroughly before updating

**Timeline:** 3-6 months evaluation

## Dependency Verification

### Security Scanning

Run weekly automated scans:

```bash
# Check for known vulnerabilities
./gradlew dependencyCheckAnalyze

# Review report
open app/build/reports/dependency-check-report.html
```

### License Compliance

Ensure all dependencies have compatible licenses:

```bash
# Generate license report
./gradlew generateLicenseReport

# Review licenses
open app/build/reports/licenses/index.html
```

### Size Impact

Monitor APK size impact of dependencies:

```bash
# Build release APK
./gradlew assembleRelease

# Analyze size
./gradlew analyzeReleaseBundle
```

## Adding New Dependencies

### Approval Process

1. **Justification**
   - Why is this needed?
   - Can we achieve this with existing dependencies?
   - What's the maintenance cost?

2. **Evaluation**
   - License compatibility (Apache 2.0, MIT, BSD)
   - Active maintenance (commits in last 6 months)
   - Community support (>1000 stars on GitHub)
   - Size impact (<1MB addition to APK)

3. **Testing**
   - Add to feature branch
   - Test functionality
   - Verify no conflicts
   - Check APK size

4. **Approval**
   - Code review required
   - Security review for Tier 1/2
   - Document in CHANGELOG.md

### Example: Adding a New Library

```kotlin
// 1. Add to gradle/libs.versions.toml
[versions]
new-library = "1.0.0"

[libraries]
new-library = { group = "com.example", name = "library", version.ref = "new-library" }

// 2. Add to app/build.gradle.kts
dependencies {
    implementation(libs.new.library)
}

// 3. Sync and verify
./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep new-library
```

## Removing Dependencies

### Deprecation Process

1. **Mark as deprecated**
   ```kotlin
   @Deprecated("Use NewLibrary instead", ReplaceWith("NewLibrary"))
   ```

2. **Remove usages**
   - Find all usages
   - Migrate to alternative
   - Update documentation

3. **Remove dependency**
   - Remove from libs.versions.toml
   - Remove from build.gradle.kts
   - Verify build succeeds

4. **Monitor**
   - Check for side effects
   - Verify APK size reduction

## Known Issues and Workarounds

### Issue: JavaPoet Conflict (KSP + Hilt)

**Problem:** KSP 2.0.21-1.0.25 conflicts with Hilt 2.51.1

**Workaround:**
```toml
ksp = "2.0.21-1.0.26"  # Use this specific version
```

**Status:** Resolved in KSP 1.0.26

### Issue: R8 Shrinking with SQLCipher

**Problem:** R8 removes SQLCipher native libraries

**Workaround:**
```
-keep class net.zetetic.** { *; }
-keepclassmembers class * extends net.sqlcipher.** { *; }
```

**Status:** Permanent workaround (ProGuard rules)

### Issue: Compose + Navigation Type Safety

**Problem:** @Serializable routes require kotlinx-serialization

**Solution:**
```toml
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

**Status:** Required for type-safe navigation

## Quarterly Update Schedule

### Q1 (January-March)
- Security updates
- Kotlin minor update
- Compose BOM update
- Dependencies audit

### Q2 (April-June)
- AGP evaluation
- Room assessment
- Firebase updates
- License compliance review

### Q3 (July-September)
- Gradle evaluation
- Navigation updates
- Testing library updates
- Performance optimization

### Q4 (October-December)
- Year-end security audit
- Prepare for next year
- Deprecate old dependencies
- Plan major updates

## Monitoring and Alerts

### Automated Checks

GitHub Dependabot configured to:
- Check for security vulnerabilities daily
- Create PRs for security updates
- Flag outdated dependencies weekly

### Manual Reviews

Monthly team review:
- Review Dependabot PRs
- Check for new major versions
- Assess community feedback
- Plan updates

## Emergency Response

### Critical Vulnerability (CVSS >= 9.0)

**Response Time:** Within 24 hours

**Actions:**
1. Assess if Bizap is affected
2. Update dependency immediately
3. Test critical paths
4. Deploy hotfix
5. Notify users if needed

### High Vulnerability (CVSS 7.0-8.9)

**Response Time:** Within 1 week

**Actions:**
1. Evaluate impact
2. Schedule update
3. Test thoroughly
4. Deploy in next minor release

### Medium/Low (CVSS < 7.0)

**Response Time:** Next quarterly update

**Actions:**
1. Track in backlog
2. Include in quarterly update
3. Standard testing

## Tools and Resources

### Dependency Analysis
```bash
# List all dependencies
./gradlew :app:dependencies

# Check for updates
./gradlew dependencyUpdates

# Security scan
./gradlew dependencyCheckAnalyze
```

### APK Analysis
```bash
# Analyze APK size
./gradlew analyzeReleaseBundle

# Compare APKs
diffuse diff old.apk new.apk
```

### External Tools
- [Gradle Versions Plugin](https://github.com/ben-manes/gradle-versions-plugin)
- [OWASP Dependency-Check](https://owasp.org/www-project-dependency-check/)
- [Dependabot](https://github.com/dependabot)
- [Snyk](https://snyk.io/)

## Summary

✅ **Current State:**
- 67 direct dependencies
- 3-tier pinning strategy
- Quarterly update schedule
- Automated security scanning

✅ **Key Principles:**
- Stability over novelty
- Security first
- Gradual updates
- Thorough testing

✅ **Next Review:** Q1 2025 (January 2025)

**Last Updated:** 2024-12-21
