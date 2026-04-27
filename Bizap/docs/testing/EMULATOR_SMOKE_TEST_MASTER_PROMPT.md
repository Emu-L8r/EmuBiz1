# 🎯 MASTER PROMPT FOR EMULATOR SMOKE TEST VERIFICATION

**Project:** Bizap v0.9.3-Gold-Stable-Testing  
**Date:** April 27, 2026  
**Purpose:** Confirm app launches and runs without crashes before physical device testing

---

## **TASK: Verify Bizap Emulator Smoke Test**

### **QUICK CONTEXT**
- ✅ Build warnings eliminated (19 → 0)
- ✅ All tests passing (1,229/1,229)
- ✅ Code committed (6c9c76af)
- ✅ Ready for emulator validation
- 🎯 Next: Physical device testing (April 28-30)

---

### **WHAT TO DO** (Step by step)

**Step 1: Verify Emulator Ready**
```powershell
adb devices
```
Expected: `emulator-5554  device` (or similar)

**Step 2: Install APK**
```powershell
cd "C:\Users\Saucey\Documents\GitHub\EmuBiz\Bizap"
adb install -r "app/build/outputs/apk/debug/app-debug.apk"
```
Expected: `Success`

**Step 3: Launch App**
```powershell
adb shell am start -n com.emul8r.bizap/com.emul8r.bizap.MainActivity
```
Expected: App window appears, dashboard displays

**Step 4: Monitor Logs (30 seconds)**
```powershell
adb logcat -s "BizapApp:V"
```
Look for: **NO ERROR, Exception, or CRASH messages**

**Step 5: Test Navigation (2 minutes)**
- Tap different menu items
- Navigate between screens
- Verify no crashes
- Test back button

---

### **SUCCESS CRITERIA** (ALL must pass ✅)
- [ ] APK installs successfully
- [ ] App launches without crashing
- [ ] Dashboard displays with data
- [ ] Logcat has NO error messages
- [ ] App stays responsive for 30+ seconds
- [ ] Navigation works smoothly

---

### **GO/NO-GO DECISION**

**GO ✅** = All tests pass → Proceed to physical device testing
**NO-GO ❌** = Any test fails → Report issues, don't proceed

---

### **RESULT TEMPLATE**

Create file: `EMULATOR_SMOKE_TEST_RESULTS_APRIL27.md`

```markdown
# Emulator Smoke Test Results

**Tester:** [Your Name]  
**Date:** April 27, 2026  
**Emulator:** [e.g., Pixel 6a API 34]  

## Results
- APK Installation: PASS / FAIL
- App Launch: PASS / FAIL
- Dashboard Display: PASS / FAIL
- Logcat Check: PASS / FAIL
- Navigation: PASS / FAIL

## Status
GO / NO-GO for device testing

## Issues (if any)
[List any problems found]
```

---

### **TROUBLESHOOTING**

| Issue | Fix |
|-------|-----|
| APK won't install | `adb uninstall com.emul8r.bizap` then retry |
| App crashes | Check logcat output for error |
| Blank screen | Wait 5 seconds; if persists check logcat |
| Can't find emulator | Start Android Studio → AVD Manager |
| Logcat shows errors | Document and report; don't proceed |
