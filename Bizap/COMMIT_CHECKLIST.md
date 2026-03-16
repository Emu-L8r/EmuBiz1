# Pre-Commit Verification Checklist

Before pushing code to GitHub, verify:

## Build Verification
- [ ] Run: `./gradlew clean build`
- [ ] Result: BUILD SUCCESSFUL (0 errors)
- [ ] Result: 0 warnings (or approved warnings only)

## Test Verification
- [ ] Run: `./gradlew testDebugUnitTest`
- [ ] Result: All tests passing
- [ ] Result: No test failures or skips

## Code Quality
- [ ] Run: `./gradlew lint`
- [ ] Result: Review warnings
- [ ] Action: Fix critical issues

## Integration Check
- [ ] Verify all related files modified work together
- [ ] Example: If DAO added, check AppDatabase has method
- [ ] Example: If entity added, check @Database annotation includes it

## Documentation
- [ ] Updated README if needed
- [ ] Updated CONTRIBUTING.md if adding standards
- [ ] Added comments for complex logic

## Git Check
- [ ] Review: `git diff --stat origin/main`
- [ ] Confirm: Only intended files changed
- [ ] Confirm: No accidental file deletions

## Final Checklist
- [ ] All above items complete
- [ ] Ready to commit
- [ ] Commit message is clear and descriptive

**Commit only if all items are checked!**
