# Path 2 Implementation: Before & After Code Examples

## Example 1: Opening a Dialog

### BEFORE (Mixed State)
```kotlin
// In Composable
IconButton(onClick = { 
    dialogState = DialogState.PaymentDialog  // Local state update
}) {
    Icon(Icons.Default.Payment, contentDescription = "Record Payment")
}

// Then later in render logic:
if (dialogState is DialogState.PaymentDialog) {
    RecordPaymentDialogV2(
        // ... params ...
        onSuccess = { 
            dialogState = DialogState.None  // Manual close
        }
    )
}
```

### AFTER (Unified State)
```kotlin
// In Composable
IconButton(onClick = { 
    viewModel.openPaymentDialog()  // ViewModel method
}) {
    Icon(Icons.Default.Payment, contentDescription = "Record Payment")
}

// Then later in render logic:
if (state.dialogState is DialogState.PaymentDialog) {
    RecordPaymentDialogV2(
        // ... params ...
        isLoading = state.paymentLoading,    // From ViewModel
        error = state.paymentError,          // From ViewModel
        onSuccess = { amount -> 
            viewModel.recordPayment(amount)  // ViewModel operation
        }
    )
}
```

**Benefits:**
- ✅ No local state mutation
- ✅ Dialog state in ViewModel
- ✅ Loading/error visible from Composable

---

## Example 2: Recording Payment

### BEFORE (Event-Based)
```kotlin
// ViewModel
private val _paymentEvent = MutableSharedFlow<String>()
val paymentEvent: SharedFlow<String> = _paymentEvent.asSharedFlow()

fun recordPayment(amount: Long) {
    viewModelScope.launch {
        try {
            // ... validation ...
            invoiceDao.updateAmountPaid(invoiceId, newAmountPaid)
            invoiceDao.updateStatus(invoiceId, newStatus)
            _paymentEvent.emit("Payment recorded successfully.")
        } catch (e: Exception) {
            _paymentEvent.emit("Failed to record payment: ${e.message}")
        }
    }
}

// Composable
LaunchedEffect(Unit) {
    viewModel.paymentEvent.collect { message ->
        if (message.startsWith("Payment")) {
            onSuccess()  // Success callback
        } else {
            showSnackbar(message)  // Error feedback
        }
    }
}
```

### AFTER (State-Based)
```kotlin
// ViewModel
fun recordPayment(amount: Long) {
    viewModelScope.launch {
        val currentState = _uiState.value
        if (currentState !is InvoiceDetailUiStateV2.Success) return@launch

        // Set loading
        _uiState.value = currentState.copy(paymentLoading = true, paymentError = null)

        try {
            // ... validation ...
            invoiceDao.updateAmountPaid(invoiceId, newAmountPaid)
            invoiceDao.updateStatus(invoiceId, newStatus)
            
            // Success: close dialog
            _uiState.value = currentState.copy(
                dialogState = DialogState.None,
                paymentLoading = false,
                paymentError = null
            )
        } catch (e: Exception) {
            // Error: show in dialog
            _uiState.value = currentState.copy(
                paymentLoading = false,
                paymentError = e.message ?: "Unknown error"
            )
        }
    }
}

// Composable - all state-driven
if (state.dialogState is DialogState.PaymentDialog) {
    RecordPaymentDialogV2(
        isLoading = state.paymentLoading,      // Spinner shown
        error = state.paymentError,            // Error displayed
        onSuccess = { amount -> 
            viewModel.recordPayment(amount)
        },
        onDismiss = { 
            viewModel.closeDialog()
        }
    )
}
```

**Benefits:**
- ✅ Dialog auto-closes on success (state-driven)
- ✅ Error persists in dialog (visible in state)
- ✅ Loading state visible (can show spinner)
- ✅ No event stream management

---

## Example 3: PDF Export

