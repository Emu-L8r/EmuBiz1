package com.emul8r.bizap.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * TIMBER.TREE PATTERN EXPLANATION
 * ================================
 * Timber is a logging library that works via the Timber.Tree abstraction.
 * Instead of calling Android's Log directly, you call Timber.i(), Timber.d(), Timber.e(), etc.
 * Timber then delegates these calls to all registered Tree instances.
 *
 * WHY USE TIMBER.TREE?
 * - Single API for all logging (don't mix Android Log with Sentry with Firebase)
 * - Easy to swap implementations (DebugTree in DEBUG, CrashlyticsTree in RELEASE)
 * - Automatic tag extraction from class names
 * - Tree instances can filter or modify logs before they're sent
 *
 * HOW IT WORKS:
 * 1. In BizapApplication.onCreate(), we plant different trees based on BUILD TYPE:
 *    - DEBUG:   Timber.plant(Timber.DebugTree())      → logs to Android Logcat
 *    - RELEASE: Timber.plant(CrashlyticsTree())        → logs to Firebase Crashlytics
 *
 * 2. When you call Timber.e("error message"), Timber loops through ALL planted trees
 *    and calls tree.log(priority, tag, message, exception)
 *
 * 3. CrashlyticsTree.log() is your custom implementation that:
 *    - Filters out low-priority logs (below WARN level)
 *    - Sends the message to Firebase Crashlytics
 *    - Records exceptions separately for better crash analysis
 *
 * FIREBASE CRASHLYTICS INTEGRATION
 * ===============================
 * Firebase Crashlytics automatically captures:
 * - Uncaught exceptions (crashes)
 * - ANRs (Application Not Responding)
 *
 * We enhance this by logging warnings and errors via CrashlyticsTree so that
 * when a crash occurs, Firebase shows the full breadcrumb trail:
 * - What operations were being performed
 * - What errors occurred before the crash
 * - What decisions the code made
 *
 * EXAMPLE BREADCRUMB TRAIL:
 * Timeline in Firebase Console:
 *   10:15:23 - "✅ Customer selected: John Doe"
 *   10:15:24 - "✅ Line items mapped: 3 items"
 *   10:15:25 - "⚠️ WARN: Invalid tax rate, using default"
 *   10:15:26 - "❌ ERROR: Database write failed - IOException"
 *   10:15:27 - "CRASH: NullPointerException in SaveThread"
 *
 * This trail helps you debug production issues without needing to ask users "what did you do?"
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only forward WARN, ERROR, and ASSERT levels
        // DEBUG and INFO are too verbose for production
        if (priority < Log.WARN) return

        // 1. SEND MESSAGE TO FIREBASE
        // FirebaseCrashlytics.log() adds this to the breadcrumb trail
        // The tag (e.g., "CreateInvoiceViewModel") helps you search the logs later
        FirebaseCrashlytics.getInstance().log("${tag ?: "Bizap"}: $message")

        // 2. RECORD EXCEPTION SEPARATELY
        // This ensures exceptions appear in the "Crashes" dashboard
        // Also creates additional context for debugging
        if (t != null) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }
}

/**
 * HOW TO USE THIS IN YOUR CODE:
 * =============================
 *
 * BEFORE (Android Log - no Firebase):
 *     Log.e("MyTag", "Error saving invoice", exception)
 *     // ❌ Problem: Not captured in production, hard to debug
 *
 * AFTER (Timber - goes to Firebase in RELEASE builds):
 *     Timber.e(exception, "Error saving invoice")
 *     // ✅ Benefit: Captured in Firebase, visible in crash context
 *
 * TIMBER LOG LEVELS (from least to most severe):
 *   Timber.v()  = Verbose (use for detailed flow tracing)
 *   Timber.d()  = Debug   (use for data values, state changes)
 *   Timber.i()  = Info    (use for important events)
 *   Timber.w()  = Warning (use for potential issues)
 *   Timber.e()  = Error   (use for failures that recovery from)
 *   Timber.wtf() = Assert (use for critical "should never happen" cases)
 *
 * GOOD LOGGING PRACTICES:
 * ========================
 * ✅ Log at function entry/exit for important flows
 * ✅ Log before and after I/O operations (database, network, file)
 * ✅ Log state changes and decisions
 * ✅ Log exceptions with context
 * ✅ Use emoji prefixes (✅, ❌, ⚠️) to skim logs quickly
 *
 * ❌ DON'T log personally identifiable information (PII)
 * ❌ DON'T log passwords, API keys, tokens
 * ❌ DON'T log large objects (will clutter Firebase)
 * ❌ DON'T call Timber.e() in loops (creates noise)
 *
 * TESTING YOUR SETUP:
 * ===================
 * To test if Firebase Crashlytics is wired up:
 * 1. In a ViewModel or Activity, add this code:
 *    Timber.e(Exception("Test crash"), "Test logging")
 * 2. Build release APK: ./gradlew assembleRelease
 * 3. Install and trigger the exception
 * 4. Go to Firebase Console > Crashlytics
 * 5. You should see the error in the logs section
 */

