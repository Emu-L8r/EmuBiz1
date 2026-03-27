# 🎨 PAYMENT COMPONENT ARCHITECTURE DIAGRAMS

**Visual Reference for Issue Analysis**

---

## 📊 ISSUE #1: GUI2 PAYMENT TAB CRASH - FLOW DIAGRAM

### Current Broken Flow:

```
┌──────────────────────────────────────────────────────────────┐
│ InvoiceDetailScreenV2.kt (GUI2)                             │
│  ├─ Invoice clicked                                          │
│  └─ Tabs: Details | Items | Payment History ← User clicks   │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ PaymentHistoryTab()                                          │
│  └─ PaymentHistoryScreen(                                    │
│       invoiceId = invoice.invoice.id,                        │
│       modifier = ...                                         │
│     )                                                        │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ PaymentHistoryScreen Composable                              │
│                                                              │
│  val viewModel: PaymentHistoryViewModel = hiltViewModel()   │
│                                                              │
│  ❌ PROBLEM: Hilt tries to get "invoiceId" from             │
│     SavedStateHandle["invoiceId"] but it's NULL!            │
│     No explicit parameter passed to Screen!                 │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ PaymentHistoryViewModel Constructor                          │
│                                                              │
│  @Inject constructor(                                        │
│    savedStateHandle: SavedStateHandle  ← Receives NULL      │
│  )                                                           │
│                                                              │
│  private val invoiceIdFromHandle: Long? =                   │
│    savedStateHandle.get<Long>("invoiceId")  ← Returns NULL! │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ paymentHistory Flow Property                                 │
│                                                              │
│  val paymentHistory: Flow<PaymentHistoryUiState> =           │
│    if (invoiceIdFromHandle != null && invoiceIdFromHandle > 0) {
│      // FALSE BRANCH - invoiceIdFromHandle is null!          │
│    } else {                                                   │
│      flowOf(Error("No invoice ID provided", -1L))           │
│      // ❌ CRASH: UI shows Error state                       │
│    }                                                         │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ Screen receives Error state                                  │
│                                                              │
│  when (uiState) {                                            │
│    is PaymentHistoryUiState.Error -> {                       │
│      // Show error message with negative invoiceId (-1)      │
│      // ❌ BAD USER EXPERIENCE                               │
│    }                                                         │
│  }                                                           │
└──────────────────────────────────────────────────────────────┘

⚠️ ROOT CAUSE: invoiceId parameter never passed from screen
              to ViewModel, relying on SavedStateHandle which
              doesn't contain the value!
```

### Fixed Flow (Approach 1):

```
┌──────────────────────────────────────────────────────────────┐
│ InvoiceDetailScreenV2.kt (GUI2)                             │
│  ├─ Invoice clicked                                          │
│  └─ Tabs: Details | Items | Payment History ← User clicks   │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ PaymentHistoryTab()                                          │
│  └─ PaymentHistoryScreen(                                    │
│       invoiceId = invoice.invoice.id,  ✅ PASS EXPLICITLY   │
│       businessId = businessId,         ✅ ADDED              │
│       modifier = ...                                         │
│     )                                                        │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ PaymentHistoryScreen Composable                              │
│                                                              │
│  fun PaymentHistoryScreen(                                   │
│    invoiceId: Long,     ✅ Parameter received                │
│    businessId: Long,    ✅ Multi-tenant safety               │
│    ...                                                       │
│  ) {                                                         │
│    // Validate parameters                                    │
│    require(invoiceId > 0)                                    │
│    require(businessId > 0)                                   │
│                                                              │
│    // Create factory with explicit parameters               │
│    val viewModelFactory = viewModelFactory {                 │
│      PaymentHistoryViewModel(invoiceId, businessId, repo)   │
│    }                                                         │
│    val viewModel = viewModel(factory = viewModelFactory)     │
│  }                                                           │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ PaymentHistoryViewModel Constructor                          │
│                                                              │
│  @HiltViewModel                                              │
│  class PaymentHistoryViewModel(                              │
│    private val invoiceId: Long,        ✅ Explicit param     │
│    private val businessId: Long,       ✅ Explicit param     │
│    private val repository: InvoiceRepository,                │
│    savedStateHandle: SavedStateHandle                        │
│  )                                                           │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ paymentHistory Flow Property                                 │
│                                                              │
│  val paymentHistory: Flow<PaymentHistoryUiState> =           │
│    if (invoiceId > 0 && businessId > 0) {                    │
│      createPaymentHistoryFlow(invoiceId, businessId)         │
│      // ✅ TRUE BRANCH - params are valid!                   │
│    } else {                                                   │
│      flowOf(Error("Invalid parameters", -1L))               │
│    }                                                         │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ createPaymentHistoryFlow(invoiceId, businessId)             │
│                                                              │
│  private fun createPaymentHistoryFlow(                       │
│    invoiceId: Long,                                          │
│    businessId: Long                                          │
│  ): Flow<PaymentHistoryUiState> {                            │
│                                                              │
│    return repository.getInvoiceWithItemsById(invoiceId)      │
│      .flatMapLatest { invoice ->                             │
│        if (invoice == null) {                                │
│          flowOf(NotFound(invoiceId))                         │
│        } else {                                              │
│          // ✅ Pass businessId for multi-tenant safety       │
│          repository.observePaymentHistory(                   │
│            invoiceId,                                        │
│            businessId                                        │
│          )                                                   │
│        }                                                     │
│      }                                                       │
│      .catch { ... emit(Error(...)) ... }                     │
│  }                                                           │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌──────────────────────────────────────────────────────────────┐
│ Screen receives Success state                                │
│                                                              │
│  when (uiState) {                                            │
│    is PaymentHistoryUiState.Success -> {                     │
│      // ✅ Display payment history correctly                 │
│      PaymentHistoryContent(successState)                     │
│    }                                                         │
│  }                                                           │
└──────────────────────────────────────────────────────────────┘

✅ FIXED: Parameters passed explicitly, validated, and used!
```

