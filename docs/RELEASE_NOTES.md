# Release Notes — Bizap v1.0.0

**Release Date:** March 2026  
**App Package:** `com.emul8r.bizap`  
**Minimum Android Version:** Android 8.0 (API 26)  
**Target Android Version:** Android 14 (API 34)

---

## What's New in v1.0.0

This is the first production release of Bizap — a professional invoicing and business management application for Android.

### Dual-Mode UI (New)

Bizap now ships with two UI display modes that can be toggled at any time in **Settings → Appearance**:

- **Modern** (default) — Spacious Material 3 cards with full details visible at a glance.
- **Compact** — Dense list rows for power users and small screens.

Your choice is persisted automatically and applies instantly across Dashboard, Invoice List, and Customer List screens. Both modes read from the exact same data layer — no fake or stale data in either mode.

See `Bizap/DUAL_MODE_UI_GUIDE.md` for full details.

### Architecture Improvements (v1.0 baseline)

- **No more mock data fallbacks** — Payment and Revenue analytics now show a proper `Error` state instead of hardcoded sample numbers when real data is unavailable.
- **Clean Architecture enforced** — Domain layer has zero data-layer imports. Data layer has zero UI-layer imports. Enforced by automated `ArchitectureTest`.
- **Domain models for all repository interfaces** — `DocumentRepository`, `CustomFieldRepository`, and `PrefilledItemRepository` now use pure Kotlin domain models (`GeneratedDocument`, `CustomField`, `PrefilledItem`) instead of Room entities.
- **Service layer reorganised** — `AccountingService` correctly lives in the data layer alongside the DAOs it uses. HTML/PDF processing classes moved from `ui/` to `data/service/`.

---

## What's Included

### Core Features

#### Invoice Management
- Create, edit, and delete invoices
- Auto-generated invoice numbers (INV-YYYY-XXXXXX format)
- Line item management (name, quantity, unit price, tax)
- Invoice versioning (multiple versions per invoice group)
- Status workflow: DRAFT → SENT → PARTIALLY_PAID → PAID
- PDF generation with business branding
- Invoice templates for recurring invoice types

#### Payment Tracking
- Record full and partial payments against invoices
- Automatic status update on payment recording
  - Partial payment → PARTIALLY_PAID
  - Full payment → PAID
- Payment history per invoice
- Outstanding balance calculation

#### Revenue Dashboard
- Month-to-date (MTD) revenue
- Year-to-date (YTD) revenue
- Weekly revenue
- 30-day revenue trend chart
- Collection rate (amount-based)
- Outstanding amounts
- Overdue invoice count and aging

#### Customer Management
- Full customer profile (name, email, phone, address)
- Customer validation (required fields, email format)
- Customer selection on invoice creation
- Customer history view

#### Business Profile
- Business name, logo, address
- ABN/Tax number support
- Currency selection (multi-currency display)
- Invoice footer customization

#### Offline-First Architecture
- All features work without internet connection
- Operations queued when offline, automatically synced on reconnect
- Real-time sync status indicator in both UIs
- Zero data loss guaranteed

#### Dual GUI System
- GUI1 (Classic): Traditional navigation style
- GUI2 (Modern): Jetpack Compose-based modern UI
- Both GUIs show identical, consistent data
- Switch between GUIs at any time from the landing screen

#### Security
- PIN authentication (required on every launch or after timeout)
- Session management with configurable timeout
- Secure local storage (Android Keystore where applicable)

---

## What's Coming in v1.1 (Post-Launch)

These features are planned for the first post-launch update, expected 2-4 weeks after v1.0 release:

| Feature | Priority | Estimated Timeline |
|---------|----------|-------------------|
| Database Encryption (SQLCipher) | HIGH | 1-2 weeks |
| Cloud Backup & Sync | HIGH | 2-3 weeks |
| Biometric Authentication (fingerprint/face) | MEDIUM | 2-3 weeks |
| Advanced Analytics (profit/loss, tax reports) | MEDIUM | 3-4 weeks |
| Multi-user Support | LOW | 4+ weeks |
| Tablet Layout Optimization | LOW | 4+ weeks |
| Payment Gateway Integration | LOW | TBD |

---

## Known Limitations

These are transparent acknowledgments of current v1.0 limitations:

### Security
- **Database is not encrypted at rest.** The SQLite database is stored in the app's private directory, which is inaccessible on non-rooted devices under normal circumstances. Encryption via SQLCipher is planned for v1.1.
- **PIN only (no biometric).** Fingerprint and face authentication are planned for v1.1.

### Sync
- **Single-user only.** Conflict resolution uses "server wins" strategy. Multi-user collaborative editing is not supported in v1.0.
- **No cloud backup.** Data is local-only. Loss or replacement of device means loss of data. Cloud backup is planned for v1.1.

### UI
- **Not optimized for tablets.** The app is designed for phone form factors. Tablet layouts are planned for v1.1.
- **Two GUIs in transition.** GUI1 and GUI2 are both functional but the team is migrating to GUI2 as the primary UI. GUI1 will be deprecated in v1.2.

### Reporting
- **No tax reports.** Automated tax calculation and reporting is planned for v1.1 analytics.

---

## Bug Fixes (vs. Beta)

- Fixed dashboard showing $0 revenue on first launch (caused by strict PAID-only filter — now includes PARTIALLY_PAID)
- Fixed race condition in snapshot sync (now non-blocking; UI reads from live table)
- Fixed GUI1 and GUI2 showing different revenue totals (now both read from InvoiceDaoV2)
- Fixed 31 test compilation errors after Kotlin/Gradle update (March 2026 test suite recovery)

---

## Technical Notes

| Property | Value |
|----------|-------|
| Minimum SDK | 26 (Android 8.0) |
| Target SDK | 34 (Android 14) |
| Database Version | 32 |
| Kotlin Version | See `gradle/libs.versions.toml` |
| Jetpack Compose | Material 3 |
| Architecture | MVVM + Clean Architecture |
| Dependency Injection | Hilt |
| Local Database | Room (SQLite) |
| Background Work | WorkManager |

---

## Upgrade Notes

If upgrading from a beta/development build:
1. The app will automatically run database migrations (v21 → v32)
2. All existing invoice, customer, and payment data will be preserved
3. No manual action required

If installing fresh (no prior version):
1. You will be prompted to set up a PIN on first launch
2. No data migration needed

---

## Feedback & Support

For issues, feedback, or feature requests, please use the in-app feedback option or file an issue in the project repository.
