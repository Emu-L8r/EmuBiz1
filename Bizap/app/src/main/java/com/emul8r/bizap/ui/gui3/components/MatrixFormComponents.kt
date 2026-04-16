package com.emul8r.bizap.ui.gui3.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.ui.gui3.theme.MatrixBlack
import com.emul8r.bizap.ui.gui3.theme.MatrixGreen
import com.emul8r.bizap.ui.gui3.theme.MatrixGreenBright
import com.emul8r.bizap.ui.gui3.theme.MatrixSurface
import com.emul8r.bizap.ui.theme.Spacing

/**
 * PHASE 1B: Matrix Form Components Library
 *
 * Reusable building blocks for GUI3 forms (Customer, Invoice, Payment, etc.)
 * Single source of truth for Matrix aesthetic in forms.
 *
 * Features:
 * - Monospace font throughout
 * - Green borders and glows
 * - Consistent spacing and sizing
 * - Error handling with Matrix styling
 * - Loading states
 * - Cascade-aware interactions (optional)
 *
 * Usage:
 * ```kotlin
 * MatrixTextField(
 *     value = customerName,
 *     onValueChange = { name = it },
 *     label = "CUSTOMER NAME",
 *     error = nameError
 * )
 * ```
 *
 * @see CreateCustomerScreenV3 for usage example
 * @see CreateInvoiceScreenV3 for usage example
 */

/**
 * Matrix-themed text input field
 *
 * Features:
 * - Monospace font
 * - Green border with subtle glow
 * - Error state with red border
 * - Focus animation
 * - Full width by default
 *
 * @param value Current text value
 * @param onValueChange Callback when text changes
 * @param label Field label (displayed above)
 * @param error Error message (displayed below if present)
 * @param keyboardType Keyboard type (default: Text)
 * @param imeAction IME action (default: Next)
 * @param isFocused Whether field is focused (for manual control)
 * @param modifier Additional modifier
 */
@Composable
fun MatrixTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    var isFocused by remember { mutableStateOf(false) }

    val borderColor = when {
        error != null -> Color(0xFFFF0000).copy(alpha = 0.8f)
        isFocused -> MatrixGreenBright
        else -> MatrixGreen.copy(alpha = 0.6f)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MatrixGreen.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = MatrixGreen,
                fontSize = 13.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = borderColor,
                focusedBorderColor = MatrixGreenBright,
                unfocusedContainerColor = MatrixBlack.copy(alpha = 0.9f),
                focusedContainerColor = MatrixBlack.copy(alpha = 0.95f),
                cursorColor = MatrixGreenBright
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            )
        )

        if (error != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    modifier = Modifier.size(14.dp),
                    tint = Color(0xFFFF0000).copy(alpha = 0.8f)
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFFFF0000).copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}

