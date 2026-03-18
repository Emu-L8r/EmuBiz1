package com.emul8r.bizap.presentation.ui.components.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.BuildConfig

/**
 * Settings card that displays app version, build type, and other meta information.
 */
@Composable
fun AboutSettingsCard(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            AboutRow(label = "App version", value = BuildConfig.VERSION_NAME)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            AboutRow(label = "Build type", value = BuildConfig.BUILD_TYPE)
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            AboutRow(label = "Package", value = BuildConfig.APPLICATION_ID)
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
