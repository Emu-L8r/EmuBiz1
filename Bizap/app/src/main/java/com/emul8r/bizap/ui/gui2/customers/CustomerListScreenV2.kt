package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.runtime.Composable
import com.emul8r.bizap.ui.customers.CustomerListScreen
import com.emul8r.bizap.ui.landing.GuiMode

@Composable
fun CustomerListScreenV2(
    businessId: Long,
    onCustomerClick: (Long) -> Unit,
    onCreateCustomer: () -> Unit,
    onBack: () -> Unit
) {
    CustomerListScreen(
        guiMode = GuiMode.GUI2,
        businessId = businessId,
        onCustomerClick = onCustomerClick,
        onCreateCustomer = onCreateCustomer,
        onBack = onBack
    )
}
