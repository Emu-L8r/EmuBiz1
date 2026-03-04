# Firebase Crashlytics & Timber Logging Guide

## What We Just Set Up

Your Bizap app now has **production-grade crash monitoring and structured logging**. This means:

- ✅ Crashes are automatically reported to Firebase Crashlytics console
- ✅ Warnings and errors are logged with breadcrumb trails
- ✅ You can see what happened BEFORE a crash occurred
- ✅ Different logging behavior for DEBUG (development) vs RELEASE (production)

---

## Part 1: Understanding the Problem

### Before: Android's Built-in Logging

```kotlin
// OLD WAY (don't do this):
Log.d("MyTag", "Starting save...")
Log.e("MyTag", "Save failed", exception)
Log.i("MyTag", "Save complete")
```

**Problems:**
- Only appears in Logcat while app is running
- No history in production
- Can't see crash context
- Hard to debug user issues remotely
- No structured data collection

### After: Timber + Firebase Crashlytics

```kotlin
// NEW WAY (what we implemented):
Timber.d("Starting save...")
Timber.e(exception, "Save failed")
Timber.i("Save complete")
```

**Benefits:**
- ✅ Still appears in Logcat during development
- ✅ Logs appear in Firebase Crashlytics in production
- ✅ Full breadcrumb trail before crashes
- ✅ Can analyze patterns across users
- ✅ Structured data collection

---

## Part 2: The Architecture

### How It Works

```
Your Code
   ↓
Timber.d/e/w/i() calls
   ↓
Timber Facade (routes to all Trees)
   ↓
   ├─ DebugTree (DEBUG builds) → Logcat
   └─ CrashlyticsTree (RELEASE builds) → Firebase Crashlytics
```

### The Three Layers

#### Layer 1: Timber (Logging Abstraction)
**File:** Not created by you - it's a library
**What it does:** Single API for logging
**Why:** Decouples your code from specific logging backend

```kotlin
// You only ever write this:
Timber.d("message")

// Timber decides where it goes based on what Trees are planted
```

#### Layer 2: CrashlyticsTree (Custom Implementation)
**File:** `app/src/main/java/com/emul8r/bizap/utils/CrashlyticsTree.kt`
**What it does:** Filters and forwards logs to Firebase
**Why:** Customize which log levels Firebase sees

```kotlin
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only send WARN and ERROR to Firebase (not DEBUG/VERBOSE)
        if (priority < Log.WARN) return
        
        FirebaseCrashlytics.getInstance().log("$tag: $message")
        if (t != null) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }
}
```

#### Layer 3: Firebase Crashlytics (Backend)
**File:** Firebase project (cloud)
**What it does:** Collects, stores, and displays crashes
**Why:** Central dashboard for all production issues

---

## Part 3: How to Use It

### Logging Levels (From Least to Most Severe)

```kotlin
Timber.v()  // VERBOSE - Detailed flow tracing (development only)
Timber.d()  // DEBUG   - Data values, state changes
Timber.i()  // INFO    - Important events
Timber.w()  // WARN    - Potential issues (sent to Firebase)
Timber.e()  // ERROR   - Failures (sent to Firebase)
Timber.wtf()// ASSERT  - Should never happen (sent to Firebase)
```

### Example: Good Logging in CreateInvoiceViewModel

```kotlin
fun onSaveClicked() {
    viewModelScope.launch {
        try {
            // 1. Log entry point
            Timber.d("🔵 INVOICE SAVE STARTED")
            
            // 2. Log intermediate steps
            val customer = state.selectedCustomer ?: throw Exception("Please select customer")
            Timber.d("✅ Customer selected: ${customer.name}")
            
            // 3. Log calculations
            val subtotal = lineItems.sumOf { it.calculateTotal() }
            Timber.d("✅ Subtotal calculated: $subtotal cents")
            
            // 4. Log success
            Timber.d("✅ INVOICE SAVE COMPLETE - SUCCESS")
            
        } catch (e: Exception) {
            // 5. Log errors WITH context
            Timber.e(e, "❌ INVOICE SAVE FAILED: ${e.message}")
        }
    }
}
```

