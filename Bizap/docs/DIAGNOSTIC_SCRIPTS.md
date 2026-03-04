# Bizap Diagnostic Script Collection

This directory contains diagnostic scripts and commands for troubleshooting Bizap runtime issues.

## Quick Diagnostics

### 1. Check Database Version
```bash
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db "PRAGMA user_version;"
# Should return: 24
```

### 2. Verify Line Items Schema
```bash
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db "PRAGMA table_info(line_items);"
# unitPrice should show affinity: INTEGER (not REAL)
# quantity should show affinity: REAL
```

### 3. Export Database for Local Analysis
```bash
adb pull /data/data/com.emul8r.bizap/databases/bizap-db ./local-bizap-db
sqlite3 ./local-bizap-db "SELECT * FROM line_items LIMIT 5;"
```

### 4. Monitor Logcat for Type Errors
```bash
adb logcat | grep -E "f != java.lang.Long|Type mismatch|IllegalFormat|ClassCastException|updateLineItem|SAVE:"
```

### 5. Get Full Stack Trace on Crash
```bash
adb logcat | grep -A 30 "AndroidRuntime.*FATAL"
```

### 6. Check Current Invoice State
```bash
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db "SELECT id, customerName, totalAmount, status FROM invoices ORDER BY date DESC LIMIT 3;"
```

## Scenario-Based Debugging

### Scenario: "All line items updated together"

1. **Add logging to UpdateLineItem:**
   - Edit `CreateInvoiceViewModel.kt`
   - Find `updateLineItem()` function
   - Add: `Timber.d("updateLineItem: id=$id, items.count=${state.items.size}")`

2. **Monitor output:**
   ```bash
   adb logcat | grep "updateLineItem"
   ```

3. **Expected vs Actual:**
   - Expected: `id=null` matches ONE item
   - Actual: `id=null` matches ALL items with null id

### Scenario: "f != java.lang.Long error on save"

1. **Add type logging before save:**
   ```bash
   adb logcat | grep "SAVE:\|Domain:\|Total"
   ```

2. **Check for String.format usage:**
   ```bash
   grep -r "String.format.*%.2f" app/src/main/java/com/emul8r/bizap/ui/
   # Should return: ZERO matches (all should use CentsFormatter)
   ```

3. **Verify CentsFormatter is used everywhere:**
   ```bash
   grep -r "CentsFormatter.formatCents" app/src/main/java/com/emul8r/bizap/ui/
   # Should return: All display code
   ```

## Files to Check for Type Issues

1. **Display Formatting (MOST COMMON)**
   ```
   app/src/main/java/com/emul8r/bizap/ui/invoices/InvoiceListScreen.kt
   app/src/main/java/com/emul8r/bizap/ui/invoices/InvoiceDetailScreen.kt
   app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceScreen.kt
   app/src/main/java/com/emul8r/bizap/ui/invoices/EditInvoiceScreen.kt
   app/src/main/java/com/emul8r/bizap/ui/dashboard/RevenueDashboardScreen.kt
   ```

