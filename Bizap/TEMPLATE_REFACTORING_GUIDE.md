# 📋 TEMPLATE REFACTORING GUIDE - Universal Invoice Grid System

**Date:** April 5, 2026  
**Phase:** Phase B - Template Refactoring  
**Objective:** Update all existing templates to use InvoiceGridSystem for consistency

---

## 🎯 OVERVIEW

All HTML invoice templates must be refactored to use the `InvoiceGridSystem` constants.

**Current Templates to Update:**
1. Modern (HTML) - `invoice-styles.css` + `HtmlPdfInvoiceService.kt`
2. Minimal - `invoice-styles-minimal.css`
3. Corporate - `invoice-styles-corporate.css`
4. Creative - `invoice-styles-creative.css`
5. Refined - `invoice-styles-refined.css`

---

## 📐 SPACING STANDARDIZATION

### Required Changes to ALL CSS Files

**Before:**
```css
/* Inconsistent spacing */
.header { padding: 20px; }           /* Some use 20px */
.card { padding: 12px; }             /* Some use 12px */
.items-row { height: 32px; }         /* Some use 32px */
margin: 10px;                         /* Various margins */
```

**After:**
```css
/* All use unified grid */
:root {
    /* Import from InvoiceGridSystem */
    --page-margin: 15mm;
    --content-width: 510px;
    --padding-large: 16px;
    --padding-medium: 12px;
    --gap-section: 16px;
    --item-row-height: 28px;
    --card-width-pct: 48%;
    --card-gap-pct: 4%;
}

.header { 
    padding: var(--padding-large);
    height: 60px;
    margin: var(--page-margin);
}

.card { 
    padding: var(--padding-large);
    width: var(--card-width-pct);
    margin-bottom: var(--gap-section);
}

.items-row { 
    height: var(--item-row-height);
    padding: var(--padding-small);
}
```

---

## 📋 STEP-BY-STEP REFACTORING CHECKLIST

### For Each Template File:

#### Step 1: Define CSS Variables (Top of file)
- [ ] `--page-margin: 15mm`
- [ ] `--content-width: 510px`
- [ ] `--padding-large: 16px`
- [ ] `--padding-medium: 12px`
- [ ] `--padding-small: 8px`
- [ ] `--gap-section: 16px`
- [ ] `--gap-subsection: 12px`
- [ ] `--item-row-height: 28px`
- [ ] `--card-width-pct: 48%`
- [ ] `--card-gap-pct: 4%`
- [ ] Header colors (primary, secondary, text colors)

#### Step 2: Update Header Section
- [ ] Height: 60px (from InvoiceGridSystem.HEADER_HEIGHT)
- [ ] Padding: 16px (from InvoiceGridSystem.PADDING_LARGE)
- [ ] Margin: 15mm (from InvoiceGridSystem.MARGIN_*_MM)

#### Step 3: Update Cards Section (Bill To & Invoice Details)
- [ ] Each card width: 48%
- [ ] Gap between cards: 4%
- [ ] Card padding: 16px
- [ ] Card height: ~80px max (InvoiceGridSystem.CARD_HEIGHT)

#### Step 4: Update Items Table
- [ ] Row height: 28px (from InvoiceGridSystem.ITEM_ROW_HEIGHT)
- [ ] Column widths:
  - Description: 50%
  - Quantity: 13%
  - Unit Price: 18%
  - Amount: 19%
- [ ] Stripe pattern:
  - Odd rows: #FFFFFF (white)
  - Even rows: #F9F9F9 (light gray)
- [ ] Border: 1px #E0E0E0

#### Step 5: Update Totals Section
- [ ] Gap above: 16px (from InvoiceGridSystem.GAP_SECTION)
- [ ] Padding: 16px
- [ ] Right-aligned
- [ ] Font sizes follow hierarchy (11pt body, 14pt heading, 24pt title)

#### Step 6: Update Payment Section (if present)
- [ ] Gap above: 16px
- [ ] Gap below: 16px
- [ ] Padding: 16px
- [ ] Left border accent: 4px + theme color
- [ ] Background: subtle or transparent

#### Step 7: Update Footer Section
- [ ] Height: 40px (from InvoiceGridSystem.FOOTER_HEIGHT)
- [ ] Padding: 16px
- [ ] Margin: 15mm all sides
- [ ] Font size: 8-9pt (InvoiceGridSystem.FONT_SIZE_TINY)

