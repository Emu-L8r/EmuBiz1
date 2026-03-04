# 📚 BIZAP TROUBLESHOOTING & DIAGNOSTICS INDEX

**Last Updated:** March 4, 2026  
**Status:** COMPREHENSIVE - All critical issues documented

---

## QUICK START: Choose Your Document

### 🚨 **Just Got an Error?**
→ Read: **COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md** Section 7: "Common Symptom-to-Root-Cause Map"

### 🔧 **Need to Debug Something?**
→ Read: **DIAGNOSTIC_SCRIPTS.md** → Pick your scenario and run the commands

### 🏗️ **Understanding Architecture?**
→ Read: **DATA_FLOW_TYPE_MAPPING.md** → See exact type transformations

### 📖 **Learning the Codebase?**
→ Read: **COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md** Section 1-5 → Complete context

---

## DOCUMENT GUIDE

### 1. COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md (PRIMARY REFERENCE)

**Use when:** You need complete context and understanding

**Contents:**
- ✅ Architecture overview (technology stack, data flow)
- ✅ BUG #1 detailed analysis - NULL ID collision
  - Where it happens
  - Why it happens  
  - How to diagnose
  - Potential fixes
- ✅ BUG #2 detailed analysis - "f != java.lang.Long"
  - Four suspected locations
  - Diagnostic procedures
  - Type assertions
- ✅ Database migration tracking
- ✅ Type system consistency checklist
- ✅ Runtime debugging commands
- ✅ Symptom-to-root-cause mapping table
- ✅ Code locations for quick reference
- ✅ Prevention strategies
- ✅ Quick diagnostic flowchart

**Key Sections:**
- **Section 1:** Architecture overview (5 min read)
- **Section 2:** BUG #1 complete analysis (10 min read)
- **Section 3:** BUG #2 complete analysis (10 min read)
- **Section 4:** Database migrations (5 min read)
- **Section 5:** Type consistency checklist (5 min read)
- **Section 7:** Symptom map (reference table)
- **Section 8:** Code locations (reference table)

**Best for:** Deep understanding, architectural decisions, complete context

---

### 2. DIAGNOSTIC_SCRIPTS.md (OPERATIONAL REFERENCE)

**Use when:** You need specific commands to diagnose an issue

**Contents:**
- ✅ Quick diagnostics (database version, schema, exports)
- ✅ Logcat monitoring commands
- ✅ Scenario-based debugging (all items updated, type mismatch, migration issues)
- ✅ Database recovery steps
- ✅ File locations to check
- ✅ Database health metrics
- ✅ Performance diagnostics
- ✅ Common issues quick reference table
- ✅ Escalation procedures

**Quick Commands Reference:**
```bash
# Check database version
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db "PRAGMA user_version;"

# Verify schema
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap-db "PRAGMA table_info(line_items);"

# Monitor type errors
adb logcat | grep "f != java.lang.Long"

# Export database
adb pull /data/data/com.emul8r.bizap/databases/bizap-db ./local-bizap-db
```

**Best for:** Running diagnostics, collecting data, operational troubleshooting

---

### 3. DATA_FLOW_TYPE_MAPPING.md (TECHNICAL REFERENCE)

**Use when:** You need to understand exact type transformations

