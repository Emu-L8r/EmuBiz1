# DATA FLOW TYPE MAPPING - Bizap Invoice System

**Purpose:** Track exact type transformations as data flows through all layers

**Date:** March 4, 2026

---

## FLOW 1: Creating a New Invoice (Happy Path)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ USER ACTION: Click "+ Add Line Item"                                         │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ CreateInvoiceScreen.kt line 150                                              │
│ onUpdate = { desc: String, qty: Double, price: Long ->                       │
│   viewModel.updateLineItem(item.id, desc, qty, price)                        │
│ }                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ CreateInvoiceViewModel.kt line 127-132                                       │
│ updateLineItem(id: Long? = null, description: String, quantity: Double,     │
│               unitPrice: Long)                                                │
│                                                                                │
│ _uiState.update { state ->                                                   │
│   state.copy(items = state.items.map {                                       │
│     if (it.id == id) ← BUG #1: When id=null, matches ALL null items!        │
│       it.copy(description = description,                                     │
│               quantity = quantity,                                            │
│               unitPrice = unitPrice)                                          │
│     else it                                                                    │
│   })                                                                           │
│ }                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ CreateInvoiceUiState                                                          │
│ items: List<LineItemForm> = [                                                │
│   LineItemForm(                                                               │
│     id = null,                          ← The problem! All new items have null│
│     transientId = UUID.randomUUID(),    ← Unique, but not used for matching! │
│     description = "Service A",                                                │
│     quantity = 1.0,          (Double)                                         │
│     unitPrice = 5000L        (Long = 5000 cents = $50.00)                    │
│   ),                                                                           │
│   LineItemForm(                                                               │
│     id = null,               ← Same id as above! Collision!                   │
│     transientId = UUID.randomUUID(),                                          │
│     description = "Service B",                                                │
│     quantity = 2.0,                                                           │
│     unitPrice = 2500L                                                         │
│   )                                                                            │
│ ]                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ USER ACTION: Click "Save Invoice"                                             │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ CreateInvoiceViewModel.kt line 145-175 (onSaveClicked)                       │
│                                                                                │
│ val state = _uiState.value                                                   │
│ val customer = state.selectedCustomer  (Customer object)                      │
│ val lineItems = state.items.map { it.toDomain() }  ← Convert to domain      │
│                                                                                │
│ After conversion:                                                             │
│ List<LineItem> = [                                                            │
│   LineItem(                                                                    │
│     id = null ?: 0L = 0L,   ← Converts null to 0L                            │
│     description = "Service A",                                                │
│     quantity = 1.0,         (Double)                                          │
│     unitPrice = 5000L       (Long, in cents)                                 │
│   ),                                                                           │
│   LineItem(                                                                    │
│     id = 0L,                ← All new items now have id=0L!                  │
│     description = "Service B",                                                │
│     quantity = 2.0,                                                           │
│     unitPrice = 2500L                                                         │
│   )                                                                            │
│ ]                                                                              │
│                                                                                │
│ val subtotal = lineItems.sumOf { it.calculateTotal() }                        │
│ ├─ For item 1: 5000L (Long) * 1.0 (Double) = 5000.0 (Double)                │
│ │                                             ↓ .toLong()                     │
│ │                                             5000L (Long)                    │
│ ├─ For item 2: 2500L * 2.0 = 5000.0 ↓ .toLong() = 5000L                     │
│ └─ sum = 10000L (Long, in cents = $100.00)                                   │
│                                                                                │
│ val invoice = Invoice(                                                         │
│   customerId = customer.id,                                                   │
│   customerName = customer.name,                                               │
│   totalAmount = subtotal + taxAmount,  (Long, in cents)                      │
│   items = lineItems,                   (List<LineItem>)                      │
│   ...                                                                          │
│ )                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ InvoiceRepositoryImpl.kt line 51-68 (saveInvoice)                             │
│                                                                                │
│ val invoiceEntity = invoiceToSave.toEntity()                                 │
│ ├─ Converts Invoice (domain) → InvoiceEntity (data)                          │
│ ├─ totalAmount: Long → InvoiceEntity.totalAmount: Long (affinity: INTEGER)  │
│ └─ Result: InvoiceEntity with id=0 (new)                                     │
│                                                                                │
│ val lineItemEntities = invoiceToSave.items.map { it.toEntity(invoiceId) }    │
│ ├─ For each LineItem (domain):                                               │
│ │  └─ Converts to LineItemEntity (data):                                     │
│ │     ├─ quantity: Double → quantity: Double (affinity: REAL)                │
│ │     └─ unitPrice: Long → unitPrice: Long (affinity: INTEGER)              │
│ └─ Result: List<LineItemEntity> ready for database                          │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ InvoiceDao.kt line 26-34 (insert transaction)                                │
│                                                                                │
│ @Transaction                                                                  │
│ suspend fun insert(invoice: InvoiceEntity, items: List<LineItemEntity>) {   │
│   val id = insertInvoice(invoice)                                            │
│   if (invoice.id != 0L) {                                                     │
│     deleteLineItems(invoice.id)  ← Deletes OLD items if updating             │
│   }                                                                            │
│   val itemsWithId = items.map { it.copy(invoiceId = id) }                    │
│   insertLineItems(itemsWithId)                                               │
│   return id                                                                    │
│ }                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ Room/SQLite Database Insertion                                                │
│                                                                                │
│ INSERT INTO invoices (id, businessProfileId, customerId, ..., totalAmount)   │
│ VALUES (NULL, 1, 101, ..., 10000)  ← id=NULL (autoincrement), totalAmount as │
│                                       INTEGER                                  │
│                                                                                │
│ INSERT INTO line_items (id, invoiceId, description, quantity, unitPrice, ... │
│ VALUES                                                                         │
│ (NULL, 1, "Service A", 1.0, 5000, "AUD"),  ← quantity as REAL (1.0)         │
│ (NULL, 1, "Service B", 2.0, 2500, "AUD")   ← unitPrice as INTEGER (5000 cents)
│                                                                                │
│ SQLite Affinity Mapping:                                                      │
│ quantity: 1.0 (Double) → REAL column ✓ (affinity matches)                    │
│ unitPrice: 5000L (Long) → INTEGER column ✓ (affinity matches)                │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
                         ✓ SUCCESS
```

---

## FLOW 2: Editing a Line Item (Where BUG #1 Manifests)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ STARTING STATE:                                                               │
│ items: [                                                                       │
│   LineItemForm(id=null, transientId=UUID-A, description="Service A", qty=1.0)
│   LineItemForm(id=null, transientId=UUID-B, description="Service B", qty=2.0)
│   LineItemForm(id=null, transientId=UUID-C, description="Service C", qty=3.0)
│ ]                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ USER ACTION: Edit item #1 (UUID-A)                                            │
│ - Quantity field: 1.0 → 2.5                                                  │
│ - onUpdate is called with: (description="Service A", qty=2.5, price=5000L)   │
│                                                                                │
│ Callback passes to ViewModel:                                                 │
│ updateLineItem(id=null, "Service A", 2.5, 5000L)                             │
│           (↑ NULL because LineItemForm.id = null for new items!)              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ CreateInvoiceViewModel.updateLineItem()                                       │
│                                                                                │
│ _uiState.update { state ->                                                   │
│   state.copy(items = state.items.map { item ->                               │
│     if (item.id == null) {  ← HERE'S THE BUG!                                │
│       // This condition is TRUE for ALL THREE items!                          │
│       // Because all three have id=null                                       │
│       item.copy(                                                              │
│         description = "Service A",  ← WRONG! Overwrites all items!           │
│         quantity = 2.5,             ← WRONG! Overwrites all items!           │
│         unitPrice = 5000L           ← WRONG! Overwrites all items!           │
│       )                                                                         │
│     } else {                                                                   │
│       item  ← Never reached for new items                                     │
│     }                                                                          │
│   })                                                                           │
│ }                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ ENDING STATE (WRONG!):                                                         │
│ items: [                                                                       │
│   LineItemForm(id=null, transientId=UUID-A, description="Service A", qty=2.5)│
│   LineItemForm(id=null, transientId=UUID-B, description="Service A", qty=2.5)│ ← SAME!
│   LineItemForm(id=null, transientId=UUID-C, description="Service A", qty=2.5)│ ← SAME!
│ ]                                                                              │
│                                                                                │
│ ❌ Expected: Only item #1 changed                                             │
│ ❌ Actual: ALL items changed to same values                                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## FLOW 3: Displaying Amounts (Where BUG #2 Occurs)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ Invoice object in memory (Domain layer):                                      │
│ Invoice(                                                                       │
│   totalAmount = 10000L,  (Long, in cents)                                    │
│   items = [                                                                    │
│     LineItem(quantity = 1.0, unitPrice = 5000L),                             │
│     LineItem(quantity = 2.0, unitPrice = 2500L)                              │
│   ]                                                                            │
│ )                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ InvoiceListScreen.kt (display code)                                           │
│                                                                                │
│ WRONG PATTERN:                                                                │
│ Text("Total: $${String.format(Locale.getDefault(), "%.2f",                  │
│      invoice.totalAmount)}")                                                  │
│ ├─ String.format() expects Double or Float for "%.2f"                        │
│ ├─ But invoice.totalAmount is Long (10000)                                   │
│ └─ ❌ Result: java.lang.IllegalFormatConversionException                      │
│               "f != java.lang.Long"                                            │
│                                                                                │
│ RIGHT PATTERN:                                                                │
│ Text("Total: ${CentsFormatter.formatCents(invoice.totalAmount, "AUD")}")      │
│ ├─ CentsFormatter.formatCents(cents: Long, code: String)                     │
│ │  ├─ dollars = cents / 100.0 = 10000L / 100.0 = 100.0 (Double)            │
│ │  ├─ Uses DecimalFormat with currency locale                                │
│ │  └─ Returns: "A$100.00"                                                     │
│ └─ ✓ Result: "Total: A$100.00"                                               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## FLOW 4: Calculating Line Item Total (Type Conversions)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ LineItem data:                                                                │
│ quantity = 2.5 (Double)                                                      │
│ unitPrice = 5000L (Long, cents)                                              │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ Calculation 1: WRONG PATTERN                                                  │
│ val total = unitPrice * quantity                                              │
│         = 5000L * 2.5                                                         │
│         = 5000 (Long) * 2.5 (Double)                                          │
│         = 12500.0 (Double, NOT Long!)  ← TYPE CHANGED!                       │
│         ❌ Cannot assign Double to Long field                                 │
└─────────────────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────────────────┐
│ Calculation 2: CORRECT PATTERN (what code uses)                              │
│ val total = (unitPrice.toDouble() * quantity).toLong()                       │
│          = (5000L.toDouble() * 2.5).toLong()                                │
│          = (5000.0 * 2.5).toLong()                                            │
│          = 12500.0.toLong()                                                   │
│          = 12500L (Long) ✓                                                    │
│                                                                                │
│ OR:                                                                            │
│ val total = (unitPrice * quantity).toLong()                                  │
│          = (5000L * 2.5).toLong()                                             │
│          = 12500.0.toLong()  ← Kotlin auto-converts to Double first          │
│          = 12500L (Long) ✓                                                    │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## TYPE TRANSFORMATION MATRIX

### When Data Enters the System (UI Input)

| Field | Input | Type | Kotlin Type | Notes |
|-------|-------|------|-------------|-------|
| Description | TextField | String | String | Text input |
| Quantity | TextField | "2.5" | String | Must convert |
| Unit Price | TextField | "50.00" | String | Must convert |

| Transformation | Code | Input Type | Output Type | Result |
|----------------|------|-----------|------------|--------|
| Quantity parse | `it.toDoubleOrNull()` | String | Double? | 2.5 |
| Price parse & convert | `(it.toDoubleOrNull() * 100).toLong()` | String | Long | 5000L |

### When Data Goes to Database

| Entity Field | Kotlin Type | SQLite Affinity | Storage | Range |
|--------------|------------|-----------------|---------|-------|
| description | String | TEXT | Text | Any length |
| quantity | Double | REAL | 64-bit float | 0.001 to 999,999.999 |
| unitPrice | Long | INTEGER | 64-bit int | 0 to 9,223,372,036,854,775,807 |
| totalAmount | Long | INTEGER | 64-bit int | 0 to 9,223,372,036,854,775,807 |

### When Data Comes from Database

| Column | SQLite Type | Room Type | Kotlin Type | Display |
|--------|-----------|-----------|-------------|---------|
| quantity | REAL | Double | Double | `quantity.toString()` = "2.5" |
| unitPrice | INTEGER | Long | Long | `CentsFormatter.formatCents(unitPrice, code)` = "A$50.00" |
| totalAmount | INTEGER | Long | Long | `CentsFormatter.formatCents(totalAmount, code)` = "A$125.00" |

---

## CRITICAL TRANSFORMATION CHECKPOINTS

### Checkpoint 1: UI Input → ViewModel State
```kotlin
✓ Quantity: "2.5" → 2.5 (Double)
✓ Unit Price: "50.00" → 5000L (Long, multiply by 100)
✓ Description: "Service A" → "Service A" (String)
```

### Checkpoint 2: ViewModel State → Domain Model
```kotlin
✓ LineItemForm → LineItem (toDomain())
✓ All types preserved: Double→Double, Long→Long
```

### Checkpoint 3: Domain Model → Entity
```kotlin
✓ LineItem → LineItemEntity (toEntity())
✓ quantity: Double → REAL column
✓ unitPrice: Long → INTEGER column
```

### Checkpoint 4: Entity → Database
```kotlin
✓ Room binding: Double goes to REAL, Long goes to INTEGER
✓ SQLite storage: Values preserved with correct affinity
```

### Checkpoint 5: Database → Entity → Domain
```kotlin
✓ SQLite → Room: REAL→Double, INTEGER→Long
✓ Entity → Domain: Types match exactly
```

### Checkpoint 6: Domain → Display
```kotlin
✓ totalAmount: Long → CentsFormatter.formatCents() → "A$100.00"
✗ totalAmount: Long → String.format("%.2f") → CRASH!
```

---

## SUMMARY: SAFE PATTERNS vs DANGEROUS PATTERNS

### For Monetary Amounts

**DANGEROUS:**
```kotlin
String.format("%.2f", longValue)  // ❌ Float format, Long value
val result = longValue * doubleValue  // ❌ Mixed types = Double
doubleValue.toInt()  // ❌ Truncates decimal
"$" + doubleValue  // ❌ Floating-point errors shown
```

**SAFE:**
```kotlin
CentsFormatter.formatCents(longValue, code)  // ✓ Built for Long cents
val result = (longValue.toDouble() * doubleValue).toLong()  // ✓ Explicit
val result = (longValue.toDouble() * doubleValue).roundToLong()  // ✓ Better
CentsFormatter.formatCentsWithSymbol(longValue, "$")  // ✓ Also safe
```

### For Line Item Updates

**DANGEROUS:**
```kotlin
if (it.id == id) { ... }  // ❌ When id=null, matches ALL nulls
```

**SAFE:**
```kotlin
if (it.transientId == transientId) { ... }  // ✓ Each item unique
if (index == targetIndex) { ... }  // ✓ Position-based
```

---

## SECTION 6: BUG INTERACTION FLOW - HOW CORRUPTION LEADS TO TYPE ERROR

### The Complete Failure Chain

When Bug #1 corrupts the data, it creates a precondition that triggers Bug #2:

```
┌─────────────────────────────────────────────────────────────────┐
│ BUG #1: NULL ID COLLISION (Edit Phase)                           │
│                                                                   │
│ User edits Item #1: qty = 2.5                                   │
│   ↓                                                               │
│ updateLineItem(id=null, "Service A", 2.5, 5000L)                │
│   ↓                                                               │
│ if (it.id == null) { ← ALL 3 items match! }                     │
│   ↓                                                               │
│ State becomes CORRUPTED:                                          │
│   [Item("Service A", 2.5), Item("Service A", 2.5), Item("Service A", 2.5)]
│                                                                   │
│ ✗ BUG #1 DAMAGE: All items now identical                        │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ User clicks Save                                                  │
│   ↓                                                               │
│ Corrupted state flows into save pipeline:                         │
│ [Item("Service A", 2.5), Item("Service A", 2.5), Item("Service A", 2.5)]
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ onSaveClicked() → lineItems = state.items.map { it.toDomain() } │
│   ↓                                                               │
│ Result: List<LineItem> with 3 identical items                   │
│   ↓                                                               │
│ subtotal = lineItems.sumOf { (it.unitPrice * it.quantity).toLong() }
│         = (5000L * 2.5 * 3)  ← All three multiply same way      │
│         = 37500L                                                  │
│   ↓                                                               │
│ ✓ Calculation completes (no error yet)                           │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ BUG #2: TYPE MISMATCH (Save Phase)                               │
│                                                                   │
│ Data ready for database insert:                                  │
│ Invoice(                                                          │
│   totalAmount = 37500L,  ← Long                                  │
│   items = [3 identical items]                                    │
│ )                                                                 │
│   ↓                                                               │
│ Display screen tries to show total:                              │
│ Text("Total: ${String.format("%.2f", invoice.totalAmount)}")    │
│        ↑                            ↑                             │
│        ↑ Expects Double/Float      ↑ Provides Long (37500L)     │
│   ↓                                                               │
│ ❌ IllegalFormatConversionException: f != java.lang.Long        │
│                                                                   │
│ OR during database binding:                                       │
│ Room tries to insert Long value into column with wrong affinity  │
│   ↓                                                               │
│ ❌ Type mismatch error                                            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Save fails, user loses all changes                               │
│ User tries again, same two bugs trigger again                    │
│ Infinite loop of failure                                          │
└─────────────────────────────────────────────────────────────────┘
```

### Why Bug #2 Only Appears After Bug #1

**Key insight:** Bug #2 might not occur at all with normal, non-corrupted data.

Possible reasons Bug #2 only manifests with Bug #1's corruption:

1. **Type conversion edge case:**
   - Normal data: varied values → type conversion succeeds
   - Corrupted data: 3 identical values → specific calculation fails

2. **Database constraint issue:**
   - Normal data: unique values pass validation
   - Corrupted data: duplicate items trigger constraint violation

3. **Display formatter quirk:**
   - Normal data: varied amounts display correctly
   - Corrupted data: identical amounts trigger formatter bug

4. **Calculation overflow:**
   - Normal data: sum is reasonable
   - Corrupted data: 3x multiplication results in unexpected type

### The Evidence

**Why this theory is credible:**

1. Bug #1 is **100% reproducible** - every edit causes collision
2. Bug #2 is **only observed after invoice edit** - not on first create
3. Bug #1 creates **identical values** - unusual state not in normal flow
4. Bug #2 is **type-related** - suggests edge case in type handling

**This suggests:**
- Bug #2's root cause is something that only breaks with specific value patterns
- Bug #1's corruption creates exactly those value patterns
- They're linked mechanically, not coincidentally

---

**END OF DATA FLOW TYPE MAPPING**

Version: 1.1  
---

## CRITICAL: HOW BUG #1 AND BUG #2 CREATE A COMPOUND FAILURE

### The Vicious Cycle That Breaks Invoice Creation

When both bugs are present, they interact destructively:

**Bug #1 creates degenerate state:**
```
All line items become identical because they all have id=null
Items: [
  LineItemForm(id=null, transientId=UUID-A, description="Service A", qty=2.5),
  LineItemForm(id=null, transientId=UUID-B, description="Service A", qty=2.5),  ← WRONG!
  LineItemForm(id=null, transientId=UUID-C, description="Service A", qty=2.5)   ← WRONG!
]
```

**Bug #2 fails on degenerate state:**
```
When saving this corrupted state, a type mismatch error occurs
Possible locations:
- Display formatter expecting Double, gets Long
- Database binding seeing type mismatch on corrupted values
- Calculation expecting unique values, fails on duplicates
```

**Result: Complete feature failure**
- User edits items → Bug #1 creates identical items (not obvious)
- User tries to save → Bug #2 throws type error (mysterious)
- User loses all work → No recovery path
- User tries again → Same cycle repeats

### Why This Matters

The two bugs are **NOT independent**:
- Bug #1 alone would at least let you save (with wrong data)
- Bug #2 alone would only affect certain states
- **Together they make the feature completely non-functional**

---

**Last Updated:** March 4, 2026
Includes: Bug interaction flow analysis

Use this document as reference when:
- Adding new line items
- Saving invoices
- Displaying amounts
- Debugging type errors
- Understanding bug interactions
- Adding new features that handle money


