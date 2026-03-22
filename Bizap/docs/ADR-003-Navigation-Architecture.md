# ADR-003: Navigation Architecture

**Status:** Accepted  
**Date:** March 22, 2026  
**Decision Makers:** Development Team  
**Phase:** Phase 1 (Foundation + Baseline)

---

## Context

Bizap v1.0 has a complex navigation architecture due to its dual GUI system (Classic and Modern themes). This has led to several challenges:

**Current Navigation Issues:**

1. **Dual Navigation Graphs**
   - `NavGraph.kt` for Classic GUI (GUI1)
   - `GuiV2NavGraph.kt` for Modern GUI (GUI2)
   - Duplicate route definitions
   - Inconsistent parameter passing

2. **Multiple Screen Definitions**
   - `Screen.kt` (GUI1 sealed class)
   - `ScreenV2.kt` (GUI2 sealed class with `@Serializable`)
   - `AppScreen.kt` (unified navigation attempt)
   - Adapters: `Gui1NavAdapter.kt`, `Gui2NavAdapter.kt`

3. **State Loss on Theme Switch**
   - Switching themes recreates navigation state
   - User loses their place in the app
   - Back stack is cleared

4. **Complex Deep Linking**
   - Different route formats for each GUI
   - Hard to implement universal deep links
   - Testing is complicated

5. **Navigation Parameter Passing**
   - String-based arguments (GUI1)
   - Type-safe `@Serializable` (GUI2)
   - Inconsistent validation

**Current Architecture:**
```
MainActivity
├── AppStateViewModel (determines GUI1 or GUI2)
├── BizapApp
│   └── NavGraph (GUI1) OR GuiV2NavGraph (GUI2)
│       └── Individual screens
```

---

## Decision

We will adopt a **unified, type-safe navigation architecture** that works seamlessly across both GUIs and enables smooth theme switching without state loss.

**Core Principles:**

1. **Single Navigation Graph**
   - One NavHost for entire app
   - Shared by both GUIs
   - Theme switching doesn't recreate NavHost

2. **Type-Safe Navigation**
   - Use Kotlin serialization for all routes
   - Compile-time route validation
   - No string-based route building

3. **Composable-Based Navigation**
   - Each screen is a composable destination
   - ViewModels scoped to navigation graph
   - Hilt provides dependencies

4. **Persistent Navigation State**
   - Navigation state survives theme changes
   - Back stack preserved across configuration changes
   - Deep links work universally

5. **Centralized Route Definitions**
   - Single source of truth for routes
   - No duplicate screen definitions
   - Clear navigation intent

---

## Navigation Architecture

### Unified Navigation Graph

```
MainActivity
└── ThemeProvider (observes ThemeManager)
    └── BizapApp
        └── NavHost (single, persistent)
            ├── Landing
            ├── Dashboard
            ├── InvoiceList
            ├── InvoiceDetail(id)
            ├── CreateInvoice
            ├── EditInvoice(id)
            ├── CustomerList
            ├── CustomerDetail(id)
            ├── Settings
            └── ...
```

**Key Changes:**
- NavHost is NOT recreated on theme change
- ViewModels survive theme switching
- Navigation state persists
- Back button works consistently

### Route Definitions

```kotlin
// ui/navigation/Destinations.kt
@Serializable
sealed interface Destination {
    
    @Serializable
    data object Landing : Destination
    
    @Serializable
    data object Dashboard : Destination
    
    @Serializable
    data object InvoiceList : Destination
    
    @Serializable
    data class InvoiceDetail(val invoiceId: Long) : Destination
    
    @Serializable
    data object CreateInvoice : Destination
    
    @Serializable
    data class EditInvoice(val invoiceId: Long) : Destination
    
    @Serializable
    data object CustomerList : Destination
    
    @Serializable
    data class CustomerDetail(val customerId: Long) : Destination
    
    @Serializable
    data object Settings : Destination
    
    @Serializable
    data object BusinessProfile : Destination
    
    // Add more destinations as needed
}
```

