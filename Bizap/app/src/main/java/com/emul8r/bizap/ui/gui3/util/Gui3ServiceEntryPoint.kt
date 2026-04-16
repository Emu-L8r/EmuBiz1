package com.emul8r.bizap.ui.gui3.util

import com.emul8r.bizap.data.config.FeatureFlagManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface Gui3ServiceEntryPoint {
    fun featureFlagManager(): FeatureFlagManager
    fun adaptivePerformanceManager(): AdaptivePerformanceManager
    fun performanceProfiler(): PerformanceProfiler
}

