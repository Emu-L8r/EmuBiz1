# 📐 INVOICE DESIGN SPECIFICATION V1.0
## Professional, Grid-Based Invoice PDF Layout System

**Status:** PHASE 1 Foundation Complete  
**Last Updated:** April 4, 2026  
**Prepared for:** Bizap Invoice PDF Redesign  

---

## Executive Summary

This specification defines the professional, grid-based layout system for Bizap invoice PDFs. It replaces the previous arbitrary spacing approach with a **systematic, measurable design framework** that produces invoices with:

- ✅ **85%+ page coverage** (instead of 40-50% wasted space)
- ✅ **Intentional spacing** (12px max gaps, not random values)
- ✅ **Visual hierarchy** (sections flow together, not scattered)
- ✅ **Professional appearance** (clean, organized, artistic)
- ✅ **Consistency** (Canvas and HTML rendering use same system)

---

## Part 1: Grid System Foundation

### Base Grid Unit
- **Grid Unit:** 8px
- **Rationale:** Standard for modern web/print design; divisible into most common measurements
- **Application:** All horizontal and vertical spacing calculated as multiples of 8px

### Page Dimensions (A4 Standard)
| Dimension | Value | Notes |
|-----------|-------|-------|
| Page Width | 210mm | 595px |
| Page Height | 297mm | 842px |
| Left Margin | 15mm | ≈ 42.5px |
| Right Margin | 15mm | ≈ 42.5px |
| Top Margin | 12mm | ≈ 34px |
| Bottom Margin | 10mm | ≈ 28.3px |

### Usable Content Area
- **Width:** 595 - 42.5 - 42.5 = **510px**
- **Height:** 842 - 34 - 28.3 = **779.7px**
- **Target Coverage:** 85% = Use ~662px of 779.7px available

---

## Part 2: Spacing System

### Vertical Spacing (Gap Heights)
| Gap Type | Value | Usage | Density |
|----------|-------|-------|---------|
| Section Gap | 12px | Between major sections (header → bill to) | Medium |
| Subsection Gap | 8px | Between Bill To and Invoice Details | High |
| Line Spacing | 4px | Between text lines within sections | High |
| Density Low | 20px | Visual breathing room (footer, totals emphasis) | Low |

### Horizontal Spacing (Padding)
| Padding | Value | Usage |
|---------|-------|-------|
| Padding H | 12px | Inside cards and containers |
| Padding V | 8px | Top/bottom inside containers |
| Column Gap | 8px | Between table columns |
| Label-Value Gap | 6px | "TOTAL DUE" → "$1,100.00" |

---

## Part 3: Component Measurements & Layout

### HIGH DENSITY ZONE: Header Block (Header + Bill To + Invoice Details)

**Purpose:** Compress essential information into minimal space while maintaining readability

#### 1. HEADER SECTION
```
┌─────────────────────────────────────────────────────────┐
│  📦 ACME CORP                          INVOICE          │  60px height
│  ABN: XX XXX XXX XXX                   INV-2024-001     │
│  +61 2 1234 5678 | hello@acme.com.au   Due: Apr 15      │
└─────────────────────────────────────────────────────────┘
```
- **Height:** 60px (compressed from 100px)
- **Components:**
  - Company name: 18px bold (left-aligned)
  - INVOICE label: 11px gray (right-aligned)
  - Business info: 8px regular, gray
- **Design:**
  - Primary color background
  - White text
  - Left accent bar (4px, secondary color)
  - Diagonal accent overlay (subtle, 5% opacity)

#### 2. BILL TO & INVOICE DETAILS (Side-by-Side)
```
┌──────────────────────────┬──────────────────────────┐
│ BILL TO                  │ INVOICE DETAILS          │ 80px height
│ John Smith               │ INV-2024-001             │ each
│ 123 Main St              │ Date: Apr 1, 2026        │
│ john@example.com         │ Due: Apr 15, 2026        │
│ Mob: +1 555 1234         │ Status: Outstanding      │
└──────────────────────────┴──────────────────────────┘
```
- **Layout:** Two columns, equal width, 8px gap between
- **Each Column Height:** 80px
- **Column Width:** (510 - 8) / 2 = 251px each
- **Design:**
  - White background with 1px border
  - 4px left accent bar (secondary color)
  - 2px drop shadow below
  - 8px padding inside
  - Section labels: 8.5px bold, primary color
  - Content: 10px regular, dark gray

