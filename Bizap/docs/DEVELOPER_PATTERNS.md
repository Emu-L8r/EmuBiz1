# DEVELOPER PATTERNS — Bizap Code Templates & Checklist

**Last Updated:** March 20, 2026  
**Purpose:** Reduce boilerplate ceremony and establish consistent patterns  
**Target Time:** 15 min to add new screen, ~1 hour without this guide

---

## Quick Checklist: Adding a New Screen

Follow this **5-step checklist** to add a new screen in 15 minutes:

- [ ] **Step 1:** Add route to `AppScreen.kt`
- [ ] **Step 2:** Add mapping to `Gui1NavAdapter.kt` (or return `null` if GUI2-only)
- [ ] **Step 3:** Add mapping to `Gui2NavAdapter.kt` (or return `null` if GUI1-only)
- [ ] **Step 4:** Create ViewModel (use template below)
- [ ] **Step 5:** Register in both nav graphs + create UI composable
- [ ] **Bonus:** Add integration test for route

---

## Pattern #1: Unified Navigation Model

### Problem
GUI1 uses `Screen` routes, GUI2 uses `ScreenV2` routes. To add a new screen, must touch multiple route types.

### Solution
Create **one unified route** (`AppScreen`) that adapters translate to GUI-specific routes.

### Example: Adding "Invoice PDF Screen"

#### Step 1: Define AppScreen Route

**File:** `app/src/main/java/com/emul8r/bizap/ui/navigation/unified/AppScreen.kt`

```kotlin
sealed interface AppScreen {
    // Existing routes...
    data class Dashboard(val businessId: Long? = null) : AppScreen
    
    // NEW: Invoice PDF viewer
    data class InvoicePdf(
        val invoiceId: Long,
        val businessId: Long? = null,
        val isQuote: Boolean = false
    ) : AppScreen
}
```

#### Step 2: Map to GUI1 Route

**File:** `app/src/main/java/com/emul8r/bizap/ui/navigation/unified/Gui1NavAdapter.kt`

```kotlin
object Gui1NavAdapter {
    fun toScreen(appScreen: AppScreen): Screen? = when (appScreen) {
        // Existing mappings...
        is AppScreen.Dashboard -> Screen.Dashboard
        
        // NEW: Map to GUI1 route
        is AppScreen.InvoicePdf -> Screen.InvoicePdf(
            invoiceId = appScreen.invoiceId,
            isQuote = appScreen.isQuote
        )
    }
}
```

#### Step 3: Map to GUI2 Route

**File:** `app/src/main/java/com/emul8r/bizap/ui/navigation/unified/Gui2NavAdapter.kt`

```kotlin
object Gui2NavAdapter {
    fun toScreen(appScreen: AppScreen): ScreenV2? = when (appScreen) {
        // Existing mappings...
        is AppScreen.Dashboard -> ScreenV2.Dashboard(appScreen.businessId ?: 0L)
        
        // NEW: Map to GUI2 route (same screen, different data structure)
        is AppScreen.InvoicePdf -> ScreenV2.InvoicePdf(
            businessId = appScreen.businessId ?: 0L,
            invoiceId = appScreen.invoiceId,
            isQuote = appScreen.isQuote
        )
    }
}
```

#### Step 4: Add Tests

**File:** `app/src/test/java/com/emul8r/bizap/ui/navigation/unified/Gui1NavAdapterTest.kt`

```kotlin
class Gui1NavAdapterTest {
    @Test
    fun `InvoicePdf maps to Screen InvoicePdf with correct ids`() {
        assertEquals(
            Screen.InvoicePdf(invoiceId = 42L, isQuote = true),
            Gui1NavAdapter.toScreen(AppScreen.InvoicePdf(invoiceId = 42L, isQuote = true))
        )
    }
}
```

---

## Pattern #2: ViewModel + StateFlow

### Problem
Every ViewModel follows same pattern: load data → emit state → handle errors. Writing from scratch is slow.

### Solution
Use this **canonical template** for all ViewModels.

### Template

