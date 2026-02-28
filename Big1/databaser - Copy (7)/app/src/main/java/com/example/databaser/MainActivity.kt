package com.example.databaser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.databaser.ui.theme.DatabaserTheme
import com.example.databaser.viewmodel.SettingsViewModel
import com.example.databaser.viewmodel.ThemeViewModel
import kotlinx.coroutines.launch

private val mainScreens = listOf(
    Screen.Dashboard,
    Screen.CustomerList,
    Screen.AllInvoices,
    Screen.AllQuotes,
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Show a loading UI until the AppContainer is ready
        setContent {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        lifecycleScope.launch {
            val app = application as MyApplication
            app.awaitContainer() // suspend until container ready

            // Now set the actual content
            setContent {
                val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
                val useDarkMode by settingsViewModel.useDarkMode.collectAsStateWithLifecycle()
                val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
                val theme by themeViewModel.theme.collectAsStateWithLifecycle()
                val windowSizeClass = calculateWindowSizeClass(this@MainActivity)

                val darkTheme = when (useDarkMode) {
                    true -> true
                    false -> false
                    else -> isSystemInDarkTheme()
                }

                DatabaserTheme(darkTheme = darkTheme, theme = theme) {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            val isMainScreen = mainScreens.any { it.route == currentDestination?.route }
                            if (isMainScreen) {
                                AppBottomNavigation(navController = navController)
                            }
                        }
                    ) { innerPadding ->
                        NavGraph(navController = navController, modifier = Modifier.padding(innerPadding), windowSizeClass = windowSizeClass)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomNavigation(navController: NavController) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        mainScreens.forEach { screen ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                icon = { Icon(screen.icon!!, contentDescription = stringResource(screen.resourceId!!)) },
                label = { Text(stringResource(screen.resourceId!!)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}