### Navigation Graph Implementation

```kotlin
// ui/navigation/AppNavGraph.kt
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Destination = Destination.Landing
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Landing
        composable<Destination.Landing> {
            LandingScreen(
                onNavigateToDashboard = {
                    navController.navigate(Destination.Dashboard)
                }
            )
        }
        
        // Dashboard
        composable<Destination.Dashboard> {
            // Theme-aware screen selection
            val theme by ThemeManager.theme.collectAsStateWithLifecycle()
            when (theme) {
                AppTheme.CLASSIC -> DashboardScreen(navController)
                AppTheme.MODERN -> DashboardScreenV2(navController)
            }
        }
        
        // Invoice List
        composable<Destination.InvoiceList> {
            val theme by ThemeManager.theme.collectAsStateWithLifecycle()
            when (theme) {
                AppTheme.CLASSIC -> InvoiceListScreen(
                    onInvoiceClick = { id ->
                        navController.navigate(Destination.InvoiceDetail(id))
                    },
                    onCreateClick = {
                        navController.navigate(Destination.CreateInvoice)
                    }
                )
                AppTheme.MODERN -> InvoiceListScreenV2(
                    onInvoiceClick = { id ->
                        navController.navigate(Destination.InvoiceDetail(id))
                    },
                    onCreateClick = {
                        navController.navigate(Destination.CreateInvoice)
                    }
                )
            }
        }
        
        // Invoice Detail (shared navigation, different UI)
        composable<Destination.InvoiceDetail> { backStackEntry ->
            val args = backStackEntry.toRoute<Destination.InvoiceDetail>()
            val theme by ThemeManager.theme.collectAsStateWithLifecycle()
            
            when (theme) {
                AppTheme.CLASSIC -> InvoiceDetailScreen(
                    invoiceId = args.invoiceId,
                    onEditClick = {
                        navController.navigate(Destination.EditInvoice(args.invoiceId))
                    },
                    onBackClick = { navController.navigateUp() }
                )
                AppTheme.MODERN -> InvoiceDetailScreenV2(
                    invoiceId = args.invoiceId,
                    onEditClick = {
                        navController.navigate(Destination.EditInvoice(args.invoiceId))
                    },
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
        
        // Add more destinations...
    }
}
```

### ViewModel Scoping

```kotlin
// Scope ViewModel to navigation graph (survives theme changes)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: InvoiceDetailViewModel = hiltViewModel()
) {
    // ViewModel is scoped to navigation destination
    // Survives theme switching because navigation graph persists
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }
    
    // UI implementation...
}
```

---

## Implementation Guidelines

### 1. Destination Definition

**DO:**
```kotlin
@Serializable
data class InvoiceDetail(val invoiceId: Long) : Destination

// Usage
navController.navigate(Destination.InvoiceDetail(invoiceId = 123))
```

**DON'T:**
```kotlin
// ❌ String-based routes
navController.navigate("invoice_detail/123")

// ❌ Manual route building
navController.navigate("invoice_detail/${invoiceId}")
```

### 2. Screen Composition

**DO:**
```kotlin
composable<Destination.InvoiceDetail> { backStackEntry ->
    val args = backStackEntry.toRoute<Destination.InvoiceDetail>()
    val theme by ThemeManager.theme.collectAsStateWithLifecycle()
    
    // Theme-aware screen selection
    when (theme) {
        AppTheme.CLASSIC -> InvoiceDetailScreen(args.invoiceId)
        AppTheme.MODERN -> InvoiceDetailScreenV2(args.invoiceId)
    }
}
```

**DON'T:**
```kotlin
// ❌ Hardcoded theme selection
composable("invoice_detail/{id}") {
    InvoiceDetailScreen(...)  // Only works for one theme
}
```

### 3. Navigation Actions

**DO:**
```kotlin
// Type-safe navigation
fun onInvoiceClick(id: Long) {
    navController.navigate(Destination.InvoiceDetail(id))
}

// Named parameters for clarity
navController.navigate(
    Destination.EditInvoice(invoiceId = selectedInvoice.id)
)
```

