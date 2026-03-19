package com.emul8r.bizap.ui.gui2.settings

import androidx.compose.runtime.Composable
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.settings.BusinessProfileScreen

@Composable
fun BusinessProfileScreenV2(
    onBack: () -> Unit
) {
    BusinessProfileScreen(
        guiMode = GuiMode.GUI2,
        onBack = onBack
    )
}
