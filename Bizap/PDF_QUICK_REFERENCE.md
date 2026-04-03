# 🎯 PDF Issues - Quick Reference Card

**Print This or Bookmark It!**

---

## 🔍 Logcat Cheat Sheet

### Filter to Use
```
HtmlPdfInvoiceService|InvoiceSettingsViewModel
```

### Search For These Logs

#### **Style Selection Lock - Issue #1**
```
🎨 updateSelectedHtmlStyle() CALLED
💾 SAVE_SETTINGS_CALLED
✓ selectedHtmlStyle: [YOUR SELECTED STYLE]
✓ selectedHtmlStyle now in database: [YOUR SELECTED STYLE]
✅ SAVE_SETTINGS COMPLETE
```

**If you see these**: ✅ Style selection IS working  
**If selectedHtmlStyle shows MODERN when you selected MINIMAL**: ❌ Database issue

---

#### **Blank PDF - Issue #2**
```
📝 GENERATING HTML CONTENT FROM INVOICE DATA
Total Items: [NUMBER]
✓ Item: [Description]
🎨 STEP 3: EMBEDDING CSS INTO HTML
🔄 STEP 4: HTML-TO-PDF CONVERSION
✅ Page count: [NUMBER]
📦 File size: [KB] KB
```

**If you see these**: ✅ PDF generation IS working  
**If Total Items: 0**: ❌ Invoice has no items  
**If File size: 0 bytes**: ❌ PDF conversion failed  
**If File size < 5 KB**: ⚠️ PDF might be mostly blank

---

## 📱 Testing Sequence

### Test 1: Style Selection (3 minutes)
1. Settings → Invoice Settings
2. Select "Modern HTML Style" theme
3. Click "Minimalist (Clean)"
4. **Watch Logcat** for `🎨 updateSelectedHtmlStyle()`
5. Click "Save Settings"
6. **Watch Logcat** for `💾 SAVE_SETTINGS_CALLED`
7. Check Logcat shows: `✓ selectedHtmlStyle: Minimalist (Clean)`
8. Close Settings and reopen
9. **Check**: Is Minimalist still selected?

### Test 2: PDF Generation (5 minutes)
1. Create invoice with 3+ line items
2. Ensure each item has quantity and amount
3. Go to invoice detail
4. Ensure "Modern HTML Style" selected in Settings
5. Generate PDF
6. **Watch Logcat** for `📝 GENERATING HTML CONTENT`
7. Check: `Total Items: 3` (or your number)
8. **Watch Logcat** for `🔄 STEP 4`
9. Check: `File size: XXXX bytes`
10. Open PDF in vault
11. **Check**: Does it show content or is it blank?

---

## 🐛 Diagnosis Quick Guide

### **Problem: Can't Change Styles**

**Check Logs For**:
```
🎨 updateSelectedHtmlStyle() CALLED
   Selected Style: [Style you clicked]
```

**If NOT FOUND**:
- ❌ Click handler not firing
- Fix: Check onClick callback in UI

**If FOUND but wrong style in saveSettings()**:
- ❌ Style reverts before save
- Fix: Check UI state management

**If savedStyle shows MODERN**:
- ❌ Database not saving selected style
- Fix: Check InvoiceSettingsRepository.saveSettings()

---

### **Problem: Blank PDFs**

**Check Logs For**:
```
📝 GENERATING HTML CONTENT
Total Items: [NUMBER]
```

**If Total Items: 0**:
- ❌ Invoice has no line items
- Fix: Create invoice WITH items

**If Total Items > 0 but amounts are 0**:
- ⚠️ Items exist but no prices
- Fix: Add prices to items

**Check Logs For**:
```
🔄 STEP 4
File size: [SIZE] bytes
```

**If File size: 0 bytes**:
- ❌ PDF conversion failed
- Fix: Check for exceptions before this log

**If File size: 5000-10000 bytes**:
- ⚠️ Very small - might be empty
- Fix: Check if HTML has items

**If File size: 50000+ bytes**:
- ✅ PDF probably has content
- Fix: Test by opening in vault

---

## 🛠️ Quick Fixes Reference

| Issue | Symptom | Log Check | Likely Fix |
|-------|---------|-----------|-----------|
| Style Lock | Can't select styles | Look for `updateSelectedHtmlStyle()` | Repository save method |
| Style Reverts | Saves but reverts | `selectedHtmlStyle` shows MODERN | Database migration issue |
| No Items in PDF | PDF blank/empty | `Total Items: 0` | Create invoice with items |
| Invalid Amounts | PDF shows $0 | Amounts are 0 in logs | Add prices to line items |
| PDF Too Small | File < 5 KB | Check `File size:` | Conversion failed |
| CSS Not Applied | PDF unstyled | Check `EMBEDDING CSS` logs | CSS file missing/broken |

