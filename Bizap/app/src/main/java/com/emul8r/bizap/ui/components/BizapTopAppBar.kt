package com.emul8r.bizap.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.R
import com.emul8r.bizap.utils.ImageCompressor

/**
 * Reusable TopAppBar component that displays the business logo (if available)
 * alongside the screen title.
 *
 * @param title The screen title to display
 * @param logoBase64 The Base64-encoded business logo (null if no logo set)
 * @param showLogo Whether to show the logo (default: true on Dashboard, false elsewhere)
 * @param showBackButton Whether to show a back navigation button
 * @param onBackClick Callback for back button click
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BizapTopAppBar(
    title: String,
    logoBase64: String? = null,
    showLogo: Boolean = false,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Show logo if available and enabled
                if (showLogo) {
                    if (logoBase64 != null) {
                        // Load Base64 logo asynchronously to avoid blocking UI thread
                        val logoBitmap = remember { mutableStateOf<android.graphics.Bitmap?>(null) }

                        LaunchedEffect(logoBase64) {
                            logoBitmap.value = ImageCompressor.base64ToBitmap(logoBase64)
                        }

                        logoBitmap.value?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = "Business Logo",
                                modifier = Modifier.size(32.dp),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    } else {
                        // Fallback to default logo if no custom logo set
                        Image(
                            painter = painterResource(R.drawable.company_logo),
                            contentDescription = "Default Logo",
                            modifier = Modifier.size(32.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
                Text(title)
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}


