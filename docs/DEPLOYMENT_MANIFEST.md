# Deployment Manifest — Bizap v1.0.0

**Purpose:** Step-by-step guide for App Store submission of Bizap v1.0.0  
**Last Updated:** March 2026  
**Target Store:** Google Play Store (primary) + Apple App Store (if applicable)

---

## Prerequisites Checklist

Before starting the submission process:

```bash
# 1. Verify all tests pass
cd Bizap && ./gradlew :app:testDebugUnitTest
# Expected: BUILD SUCCESSFUL, 936 tests passing

# 2. Verify clean build
./gradlew clean :app:assembleRelease
# Expected: BUILD SUCCESSFUL

# 3. Verify no lint errors
./gradlew :app:lintRelease
# Expected: No errors (warnings acceptable)
```

---

## Step 1: Update Version Numbers

Edit `Bizap/app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        versionCode = 1          // Increment by 1 for each submission
        versionName = "1.0.0"   // Semantic version for display
    }
}
```

Commit the version bump:
```bash
git add Bizap/app/build.gradle.kts
git commit -m "chore: bump version to 1.0.0 (versionCode 1)"
git tag -a v1.0.0 -m "Release v1.0.0"
```

---

## Step 2: Generate Signed Release APK/AAB

### 2.1 Generate a Release Keystore (One-Time Setup)

If you don't have a keystore yet:
```bash
keytool -genkey -v \
  -keystore bizap-release.keystore \
  -alias bizap-key \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

**⚠️ Store the keystore file and passwords securely. Loss of keystore means you cannot update the app on the Play Store.**

### 2.2 Configure Signing in `build.gradle.kts`

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "bizap-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS") ?: "bizap-key"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

### 2.3 Build Signed AAB (Recommended for Play Store)

```bash
# Set environment variables (or use local.properties — never commit passwords)
export KEYSTORE_PATH=/path/to/bizap-release.keystore
export KEYSTORE_PASSWORD=your_keystore_password
export KEY_ALIAS=bizap-key
export KEY_PASSWORD=your_key_password

# Build Android App Bundle
cd Bizap
./gradlew :app:bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
ls -lh app/build/outputs/bundle/release/app-release.aab
```

### 2.4 Build Signed APK (Alternative)

```bash
./gradlew :app:assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
ls -lh app/build/outputs/apk/release/app-release.apk
# Expected size: < 50 MB
```

---

## Step 3: Pre-Submission Device Testing

Test the signed APK/AAB on at least 3 real devices before submission:

```bash
# Install signed APK on connected device
adb install -r app/build/outputs/apk/release/app-release.apk

# Or install on all connected devices
adb devices | grep -v "List" | cut -f1 | xargs -I{} adb -s {} install -r \
  app/build/outputs/apk/release/app-release.apk
```

Complete the `MANUAL_QA_CHECKLIST.md` on each device before proceeding.

---

## Step 4: Google Play Store Submission

### 4.1 Access Play Console
1. Go to [Google Play Console](https://play.google.com/console)
2. Sign in with the developer account
3. Navigate to "All Apps" → "Create app" (first time) or select existing Bizap listing

### 4.2 Upload the Build
1. Navigate to "Release" → "Production" → "Create new release"
2. Upload `app-release.aab`
3. Add release notes (copy from `docs/RELEASE_NOTES.md`)

### 4.3 App Store Listing Requirements

**Required assets:**
- App icon: 512 × 512 px PNG (no alpha)
- Feature graphic: 1024 × 500 px JPEG or PNG
- Screenshots: Min 2, max 8 per device type
  - Phone: 320 × 480 to 3840 × 2160 px
  - 7-inch tablet: 1080 × 1920 px recommended
  - 10-inch tablet: 1920 × 1080 px recommended

**Store listing text:**
- Title: "Bizap - Business Invoicing" (max 30 chars)
- Short description: Max 80 characters
- Full description: Max 4000 characters (use content from `docs/RELEASE_NOTES.md`)

### 4.4 Content Rating
Complete the content rating questionnaire in "Policy" → "App content":
- This app does not contain violence, sexual content, or gambling
- Category: Business / Productivity
- Expected rating: Everyone / PEGI 3

### 4.5 Pricing & Distribution
- Free app (confirm or set pricing)
- Available countries: Select all or target markets
- Confirm app complies with Google Play policies

### 4.6 Submit for Review
1. Complete all required sections (green checkmarks)
2. Click "Review release" → "Start rollout to Production"
3. Choose rollout percentage (recommended: start with 20% for monitoring)

---

## Step 5: Post-Submission Monitoring

### Immediate (0-24 hours)
- Monitor Play Console for review status
- Google Play review typically takes 1-3 business days for first submission
- Check email for any policy violations or requests for more information

### First 48 Hours After Launch
```bash
# Monitor crash rate in Play Console
# Navigate to: Android Vitals → Crashes & ANRs

# Check for user reviews (1-star reviews often indicate UX issues)
# Navigate to: Reviews → Rating & reviews
```

Target metrics for v1.0.0:
- Crash rate: < 1%
- ANR rate: < 0.5%
- Rating target: ≥ 4.0 stars within first week

Refer to `docs/KNOWN_ISSUES_AND_MONITORING.md` for full monitoring guide.

---

## Step 6: Apple App Store (If Applicable)

*Note: Bizap is an Android app built with Kotlin/Jetpack Compose. App Store submission requires an iOS build. See iOS roadmap documentation if applicable.*

If a React Native or cross-platform version exists:
1. Build iOS release in Xcode (Archive → Distribute App)
2. Upload via Xcode or Transporter
3. Submit via App Store Connect for TestFlight → App Store review

---

## Rollback Procedure

If critical issues are discovered post-launch:

```bash
# 1. Halt rollout (in Play Console: halt rollout immediately)
# 2. If already at 100%, publish hotfix:
git checkout -b hotfix/1.0.1
# Make fix
git commit -m "fix: <description>"
git tag -a v1.0.1 -m "Hotfix v1.0.1"
# Build and submit new release

# 3. Communicate with users via in-app update notification
```

---

## Deployment Contacts

| Role | Responsibility |
|------|---------------|
| Release Manager | Coordinates submission timing, approves rollout |
| QA Lead | Signs off on MANUAL_QA_CHECKLIST.md |
| Android Developer | Builds and signs the release APK/AAB |
| Store Manager | Manages Play Console listing and reviews |

---

## Checklist Summary

- [ ] Version bumped (`versionCode = 1`, `versionName = "1.0.0"`)
- [ ] All 936 tests passing
- [ ] Signed AAB/APK built successfully
- [ ] APK size < 50 MB
- [ ] Tested on 3+ physical devices (see MANUAL_QA_CHECKLIST.md)
- [ ] Store listing text prepared
- [ ] Screenshots captured
- [ ] Content rating questionnaire completed
- [ ] Privacy policy URL available
- [ ] Release notes added
- [ ] Submitted to Play Store
- [ ] Monitoring alerts set up
