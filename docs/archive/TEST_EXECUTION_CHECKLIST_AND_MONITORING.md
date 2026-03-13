# 🎯 TEST EXECUTION CHECKLIST & LOG MONITORING GUIDE

**Date**: March 8, 2026  
**Status**: ✅ **READY FOR EMULATOR TESTING**

---

## PART 1: PRE-TEST SETUP

### **Step 1: Install APK**
```bash
# Uninstall if exists
adb uninstall com.emul8r.bizap

# Install latest debug APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Verify installation
adb shell pm list packages | grep bizap
```

### **Step 2: Prepare Logcat Monitoring**
```bash
# Clear logcat
adb logcat -c

# Start monitoring (keep this terminal open)
adb logcat | grep -E "InvoiceDetailViewModel|OfflineQueue|Payment|❌|✅|📶"
```

### **Step 3: Launch App**
```bash
adb shell am start -n com.emul8r.bizap/.MainActivity
```

### **Step 4: Setup Test Data**
- Create test customer "Test Customer A"
- Create test invoice for A$100.00
- Status: DRAFT
- Save invoice

---

## PART 2: QUICK TEST EXECUTION (5 minutes)

### ✅ **Quick Test: Basic Payment Flow**

**Objective**: Verify core payment validation works

**Steps**:
1. Open invoice (A$100, Status: DRAFT)
2. Click "Record Payment"
3. Enter "50"
4. Click "Confirm"
5. Verify success message
6. Check Amount Paid updated to A$50.00
7. Check Outstanding updated to A$50.00

**Expected Result**: ✅ Payment recorded successfully

**Logcat Watch**: Look for message containing "Payment of 5000"

---

## PART 3: STANDARD TEST EXECUTION (15 minutes)

### ✅ **Test 1: Valid Partial Payment**
```
Duration: 2 minutes
Input: A$50 on A$100 invoice
Expected: Success ✅
Logcat: "Payment of 5000 cents recorded"
```

### ✅ **Test 2: Overpayment Rejection**
```
Duration: 1 minute
Input: A$150 on A$100 invoice
Expected: Error message ✅
Logcat: "Payment exceeds remaining balance"
```

### ✅ **Test 3: Full Payment**
```
Duration: 2 minutes
Input: A$50 (final payment on A$50 remaining)
Expected: Status → PAID ✅
Logcat: "Status updated to PAID"
```

### ✅ **Test 4: Fully Paid Protection**
```
Duration: 1 minute
Input: Try to pay already paid invoice
Expected: Dialog shows "already fully paid" ✅
Logcat: "Attempt to pay fully paid invoice blocked"
```

### ✅ **Test 5: Invalid Amount**
```
Duration: 1 minute
Input: "abc" or empty
Expected: Error message ✅
Logcat: "Invalid amount"
```

### ✅ **Test 6: Zero Amount**
```
Duration: 1 minute
Input: "0"
Expected: Error "must be greater than $0" ✅
Logcat: "Zero payment rejected"
```

### ✅ **Test 7: Real-Time Validation**
```
Duration: 2 minutes
Input: Type amounts and watch errors appear/disappear
Expected: Smooth UX ✅
Logcat: No errors during typing
```

### ✅ **Test 8: Offline Payment (Optional)**
```
Duration: 3 minutes
Setup: Toggle Airplane Mode ON
Input: Record payment while offline
Expected: Payment queued ✅
Logcat: "Offline detected. Queueing payment"
```

**Total Time**: ~15 minutes

---

## PART 4: COMPREHENSIVE TEST EXECUTION (30 minutes)

Run all tests from Part 3 PLUS:

### ✅ **Test 9: Multiple Partial Payments**
```
Duration: 3 minutes
Invoice: A$200
Payment 1: A$50 → Outstanding should be A$150
Payment 2: A$75 → Outstanding should be A$75
Payment 3: A$75 → Outstanding should be A$0, Status → PAID
Expected: All transitions correct ✅
```

