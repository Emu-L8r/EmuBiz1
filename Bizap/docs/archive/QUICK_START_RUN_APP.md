# 🚀 **QUICK START: RUN THE APP**

**This guide will get your app running in 10 minutes**

---

## **PREREQUISITES**

Make sure you have:
- ✅ Android Studio installed
- ✅ Android SDK 35 installed (via SDK Manager)
- ✅ Device or emulator available
- ✅ `EXCHANGE_RATE_API_KEY` environment variable set (optional, can use dummy value)

---

## **STEP 1: BUILD THE APP (5 minutes)**

Open Terminal/PowerShell and run:

```bash
cd C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap

# Clean and build
./gradlew clean assembleDebug

# You should see:
# BUILD SUCCESSFUL in X seconds
```

**APK Location:** `app/build/outputs/apk/debug/app-debug.apk`

---

## **STEP 2: INSTALL ON DEVICE/EMULATOR (3 minutes)**

### **Option A: Via Android Studio (Easiest)**
1. Open Android Studio
2. Click "Run" button (green play icon)
3. Select device/emulator
4. App installs and launches automatically

### **Option B: Via Command Line**
```bash
# See connected devices
adb devices

# Install APK
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.emul8r.bizap/.MainActivity
```

### **Option C: Manual APK Install**
1. Find the APK file
2. Double-click it (on emulator)
3. Follow prompts to install

---

## **STEP 3: TEST THE APP (2 minutes)**

Once app launches, you should see:

### **Dashboard Screen**
- Welcome message or empty state
- Bottom navigation with tabs

### **Try These Features**

#### **Create a Customer**
```
1. Tap "Customers" tab
2. Tap "+" button
3. Enter name, email, phone
4. Tap "Save Customer"
✅ Customer appears in list
```

#### **Create an Invoice**
```
1. Tap "Invoices" tab
2. Tap "+" button
3. Select customer
4. Add line items
5. Click "Save Invoice"
✅ Invoice appears in list
```

#### **Record Payment** (NEW - Just Fixed! ✅)
```
1. Open an invoice
2. Tap "Record Payment"
3. Enter amount
4. Tap "Save"
✅ Amount updates without error
```

#### **Edit Invoice** (NEW - Just Fixed! ✅)
```
1. Open an invoice
2. Tap "Edit"
3. Change amount or items
4. Tap "Save Invoice"
✅ Changes saved without error
```

#### **Change Status** (NEW - Just Fixed! ✅)
```
1. Open an invoice (status = DRAFT)
2. Tap status dropdown
3. Change to "SENT" or "PAID"
4. Tap "Save"
✅ Status updates without error
```

---

## **STEP 4: VERIFY EVERYTHING WORKS**

### **All Features Should Work:**

✅ Dashboard tab loads  
✅ Create customer succeeds  
✅ Create invoice succeeds  
✅ Add multiple line items (NEW FIX)  
✅ Record payment works (NEW FIX)  
✅ Edit invoice works (NEW FIX)  
✅ Change status works (NEW FIX)  
✅ Delete invoice works  
✅ View settings  
✅ No crashes or errors  

---

## **IF YOU SEE ERRORS**

### **"App keeps crashing"**
```
1. Check logcat output
2. Most common: Hilt setup issue
3. Solution: Clean and rebuild
   ./gradlew clean build
```

### **"Payment recording fails"**
This was just fixed! Make sure you have the latest code:
```bash
git pull origin main
./gradlew clean assembleDebug
```

### **"Edit invoice fails"**
This was also just fixed! Same solution as above.

### **"Multiple line items cause error"**
Also fixed! Pull latest and rebuild.

---

## **WHAT YOU'LL LEARN**

After running the app, you'll see:
- ✅ All features implemented
- ✅ Clean Material 3 design
- ✅ Smooth user experience
- ✅ Proper error handling
- ✅ Real-time updates
- ✅ Professional quality

---

## **LOGS & DEBUGGING**

### **View App Logs**
```bash
# See all logs
adb logcat | grep -i bizap

# See just errors
adb logcat | grep -i error

# Save logs to file
adb logcat > logs.txt
```

### **Debug in Android Studio**
1. Set breakpoints in code
2. Tap "Debug" instead of "Run"
3. App pauses at breakpoints
4. Inspect variables

---

## **PERFORMANCE TIPS**

### **Faster Development**
```bash
# Just build and install (skip clean)
./gradlew assembleDebug

# Run tests in parallel
./gradlew test --parallel

# Build with specific tasks
./gradlew :app:assembleDebug
```

### **Device Performance**
- Emulator: Use x86_64, not ARM
- Phone: Use USB cable (faster than WiFi)
- RAM: Allocate 4GB+ to emulator

---

## **NEXT STEPS**

After verifying the app works:

### **1. Test on Multiple Devices**
- ✅ Emulator (development)
- ✅ Phone (real device)
- ✅ Different Android versions

### **2. Perform Beta Testing**
- Invite real users
- Collect feedback
- Fix reported issues

### **3. Prepare for Release**
- Fix the 3 small issues (35 minutes)
- Create README.md
- Fix icon deprecations
- Submit to Play Store

### **4. Monitor in Production**
- Firebase Crashlytics tracks errors
- Analytics show usage patterns
- Timber logs help debugging

---

## **TROUBLESHOOTING CHECKLIST**

If something doesn't work:

```
[ ] Clean and rebuild
    ./gradlew clean build

[ ] Check device is connected
    adb devices

[ ] Check logcat for errors
    adb logcat

[ ] Verify API key is set
    echo %EXCHANGE_RATE_API_KEY%

[ ] Try different device
    
[ ] Clear app data
    adb shell pm clear com.emul8r.bizap

[ ] Reinstall from scratch
    adb uninstall com.emul8r.bizap
    ./gradlew clean assembleDebug
    adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## **SUCCESS INDICATORS**

You'll know it's working when:

✅ **App Launches**
- Splash screen appears
- Dashboard screen loads
- Bottom navigation visible

✅ **Features Work**
- Tapping tabs switches screens
- Creating items succeeds
- List updates in real-time

✅ **No Errors**
- No crash dialogs
- Logcat shows no errors
- All features respond smoothly

✅ **Design Looks Good**
- Material 3 theme visible
- Dark mode works (if enabled)
- Text and icons clear
- Buttons respond to taps

---

## **WHAT TO LOOK FOR**

### **UI/UX**
- Clean, professional design
- Smooth animations
- Clear error messages
- Success confirmations

### **Functionality**
- All buttons work
- Data persists
- Numbers calculate correctly
- Status changes reflect

### **Performance**
- App launches quickly
- Transitions are smooth
- No lag when scrolling
- Database queries fast

---

## **QUICK REFERENCE**

```bash
# Build
./gradlew clean assembleDebug

# Test
./gradlew testDebugUnitTest

# Run from Android Studio
- Click green play button
- Select device
- Done!

# Install manually
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.emul8r.bizap/.MainActivity

# View logs
adb logcat | grep bizap

# Clear data
adb shell pm clear com.emul8r.bizap
```

---

## **SUPPORT**

If you get stuck:

1. Check the detailed health check report: `PROJECT_HEALTH_CHECK_MARCH_6_2026.md`
2. Review the error testing guide: `ERROR_TESTING_GUIDE.md`
3. Check logcat output for specific errors
4. Try the troubleshooting checklist above

---

**Ready to see your app in action? Let's go!** 🚀

Built with ❤️ using Android, Kotlin, and Jetpack Compose

