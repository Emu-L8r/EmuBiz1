@echo off
setlocal enabledelayedexpansion

set PATH=%PATH%;C:\Users\Saucey\AppData\Local\Android\Sdk\platform-tools
set DB_PATH=/data/data/com.emul8r.bizap/databases/bizap.db

echo ===== PHASE 2: DATABASE MIGRATION VALIDATION =====
echo.

echo [1] Checking if database file exists...
adb shell test -f %DB_PATH% && echo Database file EXISTS || echo Database file NOT FOUND

echo.
echo [2] Listing all tables...
adb shell sqlite3 %DB_PATH% ".tables" > tables.txt 2>&1
type tables.txt

echo.
echo [3] Schema: invoiceTemplates
adb shell sqlite3 %DB_PATH% ".schema invoiceTemplates" > schema_templates.txt 2>&1
type schema_templates.txt

echo.
echo [4] Schema: invoiceCustomFields
adb shell sqlite3 %DB_PATH% ".schema invoiceCustomFields" > schema_fields.txt 2>&1
type schema_fields.txt

echo.
echo [5] Indices on both tables
adb shell sqlite3 %DB_PATH% "SELECT name, tbl_name FROM sqlite_master WHERE type='index' AND tbl_name IN ('invoiceTemplates', 'invoiceCustomFields');" > indices.txt 2>&1
type indices.txt

echo.
echo [6] Foreign keys - invoiceTemplates
adb shell sqlite3 %DB_PATH% "PRAGMA foreign_key_list(invoiceTemplates);" > fk_templates.txt 2>&1
type fk_templates.txt

echo.
echo [7] Foreign keys - invoiceCustomFields
adb shell sqlite3 %DB_PATH% "PRAGMA foreign_key_list(invoiceCustomFields);" > fk_fields.txt 2>&1
type fk_fields.txt

echo.
echo [8] Row counts
adb shell sqlite3 %DB_PATH% "SELECT 'invoiceTemplates' as table_name, COUNT(*) as count FROM invoiceTemplates UNION ALL SELECT 'invoiceCustomFields', COUNT(*) FROM invoiceCustomFields;" > counts.txt 2>&1
type counts.txt

echo.
echo ===== PHASE 2 VERIFICATION COMPLETE =====
echo.
echo Files created for review:
echo - tables.txt
echo - schema_templates.txt
echo - schema_fields.txt
echo - indices.txt
echo - fk_templates.txt
echo - fk_fields.txt
echo - counts.txt