```kotlin
package com.emul8r.bizap.ui.invoices

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.UiState
import com.emul8r.bizap.ui.gui2.navigation.ScreenV2  // For GUI2 only
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

// 1. Define UI State sealed class (one per ViewModel)
sealed class InvoiceDetailUiState {
    object Loading : InvoiceDetailUiState()
    data class Success(val invoice: Invoice) : InvoiceDetailUiState()
    data class Error(val message: String) : InvoiceDetailUiState()
}

// 2. Create ViewModel with SavedStateHandle
@HiltViewModel
class InvoiceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,  // Provides route parameters
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    // 3. Extract route parameters (GUI2 example)
    private val route: ScreenV2.InvoiceDetail = savedStateHandle.toRoute()
    val invoiceId: Long = route.invoiceId

    // 4. Create StateFlow with standard pattern
    val uiState: StateFlow<InvoiceDetailUiState> = invoiceRepository
        .observe(invoiceId)
        .map { invoice ->
            InvoiceDetailUiState.Success(invoice)  // ✅ Success path
        }
        .catch { error ->
            emit(InvoiceDetailUiState.Error(error.message ?: "Unknown error"))  // ✅ Error path
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),  // ✅ Resource efficiency
            initialValue = InvoiceDetailUiState.Loading  // ✅ Initial state
        )

    // 5. Add actions if needed
    fun updateInvoice(newData: Invoice) {
        viewModelScope.launch {
            invoiceRepository.save(newData)
        }
    }
}
```

### Key Points
- ✅ Always use `SavedStateHandle` to extract route params (type-safe)
- ✅ Use `catch { emit(...) }` to handle errors
- ✅ Use `SharingStarted.WhileSubscribed()` for resource efficiency
- ✅ Sealed class for UI state (no null states)
- ✅ `@HiltViewModel` for automatic dependency injection

---

## Pattern #3: Repository Interface

### Problem
Every data source (Room, API, cache) needs a matching repository. Easy to forget edge cases.

### Solution
Use **interface-based repositories** with clear contract.

### Template

```kotlin
// 1. Define repository interface (in domain layer)
interface InvoiceRepository {
    /** Observe all invoices for a business */
    fun observeAll(businessId: Long): Flow<Result<List<Invoice>>>
    
    /** Observe single invoice by ID */
    fun observe(invoiceId: Long): Flow<Result<Invoice>>
    
    /** Create new invoice */
    suspend fun create(invoice: Invoice): Result<Invoice>
    
    /** Update existing invoice */
    suspend fun update(invoice: Invoice): Result<Unit>
    
    /** Delete invoice by ID */
    suspend fun delete(invoiceId: Long): Result<Unit>
}

// 2. Implement in data layer
@Singleton
class InvoiceRepositoryImpl @Inject constructor(
    private val dao: InvoiceDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : InvoiceRepository {

    override fun observeAll(businessId: Long): Flow<Result<List<Invoice>>> =
        dao.observeByBusinessId(businessId)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatcher)

    override fun observe(invoiceId: Long): Flow<Result<Invoice>> =
        dao.observe(invoiceId)
            .map { Result.success(it) }
            .catch { emit(Result.failure(it)) }
            .flowOn(dispatcher)

    override suspend fun create(invoice: Invoice): Result<Invoice> = withContext(dispatcher) {
        try {
            dao.insert(invoice)
            Result.success(invoice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Similar for update, delete...
}
```

### Key Points
- ✅ Interface in `domain/` (no Android deps)
- ✅ Implementation in `data/` (Android + Room)
- ✅ Use `Result<T>` for error handling (more functional than exceptions)
- ✅ Use `Flow<Result<T>>` for streams (observables)
- ✅ Use `suspend fun` for one-shot operations

---

## Pattern #4: Room DAO Query Optimization

### Problem
Room DAOs can generate N+1 queries (one query per related item). Slow.

### Solution
Use **@Transaction** and **@Relation** for joins.

### Example: Invoice with Line Items

```kotlin
// 1. Define relationships
data class InvoiceWithItems(
    @Embedded val invoice: Invoice,
    @Relation(
        parentColumn = "id",
        entityColumn = "invoice_id"
    )
    val items: List<InvoiceLineItem>
)

// 2. Query with join
@Dao
interface InvoiceDao {
    
    // ✅ GOOD: One transaction, single query
    @Transaction
    @Query("""
        SELECT i.*, 
               COUNT(li.id) as item_count,
               SUM(li.amount) as total_amount
        FROM invoices i
        LEFT JOIN invoice_line_items li ON i.id = li.invoice_id
        WHERE i.id = :invoiceId
        GROUP BY i.id
    """)
    fun observeWithItems(invoiceId: Long): Flow<InvoiceWithItems>
    
    // ❌ BAD: N+1 queries (one per item)
    @Query("SELECT * FROM invoices WHERE id = :invoiceId")
    fun observeOld(invoiceId: Long): Flow<Invoice>
}
```

