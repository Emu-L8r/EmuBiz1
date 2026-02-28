package com.example.databaser.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.databaser.R
import com.example.databaser.data.DATABASE_NAME
import com.example.databaser.viewmodel.BusinessInfoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessInfoScreen(
    navController: NavHostController,
    onManagePredefinedLineItems: () -> Unit,
    onTrashClick: () -> Unit,
    onBackClick: () -> Unit,
    businessInfoViewModel: BusinessInfoViewModel = hiltViewModel()
) {
    val businessInfo by businessInfoViewModel.businessInfo.collectAsState(initial = null)
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var contactNumber by remember { mutableStateOf("") }
    var abn by remember { mutableStateOf("") }
    var defaultDueDateDays by remember { mutableStateOf("") }
    var dateFormat by remember { mutableStateOf("") }
    var currencySymbol by remember { mutableStateOf("") }

    LaunchedEffect(businessInfo) {
        businessInfo?.let {
            name = it.name
            address = it.address
            email = it.email ?: ""
            logoUri = it.logoPath?.let { uriString -> Uri.parse(uriString) }
            contactNumber = it.contactNumber
            abn = it.abn
            defaultDueDateDays = it.defaultDueDateDays.toString()
            dateFormat = it.dateFormat
            currencySymbol = it.currencySymbol
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { logoUri = it }
    }

    Scaffold(
        topBar = { 
            AppTopAppBar(
                title = stringResource(id = R.string.business_info), 
                navController = navController,
                canNavigateBack = true,
                onNavigateUp = onBackClick
            ) 
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).padding(16.dp)) {
            item {
                BusinessInfoFields(
                    name = name,
                    onNameChange = { name = it },
                    address = address,
                    onAddressChange = { address = it },
                    email = email,
                    onEmailChange = { email = it },
                    logoUri = logoUri,
                    onLogoClick = { imagePickerLauncher.launch("image/*") },
                    contactNumber = contactNumber,
                    onContactNumberChange = { contactNumber = it },
                    abn = abn,
                    onAbnChange = { abn = it },
                    defaultDueDateDays = defaultDueDateDays,
                    onDefaultDueDateDaysChange = { defaultDueDateDays = it },
                    dateFormat = dateFormat,
                    onDateFormatChange = { dateFormat = it },
                    currencySymbol = currencySymbol,
                    onCurrencySymbolChange = { currencySymbol = it }
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    businessInfoViewModel.saveBusinessInfo(
                        name, 
                        address, 
                        email, 
                        logoUri?.toString(), 
                        contactNumber, 
                        abn,
                        businessInfo?.useDarkMode ?: false,
                        defaultDueDateDays.toIntOrNull() ?: 14,
                        dateFormat,
                        currencySymbol
                    )
                }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.save))
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                DataManagementSection(onManagePredefinedLineItems, onTrashClick)
            }
        }
    }
}

@Composable
fun BusinessInfoFields(
    name: String,
    onNameChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    logoUri: Uri?,
    onLogoClick: () -> Unit,
    contactNumber: String,
    onContactNumberChange: (String) -> Unit,
    abn: String,
    onAbnChange: (String) -> Unit,
    defaultDueDateDays: String,
    onDefaultDueDateDaysChange: (String) -> Unit,
    dateFormat: String,
    onDateFormatChange: (String) -> Unit,
    currencySymbol: String,
    onCurrencySymbolChange: (String) -> Unit
) {
    ThemedTextField(value = name, onValueChange = onNameChange, label = stringResource(R.string.business_name), modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    ThemedTextField(value = address, onValueChange = onAddressChange, label = stringResource(R.string.address), modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    ThemedTextField(value = email, onValueChange = onEmailChange, label = stringResource(R.string.email), modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    ThemedTextField(value = contactNumber, onValueChange = onContactNumberChange, label = stringResource(R.string.contact_number), modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    ThemedTextField(value = abn, onValueChange = onAbnChange, label = stringResource(R.string.abn_acn), modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    ThemedTextField(value = defaultDueDateDays, onValueChange = onDefaultDueDateDaysChange, label = "Default Due Date (Days)", modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    ThemedTextField(value = dateFormat, onValueChange = onDateFormatChange, label = "Date Format", modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    ThemedTextField(value = currencySymbol, onValueChange = onCurrencySymbolChange, label = "Currency Symbol", modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.clickable(onClick = onLogoClick), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.business_logo))
        Spacer(modifier = Modifier.weight(1f))
        AsyncImage(
            model = logoUri,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            fallback = painterResource(id = R.drawable.thswalogo)
        )
    }
}

@Composable
fun DataManagementSection(
    onManagePredefinedLineItems: () -> Unit,
    onTrashClick: () -> Unit,
    businessInfoViewModel: BusinessInfoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    val backupLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val dbFile = context.getDatabasePath(DATABASE_NAME)
                    val walFile = File(dbFile.path + "-wal")
                    val shmFile = File(dbFile.path + "-shm")

                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            dbFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        // Delete old journal files
                        if (walFile.exists()) {
                            walFile.delete()
                        }
                        if (shmFile.exists()) {
                            shmFile.delete()
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Restore successful. Please restart the app.", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Restore failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to clear all data? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    businessInfoViewModel.clearAllData()
                    showClearDataDialog = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.manage_pre_filled_items)) },
                modifier = Modifier.clickable { onManagePredefinedLineItems() }
            )
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.trash)) },
                modifier = Modifier.clickable { onTrashClick() }
            )
            Box {
                ListItem(
                    headlineContent = { Text(stringResource(id = R.string.backup_data)) },
                    modifier = Modifier.clickable { expanded = true },
                    trailingContent = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.backup_data)) },
                        onClick = {
                            coroutineScope.launch {
                                withContext(Dispatchers.IO) {
                                    val dbFile = context.getDatabasePath(DATABASE_NAME)
                                    val backupDir = File(context.getExternalFilesDir(null), "backups").apply { mkdirs() }
                                    val backupFile = File(backupDir, context.getString(R.string.backup_file_name))
                                    dbFile.copyTo(backupFile, overwrite = true)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Backup successful", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.restore_data)) },
                        onClick = {
                            backupLauncher.launch("*/*")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.clear_all_data)) },
                        onClick = {
                            showClearDataDialog = true
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
