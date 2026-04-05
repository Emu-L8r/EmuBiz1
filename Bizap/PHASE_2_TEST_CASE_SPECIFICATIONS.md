# 📋 PHASE 2 TEST CASES - DETAILED SPECIFICATIONS

**Document:** Test Case Reference for Live Execution  
**Date:** April 5, 2026  
**Status:** Ready to Execute  

---

## TEST CASE 1: MINIMAL INVOICE (3 ITEMS)

### Test ID
**TEST-001**

### Purpose
Validate basic PDF generation and layout with minimal item count

### Preconditions
- [ ] App deployed and running
- [ ] User authenticated (PIN 1234)
- [ ] Test Customer created and available
- [ ] Create Invoice screen accessible

### Test Data

```
INVOICE DETAILS:
  Invoice #:      TEST-001
  Date:           [Today's date]
  Due Date:       [30 days from today]
  Customer:       Test Customer
  Currency:       AUD (or default)
  Tax Rate:       10%

ITEMS:
  1. Service A              Qty: 1      Unit Price: $100.00     Total: $100.00
  2. Service B              Qty: 1      Unit Price: $200.00     Total: $200.00
  3. Service C              Qty: 1      Unit Price: $150.00     Total: $150.00

CALCULATED TOTALS:
  Subtotal:       $450.00
  Tax (10%):      $45.00
  Total Due:      $495.00
```

### Execution Steps

**Step 1: Navigate to Create Invoice**
1. From Dashboard, tap "Create Invoice" button
2. Verify "Create Invoice" screen loads
3. Confirm Test Customer is selected in dropdown

**Step 2: Fill Invoice Header**
1. Invoice #: Enter "TEST-001"
2. Date: Auto-filled (today)
3. Due Date: Auto-filled (30 days from today)
4. Customer: Select "Test Customer"
5. Currency: Leave as default (AUD)

**Step 3: Add Items**
```
ADD ITEM 1:
  Description: Service A
  Quantity:    1
  Unit Price:  100.00
  [Tap "Add Item"]

ADD ITEM 2:
  Description: Service B
  Quantity:    1
  Unit Price:  200.00
  [Tap "Add Item"]

ADD ITEM 3:
  Description: Service C
  Quantity:    1
  Unit Price:  150.00
  [Tap "Add Item"]
```

**Step 4: Verify Calculations**
- Subtotal should display: $450.00
- Tax (10%) should display: $45.00
- Total Due should display: $495.00

**Step 5: Generate PDF**
1. Tap "Generate PDF" or "Create PDF" button
2. Wait for PDF generation (expect <5 seconds)
3. PDF should save automatically
4. May see "PDF saved" notification

### Expected Results

**Timing:**
- Generation time: <5 seconds ✓

**File Generation:**
- PDF file created successfully ✓
- File size: 50-100 KB (typical) ✓
- File location: App storage or Downloads folder ✓

**PDF Content - Visual Inspection:**
- Single page (1 of 1) ✓
- Header visible at top ✓
- "INVOICE" label visible ✓
- Company name/branding present ✓
- Bill To section visible and readable ✓
- Invoice Details visible (number, date, due date) ✓
- Items table with 3 rows ✓
- Column headers visible (Description, Qty, Price, Total) ✓
- All 3 items listed with correct values ✓
- Totals section visible and readable ✓
- Subtotal: $450.00 ✓
- Tax: $45.00 ✓
- Total Due: $495.00 ✓
- Footer visible at bottom ✓

**PDF Layout - Spacing Validation (±2px tolerance):**
- Header height: ~60px ✓
- Bill To height: ~80px ✓
- Invoice Details height: ~80px ✓
- Table header height: ~32px ✓
- Item row height: ~28px each (3 rows = 84px) ✓
- Totals height: ~40px ✓
- Footer height: ~40px ✓

**Page Coverage:**
- Expected coverage: ~50% of page ✓
- Reason: 3 items is minimal, leaves whitespace ✓

**Professional Appearance:**
- Text alignment: Clean ✓
- No overlapping elements ✓
- No text cutoff ✓
- Professional color scheme ✓
- Grid-based layout evident ✓