**What this accomplishes:**
- If the user's invoice fails to save, Firebase shows the full timeline
- You can see exactly where it failed
- You know what the error was
- You know what state the app was in

### Breadcrumb Trail Example

When an exception occurs, Firebase shows something like:

```
Timeline for crash on March 5, 2026 10:15 AM:
  10:15:23 "🔵 INVOICE SAVE STARTED"
  10:15:23 "✅ Customer selected: John Doe"
  10:15:24 "✅ Subtotal calculated: 14999 cents"
  10:15:25 "❌ INVOICE SAVE FAILED: Database locked"
  10:15:26 CRASH: SQLiteException (Thread: SaveThread)
```

This tells you:
- The user got to selecting a customer
- The subtotal was calculated correctly
- Database was locked (the actual problem)
- This happened in SaveThread

Without logs, you'd just see: "SQLiteException occurred" 🤷

---

## Part 4: DEBUG vs RELEASE Configuration

### What Changed in BizapApplication.kt

```kotlin
override fun onCreate() {
    super.onCreate()
    
    if (BuildConfig.DEBUG) {
        // DEVELOPMENT: Log everything to Logcat
        Timber.plant(Timber.DebugTree())
        Timber.d("🚀 Bizap in DEBUG mode - full logging enabled")
    } else {
        // PRODUCTION: Log only WARN/ERROR to Firebase
        Timber.plant(CrashlyticsTree())
        Timber.d("🚀 Bizap in RELEASE mode - logging to Firebase")
    }
}
```

### What This Means

| Scenario | DEBUG Build | RELEASE Build |
|----------|------------|---------------|
| `Timber.d("Starting save")` | ✅ Visible in Logcat | ❌ Not sent to Firebase |
| `Timber.w("Invalid input")` | ✅ Visible in Logcat | ✅ Sent to Firebase |
| `Timber.e(exception, "Failed")` | ✅ Visible in Logcat | ✅ Sent to Firebase + recorded |

### Why This Matters

**Debug logs are HUGE:**
- If you log everything to production, your Firebase account fills up fast
- Users' device storage gets bloated with debug data
- Battery drains from excessive I/O
- Only send necessary data to production

---

## Part 5: Setting Up Firebase Console

### What You Need

