package com.example.databaser

import android.app.Application
import com.example.databaser.data.AppContainer
import com.example.databaser.data.AppDataContainer

class DatabaserApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
