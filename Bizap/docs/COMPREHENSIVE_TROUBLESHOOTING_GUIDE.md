# 🔧 BIZAP COMPREHENSIVE TROUBLESHOOTING & DIAGNOSTIC GUIDE

**Date:** March 4, 2026  
**Project:** Bizap v0.1.0 (Android Invoicing App)  
**Kotlin + Jetpack Compose + Room Database  v24 + Hilt DI**

---

## SECTION 1: ARCHITECTURE OVERVIEW

### Core Technology Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room (SQLite) - v24
- **DI:** Hilt
- **State Management:** Kotlin StateFlow + MutableStateFlow
- **Async:** Coroutines

### Data Flow Architecture

```
UI Layer (Compose)
    ↓
ViewModel (State Management)
    ↓
UseCase (Business Logic)
    ↓
Repository (Data Abstraction)
    ↓
DAO (Database Access)
    ↓
Room Database (SQLite)
```

### Key Entities & Their Type Systems

| Entity | Location | Monetary Fields | Purpose |
|--------|----------|-----------------|---------|
| `LineItemEntity` | Data layer | `quantity: Double` (REAL), `unitPrice: Long` (INTEGER) | Invoice line items in database |
| `LineItemForm` | UI layer | `quantity: Double`, `unitPrice: Long` | UI form state |
| `LineItem` | Domain layer | `quantity: Double`, `unitPrice: Long` | Business logic model |
| `InvoiceEntity` | Data layer | `totalAmount: Long` (INTEGER) | Invoice in database |
| `Invoice` | Domain layer | `totalAmount: Long` | Business logic model |
| `InvoicePaymentEntity` | Data layer | `amountPaid: Long` (INTEGER - migrated v23→24) | Payment records |

**Critical Design Principle:**
- All monetary amounts stored as **Long (cents)** in database
- All calculations done in cents to avoid floating-point errors
- Display formatting via `CentsFormatter.formatCents(Long, currencyCode)`

---

## SECTION 2: CRITICAL BUG #1 - NULL ID COLLISION IN LINE ITEM UPDATES

### Bug Details

**Symptom:** Editing one line item updates ALL new line items with identical values

**Root Cause:** Using `item.id` (null for new items) as match key instead of `item.transientId` (unique UUID)

### Where It Happens

**File:** `CreateInvoiceViewModel.kt` line 130
```kotlin
fun updateLineItem(id: Long?, description: String, quantity: Double, unitPrice: Long) {
    _uiState.update { state ->
        state.copy(items = state.items.map {
            if (it.id == id) it.copy(...)  // ← PROBLEM: When id=null, ALL items match!
        })
    }
}
```

**Related File:** `EditInvoiceViewModel.kt` line 108 (same issue)

### Why It Happens

**Data Model Issue:**
```kotlin
data class LineItemForm(
    val id: Long? = null,              // NULL for unsaved items
    val transientId: UUID = UUID.randomUUID(),  // UNIQUE - but ignored!
    val description: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Long = 0
)
```

**Compose-ViewModel Disconnect:**
- Compose list uses correct key: `key = { it.transientId.toString() }`
- ViewModel uses wrong identifier: `onUpdate = { ... updateLineItem(item.id, ...) }`
- No connection between the two!

### How to Diagnose

1. **Check ViewModel logs:** Add this to `updateLineItem()`:
```kotlin
Timber.d("updateLineItem called: id=$id, desc=$description, qty=$quantity, price=$unitPrice")
Timber.d("Before update: items=${state.items.map { "${it.transientId}:${it.description}" }}")
```

2. **Check UI state:** Add this to `LineItemEditor` composable:
```kotlin
Timber.d("LineItemEditor rendered: transientId=${item.transientId}, id=${item.id}, desc=${item.description}")
```

3. **Expected vs Actual:**
   - **Expected:** Only the item being edited changes
   - **Actual:** All items with `id == null` get the same values

### The Fix (Future)

**Option A: Use transientId for matching**
```kotlin
fun updateLineItem(transientId: UUID, description: String, quantity: Double, unitPrice: Long) {
    _uiState.update { state ->
        state.copy(items = state.items.map {
            if (it.transientId == transientId) it.copy(...) else it
        })
    }
}
```

