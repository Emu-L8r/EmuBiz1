# EmuBiz Bizap - Testing Checklist

## Build Status
✅ **Build Successful**
- APK: `app/build/outputs/apk/debug/app-debug.apk`
- Kotlin: 2.0.21
- KSP: 2.0.21-1.0.27
- Build Time: 44 seconds
- Status: Ready for Device Testing

## Code Changes Verified
✅ All architectural fixes committed:
- DocumentExportService injection removed
- exportToDownloadsQ() method deleted
- pdfUri field added to Invoice domain model
- Domain/entity mappings updated across UI screens
- Duplicate formatDate() removed

## Testing Phases

### Phase 1: App Launch & Stability
- [ ] Install APK on device/emulator: `adb install -r app-debug.apk`
- [ ] App launches without crash
- [ ] No ANR (Application Not Responding) errors
- [ ] UI renders correctly
- [ ] Logcat shows no ERROR level messages on startup

### Phase 2: Customer Management (CRUD)
- [ ] **Create**: Add new customer with all fields
  - [ ] Name (required)
  - [ ] Business Name (optional)
  - [ ] Email (optional)
  - [ ] Phone (optional)
  - [ ] Address (optional)
- [ ] **Read**: View customer list
  - [ ] All customers display
  - [ ] Can click to view details
- [ ] **Update**: Edit customer information
  - [ ] Changes persist after save
  - [ ] Changes visible in list
- [ ] **Delete**: Remove a customer
  - [ ] Customer removed from list
  - [ ] No orphaned invoices

### Phase 3: Invoice Management (CRUD)
- [ ] **Create**: New invoice
  - [ ] Select customer
  - [ ] Add line items (description, qty, price)
  - [ ] Calculate totals correctly
  - [ ] Add optional fields (header, subheader, notes, footer)
  - [ ] Save successfully
- [ ] **Read**: View invoice list
  - [ ] All invoices display with correct totals
  - [ ] Status shows correctly (DRAFT, SENT, PAID)
  - [ ] Click to view details
- [ ] **Update**: Edit invoice (if implemented)
  - [ ] Changes persist
  - [ ] Totals recalculate
- [ ] **Delete**: Remove invoice (if implemented)
  - [ ] Invoice removed from list

### Phase 4: Invoice Details & PDFs
- [ ] Open invoice detail screen
  - [ ] All invoice data displays
  - [ ] Line items list shows correctly
  - [ ] Total amount calculates correctly
- [ ] **PDF Generation**
  - [ ] Generate PDF button works
  - [ ] PDF file created at expected location
  - [ ] PDF contains invoice data
- [ ] **PDF Sharing**
  - [ ] Share button works
  - [ ] Can select email/messaging app
- [ ] **PDF Viewing**
  - [ ] View saved PDF button works
  - [ ] Opens in PDF viewer

### Phase 5: Navigation & State
- [ ] Navigate Customers → Invoices → Documents
  - [ ] No black screens
  - [ ] No freezing/lag
  - [ ] Back navigation works
- [ ] **State Persistence**
  - [ ] Close app completely
  - [ ] Reopen app
  - [ ] All data (customers, invoices) persists
  - [ ] No data loss

### Phase 6: Data Integrity
- [ ] Create invoice with multiple line items
  - [ ] Total = sum of (quantity × price) for all items
  - [ ] Matches display in list and detail screens
- [ ] Customer linked to invoices
  - [ ] Cannot delete customer with active invoices (or handle gracefully)
  - [ ] Invoice shows correct customer name

## Known Issues to Watch For
1. Deprecated Compose APIs (warnings only, not blocking)
   - SearchBar in DocumentVaultScreen
   - menuAnchor() in CreateInvoiceScreen

2. Native library stripping warnings (non-critical)
   - "Unable to strip: libandroidx.graphics.path.so"

## Version Compatibility Note
**Kotlin 2.1.0 + KSP 2.1.0-1.0.29 incompatible with Hilt 2.52**
- Verified through two independent builds
- Current versions (2.0.21 / 2.0.21-1.0.27) are stable
- Will upgrade when Hilt 2.53+ released

## Reporting Format
When reporting test results, use:

```
✅ Phase 1: PASSED / ❌ FAILED
✅ Phase 2: PASSED / ❌ FAILED
   - Issue: [Description]
   - Steps to reproduce: [Steps]
✅ Phase 3: PASSED / ❌ FAILED
✅ Phase 4: PASSED / ❌ FAILED
✅ Phase 5: PASSED / ❌ FAILED
✅ Phase 6: PASSED / ❌ FAILED

Logcat Errors (if any):
[Paste relevant ERROR level messages]
```

---

**Status**: Ready for Testing
**Date**: 2026-02-27
**APK Location**: `app/build/outputs/apk/debug/app-debug.apk`

