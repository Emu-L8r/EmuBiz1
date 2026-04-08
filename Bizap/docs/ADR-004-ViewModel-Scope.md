# ADR-004: ViewModel Scope Per Screen

**Status:** Accepted  
**Date:** March 22, 2026  
**Decision Makers:** Development Team  
**Phase:** Phase 1 (Foundation + Baseline)

---

## Context

Bizap v1.0 has inconsistent ViewModel scoping patterns, leading to several issues:

**Current ViewModel Problems:**

1. **Shared ViewModels Across Screens**
   - Some ViewModels shared between multiple screens
   - State persists when it shouldn't
   - Memory leaks from unreleased ViewModels
   - Difficult to test in isolation

2. **Activity-Scoped ViewModels**
   - ViewModels survive too long
   - State not cleared between navigation
   - Memory usage grows over time
   - Inappropriate data sharing

3. **Mixed Scoping Strategies**
   - Some screens: destination-scoped
   - Some screens: activity-scoped
   - Some screens: parent-scoped
   - No clear pattern or rationale

4. **Theme Switching Issues**
   - Activity recreation clears all ViewModels
   - Data reloaded unnecessarily
   - Poor user experience

5. **Inconsistent Lifecycle Management**
   - ViewModels not cleared when expected
   - onCleared() called at wrong times
   - Resource cleanup unreliable

**Examples of Issues:**
```kotlin
// Problem 1: Activity-scoped ViewModel shared between unrelated screens
@HiltViewModel
class InvoiceListViewModel @Inject constructor(...) : ViewModel() {
    // Used by both InvoiceListScreen and DashboardScreen
    // State persists when navigating away
}

// Problem 2: Parent navigation scope confusion
val parentEntry = navController.getBackStackEntry(...)
val sharedViewModel: SomeViewModel = hiltViewModel(parentEntry)
// When is this ViewModel cleared? Unclear!

// Problem 3: Manual ViewModel creation (no Hilt)
class CreateInvoiceViewModel(repository: InvoiceRepository) {
    // Not dependency-injected
    // Difficult to test
}
```

---

## Decision

We will adopt a **strict ViewModel scoping policy**: **One ViewModel per screen, scoped to the navigation destination.**

**Core Principles:**

1. **Destination-Scoped ViewModels**
   - Each screen has its own ViewModel
   - ViewModel scoped to navigation destination
   - Cleared when destination removed from back stack
   - No ViewModel sharing between screens (except parent-child)

2. **Hilt Dependency Injection**
   - All ViewModels annotated with `@HiltViewModel`
   - Dependencies injected via `@Inject constructor`
   - Use `hiltViewModel()` in composables
   - No manual ViewModel creation

3. **Single Responsibility**
   - Each ViewModel manages ONE screen's state
   - Clear separation of concerns
   - Easier to test and maintain
   - Predictable lifecycle

4. **Parent-Child Relationship**
   - Child screens can access parent ViewModel (explicitly)
   - Parent cannot access child ViewModels
   - Clear data flow direction
   - Used for shared forms, dialogs

5. **No Activity-Scoped ViewModels**
   - Activity scope reserved for app-level state only
   - Use repositories for cross-screen data
   - State flow through navigation parameters

---

## ViewModel Scoping Architecture

### Scoping Hierarchy

