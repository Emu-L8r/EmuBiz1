# System Comprehension Guide

**Date:** 2026-03-04  
**App:** Bizap / EmuBiz — Sole Trader Business Management  
**Package:** `com.emul8r.bizap`

---

## Purpose

This guide explains how the system works end-to-end: how data flows through layers, which classes are responsible for what, and how the key features are implemented. It is intended for developers joining the project or returning after a break.

---

## Feature Walkthroughs

### 1. Customer Management

**Navigation path:** Dashboard → Customers tab → Customer detail

**How it works:**

1. `CustomerViewModel` (hiltViewModel) observes `CustomerRepository.getAllCustomers()` which returns a `Flow<List<Customer>>`
2. The flow is scoped to the active business: `businessProfileRepository.activeProfile.flatMapLatest { business -> customerDao.getCustomersByBusiness(business.id) }`
3. `CustomerListScreen` renders the list; tapping a customer navigates to `CustomerDetailScreen` with the `customerId`
4. `CustomerDetailViewModel` loads the specific customer and their associated invoices using `CustomerRepository.getCustomerById(id)` + `InvoiceRepository` filtered by `customerId`
5. Customer creation/editing posts to `CustomerRepository.saveCustomer(customer)` which calls `CustomerDao.upsert()`

**Key classes:**
- `CustomerViewModel` — `ui/customers/CustomerViewModel.kt`
- `CustomerDetailViewModel` — `ui/customers/CustomerDetailViewModel.kt`
- `CustomerRepository` (interface) — `domain/repository/CustomerRepository.kt`
- `CustomerRepositoryImpl` — `data/repository/CustomerRepositoryImpl.kt`
- `CustomerDao` — `data/local/CustomerDao.kt`
- `CustomerEntity` — `data/local/entities/CustomerEntity.kt`
- `Customer` (domain model) — `domain/model/Customer.kt`

---

### 2. Invoice Creation

**Navigation path:** Invoices tab → FAB / "New Invoice" → `CreateInvoiceScreen`

**How it works:**

1. `CreateInvoiceViewModel` initialises with an empty `Invoice` domain object
2. User selects a customer from the customer list (optional — invoices can be customerless)
3. User adds line items; each new item gets a `transientId = UUID.randomUUID().toString()` to track it before persistence
4. User sets header/subheader/notes/footer, date, due date, tax rate
5. On "Save", `CreateInvoiceViewModel.saveInvoice()` calls `SaveInvoiceUseCase(invoice)`
6. `SaveInvoiceUseCase` delegates to `InvoiceRepository.saveInvoice(invoice)`
7. `InvoiceRepositoryImpl.saveInvoice()` fetches `activeBusinessId`, assigns `invoiceYear`, `invoiceSequence`, `version = 1`, then calls `InvoiceDao.insert(invoiceEntity, lineItemEntities)`
8. `InvoiceDao.insert()` is `@Transaction`: inserts invoice (gets auto-generated ID), maps all line items to that ID, bulk-upserts line items

**Scoped Invoice Numbering:**

```
INV-2026-000001
     ^^^^  ^^^^^^
     year  sequence (6-digit padded, per business per year)
```

```kotlin
val nextSequence = invoiceDao.getMaxSequenceForYear(currentYear, activeBusinessId) + 1
invoiceToSave = invoiceToSave.copy(
    invoiceYear = currentYear,
    invoiceSequence = nextSequence,
    version = 1
)
```

---

### 3. Invoice Editing

**Navigation path:** Invoice Detail → "Edit" button → `EditInvoiceScreen`

**How it works:**

1. `EditInvoiceViewModel` receives `invoiceId` from `SavedStateHandle`
2. `uiState` is a `combine()` of three flows: the repository's live invoice, all customers, and `_editState` (local edits not yet saved)
3. Local edits are stored in `_editState: MutableStateFlow<Invoice?>` — when non-null, they override the repository version in `uiState`
4. Mutation functions (`addLineItem`, `removeLineItem`, `updateLineItem`, `onHeaderChange`, etc.) all call `_editState.update { it?.copy(...) }`
5. On "Save", `saveInvoice()` calls `invoiceRepository.saveInvoice(state.invoice)` which calls `InvoiceDao.insert()` with `OnConflictStrategy.ABORT` for the invoice and `Upsert` for line items
6. On success, `_navigationEvent.emit(NavigationEvent.BackToInvoiceDetail)`

**Edit state isolation pattern:**

