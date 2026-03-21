# 🗂️ ISSUES TRACKER — All Problems Identified (March 21, 2026)

**Total Issues Found:** 15  
**Critical (Must Fix):** 3  
**High (Should Fix Soon):** 5  
**Medium (Fix Before Production):** 7  

---

## 🔴 CRITICAL ISSUES (Blocks Testing)

| # | Issue | File | Symptom | Fix Time | Priority |
|---|-------|------|---------|----------|----------|
| C1 | LineItemsEditor wrapper ViewModel injection | `ui/components/LineItemsEditor.kt:42` | ClassCastException on invoice creation | 20 min | 🔴 NOW |
| C2 | CurrencySelector wrapper injection | `ui/components/CurrencySelector.kt:19` | ClassCastException on currency select | 10 min | 🔴 NOW |
| C3 | InvoiceCustomizationEditor wrapper injection | `ui/components/InvoiceCustomizationEditor.kt:20` | ClassCastException on customization | 10 min | 🔴 NOW |

**Subtotal Critical:** 40 minutes

---

## 🟡 HIGH ISSUES (Risk of Runtime Crashes)

| # | Issue | File | Symptom | Fix Time | Priority |
|---|-------|------|---------|----------|----------|
| H1 | PhotoAttachmentPicker wrapper injection | `ui/components/PhotoAttachmentPicker.kt:19` | ClassCastException on photo attachment | 10 min | 🟡 High |
| H2 | Null pointer: LineItems without validation | `ui/components/LineItemsEditor.kt` | Crash if items list is empty | 20 min | 🟡 High |
| H3 | Null pointer: Photos without validation | `ui/components/PhotoAttachmentPicker.kt` | Crash if photos list is empty | 15 min | 🟡 High |
| H4 | Null pointer: Customer reference | `ui/gui2/invoices/CreateInvoiceScreenV2.kt` | Crash if no customer selected | 20 min | 🟡 High |
| H5 | Null pointer: Currency not initialized | `ui/components/CurrencySelector.kt` | Crash if currency is null | 15 min | 🟡 High |

**Subtotal High:** 80 minutes

---

## 🟡 MEDIUM ISSUES (Code Quality)

| # | Issue | Files | Type | Fix Time | Priority |
|---|-------|-------|------|----------|----------|
| M1 | Deprecated Icons (Icons.Filled.Send) | `ui/common/StyledCards.kt:93,155` | API Deprecation | 15 min | 🟡 Medium |
| M2 | Deprecated Icons (Icons.Filled.TrendingUp) | `ui/dashboard/DashboardScreen.kt:203` `ui/gui2/dashboard/DashboardScreenV2.kt:163` | API Deprecation | 15 min | 🟡 Medium |
| M3 | Deprecated Icons (Icons.Filled.HelpOutline) | `ui/settings/SettingsHubScreen.kt:142,270` `ui/gui2/settings/SettingsHubScreenV2.kt:153` | API Deprecation | 15 min | 🟡 Medium |
| M4 | Deprecated Icons (Icons.Filled.Notes) | `ui/dashboard/components/NotesCard.kt:44` | API Deprecation | 5 min | 🟡 Medium |
| M5 | Deprecated Icons (Icons.Filled.ArrowBack) | `ui/notes/NotesScreen.kt:48` | API Deprecation | 5 min | 🟡 Medium |
| M6 | Deprecated Divider (use HorizontalDivider) | `ui/gui2/settings/SettingsHubScreenV2.kt:135,159,197` | API Deprecation | 10 min | 🟡 Medium |
| M7 | Missing error boundary wrappers | Multiple screens | Crash handling | 90 min | 🟡 Medium |

**Subtotal Medium:** 155 minutes

---

## SUMMARY TABLE

| Severity | Count | Total Time | Blocks Progress |
|----------|-------|------------|-----------------|
| 🔴 Critical | 3 | 40 min | YES - Invoice Features |
| 🟡 High | 5 | 80 min | NO - But crash risk |
| 🟡 Medium | 7 | 155 min | NO - Code quality |
| **TOTAL** | **15** | **275 min (4.6 hrs)** | |

---

## ISSUE GROUPING BY PATTERN

### Pattern A: Wrapper Component ViewModel Injection (4 issues)
```
Problem: Wrappers trying to inject ViewModels
Solution: Pass theme as parameter instead
Time: 50 min
Impact: Critical
```

**Affected Components:**
- LineItemsEditor.kt
- CurrencySelector.kt
- InvoiceCustomizationEditor.kt
- PhotoAttachmentPicker.kt