### ✅ **Test 10: Status Transitions**
```
Duration: 2 minutes
Create 3 invoices with different statuses
DRAFT invoice: Record payment → Should become PARTIALLY_PAID
SENT invoice: Record payment → Should become PARTIALLY_PAID
PAID invoice: Try payment → Should show "already fully paid"
Expected: All correct ✅
```

### ✅ **Test 11: Currency Formatting**
```
Duration: 1 minute
Verify all amounts show currency correctly
Examples: A$50.00, A$100.00, A$0.00
Expected: Consistent formatting ✅
```

### ✅ **Test 12: Error Messages**
```
Duration: 2 minutes
Record various invalid payments
Check each error message is clear and helpful
Expected: User-friendly messages ✅
```

### ✅ **Test 13: UI Responsiveness**
```
Duration: 2 minutes
Tap buttons rapidly
Check for lag or crashes
Expected: Smooth operation ✅
```

### ✅ **Test 14: Success Feedback**
```
Duration: 1 minute
After valid payment, verify:
- Snackbar appears
- Message shows correct amount
- Dialog closes
- Invoice updates
Expected: All working ✅
```

### ✅ **Test 15: Edge Cases**
```
Duration: 3 minutes
Test edge cases:
- Payment of A$0.01
- Very large payment (A$999,999)
- Decimal amounts (A$50.50)
- Special characters (shouldn't be allowed)
Expected: Handled correctly ✅
```

**Total Time**: ~30 minutes

---

## PART 5: LOGCAT MONITORING CHECKLIST

### **Start Monitoring**
```bash
adb logcat | grep -E "Payment|ViewModel|Error|Offline"
```

### **Expected Log Patterns**

#### ✅ **Valid Payment**
```
[InvoiceDetailViewModel] ✅ Payment of 5000 cents recorded.
[Database] Updated amount_paid in invoices table
[Timber] Payment recorded for invoice 1
[ViewModel] Status updated: DRAFT → PARTIALLY_PAID
```

#### ❌ **Overpayment Blocked**
```
[InvoiceDetailViewModel] Validation failed: amount exceeds remaining balance
[ViewModel] Error emitted to UI: "Payment of A$150 exceeds outstanding..."
[Dialog] Error message shown: "Payment exceeds remaining balance of A$100.00"
```

#### 📶 **Offline Payment**
```
[OfflineQueueService] 📶 Offline detected. Queueing payment for sync.
[OfflineOperationDao] ✅ Payment queued for invoice 1
[Timber] Payment queued: invoiceId=1, amount=5000
[SnapshotSync] Skipped (offline)
```

#### 🔄 **Status Transition**
```
[InvoiceDetailViewModel] Recording payment for invoice 1
[ViewModel] newAmountPaid: 5000, totalAmount: 10000
[ViewModel] Status determination: newAmountPaid < totalAmount → PARTIALLY_PAID
[Database] Updated status to PARTIALLY_PAID
[Timber] ✅ Status updated: DRAFT → PARTIALLY_PAID
```

### **Error Patterns to Watch**

```
❌ If you see:
  [Exception] java.lang.NullPointerException
  → Invoice not loaded properly

❌ If you see:
  [SQLiteException] Database locked
  → Concurrent access issue

❌ If you see:
  [ViewModel] Failed to record payment
  → Repository call failed

⚠️ If you see:
  [Debug] UI layer received null
  → State management issue
```

---

## PART 6: TEST RESULT RECORDING

### **Create Test Results Document**

