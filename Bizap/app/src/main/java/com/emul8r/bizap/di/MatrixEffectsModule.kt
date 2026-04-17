package com.emul8r.bizap.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import com.emul8r.bizap.ui.gui3.util.RainParticleEffect
import com.emul8r.bizap.ui.gui3.util.GlitchEffect
import com.emul8r.bizap.ui.gui3.util.ScanlineEffect
import com.emul8r.bizap.ui.gui3.components.effects.EffectRegistry
import com.emul8r.bizap.ui.gui3.components.effects.MatrixEffectsPipeline
import com.emul8r.bizap.ui.gui3.util.PerformanceProfiler
import com.emul8r.bizap.ui.gui3.util.AdaptivePerformanceManager
import com.emul8r.bizap.utils.FirebaseEventTracker

/**
 * Matrix Effects DI Module
 *
 * Registers all GPU effects and pipeline with Hilt
 * Scoped to ActivityComponent (one instance per activity)
 *
 * ✅ NEW: Provides SharedPreferences for effects persistence
 */
@Module
@InstallIn(SingletonComponent::class)
object MatrixEffectsModule {

    // ✅ NEW: Provide SharedPreferences for matrix effects (singleton, separate file)
    @Singleton
    @Named("matrix_effects_prefs")
    @Provides
    fun provideMatrixEffectsPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("matrix_effects_prefs", Context.MODE_PRIVATE)
    }

    // ✅ NEW: Provide AdaptivePerformanceManager as singleton with SharedPreferences backing
    @Singleton
    @Provides
    fun provideAdaptivePerformanceManager(
        eventTracker: FirebaseEventTracker?,
        @Named("matrix_effects_prefs") prefs: SharedPreferences
    ): AdaptivePerformanceManager {
        return AdaptivePerformanceManager(eventTracker, prefs)
    }
}

/**
 * Activity-scoped effects components (rendering pipeline)
 */
@Module
@InstallIn(ActivityComponent::class)
object MatrixEffectsActivityModule {

    @ActivityScoped
    @Provides
    fun providePerformanceProfiler(
        eventTracker: FirebaseEventTracker?
    ): PerformanceProfiler {
        val crashlytics = try {
            com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance()
        } catch (e: Exception) {
            null
        }
        return PerformanceProfiler(crashlytics, eventTracker)
    }

    @ActivityScoped
    @Provides
    fun provideRainParticleEffect(): RainParticleEffect {
        return RainParticleEffect()
    }

    @ActivityScoped
    @Provides
    fun provideGlitchEffect(): GlitchEffect {
        return GlitchEffect()
    }

    @ActivityScoped
    @Provides
    fun provideScanlineEffect(): ScanlineEffect {
        return ScanlineEffect()
    }

    @ActivityScoped
    @Provides
    fun provideEffectRegistry(
        rain: RainParticleEffect,
        glitch: GlitchEffect,
        scanline: ScanlineEffect
    ): EffectRegistry {
        return EffectRegistry(rain, glitch, scanline)
    }

    @ActivityScoped
    @Provides
    fun provideMatrixEffectsPipeline(
        registry: EffectRegistry,
        eventTracker: FirebaseEventTracker?,
        profiler: PerformanceProfiler
    ): MatrixEffectsPipeline {
        return MatrixEffectsPipeline(registry, eventTracker, profiler)
    }
}
