package com.example.databaser

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase

@HiltAndroidApp
class MyApplication : Application() {

    private val appScope = CoroutineScope(Dispatchers.Default)
    private val containerDeferred = CompletableDeferred<AppContainer>()

    override fun onCreate() {
        super.onCreate()
        // Load SQLCipher native libs on a background thread to avoid blocking
        Thread { SQLiteDatabase.loadLibs(this) }.start()

        // Initialize DB and container asynchronously
        appScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@MyApplication)
            val container = AppDataContainer(db)
            containerDeferred.complete(container)
        }
    }

    suspend fun awaitContainer(): AppContainer = containerDeferred.await()
}
