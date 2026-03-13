# 🎯 QUICK START TESTING CARD

**Print this page and keep it handy!**

---

## ⏱️ QUICK TIMELINE

| Phase | Task | Time | Command |
|-------|------|------|---------|
| 1️⃣ | Build | 10 min | `./gradlew clean assembleDebug` |
| 2️⃣ | Tests | 15 min | `./gradlew testDebugUnitTest` |
| 3️⃣ | Install | 5 min | `adb install -r app/build/outputs/apk/debug/app-debug.apk` |
| 4️⃣ | Dashboard | 10 min | Manual: Create invoice, check revenue |
| 5️⃣ | Analytics | 10 min | Manual: Test payment, rates |
| 6️⃣ | Consistency | 5 min | Manual: Verify 3 dashboards match |
| 7️⃣ | Edge Cases | 5 min | Manual: Test zero, all unpaid |
| 8️⃣ | Logs | 5 min | Check: `adb logcat \| grep "CRITICAL"` |

**TOTAL: 65 minutes**

---

## ✅ INSTANT SUCCESS CRITERIA

### ✅ BUILD
```
./gradlew clean assembleDebug
Result: ✅ BUILD SUCCESSFUL
```

### ✅ UNIT TESTS
```
./gradlew testDebugUnitTest
Result: ✅ 74 tests passed
```

### ✅ DEVICE
```
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.emul8r.bizap/.MainActivity
Result: ✅ App launches, no crash
```

### ✅ DASHBOARD
- Create invoice for A$100, PAID
- Revenue increases by A$100 immediately
- No manual refresh needed

### ✅ ANALYTICS
- Create invoice A$1000, SENT
- Record payment A$500
- Outstanding shows A$500, Rate shows 50%

### ✅ CONSISTENCY
- Dashboard revenue = Segments revenue
- Payment Analytics count = Dashboard count
- All numbers match

### ✅ EDGE CASES
- Zero outstanding: Works
- All unpaid: Works
- No crashes

### ✅ LOGS
```
adb logcat | grep "SNAPSHOT"     → Shows ✅ updates
adb logcat | grep "CRITICAL"     → Shows exceptions
adb logcat | grep "Health"       → Shows health check
adb logcat | grep "Analytics"    → Shows events
```

---

## 🔴 FAIL INDICATORS

| Symptom | Problem | Action |
|---------|---------|--------|
| `BUILD FAILED` | Compilation error | Fix code, retry |
| `XX tests failed` | Logic error | Check failing test file |
| Installation fails | Device issue | Check `adb devices` |
| Revenue doesn't update | Snapshot not syncing | Check logs for errors |
| All dashboards differ | Consistency issue | Check query logic |
| Silent failure | Exception caught | Search for try/catch |

---

## 📱 MANUAL TEST CARDS

### Card 1: Dashboard Revenue Test
```
BEFORE: Revenue = A$___
ACTION: Create invoice A$100, PAID
AFTER:  Revenue = A$___
EXPECT: Increased by A$100? ✅ / ❌
EXPECT: Immediate (no refresh)? ✅ / ❌
```

### Card 2: Payment Recording Test
```
ACTION: Create A$1000 invoice, SENT
CHECK:  Outstanding = A$1000
ACTION: Record payment A$500
CHECK:  Outstanding = A$500? ✅ / ❌
CHECK:  Rate = 50%? ✅ / ❌
```

### Card 3: Status Change Test
```
ACTION: Create invoice, status DRAFT
CHECK:  Not counted (revenue A$0)
ACTION: Change to PAID
CHECK:  Revenue increases? ✅ / ❌
CHECK:  Immediate? ✅ / ❌
```

### Card 4: Cross-Dashboard Test
```
INVOKE: Create A$500 invoice, PAID
CHECK Dashboard Revenue:        A$500
CHECK Analytics Outstanding:    A$0
CHECK Segments Revenue:         A$500
VERIFY: All match? ✅ / ❌
```

---

## 🔍 LOG CHECK COMMANDS

```bash
# Clear logs before test
adb logcat -c

# Check for exceptions
adb logcat | grep "❌ CRITICAL"

# Check snapshot updates
adb logcat | grep "SNAPSHOT"

# Check health
adb logcat | grep "Health"

# Check events
adb logcat | grep "Analytics event"

# Check metrics comparison
adb logcat | grep "METRICS COMPARISON"
```

