# Path 2: Unified Dialog State Architecture - Visual Guide

## System Architecture Diagram

```
┌───────────────────────────────────────────────────────────────────────────────┐
│                            INVOICE DETAIL SCREEN                              │
│                          (InvoiceDetailScreenV2.kt)                           │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │  Scaffold/TopAppBar                                                     │ │
│  │  ┌──────────────┬─────────────────┬──────────────────┐                 │ │
│  │  │ [←] Back     │   Invoice Detail │ [📄] [💳] [📋]  │                 │ │
│  │  │              │                 │                  │                 │ │
│  │  │              │                 │  ↓    ↓     ↓    │                 │ │
│  │  │              │                 │ PDF  Payment Status                │ │
│  │  └──────────────┴─────────────────┴──────────────────┘                 │ │
│  │                                                                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐   │ │
│  │  │  Content Display (Main Invoice View)                            │   │ │
│  │  │  - Details Tab                                                  │   │ │
│  │  │  - Items Tab                                                    │   │ │
│  │  │  - Payment History Tab                                          │   │ │
│  │  └─────────────────────────────────────────────────────────────────┘   │ │
│  │                                                                         │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐   │ │
│  │  │  Dialog Layer (Rendered based on uiState.dialogState)           │   │ │
│  │  │                                                                 │   │ │
│  │  │  if (dialogState is PaymentDialog)                            │   │ │
│  │  │      → RecordPaymentDialogV2(...)                             │   │ │
│  │  │                                                                 │   │ │
│  │  │  if (dialogState is StatusMenu)                               │   │ │
│  │  │      ��� StatusUpdateMenuV2(...)                                │   │ │
│  │  │                                                                 │   │ │
│  │  │  when (dialogState) {                                          │   │ │
│  │  │      is PdfExport.Loading → Loading dialog                    │   │ │
│  │  │      is PdfExport.Success → Success dialog                    │   │ │
│  │  │      is PdfExport.Error → Error dialog                        │   │ │
│  │  │  }                                                              │   │ │
│  │  └─────────────────────────────────────────────────────────────────┘   │ │
│  │                                                                         │ │
│  │  All dialogs read from: state.dialogState, state.paymentLoading,      │ │
│  │  state.paymentError, state.statusUpdateError                          │ │
│  │                                                                         │ │
│  │  All dialogs call: viewModel.openPaymentDialog(), etc.               │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────────────────┘
                                      ↓ (StateFlow)
                                      ↓
┌───────────────────────────────────────────────────────────────────────────────┐
│                     VIEWMODEL LAYER                                           │
│              (InvoiceDetailViewModelV2.kt)                                    │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐ │
│  │  uiState: StateFlow<InvoiceDetailUiStateV2>                             │ │
│  │                                                                         │ │
│  │  Success {                                                              │ │
│  │      invoice: InvoiceWithItems                                         │ │
│  │                                                                         │ │
│  │      ╔═══════════════════════════════════════════════╗                │ │
│  │      ║  NEW: Dialog State (Single Source of Truth)  ║                │ │
│  │      ║───────────────────────────────────────────────║                │ │
│  │      ║  dialogState: DialogState                    ║                │ │
│  │      ║    ├─ None                                    ║                │ │
│  │      ║    ├─ PaymentDialog                           ║                │ │
│  │      ║    ├─ StatusMenu                              ║                │ │
│  │      ║    └─ PdfExport                               ║                │ │
│  │      ║       ├─ Loading                              ║                │ │
│  │      ║       ├─ Success(file)                        ║                │ │
│  │      ║       └─ Error(message)                       ║                │ │
│  │      ║                                                ║                │ │
│  │      ║  paymentLoading: Boolean                      ║                │ │
│  │      ║  paymentError: String?                        ║                │ │
│  │      ║  statusUpdateError: String?                   ║                │ │
│  │      ╚═══════════════════════════════════════════════╝                │ │
│  │  }                                                                      │ │
│  │                                                                         │ │
│  │  Dialog Control Methods:                                               │ │
│  │  ├─ openPaymentDialog()                                              │ │
│  │  ├─ openStatusMenu()                                                 │ │
│  │  ├─ openPdfExport()                                                  │ │
│  │  └─ closeDialog()                                                     │ │
│  │                                                                         │ │
│  │  Operation Methods:                                                    │ │
│  │  ├─ recordPayment(amount: Long)                                       │ │
│  │  │  └─ Updates: paymentLoading, paymentError, dialogState            │ │
│  │  │                                                                     │ │
│  │  ├─ updateInvoiceStatus(status: InvoiceStatus)                       │ │
│  │  │  └─ Updates: statusUpdateError, dialogState                       │ │
│  │  │                                                                     │ │
│  │  └─ exportToPdf(invoice: InvoiceWithItems)                            │ │
│  │     └─ Updates: dialogState (Loading → Success/Error)                │ │
│  └─────────────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────────────────────┘
           ↓                              ↓                              ↓
    ┌────────────────┐          ┌──────────────────┐         ┌─────────────────┐
    │   Invoice DB   │          │  Payment DB      │         │   PDF Service   │
    │                │          │                  │         │                 │
    │ updateAmountPaid           updateStatus      │         │ generatePdf()   │
    │ updateStatus   │          markInvoiceAsPaid  │         │                 │
    └────────────────┘          └──────────────────┘         └─────────────────┘
```

