# 🧪 FINAL TESTING READINESS CHECKLIST

**Date:** March 21, 2026  
**Status:** Ready for QA Testing  
**Target:** Production-Ready Verification  
**Estimated Time:** 2 hours total

---

## ✅ PHASE 1: GUI FEATURE PARITY VERIFICATION (45 minutes)

### Section 1.1: Dashboard Screens
- [ ] **GUI1 Dashboard**
  - [ ] Business header displays with company name and ABN
  - [ ] Swap Business button works (top right, swap icon)
  - [ ] Pie chart shows invoice status breakdown (PAID, SENT, DRAFT)
  - [ ] Notes card visible and clickable
  - [ ] All metric cards render (Total Clients, Total Invoices, Paid, Pending, Outstanding, Overdue)
  - [ ] Invoicing Velocity card shows stacked bars:
    - [ ] Blue bars represent SENT invoices
    - [ ] Green bars represent PAID invoices
    - [ ] Shows "X sent", "X paid", "X draft" counts
  - [ ] Analytics section displays with charts (if data present)

- [ ] **GUI2 Dashboard**
  - [ ] Settings button shows in top bar (gear icon, top right)
  - [ ] Switch to GUI1 button shows in top bar (swap icon, top right)
  - [ ] Both buttons are clickable and functional
  - [ ] Quick Action buttons visible (New Customer, New Invoice)
  - [ ] Revenue metrics display correctly
  - [ ] Payment metrics display correctly
  - [ ] Risk indicators display correctly

**Expected Outcome:** Both dashboards render all components without crashes

---

### Section 1.2: Invoice Management
- [ ] **GUI1 Invoice Path**
  - [ ] Can navigate to Invoices screen (bottom nav)
  - [ ] Can view invoice list
  - [ ] Can click create invoice button
  - [ ] Invoice form displays all required fields
  - [ ] Can select customer from dropdown
  - [ ] Can save invoice successfully
  - [ ] New invoice appears in list

- [ ] **GUI2 Invoice Path**
  - [ ] Quick Action "New Invoice" button navigates to creation screen
  - [ ] Invoice creation screen matches GUI1 functionality
  - [ ] Status update menu works (if present on detail screen)
  - [ ] Can view invoice details
  - [ ] Can navigate back to dashboard

**Expected Outcome:** Both invoice creation flows work end-to-end

---

### Section 1.3: Customer Management
- [ ] **GUI1 Customer Path**
  - [ ] Can navigate to Customers screen (bottom nav)
  - [ ] Can view customer list
  - [ ] Can click add customer button
  - [ ] Customer form displays all fields
  - [ ] Can create new customer with email
  - [ ] **CRITICAL:** Can create customer WITHOUT email (tests email validation fix)
  - [ ] Can edit existing customer

- [ ] **GUI2 Customer Path**
  - [ ] Quick Action "New Customer" button navigates to creation screen
  - [ ] Customer creation screen displays all fields
  - [ ] Can create new customer with email
  - [ ] **CRITICAL:** Can create customer WITHOUT email
  - [ ] Customer list displays correctly

**Expected Outcome:** Both customer management paths work, email is optional

---

### Section 1.4: Settings & Configuration
- [ ] **GUI1 Settings**
  - [ ] Settings accessible (bottom nav or Settings button in top bar)
  - [ ] Settings button appears in top bar (gear icon)
  - [ ] Theme/Appearance section present
  - [ ] "Switch to GUI2" button visible and functional
  - [ ] Tapping "Switch to GUI2" returns to Landing Screen

- [ ] **GUI2 Settings**
  - [ ] Settings accessible via top-right gear icon
  - [ ] Theme/Appearance section present
  - [ ] Only ONE "Advanced Color Themes" option (duplicate removed ✅)
  - [ ] "Switch to GUI1" button visible and functional
  - [ ] Tapping "Switch to GUI1" returns to Landing Screen

**Expected Outcome:** Settings accessible in both GUIs, switching works bidirectionally

---

### Section 1.5: Top Bar Buttons (Both GUIs)
- [ ] **GUI1 Top Bar**
  - [ ] Settings button present (gear icon, top right)
  - [ ] Switch GUI button present (swap icon, top right)
  - [ ] Both buttons clickable
  - [ ] Settings navigates to Settings screen
  - [ ] Switch button shows Landing Screen

- [ ] **GUI2 Top Bar**
  - [ ] Settings button present (gear icon, top right)
  - [ ] Switch to GUI1 button present (swap icon, top right)
  - [ ] Both buttons clickable
  - [ ] Settings navigates to Settings screen
  - [ ] Switch button shows Landing Screen

**Expected Outcome:** Consistent button layout in both GUIs

---

## ✅ PHASE 2: DATA CONSISTENCY & STACKED BAR CHART TEST (30 minutes)

### Section 2.1: Create Test Data
- [ ] **In GUI1, create 3 invoices:**
  - [ ] Invoice #1: DRAFT status (don't send)
  - [ ] Invoice #2: SENT status (create, don't mark paid)
  - [ ] Invoice #3: PAID status (create, mark paid or manually set)

- [ ] **Create 1 test customer:**
  - [ ] Name: "Test Customer No Email"
  - [ ] NO email address (leave blank)
  - [ ] Phone/Address optional
  - [ ] **CRITICAL TEST:** This should NOT crash the app

