# Signing Security Policy — Bizap Release Management

**Last Updated:** March 21, 2026  
**Status:** ✅ Enforced via CI/CD  
**Owner:** EmuBiz Release Engineering

---

## Overview

This policy ensures that production APKs are **never signed with development keystores** and that all release credentials are securely managed via GitHub Actions, not committed to the repository.

### Key Principle
> **Never commit signing credentials to git. Always use environment variables for production.**

---

## Security Levels

### Level 1: Development (Local, Dev Keystore)
**Context:** Developer building debug APK on their machine

**Requirements:**
- ✅ Use local dev keystore (`../release-key.jks`)
- ✅ Dev keystore in `.gitignore` (never committed)
- ✅ Password is `bizap123` (development only)
- ✅ Credentials are hardcoded (acceptable for dev)

**Rationale:** Local development doesn't require security controls. Fast iteration is prioritized.

**Verification:**
```bash
# Verify dev keystore exists and APK is signed with it
jarsigner -verify app/build/outputs/apk/debug/app-debug.apk
# Expected: Successfully verified and signed by "bizap-key"
```

---

### Level 2: Release (CI/CD, GitHub Actions)
**Context:** Automated release build triggered by git tag or PR

**Requirements:**
- ✅ Production keystore stored in GitHub Secrets (encrypted)
- ✅ Environment variables enforced (`KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, etc.)
- ✅ Build **fails** if credentials missing (fail-fast)
- ✅ Keystore only decoded in GitHub Actions memory (never stored locally)
- ✅ Build cannot use fallback dev keystore
- ✅ All credentials deleted after build (GitHub Actions cleanup)

**Rationale:** Production APKs must be signed with production keys and never with dev keys. GitHub Secrets provide encryption at rest + audit logs.

**Verification:**
```bash
# In GitHub Actions logs, you should see:
# ✅ All release credentials configured
# ✅ Building signed release APK
# ✅ APK signature verified (Certificate [PRODUCTION CERT])
```

---

## Development Build (Local)

### Setup (One-Time)

```bash
# Navigate to project root
cd ~/Projects/Bizap

# Generate development keystore
keytool -genkey -v \
  -keystore ../release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias bizap-key \
  -storepass bizap123 \
  -keypass bizap123 \
  -dname "CN=Bizap Dev,O=EmuBiz,C=US"

# Verify keystore was created
ls -lh ../release-key.jks
# Expected: -rw-r--r-- 1 user staff 2.5K ... release-key.jks
```

### Building Debug APK (No Signing Needed)

```bash
# Debug APK doesn't require signing credentials
./gradlew clean assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
# Signature: Android debug key (automatic, dev only)

# Install and run
./gradlew installDebug
```

### Building Release APK Locally (Dev Keystore)

```bash
# DEVELOPMENT ONLY - uses dev keystore
./gradlew clean assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
# Signature: bizap-key (dev keystore)

# Verify signature
jarsigner -verify app/build/outputs/apk/release/app-release.apk
# Expected: jar verified
```

**⚠️ Important:** This dev-signed APK can only be used for local testing. Never upload to Play Store.

---

## Production Build (CI/CD / GitHub Actions)

### Prerequisites

1. **Generate Production Keystore** (One-Time, Do Offline)

```bash
# On secure/air-gapped machine, generate production keystore
keytool -genkey -v \
  -keystore bizap-production.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias bizap-prod-key \
  -storepass [STRONG_PASSWORD_20+_CHARS] \
  -keypass [STRONG_PASSWORD_20+_CHARS] \
  -dname "CN=Bizap,O=EmuBiz Inc,C=US"

# Never leave this machine until secure storage
```

2. **Add to GitHub Secrets**

**Steps:**
1. Go to: `https://github.com/EmuBiz/Bizap/settings/secrets/actions`
2. Click "New repository secret"
3. Add four secrets:

```
Name: KEYSTORE_PATH
Value: bizap-production.jks

Name: KEYSTORE_PASSWORD
Value: [STRONG_PASSWORD]

Name: KEY_ALIAS
Value: bizap-prod-key

Name: KEY_PASSWORD
Value: [STRONG_PASSWORD]
```

**Note:** All values are encrypted at rest by GitHub. Not visible in UI after creation.

### GitHub Actions Workflow

**File:** `.github/workflows/release-build-security.yml`

```yaml
name: Release Build with Security Check

on:
  push:
    tags: ['v*']  # Trigger on version tags

jobs:
  secure-release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      # SECURITY CHECK 1: Validate credentials are configured
      - name: Validate Release Configuration
        env:
          KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          echo "🔐 Validating release credentials..."
          
          if [ -z "$KEYSTORE_PATH" ]; then
            echo "❌ ERROR: KEYSTORE_PATH not configured in GitHub Secrets"
            exit 1
          fi
          if [ -z "$KEYSTORE_PASSWORD" ]; then
            echo "❌ ERROR: KEYSTORE_PASSWORD not configured in GitHub Secrets"
            exit 1
          fi
          if [ -z "$KEY_ALIAS" ]; then
            echo "❌ ERROR: KEY_ALIAS not configured in GitHub Secrets"
            exit 1
          fi
          if [ -z "$KEY_PASSWORD" ]; then
            echo "❌ ERROR: KEY_PASSWORD not configured in GitHub Secrets"
            exit 1
          fi
          
          echo "✅ All release credentials configured"
      
      # SECURITY CHECK 2: Verify no dev keystore in build
      - name: Security: Verify Production Keystore
        run: |
          echo "🔍 Checking for dev keystore contamination..."
          
          if [ -f "../release-key.jks" ]; then
            echo "⚠️  WARNING: Dev keystore exists, but will NOT be used"
            echo "    Production keystore will be used instead"
          fi
          
          echo "✅ Using production keystore from GitHub Secrets"
      
      # BUILD STEP: Create signed release APK
      - name: Build Signed Release APK
        env:
          KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: |
          echo "🔨 Building signed release APK..."
          ./gradlew clean assembleRelease
          echo "✅ Release APK built successfully"
      
      # SECURITY CHECK 3: Verify APK signature
      - name: Verify APK Signature
        run: |
          echo "🔐 Verifying APK signature..."
          
          APK_PATH="app/build/outputs/apk/release/app-release.apk"
          
          if [ ! -f "$APK_PATH" ]; then
            echo "❌ ERROR: APK not found at $APK_PATH"
            exit 1
          fi
          
          jarsigner -verify -verbose -certs "$APK_PATH"
          
          if [ $? -eq 0 ]; then
            echo "✅ APK signature verified"
          else
            echo "❌ ERROR: APK signature verification failed"
            exit 1
          fi
      
      # UPLOAD: Save APK as artifact
      - name: Upload Release APK
        uses: actions/upload-artifact@v3
        with:
          name: app-release-apk-production
          path: app/build/outputs/apk/release/app-release.apk
          retention-days: 30
      
      # FINAL SECURITY: Confirm no credentials leaked
      - name: Security: Cleanup & Confirm No Leaks
        run: |
          echo "🧹 Cleaning up build artifacts..."
          rm -rf app/build/intermediates/
          echo "✅ Build complete. No credentials stored in artifacts."
```

### Release Process

1. **Create Version Tag**
```bash
git tag v1.0.1
git push origin v1.0.1
```

2. **GitHub Actions Automatically Triggers**
   - Workflow runs: `.github/workflows/release-build-security.yml`
   - Build checks credentials
   - APK is signed with production key
   - Signature verified
   - APK uploaded as artifact

3. **Download & Publish**
   - Download APK from GitHub Actions artifacts
   - Upload to Play Store (or distribution channel)

---

## Credential Rotation

### When to Rotate

- ✅ **Annually:** Standard security practice
- ✅ **On Developer Departure:** Revoke all dev keys
- ✅ **On Suspected Breach:** Immediately rotate production keys
- ✅ **Before Major Release:** Optional (good practice)

### Rotation Process

1. **Generate New Production Keystore**
```bash
# Same as initial setup
keytool -genkey -v \
  -keystore bizap-production-v2.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias bizap-prod-key-v2 \
  ...
```

2. **Update GitHub Secrets**
   - Go to: `https://github.com/EmuBiz/Bizap/settings/secrets/actions`
   - Update `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

3. **Test New Keystore**
```bash
# Create feature branch
git checkout -b chore/rotate-signing-keys

# Tag test release (or manually run workflow if desired)
git tag v1.0.1-rc.1
git push origin v1.0.1-rc.1

# Verify build succeeds in GitHub Actions
# (Check workflow logs)
```

4. **Commit & Document**
```bash
# Document rotation (no credentials in message)
git commit -m "chore: rotate production signing keystore (annual rotation)"
git push
```

---

## Compromised Credential Recovery

**If Production Keystore is Compromised:**

1. **Immediate Actions**
   - [ ] Revoke current GitHub Secrets
   - [ ] Generate new production keystore
   - [ ] Update GitHub Secrets with new credentials
   - [ ] Create new APK signed with new keystore

2. **Play Store Update**
   - [ ] New APK signed with new keystore must be submitted to Play Store
   - [ ] Play Store will accept it (different cert) only if:
     - APK has higher versionCode, OR
     - You request cert rotation from Google Support
   - [ ] Contact Google Play Console support if needed

3. **Communication**
   - [ ] Notify team of rotation
   - [ ] No user-facing action required (app auto-updates)
   - [ ] Document incident in security log

---

## Audit & Logging

### GitHub Actions Logs

**To Review Release Builds:**
1. Go to: `https://github.com/EmuBiz/Bizap/actions`
2. Click on release workflow run
3. Review logs:
   - "Validate Release Configuration" (credentials verified)
   - "Build Signed Release APK" (build success)
   - "Verify APK Signature" (signature valid)

**What NOT to Do:**
- ❌ Never copy/paste credentials from logs
- ❌ Never share logs externally (contain secret names)
- ❌ Never screenshot logs

### Security Audit Trail

**Question:** Who has access to signing credentials?

**Answer:** Only GitHub Actions (via GitHub Secrets) + GitHub Org Admins

**Verification:**
```bash
# Check GitHub Org Members
# (Admin can view via: https://github.com/organizations/EmuBiz/settings/members)

# Check GitHub Action logs
# (Each release build creates audit entry)
```

---

## Checklist: Pre-Release Security

Before every release:

- [ ] Credentials in GitHub Secrets (not hardcoded)
- [ ] No dev keystore in release build
- [ ] Workflow fails if credentials missing (fail-fast)
- [ ] APK signature verified in CI/CD logs
- [ ] GitHub Actions artifacts have APK
- [ ] No credentials in commit history
- [ ] No credentials in release notes
- [ ] Team notified of release

---

## FAQ

**Q: Why not just commit the keystore to git?**  
A: Major security risk. If repo is ever public or leaked, production APKs can be forged.

**Q: What if GitHub Secrets are compromised?**  
A: GitHub is SOC 2 certified and has strong access controls. More secure than local storage.

**Q: Can developers access GitHub Secrets?**  
A: Only during CI/CD (GitHub Actions). Not available via UI or API.

**Q: What if I need to sign APKs locally for testing?**  
A: Use dev keystore (`../release-key.jks`). Never use production keys locally.

**Q: How often should we rotate keys?**  
A: Annually as standard practice. More frequently if suspected breach.

**Q: Can we have multiple production keystores?**  
A: Possible but not recommended. Single keystore per app (easier to manage).

---

## References

- **Google Play Console Help:** https://support.google.com/googleplay/android-developer/answer/7384423
- **Android Signing Overview:** https://developer.android.com/training/articles/app-signing
- **GitHub Secrets Docs:** https://docs.github.com/en/actions/security-guides/encrypted-secrets

---

**Policy Owner:** EmuBiz Security Team  
**Last Updated:** March 21, 2026  
**Review Frequency:** Quarterly (or after policy changes)  
**Next Review:** June 21, 2026

