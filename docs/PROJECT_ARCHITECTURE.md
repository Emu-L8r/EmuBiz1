# Project Architecture — Bizap (EmuBiz1)

**Last Updated:** 2026-03-08  
**Status:** Current (DB v32)  
**App Package:** `com.emul8r.bizap`

---

## Table of Contents

1. [Architectural Overview](#1-architectural-overview)
2. [Layer Breakdown](#2-layer-breakdown)
3. [Module Structure](#3-module-structure)
4. [Dependency Injection Graph](#4-dependency-injection-graph)
5. [Data Flow Diagrams](#5-data-flow-diagrams)
6. [Dual GUI System](#6-dual-gui-system)
7. [Clean Architecture Principles Applied](#7-clean-architecture-principles-applied)
8. [Key Design Decisions](#8-key-design-decisions)

---

## 1. Architectural Overview

Bizap follows **Clean Architecture** with **MVVM** at the presentation layer and an **offline-first** data strategy.

```
┌─────────────────────────────────────────────────────────┐
│                  Presentation Layer                     │
│   Activities · ViewModels · Screens (Compose) · Nav     │
├─────────────────────────────────────────────────────────┤
│                    Domain Layer                         │
│       Use Cases · Repository Interfaces · Models        │
├─────────────────────────────────────────────────────────┤
│                     Data Layer                          │
│   Repository Impls · Room DAOs · Entities · Remote API  │
├─────────────────────────────────────────────────────────┤
│                  Infrastructure                         │
│       Room (SQLite) · WorkManager · DataStore           │
└─────────────────────────────────────────────────────────┘
```

**Technology Stack:**

| Concern | Technology |
|---------|-----------|
| UI Framework | Jetpack Compose (Material 3) |
| Architecture | MVVM + Clean Architecture |
| DI | Hilt |
| Local DB | Room (SQLite) |
| Reactive | Kotlin Flow + StateFlow |
| Background | WorkManager |
| Settings | DataStore Preferences |
| Networking | Retrofit + OkHttp |
| Serialisation | KotlinX Serialization |
| Logging | Timber |
| PDF | Custom InvoicePdfService |

---

## 2. Layer Breakdown

### 2.1 Presentation Layer — `ui/`

Responsible for displaying data and handling user interaction only.

```
ui/
├── activities/
│   ├── MainActivity.kt                  # App entry point
│   ├── TraditionalGUIMainActivity.kt    # GUI1 host activity
│   └── ModernGUIMainActivity.kt         # GUI2 host activity
├── landing/                             # GUI selector landing page
├── gui2/                                # Modern GUI (GUI2) screens
│   ├── dashboard/                       # Dashboard screens + ViewModel
│   ├── invoices/                        # Invoice list/detail/create/edit
│   ├── customers/                       # Customer list/detail/create/edit
│   ├── analytics/                       # Revenue/Payment/Risk analytics
│   ├── navigation/                      # GuiV2NavGraph + routes
│   ├── components/                      # Reusable Compose components
│   └── settings/                        # GUI2 settings screens
├── invoices/                            # GUI1 invoice screens
├── customers/                           # GUI1 customer screens
├── dashboard/                           # GUI1 dashboard
├── revenue/                             # GUI1 analytics screens
├── templates/                           # Invoice template management
├── theme/                               # Material 3 design tokens
└── navigation/                          # GUI1 navigation graph
```

**ViewModel Rules:**
- All ViewModels are `@HiltViewModel`
- State exposed as `StateFlow<UiState>` (never `LiveData`)
- One-shot events via `SharedFlow`
- No Android framework imports in ViewModels (except `Application` where necessary)

### 2.2 Domain Layer — `domain/`

Pure Kotlin — zero Android dependencies. Contains business logic.

```
domain/
├── model/                               # Domain models (data classes)
│   └── gui2/                            # GUI2-specific domain models
├── repository/                          # Repository interfaces (contracts)
├── usecase/                             # Use case implementations
│   ├── RecordPaymentUseCase.kt
│   ├── SaveInvoiceUseCase.kt
│   ├── UpdateInvoiceUseCase.kt
│   ├── DeleteInvoiceUseCase.kt
│   ├── GenerateAndSaveInvoiceUseCase.kt
│   └── SyncPendingOperationsUseCase.kt
├── validation/                          # Validation rules
├── invoice/                             # Invoice domain sub-package
├── customer/                            # Customer domain sub-package
├── revenue/                             # Revenue domain sub-package
├── analytics/                           # Analytics domain sub-package
├── pdf/                                 # PDF generation contracts
└── error/                               # Domain error types
```

### 2.3 Data Layer — `data/`

Implements domain interfaces. Contains all Android/framework dependencies.

```
data/
├── local/
│   ├── AppDatabase.kt                   # Room DB (v32, 22 entities, 17 DAOs)
│   ├── entities/                        # Room entity data classes
│   ├── dao/                             # Room DAO interfaces
│   ├── migrations/                      # Schema migrations (v21→v32)
│   ├── offline/                         # Offline operation queue
│   └── typeconverters/                  # Room type converters
├── repository/
│   ├── InvoiceRepositoryImpl.kt
│   ├── CustomerRepositoryImpl.kt
│   ├── BusinessProfileRepositoryImpl.kt
│   └── gui2/                            # GUI2-optimised repositories
│       ├── PaymentRepositoryV2.kt
│       ├── RevenueRepositoryV2.kt
│       ├── PaymentAnalyticsRepositoryV2.kt
│       ├── RiskAnalyticsRepositoryV2.kt
│       └── BusinessContextRepositoryV2.kt
├── remote/api/                          # Retrofit service definitions
├── mapper/                              # Entity ↔ domain model mappers
├── service/                             # Business-logic data services
├── worker/                              # WorkManager workers
├── network/                             # Network utilities
└── backup/                              # Backup/restore services
```

### 2.4 DI Layer — `di/`

Hilt modules wiring everything together.

```
di/
├── DatabaseModule.kt       # AppDatabase + all 17 DAOs
├── RepositoryModule.kt     # Interface → Implementation bindings
├── GuiV2Module.kt          # GUI2-specific repository providers
├── NetworkModule.kt        # Retrofit + OkHttp
├── SyncWorkerModule.kt     # WorkManager + scheduler
└── DataStoreModule.kt      # DataStore (preferences, GUI mode)
```

---

## 3. Module Structure

The project is a single-module Android application. All code resides in `:app`.

```
Bizap/
├── app/
│   ├── src/
│   │   ├── main/java/com/emul8r/bizap/   # ~300 Kotlin files
│   │   ├── test/java/                     # 47 unit test files
│   │   └── androidTest/java/              # 11 integration tests
│   └── build.gradle.kts
├── gradle/libs.versions.toml              # Version catalog
└── build.gradle.kts
```

---

## 4. Dependency Injection Graph

```
SingletonComponent
│
├── DatabaseModule
│   ├── AppDatabase → (provides all 17 DAOs)
│   └── DataStore<Preferences>
│
├── RepositoryModule (@Binds)
│   ├── InvoiceRepository → InvoiceRepositoryImpl
│   ├── CustomerRepository → CustomerRepositoryImpl
│   ├── BusinessProfileRepository → BusinessProfileRepositoryImpl
│   ├── DocumentRepository → DocumentRepositoryImpl
│   ├── RevenueRepository → RevenueRepositoryImpl
│   ├── PaymentAnalyticsRepository → PaymentAnalyticsRepositoryImpl
│   ├── CustomerAnalyticsRepository → CustomerAnalyticsRepositoryImpl
│   ├── OfflineQueueRepository → OfflineQueueRepositoryImpl
│   └── ... (14 repositories total)
│
├── GuiV2Module (@Provides)
│   ├── RevenueRepositoryV2 (InvoiceDaoV2)
│   ├── PaymentAnalyticsRepositoryV2 (InvoiceDaoV2)
│   ├── RiskAnalyticsRepositoryV2 (InvoiceDaoV2)
│   ├── BusinessContextRepositoryV2 (BusinessProfileRepository)
│   └── PaymentRepositoryV2 (AppDatabase, InvoiceDaoV2, PaymentDaoV2)
│
├── NetworkModule
│   ├── Json (KotlinX Serialization)
│   ├── OkHttpClient
│   ├── Retrofit → ExchangeRateService
│   └── ExchangeRateService
│
└── SyncWorkerModule
    ├── WorkManager
    └── SyncWorkerScheduler

ViewModelComponent (scoped to ViewModel lifecycle)
│
└── All @HiltViewModel classes (injected from SingletonComponent)
```

---

## 5. Data Flow Diagrams

### 5.1 Read Flow (Reactive)

```
Room DB
  └─[Flow]─► DAO
               └─[Flow]─► Repository
                            └─[StateFlow]─► ViewModel
                                             └─[collectAsState]─► Composable
```

Changes in the database automatically propagate to the UI without explicit refresh calls.

### 5.2 Write Flow (Command)

```
User Action
  └─► Composable
        └─► ViewModel.method()
              └─► UseCase.invoke()
                    ├─► Repository.write()
                    │     └─► DAO.insert/update/delete()
                    └─► Result<T> returned to ViewModel
                          └─► UiState updated → UI reflects change
```

### 5.3 Offline-First Write Flow

```
User Action
  └─► UseCase.invoke()
        ├─[Online]─► Repository.write() → DAO → DB
        │              └─► SnapshotSyncHelper.sync()
        └─[Offline]─► OfflineQueueService.queue()
                        └─► PendingOperationEntity saved to DB
                              └─[WorkManager, 15min]─► SyncWorker
                                                         └─► SyncPendingOperationsUseCase
                                                               └─► Process queue FIFO
```

### 5.4 Payment Recording Flow

```
RecordPaymentViewModel.submit()
  └─► RecordPaymentUseCase.invoke()
        ├── Validate: amount > 0, amount ≤ outstanding
        ├── Validate: paymentDate ≤ today, ≥ invoiceDate
        └── PaymentRepositoryV2.recordPayment()
              └── database.withTransaction {
                    INSERT PaymentEntity
                    UPDATE invoices SET amountPaid = amountPaid + amount
                    UPDATE invoices SET status = PAID|PARTIALLY_PAID
                  }
```

---

## 6. Dual GUI System

Bizap supports two parallel GUI implementations sharing the same data layer.

```
                   ┌──────────────────────┐
                   │      MainActivity    │
                   │  (GUI Mode selector) │
                   └────────┬─────────────┘
                            │
              ┌─────────────┴──────────────┐
              ▼                            ▼
 TraditionalGUIMainActivity    ModernGUIMainActivity
     (GUI1 — Classic)              (GUI2 — Modern)
   MainScreen (GUI1 NavGraph)   GuiV2NavGraph
              │                            │
              └─────────────┬──────────────┘
                            ▼
                    Shared Data Layer
              (Same DAOs, Entities, Repositories)
```

**GUI Mode Persistence:**
- User preference stored in `DataStore<Preferences>`
- `LandingViewModel` reads/writes `GuiMode` enum
- Mode persists across app restarts

**GUI1 vs GUI2 — Key Differences:**

| Feature | GUI1 | GUI2 |
|---------|------|------|
| UI Style | Traditional Material | Modern Compose with animations |
| Repositories | Shared (`InvoiceRepository`) | Shared + V2 variants |
| ViewModels | Shared ViewModel classes | `*ViewModelV2` classes |
| Analytics | `RevenueDashboardViewModel` | `RevenueAnalyticsViewModelV2` |
| Navigation | `Screen` sealed class | `GuiV2NavGraph` destinations |

---

## 7. Clean Architecture Principles Applied

| Principle | How Applied |
|-----------|-------------|
| **Dependency Rule** | Inner layers (domain) never import outer layers (data, UI) |
| **Abstraction** | Domain defines interfaces; data layer implements them |
| **Single Responsibility** | Each class has one reason to change |
| **Testability** | Domain use cases are pure Kotlin — fully unit-testable |
| **Framework Independence** | Domain layer has zero Android imports |
| **Unidirectional Data Flow** | UI reads StateFlow; writes go through ViewModels → UseCases |

---

## 8. Key Design Decisions

### 8.1 StateFlow over LiveData
All ViewModels expose `StateFlow<UiState>` sealed classes. `UiState` typically has variants: `Loading`, `Success(data)`, `Error(message)`.

### 8.2 Result<T> for Error Handling
All repository methods and use cases return `Result<T>`. ViewModels `fold` on the result to update the UI state, eliminating null pointer exceptions.

### 8.3 Atomic Payment Transactions
`PaymentRepositoryV2.recordPayment()` uses `database.withTransaction {}` to atomically insert the payment and update `amountPaid` on the invoice, preventing partial writes.

### 8.4 Denormalised Analytics Snapshots
To avoid expensive joins at query time, analytics data is pre-aggregated into snapshot entities (`InvoiceAnalyticsSnapshot`, `DailyRevenueSnapshot`, etc.). These are updated at write time.

### 8.5 Offline-First Architecture
All mutating operations check `ConnectivityHelper`. If offline, the operation is queued in `OfflineOperation` (Room). `SyncWorker` replays the queue every 15 minutes when connectivity is restored.

### 8.6 Gradle Version Catalog
All dependency versions are centralised in `gradle/libs.versions.toml`, preventing version drift and simplifying dependency management.