### Section 2.2: Verify Stacked Bar Chart
- [ ] **GUI1 Dashboard - Invoicing Velocity Card**
  - [ ] Bar chart shows 14-day trend (last 14 days as bars)
  - [ ] For today's date:
    - [ ] Bottom portion: GREEN bar (represents 1 PAID invoice)
    - [ ] Top portion: BLUE bar (represents 1 SENT invoice)
    - [ ] Separate bar: No bar or small bar (0 SENT for DRAFT)
  - [ ] Counts display correctly:
    - [ ] "📊 1 sent" (SENT invoices only)
    - [ ] "✅ 1 paid" (PAID invoices only)
    - [ ] "📝 1 draft" (DRAFT invoices only)

### Section 2.3: Test Customer Without Email
- [ ] **Create Invoice using customer without email:**
  - [ ] Navigate to Create Invoice
  - [ ] Click customer dropdown
  - [ ] Select "Test Customer No Email"
  - [ ] **Should NOT crash** with "Customer email cannot be blank"
  - [ ] Can complete invoice creation

### Section 2.4: Switch Between GUIs
- [ ] Switch from GUI1 → GUI2
  - [ ] Tap "Switch to GUI2" button in top bar
  - [ ] Landing Screen appears
  - [ ] Select "Modern Experience"
  - [ ] Data persists (same invoices/customers visible in GUI2)
  - [ ] No crashes during switch

- [ ] Switch back GUI2 → GUI1
  - [ ] Tap "Switch to GUI1" button in top bar
  - [ ] Landing Screen appears
  - [ ] Select "Classic Experience"
  - [ ] Data still intact and correct
  - [ ] Dashboard metrics still correct
  - [ ] Stacked bar chart still shows green/blue correctly

**Expected Outcome:** Data consistent across both GUIs, no silent failures or crashes

---

## ✅ PHASE 3: CRITICAL FUNCTIONALITY & EDGE CASES (30 minutes)

### Section 3.1: Invoice Status Updates
- [ ] **In GUI1: Mark invoice as PAID**
  - [ ] Navigate to Invoices list
  - [ ] Click on the SENT invoice
  - [ ] Find way to mark as PAID (menu, button, etc.)
  - [ ] Mark it as PAID
  - [ ] Return to Dashboard
  - [ ] **Verify:** Pie chart updates (now shows 2 PAID instead of 1)
  - [ ] **Verify:** Stacked bar chart updates (more green bar, less blue bar)

### Section 3.2: Edge Cases
- [ ] Customer without email can be selected for invoice (✅ FIXED - verify it works)
- [ ] Can create invoice successfully with "no email" customer
- [ ] Can view empty invoice list (no invoices)
- [ ] Can view empty customer list (no customers)
- [ ] No crashes when scrolling dashboard with minimal data

### Section 3.3: Navigation Flows
- [ ] Settings → Advanced Colors → back to Settings works
- [ ] Can switch GUI from Settings without data loss
- [ ] Landing screen shows on GUI switch
- [ ] Creating invoice → viewing invoice → back to dashboard works
- [ ] Creating customer → viewing in list → back to dashboard works

**Expected Outcome:** All critical paths work without crashes

---

## ✅ PHASE 4: BUILD & RESOURCE VERIFICATION (20 minutes)

- [ ] Build APK with current settings
  - [ ] `isShrinkResources = false` is still required (don't change)
  - [ ] Build completes without errors
  - [ ] APK size is reasonable (~25-40 MB)

- [ ] Install and test signed APK on device/emulator
  - [ ] App launches successfully
  - [ ] Settings persist across restarts
  - [ ] Database encryption works (SQLCipher loads)
  - [ ] No native library crashes (libsqlcipher.so)

**Expected Outcome:** Release build verified, resource issues documented

---

## 📋 QUICK REFERENCE - KEY FIXES TO VERIFY

| Fix | Location | What to Test | Expected Result |
|-----|----------|--------------|-----------------|
| Email Validation Removed | CreateInvoiceViewModel | Create invoice with customer with NO email | Should work, no crash |
| Stacked Bar Chart | InvoicingVelocityCard | View dashboard, check bar colors | Blue=SENT, Green=PAID |
| Both Buttons Added | GUI1 + GUI2 Top Bar | Click Settings & Switch buttons | Both visible, both work |
| Duplicate Theme Removed | SettingsScreen | Go to Settings → Theme & Display | Only "Advanced Colors" shown |

---

## 📊 RESULTS SUMMARY

After testing, fill in results:

### Phase 1: GUI Parity
- **Total Checks:** 15
- **Passed:** ___
- **Failed:** ___
- **Issues Found:** (list below)

### Phase 2: Data Consistency
- **Total Checks:** 4
- **Passed:** ___
- **Failed:** ___
- **Issues Found:** (list below)

### Phase 3: Critical Functionality
- **Total Checks:** 8
- **Passed:** ___
- **Failed:** ___
- **Issues Found:** (list below)

### Phase 4: Build Verification
- **Total Checks:** 3
- **Passed:** ___
- **Failed:** ___
- **Issues Found:** (list below)

---

## 🐛 ISSUES FOUND (Document Here)

**Issue #1:**
- Screen/Feature: _______
- Steps to Reproduce: _______
- Expected: _______
- Actual: _______
- Severity: (Critical/High/Medium/Low)

**Issue #2:**
- ...

---

## ✅ SIGN-OFF

- [ ] All Phase 1 items checked
- [ ] All Phase 2 items checked
- [ ] All Phase 3 items checked
- [ ] All Phase 4 items checked
- [ ] No critical issues blocking release
- [ ] Issues documented and prioritized

**Tester Name:** _______  
**Date Tested:** _______  
**Ready for Release:** ✅ YES / ❌ NO

---

**Last Updated:** March 21, 2026  
**Created By:** AI Assistant  
**Next Steps:** After testing, create KNOWN_ISSUES.md and GUI_PARITY_MATRIX.md

