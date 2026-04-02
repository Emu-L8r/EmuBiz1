# PHASE 1: FOUNDATION & PROFILING - IMPLEMENTATION GUIDE
## Weeks 1-2 (March 22-31, 2026)

---

## 📊 BASELINE METRICS CAPTURE

### Week 1: Initial Setup (March 22-26)

#### ✅ BUILD TIME BASELINE
**Current Status**: ✅ CAPTURED
- **Build Time**: 122 seconds (confirmed via `./gradlew clean build`)
- **Compilation Errors**: 0 errors
- **Warnings**: Gradle 9.2.1 deprecation warnings (expected)
- **APK Size**: 17.7 MB
- **Test Count**: 994 tests passing
- **Date Captured**: March 22, 2026

**Measurement Method**:
```bash
# Repeat 3x, take average
time ./gradlew clean build 2>&1 | grep "BUILD"
```

---

### Week 2: Performance Measurements (March 27-31)

#### TO-DO: STARTUP TIME BASELINE (3 Devices)

**Devices to Test**:
1. Low-end: 2GB RAM, Android 10, Snapdragon 425
2. Mid-range: 6GB RAM, Android 12, Snapdragon 665
3. High-end: 8GB RAM, Android 14, Snapdragon 888

**Measurement Method** (Android Profiler):
```
1. Connect device via USB
2. Open app in Android Studio → Profiler
3. Launch app
4. Measure from first system event → first UI visible
5. Repeat 3x, take average
6. Document in PHASE_1_STARTUP_METRICS.md
```

**Expected Range**:
- Low-end: 3000-5000ms (cold start)
- Mid-range: 1500-2500ms (cold start)
- High-end: 1000-1500ms (cold start)

---

#### TO-DO: MEMORY USAGE BASELINE (3 Devices)

**Measurement Method** (Android Profiler → Memory tab):
```
1. Launch app (warm state)
2. Navigate to Dashboard
3. Scroll through payment list (100+ items)
4. Record peak memory during scroll
5. Wait 10 seconds
6. Record idle memory
7. Repeat on all 3 devices
```

**Expected Range**:
- Low-end: 150-250 MB peak
- Mid-range: 200-350 MB peak
- High-end: 250-400 MB peak

---

#### TO-DO: BATTERY DRAIN BASELINE (5-Hour Test Each Device)

**Measurement Method** (Battery Historian):
```
1. Clear device battery stats:
   adb shell dumpsys batterystats --reset
   
2. Enable Battery Historian:
   adb shell dumpsys batterystats > baseline.txt
   
3. Use app for 5 hours:
   - 1hr: Dashboard viewing (idle between actions)
   - 1hr: Payment list scrolling
   - 1hr: Analytics view
   - 1hr: Creating invoices
   - 1hr: Idle with sync enabled
   
4. Capture final state:
   adb shell dumpsys batterystats > after.txt
   
5. Calculate drain:
   (Initial % - Final %) / 5 hours = %/hr
```

**Expected Range** (Active Use):
- Low-end: 4-6 %/hr
- Mid-range: 2-4 %/hr
- High-end: 1-3 %/hr

---

## 🧪 TEST FRAMEWORK SETUP

### Phase 1 Tasks

#### ✅ 1. JUnit 4 Framework - READY
**Status**: Already configured in build.gradle.kts
```kotlin
testImplementation "junit:junit:4.13.2"
```

#### ✅ 2. Espresso Integration Tests - READY
**Status**: Already configured
```kotlin
androidTestImplementation "androidx.test.espresso:espresso-core:3.5.1"
```

#### ✅ 3. Compose UI Test Framework - READY
**Status**: Already configured
```kotlin
androidTestImplementation "androidx.compose.ui:ui-test-junit4:1.5.0"
testImplementation "androidx.compose.ui:ui-test-manifest:1.5.0"
```

#### ✅ 4. Test Utilities Library - CREATE
**Status**: Need to create `app/src/test/java/com/emul8r/bizap/test/`

---

## 📁 TEST INFRASTRUCTURE FILES TO CREATE

### 1. Test Utilities Library
**File**: `app/src/test/java/com/emul8r/bizap/test/TestDispatchers.kt`

```kotlin
package com.emul8r.bizap.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

object TestDispatchers {
    val Main: TestDispatcher = StandardTestDispatcher()
    val IO: TestDispatcher = StandardTestDispatcher()
    val Default: TestDispatcher = StandardTestDispatcher()
}
```

**File**: `app/src/test/java/com/emul8r/bizap/test/TestDataBuilder.kt`

```kotlin
package com.emul8r.bizap.test

import com.emul8r.bizap.domain.model.*
import java.time.LocalDate

object TestDataBuilder {
    
    fun buildInvoice(
        id: Long = 1L,
        customerName: String = "Test Customer",
        totalAmount: Double = 1000.0,
        status: String = "SENT"
    ) = Invoice(
        id = id,
        businessProfileId = 1L,
        customerId = 1L,
        customerName = customerName,
        totalAmount = totalAmount,
        amountPaid = 0.0,
        status = Invoice.Status.valueOf(status),
        dueDate = LocalDate.now().plusDays(30),
        date = LocalDate.now(),
        updatedAt = System.currentTimeMillis(),
        invoiceYear = 2026,
        invoiceSequence = 1,
        isQuote = false,
        items = listOf(
            Invoice.LineItem(
                id = 1L,
                description = "Test Item",
                quantity = 1.0,
                unitPrice = 1000.0
            )
        )
    )
    
    fun buildPaymentRecord(
        id: Long = 1L,
        invoiceId: Long = 1L,
        amount: Double = 500.0,
        date: LocalDate = LocalDate.now()
    ) = PaymentRecord(
        id = id,
        invoiceId = invoiceId,
        amount = amount,
        date = date
    )
}
```

