# Testing Strategy

**Date:** 2026-03-04  
**Project:** Bizap / EmuBiz  
**Test Infrastructure:** JUnit4, MockK, Coroutines Test, Arch Core Test, Robolectric, Compose Testing

---

## Overview

As of 2026-03-04, **zero automated test files** exist in any test source set. The testing infrastructure is fully configured in `build.gradle` — writing tests requires no additional setup. This document covers both manual testing procedures for the current app and a roadmap for implementing automated tests.

---

## Manual Testing Procedures

### MT-001: Business Profile Setup

**Preconditions:** Fresh install or cleared app data

**Steps:**

1. Launch app
2. App should prompt for business profile if none exists
3. Navigate to Settings → Business Profile
4. Fill in: Business Name, ABN (11 digits), Email, Phone, Address, Website
5. Fill in bank details: BSN Number, Account Number, Account Name, Bank Name
6. Toggle "Tax Registered" → verify default tax rate (10%) appears
7. Upload a logo image
8. Tap "Save"

**Expected Results:**
- Profile saved without error
- Dashboard header shows business name and ABN
- Invoice PDFs include the business details

**Edge Cases:**
- ABN field with < 11 digits → should show validation error
- Empty business name → should show validation error
- Logo > 10 MB → behaviour should be defined (compression or rejection)

---

### MT-002: Multi-Business Switching

**Preconditions:** At least 2 business profiles created

**Steps:**

1. On Dashboard, tap the `⇄` (SwapHoriz) icon in the top-right
2. `BusinessSwitcherDialog` appears listing all business profiles
3. Select a different business
4. Dialog closes, Dashboard header updates to new business name/ABN
5. Invoice list updates to show only that business's invoices
6. Customer list updates to show only that business's customers

**Expected Results:**
- All data correctly scoped to newly selected business
- No data leakage from previous business

**Edge Cases:**
- Switching business while in Invoice Detail → back stack should be cleared or Invoice Detail should update/close

---

### MT-003: Customer Creation

**Steps:**

1. Navigate to Customers tab
2. Tap "Add Customer" / FAB
3. Fill in: Name, Email, Phone, Address
4. Save

**Expected Results:**
- Customer appears in list
- Customer count on Dashboard increments

**Edge Cases:**
- Duplicate email → app should not crash (Room REPLACE strategy will upsert)
- Empty name → validation should prevent save
- Very long name (200+ chars) → should truncate or wrap correctly in list

---

### MT-004: Invoice Creation

**Steps:**

1. Navigate to Invoices tab → Tap "New Invoice"
2. Select customer from picker (or proceed without customer)
3. Add 3 line items with different quantities and prices
4. Set invoice header, notes, footer
5. Set due date to 30 days from today
6. Toggle "Is Quote" to test quote flow
7. Save

**Expected Results:**
- Invoice appears in list with status DRAFT
- Invoice number format: `INV-2026-NNNNNN`
- Total amount = sum of (quantity × unitPrice) + tax
- Tax only applied if business is tax registered

**Edge Cases:**
- Zero line items → should prevent save or save as empty invoice
- Negative unit price → should validate
- Very large amounts (e.g., $999,999.99) → verify Long cents won't overflow (`99999999L` is well within `Long` range)
- Invoice without a customer → should succeed; customer fields blank on PDF

---

### MT-005: Invoice Editing

**Steps:**

1. Open an existing invoice
2. Tap "Edit"
3. Change the header text
4. Add one new line item
5. Delete an existing line item
6. Change a line item's price
7. Tap "Save"

**Expected Results:**
- Invoice Detail refreshes with all changes
- Logcat: `"Persist successful."` from `EditInvoiceViewModel`
- Invoice total recalculates correctly

**Edge Cases:**
- Edit with no changes → Save should still succeed (no-op)
- Delete all line items → Total = $0.00; should save
- Change date to past → No validation currently; should save
- Navigate back (discard) → Original data preserved, no partial save

---

### MT-006: PDF Generation and Export

**Steps:**

1. Open an invoice with at least 2 line items
2. Tap "Export PDF"
3. If "Overwrite" dialog appears → select "Overwrite Existing"
4. Wait for completion
5. Check `Snackbar`: "Success: Documents exported to Downloads/Bizap"
6. Open phone's Files app → Downloads/Bizap → verify PDF exists
7. Open PDF → verify all fields are correct

