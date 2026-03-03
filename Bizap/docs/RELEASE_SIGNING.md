# Bizap Release Build & Signing Guide

This guide explains how to configure signing for release builds and build the APK for Google Play Store distribution.

## Overview

The release build includes:
- **R8/ProGuard code minification** (shrinks unused code ~30-40%)
- **Resource shrinking** (removes unused resources)
- **Release signing** (required for Play Store)

## Step 1: Generate a Keystore

A keystore contains your signing credentials. Generate one with:

```bash
keytool -genkey -v -keystore bizap-release.keystore \
  -alias bizap \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -dname "CN=Bizap Release,O=Your Company,C=US"
```

**Explanation:**
- `-keystore bizap-release.keystore` — Creates the keystore file
- `-alias bizap` — Alias name (referenced in gradle config)
- `-keyalg RSA` — Algorithm (standard for Play Store)
- `-keysize 2048` — Key size (2048-bit is required minimum)
- `-validity 10000` — Valid for ~27 years
- `-dname` — Your certificate details (optional; interactive if omitted)

**Store this password securely.** You'll need it to sign every release.

## Step 2: Add Keystore Path to local.properties

The keystore file should be stored locally (NOT in git). Add it to `Bizap/local.properties`:

```properties
sdk.dir=/path/to/Android/Sdk

# Release signing (KEEP PRIVATE - do NOT commit to git)
KEYSTORE_PATH=/full/path/to/bizap-release.keystore
KEYSTORE_PASSWORD=your_keystore_password
KEY_ALIAS=bizap
KEY_PASSWORD=your_key_password
```

**Security note:** `local.properties` is in `.gitignore` and will never be committed.

## Step 3: Configure Signing in build.gradle.kts

Add the signing configuration to `app/build.gradle.kts`:

```kotlin
android {
    // ... existing config ...

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

Or, read from `local.properties`:

```kotlin
val localProperties = Properties()
localProperties.load(File(rootDir, "local.properties").inputStream())

android {
    signingConfigs {
        create("release") {
            storeFile = File(localProperties.getProperty("KEYSTORE_PATH", ""))
            storePassword = localProperties.getProperty("KEYSTORE_PASSWORD")
            keyAlias = localProperties.getProperty("KEY_ALIAS")
            keyPassword = localProperties.getProperty("KEY_PASSWORD")
        }
    }
}
```

## Step 4: Build Release APK

Once signing is configured:

```bash
cd Bizap

# Build unsigned release APK (for testing minification)
./gradlew :app:assembleRelease --no-sign

# Build signed release APK (for Play Store)
./gradlew :app:assembleRelease
```

The signed APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

## Step 5: Verify the Release APK

Check the release APK is signed:

```bash
jarsigner -verify -verbose app/build/outputs/apk/release/app-release.apk
```

Expected output:
```
jar verified. This jar contains entries whose certificate chain is not validated.
smk  257 Tue Mar 03 18:00:00 UTC 2026 META-INF/MANIFEST.MF
smk  168 Tue Mar 03 18:00:00 UTC 2026 META-INF/CERT.SF
smk  0 Tue Mar 03 18:00:00 UTC 2026 META-INF/CERT.RSA
```

## Step 6: Upload to Google Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select your app → Release → Production
3. Upload the signed APK
4. Fill in release notes and review
5. Roll out to production

## Release Build Details

### Minification

ProGuard rules in `app/proguard-rules.pro` ensure:
- ✅ Room entities/DAOs are kept (reflection-dependent)
- ✅ Hilt DI graph is kept (code-generated)
- ✅ Retrofit API interfaces are kept
- ✅ Kotlinx.Serialization classes are kept
- ✅ Timber logging is removed (debug only)
- ✅ Firebase Crashlytics is kept
- ✅ All Bizap app code is kept (no obfuscation)

### File Size

- **Debug APK:** ~23.8 MB (unminified, includes test code)
- **Release APK:** ~12-15 MB (minified + resource shrinking)

### Optimization Flags

```
-optimizationpasses 5     # Multiple optimization iterations
-verbose                  # Log what's being kept/removed
```

## Troubleshooting

### Build fails: "Keystore file not found"
- Verify `KEYSTORE_PATH` is set correctly in `local.properties`
- Use absolute path (e.g., `/home/user/bizap-release.keystore`)

### Build fails: "Wrong password"
- Double-check `KEYSTORE_PASSWORD` and `KEY_PASSWORD`
- Note: These are often THE SAME password

### APK is too large
- Check logcat: `./gradlew clean :app:assembleRelease`
- Look for "unused resources" warnings
- Consider: which dependencies are actually used?

### App crashes after release build
- Check ProGuard kept necessary classes
- Look for "obfuscation" issues in crashes
- ProGuard rules may need adjustment

## Signing Certificate Details

Get information about your signing certificate:

```bash
keytool -list -v -keystore bizap-release.keystore -alias bizap -storepass your_password
```

Output includes:
- Certificate fingerprint (SHA-256)
- Validity dates
- Public key algorithm

**Save the SHA-256 fingerprint** — you'll need it for Google Play App Signing setup.

## One-Time Setup vs. Per-Release

- **One-time:** Generate keystore, configure signing in gradle
- **Per-release:** Just run `./gradlew :app:assembleRelease` and upload

## References

- [Android App Signing Overview](https://developer.android.com/training/articles/app-signing)
- [Google Play Console Help: App Signing](https://support.google.com/googleplay/android-developer/answer/7384423)
- [ProGuard Manual](https://www.guardsquare.com/manual/configuration/usage)

