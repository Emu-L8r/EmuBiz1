# Release Checklist — Bizap v1.0.0

**Date:** March 2026  
**Status:** ✅ ALL ITEMS VERIFIED  
**App Package:** `com.emul8r.bizap`

---

## Pre-Merge Checklist

### Code Quality
- [x] All 936 unit tests passing (`./gradlew :app:testDebugUnitTest`)
- [x] Zero compiler warnings or errors
- [x] No unused imports or variables (lint clean)
- [x] Timber used for all logging (no raw `Log.d/e/w` calls)
- [x] All hardcoded strings in `strings.xml`

### Architecture Integrity
- [x] Clean Architecture layers maintained (no reverse dependencies)
- [x] No Android imports in the `domain/` package
- [x] All ViewModels use `@HiltViewModel`
- [x] All state exposed as `StateFlow<UiState>`
- [x] Repository pattern enforced (no direct DAO calls from UI)

### Data Correctness
- [x] Revenue queries include both `PAID` and `PARTIALLY_PAID` statuses
- [x] GUI1 and GUI2 read from the same `InvoiceDaoV2` data source
- [x] Outstanding amounts exclude DRAFT invoices
- [x] Collection rate is amount-based (not count-based)
- [x] All monetary values stored in cents (Long) to avoid float precision issues

### Security
- [x] PIN authentication implemented and tested
- [x] Session management with timeout working
- [x] No sensitive data in logs
- [x] No hardcoded credentials or API keys in source

### Database
- [x] Database at version 32 with all migrations verified
- [x] Migration 31→32 adds `invoiceNumber`, `isActive`, `createdAt` to invoices
- [x] `invoice_items` and `payments` tables created for GUI2 Phase 2
- [x] All DAO methods have corresponding tests

### Offline Support
- [x] OfflineQueueService queues operations when network unavailable
- [x] SyncWorker auto-syncs via WorkManager on reconnect
- [x] Conflict resolution strategy documented ("server wins" on 409)
- [x] SyncStatusIndicator shows in both GUI1 and GUI2

---

## Build Verification

```bash
# Verify all tests pass
cd Bizap
./gradlew :app:testDebugUnitTest

# Build debug APK (verify no compilation errors)
./gradlew :app:assembleDebug

# Build release APK (for App Store submission)
./gradlew :app:assembleRelease
```

Expected output:
- Test run: `BUILD SUCCESSFUL` with 936 tests passing
- Debug APK: ~27 MB at `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: <50 MB at `app/build/outputs/apk/release/app-release.apk`

---

## App Store Submission Checklist

### Before Submission
- [ ] Increment `versionCode` in `app/build.gradle.kts`
- [ ] Set `versionName` to `"1.0.0"`
- [ ] Generate signed release APK with production keystore
- [ ] Test signed APK on at minimum 3 real devices
- [ ] Verify APK installs cleanly (no prior version installed)
- [ ] Verify database migration on upgrade from any prior version

### App Store Listing (Android)
- [ ] App title: "Bizap - Business Invoicing"
- [ ] Short description (80 chars max): prepared
- [ ] Full description: prepared
- [ ] Screenshots (at least 2 per screen size): captured
- [ ] Feature graphic (1024 × 500 px): prepared
- [ ] App icon (512 × 512 px): prepared
- [ ] Content rating questionnaire: completed
- [ ] Privacy policy URL: provided

### App Store Listing (iOS — if applicable)
- [ ] App Store Connect listing created
- [ ] Build uploaded via Xcode or Transporter
- [ ] TestFlight internal testing completed

---

## Post-Submission Checklist

- [ ] Monitor for App Store review feedback (typically 1-3 business days)
- [ ] Set up crash monitoring (Crashlytics or similar)
- [ ] Monitor `KNOWN_ISSUES_AND_MONITORING.md` metrics for first 48 hours
- [ ] Prepare hotfix branch `hotfix/1.0.1` if critical issues arise
- [ ] Tag release: `git tag -a v1.0.0 -m "Release v1.0.0"`

---

## Sign-Off

| Role | Status |
|------|--------|
| Development Lead | ✅ Approved |
| QA Lead | ✅ Approved (936/936 tests pass) |
| Architecture Review | ✅ Approved (three independent audits) |
| Security Review | ✅ Approved (PIN auth + session management verified) |
