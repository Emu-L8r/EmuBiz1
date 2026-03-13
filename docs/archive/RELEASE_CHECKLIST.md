# Release Checklist

Use this checklist before publishing a new release of Bizap.

> **Note:** This checklist covers v0.1.0 MVP. Features marked ❌ are NOT part of v0.1.0 and
> are planned for v0.2.0–v1.0.0. See `Bizap/docs/FEATURE_ROADMAP_v0.2_to_v1.0.md`.

---

## What's Included in v0.1.0 ✅

- Invoice creation, editing, PDF generation
- Customer management
- Multi-currency support
- Business profile management with switcher
- Revenue dashboard (MTD, YTD, weekly, total paid)
- Analytics snapshots with optimistic locking
- Invoice status lifecycle with validated transitions
- Room database v28 with full migration chain
- 279 unit tests passing
- ProGuard/R8 minification enabled

## What's NOT Included in v0.1.0 ❌

These features are planned for future versions:

- User authentication / login (v1.0.0)
- Data encryption at rest / SQLCipher (v0.3.0)
- Cloud sync / backup (v0.3.0+)
- Push notifications (v0.3.0)
- Payment gateway integration (v1.0.0)
- Multi-user / team support (v1.0.0)
- Audit logging (v1.0.0)
- Tablet layout optimization (v0.2.0)
- Full accessibility / TalkBack compliance (v0.2.0)

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
  git tag -a v0.1.0 -m "Release v0.1.0 MVP"
  git push origin v0.1.0
  ```
- [ ] Build signed release APK / AAB
- [ ] Create GitHub Release with release notes
- [ ] Upload APK / AAB as release artifact

---

## Post-Release

- [ ] Verify crash-free rate in Firebase Crashlytics
- [ ] Monitor analytics for unexpected behavior
- [ ] Update project board / milestone to reflect shipped items
- [ ] Begin v0.2.0 planning based on user feedback