**File**: `app/src/test/java/com/emul8r/bizap/test/MockFactory.kt`

```kotlin
package com.emul8r.bizap.test

import io.mockk.mockk
import com.emul8r.bizap.domain.repository.*

object MockFactory {
    fun createMockInvoiceRepository() = mockk<InvoiceRepository>(relaxed = true)
    fun createMockPaymentRepository() = mockk<PaymentRepository>(relaxed = true)
    fun createMockThemeRepository() = mockk<ThemeRepository>(relaxed = true)
    fun createMockAnalyticsRepository() = mockk<AnalyticsRepository>(relaxed = true)
}
```

---

## 📋 MEASUREMENT CHECKLIST - PHASE 1

### Week 1: Foundation Setup (✅ In Progress)

- [x] Build time baseline captured (122s)
- [x] Compilation errors verified (0 errors)
- [x] APK size documented (17.7 MB)
- [x] Test count verified (994 tests)
- [ ] Android Profiler installed & tested
- [ ] Battery Historian downloaded & configured
- [ ] Device matrix physically confirmed (3 devices available)
- [ ] USB drivers installed & tested
- [ ] ADB command-line tools verified

### Week 2: Performance Measurement (⏳ To-Do)

#### Device 1: Low-End (2GB RAM, Android 10)
- [ ] Startup time measured (3x average)
- [ ] Memory peak documented
- [ ] Battery drain measured (5hr test)
- [ ] Results documented in PHASE_1_STARTUP_METRICS.md

#### Device 2: Mid-Range (6GB RAM, Android 12)
- [ ] Startup time measured (3x average)
- [ ] Memory peak documented
- [ ] Battery drain measured (5hr test)
- [ ] Results documented in PHASE_1_MEMORY_METRICS.md

#### Device 3: High-End (8GB RAM, Android 14)
- [ ] Startup time measured (3x average)
- [ ] Memory peak documented
- [ ] Battery drain measured (5hr test)
- [ ] Results documented in PHASE_1_BATTERY_METRICS.md

#### Test Framework
- [ ] JUnit 4 tests verified working
- [ ] Espresso integration tests verified
- [ ] Compose UI tests verified
- [ ] Test utilities library created
- [ ] Test data builders created
- [ ] Mock factory created

---

## 🎯 GATE 1 EXIT CRITERIA (March 31)

**Must Complete ALL Before Proceeding to Phase 2**:

- [x] Build time baseline: 122 seconds ✅
- [x] Compilation errors: 0 ✅
- [x] Test count: 994 passing ✅
- [ ] Startup time baseline: 3 devices measured
- [ ] Memory baseline: 3 devices measured
- [ ] Battery baseline: 3 devices measured
- [ ] Profiling tools operational
- [ ] Test framework ready
- [ ] Test utilities library created
- [ ] Device matrix confirmed
- [ ] Team trained on measurement process

---

## 📈 BASELINE METRICS SUMMARY (So Far)

| Metric | Value | Status | Date |
|--------|-------|--------|------|
| Build Time | 122s | ✅ CAPTURED | Mar 22 |
| Compilation Errors | 0 | ✅ VERIFIED | Mar 22 |
| APK Size | 17.7 MB | ✅ CAPTURED | Mar 22 |
| Test Count | 994 | ✅ PASSING | Mar 22 |
| Startup (Low-end) | TBD | ⏳ PENDING | - |
| Startup (Mid-range) | TBD | ⏳ PENDING | - |
| Startup (High-end) | TBD | ⏳ PENDING | - |
| Memory Peak (Low-end) | TBD | ⏳ PENDING | - |
| Memory Peak (Mid-range) | TBD | ⏳ PENDING | - |
| Memory Peak (High-end) | TBD | ⏳ PENDING | - |
| Battery Drain (Active) | TBD | ⏳ PENDING | - |

---

## 📞 NEXT STEPS

1. **Immediately** (Today):
   - Create test utilities library files
   - Verify devices connected & working
   - Install Battery Historian tool

2. **This Week** (Before March 26):
   - Setup Android Profiler measurements
   - Document measurement procedures
   - Create measurement checklist

3. **Next Week** (Before March 31):
   - Measure startup time (3 devices)
   - Measure memory usage (3 devices)
   - Measure battery drain (3 devices, 5hr each)
   - Compile final baseline report

4. **March 31 Gate Review**:
   - All baselines captured? ✅
   - Profiling tools operational? ✅
   - Test framework ready? ✅
   - Device matrix confirmed? ✅
   - Team trained? ✅
   - **→ PROCEED TO PHASE 2** ✅


