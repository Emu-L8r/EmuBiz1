# 🧪 HTML STYLE SELECTION FEATURE - QUICK TESTING GUIDE

## 📱 TESTING CHECKLIST

### STEP 1: Install Updated APK ✓
```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
.\gradlew.bat assembleDebug
```

Transfer `app/build/outputs/apk/debug/app-debug.apk` to your Android device and install.

---

### STEP 2: UI Visibility Test ✓

1. **Open the app** on your Android device
2. **Navigate to Settings** (bottom menu or settings icon)
3. **Tap "Invoice Settings"** or **"PDF Settings"**
4. **Scroll down** to "Invoice Theme" section

**Expected Result:**
- See two options: "Canvas Style" and "Modern HTML Style"
- Canvas Style should be selected by default

---

### STEP 3: Theme Selection Test ✓

1. **Tap "Modern HTML Style"** radio button
2. **Scroll down** to see the new section

**Expected Result:**
- New section appears: **"HTML Invoice Style"**
- Shows 4 style options:
  - ✓ MODERN (Premium) - Selected (highlighted, check icon)
  - ○ MINIMAL (Clean)
  - ○ CORPORATE (Formal)
  - ○ CREATIVE (Startup)

---

### STEP 4: Style Selection Test ✓

In the "HTML Invoice Style" section:

1. **Tap "MINIMAL (Clean)"** radio button

**Expected Result:**
- MINIMAL card highlights with purple border
- Check icon appears on MINIMAL
- MODERN loses highlight and check icon

2. **Tap "CORPORATE (Formal)"** radio button

**Expected Result:**
- CORPORATE card highlights
- Other styles lose highlight
- Check icon appears on CORPORATE

3. **Tap "CREATIVE (Startup)"** radio button

**Expected Result:**
- CREATIVE card highlights
- Check icon appears on CREATIVE

4. **Tap back to "MODERN (Premium)"**

**Expected Result:**
- MODERN highlights again
- All transitions are smooth

---

### STEP 5: Persistence Test ✓

1. **Select "MINIMAL (Clean)"** style
2. **Scroll to bottom** and **tap "Save Settings"**
3. **See success message:** "✅ Settings saved successfully"
4. **Go back** to main menu (close settings)
5. **Open Settings again** and go to **Invoice Settings**

**Expected Result:**
- Navigate to "HTML Invoice Style" section
- **"MINIMAL (Clean)"** is still selected
- Settings persisted across navigation!

6. **Restart the entire app:**
   - Close the app completely
   - Reopen it
   - Go to Settings > Invoice Settings
   - Navigate to HTML Invoice Style section

**Expected Result:**
- **"MINIMAL (Clean)"** is STILL selected
- Settings persisted across app restarts!

---

### STEP 6: PDF Generation Test ✓

1. **Select different HTML styles** (one at a time):
   - MODERN (Premium)
   - MINIMAL (Clean)
   - CORPORATE (Formal)
   - CREATIVE (Startup)

2. **For each style:**
   - Save the setting
   - Go to Invoices
   - Create a test invoice
   - Generate PDF
   - Download/view the PDF

**Expected Results:**
- MODERN PDF: Purple gradient header, modern sans-serif fonts
- MINIMAL PDF: Black & white, clean minimal design
- CORPORATE PDF: Serif fonts, blue tones, formal layout
- CREATIVE PDF: Orange/teal colors, startup vibe

---

### STEP 7: Switching Back to Canvas ✓

1. **Go to Settings > Invoice Settings**
2. **Select "Canvas Style"** radio button
3. **Scroll to "Invoice Theme" section**

**Expected Result:**
- **"HTML Invoice Style" section DISAPPEARS**
- Only shows Canvas Style option
- This is correct behavior!

4. **Save settings**
5. **Generate a PDF**

**Expected Result:**
- PDF uses Canvas style (original design)
- Not HTML style anymore

---

### STEP 8: Back to HTML ✓

1. **Select "Modern HTML Style"** again
2. **Scroll down**

**Expected Result:**
- "HTML Invoice Style" section reappears
- Shows all 4 style options
- Previously selected style is still selected! (persistence)

---

## ✅ SUCCESS CRITERIA

All tests pass if:
- [x] HTML Style section appears only when HTML_PDF theme selected
- [x] All 4 styles display correctly (name + description)
- [x] Radio buttons work correctly
- [x] Selected style has highlight and check icon
- [x] Can switch between styles smoothly
- [x] Selected style persists after clicking Save
- [x] Selected style persists after app restart
- [x] Switching to Canvas hides HTML Style section
- [x] PDFs generate with correct style applied

---

## 🎨 VISUAL VERIFICATION

When you see the HTML Invoice Style section, it should look like:

```
┌─────────────────────────────────────────────────────┐
│ HTML Invoice Style                                  │
│ Choose your preferred HTML invoice design           │
│                                                     │
│ ◉ MODERN (Premium)                            ✓    │
│   Professional modern design with purple...         │
│                                                     │
│ ○ MINIMAL (Clean)                                  │
│   Clean, elegant design with minimal styling       │
│                                                     │
│ ○ CORPORATE (Formal)                               │
│   Formal business design with serif typography    │
│                                                     │
│ ○ CREATIVE (Startup)                               │
│   Vibrant, modern design perfect for startups      │
└─────────────────────────────────────────────────────┘
```

---

## 🐛 TROUBLESHOOTING

| Issue | Solution |
|-------|----------|
| HTML Style section doesn't show | Make sure "Modern HTML Style" is selected in Theme section, not Canvas |
| Styles don't persist | Tap "Save Settings" button at bottom of screen |
| PDF still looks same | Restart the app, ensure style is saved, regenerate PDF |
| Selection jumps to MODERN | Check that you're clicking the radio button, not just the text |

---

## 📸 SCREENSHOTS TO TAKE

For documentation, take screenshots of:
1. Theme selection (Canvas vs HTML)
2. HTML Style section (all 4 styles visible)
3. Selected MINIMAL style (with highlight)
4. Selected CORPORATE style (with highlight)
5. Success message after saving
6. Different PDFs generated with each style

---

## 🎯 EXPECTED USER JOURNEY

```
Settings Screen
        ↓
Invoice Settings
        ↓
Select "Modern HTML Style"
        ↓
HTML Invoice Style section appears
        ↓
User browses 4 styles:
├── MODERN - Professional purple gradient
├── MINIMAL - Clean and minimal
├── CORPORATE - Formal business style
└── CREATIVE - Vibrant startup style
        ↓
Select preferred style
        ↓
Click "Save Settings"
        ↓
Success notification
        ↓
Create Invoice
        ↓
Generate PDF
        ↓
PDF uses selected style!
```

---

## ✨ FEATURE HIGHLIGHTS

✅ **Professional Styling** - 4 beautiful invoice design options
✅ **Conditional UI** - HTML styles only show when HTML theme selected
✅ **Persistent Storage** - Selection saved across app restarts
✅ **User-Friendly** - Clear descriptions of each style
✅ **Consistent Design** - Matches existing UI patterns
✅ **Easy to Use** - Simple radio button selection

---

**Ready to Test!** 🚀

Happy testing! Please verify all steps work as expected and report any issues.

