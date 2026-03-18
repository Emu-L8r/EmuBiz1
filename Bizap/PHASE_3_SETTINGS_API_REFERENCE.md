# Phase 3 Settings – API Reference

## SettingsRepository

```kotlin
interface SettingsRepository {
    val settings: Flow<Settings>

    suspend fun updateThemePreference(preference: ThemePreference)
    suspend fun updateDisplayMode(mode: DisplayMode)
    suspend fun updateUiDensity(density: UiDensity)
    suspend fun updateDefaultInvoiceStatusFilter(status: String)
    suspend fun updateDefaultDaysLookback(days: Int)
    suspend fun updateNotificationsEnabled(enabled: Boolean)
    suspend fun updateEmailNotificationsEnabled(enabled: Boolean)
    suspend fun updateCurrencyCode(code: String)
    suspend fun updateLocaleLanguage(language: String)
    suspend fun updateAutoSyncEnabled(enabled: Boolean)
    suspend fun updateSyncFrequencyMinutes(minutes: Int)
    suspend fun resetToDefaults()
}
```

---

## Settings (data class)

```kotlin
@Serializable
data class Settings(
    val themePreference: ThemePreference = ThemePreference.AUTO,
    val displayMode: DisplayMode = DisplayMode.LIST_VIEW,
    val defaultInvoiceStatusFilter: String = "ALL",
    val defaultDaysLookback: Int = 30,
    val uiDensity: UiDensity = UiDensity.COMFORTABLE,
    val notificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true,
    val currencyCode: String = "USD",
    val localeLanguage: String = "en",
    val autoSyncEnabled: Boolean = true,
    val syncFrequencyMinutes: Int = 15,
    val lastUpdated: Long = 0
)
```

---

## Use Cases

| Use Case | Location | Operator / Method |
|----------|----------|-------------------|
| `GetSettingsUseCase` | `domain/usecase/settings/` | `invoke(): Flow<Settings>` |
| `UpdateThemeUseCase` | `domain/usecase/settings/` | `invoke(ThemePreference)` |
| `UpdateDisplayModeUseCase` | `domain/usecase/settings/` | `invoke(DisplayMode)` |
| `UpdateNotificationSettingsUseCase` | `domain/usecase/settings/` | `setNotificationsEnabled(Boolean)`, `setEmailNotificationsEnabled(Boolean)` |
| `UpdateSyncSettingsUseCase` | `domain/usecase/settings/` | `setAutoSyncEnabled(Boolean)`, `setSyncFrequencyMinutes(Int)` |
| `ResetSettingsToDefaultUseCase` | `domain/usecase/settings/` | `invoke()` |

---

## SettingsViewModel

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(...) : ViewModel() {

    // Aggregate
    val settings: StateFlow<Settings>

    // Derived
    val themePreference: StateFlow<ThemePreference>
    val displayMode: StateFlow<DisplayMode>
    val uiDensity: StateFlow<UiDensity>
    val notificationsEnabled: StateFlow<Boolean>
    val emailNotificationsEnabled: StateFlow<Boolean>
    val autoSyncEnabled: StateFlow<Boolean>
    val syncFrequencyMinutes: StateFlow<Int>

    // Write helpers
    fun setThemePreference(preference: ThemePreference)
    fun setDisplayMode(mode: DisplayMode)
    fun setUiDensity(density: UiDensity)
    fun setNotificationsEnabled(enabled: Boolean)
    fun setEmailNotificationsEnabled(enabled: Boolean)
    fun setAutoSyncEnabled(enabled: Boolean)
    fun setSyncFrequencyMinutes(minutes: Int)
    fun resetToDefaults()
}
```

---

## ThemeProvider

```kotlin
@Composable
fun ThemeProvider(
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
    content: @Composable () -> Unit
)
```

Wraps the entire app composition tree in `MainActivity`.  Reads `ThemePreference` from
`SettingsViewModel` and seed colour from `ThemeViewModel`; passes the combined
`ThemeConfig` to `BizapTheme`.

---

## Navigation Destinations

### GUI 1

```kotlin
// Navigate to the new comprehensive settings screen
navController.navigate(Screen.AppSettings)
```

### GUI 2

```kotlin
// Navigate to the new comprehensive settings screen
navController.navigateToAppSettingsV2(businessId)
```

---

## Enums

```kotlin
enum class ThemePreference { LIGHT, DARK, AUTO }
enum class DisplayMode     { LIST_VIEW, GRID_VIEW, CARD_VIEW }
enum class UiDensity       { COMFORTABLE, COMPACT, RELAXED }
```
