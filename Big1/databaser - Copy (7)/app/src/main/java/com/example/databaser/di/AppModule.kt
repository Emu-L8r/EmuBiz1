package com.example.databaser.di

import android.content.Context
import com.example.databaser.data.AppDatabase
import com.example.databaser.data.PassphraseProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePassphraseProvider(@ApplicationContext context: Context): PassphraseProvider {
        return PassphraseProvider(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context, passphraseProvider: PassphraseProvider): AppDatabase {
        // For now, initialize synchronously using runBlocking to bridge suspend API
        return runBlocking {
            AppDatabase.getDatabase(context, passphraseProvider)
        }
    }
}