### BEFORE (Multiple State Sources)
```kotlin
// ViewModel
private val _pdfExportState = MutableStateFlow<PdfExportState>(PdfExportState.Idle)
val pdfExportState: StateFlow<PdfExportState> = _pdfExportState.asStateFlow()

fun exportToPdf(invoice: InvoiceWithItems) {
    viewModelScope.launch {
        _pdfExportState.value = PdfExportState.Loading
        try {
            val file = pdfGenerationService.generatePdf(...)
            _pdfExportState.value = PdfExportState.Success(file)
        } catch (e: Exception) {
            _pdfExportState.value = PdfExportState.Error(e.message ?: "Error")
        }
    }
}

// Composable
if (dialogState is DialogState.ExportPdf) {
    val pdfExportState by viewModel.pdfExportState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.exportToPdf(state.invoice)  // Manual trigger
    }
    
    when (pdfExportState) {
        is PdfExportState.Loading -> AlertDialog(...)
        is PdfExportState.Success -> AlertDialog(...)
        is PdfExportState.Error -> AlertDialog(...)
        else -> {}
    }
}
```

**Problems:**
- ❌ Two state sources (dialogState + pdfExportState)
- ❌ Manual trigger in LaunchedEffect (race conditions)
- ❌ DialogState.ExportPdf separate from PDF progress

### AFTER (Unified State)
```kotlin
// ViewModel
fun openPdfExport() {
    val currentState = _uiState.value
    if (currentState is InvoiceDetailUiStateV2.Success) {
        _uiState.value = currentState.copy(
            dialogState = DialogState.PdfExport.Loading
        )
        // Auto-trigger export
        exportToPdf(currentState.invoice)
    }
}

fun exportToPdf(invoice: InvoiceWithItems) {
    viewModelScope.launch {
        try {
            val currentState = _uiState.value
            if (currentState !is InvoiceDetailUiStateV2.Success) return@launch
            
            val file = pdfGenerationService.generatePdf(...)
            
            _uiState.value = currentState.copy(
                dialogState = DialogState.PdfExport.Success(file)
            )
        } catch (e: Exception) {
            val currentState = _uiState.value
            if (currentState is InvoiceDetailUiStateV2.Success) {
                _uiState.value = currentState.copy(
                    dialogState = DialogState.PdfExport.Error(e.message ?: "Error")
                )
            }
        }
    }
}

// Composable
when (state.dialogState) {
    is DialogState.PdfExport.Loading -> {
        AlertDialog(
            title = { Text("📄 Generating PDF") },
            text = {
                Column(...) {
                    CircularProgressIndicator()
                    Text("Creating professional invoice PDF...")
                }
            }
        )
    }
    is DialogState.PdfExport.Success -> {
        val file = (state.dialogState as DialogState.PdfExport.Success).file
        AlertDialog(
            title = { Text("✅ PDFs Generated Successfully") },
            text = {
                Column {
                    Text("Invoice PDF: ${file.name}")
                    Text("Size: ${(file.length() / 1024).toInt()} KB")
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.closeDialog() }) {
                    Text("Done")
                }
            }
        )
    }
    is DialogState.PdfExport.Error -> {
        val message = (state.dialogState as DialogState.PdfExport.Error).message
        AlertDialog(
            title = { Text("❌ Export Failed") },
            text = { Text(message) },
            confirmButton = {
                Button(onClick = { viewModel.closeDialog() }) {
                    Text("OK")
                }
            }
        )
    }
    else -> {}
}
```

**Benefits:**
- ✅ Single state source (dialogState)
- ✅ Auto-trigger when dialog opens (no LaunchedEffect)
- ✅ All PDF states (Loading/Success/Error) in one place
- ✅ No separate pdfExportState flow needed

---

## Example 4: Error Handling & Display

### BEFORE (Event Stream)
```kotlin
// ViewModel emits error events
private val _paymentEvent = MutableSharedFlow<String>()

fun recordPayment(amount: Long) {
    viewModelScope.launch {
        try {
            // ... operation ...
            _paymentEvent.emit("Success message")
        } catch (e: Exception) {
            _paymentEvent.emit("Error: ${e.message}")
        }
    }
}

// Composable must listen to events
LaunchedEffect(Unit) {
    viewModel.paymentEvent.collect { message ->
        when {
            message.startsWith("Success") -> {
                showSuccessSnackbar(message)
                onSuccess()
            }
            message.startsWith("Error") -> {
                showErrorSnackbar(message)
            }
        }
    }
}
```

**Problems:**
- ❌ Events disappear if not collected immediately
- ❌ String parsing to determine success/error
- ❌ Race conditions if called multiple times

