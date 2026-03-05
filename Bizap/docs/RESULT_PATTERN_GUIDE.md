# Result Pattern Guide

## Why We Use `Result<T>` in Repositories

Raw return types (e.g., `suspend fun saveInvoice(...): Long`) leave database errors uncaught,
which causes app crashes. Wrapping return values in Kotlin's standard `Result<T>` type makes
every possible outcome explicit and forces callers to handle errors.

**Benefits:**
- **Robustness** – database errors are captured and never propagate as uncaught exceptions
- **Testability** – failure paths are part of the public API and can be asserted in tests
- **Maintainability** – the compiler reminds callers to handle both success and failure

## Pattern: Repository Layer

All `suspend` functions in a repository interface return `Result<T>`:

```kotlin
interface InvoiceRepository {
    suspend fun saveInvoice(invoice: Invoice): Result<Long>
    suspend fun deleteInvoice(id: Long): Result<Unit>
    suspend fun updateInvoiceStatus(invoiceId: Long, status: InvoiceStatus): Result<Unit>
    // Flow-returning functions stay as Flow<T> – they handle errors via .catch { }
    fun getAllInvoicesWithItems(): Flow<List<Invoice>>
}
```

The implementation wraps each body with `runCatching` and logs failures:

```kotlin
override suspend fun saveInvoice(invoice: Invoice): Result<Long> = runCatching {
    val id = invoiceDao.insert(invoiceEntity, lineItemEntities)
    id
}.also { result ->
    result.onFailure { e -> Timber.e(e, "Database operation failed during saveInvoice") }
}
```

## Pattern: ViewModel Layer

Use `.onSuccess` / `.onFailure` to react to the result without wrapping in try-catch:

```kotlin
fun deleteInvoice(id: Long) {
    viewModelScope.launch {
        invoiceRepo.deleteInvoice(id)
            .onSuccess { _event.emit(InvoiceDetailEvent.InvoiceDeleted) }
            .onFailure { e ->
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to delete invoice: ${e.message}"))
            }
    }
}
```

When the calling code is already inside a `try-catch` (e.g., a larger operation), use
`getOrThrow()` to re-throw and let the outer handler deal with it:

```kotlin
val invoiceId = invoiceRepository.saveInvoice(invoice).getOrThrow()
```

## Pattern: Use Case Layer

Use Cases that already return `Result<T>` can delegate directly to the repository:

```kotlin
class SaveInvoiceUseCase @Inject constructor(
    private val repository: InvoiceRepository
) {
    suspend operator fun invoke(invoice: Invoice): Result<Long> {
        if (invoice.items.isEmpty()) {
            return Result.failure(IllegalArgumentException("Invoice must have at least one line item"))
        }
        return repository.saveInvoice(invoice)
    }
}
```

## Flow-Returning Functions

`Flow`-based functions (non-suspend) are **not** wrapped in `Result`. Instead, use `.catch`
in the stream to handle errors reactively:

```kotlin
invoiceRepo.getAllInvoicesWithItems()
    .catch { e -> emit(emptyList()) }
    .collect { invoices -> _uiState.value = Success(invoices) }
```

## Scaling to Other Repositories

Follow the same three steps for each new repository:

1. **Interface** – change `suspend fun foo(...): T` to `suspend fun foo(...): Result<T>`
2. **Implementation** – wrap the body with `runCatching { ... }.also { it.onFailure { e -> Timber.e(e, ...) } }`
3. **Callers** – use `.onSuccess`/`.onFailure` or `.getOrThrow()` where appropriate

Repositories already refactored: `InvoiceRepository`
Planned: `CustomerRepository`, `DocumentRepository`, `BusinessProfileRepository`, `CurrencyRepository`
