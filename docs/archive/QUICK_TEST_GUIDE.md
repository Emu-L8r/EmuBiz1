# ⚡ **QUICK TEST GUIDE - 3 BUG FIXES**

**Time to Complete:** ~10 minutes  
**Installation:** 2 minutes  
**Testing:** 6 minutes  
**Reporting:** 2 minutes

---

## **INSTALLATION (2 min)**

```bash
# Option A: Install from built APK
adb uninstall com.emul8r.bizap
adb install app/build/outputs/apk/debug/app-debug.apk

# Option B: Build and install
git pull origin main
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## **TEST #1: Template Creation (2 min)**

### Steps
```
1. Tap "Templates" tab (bottom navigation)
2. Tap "+" button (floating action button)
3. Fill form:
   - Template Name: "Test Template"
   - Keep other defaults
4. Tap "Create" button
5. Wait for spinner
```

### Expected Results
```
✅ Loading spinner appears for 1-2 seconds
✅ Dialog closes automatically
✅ New template appears in templates list
✅ No error message
```

### If Problem
```
❌ Dialog hangs
❌ Nothing happens on tap
❌ Error banner appears
→ Report the exact error message
```

---

## **TEST #2: Invoice Status Update (2 min)**

### Steps
```
1. Tap "Invoices" tab
2. Open any invoice (or create one)
3. Tap on status chip (top of invoice detail)
4. Select "SENT" from dropdown
5. Close dropdown
```

### Expected Results
```
✅ Dropdown menu appears with status options
✅ After selection, snackbar shows "Status updated to SENT"
✅ Status chip changes color (red → orange)
✅ Invoice detail refreshes automatically
✅ Change persists (close and reopen invoice)
```

### If Problem
```
❌ Dropdown doesn't open
❌ No snackbar message appears
❌ Status doesn't change in UI
❌ Status reverts when you close invoice
→ Report what happens instead
```

---

## **TEST #3: PDF Rendering (2 min)**

### Steps
```
1. Create invoice (or open existing)
2. Add line items with long descriptions:
   - "Long description of services rendered"
   - "Professional development and consulting"
3. Tap "View PDF" or go to Document Vault
4. Open PDF in viewer
5. Look at:
   - Header (company info)
   - Line items table
   - Footer section
```

### Expected Results
```
✅ PDF opens without crash
✅ Text is clearly readable (no overlap)
✅ Long descriptions wrap to 2-3 lines
✅ Line items table is properly spaced
✅ No words written on top of each other
✅ Professional appearance
✅ All content visible (nothing cut off)
```

### If Problem
```
❌ Text overlaps on each other
❌ Words written over each other
❌ Section headers missing
❌ Content extends past page edge
❌ Spacing too cramped
→ Take screenshot and report
```

---

## **RESULTS CHECKLIST**

After running all 3 tests, report:

```
TEST #1 (Template Creation):
[ ] ✅ PASS
[ ] ❌ FAIL (describe issue)

TEST #2 (Status Updates):
[ ] ✅ PASS
[ ] ❌ FAIL (describe issue)

TEST #3 (PDF Rendering):
[ ] ✅ PASS
[ ] ❌ FAIL (describe issue)

Overall:
[ ] All tests passed - Ready for users!
[ ] 1-2 tests failed - Issues remain
[ ] All tests failed - Major problems
```

---

## **REPORT TEMPLATE**

```
Build Date: March 6, 2026
APK Version: Latest from main branch
Device: [Your Device]

Test Results:
- Template Creation: [PASS/FAIL]
- Status Updates: [PASS/FAIL]
- PDF Rendering: [PASS/FAIL]

Issues Found:
[List any issues you found]

Overall Assessment:
[Ready for users / Needs more work / Specific issue details]
```

---

## **QUICK REFERENCE**

| Test | Time | Pass Criteria | Status |
|------|------|---------------|--------|
| **Template** | 2 min | Creates and saves | TBD |
| **Status** | 2 min | Updates persist | TBD |
| **PDF** | 2 min | Readable, no overlap | TBD |

---

**Total Time: ~10 minutes for all tests** ⏱️

Let me know your results! 🚀