```kotlin
val uiState: StateFlow<EditInvoiceUiState> = combine(
    invoiceRepository.getInvoiceWithItemsById(invoiceId),  // live DB
    customerRepository.getAllCustomers(),                    // customer list
    _editState                                              // local mutations
) { invoice, customers, editingInvoice ->
    EditInvoiceUiState.Success(
        invoice = editingInvoice ?: invoice!!,  // prefer local edits
        customers = customers
    )
}
```

---

### 4. PDF Generation

**Navigation path:** Invoice Detail → "Export PDF" or "Share PDF"

**How it works:**

1. `InvoiceDetailViewModel.exportToDownloads()` or `shareInternalPdf()` calls `checkAndProceedWithPdfGeneration(share = false/true)`
2. `pdfService.checkIfPdfExists(invoiceId, "Invoice")` checks `DocumentRepository` — if a PDF already exists, shows `PdfOverwriteState` dialog
3. User chooses "Overwrite" or "Keep Both"; both paths call `generateAndExportPdf(share, overwriteExisting)`
4. `generateAndExportPdf()` fetches `businessProfileRepository.profile.first()`, builds `InvoiceSnapshot` via `buildSnapshot()`
5. `GenerateAndSaveInvoiceUseCase` is called **twice** — once for Quote PDF, once for Invoice PDF (atomic pair)
6. `InvoicePdfService.generateInvoice()`:
   - Creates `PdfDocument` (Android native PDF API, A4: 595×842 points)
   - Draws business header, customer block, line items table via `PdfTableRenderer`
   - Draws subtotal / tax / total
   - Writes to `context.filesDir/documents/<filename>.pdf`
   - Saves record to `DocumentRepository` (GeneratedDocumentEntity)
7. For share: emits `File` via `_exportEvent`; screen creates `FileProvider` URI and launches `ACTION_SEND` chooser
8. For export: `DocumentManager.saveToDownloads()` copies to public Downloads/Bizap folder

**InvoiceSnapshot** (the PDF data transfer object):

```kotlin
data class InvoiceSnapshot(
    val invoiceId: Long,
    val invoiceNumber: String,   // formatted: INV-2026-000001
    val customerName: String,
    val customerAddress: String,
    val customerEmail: String?,
    val date: Long,              // epoch millis
    val dueDate: Long,
    val items: List<LineItemSnapshot>,
    val subtotal: Long,          // Long cents
    val taxRate: Double,         // e.g. 0.10
    val taxAmount: Long,         // Long cents
    val totalAmount: Long,       // Long cents
    val businessName: String,
    val businessAbn: String,
    val businessEmail: String,
    val businessPhone: String,
    val businessAddress: String,
    val logoBase64: String?,
    val currencyCode: String = "AUD"
)
```

---

### 5. Dashboard

**Screen:** `DashboardScreen.kt` — `ui/dashboard/DashboardScreen.kt`

**How it works:**

1. `DashboardScreen` is a `@Composable` that takes a `NavController`
2. It instantiates `CustomerViewModel` and `BusinessProfileViewModel` via `hiltViewModel()`
3. Displays active business name + ABN in header with a "Switch Business" `IconButton`
4. Two `ElevatedCard`s: "Total Clients" (live from `customerViewModel.uiState.size`) and "Revenue" (hardcoded `"$0.00"` — **known gap**)
5. `BusinessSwitcherDialog` allows switching between business profiles
6. `InvoiceList` composable shows recent invoices; tapping navigates to `InvoiceDetail`

**Current limitations:**
- No dedicated `DashboardViewModel` — data sourced directly from Customer and BusinessProfile VMs
- Revenue metric is static — `RevenueDashboardViewModel` is not used here

---

## Data Flow Examples

### Complete "Create Invoice" Flow

```
User taps "Save" on CreateInvoiceScreen
        │
        ▼
CreateInvoiceViewModel.saveInvoice()
        │
        ▼
SaveInvoiceUseCase.invoke(invoice: Invoice)
        │  (domain layer use case, orchestrates validation + save)
        ▼
InvoiceRepository.saveInvoice(invoice)        [domain interface]
        │
        ▼
InvoiceRepositoryImpl.saveInvoice(invoice)    [data layer impl]
        │  1. getActiveBusinessId() → BusinessProfileRepository
        │  2. Calculate invoiceYear, invoiceSequence, version
        │  3. invoice.toEntity() → InvoiceEntity
        │  4. items.map { it.toEntity(invoiceId) } → List<LineItemEntity>
        ▼
InvoiceDao.insert(invoiceEntity, lineItemEntities)   [Room DAO]
        │  @Transaction:
        │  1. insertInvoice(entity) → returns new Long ID
        │  2. deleteLineItems(invoice.id) if updating
        │  3. insertLineItems(items with new invoiceId)
        ▼
BizapDatabase (SQLite)
        │  Tables: invoices, line_items
        ▼
Flow<List<InvoiceWithItems>> updates automatically
        │  (Room observes table changes)
        ▼
InvoiceRepositoryImpl.getAllInvoicesWithItems()
        │  .map { it.toDomain() }
        ▼
InvoiceListViewModel / InvoiceDetailViewModel observes
        ▼
UI recomposes with updated invoice list
```