### Success Criteria
✅ **TEST-001 PASSES** when:
- PDF generates without errors
- Single page
- All content visible and readable
- Spacing matches spec (±2px)
- Professional appearance
- No layout issues

### Success Metrics
```
TEST-001: EXPECTED PASS ✅
Confidence: 98% (basic functionality, minimal complexity)
```

---

## TEST CASE 2: MEDIUM INVOICE (10 ITEMS)

### Test ID
**TEST-002**

### Purpose
Validate standard business invoice layout with moderate item count

### Test Data

```
INVOICE DETAILS:
  Invoice #:      TEST-002
  Date:           [Today's date]
  Customer:       Test Customer
  Tax Rate:       15%

ITEMS (10 total):
  1. Web Design           Qty: 1      Unit Price: $2,000.00
  2. Hosting Setup        Qty: 1      Unit Price: $500.00
  3. Domain Registration  Qty: 1      Unit Price: $100.00
  4. SSL Certificate      Qty: 1      Unit Price: $200.00
  5. Email Setup          Qty: 1      Unit Price: $150.00
  6. Backup Configuration Qty: 1      Unit Price: $300.00
  7. Security Audit       Qty: 1      Unit Price: $400.00
  8. Performance Review   Qty: 1      Unit Price: $350.00
  9. Documentation        Qty: 1      Unit Price: $250.00
  10. Support (10 hours)  Qty: 1      Unit Price: $1,000.00

CALCULATED TOTALS:
  Subtotal:       $5,250.00
  Tax (15%):      $787.50
  Total Due:      $6,037.50
```

### Execution Steps
1. Create new invoice (same process as TEST-001)
2. Fill header with TEST-002 data
3. Add all 10 items as listed
4. Verify calculations
5. Generate PDF

### Expected Results

**Layout:**
- Single page (1 of 1) ✓
- All 10 items visible ✓
- Professional appearance ✓
- No content cutoff ✓

**Page Coverage:**
- Expected coverage: ~73% of page ✓
- Demonstrates improved space utilization ✓

**Spacing:**
- Header: 60px ✓
- Each row: 28px × 10 items = 280px ✓
- Totals: 40px ✓
- All spacing matches spec ✓

### Success Criteria
✅ **TEST-002 PASSES** when:
- All 10 items fit on single page
- Page coverage ~73%
- All content visible
- Spacing correct
- Professional appearance

---

## TEST CASE 3: LARGE INVOICE (25 ITEMS + PAGINATION)

### Test ID
**TEST-003**

### Purpose
Validate pagination logic and multi-page layout with large item count

### Test Data

```
INVOICE DETAILS:
  Invoice #:      TEST-003
  Date:           [Today's date]
  Customer:       Test Customer
  Items:          25 (various services)
  Tax Rate:       10%

ITEMS (25 total - abbreviated):
  Items 1-5:      Development Tasks      $5,000.00
  Items 6-10:     Testing & QA          $3,000.00
  Items 11-15:    Deployment Setup      $2,500.00
  Items 16-20:    Documentation         $2,000.00
  Items 21-25:    Support & Training    $2,500.00

SUBTOTAL:         $15,000.00
TAX (10%):        $1,500.00
TOTAL DUE:        $16,500.00
```

### Execution Steps
1. Create new invoice (TEST-003)
2. Add 25 items (can use abbreviated form as shown)
3. Verify totals calculate correctly
4. Generate PDF
5. Examine both pages

### Expected Results

**Page 1:**
- Header, Bill To, Invoice Details ✓
- Items 1-15 (approximately) ✓
- Coverage: ~71% ✓

**Page 2:**
- Items 16-25 ✓
- Totals section ✓
- Footer ✓
- Coverage: ~44% (with footer) ✓

**Page Breaks:**
- Logical break point (between items) ✓
- No orphaned content ✓
- Both pages balanced ✓

**Pagination:**
- Shows "Page 1 of 2" or similar ✓
- All items accounted for ✓
- Totals on final page ✓

### Success Criteria
✅ **TEST-003 PASSES** when:
- PDF spans 2 pages
- Page breaks at logical point
- All content on appropriate pages
- Coverage: ~71% page 1, ~44% page 2
- No content cut off
- Professional appearance on both pages

---

