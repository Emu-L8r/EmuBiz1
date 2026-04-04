# 🚀 PHASE 2 QUICK START GUIDE - Settings UI & Preview

**Phase Status:** Ready to start  
**Estimated Duration:** 3-4 hours  
**Complexity:** Medium  

---

## 📋 Phase 2 Objectives

### Objective 1: Redesign Settings Screen UI (2 hours)
Add 5-section invoice settings interface:

1. **PDF Engine Selection**
   - Two radio buttons: Canvas | HTML+CSS
   - Description for each
   - Visual indicator for current selection

2. **Page Layout Selection**
   - Two buttons/chips: Classic | Modern
   - Description for each
   - Visual layout preview or icon

3. **Template Selection**
   - 4 buttons showing templates (dynamic based on engine)
   - Canvas templates: Modern, Professional, Creative, Minimal
   - HTML templates: Modern, Minimal, Corporate, Creative
   - Color swatches for visual preview

4. **Preview Mode Toggle**
   - Switch: "Preview with sample data"
   - Description: "Show how invoice looks without real data"
   - Toggle updates `previewWithPlaceholder` in ViewModel

5. **Live Preview Box**
   - Placeholder: Gray rectangle (200x280dp)
   - Label: "PDF Preview (Coming Soon)"
   - Later: Replace with actual PDF rendering

### Objective 2: Wire Preview System (1 hour)
- `InvoiceSettingsViewModel.updatePreviewWithPlaceholder(Boolean)`
- Pass `previewWithPlaceholder` to PDF generation
- Modify `InvoicePdfService.generatePdf()` to use placeholder when flag is true

### Objective 3: Integrate Page Layouts (1-2 hours)
- Update `HtmlPdfInvoiceService` to accept layout parameter
- Route to correct layout based on `InvoiceSettings.selectedPageLayout`
- Test both layouts generate valid PDFs

---

## 🎨 UI Design Reference

