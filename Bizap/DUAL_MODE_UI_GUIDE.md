# Dual-Mode UI Guide

Bizap supports two UI display modes that can be toggled at any time without
restarting the app. Your preference is saved automatically and persists across
app restarts.

---

## Modes

### Modern (default)

The **Modern** mode uses spacious Material 3 cards with full invoice/customer
detail visible at a glance. Metrics are displayed in large metric boxes.

Best for:
- Large screens (tablets, large phones)
- Users who prefer a clean, airy layout
- First-time users who want to see all information immediately

### Compact

The **Compact** mode uses dense list rows with a small footprint. Metrics are
displayed in a single-row chip strip.

Best for:
- Small screens
- Power users who prefer keyboard-navigation-like density
- Users managing many invoices who want to see more rows on screen

---

## Toggling the Mode

1. Open **Settings** from the Dashboard top bar.
2. Scroll to the **Appearance** section.
3. Tap the **Compact UI** toggle switch.

The change takes effect immediately — no restart required.

---

## Technical Details

| Item | Detail |
|------|--------|
| Enum | `UIMode.MODERN` / `UIMode.COMPACT` in `domain/model/UIMode.kt` |
| Persistence | DataStore key `ui_mode` via `UIPreferencesImpl` |
| Interface | `domain/settings/UIPreferences` |
| ViewModel | `AppStateViewModel.uiMode: StateFlow<UIMode>` |
| Screens affected | Dashboard, Invoice List, Customer List |

Both modes share **exactly the same data layer** — switching modes never
triggers a data reload and never shows stale or fake data.

---

## Architecture Principle

Rendering is conditional at the **screen level**, not component level:

```kotlin
if (uiMode == UIMode.COMPACT) {
    CompactInvoiceList(invoices, onInvoiceClick)
} else {
    ModernInvoiceList(invoices, onInvoiceClick)
}
```

This keeps each mode's code isolated and easy to maintain independently.
