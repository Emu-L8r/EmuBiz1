package com.emul8r.bizap.di

import android.content.Context
import com.emul8r.bizap.data.local.PINDataStore
import com.emul8r.bizap.data.local.PINStorage
import com.emul8r.bizap.data.repository.AuthenticationRepositoryImpl
import com.emul8r.bizap.domain.repository.AuthenticationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthenticationRepository(
        impl: AuthenticationRepositoryImpl
    ): AuthenticationRepository

    companion object {
        /**
         * Provide PINStorage (legacy SharedPreferences-based).
         * Kept for compatibility and fallback during migration.
         */
        @Provides
        @Singleton
        fun providePINStorage(
            @ApplicationContext context: Context
        ): PINStorage = PINStorage(context)

        /**
         * Provide PINDataStore (modern async-first implementation).
         * Non-blocking, eliminates 743 StrictMode violations detected in April 2026.
         * Automatically migrates legacy PINs on first read.
         */
        @Provides
        @Singleton
        fun providePINDataStore(
            @ApplicationContext context: Context,
            legacyPINStorage: PINStorage
        ): PINDataStore = PINDataStore(context, legacyPINStorage)
    }
}