**Expected Results:**
- PDF contains business name, ABN, email, phone, address
- PDF contains customer name, address, email
- PDF contains invoice number (formatted)
- PDF contains line items table
- PDF contains subtotal, tax (if applicable), total
- Total amount displays correctly (e.g., `$149.99` not `$14999.00`)
- PDF also generated for Quote version

**Edge Cases:**
- No logo → PDF renders without logo section
- Very long business name → should wrap or truncate
- Currency other than AUD → symbol should display correctly (when multi-currency wired)
- Invoice with 20+ line items → PDF may overflow single page (known limitation of current renderer)

---

### MT-007: PDF Share

**Steps:**

1. Open an invoice
2. Tap "Share PDF"
3. Android share sheet appears
4. Select an app (e.g., Gmail, WhatsApp)
5. Verify file is attached/shared

**Expected Results:**
- Share sheet shows PDF file name
- Selected app receives the PDF as `application/pdf`
- FileProvider URI is used (not direct file path)

---

### MT-008: Invoice Status Management

**Steps:**

1. Open an invoice with status DRAFT
2. Change status to SENT → verify badge updates
3. Change status to PAID → verify badge updates
4. Open overdue invoice → verify OVERDUE badge
5. Record partial payment → verify PARTIALLY_PAID status

**Expected Results:**
- Status badge colour reflects status (e.g., green for PAID, red for OVERDUE)
- `invoiceRepo.updateInvoiceStatus()` called with correct `InvoiceStatus` enum value

---

### MT-009: Document Vault

**Steps:**

1. Generate PDFs for at least 2 invoices
2. Navigate to Document Vault (via settings or menu)
3. Verify both PDFs appear in list
4. Tap a PDF → should open PDF viewer or offer sharing

**Expected Results:**
- Document Vault opens without crash (regression for BUG-003)
- All generated documents listed with file name, date, invoice reference
- Tapping document opens it in system PDF viewer

---

### MT-010: Theme Settings

**Steps:**

1. Navigate to Settings → Theme
2. Toggle between Light / Dark / System Default
3. Select a colour theme

**Expected Results:**
- App recomposes with new theme immediately
- Setting persists across app restart (DataStore)

---

## Edge Cases by Domain

### Empty States

| Screen | Empty State Condition | Expected Behaviour |
|---|---|---|
| Customer List | No customers | Show "No customers yet" placeholder, FAB visible |
| Invoice List | No invoices | Show "No invoices yet" placeholder, FAB visible |
| Document Vault | No generated PDFs | Show "No documents" placeholder |
| Dashboard | New business, no data | Show 0 customers, $0.00 revenue |

### Large Datasets

| Scenario | Test Method | Expected Behaviour |
|---|---|---|
| 500+ customers | Seed via ADB / direct DB insert | List scrolls smoothly, no ANR |
| 1000+ invoices | Seed via ADB | Invoice list uses `LazyColumn`; only visible items rendered |
| 20+ line items on one invoice | Add manually in CreateInvoice | All items saved and displayed; PDF may overflow page |

### Invalid Inputs

| Field | Invalid Input | Expected |
|---|---|---|
| ABN | 10 digits (not 11) | Validation error |
| Invoice due date | Before invoice date | No current validation; should warn |
| Line item price | 0 or negative | Should warn or prevent save |
| Line item quantity | 0 | Should warn or prevent save |
| Logo | Non-image file | Should reject with error |

---

## Expected Behaviours by Scenario

### Invoice Number Assignment

| Scenario | Expected Invoice Number |
|---|---|
| First invoice for Business A in 2026 | `INV-2026-000001` |
| Second invoice for Business A in 2026 | `INV-2026-000002` |
| First invoice for Business B in 2026 | `INV-2026-000001` (scoped per business) |
| Invoice correction (v2) | Same number with version label |

### Tax Calculation

