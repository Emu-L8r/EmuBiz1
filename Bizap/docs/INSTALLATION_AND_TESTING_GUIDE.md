# INSTALLATION & TESTING GUIDE

**App Built:** March 4, 2026  
**Status:** Ready for installation and testing  
**APK Location:** `Bizap/app/build/outputs/apk/debug/app-debug.apk`

---

## PREREQUISITE: Device/Emulator Connection

### Check if Device/Emulator is Connected:
```powershell
adb devices
```

**You should see:**
```
List of attached devices
emulator-5554          device
```

or

```
List of attached devices
device_id              device
```

### If No Device Connected:
1. **Start Android Emulator:**
   - Open Android Studio → Device Manager
   - Click play button on any emulator
   - Wait for emulator to fully boot (1-2 minutes)

2. **Or Connect Physical Device:**
   - Enable Developer Mode (tap Build Number 7 times in Settings)
   - Enable USB Debugging
   - Connect via USB cable
   - Approve the connection prompt on device

---

## INSTALLATION (2 STEPS)

### Step 1: Install the APK
```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
./gradlew :app:installDebug
```

**Expected Output:**
```
> Task :app:installDebug
Installing APK 'app-debug.apk' on 'emulator-5554'
Installed on 1 device.
```

### Step 2: Launch the App
```powershell
adb shell am start -n com.emul8r.bizap/.MainActivity
```

**Expected:** App launches on device/emulator in 2-3 seconds

---

## QUICK TEST VERIFICATION

After app launches, do these 3 quick checks (2 minutes total):

### Test 1: Business Profile (Issue 1 & 2)
```
1. Tap Settings (bottom right)
2. Tap "Business Profile" 
3. Edit "Business Name" field - type new name
4. Tap Save
5. Navigate back to Dashboard (left arrow or Dashboard tab)
6. VERIFY: Business name appears at top of Dashboard immediately
   ✓ PASS if name updated instantly (no app restart needed)
   ✗ FAIL if name doesn't show or needs restart
```

### Test 2: Line Items (Issue 3)
```
1. Tap Invoices tab
2. Tap "Create New Invoice"
3. Tap "Add Line Item"
   - Description: "Service A"
   - Quantity: 1.0
   - Unit Price: $50.00 (type 50)
4. Tap "Add Line Item" again
   - Description: "Service B"
   - Quantity: 2.0
   - Unit Price: $75.00 (type 75)
5. Edit first item: Change quantity from 1.0 to 5.5
6. VERIFY: Only first item changed, second item still shows 2.0
   ✓ PASS if only item 1 quantity changed
   ✗ FAIL if both items changed to 5.5
```

### Test 3: General Stability
```
1. Tap each bottom tab: Dashboard, Customers, Invoices, Vault, Settings
2. Check if any crashes occur
3. VERIFY: No crashes, all screens load
   ✓ PASS if all tabs work
   ✗ FAIL if any tab crashes
```

---

## DETAILED TEST PROCEDURES

Use these if you want to verify every aspect of each fix:

### ISSUE 1: BusinessProfileRepository Import Fix

**What was fixed:** 7 files now import from `domain.repository` instead of old `data.repository`

**Test:**
1. Go to Settings → Business Profile
2. Verify it loads without crashes
3. Check logcat for import errors:
   ```bash
   adb logcat | grep "Unresolved\|import\|error" | head -20
   ```
4. Should show NO "Unresolved reference: profile" errors
5. Profile data should load correctly from database

**Expected Result:** ✅ No errors, profile loads correctly

---

### ISSUE 2: Reactive activeProfile Flow

**What was fixed:** Changed from one-shot flow to reactive flow so profile edits update immediately

**Test:**
1. Go to Settings → Business Profile
2. Edit Business Name: Change "THSWA" to "THSWA Test Edit"
3. Tap Save
4. Do NOT restart or reload app
5. Tap Dashboard tab (or navigate away and back)
6. Look at top of Dashboard where business name displays
7. Verify you see "THSWA Test Edit" immediately

**Expected Result:** ✅ Name updates in real-time without app restart

**Advanced Test (Optional):**
- Edit business name 3 times
- Navigate between screens each time
- Verify each edit appears immediately
- This proves the reactive flow is working

---

### ISSUE 3: LINE ITEM NULL ID COLLISION

**What was fixed:** Changed from using `item.id` (null for all new items) to `item.transientId` (unique UUID for each item)

