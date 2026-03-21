# Bizap — Mobile Invoicing App for Small Businesses

**Version:** 1.0  
**Status:** 🟢 Stable & Production-Ready  
**Last Updated:** March 20, 2026

---

## What is Bizap?

Bizap is a **feature-rich invoicing application** for Android, designed for small business owners to:
- Create, manage, and track invoices
- Manage customer profiles
- Record payments and track outstanding balances
- Generate PDF reports and export data
- Securely encrypt sensitive financial data
- Switch between legacy (GUI1) and modern (GUI2) interfaces

**Target Users:** Freelancers, contractors, small business owners  
**Platform:** Android 8.0+ (minSdk 26)  
**Languages:** Kotlin + Jetpack Compose

---

## Quick Start (5 Minutes)

### Prerequisites
- Android Studio (latest)
- JDK 17+
- Git

### Setup
```bash
# Clone the repository
git clone https://github.com/EmuBiz/Bizap.git
cd Bizap

# Build and run tests (includes unit + integration tests)
./gradlew build

# Open in Android Studio
# File → Open → Select the Bizap folder
# Tools → Android → Sync Now

# Run on emulator or device
./gradlew installDebug
```

### First Run
1. Launch Bizap on your device/emulator
2. Complete PIN setup (security)
3. Choose your interface: **Get Started** (modern GUI2) or **Classic Experience** (legacy GUI1)
4. Create your first invoice

---

## Architecture at a Glance

### Three-Layer Design
```
┌─────────────────────────────────┐
│ UI: Jetpack Compose + Activities│  ← User sees
├─────────────────────────────────┤
│ Domain: Business Logic & Rules  │  ← App thinks
├─────────────────────────────────┤
│ Data: Room Database (Encrypted) │  ← App remembers
└─────────────────────────────────┘
```

### Dual GUI Strategy
Bizap offers **two user interfaces** to support different user preferences:

| Feature | GUI1 (Classic) | GUI2 (Modern) |
|---------|---|---|
| **Technology** | Traditional Activities | Jetpack Compose |
| **Theme** | Material Design 2 | Material Design 3 |
| **Navigation** | Activity switching | Nav graph |
| **Status** | Mature (being phased out) | Current focus |
| **Timeline** | Deprecated June 2027 | Primary interface |

**User Choice:** On startup, users select their preferred interface. Can switch anytime via Settings.

### Data Encryption
- **SQLCipher:** Transparent database encryption (AES-256-GCM)
- **Key Storage:** Android Keystore (hardware-backed when available)
- **Compliance:** GDPR & PCI-DSS ready

---

## Project Structure

```
Bizap/
├── app/                          # Android app module
│   ├── src/main/
│   │   ├── kotlin/
│   │   │   ├── ui/               # UI screens (Compose + Activities)
│   │   │   │   ├── gui2/         # Modern GUI (Compose)
│   │   │   │   ├── dashboard/    # Dashboard screens
│   │   │   │   ├── invoice/      # Invoice management
│   │   │   │   ├── settings/     # Settings screens
│   │   │   │   └── landing/      # GUI selection
│   │   │   ├── domain/           # Business logic
│   │   │   │   ├── model/        # Data models
│   │   │   │   ├── repository/   # Repository interfaces
│   │   │   │   └── usecase/      # Use cases
│   │   │   ├── data/             # Data layer
│   │   │   │   ├── local/        # Room database
│   │   │   │   ├── repository/   # Repository implementations
│   │   │   │   └── dao/          # Database access objects
│   │   │   └── di/               # Dependency injection (Hilt)
│   │   └── res/                  # Android resources
│   └── src/test/                 # Unit tests (1,000+)
├── data/                         # Optional: shared data module
├── domain/                       # Optional: shared domain module
├── docs/                         # Documentation
│   ├── STATUS_ARCHIVE_INDEX.md   # Historical docs catalog
│   ├── RELEASE_SIGNING.md        # Signing guide (WIP)
│   ├── SECURITY.md               # Security policy (WIP)
│   ├── BUILD_GUIDE.md            # Build instructions (WIP)
│   ├── TROUBLESHOOTING.md        # Common issues (WIP)
│   ├── DEVELOPER_PATTERNS.md     # Code patterns (WIP)
│   └── archive/                  # Historical phase docs
├── STATUS.md                     # 👈 Current project status (start here)
├── README.md                     # You are here
├── build.gradle.kts              # Root build config
├── settings.gradle.kts           # Module setup
└── gradlew                       # Gradle wrapper (Linux/Mac)
```

---

## Key Concepts

