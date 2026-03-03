#!/bin/bash

# Database path
DB_PATH="/data/data/com.emul8r.bizap/databases/bizap.db"

echo "=== PHASE 2: DATABASE MIGRATION VALIDATION ==="
echo ""

# Check if database exists
adb shell "test -f $DB_PATH && echo 'Database file exists' || echo 'Database file NOT FOUND'"

echo ""
echo "=== CHECKING TABLES ==="
adb shell "sqlite3 $DB_PATH '.tables'" 2>&1

echo ""
echo "=== SCHEMA: invoiceTemplates ==="
adb shell "sqlite3 $DB_PATH '.schema invoiceTemplates'" 2>&1

echo ""
echo "=== SCHEMA: invoiceCustomFields ==="
adb shell "sqlite3 $DB_PATH '.schema invoiceCustomFields'" 2>&1

echo ""
echo "=== INDICES ==="
adb shell "sqlite3 $DB_PATH \"SELECT name, tbl_name FROM sqlite_master WHERE type='index' AND tbl_name IN ('invoiceTemplates', 'invoiceCustomFields');\"" 2>&1

echo ""
echo "=== FOREIGN KEYS ==="
adb shell "sqlite3 $DB_PATH \"PRAGMA foreign_key_list(invoiceTemplates);\"" 2>&1
adb shell "sqlite3 $DB_PATH \"PRAGMA foreign_key_list(invoiceCustomFields);\"" 2>&1

echo ""
echo "=== ROW COUNTS ==="
adb shell "sqlite3 $DB_PATH \"SELECT 'invoiceTemplates' as table_name, COUNT(*) as count FROM invoiceTemplates UNION ALL SELECT 'invoiceCustomFields', COUNT(*) FROM invoiceCustomFields;\"" 2>&1

echo ""
echo "=== VERIFICATION COMPLETE ==="

