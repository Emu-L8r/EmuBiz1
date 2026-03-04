# Project Audit Report — Bizap / EmuBiz

**Date:** 2026-03-04  
**Auditor:** GitHub Copilot CLI  
**Repository:** EmuBiz1/EmuBiz1  
**App Name:** Bizap (package `com.emul8r.bizap`)

---

## Executive Summary

| Area | Status | Notes |
|---|---|---|
| Build Status | ✅ PASSING | APK 23.7 MB, no compile errors reported |
| Architecture Health | ✅ Clean Architecture | UI → ViewModel → Domain → Data → Database layers intact |
| Database Version | `24` | 18 entities, migrations v21→v24 documented |
| Feature Completeness | ~60% | Core invoice/customer flows working; analytics, templates, payments not wired to UI |
| Test Coverage | ❌ 0% | Test infrastructure configured, zero test files found |
| Security | ⚠️ Partial | Firebase Crashlytics integrated; no secrets in code found |

---

## Architecture Analysis

### Layer Breakdown

```
com.emul8r.bizap/
├── ui/                        # Presentation Layer
│   ├── dashboard/             # DashboardScreen.kt (no separate ViewModel)
│   ├── customers/             # CustomerViewModel, CustomerDetailViewModel
│   ├── invoices/              # CreateInvoiceViewModel, EditInvoiceViewModel,
│   │                          #   InvoiceDetailViewModel, InvoiceListViewModel
│   ├── settings/              # BusinessProfileViewModel
│   ├── documents/             # DocumentVaultViewModel
│   ├── analytics/             # RevenueDashboardViewModel, RiskDashboardViewModel
│   ├── dunning/               # DunningNoticesViewModel
│   ├── templates/             # InvoiceTemplateViewModel
│   └── navigation/            # Screen sealed class, NavGraph
├── domain/                    # Domain Layer (pure Kotlin, no Android deps)
│   ├── model/                 # Invoice, Customer, LineItem, BusinessProfile, etc.
│   ├── repository/            # Interfaces: InvoiceRepository, CustomerRepository,
│   │                          #   BusinessProfileRepository, DocumentRepository, etc.
│   └── usecase/               # SaveInvoiceUseCase, GenerateAndSaveInvoiceUseCase,
│                              #   GetCustomerAnalyticsUseCase, SegmentCustomersUseCase,
│                              #   GenerateDunningNoticesUseCase, IdentifyRiskInvoicesUseCase,
│                              #   ForecastCashFlowUseCase, GetPaymentAnalyticsUseCase,
│                              #   GetRevenueMetricsUseCase
├── data/                      # Data Layer
│   ├── local/                 # Room DAOs and entities
│   │   ├── entities/          # @Entity data classes
│   │   ├── dao/ (duplicate)   # AnalyticsDao.kt also at data/local/AnalyticsDao.kt ⚠️
│   │   └── BizapDatabase.kt   # RoomDatabase, version 24
│   ├── repository/            # InvoiceRepositoryImpl, CustomerRepositoryImpl,
│   │                          #   BusinessProfileRepository, CurrencyRepositoryImpl, etc.
│   ├── mapper/                # toDomain() / toEntity() extension functions
│   ├── service/               # InvoicePdfService, PrintService, ExchangeRateService
│   └── worker/                # ExchangeRateWorker (WorkManager)
├── di/                        # Hilt Dependency Injection
│   ├── DatabaseModule.kt      # Provides BizapDatabase, all DAOs
│   ├── RepositoryModule.kt    # Binds interfaces to implementations
│   ├── NetworkModule.kt       # Retrofit + OkHttp for exchange rates
│   ├── DataStoreModule.kt     # DataStore preferences
│   └── WorkManagerInitializer.kt
├── utils/                     # DocumentNamingUtils, DocumentManager
└── BizapApplication.kt        # Hilt entry point, Timber initialization
```

### Dependency Injection Modules

