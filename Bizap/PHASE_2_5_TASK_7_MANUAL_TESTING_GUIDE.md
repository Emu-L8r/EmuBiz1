# 🧪 PHASE 2.5 TASK 7: Manual Testing Guide

**Date:** March 21, 2026  
**Status:** Ready for Real Device Testing  
**Duration:** 2-3 hours estimated  
**Devices Recommended:** 3+ (different manufacturers/OS versions)

---

## 📋 PRE-TESTING CHECKLIST

### Build Requirements
- [ ] Clean build succeeded: `./gradlew clean build`
- [ ] Release APK available
- [ ] APK size verified (should be ~16-17 MB after Phase 2)
- [ ] Signing verified (debug or release)

### Device Requirements
- [ ] Minimum 3 test devices (recommend 5+)
- [ ] Android API 26+ (all devices)
- [ ] At least one: Pixel (reference)
- [ ] At least one: Samsung (OneUI)
- [ ] At least one: OnePlus (stock-like)
- [ ] Recommended: One older device (API 28-30)
- [ ] Recommended: One foldable (if available)

### Pre-test Preparation
- [ ] Clear app data on all devices
- [ ] Install APK on all devices
- [ ] Note device model and Android version
- [ ] Ensure sufficient storage (>500MB free)

---

## 🧪 TEST SUITE 1: Classic Theme Feature Testing

### Test 1.1: Line Items Editor (Classic)
**Steps:**
1. Open Bizap app
2. Go to Settings → Theme → Select "Classic"
3. Navigate to Create Invoice
4. Click "Add Line Item"
5. Enter: Description="Test Widget", Qty=2, Price=50.00
6. Verify: Total shows 100.00
7. Click "Add Line Item" again
8. Enter: Description="Another Item", Qty=1, Price=30.00
9. Verify: Line items list shows 2 items
10. Click delete on first item
11. Verify: First item removed, list shows 1 item

**Expected Result:**
- ✅ Items add successfully
- ✅ Totals calculate correctly (qty × price)
- ✅ Delete removes item from list
- ✅ UI responsive, no lags

**Status:** Pass / Fail / Issues

---

### Test 1.2: Invoice Customization (Classic)
**Steps:**
1. In Create Invoice screen (Classic theme)
2. Locate "Invoice Customization" section
3. Enter: Company Name = "Test Company"
4. Enter: Header Text = "Invoice Header"
5. Enter: Footer Text = "Invoice Footer"
6. Select Template Type = "Minimal"
7. Scroll and verify fields persist
8. Switch to another screen and back
9. Verify customization fields still populated

**Expected Result:**
- ✅ All fields save input
- ✅ Template dropdown shows: standard, minimal, detailed
- ✅ Fields persist across navigation
- ✅ No data loss

**Status:** Pass / Fail / Issues

---

### Test 1.3: Currency Selection (Classic)
**Steps:**
1. In Create Invoice screen (Classic theme)
2. Locate Currency Selector
3. Click on currency dropdown (currently "AUD")
4. Verify dropdown shows 8 currencies: USD, EUR, GBP, AUD, CAD, JPY, CHF, INR
5. Select "USD"
6. Verify UI updates to show "USD"
7. Select "EUR"
8. Create invoice or navigate away
9. Return to Create Invoice
10. Verify currency still "EUR"

**Expected Result:**
- ✅ All 8 currencies available
- ✅ Selection persists
- ✅ UI updates instantly
- ✅ No crashes

**Status:** Pass / Fail / Issues

---

### Test 1.4: Photo Attachments (Classic)
**Steps:**
1. In Create Invoice screen (Classic theme)
2. Locate "Attachments" section
3. Click "Add Photo"
4. Grant camera/gallery permission if prompted
5. Take a photo or select from gallery
6. Verify: Photo thumbnail appears
7. Verify: Photos section shows 1 photo
8. Click "Add Photo" again
9. Add another photo
10. Verify: 2 photos displayed
11. Click delete (X) on first photo
12. Verify: First photo removed, 1 photo remains

