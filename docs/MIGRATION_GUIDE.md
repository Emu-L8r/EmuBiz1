# GUI2 Migration Guide

## How Users Transition from GUI1 to GUI2

### First Launch
On first app launch (or after a reset), users see the **Landing Screen**:
- **New Experience (GUI2)** — recommended, context-aware, modern architecture
- **Classic Experience (GUI1)** — original app, kept as-is

The choice is persisted in DataStore and remembered across restarts.

---

## Switching Between GUI Versions

### From GUI2 → GUI1
- Tap **"Switch to Classic"** in the GUI2 dashboard top bar.
- The landing screen reappears so the user can confirm their choice.

### From GUI1 → GUI2
- A "Switch UI" option can be added to `SettingsHubScreen` by calling `landingViewModel.resetMode()`.
- This clears the DataStore preference and shows the landing screen again.

---

## Data Compatibility

GUI2 reads the same `invoices` table as GUI1. No data migration is needed:
- All existing invoices are immediately visible in GUI2.
- GUI1 and GUI2 can be used interchangeably — they share the same database.
- There are **no separate snapshot tables** for GUI2 (Option C: direct queries).

---

## Developer Notes

### Adding a New GUI2 Screen
1. Add a route object to `ScreenV2.kt` (include `businessId`).
2. Add a `composable<ScreenV2.YourScreen>` block in `GuiV2NavGraph.kt`.
3. Add a navigation extension in `NavExtensionsV2.kt`.
4. Create the ViewModel (`@HiltViewModel`, use `savedStateHandle.toRoute<ScreenV2.YourScreen>()`).
5. Create the Composable screen.

### Query Conventions
- All `InvoiceDaoV2` queries include a `businessId` parameter.
- Monetary values are always in **cents** (Long).
- Timestamps are stored as epoch milliseconds; use `DATE(ts/1000, 'unixepoch')` in SQL.

### State Management
All GUI2 ViewModels follow this pattern:
```kotlin
val uiState: StateFlow<YourUiState> = repository.observeSomething(businessId)
    .map { data -> YourUiState.Success(data) }
    .catch { emit(YourUiState.Error(it.message ?: "Unknown error")) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), YourUiState.Loading)
```
