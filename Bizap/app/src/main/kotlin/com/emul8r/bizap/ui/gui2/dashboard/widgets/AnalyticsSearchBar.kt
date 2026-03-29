package com.emul8r.bizap.ui.gui2.dashboard.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emul8r.bizap.domain.analytics.SearchQuery
import com.emul8r.bizap.domain.analytics.SearchResult
import com.emul8r.bizap.domain.analytics.SearchType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Analytics Search Bar for searching invoices and customers.
 *
 * Features:
 * - Real-time search as user types
 * - Search across invoices and customers
 * - Debounced queries (300ms delay)
 * - Up to 10 results displayed
 * - Clickable results with navigation callback
 * - Material 3 design
 *
 * @param onSearch Callback when user searches (receives query)
 * @param onResultClick Callback when user clicks a result
 * @param searchResults Current search results to display
 * @param isLoading Whether a search is in progress
 * @param modifier Optional modifier
 */
@Composable
fun AnalyticsSearchBar(
    onSearch: (SearchQuery) -> Unit,
    onResultClick: (SearchResult) -> Unit,
    searchResults: List<SearchResult> = emptyList(),
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        // Search Field
        SearchTextField(
            query = query,
            onQueryChange = { newQuery ->
                query = newQuery
                isExpanded = newQuery.trim().isNotEmpty()

                // Cancel previous job
                searchJob?.cancel()

                // Debounce search (300ms)
                searchJob = scope.launch {
                    delay(300)
                    if (query.trim().isNotEmpty()) {
                        onSearch(SearchQuery(query, SearchType.ALL))
                    }
                }
            },
            onClear = {
                query = ""
                isExpanded = false
                searchJob?.cancel()
            }
        )

        // Search Results Dropdown
        AnimatedVisibility(
            visible = isExpanded && (searchResults.isNotEmpty() || isLoading),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxWidth()
        ) {
            SearchResultsDropdown(
                results = searchResults,
                isLoading = isLoading,
                onResultClick = { result ->
                    onResultClick(result)
                    query = ""
                    isExpanded = false
                }
            )
        }
    }
}

/**
 * Search text input field.
 *
 * Material 3 styled search field with clear button.
 */
@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        placeholder = { Text("Search invoices & customers...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

/**
 * Dropdown displaying search results.
 *
 * Shows up to 10 results in a scrollable list.
 */
@Composable
private fun SearchResultsDropdown(
    results: List<SearchResult>,
    isLoading: Boolean,
    onResultClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        when {
            isLoading -> {
                // Loading state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            results.isEmpty() -> {
                // No results state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No results found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            else -> {
                // Results list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(
                        items = results.take(10),  // Limit to 10 results
                        key = { it.id }
                    ) { result ->
                        SearchResultItem(
                            result = result,
                            onClick = { onResultClick(result) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual search result item.
 *
 * Displays icon, title, subtitle, and result type.
 */
@Composable
private fun SearchResultItem(
    result: SearchResult,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            imageVector = when (result.type) {
                SearchType.INVOICE -> Icons.Default.Receipt
                SearchType.CUSTOMER -> Icons.Default.Person
                SearchType.ALL -> Icons.Default.Search
            },
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when (result.type) {
                        SearchType.INVOICE -> MaterialTheme.colorScheme.primaryContainer
                        SearchType.CUSTOMER -> MaterialTheme.colorScheme.secondaryContainer
                        SearchType.ALL -> MaterialTheme.colorScheme.tertiaryContainer
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(6.dp),
            tint = when (result.type) {
                SearchType.INVOICE -> MaterialTheme.colorScheme.primary
                SearchType.CUSTOMER -> MaterialTheme.colorScheme.secondary
                SearchType.ALL -> MaterialTheme.colorScheme.tertiary
            }
        )

        // Text Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = result.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = result.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Type Badge
        Surface(
            modifier = Modifier,
            color = when (result.type) {
                SearchType.INVOICE -> MaterialTheme.colorScheme.primaryContainer
                SearchType.CUSTOMER -> MaterialTheme.colorScheme.secondaryContainer
                SearchType.ALL -> MaterialTheme.colorScheme.tertiaryContainer
            },
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = when (result.type) {
                    SearchType.INVOICE -> "Invoice"
                    SearchType.CUSTOMER -> "Customer"
                    SearchType.ALL -> "Other"
                },
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                color = when (result.type) {
                    SearchType.INVOICE -> MaterialTheme.colorScheme.onPrimaryContainer
                    SearchType.CUSTOMER -> MaterialTheme.colorScheme.onSecondaryContainer
                    SearchType.ALL -> MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }
    }

    HorizontalDivider(modifier = Modifier.padding(horizontal = 12.dp))
}
