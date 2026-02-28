package com.example.databaser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.databaser.data.PassphraseManager
import com.example.databaser.ui.LandingScreen
import com.example.databaser.ui.theme.DatabaserTheme
import com.example.databaser.viewmodel.SettingsViewModel
import com.example.databaser.viewmodel.SettingsViewModelFactory

private val mainScreens = listOf(
    Screen.Dashboard,
    Screen.CustomerList,
    Screen.AllInvoices,
    Screen.AllQuotes,
)

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private lateinit var passphraseManager: PassphraseManager

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        passphraseManager = PassphraseManager(this)

        setContent {
            val settingsViewModel: SettingsViewModel = ViewModelProvider(this, SettingsViewModelFactory((application as DatabaserApplication).container.mySavesRepository)).get(SettingsViewModel::class.java)
            val useDarkMode by settingsViewModel.useDarkMode.collectAsStateWithLifecycle()
            val theme by settingsViewModel.theme.collectAsStateWithLifecycle()
            val windowSizeClass = calculateWindowSizeClass(this)

            val darkTheme = when (theme) {
                "System" -> isSystemInDarkTheme()
                else -> useDarkMode
            }

            var passphraseSet by remember { mutableStateOf(false) }

            if (!passphraseSet) {
                LandingScreen(passphraseManager = passphraseManager) {
                    passphraseSet = true
                }
            } else {
                DatabaserTheme(darkTheme = darkTheme) {
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
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        mainScreens.forEach { screen ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    unselectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.primary,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