---

## 📊 RECORDING RESULTS

Print and fill in:

```
PHASE 1 - BUILD
  Build successful? ✅ / ❌
  Time taken: ____ min
  
PHASE 2 - TESTS
  74+ tests passing? ✅ / ❌
  Coverage >80%? ✅ / ❌
  Time taken: ____ min

PHASE 3 - INSTALL
  App installed? ✅ / ❌
  App launched? ✅ / ❌
  Time taken: ____ min

PHASE 4 - DASHBOARD
  Revenue updates? ✅ / ❌
  Count accurate? ✅ / ❌
  Time taken: ____ min

PHASE 5 - ANALYTICS
  Rates calculate? ✅ / ❌
  Aging buckets? ✅ / ❌
  Time taken: ____ min

PHASE 6 - CONSISTENCY
  Cross-dashboard match? ✅ / ❌
  Time taken: ____ min

PHASE 7 - EDGE CASES
  Zero outstanding? ✅ / ❌
  All unpaid? ✅ / ❌
  Time taken: ____ min

PHASE 8 - LOGS
  Exceptions visible? ✅ / ❌
  Snapshots logged? ✅ / ❌
  Health check? ✅ / ❌
  Events? ✅ / ❌
  Time taken: ____ min

═════════════════════════════════════
OVERALL RESULT: ✅ / ❌
READY TO DEPLOY: ✅ / ❌
START TIME: _____ END TIME: _____
TOTAL TIME: _____ min
═════════════════════════════════════
```

---

## 🆘 TROUBLESHOOTING QUICK GUIDE

### Build Fails
```bash
# Try:
./gradlew clean
./gradlew assembleDebug --stacktrace

# If still fails:
# Check: Kotlin version, Java version
# Fix: Update build.gradle.kts
```

### Tests Fail
```bash
# Try:
./gradlew testDebugUnitTest --tests "*ExceptionTest*"
./gradlew testDebugUnitTest --tests "*SnapshotSyncTest*"
# etc. (test one pathway at a time)
```

### App Won't Install
```bash
# Try:
adb devices  # Check if device connected
adb uninstall com.emul8r.bizap  # Uninstall old version
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Revenue Not Updating
```bash
# Check logs:
adb logcat -c
# Then create invoice
adb logcat | grep "SNAPSHOT"
# Should see: ✅ Updated DailyRevenueSnapshot
```

### Dashboards Don't Match
```bash
# Check:
1. Are all snapshots being created?
2. Are queries using same business ID?
3. Are calculations the same?

# Log check:
adb logcat | grep "METRICS COMPARISON"
```

---

## 🎯 PASS/FAIL AT A GLANCE

### ALL GOOD ✅
```
✅ BUILD SUCCESSFUL
✅ 74 tests PASSED
✅ App installs & launches
✅ Revenue updates immediately
✅ Dashboards all consistent
✅ Logs show all operations
✅ No crashes or errors
✅ Ready to merge!
```

### NEEDS WORK ❌
```
❌ Build fails
❌ Tests failing
❌ App crashes
❌ Revenue not updating
❌ Dashboards inconsistent
❌ Silent failures in logs
❌ Crashes on edge cases
❌ Need to debug
```

---

## 📞 REFERENCE DOCUMENTS

1. **COMPREHENSIVE_TESTING_GUIDE.md** - Detailed explanation of each test
2. **TESTING_IMPLEMENTATION_PLAN.md** - Step-by-step execution guide
3. **TESTING_STRATEGY_SUMMARY.md** - Overview and context

---

## 🚀 WHEN YOU'RE DONE

If all checkmarks are ✅:

```
1. Commit results to git
2. Create summary report
3. Notify team: "Ready to merge"
4. Merge to main branch
5. Deploy with confidence!
```

---

**Print this card!** Keep it by your keyboard while testing.

**Questions?** See the detailed guides above.

**Ready to start?** Begin with Phase 1: `./gradlew clean assembleDebug`

---

**Last Updated:** March 7, 2026
**Status:** Ready for Use

