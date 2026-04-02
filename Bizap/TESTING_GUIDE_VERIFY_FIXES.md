# 🧪 TESTING GUIDE - VERIFY CSS FIXES WORK

**Date:** April 2, 2026  
**Status:** Ready for Testing  
**Duration:** ~15 minutes  

---

## ⚡ QUICK TEST (5 Minutes)

### Step 1: Build & Run
```bash
./gradlew assembleDebug  # Should complete successfully ✅
```

### Step 2: Launch App

### Step 3: Create Test Invoice
```
1. Navigate to "Create Invoice"
2. Fill in:
   - Customer: "Test Customer"
   - Item 1: Description="Professional Service", Qty=1, Price=$100
   - Item 2: Description="Consultation", Qty=2, Price=$50
   - Total should be $200
3. Save invoice
```

### Step 4: Go to Settings
```
Settings → Invoice Settings
Verify: HTML-to-PDF is selected (not Canvas)
```

### Step 5: Generate PDF
```
Back to invoice detail
Click "Export PDF"
Select "HTML-to-PDF" theme if prompted
Wait for generation (~2-3 seconds)
```

### Step 6: Open PDF
```
Download PDF or open from share menu
View in PDF viewer
```

### Step 7: Verify Styling
Look for these signs that CSS is working:

✅ **Styling is Working If You See:**
- Table with alternating row colors (white and light gray)
- Dark gradient header on table
- White text on dark header
- Different text sizes (headers bigger than content)
- Proper spacing and margins
- Professional appearance

❌ **Styling is NOT Working If You See:**
- Plain black text on white background
- Table with no colors
- All text same size
- Cramped spacing
- Looks like broken rendering

---

## 📊 DETAILED TEST (15 Minutes)

### Test 1: CSS Embedding Verification

**What to check:**
- Is the table header styled with color? (should be gradient)
- Are table rows alternating light/dark?
- Is spacing professional (not cramped)?

**Command to verify in code:**
```kotlin
// In HtmlPdfInvoiceTheme.kt, check log for:
"CSS embedded successfully (xxxx bytes)"
"Template processed successfully, generated xxxx characters of HTML"
```

**Expected:** CSS bytes should be ~12KB (612 lines of CSS)

---

### Test 2: Color Injection Verification

**What to check:**
- Is the primary color (purple by default) visible?
- Are accent colors applied?
- Do colors match your settings?