2. **ViewModel State Logic (BUG #1)**
   ```
   app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceViewModel.kt (line 130)
   app/src/main/java/com/emul8r/bizap/ui/invoices/EditInvoiceViewModel.kt (line 108)
   ```

3. **Database Operations**
   ```
   app/src/main/java/com/emul8r/bizap/data/local/InvoiceDao.kt
   app/src/main/java/com/emul8r/bizap/data/repository/InvoiceRepositoryImpl.kt
   ```

4. **Type Conversions**
   ```
   app/src/main/java/com/emul8r/bizap/ui/invoices/Mappers.kt
   app/src/main/java/com/emul8r/bizap/data/mapper/InvoiceMapper.kt
   app/src/main/java/com/emul8r/bizap/domain/model/LineItemExtensions.kt
   ```

## Database Recovery

### If database is corrupted:

```bash
# 1. Clear app data
adb shell pm clear com.emul8r.bizap

# 2. Uninstall app
adb uninstall com.emul8r.bizap

# 3. Reinstall
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 4. Verify migrations ran
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db "PRAGMA user_version;"
```

### If specific table is corrupted:

```bash
# Backup database
adb pull /data/data/com.emul8r.bizap/databases/bizap-db ./bizap-db-backup

# Inspect table
sqlite3 ./bizap-db-backup "PRAGMA table_info(line_items);"

# Check for type mismatches
sqlite3 ./bizap-db-backup "SELECT typeof(unitPrice), typeof(quantity), COUNT(*) FROM line_items GROUP BY typeof(unitPrice), typeof(quantity);"
```

## Key Metrics to Track

### Database Health
- Migrations applied: `PRAGMA user_version;` (should be 24)
- Line items count: `SELECT COUNT(*) FROM line_items;`
- Invoices count: `SELECT COUNT(*) FROM invoices;`
- Average line items per invoice: `SELECT AVG(item_count) FROM (SELECT COUNT(*) as item_count FROM line_items GROUP BY invoiceId);`

### Type Consistency
- All unitPrice values are INTEGER: `SELECT COUNT(*) FROM line_items WHERE typeof(unitPrice) != 'integer';` (should be 0)
- All quantity values are REAL: `SELECT COUNT(*) FROM line_items WHERE typeof(quantity) != 'real';` (should be 0)

### Data Integrity
- No orphaned line items: `SELECT COUNT(*) FROM line_items WHERE invoiceId NOT IN (SELECT id FROM invoices);` (should be 0)
- All invoices have at least one item: `SELECT COUNT(*) FROM invoices WHERE id NOT IN (SELECT DISTINCT invoiceId FROM line_items);`

## Monitoring Commands

```bash
# Start continuous monitoring of logcat
adb logcat -s "AndroidRuntime:E" "BizapApp:D" "Room:E" "Hilt:W" > ~/bizap-debug-$(date +%Y%m%d_%H%M%S).log &

# Watch for type errors in real-time
watch -n 1 'adb logcat -d | grep -E "f != java.lang.Long|Type mismatch" | tail -20'

# Monitor database writes
adb shell "sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db \"SELECT COUNT(*) FROM line_items;\"" && sleep 5 && adb shell "sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db \"SELECT COUNT(*) FROM line_items;\""
```

## Performance Diagnostics

### Check database query performance
```bash
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db ".eqp on" "SELECT * FROM invoices WHERE businessProfileId = 1 ORDER BY date DESC LIMIT 10;"
# Look for sequential scans (should use index instead)
```

### Check query execution time
```bash
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db ".timer on" "SELECT * FROM invoices WHERE businessProfileId = 1 ORDER BY date DESC LIMIT 10;"
```

## Common Issues Quick Reference

| Issue | Check | Command |
|-------|-------|---------|
| Migration not applied | Database version | `adb shell sqlite3 ... "PRAGMA user_version;"` |
| Type mismatch on save | Column affinity | `adb shell sqlite3 ... "PRAGMA table_info(line_items);"` |
| All items updated together | ViewModel logic | `grep "if (it.id == id)" CreateInvoiceViewModel.kt` |
| Amounts display wrong | Formatter usage | `grep "String.format.*%.2f" **/*.kt` |
| Database corrupted | Migrate from backup | See "Database Recovery" section |
| Orphaned line items | Foreign key constraint | Check if invoice was deleted |

## Contact & Escalation

If diagnostics don't resolve the issue:

1. **Capture full diagnostics package:**
   ```bash
   mkdir bizap-diagnostics-$(date +%Y%m%d_%H%M%S)
   cd bizap-diagnostics-*
   adb pull /data/data/com.emul8r.bizap/databases/bizap-db .
   adb logcat -d > logcat.txt
   adb shell dumpsys meminfo com.emul8r.bizap > meminfo.txt
   adb shell ps | grep bizap > process.txt
   ```

2. **Document exact steps to reproduce:**
   - Create invoice → Add N items → Edit item M → Error occurs at _____

3. **Include this info:**
   - Device/emulator model
   - Android version
   - App version (from BuildConfig)
   - Database version (PRAGMA user_version)
   - Exact error message
   - Logcat output

---

**Last Updated:** March 4, 2026
**Version:** 1.0