| Module | Provides |
|---|---|
| `DatabaseModule` | `BizapDatabase`, `InvoiceDao`, `CustomerDao`, `AnalyticsDao`, `CurrencyDao`, `DocumentDao`, `PaymentDao` |
| `RepositoryModule` | Binds `InvoiceRepository → InvoiceRepositoryImpl`, `CustomerRepository → CustomerRepositoryImpl`, `BusinessProfileRepository` (concrete), `CurrencyRepository → CurrencyRepositoryImpl`, `DocumentRepository → DocumentRepositoryImpl` |
| `NetworkModule` | `Retrofit` instance, `ExchangeRateService` API interface, `OkHttpClient` |
| `DataStoreModule` | `DataStore<Preferences>` for theme settings |
| `WorkManagerInitializer` | Initializes `ExchangeRateWorker` periodic task |

### Key Architectural Patterns

| Pattern | Implementation |
|---|---|
| **StateFlow/MutableStateFlow** | ViewModels expose `StateFlow<UiState>`, internally use `MutableStateFlow` |
| **Repository Pattern** | Domain interfaces in `domain/repository/`, implementations in `data/repository/` |
| **Use Case Pattern** | Single-responsibility classes in `domain/usecase/`, invoked by ViewModels |
| **Clean Architecture** | Dependencies flow inward: UI → ViewModel → UseCase → Repository Interface → Impl |
| **Hilt DI** | `@HiltViewModel`, `@Inject constructor`, `@Singleton`, `@ApplicationContext` |
| **Room + Flow** | All DAO queries return `Flow<>`, enabling reactive UI updates |
| **`flatMapLatest` scoping** | Invoices scoped to active business via `businessProfileRepository.activeProfile.flatMapLatest` |

### Architecture Strengths

- Clean separation of domain interfaces from data implementations — swapping Room for a remote backend requires only new `RepositoryImpl` classes
- `businessProfileRepository.activeProfile` Flow correctly scopes all data queries to the active business profile
- Mapper pattern (`toDomain()` / `toEntity()`) keeps entity/domain models decoupled
- `@Transaction` annotations on DAO compound operations protect data integrity

### Suggested Improvements

- `BusinessProfileRepository` is a concrete class in `data/repository/` instead of having a domain interface — inconsistent with the repository pattern used elsewhere
- `DashboardScreen.kt` has no dedicated ViewModel; revenue data is hardcoded as `"$0.00"` — should use `RevenueDashboardViewModel`
- Duplicate DAO and entity files need consolidation (see Code Review Findings)

---

## Tech Stack Inventory

### Core Platform

| Component | Version |
|---|---|
| Kotlin | 2.0.21 |
| Android Gradle Plugin | 8.13.2 |
| KSP (Kotlin Symbol Processing) | 2.0.21-1.0.27 |
| compileSdk | 35 |
| minSdk | 26 (Android 8.0 Oreo) |
| targetSdk | 35 |

### Jetpack / AndroidX

| Library | Version |
|---|---|
| Compose BOM | 2024.12.01 |
| Compose Material3 | (via BOM) |
| Navigation Compose | 2.8.5 |
| Hilt (DI) | 2.52 |
| Hilt Navigation Compose | 1.2.0 |
| Room | 2.6.1 |
| DataStore | 1.1.1 |
| Lifecycle (ViewModel, Compose) | 2.8.7 |
| WorkManager | 2.9.0 |

### Networking & Images

| Library | Version |
|---|---|
| Retrofit | 2.9.0 |
| OkHttp | 4.11.0 |
| Gson Converter | 2.9.0 |
| Coil (image loading) | 2.7.0 |

### Analytics & Observability

| Library | Version |
|---|---|
| Firebase BOM | 34.9.0 |
| Firebase Analytics | (via BOM) |
| Firebase Crashlytics | (via BOM) |
| Timber | 5.0.1 |

### Charting & UI

| Library | Version |
|---|---|
| Vico Charts | 1.13.1 |

### Testing (configured, no tests written)

| Library | Version |
|---|---|
| JUnit4 | (default) |
| MockK | (configured) |
| Kotlinx Coroutines Test | (configured) |
| Arch Core Test | (configured) |
| Robolectric | (configured) |
| Compose Testing | (via BOM) |

---

## Database Schema Overview

