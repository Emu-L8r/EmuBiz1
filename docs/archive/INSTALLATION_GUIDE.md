# 📱 **INSTALL APK VIA ANDROID STUDIO** (Recommended)

Since `adb` is not in your system PATH, the easiest method is to use Android Studio's built-in Run feature.

---

## **METHOD 1: Android Studio (Easiest)** ✅ RECOMMENDED

### **Step 1: Open Android Studio**
- Launch Android Studio
- Open the Bizap project (if not already open)
- Navigate to File → Open Recent → Select Bizap

### **Step 2: Connect Device or Open Emulator**

**Option A: Physical Device**
- Connect Android device via USB
- Enable Developer Mode (Settings → About Phone → Tap Build Number 7 times)
- Enable USB Debugging (Settings → Developer Options → USB Debugging)
- Check that device shows in Android Studio

**Option B: Emulator**
- Open Android Studio → Device Manager
- Click "Launch" next to any emulator
- Wait for it to fully boot

### **Step 3: Run the App**
1. Click the "Run" button (green play icon) in Android Studio toolbar
   - OR press **Shift + F10** (Windows/Linux) or **Ctrl + R** (Mac)
2. Select your device/emulator from the popup
3. Click "Run"

**Android Studio will:**
- Build the APK
- Install it on your device
- Launch the app automatically

**Expected:**
- ✅ App installs and launches
- ✅ Bizap home screen appears
- ✅ App is ready for testing

---

## **METHOD 2: Manual APK Installation via File Explorer**

If you want to install manually without adb:

### **Step 1: Enable Installation from Unknown Sources**
On your device:
- Settings → Apps → Special app access
- Install unknown apps → Select your file manager
- Toggle ON "Allow from this source"

### **Step 2: Transfer APK to Device**
- Connect device via USB (File Transfer mode)
- Copy: `app/build/outputs/apk/debug/app-debug.apk`
- Paste to device storage (e.g., Downloads folder)

### **Step 3: Install on Device**
- Open file manager on device
- Navigate to where you copied the APK
- Tap the .apk file
- Select "Install"
- App installs and is ready to use

---

## **METHOD 3: ADB via Android SDK (If You Want to Use ADB)**

If you want to use the adb command:

### **Step 1: Locate adb**
Android SDK typically installs adb at:
```
C:\Users\[YourUsername]\AppData\Local\Android\sdk\platform-tools\adb.exe
```

### **Step 2: Set Full Path**
Replace `adb` with the full path:
```powershell
"C:\Users\Saucey\AppData\Local\Android\sdk\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
```

### **Step 3: If Still Doesn't Work**
You may need to add it to PATH:
1. Search: "Edit environment variables"
2. Click "Environment Variables"
3. Under "User variables", click "New"
4. Variable name: `PATH`
5. Variable value: `C:\Users\Saucey\AppData\Local\Android\sdk\platform-tools`
6. Click OK twice
7. Restart PowerShell
8. Try: `adb install -r app/build/outputs/apk/debug/app-debug.apk`

---

## **⚡ QUICK SUMMARY**

| Method | Difficulty | Time | Notes |
|--------|-----------|------|-------|
| **Android Studio Run** | Easy | 1-2 min | ✅ Recommended - Automatic |
| **Manual APK Install** | Medium | 3-5 min | Works without adb/Studio |
| **ADB Command** | Medium | 2-3 min | Need to set up PATH first |

---

## **WHAT TO DO NOW**

### **Recommended: Use Android Studio**
1. Open Android Studio
2. Select your device/emulator
3. Click "Run" (green play button)
4. Wait for app to install and launch

### **Then Proceed to STEP 2: Run the 4 Tests**
See `QUICK_TEST_REFERENCE.md` for the test procedures.

---

## **TROUBLESHOOTING**

### **"Device not found" in Android Studio**
```
1. Check device is actually connected
2. Enable USB Debugging on device
3. Unplug/replug USB cable
4. Restart Android Studio
```

### **"Installation failed"**
```
1. Uninstall old version: Settings → Apps → Remove Bizap
2. Try installing again
3. If still fails, check device storage space
```

### **App crashes on launch**
```
1. Check logcat in Android Studio for error message
2. Take screenshot of error
3. Report back with the error
```

---

**Status:** ✅ Ready to install  
**Next:** Choose method above and install!