```
┌───────────────────────────────────────────────────────────────┐
│                    Application Scope                          │
│  - App-level state (theme, settings)                          │
│  - Managed by repositories, not ViewModels                    │
│  - Singleton dependencies (Hilt @Singleton)                   │
└───────────────────────────────────────────────────────────────┘
                            │
┌───────────────────────────────────────────────────────────────┐
│                   Navigation Graph Scope                      │
│  - NavController                                              │
│  - Navigation state                                           │
│  - Back stack management                                      │
└───────────────────────────────────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                ▼                       ▼
┌──────────────────────────┐  ┌──────────────────────────┐
│  Destination Scope       │  │  Destination Scope       │
│  (InvoiceListScreen)     │  │  (CustomerListScreen)    │
│                          │  │                          │
│  InvoiceListViewModel    │  │  CustomerListViewModel   │
│  - Scoped to destination │  │  - Scoped to destination │
│  - Cleared when popped   │  │  - Cleared when popped   │
└──────────────────────────┘  └──────────────────────────┘
        │
        └────► Child Destination
               (InvoiceDetailScreen)
               InvoiceDetailViewModel
               - Can access parent ViewModel
               - Scoped to child destination
```

### Lifecycle Flow

```
User navigates to InvoiceListScreen
    ↓
InvoiceListViewModel created (Hilt)
    ↓
Screen rendered, ViewModel.init{} runs
    ↓
User interacts, ViewModel updates state
    ↓
User navigates to InvoiceDetail(id=123)
    ↓
InvoiceDetailViewModel created
    - InvoiceListViewModel STILL ALIVE (parent in back stack)
    ↓
User navigates back
    ↓
InvoiceDetailViewModel.onCleared() called
    - Resources released
    - InvoiceListViewModel STILL ALIVE
    ↓
User navigates to CustomerListScreen
    ↓
InvoiceListViewModel.onCleared() called
    - Removed from back stack
    - Resources released
```

---

## Implementation Guidelines

### 1. ViewModel Definition

**DO:**
```kotlin
@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val invoiceRepository: InvoiceRepository,
    private val savedStateHandle: SavedStateHandle  // For state restoration
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<InvoiceListUiState>(Loading)
    val uiState: StateFlow<InvoiceListUiState> = _uiState.asStateFlow()
    
    init {
        loadInvoices()
    }
    
    private fun loadInvoices() {
        viewModelScope.launch {
            invoiceRepository.getInvoices()
                .catch { e -> _uiState.value = Error(e) }
                .collect { invoices ->
                    _uiState.value = Success(invoices)
                }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Clean up resources if needed
        // (Coroutines cancelled automatically)
    }
}
```

**DON'T:**
```kotlin
// ❌ No @HiltViewModel annotation
class InvoiceListViewModel(repository: InvoiceRepository) : ViewModel()

// ❌ Manual dependency management
class InvoiceListViewModel : ViewModel() {
    private val repository = InvoiceRepository()  // Don't do this!
}

// ❌ Activity-scoped without justification
@HiltViewModel
class SharedViewModel @Inject constructor(...) : ViewModel() {
    // Used by 5 different screens - WRONG!
}
```

### 2. Screen Usage

**DO:**
```kotlin
@Composable
fun InvoiceListScreen(
    viewModel: InvoiceListViewModel = hiltViewModel(),  // Destination-scoped
    onInvoiceClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (uiState) {
        is Loading -> LoadingIndicator()
        is Success -> {
            LazyColumn {
                items(uiState.invoices) { invoice ->
                    InvoiceCard(
                        invoice = invoice,
                        onClick = { onInvoiceClick(invoice.id) }
                    )
                }
            }
        }
        is Error -> ErrorMessage(uiState.error)
    }
}
```

**DON'T:**
```kotlin
// ❌ Activity-scoped ViewModel
@Composable
fun InvoiceListScreen() {
    val activity = LocalContext.current as ComponentActivity
    val viewModel: InvoiceListViewModel = viewModel(activity)  // Wrong scope!
}

// ❌ Manual ViewModel creation
@Composable
fun InvoiceListScreen() {
    val viewModel = remember { InvoiceListViewModel() }  // Not Hilt-injected!
}
```

### 3. Parent-Child ViewModel Access

