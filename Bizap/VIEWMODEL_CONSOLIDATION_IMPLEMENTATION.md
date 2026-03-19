# ✅ VIEWMODEL CONSOLIDATION IMPLEMENTATION - PR #NEXT

**Date:** March 19, 2026  
**Status:** Implementation in progress  
**Objective:** Consolidate Customer ViewModels following Invoice consolidation pattern

---

## 📋 CHANGES IMPLEMENTED

### 1. **CustomerDetailViewModel.kt** ✅
**File:** `app/src/main/java/com/emul8r/bizap/ui/customers/CustomerDetailViewModel.kt`

**Changes:**
- ✅ Added `SavedStateHandle` parameter to constructor
- ✅ Added `SavedStateHandle.toRoute()` for routing support (both GUIs)
- ✅ Extract `customerId` from route during initialization
- ✅ Auto-load customer on init if ID is valid
- ✅ Added comprehensive documentation
- ✅ Maintained all existing error handling and event emission

**Pattern Used:**
```kotlin
@HiltViewModel
class CustomerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,  // NEW
    private val repository: CustomerRepository
) : ViewModel() {
    val customerId: Long = try {
        val route: Screen.CustomerDetail = savedStateHandle.toRoute()  // NEW
        route.customerId
    } catch (e: Exception) {
        0L
    }
    // ... existing code ...
}
```

---

### 2. **BusinessProfileViewModel.kt** ✅
**File:** `app/src/main/java/com/emul8r/bizap/ui/settings/BusinessProfileViewModel.kt`

**Changes:**
- ✅ Added `SavedStateHandle` parameter for consistency (marked as unused)
- ✅ Changed `SharingStarted.WhileSubscribed(5000)` → `SharingStarted.Eagerly`
- ✅ Added comprehensive documentation
- ✅ Added success logging to updateProfile method
- ✅ Maintained all existing functionality and debug features

**Pattern Used:**
```kotlin
@HiltViewModel
class BusinessProfileViewModel @Inject constructor(
    @Suppress("UNUSED_PARAMETER") savedStateHandle: SavedStateHandle,  // NEW
    private val repository: BusinessProfileRepository
) : ViewModel() {
    val profileState: StateFlow<BusinessProfile> = repository.activeProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,  // CHANGED (was WhileSubscribed)
            initialValue = BusinessProfile()
        )
}
```

---

### 3. **CustomerListViewModelNew.kt** ✨ (NEW)
**File:** `app/src/main/java/com/emul8r/bizap/ui/customers/CustomerListViewModelNew.kt`

**Purpose:** Consolidated replacement for both `CustomerListViewModel` (V1) and `CustomerListViewModelV2` (V2)

**Features:**
- ✅ Single `@HiltViewModel` (not V1 + V2)
- ✅ Uses `SavedStateHandle.toRoute()` for both GUIs
- ✅ Single `StateFlow<CustomerListUiState>` (consolidated state)
- ✅ No references to V2 classes
- ✅ `SharingStarted.Eagerly` for consistent state availability
- ✅ Error handling and fallback business ID (default: 1L)

**UI State (Consolidated):**
```kotlin
sealed interface CustomerListUiState {
    object Loading : CustomerListUiState
    data class Error(val message: String) : CustomerListUiState
    data class Success(val customers: List<Customer>) : CustomerListUiState
}
```

---

### 4. **CustomerListScreen.kt** ✅
**File:** `app/src/main/java/com/emul8r/bizap/ui/customers/CustomerListScreen.kt`

**Changes:**
- ✅ Updated `CustomerListScreenV2Content` to use `CustomerListViewModelNew` instead of `CustomerListViewModelV2`
- ✅ Updated all state type references from `CustomerListUiStateV2` to `CustomerListUiState`
- ✅ Removed old imports for V2 classes
- ✅ Removed duplicate `CustomerListV2Content` function definition
- ✅ Consolidated both GUI content functions to share same ViewModel

**Import Changes:**
```kotlin
// BEFORE (V2 imports)
import com.emul8r.bizap.ui.gui2.customers.CustomerListUiStateV2
import com.emul8r.bizap.ui.gui2.customers.CustomerListViewModelV2

// AFTER (Consolidated)
import com.emul8r.bizap.ui.customers.CustomerListUiState
import com.emul8r.bizap.ui.customers.CustomerListViewModelNew
```

---

## ✅ CONSOLIDATION CHECKLIST

