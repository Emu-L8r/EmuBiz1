# 📚 **PHASE 3 - CODE REFERENCE GUIDE**

---

## **PACKAGE STRUCTURE**

```
com.emul8r.bizap/
├─ data/
│  └─ repository/
│     └─ InvoiceTemplateRepository.kt ✅
│        └── getAllTemplates, getTemplate, createTemplate, updateTemplate, 
│           deleteTemplate, setAsDefault, addCustomField, etc.
│
├─ ui/
│  └─ templates/
│     ├─ InvoiceTemplateViewModel.kt ✅
│     │  └── loadTemplates, deleteTemplate, navigateToCreate, navigateToEdit
│     ├─ TemplateListScreen.kt ✅
│     │  └── Main screen + helper composables (EmptyState, ErrorState, TemplatesList)
│     └─ TemplateListItem.kt ✅
│        └── Single template card with actions
│
└─ test/
   ├─ data/
   │  └─ repository/
   │     └─ InvoiceTemplateRepositoryTest.kt ✅ (15 tests)
   │
   └─ ui/
      └─ templates/
         └─ InvoiceTemplateViewModelTest.kt ✅ (12 tests)
```

---

## **REPOSITORY API**

### **Get Operations**
```kotlin
// Get all active templates for business
suspend fun getAllTemplates(businessProfileId: Long): Result<List<InvoiceTemplate>>

// Get single template
suspend fun getTemplate(templateId: String): Result<InvoiceTemplate?>

// Get template with its custom fields
suspend fun getTemplateWithFields(templateId: String): Result<Pair<InvoiceTemplate?, List<InvoiceCustomField>>>

// Get default template for business
suspend fun getDefaultTemplate(businessProfileId: Long): Result<InvoiceTemplate?>

// Get custom fields for template
suspend fun getCustomFields(templateId: String): Result<List<InvoiceCustomField>>
```

### **Create Operations**
```kotlin
// Create new template (validates max 100 per business)
suspend fun createTemplate(template: InvoiceTemplate): Result<String>

// Add custom field (validates max 50 per template)
suspend fun addCustomField(field: InvoiceCustomField): Result<String>
```

### **Update Operations**
```kotlin
// Update existing template
suspend fun updateTemplate(template: InvoiceTemplate): Result<Unit>

// Update custom field
suspend fun updateCustomField(field: InvoiceCustomField): Result<Unit>

// Set template as default
suspend fun setAsDefault(templateId: String, businessProfileId: Long): Result<Unit>
```

### **Delete Operations**
```kotlin
// Soft delete template (sets isActive = false)
suspend fun deleteTemplate(templateId: String): Result<Unit>

// Soft delete custom field
suspend fun deleteCustomField(fieldId: String): Result<Unit>
```

---

## **VIEWMODEL STATE**

```kotlin
// Observable state
val templates: StateFlow<List<InvoiceTemplate>>
val isLoading: StateFlow<Boolean>
val error: StateFlow<String?>
val selectedTemplate: StateFlow<InvoiceTemplate?>
val navigationEvent: StateFlow<NavigationEvent?>

// Navigation events (sealed class)
sealed class NavigationEvent {
    data class NavigateToCreate(val businessProfileId: Long) : NavigationEvent()
    data class NavigateToEdit(val templateId: String) : NavigationEvent()
}
```

---

## **VIEWMODEL METHODS**

```kotlin
// Data operations
fun loadTemplates(businessProfileId: Long)
fun deleteTemplate(templateId: String)
fun setAsDefault(templateId: String, businessProfileId: Long)

// Navigation
fun navigateToCreate(businessProfileId: Long)
fun navigateToEdit(templateId: String)

// State management
fun clearNavigationEvent()
fun clearError()
fun retryLoadTemplates(businessProfileId: Long)
```

---

## **COMPOSABLE SIGNATURES**

### **Main Screen**
```kotlin
@Composable
fun TemplateListScreen(
    businessProfileId: Long,
    onNavigateToCreate: (Long) -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {},
    viewModel: InvoiceTemplateViewModel = hiltViewModel()
)
```

### **List Item**
```kotlin
@Composable
fun TemplateListItem(
    template: InvoiceTemplate,
    onEdit: (String) -> Unit = {},
    onDelete: (String) -> Unit = {},
    onSetDefault: (String) -> Unit = {}
)
```

---

## **KEY FEATURES**