**DO (When Needed):**
```kotlin
// Parent: Invoice List Screen
composable<Destination.InvoiceList> {
    InvoiceListScreen(
        onCreateClick = {
            navController.navigate(Destination.CreateInvoice)
        }
    )
}

// Child: Create Invoice Dialog (needs to update parent)
composable<Destination.CreateInvoice> { backStackEntry ->
    // Access parent ViewModel explicitly
    val parentEntry = remember(backStackEntry) {
        navController.getBackStackEntry(Destination.InvoiceList)
    }
    val parentViewModel: InvoiceListViewModel = hiltViewModel(parentEntry)
    
    CreateInvoiceScreen(
        onSave = { invoice ->
            // Parent ViewModel handles the update
            parentViewModel.addInvoice(invoice)
            navController.navigateUp()
        },
        onDismiss = { navController.navigateUp() }
    )
}
```

**DON'T:**
```kotlin
// ❌ Implicit parent ViewModel sharing
composable<Destination.CreateInvoice> {
    CreateInvoiceScreen(
        viewModel = hiltViewModel()  // Where is this scoped? Unclear!
    )
}

// ❌ Activity-scoped for communication
val sharedViewModel: SharedViewModel = hiltViewModel(activity)
// Use parent-child relationship or navigation parameters instead
```

### 4. Navigation Parameter Passing

**DO:**
```kotlin
// Pass data via navigation parameters
@Serializable
data class EditInvoice(val invoiceId: Long) : Destination

composable<Destination.EditInvoice> { backStackEntry ->
    val args = backStackEntry.toRoute<Destination.EditInvoice>()
    
    EditInvoiceScreen(
        invoiceId = args.invoiceId,  // Parameter from navigation
        viewModel = hiltViewModel()  // Destination-scoped
    )
}

// ViewModel loads data based on parameter
@HiltViewModel
class EditInvoiceViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: InvoiceRepository
) : ViewModel() {
    
    private val invoiceId: Long = 
        savedStateHandle.get<Long>("invoiceId") ?: error("Missing invoiceId")
    
    init {
        loadInvoice(invoiceId)
    }
}
```

**DON'T:**
```kotlin
// ❌ Shared ViewModel for data passing
val sharedViewModel: DataTransferViewModel = hiltViewModel(activity)
sharedViewModel.selectedInvoiceId = 123
navController.navigate(Destination.EditInvoice)

// Use navigation parameters instead!
```

---

## State Restoration

### SavedStateHandle Usage

```kotlin
@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val repository: InvoiceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // State that survives process death
    var searchQuery: String
        get() = savedStateHandle.get<String>("search_query") ?: ""
        set(value) { savedStateHandle["search_query"] = value }
    
    var selectedFilter: InvoiceFilter
        get() = savedStateHandle.get<InvoiceFilter>("filter") ?: InvoiceFilter.ALL
        set(value) { savedStateHandle["filter"] = value }
    
    // StateFlow that restores from saved state
    private val _scrollPosition = MutableStateFlow(
        savedStateHandle.get<Int>("scroll_position") ?: 0
    )
    val scrollPosition = _scrollPosition.asStateFlow()
    
    fun updateScrollPosition(position: Int) {
        _scrollPosition.value = position
        savedStateHandle["scroll_position"] = position
    }
}
```

### Process Death Survival

```
App in foreground
    ↓
User scrolls to invoice #50
    - ViewModel saves scroll position to SavedStateHandle
    ↓
Android kills app process (low memory)
    - SavedStateHandle persisted by framework
    ↓
User returns to app
    ↓
ViewModel recreated
    - Reads scroll position from SavedStateHandle
    - Restores UI state
    ↓
User sees invoice #50 (state restored!)
```

---

## Common Patterns

### Pattern 1: List Screen ViewModel

