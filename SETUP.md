# Setting Up Bizap — Local Development Guide

This guide walks you through getting Bizap running on your local machine for development and testing.

---

## System Requirements

| Requirement | Minimum Version |
|-------------|----------------|
| JDK | 17 (OpenJDK or Temurin recommended) |
| Android Studio | Hedgehog (2023.1.1) or newer |
| Android SDK | API 35 (compile), API 26 (minimum) |
| Kotlin | 1.9+ (bundled with Android Studio) |
| Gradle | 8.x (wrapper included) |
| Git | 2.30+ |

---

## Step-by-Step Setup

### 1. Clone the Repository

```bash
git clone https://github.com/Emu-L8r/EmuBiz1.git
cd EmuBiz1
```

### 2. Open in Android Studio

1. Launch Android Studio.
2. Select **File → Open** and navigate to the `EmuBiz1/Bizap` directory.
3. Wait for Gradle sync to complete (this may take a few minutes on first run).

### 3. Configure API Keys

Bizap uses an external exchange rate API. You must configure your API key before building.

1. Open (or create) `~/.gradle/gradle.properties` (global Gradle properties):

   ```properties
   EXCHANGE_RATE_API_KEY=your_api_key_here
   ```

   > **Tip**: Get a free API key at [exchangerate-api.com](https://www.exchangerate-api.com/) or [openexchangerates.org](https://openexchangerates.org/).

2. Alternatively, add it to `Bizap/local.properties` (never commit this file):

   ```properties
   EXCHANGE_RATE_API_KEY=your_api_key_here
   ```

The app will build and run without an API key, but exchange rate features will not work.

---

## Firebase Setup (Optional for Development)

Bizap uses Firebase Analytics and Crashlytics for production monitoring.

### For local development (Firebase is optional):

The app runs without Firebase configuration in debug builds. However, if you want Firebase enabled:

1. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com/).
2. Add an Android app with the package name `com.emul8r.bizap`.
3. Download `google-services.json` and place it at:
   ```
   Bizap/app/google-services.json
   ```
4. Enable **Crashlytics** and **Analytics** in the Firebase console.

> **Note**: The `google-services.json` file is gitignored and must never be committed to the repository.

---

## Building and Running Locally

### Build Debug APK

```bash
cd Bizap
./gradlew :app:assembleDebug
```

The output APK will be at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Build Release APK

```bash
./gradlew :app:assembleRelease
```

### Run on a Device or Emulator

From Android Studio, select your target device in the toolbar and press **Run** (▶️), or from the command line:

```bash
./gradlew :app:installDebug
```

---

## Database Setup and Migrations

Bizap uses Room for local data persistence. The database is created automatically on first launch.

### Schema Files

Room schema JSON files are exported to:
```
Bizap/app/schemas/
```

These are version-controlled and used to verify migrations. Do not manually edit them.

### Running Migrations

Migrations run automatically when you update the database version. When adding a new migration:

1. Create a new file in `app/src/main/java/com/emul8r/bizap/data/local/migrations/`:
   ```
   Migration_XX_YY.kt
   ```
2. Register the migration in `DatabaseModule.kt`.
3. Increment the database version in `AppDatabase.kt`.

---

## Running Tests Locally

### Unit Tests

```bash
cd Bizap
./gradlew testDebugUnitTest
```

Results are available at:
```
app/build/reports/tests/testDebugUnitTest/index.html
```

### Lint

```bash
./gradlew lint
```

Results:
```
app/build/reports/lint-results-debug.html
```

### Instrumented / E2E Tests

Instrumented tests require a connected device or running emulator:

```bash
./gradlew :app:connectedAndroidTest
```

---

## Common Troubleshooting

### Gradle sync fails

- Ensure you are using JDK 17. Check with `java -version`.
- Try **File → Invalidate Caches → Invalidate and Restart** in Android Studio.
- Delete `Bizap/.gradle` and `Bizap/build` directories, then re-sync.

### "google-services.json not found"

- This is expected if you have not set up Firebase.
- The app still builds and runs without it in debug mode.

### Exchange rate API not working

- Verify your `EXCHANGE_RATE_API_KEY` is set in `~/.gradle/gradle.properties`.
- Check network connectivity and API quota limits.

### Room database errors

- If you see schema mismatch errors, ensure you have created and registered a migration file for the new database version.
- For development only, you can enable `fallbackToDestructiveMigration()` in `DatabaseModule.kt` (never in production).

### Build fails with "KSP" errors

- Clean the project: `./gradlew clean`
- Ensure your Android Studio and Kotlin plugin versions match the versions in `build.gradle.kts`.

---

## Project Structure

```
EmuBiz1/
├── Bizap/                          ← Android project root
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/               ← Production code
│   │   │   ├── test/               ← Unit tests
│   │   │   └── androidTest/        ← Instrumented / E2E tests
│   │   ├── schemas/                ← Room schema exports
│   │   └── build.gradle.kts
│   └── build.gradle.kts
├── CONTRIBUTING.md
├── SETUP.md                        ← This file
├── CODE_OF_CONDUCT.md
└── README.md
```
