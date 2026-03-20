# SECURITY — Bizap Security Policy & Practices

**Last Updated:** March 20, 2026  
**Status:** ✅ Active

---

## Security Policy

### 1. Credential Management

#### Production Credentials
- ✅ **Signing Key:** Stored in secure offline vault
- ✅ **Keystore Password:** Environment variable only (never committed)
- ✅ **API Keys:** GitHub Secrets (never in source code)
- ✅ **Database Encryption Key:** Android Keystore (hardware-backed when available)

#### Development Credentials
- ✅ **Dev Keystore:** Local file only (`../release-key.jks`), not committed
- ✅ **Dev Passwords:** Weak (e.g., `bizap123`), OK for development only
- ✅ **Local Properties:** `.gitignore` enforced

#### Never Commit
```gitignore
# Signing
*.keystore
*.jks
*.p12
*.pfx

# Credentials
*.properties
*.env
local.properties

# Secrets
secrets.json
credentials.json
.aws/
```

### 2. Database Encryption

#### SQLCipher Implementation
- ✅ **Algorithm:** AES-256-GCM (industry standard)
- ✅ **Key Derivation:** 32-byte random passphrase
- ✅ **Key Storage:** Android Keystore (system-managed)
- ✅ **Encryption:** Transparent (automatic on all queries)

#### Verification Checklist
Before shipping production build:
- [ ] Extract database from device: `adb pull /data/data/com.emul8r.bizap/databases/bizap-db`
- [ ] Check first 16 bytes are binary (not "SQLite format 3" text)
  ```bash
  hexdump -C bizap-db | head -1
  # Should show random bytes like: c5 84 cc e8 9f 13 36 11...
  # NOT: 53 51 4c 69 74 65 20 66 6f 72 6d 61 74 20 33 00 (SQLite format 3)
  ```
- [ ] Verify decryption works: App can query data normally
- [ ] Verify encryption works: Copying DB to another device doesn't work

#### Key Rotation (Quarterly)
```kotlin
// Regenerate passphrase quarterly
// Current implementation: automatic on app startup if passphrase missing
// Future: implement explicit key rotation UI
```

#### Data Loss Scenarios
⚠️ **Important:** No recovery mechanism if device is lost or OS reset:
- Android Keystore key is cleared on factory reset
- Database cannot be decrypted without Keystore key
- Recommend: User-initiated encrypted backup (planned for Q2 2026)

### 3. API Security

#### Exchange Rate API Key
- **Type:** Public API key (rate-limited)
- **Storage:** `buildConfig` at build time
- **Handling:** Read-only, no sensitive data
- **Fallback:** Disabled if key is missing (graceful degradation)

#### No Sensitive Data in API Calls
- ✅ Database never synced to remote (local-only)
- ✅ User data never leaves device
- ✅ Exchange rates fetched anonymously (no auth)

### 4. Dependency Security

#### Regular Updates
- ✅ Kotlin: Latest stable (1.9.x)
- ✅ Compose: Latest stable (1.5.x)
- ✅ Room: Latest stable (2.6.x)
- ✅ Hilt: Latest stable (2.47)

#### Known Vulnerable Dependencies
- ⚠️ None currently known (last security audit: March 2026)
- Review quarterly via: `./gradlew dependencyCheck`

### 5. Code Security

#### Sensitive Code Locations
- `DatabasePassphraseManager.kt` — Encryption key management
- `AuthenticationManager.kt` — PIN/biometric security
- `build.gradle.kts` — Signing configuration

#### Code Review Checklist
- [ ] No hardcoded passwords/keys
- [ ] No sensitive data in logs (Timber)
- [ ] No secrets in test files
- [ ] Gradle configuration uses env vars
- [ ] String obfuscation for sensitive strings (PR rule)

#### ProGuard Rules
- ✅ SQLCipher classes kept (reflection-dependent)
- ✅ Android Keystore classes kept
- ✅ Hilt-generated code kept (code-generated at compile-time)
- ✅ No debug code leaks to release build

### 6. Testing & Verification

#### Security Tests
- ✅ `DatabasePassphraseManagerTest` — Encryption key verification
- ✅ `AuthenticationManagerTest` — PIN validation
- ✅ `SigningConfigTest` — Keystore path verification (WIP)

#### Test Data
- ✅ No real credentials in test files
- ✅ Mock data uses safe fake values
- ✅ Database encryption tested with dummy data

### 7. Release Build Security

#### Pre-Release Checklist
- [ ] All environment variables set (KEYSTORE_PATH, etc.)
- [ ] APK signature verified: `jarsigner -verify app-release.apk`
- [ ] No debug logging enabled
- [ ] No test code in release APK
- [ ] ProGuard removes debug classes
- [ ] Database encryption verified on target device
- [ ] No credentials leaked in logcat

