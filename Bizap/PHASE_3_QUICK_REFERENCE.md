# 🚀 PHASE 3 QUICK REFERENCE - What's Ready to Use

## Files Created & Available

### 1. Layout System
```
PageLayoutProvider.kt
├── PageLayoutProvider (interface)
│   └── fun buildInvoiceHtml(snapshot, isQuote, colorScheme): String
├── InvoiceColorScheme (data class)
│   ├── primaryColor: String
│   ├── accentColor: String
│   ├── lightBackground: String
│   ├── textDark: String
│   ├── textLight: String
│   └── borderColor: String
├── ClassicPageLayout (stub)
│   └── Awaits: buildInvoiceHtml implementation
└── ModernPageLayout (stub)
    └── Awaits: buildInvoiceHtml implementation
```

### 2. Layout Factory
```
PageLayoutFactory.kt
├── PageLayoutFactory (object)
│   ├── fun createLayout(PageLayout): PageLayoutProvider
│   └── fun getLayoutName(PageLayout): String
└── PageLayoutManager (class)
    ├── fun generateInvoiceHtmlWithLayout(...): String
    └── fun extractColorScheme(InvoiceSettings): InvoiceColorScheme
```

### 3. Preview System
```
PlaceholderInvoiceGenerator.kt
├── fun generatePreviewInvoice(): InvoiceSnapshot
│   └── Full invoice with 3 line items, complete data
├── fun generateMinimalPreviewInvoice(): InvoiceSnapshot
│   └── Minimal invoice with sparse data for testing
└── fun generatePreviewInvoiceWithBusinessName(name): InvoiceSnapshot
    └── Preview with custom business name
```

### 4. PDF Generation with Preview
```
PdfGenerationWithPreview.kt
├── fun generatePdfWithPreviewSupport(...): File
│   └── Routes to preview data if previewWithPlaceholder=true
├── fun generatePreviewPdf(...): File
│   └── Always uses placeholder data
└── fun generateMinimalPreviewPdf(): File
    └── Quick test variant
```

---

## How to Use These Components

### Using the Placeholder Generator
```kotlin
// Full preview invoice
val preview = PlaceholderInvoiceGenerator.generatePreviewInvoice()
// Now preview is an InvoiceSnapshot with sample data

// Minimal preview (sparse data)
val minimal = PlaceholderInvoiceGenerator.generateMinimalPreviewInvoice()

// Custom business name
val custom = PlaceholderInvoiceGenerator.generatePreviewInvoiceWithBusinessName("XYZ Corp")
```

### Creating a Layout
```kotlin
// Get a layout instance
val layout = PageLayoutFactory.createLayout(PageLayout.MODERN)

// Get layout name for logging
val name = PageLayoutFactory.getLayoutName(PageLayout.CLASSIC)
// Output: "CLASSIC" or "MODERN"

// Get color scheme from settings
val manager = PageLayoutManager()
val colors = manager.extractColorScheme(settings)

// Generate HTML with layout
val html = manager.generateInvoiceHtmlWithLayout(
    snapshot = invoiceSnapshot,
    layout = PageLayout.MODERN,
    colorScheme = colors,
    isQuote = false
)
```

### Using Preview Mode
```kotlin
val generator = PdfGenerationWithPreview(context, settings)

// Generate PDF with preview support
// If settings.previewWithPlaceholder = true, uses placeholder data
// Otherwise uses the provided snapshot
val pdfFile = generator.generatePdfWithPreviewSupport(
    snapshot = realInvoiceSnapshot,
    isQuote = false,
    overwriteExisting = true
)

// Always use placeholder (for preview display)
val previewPdf = generator.generatePreviewPdf()
```

---

## Integration Points

### In InvoiceViewModel
```kotlin
// When user generates PDF
val generator = PdfGenerationWithPreview(context, settings)
val file = generator.generatePdfWithPreviewSupport(
    snapshot = currentInvoice,
    isQuote = isQuote,
    overwriteExisting = overwriteExisting
)
// If settings.previewWithPlaceholder is true, 
// it will use placeholder data instead of currentInvoice
```

### In HtmlPdfInvoiceService
```kotlin
// Current method already captures layout:
private fun generateHtmlContent(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
    val layout = settings?.selectedPageLayout  // Now available!
    Timber.d("📐 Generating HTML with layout=$layout")
    
    // TODO: Route by layout
    // if (layout == PageLayout.MODERN) {
    //     return buildModernLayoutHtml(snapshot, isQuote)
    // } else {
    //     return buildClassicLayoutHtml(snapshot, isQuote)
    // }
}
```

---

## Data Available

### Sample Invoice Data
```
Business: ACME Corporation
Customer: Smith & Associates, Inc.
Invoice #: INV-2026-04-001

Line Items:
1. Professional Services (40 hrs × $150) = $6,000.00
2. Software License (Annual) = $500.00
3. Support & Maintenance = $300.00

Subtotal: $6,800.00
Tax (10%): $680.00
Total: $7,480.00

Payment: Bank transfer (Commonwealth Bank)
```

