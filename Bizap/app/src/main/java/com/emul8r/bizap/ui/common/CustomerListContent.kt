package com.emul8r.bizap.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.Customer

/**
 * Shared customer list composable used by both GUI1 and GUI2.
 *
 * Renders a lazy scrollable list of [Customer] items.  Each row calls
 * [onCustomerClick] with the customer ID when tapped.
 *
 * This component is **display-only**: it does not own a ViewModel.
 * Pass pre-loaded data from whichever ViewModel is driving the screen.
 *
 * **Usage:**
 * ```kotlin
 * CustomerListContent(
 *     customers = customerState.customers,
 *     onCustomerClick = { id -> navController.navigate("customer/$id") }
 * )
 * ```
 */
@Composable
fun CustomerListContent(
    customers: List<Customer>,
    onCustomerClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(customers, key = { it.id }) { customer ->
            CustomerListRow(customer = customer, onClick = { onCustomerClick(customer.id) })
        }
    }
}

/**
 * Single row in a customer list.
 *
 * Extracted as a standalone composable so it can be previewed and
 * reused independently of [CustomerListContent].
 */
@Composable
fun CustomerListRow(
    customer: Customer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                customer.businessName?.let { biz ->
                    if (biz.isNotBlank()) {
                        Text(
                            text = biz,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                customer.email?.let { email ->
                    if (email.isNotBlank()) {
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Empty-state placeholder shown when a customer list has no items.
 */
@Composable
fun EmptyCustomerList(
    message: String = "No customers found",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
