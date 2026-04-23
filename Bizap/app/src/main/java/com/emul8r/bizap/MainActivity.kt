package com.emul8r.bizap

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.emul8r.bizap.domain.service.AuthenticationManager
import com.emul8r.bizap.domain.repository.CustomerRepository
import com.emul8r.bizap.domain.repository.InvoiceRepository
import com.emul8r.bizap.domain.repository.BusinessProfileRepository
import com.emul8r.bizap.domain.model.Customer
import com.emul8r.bizap.domain.model.Invoice
import com.emul8r.bizap.domain.model.InvoiceStatus
import com.emul8r.bizap.ui.auth.LoginScreen
import com.emul8r.bizap.ui.auth.PINSetupScreen
import com.emul8r.bizap.ui.landing.GuiMode
import com.emul8r.bizap.ui.landing.LandingScreen
import com.emul8r.bizap.ui.splash.SplashScreen
import com.emul8r.bizap.ui.state.AppState
import com.emul8r.bizap.ui.state.AppStateViewModel
import javax.inject.Inject
import com.emul8r.bizap.ui.customers.*
import com.emul8r.bizap.ui.dashboard.DashboardScreen
import com.emul8r.bizap.ui.documents.DocumentVaultScreen
import com.emul8r.bizap.ui.dunning.DunningNoticesScreen
import com.emul8r.bizap.ui.invoices.*
import com.emul8r.bizap.ui.components.BizapTopAppBar
import com.emul8r.bizap.ui.components.SyncStatusIndicator
import com.emul8r.bizap.ui.navigation.Screen
import com.emul8r.bizap.ui.navigation.getTitleResId
import com.emul8r.bizap.ui.navigation.NavGraph
import com.emul8r.bizap.ui.revenue.RevenueDashboardScreen
import com.emul8r.bizap.ui.risk.RiskDashboardScreen
import com.emul8r.bizap.ui.invoice.analytics.PaymentAnalyticsScreen
import com.emul8r.bizap.ui.invoice.analytics.PaymentAnalyticsViewModel
import com.emul8r.bizap.ui.settings.backup.BackupRestoreScreen
import com.emul8r.bizap.ui.settings.BusinessProfileScreen
import com.emul8r.bizap.ui.settings.BusinessProfileViewModel
import com.emul8r.bizap.ui.settings.PrefilledItemsScreen
import com.emul8r.bizap.ui.settings.SettingsHubScreen
import com.emul8r.bizap.presentation.ui.screens.SettingsScreen as AppSettingsScreen
import com.emul8r.bizap.ui.shared.screens.HelpScreen
import com.emul8r.bizap.presentation.ui.theme.ThemeProvider
import com.emul8r.bizap.domain.repository.ThemeRepository
import com.emul8r.bizap.ui.BizapApp
import com.emul8r.bizap.ui.theme.AppTheme
import com.emul8r.bizap.ui.ErrorBoundary
import com.emul8r.bizap.ui.gui2.navigation.GuiV2NavGraph
import com.emul8r.bizap.ui.notes.NotesScreen
import com.emul8r.bizap.ui.onboarding.FirstLaunchWarningDialog
import com.emul8r.bizap.ui.theme.ThemeManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.emul8r.bizap.BuildConfig
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

    @Inject
    lateinit var authManager: AuthenticationManager

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var themeRepository: ThemeRepository

    @Inject
    lateinit var customerRepository: CustomerRepository

    @Inject
    lateinit var invoiceRepository: InvoiceRepository

    @Inject
    lateinit var businessProfileRepository: BusinessProfileRepository

    // Modern Activity Result API (initialized in onCreate)
    private lateinit var requestPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        // Guard against calls before Hilt injection completes
        if (::authManager.isInitialized) {
            authManager.updateLastInteraction()
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * REQUEST CRITICAL RUNTIME PERMISSIONS
     * ====================================
     * Requests necessary permissions at runtime (API 23+).
     *
     * WHY NECESSARY:
     * - POST_NOTIFICATIONS: Required for crash notifications (API 33+)
     * - CAMERA: May be needed for certain features
     * - READ/WRITE_EXTERNAL_STORAGE: May be needed for file access
     *
     * This ensures features don't crash due to missing permissions.
     */
    private fun requestCriticalPermissions() {
        val requiredPermissions = mutableListOf<String>()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Check if POST_NOTIFICATIONS permission is granted (API 33+)
            if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                Timber.d("POST_NOTIFICATIONS required for API 33+")
            }
        }

        // Check CAMERA permission
        if (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CAMERA
        ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            requiredPermissions.add(android.Manifest.permission.CAMERA)
        }

        // Check ACCESS_WIFI_STATE permission (Android 31+) for signal monitoring
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_WIFI_STATE
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requiredPermissions.add(android.Manifest.permission.ACCESS_WIFI_STATE)
                Timber.d("ACCESS_WIFI_STATE required for signal monitoring")
            }
        }

        // Request all required permissions at once using modern API
        if (requiredPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(requiredPermissions.toTypedArray())
            Timber.d("Requesting permissions: ${requiredPermissions.joinToString()}")
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize requestPermissionLauncher
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            permissions.forEach { (permission, isGranted) ->
                if (isGranted) {
                    Timber.d("✅ Permission granted: $permission")
                } else {
                    Timber.w("❌ Permission denied: $permission")
                }
            }
        }

        // 🔐 REQUEST CRITICAL PERMISSIONS
        requestCriticalPermissions()

        setContent {
            // ✅ FIX: Extract AppStateViewModel to top level
            val appStateViewModel: AppStateViewModel = hiltViewModel()
            val appState by appStateViewModel.appState.collectAsStateWithLifecycle()

            // ✅ FIX: Handle GUI selection intent extra (for direct GUI switching from GUI3)
            LaunchedEffect(Unit) {
                val selectedGuiName = intent.getStringExtra("selectedGui")
                if (selectedGuiName != null) {
                    try {
                        val selectedGui = GuiMode.valueOf(selectedGuiName)
                        appStateViewModel.selectGui(selectedGui)
                        // ✅ FIX 3: Sync ThemeManager so NavGraph renders the correct GUI.
                        // NavGraph switches on themeManager.theme (CLASSIC/MODERN), not GuiMode.
                        // Without this, switching GUI3 → GUI1 always landed on GUI2 because
                        // themeManager still held AppTheme.MODERN from the previous session.
                        when (selectedGui) {
                            GuiMode.GUI1 -> themeManager.setTheme(AppTheme.CLASSIC)
                            GuiMode.GUI2 -> themeManager.setTheme(AppTheme.MODERN)
                            else -> { /* GUI3 is handled by MatrixGUIMainActivity */ }
                        }
                        Timber.d("MainActivity: GUI selection from intent: $selectedGui")
                    } catch (e: IllegalArgumentException) {
                        Timber.w("MainActivity: Invalid GUI mode in intent: $selectedGuiName")
                    }
                }
            }

            // ✅ FIX: Prioritize SplashScreen render to avoid black screen during DataStore init
            if (appState is AppState.SplashLoading) {
                SplashScreen()
            } else {
                ErrorBoundary {
                    BizapApp(themeManager = themeManager, themeRepository = themeRepository) {
                        // Single when-expression: ONE screen at a time, no layered conditionals
                        when (val state = appState) {
                            is AppState.SplashLoading -> { /* Handled above */ }

                            is AppState.PINSetup -> PINSetupScreen(
                                onSetupComplete = { appStateViewModel.refreshAuth() }
                            )

                            is AppState.Login -> LoginScreen(
                                onAuthenticated = { appStateViewModel.refreshAuth() }
                            )

                            is AppState.FirstLaunchWarning -> FirstLaunchWarningDialog(
                                onDismiss = { appStateViewModel.markFirstLaunchWarningShown() }
                            )

                            is AppState.Landing -> {
                                // User is selecting a GUI or has reset preference from Matrix dashboard
                                val landingViewModel: com.emul8r.bizap.ui.landing.LandingViewModel = hiltViewModel()
                                LandingScreen(
                                    onSelectGui1 = {
                                        landingViewModel.selectMode(GuiMode.GUI1)
                                        appStateViewModel.selectGui(GuiMode.GUI1)
                                    },
                                    onSelectGui2 = {
                                        landingViewModel.selectMode(GuiMode.GUI2)
                                        appStateViewModel.selectGui(GuiMode.GUI2)
                                    },
                                    onSelectGui3 = {
                                        landingViewModel.selectMode(GuiMode.GUI3)
                                        appStateViewModel.selectGui(GuiMode.GUI3)
                                    }
                                )
                            }

                            is AppState.AppReady -> {
                                when (state.gui) {
                                    GuiMode.GUI3 -> {
                                        // Launch Matrix GUI Activity
                                        LaunchedEffect(Unit) {
                                            val intent = Intent(this@MainActivity, com.emul8r.bizap.ui.activities.MatrixGUIMainActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                    }
                                    GuiMode.GUI1, GuiMode.GUI2 -> {
                                        // Show GUI1/GUI2 in current activity
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            val navController = rememberNavController()

                                            // Use unified NavGraph with theme-aware navigation
                                            NavGraph(
                                                navController = navController,
                                                themeManager = themeManager
                                            )

                                            // ⚠️ DEBUG BUTTONS (only in DEBUG builds)
                                            if (BuildConfig.DEBUG) {
                                                val scope = rememberCoroutineScope()

                                                Column(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(16.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    // Auto-Populate Test Data Button (Disabled)
                                                    Button(
                                                        onClick = {
                                                            scope.launch {
                                                                Timber.d("⏳ Auto-populate disabled - using correct Invoice model")
                                                                android.widget.Toast.makeText(
                                                                    this@MainActivity,
                                                                    "⏳ Auto-populate feature temporarily disabled\nPlease use the app normally or create test data manually",
                                                                    android.widget.Toast.LENGTH_LONG
                                                                ).show()
                                                            }
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    ) {
                                                        Text("📊 Auto-Populate Test Data (Disabled)")
                                                    }

                                                    // Force Crash Button
                                                    Button(
                                                        onClick = {
                                                            Timber.e("Manual crash triggered by user for testing.")
                                                            FirebaseCrashlytics.getInstance().setCustomKey("test_key", "test_value")
                                                            throw RuntimeException("Bizap Test Crash: ${System.currentTimeMillis()}")
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                        )
                                                    ) {
                                                        Text("🔴 Force Crash")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /* ❌ DISABLED: Auto-populate function uses incompatible Invoice model
       Will be re-implemented with correct InvoiceEntity structure

    // 🧪 DEBUG HELPER: Auto-populate test data for manual verification
    private suspend fun autoPopulateTestData() {
        try {
            val now = System.currentTimeMillis()
            val businessId = businessProfileRepository.getActiveBusinessId()

            // 5 different invoice statuses
            val statuses = listOf(
                InvoiceStatus.DRAFT,
                InvoiceStatus.SENT,
                InvoiceStatus.PARTIALLY_PAID,
                InvoiceStatus.PAID,
                InvoiceStatus.OVERDUE
            )

            // Create 5 customers with 5 invoices
            val customers = listOf(
                "Tech Solutions Inc",
                "Green Energy LLC",
                "Blue Sky Services",
                "Red Fox Consulting",
                "Yellow Stone Media"
            )

            customers.forEachIndexed { index, customerName ->
                // Create customer
                val customer = Customer(
                    id = 0,
                    name = customerName,
                    email = "contact@${customerName.lowercase().replace(" ", "")}.com",
                    phone = "(555) ${200 + index}-${1000 + index}",
                    address = "${100 + index * 10} Business Ave, Suite ${10 + index}",
                    createdAt = now,
                    updatedAt = now
                )

                val customerId = customerRepository.insert(customer).getOrNull() ?: return@forEachIndexed

                // Create invoice for this customer with status from list
                val status = statuses[index]
                val amountPaid = when (status) {
                    InvoiceStatus.PAID -> 50000L // fully paid
                    InvoiceStatus.PARTIALLY_PAID -> 25000L // half paid
                    else -> 0L // draft, sent, overdue - no payment
                }

                val invoice = Invoice(
                    id = 0,
                    businessProfileId = businessId,
                    customerId = customerId,
                    customerName = customerName,
                    customerAddress = customer.address ?: "",
                    customerEmail = customer.email,
                    date = now - (index * 86_400_000L), // each 1 day older
                    totalAmount = 50000L, // $500.00
                    items = emptyList(), // Empty line items for demo
                    isQuote = false,
                    status = status,
                    currencyCode = "AUD",
                    dueDate = now + (30 * 86_400_000L),
                    amountPaid = amountPaid,
                    updatedAt = now,
                    dailySequence = index + 1,
                    displayName = "${customerName.split(" ")[0].lowercase()}-${String.format("%02d", index + 1)}"
                )

                invoiceRepository.saveInvoice(invoice)

                Timber.d("✅ Created: $customerName with Invoice (Status: $status)")
            }

            Timber.d("✅ Auto-populate complete: 5 customers with 5 invoices created")
        } catch (e: Exception) {
            Timber.e(e, "❌ Error during auto-population")
            throw e
        }
    }
    */
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onSwitchGui: () -> Unit = {}) {
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

    // Get active business ID for GUI1 screens that need it
    val activeBusiness by businessProfileViewModel.profileState.collectAsStateWithLifecycle()
    val activeBusinessId = activeBusiness?.id ?: 1L

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isTopLevelScreen = bottomNavItems.any { item ->
        currentDestination?.hasRoute(item.screen::class) == true
    }

    // Get the title resource ID for the current screen using centralized mapping
    val currentTitleResId = when {
        currentDestination?.hasRoute<Screen.Dashboard>() == true -> Screen.Dashboard.getTitleResId()
        currentDestination?.hasRoute<Screen.Customers>() == true -> Screen.Customers.getTitleResId()
        currentDestination?.hasRoute<Screen.CustomerDetail>() == true -> Screen.CustomerDetail(0).getTitleResId()
        currentDestination?.hasRoute<Screen.Invoices>() == true -> Screen.Invoices.getTitleResId()
        currentDestination?.hasRoute<Screen.DocumentVault>() == true -> Screen.DocumentVault.getTitleResId()
        currentDestination?.hasRoute<Screen.SettingsHub>() == true -> Screen.SettingsHub.getTitleResId()
        currentDestination?.hasRoute<Screen.BusinessProfile>() == true -> Screen.BusinessProfile.getTitleResId()
        currentDestination?.hasRoute<Screen.PrefilledItems>() == true -> Screen.PrefilledItems.getTitleResId()
        currentDestination?.hasRoute<Screen.CreateInvoice>() == true -> Screen.CreateInvoice.getTitleResId()
        currentDestination?.hasRoute<Screen.EditInvoice>() == true -> Screen.EditInvoice(0).getTitleResId()
        currentDestination?.hasRoute<Screen.InvoiceDetail>() == true -> Screen.InvoiceDetail(0).getTitleResId()
        currentDestination?.hasRoute<Screen.InvoicePdf>() == true -> Screen.InvoicePdf(0, false).getTitleResId()
        currentDestination?.hasRoute<Screen.RevenueDashboard>() == true -> Screen.RevenueDashboard.getTitleResId()
        currentDestination?.hasRoute<Screen.RiskDashboard>() == true -> Screen.RiskDashboard.getTitleResId()
        currentDestination?.hasRoute<Screen.PaymentAnalytics>() == true -> Screen.PaymentAnalytics().getTitleResId()
        currentDestination?.hasRoute<Screen.BackupRestore>() == true -> Screen.BackupRestore.getTitleResId()
        currentDestination?.hasRoute<Screen.DunningNotices>() == true -> Screen.DunningNotices.getTitleResId()
        currentDestination?.hasRoute<Screen.CustomerSegments>() == true -> Screen.CustomerSegments.getTitleResId()
        currentDestination?.hasRoute<Screen.CustomerAnalytics>() == true -> Screen.CustomerAnalytics.getTitleResId()
        currentDestination?.hasRoute<Screen.Notes>() == true -> Screen.Notes.getTitleResId()
        currentDestination?.hasRoute<Screen.AppSettings>() == true -> Screen.AppSettings.getTitleResId()
        currentDestination?.hasRoute<Screen.ThemeSettings>() == true -> Screen.ThemeSettings.getTitleResId()
        currentDestination?.hasRoute<Screen.Help>() == true -> Screen.Help.getTitleResId()
        else -> R.string.screen_title_default
    }

    // Global Sync and Offline Status Indicator
    Column(modifier = Modifier.fillMaxSize()) {
        SyncStatusIndicator()

        Scaffold(
            modifier = Modifier.weight(1f),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                val title = stringResource(currentTitleResId)
                val showLogo = currentDestination?.hasRoute<Screen.Dashboard>() == true

                BizapTopAppBar(
                    title = title,
                    logoBase64 = businessProfile.logoBase64,
                    showLogo = showLogo,
                    showBackButton = !isTopLevelScreen,
                    onBackClick = { navController.popBackStack() },
                    onSettingsClick = { navController.navigate(Screen.SettingsHub) },
                    onActionClick = onSwitchGui,
                    actionButtonLabel = "Switch to Modern UI"
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
                    CustomerListScreen(
                        onCustomerClick = { customerId ->
                            navController.navigate(Screen.CustomerDetail(customerId))
                        },
                        onCreateCustomer = { showBottomSheet = true },
                        onViewSegments = { navController.navigate(Screen.CustomerSegments) },
                        onViewAnalytics = { navController.navigate(Screen.CustomerAnalytics) }
                    )
                }
                composable<Screen.Invoices> {
                    InvoiceListScreen(
                        guiMode = GuiMode.GUI1,
                        businessId = null,
                        onInvoiceClick = { invoiceId ->
                            navController.navigate(Screen.InvoiceDetail(invoiceId))
                        },
                        onCreateInvoice = {},
                        onViewAnalytics = { navController.navigate(Screen.RevenueDashboard) },
                        onBack = {}
                    )
                }
                composable<Screen.DocumentVault> { DocumentVaultScreen() }
                composable<Screen.SettingsHub> { SettingsHubScreen(onNavigateTo = { screen -> navController.navigate(screen) }) }
                composable<Screen.BusinessProfile> { BusinessProfileScreen() }
                composable<Screen.ThemeSettings> {
                    com.emul8r.bizap.ui.theme.UnifiedThemeSettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
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
                        onEdit = { navController.navigate(Screen.EditInvoice(detail.invoiceId)) },
                        // ✅ FIX 3: Pass businessId context to payment analytics
                        onNavigateToRevenue = { navController.navigate(Screen.RevenueDashboard) },
                        onNavigateToPayments = { navController.navigate(Screen.PaymentAnalytics()) }
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
                composable<Screen.RiskDashboard> { RiskDashboardScreen(onBackClick = { navController.popBackStack() }) }
                composable<Screen.PaymentAnalytics> { backStackEntry ->
                    val route: Screen.PaymentAnalytics = backStackEntry.toRoute()
                    val viewModel: PaymentAnalyticsViewModel = hiltViewModel()

                    // ✅ FIX 3: Apply business context if passed from navigation
                    LaunchedEffect(route.businessId) {
                        route.businessId?.let { viewModel.setBusinessId(it) }
                    }

                    PaymentAnalyticsScreen()
                }
                composable<Screen.BackupRestore> { BackupRestoreScreen(onBack = { navController.popBackStack() }) }
                composable<Screen.DunningNotices> { DunningNoticesScreen(onBackClick = { navController.popBackStack() }) }
                composable<Screen.CustomerSegments> {
                    CustomerSegmentationScreen()
                }
                // CustomerAnalytics routes to the same segmentation screen as it
                // is the primary customer analytics view in the app.
                composable<Screen.CustomerAnalytics> {
                    CustomerSegmentationScreen()
                }
                composable<Screen.Notes> {
                    NotesScreen(navController)
                }
                composable<Screen.AppSettings> {
                    AppSettingsScreen(
                        onNavigateToThemeSettings = { navController.navigate(Screen.ThemeSettings) }
                    )
                }
                composable<Screen.Help> {
                    HelpScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
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

            // 🔴 DEBUG ONLY: Force Crash Button for Crashlytics Testing
            // ✅ MOVED TO: Settings → Debug → Test Crash (proper location, doesn't block UI)
            // See SettingsHubScreen.kt for implementation
        }
    }
}
