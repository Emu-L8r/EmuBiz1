package com.example.databaser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.util.PatternsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.databaser.R
import com.example.databaser.data.Customer
import com.example.databaser.viewmodel.CustomerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    customerId: Int?,
    customerViewModel: CustomerViewModel = hiltViewModel(),
    onCustomerSaved: () -> Unit,
    onBackClick: () -> Unit,
    navController: NavHostController
) {
    val customer by if (customerId != null && customerId != -1) {
        customerViewModel.getCustomerById(customerId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var abn by remember { mutableStateOf("") }
    var acn by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var contactNumberError by remember { mutableStateOf<String?>(null) }

    fun validateFields(): Boolean {
        nameError = if (name.isBlank()) "Name cannot be empty" else null
        addressError = if (address.isBlank()) "Address cannot be empty" else null
        emailError = if (email.isNotBlank() && !PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) "Invalid email format" else null
        contactNumberError = if (contactNumber.isNotBlank() && !contactNumber.all { it.isDigit() }) "Contact number must be numeric" else null
        return nameError == null && addressError == null && emailError == null && contactNumberError == null
    }

    LaunchedEffect(customer) {
        customer?.let {
            name = it.name
            email = it.email
            address = it.address
            contactNumber = it.contactNumber
            abn = it.abn ?: ""
            acn = it.acn ?: ""
        }
    }

    Scaffold(
        topBar = {
            AppTopAppBar(
                title = stringResource(id = R.string.add_edit_customer),
                canNavigateBack = true,
                onNavigateUp = onBackClick,
                navController = navController
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Card(elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ThemedTextField(
                        value = name,
                        onValueChange = { 
                            name = it 
                            validateFields()
                        },
                        label = stringResource(id = R.string.name),
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError != null,
                        errorMessage = nameError
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            validateFields()
                         },
                        label = stringResource(id = R.string.email),
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError != null,
                        errorMessage = emailError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemedTextField(
                        value = address,
                        onValueChange = { 
                            address = it
                            validateFields()
                         },
                        label = stringResource(id = R.string.address),
                        modifier = Modifier.fillMaxWidth(),
                        isError = addressError != null,
                        errorMessage = addressError
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemedTextField(
                        value = contactNumber,
                        onValueChange = { 
                            contactNumber = it 
                            validateFields()
                        },
                        label = stringResource(id = R.string.contact_number),
                        modifier = Modifier.fillMaxWidth(),
                        isError = contactNumberError != null,
                        errorMessage = contactNumberError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemedTextField(
                        value = abn,
                        onValueChange = { abn = it },
                        label = "ABN",
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ThemedTextField(
                        value = acn,
                        onValueChange = { acn = it },
                        label = "ACN",
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (validateFields()) {
                        coroutineScope.launch {
                            val updatedCustomer = Customer(
                                id = if (customerId == null || customerId == -1) 0 else customerId,
                                name = name,
                                email = email,
                                address = address,
                                contactNumber = contactNumber,
                                abn = abn,
                                acn = acn,
                            )
                            if (customerId == null || customerId == -1) {
                                val newCustomerId = customerViewModel.addCustomer(updatedCustomer)
                                navController.previousBackStackEntry?.savedStateHandle?.set("newCustomerId", newCustomerId.toInt())
                            } else {
                                customerViewModel.updateCustomer(updatedCustomer)
                            }
                            onCustomerSaved()
                        }
                    }
                },
                enabled = name.isNotBlank() && address.isNotBlank() && emailError == null && contactNumberError == null
            ) {
                Text(stringResource(id = R.string.save))
            }
        }
    }
}
