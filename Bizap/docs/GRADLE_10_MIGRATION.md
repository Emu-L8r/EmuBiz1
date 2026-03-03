# Gradle 10 Migration Guide

## Pre-Migration Checklist
- [ ] Wait for AGP 9.0 stable release
- [ ] Verify all plugins support Gradle 10
- [ ] Run: `./gradlew :app:assembleDebug --warning-mode all`
- [ ] Create feature branch: `feature/gradle-10-upgrade`

## Step-by-Step Migration
1. Update Gradle Wrapper: `./gradlew wrapper --gradle-version=10.0`
2. Update libs.versions.toml (agp = "9.0.0", kotlin = "2.1.0")
3. Test configuration cache
4. Fix any new deprecations
5. Run full test suite

## Rollback Plan
```bash
git checkout main
git branch -D feature/gradle-10-upgrade
./gradlew wrapper --gradle-version=9.2.1
```
