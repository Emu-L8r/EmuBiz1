package com.emul8r.bizap.ui.gui2.customers.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emul8r.bizap.domain.model.Customer

/**
 * Dense customer list for Compact mode using [CompactCustomerItem].
 */
@Composable
fun CompactCustomerList(
    customers: List<Customer>,
    onCustomerClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(customers, key = { it.id }) { customer ->
            CompactCustomerItem(
                customer = customer,
                onClick = { onCustomerClick(customer.id) }
            )
            HorizontalDivider()
        }
    }
}
