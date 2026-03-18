# Phase 3: Settings Consolidation & User Preference Management

## Overview

Phase 3 introduces a unified settings and user-preference management system backed by
Jetpack DataStore.  All preferences are consolidated under a single
`SettingsRepository` interface, exposed reactively via `StateFlow`, and surfaced
through a comprehensive `SettingsScreen` UI with four tabs.

---

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│  UI Layer                                                        │
│  presentation/ui/screens/SettingsScreen.kt                       │
│  ├── ThemeSettingsCard                                           │
│  ├── DisplayModeSettingsCard                                     │
│  ├── NotificationSettingsCard                                    │
│  ├── SyncSettingsCard                                            │
│  └── AboutSettingsCard                                           │
├──────────────────────────────────────────────────────────────────┤
│  ViewModel Layer                                                 │
│  presentation/viewmodel/SettingsViewModel.kt                     │
│  ├── settings: StateFlow<Settings>                               │
│  ├── themePreference: StateFlow<ThemePreference>                 │
│  └── setThemePreference / setDisplayMode / …                     │
├──────────────────────────────────────────────────────────────────┤
│  Use-Case Layer                                                  │
│  domain/usecase/settings/                                        │
│  ├── GetSettingsUseCase                                          │
│  ├── UpdateThemeUseCase                                          │
│  ├── UpdateDisplayModeUseCase                                    │
│  ├── UpdateNotificationSettingsUseCase                           │
│  ├── UpdateSyncSettingsUseCase                                   │
│  └── ResetSettingsToDefaultUseCase                               │
├──────────────────────────────────────────────────────────────────┤
│  Repository Layer                                                │
│  domain/repository/SettingsRepository.kt  (interface)           │
│  data/repository/SettingsRepositoryImpl.kt (DataStore-backed)   │
├──────────────────────────────────────────────────────────────────┤
│  DI                                                              │
│  di/SettingsModule.kt – binds impl to interface (Singleton)      │
│  di/DatabaseModule.kt – provides DataStore<Preferences>         │
└──────────────────────────────────────────────────────────────────┘
```

---

## Domain Models

| Type | Location | Purpose |
|------|----------|---------|
| `Settings` | `domain/model/Settings.kt` | Aggregate data class for all preferences |
| `ThemePreference` | `domain/model/ThemePreference.kt` | LIGHT / DARK / AUTO |
| `DisplayMode` | `domain/model/DisplayMode.kt` | LIST_VIEW / GRID_VIEW / CARD_VIEW |
| `UiDensity` | `domain/model/UiDensity.kt` | COMFORTABLE / COMPACT / RELAXED |

---

## How to Add a New Setting

1. **Add the field** to `Settings.kt` with a default value.
2. **Add a key** in `SettingsRepositoryImpl.Keys`.
3. **Add a read** in `SettingsRepositoryImpl.Preferences.toSettings()`.
4. **Add a write** method to `SettingsRepository` interface and implement it.
5. **Add a use case** in `domain/usecase/settings/` (optional for thin operations).
6. **Expose it** from `SettingsViewModel` (derived `StateFlow` + write helper).
7. **Add a UI control** in the relevant `*SettingsCard` composable.
8. **Write a test** in `SettingsRepositoryImplTest` (save/load round-trip).

---

## Theme System Integration

`ThemeProvider` (`presentation/ui/theme/ThemeProvider.kt`) sits at the root of the
composition tree in `MainActivity`.  It reads `ThemePreference` from
`SettingsViewModel` and maps it to the `BizapTheme` call:

| Preference | Effective dark-mode |
|------------|---------------------|
| `LIGHT` | Always light |
| `DARK` | Always dark |
| `AUTO` | Follows `isSystemInDarkTheme()` |

The legacy `ThemeViewModel` seed-colour is still respected, so per-business colour
customisation continues to work alongside the new three-way toggle.

---

## Navigation

### GUI 1 (MainScreen)

| Destination | Route |
|-------------|-------|
| Existing settings hub | `Screen.SettingsHub` |
| New comprehensive settings | `Screen.AppSettings` |

A new **"App Settings"** entry has been added to `SettingsHubScreen`.

### GUI 2 (GuiV2NavGraph)

| Destination | Route |
|-------------|-------|
| Existing settings hub | `ScreenV2.Settings(businessId)` |
| New comprehensive settings | `ScreenV2.AppSettings(businessId)` |

A new **"App Settings"** card has been added to `SettingsHubScreenV2`.

---

## DataStore Key Namespace

All keys written by `SettingsRepositoryImpl` are prefixed with `settings_` to avoid
collisions with other DataStore writers (e.g. `ThemeRepositoryImpl`, `LandingViewModel`):

```
settings_theme_preference
settings_display_mode
settings_default_invoice_status_filter
settings_default_days_lookback
settings_ui_density
settings_notifications_enabled
settings_email_notifications_enabled
settings_currency_code
settings_locale_language
settings_auto_sync_enabled
settings_sync_frequency_minutes
settings_last_updated
```

---

## Testing Guidelines

- `SettingsRepositoryImplTest` uses a real `PreferenceDataStoreFactory` backed by a
  temporary file (via `TemporaryFolder` JUnit rule) so there is no DataStore singleton
  contention between tests.
- `SettingsViewModelTest` mocks the repository and verifies delegation using MockK.
- Run tests with: `./gradlew :app:testDebugUnitTest`

---

## Performance Considerations

- All DataStore reads are non-blocking `Flow` collectors; no synchronous reads are used.
- `SharingStarted.WhileSubscribed(5_000)` means the upstream flow is kept alive for
  5 seconds after the last subscriber disappears, avoiding redundant cold starts when
  navigating between settings sub-screens.
- `resetToDefaults()` calls `dataStore.edit { it.clear() }` which is a single atomic
  write — all keys are cleared in one transaction.