---

## 📊 ISSUE #2: GUI1 PAYMENT HISTORY SCOPE PROBLEM - DATA DIAGRAM

### Current Broken Data Flow:

```
Database Layer:
┌─────────────────────────────────────────────────────────┐
│ invoice_payment_snapshots table                         │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ invoiceId │ businessId │ amount │ status │ date    │ │
│ ├─────────────────────────────────────────────────────┤ │
│ │ 100       │ 1          │ $500   │ PAID   │ 3/20    │ │
│ │ 101       │ 1          │ $250   │ PAID   │ 3/21    │ │ ← All for business 1
│ │ 102       │ 1          │ $1000  │ UNPAID │ 3/22    │ │
│ │ 103       │ 1          │ $750   │ PAID   │ 3/23    │ │
│ │ 200       │ 2          │ $300   │ PAID   │ 3/20    │ │ ← Different business
│ │ 201       │ 2          │ $600   │ PAID   │ 3/21    │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
           │
           ▼
Query Layer (BROKEN):
┌─────────────────────────────────────────────────────────┐
│ InvoicePaymentDao.observeAllSnapshots(businessId=1)    │
│                                                         │
│ SELECT * FROM invoice_payment_snapshots                 │
│ WHERE businessProfileId = 1                             │
│ ❌ NO invoiceId filter!                                 │
│ ❌ NO customerId filter!                                │
│ ORDER BY dueDate ASC                                    │
└─────────────────────────────────────────────────────────┘
           │
           ▼
Returned Data (WRONG):
┌─────────────────────────────────────────────────────────┐
│ For User viewing Invoice #100 payment history:          │
│                                                         │
│ ✅ Invoice 100 - $500 (Correct)                         │
│ ❌ Invoice 101 - $250 (Wrong - different invoice)       │
│ ❌ Invoice 102 - $1000 (Wrong - different invoice)      │
│ ❌ Invoice 103 - $750 (Wrong - different invoice)       │
│                                                         │
│ Shows 4 payment records instead of 1!                   │
│ User sees ALL business payments instead of              │
│ just Invoice #100 payments!                             │
└─────────────────────────────────────────────────────────┘

⚠️ SCOPE ISSUE: Query returns all business payments,
               not just payments for selected invoice!
```

### Fixed Data Flow (Approach 3):

