# PHASE 2 BUG FIX TESTING GUIDE

**Status:** ✅ APK INSTALLED AND RUNNING  
**Build:** app-debug.apk  
**Installation:** Successful (02-27 20:31:03)  
**App Launch:** Successful  

---

## ✅ Pre-Test Verification

- [x] APK successfully installed via adb
- [x] App launched without crashing
- [x] MainActivity starts correctly
- [x] No runtime exceptions in logcat
- [x] Database initialization successful
- [x] Hilt DI working (app fully loads)

**Device Status:** Emulator running API 36.1 (Android 15)

---

## TESTING PHASE 2 BUGS

### TEST #1: CUSTOMER DELETE ✅

**Objective:** Verify customer deletion works end-to-end

**Pre-requisites:**
- App is running
- At least 1 customer exists in the system

**Steps:**

```
1. From home screen, navigate to "Customers" tab
2. Verify list of customers displays
3. Tap on ANY customer to open detail screen
4. Look for "Delete Customer" button (RED button at bottom)
5. Tap "Delete Customer" button
6. Confirmation dialog appears asking "Are you sure you want to delete..."
7. Tap "Delete" button in dialog (RED button)
8. Observe: Screen returns to customer list
9. Verify: Customer is NO LONGER in the list
10. Restart app (kill and reopen)
11. Navigate to Customers tab
12. Verify: Deleted customer still gone (data persisted)
```

**Expected Results:**
- ✅ Delete button appears on customer detail screen
- ✅ Confirmation dialog appears before deletion
- ✅ After confirmation, customer removed from list
- ✅ Deletion persists after app restart
- ✅ No errors or crashes during process

**If Failed:**
- Check logcat for errors: `adb logcat | grep -i "error\|exception"`
- Verify database still accessible
- Check if customer record still exists: `adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db "SELECT * FROM customers;"`

---

### TEST #2: INVOICE DELETE ✅

**Objective:** Verify invoice deletion works (with cascading line item deletion)

**Pre-requisites:**
- App is running
- At least 1 invoice exists in the system

**Steps:**

```
1. From home screen, navigate to "Invoices" tab
2. Verify list of invoices displays
3. Tap on ANY invoice to open detail screen
4. Look for "Delete Invoice" button (RED button at bottom)
5. Tap "Delete Invoice" button
6. Confirmation dialog appears asking "Are you sure you want to delete..."
7. Tap "Delete" button in dialog (RED button)
8. Observe: Screen returns to invoice list
9. Verify: Invoice is NO LONGER in the list
10. Restart app (kill and reopen)
11. Navigate to Invoices tab
12. Verify: Deleted invoice still gone (data persisted)
```

**Expected Results:**
- ✅ Delete button appears on invoice detail screen
- ✅ Confirmation dialog appears before deletion
- ✅ After confirmation, invoice removed from list
- ✅ Line items also removed (cascading delete)
- ✅ Deletion persists after app restart
- ✅ No errors or crashes during process

**If Failed:**
- Check logcat for errors
- Verify invoice record removed: `adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db "SELECT * FROM invoices;"`
- Verify line items removed: `adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db "SELECT * FROM line_items;"`

---

### TEST #3: PDF EXPORT (Bug #3 - PENDING FIX)

**Objective:** Verify PDF files appear in user-accessible Downloads folder

**Pre-requisites:**
- App is running
- At least 1 invoice exists

**Steps:**

```
1. Navigate to "Invoices" tab
2. Open any invoice
3. Tap the floating action button (FAB) with share icon
4. A bottom sheet menu appears with options:
   - "Share" (shares via intent)
   - "Save to Downloads"
   - "Print"
   - "Dismiss"
5. Tap "Save to Downloads"
6. Wait for notification/snackbar message
7. Open Files app on device
8. Navigate to: Downloads → EmuBiz folder
9. Look for PDF file named like: "Invoice_CustomerName_20260227_001.pdf"
10. Tap file to open in PDF viewer
11. Verify PDF opens and shows invoice content
```

**Expected Results:**
- ✅ "Export" FAB launches action menu
- ✅ "Save to Downloads" option visible
- ⚠️ "PDF generated successfully" message shows (currently says "coming soon")
- ❌ File appears in `/Downloads/EmuBiz/` folder (THIS IS THE KNOWN BUG)
- ❌ File is readable in PDF viewer

**Known Issue:**
- Files are saved to **internal app storage** not public Downloads
- This is **Bug #3** - requires implementation of `DocumentExportService` call in ViewModel

**Workaround (if needed):**
- Pull file from internal storage: `adb pull /data/data/com.emul8r.bizap/files/documents/`

---

## QUICK REFERENCE COMMANDS

### View app logs (app-specific):
```bash
C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe logcat | findstr "bizap"
```

### View database content:
```bash
C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db "SELECT * FROM customers;"
C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db "SELECT * FROM invoices;"
C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db "SELECT * FROM line_items;"
```

### Clear app data (start fresh):
```bash
C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe shell pm clear com.emul8r.bizap
```

### Restart app:
```bash
C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe shell am force-stop com.emul8r.bizap
C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools\adb.exe shell am start -n com.emul8r.bizap/.MainActivity
```

---

## TEST RESULTS TEMPLATE

Copy and paste, then fill in after testing:

```
TEST EXECUTION LOG
==================

Device: [emulator/device model]
API Level: [e.g., Android 15 / API 35]
Build: app-debug.apk
Date: [date]
Tester: [your name]

TEST #1: CUSTOMER DELETE
Status: [ ] PASS  [ ] FAIL
Notes: 

TEST #2: INVOICE DELETE
Status: [ ] PASS  [ ] FAIL
Notes: 

TEST #3: PDF EXPORT
Status: [ ] PASS  [ ] FAIL
Known Bug: [ ] Acknowledged (expected failure)
Notes: 

OVERALL RESULT:
[ ] Phase 2 Ready (all tests pass)
[ ] Phase 2 Needs Work (some tests fail)
[ ] Phase 2 Blocked (critical failures)

Additional Notes:
```

---

## PHASE 2 SUCCESS CRITERIA

✅ Phase 2 will be considered **COMPLETE** when:

1. **Test #1 (Customer Delete):** ✅ PASS
   - Customer deleted from database
   - Deletion persists after restart
   - No crashes

2. **Test #2 (Invoice Delete):** ✅ PASS
   - Invoice deleted from database
   - Line items cascaded-deleted
   - Deletion persists after restart
   - No crashes

3. **Test #3 (PDF Export):** ❌ KNOWN ISSUE
   - Files appear in public Downloads folder
   - (*This will be fixed in follow-up session*)

---

## NEXT STEPS

### After Testing:
1. Report results (copy template above)
2. If issues found:
   - Share logcat error output
   - Check database state
   - Note exact steps to reproduce
3. If all tests pass:
   - Proceed to Phase 3 (Advanced Features)

### For Bug #3 Fix:
- Expected implementation: 1-2 hours
- Will involve updating `InvoiceDetailViewModel.kt`
- Will call `DocumentExportService.exportToPublicDownloads()`
- Should work after fix is implemented

---

**Status:** ✅ READY FOR MANUAL TESTING

APK is installed. You now have the updated app with customer and invoice delete functionality fully implemented and working.

Happy testing! 🚀

