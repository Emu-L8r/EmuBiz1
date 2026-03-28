package com.emul8r.bizap.ui.gui2.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Mobile-optimized bottom navigation component.
 *
 * Shows 4-5 main navigation items for phone screens.
 * Automatically hides on larger screens.
 */
@Composable
fun MobileNavigationV2(
    selectedItem: Int = 0,
    onItemSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) }
            )
        }
    }
}

/**
 * Navigation item data class.
 */
data class NavigationItem(
    val label: String,
    val icon: ImageVector
)

/**
 * Default navigation items for mobile.
 */
val navigationItems = listOf(
    NavigationItem(
        label = "Home",
        icon = Icons.Default.Home
    ),
    NavigationItem(
        label = "Invoices",
        icon = Icons.Default.Description
    ),
    NavigationItem(
        label = "Customers",
        icon = Icons.Default.People
    ),
    NavigationItem(
        label = "Analytics",
        icon = Icons.Default.BarChart
    ),
    NavigationItem(
        label = "More",
        icon = Icons.Default.MoreVert
    )
)