**Expected Result:**
- ✅ Photos add successfully
- ✅ Thumbnails display correctly
- ✅ Delete removes photo
- ✅ Multiple photos supported
- ✅ Permissions handled correctly

**Status:** Pass / Fail / Issues

---

## 🧪 TEST SUITE 2: Modern Theme Feature Testing

### Test 2.1: Line Items Editor (Modern)
**Steps:**
1. Open Bizap app (or navigate to Settings)
2. Go to Settings → Theme → Select "Modern"
3. Navigate to Create Invoice
4. Verify: UI looks different from Classic (modern styling)
5. Click "+ Add Item" (Modern button text)
6. Enter: Description="Modern Widget", Qty=3, Price=75.00
7. Verify: Total shows 225.00
8. Verify: Items display in cards (not rows)
9. Add and delete items
10. Verify all operations work in Modern theme

**Expected Result:**
- ✅ Modern theme renders (different colors, rounded corners)
- ✅ "+ Add Item" button (Modern style)
- ✅ Items in card layout (not borders)
- ✅ All operations work identically to Classic
- ✅ Totals calculate correctly

**Status:** Pass / Fail / Issues

---

### Test 2.2: Invoice Customization (Modern)
**Steps:**
1. In Create Invoice screen (Modern theme)
2. Locate customization section (should have modern styling)
3. Verify: Rounded corners, larger padding
4. Enter: Company Name = "Modern Company"
5. Select Template = "Detailed"
6. Verify: All fields work same as Classic
7. Verify: Modern styling applied

**Expected Result:**
- ✅ Modern styling visible (colors, borders, spacing)
- ✅ Same fields as Classic
- ✅ Same functionality as Classic
- ✅ No crashes or layout issues

**Status:** Pass / Fail / Issues

---

### Test 2.3: Currency Selection (Modern)
**Steps:**
1. In Create Invoice screen (Modern theme)
2. Verify Currency Selector has modern styling
3. Click currency button (should be large, rounded)
4. Select "GBP"
5. Verify dropdown has modern styling
6. Test selecting multiple currencies
7. Verify selection persists

**Expected Result:**
- ✅ Modern button styling (large, rounded corners)
- ✅ Same 8 currencies available
- ✅ Dropdown works correctly
- ✅ Selection persists

**Status:** Pass / Fail / Issues

---

### Test 2.4: Photo Attachments (Modern)
**Steps:**
1. In Create Invoice screen (Modern theme)
2. Verify photo section has modern styling (cards instead of grid)
3. Add photos
4. Verify: Thumbnails in card layout with rounded corners
5. Verify: Delete button (X) styled for Modern theme
6. Delete and re-add photos
7. Verify all operations work

**Expected Result:**
- ✅ Photos in card layout (Modern styling)
- ✅ Rounded corners on thumbnails
- ✅ Modern delete button styling
- ✅ All operations work

**Status:** Pass / Fail / Issues

---

## 🧪 TEST SUITE 3: Theme Switching Mid-Flow

### Test 3.1: Switch Classic → Modern Mid-Edit
**Steps:**
1. Create invoice in Classic theme
2. Add line items, customization, photos
3. Go to Settings → Theme → Switch to "Modern"
4. Verify: Screen updates to Modern styling
5. Verify: All data persists (line items, photos, customization)
6. Continue editing in Modern
7. Add another line item
8. Verify: Works in Modern theme

**Expected Result:**
- ✅ Theme switches instantly (no app restart)
- ✅ All data preserved
- ✅ UI updates to Modern styling
- ✅ Can continue editing without issues
- ✅ New actions work in Modern theme

**Status:** Pass / Fail / Issues

---