### Navigation Adapters (Phase 3.3)
To avoid duplicating routes for GUI1 and GUI2, Bizap uses **adapters**:

```kotlin
// Unified navigation model (both GUIs understand this)
AppScreen.Invoice(invoiceId = 42)

// GUI1 uses adapter to convert to its route
Gui1NavAdapter.toScreen(AppScreen.Invoice(42)) → Screen.InvoiceDetail(42)

// GUI2 uses adapter to convert to its route
Gui2NavAdapter.toScreen(AppScreen.Invoice(42)) → ScreenV2.InvoiceDetail(businessId=1, invoiceId=42)
```

This keeps the core app logic GUI-agnostic.

### State Management Pattern
All ViewModels use a consistent sealed class pattern:

```kotlin
sealed class InvoiceUiState {
    object Loading : InvoiceUiState()
    data class Success(val invoice: Invoice) : InvoiceUiState()
    data class Error(val message: String) : InvoiceUiState()
}

val uiState: StateFlow<InvoiceUiState> = repository.observe()
    .map { InvoiceUiState.Success(it) }
    .catch { InvoiceUiState.Error(it.message ?: "Unknown") }
    .stateIn(viewModelScope, SharingStarted.Eagerly, InvoiceUiState.Loading)
```

### Dependency Injection (Hilt)
All major classes are provided by Hilt DI:

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository  // Injected
) : ViewModel() { ... }

@Singleton
@Provides
fun provideMyRepository(dao: MyDAO): MyRepository = MyRepositoryImpl(dao)
```

---

## Common Developer Tasks

### Add a New Screen
See `/docs/DEVELOPER_PATTERNS.md` for the complete checklist. Quick version:

1. **Define the route** → Add to `AppScreen.kt`
2. **Map for GUI1** → Add to `Gui1NavAdapter.kt`
3. **Map for GUI2** → Add to `Gui2NavAdapter.kt`
4. **Create ViewModel** → Extend `ViewModel`, use `StateFlow<UiState>`
5. **Register in nav graphs** → Add `composable<>` to both navigation files
6. **Test** → Add integration test for route + switching

**Time:** ~15 minutes (once pattern is understood)

### Debug a Navigation Crash
1. Check `/docs/TROUBLESHOOTING.md` (navigation section)
2. Verify route is registered in `GuiV2NavGraph` (if GUI2)
3. Check adapter returns non-null for your AppScreen
4. Look at Logcat for "Navigation" errors
5. See `/docs/NAVIGATION_GUIDE.md` for detailed walkthrough

### Run Tests
```bash
# Unit tests only (fast, ~2 sec)
./gradlew test

# Integration tests only (slower, ~15 sec)
./gradlew connectedAndroidTest

# All tests
./gradlew build  # includes both above

# Specific test file
./gradlew test -k "InvoiceRepositoryImplEnhancedTest"
```

### Build Release APK
```bash
# Set signing credentials
export KEYSTORE_PATH=/path/to/release-key.jks
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=your_alias
export KEY_PASSWORD=your_key_password

# Build signed APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk

