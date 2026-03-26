# 🖥️ POWERSHELL COMMAND REFERENCE - PDF EXPORT TEST

## Quick Commands (Copy & Paste)

### Start Fresh - Clear Device
```powershell
adb shell pm clear com.emul8r.bizap
```

### Launch App
```powershell
adb shell am start -n com.emul8r.bizap/.MainActivity
```

### Monitor Logs (Windows PowerShell)
```powershell
adb logcat | Select-String -Pattern "PDF|Export|bizap" -CaseSensitive
```

### Monitor ONLY Errors
```powershell
adb logcat | Select-String -Pattern "ERROR|Failed|❌" -CaseSensitive
```

### Monitor ONLY Success
```powershell
adb logcat | Select-String -Pattern "✅|Ready|successful" -CaseSensitive
```

### Real-Time Everything (All Logs)
```powershell
adb logcat
```
(Press Ctrl+C to stop)

---

## 🔄 Complete Test Workflow (Manual)

### Terminal 1: Start Logcat Monitoring
```powershell
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap
adb logcat | Select-String -Pattern "PDF|Export|bizap|ERROR"
```

### Terminal 2: Launch App
```powershell
adb shell am start -n com.emul8r.bizap/.MainActivity
```

### Terminal 3 (Optional): Watch Device
```powershell
# See what app is running
adb shell "ps | Select-String bizap"

# Or see recent logcat lines (last 50)
adb logcat -t 50
```

### In the App (on device/emulator):
1. Go to **Invoices**
2. Click on any **Invoice**
3. Click **Export Document** button
4. Watch **Terminal 1** for logs

---

## 🎯 What The Logs Mean

### ✅ Good Signs (Success)
```
📄 Starting PDF preview preparation for invoice: 123
📝 Generated invoice snapshot for John Doe
🔄 Temporary PDF generated: /data/user/0/com/emul8r/bizap/cache/invoice_123.pdf
📁 PDF archived to internal storage: /data/user/0/com/emul8r/bizap/files/documents/invoice_123.pdf
💾 PDF path updated in database
🖼️ PDF preview bitmap created successfully
✅ PDF preview ready for invoice: 123
```

### ❌ Bad Signs (Error - NEED TO FIX)
```
❌ Failed to generate PDF bitmap from: /path/to/file
java.lang.IllegalStateException: Failed to generate PDF preview: Could not open PDF file descriptor
❌ Error preparing PDF preview for invoice: 123
```

### 📤 For Share Test
```
📤 Sharing PDF file: invoice_123.pdf
✅ Share intent launched successfully
```

### 💾 For Download Test  
```
💾 Exporting PDF to Downloads: invoice_123.pdf
✅ PDF exported to Downloads: content://media/...
```

### 🖨️ For Print Test
```
🖨️ Launching system print dialog for: invoice_123.pdf
⚠️ System print not yet fully implemented
```

---

## 🚨 Error Reference

### If You See This Error:
```
E/bizap: ❌ Could not open PDF file descriptor: /data/user/0/com.emul8r.bizap/files/documents/invoice_123.pdf
```
**Means:** FileProvider issue - file exists but can't be accessed  
**Action:** Check FileProvider configuration in AndroidManifest.xml

### If You See This Error:
```
E/bizap: ❌ Failed to generate PDF bitmap from: /path/to/file
```
**Means:** PDF file exists but PdfRenderer can't open it  
**Action:** Check PDF file is not corrupted, has proper permissions

### If You See This Error:
```
E/bizap: ❌ Error preparing PDF preview for invoice: 123
     java.lang.IllegalStateException: Invoice not found
```
**Means:** Invoice database issue  
**Action:** Try creating a new invoice

### If You See This Error:
```
E/bizap: ❌ Failed to export PDF to Downloads (returned null)
```
**Means:** Download permission or storage issue  
**Action:** Check app has WRITE_EXTERNAL_STORAGE permission

---

## 📊 Log Levels Explained

| Level | Color | Meaning | Example |
|-------|-------|---------|---------|
| D | 🔵 Debug | Low-level details | `D: 📄 Starting PDF preview` |
| I | 🟢 Info | Important status | `I: ✅ PDF preview ready` |
| W | 🟡 Warning | Needs attention | `W: ⚠️ System print not implemented` |
| E | 🔴 Error | Something failed | `E: ❌ Failed to generate bitmap` |
| F | ⚫ Fatal | App will crash | (Usually shows with exception) |