### Test 3.2: Switch Modern → Classic Mid-Edit
**Steps:**
1. Create invoice in Modern theme
2. Add line items, photos, customization
3. Go to Settings → Theme → Switch to "Classic"
4. Verify: Screen updates to Classic styling
5. Verify: All data persists
6. Continue editing
7. Add more items
8. Verify: Works in Classic theme

**Expected Result:**
- ✅ Theme switches instantly
- ✅ All data preserved
- ✅ UI updates to Classic styling
- ✅ Can continue editing
- ✅ No data loss

**Status:** Pass / Fail / Issues

---

### Test 3.3: Multiple Theme Switches
**Steps:**
1. Start in Classic, add data
2. Switch to Modern (verify data)
3. Switch back to Classic (verify data)
4. Switch to Modern (verify data)
5. Repeat 3-4 times
6. Verify data integrity throughout

**Expected Result:**
- ✅ Multiple switches work
- ✅ Data never lost
- ✅ UI updates correctly each time
- ✅ No performance degradation
- ✅ No memory leaks (app stays responsive)

**Status:** Pass / Fail / Issues

---

## 🧪 TEST SUITE 4: Persistence Testing

### Test 4.1: Save and App Restart (Classic)
**Steps:**
1. Create invoice in Classic theme
2. Add: 3 line items, customization, photos
3. Click "Save" button
4. Verify: Success message or confirmation
5. Close app completely (force stop)
6. Wait 5 seconds
7. Reopen app
8. Navigate to invoice (view/edit)
9. Verify: All line items present
10. Verify: Customization data present
11. Verify: Photos present

**Expected Result:**
- ✅ Invoice saves successfully
- ✅ Data persists across restart
- ✅ Line items not lost
- ✅ Photos not lost
- ✅ Customization preserved

**Status:** Pass / Fail / Issues

---

### Test 4.2: Save and App Restart (Modern)
**Steps:**
1. Create invoice in Modern theme
2. Add: 3 line items, customization, photos
3. Click "Save" button
4. Close and restart app
5. Return to invoice
6. Verify: All data present
7. Verify: Theme still Modern
8. Edit invoice (add more items)
9. Save again
10. Close and restart
11. Verify: Updated data persists

**Expected Result:**
- ✅ Modern theme invoice saves correctly
- ✅ Data persists across restarts
- ✅ Updates persist
- ✅ Theme preference preserved

**Status:** Pass / Fail / Issues

---

### Test 4.3: Cross-Theme Persistence
**Steps:**
1. Create invoice in Classic theme
2. Add line items, photos, customization
3. Save invoice
4. Switch to Modern theme
5. Open same invoice
6. Verify: All data present and correct
7. Add more items in Modern
8. Save
9. Switch back to Classic
10. Open same invoice
11. Verify: Updated data present

**Expected Result:**
- ✅ Data created in Classic accessible in Modern
- ✅ Data created in Modern accessible in Classic
- ✅ No data loss between themes
- ✅ Updates persist across themes

**Status:** Pass / Fail / Issues

---

## 🧪 TEST SUITE 5: Edge Cases & Error Handling

### Test 5.1: Maximum Items
**Steps:**
1. Create invoice with 50+ line items
2. Verify: App doesn't crash
3. Verify: Scroll works
4. Verify: Calculations still correct
5. Try to save
6. Verify: Saves successfully or shows error gracefully

**Expected Result:**
- ✅ No crash with many items
- ✅ Scrolling works
- ✅ Calculations accurate
- ✅ Save works or shows error

**Status:** Pass / Fail / Issues

---

