package com.emul8r.bizap.ui.settings

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.data.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.model.BusinessProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: BusinessProfileRepository,
    private val application: Application
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val profileState: StateFlow<BusinessProfile> = repository.profile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BusinessProfile()
        )

    var hasLogo by mutableStateOf(false)
        private set

    fun updateProfile(newProfile: BusinessProfile) {
        viewModelScope.launch {
            repository.updateProfile(newProfile)
        }
    }

    fun saveLogo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Create a permanent internal destination
                val brandingDir = File(application.filesDir, "branding")
                if (!brandingDir.exists()) brandingDir.mkdirs()

                val localFile = File(brandingDir, "logo_main.png")

                // 2. Open Stream from the "Untrusted" URI and Copy
                application.contentResolver.openInputStream(uri)?.use { input ->
                    localFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // 3. Update the Repository with the LOCAL path
                repository.updateLogoPath(localFile.absolutePath)

                _uiEvent.emit(UiEvent.ShowSnackbar("Logo updated successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Failed to process image"))
            }
        }
    }
}