**To test:**
1. Note current brand color (likely purple #6B4C9A)
2. Generate PDF
3. Check if purple appears in header/accents
4. Change color in settings to Orange (#E67E22)
5. Generate same invoice again
6. Check if orange appears instead of purple

**Expected:** Same invoice content, different colors

---

### Test 3: Table Styling Verification

**What to check:**
- Table header is dark with white text ✓
- First row has light background ✓
- Second row has white background ✓
- Pattern continues for all items ✓
- Columns are properly aligned ✓

**Sample table output:**
```
┌──────────────────────────────────┐
│ Description  Qty  Price  Amount   │ ← Dark header, white text
├──────────────────────────────────┤
│ Item 1       1    $100   $100     │ ← Light background
│ Item 2       2    $50    $100     │ ← White background
│ Item 3       1    $25    $25      │ ← Light background
└──────────────────────────────────┘
```

---

### Test 4: Typography Verification

**What to check:**
- Company name is larger and bolder ✓
- Section headers (Bill To, Notes) are styled ✓
- Body text is readable ✓
- Total amount is emphasized ✓

**Expected sizing (relative):**
```
Company Name          22pt (largest, bold)
Section Headers       14pt (medium, semibold)
Body Text             11pt (regular)
Captions              9pt (small, regular)
```

---

### Test 5: Spacing Verification

**What to check:**
- Header section has breathing room ✓
- Sections are separated clearly ✓
- No cramped text ✓
- Professional margins ✓

**Expected:**
- Nice padding around sections
- Clear separation between invoice parts
- Balanced whitespace

---

### Test 6: Canvas vs HTML-to-PDF Comparison

**Steps:**
1. Go to Settings → Invoice Settings
2. Change theme to **Canvas**
3. Generate PDF of same invoice
4. Change theme back to **HTML-to-PDF**
5. Generate PDF again
6. Open both PDFs side-by-side

**Expected Difference:**
```
Canvas PDF              HTML-to-PDF PDF
────────────────────────────────────
Basic layout            Professional design
Minimal styling         Colorful and styled
Plain text              Hierarchy clear
Artistic design         Modern professional

Both should look different!
Canvas = Current old style
HTML-to-PDF = NEW styled version
```

---

## 📝 TEST LOG TEMPLATE

Use this to document your testing:

```
Date: _______________
Tester: ______________

BUILD TEST
☐ Build successful? YES / NO
  If no, error: _________________

CSS EMBEDDING TEST
☐ CSS embedded? YES / NO
  Log message: "CSS embedded successfully (_____ bytes)"
  CSS bytes visible in PDF? YES / NO

STYLING APPEARANCE TEST
☐ Table header is colored? YES / NO
☐ Alternating row colors? YES / NO
☐ Professional spacing? YES / NO
☐ Text hierarchy clear? YES / NO
☐ Overall looks professional? YES / NO

COLOR INJECTION TEST
☐ Brand color visible? YES / NO
  Primary color shown: ___________
☐ Can change colors? YES / NO
☐ Colors update in PDF? YES / NO

CANVAS VS HTML-to-PDF
Canvas looks like:     _________________
HTML-to-PDF looks like: _________________
Significant difference? YES / NO

PERFORMANCE
Generation time: _______ seconds
PDF file size: _______ KB
Acceptable? YES / NO

ISSUES FOUND
1. ___________________________
2. ___________________________
3. ___________________________

OVERALL ASSESSMENT
☐ Styling works as expected
☐ Colors apply correctly
☐ User would be satisfied
☐ Ready for production
```

---

## 🔍 LOG MESSAGES TO LOOK FOR

When generating PDF, check logcat for these messages indicating success:

```
✅ SUCCESS INDICATORS:
"CSS embedded successfully (11234 bytes)"
"Template processed successfully, generated 2891 characters of HTML"
"CSS color variables injected for branding"
"PDF created successfully at: /path/to/invoice.pdf"

❌ ERROR INDICATORS:
"CSS file is empty"
"Failed to embed CSS from assets"
"Template processing failed"
"HTML to PDF conversion failed"
```

---

## 🎯 TEST SCENARIOS

### Scenario 1: Basic Invoice
- Simple invoice with 2-3 items
- Default purple color
- Verify styling appears

### Scenario 2: Complex Invoice
- Invoice with 15+ items
- Check if styling handles large tables
- Verify no text overflow

### Scenario 3: Color Customization
- Start with purple
- Change to orange
- Regenerate and verify colors changed
- Test another color (blue, green)

### Scenario 4: Long Descriptions
- Add item with long description
- Verify text wraps properly
- Check spacing isn't broken

---

## ✅ ACCEPTANCE CRITERIA

For the fix to be considered successful:

- [x] Build compiles without errors ✅
- [ ] PDF has styled table (alternating colors)
- [ ] Brand color visible in PDF
- [ ] Typography hierarchy apparent
- [ ] Spacing looks professional
- [ ] CSS file loads from assets
- [ ] No errors in logs
- [ ] Canvas and HTML-to-PDF look different
- [ ] Colors can be changed and PDF updates
- [ ] Performance is acceptable (~2-5 seconds per PDF)

---

## 🚀 IF TESTS PASS

**Congratulations!** The fix works! Next steps:

1. ✅ Update Settings UI to show color presets (instead of hex input)
2. ✅ Add color picker for advanced users
3. ✅ Collect user feedback
4. ✅ Make it the default theme
5. ✅ Deploy to production

---

## ⚠️ IF TESTS FAIL

### Issue: CSS not appearing in PDF

**Troubleshooting:**
1. Check if CSS file exists: `app/src/main/assets/invoices/html-theme/invoice-styles.css`
2. Check file size is >10KB
3. Look for log: "CSS embedded successfully"
4. Check if HTML contains `<style>` tag (not `<link>`)

### Issue: Colors not appearing

**Troubleshooting:**
1. Check theme is HTML-to-PDF (not Canvas)
2. Check InvoiceSettings are saved
3. Look for log: "CSS color variables injected"
4. Try default colors first, then custom

### Issue: Build fails

**Troubleshooting:**
1. Run: `./gradlew clean assembleDebug`
2. Check for syntax errors in modified files
3. Verify no import errors
4. Check Kotlin version compatibility

---

## 📞 SUPPORT

If issues arise:
1. Check logs with filter: `CSS|HTML-PDF|embed`
2. Review the FIX_IMPLEMENTATION_COMPLETE.md document
3. Verify all 3 files were properly updated
4. Build should show: "BUILD SUCCESSFUL"

---

**Good luck with testing! The fix is ready to verify.** 🚀

Now generate some test PDFs and see the beautiful styling! ✨