### Key Points
- ✅ Use `@Transaction` for multi-step queries
- ✅ Use `@Relation` for Room-generated joins
- ✅ Batch queries with `combine()` if needed
- ✅ Test with actual device/emulator (N+1 hard to spot in tests)

---

## Pattern #5: Sealed Class for Navigation Events

### Problem
ViewModels need to emit navigation events (to parent, to router, etc.). Easy to miss cases.

### Solution
Use **sealed class** for all navigation events.

### Example

```kotlin
sealed class InvoiceDetailViewModel.NavigationEvent {
    data class EditInvoice(val invoiceId: Long) : NavigationEvent()
    data class ViewPdf(val invoiceId: Long, val isQuote: Boolean) : NavigationEvent()
    object Back : NavigationEvent()
    object ShowError : NavigationEvent()
}

// In ViewModel
private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
val navigationEvent = _navigationEvent.asSharedFlow()

fun editInvoice() {
    viewModelScope.launch {
        _navigationEvent.emit(NavigationEvent.EditInvoice(invoiceId))
    }
}

// In UI
LaunchedEffect(Unit) {
    viewModel.navigationEvent.collect { event ->
        when (event) {
            is NavigationEvent.EditInvoice -> navController.navigate(AppScreen.EditInvoice(event.invoiceId))
            is NavigationEvent.ViewPdf -> navController.navigate(AppScreen.InvoicePdf(event.invoiceId, event.isQuote))
            NavigationEvent.Back -> navController.popBackStack()
            NavigationEvent.ShowError -> snackbar.show("Error")
        }
    }
}
```

---

## Pattern #6: Testing ViewModel Logic

### Problem
ViewModels have Hilt dependencies; testing is slow/complex. Hard to isolate logic.

### Solution
Use **mocks** + **test coroutine dispatcher** + **runTest { }**.

### Template

```kotlin
@HiltTest  // Or use @RunWith(RobolectricTestRunner::class)
class InvoiceDetailViewModelTest : BaseUnitTest() {
    
    // 1. Mock dependencies
    private val repositoryMock: InvoiceRepository = mockk()
    
    // 2. Create ViewModel with mocks
    private lateinit var viewModel: InvoiceDetailViewModel

    @Before
    fun setup() {
        val savedStateHandle = SavedStateHandle(mapOf(
            "invoiceId" to 42L
        ))
        viewModel = InvoiceDetailViewModel(savedStateHandle, repositoryMock)
    }

    @Test
    fun `loading state emitted on start`() = runTest {
        // Given
        every { repositoryMock.observe(42L) } returns flowOf(
            Result.success(testInvoice())
        )

        // When
        viewModel.uiState.test {
            // Then
            assertEquals(InvoiceDetailUiState.Loading, awaitItem())
            assertEquals(
                InvoiceDetailUiState.Success(testInvoice()),
                awaitItem()
            )
        }
    }

    @Test
    fun `error state emitted on failure`() = runTest {
        // Given
        every { repositoryMock.observe(42L) } returns flow {
            throw RuntimeException("Network error")
        }

        // When
        viewModel.uiState.test {
            // Then
            assertEquals(InvoiceDetailUiState.Loading, awaitItem())
            val errorState = awaitItem() as InvoiceDetailUiState.Error
            assertTrue(errorState.message.contains("Network error"))
        }
    }

    private fun testInvoice() = Invoice(id = 42L, customerId = 1L, ...)
}
```

### Key Points
- ✅ Use `mockk` for Kotlin mocking
- ✅ Use `runTest { }` for coroutine testing
- ✅ Use `.test { }` for Flow assertions
- ✅ Mock all dependencies; inject via SavedStateHandle/constructor
- ✅ Test success, error, and loading states

---

## Anti-Patterns (Don't Do This)

### ❌ Anti-Pattern 1: Hardcoding Route Types in UI