**Test - Part A: Editing One Item**
1. Go to Invoices → Create New Invoice
2. Add Line Item 1:
   - Description: "Item One"
   - Quantity: 1.0
   - Unit Price: 50
3. Add Line Item 2:
   - Description: "Item Two"
   - Quantity: 2.0
   - Unit Price: 75
4. Add Line Item 3:
   - Description: "Item Three"
   - Quantity: 3.0
   - Unit Price: 100
5. **CRITICAL TEST:** Tap item 1's quantity field, change 1.0 → 7.5
6. Verify screen shows:
   - Item 1: Qty 7.5 ✓
   - Item 2: Qty 2.0 ✓ (NOT 7.5)
   - Item 3: Qty 3.0 ✓ (NOT 7.5)

**Result:** ✅ PASS if only item 1 changed

---

### ISSUE 3: Part B - Editing Different Items

1. From previous screen with 3 items
2. Edit item 2 description: "Item Two" → "MODIFIED ITEM TWO"
3. Verify:
   - Item 1: "Item One" (unchanged)
   - Item 2: "MODIFIED ITEM TWO" ✓
   - Item 3: "Item Three" (unchanged)

**Result:** ✅ PASS if only item 2 changed

---

### ISSUE 3: Part C - Save & Verify

1. Complete the invoice:
   - Select a customer
   - Tap Save Invoice
2. Navigate back to invoice
3. Verify all edits were persisted correctly:
   - Item 1: Description "Item One", Qty 7.5
   - Item 2: Description "MODIFIED ITEM TWO", Qty 2.0
   - Item 3: Description "Item Three", Qty 3.0

**Result:** ✅ PASS if all data saved correctly

---

## TROUBLESHOOTING

### If App Crashes on Startup:
```bash
# Check crash logs
adb logcat -d | grep -A 10 "AndroidRuntime\|FATAL"
```
Look for import errors or null pointer exceptions related to BusinessProfileRepository.

### If Business Profile Doesn't Update:
- Could indicate reactive flow not working
- Check: Did you navigate back to Dashboard after editing?
- Try editing again and checking logcat for errors

### If Line Items Still Update Together:
- Could indicate transientId fix didn't apply
- Check: Are you editing the quantity field?
- Try creating fresh invoice with new items

### If Tests Pass Individually But Fail Together:
- Could indicate a state management issue
- Try: Restart app between tests
- Verify: All files were actually modified (check git log)

---

## LOGCAT MONITORING (Optional)

Watch real-time logs while testing:
```bash
adb logcat -s AndroidRuntime:E BizapApp:D Room:E
```

**What to look for:**
- `E AndroidRuntime` = Fatal errors (bad)
- `D BizapApp` = Debug logs (OK)
- `E Room` = Database errors (bad)

If you see many errors, test likely failed.

---

## SUCCESS CRITERIA

| Test | Status | Evidence |
|------|--------|----------|
| Issue 1 - Import Fix | ✅ | Business Profile loads without errors |
| Issue 2 - Reactive Flow | ✅ | Profile edits appear immediately on Dashboard |
| Issue 3 - Line Item Collision | ✅ | Editing one item only changes that item |
| Stability | ✅ | All 5 tabs work, no crashes |

---

## AFTER TESTING

1. **If all tests pass:**
   - Note which tests you ran
   - Delete the 3 stale files (see FILES_TO_DELETE.md)
   - Run clean build again
   - Verify BUILD SUCCESSFUL

2. **If any test fails:**
   - Note which test failed
   - Capture the logcat error output
   - Report the specific failure

---

## TIME ESTIMATE

- Installation: 30 seconds
- Quick Test (all 3): 2 minutes
- Detailed Test (all parts): 10 minutes
- Total: 3-15 minutes depending on depth

---

## QUICK COMMAND REFERENCE

```powershell
# Install and launch
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
./gradlew :app:installDebug
adb shell am start -n com.emul8r.bizap/.MainActivity

# Monitor logs
adb logcat -s AndroidRuntime:E

# Delete stale files (after testing)
Remove-Item "Bizap/app/src/main/java/com/emul8r/bizap/data/repository/BusinessProfileRepository.kt" -Force
Remove-Item "Bizap/ui/invoices/InvoiceDetailViewModel.kt" -Force

# Final verification build
./gradlew clean :app:assembleDebug
```

---

**Ready to test? Install and follow the test procedures above!**

