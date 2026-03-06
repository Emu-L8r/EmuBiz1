# Release Checklist

Use this checklist before publishing a new release of Bizap.

---

## Code Quality

- [ ] All unit tests passing
- [ ] Test coverage ≥ 40%
- [ ] No compiler warnings introduced
- [ ] No Lint errors (warnings acceptable)
- [ ] Code review completed and approved

---

## Security

- [ ] No API keys or secrets committed to the repository
- [ ] ProGuard/R8 minification enabled in release build
- [ ] `allowBackup=false` set in `AndroidManifest.xml`
- [ ] Data extraction rules configured
- [ ] No `Timber.d` / debug logging leaking sensitive data in release build

---

## Performance

- [ ] Build time < 5 minutes
- [ ] Release APK size < 50 MB
- [ ] Database indexes added for frequently queried columns
- [ ] No obvious memory leaks (checked with Android Profiler)

---

## Database

- [ ] Migration file created for new database version
- [ ] Migration registered in `DatabaseModule.kt`
- [ ] Database version incremented in `AppDatabase.kt`
- [ ] Schema JSON exported and committed to `app/schemas/`
- [ ] Existing data preserved (no destructive migration in release)

---

## Documentation

- [ ] `README.md` updated with any new features
- [ ] `CONTRIBUTING.md` accurate and up to date
- [ ] `SETUP.md` reflects current requirements
- [ ] `CODE_OF_CONDUCT.md` present
- [ ] Inline code comments added for complex logic
- [ ] CHANGELOG updated with release notes

---

## Before Publishing the Release

- [ ] Increment `versionCode` and `versionName` in `app/build.gradle.kts`
- [ ] Tag the release commit:
  ```bash
  git tag -a v0.1.0-beta -m "Beta release v0.1.0"
  git push origin v0.1.0-beta
  ```
- [ ] Build signed release APK / AAB
- [ ] Create GitHub Release with release notes
- [ ] Upload APK / AAB as release artifact

---

## Post-Release

- [ ] Verify crash-free rate in Firebase Crashlytics
- [ ] Monitor analytics for unexpected behavior
- [ ] Update project board / milestone to reflect shipped items