```kotlin
@HiltViewModel
class InvoiceListViewModel @Inject constructor(
    private val repository: InvoiceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<InvoiceListUiState>(Loading)
    val uiState: StateFlow<InvoiceListUiState> = _uiState.asStateFlow()
    
    // Filters/search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    
    init {
        loadInvoices()
    }
    
    fun loadInvoices() {
        viewModelScope.launch {
            repository.getInvoices()
                .combine(searchQuery) { invoices, query ->
                    invoices.filter { it.matchesQuery(query) }
                }
                .catch { e -> _uiState.value = Error(e) }
                .collect { invoices ->
                    _uiState.value = Success(invoices)
                }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        savedStateHandle["search_query"] = query
    }
    
    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
            // List updates automatically via Flow
        }
    }
}
```

### Pattern 2: Detail Screen ViewModel

```kotlin
@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    private val repository: InvoiceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val invoiceId: Long = 
        savedStateHandle.get<Long>("invoiceId") 
            ?: throw IllegalArgumentException("invoiceId required")
    
    val uiState: StateFlow<InvoiceDetailUiState> = 
        repository.getInvoice(invoiceId)
            .map { invoice ->
                if (invoice == null) {
                    InvoiceDetailUiState.NotFound
                } else {
                    InvoiceDetailUiState.Success(invoice)
                }
            }
            .catch { e ->
                emit(InvoiceDetailUiState.Error(e))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = InvoiceDetailUiState.Loading
            )
    
    fun deleteInvoice() {
        viewModelScope.launch {
            repository.deleteInvoice(invoiceId)
        }
    }
}
```

### Pattern 3: Form Screen ViewModel

```kotlin
@HiltViewModel
class CreateInvoiceViewModel @Inject constructor(
    private val repository: InvoiceRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    // Form state
    private val _invoiceNumber = MutableStateFlow("")
    val invoiceNumber = _invoiceNumber.asStateFlow()
    
    private val _customerId = MutableStateFlow<Long?>(null)
    val customerId = _customerId.asStateFlow()
    
    private val _lineItems = MutableStateFlow<List<LineItem>>(emptyList())
    val lineItems = _lineItems.asStateFlow()
    
    // Computed state
    val total: StateFlow<Double> = lineItems
        .map { items -> items.sumOf { it.amount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)
    
    // Validation
    val isValid: StateFlow<Boolean> = combine(
        invoiceNumber,
        customerId,
        lineItems
    ) { number, customer, items ->
        number.isNotBlank() && customer != null && items.isNotEmpty()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)
    
    fun updateInvoiceNumber(number: String) {
        _invoiceNumber.value = number
        savedStateHandle["invoice_number"] = number
    }
    
    fun updateCustomer(id: Long) {
        _customerId.value = id
        savedStateHandle["customer_id"] = id
    }
    
    fun addLineItem(item: LineItem) {
        _lineItems.value = _lineItems.value + item
    }
    
    suspend fun saveInvoice(): Result<Long> {
        return try {
            val invoice = Invoice(
                invoiceNumber = invoiceNumber.value,
                customerId = customerId.value ?: return Result.failure(
                    IllegalStateException("Customer required")
                ),
                items = lineItems.value,
                // ... other fields
            )
            val id = repository.createInvoice(invoice)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## Testing Strategy

### ViewModel Unit Tests

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class InvoiceListViewModelTest {
    
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    
    private lateinit var repository: InvoiceRepository
    private lateinit var viewModel: InvoiceListViewModel
    
    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        viewModel = InvoiceListViewModel(
            repository = repository,
            savedStateHandle = SavedStateHandle()
        )
    }
    
    @Test
    fun `initial state is Loading`() = runTest {
        assertEquals(Loading, viewModel.uiState.value)
    }
    
    @Test
    fun `loadInvoices updates state to Success`() = runTest {
        val invoices = listOf(TestDataBuilders.createTestInvoice())
        coEvery { repository.getInvoices() } returns flowOf(invoices)
        
        viewModel.loadInvoices()
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value is Success)
        assertEquals(invoices, (viewModel.uiState.value as Success).invoices)
    }
    
    @Test
    fun `deleteInvoice calls repository`() = runTest {
        viewModel.deleteInvoice(123)
        advanceUntilIdle()
        
        coVerify { repository.deleteInvoice(123) }
    }
}
```