---

## 🔍 Deep Dive: Line-by-Line Test

### Test 1: Check Device Connection
```powershell
adb devices

# Expected output:
# List of attached devices
# emulator-5554           device
# (or your phone's serial)
```

### Test 2: Clear App State
```powershell
adb shell pm clear com.emul8r.bizap

# Expected: No error output
```

### Test 3: Launch App
```powershell
adb shell am start -n com.emul8r.bizap/.MainActivity

# Expected output shows:
# Starting: Intent { act=android.intent.action.MAIN ... }
```

### Test 4: Get App Logs (Last Hour)
```powershell
adb logcat -t 60:00

# Shows all logs from last 60 minutes
```

### Test 5: Save Logs to File
```powershell
adb logcat > logs.txt

# Run this, do your test, then Ctrl+C
# Then examine logs.txt with notepad
```

### Test 6: Filter Logs by App
```powershell
adb logcat | Select-String "com.emul8r.bizap"

# Shows only logs from your app
```

---

## 💡 Pro Tips

### Tip 1: Faster Logcat Monitoring
```powershell
# Don't show all the system logs, just your app
adb logcat *:S com.emul8r.bizap:V

# *:S = Silent all, com.emul8r.bizap:V = Verbose your app
```

### Tip 2: Save & Review Later
```powershell
# Save all logs to file
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
adb logcat > "pdf_test_$timestamp.log"

# Then after testing, check the file
Notepad "pdf_test_$timestamp.log"
```

### Tip 3: Follow a Specific Line
```powershell
# Watch for just one keyword
adb logcat | Select-String "PDF"

# Or for errors
adb logcat | Select-String "ERROR"
```

### Tip 4: Real-Time Color Filtering
```powershell
# Red for errors, Green for success, Cyan for info
adb logcat | ForEach-Object {
    if ($_ -match 'ERROR|❌') { Write-Host $_ -ForegroundColor Red }
    elseif ($_ -match '✅') { Write-Host $_ -ForegroundColor Green }
    else { Write-Host $_ -ForegroundColor Cyan }
}
```

---

## 🎯 Complete Test Checklist

Run through this checklist and report back:

- [ ] Device connected (`adb devices` shows device)
- [ ] App launches (appears on screen)
- [ ] Can navigate to invoice
- [ ] Click Export Document button
- [ ] Logcat shows: `📄 Starting PDF preview`
- [ ] Logcat shows: `✅ PDF preview ready` (✅ SUCCESS)
  - OR shows error message (❌ FAILURE)
- [ ] PDF preview image appears
- [ ] Can click Share icon
- [ ] Logcat shows: `✅ Share intent launched`
- [ ] Can click Save to Downloads
- [ ] Logcat shows: `✅ PDF exported`
- [ ] Can verify file in Downloads/Bizap folder
- [ ] No app crashes during any operation

---

## 🚀 If Everything Works ✅

Report back:
```
✅ SUCCESS REPORT:
   - PDF preview generated: YES
   - Logcat showed success messages: YES
   - App didn't crash: YES
   - Share worked: YES
   - Download worked: YES
   - File saved to Downloads: YES
   
🎉 PDF Export Crash is FIXED!
```

---

## ❌ If Something Fails

Report back:
```
❌ FAILURE REPORT:
   - What happened: [describe]
   - Error message from Logcat: [copy exact text]
   - Steps to reproduce: [what you did]
   - Screenshot/logs attached: [yes/no]
```

Then I'll:
1. Analyze the error
2. Identify the root cause
3. Create a targeted fix
4. Test it with you again

---

## 📞 Quick Help

**Lost? Run this:**
```powershell
.\test-pdf-fix.ps1
```

**Need to see logs?**
```powershell
adb logcat | Select-String "PDF|Export"
```

**App crashed?**
```powershell
adb logcat | Select-String "CRASH|FATAL|Exception"
```

**Need to restart?**
```powershell
adb shell am force-stop com.emul8r.bizap
adb shell am start -n com.emul8r.bizap/.MainActivity
```

---

**Ready? Run the test and report back! 🚀**

