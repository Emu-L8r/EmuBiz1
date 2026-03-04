# INSTALLATION & TESTING INSTRUCTIONS

**App Status:** Built and ready for installation  
**APK Location:** `Bizap/app/build/outputs/apk/debug/app-debug.apk`

---

## STEP 1: PREPARE YOUR DEVICE/EMULATOR

### Option A: Start Android Emulator
1. Open Android Studio
2. Go to Device Manager
3. Select any emulator and click the play ▶️ button
4. Wait for emulator to fully boot (2-3 minutes)

### Option B: Connect Physical Device
1. Enable Developer Mode (tap Build Number 7 times in Settings → About)
2. Enable USB Debugging (Settings → Developer Options)
3. Connect via USB cable
4. Approve the connection prompt on device

### Verify Connection:
```powershell
adb devices
```
**You should see:**
```
List of attached devices
emulator-5554          device
```

---

## STEP 2: INSTALL THE APP

```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
./gradlew :app:installDebug
```

**Expected output:**
```
> Task :app:installDebug
Installing APK 'app-debug.apk' on 'emulator-5554'
Installed on 1 device.
```

---

## STEP 3: LAUNCH THE APP

```powershell
adb shell am start -n com.emul8r.bizap/.MainActivity
```

**Expected:** App launches within 2-3 seconds

---

## STEP 4: REVIEW THE UI/UX CHANGES

### Test 1: Check for Single Header (CRITICAL)
**What to do:**
1. Look at the top of the screen
2. Navigate to different tabs: Dashboard → Customers → Invoices → Vault → Settings
3. Open detail screens: Create Invoice, Edit Customer, etc.

**Expected:**
- ✅ Exactly ONE header visible at the very top
- ✅ Header shows app name/current screen
- ✅ No duplication or stacking

**If you see:**
- ❌ TWO headers stacked = FAILED (this was the bug we fixed)

### Test 2: Theme Color Consistency
**What to do:**
1. Navigate to Settings → App Appearance
2. Change Seed Color (e.g., from purple to blue)
3. Navigate back to Dashboard and other screens
4. Observe the header color

**Expected:**
- ✅ Header immediately changes to new color
- ✅ All UI elements reflect the theme color
- ✅ Color consistent across all screens
- ✅ Changing theme updates all screens in real-time

### Test 3: Dark Mode Works
**What to do:**
1. Settings → App Appearance
2. Toggle Dark Mode ON
3. Navigate through all screens

**Expected:**
- ✅ All text is readable
- ✅ Good contrast between text and background
- ✅ No white text on white background
- ✅ No black text on black background
- ✅ UI elements visible in dark mode

### Test 4: All Features Still Work
**What to do:**

**Invoices:**
1. Click Invoices tab
2. Click Create Invoice button (blue +)
3. Select a customer
4. Add some line items
5. Save

**Expected:** Invoice saves without errors ✅

**Customers:**
1. Click Customers tab
2. Click a customer to view details
3. Click Edit
4. Change a field (e.g., email)
5. Save

**Expected:** Changes save without errors ✅

**Settings:**
1. Click Settings tab
2. Go to Business Profile
3. Change business name
4. Go back to Dashboard

**Expected:** Name change appears immediately ✅

**Documents:**
1. Click Vault tab
2. Try searching with the search field

**Expected:** Search works, documents filter correctly ✅

### Test 5: Navigation Buttons Work
**What to do:**
1. Navigate into various detail screens
2. Click back buttons/navigation
3. Try tapping back button on device

**Expected:**
- ✅ Back navigation works correctly
- ✅ No crashes
- ✅ Proper screen transitions

---

## STEP 5: CHECK FOR ISSUES

### Look for:
- ❌ Crashes (dialog: "Unfortunately Bizap has stopped")
- ❌ Double headers (main issue we fixed)
- ❌ Unreadable text (bad contrast)
- ❌ Missing UI elements
- ❌ Color inconsistencies

### Monitor Logcat for Errors:
```powershell
adb logcat | grep -E "AndroidRuntime|ERROR" | head -20
```

---

## WHAT TO EXPECT

### Visual Improvements:
✅ **Single, clean header** on every screen  
✅ **Consistent theme colors** throughout the app  
✅ **Proper dark mode** support with good contrast  
✅ **Professional typography** using Material 3 standards  
✅ **No visual duplications or overlaps  

### Functional Improvements:
✅ **All CRUD operations** (Create, Read, Update, Delete) work  
✅ **All navigation** buttons work correctly  
✅ **All snackbars** and messages appear  
✅ **All FABs** (floating action buttons) work  
✅ **Search functionality** works (DocumentVault)  

### No Regressions:
✅ **No new crashes**  
✅ **No data loss**  
✅ **All previous features** still work  
✅ **All integrations** (Firebase, Room, etc.) still work  

---

## TROUBLESHOOTING

### If APK Installation Fails:
```powershell
# Uninstall and try again
adb uninstall com.emul8r.bizap
./gradlew :app:installDebug
```

### If App Crashes on Launch:
```powershell
# Check crash logs
adb logcat | grep -E "AndroidRuntime|FATAL" | head -30
```

### If You See Double Headers:
This means the fix didn't apply. Report which screen shows this.

### If Theme Colors Don't Change:
1. Restart the app
2. Go to Settings → App Appearance again
3. Change the color again
4. Return to previous screen

If still not working, check logcat for theme-related errors.

---

## COMPLETION CHECKLIST

- [ ] App installs without errors
- [ ] App launches without crashes
- [ ] Single header visible on all screens
- [ ] Theme color changes apply to all screens
- [ ] Dark mode works correctly
- [ ] Create/Edit/Delete operations work
- [ ] Navigation works correctly
- [ ] No double headers anywhere
- [ ] No hardcoded colors visible
- [ ] All features functional

---

## READY TO TEST!

Once you've completed the installation steps above, the app is ready for review. 

**Report back with:**
1. Which test(s) passed/failed
2. Any visual issues observed
3. Any crashes or errors
4. Overall impression of the UI improvements

---

**Build Date:** March 4, 2026  
**Changes:** Complete UI/UX overhaul (16 screens, 3 theme files updated)  
**Status:** ✅ READY FOR TESTING

