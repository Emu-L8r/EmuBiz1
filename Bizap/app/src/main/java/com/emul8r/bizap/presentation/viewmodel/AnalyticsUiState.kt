package com.emul8r.bizap.presentation.viewmodel

import com.emul8r.bizap.data.model.AnalyticsData

/**
 * UI state for the analytics screen.
 *
 * Using a sealed class guarantees exhaustive `when` expressions in the UI
 * layer, eliminating the need for an `else` branch.
 *
 * [Loading] is emitted immediately on collection before the first DB result
 * arrives. [Success] carries the fully-aggregated [AnalyticsData]. [Error]
 * surfaces any exception that propagated from the data layer.
 */
sealed class AnalyticsUiState {
    /** Initial state while data is being loaded. */
    object Loading : AnalyticsUiState()

    /** All metrics loaded successfully. */
    data class Success(val data: AnalyticsData) : AnalyticsUiState()

    /** An error occurred while loading analytics data. */
    data class Error(val message: String) : AnalyticsUiState()
}
