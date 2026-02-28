package com.example.databaser.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun SettingsScreen(
    onBusinessInfoSaved: () -> Unit,
    onManagePredefinedLineItems: () -> Unit,
    onTrashClick: () -> Unit,
    businessInfoViewModel: BusinessInfoViewModel = viewModel(factory = BusinessInfoViewModel.Factory)
) {
    val businessInfo by businessInfoViewModel.businessInfo.collectAsState(initial = null)
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var contactNumber by remember { mutableStateOf("") }
    var abn by remember { mutableStateOf("") }
    var useDarkMode by remember { mutableStateOf(false) }
    var defaultDueDateDays by remember { mutableStateOf("") }
    var dateFormat by remember { mutableStateOf("") }
    var currencySymbol by remember { mutableStateOf("") }

    LaunchedEffect(businessInfo) {
        businessInfo?.let {
            name = it.name ?: ""
            address = it.address ?: ""
            email = it.email ?: ""
            logoUri = it.logoPath?.let { uriString -> Uri.parse(uriString) }
            contactNumber = it.contactNumber
            abn = it.abn
            useDarkMode = it.useDarkMode
            defaultDueDateDays = it.defaultDueDateDays.toString()
            dateFormat = it.dateFormat
            currencySymbol = it.currencySymbol
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { logoUri = it }
    }

    Scaffold {
        Column(modifier = Modifier.padding(it).padding(16.dp)) {
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
                useDarkMode = useDarkMode,
                onUseDarkModeChange = { useDarkMode = it },
                defaultDueDateDays = defaultDueDateDays,
                onDefaultDueDateDaysChange = { defaultDueDateDays = it },
                dateFormat = dateFormat,
                onDateFormatChange = { dateFormat = it },
                currencySymbol = currencySymbol,
                onCurrencySymbolChange = { currencySymbol = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                businessInfoViewModel.saveBusinessInfo(
                    name, 
                    address, 
                    email, 
                    logoUri?.toString(), 
                    contactNumber, 
                    abn,
                    useDarkMode, 
                    defaultDueDateDays.toIntOrNull() ?: 14,
                    dateFormat,
                    currencySymbol
                )
                onBusinessInfoSaved()
            }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.save))
            }
            Spacer(modifier = Modifier.height(16.dp))
            DataManagementSection(onManagePredefinedLineItems, onTrashClick)
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
    useDarkMode: Boolean,
    onUseDarkModeChange: (Boolean) -> Unit,
    defaultDueDateDays: String,
    onDefaultDueDateDaysChange: (String) -> Unit,
    dateFormat: String,
    onDateFormatChange: (String) -> Unit,
    currencySymbol: String,
    onCurrencySymbolChange: (String) -> Unit
) {
    TextField(value = name, onValueChange = onNameChange, label = { Text(stringResource(R.string.business_name)) }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = address, onValueChange = onAddressChange, label = { Text(stringResource(R.string.address)) }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = email, onValueChange = onEmailChange, label = { Text(stringResource(R.string.email)) }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = contactNumber, onValueChange = onContactNumberChange, label = { Text(stringResource(R.string.contact_number)) }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = abn, onValueChange = onAbnChange, label = { Text(stringResource(R.string.abn_acn)) }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = defaultDueDateDays, onValueChange = onDefaultDueDateDaysChange, label = { Text("Default Due Date (Days)") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = dateFormat, onValueChange = onDateFormatChange, label = { Text("Date Format") }, modifier = Modifier.fillMaxWidth())
    Spacer(modifier = Modifier.height(8.dp))
    TextField(value = currencySymbol, onValueChange = onCurrencySymbolChange, label = { Text("Currency Symbol") }, modifier = Modifier.fillMaxWidth())
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
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Use Dark Mode")
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = useDarkMode, onCheckedChange = onUseDarkModeChange)
    }
}

@Composable
fun DataManagementSection(
    onManagePredefinedLineItems: () -> Unit,
    onTrashClick: () -> Unit,
    businessInfoViewModel: BusinessInfoViewModel = viewModel(factory = BusinessInfoViewModel.Factory)
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
        businessInfoViewModel.clearAllData()
        showClearDataDialog = false
    }

    Card(modifier = Modifier.fillMaxWidth()) {
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