### Pattern B: Missing Null Safety Checks (5 issues)
```
Problem: No validation before using objects
Solution: Add null checks and defaults
Time: 70 min (total for all)
Impact: High (runtime crashes)
```

**Affected Areas:**
- Line items handling
- Photo lists
- Customer selection
- Currency state
- Payment amounts

### Pattern C: Deprecated API Usage (6 issues)
```
Problem: Using old Material Design icons and components
Solution: Use AutoMirrored variants and new components
Time: 65 min
Impact: Medium (breaks in future update)
```

**Affected Components:**
- Material Icons (5 screens)
- Divider component (1 screen)

### Pattern D: Missing Error Boundaries (1 issue)
```
Problem: No graceful degradation on crash
Solution: Wrap screens in ErrorBoundary
Time: 90 min
Impact: Medium (UX improvement)
```

---

## DEPENDENCY CHAIN

```
To Restore Invoice Creation:
  └─ Fix LineItemsEditor wrapper (20 min)
     └─ Fix CurrencySelector wrapper (10 min)
     └─ Fix InvoiceCustomizationEditor wrapper (10 min)
     └─ Fix PhotoAttachmentPicker wrapper (10 min)
   TOTAL: 50 min → Unblocks 5 features

To Prevent Runtime Crashes:
  └─ Add null checks (70 min)
  └─ Fix deprecated APIs (65 min)
   TOTAL: 135 min → Prevents 5-10 crashes

To Full Production Readiness:
  └─ All above: 50 + 135 = 185 min
  └─ Add error boundaries: 90 min
  └─ Testing: 240 min
   TOTAL: 515 min (8.6 hours)
```

---

## PRIORITY ROADMAP

### ⏰ IMMEDIATE (Next 50 min)
```
[ ] Fix LineItemsEditor wrapper
[ ] Fix CurrencySelector wrapper
[ ] Fix InvoiceCustomizationEditor wrapper
[ ] Fix PhotoAttachmentPicker wrapper
[ ] Verify build succeeds
[ ] Test invoice creation works
```

### ⏰ SHORT TERM (Next 2-3 hours)
```
[ ] Add null safety checks to 5 high-risk areas
[ ] Fix deprecated icons (6 screens)
[ ] Fix deprecated divider
[ ] Rebuild and verify
```

### ⏰ MEDIUM TERM (Next 4 hours)
```
[ ] Run Phase 2.5 Task 7 manual testing (13 suites)
[ ] Document results
[ ] Fix any issues found
[ ] Production readiness check
```

### ⏰ OPTIONAL (Polish)
```
[ ] Add error boundaries
[ ] Add retry logic
[ ] Performance optimization
[ ] Code review
```

---

## TRACKING CHECKLIST

### Phase 1: Critical Fixes (Tier 1)
- [ ] LineItemsEditor wrapper fixed
- [ ] CurrencySelector wrapper fixed
- [ ] InvoiceCustomizationEditor wrapper fixed
- [ ] PhotoAttachmentPicker wrapper fixed
- [ ] Build successful
- [ ] Invoice creation works
- [ ] Invoice editing works
- [ ] Payment recording works

**Completion:** ___% (8 tasks)

### Phase 2: Safety Nets (Tier 2)
- [ ] Line items null check added
- [ ] Photos null check added
- [ ] Customer null check added
- [ ] Currency null check added
- [ ] Payment amount validation added
- [ ] Deprecated icons updated (6 screens)
- [ ] Deprecated divider fixed
- [ ] Build successful

**Completion:** ___% (8 tasks)

### Phase 3: Validation (Tier 3)
- [ ] Manual testing suite 1-3 (Classic Theme)
- [ ] Manual testing suite 4-7 (Modern Theme)
- [ ] Manual testing suite 8-10 (Theme Switching)
- [ ] Manual testing suite 11-13 (Persistence & Edge Cases)
- [ ] All tests passing
- [ ] No crashes observed
- [ ] Performance acceptable
- [ ] Production approval

**Completion:** ___% (8 tasks)

---

## FINAL STATUS

```
Date: March 21, 2026
Time: 4:30 PM
Status: 🟡 YELLOW
Score: 6.2/10

Next Milestone: 🎯 8.5/10 (Production Ready)
Estimated Time: 10-11 hours
Recommendation: Start with Phase 1 immediately
```

---

**Document Version:** 1.0  
**Last Updated:** March 21, 2026 - 16:30  
**Next Review:** After Phase 1 completion