@Composable
fun <T> MatrixDropdown(
    modifier: Modifier = Modifier,
    items: List<T>,
    selectedItem: T?,
    onSelect: (T) -> Unit,
    itemLabel: (T) -> String,
    placeholder: String = "SELECT..."
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MatrixBlack.copy(alpha = 0.9f))
                .border(1.dp, MatrixGreen.copy(alpha = 0.6f))
                .clickable { expanded = !expanded }
                .padding(12.dp)
        ) {
            Text(
                text = selectedItem?.let { itemLabel(it) } ?: placeholder,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    color = if (selectedItem != null) MatrixGreen else MatrixGreen.copy(alpha = 0.5f),
                    fontSize = 13.sp
                )
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MatrixSurface)
                    .border(1.dp, MatrixGreen.copy(alpha = 0.3f))
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelect(item)
                                expanded = false
                            }
                            .padding(12.dp)
                    ) {
                        Text(
                            itemLabel(item),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MatrixGreen,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Matrix-themed action button for forms
 *
 * Features:
 * - Full width by default
 * - Monospace font
 * - Green color with glow
 * - Loading spinner support
 * - Disabled state
 *
 * @param text Button text
 * @param onClick Callback when clicked
 * @param isLoading Show loading spinner
 * @param isEnabled Enable/disable button
 * @param modifier Additional modifier
 */
@Composable
fun MatrixFormButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = isEnabled && !isLoading,
        modifier = modifier
            .height(52.dp)
            .border(1.dp, MatrixGreen, shape = MaterialTheme.shapes.small)
            .background(
                if (isEnabled) MatrixGreen.copy(alpha = 0.15f)
                else MatrixGreen.copy(alpha = 0.05f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnabled) MatrixGreen.copy(alpha = 0.15f)
            else MatrixGreen.copy(alpha = 0.05f),
            contentColor = MatrixGreen,
            disabledContainerColor = MatrixGreen.copy(alpha = 0.05f),
            disabledContentColor = MatrixGreen.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.small
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MatrixGreen,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
    }
}

/**
 * Matrix form section container
 *
 * Provides consistent styling for form sections with:
 * - Title bar with green accent
 * - Content area with spacing
 * - Bordered container
 *
 * @param title Section title (e.g., ">> CUSTOMER DETAILS")
 * @param content Section content composable
 * @param modifier Additional modifier
 */
@Composable
fun MatrixFormSection(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MatrixGreen.copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
            .background(MatrixBlack.copy(alpha = 0.7f), shape = MaterialTheme.shapes.small)
            .padding(Spacing.md)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = MatrixGreenBright,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.md)
                .border(1.dp, MatrixGreen.copy(alpha = 0.3f), shape = RectangleShape)
                .padding(bottom = Spacing.sm)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            content()
        }
    }
}

/**
 * Matrix form error message display
 *
 * Shows error with Matrix styling and icon
 *
 * @param message Error message text
 * @param modifier Additional modifier
 */
@Composable
fun MatrixFormError(
    modifier: Modifier = Modifier,
    message: String
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Color(0xFFFF0000).copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.small
            )
            .border(1.dp, Color(0xFFFF0000).copy(alpha = 0.5f), shape = MaterialTheme.shapes.small)
            .padding(Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(18.dp),
            tint = Color(0xFFFF0000).copy(alpha = 0.8f)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                color = Color(0xFFFF0000).copy(alpha = 0.9f),
                fontSize = 12.sp
            )
        )
    }
}

/**
 * Matrix form loading state overlay
 *
 * Displayed while form is submitting
 *
 * @param isVisible Whether overlay is visible
 */
@Composable
fun MatrixFormLoadingOverlay(
    modifier: Modifier = Modifier,
    isVisible: Boolean
) {
    if (isVisible) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MatrixBlack.copy(alpha = 0.7f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                CircularProgressIndicator(
                    color = MatrixGreen,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    "PROCESSING...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = MatrixGreen,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

/**
 * Matrix Customer Selector Dropdown
 *
 * Reusable dropdown for selecting customers across all forms.
 * Uses ExposedDropdownMenuBox pattern (proven from CurrencySelector).
 * Binds to ViewModel state directly.
 *
 * @param value Currently selected customer
 * @param customers List of available customers from ViewModel
 * @param onSelect Callback when customer selected (calls viewModel.selectCustomer)
 * @param modifier Additional modifier
 * @param label Label for the dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixCustomerDropdown(
    value: com.emul8r.bizap.domain.model.Customer?,
    customers: List<com.emul8r.bizap.domain.model.Customer>,
    onSelect: (com.emul8r.bizap.domain.model.Customer) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "CUSTOMER *"
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        // Use OutlinedTextField directly (not MatrixTextField) to support ExposedDropdownMenuBox
        OutlinedTextField(
            value = value?.name ?: label,
            onValueChange = { },
            readOnly = true,
            label = {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MatrixGreen.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                color = MatrixGreen,
                fontSize = 13.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MatrixGreen.copy(alpha = 0.6f),
                focusedBorderColor = MatrixGreenBright,
                unfocusedContainerColor = MatrixBlack.copy(alpha = 0.9f),
                focusedContainerColor = MatrixBlack.copy(alpha = 0.95f),
                cursorColor = MatrixGreenBright
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (customers.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "No customers available",
                            color = MatrixGreen.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    },
                    onClick = { }
                )
            } else {
                customers.forEach { customer ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                customer.name,
                                color = MatrixGreen,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        },
                        onClick = {
                            onSelect(customer)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

