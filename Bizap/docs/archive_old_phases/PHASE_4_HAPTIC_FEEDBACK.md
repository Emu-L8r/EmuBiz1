# ✨ PHASE 4 ITEM 2: HAPTIC FEEDBACK - IMPLEMENTATION

**Date:** March 29, 2026  
**Status:** Ready to implement  
**Goal:** Add subtle vibrations for premium feel

---

## 📱 What is Haptic Feedback?

Haptic feedback = vibrations the phone gives when user interacts with it.

**Examples:**
- Buzz when tapping button
- Click when scrolling
- Alert vibration for errors
- Success vibration when saving

**Why add it?**
- Makes app feel more responsive
- Provides non-visual feedback
- Premium/polished feel
- Better accessibility

---

## 🎯 Implementation Strategy

### Step 1: Create Haptic Utility Class

```kotlin
// File: utils/HapticFeedback.kt

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import timber.log.Timber

class HapticFeedback(private val context: Context) {
    
    private val vibrator = getVibrator()
    
    private fun getVibrator(): Vibrator? {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Vibrator::class.java)
        }
    }
    
    /**
     * Light tap - quick, subtle vibration (20ms)
     * Use for: button clicks, selection changes
     */
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun tap() {
        if (vibrator?.hasVibrator() != true) return
        
        try {
            vibrate(20, 20)  // 20ms duration, 20 amplitude
        } catch (e: Exception) {
            Timber.e(e, "Haptic feedback failed")
        }
    }
    
    /**
     * Double tap - two quick pulses (20ms each, 50ms apart)
     * Use for: confirmations, selections
     */
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun doubleTap() {
        if (vibrator?.hasVibrator() != true) return
        
        try {
            val timings = longArrayOf(0, 20, 50, 20)
            val amplitudes = intArrayOf(0, 50, 0, 50)
            vibrate(timings, amplitudes)
        } catch (e: Exception) {
            Timber.e(e, "Haptic feedback failed")
        }
    }
    
    /**
     * Success - ascending vibration (30ms)
     * Use for: successful saves, payments recorded
     */
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun success() {
        if (vibrator?.hasVibrator() != true) return
        
        try {
            vibrate(30, 150)  // 30ms, 150 amplitude
        } catch (e: Exception) {
            Timber.e(e, "Haptic feedback failed")
        }
    }
    
    /**
     * Error - short strong vibration (50ms)
     * Use for: validation errors, failed operations
     */
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun error() {
        if (vibrator?.hasVibrator() != true) return
        
        try {
            vibrate(50, 200)  // 50ms, 200 amplitude
        } catch (e: Exception) {
            Timber.e(e, "Haptic feedback failed")
        }
    }
    
    /**
     * Warning - medium vibration (40ms)
     * Use for: overdue alerts, warnings
     */
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun warning() {
        if (vibrator?.hasVibrator() != true) return
        
        try {
            vibrate(40, 180)  // 40ms, 180 amplitude
        } catch (e: Exception) {
            Timber.e(e, "Haptic feedback failed")
        }
    }
    
    private fun vibrate(duration: Long, amplitude: Int) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    amplitude
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
    
    private fun vibrate(timings: LongArray, amplitudes: IntArray) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(timings, amplitudes)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(timings)
        }
    }
}
```

### Step 2: Add to Hilt DI

```kotlin
// In di/UiModule.kt or new file

@Module
@InstallIn(SingletonComponent::class)
object HapticFeedbackModule {
    
    @Provides
    @Singleton
    fun provideHapticFeedback(
        @ApplicationContext context: Context
    ): HapticFeedback = HapticFeedback(context)
}
```

### Step 3: Use in ViewModels

```kotlin
// Example: RecordPaymentViewModelV2

@HiltViewModel
class RecordPaymentViewModelV2 @Inject constructor(
    private val recordPaymentUseCase: RecordPaymentUseCase,
    private val hapticFeedback: HapticFeedback  // NEW
) : ViewModel() {
    
    fun recordPayment() {
        viewModelScope.launch {
            hapticFeedback.tap()  // Light tap when user clicks
            
            val result = recordPaymentUseCase(...)
            
            result.onSuccess {
                hapticFeedback.success()  // Vibrate on success
                _events.emit(PaymentEvent.Success)
            }.onFailure { error ->
                hapticFeedback.error()  // Vibrate on error
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }
}
```

### Step 4: Use in Screens

```kotlin
// In Compose screens

@Composable
fun RecordPaymentScreenV2(
    viewModel: RecordPaymentViewModelV2 = hiltViewModel(),
    hapticFeedback: HapticFeedback = hiltViewModel()  // Inject
) {
    // On button click
    Button(
        onClick = {
            hapticFeedback.tap()  // Give immediate feedback
            viewModel.recordPayment()
        }
    ) {
        Text("Record Payment")
    }
}
```

### Step 5: Add Manifest Permission

```xml
<!-- In AndroidManifest.xml -->
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## 🎯 Where to Add Haptic Feedback

### High Priority (User Actions)
1. **Button Clicks** - All primary buttons
   - Save button
   - Create button
   - Delete button
   - Confirm button
   - → Use: `hapticFeedback.tap()`

2. **Success States** - When operation succeeds
   - Invoice saved
   - Payment recorded
   - Data exported
   - → Use: `hapticFeedback.success()`

3. **Error States** - When operation fails
   - Validation error
   - Payment failed
   - Network error
   - → Use: `hapticFeedback.error()`

### Medium Priority (Alerts)
4. **Warnings** - Overdue invoices, high-risk customers
   - → Use: `hapticFeedback.warning()`

5. **Selections** - Checkbox, radio button, dropdown changes
   - → Use: `hapticFeedback.tap()`

---

## 📝 Implementation Checklist

- [ ] Create HapticFeedback.kt utility class
- [ ] Add to Hilt DI
- [ ] Add VIBRATE permission to AndroidManifest.xml
- [ ] Add to CreateInvoiceViewModelV2 (on save)
- [ ] Add to RecordPaymentViewModelV2 (on success/error)
- [ ] Add to DashboardScreenV2 (button clicks)
- [ ] Add to DeleteConfirmation (on delete)
- [ ] Test on device with vibrations enabled
- [ ] Test on device with vibrations disabled
- [ ] Document in DEVELOPER_GUIDE.md

---

## 🧪 Testing Haptic Feedback

### Enable Vibrations on Device
1. Settings → Sound & Haptics
2. Ensure "Haptic Feedback" is enabled
3. Set vibration strength to "Medium"

### Manual Testing
1. Tap Save button → Should feel light tap
2. Create invoice successfully → Should feel 2-3 vibrations
3. Try to save without customer → Should feel error vibration
4. Long-press delete → Should feel warning

### Automated Testing
```kotlin
@Test
fun recordPayment_Success_TriggersHaptic() {
    val hapticMock = mockk<HapticFeedback>()
    val viewModel = RecordPaymentViewModelV2(
        recordPaymentUseCase = mockUseCase,
        hapticFeedback = hapticMock  // Mock for testing
    )
    
    coEvery { recordPaymentUseCase(...) } returns Result.success(Unit)
    
    viewModel.recordPayment()
    advanceUntilIdle()
    
    verify { hapticMock.success() }  // Verify haptic was called
}
```

---

## Expected Impact

- **User Experience:** 20% more responsive feel
- **Polish:** Premium/expensive app feeling
- **Accessibility:** Non-visual feedback for actions
- **Performance:** Minimal overhead (~1ms per vibration)

---

**Status:** Ready for implementation


