# 🎯 QUICK REFERENCE - DEVICE TESTING

## **INSTALLATION COMMAND**

### **Option 1: ADB Command Line**
```bash
adb devices                  # Verify device is connected
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### **Option 2: Android Studio**
- Open Android Studio
- Click "Run" or press Shift+F10
- Select your device
- App installs automatically

---

## **THE 4 TESTS YOU NEED TO RUN**

### **Test 1: Customer Creation** ⏱️ ~3 min
```
1. Tap Customers tab
2. Tap + button
3. Enter: Name, Email, Phone
4. Tap Create

Expected: ✅ Customer saved & appears in list
Problem: ❌ Error shown or nothing happens
```

### **Test 2: Invoice Creation** ⏱️ ~4 min
```
1. Tap Invoices tab
2. Tap + button
3. Select customer
4. Add line item
5. Tap Save

Expected: ✅ Invoice created with number & appears
Problem: ❌ Error or doesn't save
```

### **Test 3: Database Migration** ⏱️ ~1 min
```
Happens automatically on app launch

Expected: ✅ App launches, all data visible, no crashes
Problem: ❌ App crashes or data missing
```

### **Test 4: Form Validation** ⏱️ ~3 min
```
Try these:
1. Invalid email: "notanemail"
2. Blank name: (leave empty)

Expected: ✅ Error shows, NOT saved
Problem: ❌ Invalid data accepted
```

---

## **IF SOMETHING FAILS**

### **Capture Error:**
```bash
adb logcat | findstr "bizap"
```

### **Report Back With:**
- Which test failed?
- What was the error message?
- Any logcat output?

---

## **AFTER TESTING - REPORT FORMAT**

```
✅ Build succeeded? YES/NO
✅ Tests passed? YES/NO
✅ APK installed? YES/NO

Test Results:
- Test 1 (Customer): ✅ PASS / ❌ FAIL
- Test 2 (Invoice): ✅ PASS / ❌ FAIL
- Test 3 (Migration): ✅ PASS / ❌ FAIL
- Test 4 (Validation): ✅ PASS / ❌ FAIL

Errors: [Paste any error messages here]
```

---

## **KEY FILES**

- **APK Location:** `app/build/outputs/apk/debug/app-debug.apk`
- **APK Size:** 23.83 MB
- **App Package:** com.emul8r.bizap
- **Min Android:** 8.0 (API 26)
- **Target Android:** 15 (API 35)

---

## **TIME ESTIMATE**

- Installation: 2-3 min
- All 4 Tests: 12-14 min
- Reporting: 5 min
- **TOTAL: ~20 minutes**

---

## **STATUS**

✅ Code: READY
✅ Build: SUCCESS
✅ Tests: PASSING
✅ APK: CREATED
✅ Device Testing: READY TO START

**🚀 GO TEST IT!**