```
Database Layer:
┌─────────────────────────────────────────────────────────┐
│ invoice_payment_snapshots table                         │
│                                                         │
│ ┌─────────────────────────────────────────────────────┐ │
│ │ invoiceId │ businessId │ amount │ status │ date    │ │
│ ├─────────────────────────────────────────────────────┤ │
│ │ 100       │ 1          │ $500   │ PAID   │ 3/20    │ │
│ │ 101       │ 1          │ $250   │ PAID   │ 3/21    │ │
│ │ 102       │ 1          │ $1000  │ UNPAID │ 3/22    │ │
│ │ 103       │ 1          │ $750   │ PAID   │ 3/23    │ │
│ │ 200       │ 2          │ $300   │ PAID   │ 3/20    │ │
│ │ 201       │ 2          │ $600   │ PAID   │ 3/21    │ │
│ └─────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
           │
           ▼
Query Layer (FIXED):
┌─────────────────────────────────────────────────────────┐
│ InvoicePaymentDao.observePaymentHistory(                │
│   invoiceId=100,                                        │
│   businessId=1                                          │
│ )                                                       │
│                                                         │
│ SELECT * FROM invoice_payment_snapshots                 │
│ WHERE invoiceId = 100          ✅ Filtered by invoice   │
│   AND businessProfileId = 1    ✅ Multi-tenant safety   │
│ ORDER BY lastUpdatedMs DESC                             │
└─────────────────────────────────────────────────────────┘
           │
           ▼
Returned Data (CORRECT):
┌─────────────────────────────────────────────────────────┐
│ For User viewing Invoice #100 payment history:          │
│                                                         │
│ ✅ Invoice 100 - $500 (Correct)                         │
│                                                         │
│ Shows 1 payment record - EXACTLY what belongs to        │
│ Invoice #100! Multi-tenant isolation preserved!         │
└─────────────────────────────────────────────────────────┘

✅ FIXED: Query filters by both invoiceId AND businessId!
```

---

## 🏗️ NESTED FLOWS PROBLEM DIAGRAM (Issue #1 Variant)

### Current Code (Problematic):

```kotlin
private fun createPaymentHistoryFlow(invoiceId: Long): Flow<PaymentHistoryUiState> {
    return kotlinx.coroutines.flow.flow {
        emit(PaymentHistoryUiState.Loading)
        
        try {
            invoiceRepository.getInvoiceWithItemsById(invoiceId)
                .collect { invoice ->  // ← OUTER collect
                    if (invoice == null) {
                        emit(PaymentHistoryUiState.NotFound(invoiceId))
                    } else {
                        invoiceRepository.observePaymentHistory(invoiceId)
                            .collect { snapshots ->  // ← INNER collect
                                // Transform snapshots
                                emit(PaymentHistoryUiState.Success(...))
                            }
                    }
                }
        } catch (e: Exception) {
            emit(PaymentHistoryUiState.Error(...))
        }
    }
}
```

**Problem Visualization:**

```
┌─────────────────────────────────────────────────────────┐
│ Outer Flow (createPaymentHistoryFlow)                   │
│                                                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │ Outer collect block                               │  │
│  │                                                   │  │
│  │  getInvoiceWithItemsById(invoiceId)               │  │
│  │    .collect { invoice ->  ← BLOCKS HERE            │  │
│  │                                                   │  │
│  │    ┌─────────────────────────────────────────┐   │  │
│  │    │ Inner Flow                              │   │  │
│  │    │                                         │   │  │
│  │    │ observePaymentHistory(invoiceId)        │   │  │
│  │    │   .collect { snapshots ->  ← BLOCKS HERE│   │  │
│  │    │     emit(Success(...))                  │   │  │
│  │    │   }  ← Inner collect COMPLETES          │   │  │
│  │    └─────────────────────────────────────────┘   │  │
│  │                                                   │  │
│  │  }  ← Outer collect COMPLETES                     │  │
│  └───────────────────────────────────────────────────┘  │
│                                                         │
│  ❌ PROBLEM: Nested .collect() blocks can cause:        │
│     1. Deadlocks if flows emit concurrently            │
│     2. Lost emissions if outer completes early         │
│     3. Improper cancellation propagation               │
│     4. Memory leaks if exception in inner scope        │
└─────────────────────────────────────────────────────────┘
```

### Fixed Code (Using flatMapLatest):

```kotlin
private fun createPaymentHistoryFlow(invoiceId: Long): Flow<PaymentHistoryUiState> {
    return invoiceRepository.getInvoiceWithItemsById(invoiceId)
        .flatMapLatest { invoice ->  // ← NO nested collect
            if (invoice == null) {
                flowOf(PaymentHistoryUiState.NotFound(invoiceId))
            } else {
                invoiceRepository.observePaymentHistory(invoiceId)
                    .map { snapshots ->  // ← Transform, don't collect
                        PaymentHistoryUiState.Success(...)
                    }
            }
        }
        .catch { e ->
            emit(PaymentHistoryUiState.Error(...))
        }
        .onStart {
            emit(PaymentHistoryUiState.Loading)
        }
}
```

