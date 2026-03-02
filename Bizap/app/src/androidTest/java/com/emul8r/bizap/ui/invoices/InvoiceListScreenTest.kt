package com.emul8r.bizap.ui.invoices

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.emul8r.bizap.domain.model.ThemeConfig
import com.emul8r.bizap.ui.theme.BizapTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI tests for the Sync Indicator Badge in InvoiceListScreen.
 * Verifies visual state transitions for Offline, Pending, and Synced states.
 */
class InvoiceListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val defaultThemeConfig = ThemeConfig()

    @Test
    fun syncIndicator_showsOfflineBadge_whenNotOnline() {
        // Arrange
        val syncState = SyncUiState(isOnline = false, pendingCount = 0)

        // Act
        composeTestRule.setContent {
            BizapTheme(themeConfig = defaultThemeConfig) {
                SyncStatusIndicator(syncState = syncState)
            }
        }

        // Assert
        composeTestRule.onNodeWithTag("SyncStatusIndicator").assertIsDisplayed()
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
    }

    @Test
    fun syncIndicator_showsPendingBadge_whenOnlineWithUnsyncedOps() {
        // Arrange
        val syncState = SyncUiState(isOnline = true, pendingCount = 5)

        // Act
        composeTestRule.setContent {
            BizapTheme(themeConfig = defaultThemeConfig) {
                SyncStatusIndicator(syncState = syncState)
            }
        }

        // Assert
        composeTestRule.onNodeWithTag("SyncStatusIndicator").assertIsDisplayed()
        composeTestRule.onNodeWithText("5 operations pending").assertIsDisplayed()
    }

    @Test
    fun syncIndicator_hidesBadge_whenOnlineAndNoPendingOps() {
        // Arrange
        val syncState = SyncUiState(isOnline = true, pendingCount = 0)

        // Act
        composeTestRule.setContent {
            BizapTheme(themeConfig = defaultThemeConfig) {
                SyncStatusIndicator(syncState = syncState)
            }
        }

        // Assert
        // AnimatedVisibility should hide the node when visible is false
        composeTestRule.onNodeWithTag("SyncStatusIndicator").assertDoesNotExist()
    }

    @Test
    fun syncIndicator_prioritizesOfflineOverPending() {
        // Arrange: Both offline and having pending ops
        val syncState = SyncUiState(isOnline = false, pendingCount = 3)

        // Act
        composeTestRule.setContent {
            BizapTheme(themeConfig = defaultThemeConfig) {
                SyncStatusIndicator(syncState = syncState)
            }
        }

        // Assert: Should show "Offline" as it's the more critical state
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 operations pending").assertDoesNotExist()
    }

    @Test
    fun syncIndicator_transitionsStatesCorrectly() {
        // Start with a mutable state to simulate transitions
        var currentState by mutableStateOf(SyncUiState(isOnline = true, pendingCount = 0))

        composeTestRule.setContent {
            BizapTheme(themeConfig = defaultThemeConfig) {
                SyncStatusIndicator(syncState = currentState)
            }
        }

        // 1. Initially hidden
        composeTestRule.onNodeWithTag("SyncStatusIndicator").assertDoesNotExist()

        // 2. Go Offline
        currentState = SyncUiState(isOnline = false, pendingCount = 0)
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed()

        // 3. Add pending ops (still offline)
        currentState = SyncUiState(isOnline = false, pendingCount = 1)
        composeTestRule.onNodeWithText("Offline").assertIsDisplayed() // Still shows offline

        // 4. Reconnect (now shows pending)
        currentState = SyncUiState(isOnline = true, pendingCount = 1)
        composeTestRule.onNodeWithText("1 operations pending").assertIsDisplayed()

        // 5. Sync complete
        currentState = SyncUiState(isOnline = true, pendingCount = 0)
        composeTestRule.onNodeWithTag("SyncStatusIndicator").assertDoesNotExist()
    }
}
