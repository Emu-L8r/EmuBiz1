# Build Health Checklist

Before every commit:
- [ ] `./gradlew clean build` succeeds
- [ ] `./gradlew testDebugUnitTest` all pass
- [ ] No new compiler warnings
- [ ] No @Ignore decorators on tests
- [ ] No commented-out code blocks
- [ ] No TODO/FIXME without context
- [ ] Logging is appropriate (not excessive)

Before pushing to GitHub:
- [ ] All architecture rules followed
- [ ] Tests updated when code changes
- [ ] No direct DAO injection in ViewModels
- [ ] No Android imports in domain layer
- [ ] KDoc added for public APIs