**Fixed Visualization:**

```
┌──────────────────────────────────────────────────────────┐
│ flatMapLatest Operator (Reactive Composition)            │
│                                                          │
│ getInvoiceWithItemsById(invoiceId)                       │
│      │                                                   │
│      ├─→ [Invoice found]                                │
│      │      │                                            │
│      │      ▼                                            │
│      │   flatMapLatest { invoice ->                      │
│      │      │                                            │
│      │      observePaymentHistory(invoiceId)             │
│      │           │                                       │
│      │           ├─→ [Snapshots]                         │
│      │           │      │                                │
│      │           │      ▼                                │
│      │           │   map { snapshots ->                  │
│      │           │      Success(...)                     │
│      │           │   }                                   │
│      │           │                                       │
│      │           ├─→ [Error]                             │
│      │                  │                                │
│      │                  ▼                                │
│      │            catch { emit(Error(...)) }             │
│      │                                                   │
│      ├─→ [Invoice not found]                             │
│             │                                            │
│             ▼                                            │
│          NotFound(invoiceId)                             │
│                                                          │
│ ✅ BENEFITS:                                             │
│  • No nested .collect blocks                             │
│  • Proper flow cancellation                              │
│  • Automatic memory management                          │
│  • Better error propagation                              │
│  • More reactive and composable                          │
└──────────────────────────────────────────────────────────┘
```

---

## 🔐 MULTI-TENANT SAFETY DIAGRAM

### Current Risk:

```
Business 1 (ID=111)          Business 2 (ID=222)
    │                             │
    ├─ Invoice 100                ├─ Invoice 200
    │   └─ Payments: $500, $250   │   └─ Payments: $300, $600
    │                             │
    └─ Invoice 101                └─ Invoice 201
        └─ Payments: $1000            └─ Payments: $400

❌ VULNERABLE QUERY:
   SELECT * FROM invoice_payment_snapshots
   WHERE businessProfileId = 111
   
   Returns: 100, 101, 200, 201 ← SHOULD NOT INCLUDE 200, 201!
            (all invoices for ALL businesses in table!)

Business 2 user can potentially see Business 1's invoice data!
```

### After Fix:

```
Business 1 (ID=111)          Business 2 (ID=222)
    │                             │
    ├─ Invoice 100  ✅            ├─ Invoice 200  ✅
    │   └─ Payments: $500, $250   │   └─ Payments: $300, $600
    │                             │
    └─ Invoice 101  ✅            └─ Invoice 201  ✅
        └─ Payments: $1000            └─ Payments: $400

✅ PROTECTED QUERY:
   SELECT * FROM invoice_payment_snapshots
   WHERE businessProfileId = 111
     AND invoiceId = 100
   
   Returns: ONLY Invoice 100 payments ($500, $250)
           Isolation preserved! Multi-tenant safe!

Business 2 user can NEVER see Business 1's data!
```

---

## 📈 IMPACT SUMMARY

```
┌─────────────────────────────────────────────────────────┐
│                    ISSUE IMPACT                         │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ Issue #1: GUI2 Crash                                    │
│ ─────────────────────                                   │
│ Impact:  Users cannot view payment history              │
│ Scope:   Any user accessing invoice in GUI2             │
│ Risk:    High (breaks feature completely)               │
│ Fix:     Parameter injection + Explicit passing         │
│ Time:    2-3 hours                                      │
│                                                         │
│ Issue #2: GUI1 Scope Problem                            │
│ ──────────────────────────                              │
│ Impact:  Data leakage (see all payments, not filtered)  │
│ Scope:   Any user in GUI1 payment history               │
│ Risk:    HIGH (data visibility, compliance issue)       │
│ Fix:     Repository filtering with invoiceId filter     │
│ Time:    1-2 hours                                      │
│                                                         │
│ Combined Impact:                                        │
│ ─────────────────                                       │
│ Total Fix Time: 3-5 hours                               │
│ Total Risk Level: MEDIUM (breaking changes)             │
│ Recommended: Fix both issues together                   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

**Diagrams Version:** 1.0  
**Last Updated:** March 27, 2026  
**Purpose:** Visual Reference for Issue Understanding  

