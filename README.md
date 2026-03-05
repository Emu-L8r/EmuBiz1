# 📱 Bizap - Invoice Management & Business App

**A modern, feature-rich Android application for managing invoices, customers, and business operations.**

---

## 🎯 Overview

Bizap is a professional-grade invoice management system built with **Kotlin**, **Jetpack Compose**, and **Android Architecture Components**. It provides a complete solution for small to medium-sized businesses to create, manage, and track invoices.

### Key Features
- ✅ **Invoice Management** - Create, edit, delete, and archive invoices
- ✅ **Customer Management** - Store and manage customer information
- ✅ **Currency Support** - Real-time exchange rates and multi-currency support
- ✅ **Local Database** - Room database for offline functionality
- ✅ **Modern UI** - Jetpack Compose for beautiful, responsive interfaces
- ✅ **Validation System** - Comprehensive domain validation to prevent data errors
- ✅ **Testing** - 60+ unit tests with MockK framework
- ✅ **Crash Reporting** - Firebase Crashlytics integration

---

## 🏗️ Architecture

```
Bizap/
├── app/
│   ├── src/main/java/com/emul8r/bizap/
│   │   ├── data/                    ← Database & Repository layer
│   │   │   ├── local/
│   │   │   ├── repository/
│   │   │   └── datasource/
│   │   ├── domain/                  ← Domain layer & Business logic
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   ├── usecase/
│   │   │   └── validation/
│   │   ├── ui/                      ← UI layer
│   │   │   ├── screens/
│   │   │   ├── components/
│   │   │   └── viewmodel/
│   │   └── MainActivity.kt          ← App entry point
│   └── src/test/                    ← Unit tests (MockK)
└── build.gradle.kts                 ← Build configuration
```

**Architecture Pattern:** Clean Architecture + MVVM

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (2023.1.0 or later)
- **JDK 17** or higher
- **Android SDK 35** (installed via Android Studio SDK Manager)
- **Git** for version control

### Installation Steps

#### 1. Clone the Repository
```bash
git clone https://github.com/Emu-L8r/EmuBiz1.git
cd EmuBiz1/Bizap
```

#### 2. Install Android SDK
```bash
# Open Android Studio → SDK Manager
# Ensure "Android 15" (API 35) is installed
# Confirm you have Android SDK build-tools installed
```

#### 3. Set Up Environment Variables
```bash
# Create or update local.properties in the Bizap directory
EXCHANGE_RATE_API_KEY=your_api_key_here

# Get a free key from: https://exchangerate-api.com/
# (Free tier: 1500 requests/month)
```

#### 4. Build the Project
```bash
cd Bizap

# Clean build
./gradlew clean build

# Or use Android Studio Build menu: Build → Make Project
```

#### 5. Run Tests
```bash
# Unit tests (60+ tests with MockK)
./gradlew testDebugUnitTest

# Expected output: "BUILD SUCCESSFUL"
```

#### 6. Run on Device/Emulator
```bash
# Build debug APK
./gradlew assembleDebug

# Install on device
adb devices                    # Verify device is connected
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or just press "Run" in Android Studio
```

---

## 📊 Build Configuration

### Gradle Settings
```kotlin
compileSdk = 35                    // Android 15
minSdk = 26                        // Android 8.0+
targetSdk = 35
versionCode = 2
versionName = "1.0"
jvmTarget = "17"                   // Java 17
```

### Key Dependencies

#### Kotlin & Android
- `org.jetbrains.kotlin:kotlin-stdlib` - Kotlin stdlib
- `androidx.appcompat:appcompat` - Android compatibility
- `androidx.core:core-ktx` - Kotlin extensions

#### UI & Compose
- `androidx.compose.ui:ui` - Compose UI framework
- `androidx.compose.material3:material3` - Material Design 3
- `androidx.activity:activity-compose` - Compose integration

#### Data & Persistence
- `androidx.room:room` - Local database
- `androidx.datastore:datastore` - Preferences storage
- `com.google.gson:gson` - JSON parsing
- `com.squareup.retrofit2:retrofit` - REST client

#### Dependency Injection
- `com.google.dagger:hilt` - Dependency injection framework
- `com.google.dagger:hilt-compiler` - Hilt annotation processor

#### Logging & Monitoring
- `com.jakewharton.timber:timber` - Better logging
- `com.google.firebase:firebase-analytics` - Analytics
- `com.google.firebase:firebase-crashlytics` - Crash reporting

#### Testing
- `io.mockk:mockk` - **Modern Kotlin mocking** (recently converted from Mockito)
- `junit:junit` - Unit testing framework
- `androidx.test:core` - Android test utilities

---

## 🧪 Testing

### Run All Tests
```bash
./gradlew testDebugUnitTest
```

### Run Specific Test Class
```bash
# Validation tests
./gradlew testDebugUnitTest -k ValidationRulesTest

# Core unit tests (converted to MockK)
./gradlew testDebugUnitTest --tests "CoreUnitTests"

# Repository tests (converted to MockK)
./gradlew testDebugUnitTest --tests "InvoiceTemplateRepositoryTest"
```

### Test Coverage
- **Total Tests:** 60+ unit tests
- **New Tests (Week 3):** 30+ validation tests
- **Framework:** MockK (modern Kotlin-native mocking)
- **Patterns:** Arrange-Act-Assert (AAA)

---

## 🔧 Troubleshooting