1. **Google account** (you probably have one)
2. **Firebase project** (create at https://console.firebase.google.com)
3. **google-services.json** file (downloaded from Firebase console)

### How to Get google-services.json

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create new project (or use existing)
3. Add Android app:
   - Package name: `com.emul8r.bizap`
   - SHA-1 hash: Get from: `./gradlew signingReport`
4. Download `google-services.json`
5. Place in: `Bizap/app/google-services.json`

### Without google-services.json

If you don't have a Firebase project yet, the app will:
- ✅ Still compile and run
- ✅ Still log to Logcat in DEBUG
- ✅ Just won't send to Firebase in RELEASE (gracefully fails)

The `try/catch` blocks in BizapApplication handle this:

```kotlin
try {
    FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true)
} catch (e: Exception) {
    Timber.w(e, "Firebase initialization failed (expected if google-services.json missing)")
}
```

---

## Part 6: How to Test Logging

### Test 1: Verify DEBUG Logging

1. Build and run the app: `./gradlew :app:installDebug`
2. Open Logcat in Android Studio: `View → Tool Windows → Logcat`
3. Filter by: `"Bizap"`
4. Start creating an invoice
5. Watch the breadcrumb trail appear:
   ```
   D/Bizap: 🔵 INVOICE SAVE STARTED
   D/Bizap: ✅ Customer selected: Test Customer
   D/Bizap: ✅ Line items mapped: 3 items
   D/Bizap: ✅ Subtotal calculated: 14999 cents
   D/Bizap: ✅ INVOICE SAVE COMPLETE - SUCCESS
   ```

### Test 2: Throw a Test Exception (Advanced)

Add this temporarily to CreateInvoiceViewModel:

```kotlin
fun testCrashlytics() {
    viewModelScope.launch {
        Timber.w("Test warning - should appear in Firebase")
        Timber.e(Exception("Test exception"), "Test error - should appear in Firebase")
    }
}
```

Then call it from a button. In RELEASE builds, these appear in Firebase Crashlytics.

### Test 3: Monitor Firebase Console

1. Go to Firebase Console
2. Select your project
3. Navigate to: **Crashlytics** → **Alerts** or **Issues**
4. You'll see errors and warnings from your test

---

## Part 7: Common Mistakes to Avoid

### ❌ Don't Log Personally Identifiable Information (PII)

```kotlin
// BAD - Don't do this:
Timber.d("Saving invoice for ${customer.email}")
Timber.d("Tax ID: ${customer.taxId}")
Timber.d("Payment method: ${customer.paymentDetails}")

// GOOD - Do this instead:
Timber.d("Saving invoice for customer ID: ${customer.id}")
Timber.d("Tax registration status: ${businessProfile.isTaxRegistered}")
```

**Why:** PII in Firebase is a GDPR/privacy violation. Firebase isn't encrypted for sensitive data.

### ❌ Don't Log Passwords, API Keys, Tokens

```kotlin
// BAD:
Timber.d("API Key: $apiKey")

// GOOD:
Timber.d("API request sent")
```

### ❌ Don't Log Large Objects

```kotlin
// BAD:
Timber.d("Invoice: $invoice")  // Entire object

// GOOD:
Timber.d("Invoice saved: ID=${invoice.id}, total=${invoice.totalAmount}")
```

### ❌ Don't Use Timber in Loops

```kotlin
// BAD - creates 1000 log entries:
for (item in lineItems) {
    Timber.d("Item: $item")  // Called 1000 times
}

// GOOD:
Timber.d("Processing ${lineItems.size} items")
```

### ❌ Don't Forget Try/Catch Around Firebase Calls

The code already handles this:

```kotlin
try {
    FirebaseAnalytics.getInstance().logEvent("invoice_created", params)
} catch (e: Exception) {
    Timber.w("Analytics failed - that's OK")
}
```

---

## Part 8: Files Created/Modified

### Files Modified

1. **gradle/libs.versions.toml**
   - Added: `timber = "5.0.1"`
   - Added: `firebase-bom = "34.9.0"`

2. **app/build.gradle.kts**
   - Added: `implementation(libs.timber)`
   - Already had: Firebase dependencies

3. **app/src/main/java/com/emul8r/bizap/BizapApplication.kt**
   - Added: Timber initialization with DEBUG/RELEASE logic
   - Added: Firebase Analytics initialization

4. **app/src/main/java/com/emul8r/bizap/utils/CrashlyticsTree.kt**
   - Enhanced with detailed documentation

5. **app/src/main/java/com/emul8r/bizap/ui/invoices/CreateInvoiceViewModel.kt**
   - Added: Firebase Analytics event logging on invoice save

### Files NOT Modified

- No Android manifest changes needed (Firebase plugins handle it)
- No permission changes (handled by Gradle plugin)
- No database migrations needed

---

## Part 9: What Happens When You Build

### Debug Build Flow

```
./gradlew :app:assembleDebug
    ↓
Google Services Plugin: Processes google-services.json (if present)
    ↓
Gradle: Includes Timber (5.0.1) + Firebase (34.9.0)
    ↓
Kotlin Compiler: Compiles everything
    ↓
APK Created: Includes logging infrastructure
    ↓
When App Runs:
  - BizapApplication.onCreate() → plants Timber.DebugTree()
  - Timber.d() → logs to Logcat
  - Timber.e() → logs to Logcat
```

### Release Build Flow

```
./gradlew :app:assembleRelease
    ↓
(Same as above)
    ↓
When App Runs:
  - BizapApplication.onCreate() → plants CrashlyticsTree()
  - Timber.d() → filtered out (not sent to Firebase)
  - Timber.e() → forwarded to Firebase Crashlytics
  - Exceptions → recorded in Crashlytics
```

---

## Part 10: Next Steps

### Immediate

1. **Download google-services.json** from Firebase Console
2. **Place it** in `Bizap/app/google-services.json`
3. **Rebuild**: `./gradlew :app:assembleDebug`
4. **Test logging** by creating an invoice and watching Logcat

### This Week

1. Add logging to other critical ViewModels:
   - `EditInvoiceViewModel` (same pattern as CreateInvoice)
   - `CustomerViewModel`
   - `RevenueRepository` (database operations)

2. Identify 3-5 failure scenarios, add logging for each:
   - Database save failed
   - Network request failed
   - User input validation failed

3. Monitor Firebase Console for real patterns

### Best Practices Going Forward

1. **Always wrap database operations with logs:**
   ```kotlin
   try {
       Timber.d("Loading customers from database...")
       val customers = dao.getAllCustomers()
       Timber.d("✅ Loaded ${customers.size} customers")
       return customers
   } catch (e: Exception) {
       Timber.e(e, "❌ Failed to load customers")
       throw e
   }
   ```

2. **Log state transitions:**
   ```kotlin
   Timber.d("State: IDLE → LOADING")
   // ... do work ...
   Timber.d("State: LOADING → SUCCESS")
   ```

3. **Use emoji prefixes for quick scanning:**
   - 🔵 Blue = Starting something
   - ✅ Green = Success/completion
   - ⚠️ Orange = Warning
   - ❌ Red = Error/failure

---

## Part 11: Troubleshooting

### Problem: "google-services.json missing" in build

**Solution:** Add a placeholder (app won't send to Firebase, but will run):
```json
{
  "project_info": { "project_number": "000000000000", "project_id": "placeholder" },
  "client": [{ "client_info": { "package_name": "com.emul8r.bizap" } }]
}
```

### Problem: Gradle sync fails with Firebase errors

**Solution:** 
1. `./gradlew clean`
2. Invalidate Android Studio cache: `File → Invalidate Caches`
3. Rebuild

### Problem: Logcat is too noisy

**Solution:** Filter by tag:
1. In Logcat, search box: `tag:"^Bizap$"`
2. Or search: `"✅|❌|⚠️"` to see only marked logs

### Problem: Firebase Console shows no data

**Solution:**
1. Verify `google-services.json` is present
2. Ensure BuildConfig.DEBUG is false for RELEASE builds
3. Give Firebase a few minutes to sync
4. Check Firebase Console for API key configuration

---

## Summary

You now have:

✅ **Timber logging framework** - Single API for all logging
✅ **CrashlyticsTree** - Forwards production errors to Firebase
✅ **DEBUG/RELEASE differentiation** - Different behavior per build type
✅ **Firebase Crashlytics integration** - Production crash monitoring
✅ **Firebase Analytics** - User behavior tracking (example in CreateInvoiceViewModel)
✅ **Breadcrumb trails** - See what happened before a crash

This is production-grade infrastructure. The next step is **using it consistently** across all your ViewModels and repositories.

---

## Quick Reference

| Task | Code |
|------|------|
| Log debug info | `Timber.d("message")` |
| Log warning | `Timber.w("message")` |
| Log error with exception | `Timber.e(exception, "message")` |
| Log analytics event | `FirebaseAnalytics.getInstance().logEvent("event_name", Bundle())` |
| Build for testing | `./gradlew :app:assembleDebug` |
| Build for release | `./gradlew :app:assembleRelease` |
| View Logcat | Android Studio → Logcat tab |
| Monitor crashes | Firebase Console → Crashlytics |