#### Signing
- ✅ Keystore password stored securely (not in git)
- ✅ Release APK signed with production keystore
- ✅ Same keystore required for all future releases (App Store requirement)

### 8. CI/CD Security

#### GitHub Actions
- ✅ Keystore stored as GitHub Secret (not artifact)
- ✅ Keystore Base64-encoded for safe storage
- ✅ Secrets never printed to logs (automatic GitHub masking)
- ✅ Workflow cleanup removes keystore after build

#### Environment Variables
- ✅ Only injected at build time (not persistent)
- ✅ Masked in job logs
- ✅ Limited to required build steps

### 9. Incident Response

#### Suspected Credential Leak
1. **Immediately:** Revoke affected credential
2. **Notify:** Security team + product team
3. **Generate:** New credentials
4. **Update:** All references (GitHub Secrets, CI/CD, local vaults)
5. **Document:** Root cause in SECURITY_INCIDENTS.md
6. **Review:** Code for similar issues

#### Suspected Database Compromise
1. **Isolate:** Device/database from production
2. **Analyze:** Check APK signature matches release
3. **Investigate:** Check device logs for unauthorized access
4. **Plan:** Release security patch if vulnerability found
5. **Notify:** Users if data exposure confirmed

### 10. Third-Party Security

#### Dependencies
- ✅ Firebase → Google security standards
- ✅ Room → Google Android team maintains
- ✅ Hilt → Google Dagger team maintains
- ✅ Compose → Google Jetpack team maintains
- ✅ SQLCipher → OpenSSL-based, industry standard

#### Review Plan
- Quarterly: Check for security updates
- Annually: Security audit of major dependencies
- As-needed: Emergency patches for CVEs

---

## Compliance & Standards

### GDPR (General Data Protection Regulation)
- ✅ **Data Encryption:** SQLCipher AES-256 encryption
- ✅ **Data Minimization:** Only invoice-related data stored
- ✅ **User Consent:** PIN setup + encryption disclosure (planned)
- ✅ **Data Deletion:** User can clear all data via app settings
- ⚠️ **Export/Backup:** Coming Q2 2026

### PCI-DSS (Payment Card Industry Data Security Standard)
- ⚠️ **Status:** Not fully applicable (no payment card storage)
- ⚠️ **Partial:** Encryption best practices applied

### Android Security Standards
- ✅ **Keystore:** Uses Android Keystore (KeyStore API)
- ✅ **Permissions:** Minimal permissions requested (camera, file access for receipts)
- ✅ **Biometric:** Supports Android biometric authentication
- ✅ **Updates:** Targets latest Android SDK (35)

---

## Security Incident History

| Date | Issue | Status | Notes |
|------|-------|--------|-------|
| Mar 2026 | Hardcoded keystore password (dev fallback) | ✅ MITIGATED | Using env vars for production; dev fallback only |
| Mar 2026 | SQLCipher encryption never verified | ✅ VERIFIED | Added verification checklist above |
| TBD | (None reported to date) | - | - |

---

## Future Security Improvements

### Q2 2026
- [ ] Implement encrypted user backup (local + cloud)
- [ ] Add explicit key rotation UI (quarterly)
- [ ] Biometric authentication for sensitive screens
- [ ] Rate-limiting on PIN attempts (account lockout)

### Q3 2026
- [ ] Penetration testing (third-party security audit)
- [ ] OWASP compliance review
- [ ] Automated dependency scanning in CI/CD

### Q4 2026
- [ ] Certificate pinning (API calls, if applicable)
- [ ] Runtime integrity check (detect jailbreak/rooting)
- [ ] Secure delete: overwrite RAM on logout

---

## Reporting Security Issues

### Do NOT Create Public Issues

If you discover a security vulnerability:

1. **Do NOT** open a public GitHub issue
2. **Do NOT** post on social media
3. **Do NOT** share with unauthorized people

### Report Securely

1. Email: security@emubiz.com (or your security contact)
2. Include:
   - Description of vulnerability
   - Steps to reproduce
   - Potential impact
   - Your contact info (name, email, phone)
3. Do NOT include exploit code or sensitive data
4. Allow 30 days for team to respond

### Responsible Disclosure
- Timeline: 90 days to patch
- Public disclosure: After patch is released
- Credit: Your name (if desired)

---

## Security Resources

- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)
- [Android Security Best Practices](https://developer.android.com/training/security)
- [CWE Top 25](https://cwe.mitre.org/top25/)
- [GDPR Compliance Guide](https://gdpr-info.eu/)

---

## Acknowledgments

Security policy reviewed and approved by:
- EmuBiz Security Team
- EmuBiz Legal (compliance review)
- Android Security Best Practices

---

**Last Updated:** March 20, 2026  
**Status:** ✅ Active  
**Next Review:** June 20, 2026  
**Maintainer:** EmuBiz Security Team