### PDF Export Flow

```
User taps "Export PDF" on InvoiceDetailScreen
        │
        ▼
InvoiceDetailViewModel.exportToDownloads()
        │
        ▼
pdfService.checkIfPdfExists(invoiceId, "Invoice")
        │  queries DocumentRepository → GeneratedDocumentEntity
        │
        ├─ PDF exists → _showOverwriteDialog.value = PdfOverwriteState(...)
        │
        └─ No PDF → generateAndExportPdf(share=false, overwriteExisting=true)
                │
                ▼
        businessProfileRepository.profile.first()  → BusinessProfile
                │
                ▼
        buildSnapshot(invoice, businessProfile)  → InvoiceSnapshot
                │
                ▼
        GenerateAndSaveInvoiceUseCase(invoice, snapshot, isQuote=true)
        GenerateAndSaveInvoiceUseCase(invoice, snapshot, isQuote=false)
                │  Both return Result<File>
                ▼
        InvoicePdfService.generateInvoice(snapshot, isQuote)
                │  1. Determine filename via DocumentNamingUtils
                │  2. Create PdfDocument (595×842pt)
                │  3. PdfTableRenderer draws line items
                │  4. Write to filesDir/documents/<name>.pdf
                │  5. documentRepository.saveDocument(entity)
                ▼
        documentManager.saveToDownloads(file, name)
                │  Copies to MediaStore Downloads/Bizap/
                ▼
        _uiEvent.emit(ShowSnackbar("Success: Documents exported to Downloads/Bizap"))
```

---

## Key Class Responsibilities

### ViewModels

| ViewModel | File | Responsibility |
|---|---|---|
| `CreateInvoiceViewModel` | `ui/invoices/CreateInvoiceViewModel.kt` | Manages new invoice creation form state, line item CRUD, customer selection, delegates to `SaveInvoiceUseCase` |
| `EditInvoiceViewModel` | `ui/invoices/EditInvoiceViewModel.kt` | Loads existing invoice, manages local edit state via `_editState`, saves via `InvoiceRepository`, handles PDF share |
| `InvoiceDetailViewModel` | `ui/invoices/InvoiceDetailViewModel.kt` | Loads invoice + version group, handles PDF export/share/print, payment recording, status updates, invoice deletion, correction creation |
| `InvoiceListViewModel` | `ui/invoices/InvoiceListViewModel.kt` | Observes all invoices for active business, provides list to `InvoiceList` composable |
| `CustomerViewModel` | `ui/customers/CustomerViewModel.kt` | Observes customer list for active business |
| `CustomerDetailViewModel` | `ui/customers/CustomerDetailViewModel.kt` | Loads single customer + their invoices |
| `BusinessProfileViewModel` | `ui/settings/BusinessProfileViewModel.kt` | Loads/saves business profile, logo, signature |
| `DocumentVaultViewModel` | `ui/documents/DocumentVaultViewModel.kt` | Lists saved PDF documents via `DocumentRepository` |
| `RevenueDashboardViewModel` | `ui/analytics/RevenueDashboardViewModel.kt` | Aggregates revenue metrics via `GetRevenueMetricsUseCase` — **not wired to navigation** |
| `RiskDashboardViewModel` | `ui/analytics/RiskDashboardViewModel.kt` | Identifies risk invoices via `IdentifyRiskInvoicesUseCase` — **not wired** |
| `DunningNoticesViewModel` | `ui/dunning/DunningNoticesViewModel.kt` | Generates overdue payment notices via `GenerateDunningNoticesUseCase` — **not wired** |
| `InvoiceTemplateViewModel` | `ui/templates/InvoiceTemplateViewModel.kt` | Manages invoice templates — **not wired** |

### Repositories