### Test 5.2: Empty Fields
**Steps:**
1. Try to save invoice with no line items
2. Try to save with empty required fields
3. Verify: Error message shown (don't let invalid data save)
4. Try to add line item with empty description
5. Verify: Error or validation prevents save

**Expected Result:**
- ✅ Validation prevents invalid saves
- ✅ Error messages clear
- ✅ User knows what's wrong
- ✅ App doesn't crash

**Status:** Pass / Fail / Issues

---

### Test 5.3: Permission Handling
**Steps:**
1. In photo section, click "Add Photo"
2. First time: Grant permission
3. Verify: Photo picker opens, can select photo
4. Revoke camera/gallery permission in Settings
5. Try to add photo again
6. Verify: Permission request shown (handled gracefully)

**Expected Result:**
- ✅ Permissions requested correctly
- ✅ Denied permissions handled gracefully
- ✅ No crashes when permission denied
- ✅ User can retry or go back

**Status:** Pass / Fail / Issues

---

## 📊 TEST RESULT SUMMARY

### Classic Theme Tests
| Test | Result | Issues |
|------|--------|--------|
| 1.1 Line Items | Pass/Fail | |
| 1.2 Customization | Pass/Fail | |
| 1.3 Currency | Pass/Fail | |
| 1.4 Photos | Pass/Fail | |

### Modern Theme Tests
| Test | Result | Issues |
|------|--------|--------|
| 2.1 Line Items | Pass/Fail | |
| 2.2 Customization | Pass/Fail | |
| 2.3 Currency | Pass/Fail | |
| 2.4 Photos | Pass/Fail | |

### Theme Switching Tests
| Test | Result | Issues |
|------|--------|--------|
| 3.1 Classic → Modern | Pass/Fail | |
| 3.2 Modern → Classic | Pass/Fail | |
| 3.3 Multiple Switches | Pass/Fail | |

### Persistence Tests
| Test | Result | Issues |
|------|--------|--------|
| 4.1 Classic Restart | Pass/Fail | |
| 4.2 Modern Restart | Pass/Fail | |
| 4.3 Cross-Theme | Pass/Fail | |

### Edge Cases
| Test | Result | Issues |
|------|--------|--------|
| 5.1 Max Items | Pass/Fail | |
| 5.2 Validation | Pass/Fail | |
| 5.3 Permissions | Pass/Fail | |

---

## 📝 DEVICE TEST LOG

### Device 1: ______________________
- Model: 
- OS Version: 
- Start Time: 
- Result: ✅ Pass / ❌ Fail
- Issues: 
- Notes: 

### Device 2: ______________________
- Model: 
- OS Version: 
- Start Time: 
- Result: ✅ Pass / ❌ Fail
- Issues: 
- Notes: 

### Device 3: ______________________
- Model: 
- OS Version: 
- Start Time: 
- Result: ✅ Pass / ❌ Fail
- Issues: 
- Notes: 

---

## 🎯 SUCCESS CRITERIA

**Phase 2.5 Manual Testing Complete When:**

✅ All 13 test suites executed  
✅ Tested on 3+ different devices  
✅ Tested on 2+ different Android versions  
✅ Tested both Classic and Modern themes  
✅ Theme switching verified  
✅ Persistence verified  
✅ No critical crashes  
✅ All features functional  

---

## 📝 FINAL CHECKLIST

- [ ] 13 test suites executed
- [ ] 3+ devices tested
- [ ] 2+ Android versions tested
- [ ] All tests passed or issues documented
- [ ] No critical bugs found
- [ ] Features work as expected
- [ ] Performance acceptable
- [ ] Ready for production

---

## 🚀 IF ALL TESTS PASS

✅ Phase 2.5 complete  
✅ Phase 2 complete (95% → 100%)  
✅ Ready for production release  
✅ GUI2 feature parity achieved  
✅ Theme-aware architecture verified  

**Timeline:** Ready to merge and release!

---

## ❌ IF ISSUES FOUND

1. Document issue clearly
2. Assign severity: Critical/High/Medium/Low
3. Create bug fix PR
4. Re-test on same device
5. Verify fix works
6. Proceed with release

---

**TASK 7: Manual Testing is now READY TO EXECUTE**

**Good luck! 🚀**