**Contents:**
- ✅ Complete flow: Creating an invoice with type annotations
- ✅ Flow: Editing a line item (where BUG #1 manifests)
- ✅ Flow: Displaying amounts (where BUG #2 occurs)
- ✅ Flow: Calculating totals (type coercion details)
- ✅ Type transformation matrix
- ✅ Critical checkpoints
- ✅ Safe vs dangerous patterns

**Key Flows:**
1. **Creating invoice** - Shows complete data flow with all types
2. **Editing line item** - Shows how NULL ID collision occurs
3. **Displaying amounts** - Shows where String.format fails
4. **Calculating totals** - Shows type conversions

**Type Transformation Matrix:**
Shows what happens at each layer:
- UI Input layer
- ViewModel state layer
- Domain model layer
- Entity layer
- Database layer
- Display layer

**Best for:** Understanding type system, debugging specific flows, code reviews

---

## CRITICAL BUG REFERENCE

### BUG #1: NULL ID Collision in Line Item Updates

**Status:** ❌ UNRESOLVED (Known issue, documented)

**Symptom:** Editing one line item updates ALL new line items

**Root Cause:** Using `item.id` (null for new) instead of `item.transientId` (unique)

**Location:** 
- `CreateInvoiceViewModel.kt` line 130
- `EditInvoiceViewModel.kt` line 108

**Diagnostic:** Run scenario in DIAGNOSTIC_SCRIPTS.md: "All line items updated together"

**Documentation:** COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 2

**Visual Flow:** DATA_FLOW_TYPE_MAPPING.md "FLOW 2: Editing a Line Item"

---

### BUG #2: Type Mismatch "f != java.lang.Long"

**Status:** ❌ UNRESOLVED (Root cause location uncertain)

**Symptom:** Runtime error when saving invoice with type mismatch

**Suspected Causes:**
1. Display formatting using `String.format("%.2f", Long)` ← MOST LIKELY
2. Database column type mismatch
3. Calculation result type coercion
4. Compose state closure capture issue

**Locations to Check:**
- InvoiceListScreen.kt (display formatting)
- InvoiceDetailScreen.kt (display formatting)
- RevenueDashboardScreen.kt (display formatting)
- InvoicePdfService.kt (formatting)
- LineItemEditor composable (TextFields)

**Diagnostic:** Run checks in DIAGNOSTIC_SCRIPTS.md: "f != java.long.Long error on save"

**Documentation:** COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 3

**Visual Flow:** DATA_FLOW_TYPE_MAPPING.md "FLOW 3: Displaying Amounts"

---

## ARCHITECTURE REFERENCE

### Layer Overview
```
UI Layer (Compose)
  ↓ (LineItemForm)
ViewModel (StateFlow)
  ↓ (LineItem)
Domain Layer (UseCase)
  ↓ (LineItem → LineItemEntity)
Repository
  ↓ (LineItemEntity)
DAO
  ↓ (Room/SQLite)
Database (v24)
```

### Type System at Each Layer

| Layer | quantity | unitPrice | id |
|-------|----------|-----------|-----|
| UI Form | Double | Long | null |
| ViewModel | Double | Long | null |
| Domain | Double | Long | 0L |
| Entity | Double | REAL | Long | INTEGER |
| Database | REAL | INTEGER | INTEGER |

### Database Versions
- v21: Initial (has pending_operations)
- v22: Removed pending_operations
- v23: Added currencyCode to line_items
- v24: Fixed payment entities Double→Long (current)

---

## HOW TO USE THESE DOCS

### Scenario 1: "App crashed with type error"

**Step 1:** Read symptom → COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 7
**Step 2:** Identify root cause → Run commands from DIAGNOSTIC_SCRIPTS.md
**Step 3:** Understand flow → DATA_FLOW_TYPE_MAPPING.md appropriate flow
**Step 4:** Check code → COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 8

### Scenario 2: "Editing line item changed all items"

**Step 1:** Read about BUG #1 → COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 2
**Step 2:** Understand mechanism → DATA_FLOW_TYPE_MAPPING.md "FLOW 2"
**Step 3:** Verify issue → DIAGNOSTIC_SCRIPTS.md "Scenario: All line items updated"
**Step 4:** Review code → CreateInvoiceViewModel.kt line 130

### Scenario 3: "Database seems corrupted"

**Step 1:** Check version → DIAGNOSTIC_SCRIPTS.md "Check Database Version"
**Step 2:** Verify schema → DIAGNOSTIC_SCRIPTS.md "Verify Line Items Schema"
**Step 3:** Recovery steps → DIAGNOSTIC_SCRIPTS.md "Database Recovery"
**Step 4:** Verify fix → Run verification commands again

### Scenario 4: "Adding new feature with money"

**Step 1:** Review type rules → COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 5
**Step 2:** Study safe patterns → DATA_FLOW_TYPE_MAPPING.md "Safe Patterns vs Dangerous"
**Step 3:** Check locations → COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 8
**Step 4:** Reference flows → DATA_FLOW_TYPE_MAPPING.md all flows

---

## CONTACT & ESCALATION

If after using these docs you still can't resolve the issue:

1. **Collect diagnostics package:**
   ```bash
   # Run all diagnostic commands from DIAGNOSTIC_SCRIPTS.md
   # Export database, logcat, process info
   mkdir bizap-debug-$(date +%Y%m%d)
   cd bizap-debug-*
   # ... run all commands ...
   ```

2. **Document exact reproduction:**
   - Step-by-step actions
   - Expected vs actual behavior
   - When it started happening
   - Device/emulator details

3. **Include context from docs:**
   - Which document section references this?
   - What does the type mapping show?
   - What does the symptom map say?

---

## INDEX OF KEY INFORMATION

### Type Safety Rules
- COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 5
- DATA_FLOW_TYPE_MAPPING.md Section "Safe Patterns vs Dangerous"

### Database Schema
- COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 4
- DIAGNOSTIC_SCRIPTS.md "Verify Line Items Schema"
- DATA_FLOW_TYPE_MAPPING.md "Type Transformation Matrix"

### Display Formatting
- DIAGNOSTIC_SCRIPTS.md "Check for String.format usage"
- DATA_FLOW_TYPE_MAPPING.md "FLOW 3: Displaying Amounts"
- COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 3 "Possibility 1"

### Line Item Update Logic
- COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 2 "Where It Happens"
- DATA_FLOW_TYPE_MAPPING.md "FLOW 2: Editing a Line Item"
- Code: CreateInvoiceViewModel.kt line 130

### Migrations
- COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 4
- DIAGNOSTIC_SCRIPTS.md "If database is corrupted"

### Testing
- COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 9

### Prevention
- COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md Section 10

---

## DOCUMENT VERSION HISTORY

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | Mar 4, 2026 | Initial comprehensive documentation |

---

## FILE LOCATIONS IN REPO

```
docs/
├── COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md  ← Primary reference
├── DIAGNOSTIC_SCRIPTS.md                    ← Operational commands
├── DATA_FLOW_TYPE_MAPPING.md               ← Technical flows
└── TROUBLESHOOTING_INDEX.md                ← This file
```

---

## QUICK REFERENCE CHEAT SHEET

```
SAFE CODE:
✓ CentsFormatter.formatCents(longValue, currencyCode)
✓ (unitPrice.toDouble() * quantity).toLong()
✓ if (it.transientId == transientId) { ... }
✓ CentsFormatter.dollarsToCents(doubleValue)

DANGEROUS CODE:
✗ String.format("%.2f", longValue)
✗ longValue * doubleValue  ← Returns Double!
✗ if (it.id == null) { ... }  ← Matches all nulls!
✗ doubleValue.toInt()  ← Truncates decimal!

DATABASE CHECKS:
✓ PRAGMA user_version;  ← Should be 24
✓ PRAGMA table_info(line_items);  ← Check affinity
✓ SELECT typeof(unitPrice) FROM line_items;  ← Should be integer

ERROR PATTERNS:
"f != java.lang.Long" → String.format with Long
"Type mismatch" → Compilation error (compile first!)
"All items same" → NULL ID collision in updateLineItem
"Migration failed" → Clear app data and reinstall
```

---

**These documents are ready for immediate use.**

Start with COMPREHENSIVE_TROUBLESHOOTING_GUIDE.md if you're new.
Use DIAGNOSTIC_SCRIPTS.md for quick commands.
Reference DATA_FLOW_TYPE_MAPPING.md for technical details.

Questions? Check Section 10 (Prevention Strategies) in the main guide.