**Database name:** `bizap_database`  
**Room version:** 24  
**Entities:** 18  
**Location:** `data/local/BizapDatabase.kt`

### Entity Reference

| # | Entity Class | Table Name | Key Columns |
|---|---|---|---|
| 1 | `CustomerEntity` | `customers` | `id` (PK autoGen), `businessProfileId`, `name`, `email`, `phone`, `address` |
| 2 | `InvoiceEntity` | `invoices` | `id` (PK autoGen), `businessProfileId`, `customerId` (nullable), `customerName`, `date`, `dueDate`, `totalAmount` (Long cents), `taxAmount` (Long cents), `taxRate` (Double), `status`, `isQuote`, `currencyCode`, `invoiceYear`, `invoiceSequence`, `version`, `amountPaid`, `parentInvoiceId`, `pdfUri`, `header`, `subheader`, `notes`, `footer` |
| 3 | `LineItemEntity` | `line_items` | `id` (PK autoGen), `invoiceId` (FK), `description`, `quantity` (Double), `unitPrice` (Long cents), `currencyCode` |
| 4 | `PrefilledItemEntity` | `prefilled_items` | `id`, `businessProfileId`, `description`, `defaultPrice` (Long cents) |
| 5 | `GeneratedDocumentEntity` | (documents) | `id`, `invoiceId`, `fileName`, `absolutePath`, `fileType`, `createdAt` |
| 6 | `BusinessProfileEntity` | `business_profiles` | `id` (PK autoGen), `businessName`, `abn`, `email`, `phone`, `address`, `website`, `bsbNumber`, `accountNumber`, `accountName`, `bankName`, `logoBase64`, `signatureUri`, `isTaxRegistered`, `defaultTaxRate` (Float), `createdAt` |
| 7 | `CurrencyEntity` | (currencies) | `code` (PK), `name`, `symbol`, `isEnabled` |
| 8 | `ExchangeRateEntity` | (exchange_rates) | `fromCode`, `toCode`, `rate`, `lastUpdated` |
| 9 | `InvoiceAnalyticsSnapshot` | (analytics snapshots) | `invoiceId`, `snapshotDate`, `revenue`, `status` |
| 10 | `DailyRevenueSnapshot` | (daily_revenue_snapshots) | `date`, `businessId`, `totalRevenue`, `invoiceCount` |
| 11 | `CustomerAnalyticsSnapshot` | (customer analytics) | `customerId`, `totalRevenue`, `invoiceCount`, `avgDaysToPayment` |
| 12 | `BusinessHealthMetrics` | (health_metrics) | `businessId`, `score`, `calculatedAt` |
| 13 | `InvoicePaymentEntity` | `invoice_payments` | `id`, `invoiceId`, `amount` (Long cents), `paidAt`, `method` |
| 14 | `InvoicePaymentSnapshot` | `invoice_payment_snapshots` | `invoiceId`, `totalPaid`, `outstandingBalance`, `lastPaymentDate` |
| 15 | `DailyPaymentSnapshot` | `daily_payment_snapshots` | `date`, `businessId`, `totalCollected`, `invoiceCount` |
| 16 | `CollectionMetrics` | `collection_metrics` | `businessId`, `avgDaysToCollection`, `collectionRate`, `calculatedAt` |
| 17 | `InvoiceTemplate` | `invoice_templates` | `id`, `businessProfileId`, `name`, `primaryColor`, `fontFamily`, `hideLineItems`, `hidePaymentTerms` |
| 18 | `InvoiceCustomField` | `invoice_custom_fields` | `id`, `templateId`, `fieldName`, `fieldValue`, `displayOrder` |

### Relationships

```
business_profiles (1) ──< invoices (many)
business_profiles (1) ──< customers (many)
business_profiles (1) ──< prefilled_items (many)
business_profiles (1) ──< invoice_templates (many)
invoices (1) ──< line_items (many)          [via InvoiceWithItems @Relation]
invoices (1) ──< invoice_payments (many)
invoices (1) ── GeneratedDocumentEntity     [via documentRepository]
invoice_templates (1) ──< invoice_custom_fields (many)
invoices (self-reference) parentInvoiceId   [versioning/corrections]
```