# Verify signature
jarsigner -verify app/build/outputs/apk/release/app-release.apk
```

See `/docs/RELEASE_SIGNING.md` for detailed signing guide.

---

## Testing

### Test Suite Overview
- **1,081+ unit tests** — Fast, isolated logic verification
- **40+ integration tests** — Critical flow verification
- **80+ manual test cases** — Device/emulator acceptance

### Running Locally
```bash
./gradlew test                    # Unit tests only
./gradlew connectedAndroidTest   # Integration tests (needs device/emulator)
./gradlew build                  # Both
```

### Test Organization
- `src/test/java/` — Unit tests (mocked)
- `src/androidTest/java/` — Integration tests (real device/emulator)
- `src/test/resources/` — Test data (JSON, fixtures)

---

## Known Issues & Workarounds

| Issue | Status | Workaround |
|-------|--------|-----------|
| Resource shrinking crashes | 🟡 Known, accepted | Disabled for MVP; APK slightly larger |
| Dual GUI maintenance | 🟡 By design | Adapter pattern + 12-mo sunset timeline |
| Signing credentials in dev fallback | 🟡 In progress | Using env vars in production; local fallback for dev |
| Navigation test coverage gaps | 🟡 Being addressed | Adding integration tests (Week 2 current sprint) |

See `/STATUS.md` for full details.

---

## Deployment

### App Store Submission Checklist
- [ ] Verify release build generates valid APK
- [ ] Sign with production keystore (not dev keystore)
- [ ] Test on real device (not just emulator)
- [ ] Generate changelog
- [ ] Create store listing screenshots
- [ ] Submit to Google Play Console

### CI/CD (GitHub Actions)
GitHub Actions signing integration is coming (Week 1–2 current sprint).

See `/docs/RELEASE_SIGNING.md` (WIP) for setup details.

---

## Documentation Map

| Document | Purpose | Audience |
|----------|---------|----------|
| **README.md** (you are here) | Project overview | Everyone |
| **STATUS.md** | Current status & blockers | Project leads, developers |
| **docs/DEVELOPER_PATTERNS.md** | Code patterns & templates | Developers |
| **docs/TROUBLESHOOTING.md** | Common issues & fixes | Developers |
| **docs/BUILD_GUIDE.md** | Build & release workflow | DevOps, release managers |
| **docs/RELEASE_SIGNING.md** | Signing & deployment | DevOps, security team |
| **docs/SECURITY.md** | Credential policy & encryption | Security, compliance |
| **docs/ARCHITECTURE.md** | Deep architecture dive | Architects, senior devs |
| **docs/STATUS_ARCHIVE_INDEX.md** | Historical document catalog | Reference only |

---

## Support & Questions

### Getting Help
1. Check `/docs/TROUBLESHOOTING.md` (most common issues covered)
2. Search existing [GitHub Issues](https://github.com/EmuBiz/Bizap/issues)
3. Check `/STATUS.md` for current known issues
4. Ask in team chat (link to your Slack/Discord channel)

### Reporting Bugs
1. Create a [GitHub Issue](https://github.com/EmuBiz/Bizap/issues/new)
2. Include: device, Android version, steps to reproduce, logs
3. Label with area (`ui`, `data`, `navigation`, etc.)

### Contributing
- Fork the repo
- Create a feature branch: `git checkout -b feature/your-feature`
- Follow patterns in `/docs/DEVELOPER_PATTERNS.md`
- Submit PR with description

---

## License & Attribution

**License:** [Insert your license here, e.g., MIT, GPL, etc.]  
**Copyright:** © 2026 EmuBiz  
**Maintained By:** EmuBiz Development Team

---

## Quick Links

- 📊 [STATUS.md](STATUS.md) — Current project status
- 🐛 [GitHub Issues](https://github.com/EmuBiz/Bizap/issues)
- 📚 [Docs Folder](docs/) — All documentation
- 🔒 [SECURITY.md](docs/SECURITY.md) — Security policy (WIP)
- 🏗️ [ARCHITECTURE.md](docs/ARCHITECTURE.md) — Architecture deep-dive (WIP)
- 🛠️ [DEVELOPER_PATTERNS.md](docs/DEVELOPER_PATTERNS.md) — Code patterns (WIP)

---

## Changelog

**v1.0 (March 20, 2026)** — Production Release
- GUI2 feature parity with GUI1
- SQLCipher database encryption
- Navigation adapter consolidation
- 1,081+ passing unit tests
- Root directory cleanup & documentation overhaul

**v0.9 (January 2026)** — Beta Release
- GUI2 modern interface launched
- Navigation unification (AppScreen adapters)
- Payment tracking enhancements

**v0.1–0.8 (2025)** — Alpha/Development
- Initial GUI1 architecture
- Core invoicing features
- Database setup & DAOs

---

## 📚 Documentation

### Canonical Sources (Read These First)
- **[STATUS.md](STATUS.md)** — Current project health score and active initiatives
- **[DECISION_LOG.md](DECISION_LOG.md)** — Architectural decisions (GUI1 sunset, etc.)
- **[docs/](docs/)** — Technical guides and implementation documentation

### Historical Documentation
- **[docs/ARCHIVE_INDEX.md](docs/ARCHIVE_INDEX.md)** — Guide to archived documentation (100+ files organized by category)

All historical status reports, phase documents, and build diagnostics are archived in `docs/archive/` for reference but should not be considered current project state.

---

## 🚀 Contributing

### Getting Started
1. Read [STATUS.md](STATUS.md) for current project state
2. Review [DECISION_LOG.md](DECISION_LOG.md) for architectural context
3. Check existing PRs for current work
4. Pick a task from the roadmap

### Code Standards
- **Tests:** Run `./gradlew test` before submitting PR
- **Build:** Verify `./gradlew clean build` passes
- **Format:** Follow Kotlin style guide (auto-formatted by IDE)

### Before Committing
```bash
# Run full verification
./gradlew clean build test

# Expected: BUILD SUCCESSFUL with all tests passing
```

---

## Version History

