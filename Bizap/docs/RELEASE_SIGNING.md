# Release Signing Guide — Bizap v1.0

**Last Updated:** March 20, 2026  
**Status:** ✅ Complete (with security best practices)

---

## Overview

This guide explains how to sign Bizap release builds securely. Production builds require signing credentials via environment variables. Development builds can use a local keystore.

### Key Principle
**Never commit signing credentials to git.** Always use environment variables for production.

The release build includes:
- **R8/ProGuard code minification** (shrinks unused code ~30-40%)
- **Code optimization** via ProGuard
- **Release signing** (required for Play Store)

---

## Table of Contents

1. [Generate Release Keystore (One-time)](#generate-release-keystore-one-time)
2. [Local Development Setup](#local-development-setup)
3. [Production Release Setup](#production-release-setup)
4. [GitHub Actions Integration](#github-actions-integration)
5. [Building Release APK](#building-release-apk)
6. [Verifying Signatures](#verifying-signatures)
7. [Troubleshooting](#troubleshooting)
8. [Security Best Practices](#security-best-practices)

---

## Generate Release Keystore (One-time)

> Only do this once. Store the keystore file securely offline.

### Step 1: Create Keystore File

```bash
keytool -genkey -v \
  -keystore bizap-release.keystore \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias bizap-key \
  -dname "CN=Bizap Release,O=EmuBiz,C=US"
```

When prompted:
- **Keystore password:** (enter something strong, remember it!)
- **Key password:** (can be same as keystore password)

### Step 2: Store Keystore Securely

```bash
# Move to secure location (NOT in git repo)
mv bizap-release.keystore ~/Secure/bizap-release.keystore

# Set restrictive permissions (Unix/Mac only)
chmod 600 ~/Secure/bizap-release.keystore
```

### Step 3: Verify Keystore

```bash
keytool -list -v -keystore ~/Secure/bizap-release.keystore
```

---

## Local Development Setup

For developers building release APK locally (for testing before CI/CD).

### Option A: Create Dev Keystore (Recommended)

```bash
# Generate a dev keystore (different from production)
keytool -genkey -v \
  -keystore bizap-dev-release.keystore \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias bizap-dev-key \
  -storepass bizap123 \
  -keypass bizap123 \
  -dname "CN=Bizap Dev,O=EmuBiz,C=US"

# Move to project root (already in .gitignore)
cp bizap-dev-release.keystore ../release-key.jks
```

### Build Release APK

```bash
# Dev keystore already set up
./gradlew assembleRelease
```

---

## Production Release Setup

For release manager or CI/CD pipeline.

### Step 1: Set Environment Variables

**Linux/Mac:**
```bash
export KEYSTORE_PATH="$HOME/Secure/bizap-release.keystore"
export KEYSTORE_PASSWORD="your_strong_password"
export KEY_ALIAS="bizap-key"
export KEY_PASSWORD="your_key_password"

# Verify set
echo $KEYSTORE_PATH
```

**Windows PowerShell:**
```powershell
$env:KEYSTORE_PATH="C:\Users\[YourUsername]\AppData\Local\Secure\bizap-release.keystore"
$env:KEYSTORE_PASSWORD="your_strong_password"
$env:KEY_ALIAS="bizap-key"
$env:KEY_PASSWORD="your_key_password"

# Verify set
echo $env:KEYSTORE_PATH
```

### Step 2: Build Release APK

```bash
./gradlew clean assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

---

## GitHub Actions Integration

### Step 1: Generate Base64 Keystore

```bash
# Convert keystore to Base64
base64 ~/Secure/bizap-release.keystore > keystore-base64.txt

# Copy contents
cat keystore-base64.txt
```

### Step 2: Create GitHub Secrets

1. Go to GitHub: Repository → Settings → Secrets and variables → Actions
2. Create new secrets:
   - `KEYSTORE_BASE64`: (paste base64 content)
   - `KEYSTORE_PASSWORD`: (your keystore password)
   - `KEY_ALIAS`: `bizap-key`
   - `KEY_PASSWORD`: (your key password)

### Step 3: Create GitHub Actions Workflow

Create `.github/workflows/release-signing.yml`:

```yaml
name: Build & Sign Release APK

on:
  push:
    branches: [main]
    tags: ['v*']

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Decode Keystore
        run: echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 -d > keystore.jks
      
      - name: Build & Sign Release APK
        env:
          KEYSTORE_PATH: ${{ github.workspace }}/keystore.jks
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
        run: ./gradlew assembleRelease
      
      - name: Upload Signed APK
        uses: actions/upload-artifact@v3
        with:
          name: app-release.apk
          path: app/build/outputs/apk/release/app-release.apk
```

---

## Building Release APK

```bash
# Method 1: Dev local build
./gradlew assembleRelease

# Method 2: Production with env vars
export KEYSTORE_PATH=~/Secure/bizap-release.keystore
export KEYSTORE_PASSWORD="password"
export KEY_ALIAS=bizap-key
export KEY_PASSWORD="password"
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

---

## Verifying Signatures

```bash
# Verify signature is valid
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk

# Output: jar verified.
```

---

## Troubleshooting

### "Keystore file not found"
```bash
# Set environment variables
echo $KEYSTORE_PATH

# Or create dev keystore
keytool -genkey -v -keystore ../release-key.jks ...
```

### "Jar NOT verified"
```bash
# Clean and rebuild
rm -rf app/build
./gradlew clean assembleRelease

# Verify again
jarsigner -verify app/build/outputs/apk/release/app-release.apk
```

---

## Security Best Practices

1. **Never commit keystore to git** — already in `.gitignore`
2. **Use strong passwords** — 16+ characters, mixed case + numbers
3. **Restrict permissions** — `chmod 600 keystore.jks`
4. **Use environment variables** — Never hardcode passwords
5. **Rotate passwords quarterly** — Security best practice
6. **Backup offline** — Store keystore in secure vault
7. **Log key activities** — Document who has access

---

## Release Build Details

### Minification
ProGuard rules in `app/proguard-rules.pro` ensure:
- ✅ Room entities/DAOs kept (reflection)
- ✅ Hilt DI graph kept (code-generated)
- ✅ Retrofit interfaces kept
- ✅ Kotlinx.Serialization classes kept
- ✅ Timber logging removed (debug only)
- ✅ Firebase Crashlytics kept

### File Size
- **Debug APK:** ~23.8 MB (unminified)
- **Release APK:** ~12-15 MB (minified)

## References

- [Android App Signing Overview](https://developer.android.com/training/articles/app-signing)
- [Google Play Console Help: App Signing](https://support.google.com/googleplay/android-developer/answer/7384423)
- [ProGuard Manual](https://www.guardsquare.com/manual/configuration/usage)

