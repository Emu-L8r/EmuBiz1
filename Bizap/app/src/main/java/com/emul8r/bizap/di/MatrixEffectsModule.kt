package com.emul8r.bizap.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped
import com.emul8r.bizap.ui.gui3.components.effects.*

/**
 * Matrix Effects DI Module
 *
 * Registers all GPU effects and pipeline with Hilt
 * Scoped to ActivityComponent (one instance per activity)
 */
@Module
@InstallIn(ActivityComponent::class)
object MatrixEffectsModule {

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
        registry: EffectRegistry
    ): MatrixEffectsPipeline {
        return MatrixEffectsPipeline(registry)
    }
}

