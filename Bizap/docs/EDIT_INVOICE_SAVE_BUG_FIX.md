# 🐛 Edit Invoice Save Bug - Root Cause & Fix

**Date:** February 28, 2026  
**Status:** Fixed & Testing  
**Severity:** Critical (P0 - blocks core functionality)

---

## 🔍 PROBLEM DESCRIPTION

**Reported Issue:**
> "Sometimes when I'm on the edit invoice page, if I have two or more line items, I'm unable to save the invoice."

**Actual Issue:**
> The save functionality on EditInvoiceScreen **was never implemented** - it had a literal `TODO` comment instead of actual save logic.

---

## 🎯 ROOT CAUSE ANALYSIS

### Issue #1: Save Button Not Wired Up
**File:** `EditInvoiceScreen.kt` line 110

```kotlin
bottomBar = {
    InvoiceBottomSummary(
        total = invoice.items.sumOf { it.quantity * it.unitPrice },
        isSaving = false,
        onSave = { /* TODO: Save invoice changes */ }  // ❌ DID NOTHING
    )
}
```

**Impact:** Clicking "Save Invoice" had **zero effect** on the database.

---

### Issue #2: No `saveInvoice()` Method
**File:** `EditInvoiceViewModel.kt`

The ViewModel had:
- ✅ `addLineItem()` - worked
- ✅ `removeLineItem()` - worked  
- ✅ `updateLineItem()` - worked
- ✅ `onHeaderChange()`, `onNotesChange()`, etc. - worked
- ❌ **No `saveInvoice()` method** to persist the complete updated invoice

**Why this is critical:** All field updates were tracked in memory (`_editState` flow), but **never written to the database**.

---

### Issue #3: Line Item Update Logic Flaw
**File:** `InvoiceDao.kt` line 30-34

```kotlin
@Transaction
suspend fun insert(invoice: InvoiceEntity, items: List<LineItemEntity>): Long {
    val id = insertInvoice(invoice)
    val itemsWithId = items.map { it.copy(invoiceId = id) }
    insertLineItems(itemsWithId)  // ❌ DOESN'T DELETE OLD ITEMS FIRST
    return id
}
```

**The Problem:**
1. `@Upsert` on line items **doesn't delete removed items**
2. When editing an existing invoice (id = 100):
   - Old line items: [Item A, Item B, Item C]
   - User removes Item B
   - New line items: [Item A, Item C]
   - **But Item B is still in the database!**
3. When user adds items, duplicate ID conflicts occur

**Impact:** Even if the save button worked, line item changes wouldn't persist correctly.

---

## ✅ THE FIX

### Fix #1: Wire Up Save Button
**File:** `EditInvoiceScreen.kt`

**Before:**
```kotlin
onSave = { /* TODO: Save invoice changes */ }
```

**After:**
```kotlin
onSave = { viewModel.saveInvoice() }
```

---

### Fix #2: Implement `saveInvoice()` Method
**File:** `EditInvoiceViewModel.kt`

**Added:**
```kotlin
fun saveInvoice() {
    viewModelScope.launch {
        android.util.Log.d("EditInvoice", "Save button clicked!")
        val state = uiState.first()
        android.util.Log.d("EditInvoice", "Current state: $state")
        
        if (state is EditInvoiceUiState.Success) {
            try {
                val invoiceToSave = _editState.value ?: state.invoice
                android.util.Log.d("EditInvoice", "About to save invoice: id=${invoiceToSave.id}, items=${invoiceToSave.items.size}")
                
                invoiceRepository.saveInvoice(invoiceToSave)
                
                android.util.Log.d("EditInvoice", "Save successful!")
                _editState.update { null } // Clear edit state after successful save
            } catch (e: Exception) {
                android.util.Log.e("EditInvoiceViewModel", "Failed to save invoice: ${e.message}", e)
            }
        } else {
            android.util.Log.e("EditInvoice", "State is not Success, cannot save")
        }
    }
}
```

**Key Features:**
- ✅ Gets current edited invoice from `_editState` (or falls back to original)
- ✅ Calls `invoiceRepository.saveInvoice()`
- ✅ Clears edit state after successful save
- ✅ Comprehensive debug logging
- ✅ Error handling with log output

---

### Fix #3: Delete Old Line Items Before Update
**File:** `InvoiceDao.kt`

**Before:**
```kotlin
@Transaction
suspend fun insert(invoice: InvoiceEntity, items: List<LineItemEntity>): Long {
    val id = insertInvoice(invoice)
    val itemsWithId = items.map { it.copy(invoiceId = id) }
    insertLineItems(itemsWithId)
    return id
}
```

**After:**
```kotlin
@Transaction
suspend fun insert(invoice: InvoiceEntity, items: List<LineItemEntity>): Long {
    val id = insertInvoice(invoice)
    
    // If this is an update (invoice has existing ID), delete old line items first
    if (invoice.id != 0L) {
        deleteLineItems(invoice.id)
    }
    
    val itemsWithId = items.map { it.copy(invoiceId = id) }
    insertLineItems(itemsWithId)
    return id
}
```

**Why This Works:**
1. **New invoice** (id = 0): Skips deletion, inserts fresh items ✅
2. **Existing invoice** (id > 0): Deletes all old items, then inserts current items ✅
3. **Removed items:** Actually deleted from database ✅
4. **Added items:** No duplicate ID conflicts ✅

---

## 🧪 TESTING PROTOCOL

Run all 3 test cases again:

### Test Case 1: Edit existing invoice with 2+ line items
```
1. Open existing invoice (e.g., Invoice #5)
2. Add new line item → "Widget X" / Qty: 3 / Price: $25
3. Click "Save Invoice"
4. Navigate away (go back to invoice list)
5. Reopen Invoice #5
6. VERIFY: "Widget X" line item is still present ✅
```

