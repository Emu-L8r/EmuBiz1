# ✏️ EXACT CODE CHANGES NEEDED — Fix All 4 Wrapper Components

**Time Budget:** 50 minutes  
**Files to Modify:** 4  
**Changes per File:** ~10 lines each  
**Verification:** 5 minutes  

---

## FIX #1: LineItemsEditor.kt (20 minutes)

### Current Code (BROKEN) ❌
```kotlin
@Composable
fun LineItemsEditor(
    items: List<LineItem>,
    onItemsChange: (List<LineItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val invoiceViewModel: InvoiceViewModel = hiltViewModel()  // ❌ WRONG
    val theme = invoiceViewModel.theme.collectAsStateWithLifecycle().value  // ❌ WRONG
    
    when (theme) {
        AppTheme.CLASSIC -> ClassicLineItemsEditor(items, onItemsChange, modifier)
        AppTheme.MODERN -> ModernLineItemsEditor(items, onItemsChange, modifier)
    }
}
```

### Fixed Code (CORRECT) ✅
```kotlin
@Composable
fun LineItemsEditor(
    items: List<LineItem>,
    onItemsChange: (List<LineItem>) -> Unit,
    theme: AppTheme,                    // ✅ ADD THIS PARAMETER
    modifier: Modifier = Modifier
) {
    // ✅ No injection needed, use parameter directly
    when (theme) {
        AppTheme.CLASSIC -> ClassicLineItemsEditor(items, onItemsChange, modifier)
        AppTheme.MODERN -> ModernLineItemsEditor(items, onItemsChange, modifier)
    }
}
```

### Where This Component Is Called (Need to Update)
```kotlin
// In CreateInvoiceScreenV2.kt and EditInvoiceScreenV2.kt
// Find this:
LineItemsEditor(
    items = newItems,
    onItemsChange = { updatedItems -> ... }
)

// Change to:
LineItemsEditor(
    items = newItems,
    onItemsChange = { updatedItems -> ... },
    theme = currentTheme  // ← ADD THIS
)
```

---

## FIX #2: CurrencySelector.kt (10 minutes)

### Current Code (BROKEN) ❌
```kotlin
@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeManager: ThemeManager = hiltViewModel()  // ❌ WRONG
    val theme = themeManager.theme.collectAsStateWithLifecycle().value  // ❌ WRONG
    
    when (theme) {
        AppTheme.CLASSIC -> ClassicCurrencySelector(selectedCurrency, onCurrencyChange, modifier)
        AppTheme.MODERN -> ModernCurrencySelector(selectedCurrency, onCurrencyChange, modifier)
    }
}
```

### Fixed Code (CORRECT) ✅
```kotlin
@Composable
fun CurrencySelector(
    selectedCurrency: String,
    onCurrencyChange: (String) -> Unit,
    theme: AppTheme,                    // ✅ ADD THIS PARAMETER
    modifier: Modifier = Modifier
) {
    // ✅ No injection needed, use parameter directly
    when (theme) {
        AppTheme.CLASSIC -> ClassicCurrencySelector(selectedCurrency, onCurrencyChange, modifier)
        AppTheme.MODERN -> ModernCurrencySelector(selectedCurrency, onCurrencyChange, modifier)
    }
}
```

---

## FIX #3: InvoiceCustomizationEditor.kt (10 minutes)

### Current Code (BROKEN) ❌
```kotlin
@Composable
fun InvoiceCustomizationEditor(
    customization: InvoiceCustomization,
    onCustomizationChange: (InvoiceCustomization) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeManager: ThemeManager = hiltViewModel()  // ❌ WRONG
    val theme = themeManager.theme.collectAsStateWithLifecycle().value  // ❌ WRONG
    
    when (theme) {
        AppTheme.CLASSIC -> ClassicInvoiceCustomizationEditor(customization, onCustomizationChange, modifier)
        AppTheme.MODERN -> ModernInvoiceCustomizationEditor(customization, onCustomizationChange, modifier)
    }
}
```

### Fixed Code (CORRECT) ✅
```kotlin
@Composable
fun InvoiceCustomizationEditor(
    customization: InvoiceCustomization,
    onCustomizationChange: (InvoiceCustomization) -> Unit,
    theme: AppTheme,                    // ✅ ADD THIS PARAMETER
    modifier: Modifier = Modifier
) {
    // ✅ No injection needed, use parameter directly
    when (theme) {
        AppTheme.CLASSIC -> ClassicInvoiceCustomizationEditor(customization, onCustomizationChange, modifier)
        AppTheme.MODERN -> ModernInvoiceCustomizationEditor(customization, onCustomizationChange, modifier)
    }
}
```

