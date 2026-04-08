# ADR-002: Design System Components

**Status:** Accepted  
**Date:** March 22, 2026  
**Decision Makers:** Development Team  
**Phase:** Phase 1 (Foundation + Baseline)

---

## Context

Bizap v1.0 has grown organically with UI components scattered across the codebase. The dual GUI architecture (Classic and Modern) has led to component duplication and inconsistency:

**Current Issues:**
- **Duplicate Components:** Similar components exist in both `ui/` (GUI1) and `ui/gui2/` (GUI2) packages
- **Inconsistent Styling:** Colors, typography, spacing vary between screens
- **No Reusability:** Components tightly coupled to specific screens
- **Hard to Maintain:** Changes require updates in multiple places
- **Poor Accessibility:** Inconsistent touch targets, contrast ratios
- **Testing Challenges:** No standardized component testing approach

**Examples of Duplication:**
```
ui/invoices/components/StatusBadge.kt          (GUI1)
ui/gui2/invoices/components/StatusBadgeV2.kt   (GUI2)

ui/dashboard/AnalyticsCard.kt                  (GUI1)
ui/gui2/dashboard/AnalyticsCardV2.kt           (GUI2)

ui/invoices/InvoiceCard.kt                     (GUI1)
ui/gui2/invoices/InvoiceCardV2.kt              (GUI2)
```

**Design System Benefits:**
- Consistent user experience across all screens
- Faster development (reusable components)
- Easier maintenance (single source of truth)
- Better accessibility (standardized patterns)
- Improved testing (component library tests)

---

## Decision

We will create a **comprehensive Design System** for Bizap v1.1 with shared, reusable components following Material Design 3 principles.

**Core Principles:**

1. **Atomic Design Methodology**
   - **Atoms:** Basic building blocks (Button, Text, Icon)
   - **Molecules:** Simple combinations (IconButton, TextFieldWithLabel)
   - **Organisms:** Complex components (InvoiceCard, StatusBadge, AnalyticsCard)
   - **Templates:** Page layouts (ListScreenTemplate, DetailScreenTemplate)
   - **Pages:** Actual screens (InvoiceListScreen, DashboardScreen)

2. **Single Component Library**
   - All components in `ui/components/` package
   - Shared by both GUI1 and GUI2
   - No `V2` suffixes or duplicates
   - Theme-aware (adapts to Classic/Modern theme)

3. **Composition Over Inheritance**
   - Small, focused components
   - Compose components together
   - No deep component hierarchies
   - Prefer function parameters over inheritance

4. **Design Tokens**
   - Centralized theme values
   - Colors, typography, spacing, shapes
   - Consistent across all components
   - Easy to update globally

5. **Accessibility First**
   - Minimum touch target: 48dp
   - Color contrast ≥ 4.5:1 (WCAG AA)
   - Screen reader support
   - Keyboard navigation

---

## Design System Architecture

### Package Structure

```
app/src/main/java/com/emul8r/bizap/
├── ui/
│   ├── components/                    # Design System (NEW)
│   │   ├── atoms/                     # Basic components
│   │   │   ├── BizapButton.kt
│   │   │   ├── BizapText.kt
│   │   │   ├── BizapIcon.kt
│   │   │   └── BizapDivider.kt
│   │   ├── molecules/                 # Simple composites
│   │   │   ├── BizapTextField.kt
│   │   │   ├── BizapSearchBar.kt
│   │   │   ├── BizapToggle.kt
│   │   │   └── BizapChip.kt
│   │   ├── organisms/                 # Complex components
│   │   │   ├── StatusBadge.kt         # Shared by both GUIs
│   │   │   ├── InvoiceCard.kt         # Shared by both GUIs
│   │   │   ├── PaymentCard.kt         # Shared by both GUIs
│   │   │   ├── AnalyticsCard.kt       # Shared by both GUIs
│   │   │   ├── CustomerCard.kt        # Shared by both GUIs
│   │   │   └── EmptyState.kt          # Shared by both GUIs
│   │   └── templates/                 # Page layouts
│   │       ├── ListScreenTemplate.kt
│   │       ├── DetailScreenTemplate.kt
│   │       └── FormScreenTemplate.kt
│   ├── theme/                         # Theme definitions
│   │   ├── Color.kt                   # Design tokens: colors
│   │   ├── Typography.kt              # Design tokens: typography
│   │   ├── Spacing.kt                 # Design tokens: spacing
│   │   ├── Shape.kt                   # Design tokens: shapes
│   │   └── Theme.kt                   # Theme application
│   ├── invoices/                      # GUI1 screens
│   ├── gui2/invoices/                 # GUI2 screens
│   └── ...
```

