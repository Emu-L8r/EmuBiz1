# EmuBiz1 — BizAP Invoicing App

**Production-ready invoicing platform for small businesses built with Kotlin, Jetpack Compose, and Clean Architecture.**

---

## Quick Start

### Prerequisites
- Android Studio Ladybug (or later)
- JDK 17
- Android SDK 34
- Firebase project (optional — app degrades gracefully without it)

### Build & Run

```bash
# Clone the repository
git clone https://github.com/Emu-L8r/EmuBiz1.git
cd EmuBiz1/Bizap

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Install on connected device
./gradlew installDebug
```

### Environment Variables (optional)

| Variable | Purpose | Default |
|---|---|---|
| `KEYSTORE_PATH` | Release signing keystore path | Dev keystore |
| `KEYSTORE_PASSWORD` | Keystore password | — |
| `KEY_ALIAS` | Key alias | — |
| `KEY_PASSWORD` | Key password | — |
| `EXCHANGE_RATE_API_KEY` | Live exchange-rate API | Shows warning |

---

## Key Features

| Feature | Status | Notes |
|---|---|---|
| Invoice CRUD | ✅ Production | Full create/edit/delete/list |
| PDF Generation | ✅ Production | 7 styles (Canvas + HTML) incl. SASS Professional |
| Payment Tracking | ✅ Production | Single + partial payments |
| Multi-Currency | ✅ Production | 30+ currencies with live rates |
| Tax Integration | ✅ Production | Per-invoice + global defaults |
| Analytics Dashboard | ✅ Production | Revenue, payments, customer metrics |
| Business Profiles | ✅ Production | Multi-business support |
| Customer Management | ✅ Production | Full CRM features |
| PDF Live Preview | ✅ Production | Real-time preview in settings |
| SASS Style Engine | ✅ Production | Compiled CSS from design tokens |
| Backup / Restore | 🔧 In-Dev | Infrastructure ready, ops stubbed |
| QR Code on PDFs | 🔧 In-Dev | `PdfQrCodeRenderer` wired in |

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose + ViewModels + Hilt)         │
│  ├── gui2/  (primary — Compose)                          │
│  └── gui1/  (legacy — deprecated June 2027)             │
├─────────────────────────────────────────────────────────┤
│  Domain Layer (pure Kotlin — no Android deps)           │
│  ├── model/    (Invoice, Customer, InvoiceSettings, …)  │
│  ├── usecase/  (business rules)                         │
│  └── pdf/      (PDF renderer interfaces)                │
├─────────────────────────────────────────────────────────┤
│  Data Layer (Room + SQLCipher + Repositories)           │
│  ├── local/    (Room DAOs, entities, migrations)        │
│  ├── service/  (HtmlPdfInvoiceService, SassStyleEngine) │
│  └── repository/ (repository implementations)           │
└─────────────────────────────────────────────────────────┘
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for a full deep-dive.

---

## Common Tasks

### Create an Invoice
1. Open app → Invoices → **+** button
2. Fill in customer, line items, tax, notes
3. Tap **Save**

### Generate a PDF
1. Open invoice detail
2. Tap **Generate PDF**  
3. Choose email / save / share

### Choose PDF Style
1. Settings → **PDF Settings**
2. Select **HTML CSS** engine
3. Pick a style (SASS Professional recommended)
4. See live preview update immediately
5. Tap **Save Settings**

### Track a Payment
1. Open invoice → **Payments** tab
2. Tap **Record Payment**
3. Enter amount (supports partial)

---

## Documentation Index

| Document | Purpose |
|---|---|
| [ARCHITECTURE.md](ARCHITECTURE.md) | Layer structure and design patterns |
| [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) | Entity relationships and schema |
| [FEATURES.md](FEATURES.md) | Feature inventory and status |
| [TESTING_GUIDE.md](TESTING_GUIDE.md) | How to run and write tests |
| [SECURITY.md](SECURITY.md) | Security practices |
| [FEATURE_STATUS_MATRIX.md](FEATURE_STATUS_MATRIX.md) | GUI1 vs GUI2 feature support |
| [GUI_MIGRATION_PLAN.md](GUI_MIGRATION_PLAN.md) | GUI1 deprecation timeline |
| [DEVELOPMENT_WORKFLOW.md](DEVELOPMENT_WORKFLOW.md) | Code standards and PR process |
| [PERFORMANCE_BASELINE.md](PERFORMANCE_BASELINE.md) | Performance targets and baselines |
| [TROUBLESHOOTING.md](TROUBLESHOOTING.md) | Common issues and solutions |

---

## Tech Stack

- **Language:** Kotlin 1.9.22
- **Build:** Gradle 8.8 (KTS)
- **UI:** Jetpack Compose (Material 3)
- **DI:** Hilt
- **Database:** Room 2.6 + SQLCipher (encrypted)
- **PDF:** iText7 (HTML-to-PDF) + Canvas API
- **Style Engine:** SassStyleEngine (custom SASS-inspired Kotlin compiler)
- **Charts:** Vico
- **Logging:** Timber + Firebase Crashlytics
- **Min SDK:** 21 | **Target SDK:** 34
