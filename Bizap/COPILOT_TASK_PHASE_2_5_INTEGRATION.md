# 🚀 COPILOT TASK: Phase 2.5 - Integrate Components (InvoiceViewModel + Screens)

## ⚡ QUICK SUMMARY

**Phase 2 Status:** ✅ Components created (15 files)  
**Current Phase:** 2.5 - Integrate components into ViewModels + Screens  
**Goal:** Connect components to business logic  
**Timeline:** 10-15 hours (Week 2 of Phase 2)  
**Result:** Fully functional line items, customization, currency, photos

---

## 📋 WHAT'S ALREADY DONE

✅ **Created 15 component files:**
- LineItem domain model
- LineItemsEditor (wrapper + Classic + Modern)
- InvoiceCustomization domain model
- InvoiceCustomizationEditor (wrapper + Classic + Modern)
- CurrencySelector (wrapper + Classic + Modern)
- PhotoAttachmentPicker (wrapper + Classic + Modern)

**All components are:**
- ✅ Standalone (don't depend on existing code)
- ✅ Tested to compile
- ✅ Theme-aware (work in both Classic and Modern)
- ✅ Ready to integrate

---

## 🎯 INTEGRATION TASKS (Phase 2.5)

### TASK 1: Update Invoice Domain Model (2 hours)

**EDIT:** `app/src/main/java/com/emul8r/bizap/domain/model/Invoice.kt`

Add new fields:
```kotlin
data class Invoice(
    // ...existing fields...
    val lineItems: List<LineItem> = emptyList(),
    val customization: InvoiceCustomization = InvoiceCustomization(),
    val currency: String = "USD",
    val attachmentPhotoUris: List<String> = emptyList()
)
```

**Verify:** Compiles without errors

---

### TASK 2: Update InvoiceViewModel (3-4 hours)

**EDIT:** `app/src/main/java/com/emul8r/bizap/presentation/viewmodel/InvoiceViewModel.kt`

Update UiState:
```kotlin
sealed class InvoiceUiState {
    data class Success(
        val invoice: Invoice,
        val lineItems: List<LineItem> = emptyList(),
        val customization: InvoiceCustomization = InvoiceCustomization(),
        val currency: String = "USD",
        val photos: List<String> = emptyList()
    ) : InvoiceUiState()
    
    // ... other states ...
}
```

Add state management functions:
```kotlin
fun updateLineItems(items: List<LineItem>) {
    val currentState = _uiState.value as? InvoiceUiState.Success ?: return
    _uiState.value = currentState.copy(lineItems = items)
}

fun updateCustomization(customization: InvoiceCustomization) {
    val currentState = _uiState.value as? InvoiceUiState.Success ?: return
    _uiState.value = currentState.copy(customization = customization)
}

fun updateCurrency(currency: String) {
    val currentState = _uiState.value as? InvoiceUiState.Success ?: return
    _uiState.value = currentState.copy(currency = currency)
}

fun addPhoto(uri: String) {
    val currentState = _uiState.value as? InvoiceUiState.Success ?: return
    _uiState.value = currentState.copy(photos = currentState.photos + uri)
}

fun removePhoto(uri: String) {
    val currentState = _uiState.value as? InvoiceUiState.Success ?: return
    _uiState.value = currentState.copy(photos = currentState.photos.filter { it != uri })
}
```

**Verify:** Compiles, state updates correctly

---

### TASK 3: Update CreateInvoiceScreen - Classic (2 hours)

**EDIT:** `app/src/main/java/com/emul8r/bizap/ui/screens/invoice/classic/ClassicCreateInvoiceScreen.kt`

Add components to the screen:
```kotlin
@Composable
fun ClassicCreateInvoiceScreen(
    state: InvoiceUiState.Success,
    viewModel: InvoiceViewModel,
    onNavigate: (AppRoute) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ... existing fields ...
        
        // Line Items
        LineItemsEditor(
            items = state.lineItems,
            onItemsChange = { viewModel.updateLineItems(it) }
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Customization
        InvoiceCustomizationEditor(
            customization = state.customization,
            onCustomizationChange = { viewModel.updateCustomization(it) }
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Currency
        CurrencySelector(
            selectedCurrency = state.currency,
            onCurrencyChange = { viewModel.updateCurrency(it) }
        )
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Photos
        PhotoAttachmentPicker(
            photos = state.photos,
            onPhotosChange = { newPhotos ->
                // Handle photo changes
            }
        )
        
        // ... rest of screen ...
    }
}
```

**Verify:** Screen renders all components, no errors

---

### TASK 4: Update CreateInvoiceScreen - Modern (2 hours)

**EDIT:** `app/src/main/java/com/emul8r/bizap/ui/screens/invoice/modern/ModernCreateInvoiceScreen.kt`

Same structure as Classic, but modern styling (already handled by theme-aware components):
```kotlin
@Composable
fun ModernCreateInvoiceScreen(
    state: InvoiceUiState.Success,
    viewModel: InvoiceViewModel,
    onNavigate: (AppRoute) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)  // Modern: larger padding
    ) {
        // ... existing fields ...
        
        LineItemsEditor(
            items = state.lineItems,
            onItemsChange = { viewModel.updateLineItems(it) }
        )
        
        // ... rest identical to Classic ...
        // (Theme-aware components automatically render Modern style)
    }
}
```

**Verify:** Screen renders with Modern styling, no errors

---

### TASK 5: Update Repository Layer (2-3 hours)

**CREATE:** `app/src/main/java/com/emul8r/bizap/data/local/dao/LineItemDao.kt`

```kotlin
@Dao
interface LineItemDao {
    @Insert
    suspend fun insertLineItem(lineItem: LineItemEntity)
    
    @Query("SELECT * FROM line_items WHERE invoiceId = :invoiceId")
    fun getLineItemsForInvoice(invoiceId: Long): Flow<List<LineItemEntity>>
    
    @Delete
    suspend fun deleteLineItem(lineItem: LineItemEntity)
}
```

**CREATE:** `app/src/main/java/com/emul8r/bizap/data/local/entities/LineItemEntity.kt`

```kotlin
@Entity(tableName = "line_items")
data class LineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val description: String,
    val quantity: Int,
    val unitPrice: Double,
    val notes: String
)
```

**UPDATE:** `app/src/main/java/com/emul8r/bizap/domain/repository/InvoiceRepository.kt`

Add methods for persistence:
```kotlin
suspend fun saveLineItems(invoiceId: Long, items: List<LineItem>)
suspend fun getLineItems(invoiceId: Long): Flow<List<LineItem>>
```

---

### TASK 6: Add Integration Tests (4-6 hours)

**CREATE:** `app/src/test/java/com/emul8r/bizap/ui/components/LineItemsEditorTest.kt`

```kotlin
class LineItemsEditorTest {
    @Test
    fun addLineItem_increasesListSize() = runTest {
        val items = listOf(
            LineItem(1, "Item 1", 1, 100.0)
        )
        var result = items
        
        // Simulate adding item
        result = items + LineItem(2, "Item 2", 2, 200.0)
        
        assertEquals(2, result.size)
    }
    
    @Test
    fun calculateTotal_multiplyQuantityAndPrice() {
        val item = LineItem(1, "Item", 3, 50.0)
        assertEquals(150.0, item.total)
    }
}
```

Repeat for:
- InvoiceCustomizationEditor
- CurrencySelector
- PhotoAttachmentPicker

**CREATE:** `app/src/androidTest/java/com/emul8r/bizap/ui/components/LineItemsEditorScreenTest.kt`

```kotlin
class LineItemsEditorScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun classicLineItemsEditor_renders() {
        composeTestRule.setContent {
            ClassicLineItemsEditor(
                items = emptyList(),
                onItemsChange = {}
            )
        }
        
        composeTestRule.onNodeWithText("Add Item").assertExists()
    }
    
    @Test
    fun modernLineItemsEditor_renders() {
        composeTestRule.setContent {
            ModernLineItemsEditor(
                items = emptyList(),
                onItemsChange = {}
            )
        }
        
        composeTestRule.onNodeWithText("+ Add Item").assertExists()
    }
}
```

---

### TASK 7: Manual Testing (2-3 hours)

**Checklist:**
- [ ] Create invoice with line items (Classic theme)
- [ ] Add/remove line items
- [ ] Verify totals calculated correctly
- [ ] Set invoice customization (header, footer, company)
- [ ] Change currency
- [ ] Add photos (if camera/gallery implemented)
- [ ] Switch to Modern theme (mid-flow)
- [ ] Verify all features still work
- [ ] Switch back to Classic theme
- [ ] Verify data persists across theme switches

---

## ✅ COMPLETION CHECKLIST

### Integration Done?
- [ ] Invoice model updated with new fields
- [ ] InvoiceViewModel updated with state management
- [ ] ClassicCreateInvoiceScreen displays all components
- [ ] ModernCreateInvoiceScreen displays all components
- [ ] Repository layer updated (DAOs, Entities)
- [ ] Integration tests created (15+ tests)
- [ ] Manual testing passed (all features work both themes)
- [ ] Build succeeds: `./gradlew clean build`
- [ ] Tests pass: `./gradlew test`

---

## 🎯 SUCCESS CRITERIA

When Phase 2.5 is complete:
- ✅ All 4 features fully integrated into screens
- ✅ State management working correctly
- ✅ Data persistence working (save/load across sessions)
- ✅ Theme switching works with active data
- ✅ Both Classic and Modern themes display all features
- ✅ Tests passing (1,100+ unit + 20+ integration)

---

## 📊 TIMELINE

- Integration: 10-15 hours
- **Total Phase 2:** ~20-25 hours (1 week)

---

## 🚀 READY FOR INTEGRATION?

Copy this prompt into your IDE Copilot and say:

> "Execute Phase 2.5 integration step-by-step. Start with TASK 1 (Update Invoice Model). After each task, build and verify. When all 7 tasks complete, run tests and commit to git. Let me know when Phase 2 is 100% complete."

---

**Next:** Phase 3 (Clean Architecture - decouple domain layer)

