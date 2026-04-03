# 🔍 PDF STYLES - DEBUGGING & VERIFICATION GUIDE

## **ISSUE SUMMARY**
User reports:
- ❌ Can only select "Modern" style (not seeing Minimal, Corporate, Creative)
- ❌ PDFs look the same regardless of style selection
- ❌ Tried switching between HTML and original method - same result

## **ROOT CAUSE ANALYSIS**

### Possibility #1: UI Only Shows Modern
If the settings screen only displays 1-2 styles instead of 4, the problem is in the UI layer.

**Check**: Does `HtmlInvoiceStyle.values().forEach` iterate through all 4?
- File: `InvoiceSettingsScreen.kt` line ~465
- Expected: Loop should execute 4 times, creating 4 card UI elements

### Possibility #2: Styles Are Visually Identical
If all 4 styles render the same, the CSS differences might not be pronounced enough.

**Check**: Do the CSS files have significantly different visual properties?
- Header colors/styles
- Font families
- Overall layout/spacing

### Possibility #3: CSS Not Embedding Correctly
The CSS might not be getting loaded/embedded into the HTML properly.

**Check**: Is the CSS being loaded from the correct file path?
- File: `HtmlPdfInvoiceService.kt` line ~107
- Should load from: `invoices/html-theme/{styleFile}`

### Possibility #4: Settings Not Persisting
Style might be selected but not saved to database, so it reverts to default.

**Check**: Is the selected style being saved?
- Look for: `updateSelectedHtmlStyle()` callback

---

## 🧪 STEP-BY-STEP DEBUG TEST

### Test 1: Verify UI Shows All 4 Styles
1. Open Settings → Invoice Settings
2. Select "Modern (HTML)" as theme
3. Scroll down to "HTML Invoice Styles" section
4. **Expected**: See 4 cards:
   - ✓ Modern (Premium) - Purple color preview
   - ✓ Minimalist (Clean) - Black color preview
   - ✓ Corporate (Formal) - Navy color preview
   - ✓ Creative (Startup) - Orange color preview

**If only 1-2 show**: The UI has a bug. Styles aren't all being rendered.

### Test 2: Verify Style Selection Works
1. Click on "Minimalist (Clean)" card
2. Tap "Save Settings"
3. Reopen Settings
4. Scroll to HTML Invoice Styles
5. **Expected**: "Minimalist (Clean)" should be selected (checked)

**If it reverts to Modern**: Settings aren't persisting.

### Test 3: Generate PDF with Each Style
1. Go to Invoices tab
2. Select an invoice
3. Tap "Generate PDF"
4. Select "Modern (HTML)" theme
5. Generate and view PDF
6. **Expected**: Purple gradient header

Then repeat:
7. Change to "Minimalist" style in Settings
8. Generate same PDF again
9. **Expected**: Black & white clean design

### Test 4: Inspect Logs
Filter Timber logs for "CSS" and "HtmlPdfInvoiceService":

```
HtmlPdfInvoiceService: Loading CSS for style: Modern (Premium) (file: invoice-styles.css)
HtmlPdfInvoiceService: Loading CSS for style: Minimalist (Clean) (file: invoice-styles-minimal.css)
HtmlPdfInvoiceService: CSS loaded: 18234 characters
```

**If you don't see the right CSS file loading**: The selection isn't reaching the service.

---

## 🎨 VISUAL DIFFERENCES - What to Expect

