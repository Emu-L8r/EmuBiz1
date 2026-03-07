# Security Roadmap

**Project:** Bizap  
**Document Version:** 1.0  
**Created:** March 7, 2026  
**Status:** Active

---

## Current State: v0.1.0 (MVP Security)

### What's Protected ✅
- No API keys or secrets committed to the repository
- ProGuard/R8 minification enabled in release builds (code obfuscation)
- `allowBackup="false"` in AndroidManifest — prevents ADB backup extraction
- Data extraction rules configured
- No `Timber.d` debug logs in release (release build type disables debug logging)
- Room database on internal app storage (not accessible to other apps without root)

### Known Gaps ⚠️
- No user authentication — any device holder can access all data
- SQLite database not encrypted at rest (accessible on rooted devices)
- No input sanitization beyond basic field length limits
- No certificate pinning (no network calls in v0.1.0, so N/A for now)
- No audit logging of sensitive operations

---

## v0.2.0: Input Validation & Error Boundaries (Q2 2026)

**Priority:** High  
**Risk Addressed:** Injection attacks, data corruption from malformed inputs

### Tasks
- [ ] Implement input sanitization on all user-facing form fields
  - Invoice amounts: numeric validation, max value check
  - Customer names/emails: character whitelist + length limits
  - ABN validation: Australian Business Number format check (11 digits, checksum)
  - Currency codes: validate against known ISO 4217 list
- [ ] Add error boundaries (catch + show error UI, no crashes exposed)
- [ ] Validate invoice line items: quantity > 0, unit price ≥ 0
- [ ] Prevent SQL injection (Room parameterized queries already used — verify all raw queries)
- [ ] Review all `@Query` annotations for raw string concatenation (none expected)

### Acceptance Criteria
- All form inputs validated before database write
- No runtime exceptions visible to the user
- OWASP Mobile Top 10 self-assessment: address M7 (client code quality)

---

## v0.3.0: Data Encryption & API Security (Q3 2026)

**Priority:** High  
**Risk Addressed:** Data breach on stolen/rooted devices, insecure data exposure

### SQLCipher Integration
- [ ] Add SQLCipher dependency to `app/build.gradle.kts`
- [ ] Migrate `AppDatabase` to use `SupportFactory` with SQLCipher
- [ ] Generate encryption key using Android Keystore (not stored in code)
- [ ] Write migration guide for existing users (first launch migration from unencrypted DB)
- [ ] Test migration path on existing v0.1.0 databases

### Keystore & Key Management
- [ ] Use `AndroidKeyStore` for all cryptographic keys
- [ ] Keys tied to device authentication (biometric or PIN)
- [ ] Key rotation policy documented

### Secure Storage
- [ ] Move any sensitive preferences to `EncryptedSharedPreferences`
- [ ] Ensure no sensitive data cached in external storage directories

### If APIs Are Introduced
- [ ] HTTPS only — no HTTP fallback
- [ ] Certificate pinning for all API endpoints
- [ ] API keys stored in Keystore, not in code or `local.properties`
- [ ] Token refresh and expiry handled securely

### Acceptance Criteria
- SQLite database encrypted with AES-256
- Encryption key not recoverable without device PIN/biometric
- Security audit performed (manual or automated tool like MobSF)

---

## v1.0.0: Authentication, RBAC & Audit Logging (Q4 2026)

**Priority:** Critical for enterprise  
**Risk Addressed:** Unauthorized access, compliance requirements, data integrity

### User Authentication
- [ ] On-device: PIN or biometric (fingerprint/face) using BiometricPrompt
- [ ] Optional cloud account: email + password with bcrypt hashing
- [ ] Session management: automatic lock after configurable idle timeout
- [ ] Account recovery flow (if cloud account enabled)

### Role-Based Access Control (RBAC)
- [ ] Define permission model:
  - `ADMIN`: full access including business profile and user management
  - `ACCOUNTANT`: invoice and customer read/write, no business profile edit
  - `VIEWER`: read-only access to invoices and customers
- [ ] Enforce permissions at ViewModel layer (before DB writes)
- [ ] UI adapts to role (hide/disable restricted actions)

### Audit Logging
- [ ] Immutable audit log table in Room (append-only, no UPDATE/DELETE)
- [ ] Log entries: timestamp, user, action type, entity ID, before/after values
- [ ] Hash chain linking log entries to detect tampering
- [ ] Export audit log as signed PDF or JSON
- [ ] Log retention policy: configurable (default 2 years)

### Compliance Considerations
- [ ] GDPR: user data export and deletion on request
- [ ] Australian Privacy Act: data handling policy documented
- [ ] Data residency: document where data is stored (local-only in v1.0)

### Acceptance Criteria
- Authentication required on app launch
- All RBAC permissions enforced
- Audit log captures all state-changing operations
- No critical findings in security audit

---

## Security Tooling

| Tool | Purpose | When to Run |
|---|---|---|
| Android Lint | Static analysis | Every PR |
| Detekt | Kotlin static analysis | Every PR |
| MobSF | Mobile app security framework | Before each milestone release |
| OWASP Dependency Check | Dependency vulnerability scan | Monthly |
| Manual pen test | Attack simulation | Before v1.0.0 GA |
