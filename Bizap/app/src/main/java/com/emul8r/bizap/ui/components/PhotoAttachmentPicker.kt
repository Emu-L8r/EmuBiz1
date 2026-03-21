package com.emul8r.bizap.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.theme.ThemeManager
import com.emul8r.bizap.ui.components.classic.ClassicPhotoAttachmentPicker
import com.emul8r.bizap.ui.components.modern.ModernPhotoAttachmentPicker

@Composable
fun PhotoAttachmentPicker(
    photos: List<String>,
    onPhotosChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val themeManager: ThemeManager = hiltViewModel()
    val theme = themeManager.theme.collectAsStateWithLifecycle().value

    when (theme) {
        AppTheme.CLASSIC -> ClassicPhotoAttachmentPicker(photos, onPhotosChange, modifier)
        AppTheme.MODERN -> ModernPhotoAttachmentPicker(photos, onPhotosChange, modifier)
    }
}

