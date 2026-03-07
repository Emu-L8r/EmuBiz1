# Bizap Design System — Material 3

## Overview

The Bizap design system is built on [Material 3](https://m3.material.io/) — Google's modern, expressive design specification. Every color, typography style, spacing value, and component follows the Material 3 guidelines to ensure a clean, minimal, and intuitive user interface.

**Files:**
| File | Purpose |
|---|---|
| `ui/theme/DesignSystem.kt` | Compose constants: colors, typography, spacing, radii |
| `ui/theme/Theme.kt` | `BizapTheme` composable — applies the color scheme and typography |
| `ui/theme/SemanticColors.kt` | Extensions on `ColorScheme` for invoice/risk status colors |
| `res/values/colors.xml` | Material 3 color XML resources (for View-based components) |
| `res/values/dimens.xml` | Spacing and corner-radius XML resources |
| `res/values/styles.xml` | Material 3 component style definitions |

---

## Color Palette

### Primary Brand Colors

| Role | Kotlin val | Hex | Usage |
|---|---|---|---|
| Primary | `md_theme_light_primary` | `#6750A4` | Buttons, FABs, active states |
| On Primary | `md_theme_light_onPrimary` | `#FFFFFF` | Text/icons on primary |
| Primary Container | `md_theme_light_primaryContainer` | `#EADDFF` | Tonal button backgrounds |
| On Primary Container | `md_theme_light_onPrimaryContainer` | `#21005D` | Text on primary container |

### Secondary Colors

| Role | Kotlin val | Hex | Usage |
|---|---|---|---|
| Secondary | `md_theme_light_secondary` | `#625B71` | Supporting actions |
| On Secondary | `md_theme_light_onSecondary` | `#FFFFFF` | Text/icons on secondary |
| Secondary Container | `md_theme_light_secondaryContainer` | `#E8DEF8` | Highlighted secondary surface |
| On Secondary Container | `md_theme_light_onSecondaryContainer` | `#1D192B` | Text on secondary container |

### Tertiary Colors

| Role | Kotlin val | Hex | Usage |
|---|---|---|---|
| Tertiary | `md_theme_light_tertiary` | `#7D5260` | Contrasting accents |
| On Tertiary | `md_theme_light_onTertiary` | `#FFFFFF` | Text/icons on tertiary |
| Tertiary Container | `md_theme_light_tertiaryContainer` | `#FFD8E4` | Tertiary highlight surface |
| On Tertiary Container | `md_theme_light_onTertiaryContainer` | `#31111D` | Text on tertiary container |

### Neutral (Surface / Background)

| Role | Kotlin val | Hex | Usage |
|---|---|---|---|
| Surface | `md_theme_light_surface` | `#FAFAFA` | Card and panel backgrounds |
| On Surface | `md_theme_light_onSurface` | `#1C1C1C` | Primary text on surfaces |
| Surface Variant | `md_theme_light_surfaceVariant` | `#E7E0EC` | Input fields, chips |
| On Surface Variant | `md_theme_light_onSurfaceVariant` | `#49454F` | Secondary text on surfaces |
| Background | `md_theme_light_background` | `#FBFDF8` | Screen background |
| On Background | `md_theme_light_onBackground` | `#191C19` | Text on background |

### Semantic / Status Colors

| Name | Kotlin val | Hex | Usage |
|---|---|---|---|
| Success | `semanticSuccess` | `#4CAF50` | PAID invoices, positive metrics |
| Warning | `semanticWarning` | `#FFA500` | PARTIALLY_PAID, caution states |
| Error | `semanticError` | `#B3261E` | OVERDUE invoices, errors |
| Info | `semanticInfo` | `#2196F3` | Informational badges, links |

---

## Typography Hierarchy

All styles are defined as `TextStyle` values in `DesignSystem.kt` and mapped to Material 3 roles via `Typography.kt`.

| Scale | Font Size | Weight | Line Height | Usage |
|---|---|---|---|---|
| `displayLarge` | 57 sp | Bold | 64 sp | Hero numbers, splash screens |
| `displayMedium` | 45 sp | Bold | 52 sp | Large metric values |
| `displaySmall` | 36 sp | Bold | 44 sp | Section heroes |
| `headlineLarge` | 32 sp | Bold | 40 sp | Page titles |
| `headlineMedium` | 28 sp | Bold | 36 sp | Card headers |
| `headlineSmall` | 24 sp | Bold | 32 sp | Dialog titles, section titles |
| `titleLarge` | 22 sp | SemiBold | 28 sp | List item titles |
| `titleMedium` | 16 sp | SemiBold | 24 sp | Sub-section headers |
| `titleSmall` | 14 sp | SemiBold | 20 sp | Compact item labels |
| `bodyLarge` | 16 sp | Normal | 24 sp | Primary body text |
| `bodyMedium` | 14 sp | Normal | 20 sp | Secondary body text |
| `bodySmall` | 12 sp | Normal | 16 sp | Captions, descriptions |
| `labelLarge` | 14 sp | SemiBold | 20 sp | Button text |
| `labelMedium` | 12 sp | SemiBold | 16 sp | Chip labels, tabs |
| `labelSmall` | 11 sp | SemiBold | 16 sp | Overlines, micro labels |

---

## Spacing Guidelines

The spacing system uses an **8 dp baseline grid**. All layout padding, margins, and gaps should be multiples of 4 dp (preferred multiples of 8 dp).

| Kotlin val | XML resource | dp | Usage |
|---|---|---|---|
| `spacing2` | `@dimen/spacing_2` | 2 dp | Icon internal padding, dividers |
| `spacing4` | `@dimen/spacing_4` | 4 dp | Icon-to-label gap, tight chips |
| `spacing8` | `@dimen/spacing_8` | 8 dp | Inner component padding |
| `spacing12` | `@dimen/spacing_12` | 12 dp | List item vertical padding |
| `spacing16` | `@dimen/spacing_16` | 16 dp | Standard screen horizontal margin |
| `spacing24` | `@dimen/spacing_24` | 24 dp | Card inner padding |
| `spacing32` | `@dimen/spacing_32` | 32 dp | Section vertical gaps |
| `spacing48` | `@dimen/spacing_48` | 48 dp | Large vertical gaps, hero areas |

---

## Corner Radius Scale

| Kotlin val | XML resource | dp | Components |
|---|---|---|---|
| `cornerRadiusSmall` | `@dimen/corner_radius_small` | 8 dp | Chips, text fields, small cards |
| `cornerRadiusMedium` | `@dimen/corner_radius_medium` | 12 dp | Cards, dialog containers |
| `cornerRadiusLarge` | `@dimen/corner_radius_large` | 16 dp | Bottom sheets, large cards |
| `cornerRadiusExtraLarge` | `@dimen/corner_radius_extra_large` | 28 dp | FABs, fully-rounded elements |

---

## Component Specifications

### Buttons

| Style | XML style | Usage |
|---|---|---|
| Filled | `Widget.Bizap.Button.Filled` | Primary CTA |
| Outlined | `Widget.Bizap.Button.Outlined` | Secondary action |
| Text | `Widget.Bizap.Button.Text` | Inline actions |
| Elevated | `Widget.Bizap.Button.Elevated` | Prominent secondary |
| Tonal | `Widget.Bizap.Button.Tonal` | Accent secondary |

### Cards

| Style | XML style | Usage |
|---|---|---|
| Elevated | `Widget.Bizap.Card.Elevated` | Standard content cards |
| Outlined | `Widget.Bizap.Card.Outlined` | Selectable / bordered cards |
| Filled | `Widget.Bizap.Card.Filled` | Tonal highlight cards |

### Text Fields

| Style | XML style | Usage |
|---|---|---|
| Outlined | `Widget.Bizap.TextInputLayout.Outlined` | Forms, search |
| Filled | `Widget.Bizap.TextInputLayout.Filled` | Inline edit fields |

### Chips

| Style | XML style | Usage |
|---|---|---|
| Input | `Widget.Bizap.Chip.Input` | User-entered tags |
| Filter | `Widget.Bizap.Chip.Filter` | List filters |
| Suggestion | `Widget.Bizap.Chip.Suggestion` | Autocomplete |
| Assist | `Widget.Bizap.Chip.Assist` | Assistive shortcuts |

---

## Animation / Transition Specifications

Follow Material 3 motion guidelines:

- **Standard easing** (`FastOutSlowIn`) — screen transitions, shared element transitions
- **Decelerate easing** (`LinearOutSlowIn`) — elements entering the screen
- **Accelerate easing** (`FastOutLinearIn`) — elements leaving the screen
- **Duration short** — 100–200 ms (micro-interactions: ripples, checkbox)
- **Duration medium** — 250–400 ms (component transitions: bottom sheet, dialog)
- **Duration long** — 400–500 ms (full-screen transitions)

---

## Accessibility Guidelines

1. **Color contrast** — maintain a minimum 4.5:1 contrast ratio for body text; 3:1 for large text and UI components.
2. **Touch targets** — all interactive elements must be at least **48 × 48 dp**.
3. **Content descriptions** — every `Icon` composable must supply a non-null `contentDescription`.
4. **Typography scale** — support dynamic text sizes (do not hard-code `sp` in XML layouts outside the design system).
5. **Focus order** — ensure logical tab/focus traversal for keyboard and switch-access users.
6. **Error messaging** — display inline error text below fields; never rely on color alone.

---

## Dual GUI Architecture

The app supports two UI experiences selectable from the **Landing Screen**:

| Mode | `GuiMode` | Description |
|---|---|---|
| Classic (GUI1) | `GuiMode.GUI1` | Original app — lists, tables, familiar navigation |
| New Experience (GUI2) | `GuiMode.GUI2` | Material 3 cards, context-aware navigation, unified dashboards |

The user's selection is persisted via **DataStore** (`LandingViewModel`) and respected on every subsequent launch. The selection can be changed at any time through Settings.
