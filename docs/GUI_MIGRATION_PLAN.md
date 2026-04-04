# GUI Migration Plan

## Overview

BizAP currently ships two parallel GUIs:

| GUI | Technology | Entry Point | Status |
|---|---|---|---|
| **GUI1 (legacy)** | Activities + XML layouts | `TraditionalGUIMainActivity` | Deprecated — EOL June 2027 |
| **GUI2 (primary)** | Jetpack Compose + MVVM | `ModernGUIMainActivity` → `BizapApp` | Active development |

Since v2.0, `AppStateViewModel.computeAppState()` always routes to **GUI2**. GUI1 code remains in the codebase for reference and backward compatibility during the migration window.

---

## Timeline

| Date | Milestone |
|---|---|
| **April 2026 (now)** | Both GUIs functional; GUI2 is default |
| **Q3 2026** | GUI1 officially deprecated; `@Deprecated` annotation added to `TraditionalGUIMainActivity` |
| **Q4 2026** | In-app migration prompt shown to any remaining GUI1 users |
| **Q2 2027** | Final GUI1 support; no new features will target GUI1 |
| **June 2027** | GUI1 code removal; all Activities, XML layouts, and GUI1-only ViewModels deleted |

---

## Current Architecture

```
MainActivity
  └─ AppStateViewModel.computeAppState()
        └─ always returns AppReady(GuiMode.GUI2)
              └─ BizapApp
                    ├─ NavGraph
                    └─ GuiV2NavGraph (Compose navigation)
```

GUI1 (`MainScreen` + `TraditionalGUIMainActivity`) is unreachable from the main app flow but still compiles.

---

## Data Consistency

Both GUIs share the **same database, repositories, and ViewModels**. There is zero risk of data loss or inconsistency when switching. The GUI is purely a presentation layer.

- Invoices, customers, payments → same Room database
- PDF generation → same `InvoicePdfService`
- Settings → same `InvoiceSettingsRepository`

---

## How to Switch GUIs (Developer)

To temporarily restore GUI1 for testing, change `computeAppState()` in `AppStateViewModel.kt`:

```kotlin
// Temporarily force GUI1
private fun computeAppState(): AppState = AppReady(GuiMode.GUI1)
```

Revert before merging. GUI1 should not be used in production.

---

## Feature Parity Checklist

See [FEATURE_STATUS_MATRIX.md](FEATURE_STATUS_MATRIX.md) for the full list.

**GUI2-only features (not in GUI1):**
- PDF live preview
- SASS Professional style
- Advanced search/filter
- Mobile-optimised layouts
- Photo attachments

**Remaining GUI2 gaps:**
- QR code PDFs
- Backup/restore operations
- Date range analytics filter

---

## User Communication

When the Q3 2026 deprecation milestone is reached:

1. Add `@Deprecated` annotation to `TraditionalGUIMainActivity`
2. Show one-time in-app dialog: _"You're using the classic interface. Switch to the new experience for better performance and new features."_
3. Include "Switch Now" button that persists the GUI2 preference
4. Include "Remind me later" (shows again after 7 days)

---

## Code Removal Checklist (June 2027)

When ready to remove GUI1:

- [ ] Delete `ui/gui1/` and `ui/MainScreen.kt`
- [ ] Delete `TraditionalGUIMainActivity`
- [ ] Remove `GuiMode.GUI1` from `AppState`
- [ ] Remove `GUISelection` state from `AppStateViewModel`
- [ ] Remove GUI1 references from navigation (`Screen.kt`, `ScreenTitles.kt`)
- [ ] Remove `DUAL_GUI_TECHNICAL_SPEC.md` from docs
- [ ] Update `ARCHITECTURE.md` to remove GUI1 references
- [ ] Run full test suite; fix any GUI1-specific test references
- [ ] Bump `AppDatabase` version if any GUI1-specific schema is removed