```kotlin
// BAD
if (selectedGui == GUI1) {
    navController.navigate(Screen.Dashboard)
} else {
    navController.navigate(ScreenV2.Dashboard(businessId = 1L))
}

// GOOD: Use AppScreen + adapters
val appScreen = AppScreen.Dashboard(businessId = 1L)
val guiRoute = when (selectedGui) {
    GUI1 -> Gui1NavAdapter.toScreen(appScreen)
    GUI2 -> Gui2NavAdapter.toScreen(appScreen)
}
if (guiRoute != null) {
    navController.navigate(guiRoute)
}
```

### ❌ Anti-Pattern 2: Mixing UI Logic in ViewModel

```kotlin
// BAD
class MyViewModel : ViewModel() {
    fun showSnackbar(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()  // ❌ Android UI in ViewModel!
    }
}

// GOOD: Emit event, let UI handle it
sealed class MyViewModel.Event {
    data class ShowSnackbar(val message: String) : Event()
}

class MyViewModel : ViewModel() {
    private val _event = MutableSharedFlow<Event>()
    val event = _event.asSharedFlow()

    suspend fun doSomething() {
        _event.emit(Event.ShowSnackbar("Done!"))  // ✅ ViewModel emits, UI handles
    }
}
```

### ❌ Anti-Pattern 3: Direct Context in ViewModel

```kotlin
// BAD
@HiltViewModel
class MyViewModel @Inject constructor(
    private val context: Context  // ❌ Binds ViewModel to Android
) : ViewModel()

// GOOD: Use repository or use case
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository  // ✅ Abstract from Android
) : ViewModel()
```

### ❌ Anti-Pattern 4: Using `ViewModelFactory` (Hilt handles it)

```kotlin
// BAD
class MyViewModelFactory @Inject constructor(
    private val repository: MyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ...
}

// GOOD: Just use @HiltViewModel, Hilt creates it
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel()
```

---

## Checklist: New ViewModel

Before submitting PR, verify:

- [ ] ViewModel has `@HiltViewModel` annotation
- [ ] ViewModel uses `SavedStateHandle` for route params (if needed)
- [ ] ViewModel exposes `val uiState: StateFlow<UiState>`
- [ ] All three states defined: `Loading`, `Success(data)`, `Error(message)`
- [ ] Error handling uses `.catch { emit(...) }`
- [ ] No Android UI code (Toast, Snackbar, context) in ViewModel
- [ ] StateFlow uses `SharingStarted.WhileSubscribed()` (resource efficiency)
- [ ] Unit tests exist and cover happy path + error case
- [ ] No hardcoded business logic in UI layer
- [ ] Compose screen uses `.collectAsStateWithLifecycle()` (not `.collectAsState()`)

---

## Checklist: New Screen

Before submitting PR, verify:

- [ ] Route added to `AppScreen.kt`
- [ ] Mapping added to `Gui1NavAdapter.kt` (return `null` if GUI2-only)
- [ ] Mapping added to `Gui2NavAdapter.kt` (return `null` if GUI1-only)
- [ ] Tests added for adapter round-trips
- [ ] Screen registered in `MainActivity.kt` NavHost (GUI1)
- [ ] Screen registered in `GuiV2NavGraph.kt` NavHost (GUI2)
- [ ] ViewModel created using template above
- [ ] UI composable created
- [ ] Navigation tested: can reach screen and navigate back
- [ ] No crashes when switching GUIs

---

## Quick Reference: Common Tasks

| Task | Time | Steps |
|------|------|-------|
| Add new screen | 15 min | 5-step checklist above |
| Add new field to model | 30 min | Add to model + DAO + Mapper + ViewModel + UI |
| Fix navigation crash | 10 min | Check adapter, check route registration, logcat |
| Add test | 5 min | Use template, run `./gradlew test -k "ClassName"` |
| Build release APK | 3 min | See `/docs/BUILD_GUIDE.md` |

---

## Further Reading

- `/README.md` — Project overview
- `/DECISION_LOG.md` — Why we built it this way
- `/docs/TROUBLESHOOTING.md` — Common issues
- `/docs/NAVIGATION_GUIDE.md` — Deep dive on adapters (planned)

---

**Last Updated:** March 20, 2026  
**Status:** ✅ Complete  
**Maintainer:** EmuBiz Architecture Team  
**Target Audience:** Developers new to Bizap codebase