#### Step 8: Verify Typography
- [ ] Title: 24pt (InvoiceGridSystem.FONT_SIZE_TITLE)
- [ ] Headings: 14pt (InvoiceGridSystem.FONT_SIZE_HEADING)
- [ ] Body: 11pt (InvoiceGridSystem.FONT_SIZE_BODY)
- [ ] Small: 9pt (InvoiceGridSystem.FONT_SIZE_SMALL)
- [ ] Tiny: 8pt (InvoiceGridSystem.FONT_SIZE_TINY)

---

## 🎨 THEME-SPECIFIC CUSTOMIZATION (ONLY)

After refactoring to use grid system, templates can customize:
- ✅ Primary color
- ✅ Secondary color
- ✅ Text colors
- ✅ Fonts
- ✅ Decorative elements (borders, shadows, gradients)

Templates CANNOT customize:
- ❌ Spacing/padding
- ❌ Section heights
- ❌ Margins
- ❌ Column widths
- ❌ Row heights

---

## 📝 EXAMPLE: Modern Template Refactoring

**Before (Inconsistent):**
```css
.header {
    background: linear-gradient(135deg, #6B4C9A 0%, #a372c8 100%);
    padding: 20px 16px;
    height: auto;
    margin: 10px;
}

.card {
    border-left: 3px solid #FF9F43;
    padding: 14px;
    margin-bottom: 20px;
    width: 45%;
}

.item-row {
    height: 32px;
    padding: 10px;
    background: white;
}

.item-row.even {
    background: #fafafa;
}
```

**After (Grid System):**
```css
:root {
    --page-margin: 15mm;
    --content-width: 510px;
    --padding-large: 16px;
    --gap-section: 16px;
    --item-row-height: 28px;
    --card-width-pct: 48%;
    --card-gap-pct: 4%;
    --primary-color: #6B4C9A;
    --secondary-color: #FF9F43;
}

.header {
    background: linear-gradient(135deg, var(--primary-color) 0%, #a372c8 100%);
    padding: var(--padding-large);
    height: 60px;
    margin: var(--page-margin);
}

.card {
    border-left: 4px solid var(--secondary-color);
    padding: var(--padding-large);
    margin-bottom: var(--gap-section);
    width: var(--card-width-pct);
    gap: var(--card-gap-pct);
}

.item-row {
    height: var(--item-row-height);
    padding: var(--padding-small);
    background: #FFFFFF;
}

.item-row.even {
    background: #F9F9F9;
}
```

---

## ✅ VERIFICATION CHECKLIST

After refactoring each template, verify:

- [ ] All margins are 15mm
- [ ] All section gaps are 16px
- [ ] All padding is 16px (or 12px/8px as specified)
- [ ] Items table rows are exactly 28px
- [ ] Cards are 48% width with 4% gap
- [ ] Column widths are 50/13/18/19%
- [ ] Stripe pattern is white/#F9F9F9
- [ ] Font sizes follow hierarchy
- [ ] Header is 60px
- [ ] Footer is 40px
- [ ] Border is 1px #E0E0E0
- [ ] No custom spacing unique to this template

---

## 📊 REFACTORING TIMELINE

**Modern Template:** 1 hour (as reference)  
**Minimal Template:** 45 min  
**Corporate Template:** 45 min  
**Creative Template:** 45 min  
**Refined Template:** 45 min  

**Total Phase B time:** ~4 hours

---

## 🎯 EXPECTED RESULT

**Before:**
```
Template Quality Assessment:
├─ Modern:    85% (pretty good)
├─ Minimal:   70% (acceptable)
├─ Corporate: 65% (okay)
├─ Creative:  55% (needs work)
└─ Refined:   60% (okay)
```

**After (All using InvoiceGridSystem):**
```
Template Quality Assessment:
├─ Modern:    100% (excellent - consistent grid)
├─ Minimal:   100% (excellent - consistent grid)
├─ Corporate: 100% (excellent - consistent grid)
├─ Creative:  100% (excellent - consistent grid)
└─ Refined:   100% (excellent - consistent grid)
```

---

## 🚀 NEXT STEPS

1. ✅ **Phase A Complete:** Flicker fix applied + Grid System created
2. ⏳ **Phase B (Option):** Refactor templates using this guide
3. ⏳ **Phase C (Optional):** QA testing with 3, 10, 25-item invoices

**Begin Phase B whenever ready!**