---

## FIX #4: PhotoAttachmentPicker.kt (10 minutes)

### Current Code (BROKEN) ❌
```kotlin
@Composable
fun PhotoAttachmentPicker(
    photos: List<String>,
    onPhotosChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeManager: ThemeManager = hiltViewModel()  // ❌ WRONG
    val theme = themeManager.theme.collectAsStateWithLifecycle().value  // ❌ WRONG
    
    when (theme) {
        AppTheme.CLASSIC -> ClassicPhotoAttachmentPicker(photos, onPhotosChange, modifier)
        AppTheme.MODERN -> ModernPhotoAttachmentPicker(photos, onPhotosChange, modifier)
    }
}
```

### Fixed Code (CORRECT) ✅
```kotlin
@Composable
fun PhotoAttachmentPicker(
    photos: List<String>,
    onPhotosChange: (List<String>) -> Unit,
    theme: AppTheme,                    // ✅ ADD THIS PARAMETER
    modifier: Modifier = Modifier
) {
    // ✅ No injection needed, use parameter directly
    when (theme) {
        AppTheme.CLASSIC -> ClassicPhotoAttachmentPicker(photos, onPhotosChange, modifier)
        AppTheme.MODERN -> ModernPhotoAttachmentPicker(photos, onPhotosChange, modifier)
    }
}
```

---

## UPDATE CALL SITES (Screening for uses)

### Where LineItemsEditor is called:
```kotlin
// Search for: LineItemsEditor(
// Expected location: CreateInvoiceScreenV2.kt, EditInvoiceScreenV2.kt
```

### Where CurrencySelector is called:
```kotlin
// Search for: CurrencySelector(
// Expected location: CreateInvoiceScreenV2.kt, EditInvoiceScreenV2.kt
```

### Where InvoiceCustomizationEditor is called:
```kotlin
// Search for: InvoiceCustomizationEditor(
// Expected location: CreateInvoiceScreenV2.kt, EditInvoiceScreenV2.kt
```

### Where PhotoAttachmentPicker is called:
```kotlin
// Search for: PhotoAttachmentPicker(
// Expected location: CreateInvoiceScreenV2.kt, EditInvoiceScreenV2.kt
```

### In CreateInvoiceScreenV2.kt (likely looks like this):
```kotlin
// Add this at the top to get theme
val theme by themeManager.theme.collectAsStateWithLifecycle()

// Then when calling wrappers, add theme parameter
LineItemsEditor(
    items = lineItems,
    onItemsChange = { updateLineItems(it) },
    theme = theme  // ← ADD
)

CurrencySelector(
    selectedCurrency = selectedCurrency,
    onCurrencyChange = { updateCurrency(it) },
    theme = theme  // ← ADD
)

InvoiceCustomizationEditor(
    customization = customization,
    onCustomizationChange = { updateCustomization(it) },
    theme = theme  // ← ADD
)

PhotoAttachmentPicker(
    photos = attachedPhotos,
    onPhotosChange = { updatePhotos(it) },
    theme = theme  // ← ADD
)
```

---

## VERIFICATION STEPS

After making all changes:

```bash
# 1. Build to catch any syntax errors
./gradlew clean build -x test 2>&1 | grep -i "error\|failed"

# 2. Should see: BUILD SUCCESSFUL
# Should NOT see: ClassCastException, Cannot infer type

# 3. Install fresh
./gradlew installDebug

# 4. Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity

# 5. Wait 5 seconds
sleep 5

# 6. Check for crashes
adb logcat -d -s AndroidRuntime:E | head -20

# 7. Should see: (nothing - no crashes)
```

---

## SUCCESS CRITERIA

✅ Build completes without errors  
✅ No `ClassCastException` in logcat  
✅ No `Cannot infer type` errors  
✅ App launches cleanly  
✅ CreateInvoiceScreenV2 loads  
✅ LineItemsEditor renders  
✅ Theme switching still works  
✅ Invoice can be created  

---

## SUMMARY

| File | Changes | Time | Status |
|------|---------|------|--------|
| LineItemsEditor.kt | Add theme param, remove injection | 20 min | - |
| CurrencySelector.kt | Add theme param, remove injection | 10 min | - |
| InvoiceCustomizationEditor.kt | Add theme param, remove injection | 10 min | - |
| PhotoAttachmentPicker.kt | Add theme param, remove injection | 10 min | - |
| Call sites (multiple files) | Pass theme parameter | Included above | - |
| **TOTAL** | | **50 min** | |

---

**Ready to apply fixes? I can make these changes for you, or you can do it manually.**

Which would you prefer?