---

## State Diagram: Payment Operation

```
                    ┌─────────────────────────────────┐
                    │    INITIAL STATE                │
                    │ dialogState = None              │
                    │ paymentLoading = false          │
                    │ paymentError = null             │
                    └──────────────┬──────────────────┘
                                   │
                                   │ User clicks payment button
                                   │ viewModel.openPaymentDialog()
                                   ↓
                    ┌─────────────────────────────────┐
                    │    DIALOG OPEN                  │
                    │ dialogState = PaymentDialog     │
                    │ paymentLoading = false          │
                    │ paymentError = null             │
                    └──────────────┬──────────────────┘
                                   │
                                   │ Dialog appears, user enters amount
                                   │ User clicks "Record Payment"
                                   │ onSuccess(5000)
                                   │ → viewModel.recordPayment(5000)
                                   ↓
                    ┌─────────────────────────────────┐
                    │    PAYMENT IN PROGRESS          │
                    │ dialogState = PaymentDialog     │
                    │ paymentLoading = true  ← LOADING│
                    │ paymentError = null             │
                    │                                 │
                    │ [Dialog shows spinner]          │
                    └──────────────┬──────────────────┘
                                   │
                        ┌──────────┴──────────┐
                        │                     │
                        ↓                     ↓
           ┌──────────────────────┐ ┌──────────────────────┐
           │   SUCCESS PATH       │ │   ERROR PATH         │
           ├──────────────────────┤ ├──────────────────────┤
           │ Database updated     │ │ Exception thrown     │
           │ Status auto-set      │ │ or validation error  │
           └──────┬───────────────┘ └──────┬───────────────┘
                  │                        │
                  ↓                        ↓
    ┌─────────────────────────────┐ ┌──────────────────────┐
    │ SUCCESS STATE               │ │ ERROR STATE          │
    │ dialogState = None          │ │ dialogState = Payment│
    │ paymentLoading = false      │ │ paymentLoading = false
    │ paymentError = null         │ │ paymentError = "..."│
    │                             │ │                      │
    │ [Dialog closes, screen      │ │ [Error shown in      │
    │  refreshes with new data]   │ │  dialog, user can    │
    │                             │ │  retry]              │
    └─────────────────────────────┘ └──────────────────────┘
```

---

## State Diagram: PDF Export Operation

```
            ┌─────────────────────────────────┐
            │    INITIAL STATE                │
            │ dialogState = None              │
            └──────────────┬──────────────────┘
                           │
                           │ User clicks PDF button
                           │ viewModel.openPdfExport()
                           ↓
            ┌─────────────────────────────────────────┐
            │    PDF EXPORT STARTING                  │
            │ dialogState = PdfExport.Loading         │
            │                                         │
            │ [Loading dialog shown with spinner]     │
            │ [exportToPdf() triggered automatically] │
            └──────────────┬──────────────────────────┘
                           │
                           │ Generating PDF files...
                           │ (may take several seconds)
                           │
                ┌──────────┴──────────┐
                │                     │
                ↓                     ↓
   ┌──────────────────────┐ ┌──────────────────────┐
   │   SUCCESS PATH       │ │   ERROR PATH         │
   ├──────────────────────┤ ├──────────────────────┤
   │ PDFs generated       │ │ Generation failed    │
   │ Files saved to vault │ │ or permissions issue │
   └──────┬───────────────┘ └──────┬───────────────┘
          │                        │
          ↓                        ↓
┌──────────────────────────┐ ┌──────────────────────┐
│ SUCCESS STATE            │ │ ERROR STATE          │
│ dialogState =            │ │ dialogState =        │
│   PdfExport.Success(file)│ │   PdfExport.Error    │
│                          │ │                      │
│ [Success dialog shown]   │ │ [Error dialog shown] │
│ - File name              │ │ - Error message      │
│ - File size              │ │ - Retry option       │
│ - "Done" button          │ │ - "OK" button        │
│                          │ │                      │
└──────┬───────────────────┘ └──────┬───────────────┘
       │                            │
       │ User clicks "Done"         │ User clicks "OK"
       │                            │
       ↓                            ↓
┌─────────────────────────────────────────────┐
│    FINAL STATE                              │
│ dialogState = None                          │
│ [Dialog closes, back to main screen]        │
└─────────────────────────────────────────────┘
```

---

## Data Flow: Single Operation Example