```
┌─────────────────────────────────────────────────────────┐
│ PDF SETTINGS                                      [←Back]│
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 1️⃣  PDF RENDERING ENGINE                             │
│  ┌─────────────┐        ┌──────────────┐             │
│  │ 🎨 Canvas   │        │ 📄 HTML+CSS  │             │
│  │ (selected)  │        │              │             │
│  └─────────────┘        └──────────────┘             │
│  Direct coordinate       CSS-based styling           │
│  control, artistic       Professional layouts        │
│                                                         │
│ 2️⃣  PAGE LAYOUT                                        │
│  ┌──────────────┐      ┌──────────────┐             │
│  │ 📋 Classic   │      │ 🎯 Modern    │             │
│  │ (Traditional)│      │ (Compact)    │             │
│  │              │      │ [selected]   │             │
│  └──────────────┘      └──────────────┘             │
│                                                         │
│ 3️⃣  INVOICE TEMPLATE                                   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐ │
│  │🎨 Modern │ │💼 Corp   │ │✨ Create │ │▦ Mini  │ │
│  │  ████    │ │  ████    │ │  ████    │ │  ████  │ │
│  │ [sel]    │ │          │ │          │ │        │ │
│  └──────────┘ └──────────┘ └──────────┘ └────────┘ │
│                                                         │
│ 4️⃣  PREVIEW MODE                                       │
│  ☑️  Show sample data preview                         │
│     (Displays invoice with placeholder data)          │
│                                                         │
│ 5️⃣  LIVE PREVIEW                                       │
│  ┌─────────────────────────────────────┐             │
│  │                                     │             │
│  │   PDF Preview                       │             │
│  │   (Coming Soon)                     │             │
│  │   [Gray placeholder area]           │             │
│  │                                     │             │
│  │   200 × 280 dp                      │             │
│  │                                     │             │
│  └─────────────────────────────────────┘             │
│                                                         │
│                      [SAVE SETTINGS]                   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 🔧 Implementation Checklist

### Step 1: UI Components
- [ ] Create `PdfEngineSelector()` composable
- [ ] Create `PageLayoutSelector()` composable
- [ ] Create `TemplateSelector()` composable
- [ ] Create `PreviewToggle()` composable
- [ ] Create `PdfPreviewPlaceholder()` composable

### Step 2: Update InvoiceSettingsScreen
- [ ] Replace existing theme UI with new 5-section layout
- [ ] Wire selectors to ViewModel methods
- [ ] Observe `uiState` for selected values
- [ ] Add save button (already exists, wire to new settings)

### Step 3: Update ViewModel
- [ ] Import new enums (PdfEngine, PageLayout)
- [ ] Methods already added in Phase 1:
  - `updateSelectedPdfEngine()`
  - `updateSelectedPageLayout()`
  - `updatePreviewWithPlaceholder()`

### Step 4: Wire Preview System
- [ ] Pass `previewWithPlaceholder` flag when generating PDF
- [ ] Modify `InvoicePdfService.generatePdf()`:
  ```kotlin
  val invoiceData = if (previewMode && settings.previewWithPlaceholder) {
      PlaceholderInvoiceGenerator.generatePreviewInvoice()
  } else {
      snapshot
  }
  ```

### Step 5: Integrate Page Layouts
- [ ] Update `HtmlPdfInvoiceService.generatePdf()` to accept layout
- [ ] Modify `HtmlPdfInvoiceService.generateHtmlContent()`:
  ```kotlin
  val layout = when (settings.selectedPageLayout) {
      PageLayout.CLASSIC -> ClassicLayout()
      PageLayout.MODERN -> ModernLayout()
  }
  
  val htmlContent = layout.generateHtml(snapshot, cssContent)
  ```
- [ ] Test both layouts produce valid PDFs

### Step 6: Testing
- [ ] Select each engine + layout + template combination
- [ ] Verify correct PDF generated
- [ ] Test preview mode with placeholder data
- [ ] Test save/load settings persistence
- [ ] Build and run on device

---

## 📂 Key Files to Modify

### Already Updated (Phase 1)
- ✅ `InvoiceSettings.kt` - New enums & fields
- ✅ `InvoiceSettingsViewModel.kt` - New update methods
- ✅ `PlaceholderInvoiceGenerator.kt` - Sample data (new)
- ✅ `PageLayout.kt` - Layout system (new)

### To Modify Phase 2
- 🔧 `InvoiceSettingsScreen.kt` - Complete UI redesign
- 🔧 `HtmlPdfInvoiceService.kt` - Integrate layouts
- 🔧 `InvoicePdfService.kt` - Handle preview mode

---

## 💡 Tips

1. **Start with UI first** - Get layout looking good before wiring logic
2. **Use Modifier.weight()** - For responsive layout divisions
3. **Extract composables** - Keep `InvoiceSettingsScreen.kt` readable
4. **Test incrementally** - Wire one section at a time
5. **Use LazyColumn** - For scrollable settings list

---

## 🚀 Success Criteria

Phase 2 is complete when:
- ✅ Settings screen shows all 5 sections
- ✅ All selectors update ViewModel correctly
- ✅ Settings persist across app restarts
- ✅ Preview toggle enables/disables placeholder
- ✅ Classic and Modern layouts generate different PDFs
- ✅ PDF Engine selection routes to correct renderer
- ✅ Template selection applies correct styling
- ✅ Build compiles without errors
- ✅ All functionality testable on device

---

## 📊 Phase 2 Expected Outcomes

From broken PDF system → Professional multi-engine system with:
- ✅ 2 rendering engines (Canvas, HTML+CSS)
- ✅ 2 page layouts (Classic, Modern)
- ✅ 4 template options per engine
- ✅ Live preview capability (stub)
- ✅ Professional settings UI
- ✅ Backward compatible with existing code

**Status: READY TO BEGIN PHASE 2**

---

*Next: Execute Phase 2 implementation*

