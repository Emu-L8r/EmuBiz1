package com.emul8r.bizap.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import timber.log.Timber

/**
 * Entry point to access HiltWorkerFactory from Hilt
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkManagerEntryPoint {
    fun getWorkerFactory(): HiltWorkerFactory
}

/**
 * Initializer for WorkManager with Hilt support
 * This tells WorkManager to use HiltWorkerFactory for dependency injection
 */
class WorkManagerInitializer : Initializer<WorkManager> {

    override fun create(context: Context): WorkManager {
        try {
            // Get HiltWorkerFactory from Hilt
            val entryPoint = androidx.hilt.android.EntryPointAccessors.fromApplication(
                context,
                WorkManagerEntryPoint::class.java
            )
            val workerFactory = entryPoint.getWorkerFactory()

            val config = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()

            WorkManager.initialize(context, config)
            Timber.d("✅ WorkManager initialized with HiltWorkerFactory")
        } catch (e: Exception) {
            Timber.e("❌ Failed to initialize WorkManager: ${e.message}")
            // Continue without explicit factory - WorkManager will use default
        }

        return WorkManager.getInstance(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}