```markdown
# Payment Validation Test Results - March 8, 2026

## Test Set 1: Valid Payments
- [ ] Test 1.1: Partial Payment (A$50/A$100) - PASS/FAIL
- [ ] Test 1.2: Full Payment (A$75.50 exact) - PASS/FAIL
- [ ] Test 1.3: Multiple Payments - PASS/FAIL

## Test Set 2: Invalid Payments
- [ ] Test 2.1: Overpayment (A$150/A$100) - PASS/FAIL
- [ ] Test 2.2: Zero Payment (A$0) - PASS/FAIL
- [ ] Test 2.3: Invalid Format (abc) - PASS/FAIL
- [ ] Test 2.4: Negative Payment (-A$50) - PASS/FAIL

## Test Set 3: Fully Paid
- [ ] Test 3.1: Cannot pay fully paid invoice - PASS/FAIL

## Test Set 4: UI/UX
- [ ] Test 4.1: Real-time validation - PASS/FAIL
- [ ] Test 4.2: Remaining balance display - PASS/FAIL
- [ ] Test 4.3: Success message - PASS/FAIL

## Test Set 5: Status Transitions
- [ ] Test 5.1: DRAFT → PARTIALLY_PAID - PASS/FAIL
- [ ] Test 5.2: PARTIALLY_PAID → PAID - PASS/FAIL
- [ ] Test 5.3: SENT → PARTIALLY_PAID - PASS/FAIL

## Test Set 6: Offline
- [ ] Test 6.1: Record payment offline - PASS/FAIL

## Summary
- Total Tests: 15+
- Passed: __
- Failed: __
- Pass Rate: ___%
```

---

## PART 7: PASS/FAIL CRITERIA

### ✅ **PASS Criteria (All Must Be True)**

1. **Validation Accuracy**
   - ✅ Valid payments accepted
   - ❌ Invalid payments rejected
   - ✅ Outstanding calculated correctly

2. **User Experience**
   - ✅ Error messages clear
   - ✅ Real-time feedback works
   - ✅ Dialog closes on success
   - ✅ Snackbar message shown

3. **Data Integrity**
   - ✅ No negative outstanding
   - ✅ No overpayments saved
   - ✅ Amount Paid never exceeds Total
   - ✅ Status updates correctly

4. **Error Handling**
   - ✅ Invalid formats rejected
   - ✅ App doesn't crash
   - ✅ User informed of failures
   - ✅ Recovery possible

### ⚠️ **WARNING Signs (Not Pass/Fail)**

- ⚠️ Slow response time (>2 seconds)
- ⚠️ Laggy UI when entering amounts
- ⚠️ Dialog doesn't close immediately
- ⚠️ Snackbar appears but disappears quickly

### ❌ **FAIL Criteria (Any One = FAIL)**

- ❌ App crashes
- ❌ Negative outstanding saved
- ❌ Overpayment accepted
- ❌ Status doesn't update
- ❌ Invalid payment saved
- ❌ No error message shown for invalid input

---

## PART 8: TROUBLESHOOTING

### **If Tests Fail**

**Problem**: App crashes on payment
**Solution**: Check logcat for exception, rebuild APK

**Problem**: Payment shows as recorded but not visible
**Solution**: Restart app, check database directly

**Problem**: Error message not showing
**Solution**: Verify UI layer is receiving events

**Problem**: Status not updating to PAID
**Solution**: Check status update logic in ViewModel

**Problem**: Offline queue not working
**Solution**: Verify airplane mode toggled correctly, check logcat for queue messages

---

## PART 9: NEXT STEPS AFTER TESTING

### ✅ **If All Tests Pass**
1. ✅ Document all results
2. ✅ Create passing test report
3. ✅ Commit to git
4. ✅ Proceed with Phase 3 implementation
5. ✅ Begin payment scheduling feature

### ❌ **If Tests Fail**
1. ❌ Document failures with screenshots
2. ❌ Create bug report
3. ❌ Fix identified issues
4. ❌ Re-run failing tests
5. ❌ Verify fixes work

---

## QUICK COMMANDS REFERENCE

```bash
# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Start monitoring logs
adb logcat | grep -E "Payment|Error"

# Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity

# Clear app data (restart fresh)
adb shell pm clear com.emul8r.bizap

# Toggle airplane mode
adb shell settings put global airplane_mode_on 1
adb shell am broadcast -a android.intent.action.AIRPLANE_MODE

# Stop monitoring
Ctrl+C

# View database (if available)
adb shell sqlite3 /data/data/com.emul8r.bizap/databases/bizap.db
```

---

## SIGN-OFF

**Testing Guide**: ✅ **COMPLETE & READY**

**Estimated Testing Time**:
- Quick: 5 minutes
- Standard: 15 minutes
- Comprehensive: 30 minutes

**Ready to Proceed**: ✅ **YES**

**Date**: March 8, 2026


