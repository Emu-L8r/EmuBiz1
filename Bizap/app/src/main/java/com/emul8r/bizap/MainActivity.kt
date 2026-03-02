package com.emul8r.bizap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.emul8r.bizap.ui.customers.*
import com.emul8r.bizap.ui.dashboard.DashboardScreen
import com.emul8r.bizap.ui.documents.DocumentVaultScreen
import com.emul8r.bizap.ui.invoices.*
import com.emul8r.bizap.ui.components.BizapTopAppBar
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.ui.revenue.RevenueDashboardScreen
import com.emul8r.bizap.ui.settings.BusinessProfileScreen
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.settings.PrefilledItemsScreen
import com.emul8r.bizap.ui.settings.SettingsHubScreen
import com.emul8r.bizap.ui.settings.ThemeSettingsScreen
import com.emul8r.bizap.ui.settings.ThemeViewModel
import com.emul8r.bizap.ui.theme.EmuBizzzTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

data class NavigationItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val config by themeViewModel.themeConfig.collectAsStateWithLifecycle()

            Timber.d("🎨 Theme recomposed: seedColorHex = ${config.seedColorHex}")

            EmuBizzzTheme(themeConfig = config) {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val businessProfileViewModel: BusinessProfileViewModel = hiltViewModel()
    val businessProfile by businessProfileViewModel.profileState.collectAsStateWithLifecycle()

    val bottomNavItems = listOf(
        NavigationItem(Screen.Dashboard, "Dashboard", Icons.Default.Dashboard),
        NavigationItem(Screen.Customers, "Customers", Icons.Default.People),
        NavigationItem(Screen.Invoices, "Invoices", Icons.Default.Receipt),
        NavigationItem(Screen.DocumentVault, "Vault", Icons.Default.Inventory),
        NavigationItem(Screen.SettingsHub, "Settings", Icons.Default.Settings)
    )

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isTopLevelScreen = bottomNavItems.any { item -> 
        currentDestination?.hasRoute(item.screen::class) == true 
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            val title = when {
                currentDestination?.hasRoute<Screen.Dashboard>() == true -> "Dashboard"
                currentDestination?.hasRoute<Screen.Customers>() == true -> "Customers"
                currentDestination?.hasRoute<Screen.CustomerDetail>() == true -> "Customer Detail"
                currentDestination?.hasRoute<Screen.Invoices>() == true -> "Invoices"
                currentDestination?.hasRoute<Screen.DocumentVault>() == true -> "Document Vault"
                currentDestination?.hasRoute<Screen.SettingsHub>() == true -> "Settings"
                currentDestination?.hasRoute<Screen.BusinessProfile>() == true -> "Business Profile"
                currentDestination?.hasRoute<Screen.ThemeSettings>() == true -> "Theme Settings"
                currentDestination?.hasRoute<Screen.PrefilledItems>() == true -> "Prefilled Items"
                currentDestination?.hasRoute<Screen.CreateInvoice>() == true -> "Create Invoice"
                currentDestination?.hasRoute<Screen.EditInvoice>() == true -> "Edit Invoice"
                currentDestination?.hasRoute<Screen.InvoiceDetail>() == true -> "Invoice Detail"
                currentDestination?.hasRoute<Screen.InvoicePdf>() == true -> "PDF Preview"
                currentDestination?.hasRoute<Screen.RevenueDashboard>() == true -> "Revenue Dashboard"
                else -> "Bizap"
            }

            val showLogo = currentDestination?.hasRoute<Screen.Dashboard>() == true

            BizapTopAppBar(
                title = title,
                logoBase64 = businessProfile.logoBase64,
                showLogo = showLogo,
                showBackButton = !isTopLevelScreen,
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.title) },
                            selected = currentDestination?.hierarchy?.any { it.hasRoute(item.screen::class) } == true,
                            onClick = {
                                navController.navigate(item.screen) {
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
        },
        floatingActionButton = {
            when {
                currentDestination?.hasRoute<Screen.Customers>() == true -> {
                    FloatingActionButton(
                        onClick = { showBottomSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Customer")
                    }
                }
                currentDestination?.hasRoute<Screen.Invoices>() == true -> {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.CreateInvoice) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = "Create Invoice")
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Dashboard,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Dashboard> { DashboardScreen(navController) }
            composable<Screen.Customers> {
                CustomerListScreen(onCustomerClick = { customerId ->
                    navController.navigate(Screen.CustomerDetail(customerId))
                })
            }
            composable<Screen.Invoices> {
                InvoiceListScreen(onInvoiceClick = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetail(invoiceId))
                })
            }
            composable<Screen.DocumentVault> { DocumentVaultScreen() }
            composable<Screen.SettingsHub> { SettingsHubScreen(onNavigate = { screen -> navController.navigate(screen) }) }
            composable<Screen.BusinessProfile> { BusinessProfileScreen() }
            composable<Screen.ThemeSettings> { ThemeSettingsScreen() }
            composable<Screen.PrefilledItems> { PrefilledItemsScreen() }
            composable<Screen.CreateInvoice> { CreateInvoiceScreen(onInvoiceSaved = { navController.popBackStack() }) }
            composable<Screen.EditInvoice> { backStackEntry ->
                val detail: Screen.EditInvoice = backStackEntry.toRoute()
                EditInvoiceScreen(onInvoiceUpdated = { navController.popBackStack() })
            }
            composable<Screen.InvoiceDetail> { backStackEntry ->
                val detail: Screen.InvoiceDetail = backStackEntry.toRoute()
                InvoiceDetailScreen(
                    invoiceId = detail.invoiceId,
                    onEdit = { navController.navigate(Screen.EditInvoice(detail.invoiceId)) }
                )
            }
            composable<Screen.CustomerDetail> { backStackEntry ->
                val detail: Screen.CustomerDetail = backStackEntry.toRoute()
                CustomerDetailScreen(customerId = detail.customerId)
            }
            composable<Screen.InvoicePdf> { backStackEntry ->
                val detail: Screen.InvoicePdf = backStackEntry.toRoute()
                InvoicePdfScreen(invoiceId = detail.invoiceId, isQuote = detail.isQuote)
            }
            composable<Screen.RevenueDashboard> { RevenueDashboardScreen() }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState
            ) {
                val customerViewModel: CustomerViewModel = hiltViewModel()
                AddCustomerForm(
                    viewModel = customerViewModel,
                    onCustomerSaved = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}