## TEST CASE 4: PAYMENT DETAILS SECTION

### Test ID
**TEST-004**

### Purpose
Validate optional payment details section rendering

### Test Data

```
INVOICE DETAILS:
  Invoice #:      TEST-004
  Date:           [Today's date]
  Customer:       Test Customer
  Items:          8 (standard items)
  Tax Rate:       10%

PAYMENT DETAILS:
  Bank Name:              First National Bank
  Account Name:           Test Customer Business
  BSB:                    123456
  Account Number:         987654321
  Payment Terms:          Due within 30 days

ITEMS:
  (Use 8 standard items, values don't matter for this test)

SUBTOTAL:         $2,000.00
TAX (10%):        $200.00
TOTAL DUE:        $2,200.00
```

### Execution Steps
1. Create new invoice (TEST-004)
2. Add 8 items
3. **IMPORTANT:** Before generating PDF, fill in payment details:
   - Navigate to "Payment Details" or similar section
   - Enter Bank Name, Account Name, BSB, Account Number
   - Verify these fields save
4. Generate PDF

### Expected Results

**Layout:**
- Single page (1 of 1) ✓
- All content visible ✓

**Payment Details Section:**
- Bank details section visible ✓
- "Bank Name:" label + value visible ✓
- "Account Name:" label + value visible ✓
- "BSB:" label + value visible ✓
- "Account Number:" label + value visible ✓
- Section properly separated from totals ✓

**Formatting:**
- Section has distinct background/styling ✓
- Text readable ✓
- Proper alignment ✓
- Professional appearance ✓

**Page Coverage:**
- Fits on single page with payment details ✓
- Coverage: ~70-80% ✓

### Success Criteria
✅ **TEST-004 PASSES** when:
- Payment details section visible
- All bank fields populated and displayed
- Professional formatting
- Proper spacing from totals
- Single page
- No content cut off

---

## MEASUREMENT SPECIFICATIONS

### Tools Needed
- PDF viewer with zoom capability
- Physical ruler (optional, for printed measurement)
- or Digital measurement tool (Adobe Acrobat, online ruler)

### Measurement Procedure
1. Open PDF at 100% zoom (actual size)
2. Measure each component
3. Compare to specification
4. Tolerance: ±2px

### Components to Measure

```
HEADER:
  Expected: 60px (from top margin to Bill To start)
  Measure: Top of page to start of customer details

BILL TO SECTION:
  Expected: 80px height
  Measure: Top of "Bill To" to bottom of customer info

INVOICE DETAILS:
  Expected: 80px height (side-by-side with Bill To)
  Measure: Top of "Invoice Details" to bottom of invoice info

TABLE HEADER:
  Expected: 32px
  Measure: One row height (Description, Qty, Price, Total)

TABLE ROWS:
  Expected: 28px each
  Measure: One item row (should be consistent for all)

TOTALS SECTION:
  Expected: 40px
  Measure: From "Subtotal" to "Total Due" line

FOOTER:
  Expected: 40px
  Measure: Bottom section with company info
```

### Recording Results
For each test case, record:
- [ ] Measured value (pixels or mm)
- [ ] Expected value
- [ ] Tolerance pass/fail
- [ ] Notes

---

## COMPLETION CHECKLIST

### Test Execution
- [ ] TEST-001 (3 items): PASS/FAIL
- [ ] TEST-002 (10 items): PASS/FAIL
- [ ] TEST-003 (25 items): PASS/FAIL
- [ ] TEST-004 (Payment): PASS/FAIL

### Measurements
- [ ] Header: ±2px
- [ ] Bill To: ±2px
- [ ] Invoice Details: ±2px
- [ ] Table Header: ±2px
- [ ] Table Rows: ±2px
- [ ] Totals: ±2px
- [ ] Footer: ±2px

### Documentation
- [ ] All PDFs saved for reference
- [ ] Measurements recorded
- [ ] Issues/observations noted
- [ ] Test report created

### Phase 2 Sign-Off
- [ ] All 4 test cases PASS
- [ ] Measurements match spec
- [ ] Visual appearance professional
- [ ] Phase 2 COMPLETE

---

**Ready to execute TEST CASE 1? Let's go! 🚀**


