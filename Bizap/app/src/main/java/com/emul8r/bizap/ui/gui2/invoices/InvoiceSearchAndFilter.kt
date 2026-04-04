package com.emul8r.bizap.ui.gui2.invoices

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.emul8r.bizap.domain.model.InvoiceStatusConstants
import timber.log.Timber

/**
 * WIN #11: Smart Search & Filter for Invoices
 *
 * Features:
 * - Full-text search (invoice #, customer name, amount)
 * - Multi-select status filters
 * - Date range filter
 * - Amount range filter
 * - Smart sorting (newest, due soon, overdue)
 * - Live results with debouncing
 *
 * Find any invoice in <1 second!
 *
 * Usage:
 * InvoiceSearchAndFilter(
 *     onSearch = { query -> ... },
 *     onFilterChange = { filters -> ... }
 * )
 */

// Data class for search query
data class InvoiceSearchQuery(
    val searchText: String = "",
    val statusFilter: List<String> = emptyList(),
    val dateRangeFilter: DateRange? = null,
    val amountRangeFilter: LongRange? = null,
    val sortBy: SortOption = SortOption.NEWEST
)

// Date range filter
data class DateRange(
    val startDate: Long,  // ms
    val endDate: Long     // ms
)

// Sort options
enum class SortOption {
    NEWEST, OLDEST, HIGHEST_AMOUNT, LOWEST_AMOUNT, DUE_SOON, OVERDUE_FIRST
}

@Composable
fun InvoiceSearchAndFilter(
    onSearch: (InvoiceSearchQuery) -> Unit,
    onFilterChange: (InvoiceSearchQuery) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var selectedStatuses by remember { mutableStateOf<List<String>>(emptyList()) }
    var sortBy by remember { mutableStateOf(SortOption.NEWEST) }
    var showAdvanced by remember { mutableStateOf(false) }

    // Debounce search
    LaunchedEffect(searchText) {
        kotlinx.coroutines.delay(500)  // 500ms debounce
        val query = InvoiceSearchQuery(
            searchText = searchText,
            statusFilter = selectedStatuses,
            sortBy = sortBy
        )
        Timber.d("InvoiceSearchAndFilter: search='$searchText' statuses=$selectedStatuses sort=$sortBy")
        onSearch(query)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search bar
        SearchBarWithFilters(
            searchText = searchText,
            onSearchChange = { searchText = it },
            sortBy = sortBy,
            onSortChange = { sortBy = it },
            showAdvanced = showAdvanced,
            onToggleAdvanced = { showAdvanced = !showAdvanced }
        )

        // Status filter chips
        if (searchText.isNotEmpty() || selectedStatuses.isNotEmpty()) {
            StatusFilterChips(
                selectedStatuses = selectedStatuses,
                onStatusToggle = { status ->
                    selectedStatuses = if (status in selectedStatuses) {
                        selectedStatuses - status
                    } else {
                        selectedStatuses + status
                    }
                    val query = InvoiceSearchQuery(
                        searchText = searchText,
                        statusFilter = selectedStatuses,
                        sortBy = sortBy
                    )
                    onFilterChange(query)
                }
            )
        }

        // Advanced filters (optional expansion)
        if (showAdvanced) {
            AdvancedFilters(
                onApply = { dateRange, amountRange ->
                    val query = InvoiceSearchQuery(
                        searchText = searchText,
                        statusFilter = selectedStatuses,
                        dateRangeFilter = dateRange,
                        amountRangeFilter = amountRange,
                        sortBy = sortBy
                    )
                    onFilterChange(query)
                }
            )
        }
    }
}