### Migration History

| Migration | Change | Reason |
|---|---|---|
| v21 → v22 | `DROP TABLE IF EXISTS pending_operations` | Removed offline sync feature |
| v22 → v23 | `ALTER TABLE line_items ADD COLUMN currencyCode TEXT NOT NULL DEFAULT 'AUD'` | Multi-currency support for line items |
| v23 → v24 | Fixed `Double → Long` for monetary fields in `invoice_payments`, `invoice_payment_snapshots`, `daily_payment_snapshots`, `collection_metrics` | Consistent cents-based monetary representation |

### Advanced Database Features (not wired to UI)

| Feature | Tables | Status |
|---|---|---|
| Invoice payment tracking | `invoice_payments`, `invoice_payment_snapshots` | Schema exists, `PaymentDao` present, not in navigation |
| Currency conversion | `CurrencyEntity`, `ExchangeRateEntity` | Schema + `ExchangeRateWorker` + `ExchangeRateService` exist, `CurrencyRepository` bound but no UI |
| Analytics snapshots | `DailyRevenueSnapshot`, `InvoiceAnalyticsSnapshot` | Schema exists, worker/use cases present, `RevenueDashboardScreen` exists but not in `NavGraph` |
| Customer segmentation | `CustomerAnalyticsSnapshot`, `CollectionMetrics` | Full use case stack (`SegmentCustomersUseCase`, `GetCustomerAnalyticsUseCase`), no nav entry |
| Invoice templates | `invoice_templates`, `invoice_custom_fields` | Full entity/DAO/ViewModel stack, not in navigation |
| Business health metrics | `BusinessHealthMetrics` | Schema only, no ViewModel wired |

---

## Feature Completeness Matrix

| Feature | Working | Partial | Not Wired to UI |
|---|---|---|---|
| Business Profile Setup | ✅ | | |
| Multi-Business Switching | ✅ | | |
| Customer CRUD | ✅ | | |
| Invoice Creation | ✅ | | |
| Invoice Editing | ✅ | | |
| Invoice Detail View | ✅ | | |
| Invoice PDF Generation | ✅ | | |
| Invoice PDF Export/Share | ✅ | | |
| Invoice Printing | ✅ | | |
| Invoice Status Management | ✅ | | |
| Invoice Versioning / Corrections | ✅ | | |
| Quote Generation | ✅ | | |
| Document Vault | ✅ | | |
| Prefilled Items | ✅ | | |
| Theme Settings | ✅ | | |
| Dashboard (Revenue Card) | | ⚠️ Hardcoded `$0.00` | |
| Payment Recording (`recordPayment`) | | ⚠️ ViewModel only | |
| Revenue Dashboard | | | ❌ Screen exists, not in NavGraph |
| Invoice Templates | | | ❌ Full stack, not in NavGraph |
| Multi-Currency | | | ❌ Infrastructure built, no UI |
| Payment Analytics | | | ❌ Use cases + ViewModel, no nav |
| Customer Segmentation | | | ❌ Use cases built, no nav |
| Dunning Notices | | | ❌ Full ViewModel, no nav |
| Risk Scoring | | | ❌ Full ViewModel, no nav |
| Cash Flow Forecasting | | | ❌ Use case built, no nav |
| Business Health Metrics | | | ❌ Schema only |
| Automated Tests | | | ❌ Infrastructure configured, 0 test files |

---

## Build Information

| Property | Value |
|---|---|
| Package Name | `com.emul8r.bizap` |
| Version Code | 2 |
| Version Name | `1.0` |
| APK Size | 23.7 MB |
| Main Activity | `com.emul8r.bizap.MainActivity` |
| File Provider Authority | `com.emul8r.bizap.fileprovider` |
| Build Type (release) | minified, Proguard enabled |

### Build Commands

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run unit tests (when written)
./gradlew test

# Run instrumented tests (when written)
./gradlew connectedAndroidTest
```

### Source Root

```
Bizap/app/src/main/java/com/emul8r/bizap/
```