#### Header Block Total Height
```
60px (header) + 12px (gap) + 80px (bill to) = 152px
Used: 152/779.7 = 19.5% of page ✅ (efficient)
```

---

### MEDIUM DENSITY ZONE: Items Table

**Purpose:** Display invoice items in readable but compact format

```
┌──────────────┬──────────┬──────────┬──────────┐
│ Description  │ Qty      │ Unit $   │ Amount   │ Header: 32px
├──────────────┼──────────┼──────────┼──────────┤
│ Web Design   │ 1        │ 500.00   │ 500.00   │ Rows: 28px each
│ Hosting (3mo)│ 3        │ 50.00    │ 150.00   │
│ Support      │ 8        │ 50.00    │ 400.00   │
└──────────────┴──────────┴──────────┴──────────┘
```
- **Row Height:** 28px (readable, not cramped)
- **Header Height:** 32px (bold, colored background)
- **Column Widths:**
  - Description: 50% (255px)
  - Quantity: 15% (76.5px)
  - Unit Price: 17.5% (89.25px)
  - Amount: 17.5% (89.25px)
- **Design:**
  - Alternating row colors (white, light gray) for readability
  - 1px borders, #e0e0e0 color
  - Header: Primary color background, white text, bold

**Density Calculation:**
- Max 20 items on one page: 20 × 28px = 560px
- Plus header: 32px
- Plus gaps: 12px (before) + 12px (after) = 24px
- **Total: ~616px = 79% of available height** ✅

---

### HIGH VISUAL FOCUS ZONE: Totals Section

**Current Problem:** "TOTAL DUE" label crammed next to amount, floating box, cramped appearance  
**New Solution:** Typography-driven hierarchy, integrated layout

```
SUBTOTAL                                    $1,050.00

TAX (10%)                                   $  105.00
────────────────────────────────────────────────────

     TOTAL DUE                              $ 1,155.00
     ═══════════════════════════════════════════════════
```

- **Height:** 40px (tight but breathable)
- **Layout:**
  - Subtotal line: 10px regular
  - Tax line: 10px regular
  - Divider: 1px line, secondary color
  - **Total line:** 16px **BOLD**, primary color (emphasized)
  - Underline: 2px solid accent line (visual emphasis)
- **Spacing:**
  - Label-to-value: 6px gap (aligned right)
  - Line height: 4px
  - No floating box (integrated with document flow)

**Visual Hierarchy:**
1. Eye is drawn to TOTAL DUE (largest, boldest)
2. Tax visible but secondary
3. Subtotal for reference
4. Accent underline adds polish

---

### LOW DENSITY ZONE: Footer

**Purpose:** Visual closure, thank you message, optional QR code