**Option B: Use index-based updates**
```kotlin
fun updateLineItem(index: Int, description: String, quantity: Double, unitPrice: Long) {
    _uiState.update { state ->
        state.copy(items = state.items.mapIndexed { i, item ->
            if (i == index) item.copy(...) else item
        })
    }
}
```

**Option C: Pass entire item and compare all fields**
```kotlin
fun updateLineItem(oldItem: LineItemForm, description: String, quantity: Double, unitPrice: Long) {
    _uiState.update { state ->
        state.copy(items = state.items.map {
            if (it.transientId == oldItem.transientId) it.copy(...) else it
        })
    }
}
```

**Recommended:** Option A (use transientId)

---

## SECTION 3: CRITICAL BUG #2 - TYPE MISMATCH ERROR "f != java.lang.Long"

### Bug Details

**Symptom:** Runtime error when saving invoice after editing line items

**Error Message:** `f != java.lang.Long` (Float provided where Long expected)

**Current Status:** Root cause NOT definitively located

### Suspected Locations

#### Possibility 1: Display Formatting (MOST LIKELY)
**Location:** Any Compose screen using `String.format()` with Long values

**Bad Pattern:**
```kotlin
// WRONG - String.format expects Double, gets Long
String.format("%.2f", invoice.totalAmount)  // totalAmount is Long!
```

**Correct Pattern:**
```kotlin
// RIGHT - CentsFormatter handles Long
CentsFormatter.formatCents(invoice.totalAmount, "AUD")
```

**Places to Check:**
- `InvoiceListScreen.kt`
- `InvoiceDetailScreen.kt`
- `RevenueDashboardScreen.kt`
- `DunningNoticesScreen.kt`
- `InvoicePdfService.kt`
- `CurrencyDisplayWithAmount` composable

#### Possibility 2: Database Type Mismatch
**Location:** Room binding layer or SQLite column type

**Checks Needed:**
1. Verify actual database schema at runtime:
```bash
adb shell "sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db 'PRAGMA table_info(line_items);'"
```

2. Expected output:
```
cid|name|type|notnull|dflt_value|pk
0|id|INTEGER|1||1
1|invoiceId|INTEGER|1||0
2|description|TEXT|1||0
3|quantity|REAL|1||0
4|unitPrice|INTEGER|1||0  ← Should be INTEGER (Long)
5|currencyCode|TEXT|1||0
```

3. If `unitPrice` is REAL or TEXT, migration didn't apply correctly

#### Possibility 3: Calculation Type Coercion
**Location:** Arithmetic operations mixing Long, Double, Float

**Bad Pattern:**
```kotlin
val result = (unitPrice * quantity)  // Long * Double = Double, not Long!
```

**Correct Pattern:**
```kotlin
val result = (unitPrice.toDouble() * quantity).toLong()  // Explicit conversion
```

**Places to Check:**
- `InvoiceWithItems.kt` line 20: `val subtotal: Long get() = items.sumOf { (it.unitPrice * it.quantity).toLong() }`
- `LineItemExtensions.kt`: `fun LineItem.calculateTotal(): Long`
- `Mappers.kt`: `fun LineItemForm.calculateTotal(): Long`

#### Possibility 4: Compose State Closure Capture
**Location:** LineItemEditor composable TextFields

**Issue:** If state updates happen during text input, captured values might be stale

**Code:**
```kotlin
OutlinedTextField(
    value = if (unitPrice == 0L) "" else (unitPrice.toDouble() / 100.0).toString(),
    onValueChange = { it.toDoubleOrNull()?.let { valPrice -> onUpdate(description, quantity, (valPrice * 100).toLong()) } }
    //                                           ^^^^^^^
    //                      Could be Float if parsing quirk occurs
)
```

### How to Diagnose

#### Step 1: Enable Timber Logging
Add to `CreateInvoiceViewModel.onSaveClicked()`:
```kotlin
Timber.d("🔵 SAVE: Items count=${state.items.size}")
state.items.forEach { item ->
    Timber.d("  Item: id=${item.id}, desc=${item.description}, qty=${item.quantity}(${item.quantity.javaClass.simpleName}), price=${item.unitPrice}(${item.unitPrice.javaClass.simpleName})")
}

val lineItems = state.items.map { it.toDomain() }
lineItems.forEach { item ->
    Timber.d("  Domain: id=${item.id}, qty=${item.quantity}(${item.quantity.javaClass.simpleName}), price=${item.unitPrice}(${item.unitPrice.javaClass.simpleName})")
    val total = item.calculateTotal()
    Timber.d("  Total calculated: ${total}(${total.javaClass.simpleName})")
}
```

