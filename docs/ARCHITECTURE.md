# Architecture Documentation

## Overview

Bizap follows Clean Architecture principles with three clearly separated layers:
**Domain**, **Data**, and **Presentation**. This document describes the layer
structure, dependency rules, and key conventions used throughout the codebase.

---

## Layer Structure

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (ViewModels, Composables, UI State)    │
│  ui/**, presentation/**                 │
└──────────────────┬──────────────────────┘
                   │ depends on
┌──────────────────▼──────────────────────┐
│           Domain Layer                  │
│  (Models, Repository interfaces,        │
│   Use Cases, Business Logic)            │
│  domain/**                              │
└──────────────────┬──────────────────────┘
                   │ implements
┌──────────────────▼──────────────────────┐
│            Data Layer                   │
│  (Room DAOs, Repositories Impl,         │
│   Network, DataStore)                   │
│  data/**                                │
└─────────────────────────────────────────┘
```

---

## Dependency Rules

1. **Domain layer has zero Android framework dependencies.**  
   Pure Kotlin only. No `android.*`, `androidx.*`, or Room annotations allowed
   in `domain/model/`, `domain/repository/`, or `domain/usecase/`.

2. **Data layer implements Domain interfaces.**  
   All repository implementations in `data/repository/` implement interfaces
   defined in `domain/repository/`. Room entities in `data/local/entity/` have
   corresponding pure Kotlin domain models in `domain/model/`.

3. **Presentation layer depends only on Domain.**  
   ViewModels inject repository interfaces (not implementations). No direct
   Room DAO or database access from ViewModels — everything goes through domain
   repositories.

4. **No circular dependencies.**  
   Data → Domain only. Domain never imports from Data or Presentation.
   Presentation never imports from Data directly.

---

## Key Packages

### Domain (`com.emul8r.bizap.domain`)

| Package | Contents |
|---------|----------|
| `domain/model/` | Pure Kotlin data classes: `Invoice`, `Customer`, `Currency`, `BusinessProfile`, etc. |
| `domain/repository/` | Repository interfaces: `InvoiceRepository`, `CustomerRepository`, etc. |
| `domain/usecase/` | Single-responsibility use cases: `GenerateAndSaveInvoiceUseCase`, `CalculateInvoiceMetricsUseCase`, etc. |
| `domain/validation/` | Business rule validation: `ValidationRules` |
| `domain/service/` | Domain service interfaces: `AuthenticationManager` |
| `domain/manager/` | Domain managers: `BusinessContextManager` |

### Data (`com.emul8r.bizap.data`)

| Package | Contents |
|---------|----------|
| `data/local/entity/` | Room annotated entities: `InvoiceEntity`, `CustomerEntity`, etc. |
| `data/local/dao/` | Room DAOs: `InvoiceDao`, `CustomerDao`, etc. |
| `data/repository/` | Repository implementations using Room and/or network |
| `data/service/` | Service implementations: `InvoicePdfService`, `PdfStyler` |

**Mapper pattern**: Each entity has `toEntity()` and `toDomain()` extension
functions that convert between the data and domain representations.

### Presentation (`com.emul8r.bizap.ui` / `com.emul8r.bizap.presentation`)

| Package | Contents |
|---------|----------|
| `ui/gui2/` | GUI2 screens, ViewModels, navigation |
| `ui/customers/` | Customer screens (shared between GUI1 and GUI2) |
| `ui/settings/` | Settings screens |
| `ui/shared/screens/` | Screens shared between GUI1 and GUI2 |
| `presentation/viewmodel/` | ViewModels that use `SettingsRepository` |

---

## GUI Architecture

The app has a single entry point (`MainActivity`) that routes to GUI2 via
`GuiV2NavGraph`. The navigation stack is entirely Compose-based using
typed routes (`ScreenV2` sealed class + `@Serializable`).

**State management**: Each screen has a corresponding ViewModel that exposes
a single `uiState: StateFlow<UiState>` using sealed classes for Loading /
Error / Success patterns.

**Business context**: `BusinessContextManager` is a `@Singleton` that holds
the active `businessId` in memory. ViewModels call
`setActiveBusinessId(id)` on navigation and `requireActiveBusinessId()` to
retrieve it.

### Dual-Mode UI

Bizap supports two display modes: **Modern** (spacious cards) and **Compact**
(dense lists). The user's preference is stored in DataStore and exposed as a
`StateFlow<UIMode>` from `AppStateViewModel`.

- **`UIMode` enum** lives in `domain/model/UIMode.kt` (pure Kotlin, no Android deps).
- **`UIPreferences`** interface in `domain/settings/` is implemented by
  `UIPreferencesImpl` in `data/settings/` using DataStore.
- **Conditional rendering** is done at the **screen level** (not component level):
  each screen composable checks `uiMode == UIMode.COMPACT` and delegates to
  either a `Compact*` or `Modern*` composable.
- **Compact-mode dimension tokens** live in `ui/theme/CompactDimensions.kt`.

Both modes share the same repositories, DAOs, and ViewModel — toggling the
mode never triggers a data reload and never shows stale data.

---

## Dependency Injection

Hilt is used for dependency injection. Module bindings are organised in:

| Module | Bindings |
|--------|----------|
| `di/DatabaseModule.kt` | Room database and all DAOs |
| `di/RepositoryModule.kt` | Repository interface → implementation bindings |
| `di/GuiV2Module.kt` | GUI2-specific bindings |
| `di/SettingsModule.kt` | `SettingsRepository` binding |
| `di/TickerModule.kt` | `DateChangeTickerManager` singleton |

---

## Testing Strategy

- **Unit tests** (`src/test/`): Test ViewModels, use cases, repositories, and
  pure domain logic using MockK and `TestCoroutineDispatcher`.
- **Robolectric tests**: Tests requiring Android context (e.g., Activity tests)
  use Robolectric with `@Config(sdk = [28])`.
- **Architecture tests** (`ArchitectureTest.kt`): Verify that layering rules
  are not violated by scanning source files for forbidden imports.

---

## Key Conventions

- **Repository pattern**: All data access goes through domain repository
  interfaces. ViewModels never reference Room or network directly.
- **Result type**: Repository methods return `Result<T>` to propagate errors
  without throwing exceptions.
- **StateFlow**: ViewModels expose state as `StateFlow`, collected in
  Composables with `collectAsStateWithLifecycle()`.
- **Single source of truth for invoice metrics**: `CalculateInvoiceMetricsUseCase`
  is the only place where subtotal, tax, and total are computed.
- **Note model separation**: `domain/model/Note.kt` is pure Kotlin;
  `data/local/entity/NoteEntity.kt` has Room annotations. Mappers in
  the repository convert between them.