### AFTER (State Field)
```kotlin
// ViewModel updates state
data class Success(
    val paymentError: String? = null,  // Error state
    val paymentLoading: Boolean = false, // Loading state
)

fun recordPayment(amount: Long) {
    viewModelScope.launch {
        val currentState = _uiState.value as Success
        
        _uiState.value = currentState.copy(paymentLoading = true)
        
        try {
            // ... operation ...
            _uiState.value = currentState.copy(
                dialogState = DialogState.None,
                paymentLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = currentState.copy(
                paymentLoading = false,
                paymentError = e.message
            )
        }
    }
}

// Composable reads error state
if (state.dialogState is DialogState.PaymentDialog) {
    RecordPaymentDialogV2(
        isLoading = state.paymentLoading,
        error = state.paymentError,  // Error displayed in dialog
        onSuccess = { amount -> viewModel.recordPayment(amount) }
    )
}
```

**Benefits:**
- ✅ Error state persists (visible until user closes dialog)
- ✅ Loading state visible separately
- ✅ Dialog controls error display (no external feedback needed)
- ✅ Clear type-safe state

---

## Example 5: Dialog Open/Close Flow

### BEFORE (Local State Management)
```kotlin
// Multiple manual state updates
var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

// Open payment dialog
IconButton(onClick = { 
    dialogState = DialogState.PaymentDialog
})

// Close after success
LaunchedEffect(Unit) {
    viewModel.paymentEvent.collect { event ->
        if (event.startsWith("Success")) {
            dialogState = DialogState.None  // Manual close
        }
    }
}

// Manual cancel
TextButton(onClick = { 
    dialogState = DialogState.None  // Manual close
})

// Payment form close
RecordPaymentDialogV2(
    onDismiss = { dialogState = DialogState.None },  // Manual
    onSuccess = { dialogState = DialogState.None }   // Manual
)
```

**Problems:**
- ❌ Multiple places close the dialog (inconsistent)
- ❌ Manual state management (error-prone)
- ❌ Hard to track dialog state

### AFTER (ViewModel Controlled)
```kotlin
// All dialog open/close in ViewModel

// Open payment dialog
IconButton(onClick = { 
    viewModel.openPaymentDialog()  // ViewModel manages
})

// Close any dialog
fun closeDialog() {
    _uiState.value = currentState.copy(
        dialogState = DialogState.None,
        paymentError = null,
        statusUpdateError = null
    )
}

// Payment form submission
RecordPaymentDialogV2(
    onSuccess = { amount -> 
        // Success closes dialog automatically
        viewModel.recordPayment(amount)
        // ViewModel sets dialogState = None on success
    },
    onDismiss = { 
        viewModel.closeDialog()
    }
)

// Single, consistent close method
// All dialogs use viewModel.closeDialog()
```

**Benefits:**
- ✅ Single method to close dialogs (consistent)
- ✅ ViewModel controls when dialog closes
- ✅ Clear responsibility (ViewModel closes on success)
- ✅ Easy to add cleanup logic

---

## Summary: State Management Comparison

| Scenario | Before | After |
|----------|--------|-------|
| **Open Dialog** | Set local state | Call ViewModel method |
| **Show Loading** | Separate state object | State field (unified) |
| **Show Error** | Event + string parsing | State field (typed) |
| **Close Dialog** | Manual in multiple places | Unified `closeDialog()` |
| **Auto-trigger** | LaunchedEffect (risky) | ViewModel method |
| **Test Dialogs** | Mock flows + local state | Just mock ViewModel |
| **Reuse Pattern** | Different per dialog | Same for all dialogs |
| **State Sources** | 3-4 (scattered) | 1 (unified) |
| **Lines of Code** | ~100 dialog logic | ~50 dialog logic |

---

## Key Takeaway

**Path 2 transforms dialog management from a scattered, multi-source, event-based system into a clean, unified, state-based system where:**

1. **ViewModel is the source of truth** for all dialog states
2. **Composable is a pure presenter** that reads state and calls methods
3. **Every operation follows the same pattern** (loading → success/error)
4. **Errors are persistent** and live in the dialog
5. **Testing is simple** (mock ViewModel, check state transitions)

This makes the codebase more maintainable, testable, and scalable.