#### Step 2: Check Logcat During Error
```bash
adb logcat | grep "SAVE:\|Domain:\|Total\|f != java.lang.Long\|type.*mismatch"
```

#### Step 3: Add Type Guards
```kotlin
fun updateLineItem(id: Long?, description: String, quantity: Double, unitPrice: Long) {
    // Type validation
    require(quantity > 0.0 && quantity < 10000.0) { "Quantity out of range: $quantity" }
    require(unitPrice > 0L && unitPrice < 10_000_000L) { "Price out of range: $unitPrice" }
    
    Timber.d("updateLineItem: id=$id, desc=$description, qty=$quantity(${quantity.javaClass.simpleName}), price=$unitPrice(${unitPrice.javaClass.simpleName})")
    
    _uiState.update { state ->
        state.copy(items = state.items.map {
            if (it.id == id) it.copy(description = description, quantity = quantity, unitPrice = unitPrice) else it
        })
    }
}
```

### Critical Type Assertions

**In all formatters and displays, use this pattern:**
```kotlin
// NEVER do this:
val display = "$${value}"  // Don't assume value is Double

// ALWAYS do this:
val display = CentsFormatter.formatCents(value as Long, "AUD")  // Explicit type

// Or convert first:
val display = "$${value / 100.0}"  // Explicit conversion
```

---

## SECTION 4: DATABASE MIGRATION TRACKING

### Version History

| Version | Migration | Changes | Status |
|---------|-----------|---------|--------|
| 21 | 21→22 | Drops `pending_operations` table (sync subsystem removed) | ✅ Complete |
| 22 | 22→23 | Adds `currencyCode: TEXT` to `line_items` | ✅ Complete |
| 23 | 23→24 | Fixes payment entities: Double → Long for `invoice_payments`, `invoice_payment_snapshots`, `daily_payment_snapshots`, `collection_metrics` | ✅ Complete |
| 24 | (current) | - | ✅ Current |

### Migration Files Location
```
app/src/main/java/com/emul8r/bizap/data/local/migrations/
├── Migration_21_22.kt
├── Migration_22_23.kt
└── Migration_23_24.kt
```

### How to Verify Migration Was Applied

```bash
# Connect to the actual device database
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db

# Check version
sqlite> PRAGMA user_version;
24  ← Should be 24

# Check table structure
sqlite> PRAGMA table_info(line_items);
# Should show: unitPrice has type INTEGER (not REAL)

# Check table structure
sqlite> PRAGMA table_info(invoice_payments);
# Should show: amountPaid has type INTEGER (not REAL) ← Migrated in v24

sqlite> .quit
```

### If Migration Fails

**Symptom:** `PRAGMA user_version` shows 23, not 24

**Solution:**
1. Clear app data: `adb shell pm clear com.emul8r.bizap`
2. Uninstall: `adb uninstall com.emul8r.bizap`
3. Reinstall: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
4. Verify: Run above checks again

---

## SECTION 5: TYPE SYSTEM CONSISTENCY CHECKLIST

### For Each Entity Type:

**LineItemEntity (Data Layer)**
- [ ] `quantity: Double` (affinity: REAL in SQLite)
- [ ] `unitPrice: Long` (affinity: INTEGER in SQLite)
- [ ] Migration v22→23 added `currencyCode: String`

**LineItemForm (UI Layer)**
- [ ] `quantity: Double`
- [ ] `unitPrice: Long`
- [ ] `id: Long?` (null for new items - THIS IS THE BUG!)
- [ ] `transientId: UUID` (unique, but ignored in updateLineItem)

**LineItem (Domain Layer)**
- [ ] `quantity: Double`
- [ ] `unitPrice: Long`
- [ ] Must match both Entity and Form types

### Display Formatting Rules

