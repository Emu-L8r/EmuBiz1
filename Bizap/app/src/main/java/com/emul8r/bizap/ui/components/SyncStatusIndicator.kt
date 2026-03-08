package com.emul8r.bizap.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.emul8r.bizap.domain.repository.OfflineQueueRepository
import com.emul8r.bizap.utils.ConnectivityHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    offlineQueueRepository: OfflineQueueRepository
) : ViewModel() {

    val pendingCount: StateFlow<Int> = offlineQueueRepository
        .getPendingCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    /**
     * Helper to determine if we should show the "Offline" status.
     * In a real app, this would be a Flow from a NetworkMonitor.
     */
    fun isOnline(context: Context): Boolean = ConnectivityHelper.isNetworkAvailable(context)
}

// ─────────────────────────────────────────────────────────────────────────────
// Composables
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Compact banner displayed at the top of a screen to show sync and connectivity status.
 *
 * Part of Week 1: Foundation Completion.
 */
@Composable
fun SyncStatusIndicator(
    modifier: Modifier = Modifier,
    viewModel: SyncStatusViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    var isOnline by remember { mutableStateOf(viewModel.isOnline(context)) }

    // Periodically check connectivity (in a real app, use a BroadcastReceiver/Flow)
    LaunchedEffect(Unit) {
        while (true) {
            isOnline = viewModel.isOnline(context)
            delay(5000)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        SyncStatusBanner(
            pendingCount = pendingCount,
            isOnline = isOnline
        )
    }
}

@Composable
fun SyncStatusBanner(
    pendingCount: Int,
    isOnline: Boolean,
    modifier: Modifier = Modifier
) {
    // Show banner if offline OR if there are pending changes
    val isVisible = !isOnline || pendingCount > 0

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            color = if (!isOnline) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
            shape = RoundedCornerShape(bottomStart = 0.dp, bottomEnd = 0.dp, topStart = 8.dp, topEnd = 8.dp),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (!isOnline) {
                    Icon(
                        imageVector = Icons.Filled.WifiOff,
                        contentDescription = "Offline",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You are currently offline",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (pendingCount > 0) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (pendingCount == 1) "1 change syncing..." else "$pendingCount changes syncing...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = "Synced",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All changes synced",
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}
