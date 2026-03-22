# Prop Drilling Risk Assessment — Sprint 3

## What is Prop Drilling?

When you pass parameters through multiple layers of components just to get it to a leaf component, it's called "prop drilling." Too much prop drilling makes code hard to maintain and understand.

**Safe Zone:** 1-2 parameters  
**Warning Zone:** 3-4 parameters  
**Critical Zone:** 5+ parameters → Should use data class wrapper

---

## Component Audit

### LineItemsEditor
```kotlin
@Composable
fun LineItemsEditor(
    lineItems: List<LineItem>,           // ← Parameter 1 (data)
    onAddItem: () -> Unit,               // ← Parameter 2 (callback)
    onRemoveItem: (Int) -> Unit,         // ← Parameter 3 (callback)
    onUpdateItem: (Int, LineItem) -> Unit,  // ← Parameter 4 (callback)
    isDarkMode: Boolean                  // ← Parameter 5 (theme)
)
```

**Analysis:**
- ⚠️ **Warning Zone** (5 parameters)
- Could be grouped, but each parameter is semantically distinct
- Parameters are: data (1), callbacks (3), theme (1)
- **Recommendation:** OK as-is; consider wrapping if more parameters needed in future

**Potential Grouping (if needed later):**
```kotlin
data class LineItemEditorState(
    val lineItems: List<LineItem>,
    val isDarkMode: Boolean
)

data class LineItemEditorCallbacks(
    val onAddItem: () -> Unit,
    val onRemoveItem: (Int) -> Unit,
    val onUpdateItem: (Int, LineItem) -> Unit
)

@Composable
fun LineItemsEditor(
    state: LineItemEditorState,
    callbacks: LineItemEditorCallbacks
)
```

---

### CurrencySelector
```kotlin
@Composable
fun CurrencySelector(
    selectedCurrency: String,           // ← Parameter 1 (data)
    onCurrencyChange: (String) -> Unit, // ← Parameter 2 (callback)
    isDarkMode: Boolean                 // ← Parameter 3 (theme)
)
```

**Analysis:**
- ✅ **Safe Zone** (3 parameters)
- Very clean interface
- No prop drilling needed
- **Recommendation:** Perfect as-is

---

### CreateInvoiceScreenV2
```kotlin
@Composable
fun CreateInvoiceScreenV2(
    viewModel: CreateInvoiceViewModelV2  // ← 1 parameter (state container)
)
```

Uses ViewModel's StateFlow:
- `uiState.customers`
- `uiState.lineItems`
- `uiState.selectedCurrency`
- `uiState.isDarkMode`
- Callbacks: `onAddLineItem`, `onSaveInvoice`, etc.

**Analysis:**
- ✅ **Safe Zone** (1 parameter)
- All state/callbacks accessed through ViewModel
- No prop drilling to child components
- **Recommendation:** Perfect pattern

---

### ErrorBoundary
```kotlin
@Composable
fun ErrorBoundary(
    onError: ((Exception) -> Unit)? = null,  // ← Parameter 1 (callback)
    content: @Composable () -> Unit          // ← Parameter 2 (content lambda)
)
```

**Analysis:**
- ✅ **Safe Zone** (2 parameters)
- Exactly as designed
- Content lambda is Compose standard pattern
- **Recommendation:** Perfect

---

## Summary

| Component | Parameters | Zone | Status | Action |
|-----------|------------|------|--------|--------|
| LineItemsEditor | 5 | ⚠️ Warning | ✅ Acceptable | None needed |
| CurrencySelector | 3 | ✅ Safe | ✅ Perfect | None needed |
| CreateInvoiceScreenV2 | 1 | ✅ Safe | ✅ Perfect | None needed |
| ErrorBoundary | 2 | ✅ Safe | ✅ Perfect | None needed |

## Recommendations

✅ **Current state is excellent** — no immediate refactoring needed

**Future Prevention:**
1. Keep parameter count < 5 per component
2. Use ViewModel/StateHolder for complex state
3. Group related parameters into data classes if count exceeds 5
4. Test with `@Preview` — if you have too many parameters, preview becomes hard

## Decision Log

**Date:** March 22, 2026  
**Decision:** Keep current structure  
**Rationale:** 
- Parameters are semantically meaningful
- No artificial grouping needed
- Code is readable and testable
- Will reconsider if parameters exceed 6-7 items

**Status:** ✅ Approved