### Theme Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      BizapTheme                              │
│  - Wraps entire app                                          │
│  - Provides Material3 theme                                  │
│  - Observes ThemeManager for Classic/Modern toggle          │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    Design Tokens                             │
│  Colors:      Primary, Secondary, Background, Surface        │
│  Typography:  DisplayLarge, HeadlineSmall, BodyMedium        │
│  Spacing:     XSmall(4dp), Small(8dp), Medium(16dp)          │
│  Shapes:      ExtraSmall(4dp), Small(8dp), Medium(12dp)     │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                  Component Library                           │
│  - Atoms: BizapButton, BizapText, BizapIcon                 │
│  - Molecules: BizapTextField, BizapSearchBar                │
│  - Organisms: StatusBadge, InvoiceCard, AnalyticsCard       │
└──────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                       Screens                                │
│  - Use design system components                             │
│  - Compose organisms into pages                             │
│  - Apply consistent styling automatically                   │
└──────────────────────────────────────────────────────────────┘
```

---

## Implementation Guidelines

### 1. Design Tokens (Theme Values)

**Color Palette:**
```kotlin
// ui/theme/Color.kt
object BizapColors {
    // Primary colors
    val Primary = Color(0xFF6200EE)
    val PrimaryVariant = Color(0xFF3700B3)
    val Secondary = Color(0xFF03DAC6)
    
    // Status colors
    val StatusPaid = Color(0xFF4CAF50)      // Green
    val StatusOverdue = Color(0xFFF44336)   // Red
    val StatusDraft = Color(0xFF9E9E9E)     // Gray
    val StatusPending = Color(0xFFFF9800)   // Orange
    
    // Background colors
    val BackgroundLight = Color(0xFFFFFFFF)
    val BackgroundDark = Color(0xFF121212)
    val SurfaceLight = Color(0xFFF5F5F5)
    val SurfaceDark = Color(0xFF1E1E1E)
}
```

**Typography:**
```kotlin
// ui/theme/Typography.kt
val BizapTypography = Typography(
    displayLarge = TextStyle(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.SemiBold
    ),
    bodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Normal
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
    )
)
```

**Spacing:**
```kotlin
// ui/theme/Spacing.kt
object BizapSpacing {
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val XLarge = 32.dp
    val XXLarge = 48.dp
    
    // Semantic spacing
    val CardPadding = Medium
    val ScreenPadding = Medium
    val ItemSpacing = Small
    val SectionSpacing = Large
}
```

### 2. Atom Components

**BizapButton:**
```kotlin
// ui/components/atoms/BizapButton.kt
@Composable
fun BizapButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: ButtonVariant = ButtonVariant.Primary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 48.dp),  // Accessibility: min touch target
        enabled = enabled,
        colors = when (variant) {
            ButtonVariant.Primary -> ButtonDefaults.buttonColors()
            ButtonVariant.Secondary -> ButtonDefaults.outlinedButtonColors()
            ButtonVariant.Text -> ButtonDefaults.textButtonColors()
        }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

enum class ButtonVariant {
    Primary, Secondary, Text
}
```

### 3. Organism Components

**StatusBadge (Shared Component):**
```kotlin
// ui/components/organisms/StatusBadge.kt
@Composable
fun StatusBadge(
    status: InvoiceStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, statusText) = when (status) {
        InvoiceStatus.PAID -> Triple(
            BizapColors.StatusPaid.copy(alpha = 0.1f),
            BizapColors.StatusPaid,
            "Paid"
        )
        InvoiceStatus.OVERDUE -> Triple(
            BizapColors.StatusOverdue.copy(alpha = 0.1f),
            BizapColors.StatusOverdue,
            "Overdue"
        )
        InvoiceStatus.DRAFT -> Triple(
            BizapColors.StatusDraft.copy(alpha = 0.1f),
            BizapColors.StatusDraft,
            "Draft"
        )
        InvoiceStatus.PENDING -> Triple(
            BizapColors.StatusPending.copy(alpha = 0.1f),
            BizapColors.StatusPending,
            "Pending"
        )
    }
    
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = backgroundColor
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(
                horizontal = BizapSpacing.Small,
                vertical = BizapSpacing.XSmall
            ),
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}
```

**InvoiceCard (Shared Component):**
```kotlin
// ui/components/organisms/InvoiceCard.kt
@Composable
fun InvoiceCard(
    invoice: Invoice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(BizapSpacing.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = invoice.status)
            }
            
            Spacer(modifier = Modifier.height(BizapSpacing.Small))
            
            Text(
                text = invoice.customerName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(BizapSpacing.XSmall))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total: ${invoice.formattedTotal}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = invoice.formattedDueDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

