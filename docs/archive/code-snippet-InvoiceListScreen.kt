// Existing imports
import ... // other imports

// Remove line 40

// ...

// Update to handle state
when (state) {
    is InvoiceListUiState.Loading -> {
        // handle loading
    }
    is InvoiceListUiState.Error -> {
        // handle error
    }
    is InvoiceListUiState.Success -> {
        // handle success
    }
}