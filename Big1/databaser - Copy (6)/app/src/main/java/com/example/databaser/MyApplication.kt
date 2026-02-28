package com.example.databaser

import android.app.Application
import net.sqlcipher.database.SQLiteDatabase

class MyApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        SQLiteDatabase.loadLibs(this)
        container = AppDataContainer(this)
    }
}