**DON'T:**
```kotlin
// ❌ String concatenation
navController.navigate("invoice_detail/$id")

// ❌ Magic strings
navController.navigate("invoice_detail?id=$id")
```

### 4. Deep Links

```kotlin
@Serializable
@DeepLink(uriPattern = "bizap://invoice/{invoiceId}")
data class InvoiceDetail(val invoiceId: Long) : Destination

// Automatically handles:
// bizap://invoice/123
// bizap://invoice/456
```

---

## Navigation Patterns

### Pattern 1: List → Detail → Edit

```kotlin
// Invoice List Screen
@Composable
fun InvoiceListScreen(
    onInvoiceClick: (Long) -> Unit,
    onCreateClick: () -> Unit
) {
    LazyColumn {
        items(invoices) { invoice ->
            InvoiceCard(
                invoice = invoice,
                onClick = { onInvoiceClick(invoice.id) }
            )
        }
    }
    
    FloatingActionButton(onClick = onCreateClick) {
        Icon(Icons.Default.Add, contentDescription = "Create")
    }
}

// Navigation Graph
composable<Destination.InvoiceList> {
    InvoiceListScreen(
        onInvoiceClick = { id ->
            navController.navigate(Destination.InvoiceDetail(id))
        },
        onCreateClick = {
            navController.navigate(Destination.CreateInvoice)
        }
    )
}

composable<Destination.InvoiceDetail> { backStackEntry ->
    val args = backStackEntry.toRoute<Destination.InvoiceDetail>()
    InvoiceDetailScreen(
        invoiceId = args.invoiceId,
        onEditClick = {
            navController.navigate(Destination.EditInvoice(args.invoiceId))
        }
    )
}
```

### Pattern 2: Bottom Navigation

```kotlin
@Composable
fun MainScreen(navController: NavHostController) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentDestination is Destination.Dashboard,
                    onClick = { 
                        navController.navigate(Destination.Dashboard) {
                            popUpTo(Destination.Dashboard) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Dashboard") }
                )
                
                NavigationBarItem(
                    selected = currentDestination is Destination.InvoiceList,
                    onClick = {
                        navController.navigate(Destination.InvoiceList) {
                            popUpTo(Destination.Dashboard)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Receipt, null) },
                    label = { Text("Invoices") }
                )
                
                // More nav items...
            }
        }
    ) { paddingValues ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
```

### Pattern 3: Modal Navigation

```kotlin
// For dialogs/bottom sheets
@Composable
fun CreateInvoiceDialog(
    onDismiss: () -> Unit,
    onSave: (Invoice) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        // Dialog content
    }
}

// In navigation graph
composable<Destination.CreateInvoice> {
    val parentEntry = remember(it) {
        navController.getBackStackEntry(Destination.InvoiceList)
    }
    val parentViewModel: InvoiceListViewModel = hiltViewModel(parentEntry)
    
    CreateInvoiceScreen(
        onDismiss = { navController.navigateUp() },
        onSave = { invoice ->
            parentViewModel.addInvoice(invoice)
            navController.navigateUp()
        }
    )
}
```

---

## Theme Switching Without State Loss

### Current Problem
```kotlin
// ❌ Old approach - recreates entire activity
fun switchTheme(newTheme: AppTheme) {
    themeManager.setTheme(newTheme)
    activity.recreate()  // Loses navigation state!
}
```

### Solution
```kotlin
// ✅ New approach - updates theme without recreating activity
@Composable
fun BizapApp() {
    val theme by ThemeManager.theme.collectAsStateWithLifecycle()
    
    BizapTheme(theme = theme) {
        // NavHost is NOT recreated
        val navController = rememberNavController()
        AppNavGraph(navController = navController)
    }
}

// Theme switching
fun switchTheme(newTheme: AppTheme) {
    viewModelScope.launch {
        themeManager.setTheme(newTheme)
        // NavController and back stack preserved!
    }
}
```