@Composable
private fun SearchBarWithFilters(
    searchText: String,
    onSearchChange: (String) -> Unit,
    sortBy: SortOption,
    onSortChange: (SortOption) -> Unit,
    showAdvanced: Boolean,
    onToggleAdvanced: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search field
        TextField(
            value = searchText,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .height(40.dp),
            placeholder = { Text("Search by #, customer, $") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            textStyle = MaterialTheme.typography.labelSmall,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        // Sort dropdown
        SortDropdown(
            sortBy = sortBy,
            onSortChange = onSortChange
        )

        // Advanced filters button
        IconButton(
            onClick = onToggleAdvanced,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Default.Settings,  // Use Settings icon as filter icon
                contentDescription = "Advanced filters",
                tint = if (showAdvanced) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SortDropdown(
    sortBy: SortOption,
    onSortChange: (SortOption) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(
            onClick = { showMenu = true },
            modifier = Modifier.height(40.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                sortBy.displayName,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            SortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.displayName) },
                    onClick = {
                        onSortChange(option)
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusFilterChips(
    selectedStatuses: List<String>,
    onStatusToggle: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            InvoiceStatusConstants.DRAFT,
            InvoiceStatusConstants.SENT,
            InvoiceStatusConstants.PAID,
            InvoiceStatusConstants.OVERDUE,
            InvoiceStatusConstants.PARTIALLY_PAID
        ).forEach { status ->
            FilterChip(
                selected = status in selectedStatuses,
                onClick = { onStatusToggle(status) },
                label = { Text(InvoiceStatusConstants.getDisplayName(status)) },
                leadingIcon = if (status in selectedStatuses) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}

@Composable
private fun AdvancedFilters(
    onApply: (DateRange?, LongRange?) -> Unit
) {
    var startDateText by remember { mutableStateOf("") }
    var endDateText by remember { mutableStateOf("") }
    var minAmountText by remember { mutableStateOf("") }
    var maxAmountText by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Advanced Filters",
                style = MaterialTheme.typography.titleSmall
            )

            // ===== DATE RANGE =====
            Text(
                "Date Range (DD/MM/YYYY)",
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDateText,
                    onValueChange = { startDateText = it; dateError = null },
                    label = { Text("From") },
                    placeholder = { Text("01/01/2026") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = dateError != null
                )
                OutlinedTextField(
                    value = endDateText,
                    onValueChange = { endDateText = it; dateError = null },
                    label = { Text("To") },
                    placeholder = { Text("31/12/2026") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = dateError != null
                )
            }
            if (dateError != null) {
                Text(
                    dateError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // ===== AMOUNT RANGE =====
            Text(
                "Amount Range (\$)",
                style = MaterialTheme.typography.labelMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = minAmountText,
                    onValueChange = { minAmountText = it; amountError = null },
                    label = { Text("Min \$") },
                    placeholder = { Text("0") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError != null
                )
                OutlinedTextField(
                    value = maxAmountText,
                    onValueChange = { maxAmountText = it; amountError = null },
                    label = { Text("Max \$") },
                    placeholder = { Text("10000") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = amountError != null
                )
            }
            if (amountError != null) {
                Text(
                    amountError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Apply / Clear buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = {
                        startDateText = ""
                        endDateText = ""
                        minAmountText = ""
                        maxAmountText = ""
                        dateError = null
                        amountError = null
                        onApply(null, null)
                    }
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = {
                        val dateRange = parseDateRange(startDateText, endDateText)
                            .also { (range, error) -> dateError = error }
                            .first
                        val amountRange = parseAmountRange(minAmountText, maxAmountText)
                            .also { (range, error) -> amountError = error }
                            .first
                        if (dateError == null && amountError == null) {
                            onApply(dateRange, amountRange)
                        }
                    }
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}

/** Parses DD/MM/YYYY text into a ms timestamp, or null if blank/invalid. */
private fun parseDdMmYyyy(text: String): Long? {
    if (text.isBlank()) return null
    return try {
        val parts = text.trim().split("/")
        if (parts.size != 3) return null
        val day = parts[0].toInt()
        val month = parts[1].toInt() - 1  // Calendar months are 0-based
        val year = parts[2].toInt()
        java.util.Calendar.getInstance().apply {
            set(year, month, day, 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    } catch (e: Exception) {
        null
    }
}

/** Returns (DateRange?, errorMessage?) */
private fun parseDateRange(startText: String, endText: String): Pair<DateRange?, String?> {
    if (startText.isBlank() && endText.isBlank()) return null to null
    val start = parseDdMmYyyy(startText) ?: return null to "Invalid start date (use DD/MM/YYYY)"
    val end = parseDdMmYyyy(endText) ?: return null to "Invalid end date (use DD/MM/YYYY)"
    if (start > end) return null to "Start date must be before end date"
    return DateRange(start, end) to null
}

/** Returns (LongRange?, errorMessage?) where range is in cents */
private fun parseAmountRange(minText: String, maxText: String): Pair<LongRange?, String?> {
    if (minText.isBlank() && maxText.isBlank()) return null to null
    val min = minText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: if (minText.isBlank()) 0L else return null to "Invalid min amount"
    val max = maxText.toDoubleOrNull()?.let { (it * 100).toLong() } ?: if (maxText.isBlank()) Long.MAX_VALUE else return null to "Invalid max amount"
    if (min > max) return null to "Min amount must be ≤ max amount"
    return (min..max) to null
}

// Helper extension for sort option display
val SortOption.displayName: String
    get() = when (this) {
        SortOption.NEWEST -> "Newest"
        SortOption.OLDEST -> "Oldest"
        SortOption.HIGHEST_AMOUNT -> "Highest Amount"
        SortOption.LOWEST_AMOUNT -> "Lowest Amount"
        SortOption.DUE_SOON -> "Due Soon"
        SortOption.OVERDUE_FIRST -> "Overdue First"
    }