```
UI Layer (Composable)
│
├─ Button clicked
│  └─ viewModel.recordPayment(5000)
│
└─ Observes: uiState.collectAsStateWithLifecycle()
   │
   ├─ Reads: state.dialogState
   ├─ Reads: state.paymentLoading
   ├─ Reads: state.paymentError
   │
   └─ Re-renders when any field changes

        ↓↓↓

ViewModel Layer
│
├─ recordPayment(amount) called
│  │
│  └─ Updates uiState:
│     ├─ paymentLoading = true
│     ├─ paymentError = null
│     │
│     └─ Calls invoiceDao.updateAmountPaid()
│        │
│        └─ Updates database
│           │
│           └─ Invoice state updates (Flow)
│              │
│              └─ Flows back to UI via
│                 invoiceDao.getInvoiceWithItemsById()
│
├─ On success:
│  └─ Updates uiState:
│     ├─ dialogState = None
│     ├─ paymentLoading = false
│     ├─ paymentError = null
│
└─ On error:
   └─ Updates uiState:
      ├─ paymentLoading = false
      └─ paymentError = "Error message"

        ↓↓↓

Database Layer
│
├─ updateAmountPaid(invoiceId, newAmount)
├─ updateStatus(invoiceId, newStatus)
│
└─ Changes notify Flows back to ViewModel
```

---

## Comparison: Before vs After

### BEFORE (Mixed State Management)
```
┌─────────────────────────────────────────────────────────┐
│ Composable (InvoiceDetailScreenV2)                      │
│                                                         │
│ var dialogState by remember { mutableStateOf(...) }   │ ← Local
│                                                         │   state
│ LaunchedEffect { viewModel.exportToPdf(...) }          │ ← Manual
│                                                         │   trigger
│ if (dialogState is PaymentDialog)                      │
│     RecordPaymentDialogV2(onSuccess = { ... })         │ ← Callback
│                                                         │
│ when (pdfExportState) {  ← Read separate flow          │
│     is Loading → ...                                    │
│     is Success → ...                                    │
│ }                                                       │
└─────────────────────────────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────┐
│ ViewModel (Mixed Pattern)                │
│                                          │
│ _paymentEvent: SharedFlow<String>        │
│ _pdfExportState: StateFlow               │
│ _pdfFile: SharedFlow<File>               │
│                                          │
│ Logic scattered across methods           │
└──────────────────────────────────────────┘

Problems:
❌ Local state in Composable (hard to test)
❌ Multiple state sources (confusing)
❌ SharedFlow for one-shot events (error-prone)
❌ Manual LaunchedEffect triggers (race conditions)
❌ Inconsistent patterns
```

### AFTER (Unified State Management)
```
┌─────────────────────────────────────────────────────────┐
│ Composable (InvoiceDetailScreenV2)                      │
│                                                         │
│ val uiState by viewModel.uiState.collectAsState...()  │
│                                                         │
│ // All dialog state from uiState                        │
│ if (state.dialogState is PaymentDialog)                │
│     RecordPaymentDialogV2(                              │
│         isLoading = state.paymentLoading,              │
│         error = state.paymentError,                    │
│         onSuccess = { amount → viewModel.record... } │
│     )                                                   │
│                                                         │
│ when (state.dialogState) {                             │
│     is PdfExport.Loading → ...                         │
│     is PdfExport.Success → ...                         │
│     is PdfExport.Error → ...                           │
│ }                                                       │
│                                                         │
│ NO local state, NO LaunchedEffect                       │
│ Pure presenter layer!                                   │
└─────────────────────────────────────────────────────────┘
                        ↓
┌──────────────────────────────────────────┐
│ ViewModel (Unified Pattern)              │
│                                          │
│ uiState: StateFlow<InvoiceDetailUiStateV2>
│   ├─ invoice: InvoiceWithItems          │
│   ├─ dialogState: DialogState           │
│   ├─ paymentLoading: Boolean            │
│   ├─ paymentError: String?              │
│   └─ statusUpdateError: String?         │
│                                          │
│ Public methods:                          │
│   openPaymentDialog()                    │
│   recordPayment(amount)                  │
│   closeDialog()                          │
│   etc.                                   │
└──────────────────────────────────────────┘

Benefits:
✅ No local state (testable)
✅ Single state source (clear)
✅ StateFlow all the way (safe)
✅ Automatic triggers (reliable)
✅ Consistent patterns (maintainable)
```

---

## Benefits Summary

| Aspect | Before | After |
|--------|--------|-------|
| **State Sources** | 4+ (local + 2 flows) | 1 (StateFlow) |
| **Local State** | Yes (risky) | No (testable) |
| **Error Handling** | SharedFlow + field | StateFlow field |
| **Dialog Triggers** | LaunchedEffect | Direct methods |
| **Composable Size** | ~368 lines | ~250 lines |
| **Testability** | Medium | High |
| **Pattern** | Mixed | Unified |
| **Scaling** | Difficult | Easy |

---

## Next Steps

### Ready for Testing
✅ Manual testing: All three dialogs  
✅ Unit tests: ViewModel state transitions  
✅ UI tests: Dialog appearance/behavior  

### Optional Future Enhancement
→ **Path 3 Migration** (if 5+ dialogs needed)
- Extract state holders
- Improve reusability
- Further modularization

