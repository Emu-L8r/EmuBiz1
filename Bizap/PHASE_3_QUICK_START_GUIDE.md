# 🎯 PHASE 3: HTML TEMPLATE IMPLEMENTATION - QUICK START

**Date:** April 4, 2026  
**Status:** Ready to Begin  
**Objective:** Create HTML invoice template using same grid system  
**Estimated Time:** 3-4 hours  

---

## 🎯 PHASE 3 OVERVIEW

**Goal:** Create an HTML/CSS-based invoice template that uses the same grid system and spacing constants as the Canvas version, producing visually identical PDFs.

**Benefit:** Users can choose between Canvas (faster) and HTML (more flexible) rendering.

---

## 📋 PHASE 3 CHECKLIST

### Step 1: Create CSS File (1 hour)
- [ ] Create `invoice-styles-refined.css`
- [ ] Define grid system in CSS
- [ ] Define spacing variables
- [ ] Style header section
- [ ] Style cards (Bill To, Invoice Details)
- [ ] Style items table
- [ ] Style totals section
- [ ] Style footer

### Step 2: Create HTML Template (1.5 hours)
- [ ] Add `generateRefinedTemplate()` method
- [ ] Use same measurements as Canvas
- [ ] Include all invoice sections
- [ ] Apply CSS classes
- [ ] Test HTML output

### Step 3: Integrate into Service (1 hour)
- [ ] Add `HtmlInvoiceStyle.REFINED` enum
- [ ] Update theme selection logic
- [ ] Route to new template
- [ ] Test HTML PDF generation

### Step 4: Test & Validate (0.5 hours)
- [ ] Generate HTML PDFs
- [ ] Compare with Canvas PDFs
- [ ] Verify measurements match
- [ ] Visual inspection

---

## 📐 GRID SYSTEM IN CSS

### Grid Variables
```css
:root {
  --grid-unit: 8px;
  --page-width: 595px;
  --page-height: 842px;
  
  --margin-left: 42.5px;  /* 15mm */
  --margin-right: 42.5px;
  --margin-top: 34px;     /* 12mm */
  --margin-bottom: 28.3px; /* 10mm */
  
  --content-width: calc(var(--page-width) - var(--margin-left) - var(--margin-right));
  --content-height: calc(var(--page-height) - var(--margin-top) - var(--margin-bottom));
}
```

### Spacing Variables
```css
:root {
  --section-gap: 12px;
  --subsection-gap: 8px;
  --padding-h: 12px;
  --padding-v: 8px;
  
  --header-height: 60px;
  --bill-to-height: 80px;
  --invoice-details-height: 80px;
  --table-row-height: 28px;
  --table-header-height: 32px;
  --totals-height: 40px;
  --footer-height: 40px;
}
```

---

## 🎨 SECTION-BY-SECTION IMPLEMENTATION

### Header Section
**CSS Measurements:**
- Height: 60px
- Padding: 12px horizontal, 8px vertical
- Background: Primary color
- Text color: White

**Key Elements:**
- Company name: 18px bold
- INVOICE label: 11px
- Business info: 9px small

**Code Pattern:**
```css
.invoice-header {
  height: var(--header-height);
  background-color: var(--primary-color);
  padding: var(--padding-v) var(--padding-h);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.invoice-header__company-name {
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.invoice-header__details {
  font-size: 9px;
  color: #e8e8e8;
  text-align: right;
}
```

### Bill To & Invoice Details (Side-by-Side)
**CSS Measurements:**
- Each section: 80px height
- Width: 50% each with 8px gap
- Border, shadow, padding from constants

**Code Pattern:**
```css
.invoice-cards {
  display: flex;
  gap: var(--subsection-gap);
  margin-top: var(--section-gap);
}

.card {
  flex: 1;
  height: var(--bill-to-height);
  border: 1px solid #d8d8d8;
  border-radius: 8px;
  padding: var(--padding-h);
  box-shadow: 2px 2px 15% rgba(0,0,0,0.15);
  border-left: 4px solid var(--secondary-color);
}

.card__title {
  font-size: 11px;
  color: var(--primary-color);
  font-weight: bold;
  margin-bottom: 8px;
}

.card__content {
  font-size: 10px;
  color: #666;
  line-height: 1.4;
}
```

### Items Table
**CSS Measurements:**
- Header height: 32px
- Row height: 28px
- Column layout: 50%, 10%, 15%, 25%

**Code Pattern:**
```css
.items-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: var(--section-gap);
}

.items-table thead {
  background-color: var(--primary-color);
  color: white;
}

.items-table th {
  height: var(--table-header-height);
  padding: 8px var(--padding-h);
  font-size: 11px;
  font-weight: bold;
  text-align: left;
}

.items-table td {
  height: var(--table-row-height);
  padding: 8px var(--padding-h);
  font-size: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.items-table tbody tr:nth-child(odd) {
  background-color: #f9f9f9;
}

.items-table td:last-child {
  text-align: right;
}
```

### Totals Section (Integrated Typography)
**CSS Measurements:**
- Height: 40px total
- Subtotal: 10px
- Tax: 10px
- Divider: 1px
- TOTAL DUE: 11px label + 16px bold amount

**Code Pattern:**
```css
.totals {
  margin-top: var(--section-gap);
}

.totals__line {
  display: flex;
  justify-content: flex-end;
  gap: var(--label-value-gap);
  padding: 6px 0;
  font-size: 10px;
}

.totals__line:nth-child(3) {
  border-top: 1px solid var(--secondary-color);
  padding-top: 8px;
  margin-top: 8px;
}

.totals__label {
  font-size: 11px;
  color: #333;
  font-weight: bold;
}

.totals__amount {
  font-size: 16px;
  font-weight: bold;
  color: var(--primary-color);
  padding-bottom: 4px;
  border-bottom: 2px solid var(--primary-color);
}
```