### Build Fails with "compileSdk = 35"
```
Solution: Install Android SDK 35
1. Open Android Studio
2. Go to Tools → SDK Manager
3. Install "Android 15" (API 35)
```

### API Key Not Found
```
Solution: Add exchange rate API key
1. Edit Bizap/local.properties
2. Add: EXCHANGE_RATE_API_KEY=dummykey123
3. Get real key: https://exchangerate-api.com/
```

### Tests Fail with MockK Errors
```
Solution: Verify MockK is installed
1. Check app/build.gradle.kts has MockK dependencies
2. Run: ./gradlew clean build
3. Re-run tests
```

### APK Won't Install
```
Solution: Uninstall previous version first
adb uninstall com.emul8r.bizap
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Gradle Sync Fails
```
Solution: Clean gradle caches
1. Go to: File → Invalidate Caches
2. Choose: Invalidate and Restart
3. Sync again
```

---

## 📖 Documentation

### Week 3 Deliverables
- ✅ **Domain Validation System** - Type-safe error handling with Result pattern
- ✅ **MockK Conversion** - Modernized test infrastructure (Mockito → MockK)
- ✅ **60+ Tests** - Comprehensive unit test coverage
- ✅ **70+ Pages** - Complete documentation and guides

### Key Documents
```
Bizap/
├── WEEK_3_MASTER_INDEX.md         ← Start here for overview
├── WEEK_3_COMPLETION_SUMMARY.md   ← Week 3 achievements
├── QUICK_REFERENCE.md              ← MockK syntax reference
├── VALIDATION_IMPLEMENTATION_SUMMARY.md
├── docs/DOMAIN_VALIDATION_COMPLETE.md
└── README.md                        ← This file
```

---

## 🎯 Quick Command Reference

```bash
# Build
./gradlew clean build                    # Full clean build
./gradlew build                          # Incremental build
./gradlew assembleDebug                  # Build debug APK

# Testing
./gradlew test                           # All unit tests
./gradlew testDebugUnitTest              # Debug tests
./gradlew testDebugUnitTest -k TestName  # Specific test

# Install & Run
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.emul8r.bizap/.MainActivity

# Logging
adb logcat -s BizapApp:D                 # View app logs
adb logcat | grep -i error               # Error logs only

# Clean
./gradlew clean                          # Clean build artifacts
```

---

## 🏪 App Features

### Invoice Management
- Create invoices with line items
- Calculate totals with currency support
- Save invoices to local database
- Archive and delete invoices
- Generate invoice PDFs (planned)

### Customer Management
- Add customer information
- Email validation
- Phone number validation
- Business registration support
- Search and filter customers

### Validation System
- **Invoice Validation** (6 rules)
  - At least one line item
  - Total > $0
  - Valid due date
  - Customer information complete
  - All items valid
  - Valid currency code
  
- **Customer Validation** (6 rules)
  - Name length (2-100 chars)
  - Email format validation
  - Phone number format
  - Business name <= 100 chars
  - Optional field handling
  - Clear error messages

- **LineItem Validation** (5 rules)
  - Non-blank description
  - Quantity > 0
  - Unit price > 0
  - Total < $1M
  - Prevents extreme values

### Technical Features
- Clean Architecture
- MVVM pattern
- Jetpack Compose UI
- Room database
- Hilt dependency injection
- Modern testing (MockK)
- Firebase integration
- Timber logging

---

## 🤝 Contributing

### Code Standards
- Follow Kotlin style guide
- Write tests for new features
- Use domain validation for all inputs
- Add Timber logs for debugging
- Keep UI components composable

### Testing Requirements
- All new features must have tests
- Use MockK for mocking dependencies
- Follow AAA (Arrange-Act-Assert) pattern
- Aim for 80%+ code coverage

### Commit Messages
```
feat: Add new feature description
fix: Bug fix description
docs: Documentation updates
test: Test additions or fixes
chore: Build, gradle, dependencies
refactor: Code improvements without behavior change
```

---

## 📋 Version History

### v1.0 (Current)
- Complete invoice management system
- Customer management
- Domain validation system
- 60+ unit tests (MockK)
- Modern UI with Compose
- Local database with Room
- Firebase integration

### Planned Features
- PDF invoice generation
- Email invoice delivery
- Analytics dashboard
- Invoice templates
- Recurring invoices
- Payment tracking
- Multi-user support

---

## 🔐 Security

- ✅ API keys in local.properties (not in repo)
- ✅ Firebase Crashlytics for error tracking
- ✅ Type-safe validation prevents SQL injection
- ✅ Room database with encryption support
- ✅ Input validation on all screens

---

## 📞 Support

### Issues & Bug Reports
- GitHub Issues: [EmuBiz1/issues](https://github.com/Emu-L8r/EmuBiz1/issues)
- Email: [maintainer email]

### Questions?
- Check existing documentation in `/docs`
- Review test files for usage examples
- Check commit history for recent changes

---

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

---

## 🎉 Getting Help

### Resources
1. **Read the Quick Start** above
2. **Check the docs/** folder for detailed guides
3. **Review test files** for code examples
4. **Check troubleshooting** section above
5. **Open an issue** on GitHub if stuck

### Quick Commands to Get Running
```bash
cd Bizap
./gradlew clean build          # Build the project
./gradlew testDebugUnitTest    # Run tests
./gradlew assembleDebug        # Build APK
```

**You're ready to go!** 🚀

---

**Last Updated:** March 5, 2026  
**Status:** ✅ Ready for Development