| Business Setting | Tax Rate | Line Item Total | Expected Tax | Expected Grand Total |
|---|---|---|---|---|
| Not tax registered | 0% | $100.00 | $0.00 | $100.00 |
| Tax registered, 10% GST | 10% | $100.00 | $10.00 | $110.00 |
| Tax registered, 0% (manual override) | 0% | $100.00 | $0.00 | $100.00 |

### Payment Status Transitions

| Amount Paid | Total Amount | Expected Status |
|---|---|---|
| $0 | $149.99 | SENT or DRAFT |
| $50.00 | $149.99 | PARTIALLY_PAID |
| $149.99 | $149.99 | PAID |
| $149.99 | $99.99 (overpayment) | PAID (no over-payment validation currently) |

---

## Regression Test Checklist

Run this checklist after any significant code change:

### BUG-001 Regression (Business Profile method name)

- [ ] Create new invoice from scratch → Save succeeds → `"🔢 Assigning scoped invoice number"` in Logcat
- [ ] Open invoice → Export PDF → Success snackbar shown → File exists in Downloads/Bizap
- [ ] Edit invoice → Save → Changes visible in Invoice Detail
- [ ] Logcat: No `NoSuchMethodException` or `AbstractMethodError` from `BusinessProfileRepository`

### BUG-002 Regression (Double vs Long monetary)

- [ ] App launches without `IllegalStateException` crash
- [ ] Logcat at startup: Room database opens cleanly (`D/Room: database opened`)
- [ ] Monetary values display correctly (e.g., invoice for $149.99 shows `$149.99`, not `$14999.00`)
- [ ] Creating a payment snapshot does not crash

### BUG-003 Regression (Document Vault crash)

- [ ] Navigate to Document Vault → Screen opens
- [ ] Previously generated PDFs are listed
- [ ] No `NullPointerException` in Logcat from `DocumentVaultViewModel`

### BUG-004 Regression (Edit Invoice save)

- [ ] Edit invoice header → Save → Detail screen shows new header
- [ ] Edit line item price → Save → Total updates correctly
- [ ] Add new line item → Save → New item appears in Detail
- [ ] Logcat: `"Persist successful."` from `EditInvoiceViewModel`

### BUG-005 Regression (Line item ID collision)

- [ ] Create invoice with 3 line items (all new)
- [ ] Delete middle item → only middle item removed, other 2 remain
- [ ] Edit first item's description → only first item changes
- [ ] Save → All 2 remaining items correct in database

---

## Logcat Patterns to Monitor

### Success Patterns (Look for these to confirm things are working)

| Tag | Message Pattern | Meaning |
|---|---|---|
| `InvoiceRepositoryImpl` | `🔢 Assigning scoped invoice number: INV-...` | New invoice created with correct numbering |
| `EditInvoiceViewModel` | `Persist successful.` | Invoice edit saved |
| `InvoiceDetailViewModel` | `Success: Documents exported to Downloads/Bizap` | PDF export complete |
| `BizapApp` | `Timber initialized` | App startup successful |
| `Room` | `database opened` | DB schema matches entities |

### Error Patterns (Watch for these — indicate a problem)

| Tag | Message Pattern | Likely Cause |
|---|---|---|
| `EditInvoiceViewModel` | `Save failed: ...` | Repository error, check message |
| Any | `NullPointerException` | Null reference — check call site |
| `Room` | `Migration didn't properly handle` | Schema mismatch — new entity vs old DB |
| `Room` | `expected INTEGER found REAL` | Monetary type mismatch (BUG-002 pattern) |
| `InvoicePdfService` | `Exception during PDF generation` | PDF canvas error or filesystem issue |
| `WorkManager` | `Worker result FAILURE` | `ExchangeRateWorker` failed to fetch rates |
| Any | `AbstractMethodError` | Interface method not implemented in class |

### Recommended Logcat Filter

```
tag:BizapApp OR tag:CreateInvoiceViewModel OR tag:EditInvoiceViewModel OR 
tag:InvoiceDetailViewModel OR tag:InvoiceRepositoryImpl OR tag:InvoicePdfService OR 
tag:DocumentVaultViewModel OR tag:BusinessProfileRepository OR tag:Room
```

---

## Automated Testing Recommendations

### Priority 1: Unit Tests (Use JUnit4 + MockK)

These are the most valuable tests to write first — pure Kotlin, no Android framework needed:

**`SaveInvoiceUseCaseTest`**
```kotlin
// Test file location: app/src/test/java/com/emul8r/bizap/domain/usecase/SaveInvoiceUseCaseTest.kt
class SaveInvoiceUseCaseTest {
    private val mockRepository = mockk<InvoiceRepository>()
    private val useCase = SaveInvoiceUseCase(mockRepository)

    @Test
    fun `saveInvoice delegates to repository`() = runTest {
        val invoice = /* create test invoice */
        coEvery { mockRepository.saveInvoice(invoice) } returns 1L
        val result = useCase(invoice)
        coVerify { mockRepository.saveInvoice(invoice) }
    }
}
```

**`InvoiceRepositoryImplTest` (monetary logic)**
```kotlin
// Test: cent conversion, invoice numbering
@Test
fun `saveInvoice assigns correct sequence number for new invoice`() = runTest {
    coEvery { mockDao.getMaxSequenceForYear(2026, 1L) } returns 5
    // expect invoiceSequence = 6
}
```

**Monetary calculation tests:**
```kotlin
@Test
fun `tax calculation uses Long cents`() {
    val subtotal = 10000L  // $100.00
    val taxRate = 0.10
    val expected = 1000L   // $10.00
    assertEquals(expected, (subtotal * taxRate).toLong())
}
```

### Priority 2: ViewModel Tests (Use JUnit4 + MockK + Arch Core Test + Coroutines Test)

```kotlin
// Test file: app/src/test/java/com/emul8r/bizap/ui/invoices/EditInvoiceViewModelTest.kt
@get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

class EditInvoiceViewModelTest {
    @Test
    fun `saveInvoice emits BackToInvoiceDetail on success`() = runTest {
        // Setup mocks, call saveInvoice(), verify navigationEvent
    }

    @Test
    fun `addLineItem adds item with unique transientId`() = runTest {
        viewModel.addLineItem()
        viewModel.addLineItem()
        val items = (viewModel.uiState.value as Success).invoice.items
        assertEquals(2, items.size)
        assertNotEquals(items[0].transientId, items[1].transientId)
    }
}
```

### Priority 3: Repository Integration Tests (Use Robolectric + Room in-memory)

```kotlin
// Test file: app/src/test/java/com/emul8r/bizap/data/repository/InvoiceRepositoryImplTest.kt
@RunWith(RobolectricTestRunner::class)
class InvoiceRepositoryImplTest {
    private lateinit var db: BizapDatabase
    private lateinit var repository: InvoiceRepositoryImpl

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BizapDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Test
    fun `saveInvoice persists to database`() = runTest {
        val invoice = /* test invoice */
        val id = repository.saveInvoice(invoice)
        val retrieved = db.invoiceDao().getInvoiceWithItemsById(id).first()
        assertNotNull(retrieved)
    }
}
```

### Priority 4: Compose UI Tests (Use Compose Testing)

```kotlin
// Test file: app/src/androidTest/java/com/emul8r/bizap/ui/invoices/CreateInvoiceScreenTest.kt
class CreateInvoiceScreenTest {
    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun `add line item button adds item to list`() {
        composeTestRule.setContent {
            CreateInvoiceScreen(navController = rememberNavController())
        }
        composeTestRule.onNodeWithText("Add Item").performClick()
        composeTestRule.onAllNodesWithTag("line_item_row").assertCountEquals(1)
    }
}
```

### Test Coverage Targets

| Layer | Target Coverage | Tools |
|---|---|---|
| Domain Use Cases | 90% | JUnit4 + MockK |
| Repository Implementations | 70% | Robolectric + Room in-memory |
| ViewModels | 70% | JUnit4 + MockK + Arch Core |
| DAO queries | 60% | Robolectric + Room in-memory |
| Compose Screens (critical paths) | 30% | Compose Testing |
| **Overall** | **~60%** | |

### Quick Start: Writing the First Test

The test source sets are already configured. Create the first test file:

```
app/src/test/java/com/emul8r/bizap/domain/usecase/SaveInvoiceUseCaseTest.kt
```

Run with:

```bash
./gradlew test
```

No additional dependencies or configuration required — all testing libraries are already declared in `build.gradle`.
