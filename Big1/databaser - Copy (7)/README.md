# Databaser (refactor)

This repository is a refactored fork of the original `databaser` app. The main goal is to provide a fast, secure, and modern Android app using Jetpack Compose and an encrypted Room database (SQLCipher) with a keystore-wrapped passphrase.

Quick setup (local)

Requirements
- JDK 17
- Android SDK with platforms matching `compileSdk` in `app/build.gradle.kts` (currently 36)
- Android Studio or command-line Android SDK tools

Commands

# Set JAVA_HOME for current PowerShell session (example)
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-17'
$env:PATH = $env:JAVA_HOME + '\\bin;' + $env:PATH

# Build unit tests and run lint
./gradlew testDebugUnitTest lintDebug --no-daemon --stacktrace

Notes
- The app now uses a keystore-wrapped 256-bit passphrase for SQLCipher. On first run it will generate a random passphrase, wrap it with an RSA key in AndroidKeyStore, and store the ciphertext in EncryptedSharedPreferences.
- A legacy migration helper attempts to rekey old databases encrypted with the hardcoded passphrase `test`. The helper creates a timestamped backup in `files/db_backups/` before attempting migration.
- Hilt is set up for DI. Some initialization still happens asynchronously at app startup to avoid blocking the main thread.