**NEVER:**
```kotlin
String.format("%.2f", longValue)
"Total: $$${longValue}"
String.format("%.2f", doubleValue)  // If doubleValue contains cents
```

**ALWAYS:**
```kotlin
CentsFormatter.formatCents(longValue, currencyCode)
CentsFormatter.formatCentsWithSymbol(longValue, "$")
(longValue / 100.0)  // Explicit conversion to dollars
```

### Calculation Rules

**NEVER:**
```kotlin
val total = unitPrice * quantity  // Long * Double = Double!
```

**ALWAYS:**
```kotlin
val total = (unitPrice.toDouble() * quantity).roundToLong()
val total = unitPrice * quantity.toLong()  // If quantity must be Long
```

---

## SECTION 6: RUNTIME DEBUGGING COMMANDS

### Get Device Database

```bash
adb pull /data/data/com.emul8r.bizap/databases/bizap-db ./bizap-db

# Then examine with sqlite3:
sqlite3 ./bizap-db
sqlite> SELECT * FROM line_items LIMIT 1;
sqlite> PRAGMA table_info(line_items);
sqlite> SELECT typeof(unitPrice), typeof(quantity) FROM line_items LIMIT 1;
```

### Monitor Type Errors

```bash
adb logcat | grep -E "f != java.lang.Long|Type mismatch|IllegalFormat|ClassCastException"
```

### Check Compose Recomposition

Add to any Composable:
```kotlin
val recomposeCount = remember { mutableStateOf(0) }
LaunchedEffect(Unit) {
    recomposeCount.value++
    Timber.d("Recompose count: ${recomposeCount.value}")
}
```

### Verify ViewModel State

```kotlin
// In ViewModel, override a method or add logging:
Timber.d("Current state items: ${_uiState.value.items.mapIndexed { i, it -> 
    "$i: id=${it.id}, transientId=${it.transientId}, desc=${it.description}" 
}}")
```

---

## SECTION 7: COMMON SYMPTOM-TO-ROOT-CAUSE MAP

| Symptom | Possible Cause | Location | Fix |
|---------|----------------|----------|-----|
| Editing item #1 changes items #2 & #3 | NULL ID collision | `updateLineItem()` | Use `transientId` |
| "f != java.lang.Long" on save | Long passed to String.format("%.2f") | Display code | Use CentsFormatter |
| Amounts display as 0 or huge numbers | cents/dollars conversion missing | Display formatter | Add `/100.0` |
| Migration not applied | Database not cleared | Device storage | `pm clear` app |
| Data corrupt after save | Transaction rollback | `InvoiceDao.insert()` | Check Timber logs |
| Calculation gives wrong total | Double/Long mix without `.toLong()` | calculateTotal functions | Add explicit `.toLong()` |
| Types don't match in Compose | Stale closure capture | TextField `onValueChange` | Use `remember` properly |

---

## SECTION 8: CODE LOCATIONS FOR QUICK REFERENCE

### Key Files by Layer

**UI Layer (Most Type Issues Here)**
- `CreateInvoiceScreen.kt` - LineItemEditor composable, TextField callbacks
- `EditInvoiceScreen.kt` - Similar to above
- `InvoiceListScreen.kt` - Display formatting
- `InvoiceDetailScreen.kt` - Display formatting

