# 🚀 IMMEDIATE ACTION PLAN — Fix Wrapper Component Crashes (50 Minutes)

**Goal:** Restore invoice creation/editing functionality  
**Time Budget:** 50 minutes  
**ROI:** Unblocks 5 features  

---

## STEP 1: Fix LineItemsEditor (20 minutes)

### File: `LineItemsEditor.kt`

**Current Broken Code (Lines 20-42):**
```kotlin
@Composable
fun LineItemsEditor(
    items: List<LineItem>,
    onItemsChange: (List<LineItem>) -> Unit,
    modifier: Modifier = Modifier
) {
    val invoiceViewModel: InvoiceViewModel = hiltViewModel()  // ❌ REMOVE
    val theme = invoiceViewModel.theme.collectAsStateWithLifecycle().value  // ❌ WRONG
```

**Action:**
1. Open file: `app/src/main/java/com/emul8r/bizap/ui/components/LineItemsEditor.kt`
2. Add `theme: AppTheme` parameter
3. Remove ViewModel injection
4. Update component body

---

## STEP 2: Fix CurrencySelector (10 minutes)

**File:** `CurrencySelector.kt`

**Same Fix:**
1. Add `theme: AppTheme` parameter
2. Remove ViewModel injection
3. Pass to Classic/Modern components

---

## STEP 3: Fix InvoiceCustomizationEditor (10 minutes)

**File:** `InvoiceCustomizationEditor.kt`

**Same Fix:**
1. Add `theme: AppTheme` parameter  
2. Remove ViewModel injection
3. Pass to Classic/Modern components

---

## STEP 4: Fix PhotoAttachmentPicker (10 minutes)

**File:** `PhotoAttachmentPicker.kt`

**Same Fix:**
1. Add `theme: AppTheme` parameter
2. Remove ViewModel injection
3. Pass to Classic/Modern components

---

## UPDATE CALL SITES (NOT PART OF THIS FIX - But Document Them)

Once wrappers are fixed, you'll need to pass `theme` where these are called:

**CreateInvoiceScreenV2.kt:**
```kotlin
// Will need to get current theme and pass it
val theme by themeManager.theme.collectAsStateWithLifecycle()
LineItemsEditor(
    items = lineItems,
    onItemsChange = { ... },
    theme = theme,  // ← ADD THIS
    modifier = Modifier
)
```

**EditInvoiceScreenV2.kt:** (same)

---

## VERIFICATION CHECKLIST

After making changes:

```bash
# 1. Build should succeed
./gradlew clean build -x test

# 2. Check for new errors
adb logcat -d -s AndroidRuntime:E | grep -i "ClassCast\|State\|getValue"

# 3. Launch and create invoice
./gradlew installDebug
adb shell am start -n com.emul8r.bizap/.MainActivity

# Then in app:
- Navigate to Invoices
- Click "Create Invoice"
- Should see line items editor (NOT crash)
```

---

## ESTIMATED TIMELINE

| Step | Task | Duration | Cumulative |
|------|------|----------|-----------|
| 1 | LineItemsEditor fix | 20 min | 20 min |
| 2 | CurrencySelector fix | 10 min | 30 min |
| 3 | InvoiceCustomizationEditor fix | 10 min | 40 min |
| 4 | PhotoAttachmentPicker fix | 10 min | 50 min |
| | **TOTAL** | **50 min** | **50 min** |

---

## SUCCESS CRITERIA

✅ Build succeeds (no Kotlin errors)  
✅ No `ClassCastException` in logcat  
✅ CreateInvoiceScreenV2 loads  
✅ LineItemsEditor renders (not crashes)  
✅ Theme switching still works  

---

**Start with Step 1. Come back when ready for code changes.**