### **Modern (Premium)** - invoice-styles.css
- **Header**: Purple gradient (#6B4C9A to #5a3b88) with shadow
- **Font**: Segoe UI (modern sans-serif)
- **Feel**: Professional, contemporary, tech-forward
- **Border Radius**: Rounded corners on header

### **Minimalist (Clean)** - invoice-styles-minimal.css
- **Header**: Black border line, white background
- **Font**: Arial/Helvetica (clean sans-serif)
- **Feel**: Elegant, minimal, professional
- **Border Radius**: Minimal, sharp corners
- **Color**: Mostly black (#1a1a1a) and white (#ffffff)

### **Corporate (Formal)** - invoice-styles-corporate.css
- **Header**: Navy blue (#003366) background
- **Font**: Georgia/Times New Roman (serif fonts)
- **Feel**: Formal, trustworthy, enterprise
- **Color**: Blues and professional grays

### **Creative (Startup)** - invoice-styles-creative.css
- **Header**: Orange (#FF6B35) with vibrant colors
- **Font**: Segoe UI (modern)
- **Feel**: Energetic, vibrant, startup vibe
- **Accent Colors**: Teal (#004E89) accents throughout

---

## 📋 FILES TO CHECK

### If styles aren't showing:
```
✓ InvoiceSettingsScreen.kt (line 465)
  - forEach loop should iterate 4 times
  
✓ InvoiceSettings.kt (lines 170-177)
  - Enum should have 4 values: MODERN, MINIMAL, CORPORATE, CREATIVE
  
✓ InvoiceSettingsViewModel.kt
  - updateSelectedHtmlStyle() should be called
```

### If PDFs look the same:
```
✓ CSS files exist:
  - app/src/main/assets/invoices/html-theme/invoice-styles.css
  - app/src/main/assets/invoices/html-theme/invoice-styles-minimal.css
  - app/src/main/assets/invoices/html-theme/invoice-styles-corporate.css
  - app/src/main/assets/invoices/html-theme/invoice-styles-creative.css
  
✓ HtmlPdfInvoiceService.kt (line 107-122)
  - loadSelectedStyleCss() should load the right file
```

---

## 🐛 COMMON ISSUES & FIXES

### Issue: Only "Modern" shows in UI
**Cause**: `HtmlInvoiceStyle.values()` not iterating properly
**Fix**: Check if enum is properly defined with 4 values

### Issue: Style selection doesn't save
**Cause**: ViewModel callback not wired up correctly
**Fix**: Verify `onStyleSelected` → `viewModel.updateSelectedHtmlStyle(it)` chain

### Issue: PDFs still look the same after selecting different style
**Cause 1**: CSS not being embedded
**Fix 1**: Check `embedCssIntoHtml()` is finding and replacing style tags

**Cause 2**: Wrong CSS file being loaded
**Fix 2**: Add logging to see which file is being loaded

**Cause 3**: PDF converter (iText7) not processing CSS properly
**Fix 3**: Verify CSS syntax and structure

### Issue: Build fails
**Solution**: `./gradlew clean && ./gradlew assembleDebug`

---

## ✅ VERIFICATION CHECKLIST

- [ ] Can see all 4 style names in UI (Modern, Minimalist, Corporate, Creative)
- [ ] Can select each style with radio buttons
- [ ] Selected style shows checkmark/highlight
- [ ] Selected style persists after closing and reopening Settings
- [ ] Logs show correct CSS file being loaded
- [ ] Generated PDFs have different headers for each style
- [ ] Generated PDFs have different fonts for each style
- [ ] Generated PDFs have different color schemes

---

## 📊 EXPECTED TEST RESULTS

| Test | Expected Result | Status |
|------|-----------------|--------|
| UI shows 4 styles | 4 cards visible | ☐ Pass ☐ Fail |
| Can select each | Radio button works | ☐ Pass ☐ Fail |
| Selection persists | Stays after reopen | ☐ Pass ☐ Fail |
| CSS loaded correctly | Timber logs show file | ☐ Pass ☐ Fail |
| Modern PDF | Purple header | ☐ Pass ☐ Fail |
| Minimal PDF | Black & white | ☐ Pass ☐ Fail |
| Corporate PDF | Navy header, serif | ☐ Pass ☐ Fail |
| Creative PDF | Orange header, vibrant | ☐ Pass ☐ Fail |

---

## 🚀 NEXT STEPS

1. **Install APK**: `./gradlew assembleDebug`
2. **Run Test 1**: Check if all 4 styles visible
3. **Check Logs**: Open Logcat, filter for "HtmlPdfInvoice" or "CSS"
4. **Generate PDFs**: Try each style and screenshot
5. **Report Findings**: Share which tests pass/fail

---

## 📞 TROUBLESHOOTING

### Can't see logs?
```
adb logcat | grep -i "HtmlPdfInvoice\|CSS\|selectedHtmlStyle"
```

### Need to force refresh?
Settings → Apps → Bizap → Force Stop → Open again

### Want to check database?
```
adb shell
cd /data/data/com.emul8r.bizap/databases
sqlite3 bizap.db
SELECT * FROM invoice_settings;
```

---

**Created**: April 2, 2026  
**Purpose**: Debug PDF style selection and rendering issues