**Benefits:**
- User stays on current screen
- Back button still works
- ViewModels retain state
- No jarring UI recreation

---

## Navigation Testing

### Unit Tests
```kotlin
@Test
fun `navigate to invoice detail with correct ID`() = runTest {
    val navController = TestNavHostController(context)
    navController.navigate(Destination.InvoiceDetail(123))
    
    val currentDestination = navController.currentBackStackEntry
        ?.toRoute<Destination.InvoiceDetail>()
    
    assertEquals(123, currentDestination?.invoiceId)
}
```

### Integration Tests
```kotlin
@Test
fun `theme switch preserves navigation state`() {
    composeTestRule.setContent {
        val navController = rememberNavController()
        BizapApp(navController = navController)
    }
    
    // Navigate to invoice detail
    composeTestRule.onNodeWithText("Invoice #123").performClick()
    
    // Switch theme
    composeTestRule.onNodeWithContentDescription("Theme toggle").performClick()
    
    // Verify still on invoice detail screen
    composeTestRule.onNodeWithText("Invoice #123").assertExists()
}
```

---

## Migration Plan

### Phase 1 (Current - Foundation)
- ✅ Document navigation architecture (this ADR)
- ✅ Define `Destination` sealed interface
- ✅ Plan migration strategy

### Phase 2 (Implementation)
- Create unified `AppNavGraph.kt`
- Update all screens to use `Destination` routes
- Implement theme-aware screen selection
- Test navigation with theme switching

### Phase 3 (Cleanup)
- Remove old `Screen.kt` and `ScreenV2.kt`
- Delete navigation adapters
- Remove duplicate route definitions
- Simplify MainActivity

### Phase 4 (Validation)
- Navigation integration tests
- Deep link testing
- Theme switching tests
- Performance validation

---

## Consequences

### Positive

✅ **Type Safety**
- Compile-time route validation
- No runtime navigation errors
- Autocomplete for destinations

✅ **Persistent State**
- Theme switching doesn't lose navigation
- ViewModels survive configuration changes
- Better user experience

✅ **Simplified Codebase**
- Single navigation graph
- No duplicate routes
- Easier to maintain

✅ **Better Testing**
- Easier to test navigation flows
- Type-safe test assertions
- Reliable integration tests

✅ **Deep Links**
- Universal deep link support
- Works for both GUIs
- Simple to implement

### Negative

⚠️ **Migration Effort**
- Must update all navigation code
- Requires thorough testing
- Potential for regressions

⚠️ **Learning Curve**
- Team must learn new navigation API
- Different from Navigation Compose 1.x
- Documentation needed

⚠️ **Dependency on Serialization**
- Requires kotlinx-serialization plugin
- Complex objects need custom serializers
- Bundle size considerations

---

## Best Practices

1. **Always use `Destination` types**
   - Never hardcode route strings
   - Use sealed interface for exhaustive when

2. **Scope ViewModels correctly**
   - Use `hiltViewModel()` for destination-scoped VMs
   - Use parent back stack entry for shared state

3. **Handle back navigation consistently**
   - Use `navController.navigateUp()` for back
   - Set `popUpTo` for bottom nav
   - Clear back stack when appropriate

4. **Test navigation thoroughly**
   - Unit test destination routing
   - Integration test user flows
   - Test theme switching with navigation

5. **Document navigation intent**
   - Clear function names (`onInvoiceClick`, not `onClick`)
   - Document expected navigation behavior
   - Add comments for complex navigation logic

---

## Related ADRs

- **ADR-001:** Single Source of Truth for State
- **ADR-002:** Design System Components
- **ADR-004:** ViewModel Scope Per Screen

---

## References

- [Navigation Compose Type Safety](https://developer.android.com/guide/navigation/design/type-safety)
- [Kotlin Serialization](https://kotlinlang.org/docs/serialization.html)
- [Navigation Testing](https://developer.android.com/guide/navigation/navigation-testing)

---

**Last Updated:** March 22, 2026  
**Next Review:** April 22, 2026
