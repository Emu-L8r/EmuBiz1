# Consolidation Strategy

> **Updated:** Phase 2 — April 2026

This document describes the step-by-step plan for merging GUI1 and GUI2 duplicate screens
into single, unified implementations that support both themes via `GuiMode`.

---

## Overview

The app originally had parallel implementations for every screen:
- `ui/invoices/InvoiceDetailScreen.kt` (GUI1)
- `ui/gui2/invoices/InvoiceDetailScreenV2.kt` (GUI2)

The consolidation goal is one implementation per screen that renders differently based on `GuiMode`.

### Pattern

```kotlin
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    guiMode: GuiMode = GuiMode.GUI2,
    onNavigateBack: () -> Unit,
    onEditInvoice: (Long) -> Unit
) {
    val viewModel: InvoiceDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (uiState) {
        is InvoiceDetailUiState.Loading -> SharedLoadingScreen()
        is InvoiceDetailUiState.Error   -> SharedErrorScreen((uiState as InvoiceDetailUiState.Error).message)
        is InvoiceDetailUiState.Success -> {
            val invoice = (uiState as InvoiceDetailUiState.Success).invoice
            if (guiMode == GuiMode.GUI1) {
                InvoiceDetailGui1Content(invoice, onNavigateBack, onEditInvoice)
            } else {
                InvoiceDetailGui2Content(invoice, onNavigateBack, onEditInvoice)
            }
        }
    }
}
```

---

## Screen Matrix

| Screen | GUI1 file | GUI2 file | Merged into | Status |
|--------|-----------|-----------|-------------|--------|
| InvoiceDetail | `ui/invoices/InvoiceDetailScreen.kt` | `ui/gui2/invoices/InvoiceDetailScreenV2.kt` | `ui/invoices/InvoiceDetailScreen.kt` | 🔲 Pending |
| CreateInvoice | `ui/invoices/CreateInvoiceScreen.kt` | `ui/gui2/invoices/CreateInvoiceScreenV2.kt` | `ui/invoices/CreateInvoiceScreen.kt` | 🔲 Pending |
| InvoiceList | `ui/invoices/InvoiceListScreen.kt` | `ui/gui2/invoices/InvoiceListScreenV2.kt` | `ui/invoices/InvoiceListScreen.kt` | 🔲 Pending |
| CustomerList | `ui/customers/CustomerListScreen.kt` | *(GUI2 reuses shared)* | Already consolidated | ✅ Done |
| CustomerDetail | `ui/customers/CustomerDetailScreen.kt` | *(GUI2 uses shared viewmodel)* | Already consolidated | ✅ Done |
| Dashboard | `ui/dashboard/DashboardScreen.kt` | `ui/gui2/dashboard/DashboardScreenV2.kt` | `ui/dashboard/DashboardScreen.kt` | 🔲 Pending |
| Settings | `ui/settings/SettingsHubScreen.kt` | `ui/gui2/settings/SettingsHubScreenV2.kt` | `ui/settings/SettingsHubScreen.kt` | 🔲 Pending |

---

## Step-by-Step Consolidation Process

For each screen pair:

### Step 1 — Audit differences

```bash
diff ui/invoices/InvoiceDetailScreen.kt ui/gui2/invoices/InvoiceDetailScreenV2.kt
```

Identify:
- Shared logic (ViewModel, state handling, business logic)
- GUI1-only layout (legacy style)
- GUI2-only layout (modern style)

### Step 2 — Merge ViewModel

If the V2 ViewModel adds new functionality:
1. Merge into the original ViewModel
2. Preserve original state class
3. Extend state with any new fields

### Step 3 — Create unified screen

Replace the GUI1 screen with a `guiMode` parameter:

```kotlin
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    guiMode: GuiMode = GuiMode.GUI2,  // Default to GUI2 (modern)
    ...
)
```

### Step 4 — Update navigation graphs

- `GuiV1NavGraph.kt`: Pass `guiMode = GuiMode.GUI1`
- `GuiV2NavGraph.kt`: Pass `guiMode = GuiMode.GUI2`

### Step 5 — Delete V2 file

After verifying the consolidated screen works in both modes:
```bash
git rm ui/gui2/invoices/InvoiceDetailScreenV2.kt
git rm ui/gui2/invoices/InvoiceDetailViewModelV2.kt
```

### Step 6 — Run tests

```bash
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
```

---

## Shared Components

New shared components live in `ui/shared/`:

| Component | Purpose |
|-----------|---------|
| `ScreenRouter.kt` | Sealed class for type-safe route building |
| `SharedScreenComponents.kt` | `InvoiceHeaderCard`, `SharedLoadingScreen`, `SharedErrorScreen`, `MetricRow` |

---

## Review Checklist (per screen)

Before marking a consolidation complete:

- [ ] Single composable handles both `GuiMode.GUI1` and `GuiMode.GUI2`
- [ ] Navigation updated in both graphs
- [ ] V2 file deleted (no dead code)
- [ ] All existing unit tests pass
- [ ] Manual testing: both Classic and Modern themes render correctly
- [ ] No regressions in related screens

---

## Timeline

| Week | Target |
|------|--------|
| 3 | InvoiceDetail + CreateInvoice |
| 4 | InvoiceList + Dashboard |
| 5 | Settings + Utility functions |
