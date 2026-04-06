# Dual-Mode UI Guide

Bizap supports two interface styles within a single Compose codebase:
- **Modern:** Material 3, spacious layouts, full features
- **Compact:** Condensed, traditional-looking, efficient use of space

## How It Works

All screens use conditional rendering based on `UIMode` from `AppStateViewModel`:

```kotlin
@Composable
fun InvoiceListScreen(
    viewModel: InvoiceListViewModelV2,
    uiMode: UIMode
) {
    if (uiMode == UIMode.MODERN) {
        ModernInvoiceList(...)
    } else {
        CompactInvoiceList(...)
    }
}
```

## Adding a New Screen

1. Create screen in `ui/gui2/invoices/` or relevant package
2. Add both `Modern*` and `Compact*` composable functions
3. Use conditional rendering in main screen function based on `UIMode`
4. Test in both modes on device

## Data Consistency

Both modes use the same ViewModels and repositories. Data is always in sync
regardless of which mode is active.

## Performance

- Switching modes triggers recomposition of screen (no data loss)
- Memory usage identical for both modes
- No performance penalty for supporting both