---

## 📊 Sample Good Logs

### Style Selection Success
```
🎨 updateSelectedHtmlStyle() CALLED
   Selected Style: Minimalist (Clean) (ENUM: MINIMAL)
   CSS File: invoice-styles-minimal.css

✅ UI State Updated:
   Old Style: Modern (Premium)
   New Style: Minimalist (Clean)

💾 SAVE_SETTINGS_CALLED
   ✓ selectedHtmlStyle: Minimalist (Clean)
   ✓ selectedHtmlStyle now in database: Minimalist (Clean)

✅ SAVE_SETTINGS COMPLETE
```

✅ **This means**: Style selection is working perfectly

---

### PDF Generation Success
```
📝 GENERATING HTML CONTENT FROM INVOICE DATA
✅ FINANCIAL DATA:
   Subtotal: $300.00 (30000 cents)
   Tax: $30.00 (3000 cents)
   Total: $330.00 (33000 cents)

✅ LINE ITEMS DATA:
   Total Items: 3
   ✓ Item: Widget
     - Qty: 1.00
     - Unit Price: $100.00
     - Total: $100.00

🔄 STEP 4: HTML-TO-PDF CONVERSION
   ✅ Page count: 1
   ✅ File size: 87456 bytes (87.5 KB)

✅ PDF file size looks reasonable
```

✅ **This means**: PDF generation is working perfectly

---

## ❌ Sample Bad Logs

### Style Not Persisting
```
🎨 updateSelectedHtmlStyle() CALLED
   Selected Style: Minimalist (Clean)

✅ UI State Updated:
   Old Style: Modern
   New Style: Minimalist (Clean)

💾 SAVE_SETTINGS_CALLED
   ✓ selectedHtmlStyle: MODERN  ← WRONG!

✅ SAVE_SETTINGS COMPLETE
```

❌ **This means**: Selected style is reverting to MODERN in database

---

### PDF with No Items
```
📝 GENERATING HTML CONTENT FROM INVOICE DATA
✅ LINE ITEMS DATA:
   Total Items: 0  ← PROBLEM!
   [No items logged]

🔄 STEP 4
   File size: 8234 bytes (8.2 KB)  ← Very small!

⚠️ PDF is very small - may contain minimal content
```

❌ **This means**: Invoice has no items, so PDF is mostly empty

---

## 🎯 Decision Tree

```
Issue: Can't change PDF styles
  ↓
  Look for: 🎨 updateSelectedHtmlStyle() CALLED
  
  NOT FOUND?
    → Check if clicking the style button
    → Check if onClick callback is wired
  
  FOUND?
    Look for: ✓ selectedHtmlStyle: [Your Style]
    
    Shows MODERN instead of what you selected?
      → Database issue
      → Check saveSettings() in repository
    
    Shows correct style?
      → Settings IS working!
      → Issue is elsewhere
```

```
Issue: PDFs are blank
  ↓
  Look for: 📝 GENERATING HTML CONTENT
  
  NOT FOUND?
    → PDF generation didn't start
    → Check if clicking "Generate PDF"
  
  FOUND?
    Look for: Total Items: [NUMBER]
    
    Shows 0?
      → Create invoice WITH items
      → No items = blank PDF
    
    Shows > 0?
      Look for: File size: [SIZE]
      
      Shows 0 bytes?
        → PDF converter failed
        → Check for exceptions
      
      Shows < 5 KB?
        → PDF very small
        → Converter might have failed
      
      Shows > 50 KB?
        → PDF probably OK
        → Open in vault to check
```

---

## 📞 For Support

**When asking for help, provide**:
1. What you were testing (style/PDF)
2. What you saw (blank page, locked styles, etc.)
3. **The Logcat output** (copy-paste the relevant section)
4. Screenshot of settings/result

**With logs, root cause is obvious in 2 minutes** ✨

---

## ✅ Success Indicators

### Style Selection Working
- [ ] See `🎨 updateSelectedHtmlStyle()` log
- [ ] See `💾 SAVE_SETTINGS_CALLED` log
- [ ] Log shows your selected style (not MODERN)
- [ ] Settings remembers your choice after restart

### PDF Generation Working
- [ ] See `📝 GENERATING HTML CONTENT` log
- [ ] See `Total Items: [your count]`
- [ ] See `File size: XXXXX bytes` (not 0)
- [ ] PDF opens in vault and shows content

---

**Print this card and keep it nearby while testing!** 📋