### All Values Stored as Long (Cents)
- Subtotal: 680000L ($6,800.00)
- Tax: 68000L ($680.00)
- Total: 748000L ($7,480.00)
- Unit Prices: in cents (15000L = $150.00)

---

## What to Implement Next

### Step 1: Implement ClassicPageLayout
```kotlin
override fun buildInvoiceHtml(
    snapshot: InvoiceSnapshot,
    isQuote: Boolean,
    colorScheme: InvoiceColorScheme
): String {
    // Use existing template structure from HtmlPdfInvoiceService
    // Example: modern template has header, bill-to, items, totals, payment, notes, footer
}
```

### Step 2: Implement ModernPageLayout
```kotlin
override fun buildInvoiceHtml(
    snapshot: InvoiceSnapshot,
    isQuote: Boolean,
    colorScheme: InvoiceColorScheme
): String {
    // Create compact grid-based layout
    // Side-by-side sections, optimized spacing
}
```

### Step 3: Update HtmlPdfInvoiceService
```kotlin
private fun generateHtmlContent(snapshot: InvoiceSnapshot, isQuote: Boolean): String {
    val layout = settings?.selectedPageLayout ?: PageLayout.CLASSIC
    
    // Create manager
    val layoutManager = PageLayoutManager()
    val colors = layoutManager.extractColorScheme(settings!!)
    
    // If using layout-based generation
    if (useLayoutAwareGeneration) {
        return layoutManager.generateInvoiceHtmlWithLayout(
            snapshot = clean,
            layout = layout,
            colorScheme = colors,
            isQuote = isQuote
        )
    }
    
    // Fall back to style-based (existing)
    val style = settings.selectedHtmlStyle ?: HtmlInvoiceStyle.MODERN
    return when (style) {
        HtmlInvoiceStyle.MODERN -> generateModernTemplate(clean, isQuote)
        // ... other templates
    }
}
```

### Step 4: Test
```kotlin
// Test Classic layout with placeholder
val preview = PlaceholderInvoiceGenerator.generatePreviewInvoice()
val layout = PageLayoutFactory.createLayout(PageLayout.CLASSIC)
val colors = InvoiceColorScheme()
val html = layout.buildInvoiceHtml(preview, false, colors)
// Verify HTML looks correct

// Test Modern layout
val modernLayout = PageLayoutFactory.createLayout(PageLayout.MODERN)
val modernHtml = modernLayout.buildInvoiceHtml(preview, false, colors)
// Verify Modern layout looks correct

// Test preview mode
val generator = PdfGenerationWithPreview(context, settings)
settings.previewWithPlaceholder = true
val pdfWithPreview = generator.generatePdfWithPreviewSupport(realInvoice)
// Should use placeholder data
```

---

## Testing Checklist

### Build & Compile
- [x] All files compile
- [x] Zero errors
- [x] Clean build passes

### Layout Factory
- [ ] CreateLayout(CLASSIC) returns ClassicPageLayout
- [ ] CreateLayout(MODERN) returns ModernPageLayout
- [ ] GetLayoutName returns correct names

### Placeholder Generator
- [ ] generatePreviewInvoice() returns full invoice
- [ ] generateMinimalPreviewInvoice() returns sparse invoice
- [ ] All fields properly populated
- [ ] All amounts in correct format (Long/cents)

### Preview System
- [ ] generatePdfWithPreviewSupport uses placeholder when enabled
- [ ] generatePdfWithPreviewSupport uses real data when disabled
- [ ] Logging shows preview mode status

### HTML Generation (Phase 3 Continued)
- [ ] Classic layout generates valid HTML
- [ ] Modern layout generates valid HTML
- [ ] Colors applied correctly
- [ ] No overlapping text

### Integration
- [ ] Settings persist selectedPageLayout
- [ ] ViewModel updates when layout changed
- [ ] PDF uses selected layout
- [ ] Preview mode toggles properly

---

## File Locations Reference

```
app/src/main/java/com/emul8r/bizap/
├── domain/model/
│   └── PageLayoutProvider.kt ← Layout interface & implementations
├── data/service/
│   ├── HtmlPdfInvoiceService.kt ← Modified to log layout
│   └── layout/
│       └── PageLayoutFactory.kt ← Layout factory & manager
│   └── preview/
│       ├── PlaceholderInvoiceGenerator.kt ← Sample data
│       └── PdfGenerationWithPreview.kt ← Preview mode manager
```

---

## Build Verification

```bash
# Build all code
./gradlew build -x test

# Expected output:
# BUILD SUCCESSFUL in ~30s
# 0 compilation errors
```

---

## Next Steps Summary

1. **Implement ClassicPageLayout.buildInvoiceHtml()**
2. **Implement ModernPageLayout.buildInvoiceHtml()**
3. **Update HtmlPdfInvoiceService to route by layout**
4. **Wire PdfGenerationWithPreview to PDF generation**
5. **Test all 2 layouts × 4 templates = 8 combinations**
6. **Create PdfToImageConverter for preview display**
7. **Test preview mode with live PDF rendering**

---

**Status: ✅ Ready for Phase 3 Implementation**

All foundation is in place. Next session: Implement the HTML builders and complete the integration.


