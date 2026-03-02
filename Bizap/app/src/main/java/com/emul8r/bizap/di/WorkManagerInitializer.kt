package com.emul8r.bizap.di

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.Initializer
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
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
            val entryPoint = EntryPointAccessors.fromApplication(
                context,
                WorkManagerEntryPoint::class.java
            )
            val workerFactory = entryPoint.getWorkerFactory()

            WorkManager.initialize(
                context,
                Configuration.Builder()
                    .setWorkerFactory(workerFactory)
                    .build()
            )
            return WorkManager.getInstance(context)
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize WorkManager with Hilt")
            throw e
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