### Screen Integration Tests

```kotlin
@Test
fun `ViewModel survives configuration change`() {
    lateinit var viewModel: InvoiceListViewModel
    
    composeTestRule.setContent {
        viewModel = hiltViewModel()
        InvoiceListScreen(viewModel = viewModel)
    }
    
    // Trigger configuration change
    composeTestRule.activity.recreate()
    
    composeTestRule.setContent {
        val newViewModel: InvoiceListViewModel = hiltViewModel()
        // Same instance due to SavedStateHandle
        assertSame(viewModel, newViewModel)
    }
}
```

---

## Migration Plan

### Phase 1 (Current - Audit)
- ✅ Document ViewModel scoping policy (this ADR)
- Audit all existing ViewModels
- Identify incorrectly scoped ViewModels
- Plan migration priorities

### Phase 2 (Fix Critical Issues)
- Convert activity-scoped ViewModels to destination-scoped
- Add `@HiltViewModel` annotations where missing
- Fix shared ViewModel anti-patterns
- Add SavedStateHandle for state restoration

### Phase 3 (Standardize)
- Ensure one ViewModel per screen
- Update navigation to use proper scoping
- Refactor parent-child relationships
- Add ViewModel lifecycle tests

### Phase 4 (Validation)
- Test ViewModel lifecycle
- Verify state restoration
- Performance validation
- Memory leak detection

---

## Consequences

### Positive

✅ **Predictable Lifecycle**
- Clear when ViewModels are created/destroyed
- No unexpected state persistence
- Easier to debug lifecycle issues

✅ **Better Memory Management**
- ViewModels cleared when not needed
- Reduced memory leaks
- More efficient resource usage

✅ **Easier Testing**
- Each ViewModel testable in isolation
- Mock dependencies via Hilt
- Predictable state transitions

✅ **Clear Responsibilities**
- Each ViewModel owns one screen
- Single Responsibility Principle
- Easier to maintain and extend

✅ **State Restoration**
- SavedStateHandle ensures process death survival
- Better user experience
- Consistent behavior across devices

### Negative

⚠️ **No Cross-Screen State Sharing**
- Must use repositories for shared data
- Navigation parameters for data passing
- Parent-child relationship for forms

⚠️ **More ViewModels**
- One per screen (more classes)
- More dependency injection setup
- Slightly increased complexity

⚠️ **Learning Curve**
- Team must understand scoping
- Proper use of SavedStateHandle
- Parent-child ViewModel patterns

---

## Best Practices

1. **Always use `@HiltViewModel`**
   - Dependency injection for all ViewModels
   - Use `hiltViewModel()` in composables
   - Never manually create ViewModels

2. **One ViewModel per screen**
   - Clear 1:1 mapping
   - Single Responsibility Principle
   - Easier to reason about

3. **Use SavedStateHandle**
   - For state that should survive process death
   - Save scroll position, search queries, filters
   - Restore state in ViewModel init

4. **Parent-child only when needed**
   - For forms that update parent list
   - For dialogs that share state
   - Document the relationship clearly

5. **Pass data via navigation**
   - Use type-safe `Destination` parameters
   - Don't rely on ViewModel sharing
   - Clear, explicit data flow

---

## Related ADRs

- **ADR-001:** Single Source of Truth for State
- **ADR-002:** Design System Components
- **ADR-003:** Navigation Architecture

---

## References

- [Android ViewModel Overview](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Saved State Module for ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel-savedstate)
- [Hilt ViewModel Integration](https://developer.android.com/training/dependency-injection/hilt-jetpack)
- [Navigation and ViewModel](https://developer.android.com/guide/navigation/navigation-programmatic#viewmodel)

---

**Last Updated:** March 22, 2026  
**Next Review:** April 22, 2026
