# 🧪 Email Validation Fix - Testing Guide

## Quick Test Scenarios

### Scenario 1: Create First Customer Without Email (Should FAIL)
1. Open "New Customer" screen
2. Enter Name: `"Test Customer 1"`
3. Leave Email blank
4. Click "Create Customer"
5. **Expected:** Red error message appears under Email field: `"Email is required"`
6. **Button:** Disabled (grayed out)
7. **Result:** ✅ PASS - No database call made

---

### Scenario 2: Create First Customer With Valid Email (Should SUCCEED)
1. Open "New Customer" screen
2. Enter Name: `"Test Customer 1"`
3. Enter Email: `"test1@example.com"`
4. Click "Create Customer"
5. **Expected:** Success notification and return to customer list
6. **Result:** ✅ PASS - Customer appears in list

---

### Scenario 3: Create Second Customer Without Email (Should FAIL)
1. Open "New Customer" screen
2. Enter Name: `"Test Customer 2"`
3. Leave Email blank
4. Click "Create Customer"
5. **Expected:** Red error message: `"Email is required"`
6. **Button:** Create button does NOT get disabled - user sees immediate feedback
7. **Result:** ✅ PASS - Silent failure problem is SOLVED

---

### Scenario 4: Create Second Customer With Duplicate Email (Should FAIL)
1. Open "New Customer" screen
2. Enter Name: `"Test Customer 2"`
3. Enter Email: `"test1@example.com"` (same as first customer)
4. Click "Create Customer"
5. **Expected:** Snackbar at bottom with error: `"Email address is already in use. Please use a different email."`
6. **Button:** Shows loading spinner briefly then stops
7. **Result:** ✅ PASS - User informed why creation failed

---

### Scenario 5: Invalid Email Format (Should FAIL)
1. Open "New Customer" screen
2. Enter Name: `"Test Customer 3"`
3. Enter Email: `"invalidemail"` (no @ or domain)
4. Click "Create Customer"
5. **Expected:** Red error message: `"Please enter a valid email address (e.g., user@example.com)"`
6. **Result:** ✅ PASS - Format validation works

---

### Scenario 6: Valid Email with Missing Name (Should FAIL)
1. Open "New Customer" screen
2. Leave Name blank
3. Enter Email: `"test2@example.com"`
4. Click "Create Customer"
5. **Expected:** Red error message under Name field: `"Name is required"`
6. **Result:** ✅ PASS - Name validation still works

---

### Scenario 7: Clear Errors When User Types (Should WORK)
1. Open "New Customer" screen
2. Click "Create Customer" with both fields empty
3. **Expected:** Red error messages appear
4. Start typing in Email field: `"test3@"`
5. **Expected:** Red error under Email field DISAPPEARS as user types
6. Complete email: `"test3@example.com"`
7. **Expected:** Error stays cleared
8. Click "Create Customer"
9. **Expected:** Success (if name is filled in)
10. **Result:** ✅ PASS - Real-time error clearing works

---

## 🔍 Verification Checklist

### UI Elements
- [ ] Email label shows asterisk: `"Email *"`
- [ ] Email field shows red border when error exists
- [ ] Error text appears in red below email field
- [ ] Error clears when user starts typing
- [ ] Snackbar appears for database errors (bottom of screen)

### Validation Behavior
- [ ] First customer without email → blocked at UI
- [ ] Second customer without email → blocked at UI (NOT silently failing)
- [ ] Duplicate email → shows error message
- [ ] Invalid format (no @) → shows format error
- [ ] Invalid format (no .) → shows format error
- [ ] Valid email → proceeds to database

### Error Messages
- [ ] `"Email is required"` - appears when blank
- [ ] `"Please enter a valid email address (e.g., user@example.com)"` - appears for format errors
- [ ] `"Email address is already in use. Please use a different email."` - appears for duplicates
- [ ] All error messages are customer-facing (no technical jargon)

---

## 🔧 Debug Tips

### View Detailed Logs
```bash
adb logcat | grep CreateCustomerViewModelV2
adb logcat | grep CustomerRepositoryImpl
```

### Expected Log Output (Success Case)
```
CreateCustomerViewModelV2: Creating customer Test Customer 1
CreateCustomerViewModelV2: Customer created successfully with ID 5
```

### Expected Log Output (Validation Failure)
```
CreateCustomerViewModelV2: Email validation failed - creating customer with blank email
```

### Expected Log Output (Duplicate Email)
```
UNIQUE constraint violation on email: test1@example.com
```

---

## 📊 Test Results Template

| Test Scenario | Expected Result | Actual Result | Status |
|---|---|---|---|
| 1. No email, first time | Error shown | | |
| 2. Valid email, first time | Success | | |
| 3. No email, second time | Error shown | | |
| 4. Duplicate email | Error shown | | |
| 5. Invalid format | Error shown | | |
| 6. Missing name | Error shown | | |
| 7. Error clearing | Errors clear | | |

---

## ✅ Sign-Off

- [ ] All 7 scenarios tested
- [ ] All error messages appear correctly
- [ ] No silent failures observed
- [ ] Snackbar notifications work
- [ ] Real-time validation works
- [ ] Ready for production

---

**Test Date:** _____________  
**Tested By:** _____________  
**Notes:** ________________________________________________________________


