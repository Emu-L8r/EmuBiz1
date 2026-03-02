package com.emul8r.bizap.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

/**
 * Timber tree that forwards warning and error logs to Firebase Crashlytics.
 */
class CrashlyticsTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority < Log.WARN) return // Only forward WARN and ERROR

        FirebaseCrashlytics.getInstance().log("${tag ?: "Bizap"}: $message")

        if (t != null) {
            FirebaseCrashlytics.getInstance().recordException(t)
        }
    }
}

