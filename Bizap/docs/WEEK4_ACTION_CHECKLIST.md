# Week 4: Error Handling - Action Checklist

**What to do right now:**

---

## ✅ Review Phase (1 hour)

- [ ] Read: BizapException.kt
  - [ ] Understand 13 error types
  - [ ] See helper functions
  - [ ] Know when to throw each

- [ ] Read: ErrorHandler.kt
  - [ ] Understand mapping logic
  - [ ] See 20+ exception mappings
  - [ ] Learn logging strategy

- [ ] Read: NetworkRetryPolicy.kt
  - [ ] Understand exponential backoff
  - [ ] See formula explained
  - [ ] Know jitter purpose

- [ ] Read: CurrencyRepository.kt
  - [ ] See complete example
  - [ ] Understand fallback chain
  - [ ] Find pattern for your code

---

## 🔧 Integration Phase (2-3 hours)

### Step 1: Your Repositories (for each Repository)
- [ ] Change throws from generic Exception → BizapException
- [ ] Wrap third-party exceptions
- [ ] Add context to errors

**Example:**
```kotlin
// Before:
throw Exception("Database error")

// After:
throw BizapException.DatabaseError(
    operation = "INSERT",
    table = "invoices",
    message = e.message ?: "Unknown"
)
```

### Step 2: Your ViewModels (for each ViewModel)
- [ ] Add try/catch around repository calls
- [ ] Use ErrorHandler.handle()
- [ ] Emit ErrorInfo to UI state

**Example:**
```kotlin
try {
    repository.saveInvoice(invoice)
} catch (e: Exception) {
    val info = ErrorHandler.handle(e, "InvoiceDetailViewModel.save")
    _errorState.emit(info)
}
```

### Step 3: Your UI (for each Screen)
- [ ] Display error based on severity
- [ ] Show recovery action
- [ ] Enable retry button if needed

**Example:**
```kotlin
when (errorInfo?.severity) {
    CRITICAL -> showErrorDialog(errorInfo)
    HIGH, MEDIUM -> showSnackbar(errorInfo.userMessage)
    LOW -> {}  // Don't show
}
```

---

## 🧪 Testing Phase (2-3 hours)

For each error type:
- [ ] Test happy path (no error)
- [ ] Test with validation error
- [ ] Test with database error
- [ ] Test with network error
- [ ] Test with connectivity error
- [ ] Verify retry logic works
- [ ] Verify fallback works
- [ ] Check Timber logs

**Test Scenarios:**
1. [ ] Network fails → Retry succeeds
2. [ ] Network fails 3x → Use cache
3. [ ] Network fails, no cache → Use defaults
4. [ ] Validation error → Show user message
5. [ ] Database error → Show error + retry button
6. [ ] File error → Show friendly message

---

## 📋 Integration Checklist

### Repositories (List each one)
- [ ] InvoiceRepository
- [ ] CustomerRepository
- [ ] CurrencyRepository
- [ ] DocumentRepository
- [ ] BusinessProfileRepository
- [ ] (others)

**For each:**
- [ ] Updated to throw BizapException
- [ ] Wrapped third-party exceptions
- [ ] Added context to errors

### ViewModels (List each one)
- [ ] CreateInvoiceViewModel
- [ ] EditInvoiceViewModel
- [ ] CustomerDetailViewModel
- [ ] (others)

**For each:**
- [ ] Added try/catch
- [ ] Uses ErrorHandler
- [ ] Emits ErrorInfo

### Screens (List each one)
- [ ] InvoiceDetailScreen
- [ ] CustomerDetailScreen
- [ ] (others)

**For each:**
- [ ] Displays ErrorInfo
- [ ] Shows based on severity
- [ ] Has retry button if needed

---

## 🚀 Launch Checklist

Before shipping:
- [ ] All error paths tested
- [ ] No technical messages to user
- [ ] Retry works for network errors
- [ ] Fallback works when API down
- [ ] Logging shows in logcat
- [ ] Critical errors logged to Firebase
- [ ] User can recover from errors
- [ ] No unhandled exceptions in logcat
- [ ] Code review for error handling
- [ ] Test on slow network (Settings → Developer → Network throttle)

---

## 📈 Success Indicators

✅ You're done when:

1. **No Technical Errors Shown**
   - [ ] All user-facing messages are friendly
   - [ ] Technical terms hidden from UI

2. **Automatic Retry Works**
   - [ ] Network failures retry automatically
   - [ ] Exponential backoff visible in logs
   - [ ] After 3 retries, shows message

3. **Graceful Fallback**
   - [ ] API down → Cache used
   - [ ] Cache empty → Defaults shown
   - [ ] User never sees crash

4. **Logging Complete**
   - [ ] Each error logged to Timber
   - [ ] Context information included
   - [ ] Critical errors tagged

5. **User Can Recover**
   - [ ] Clear error messages
   - [ ] Actionable recovery steps
   - [ ] Retry button when appropriate

---

## 📊 Progress Tracker

```
Repository Integration:
  [  ] 0/5 repositories done
  
ViewModel Integration:
  [  ] 0/5 viewmodels done
  
UI Integration:
  [  ] 0/3 screens done
  
Testing:
  [  ] 0/6 scenarios passed
```

---

## 🎯 This Week's Goals

**Monday:** Review all 4 code files  
**Tuesday:** Start repository integration  
**Wednesday:** Start viewmodel integration  
**Thursday:** Start UI integration + testing  
**Friday:** Testing + verification + commit

---

## 💾 Commit Messages

As you complete each piece:

```bash
git add -A
git commit -m "feat: Add error handling to [Repository]"

git add -A
git commit -m "feat: Update [ViewModel] with ErrorHandler"

git add -A
git commit -m "feat: Add error UI to [Screen]"
```

---

## 🎉 When You're Done

Celebrate! You'll have:
- ✅ Production-grade error handling
- ✅ Automatic retry for failures
- ✅ Graceful fallbacks
- ✅ Friendly user messages
- ✅ Comprehensive logging
- ✅ Recovery suggestions

**Your app is now resilient and professional!** 🚀


