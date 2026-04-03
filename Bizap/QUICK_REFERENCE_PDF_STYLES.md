# ⚡ PDF STYLES QUICK REFERENCE CARD

**Print this or keep it handy while testing!**

---

## 🚀 3-STEP QUICK START

```
1. Install APK:
   adb install -r app\build\outputs\apk\debug\app-debug.apk

2. Run test (5 min):
   → Settings → Invoice Settings
   → Select HTML theme
   → Try different styles
   → Generate PDFs

3. Report results:
   → Which tests pass?
   → What do logs show?
   → Do PDFs look different?
```

---

## 📊 EXPECTED BEHAVIOR

| Action | Expected Result | ✓ or ✗ |
|--------|-----------------|--------|
| See all 4 styles in Settings | 4 cards visible | ☐ |
| Select "Minimalist" | Shows highlighted | ☐ |
| Tap "Save Settings" | Success message | ☐ |
| Reopen Settings | "Minimalist" still selected | ☐ |
| Generate PDF | Creates PDF file | ☐ |
| Modern PDF | Purple header | ☐ |
| Minimal PDF | Black/white header | ☐ |
| Corporate PDF | Navy header, serif font | ☐ |
| Creative PDF | Orange header | ☐ |

---

## 🎨 PDF COLORS AT A GLANCE

| Style | Header Color | Font | File |
|-------|-------------|------|------|
| **Modern** | Purple (#6B4C9A) | Segoe UI | invoice-styles.css |
| **Minimal** | Black (#1a1a1a) | Arial | invoice-styles-minimal.css |
| **Corporate** | Navy (#003366) | Georgia serif | invoice-styles-corporate.css |
| **Creative** | Orange (#FF6B35) | Segoe UI + Teal | invoice-styles-creative.css |

---

## 📋 CHECKLIST - What Should Work

- [ ] Open Settings → Invoice Settings
- [ ] See "Modern HTML Style" theme option
- [ ] Select it → 4 style cards appear
- [ ] Click "Minimalist (Clean)" → radio button moves
- [ ] Card highlights → border appears
- [ ] Click "Save Settings" button
- [ ] Get success message
- [ ] Close and reopen Settings
- [ ] "Minimalist" still selected with checkmark
- [ ] Go to Invoices tab
- [ ] Select an invoice
- [ ] Tap "Generate PDF" 
- [ ] Select "Modern HTML Style" theme
- [ ] Tap "Generate"
- [ ] Watch Logcat for logs:
  ```
  selectedHtmlStyle: Minimalist (Clean)
  CSS File: invoice-styles-minimal.css
  CSS loaded successfully: [number] characters
  ```
- [ ] View generated PDF
- [ ] Header is BLACK/WHITE (not purple)
- [ ] Font is clean/minimal

---

## 🔍 LOG WATCH ZONES

### When you SELECT a style:
```
Look for: 🎨 USER SELECTED STYLE: Minimalist (Clean)
```

### When you SAVE settings:
```
Look for: SAVE_SETTINGS_CALLED
          repository.saveSettings()
          Settings saved successfully
```

### When you GENERATE PDF:
```
Look for: 📄 InvoicePdfService.generatePdf() called
          🔍 SETTINGS LOADED FROM REPOSITORY
          - selectedHtmlStyle: Minimalist (Clean)
          - CSS file: invoice-styles-minimal.css
```

### When CSS LOADS:
```
Look for: 🎨 LOADING CSS FOR STYLE
          🎨 Selected Style: Minimalist (Clean)
          ✅ CSS loaded successfully: [number] characters
```

---

## 🚨 TROUBLESHOOTING QUICK FIXES

### Issue: Only see 1-2 styles
**Quick Fix**: Scroll down in settings (might be off-screen)
**Real Problem**: UI not rendering all styles
**Check File**: InvoiceSettingsScreen.kt line 465

### Issue: Selection reverts to Modern
**Quick Fix**: Try again, save properly
**Real Problem**: Database not saving selectedHtmlStyle
**Check DB**:
```
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db \
  "SELECT selected_html_style FROM invoice_settings;"
```

### Issue: Logs show wrong style
**Real Problem**: Settings loaded from wrong user or old cache
**Quick Fix**: 
```
adb shell pm clear com.emul8r.bizap
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Issue: PDF looks same regardless
**Real Problem**: CSS not being applied by PDF renderer
**Check**: Is header color actually different? (modern=purple, minimal=black)

---

## 📱 COMMANDS YOU NEED

### Install APK
```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

### Watch Logs (All PDF activity)
```powershell
adb logcat | findstr "🎨\|📄\|✅\|❌"
```

### Watch Logs (Style only)
```powershell
adb logcat | findstr "selectedHtmlStyle"
```

### Check Database
```powershell
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db ^
  "SELECT id, user_id, selected_theme, selected_html_style FROM invoice_settings;"
```

### Clear and Retry
```powershell
adb shell pm clear com.emul8r.bizap
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## 📊 DIAGNOSTIC TABLE

Fill this out while testing:

| Test | Expected | Actual | ✓/✗ | Notes |
|------|----------|--------|-----|-------|
| See 4 styles | 4 cards | ___ | ☐ | |
| Select Minimal | Highlighted | ___ | ☐ | |
| Save works | Success msg | ___ | ☐ | |
| Persists | Still Minimal | ___ | ☐ | |
| DB has value | MINIMAL | ___ | ☐ | |
| Logs right file | minimal.css | ___ | ☐ | |
| PDF black | Black header | ___ | ☐ | |

---

## 💡 THREE POSSIBLE CAUSES

### Cause #1: Settings Not Persisting
- [ ] Select style ✓
- [ ] Save ✓
- [ ] Reopen → See Modern instead ✗
- **Fix Location**: InvoiceSettingsViewModel.kt line 162

### Cause #2: Settings Not Loaded for PDF
- [ ] Everything above works ✓
- [ ] But PDF logs show Modern ✗
- **Fix Location**: InvoicePdfService.kt line 75

### Cause #3: CSS Not Applied to PDF
- [ ] Logs show correct CSS ✓
- [ ] But PDF looks same ✗
- **Fix Location**: CSS properties or PDF renderer

---

## ✅ SUCCESS = ALL GREEN

```
☑ Can see 4 styles
☑ Can select each
☑ Selection persists
☑ DB shows correct value
☑ PDF logs show correct style
☑ PDF logs show correct CSS
☑ PDF has correct header color
☑ PDF has correct font
```

---

## 🎯 NEXT STEPS

1. **NOW**: Read SESSION_SUMMARY_PDF_STYLES.md
2. **THEN**: Install APK and test
3. **REPORT**: Tell me which tests pass/fail and what logs show
4. **I'LL**: Provide targeted fix if needed
5. **YOU'LL**: Apply fix and retest

---

## 📞 WHEN YOU NEED HELP

Provide:
1. Which test fails first?
2. Copy of relevant logs
3. Database query result
4. What PDFs look like

Then I'll know exactly what to fix! 🎯

---

**Keep this card handy! Reference it while testing! 🚀**


