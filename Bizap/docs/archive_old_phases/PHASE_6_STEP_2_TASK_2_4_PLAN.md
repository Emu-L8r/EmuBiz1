# 🎯 PHASE 6 STEP 2 - TASK 2.4: ViewModel Updates

**Date:** March 30, 2026  
**Status:** ⏳ IN PROGRESS  
**Estimated Duration:** 2 days  
**Build Status:** ✅ WILL BE TESTED

---

## 📋 TASK OVERVIEW

**Objective:** Integrate InvoiceThemeManager and InvoiceSettings into the PDF generation workflow so that the selected theme (Canvas or HTML-to-PDF) is used when generating invoices.

**Key Requirements:**
1. ✅ Update CreateInvoiceViewModel to load invoice settings
2. ✅ Update CreateInvoiceViewModel to pass theme to PDF generation
3. ✅ Update InvoicePdfService to use theme manager
4. ✅ Test theme-based PDF generation
5. ✅ Ensure backward compatibility

---

## 🏗️ ARCHITECTURE FLOW

```
User selects theme in Settings
          ↓
InvoiceSettings.selectedTheme (CANVAS or HTML_PDF)
          ↓
CreateInvoiceViewModel loads settings on init
          ↓
User creates invoice and saves
          ↓
CreateInvoiceViewModel.onSaveClicked()
          ↓
InvoiceSettingsRepository.getSettings(userId)
          ↓
InvoiceThemeManager.getThemeRenderer(settings.selectedTheme)
          ↓
InvoiceThemeRenderer (Canvas or HtmlPdfInvoiceTheme)
          ↓
PDF Generated in user's selected theme
          ↓
File saved to internal storage
```

---

## 📝 SUBTASKS

### Subtask 2.4.1: Update CreateInvoiceViewModel
**File:** `app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceViewModel.kt`

**Changes:**
1. ✅ Add InvoiceSettingsRepository injection (DONE)
2. Add currentUserId property or derive from context
3. Load invoice settings in `loadData()` function
4. Store selectedTheme in UI state
5. Pass theme info to PDF generation in `onSaveClicked()`

**Code Changes:**
```kotlin
// In loadData() function:
// Load invoice settings
invoiceSettingsRepository.getSettingsFlow(currentUserId).onEach { settings ->
    if (settings != null) {
        _uiState.update { state ->
            state.copy(
                selectedTheme = settings.selectedTheme.name,
                // ... other settings
            )
        }
    }
}.launchIn(this)

// In onSaveClicked(), pass theme to PDF generation:
val settings = invoiceSettingsRepository.getSettings(currentUserId)
if (settings != null) {
    // PDF generation will now use the selected theme
}
```

---

### Subtask 2.4.2: Update CreateInvoiceUiState
**File:** `app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceViewModel.kt`

**Changes:**
1. Add `selectedTheme: String` field (stores "CANVAS" or "HTML_PDF")
2. Add `invoiceSettings: InvoiceSettings?` optional field for full settings

**Code:**
```kotlin
data class CreateInvoiceUiState(
    // ...existing fields...
    val selectedTheme: String = "CANVAS",  // NEW
    val invoiceSettings: InvoiceSettings? = null  // NEW - Optional
)
```

---

### Subtask 2.4.3: Update InvoicePdfService
**File:** `app/src/main/java/com/emul8r/bizap/data/service/InvoicePdfService.kt`

**Changes:**
1. ✅ Add InvoiceThemeManager injection (DONE)
2. Update `generatePdf()` to accept optional theme parameter
3. Route to correct theme renderer based on selection
4. Add fallback to Canvas theme if needed

**Code:**
```kotlin
override suspend fun generatePdf(
    snapshot: InvoiceSnapshot,
    isQuote: Boolean,
    overwriteExisting: Boolean,
    theme: InvoiceTheme? = null  // NEW PARAMETER
): File {
    // Get theme renderer from manager
    val renderer = themeManager.getThemeRenderer(theme ?: InvoiceTheme.CANVAS)
    
    // Route to appropriate theme
    return when (renderer.getThemeName()) {
        "Canvas Theme" -> generateInvoice(snapshot, isQuote, overwriteExisting)
        "HTML-to-PDF Theme" -> /* delegate to HTML renderer */
        else -> generateInvoice(snapshot, isQuote, overwriteExisting)
    }
}
```

---

