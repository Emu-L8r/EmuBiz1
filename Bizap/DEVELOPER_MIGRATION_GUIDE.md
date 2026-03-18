# Developer Migration Guide — Phase 3.3 GUI Consolidation

**Version:** 1.1  
**Date:** 2026-03-18  
**Applicable to:** Bizap Android App

---

## Overview

Phase 3.3 introduced a unified navigation model (`AppScreen`) and a shared `HelpScreen` composable.
This guide explains how to use the new abstractions and how to extend them in future development.

---

## Quick Start

### Using AppScreen for Cross-GUI Navigation

`AppScreen` is a GUI-agnostic sealed interface that represents every navigable destination in the
app. Use it when you need to reference a destination without knowing which GUI is active.

```kotlin
// Build a destination
val destination = AppScreen.CustomerDetail(customerId = 42)

// Translate to GUI1 route
val gui1Route = Gui1NavAdapter.toScreen(destination)
if (gui1Route != null) {
    navController.navigate(gui1Route)
}

// Translate to GUI2 route  
val gui2Route = Gui2NavAdapter.toScreen(destination, fallbackBusinessId = currentBusinessId)
if (gui2Route != null) {
    navController.navigate(gui2Route)
}
```

### Checking if a Destination is GUI-Specific

```kotlin
val isSupportedInGui2 = Gui2NavAdapter.toScreen(AppScreen.PrefilledItems, 1L) != null
// → false: PrefilledItems is GUI1-only
```

---

## Adding a New Shared Screen

When a screen should be available in both GUIs:

1. **Create the composable** in `ui/shared/screens/`:

   ```kotlin
   // ui/shared/screens/MyNewScreen.kt
   @Composable
   fun MyNewScreen(onBack: () -> Unit = {}) {
       // implementation
   }
   ```

2. **Add to AppScreen**:

   ```kotlin
   // ui/navigation/unified/AppScreen.kt
   @Serializable
   data class MyNewScreen(val businessId: Long? = null) : AppScreen
   ```

3. **Map in Gui1NavAdapter**:

   ```kotlin
   // Add to Screen.kt first:
   @Serializable object MyNewScreen : Screen

   // Then in Gui1NavAdapter.toScreen():
   is AppScreen.MyNewScreen -> Screen.MyNewScreen

   // And in Gui1NavAdapter.fromScreen():
   Screen.MyNewScreen -> AppScreen.MyNewScreen()
   ```

4. **Map in Gui2NavAdapter**:

   ```kotlin
   // Add to ScreenV2.kt first:
   @Serializable data class MyNewScreen(val businessId: Long) : ScreenV2

   // Then in Gui2NavAdapter.toScreen():
   is AppScreen.MyNewScreen -> ScreenV2.MyNewScreen(biz(appScreen.businessId))

   // And in Gui2NavAdapter.fromScreen():
   is ScreenV2.MyNewScreen -> AppScreen.MyNewScreen(screen.businessId)
   ```

5. **Register routes** in both navigation graphs:

   ```kotlin
   // GUI1 — MainActivity.kt NavHost:
   composable<Screen.MyNewScreen> {
       MyNewScreen(onBack = { navController.popBackStack() })
   }

   // GUI2 — GuiV2NavGraph.kt NavHost:
   composable<ScreenV2.MyNewScreen> { backStackEntry ->
       val route: ScreenV2.MyNewScreen = backStackEntry.toRoute()
       MyNewScreen(onBack = { navController.popBackStack() })
   }
   ```

6. **Add navigation extensions** in `NavExtensionsV2.kt`:

   ```kotlin
   fun NavHostController.navigateToMyNewScreenV2(businessId: Long) {
       navigate(ScreenV2.MyNewScreen(businessId))
   }
   ```

7. **Write tests** in `Gui1NavAdapterTest.kt` and `Gui2NavAdapterTest.kt`.

---

## Adding a GUI-Specific Screen

For screens that belong to only one GUI:

### GUI1-Only Screen

```kotlin
// 1. Add to Screen.kt
@Serializable object MyGui1Screen : Screen

// 2. Add to AppScreen.kt
@Serializable object MyGui1Screen : AppScreen  // Document as "GUI1 only"

// 3. Map in Gui1NavAdapter
AppScreen.MyGui1Screen -> Screen.MyGui1Screen  // in toScreen()
Screen.MyGui1Screen -> AppScreen.MyGui1Screen  // in fromScreen()

// 4. Return null in Gui2NavAdapter
is AppScreen.MyGui1Screen -> null  // GUI1-only — in toScreen()
// (no fromScreen entry needed)

// 5. Register only in GUI1 navigation graph
```

### GUI2-Only Screen

