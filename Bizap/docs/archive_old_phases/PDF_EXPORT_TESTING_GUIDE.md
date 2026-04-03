# PDF Export Fix - Quick Testing Guide

## Prerequisites
- Emulator running or device connected
- App built with `./gradlew installDebug`
- Logcat open and filtered by "FileUriProvider"

---

## Test 1: PDF Export Success Flow ✅

### Steps:
1. Open app and navigate to **Invoices** (GUI1 or GUI2)
2. Create or select an existing invoice
3. Tap **"Export as PDF"** button
4. Choose **"Share Invoice"** from the dialog
5. Select an app to share (Gmail, Email, etc.) or cancel

### Expected Results:
- ✅ No crash appears
- ✅ Share dialog appears smoothly
- ✅ Snackbar shows "Exporting..." then returns to normal
- ✅ Logcat shows:
  ```
  ✅ PDF generated successfully:
    File: Invoice_*.pdf
    Path: /data/data/com.emul8r.bizap/files/documents/...
    Size: XXXXX bytes
    Type: Invoice
  
  FileUriProvider: Successfully converted file to URI: content://...
  ```

---

## Test 2: CSV Export Flow ✅

### Steps:
1. Open invoice detail
2. Tap **"Export as CSV"** button
3. Choose an app or cancel

### Expected Results:
- ✅ No crash
- ✅ CSV share dialog appears
- ✅ Logcat shows successful file conversion:
  ```
  FileUriProvider: Successfully converted file to URI: content://...
  ```

---

## Test 3: Print Flow ✅

### Steps:
1. Open invoice detail
2. Tap **"Print"** button
3. If print service is available, select printer

### Expected Results:
- ✅ No crash
- ✅ Print dialog appears (if printer available)
- ✅ Logcat shows file validation passed

---

## Test 4: Error Handling (Simulate File Issues)

### If you want to test error scenarios (optional):

1. **Invalid File Path**
   - Look for Logcat message:
   ```
   FileProvider could not access file path. Check file_paths.xml
   ```

2. **Empty File**
   - Look for Logcat message:
   ```
   PDF file is empty (0 bytes): /data/data/...
   ```

3. **File Not Found**
   - Look for Logcat message:
   ```
   File does not exist: /path/to/file
   ```

### When errors occur, you should see:
- ✅ No crash (app stays open)
- ✅ Snackbar with message: "Failed to share PDF: [error reason]"
- ✅ Detailed Timber logs in Logcat

---

## Test 5: Firebase Crashlytics Verification

### Before deploying to production:
1. Open Firebase Console
2. Navigate to **Crashlytics**
3. Look for any PDF export related crashes
4. Expected: **0 crashes** from PDF export paths

---

## Logcat Filter Commands

### View only FileUriProvider logs:
```bash
adb logcat | grep FileUriProvider
```

### View PDF generation logs:
```bash
adb logcat | grep "PDF generated"
```

### View all PDF-related logs:
```bash
adb logcat | grep -E "PDF|FileUriProvider"
```

### Clear logcat before testing:
```bash
adb logcat -c
```

---

## Common Issues & Fixes

| Symptom | Solution |
|---------|----------|
| Still getting crash | Check file_paths.xml is updated correctly |
| Share dialog doesn't appear | Verify Intent.createChooser() is working |
| Empty snackbar message | Check error.message is not null |
| Logcat shows "OOBE is not completed" | This is Samsung Health error, not related to PDF fix |

---

## Success Criteria ✅

After implementation, your PDF export should:

1. ✅ **Never crash** on invalid file paths
2. ✅ **Show helpful error messages** if something goes wrong
3. ✅ **Generate valid PDFs** with correct file sizes
4. ✅ **Share without delay** with proper intents
5. ✅ **Log diagnostics** for troubleshooting
6. ✅ **Work across all Android versions** (minSdk to latest)

---

## Next Phase

Once PDF export is stable, you can work on:
- Vault display in modern GUI2 interface
- Firebase Crashlytics event tracking
- Payment History UI timeline
- Gradle 10 migration

---

**Happy Testing! 🎉**