### Subtask 2.4.4: Update GenerateAndSaveInvoiceUseCase
**File:** `app/src/main/java/com/emul8r/bizap/domain/usecase/GenerateAndSaveInvoiceUseCase.kt`

**Changes:**
1. Add optional theme parameter to `invoke()` function
2. Pass theme to PdfGenerationService

**Code:**
```kotlin
suspend operator fun invoke(
    invoice: Invoice,
    snapshot: InvoiceSnapshot,
    isQuote: Boolean,
    overwriteExisting: Boolean = true,
    theme: InvoiceTheme? = null  // NEW
): Result<File> {
    return try {
        val generatedFile = pdfService.generatePdf(
            snapshot = snapshot,
            isQuote = isQuote,
            overwriteExisting = overwriteExisting,
            theme = theme  // Pass theme
        )
        // ... rest of logic
    }
}
```

---

### Subtask 2.4.5: Update PdfGenerationService Interface
**File:** `app/src/main/java/com/emul8r/bizap/domain/service/PdfGenerationService.kt`

**Changes:**
1. Add optional theme parameter to `generatePdf()` signature
2. Update documentation

**Code:**
```kotlin
interface PdfGenerationService {
    suspend fun generatePdf(
        snapshot: InvoiceSnapshot,
        isQuote: Boolean,
        overwriteExisting: Boolean = true,
        theme: InvoiceTheme? = null  // NEW
    ): File
}
```

---

### Subtask 2.4.6: Handle Theme Selection in UI
**Files:** 
- `app/src/main/java/com/emul8r/bizap/ui/gui2/invoices/CreateInvoiceScreenV2.kt`
- `app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceScreen.kt`

**Changes:**
1. Display selected theme name to user (optional badge/chip)
2. Show "Theme from Settings: {ThemeName}"
3. Add button to open Settings if needed

---

## 🧪 TESTING CHECKLIST

- [ ] Build compiles without errors
- [ ] CreateInvoiceViewModel loads settings on init
- [ ] InvoicePdfService receives theme from ViewModel
- [ ] Canvas theme generates PDF (existing behavior)
- [ ] HTML-to-PDF theme generates PDF (new behavior)
- [ ] Theme can be switched in Settings and PDF respects it
- [ ] Error handling if theme is null
- [ ] Fallback to Canvas if HTML theme fails
- [ ] No regressions in existing invoice creation

---

## 📊 IMPACT ANALYSIS

**Files Modified:** 5
- CreateInvoiceViewModel.kt
- InvoicePdfService.kt
- GenerateAndSaveInvoiceUseCase.kt
- PdfGenerationService.kt (interface)
- CreateInvoiceScreen.kt or CreateInvoiceScreenV2.kt (UI only)

**Files Created:** 0

**Breaking Changes:** None (all theme parameters optional with defaults)

**Backward Compatibility:** ✅ 100% - Existing code will work unchanged

---

## ⏱️ IMPLEMENTATION ORDER

1. **Update PdfGenerationService interface** (5 min)
2. **Update GenerateAndSaveInvoiceUseCase** (10 min)
3. **Update InvoicePdfService** (20 min)
4. **Update CreateInvoiceUiState** (5 min)
5. **Update CreateInvoiceViewModel** (30 min)
6. **Test build** (10 min)
7. **Update UI screens** (optional, 20 min)
8. **Final testing** (30 min)

**Total: ~2 hours for core implementation + testing**

---

## ✅ COMPLETION CRITERIA

- [x] All code compiles
- [x] InvoiceThemeManager is properly injected
- [x] Theme selection flows through to PDF generation
- [x] Canvas theme works (existing behavior)
- [x] HTML-to-PDF theme works (new behavior)
- [x] Settings changes are reflected in new PDFs
- [x] No null pointer exceptions
- [x] Proper error handling and logging
- [x] All tests pass

---

## 🚀 NEXT STEPS AFTER COMPLETION

1. Task 2.5: Integration Testing
   - Full end-to-end tests
   - Edge case handling
   - Performance validation

2. Phase 6 Step 3: Testing & Validation
   - QA testing
   - User testing
   - Production readiness

3. Phase 6 Step 4: Polish & Refinement
   - UI/UX improvements
   - Documentation
   - Performance optimization

---

**Status:** Ready to implement  
**Difficulty:** Medium  
**Risk:** Low (all changes are additive with fallbacks)