### Footer Section
**CSS Measurements:**
- Height: 40px
- Background: Primary color
- Text color: White

**Code Pattern:**
```css
.invoice-footer {
  height: var(--footer-height);
  background-color: var(--primary-color);
  color: white;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  margin-top: var(--section-gap);
  border-top: 2px solid rgba(255,255,255,0.3);
}

.invoice-footer__message {
  font-size: 11px;
  font-weight: bold;
  margin-bottom: 4px;
}

.invoice-footer__contact {
  font-size: 9px;
  color: #e8e8e8;
}
```

---

## 📄 HTML TEMPLATE STRUCTURE

```html
<!DOCTYPE html>
<html>
<head>
  <link rel="stylesheet" href="invoice-styles-refined.css">
  <meta charset="UTF-8">
  <title>Invoice</title>
</head>
<body class="invoice-page">

  <!-- HEADER SECTION -->
  <div class="invoice-header">
    <div class="invoice-header__company">
      <div class="invoice-header__company-name">${businessName}</div>
      <div class="invoice-header__details">
        <div>ABN: ${businessAbn}</div>
        <div>${businessPhone}</div>
        <div>${businessEmail}</div>
      </div>
    </div>
    <div class="invoice-header__right">
      <div class="invoice-header__label">INVOICE</div>
      <div class="invoice-header__number">${invoiceNumber}</div>
    </div>
  </div>

  <!-- CARDS SECTION -->
  <div class="invoice-cards">
    <div class="card">
      <div class="card__title">BILL TO</div>
      <div class="card__content">
        <div>${customerName}</div>
        <div>${customerAddress}</div>
        <div>${customerEmail}</div>
        <div>Mob: ${businessPhone}</div>
      </div>
    </div>
    <div class="card">
      <div class="card__title">INVOICE</div>
      <div class="card__content">
        <div><strong>${invoiceNumber}</strong></div>
        <div>Date: ${formatDate(date)}</div>
        <div>Due: ${formatDate(dueDate)}</div>
        <div>Status: ${invoiceStatus}</div>
      </div>
    </div>
  </div>

  <!-- ITEMS TABLE -->
  <table class="items-table">
    <thead>
      <tr>
        <th>Description</th>
        <th>Qty</th>
        <th>Price</th>
        <th>Total</th>
      </tr>
    </thead>
    <tbody>
      ${items.map(item => `
        <tr>
          <td>${item.description}</td>
          <td>${item.quantity}</td>
          <td>$${(item.unitPrice/100).toFixed(2)}</td>
          <td>$${(item.total/100).toFixed(2)}</td>
        </tr>
      `).join('')}
    </tbody>
  </table>

  <!-- TOTALS SECTION -->
  <div class="totals">
    <div class="totals__line">
      <div>Subtotal:</div>
      <div>$${(subtotal/100).toFixed(2)}</div>
    </div>
    ${taxAmount > 0 ? `
      <div class="totals__line">
        <div>Tax (${(taxRate*100).toInt()}%):</div>
        <div>$${(taxAmount/100).toFixed(2)}</div>
      </div>
    ` : ''}
    <div class="totals__line">
      <div class="totals__label">TOTAL DUE</div>
      <div class="totals__amount">$${(totalAmount/100).toFixed(2)}</div>
    </div>
  </div>

  <!-- FOOTER SECTION -->
  <div class="invoice-footer">
    <div class="invoice-footer__message">Thank you for your business.</div>
    <div class="invoice-footer__contact">
      ${businessEmail} | ${businessPhone} | www.${businessEmail.split('@')[1]}
    </div>
  </div>

</body>
</html>
```

---

## 🔧 INTEGRATION STEPS

### 1. Add to HtmlInvoiceStyle enum
```kotlin
enum class HtmlInvoiceStyle {
    MODERN,
    MINIMAL,
    CREATIVE,
    REFINED  // NEW
}
```

### 2. Create template method
```kotlin
private fun generateRefinedTemplate(snapshot: InvoiceSnapshot): String {
    // Use the HTML structure above
    // Replace ${...} with actual values
    // Return complete HTML string
}
```

### 3. Update router
```kotlin
when (settings.selectedHtmlStyle) {
    HtmlInvoiceStyle.MODERN -> generateModernTemplate(...)
    HtmlInvoiceStyle.MINIMAL -> generateMinimalTemplate(...)
    HtmlInvoiceStyle.CREATIVE -> generateCreativeTemplate(...)
    HtmlInvoiceStyle.REFINED -> generateRefinedTemplate(...)  // NEW
}
```

### 4. Test PDF generation
- Generate HTML with new template
- Compare with Canvas version
- Verify measurements match spec

---

## ✅ PHASE 3 SUCCESS CRITERIA

✅ HTML template created with grid system  
✅ CSS uses same spacing constants as Canvas  
✅ All measurements match Canvas version  
✅ HTML PDFs look identical to Canvas PDFs  
✅ Code compiles without errors  
✅ Theme selection works  

---

## 📚 REFERENCE DOCUMENTS

**For measurements:**
→ INVOICE_DESIGN_SPEC_V1.md (Part 3)

**For spacing constants:**
→ InvoiceSpacingConfig.kt

**For HTML service:**
→ HtmlPdfInvoiceService.kt (existing code)

**For CSS patterns:**
→ Professional invoice templates online

---

## 🎯 EXPECTED TIMELINE

- CSS file creation: 45-60 min
- HTML template: 60 min
- Integration: 30-45 min
- Testing: 30-45 min
- **Total: 3-4 hours**

---

**Phase 3 Ready to Begin:** ✅ YES
**All references available:** ✅ YES
**Timeline realistic:** ✅ YES

Begin implementing now!