| Repository | Interface | Implementation | Key Operations |
|---|---|---|---|
| `InvoiceRepository` | `domain/repository/InvoiceRepository.kt` | `data/repository/InvoiceRepositoryImpl.kt` | `saveInvoice`, `getAllInvoicesWithItems`, `getInvoiceWithItemsById`, `updateStatus`, `deleteInvoice`, `createCorrection` |
| `CustomerRepository` | `domain/repository/CustomerRepository.kt` | `data/repository/CustomerRepositoryImpl.kt` | `getAllCustomers`, `getCustomerById`, `saveCustomer`, `deleteCustomer` |
| `BusinessProfileRepository` | *(no interface)* | `data/repository/BusinessProfileRepository.kt` | `.activeProfile: Flow<BusinessProfile>`, `.profile: Flow<BusinessProfile>`, `getActiveBusinessId()`, `saveProfile()` |
| `DocumentRepository` | `domain/repository/DocumentRepository.kt` | `data/repository/DocumentRepositoryImpl.kt` | `saveDocument`, `getDocumentByInvoiceAndType`, `getAllDocuments` |
| `CurrencyRepository` | `domain/repository/CurrencyRepository.kt` | `data/repository/CurrencyRepositoryImpl.kt` | `getEnabledCurrencies`, `getExchangeRate`, `updateRates` — **not wired to UI** |

### Use Cases

| Use Case | File | Delegates To |
|---|---|---|
| `SaveInvoiceUseCase` | `domain/usecase/SaveInvoiceUseCase.kt` | `InvoiceRepository.saveInvoice()` |
| `GenerateAndSaveInvoiceUseCase` | `domain/usecase/GenerateAndSaveInvoiceUseCase.kt` | `InvoicePdfService.generateInvoice()` + `DocumentRepository.saveDocument()`, returns `Result<File>` |
| `GetCustomerAnalyticsUseCase` | `domain/usecase/GetCustomerAnalyticsUseCase.kt` | `CustomerRepository` + analytics aggregation |
| `SegmentCustomersUseCase` | `domain/usecase/SegmentCustomersUseCase.kt` | `CustomerAnalyticsSnapshot` data |
| `GenerateDunningNoticesUseCase` | `domain/usecase/GenerateDunningNoticesUseCase.kt` | Overdue invoice detection + notice generation |
| `IdentifyRiskInvoicesUseCase` | `domain/usecase/IdentifyRiskInvoicesUseCase.kt` | Risk scoring logic on invoices |
| `ForecastCashFlowUseCase` | `domain/usecase/ForecastCashFlowUseCase.kt` | Revenue projection from payment patterns |
| `GetPaymentAnalyticsUseCase` | `domain/usecase/GetPaymentAnalyticsUseCase.kt` | Aggregates payment data from `PaymentDao` |
| `GetRevenueMetricsUseCase` | `domain/usecase/GetRevenueMetricsUseCase.kt` | Computes revenue from invoice data |

### DAOs

| DAO | File | Key Queries |
|---|---|---|
| `InvoiceDao` | `data/local/InvoiceDao.kt` | `getInvoicesByBusinessId` (Flow), `getInvoiceWithItemsById` (Flow), `insert` (@Transaction), `updateInvoiceStatus`, `updatePdfPath`, `deleteInvoiceWithItems`, `getMaxSequenceForYear`, `getInvoiceGroupWithVersions` |
| `CustomerDao` | `data/local/CustomerDao.kt` | Customer CRUD, filtered by businessId |
| `AnalyticsDao` | `data/local/dao/AnalyticsDao.kt` | Analytics snapshot queries (⚠️ duplicate at `data/local/AnalyticsDao.kt`) |
| `DocumentDao` | `data/local/DocumentDao.kt` | Generated document records |
| `PaymentDao` | `data/local/PaymentDao.kt` | `InvoicePaymentEntity` CRUD |
| `CurrencyDao` | `data/local/CurrencyDao.kt` | Currency + exchange rate queries |

---

## Type System Mappings

### Monetary Values

| Field | Kotlin Type | Storage | Example | Display |
|---|---|---|---|---|
| `totalAmount` | `Long` | cents | `14999L` | `$149.99` |
| `taxAmount` | `Long` | cents | `1363L` | `$13.63` |
| `amountPaid` | `Long` | cents | `7500L` | `$75.00` |
| `unitPrice` | `Long` | cents | `9999L` | `$99.99` |
| `taxRate` | `Double` | fraction | `0.10` | `10%` |
| `defaultTaxRate` | `Float` | fraction | `0.10f` | `10%` (BusinessProfile) |

**Display conversion:**

```kotlin
// Long cents → display string
val displayAmount = String.format("%.2f", amount / 100.0)
// → "149.99"

// With currency symbol
val symbol = getCurrencySymbol(currencyCode)  // "$", "€", "£", etc.
val formatted = "$symbol$displayAmount"
```

