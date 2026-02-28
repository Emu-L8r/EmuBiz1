package com.emul8r.bizap.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ThemeSettingsScreen(viewModel: ThemeViewModel = hiltViewModel()) {
    val config by viewModel.themeConfig.collectAsStateWithLifecycle()
    val colors = listOf("#6750A4", "#006A60", "#984061", "#3B608F", "#55624C")

    Column(modifier = Modifier.padding(16.dp)) {
        Text("App Appearance", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        
        Text("Select Brand Colour", style = MaterialTheme.typography.labelLarge)
        Row(
            Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            colors.forEach { hex ->
                val color = Color(android.graphics.Color.parseColor(hex))
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { viewModel.updateSeedColor(hex) },
                    shape = CircleShape,
                    color = color,
                    border = if (config.seedColorHex == hex) 
                             BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) 
                             else null
                ) {}
            }
        }
    }
}