**AnalyticsCard (Shared Component):**
```kotlin
// ui/components/organisms/AnalyticsCard.kt
@Composable
fun AnalyticsCard(
    title: String,
    value: String,
    trend: Trend? = null,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(BizapSpacing.CardPadding)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                trend?.let { TrendIndicator(it) }
            }
            
            Spacer(modifier = Modifier.height(BizapSpacing.Small))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

data class Trend(val percentage: Float, val isPositive: Boolean)
```

---

## Component Inventory

### Organisms to Extract (Phase 2)

| Component | Current Location | Target Location | Priority |
|-----------|------------------|-----------------|----------|
| StatusBadge | ui/invoices/, ui/gui2/invoices/ | ui/components/organisms/ | High |
| InvoiceCard | ui/invoices/, ui/gui2/invoices/ | ui/components/organisms/ | High |
| PaymentCard | ui/invoices/, ui/gui2/invoices/ | ui/components/organisms/ | High |
| AnalyticsCard | ui/dashboard/, ui/gui2/dashboard/ | ui/components/organisms/ | High |
| CustomerCard | ui/customers/, ui/gui2/customers/ | ui/components/organisms/ | Medium |
| EmptyState | Multiple locations | ui/components/organisms/ | Medium |
| LoadingIndicator | Multiple locations | ui/components/atoms/ | Medium |
| ErrorMessage | Multiple locations | ui/components/molecules/ | Low |

---

## Migration Plan

### Phase 1 (Current - Foundation)
- ✅ Document design system principles (this ADR)
- ✅ Identify components for extraction
- ✅ Define design tokens

### Phase 2 (Refactoring)
- Extract StatusBadge to shared component
- Extract PaymentCard to shared component
- Extract AnalyticsCard to shared component
- Update both GUIs to use shared components
- Delete V2 component duplicates

### Phase 3 (Expansion)
- Extract remaining organisms
- Create molecule library
- Create atom library
- Update all screens to use design system

### Phase 4 (Validation)
- Component library tests
- Visual regression tests
- Accessibility audits
- Performance validation

---

## Consequences

### Positive

✅ **Consistency**
- Unified look and feel across all screens
- No visual discrepancies between GUIs
- Professional, polished appearance

✅ **Developer Productivity**
- Faster feature development (reusable components)
- Less code duplication
- Easier onboarding for new developers

✅ **Maintainability**
- Single source of truth for components
- Changes propagate automatically
- Easier to implement design updates

✅ **Quality**
- Standardized accessibility
- Consistent testing coverage
- Better performance (optimized components)

✅ **Scalability**
- Easy to add new components
- Simple to extend existing components
- Future-proof architecture

### Negative

⚠️ **Initial Investment**
- Time to extract and standardize components
- Need to update all screens
- Potential for regressions during migration

⚠️ **Learning Curve**
- Team must learn design system
- Documentation required
- Code review overhead initially

⚠️ **Flexibility Trade-off**
- Components may not fit all use cases
- Need escape hatches for custom designs
- Balance between consistency and flexibility

---

## Testing Strategy

### Component Tests
```kotlin
// StatusBadgeTest.kt
@Test
fun `StatusBadge displays correct color for paid status`() {
    composeTestRule.setContent {
        StatusBadge(status = InvoiceStatus.PAID)
    }
    
    // Verify badge exists
    composeTestRule.onNodeWithText("Paid").assertExists()
    
    // Verify color (visual regression test)
    composeTestRule.onNodeWithText("Paid")
        .assertBackgroundColor(BizapColors.StatusPaid.copy(alpha = 0.1f))
}
```

### Visual Regression Tests
- Screenshot tests for each component
- Compare against baseline images
- Automated via CI/CD

### Accessibility Tests
- Minimum touch target verification
- Color contrast checks
- Screen reader compatibility

---

## Documentation

Each component must include:
1. **KDoc comments** explaining purpose and usage
2. **@Preview** composables showing variants
3. **Usage examples** in component file
4. **Accessibility notes** (touch targets, contrast)
5. **Design tokens used** (colors, spacing, typography)

---

## Related ADRs

- **ADR-001:** Single Source of Truth for State
- **ADR-003:** Navigation Architecture
- **ADR-004:** ViewModel Scope Per Screen

---

## References

- [Material Design 3](https://m3.material.io/)
- [Jetpack Compose Design System](https://developer.android.com/jetpack/compose/designsystems)
- [Atomic Design](https://bradfrost.com/blog/post/atomic-web-design/)
- [WCAG Accessibility Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)

---

**Last Updated:** March 22, 2026  
**Next Review:** April 22, 2026
