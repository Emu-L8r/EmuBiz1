# ADR-001: Single Source of Truth for State Management

**Status:** Accepted  
**Date:** March 22, 2026  
**Decision Makers:** Development Team  
**Phase:** Phase 1 (Foundation + Baseline)

---

## Context

Bizap v1.0 manages application state across multiple ViewModels, repositories, and UI components. As the app has grown, state management has become fragmented, leading to:

- **State Synchronization Issues:** Dual GUI architecture (Classic/Modern) requires state to be synchronized between two separate UI systems
- **Data Inconsistencies:** Multiple sources of truth for the same data (e.g., invoice lists cached in multiple ViewModels)
- **Difficult Testing:** State scattered across components makes unit testing complex
- **Performance Overhead:** Redundant state updates and unnecessary recompositions
- **Memory Leaks:** StateFlow/LiveData collectors not properly managed

**Current State Management:**
- ViewModels expose StateFlow/LiveData
- Repositories expose Flow<T> for database queries
- UI components maintain local state
- Theme management via ThemeManager (StateFlow)
- Settings via SettingsRepository (DataStore)

**Problems Identified:**
1. Invoice list state duplicated in InvoiceListViewModel and dashboard analytics
2. Customer data cached in multiple ViewModels
3. Business profile loaded separately by different screens
4. Theme state managed separately from other settings
5. Navigation state not persisted

---

## Decision

We will adopt the **Single Source of Truth (SSOT)** principle for all application state in Bizap v1.1.

**Core Principles:**

1. **One Source, Multiple Observers**
   - Each piece of data has exactly ONE authoritative source
   - Other components observe this source via reactive streams (Flow/StateFlow)
   - No local caching unless explicitly required for performance

2. **Repository as SSOT for Domain Data**
   - Repositories own domain data (invoices, customers, business profiles)
   - Room database is the persistent source of truth
   - ViewModels observe repository data, do not cache it

3. **ViewModel as SSOT for UI State**
   - ViewModels own UI-specific state (loading, error, success)
   - UI components observe ViewModel state, do not maintain local state
   - One ViewModel per screen (not shared across screens)

4. **Centralized Settings Management**
   - SettingsRepository is SSOT for all app settings
   - ThemeManager delegates to SettingsRepository
   - All settings persisted in DataStore with `settings_*` prefix

5. **Unidirectional Data Flow**
   - UI → ViewModel (user actions)
   - ViewModel → Repository (data operations)
   - Repository → Database/Network (persistence/fetching)
   - Database → Repository → ViewModel → UI (data updates)

**Architecture:**

```
┌─────────────────────────────────────────────────────────────┐
│                         UI LAYER                            │
│  - Composable Screens (observe state, emit events)         │
│  - NO local state for domain data                          │
│  - Local state only for UI-specific needs (scroll, focus)  │
└─────────────────────────────────────────────────────────────┘
                            ▲ StateFlow<UiState>
                            │ (observe)
                            │
┌─────────────────────────────────────────────────────────────┐
│                      VIEWMODEL LAYER                        │
│  - Owns UI state (Loading, Success, Error)                 │
│  - Transforms domain data → UI models                      │
│  - Handles user actions → repository operations           │
│  - One ViewModel per screen (scoped to navigation)        │
└─────────────────────────────────────────────────────────────┘
                            ▲ Flow<DomainModel>
                            │ (observe)
                            │
┌─────────────────────────────────────────────────────────────┐
│                     REPOSITORY LAYER                        │
│  - SSOT for all domain data                                │
│  - Exposes Flow<T> from database                           │
│  - Handles data operations (CRUD)                          │
│  - Manages network/database sync                           │
└─────────────────────────────────────────────────────────────┘
                            ▲ Flow<Entity>
                            │ (observe)
                            │
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                            │
│  - Room Database (persistent SSOT)                         │
│  - DataStore (settings SSOT)                               │
│  - Network (remote data source)                            │
└─────────────────────────────────────────────────────────────┘
```

---

## Implementation Guidelines

### 1. Repository Implementation

**DO:**
```kotlin
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao
) {
    // SSOT: Database Flow
    fun getInvoices(): Flow<List<Invoice>> = 
        invoiceDao.getAllInvoices()
            .map { entities -> entities.map { it.toDomain() } }
    
    // SSOT: Single invoice by ID
    fun getInvoice(id: Long): Flow<Invoice?> = 
        invoiceDao.getInvoiceById(id)
            .map { it?.toDomain() }
}
```

**DON'T:**
```kotlin
class InvoiceRepository {
    // ❌ Caching in repository violates SSOT
    private val cachedInvoices = MutableStateFlow<List<Invoice>>(emptyList())
    
    // ❌ Multiple sources of truth
    fun getInvoices(): Flow<List<Invoice>> = cachedInvoices.asStateFlow()
}
```

### 2. ViewModel Implementation

**DO:**
```kotlin
@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val repository: InvoiceRepository
) : ViewModel() {
    
    // UI state is SSOT for UI layer
    private val _uiState = MutableStateFlow<InvoiceListUiState>(Loading)
    val uiState: StateFlow<InvoiceListUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            repository.getInvoices()
                .catch { _uiState.value = Error(it) }
                .collect { invoices ->
                    _uiState.value = Success(invoices)
                }
        }
    }
}
```

**DON'T:**
```kotlin
class InvoiceListViewModel {
    // ❌ Local cache duplicates repository data
    private val _invoices = MutableStateFlow<List<Invoice>>(emptyList())
    
    // ❌ Manually managing updates
    fun loadInvoices() {
        repository.getInvoices().collect { _invoices.value = it }
    }
}
```