```kotlin
// 1. Add to ScreenV2.kt
@Serializable data class MyGui2Screen(val businessId: Long) : ScreenV2

// 2. Add to AppScreen.kt
@Serializable data class MyGui2Screen(val businessId: Long? = null) : AppScreen

// 3. Return null in Gui1NavAdapter
is AppScreen.MyGui2Screen -> null  // GUI2-only — in toScreen()

// 4. Map in Gui2NavAdapter
is AppScreen.MyGui2Screen -> ScreenV2.MyGui2Screen(biz(appScreen.businessId))  // toScreen()
is ScreenV2.MyGui2Screen -> AppScreen.MyGui2Screen(screen.businessId)  // fromScreen()

// 5. Register only in GUI2 navigation graph
```

---

## Navigation Graph Overview

```
MainActivity
├── AppState.GUISelection → LandingScreen          (choose GUI)
├── AppState.AppReady(GUI1) → MainScreen
│   └── NavHost (Screen.* routes)
│       ├── Screen.Dashboard      → DashboardScreen
│       ├── Screen.Customers      → CustomerListScreen
│       ├── Screen.Help           → HelpScreen  ← SHARED
│       ├── Screen.AppSettings    → SettingsScreen  ← SHARED
│       ├── Screen.DocumentVault  → DocumentVaultScreen  ← SHARED
│       └── ... (GUI1-specific routes)
│
└── AppState.AppReady(GUI2) → GuiV2NavGraph
    └── NavHost (ScreenV2.* routes)
        ├── ScreenV2.Dashboard    → DashboardScreenV2
        ├── ScreenV2.Customers    → CustomerListScreenV2
        ├── ScreenV2.Help         → HelpScreen  ← SHARED
        ├── ScreenV2.AppSettings  → SettingsScreen  ← SHARED
        ├── ScreenV2.Vault        → DocumentVaultScreen  ← SHARED
        └── ... (GUI2-specific routes)
```

---

## Shared Screen Inventory

| Screen | Package | Used by |
|--------|---------|---------|
| `HelpScreen` | `ui/shared/screens/` | GUI1 (`Screen.Help`), GUI2 (`ScreenV2.Help`) |
| `SettingsScreen` | `presentation/ui/screens/` | GUI1 (`Screen.AppSettings`), GUI2 (`ScreenV2.AppSettings`) |
| `DocumentVaultScreen` | `ui/documents/` | GUI1 (`Screen.DocumentVault`), GUI2 (`ScreenV2.Vault`) |
| `LandingScreen` | `ui/landing/` | Both GUIs (via `AppState.GUISelection`) |

---

## Testing Guidelines

### Adapter Tests

Every `AppScreen` → `Screen` / `ScreenV2` mapping must have a corresponding test:

```kotlin
class Gui1NavAdapterTest {
    @Test
    fun `MyNewScreen maps to Screen MyNewScreen`() {
        assertEquals(Screen.MyNewScreen, Gui1NavAdapter.toScreen(AppScreen.MyNewScreen()))
    }
    
    @Test
    fun `fromScreen MyNewScreen round-trips`() {
        assertEquals(AppScreen.MyNewScreen(), Gui1NavAdapter.fromScreen(Screen.MyNewScreen))
    }
}
```

### GUI-Specific Screen Returns Null Test

```kotlin
@Test
fun `MyGui1Screen returns null in Gui2NavAdapter`() {
    assertNull(Gui2NavAdapter.toScreen(AppScreen.MyGui1Screen, fallbackBusinessId = 1L))
}
```

### Cross-GUI Consistency Tests

For screens available in both GUIs, add a test to `CrossGuiNavigationConsistencyTest`:

```kotlin
@Test
fun `MyNewScreen is reachable in GUI1`() {
    assertNotNull(Gui1NavAdapter.toScreen(AppScreen.MyNewScreen()))
}

@Test
fun `MyNewScreen is reachable in GUI2`() {
    assertNotNull(Gui2NavAdapter.toScreen(AppScreen.MyNewScreen(), bizId))
}

@Test
fun `GUI1 round-trip MyNewScreen preserves destination`() {
    val appScreen = AppScreen.MyNewScreen()
    val route = Gui1NavAdapter.toScreen(appScreen)!!
    assertEquals(appScreen, Gui1NavAdapter.fromScreen(route))
}
```

---

## Frequently Asked Questions

**Q: When should I use AppScreen vs Screen/ScreenV2 directly?**  
A: Use `AppScreen` when writing code that needs to be GUI-agnostic (e.g., deep-link parsing, analytics tracking, notification routing). Use `Screen`/`ScreenV2` directly within GUI-specific nav graphs.

**Q: Does using AppScreen cause any performance overhead?**  
A: No. The adapter `when` expressions are O(1) sealed-class dispatches with no allocation overhead.

**Q: Can I navigate from GUI1 to a GUI2 screen?**  
A: Not directly — you must switch GUIs first via `AppStateViewModel.selectGui(GuiMode.GUI2)`. Cross-GUI navigation is intentionally not supported to avoid inconsistent UX.

**Q: What if Gui1NavAdapter.toScreen() returns null?**  
A: The destination is not available in the active GUI. Show a snackbar or silently ignore the navigation. Do not crash.

---

## Contact

For questions about the consolidation architecture, see `PHASE_3_3_CONSOLIDATION_REPORT.md`
or the GitHub pull request for Phase 3.3.
