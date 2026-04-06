# Known Issues & Workarounds

## Open Issues

### None at this time
All critical issues have been resolved in this release.

## Fixed in This Release
- DAO injection in ViewModels (now uses repositories)
- Mock data in revenue analytics (now uses real repository)
- Android imports in domain PDF layer (moved to data/service/pdf)
- Stale test files that prevented build from passing

## Accepted Tradeoffs
1. **Resource shrinking:** Enabled in release builds (APK ~2-3 MB larger in debug)
2. **SQLCipher overhead:** ~5% performance cost for AES-256 encryption (worth it for security)
3. **Dual-mode UI:** Slight code duplication in layout definitions (worth it for UX flexibility)
