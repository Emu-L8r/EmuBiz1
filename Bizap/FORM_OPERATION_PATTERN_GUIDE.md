# 📚 Form Operation Pattern - Implementation Guide

**Status:** Ready for Team Implementation  
**Date:** March 29, 2026  
**Version:** 1.0

---

## 🎯 **Overview**

This guide shows how to implement consistent form operation handling across all Bizap screens. The pattern ensures:

- ✅ Clear loading feedback during operations
- ✅ Prevents double-submission (disabled buttons)
- ✅ Consistent error handling
- ✅ Professional UX

---

## 📋 **The Pattern in 3 Steps**

### **Step 1: Add Loading State to ViewModel**

Add an `isLoading` StateFlow to track operation state:

```kotlin
@HiltViewModel
class YourViewModel @Inject constructor(
    // ... dependencies ...
) : ViewModel() {

    // Track loading state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun performOperation(
        onSuccess: () -> Unit,
        onError: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Timber.d("YourViewModel: Starting operation")
                
                // Your operation here
                repository.saveData()
                
                Timber.d("✅ YourViewModel: Operation succeeded")
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "❌ YourViewModel: Operation failed")
                onError(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
```

**Key Points:**
- Use `finally` block to always reset loading state
- Log with ✅ and ❌ emojis for clarity
- Provide error callback for UI feedback

---

### **Step 2: Observe Loading State in Screen**

Import and collect the loading state:

```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun YourScreen(
    viewModel: YourViewModel = hiltViewModel()
) {
    // Collect loading state from ViewModel
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // Rest of composable...
}
```

---

### **Step 3: Use Loading State in UI**

Apply the loading state to your form elements:

```kotlin
@Composable
fun YourScreen(
    viewModel: YourViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long,
                withDismissAction = true
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Screen") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !isLoading  // Disable during operation
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Text fields
            OutlinedTextField(
                value = fieldValue,
                onValueChange = { fieldValue = it },
                label = { Text("Field Name") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading  // Disable during operation
            )

            // Save Button
            Button(
                onClick = {
                    viewModel.performOperation(
                        onSuccess = {
                            Timber.i("✅ Operation succeeded")
                            onNavigateBack()
                        },
                        onError = { error ->
                            errorMessage = error ?: "Operation failed"
                            Timber.e("❌ Operation failed: $error")
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading  // Disable during operation
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...")
                } else {
                    Text("Save")
                }
            }
        }
    }
}
```

---

## ✅ **Implemented Examples**

### **ViewModels Already Using This Pattern**

1. **CreateCustomerViewModelV2** ✅
   - Location: `ui/gui2/customers/CreateCustomerViewModelV2.kt`
   - Status: Ready to use
   - Features: `isLoading`, error callback

2. **CreateInvoiceViewModelV2** ✅
   - Location: `ui/gui2/invoices/CreateInvoiceViewModelV2.kt`
   - Status: Ready to use
   - Features: `isLoading`, error callback

3. **EditCustomerViewModelV2** ✅
   - Location: `ui/gui2/customers/EditCustomerViewModelV2.kt`
   - Status: Ready to use
   - Features: `isLoading`, error callback

4. **EditInvoiceViewModelV2** ✅
   - Location: `ui/gui2/invoices/EditInvoiceViewModelV2.kt`
   - Status: Ready to use
   - Features: `isLoading`, error callback

5. **RecordPaymentViewModel** ✅
   - Location: `ui/gui2/invoices/RecordPaymentViewModel.kt`
   - Status: Already optimized
   - Features: Uses `PaymentFormState` with `isLoading`

---

## 🔧 **Integration Checklist**

When integrating this pattern into a new screen, follow this checklist:

- [ ] ViewModel has `_isLoading` MutableStateFlow
- [ ] ViewModel exposes `isLoading` StateFlow
- [ ] Operation function sets `_isLoading.value = true` at start
- [ ] Operation function sets `_isLoading.value = false` in finally block
- [ ] Operation function has error callback parameter
- [ ] Screen imports `collectAsStateWithLifecycle`
- [ ] Screen collects `isLoading` from ViewModel
- [ ] TextField `enabled = !isLoading`
- [ ] Button `enabled = !isLoading`
- [ ] Button shows spinner + "Saving..." text when loading
- [ ] Navigation icon `enabled = !isLoading`
- [ ] Error message triggers snackbar
- [ ] Build compiles without errors
- [ ] Tested on device/emulator

---

## 📊 **Pattern Benefits**

| Aspect | Before | After |
|--------|--------|-------|
| **User Feedback** | Silent operations | Clear loading indicator |
| **Double Submission** | Possible | Prevented by disabled button |
| **Error Handling** | Inconsistent | Standardized snackbar |
| **Code Duplication** | High | Low (reusable pattern) |
| **Professional Feel** | Basic | Premium |
| **Accessibility** | Missing states | Proper semantic states |

---

## 🎯 **Quick Reference**

### **ViewModel Template**
```kotlin
private val _isLoading = MutableStateFlow(false)
val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

fun operation(onSuccess: () -> Unit, onError: (String?) -> Unit = {}) {
    viewModelScope.launch {
        try {
            _isLoading.value = true
            // operation
            onSuccess()
        } catch (e: Exception) {
            onError(e.message)
        } finally {
            _isLoading.value = false
        }
    }
}
```

### **Screen Template**
```kotlin
val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

Button(
    onClick = { viewModel.operation(onSuccess, onError) },
    enabled = !isLoading
) {
    if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text("Saving...")
    } else {
        Text("Save")
    }
}
```

---

## 🚀 **Next Steps**

1. Review this guide with your team
2. Pick a screen to integrate first
3. Follow the checklist above
4. Test thoroughly
5. Roll out to other screens gradually

---

**Questions?** Refer to the implemented examples above.