**Expected Logcat Output:**
```
D/EditInvoice: Save button clicked!
D/EditInvoice: Current state: Success(invoice=...)
D/EditInvoice: About to save invoice: id=5, items=3
D/EditInvoice: Save successful!
```

---

### Test Case 2: Remove a line item
```
1. Open invoice with 3 line items
2. Click "Remove" on the second item
3. Click "Save Invoice"
4. Navigate away and reopen
5. VERIFY: Only 2 items remain ✅
6. VERIFY: The removed item is GONE (not just hidden) ✅
```

**Expected Database State:**
- Before: line_items table has 3 rows with invoiceId=5
- After: line_items table has 2 rows with invoiceId=5

---

### Test Case 3: Edit invoice fields
```
1. Open existing invoice
2. Change header → "REVISED INVOICE"
3. Change notes → "Updated: Customer requested changes"
4. Change line item description → "Modified Widget"
5. Click "Save Invoice"
6. Navigate away and reopen
7. VERIFY: All changes persisted ✅
```

---

## 📊 DEBUG LOGGING GUIDE

### Where to Look in Logcat

**Filter by:**
```
adb logcat | Select-String "EditInvoice|InvoiceDao|saveInvoice"
```

**Expected Flow:**
```
D/EditInvoice: Save button clicked!
D/EditInvoice: Current state: Success(invoice=Invoice(id=5, ...))
D/EditInvoice: About to save invoice: id=5, items=3
D/InvoiceRepositoryImpl: saveInvoice() called with id=5
D/InvoiceDao: insert() - invoice.id=5 (existing invoice)
D/InvoiceDao: Deleting old line items for invoiceId=5
D/InvoiceDao: Inserting 3 new line items
D/EditInvoice: Save successful!
```

**If you see errors:**
```
E/EditInvoiceViewModel: Failed to save invoice: <error message>
E/InvoiceDao: <SQL error or constraint violation>
```

---

## ⚠️ POTENTIAL REMAINING ISSUES

### Issue: Save Successful but Data Not Updating
**Symptom:** Logcat shows "Save successful!" but reopening the invoice shows old data

**Cause:** `_editState` is null when save is called (changes not tracked properly)

**Debug:**
Add this to `updateLineItem()`:
```kotlin
android.util.Log.d("EditInvoice", "Line item updated: id=$id, desc=$description")
android.util.Log.d("EditInvoice", "Current _editState: ${_editState.value}")
```

---

### Issue: Transaction Rollback
**Symptom:** No errors logged, but database doesn't update

**Cause:** Room transaction fails silently (foreign key constraint, etc.)

**Debug:**
```kotlin
// In InvoiceDao.kt
@Transaction
suspend fun insert(invoice: InvoiceEntity, items: List<LineItemEntity>): Long {
    android.util.Log.d("InvoiceDao", "Starting transaction for invoice id=${invoice.id}")
    val id = insertInvoice(invoice)
    android.util.Log.d("InvoiceDao", "Invoice inserted/updated, id=$id")
    
    if (invoice.id != 0L) {
        android.util.Log.d("InvoiceDao", "Deleting old line items for invoice $id")
        deleteLineItems(invoice.id)
    }
    
    val itemsWithId = items.map { it.copy(invoiceId = id) }
    android.util.Log.d("InvoiceDao", "Inserting ${itemsWithId.size} line items")
    insertLineItems(itemsWithId)
    
    android.util.Log.d("InvoiceDao", "Transaction complete!")
    return id
}
```

---

## 🚀 DEPLOYMENT CHECKLIST

- [x] ✅ Fix #1: Wire up save button in EditInvoiceScreen
- [x] ✅ Fix #2: Implement saveInvoice() in EditInvoiceViewModel  
- [x] ✅ Fix #3: Delete old line items before updating
- [ ] ⏳ Build successful
- [ ] ⏳ APK installed on emulator
- [ ] ⏳ Test Case 1 passed (add line item)
- [ ] ⏳ Test Case 2 passed (remove line item)
- [ ] ⏳ Test Case 3 passed (edit fields)

---

## 📝 NOTES

### Why User Thought It Was Line-Item-Specific

The user reported: *"sometimes when I have two or more line items I'm unable to save"*

**Why they thought this:**
- Creating NEW invoices worked (different ViewModel, working save logic)
- Editing EXISTING invoices never worked (regardless of line item count)
- User happened to test with 2+ line items and made the connection

**Actual issue:** Save button was never implemented - had nothing to do with line item count.

---

## 🔄 NEXT STEPS AFTER FIX

Once all 3 test cases pass:

1. ✅ Remove debug logging (or set to verbose level only)
2. ✅ Add user feedback (Toast/Snackbar: "Invoice saved successfully")
3. ✅ Add loading state (disable save button while saving)
4. ✅ Navigate back to invoice list after save
5. ✅ Consider auto-save on field changes (like Google Docs)

---

## 📦 FILES MODIFIED

1. ✅ `EditInvoiceScreen.kt` - Wired up save button
2. ✅ `EditInvoiceViewModel.kt` - Implemented saveInvoice() with logging
3. ✅ `InvoiceDao.kt` - Fixed line item update logic

---

## ✅ VERIFICATION COMMAND

Run this after deployment:
```powershell
$adb = "C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Clear logcat
& $adb logcat -c

# Launch app
& $adb shell am start -n com.emul8r.bizap/.MainActivity

# Monitor saves
& $adb logcat | Select-String "EditInvoice"
```

Then:
1. Navigate to Invoices → Click existing invoice
2. Edit any field or line item
3. Click "Save Invoice"
4. Check logcat for "Save successful!" message

---

**Status:** Awaiting test results from user. If all 3 test cases pass, the bug is fully resolved. ✅