### 3. UI Implementation

**DO:**
```kotlin
@Composable
fun InvoiceListScreen(viewModel: InvoiceListViewModel = hiltViewModel()) {
    // Observe SSOT
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is Loading -> LoadingIndicator()
        is Success -> InvoiceList(uiState.invoices)
        is Error -> ErrorMessage(uiState.error)
    }
}
```

**DON'T:**
```kotlin
@Composable
fun InvoiceListScreen() {
    // ❌ Local state duplicates ViewModel state
    var invoices by remember { mutableStateOf(emptyList<Invoice>()) }
    
    // ❌ Manual data fetching
    LaunchedEffect(Unit) {
        repository.getInvoices().collect { invoices = it }
    }
}
```

---

## Consequences

### Positive

✅ **Simplified State Management**
- Clear ownership of data
- Easier to reason about data flow
- Reduced state synchronization bugs

✅ **Better Testability**
- Single source to mock in tests
- Predictable state transitions
- Easier to write unit tests

✅ **Improved Performance**
- No redundant state updates
- Reduced memory usage
- Fewer recompositions

✅ **Consistency Across GUIs**
- Classic and Modern GUIs observe same data source
- Theme switching doesn't lose state
- Seamless navigation between GUIs

✅ **Easier Debugging**
- Single place to inspect state
- Clear audit trail of state changes
- Better logging possibilities

### Negative

⚠️ **Initial Refactoring Effort**
- Must migrate existing code to SSOT pattern
- Requires updating ViewModels and UI components
- Testing required to ensure no regressions

⚠️ **Learning Curve**
- Team must understand reactive programming
- Requires discipline to maintain SSOT
- New developers need training

⚠️ **Potential Performance Concerns**
- Database queries may be more frequent (mitigated by Room's caching)
- Need to optimize observer patterns
- May require selective caching for expensive operations

---

## Mitigation Strategies

### 1. Performance Optimization
- Use `distinctUntilChanged()` to prevent redundant emissions
- Leverage Room's built-in caching
- Implement pagination for large lists
- Use `stateIn()` to share Flows across multiple collectors

### 2. Gradual Migration
- **Phase 1:** Document SSOT principle (this ADR)
- **Phase 2:** Migrate core features (invoices, customers)
- **Phase 3:** Migrate settings and theme management
- **Phase 4:** Audit and validate all components

### 3. Team Training
- Code review checklist for SSOT compliance
- Pair programming sessions
- Documentation and examples
- Regular architecture reviews

---

## Examples

### Example 1: Invoice List

**Before (Multiple Sources):**
```kotlin
// Dashboard caches invoice count
class DashboardViewModel {
    private val _invoiceCount = MutableStateFlow(0)
    fun loadInvoiceCount() { /* ... */ }
}

// Invoice list caches full list
class InvoiceListViewModel {
    private val _invoices = MutableStateFlow(emptyList())
    fun loadInvoices() { /* ... */ }
}
```

**After (Single Source):**
```kotlin
// Repository is SSOT
class InvoiceRepository {
    fun getInvoices(): Flow<List<Invoice>> = dao.getAllInvoices()
    fun getInvoiceCount(): Flow<Int> = dao.getInvoiceCount()
}

// ViewModels observe repository
class DashboardViewModel(repository: InvoiceRepository) {
    val invoiceCount = repository.getInvoiceCount()
        .stateIn(viewModelScope, Started.WhileSubscribed(5000), 0)
}

class InvoiceListViewModel(repository: InvoiceRepository) {
    val invoices = repository.getInvoices()
        .stateIn(viewModelScope, Started.WhileSubscribed(5000), emptyList())
}
```

### Example 2: Theme Management

**Before:**
```kotlin
// ThemeManager maintains state
object ThemeManager {
    private val _theme = MutableStateFlow(AppTheme.CLASSIC)
    val theme: StateFlow<AppTheme> = _theme
}

// SettingsViewModel duplicates state
class SettingsViewModel {
    private val _selectedTheme = MutableStateFlow(AppTheme.CLASSIC)
}
```

**After:**
```kotlin
// SettingsRepository is SSOT
class SettingsRepository {
    fun getTheme(): Flow<AppTheme> = dataStore.data.map { it.theme }
    suspend fun setTheme(theme: AppTheme) { /* ... */ }
}

// ThemeManager observes repository
class ThemeManager(repository: SettingsRepository) {
    val theme = repository.getTheme()
        .stateIn(globalScope, Started.Eagerly, AppTheme.CLASSIC)
}

// ViewModels observe repository
class SettingsViewModel(repository: SettingsRepository) {
    val theme = repository.getTheme()
}
```

---

## Compliance Checklist

When implementing a new feature, verify SSOT compliance:

```
[ ] Domain data owned by Repository (not ViewModel)
[ ] Repository exposes Flow<T> from database
[ ] ViewModel transforms repository Flow → UI state
[ ] UI observes ViewModel state (no local caching)
[ ] No duplicate state across components
[ ] State updates propagate through unidirectional flow
[ ] Tests verify single source behavior
```

---

## Related ADRs

- **ADR-002:** Design System Components
- **ADR-003:** Navigation Architecture
- **ADR-004:** ViewModel Scope Per Screen

---

## References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [StateFlow and SharedFlow](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Room Database Observability](https://developer.android.com/training/data-storage/room/async-queries)

---

**Last Updated:** March 22, 2026  
**Next Review:** April 22, 2026