```
┌────────────────────────────────────────┐
│  Thank you for your business!          │  40px
│  Questions? Contact: hello@acme.com.au │
└────────────────────────────────────────┘
```
- **Height:** 40px
- **Position:** Bottom of page (10mm from edge)
- **Design:**
  - Subtle background gradient or accent line
  - 9px regular text, gray (#666)
  - Centered alignment

---

## Part 4: Typography Hierarchy

### Font Sizes
| Element | Size | Weight | Color | Usage |
|---------|------|--------|-------|-------|
| Header (Company) | 18px | Bold | #fff | Business name in header |
| Section Headers | 11px | Bold | Primary | "BILL TO", "INVOICE DETAILS" |
| Body Text | 10px | Regular | #333 | Customer details, items |
| Small Labels | 9px | Regular | #666 | ABN, phone, secondary info |
| Table Text | 10px | Regular | #333 | Items table content |
| **Total Amount** | **16px** | **Bold** | **Primary** | **TOTAL DUE value** |
| Total Label | 11px | Regular | #333 | "TOTAL DUE:" text |
| Footer | 9px | Regular | #666 | Thank you message |

### Font Family
- **Primary:** System fonts (Segoe UI, Arial, Helvetica)
- **Fallback:** Sans-serif
- **Rationale:** Ensures consistent rendering across Canvas and HTML rendering

### Line Height
- **Standard:** 1.4 × font size
- **Headers:** 1.2 × font size (tighter)
- **Body:** 1.6 × font size (readable)

---

## Part 5: Color & Design System

### Color Palette
| Role | Color | Hex | Usage |
|------|-------|-----|-------|
| Primary | Professional Purple | #6B4C9A | Headers, emphasis, accents |
| Secondary | Light Gray | #f5f5f5 | Backgrounds, accents |
| Text | Dark Gray | #333333 | Body text |
| Text Light | Medium Gray | #666666 | Secondary text |
| Border | Light Border | #d0d0d0 | Card borders |
| Accent | Dark Blue-Gray | #2c3e50 | Accents, emphasis |
| Success | Green | #27ae60 | "PAID" status |
| Error | Red | #e74c3c | "OVERDUE" status |

### Visual Elements
- **Accent Bar:** 4px solid, left side of cards (secondary color)
- **Borders:** 0.8px solid, #d0d0d0 (subtle)
- **Shadow:** 2px offset, 15% black opacity (depth)
- **Corner Radius:** 8px on cards (modern, not sharp)
- **Divider Lines:** 1px, secondary color (visual separation)

---

## Part 6: Density Zones Summary

### Zone 1: HIGH DENSITY (Header + Bill To)
- **Goal:** Essential info in minimal space
- **Spacing:** 6-8px gaps
- **Coverage:** ~152px / 779.7px = 19.5%
- **Result:** Professional, organized, dense

### Zone 2: MEDIUM DENSITY (Items Table)
- **Goal:** Readable but compact
- **Row Height:** 28px (readable)
- **Coverage:** ~616px / 779.7px = 79%
- **Result:** Fits 20+ items on one page

### Zone 3: HIGH VISUAL FOCUS (Totals)
- **Goal:** Total Due stands out
- **Method:** Large typography (16px bold)
- **Design:** Accent underline, color emphasis
- **Result:** Eye goes directly to amount

### Zone 4: LOW DENSITY (Footer)
- **Goal:** Breathing room for closure
- **Spacing:** 20px gaps
- **Design:** Gradient or accent line
- **Result:** Professional, intentional finish

---

## Part 7: Validation Checklist

Use this checklist to validate implementations against the spec.

### Spacing Validation
- [ ] Header height: 60px ±2px (was 100px)
- [ ] Bill To height: 80px ±2px
- [ ] Section gaps: 12px ±1px
- [ ] Table row height: 28px ±1px
- [ ] Totals height: 40px ±2px
- [ ] Footer height: 40px ±2px
- [ ] No gaps >20px (except intentional LOW DENSITY zones)

### Typography Validation
- [ ] Company name: 18px bold, white on color
- [ ] Section headers: 11px bold, primary color
- [ ] Body text: 10px regular, #333
- [ ] Total amount: 16px bold, primary color
- [ ] All fonts: Sans-serif (system fonts)
- [ ] Line heights: Consistent within section

### Density Validation
- [ ] Header block: 152px (19.5% of page)
- [ ] Items table: Can fit 20+ items
- [ ] Page coverage: 85%+ used (not 40-50% wasted)
- [ ] All sections visible on first page (invoices <20 items)
- [ ] No section has >20px uninterrupted whitespace

### Visual Hierarchy Validation
- [ ] TOTAL DUE is largest, boldest element
- [ ] Eye naturally flows: Header → Bill To → Items → Total
- [ ] Accent bars connect sections visually
- [ ] Cards have consistent styling (border, shadow, padding)
- [ ] No floating elements (everything integrated)

### Integration Validation
- [ ] Header visually connects to content (accent line, color)
- [ ] Bill To and Invoice Details feel like one block (side-by-side)
- [ ] Items table integrates naturally below header block
- [ ] Totals flow from items table (no gap >12px)
- [ ] Footer has intentional breathing room (LOW DENSITY)

### Contrast Validation
- [ ] Text on backgrounds: WCAG AA compliant
- [ ] Colors: Not ambiguous when printed (B&W printer test)
- [ ] Borders: Visible but not distracting
- [ ] Accent colors: Support, not compete with content

---

## Part 8: Implementation Roadmap

### Phase 1: Foundation (CURRENT - April 4)
✅ Define spacing constants (`InvoiceSpacingConfig.kt`)  
✅ Create grid layout manager (`GridLayoutManager.kt`)  
✅ Document design specification (this document)  

### Phase 2: Canvas Implementation (Week 2)
- Refactor `InvoicePdfService.kt` to use `GridLayoutManager`
- Replace hardcoded coordinates with grid calculations
- Update Canvas rendering to follow density zones
- Test with 3-item and 20-item invoices

### Phase 3: HTML Template Implementation (Week 2-3)
- Create `invoice-styles-refined.css`
- Add `HtmlInvoiceStyle.REFINED` enum option
- Implement `generateRefinedProfessionalTemplate()`
- Update `HtmlPdfInvoiceService.kt` router

### Phase 4: UI & Deployment (Week 3)
- Set REFINED as default theme
- Update `InvoiceSettingsScreen.kt`
- Create side-by-side comparison test
- Validate with actual PDF output

### Phase 5: Refinement & Polish (Week 4)
- Add design details (colors, typography finesse)
- Optimize for different paper sizes
- Performance testing (PDF generation speed)

---

## Part 9: Troubleshooting

### Issue: "PDF still looks cramped"
**Cause:** Padding values too large (>12px)  
**Solution:** Reference `InvoiceSpacingConfig` - use exact values, don't adjust by eye

### Issue: "Total Due isn't prominent enough"
**Cause:** Font size too small or color too light  
**Solution:** Use 16px bold, primary color (#6B4C9A) + accent underline

### Issue: "Table rows don't align"
**Cause:** Not using `GridLayoutManager.getItemRowY()`  
**Solution:** Calculate all Y positions through manager, not hardcoded values

### Issue: "Different on Canvas vs HTML"
**Cause:** Not sharing `InvoiceSpacingConfig` constants  
**Solution:** Both Canvas and HTML renderers must import and use same constants

### Issue: "Page coverage still <70%"
**Cause:** Padding/gaps too large, not using HIGH DENSITY zone appropriately  
**Solution:** Review section gaps - should be 12px max (6px in HIGH DENSITY zones)

---

## Part 10: Future Enhancements

### Optional Improvements (Not in Phase 1)
- [ ] Multiple paper sizes (A4, Letter, A3)
- [ ] Custom accent color selection (user can choose primary color)
- [ ] Logo integration (header right corner)
- [ ] Watermark (PAID/OVERDUE status)
- [ ] Multi-page invoices with automatic page breaks
- [ ] QR code linking to payment portal
- [ ] Payment terms footnote with visual styling

---

## Appendix: References

### Design References
- Stripe Invoice PDF: Minimal, professional, high density
- FreshBooks Invoice: Typography-driven, modern spacing
- Wave Invoice: Clean, organized, readable

### Technical References
- A4 Page Dimensions: 210mm × 297mm (595px × 842px)
- Grid System: 8px standard (Material Design, CSS grids)
- Typography Scale: 1.5x multiplier (8, 10, 11, 14, 16, 18, 24px)
- Color Contrast: WCAG AA (4.5:1 for body text)

### Tools Used
- `InvoiceSpacingConfig.kt`: Constants definition
- `GridLayoutManager.kt`: Coordinate calculation
- `PdfDocument` (Android): Canvas rendering
- `iText7`: HTML-to-PDF conversion

---

**Document Version:** 1.0  
**Last Updated:** April 4, 2026  
**Status:** READY FOR IMPLEMENTATION  
**Next Review:** Upon Phase 2 completion