### **State Management**
```kotlin
// Collect state
val templates by viewModel.templates.collectAsState()
val isLoading by viewModel.isLoading.collectAsState()
val error by viewModel.error.collectAsState()

// Subscribe to navigation events
LaunchedEffect(navigationEvent) {
    when (navigationEvent) {
        is NavigateToCreate -> ...
        is NavigateToEdit -> ...
        null -> {}
    }
}
```

### **Error Handling**
```kotlin
// Repository returns Result<T>
val result = repository.getTemplate(id)
result.onSuccess { template -> ... }
result.onFailure { exception -> ... }

// ViewModel handles errors
viewModel.error // StateFlow<String?>
viewModel.clearError() // Clear error state
viewModel.retryLoadTemplates() // Retry operation
```

### **Constraints**
```kotlin
// Max 100 templates per business
if (count >= 100) return Result.failure(...)

// Max 50 custom fields per template
if (count >= 50) return Result.failure(...)
```

---

## **TESTING PATTERNS**

### **Repository Testing**
```kotlin
@Test
fun testCreateTemplate_Success() = runBlocking {
    // Arrange
    whenever(templateDao.getActiveTemplateCount(1L)).thenReturn(5)
    
    // Act
    val result = repository.createTemplate(template)
    
    // Assert
    assertTrue(result.isSuccess)
}
```

### **ViewModel Testing**
```kotlin
@Test
fun testLoadTemplates_Success() = runTest(testDispatcher) {
    // Arrange
    whenever(repository.getAllTemplates(1L)).thenReturn(
        Result.success(templates)
    )
    
    // Act
    viewModel.loadTemplates(1L)
    testDispatcher.scheduler.advanceUntilIdle()
    
    // Assert
    assertEquals(templates.size, viewModel.templates.value.size)
}
```

---

## **MATERIAL 3 COMPONENTS**

| Component | Usage |
|-----------|-------|
| **Scaffold** | Main layout with TopAppBar + FAB |
| **TopAppBar** | Header with title |
| **FloatingActionButton** | Create template button |
| **Card** | Template list item container |
| **CircularProgressIndicator** | Loading state |
| **Button** | Retry button in error state |
| **OutlinedButton** | "Set Default" action button |
| **IconButton** | Edit/Delete action buttons |
| **AssistChip** | "Default" badge |
| **LazyColumn** | Scrollable template list |

---

## **DEPENDENCY INJECTIONS**

### **Repository**
```kotlin
@Inject constructor(
    private val templateDao: InvoiceTemplateDao,
    private val fieldDao: InvoiceCustomFieldDao
)
```

### **ViewModel**
```kotlin
@HiltViewModel
class InvoiceTemplateViewModel @Inject constructor(
    private val repository: InvoiceTemplateRepository
) : ViewModel()
```

### **UI**
```kotlin
val viewModel: InvoiceTemplateViewModel = hiltViewModel()
```

---

## **LOGGING**

All operations logged with TAG = "InvoiceTemplateRepository" or "InvoiceTemplateViewModel"

```kotlin
// Success
Log.d(TAG, "✅ Loaded ${templates.size} templates")

// Error
Log.e(TAG, "❌ Error loading templates", exception)

// Warning
Log.w(TAG, "⚠️ Template not found: $templateId")
```

---

## **FILE LOCATIONS**

```
Repository:
app/src/main/java/com/emul8r/bizap/data/repository/InvoiceTemplateRepository.kt

ViewModel:
app/src/main/java/com/emul8r/bizap/ui/templates/InvoiceTemplateViewModel.kt

Screens:
app/src/main/java/com/emul8r/bizap/ui/templates/TemplateListScreen.kt
app/src/main/java/com/emul8r/bizap/ui/templates/TemplateListItem.kt

Tests:
app/src/test/java/com/emul8r/bizap/data/repository/InvoiceTemplateRepositoryTest.kt
app/src/test/java/com/emul8r/bizap/ui/templates/InvoiceTemplateViewModelTest.kt
```

---

## **QUICK START**

### **Use in Your Screen**
```kotlin
@Composable
fun MyScreen() {
    val businessId = 1L // Get from context
    
    TemplateListScreen(
        businessProfileId = businessId,
        onNavigateToCreate = { id -> navController.navigate("templates/create/$id") },
        onNavigateToEdit = { id -> navController.navigate("templates/edit/$id") }
    )
}
```

### **Use in Tests**
```kotlin
@Test
fun myTest() = runTest {
    val repo = InvoiceTemplateRepository(mockTemplateDao, mockFieldDao)
    val result = repo.getAllTemplates(1L)
    assertTrue(result.isSuccess)
}
```

---

## ✅ **PHASE 3 REFERENCE COMPLETE**

All code is documented, tested, and ready for use in Phase 4.