### CustomerDetailViewModel
- [x] Single ViewModel (no V1 + V2 duplicates)
- [x] Using SavedStateHandle.toRoute() for both GUIs
- [x] Extract customerId from route
- [x] Auto-load on init
- [x] No references to V2 classes
- [x] Error handling intact
- [x] Event emission intact

### BusinessProfileViewModel
- [x] SavedStateHandle parameter added (for consistency)
- [x] SharingStarted.Eagerly for consistency
- [x] Single state management
- [x] All existing features maintained
- [x] Documentation added

### CustomerListViewModel (New)
- [x] Single consolidated ViewModel
- [x] Using SavedStateHandle.toRoute()
- [x] Single CustomerListUiState (no V2 variant)
- [x] No references to V2 classes
- [x] SharingStarted.Eagerly
- [x] Error handling with fallback businessId

### CustomerListScreen
- [x] References consolidated ViewModel
- [x] Uses consolidated UiState
- [x] No V2 imports
- [x] No duplicate functions
- [x] Both GUIs share same ViewModel

---

## 🔄 MIGRATION PATH

### Old V2 Structure:
```
CustomerListViewModelV2 (in ui.gui2.customers)
├─ SavedStateHandle.toRoute()
├─ StateFlow<CustomerListUiStateV2>
└─ GUI2 specific

+ CustomerViewModel (in ui.customers)
├─ getAllCustomers()
├─ StateFlow<List<Customer>>
└─ GUI1 specific
```

### New Consolidated Structure:
```
CustomerListViewModelNew (in ui.customers)
├─ SavedStateHandle.toRoute() ✅ Both GUIs
├─ StateFlow<CustomerListUiState> ✅ Unified
└─ Works for both GUI1 & GUI2 ✅
```

---

## 📊 FILES AFFECTED

| File | Type | Change |
|------|------|--------|
| CustomerDetailViewModel.kt | Modified | Added SavedStateHandle routing |
| BusinessProfileViewModel.kt | Modified | Added SavedStateHandle + Eagerly |
| CustomerListViewModelNew.kt | New | Consolidated replacement |
| CustomerListScreen.kt | Modified | Updated imports & references |

---

## 🎯 PATTERN ESTABLISHED (Follows Invoice Consolidation)

```kotlin
// Pattern for consolidated ViewModels:

@HiltViewModel
class ConsolidatedViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,                    // NEW
    private val repository: SomeRepository
) : ViewModel() {
    
    // Extract routing parameters
    private val routeParam: Long = try {
        val route: Screen.SomeName = savedStateHandle.toRoute()
        route.paramName
    } catch (e: Exception) {
        defaultValue
    }
    
    // Single UI state (not V1 + V2)
    val uiState: StateFlow<ConsolidatedUiState> = repository
        .getData()
        .map { ConsolidatedUiState.Success(it) }
        .catch { emit(ConsolidatedUiState.Error(it.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,    // IMPORTANT
            initialValue = ConsolidatedUiState.Loading
        )
}
```

---

## ✅ BUILD STATUS

**Pending:** Verification of compilation with all changes

**Next Steps:**
1. Run `./gradlew clean build -x connectedAndroidTest`
2. Verify 0 compilation errors
3. Run tests: `./gradlew test`
4. Commit all changes
5. Create PR

---

## 📝 COMMIT MESSAGE

```
feat: consolidate Customer ViewModels (Phase 3.3 continuation)

✅ Consolidate CustomerDetailViewModel with SavedStateHandle routing
✅ Update BusinessProfileViewModel for consistency  
✅ Create unified CustomerListViewModelNew (replaces V1 + V2)
✅ Update CustomerListScreen to use consolidated ViewModel
✅ Remove deprecated V2 imports and references
✅ Follow proven Invoice consolidation pattern

- Single ViewModel per screen (not V1 + V2 duplicates)
- Using SavedStateHandle.toRoute() for routing (both GUIs)
- Single StateFlow<ConsolidatedUiState> (not separate V2 states)
- SharingStarted.Eagerly for consistent state availability
- No references to V2 classes in screen files

Closes: None (Phase 3.3 continuation)
```

---

## 🚀 SUCCESS CRITERIA

- [x] All ViewModels follow consolidation pattern
- [x] SavedStateHandle used for routing support
- [x] No duplicate V1 + V2 ViewModels
- [x] Single UiState per screen
- [ ] Build successful (0 errors, 0 warnings)
- [ ] All tests passing
- [ ] Git clean commit

---

**Status:** In Progress  
**Estimated Completion:** ~5 minutes (pending build verification)  
**Expected Result:** Phase 3.3 continuation complete, ready for merge