**Critical:** Never store `Double` in monetary database columns. Migration v23→v24 fixed this.

### Date / Time

| Field | Type | Storage | Notes |
|---|---|---|---|
| `date` | `Long` | epoch millis | `System.currentTimeMillis()` |
| `dueDate` | `Long` | epoch millis | Invoice payment due |
| `createdAt` | `Long` | epoch millis | `System.currentTimeMillis()` |
| `updatedAt` | `Long` | epoch millis | Updated on every save |
| `lastUpdated` | `Long` | epoch millis | Exchange rate freshness |

**Formatting:**

```kotlin
SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(timestamp))
// → "Mar 04, 2026"
```

### Identifiers

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated by Room, 0 = new/unsaved |
| `transientId` | `String` (UUID) | Temporary ID for unsaved `LineItem` objects; never persisted |
| `businessProfileId` | `Long` | FK to `business_profiles.id` |
| `customerId` | `Long?` | Nullable FK — invoices can be issued without a linked customer |
| `parentInvoiceId` | `Long?` | Links correction/version invoices back to original |

### Invoice Status Enum

```kotlin
enum class InvoiceStatus {
    DRAFT,
    SENT,
    PAID,
    OVERDUE,
    PARTIALLY_PAID
}
```

Stored as `String` in the database (`status` column).

---

## Critical Code Paths

### Invoice Save (Critical Path)

```
CreateInvoiceViewModel.saveInvoice()
  → SaveInvoiceUseCase.invoke(invoice)
    → InvoiceRepositoryImpl.saveInvoice(invoice)
      → businessProfileRepository.getActiveBusinessId()   // must not throw
      → invoiceDao.getMaxSequenceForYear(year, businessId) // sequence assignment
      → invoice.copy(invoiceYear, invoiceSequence, version)
      → invoice.toEntity()                                  // mapper
      → items.map { it.toEntity(invoiceId = 0) }           // 0 because new
      → invoiceDao.insert(entity, items)                    // @Transaction
        → insertInvoice(entity) returns Long id
        → items.map { it.copy(invoiceId = id) }
        → insertLineItems(items)
```

**Failure points:**
- `getActiveBusinessId()` throws if no active profile exists
- `insertInvoice()` uses `OnConflictStrategy.ABORT` — duplicate invoice fails hard
- Mapper functions must correctly convert Long cents

### PDF Export (Critical Path)

```
InvoiceDetailViewModel.generateAndExportPdf()
  → businessProfileRepository.profile.first()             // MUST use .profile not .activeProfile
  → buildSnapshot(invoice, businessProfile)
  → GenerateAndSaveInvoiceUseCase(invoice, snapshot, isQuote=true)
  → GenerateAndSaveInvoiceUseCase(invoice, snapshot, isQuote=false)
    → InvoicePdfService.generateInvoice(snapshot, isQuote)
      → DocumentNamingUtils.generateFileName(...)
      → documentRepository.getDocumentByInvoiceAndType()  // check existing
      → PdfDocument() + canvas drawing
      → file.outputStream().use { pdfDocument.writeTo(it) }
      → documentRepository.saveDocument(entity)
  → documentManager.saveToDownloads(file, name)
```

**Failure points:**
- `businessProfileRepository.profile.first()` — historically caused crashes when wrong property name used (see Bug #1 in BUG_TRACKING_REGISTER.md)
- PDF canvas operations require `@RequiresApi(Build.VERSION_CODES.KITKAT)`
- `filesDir/documents/` directory must be created before writing

### Tax Calculation (Critical Path)

```kotlin
// In domain model — calculateTotal() on Invoice
val subtotal = items.sumOf { it.quantity * it.unitPrice }  // Long cents (approx)
val taxAmount = if (isTaxRegistered) (subtotal * taxRate).toLong() else 0L
val totalAmount = subtotal + taxAmount
```

**Key rule:** `taxRate` is a `Double` (e.g., `0.10`), but the result is always cast to `Long` cents. Never display raw Long values — always divide by 100.0.

### Customer Selection During Invoice Creation

```
CreateInvoiceScreen renders customer picker
  → CustomerViewModel.uiState (List<Customer>)
  → User selects → CreateInvoiceViewModel.selectCustomer(customer)
  → _invoice.update { it.copy(
        customerId = customer.id,
        customerName = customer.name,
        customerAddress = customer.address,
        customerEmail = customer.email
    ) }
```

Note: Customer fields are **denormalized** into the invoice at creation time. Subsequent changes to the customer record do NOT update existing invoices.
