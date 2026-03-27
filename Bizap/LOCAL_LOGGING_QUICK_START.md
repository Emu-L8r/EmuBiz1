# 🚀 Quick Start: Local Timber Logging (No Firebase!)

## What You Get

✅ All app logs saved to local file  
✅ Works completely offline  
✅ View logs in real-time  
✅ Search and filter operations  
✅ Export for debugging  
✅ No Firebase setup needed  

---

## Installation Complete ✅

The logging is already implemented! No additional setup needed.

---

## How to View Logs

### Method 1: Real-Time Logcat (Easiest)

```bash
# See all logs in real-time
adb logcat | grep com.emul8r.bizap

# See only PDF operations
adb logcat | grep "PDF_EXPORT"

# See only errors
adb logcat | grep "❌"
```

### Method 2: Pull Log File

```bash
# Pull the log file from device
adb pull /data/data/com.emul8r.bizap/files/bizap_logs.txt

# View logs
cat bizap_logs.txt

# Search for errors
findstr "❌" bizap_logs.txt

# Search for PDF operations
findstr "PDF_EXPORT" bizap_logs.txt
```

### Method 3: From Within App

```kotlin
// In any Activity or Fragment:
val summary = LogViewer.getPdfExportSummary(context)
Toast.makeText(context, summary, Toast.LENGTH_LONG).show()

// Or get all logs:
val allLogs = LogViewer.getAllLogs(context)
Log.d("MyApp", allLogs)

// Or share logs via email:
LogViewer.shareLogs(context, this)
```

---

## Testing PDF Export

### 1. Clear Old Logs
```bash
adb logcat -c
adb shell rm /data/data/com.emul8r.bizap/files/bizap_logs.txt
```

### 2. Test PDF Export
- Open the app
- Open an invoice
- Tap "Export as PDF"
- Tap "Share Invoice"
- Select Gmail or Email (or cancel)

### 3. View Logs
```bash
adb pull /data/data/com.emul8r.bizap/files/bizap_logs.txt
cat bizap_logs.txt
```

### 4. Expected Output
```
[14:32:45.123] [D] [InvoiceDetailViewModel] 📄 PDF_EXPORT_ATTEMPT [14:32:45.123] invoiceId=456 type=Invoice
[14:32:47.456] [D] [InvoicePdfService] ✅ PDF_EXPORT_SUCCESS [14:32:47.456] invoiceId=456 type=Invoice filePath=/data/data/.../Invoice_Customer_20260326_001.pdf sizeBytes=45678
[14:32:47.500] [D] [FileUriProvider] 🔗 FILEPROVIDER_ATTEMPT [14:32:47.500] fileName=Invoice_Customer_20260326_001.pdf
[14:32:47.550] [D] [FileUriProvider] ✅ FILEPROVIDER_SUCCESS [14:32:47.550] fileName=Invoice_Customer_20260326_001.pdf uri=content://com.emul8r.bizap.fileprovider/files/...
[14:32:47.600] [D] [InvoiceDetailScreen] 📤 SHARE_INTENT [14:32:47.600] mimeType=application/pdf fileName=Invoice_Customer_20260326_001.pdf
```

---

## What Gets Logged

### PDF Export Operations
- Attempt to export PDF
- Success with file details
- Failure with error message

### FileProvider Operations
- Attempt to convert file to content URI
- Success with URI path
- Failure with error details

### File Validation
- File exists check
- File readable check
- File size check

### Share Intent
- PDF and CSV sharing attempts

### Print Operations
- Print button taps

---

## Commands Cheat Sheet

```bash
# View real-time PDF logs
adb logcat | findstr "PDF_EXPORT"

# Pull all logs
adb pull /data/data/com.emul8r.bizap/files/

# Search logs for errors
findstr "❌" bizap_logs.txt

# Search logs for FileProvider
findstr "FILEPROVIDER" bizap_logs.txt

# Get line count of logs
find /c /v "" bizap_logs.txt

# Clear logs
adb shell rm /data/data/com.emul8r.bizap/files/bizap_logs.txt
```

---

## Log File Locations

| Item | Location |
|------|----------|
| Current Log | `/data/data/com.emul8r.bizap/files/bizap_logs.txt` |
| Archived Logs | `/data/data/com.emul8r.bizap/files/bizap_logs_*.txt` |
| Max Size | 5 MB (auto-rotates) |
| Max Archives | 3 files |

---

## Troubleshooting

### No logs appearing?
```bash
# Check log file exists
adb shell ls -la /data/data/com.emul8r.bizap/files/bizap_logs.txt

# Check logcat is working
adb logcat | head -20
```

### Log file too large?
```bash
# Check size
adb shell du -h /data/data/com.emul8r.bizap/files/

# Clear logs programmatically
LogViewer.clearLogs(context)
```

### Can't find PDF operations?
```bash
# Verify PDF export was attempted
adb logcat | grep "PDF_EXPORT_ATTEMPT"

# Check for FileProvider errors
adb logcat | grep "FILEPROVIDER_FAILURE"
```

---

## Next Steps

1. ✅ Build app: `./gradlew installDebug`
2. ✅ Test PDF export
3. ✅ Pull logs: `adb pull /data/data/com.emul8r.bizap/files/bizap_logs.txt`
4. ✅ View and analyze
5. ✅ Share when needed: `LogViewer.shareLogs(context, activity)`

---

## No Firebase? No Problem!

✅ All logs are local  
✅ Works offline  
✅ Instant access  
✅ Easy to debug  
✅ No setup required  

You're all set! 🎉

