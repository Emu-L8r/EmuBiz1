package com.emul8r.bizap.di

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import timber.log.Timber

/**
 * Initializer for WorkManager with Hilt support
 * This tells WorkManager to use HiltWorkerFactory for dependency injection
 */
class WorkManagerInitializer : Initializer<WorkManager> {

    override fun create(context: Context): WorkManager {
        val config = Configuration.Builder()
            .setWorkerFactory(HiltWorkerFactory())
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

        WorkManager.initialize(context, config)
        Timber.d("✅ WorkManager initialized with HiltWorkerFactory")

        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // WorkManager should initialize after Hilt
        return emptyList()
    }
}