**ViewModel Layer (State Management Issues)**
- `CreateInvoiceViewModel.kt` - updateLineItem() [BUG #1]
- `EditInvoiceViewModel.kt` - updateLineItem() [BUG #1]

**Repository/DAO Layer**
- `InvoiceRepositoryImpl.kt` - saveInvoice() method
- `InvoiceDao.kt` - Database transaction logic

**Domain Layer (Type Conversion)**
- `LineItem.kt` - calculateTotal(), toEntity()
- `LineItemExtensions.kt` - calculateTotal()
- `Mappers.kt` - toDomain(), calculateTotal()

**Utilities**
- `CentsFormatter.kt` - All display formatting (USE THIS!)

**Database**
- `AppDatabase.kt` - v24, 18 entities
- `LineItemEntity.kt` - quantity: Double, unitPrice: Long
- `Migrations/Migration_23_24.kt` - Payment entity type fixes

---

## SECTION 9: TESTING STRATEGY

### Unit Tests for Type Safety

```kotlin
@Test
fun lineItem_calculateTotal_returnsLongType() {
    val item = LineItem(
        description = "Test",
        quantity = 2.5,
        unitPrice = 5000L  // $50.00
    )
    val total = item.calculateTotal()
    assertThat(total).isEqualTo(12500L)  // 2.5 * 5000
    assertThat(total).isInstanceOf(Long::class.java)
}

@Test
fun updateLineItem_onlyUpdatesMatchingItem() {
    val state = CreateInvoiceUiState(
        items = listOf(
            LineItemForm(id = null, description = "Item 1"),
            LineItemForm(id = null, description = "Item 2")
        )
    )
    
    // This should ONLY update Item 1, not Item 2
    // BUG: Currently updates both!
    val newState = updateLineItem(state, null, "Updated", 1.0, 5000L)
    
    assertThat(newState.items[0].description).isEqualTo("Updated")
    assertThat(newState.items[1].description).isEqualTo("Item 2")  // Should NOT change
}
```

### Integration Tests

```kotlin
@Test
fun savingInvoiceWithLineItems_persistsCorrectly() {
    val invoice = Invoice(
        items = listOf(
            LineItem(description = "Item 1", quantity = 1.0, unitPrice = 5000L),
            LineItem(description = "Item 2", quantity = 2.5, unitPrice = 2000L)
        )
    )
    
    val savedId = runBlocking { repository.saveInvoice(invoice) }
    val retrieved = runBlocking { repository.getInvoiceWithItemsById(savedId).first() }
    
    assertThat(retrieved!!.items).hasSize(2)
    assertThat(retrieved.items[0].unitPrice).isEqualTo(5000L)
    assertThat(retrieved.items[1].quantity).isEqualTo(2.5)
}
```

---

## SECTION 10: PREVENTION STRATEGIES

### 1. Type-Safe Identifiers
Instead of using nullable IDs for matching, use:
- UUIDs (unique per item)
- Indices (position in list)
- Wrapper classes enforcing immutability

### 2. Compile-Time Type Checks
```kotlin
// Create type aliases to prevent mixing up monetary types
typealias CentsLong = Long  // Always in cents
typealias DollarsDouble = Double

// Then use in APIs:
fun formatPrice(amount: CentsLong, code: String): String {
    return CentsFormatter.formatCents(amount, code)
}
```

### 3. Runtime Validation
```kotlin
fun saveInvoice(invoice: Invoice) {
    // Validate before saving
    invoice.items.forEach { item ->
        require(item.quantity > 0 && item.quantity < 10000.0) {
            "Invalid quantity: ${item.quantity}"
        }
        require(item.unitPrice > 0L && item.unitPrice < 10_000_000L) {
            "Invalid price in cents: ${item.unitPrice}"
        }
    }
    // ... proceed with save
}
```

### 4. Automated Testing
- Unit tests for every type conversion
- Integration tests for every save/load flow
- Type-checking lints (use kotlin-linter rules)

---

## QUICK START: DIAGNOSING A NEW ISSUE

**If you see a runtime error:**

1. **Check the error message:**
   - "f != java.lang.Long" → Display formatting issue
   - "Type mismatch" → Compilation issue (won't run)
   - "NullPointerException" → Null ID problem
   - "IllegalFormatConversion" → String.format type mismatch

2. **Check the stack trace:**
   ```
   at android.text.format....  → Display issue
   at com.emul8r.bizap.ui...   → Compose/ViewModel issue
   at com.emul8r.bizap.data... → Database/Repository issue
   ```

3. **Enable verbose logging:**
   - Add Timber.d() calls around the error location
   - Log types: `value.javaClass.simpleName`
   - Log values: `value`, `value.toString()`

4. **Reproduce consistently:**
   - Exact steps to reproduce
   - What data triggers it (null items, new items, saved items?)
   - Does it happen on first save? After edits? Only with multi-currency?

5. **Check recent changes:**
   - Did this code compile before?
   - What changed since it last worked?
   - Is a migration missing?

---

**END OF TROUBLESHOOTING GUIDE**

Version: 1.0  
Last Updated: March 4, 2026  
Status: COMPREHENSIVE - Ready for future debugging

