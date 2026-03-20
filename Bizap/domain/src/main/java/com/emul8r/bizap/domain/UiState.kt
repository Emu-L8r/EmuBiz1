package com.emul8r.bizap.domain

/**
 * Generic UI state sealed class used by ViewModels that need to represent
 * loading, success, and error states for a single data type [T].
 *
 * Usage in a ViewModel:
 * ```
 * val uiState: StateFlow<UiState<List<Invoice>>> =
 *     repository.observe()
 *         .map { result ->
 *             result.fold(
 *                 onSuccess = { UiState.Success(it) },
 *                 onFailure = { UiState.Error(it.message ?: "Unknown error") }
 *             )
 *         }
 *         .stateIn(viewModelScope, SharingStarted.Eagerly, UiState.Loading())
 * ```
 */
sealed class UiState<out T> {
    /** Data is being loaded. */
    class Loading<T> : UiState<T>()

    /** Data loaded successfully. */
    data class Success<T>(val data: T) : UiState<T>()

    /** An error occurred while loading data. */
    data class Error<T>(val message: String) : UiState<T>()
}
