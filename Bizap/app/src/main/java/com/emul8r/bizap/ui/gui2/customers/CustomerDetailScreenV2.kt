package com.emul8r.bizap.ui.gui2.customers

import androidx.compose.runtime.Composable
import com.emul8r.bizap.ui.customers.CustomerDetailScreen
import com.emul8r.bizap.ui.landing.GuiMode

@Composable
fun CustomerDetailScreenV2(
    businessId: Long,
    customerId: Long,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    CustomerDetailScreen(
        guiMode = GuiMode.GUI2,
        customerId = customerId,
        businessId = businessId,
        onEdit = onEdit,
        onBack = onBack
    )
}
