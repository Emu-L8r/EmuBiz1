package com.emul8r.bizap.presentation.ui.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.ThemePreference

/**
 * Settings card for choosing the app colour-scheme (Light / Dark / Auto).
 */
@Composable
fun ThemeSettingsCard(
    currentPreference: ThemePreference,
    onPreferenceSelected: (ThemePreference) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            ThemePreference.entries.forEach { preference ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = preference.label(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    RadioButton(
                        selected = preference == currentPreference,
                        onClick = { onPreferenceSelected(preference) }
                    )
                }
            }
        }
    }
}

private fun ThemePreference.label() = when (this) {
    ThemePreference.LIGHT -> "Light"
    ThemePreference.DARK  -> "Dark"
    ThemePreference.AUTO  -> "Follow system"
}
